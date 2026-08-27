/*
 * File: MainScreen.kt
 * Purpose: Primary screen coordinating file picking, rule configurations, preview list, and action dialogs
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.example.data.preferences.ThemeChoice
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomActionBar
import com.example.ui.components.ConfirmRenameDialog
import com.example.ui.components.DropZone
import com.example.ui.components.FileListView
import com.example.ui.components.HistoryDialog
import com.example.ui.components.PermissionRationaleDialog
import com.example.ui.components.PresetsSheet
import com.example.ui.components.RuleCardPanels
import com.example.ui.components.SavePresetDialog
import com.example.ui.components.SegmentedTabs
import com.example.ui.components.SettingsSheet
import com.example.ui.components.ToolbarStrip
import com.example.ui.theme.AppTheme
import com.example.ui.theme.LocalAppColors
import com.example.util.FileRenameExecutor
import com.example.util.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val userSettings by viewModel.userSettings.collectAsState()
    val renameConfig by viewModel.renameConfig.collectAsState()
    val files by viewModel.files.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasUndo by viewModel.hasUndo.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val history by viewModel.history.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val exportedCsvFile by viewModel.exportedCsvFile.collectAsState()
    val pendingIntentSender by viewModel.pendingIntentSender.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: Exception) {
                }
            }
            viewModel.addSelectedUris(uris, isTree = false)
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
            }

            val docTree = DocumentFile.fromTreeUri(context, treeUri)
            val filesList = docTree?.listFiles()?.filter { it.isFile }?.map { it.uri } ?: emptyList()
            if (filesList.isNotEmpty()) {
                viewModel.addSelectedUris(filesList, isTree = true)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.performRename()
        } else {
            Toast.makeText(context, "Permission was not granted", Toast.LENGTH_SHORT).show()
        }
        viewModel.clearPendingIntentSender()
    }

    LaunchedEffect(pendingIntentSender) {
        pendingIntentSender?.let { sender ->
            intentSenderLauncher.launch(
                IntentSenderRequest.Builder(sender).build()
            )
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(exportedCsvFile) {
        exportedCsvFile?.let { csv ->
            val shareIntent = FileRenameExecutor.createShareLogIntent(context, csv)
            context.startActivity(Intent.createChooser(shareIntent, "Share Rename Log"))
            viewModel.clearExportedCsv()
        }
    }

    AppTheme(themeChoice = userSettings.themeChoice) {
        val colors = LocalAppColors.current
        val visiblePreviews = viewModel.getVisiblePreviews()
        val allChecked = files.isNotEmpty() && files.all { it.isChecked }
        val selectedCount = files.count { it.isChecked }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = colors.bg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppHeader(
                    onOpenSettings = { showSettingsSheet = true }
                )
            },
            bottomBar = {
                if (files.isNotEmpty()) {
                    BottomActionBar(
                        selectedCount = selectedCount,
                        totalCount = files.size,
                        isLoading = isLoading,
                        onRenameClick = {
                            if (userSettings.confirmBeforeRename) {
                                showConfirmDialog = true
                            } else {
                                viewModel.performRename()
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    DropZone(
                        onBrowseFiles = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onBrowseFolder = {
                            folderPickerLauncher.launch(null)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SegmentedTabs(
                        activeMode = renameConfig.mode,
                        onSelectMode = { viewModel.setMode(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ToolbarStrip(
                        hasUndo = hasUndo,
                        onUndo = { viewModel.undoLastRename() },
                        onOpenHistory = { showHistoryDialog = true },
                        onExportLog = { viewModel.exportLogAsCsv() },
                        onSavePreset = { showSavePresetDialog = true },
                        onOpenPresets = { showPresetsSheet = true }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RuleCardPanels(
                        config = renameConfig,
                        typeFilter = typeFilter,
                        searchQuery = searchQuery,
                        sortMode = sortMode,
                        confirmBeforeRename = userSettings.confirmBeforeRename,
                        skipDuplicates = userSettings.skipDuplicates,
                        onPrefixChange = { viewModel.updatePrefix(it) },
                        onStartNumberChange = { viewModel.updateStartNumber(it) },
                        onToggleKeepExtension = { viewModel.toggleKeepExtension() },
                        onFindTextChange = { viewModel.updateFindText(it) },
                        onReplaceTextChange = { viewModel.updateReplaceText(it) },
                        onToggleMatchCase = { viewModel.toggleMatchCase() },
                        onNumPositionChange = { viewModel.updateNumPosition(it) },
                        onNumDigitsChange = { viewModel.updateNumDigits(it) },
                        onNumSeparatorChange = { viewModel.updateNumSeparator(it) },
                        onCaseModeChange = { viewModel.updateCaseMode(it) },
                        onSuffixChange = { viewModel.updateSuffix(it) },
                        onRemoveTextChange = { viewModel.updateRemoveText(it) },
                        onRemoveFirstChange = { viewModel.updateRemoveFirst(it) },
                        onRemoveLastChange = { viewModel.updateRemoveLast(it) },
                        onKeepFirstChange = { viewModel.updateKeepFirst(it) },
                        onInsertTextChange = { viewModel.updateInsertText(it) },
                        onInsertPositionChange = { viewModel.updateInsertPosition(it) },
                        onToggleReplaceByPosition = { viewModel.toggleReplaceByPosition() },
                        onExtActionChange = { viewModel.updateExtAction(it) },
                        onNewExtensionChange = { viewModel.updateNewExtension(it) },
                        onDateSourceChange = { viewModel.updateDateSource(it) },
                        onDateFormatChange = { viewModel.updateDateFormat(it) },
                        onTypeFilterChange = { viewModel.setTypeFilter(it) },
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSortModeChange = { viewModel.setSortMode(it) },
                        onToggleConfirmBeforeRename = { viewModel.toggleConfirmBeforeRename() },
                        onToggleSkipDuplicates = { viewModel.toggleSkipDuplicates() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FileListView(
                        previews = visiblePreviews,
                        allSelected = allChecked,
                        onToggleSelectAll = { viewModel.toggleSelectAll() },
                        onToggleFileChecked = { viewModel.toggleFileChecked(it) },
                        onClearAllFiles = { viewModel.clearAllFiles() }
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        if (showSettingsSheet) {
            SettingsSheet(
                currentTheme = userSettings.themeChoice,
                currentLanguage = userSettings.languageChoice,
                onSelectTheme = { viewModel.updateTheme(it) },
                onSelectLanguage = { viewModel.updateLanguage(it) },
                onDismiss = { showSettingsSheet = false }
            )
        }

        if (showPresetsSheet) {
            PresetsSheet(
                presets = presets,
                onSelectPreset = { viewModel.loadPreset(it) },
                onDeletePreset = { viewModel.deletePreset(it) },
                onDismiss = { showPresetsSheet = false }
            )
        }

        if (showSavePresetDialog) {
            SavePresetDialog(
                defaultName = "Preset ${presets.size + 1}",
                onSave = { name -> viewModel.savePreset(name) },
                onDismiss = { showSavePresetDialog = false }
            )
        }

        if (showHistoryDialog) {
            HistoryDialog(
                historyList = history,
                onDismiss = { showHistoryDialog = false }
            )
        }

        if (showConfirmDialog) {
            ConfirmRenameDialog(
                onConfirm = { viewModel.performRename() },
                onDismiss = { showConfirmDialog = false }
            )
        }

        if (showPermissionRationale) {
            PermissionRationaleDialog(
                onGrant = {
                    permissionLauncher.launch(PermissionHelper.getRequiredMediaPermissions())
                },
                onDismiss = { showPermissionRationale = false }
            )
        }
    }
}
