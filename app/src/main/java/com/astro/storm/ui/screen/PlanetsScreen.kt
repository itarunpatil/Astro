package com.astro.storm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
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
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.components.NakshatraDetailDialog
import com.astro.storm.ui.components.PlanetDetailDialog
import com.astro.storm.ui.components.ShadbalaDialog
import com.astro.storm.ui.screen.chartdetail.tabs.PlanetsTabContent
import com.astro.storm.ui.theme.AppTheme

/**
 * Planets Screen - Standalone screen for detailed planetary analysis
 *
 * Features:
 * - Detailed planetary positions and states
 * - Retrograde, combust, and planetary war indicators
 * - Dignity status (exaltation, debilitation, own sign)
 * - Shadbala (six-fold strength) analysis
 * - Nakshatra and pada information
 * - Interactive planet cards with detailed dialog views
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetsScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    if (chart == null) {
        EmptyChartScreen(
            title = stringResource(StringKey.FEATURE_PLANETS),
            message = stringResource(StringKey.NO_PROFILE_MESSAGE),
            onBack = onBack
        )
        return
    }

    // Dialog states
    var selectedPlanetPosition by remember { mutableStateOf<PlanetPosition?>(null) }
    var selectedNakshatra by remember { mutableStateOf<Pair<Nakshatra, Int>?>(null) }
    var showShadbalaDialog by remember { mutableStateOf(false) }

    // Render dialogs
    selectedPlanetPosition?.let { position ->
        PlanetDetailDialog(
            planetPosition = position,
            chart = chart,
            onDismiss = { selectedPlanetPosition = null }
        )
    }

    selectedNakshatra?.let { (nakshatra, pada) ->
        NakshatraDetailDialog(
            nakshatra = nakshatra,
            pada = pada,
            onDismiss = { selectedNakshatra = null }
        )
    }

    if (showShadbalaDialog) {
        ShadbalaDialog(
            chart = chart,
            onDismiss = { showShadbalaDialog = false }
        )
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            PlanetsTopBar(
                chartName = chart.birthData.name,
                onBack = onBack,
                onShowShadbala = { showShadbalaDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            PlanetsTabContent(
                chart = chart,
                onPlanetClick = { selectedPlanetPosition = it },
                onNakshatraClick = { nakshatra, pada ->
                    selectedNakshatra = nakshatra to pada
                },
                onShadbalaClick = { showShadbalaDialog = true }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanetsTopBar(
    chartName: String,
    onBack: () -> Unit,
    onShowShadbala: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(StringKey.FEATURE_PLANETS),
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
                    contentDescription = stringResource(StringKey.BTN_BACK),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onShowShadbala) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = stringResource(StringKey.FEATURE_SHADBALA),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}
