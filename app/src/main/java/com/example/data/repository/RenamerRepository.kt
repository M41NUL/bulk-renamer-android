/*
 * File: RenamerRepository.kt
 * Purpose: Repository layer coordinating database, preferences, and data access
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.repository

import com.example.data.local.HistoryDao
import com.example.data.local.PresetDao
import com.example.data.model.RenameHistoryItem
import com.example.data.model.RenamePreset
import com.example.data.preferences.AppPreferences
import com.example.data.preferences.LanguageChoice
import com.example.data.preferences.ThemeChoice
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.flow.Flow

class RenamerRepository(
    private val presetDao: PresetDao,
    private val historyDao: HistoryDao,
    private val appPreferences: AppPreferences
) {
    val presets: Flow<List<RenamePreset>> = presetDao.getAllPresets()
    val history: Flow<List<RenameHistoryItem>> = historyDao.getAllHistory()
    val userSettings: Flow<UserSettings> = appPreferences.settingsFlow

    suspend fun savePreset(preset: RenamePreset): Long {
        return presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(preset: RenamePreset) {
        presetDao.deletePreset(preset)
    }

    suspend fun deletePresetById(id: Long) {
        presetDao.deletePresetById(id)
    }

    suspend fun recordHistory(historyItem: RenameHistoryItem): Long {
        return historyDao.insertHistory(historyItem)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    suspend fun updateTheme(themeChoice: ThemeChoice) {
        appPreferences.setThemeChoice(themeChoice)
    }

    suspend fun updateLanguage(languageChoice: LanguageChoice) {
        appPreferences.setLanguageChoice(languageChoice)
    }

    suspend fun updateConfirmBeforeRename(enabled: Boolean) {
        appPreferences.setConfirmBeforeRename(enabled)
    }

    suspend fun updateSkipDuplicates(enabled: Boolean) {
        appPreferences.setSkipDuplicates(enabled)
    }
}
