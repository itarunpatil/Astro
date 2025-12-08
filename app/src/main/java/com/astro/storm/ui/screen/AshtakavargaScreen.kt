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
import com.astro.storm.localization.*
import com.astro.storm.ui.screen.chartdetail.tabs.AshtakavargaTabContent
import com.astro.storm.ui.theme.AppTheme

/**
 * Ashtakavarga Screen - Standalone screen for Ashtakavarga analysis
 *
 * Features:
 * - Sarvashtakavarga (SAV) - Combined strength of all planets in each sign
 * - Bhinnashtakavarga (BAV) - Individual planet strength in each sign
 * - Total bindus summary with strongest and weakest signs
 * - Quick analysis showing favorable and challenging signs
 * - Interactive tables with color-coded strength indicators
 * - Comprehensive interpretation guide for transit timing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshtakavargaScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    if (chart == null) {
        EmptyChartScreen(
            title = getString(StringKey.ASHTAKAVARGA_TITLE),
            message = getString(StringKey.ERROR_NO_DATA),
            onBack = onBack
        )
        return
    }

    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AshtakavargaInfoDialog(onDismiss = { showInfoDialog = false })
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            AshtakavargaTopBar(
                chartName = chart.birthData.name,
                onBack = onBack,
                onShowInfo = { showInfoDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            AshtakavargaTabContent(chart = chart)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AshtakavargaTopBar(
    chartName: String,
    onBack: () -> Unit,
    onShowInfo: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = getString(StringKey.ASHTAKAVARGA_TITLE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = chartName,
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
            IconButton(onClick = onShowInfo) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = getString(StringKey.ASHTAKAVARGA_INTERPRETATION),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}

@Composable
private fun AshtakavargaInfoDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "About Ashtakavarga",
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Ashtakavarga is an ancient Vedic astrology technique for assessing planetary strength and predicting transit effects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.TextSecondary
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = getString(StringKey.ASHTAKAVARGA_SARVASHTAKA),
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = "Combined strength points (bindus) from all planets in each zodiac sign. Higher values (28+) indicate favorable areas for transits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Bhinnashtakavarga (BAV)",
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = "Individual planet strength in each sign (0-8 bindus). Use this to predict how each planet's transit will affect you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(getString(StringKey.BACK), color = AppTheme.AccentGold)
            }
        },
        containerColor = AppTheme.CardBackground
    )
}
