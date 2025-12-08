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
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.localization.getLocalizedName
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.YogaCalculator
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
            title = stringResource(StringKey.FEATURE_YOGAS),
            message = stringResource(StringKey.NO_PROFILE_MESSAGE),
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
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(StringKey.FEATURE_YOGAS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = stringResource(StringKey.YOGAS_COUNT_DETECTED, yogaCount, chartName),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(StringKey.BTN_BACK),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(StringKey.YOGA_INFORMATION),
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
                text = stringResource(StringKey.YOGA_ABOUT_TITLE),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(StringKey.YOGA_ABOUT_DESCRIPTION),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.TextSecondary
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = stringResource(StringKey.YOGA_CATEGORIES_TITLE),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.AccentGold
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_RAJA),
                    stringResource(StringKey.YOGA_CATEGORY_RAJA_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_DHANA),
                    stringResource(StringKey.YOGA_CATEGORY_DHANA_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_MAHAPURUSHA),
                    stringResource(StringKey.YOGA_CATEGORY_MAHAPURUSHA_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_NABHASA),
                    stringResource(StringKey.YOGA_CATEGORY_NABHASA_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_CHANDRA),
                    stringResource(StringKey.YOGA_CATEGORY_CHANDRA_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_SOLAR),
                    stringResource(StringKey.YOGA_CATEGORY_SOLAR_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_SPECIAL),
                    stringResource(StringKey.YOGA_CATEGORY_SPECIAL_DESC)
                )
                YogaCategoryInfo(
                    stringResource(StringKey.YOGA_CATEGORY_NEGATIVE),
                    stringResource(StringKey.YOGA_CATEGORY_NEGATIVE_DESC)
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(StringKey.YOGA_GOT_IT),
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
