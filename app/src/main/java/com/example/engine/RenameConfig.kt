/*
 * File: RenameConfig.kt
 * Purpose: State and configuration classes for all 8 rename modes and filtering
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.engine

enum class RenameMode(val key: String) {
    PATTERN("pattern"),
    FIND_REPLACE("findreplace"),
    NUMBERING("numbering"),
    CASE("case"),
    TRIM("trim"),
    POSITION("position"),
    EXTENSION("extension"),
    DATE("date");

    companion object {
        fun fromKey(key: String): RenameMode {
            return entries.firstOrNull { it.key == key } ?: PATTERN
        }
    }
}

enum class CaseMode(val key: String) {
    NONE("none"),
    UPPER("upper"),
    LOWER("lower"),
    TITLE("title");

    companion object {
        fun fromKey(key: String): CaseMode {
            return entries.firstOrNull { it.key == key } ?: NONE
        }
    }
}

enum class ExtAction(val key: String) {
    KEEP("keep"),
    CHANGE("change"),
    REMOVE("remove");

    companion object {
        fun fromKey(key: String): ExtAction {
            return entries.firstOrNull { it.key == key } ?: KEEP
        }
    }
}

enum class DateSource(val key: String) {
    CURRENT("current"),
    CREATED("created"),
    MODIFIED("modified");

    companion object {
        fun fromKey(key: String): DateSource {
            return entries.firstOrNull { it.key == key } ?: CURRENT
        }
    }
}

enum class SortMode(val key: String) {
    NONE("none"),
    NAME("name"),
    DATE("date"),
    SIZE("size");

    companion object {
        fun fromKey(key: String): SortMode {
            return entries.firstOrNull { it.key == key } ?: NONE
        }
    }
}

data class RenameConfig(
    val mode: RenameMode = RenameMode.PATTERN,
    val prefix: String = "",
    val startNumber: String = "001",
    val keepExtension: Boolean = true,
    val findText: String = "",
    val replaceText: String = "",
    val matchCase: Boolean = false,
    val numPosition: String = "suffix",
    val numDigits: String = "3",
    val numSeparator: String = "_",
    val caseMode: CaseMode = CaseMode.NONE,
    val suffix: String = "",
    val removeText: String = "",
    val removeFirst: String = "",
    val removeLast: String = "",
    val keepFirst: String = "",
    val insertText: String = "",
    val insertPosition: String = "0",
    val replaceByPosition: Boolean = false,
    val extAction: ExtAction = ExtAction.KEEP,
    val newExtension: String = "",
    val dateSource: DateSource = DateSource.CURRENT,
    val dateFormat: String = "YYYY-MM-DD"
)
