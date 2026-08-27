/*
 * File: AppPreferences.kt
 * Purpose: DataStore and SharedPreferences manager for theme, language, and behavior settings
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bulk_renamer_prefs")


enum class ThemeChoice(val key: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromKey(key: String): ThemeChoice {
            return entries.firstOrNull { it.key == key } ?: SYSTEM
        }
    }
}

enum class LanguageChoice(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "Bangla");

    companion object {
        fun fromCode(code: String): LanguageChoice {
            return entries.firstOrNull { it.code == code } ?: ENGLISH
        }
    }
}

data class UserSettings(
    val themeChoice: ThemeChoice = ThemeChoice.LIGHT,
    val languageChoice: LanguageChoice = LanguageChoice.ENGLISH,
    val confirmBeforeRename: Boolean = false,
    val skipDuplicates: Boolean = false
)

class AppPreferences(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("app_theme")
    private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_CONFIRM = booleanPreferencesKey("confirm_before_rename")
    private val KEY_SKIP_DUP = booleanPreferencesKey("skip_duplicates")

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val sp = context.getSharedPreferences("app_locale_prefs", Context.MODE_PRIVATE)
            val spLang = sp.getString("selected_lang", null)
            val chosenLangCode = prefs[KEY_LANGUAGE] ?: spLang ?: LanguageChoice.ENGLISH.code

            UserSettings(
                themeChoice = ThemeChoice.fromKey(prefs[KEY_THEME] ?: ThemeChoice.LIGHT.key),
                languageChoice = LanguageChoice.fromCode(chosenLangCode),
                confirmBeforeRename = prefs[KEY_CONFIRM] ?: false,
                skipDuplicates = prefs[KEY_SKIP_DUP] ?: false
            )
        }

    suspend fun setThemeChoice(themeChoice: ThemeChoice) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = themeChoice.key
        }
    }

    suspend fun setLanguageChoice(languageChoice: LanguageChoice) {
        // Update SharedPreferences immediately for synchronous cold-start context configuration
        val sp = context.getSharedPreferences("app_locale_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("selected_lang", languageChoice.code).apply()

        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = languageChoice.code
        }
    }

    suspend fun setConfirmBeforeRename(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CONFIRM] = enabled
        }
    }

    suspend fun setSkipDuplicates(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SKIP_DUP] = enabled
        }
    }
}
