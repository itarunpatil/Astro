package com.astro.storm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.TransitAnalyzer
import com.astro.storm.ui.screen.chartdetail.tabs.TransitsTabContent
import com.astro.storm.ui.theme.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Transits Screen - Standalone screen for current planetary transits analysis
 *
 * Features:
 * - Current planetary positions with retrograde indicators
 * - Transit overview with favorable/challenging counts
 * - Overall transit score with visual indicator
 * - Gochara (Moon-based transit) analysis with vedha effects
 * - Transit aspects to natal chart positions
 * - Significant upcoming transit periods
 * - Refresh capability for real-time updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitsScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    if (chart == null) {
        EmptyChartScreen(
            title = stringResource(StringKey.FEATURE_TRANSITS),
            message = stringResource(StringKey.NO_PROFILE_MESSAGE),
            onBack = onBack
        )
        return
    }

    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }

    val transitAnalysis = remember(chart, refreshKey) {
        val analyzer = TransitAnalyzer(context)
        try {
            analyzer.analyzeTransits(chart)
        } finally {
            analyzer.close()
        }
    }

    val currentDate = remember(refreshKey) {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            TransitsTopBar(
                chartName = chart.birthData.name,
                currentDate = currentDate,
                onBack = onBack,
                onRefresh = { refreshKey++ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            TransitsTabContent(chart = chart)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransitsTopBar(
    chartName: String,
    currentDate: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(StringKey.FEATURE_TRANSITS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = "$currentDate • $chartName",
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
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(StringKey.FEATURE_TRANSITS),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}
