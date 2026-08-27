/*
 * File: RenameEngine.kt
 * Purpose: Core calculation engine for computing new filenames, validating names, and detecting duplicates
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.engine

import com.example.data.model.FileItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ParsedName(val base: String, val ext: String)

object RenameEngine {

    fun splitName(fullName: String): ParsedName {
        val dotIndex = fullName.lastIndexOf('.')
        return if (dotIndex <= 0) {
            ParsedName(base = fullName, ext = "")
        } else {
            ParsedName(
                base = fullName.substring(0, dotIndex),
                ext = fullName.substring(dotIndex + 1)
            )
        }
    }

    private fun padNumber(n: Long, width: Int): String {
        return n.toString().padStart(width, '0')
    }

    fun computeNewName(file: FileItem, index: Int, config: RenameConfig): String {
        val (base, ext) = splitName(file.oldName)

        return when (config.mode) {
            RenameMode.PATTERN -> {
                val prefix = config.prefix.ifBlank { "File" }
                val start = config.startNumber.toLongOrNull() ?: 1L
                val width = if (config.startNumber.isNotEmpty()) config.startNumber.length else 3
                val num = padNumber(start + index, width)
                if (config.keepExtension && ext.isNotEmpty()) {
                    "${prefix}_${num}.${ext}"
                } else {
                    "${prefix}_${num}"
                }
            }

            RenameMode.FIND_REPLACE -> {
                if (config.findText.isEmpty()) {
                    if (ext.isNotEmpty()) "${base}.${ext}" else base
                } else {
                    val flags = if (config.matchCase) 0 else Pattern.CASE_INSENSITIVE
                    val newBase = try {
                        val pattern = Pattern.compile(Pattern.quote(config.findText), flags)
                        pattern.matcher(base).replaceAll(config.replaceText)
                    } catch (e: Exception) {
                        base
                    }
                    if (ext.isNotEmpty()) "${newBase}.${ext}" else newBase
                }
            }

            RenameMode.NUMBERING -> {
                val digits = config.numDigits.toIntOrNull() ?: 3
                val sep = config.numSeparator.ifEmpty { "_" }
                val num = padNumber((index + 1).toLong(), digits)
                val newBase = if (config.numPosition == "prefix") {
                    "${num}${sep}${base}"
                } else {
                    "${base}${sep}${num}"
                }
                if (ext.isNotEmpty()) "${newBase}.${ext}" else newBase
            }

            RenameMode.CASE -> {
                val newBase = when (config.caseMode) {
                    CaseMode.UPPER -> base.uppercase(Locale.getDefault())
                    CaseMode.LOWER -> base.lowercase(Locale.getDefault())
                    CaseMode.TITLE -> {
                        base.split(" ").joinToString(" ") { word ->
                            if (word.isNotEmpty()) {
                                word.substring(0, 1).uppercase(Locale.getDefault()) + word.substring(1).lowercase(Locale.getDefault())
                            } else ""
                        }
                    }
                    CaseMode.NONE -> base
                }
                if (ext.isNotEmpty()) "${newBase}.${ext}" else newBase
            }

            RenameMode.TRIM -> {
                var newBase = base
                if (config.removeText.isNotEmpty()) {
                    newBase = try {
                        val pattern = Pattern.compile(Pattern.quote(config.removeText), Pattern.CASE_INSENSITIVE)
                        pattern.matcher(newBase).replaceAll("")
                    } catch (e: Exception) {
                        newBase
                    }
                }
                val removeFirst = config.removeFirst.toIntOrNull() ?: 0
                if (removeFirst > 0 && removeFirst <= newBase.length) {
                    newBase = newBase.substring(removeFirst)
                } else if (removeFirst > newBase.length) {
                    newBase = ""
                }

                val removeLast = config.removeLast.toIntOrNull() ?: 0
                if (removeLast > 0 && removeLast <= newBase.length) {
                    newBase = newBase.substring(0, newBase.length - removeLast)
                } else if (removeLast > newBase.length) {
                    newBase = ""
                }

                val keepFirst = config.keepFirst.toIntOrNull() ?: 0
                if (keepFirst in 1..newBase.length) {
                    newBase = newBase.substring(0, keepFirst)
                }

                newBase = "${newBase}${config.suffix}"
                if (ext.isNotEmpty()) "${newBase}.${ext}" else newBase
            }

            RenameMode.POSITION -> {
                var newBase = base
                val insertText = config.insertText
                var pos = config.insertPosition.toIntOrNull() ?: 0
                if (pos < 0) pos = 0
                pos = pos.coerceAtMost(newBase.length)

                if (insertText.isNotEmpty()) {
                    newBase = if (config.replaceByPosition) {
                        val endPos = (pos + insertText.length).coerceAtMost(newBase.length)
                        newBase.substring(0, pos) + insertText + newBase.substring(endPos)
                    } else {
                        newBase.substring(0, pos) + insertText + newBase.substring(pos)
                    }
                }
                if (ext.isNotEmpty()) "${newBase}.${ext}" else newBase
            }

            RenameMode.EXTENSION -> {
                when (config.extAction) {
                    ExtAction.REMOVE -> base
                    ExtAction.CHANGE -> {
                        val cleanExt = config.newExtension.trimStart('.')
                        if (cleanExt.isNotEmpty()) "${base}.${cleanExt}" else base
                    }
                    ExtAction.KEEP -> if (ext.isNotEmpty()) "${base}.${ext}" else base
                }
            }

            RenameMode.DATE -> {
                val timestamp = when (config.dateSource) {
                    DateSource.CURRENT -> System.currentTimeMillis()
                    DateSource.MODIFIED, DateSource.CREATED -> {
                        if (file.dateModified > 0) file.dateModified else System.currentTimeMillis()
                    }
                }
                val date = Date(timestamp)
                val cal = Calendar.getInstance().apply { time = date }
                val year = cal.get(Calendar.YEAR).toString()
                val month = padNumber((cal.get(Calendar.MONTH) + 1).toLong(), 2)
                val day = padNumber(cal.get(Calendar.DAY_OF_MONTH).toLong(), 2)
                val hour = padNumber(cal.get(Calendar.HOUR_OF_DAY).toLong(), 2)
                val minute = padNumber(cal.get(Calendar.MINUTE).toLong(), 2)

                val fmt = config.dateFormat.ifBlank { "YYYY-MM-DD" }
                val stamped = fmt
                    .replace("YYYY", year)
                    .replace("MM", month)
                    .replace("DD", day)
                    .replace("HH", hour)
                    .replace("mm", minute)

                val newBase = "${base}_${stamped}"
                if (ext.isNotEmpty()) "${newBase}.${ext}" else newBase
            }
        }
    }

    fun isValidFilename(name: String): Boolean {
        if (name.isBlank()) return false
        val invalidChars = charArrayOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')
        return !name.any { it in invalidChars }
    }
}
