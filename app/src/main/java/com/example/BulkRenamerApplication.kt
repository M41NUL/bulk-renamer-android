/*
 * File: BulkRenamerApplication.kt
 * Purpose: Application entry point initializing Room database and repository instances
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.AppPreferences
import com.example.data.repository.RenamerRepository

class BulkRenamerApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val appPreferences: AppPreferences by lazy {
        AppPreferences(this)
    }

    val repository: RenamerRepository by lazy {
        RenamerRepository(
            presetDao = database.presetDao(),
            historyDao = database.historyDao(),
            appPreferences = appPreferences
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}

