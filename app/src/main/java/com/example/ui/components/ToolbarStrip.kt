/*
 * File: ToolbarStrip.kt
 * Purpose: Action bar with Undo, History, Export Log, and Preset management action chips
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@Composable
fun ToolbarStrip(
    hasUndo: Boolean,
    onUndo: () -> Unit,
    onOpenHistory: () -> Unit,
    onExportLog: () -> Unit,
    onSavePreset: () -> Unit,
    onOpenPresets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        ToolbarChip(
            icon = Icons.Outlined.Undo,
            label = stringResource(id = R.string.undo),
            enabled = hasUndo,
            onClick = onUndo,
            tag = "undo_button"
        )

        Spacer(modifier = Modifier.width(8.dp))

        ToolbarChip(
            icon = Icons.Outlined.History,
            label = stringResource(id = R.string.history),
            enabled = true,
            onClick = onOpenHistory,
            tag = "history_button"
        )

        Spacer(modifier = Modifier.width(8.dp))

        ToolbarChip(
            icon = Icons.Outlined.FileDownload,
            label = stringResource(id = R.string.export_log),
            enabled = true,
            onClick = onExportLog,
            tag = "export_log_button"
        )

        Spacer(modifier = Modifier.width(8.dp))

        ToolbarChip(
            icon = Icons.Outlined.Save,
            label = stringResource(id = R.string.save_preset),
            enabled = true,
            onClick = onSavePreset,
            tag = "save_preset_button"
        )

        Spacer(modifier = Modifier.width(8.dp))

        ToolbarChip(
            icon = Icons.Outlined.Bookmark,
            label = stringResource(id = R.string.load_preset),
            enabled = true,
            onClick = onOpenPresets,
            tag = "presets_button"
        )
    }
}

@Composable
private fun ToolbarChip(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 13.dp, vertical = 8.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = Typography.labelMedium,
                color = colors.textPrimary
            )
        }
    }
}
