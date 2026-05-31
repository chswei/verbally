package com.verbally.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.verbally.app.settings.AppLanguage
import com.verbally.app.style.AppCategory
import com.verbally.app.style.AppStyleProfile
import com.verbally.app.style.AppStyleRule
import com.verbally.app.style.OutputStyle
import com.verbally.app.style.StyleRuleDefaults
import com.verbally.app.style.normalizedStyleRuleLanguage

@Composable
internal fun StyleProfilesScreen(
    container: VerballyContainer,
    modifier: Modifier = Modifier,
    onEditorActiveChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val ruleSavedMessage = stringResource(R.string.style_rule_saved)
    val ruleRestoredMessage = stringResource(R.string.style_rule_restored)
    val styleSavedMessage = stringResource(R.string.style_saved)
    var profiles by remember { mutableStateOf(container.styleProfileRepository.list()) }
    val styleRuleLanguage = remember(context) {
        context.defaultPromptLanguageFor(container.settingsRepository.load().interfaceLanguage)
            .normalizedStyleRuleLanguage()
    }
    var styleRules by remember(styleRuleLanguage) {
        mutableStateOf(
            OutputStyle.entries.map { style ->
                container.styleRuleRepository.ruleFor(styleRuleLanguage, style)
            },
        )
    }
    var editingStyle by remember { mutableStateOf<OutputStyle?>(null) }
    fun refreshStyleRules() {
        styleRules = OutputStyle.entries.map { style ->
            container.styleRuleRepository.ruleFor(styleRuleLanguage, style)
        }
    }
    val currentEditingStyle = editingStyle
    LaunchedEffect(currentEditingStyle) {
        onEditorActiveChange(currentEditingStyle != null)
    }
    if (currentEditingStyle != null) {
        val editingRule = styleRules.first { it.style == currentEditingStyle }
        StyleRuleEditorScreen(
            language = styleRuleLanguage,
            rule = editingRule,
            onBack = { editingStyle = null },
            onSave = { ruleText ->
                container.styleRuleRepository.saveCustomRule(styleRuleLanguage, currentEditingStyle, ruleText)
                refreshStyleRules()
                Toast.makeText(context, ruleSavedMessage, Toast.LENGTH_SHORT).show()
                editingStyle = null
            },
            onRestoreDefault = {
                container.styleRuleRepository.restoreDefault(styleRuleLanguage, currentEditingStyle)
                refreshStyleRules()
                Toast.makeText(context, ruleRestoredMessage, Toast.LENGTH_SHORT).show()
            },
            modifier = modifier,
        )
        return
    }
    StyleProfilesScreenContent(
        profiles = profiles,
        styleRules = styleRules,
        onProfileChange = { profile ->
            container.styleProfileRepository.save(profile)
            profiles = container.styleProfileRepository.list()
            Toast.makeText(context, styleSavedMessage, Toast.LENGTH_SHORT).show()
        },
        onEditRule = { editingStyle = it },
        modifier = modifier,
    )
}

@Composable
fun StyleProfilesScreenContent(
    profiles: List<AppStyleProfile>,
    onProfileChange: (AppStyleProfile) -> Unit,
    modifier: Modifier = Modifier,
    styleRules: List<AppStyleRule> = emptyList(),
    onEditRule: (OutputStyle) -> Unit = {},
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.style_title),
            subtitle = stringResource(R.string.style_subtitle),
        )
        profiles.forEach { profile ->
            StyleProfileRow(
                profile = profile,
                onProfileChange = onProfileChange,
            )
        }
        if (styleRules.isNotEmpty()) {
            StyleRulesSection(
                rules = styleRules,
                onEditRule = onEditRule,
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun StyleRulesSection(
    rules: List<AppStyleRule>,
    onEditRule: (OutputStyle) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.style_rules_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.style_rules_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            rules.forEachIndexed { index, rule ->
                StyleRuleRow(
                    rule = rule,
                    onClick = { onEditRule(rule.style) },
                )
                if (index < rules.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun StyleRuleRow(
    rule: AppStyleRule,
    onClick: () -> Unit,
) {
    val styleLabel = rule.style.localizedLabel()
    val contentDescription = stringResource(R.string.style_rule_row_content_description, styleLabel)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.style_rule_row_title, styleLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (rule.isCustom) {
                    stringResource(R.string.style_rule_status_custom)
                } else {
                    stringResource(R.string.style_rule_status_default)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_app_chevron_down_24),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun StyleRuleEditorScreen(
    language: AppLanguage,
    rule: AppStyleRule,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onRestoreDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val styleLabel = rule.style.localizedLabel()
    val backContentDescription = stringResource(R.string.style_rule_back)
    val menuContentDescription = stringResource(R.string.style_rule_menu_content_description, styleLabel)
    val textContentDescription = stringResource(R.string.style_rule_text_content_description, styleLabel)
    var ruleText by remember(language, rule.style, rule.rule) { mutableStateOf(rule.rule) }
    var menuExpanded by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .offset(x = (-16).dp)
                        .semantics {
                            contentDescription = backContentDescription
                        },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_arrow_back_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.style_rule_editor_title, styleLabel),
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-16).dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .offset(x = 16.dp)
                            .semantics {
                                contentDescription = menuContentDescription
                            },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_app_more_vert_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.restore_default)) },
                            onClick = {
                                menuExpanded = false
                                onRestoreDefault()
                                ruleText = StyleRuleDefaults.defaultRuleFor(language, rule.style)
                            },
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.style_rule_editor_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.style_rule_language_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = language.localizedLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        OutlinedTextField(
            value = ruleText,
            onValueChange = { ruleText = it },
            label = { Text(stringResource(R.string.style_rule_text_label)) },
            minLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = textContentDescription
                },
        )
        Button(
            onClick = { onSave(ruleText) },
            modifier = Modifier
                .fillMaxWidth()
                .height(PrimaryActionHeight),
            enabled = ruleText.trim().isNotEmpty(),
        ) {
            Text(stringResource(R.string.style_rule_save_button, styleLabel))
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun StyleProfileRow(
    profile: AppStyleProfile,
    onProfileChange: (AppStyleProfile) -> Unit,
) {
    val categoryLabel = profile.category.localizedLabel()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = profile.category.localizedDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutputStyle.entries.forEach { outputStyle ->
                    val selected = profile.style == outputStyle
                    val styleLabel = outputStyle.localizedLabel()
                    val contentDescription = stringResource(
                        R.string.style_option_content_description,
                        categoryLabel,
                        styleLabel,
                    )
                    val buttonModifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .semantics {
                            this.contentDescription = contentDescription
                        }
                    if (selected) {
                        Button(
                            onClick = { onProfileChange(profile.copy(style = outputStyle)) },
                            modifier = buttonModifier,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(styleLabel)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onProfileChange(profile.copy(style = outputStyle)) },
                            modifier = buttonModifier,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(styleLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCategory.localizedLabel(): String = when (this) {
    AppCategory.CHAT -> stringResource(R.string.style_category_chat)
    AppCategory.WORK -> stringResource(R.string.style_category_work)
    AppCategory.OTHER -> stringResource(R.string.style_category_other)
}

@Composable
private fun AppCategory.localizedDescription(): String = when (this) {
    AppCategory.CHAT -> stringResource(R.string.style_category_chat_description)
    AppCategory.WORK -> stringResource(R.string.style_category_work_description)
    AppCategory.OTHER -> stringResource(R.string.style_category_other_description)
}

@Composable
private fun OutputStyle.localizedLabel(): String = when (this) {
    OutputStyle.FORMAL -> stringResource(R.string.output_style_formal)
    OutputStyle.CASUAL -> stringResource(R.string.output_style_casual)
}
