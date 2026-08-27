/*
 * File: RuleCardPanels.kt
 * Purpose: Rule configuration card displaying interactive controls corresponding to the active rename mode
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.CaseMode
import com.example.engine.DateSource
import com.example.engine.ExtAction
import com.example.engine.RenameConfig
import com.example.engine.RenameMode
import com.example.engine.SortMode
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Typography

@Composable
fun RuleCardPanels(
    config: RenameConfig,
    typeFilter: String,
    searchQuery: String,
    sortMode: SortMode,
    confirmBeforeRename: Boolean,
    skipDuplicates: Boolean,
    onPrefixChange: (String) -> Unit,
    onStartNumberChange: (String) -> Unit,
    onToggleKeepExtension: () -> Unit,
    onFindTextChange: (String) -> Unit,
    onReplaceTextChange: (String) -> Unit,
    onToggleMatchCase: () -> Unit,
    onNumPositionChange: (String) -> Unit,
    onNumDigitsChange: (String) -> Unit,
    onNumSeparatorChange: (String) -> Unit,
    onCaseModeChange: (CaseMode) -> Unit,
    onSuffixChange: (String) -> Unit,
    onRemoveTextChange: (String) -> Unit,
    onRemoveFirstChange: (String) -> Unit,
    onRemoveLastChange: (String) -> Unit,
    onKeepFirstChange: (String) -> Unit,
    onInsertTextChange: (String) -> Unit,
    onInsertPositionChange: (String) -> Unit,
    onToggleReplaceByPosition: () -> Unit,
    onExtActionChange: (ExtAction) -> Unit,
    onNewExtensionChange: (String) -> Unit,
    onDateSourceChange: (DateSource) -> Unit,
    onDateFormatChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onToggleConfirmBeforeRename: () -> Unit,
    onToggleSkipDuplicates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.rename_rule),
            style = Typography.titleSmall,
            color = colors.textTertiary,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(1.dp, colors.stroke, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            AnimatedContent(
                targetState = config.mode,
                label = "rule_panel_switch"
            ) { targetMode ->
                when (targetMode) {
                    RenameMode.PATTERN -> PatternPanel(
                        config = config,
                        onPrefixChange = onPrefixChange,
                        onStartNumberChange = onStartNumberChange,
                        onToggleKeepExtension = onToggleKeepExtension
                    )
                    RenameMode.FIND_REPLACE -> FindReplacePanel(
                        config = config,
                        onFindTextChange = onFindTextChange,
                        onReplaceTextChange = onReplaceTextChange,
                        onToggleMatchCase = onToggleMatchCase
                    )
                    RenameMode.NUMBERING -> NumberingPanel(
                        config = config,
                        onNumPositionChange = onNumPositionChange,
                        onNumDigitsChange = onNumDigitsChange,
                        onNumSeparatorChange = onNumSeparatorChange
                    )
                    RenameMode.CASE -> CasePanel(
                        config = config,
                        onCaseModeChange = onCaseModeChange
                    )
                    RenameMode.TRIM -> TrimPanel(
                        config = config,
                        onSuffixChange = onSuffixChange,
                        onRemoveTextChange = onRemoveTextChange,
                        onRemoveFirstChange = onRemoveFirstChange,
                        onRemoveLastChange = onRemoveLastChange,
                        onKeepFirstChange = onKeepFirstChange
                    )
                    RenameMode.POSITION -> PositionPanel(
                        config = config,
                        onInsertTextChange = onInsertTextChange,
                        onInsertPositionChange = onInsertPositionChange,
                        onToggleReplaceByPosition = onToggleReplaceByPosition
                    )
                    RenameMode.EXTENSION -> ExtensionPanel(
                        config = config,
                        onExtActionChange = onExtActionChange,
                        onNewExtensionChange = onNewExtensionChange
                    )
                    RenameMode.DATE -> DatePanel(
                        config = config,
                        onDateSourceChange = onDateSourceChange,
                        onDateFormatChange = onDateFormatChange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilterAndBehaviorControls(
            typeFilter = typeFilter,
            searchQuery = searchQuery,
            sortMode = sortMode,
            confirmBeforeRename = confirmBeforeRename,
            skipDuplicates = skipDuplicates,
            onTypeFilterChange = onTypeFilterChange,
            onSearchQueryChange = onSearchQueryChange,
            onSortModeChange = onSortModeChange,
            onToggleConfirmBeforeRename = onToggleConfirmBeforeRename,
            onToggleSkipDuplicates = onToggleSkipDuplicates
        )
    }
}

@Composable
private fun PatternPanel(
    config: RenameConfig,
    onPrefixChange: (String) -> Unit,
    onStartNumberChange: (String) -> Unit,
    onToggleKeepExtension: () -> Unit
) {
    Column {
        RuleRow(
            title = stringResource(id = R.string.prefix),
            hint = stringResource(id = R.string.prefix_hint)
        ) {
            CustomTextField(
                value = config.prefix,
                onValueChange = onPrefixChange,
                placeholder = "Trip_2026",
                modifier = Modifier.width(140.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.start_number),
            hint = stringResource(id = R.string.start_number_hint)
        ) {
            CustomTextField(
                value = config.startNumber,
                onValueChange = onStartNumberChange,
                placeholder = "001",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(90.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.keep_extension),
            hint = stringResource(id = R.string.keep_extension_hint)
        ) {
            IosSwitch(
                checked = config.keepExtension,
                onCheckedChange = { onToggleKeepExtension() }
            )
        }
    }
}

@Composable
private fun FindReplacePanel(
    config: RenameConfig,
    onFindTextChange: (String) -> Unit,
    onReplaceTextChange: (String) -> Unit,
    onToggleMatchCase: () -> Unit
) {
    Column {
        RuleRow(
            title = stringResource(id = R.string.find),
            hint = stringResource(id = R.string.find_hint)
        ) {
            CustomTextField(
                value = config.findText,
                onValueChange = onFindTextChange,
                placeholder = "IMG",
                modifier = Modifier.width(140.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.replace_with),
            hint = stringResource(id = R.string.replace_with_hint)
        ) {
            CustomTextField(
                value = config.replaceText,
                onValueChange = onReplaceTextChange,
                placeholder = "Photo",
                modifier = Modifier.width(140.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.match_case),
            hint = stringResource(id = R.string.match_case_hint)
        ) {
            IosSwitch(
                checked = config.matchCase,
                onCheckedChange = { onToggleMatchCase() }
            )
        }
    }
}

@Composable
private fun NumberingPanel(
    config: RenameConfig,
    onNumPositionChange: (String) -> Unit,
    onNumDigitsChange: (String) -> Unit,
    onNumSeparatorChange: (String) -> Unit
) {
    Column {
        RuleRow(
            title = stringResource(id = R.string.position),
            hint = stringResource(id = R.string.position_hint)
        ) {
            CustomDropdown(
                options = listOf(
                    "suffix" to stringResource(id = R.string.pos_end),
                    "prefix" to stringResource(id = R.string.pos_start)
                ),
                selectedKey = config.numPosition,
                onSelect = onNumPositionChange,
                modifier = Modifier.width(140.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.digits),
            hint = stringResource(id = R.string.digits_hint)
        ) {
            CustomDropdown(
                options = listOf(
                    "2" to "2 (01)",
                    "3" to "3 (001)",
                    "4" to "4 (0001)"
                ),
                selectedKey = config.numDigits,
                onSelect = onNumDigitsChange,
                modifier = Modifier.width(140.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.separator),
            hint = stringResource(id = R.string.separator_hint)
        ) {
            CustomTextField(
                value = config.numSeparator,
                onValueChange = onNumSeparatorChange,
                placeholder = "_",
                modifier = Modifier.width(90.dp)
            )
        }
    }
}

@Composable
private fun CasePanel(
    config: RenameConfig,
    onCaseModeChange: (CaseMode) -> Unit
) {
    val colors = LocalAppColors.current

    Column {
        RuleRow(
            title = stringResource(id = R.string.text_case),
            hint = stringResource(id = R.string.text_case_hint)
        ) {}

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg)
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val modes = listOf(
                    CaseMode.NONE to stringResource(id = R.string.case_none),
                    CaseMode.UPPER to stringResource(id = R.string.case_upper),
                    CaseMode.LOWER to stringResource(id = R.string.case_lower),
                    CaseMode.TITLE to stringResource(id = R.string.case_title)
                )

                modes.forEach { (mode, label) ->
                    val isSelected = config.caseMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(1.dp, colors.stroke, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .clickable { onCaseModeChange(mode) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelSmall,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrimPanel(
    config: RenameConfig,
    onSuffixChange: (String) -> Unit,
    onRemoveTextChange: (String) -> Unit,
    onRemoveFirstChange: (String) -> Unit,
    onRemoveLastChange: (String) -> Unit,
    onKeepFirstChange: (String) -> Unit
) {
    Column {
        RuleRow(
            title = stringResource(id = R.string.add_suffix),
            hint = stringResource(id = R.string.add_suffix_hint)
        ) {
            CustomTextField(
                value = config.suffix,
                onValueChange = onSuffixChange,
                placeholder = "_final",
                modifier = Modifier.width(130.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.remove_text),
            hint = stringResource(id = R.string.remove_text_hint)
        ) {
            CustomTextField(
                value = config.removeText,
                onValueChange = onRemoveTextChange,
                placeholder = "copy",
                modifier = Modifier.width(130.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.remove_first_chars),
            hint = stringResource(id = R.string.remove_first_chars_hint)
        ) {
            CustomTextField(
                value = config.removeFirst,
                onValueChange = onRemoveFirstChange,
                placeholder = "2",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(80.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.remove_last_chars),
            hint = stringResource(id = R.string.remove_last_chars_hint)
        ) {
            CustomTextField(
                value = config.removeLast,
                onValueChange = onRemoveLastChange,
                placeholder = "3",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(80.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.keep_first_chars),
            hint = stringResource(id = R.string.keep_first_chars_hint)
        ) {
            CustomTextField(
                value = config.keepFirst,
                onValueChange = onKeepFirstChange,
                placeholder = "10",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
private fun PositionPanel(
    config: RenameConfig,
    onInsertTextChange: (String) -> Unit,
    onInsertPositionChange: (String) -> Unit,
    onToggleReplaceByPosition: () -> Unit
) {
    Column {
        RuleRow(
            title = stringResource(id = R.string.insert_text),
            hint = stringResource(id = R.string.insert_text_hint)
        ) {
            CustomTextField(
                value = config.insertText,
                onValueChange = onInsertTextChange,
                placeholder = "-v2",
                modifier = Modifier.width(130.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.insert_position),
            hint = stringResource(id = R.string.insert_position_hint)
        ) {
            CustomTextField(
                value = config.insertPosition,
                onValueChange = onInsertPositionChange,
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(80.dp)
            )
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.replace_by_position),
            hint = stringResource(id = R.string.replace_by_position_hint)
        ) {
            IosSwitch(
                checked = config.replaceByPosition,
                onCheckedChange = { onToggleReplaceByPosition() }
            )
        }
    }
}

@Composable
private fun ExtensionPanel(
    config: RenameConfig,
    onExtActionChange: (ExtAction) -> Unit,
    onNewExtensionChange: (String) -> Unit
) {
    val colors = LocalAppColors.current

    Column {
        RuleRow(
            title = stringResource(id = R.string.ext_action),
            hint = stringResource(id = R.string.ext_action_hint)
        ) {}

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg)
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val actions = listOf(
                    ExtAction.KEEP to stringResource(id = R.string.ext_keep),
                    ExtAction.CHANGE to stringResource(id = R.string.ext_change),
                    ExtAction.REMOVE to stringResource(id = R.string.ext_remove)
                )

                actions.forEach { (action, label) ->
                    val isSelected = config.extAction == action
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(1.dp, colors.stroke, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .clickable { onExtActionChange(action) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelMedium,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (config.extAction == ExtAction.CHANGE) {
            RuleDivider()

            RuleRow(
                title = stringResource(id = R.string.new_extension),
                hint = stringResource(id = R.string.new_extension_hint)
            ) {
                CustomTextField(
                    value = config.newExtension,
                    onValueChange = onNewExtensionChange,
                    placeholder = "jpg",
                    modifier = Modifier.width(90.dp)
                )
            }
        }
    }
}

@Composable
private fun DatePanel(
    config: RenameConfig,
    onDateSourceChange: (DateSource) -> Unit,
    onDateFormatChange: (String) -> Unit
) {
    val colors = LocalAppColors.current

    Column {
        RuleRow(
            title = stringResource(id = R.string.date_source),
            hint = stringResource(id = R.string.date_source_hint)
        ) {}

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg)
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val sources = listOf(
                    DateSource.CURRENT to stringResource(id = R.string.date_current),
                    DateSource.CREATED to stringResource(id = R.string.date_created),
                    DateSource.MODIFIED to stringResource(id = R.string.date_modified)
                )

                sources.forEach { (src, label) ->
                    val isSelected = config.dateSource == src
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(1.dp, colors.stroke, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .clickable { onDateSourceChange(src) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelSmall,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        RuleDivider()

        RuleRow(
            title = stringResource(id = R.string.date_format),
            hint = stringResource(id = R.string.date_format_hint)
        ) {
            CustomTextField(
                value = config.dateFormat,
                onValueChange = onDateFormatChange,
                placeholder = "YYYY-MM-DD",
                modifier = Modifier.width(130.dp)
            )
        }
    }
}

@Composable
private fun FilterAndBehaviorControls(
    typeFilter: String,
    searchQuery: String,
    sortMode: SortMode,
    confirmBeforeRename: Boolean,
    skipDuplicates: Boolean,
    onTypeFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onToggleConfirmBeforeRename: () -> Unit,
    onToggleSkipDuplicates: () -> Unit
) {
    Column {
        RuleRow(
            title = stringResource(id = R.string.file_type_filter),
            hint = stringResource(id = R.string.file_type_filter_hint)
        ) {
            CustomDropdown(
                options = listOf(
                    "all" to stringResource(id = R.string.filter_all),
                    "image" to stringResource(id = R.string.filter_image),
                    "video" to stringResource(id = R.string.filter_video),
                    "audio" to stringResource(id = R.string.filter_audio),
                    "doc" to stringResource(id = R.string.filter_doc)
                ),
                selectedKey = typeFilter,
                onSelect = onTypeFilterChange,
                modifier = Modifier.width(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        RuleRow(
            title = stringResource(id = R.string.search_files),
            hint = stringResource(id = R.string.search_files_hint)
        ) {
            CustomTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = stringResource(id = R.string.search_files),
                textAlign = TextAlign.Start,
                modifier = Modifier.width(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        RuleRow(
            title = stringResource(id = R.string.sort_by),
            hint = stringResource(id = R.string.sort_by_hint)
        ) {
            CustomDropdown(
                options = listOf(
                    SortMode.NONE.key to stringResource(id = R.string.sort_none),
                    SortMode.NAME.key to stringResource(id = R.string.sort_name),
                    SortMode.DATE.key to stringResource(id = R.string.sort_date),
                    SortMode.SIZE.key to stringResource(id = R.string.sort_size)
                ),
                selectedKey = sortMode.key,
                onSelect = { onSortModeChange(SortMode.fromKey(it)) },
                modifier = Modifier.width(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RuleRow(
            title = stringResource(id = R.string.confirm_before_rename),
            hint = stringResource(id = R.string.confirm_before_rename_hint)
        ) {
            IosSwitch(
                checked = confirmBeforeRename,
                onCheckedChange = { onToggleConfirmBeforeRename() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RuleRow(
            title = stringResource(id = R.string.skip_duplicates),
            hint = stringResource(id = R.string.skip_duplicates_hint)
        ) {
            IosSwitch(
                checked = skipDuplicates,
                onCheckedChange = { onToggleSkipDuplicates() }
            )
        }
    }
}

@Composable
fun RuleRow(
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = colors.textPrimary
            )
            if (hint.isNotEmpty()) {
                Text(
                    text = hint,
                    style = Typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }

        content()
    }
}

@Composable
private fun RuleDivider() {
    val colors = LocalAppColors.current
    Spacer(modifier = Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.stroke)
    )
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.End
) {
    val colors = LocalAppColors.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = Typography.bodyMedium.copy(
            color = colors.textPrimary,
            textAlign = textAlign
        ),
        cursorBrush = SolidColor(colors.accent),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.bg)
                    .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = Typography.bodyMedium,
                        color = colors.textTertiary,
                        textAlign = textAlign
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun IosSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .size(width = 46.dp, height = 27.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (checked) colors.accent else colors.stroke)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(23.dp)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color.White)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdown(
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == selectedKey }?.second ?: options.firstOrNull()?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg)
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentLabel,
                    style = Typography.bodyMedium,
                    color = colors.textPrimary
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = Typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
