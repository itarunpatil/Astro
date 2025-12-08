package com.astro.storm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.PanchangaCalculator
import com.astro.storm.ui.screen.chartdetail.tabs.PanchangaTabContent
import com.astro.storm.ui.theme.AppTheme
import java.time.format.DateTimeFormatter

/**
 * Panchanga Screen - Standalone screen for Panchanga (Hindu calendar) analysis
 *
 * Features:
 * - Complete Panchanga at birth time showing all five elements
 * - Tithi (Lunar Day) - 30 tithis with deity, nature, and activities
 * - Nakshatra (Lunar Mansion) - 27 nakshatras with characteristics
 * - Yoga (Luni-Solar Combination) - 27 yogas with effects
 * - Karana (Half Tithi) - 11 karanas with significance
 * - Vara (Weekday) - 7 varas with planetary rulership
 * - Sunrise/Sunset times and moon phase visualization
 * - Detailed interpretations and activity recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanchangaScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    if (chart == null) {
        EmptyChartScreen(
            title = stringResource(StringKey.FEATURE_PANCHANGA),
            message = stringResource(StringKey.NO_PROFILE_MESSAGE),
            onBack = onBack
        )
        return
    }

    val context = LocalContext.current

    val panchangaSummary = remember(chart) {
        PanchangaCalculator(context).use { calculator ->
            val panchanga = calculator.calculatePanchanga(
                dateTime = chart.birthData.dateTime,
                latitude = chart.birthData.latitude,
                longitude = chart.birthData.longitude,
                timezone = chart.birthData.timezone
            )
            "${panchanga.tithi.tithi.displayName} | ${panchanga.nakshatra.nakshatra.displayName}"
        }
    }

    val birthDateFormatted = remember(chart) {
        chart.birthData.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            PanchangaTopBar(
                chartName = chart.birthData.name,
                birthDate = birthDateFormatted,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            PanchangaTabContent(chart = chart)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanchangaTopBar(
    chartName: String,
    birthDate: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(StringKey.FEATURE_PANCHANGA),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = "$birthDate - $chartName",
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
            IconButton(onClick = { /* Reserved for future: current day panchanga */ }) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = stringResource(StringKey.FEATURE_PANCHANGA),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}
