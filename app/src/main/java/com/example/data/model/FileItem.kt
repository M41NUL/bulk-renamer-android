/*
 * File: FileItem.kt
 * Purpose: Data models representing selectable files, metadata, and preview states
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.model

import android.net.Uri

enum class FileType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOC;

    companion object {
        fun fromMimeOrExtension(mimeType: String?, filename: String): FileType {
            if (!mimeType.isNullOrBlank()) {
                val mime = mimeType.lowercase()
                if (mime.startsWith("image/")) return IMAGE
                if (mime.startsWith("video/")) return VIDEO
                if (mime.startsWith("audio/")) return AUDIO
            }
            val ext = filename.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "svg" -> IMAGE
                "mp4", "mkv", "avi", "mov", "webm", "3gp", "flv", "wmv" -> VIDEO
                "mp3", "wav", "ogg", "m4a", "flac", "aac", "opus", "wma" -> AUDIO
                else -> DOC
            }
        }
    }
}

data class FileItem(
    val id: String,
    val uri: Uri,
    val oldName: String,
    val type: FileType,
    val size: Long = 0L,
    val dateModified: Long = System.currentTimeMillis(),
    val isChecked: Boolean = true,
    val isTreeDocument: Boolean = false,
    val isMediaStore: Boolean = false,
    val localFilePath: String? = null
)
