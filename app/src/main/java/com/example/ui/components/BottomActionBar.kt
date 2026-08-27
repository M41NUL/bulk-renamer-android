/*
 * File: BottomActionBar.kt
 * Purpose: Floating glass action bar at bottom displaying selection count and primary Rename button
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@Composable
fun BottomActionBar(
    selectedCount: Int,
    totalCount: Int,
    isLoading: Boolean,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surfaceGlass)
                .border(1.dp, colors.stroke, RoundedCornerShape(26.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedCount ${stringResource(id = R.string.of_word)} $totalCount ${stringResource(id = R.string.selected)}",
                        style = Typography.labelMedium,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRenameClick,
                    enabled = selectedCount > 0 && !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = androidx.compose.ui.graphics.Color.White,
                        disabledContainerColor = colors.textTertiary,
                        disabledContentColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("rename_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = androidx.compose.ui.graphics.Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.renaming),
                            style = Typography.labelLarge
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.rename_files),
                            style = Typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
