package com.astro.storm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.YogaCalculator
import com.astro.storm.localization.*
import com.astro.storm.ui.screen.chartdetail.tabs.YogasTabContent
import com.astro.storm.ui.theme.AppTheme

/**
 * Yogas Screen - Standalone screen for planetary yogas analysis
 *
 * Features:
 * - Complete yoga detection and analysis
 * - Category filtering (Raja, Dhana, Mahapurusha, Nabhasa, Chandra, Solar, Special, Negative)
 * - Yoga strength indicators and percentages
 * - Detailed yoga descriptions and effects
 * - Sanskrit names and activation periods
 * - Cancellation/mitigation factors for negative yogas
 * - Overall yoga strength summary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YogasScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    if (chart == null) {
        EmptyChartScreen(
            title = getString(StringKey.YOGAS_TITLE),
            message = getString(StringKey.ERROR_NO_DATA),
            onBack = onBack
        )
        return
    }

    val yogaAnalysis = remember(chart) {
        YogaCalculator.calculateYogas(chart)
    }

    var showInfoDialog by remember { mutableStateOf(false) }

    // Yoga info dialog
    if (showInfoDialog) {
        YogaInfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            YogasTopBar(
                chartName = chart.birthData.name,
                yogaCount = yogaAnalysis.allYogas.size,
                onBack = onBack,
                onInfoClick = { showInfoDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            YogasTabContent(chart = chart)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YogasTopBar(
    chartName: String,
    yogaCount: Int,
    onBack: () -> Unit,
    onInfoClick: () -> Unit
) {
    val yogasDetectedText = getString(StringKey.YOGAS_COUNT_DETECTED)
    TopAppBar(
        title = {
            Column {
                Text(
                    text = getString(StringKey.YOGAS_TITLE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = "$yogaCount $yogasDetectedText • $chartName",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = getString(StringKey.BACK),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = getString(StringKey.YOGAS_INFO_TITLE),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}

/**
 * Information dialog explaining yogas
 */
@Composable
private fun YogaInfoDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = getString(StringKey.YOGAS_INFO_TITLE),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = getString(StringKey.YOGAS_INFO_DESC),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.TextSecondary
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "${getString(StringKey.YOGAS_CATEGORY)}:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.AccentGold
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_RAJA), getString(StringKey.YOGA_CATEGORY_RAJA_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_DHANA), getString(StringKey.YOGA_CATEGORY_DHANA_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_MAHAPURUSHA), getString(StringKey.YOGA_CATEGORY_MAHAPURUSHA_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_NABHASA), getString(StringKey.YOGA_CATEGORY_NABHASA_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_CHANDRA), getString(StringKey.YOGA_CATEGORY_CHANDRA_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_SOLAR), getString(StringKey.YOGA_CATEGORY_SOLAR_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_SPECIAL), getString(StringKey.YOGA_CATEGORY_SPECIAL_DESC))
                YogaCategoryInfo(getString(StringKey.YOGA_CATEGORY_NEGATIVE), getString(StringKey.YOGA_CATEGORY_NEGATIVE_DESC))
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(
                    text = getString(StringKey.OK),
                    color = AppTheme.AccentPrimary
                )
            }
        },
        containerColor = AppTheme.CardBackground,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}

@Composable
private fun YogaCategoryInfo(
    name: String,
    description: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "• $name: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = AppTheme.TextPrimary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.TextMuted
        )
    }
}
