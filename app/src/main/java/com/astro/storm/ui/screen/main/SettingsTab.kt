package com.astro.storm.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.BikramSambatConverter
import com.astro.storm.data.localization.DateSystem
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalDateSystem
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.LocalLocalizationManager
import com.astro.storm.data.localization.LocalizationManager
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.localization.getLocalizedName
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ui.theme.AppTheme

/**
 * Settings Tab - App Settings & Profile Management
 *
 * Provides access to:
 * - Profile management (edit, delete)
 * - Export options
 * - App preferences
 * - About section
 */
@Composable
fun SettingsTab(
    currentChart: VedicChart?,
    savedCharts: List<SavedChart>,
    onEditProfile: () -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onExportChart: (ExportFormat) -> Unit,
    onManageProfiles: () -> Unit
) {
    val context = LocalContext.current
    val language = LocalLanguage.current
    val localizationManager = LocalLocalizationManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var chartToDelete by remember { mutableStateOf<SavedChart?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Profile Section
        item {
            SettingsSectionHeader(titleKey = StringKey.SETTINGS_PROFILE)
        }

        item {
            currentChart?.let { chart ->
                CurrentProfileCard(
                    chart = chart,
                    onExport = { showExportDialog = true }
                )
            } ?: run {
                EmptyProfileCard(onManageProfiles = onManageProfiles)
            }
        }

        item {
            if (currentChart != null) {
                SettingsItem(
                    icon = Icons.Outlined.Edit,
                    titleKey = StringKey.SETTINGS_EDIT_PROFILE,
                    subtitleKey = StringKey.SETTINGS_EDIT_PROFILE_DESC,
                    onClick = onEditProfile
                )
            } else {
                SettingsItem(
                    icon = Icons.Outlined.People,
                    titleKey = StringKey.SETTINGS_MANAGE_PROFILES,
                    subtitleKey = StringKey.SETTINGS_NO_PROFILE,
                    onClick = onManageProfiles
                )
            }
        }

        // Export Section
        if (currentChart != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(titleKey = StringKey.SETTINGS_EXPORT)
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.PictureAsPdf,
                    titleKey = StringKey.SETTINGS_EXPORT_PDF,
                    subtitleKey = StringKey.SETTINGS_EXPORT_PDF_DESC,
                    onClick = { onExportChart(ExportFormat.PDF) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Image,
                    titleKey = StringKey.SETTINGS_EXPORT_IMAGE,
                    subtitleKey = StringKey.SETTINGS_EXPORT_IMAGE_DESC,
                    onClick = { onExportChart(ExportFormat.IMAGE) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.ContentCopy,
                    titleKey = StringKey.SETTINGS_EXPORT_CLIPBOARD,
                    subtitleKey = StringKey.SETTINGS_EXPORT_CLIPBOARD_DESC,
                    onClick = { onExportChart(ExportFormat.CLIPBOARD) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Code,
                    titleKey = StringKey.SETTINGS_EXPORT_JSON,
                    subtitleKey = StringKey.SETTINGS_EXPORT_JSON_DESC,
                    onClick = { onExportChart(ExportFormat.JSON) }
                )
            }
        }

        // Preferences Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(titleKey = StringKey.SETTINGS_PREFERENCES)
        }

        item {
            LanguageSetting(localizationManager = localizationManager)
        }

        item {
            DateSystemSetting(localizationManager = localizationManager)
        }

        item {
            HouseSystemSetting()
        }

        item {
            AyanamsaSetting()
        }

        // About Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(titleKey = StringKey.SETTINGS_ABOUT)
        }

        item {
            SettingsItem(
                icon = Icons.Outlined.Info,
                titleKey = StringKey.SETTINGS_ABOUT_APP,
                subtitleKey = StringKey.SETTINGS_VERSION,
                subtitleArgs = arrayOf("1.0.0"),
                onClick = { /* Show about dialog */ }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Outlined.Science,
                titleKey = StringKey.SETTINGS_CALC_ENGINE,
                subtitleKey = StringKey.SETTINGS_CALC_ENGINE_DESC,
                onClick = { /* Show calculation info */ }
            )
        }

        item {
            AboutCard()
        }
    }

    // Export Dialog
    if (showExportDialog && currentChart != null) {
        ExportOptionsDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                onExportChart(format)
                showExportDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && chartToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppTheme.CardBackground,
            titleContentColor = AppTheme.TextPrimary,
            textContentColor = AppTheme.TextSecondary,
            title = { Text(stringResource(StringKey.DIALOG_DELETE_PROFILE)) },
            text = {
                Text(stringResource(StringKey.DIALOG_DELETE_CONFIRM, chartToDelete?.name ?: ""))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chartToDelete?.let { onDeleteProfile(it.id) }
                        showDeleteDialog = false
                        chartToDelete = null
                    }
                ) {
                    Text(stringResource(StringKey.BTN_DELETE), color = AppTheme.ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(StringKey.BTN_CANCEL), color = AppTheme.AccentPrimary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(titleKey: StringKey) {
    Text(
        text = stringResource(titleKey),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.AccentPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun CurrentProfileCard(
    chart: VedicChart,
    onExport: () -> Unit
) {
    val language = LocalLanguage.current
    val dateSystem = LocalDateSystem.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppTheme.AccentPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chart.birthData.name
                            .split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString(""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.AccentPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chart.birthData.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Display date based on selected date system
                    val dateDisplay = if (dateSystem == DateSystem.BS) {
                        com.astro.storm.data.localization.BikramSambatConverter.toBS(chart.birthData.dateTime.toLocalDate())
                            ?.format(language) ?: chart.birthData.dateTime.toLocalDate().toString()
                    } else {
                        chart.birthData.dateTime.toLocalDate().toString()
                    }
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                    Text(
                        text = chart.birthData.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = AppTheme.DividerColor)

            Spacer(modifier = Modifier.height(12.dp))

            // Chart details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartDetailItem(
                    labelKey = StringKey.CHART_ASCENDANT,
                    value = chart.planetPositions.find { it.planet.displayName == "Sun" }?.sign?.getLocalizedName(language)
                        ?: com.astro.storm.data.model.ZodiacSign.fromLongitude(chart.ascendant).getLocalizedName(language)
                )
                ChartDetailItem(
                    labelKey = StringKey.CHART_MOON_SIGN,
                    value = chart.planetPositions.find { it.planet == com.astro.storm.data.model.Planet.MOON }?.sign?.getLocalizedName(language)
                        ?: "-"
                )
                ChartDetailItem(
                    labelKey = StringKey.CHART_NAKSHATRA,
                    value = chart.planetPositions.find { it.planet == com.astro.storm.data.model.Planet.MOON }?.nakshatra?.getLocalizedName(language)?.take(8)
                        ?: "-"
                )
            }
        }
    }
}

@Composable
private fun ChartDetailItem(
    labelKey: StringKey,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppTheme.TextPrimary
        )
        Text(
            text = stringResource(labelKey),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
    }
}

@Composable
private fun EmptyProfileCard(onManageProfiles: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onManageProfiles() },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonAdd,
                contentDescription = null,
                tint = AppTheme.TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(StringKey.SETTINGS_NO_PROFILE),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(StringKey.SETTINGS_TAP_TO_SELECT),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    titleKey: StringKey,
    subtitleKey: StringKey,
    onClick: () -> Unit,
    subtitleArgs: Array<Any>? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val title = stringResource(titleKey)
    val subtitle = if (subtitleArgs != null) {
        stringResource(subtitleKey, *subtitleArgs)
    } else {
        stringResource(subtitleKey)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }

            trailing?.invoke() ?: Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppTheme.TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun HouseSystemSetting() {
    val language = LocalLanguage.current
    var expanded by remember { mutableStateOf(false) }
    var selectedSystem by remember { mutableStateOf(HouseSystem.DEFAULT) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.ChipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(StringKey.SETTINGS_HOUSE_SYSTEM),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = selectedSystem.getLocalizedName(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.AccentPrimary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            if (expanded) {
                HorizontalDivider(color = AppTheme.DividerColor)

                HouseSystem.entries.forEach { system ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSystem = system
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = system == selectedSystem,
                            onClick = {
                                selectedSystem = system
                                expanded = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.AccentPrimary,
                                unselectedColor = AppTheme.TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = system.getLocalizedName(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (system == selectedSystem) AppTheme.TextPrimary else AppTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Language Selection Setting
 */
@Composable
private fun LanguageSetting(localizationManager: LocalizationManager?) {
    val currentLanguage by localizationManager?.language?.collectAsState() ?: remember { mutableStateOf(Language.DEFAULT) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.ChipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(StringKey.SETTINGS_LANGUAGE),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = currentLanguage.nativeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.AccentPrimary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            if (expanded) {
                HorizontalDivider(color = AppTheme.DividerColor)

                Language.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                localizationManager?.setLanguage(language)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = {
                                localizationManager?.setLanguage(language)
                                expanded = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.AccentPrimary,
                                unselectedColor = AppTheme.TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = language.nativeName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (language == currentLanguage) AppTheme.TextPrimary else AppTheme.TextSecondary
                            )
                            Text(
                                text = language.englishName,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Date System Selection Setting (AD/BS)
 */
@Composable
private fun DateSystemSetting(localizationManager: LocalizationManager?) {
    val language by localizationManager?.language?.collectAsState() ?: remember { mutableStateOf(Language.DEFAULT) }
    val currentDateSystem by localizationManager?.dateSystem?.collectAsState() ?: remember { mutableStateOf(DateSystem.DEFAULT) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.ChipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(StringKey.SETTINGS_DATE_SYSTEM),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = currentDateSystem.getDisplayName(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.AccentPrimary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            if (expanded) {
                HorizontalDivider(color = AppTheme.DividerColor)

                DateSystem.entries.forEach { dateSystem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                localizationManager?.setDateSystem(dateSystem)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = dateSystem == currentDateSystem,
                            onClick = {
                                localizationManager?.setDateSystem(dateSystem)
                                expanded = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.AccentPrimary,
                                unselectedColor = AppTheme.TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dateSystem.getDisplayName(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (dateSystem == currentDateSystem) AppTheme.TextPrimary else AppTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AyanamsaSetting() {
    val language = LocalLanguage.current
    val ayanamsaKeys = listOf(
        StringKey.AYANAMSA_LAHIRI,
        StringKey.AYANAMSA_RAMAN,
        StringKey.AYANAMSA_KRISHNAMURTI,
        StringKey.AYANAMSA_TRUE_CHITRAPAKSHA
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedAyanamsaKey by remember { mutableStateOf(ayanamsaKeys[0]) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.ChipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(StringKey.SETTINGS_AYANAMSA),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = stringResource(selectedAyanamsaKey),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.AccentPrimary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            if (expanded) {
                HorizontalDivider(color = AppTheme.DividerColor)

                ayanamsaKeys.forEach { key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedAyanamsaKey = key
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = key == selectedAyanamsaKey,
                            onClick = {
                                selectedAyanamsaKey = key
                                expanded = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.AccentPrimary,
                                unselectedColor = AppTheme.TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(key),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (key == selectedAyanamsaKey) AppTheme.TextPrimary else AppTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AstroStorm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.AccentPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(StringKey.SETTINGS_APP_TAGLINE),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(StringKey.SETTINGS_APP_DESC),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureBadge(textKey = StringKey.SETTINGS_LAHIRI)
                FeatureBadge(textKey = StringKey.SETTINGS_PLACIDUS)
            }
        }
    }
}

@Composable
private fun FeatureBadge(textKey: StringKey) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AppTheme.ChipBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(textKey),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
    }
}

@Composable
private fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    val language = LocalLanguage.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.CardBackground,
        titleContentColor = AppTheme.TextPrimary,
        title = {
            Text(
                text = stringResource(StringKey.DIALOG_EXPORT_CHART),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                ExportFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExport(format) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = format.icon,
                            contentDescription = null,
                            tint = AppTheme.AccentPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = format.getLocalizedTitle(language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.TextPrimary
                            )
                            Text(
                                text = format.getLocalizedDescription(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(StringKey.BTN_CANCEL), color = AppTheme.AccentPrimary)
            }
        }
    )
}

enum class ExportFormat(
    val titleKey: StringKey,
    val descriptionKey: StringKey,
    val icon: ImageVector
) {
    PDF(StringKey.SETTINGS_EXPORT_PDF, StringKey.SETTINGS_EXPORT_PDF_DESC, Icons.Outlined.PictureAsPdf),
    IMAGE(StringKey.SETTINGS_EXPORT_IMAGE, StringKey.SETTINGS_EXPORT_IMAGE_DESC, Icons.Outlined.Image),
    JSON(StringKey.SETTINGS_EXPORT_JSON, StringKey.SETTINGS_EXPORT_JSON_DESC, Icons.Outlined.Code),
    CSV(StringKey.SETTINGS_EXPORT_CSV, StringKey.SETTINGS_EXPORT_CSV_DESC, Icons.Outlined.TableChart),
    CLIPBOARD(StringKey.SETTINGS_EXPORT_CLIPBOARD, StringKey.SETTINGS_EXPORT_CLIPBOARD_DESC, Icons.Outlined.ContentCopy);

    fun getLocalizedTitle(language: Language): String = StringResources.get(titleKey, language)
    fun getLocalizedDescription(language: Language): String = StringResources.get(descriptionKey, language)
}
