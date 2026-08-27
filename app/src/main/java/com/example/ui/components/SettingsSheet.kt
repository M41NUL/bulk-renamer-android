/*
 * File: SettingsSheet.kt
 * Purpose: Bottom sheet modal providing language selection and theme appearance controls
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.LanguageChoice
import com.example.data.preferences.ThemeChoice
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentTheme: ThemeChoice,
    currentLanguage: LanguageChoice,
    onSelectTheme: (ThemeChoice) -> Unit,
    onSelectLanguage: (LanguageChoice) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = LocalAppColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_title),
                    style = Typography.headlineMedium,
                    color = colors.textPrimary
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.bg)
                        .border(1.dp, colors.stroke, CircleShape)
                        .clickable { onDismiss() }
                        .testTag("settings_close_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(id = R.string.close),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.language_label),
                style = Typography.titleSmall,
                color = colors.textTertiary,
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.stroke, RoundedCornerShape(16.dp))
            ) {
                Column {
                    LanguageOptionRow(
                        title = "English",
                        isSelected = currentLanguage == LanguageChoice.ENGLISH,
                        onClick = { onSelectLanguage(LanguageChoice.ENGLISH) }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.stroke)
                    )

                    LanguageOptionRow(
                        title = "বাংলা (Bangla)",
                        isSelected = currentLanguage == LanguageChoice.BANGLA,
                        onClick = { onSelectLanguage(LanguageChoice.BANGLA) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.theme_label),
                style = Typography.titleSmall,
                color = colors.textTertiary,
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.stroke, RoundedCornerShape(16.dp))
            ) {
                Column {
                    ThemeOptionRow(
                        icon = Icons.Outlined.LightMode,
                        title = stringResource(id = R.string.light),
                        isSelected = currentTheme == ThemeChoice.LIGHT,
                        onClick = { onSelectTheme(ThemeChoice.LIGHT) }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.stroke)
                    )

                    ThemeOptionRow(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(id = R.string.dark),
                        isSelected = currentTheme == ThemeChoice.DARK,
                        onClick = { onSelectTheme(ThemeChoice.DARK) }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.stroke)
                    )

                    ThemeOptionRow(
                        icon = Icons.Outlined.SettingsBrightness,
                        title = stringResource(id = R.string.system),
                        isSelected = currentTheme == ThemeChoice.SYSTEM,
                        onClick = { onSelectTheme(ThemeChoice.SYSTEM) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LanguageOptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = Typography.titleMedium,
                color = colors.textPrimary
            )
        }

        OptionCheck(isSelected = isSelected)
    }
}

@Composable
private fun ThemeOptionRow(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = Typography.titleMedium,
                color = colors.textPrimary
            )
        }

        OptionCheck(isSelected = isSelected)
    }
}

@Composable
private fun OptionCheck(isSelected: Boolean) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (isSelected) colors.accent else colors.surface)
            .border(
                1.5.dp,
                if (isSelected) colors.accent else colors.stroke,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
