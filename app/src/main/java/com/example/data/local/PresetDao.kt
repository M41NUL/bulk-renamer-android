/*
 * File: PresetDao.kt
 * Purpose: Data Access Object interface for managing rename configuration presets
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RenamePreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM rename_presets ORDER BY createdAt DESC")
    fun getAllPresets(): Flow<List<RenamePreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: RenamePreset): Long

    @Delete
    suspend fun deletePreset(preset: RenamePreset)

    @Query("DELETE FROM rename_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}
