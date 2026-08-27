/*
 * File: SegmentedTabs.kt
 * Purpose: Horizontally scrollable segmented control for switching between all 8 rename modes
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.RenameMode
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@Composable
fun SegmentedTabs(
    activeMode: RenameMode,
    onSelectMode: (RenameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()

    val tabs = listOf(
        RenameMode.PATTERN to R.string.tab_pattern,
        RenameMode.FIND_REPLACE to R.string.tab_find_replace,
        RenameMode.NUMBERING to R.string.tab_numbering,
        RenameMode.CASE to R.string.tab_case,
        RenameMode.TRIM to R.string.tab_trim,
        RenameMode.POSITION to R.string.tab_position,
        RenameMode.EXTENSION to R.string.tab_extension,
        RenameMode.DATE to R.string.tab_date
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceGlass)
            .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            tabs.forEach { (mode, stringRes) ->
                val isSelected = mode == activeMode
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) colors.surface else androidx.compose.ui.graphics.Color.Transparent,
                    label = "tab_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.textPrimary else colors.textSecondary,
                    label = "tab_text"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(bgColor)
                        .then(
                            if (isSelected) Modifier.border(1.dp, colors.stroke, RoundedCornerShape(9.dp))
                            else Modifier
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("tab_${mode.key}")
                ) {
                    Text(
                        text = stringResource(id = stringRes),
                        style = Typography.labelMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}
