package com.astro.storm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ui.screen.chartdetail.tabs.DashasTabContent
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.DashaUiState
import com.astro.storm.ui.viewmodel.DashaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashasScreen(
    chart: VedicChart?,
    onBack: () -> Unit,
    viewModel: DashaViewModel = viewModel()
) {
    val chartKey = remember(chart) {
        chart?.let {
            "${it.birthData.dateTime}|${it.birthData.latitude}|${it.birthData.longitude}"
        }
    }

    LaunchedEffect(chartKey) {
        viewModel.loadDashaTimeline(chart)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentPeriodText by remember(uiState) {
        derivedStateOf {
            when (val state = uiState) {
                is DashaUiState.Success -> formatCurrentPeriod(state.timeline)
                is DashaUiState.Loading -> "Loading..."
                is DashaUiState.Error -> "Error"
                is DashaUiState.Idle -> "No data"
            }
        }
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            DashasTopBar(
                chartName = chart?.birthData?.name ?: "Dashas",
                currentPeriod = currentPeriodText,
                onBack = onBack,
                onJumpToToday = { }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            when (val state = uiState) {
                is DashaUiState.Loading -> {
                    LoadingContent()
                }
                is DashaUiState.Success -> {
                    DashasTabContent(timeline = state.timeline)
                }
                is DashaUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadDashaTimeline(chart) }
                    )
                }
                is DashaUiState.Idle -> {
                    if (chart == null) {
                        EmptyChartScreen(
                            title = "Dashas",
                            message = "No chart data available. Please select or create a profile first.",
                            onBack = onBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = AppTheme.AccentPrimary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Calculating Dasha Timeline...",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Calculation Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun formatCurrentPeriod(timeline: DashaCalculator.DashaTimeline): String {
    return buildString {
        timeline.currentMahadasha?.let { md ->
            append(md.planet.displayName)
            timeline.currentAntardasha?.let { ad ->
                append(" → ${ad.planet.displayName}")
            }
        } ?: append("Current period")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashasTopBar(
    chartName: String,
    currentPeriod: String,
    onBack: () -> Unit,
    onJumpToToday: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Dashas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    text = "$currentPeriod • $chartName",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppTheme.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onJumpToToday) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Jump to Today",
                    tint = AppTheme.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}