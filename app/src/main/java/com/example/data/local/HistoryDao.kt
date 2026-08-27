/*
 * File: HistoryDao.kt
 * Purpose: Data Access Object interface for storing and retrieving bulk rename history
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RenameHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM rename_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<RenameHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: RenameHistoryItem): Long

    @Query("DELETE FROM rename_history")
    suspend fun clearHistory()
}
