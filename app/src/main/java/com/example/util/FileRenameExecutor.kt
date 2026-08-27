/*
 * File: FileRenameExecutor.kt
 * Purpose: Execution engine for SAF DocumentFile, MediaStore Scoped Storage, and CSV log exporting
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.util

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.example.data.model.FileItem
import com.example.data.model.RenameLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

sealed class RenameAttemptResult {
    data class Success(val newUri: Uri?, val newName: String) : RenameAttemptResult()
    data class NeedPermission(val intentSender: IntentSender, val uris: List<Uri>) : RenameAttemptResult()
    data class Failed(val reason: String, val throwable: Throwable? = null) : RenameAttemptResult()
}

object FileRenameExecutor {

    private const val TAG = "FileRenameExecutor"

    suspend fun executeSingleRename(
        context: Context,
        item: FileItem,
        newName: String
    ): RenameAttemptResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting rename for '${item.oldName}' -> '$newName' [URI: ${item.uri}, authority: ${item.uri.authority}]")

        // Strategy 1: If it's a SAF Document URI (from OpenDocumentTree or OpenMultipleDocuments), try DocumentsContract.renameDocument
        if (DocumentsContract.isDocumentUri(context, item.uri)) {
            try {
                val newUri = DocumentsContract.renameDocument(context.contentResolver, item.uri, newName)
                if (newUri != null) {
                    Log.d(TAG, "DocumentsContract.renameDocument succeeded: ${item.oldName} -> $newName (new URI: $newUri)")
                    return@withContext RenameAttemptResult.Success(newUri = newUri, newName = newName)
                }
            } catch (rse: RecoverableSecurityException) {
                Log.i(TAG, "RecoverableSecurityException caught in DocumentsContract for ${item.oldName}")
                return@withContext RenameAttemptResult.NeedPermission(
                    intentSender = rse.userAction.actionIntent.intentSender,
                    uris = listOf(item.uri)
                )
            } catch (sec: SecurityException) {
                Log.w(TAG, "SecurityException in DocumentsContract for ${item.oldName}: ${sec.message}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val sender = getWriteRequestIntentSender(context, listOf(item.uri))
                    if (sender != null) {
                        return@withContext RenameAttemptResult.NeedPermission(intentSender = sender, uris = listOf(item.uri))
                    }
                }
            } catch (unsupported: UnsupportedOperationException) {
                Log.w(TAG, "DocumentsContract.renameDocument unsupported for ${item.oldName}: ${unsupported.message}")
            } catch (e: Exception) {
                Log.w(TAG, "DocumentsContract.renameDocument failed for ${item.oldName}: ${e.message}")
            }
        }

        // Strategy 2: MediaStore Document Resolution & MediaStore Update
        var mediaStoreUri: Uri? = null
        if (item.uri.authority == "com.android.providers.media.documents") {
            try {
                val docId = DocumentsContract.getDocumentId(item.uri)
                val split = docId.split(":")
                if (split.size == 2) {
                    val type = split[0].lowercase()
                    val id = split[1]
                    mediaStoreUri = when (type) {
                        "image" -> Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        "video" -> Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                        "audio" -> Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        else -> MediaStore.Files.getContentUri("external", id.toLong())
                    }
                    Log.d(TAG, "Resolved media document ID '$docId' to MediaStore URI: $mediaStoreUri")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed resolving media document ID for ${item.oldName}: ${e.message}")
            }
        } else if (item.isMediaStore || item.uri.authority?.contains("media") == true) {
            mediaStoreUri = item.uri
        }

        if (mediaStoreUri != null) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
                }
                val rows = context.contentResolver.update(mediaStoreUri, values, null, null)
                if (rows > 0) {
                    Log.d(TAG, "MediaStore DISPLAY_NAME update succeeded: ${item.oldName} -> $newName")
                    return@withContext RenameAttemptResult.Success(newUri = mediaStoreUri, newName = newName)
                } else {
                    Log.w(TAG, "MediaStore update returned 0 rows for ${item.oldName}")
                }
            } catch (rse: RecoverableSecurityException) {
                Log.i(TAG, "RecoverableSecurityException on MediaStore update for ${item.oldName}")
                return@withContext RenameAttemptResult.NeedPermission(
                    intentSender = rse.userAction.actionIntent.intentSender,
                    uris = listOf(mediaStoreUri)
                )
            } catch (sec: SecurityException) {
                Log.w(TAG, "SecurityException on MediaStore update for ${item.oldName}: ${sec.message}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val sender = getWriteRequestIntentSender(context, listOf(mediaStoreUri))
                    if (sender != null) {
                        return@withContext RenameAttemptResult.NeedPermission(intentSender = sender, uris = listOf(mediaStoreUri))
                    }
                }
                return@withContext RenameAttemptResult.Failed("Write permission required for ${item.oldName}", sec)
            } catch (e: Exception) {
                Log.e(TAG, "MediaStore rename failed for ${item.oldName}: ${e.message}", e)
            }
        }

        // Strategy 3: Tree Document File (when folder was picked via OpenDocumentTree)
        if (item.isTreeDocument) {
            try {
                val docFile = DocumentFile.fromSingleUri(context, item.uri)
                if (docFile != null && docFile.exists()) {
                    val renamed = docFile.renameTo(newName)
                    if (renamed) {
                        Log.d(TAG, "Tree DocumentFile.renameTo succeeded: ${item.oldName} -> $newName")
                        return@withContext RenameAttemptResult.Success(newUri = docFile.uri, newName = newName)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Tree DocumentFile rename error for ${item.oldName}: ${e.message}")
            }
        }

        // Strategy 4: Direct file path on disk (if available)
        if (item.localFilePath != null || item.uri.scheme == "file") {
            try {
                val path = item.localFilePath ?: item.uri.path
                if (path != null) {
                    val oldFile = File(path)
                    if (oldFile.exists()) {
                        val newFile = File(oldFile.parentFile, newName)
                        if (oldFile.renameTo(newFile)) {
                            Log.d(TAG, "Direct File.renameTo succeeded: ${oldFile.name} -> ${newFile.name}")
                            return@withContext RenameAttemptResult.Success(newUri = Uri.fromFile(newFile), newName = newName)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Direct file rename error for ${item.oldName}: ${e.message}", e)
            }
        }

        val failMsg = "Unable to rename '${item.oldName}'. Please try selecting files with 'Pick Folder' for full folder write access."
        Log.e(TAG, "All rename strategies exhausted for '${item.oldName}' (URI: ${item.uri})")
        RenameAttemptResult.Failed(reason = failMsg)
    }

    fun getWriteRequestIntentSender(context: Context, uris: List<Uri>): IntentSender? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createWriteRequest(context.contentResolver, uris)
                pendingIntent.intentSender
            } catch (e: Exception) {
                Log.e(TAG, "Failed creating write request: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }

    suspend fun exportHistoryToCsv(
        context: Context,
        historyLogs: List<RenameLogEntry>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val logsDir = File(context.getExternalFilesDir(null), "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            val timestamp = System.currentTimeMillis()
            val file = File(logsDir, "bulk_rename_log_${timestamp}.csv")
            FileWriter(file).use { writer ->
                writer.append("timestamp,old_name,new_name\n")
                val isoDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
                historyLogs.forEach { entry ->
                    val cleanOld = entry.oldName.replace("\"", "\"\"")
                    val cleanNew = entry.newName.replace("\"", "\"\"")
                    writer.append("$isoDate,\"$cleanOld\",\"$cleanNew\"\n")
                }
                writer.flush()
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export CSV log: ${e.message}", e)
            null
        }
    }

    fun createShareLogIntent(context: Context, csvFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Bulk Renamer Log")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
