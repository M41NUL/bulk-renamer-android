/*
 * File: RenameHistoryItem.kt
 * Purpose: Room entity representing historical bulk rename operations and logs
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class RenameLogEntry(
    val oldName: String,
    val newName: String,
    val uriString: String? = null
)

@Entity(tableName = "rename_history")
data class RenameHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val fileCount: Int = 0,
    val summary: String = "",
    val entriesJson: String = ""
)
