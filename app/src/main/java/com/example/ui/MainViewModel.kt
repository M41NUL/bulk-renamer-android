/*
 * File: MainViewModel.kt
 * Purpose: ViewModel managing file selection, rename configurations, previews, history, and presets
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui

import android.app.Application
import android.content.IntentSender
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.BulkRenamerApplication
import com.example.data.model.FileItem
import com.example.data.model.FileType
import com.example.data.model.RenameHistoryItem
import com.example.data.model.RenameLogEntry
import com.example.data.model.RenamePreset
import com.example.data.preferences.LanguageChoice
import com.example.data.preferences.ThemeChoice
import com.example.data.preferences.UserSettings
import com.example.data.repository.RenamerRepository
import com.example.engine.CaseMode
import com.example.engine.DateSource
import com.example.engine.ExtAction
import com.example.engine.RenameConfig
import com.example.engine.RenameEngine
import com.example.engine.RenameMode
import com.example.engine.SortMode
import com.example.util.FileRenameExecutor
import com.example.util.RenameAttemptResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class FilePreviewItem(
    val file: FileItem,
    val originalIndex: Int,
    val newName: String,
    val isDuplicate: Boolean,
    val isInvalid: Boolean
)

class MainViewModel(
    application: Application,
    private val repository: RenamerRepository
) : AndroidViewModel(application) {

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _renameConfig = MutableStateFlow(RenameConfig())
    val renameConfig: StateFlow<RenameConfig> = _renameConfig.asStateFlow()

    private val _typeFilter = MutableStateFlow("all")
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.NONE)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _lastSnapshot = MutableStateFlow<List<Pair<Uri, String>>?>(null)
    val hasUndo: StateFlow<Boolean> = MutableStateFlow(false)
    private val _hasUndo = hasUndo as MutableStateFlow<Boolean>

    private val _pendingIntentSender = MutableStateFlow<IntentSender?>(null)
    val pendingIntentSender: StateFlow<IntentSender?> = _pendingIntentSender.asStateFlow()

    private val _exportedCsvFile = MutableStateFlow<File?>(null)
    val exportedCsvFile: StateFlow<File?> = _exportedCsvFile.asStateFlow()

    val userSettings: StateFlow<UserSettings> = repository.userSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    val presets: StateFlow<List<RenamePreset>> = repository.presets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<RenameHistoryItem>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearExportedCsv() {
        _exportedCsvFile.value = null
    }

    fun clearPendingIntentSender() {
        _pendingIntentSender.value = null
    }

    fun setMode(mode: RenameMode) {
        _renameConfig.update { it.copy(mode = mode) }
    }

    fun updatePrefix(prefix: String) {
        _renameConfig.update { it.copy(prefix = prefix) }
    }

    fun updateStartNumber(startNumber: String) {
        _renameConfig.update { it.copy(startNumber = startNumber) }
    }

    fun toggleKeepExtension() {
        _renameConfig.update { it.copy(keepExtension = !it.keepExtension) }
    }

    fun updateFindText(findText: String) {
        _renameConfig.update { it.copy(findText = findText) }
    }

    fun updateReplaceText(replaceText: String) {
        _renameConfig.update { it.copy(replaceText = replaceText) }
    }

    fun toggleMatchCase() {
        _renameConfig.update { it.copy(matchCase = !it.matchCase) }
    }

    fun updateNumPosition(position: String) {
        _renameConfig.update { it.copy(numPosition = position) }
    }

    fun updateNumDigits(digits: String) {
        _renameConfig.update { it.copy(numDigits = digits) }
    }

    fun updateNumSeparator(separator: String) {
        _renameConfig.update { it.copy(numSeparator = separator) }
    }

    fun updateCaseMode(caseMode: CaseMode) {
        _renameConfig.update { it.copy(caseMode = caseMode) }
    }

    fun updateSuffix(suffix: String) {
        _renameConfig.update { it.copy(suffix = suffix) }
    }

    fun updateRemoveText(removeText: String) {
        _renameConfig.update { it.copy(removeText = removeText) }
    }

    fun updateRemoveFirst(removeFirst: String) {
        _renameConfig.update { it.copy(removeFirst = removeFirst) }
    }

    fun updateRemoveLast(removeLast: String) {
        _renameConfig.update { it.copy(removeLast = removeLast) }
    }

    fun updateKeepFirst(keepFirst: String) {
        _renameConfig.update { it.copy(keepFirst = keepFirst) }
    }

    fun updateInsertText(insertText: String) {
        _renameConfig.update { it.copy(insertText = insertText) }
    }

    fun updateInsertPosition(position: String) {
        _renameConfig.update { it.copy(insertPosition = position) }
    }

    fun toggleReplaceByPosition() {
        _renameConfig.update { it.copy(replaceByPosition = !it.replaceByPosition) }
    }

    fun updateExtAction(extAction: ExtAction) {
        _renameConfig.update { it.copy(extAction = extAction) }
    }

    fun updateNewExtension(newExtension: String) {
        _renameConfig.update { it.copy(newExtension = newExtension) }
    }

    fun updateDateSource(dateSource: DateSource) {
        _renameConfig.update { it.copy(dateSource = dateSource) }
    }

    fun updateDateFormat(dateFormat: String) {
        _renameConfig.update { it.copy(dateFormat = dateFormat) }
    }

    fun setTypeFilter(filter: String) {
        _typeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun toggleConfirmBeforeRename() {
        viewModelScope.launch {
            val current = userSettings.value.confirmBeforeRename
            repository.updateConfirmBeforeRename(!current)
        }
    }

    fun toggleSkipDuplicates() {
        viewModelScope.launch {
            val current = userSettings.value.skipDuplicates
            repository.updateSkipDuplicates(!current)
        }
    }

    fun updateTheme(themeChoice: ThemeChoice) {
        viewModelScope.launch {
            repository.updateTheme(themeChoice)
        }
    }

    fun updateLanguage(languageChoice: LanguageChoice) {
        viewModelScope.launch {
            repository.updateLanguage(languageChoice)
        }
    }

    fun toggleFileChecked(index: Int) {
        _files.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(isChecked = !item.isChecked) else item
            }
        }
    }

    fun toggleSelectAll() {
        val currentList = _files.value
        if (currentList.isEmpty()) return
        val allChecked = currentList.all { it.isChecked }
        _files.update { list ->
            list.map { it.copy(isChecked = !allChecked) }
        }
    }

    fun clearAllFiles() {
        _files.value = emptyList()
    }

    fun addSelectedUris(uris: List<Uri>, isTree: Boolean = false) {
        val context = getApplication<Application>().applicationContext
        val newItems = mutableListOf<FileItem>()

        uris.forEach { uri ->
            var displayName = "file_${System.currentTimeMillis()}"
            var size = 0L
            var dateModified = System.currentTimeMillis()

            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) displayName = cursor.getString(nameIdx) ?: displayName
                        if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                    }
                }
            } catch (e: Exception) {
                uri.lastPathSegment?.let { displayName = it }
            }

            val mimeType = context.contentResolver.getType(uri)
            val fileType = FileType.fromMimeOrExtension(mimeType, displayName)

            newItems.add(
                FileItem(
                    id = uri.toString(),
                    uri = uri,
                    oldName = displayName,
                    type = fileType,
                    size = size,
                    dateModified = dateModified,
                    isChecked = true,
                    isTreeDocument = isTree,
                    isMediaStore = uri.authority?.contains("media") == true
                )
            )
        }

        _files.value = newItems
    }

    fun calculatePreviews(): List<FilePreviewItem> {
        val currentFiles = _files.value
        val config = _renameConfig.value
        val allNewNames = currentFiles.mapIndexed { index, file ->
            RenameEngine.computeNewName(file, index, config)
        }

        val nameCounts = mutableMapOf<String, Int>()
        allNewNames.forEach { name ->
            nameCounts[name] = (nameCounts[name] ?: 0) + 1
        }

        return currentFiles.mapIndexed { index, file ->
            val newName = allNewNames[index]
            val isDuplicate = (nameCounts[newName] ?: 0) > 1
            val isInvalid = !RenameEngine.isValidFilename(newName)
            FilePreviewItem(
                file = file,
                originalIndex = index,
                newName = newName,
                isDuplicate = isDuplicate,
                isInvalid = isInvalid
            )
        }
    }

    fun getVisiblePreviews(): List<FilePreviewItem> {
        val previews = calculatePreviews()
        val filter = _typeFilter.value
        val query = _searchQuery.value.trim().lowercase()
        val sort = _sortMode.value

        var filtered = previews

        if (filter != "all") {
            val targetType = when (filter) {
                "image" -> FileType.IMAGE
                "video" -> FileType.VIDEO
                "audio" -> FileType.AUDIO
                else -> FileType.DOC
            }
            filtered = filtered.filter { it.file.type == targetType }
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter { it.file.oldName.lowercase().contains(query) }
        }

        return when (sort) {
            SortMode.NAME -> filtered.sortedBy { it.file.oldName.lowercase() }
            SortMode.DATE -> filtered.sortedBy { it.file.dateModified }
            SortMode.SIZE -> filtered.sortedBy { it.file.size }
            SortMode.NONE -> filtered
        }
    }

    fun performRename() {
        val context = getApplication<Application>().applicationContext
        val currentFiles = _files.value
        val selectedFiles = currentFiles.filter { it.isChecked }
        if (selectedFiles.isEmpty()) return

        _isLoading.value = true
        _lastSnapshot.value = currentFiles.map { it.uri to it.oldName }
        _hasUndo.value = true

        viewModelScope.launch {
            val config = _renameConfig.value
            val isBangla = userSettings.value.languageChoice == LanguageChoice.BANGLA
            val skipDup = userSettings.value.skipDuplicates
            val previews = calculatePreviews()
            val logEntries = mutableListOf<RenameLogEntry>()
            val updatedFiles = currentFiles.toMutableList()
            var renamedCount = 0
            var failedCount = 0
            var skippedDupCount = 0
            var skippedInvalidCount = 0
            val errorReasons = mutableListOf<String>()

            for (preview in previews) {
                if (!preview.file.isChecked) continue
                if (skipDup && preview.isDuplicate) {
                    skippedDupCount++
                    Log.d("MainViewModel", "Skipped duplicate file: ${preview.file.oldName} -> ${preview.newName}")
                    continue
                }
                if (preview.isInvalid) {
                    skippedInvalidCount++
                    Log.d("MainViewModel", "Skipped invalid file name: ${preview.file.oldName} -> ${preview.newName}")
                    continue
                }

                val targetItem = preview.file
                val newName = preview.newName
                val oldName = targetItem.oldName

                try {
                    Log.d("MainViewModel", "Executing rename: $oldName -> $newName [URI: ${targetItem.uri}]")
                    when (val result = FileRenameExecutor.executeSingleRename(context, targetItem, newName)) {
                        is RenameAttemptResult.Success -> {
                            val index = updatedFiles.indexOfFirst { it.id == targetItem.id }
                            if (index != -1) {
                                updatedFiles[index] = targetItem.copy(
                                    oldName = newName,
                                    uri = result.newUri ?: targetItem.uri
                                )
                            }
                            logEntries.add(RenameLogEntry(oldName = oldName, newName = newName, uriString = (result.newUri ?: targetItem.uri).toString()))
                            renamedCount++
                            Log.d("MainViewModel", "Successfully renamed ($renamedCount): $oldName -> $newName")
                        }
                        is RenameAttemptResult.NeedPermission -> {
                            Log.i("MainViewModel", "Requesting system write permission for: $oldName")
                            _pendingIntentSender.value = result.intentSender
                            break
                        }
                        is RenameAttemptResult.Failed -> {
                            failedCount++
                            errorReasons.add(result.reason)
                            Log.e("MainViewModel", "Failed renaming $oldName: ${result.reason}", result.throwable)
                        }
                    }
                } catch (e: Exception) {
                    failedCount++
                    errorReasons.add(e.message ?: "Unknown error")
                    Log.e("MainViewModel", "Unexpected exception renaming $oldName: ${e.message}", e)
                }
            }

            _files.value = updatedFiles
            _isLoading.value = false

            if (logEntries.isNotEmpty()) {
                val jsonArray = JSONArray()
                logEntries.forEach { entry ->
                    val obj = JSONObject()
                    obj.put("old", entry.oldName)
                    obj.put("new", entry.newName)
                    jsonArray.put(obj)
                }

                val summaryText = if (isBangla) "$renamedCount টি ফাইল রিনেম হয়েছে" else "$renamedCount files renamed"
                val historyItem = RenameHistoryItem(
                    timestamp = System.currentTimeMillis(),
                    fileCount = renamedCount,
                    summary = summaryText,
                    entriesJson = jsonArray.toString()
                )
                repository.recordHistory(historyItem)
            }

            // Build detailed feedback message for user
            val totalSkipped = skippedDupCount + skippedInvalidCount
            val feedback = when {
                renamedCount > 0 && failedCount == 0 && totalSkipped == 0 -> {
                    if (isBangla) "$renamedCount টি ফাইল সফলভাবে রিনেম হয়েছে" else "$renamedCount files renamed successfully"
                }
                renamedCount > 0 -> {
                    if (isBangla) {
                        "রিনেম হয়েছে: $renamedCount" + (if (failedCount > 0) ", ব্যর্থ: $failedCount" else "") + (if (totalSkipped > 0) ", বাদ: $totalSkipped" else "")
                    } else {
                        "Renamed: $renamedCount" + (if (failedCount > 0) ", Failed: $failedCount" else "") + (if (totalSkipped > 0) ", Skipped: $totalSkipped" else "")
                    }
                }
                failedCount > 0 -> {
                    val firstErr = errorReasons.firstOrNull() ?: "Write permission denied"
                    if (isBangla) "রিনেম ব্যর্থ হয়েছে ($failedCount টি ফাইল): $firstErr" else "Rename failed ($failedCount files): $firstErr"
                }
                totalSkipped > 0 -> {
                    if (isBangla) "কোনো ফাইল রিনেম হয়নি ($totalSkipped টি ডুপ্লিকেট/অবৈধ ফাইলের কারণে বাদ)" else "0 renamed ($totalSkipped skipped: duplicate/invalid)"
                }
                else -> {
                    if (isBangla) "0 টি ফাইল রিনেম হয়েছে" else "0 files renamed"
                }
            }
            _toastMessage.value = feedback
        }
    }

    fun undoLastRename() {
        val snapshot = _lastSnapshot.value
        val isBangla = userSettings.value.languageChoice == LanguageChoice.BANGLA
        if (snapshot == null) {
            _toastMessage.value = if (isBangla) "আনডু করার কিছু নেই" else "Nothing to undo"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val currentFiles = _files.value.toMutableList()
            var undoneCount = 0

            snapshot.forEach { (uri, originalOldName) ->
                val index = currentFiles.indexOfFirst { it.uri == uri }
                if (index != -1) {
                    val currentItem = currentFiles[index]
                    try {
                        when (val result = FileRenameExecutor.executeSingleRename(context, currentItem, originalOldName)) {
                            is RenameAttemptResult.Success -> {
                                currentFiles[index] = currentItem.copy(
                                    oldName = originalOldName,
                                    uri = result.newUri ?: currentItem.uri
                                )
                                undoneCount++
                            }
                            else -> {
                                currentFiles[index] = currentItem.copy(oldName = originalOldName)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Undo failed for ${currentItem.oldName}: ${e.message}", e)
                        currentFiles[index] = currentItem.copy(oldName = originalOldName)
                    }
                }
            }

            _files.value = currentFiles
            _lastSnapshot.value = null
            _hasUndo.value = false
            _isLoading.value = false
            _toastMessage.value = if (isBangla) "শেষ রিনেম আনডু হয়েছে" else "Last rename undone"
        }
    }

    fun exportLogAsCsv() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val historyList = history.value
            if (historyList.isEmpty()) {
                _toastMessage.value = "No rename history yet"
                return@launch
            }

            val allLogs = mutableListOf<RenameLogEntry>()
            historyList.forEach { historyItem ->
                try {
                    val jsonArray = JSONArray(historyItem.entriesJson)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        allLogs.add(
                            RenameLogEntry(
                                oldName = obj.optString("old"),
                                newName = obj.optString("new")
                            )
                        )
                    }
                } catch (e: Exception) {
                }
            }

            val csvFile = FileRenameExecutor.exportHistoryToCsv(context, allLogs)
            if (csvFile != null && csvFile.exists()) {
                _exportedCsvFile.value = csvFile
                _toastMessage.value = "Log exported"
            } else {
                _toastMessage.value = "Failed to export log"
            }
        }
    }

    fun savePreset(name: String) {
        viewModelScope.launch {
            val config = _renameConfig.value
            val preset = RenamePreset(
                name = name.ifBlank { "Preset ${presets.value.size + 1}" },
                mode = config.mode.key,
                prefix = config.prefix,
                startNumber = config.startNumber,
                keepExtension = config.keepExtension,
                findText = config.findText,
                replaceText = config.replaceText,
                matchCase = config.matchCase,
                numPosition = config.numPosition,
                numDigits = config.numDigits,
                numSeparator = config.numSeparator,
                caseMode = config.caseMode.key,
                suffix = config.suffix,
                removeText = config.removeText,
                removeFirst = config.removeFirst,
                removeLast = config.removeLast,
                keepFirst = config.keepFirst,
                insertText = config.insertText,
                insertPosition = config.insertPosition,
                replaceByPosition = config.replaceByPosition,
                extAction = config.extAction.key,
                newExtension = config.newExtension,
                dateSource = config.dateSource.key,
                dateFormat = config.dateFormat
            )
            repository.savePreset(preset)
            _toastMessage.value = "Preset saved"
        }
    }

    fun loadPreset(preset: RenamePreset) {
        _renameConfig.value = RenameConfig(
            mode = RenameMode.fromKey(preset.mode),
            prefix = preset.prefix,
            startNumber = preset.startNumber,
            keepExtension = preset.keepExtension,
            findText = preset.findText,
            replaceText = preset.replaceText,
            matchCase = preset.matchCase,
            numPosition = preset.numPosition,
            numDigits = preset.numDigits,
            numSeparator = preset.numSeparator,
            caseMode = CaseMode.fromKey(preset.caseMode),
            suffix = preset.suffix,
            removeText = preset.removeText,
            removeFirst = preset.removeFirst,
            removeLast = preset.removeLast,
            keepFirst = preset.keepFirst,
            insertText = preset.insertText,
            insertPosition = preset.insertPosition,
            replaceByPosition = preset.replaceByPosition,
            extAction = ExtAction.fromKey(preset.extAction),
            newExtension = preset.newExtension,
            dateSource = DateSource.fromKey(preset.dateSource),
            dateFormat = preset.dateFormat
        )
        _toastMessage.value = "Preset applied"
    }

    fun deletePreset(preset: RenamePreset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BulkRenamerApplication)
                MainViewModel(
                    application = application,
                    repository = application.repository
                )
            }
        }
    }
}
