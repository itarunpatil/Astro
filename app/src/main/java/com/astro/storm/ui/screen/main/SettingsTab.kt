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
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.localization.*
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
            SettingsSectionHeader(title = "Profile")
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
                    title = "Edit Profile",
                    subtitle = "Modify birth details",
                    onClick = onEditProfile
                )
            } else {
                SettingsItem(
                    icon = Icons.Outlined.People,
                    title = "Manage Profiles",
                    subtitle = "No profile selected",
                    onClick = onManageProfiles
                )
            }
        }

        // Export Section
        if (currentChart != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = "Export")
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.PictureAsPdf,
                    title = "Export as PDF",
                    subtitle = "Complete chart report",
                    onClick = { onExportChart(ExportFormat.PDF) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Image,
                    title = "Export as Image",
                    subtitle = "High-quality chart image",
                    onClick = { onExportChart(ExportFormat.IMAGE) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.ContentCopy,
                    title = "Copy to Clipboard",
                    subtitle = "Plain text format",
                    onClick = { onExportChart(ExportFormat.CLIPBOARD) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Code,
                    title = "Export as JSON",
                    subtitle = "Machine-readable format",
                    onClick = { onExportChart(ExportFormat.JSON) }
                )
            }
        }

        // Preferences Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(title = getString(StringKey.SETTINGS_TITLE))
        }

        item {
            LanguageSetting()
        }

        item {
            DateSystemSetting()
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
            SettingsSectionHeader(title = "About")
        }

        item {
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "About AstroStorm",
                subtitle = "Version 1.0.0",
                onClick = { /* Show about dialog */ }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Outlined.Science,
                title = "Calculation Engine",
                subtitle = "Swiss Ephemeris (JPL Mode)",
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
            title = { Text("Delete Profile") },
            text = {
                Text("Are you sure you want to delete ${chartToDelete?.name}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chartToDelete?.let { onDeleteProfile(it.id) }
                        showDeleteDialog = false
                        chartToDelete = null
                    }
                ) {
                    Text("Delete", color = AppTheme.ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = AppTheme.AccentPrimary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
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
                    Text(
                        text = chart.birthData.dateTime.toLocalDate().toString(),
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
                    label = "Ascendant",
                    value = chart.planetPositions.find { it.planet.displayName == "Sun" }?.sign?.displayName
                        ?: com.astro.storm.data.model.ZodiacSign.fromLongitude(chart.ascendant).displayName
                )
                ChartDetailItem(
                    label = "Moon Sign",
                    value = chart.planetPositions.find { it.planet == com.astro.storm.data.model.Planet.MOON }?.sign?.displayName
                        ?: "-"
                )
                ChartDetailItem(
                    label = "Nakshatra",
                    value = chart.planetPositions.find { it.planet == com.astro.storm.data.model.Planet.MOON }?.nakshatra?.displayName?.take(8)
                        ?: "-"
                )
            }
        }
    }
}

@Composable
private fun ChartDetailItem(
    label: String,
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
            text = label,
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
                text = "No profile selected",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to select or create a profile",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
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
                        text = "House System",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = selectedSystem.displayName,
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
                            text = system.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (system == selectedSystem) AppTheme.TextPrimary else AppTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSetting() {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by LocalizationManager.currentLanguage.collectAsState()

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
                        text = getString(StringKey.SETTINGS_LANGUAGE),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = currentLanguage.displayName,
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
                                LocalizationManager.setLanguage(language)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = {
                                LocalizationManager.setLanguage(language)
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
                                text = language.displayName,
                                style = MaterialTheme.typography.bodyMedium,
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

@Composable
private fun DateSystemSetting() {
    var expanded by remember { mutableStateOf(false) }
    val currentDateSystem by LocalizationManager.currentDateSystem.collectAsState()

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
                        text = getString(StringKey.SETTINGS_DATE_SYSTEM),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = getString(currentDateSystem.displayKey),
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
                                LocalizationManager.setDateSystem(dateSystem)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = dateSystem == currentDateSystem,
                            onClick = {
                                LocalizationManager.setDateSystem(dateSystem)
                                expanded = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.AccentPrimary,
                                unselectedColor = AppTheme.TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = getString(dateSystem.displayKey),
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
    val ayanamsaOptions = listOf("Lahiri", "Raman", "Krishnamurti", "True Chitrapaksha")
    var expanded by remember { mutableStateOf(false) }
    var selectedAyanamsa by remember { mutableStateOf(ayanamsaOptions[0]) }

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
                        text = "Ayanamsa",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        text = selectedAyanamsa,
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

                ayanamsaOptions.forEach { ayanamsa ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedAyanamsa = ayanamsa
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ayanamsa == selectedAyanamsa,
                            onClick = {
                                selectedAyanamsa = ayanamsa
                                expanded = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.AccentPrimary,
                                unselectedColor = AppTheme.TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = ayanamsa,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (ayanamsa == selectedAyanamsa) AppTheme.TextPrimary else AppTheme.TextSecondary
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
                text = "Ultra-Precision Vedic Astrology",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Powered by Swiss Ephemeris with JPL planetary data for astronomical-grade accuracy in all calculations.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureBadge(text = "Lahiri Ayanamsa")
                FeatureBadge(text = "Placidus Houses")
            }
        }
    }
}

@Composable
private fun FeatureBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AppTheme.ChipBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.CardBackground,
        titleContentColor = AppTheme.TextPrimary,
        title = {
            Text(
                text = "Export Chart",
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
                                text = format.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.TextPrimary
                            )
                            Text(
                                text = format.description,
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
                Text("Cancel", color = AppTheme.AccentPrimary)
            }
        }
    )
}

enum class ExportFormat(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    PDF("PDF Report", "Complete chart analysis", Icons.Outlined.PictureAsPdf),
    IMAGE("Chart Image", "High-quality PNG", Icons.Outlined.Image),
    JSON("JSON Data", "Machine-readable format", Icons.Outlined.Code),
    CSV("CSV Data", "Spreadsheet format", Icons.Outlined.TableChart),
    CLIPBOARD("Copy Text", "Plain text to clipboard", Icons.Outlined.ContentCopy)
}
