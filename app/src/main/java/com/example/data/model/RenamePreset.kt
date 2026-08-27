/*
 * File: RenamePreset.kt
 * Purpose: Room entity representing saved user rename rules and configurations
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rename_presets")
data class RenamePreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val mode: String = "pattern",
    val prefix: String = "",
    val startNumber: String = "001",
    val keepExtension: Boolean = true,
    val findText: String = "",
    val replaceText: String = "",
    val matchCase: Boolean = false,
    val numPosition: String = "suffix",
    val numDigits: String = "3",
    val numSeparator: String = "_",
    val caseMode: String = "none",
    val suffix: String = "",
    val removeText: String = "",
    val removeFirst: String = "",
    val removeLast: String = "",
    val keepFirst: String = "",
    val insertText: String = "",
    val insertPosition: String = "0",
    val replaceByPosition: Boolean = false,
    val extAction: String = "keep",
    val newExtension: String = "",
    val dateSource: String = "current",
    val dateFormat: String = "YYYY-MM-DD",
    val createdAt: Long = System.currentTimeMillis()
)
