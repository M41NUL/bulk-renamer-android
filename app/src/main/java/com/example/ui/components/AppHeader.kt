/*
 * File: AppHeader.kt
 * Purpose: Top navigation bar displaying brand identity, author attribution, and settings trigger
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@Composable
fun AppHeader(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceGlass)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, colors.stroke, RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = Typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = stringResource(id = R.string.brand_sub),
                    style = Typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                .clickable { onOpenSettings() }
                .testTag("settings_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(id = R.string.settings_title),
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
