/*
 * File: FileListView.kt
 * Purpose: Interactive preview list showing original vs new filenames with duplicate badges and selection checks
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FileType
import com.example.ui.FilePreviewItem
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@Composable
fun FileListView(
    previews: List<FilePreviewItem>,
    allSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    onToggleFileChecked: (Int) -> Unit,
    onClearAllFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.preview),
                style = Typography.titleSmall,
                color = colors.textTertiary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (previews.isNotEmpty()) {
                    Text(
                        text = stringResource(id = if (allSelected) R.string.deselect_all else R.string.select_all),
                        style = Typography.labelMedium,
                        color = colors.accent,
                        modifier = Modifier
                            .clickable { onToggleSelectAll() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("select_all_button")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(id = R.string.clear_files),
                        style = Typography.labelMedium,
                        color = colors.danger,
                        modifier = Modifier
                            .clickable { onClearAllFiles() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("clear_all_button")
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surfaceGlass)
                .border(1.dp, colors.stroke, RoundedCornerShape(26.dp))
                .padding(8.dp)
        ) {
            if (previews.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.stroke, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderZip,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(id = R.string.empty_state),
                        style = Typography.bodyMedium,
                        color = colors.textTertiary
                    )
                }
            } else {
                Column {
                    previews.forEachIndexed { idx, item ->
                        FileRowItem(
                            item = item,
                            onToggleChecked = { onToggleFileChecked(item.originalIndex) }
                        )

                        if (idx < previews.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.stroke.copy(alpha = 0.7f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileRowItem(
    item: FilePreviewItem,
    onToggleChecked: () -> Unit
) {
    val colors = LocalAppColors.current
    val icon = getFileIcon(item.file.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleChecked() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(colors.surface)
                .border(1.dp, colors.stroke, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.file.oldName,
                style = Typography.bodyMedium.copy(
                    textDecoration = if (item.newName != item.file.oldName) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.newName != item.file.oldName) colors.textTertiary else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val previewColor = if (item.isInvalid || item.isDuplicate) colors.danger else colors.success

                Text(
                    text = item.newName,
                    style = Typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = previewColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (item.isInvalid) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(
                        text = stringResource(id = R.string.invalid_warning),
                        color = colors.danger
                    )
                } else if (item.isDuplicate) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(
                        text = stringResource(id = R.string.dup_warning),
                        color = colors.danger
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (item.file.isChecked) colors.accent else colors.surface)
                .border(
                    width = 1.5.dp,
                    color = if (item.file.isChecked) colors.accent else colors.stroke,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.file.isChecked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun Badge(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = Typography.labelSmall,
            color = color
        )
    }
}

private fun getFileIcon(type: FileType): ImageVector {
    return when (type) {
        FileType.IMAGE -> Icons.Outlined.Image
        FileType.VIDEO -> Icons.Outlined.Videocam
        FileType.AUDIO -> Icons.Outlined.Audiotrack
        FileType.DOC -> Icons.Outlined.Description
    }
}
