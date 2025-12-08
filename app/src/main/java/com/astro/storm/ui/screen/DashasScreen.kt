package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.getLocalizedName
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.VedicChart
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
        chart?.generateUniqueKey()
    }

    LaunchedEffect(chartKey) {
        viewModel.loadDashaTimeline(chart)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language = LocalLanguage.current

    val currentPeriodInfo = remember(uiState, language) {
        extractCurrentPeriodInfo(uiState, language)
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            DashasTopBar(
                chartName = chart?.birthData?.name ?: stringResource(StringKey.MISC_UNKNOWN),
                currentPeriodInfo = currentPeriodInfo,
                onBack = onBack,
                showJumpToToday = uiState is DashaUiState.Success,
                onJumpToToday = { viewModel.requestScrollToToday() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(200))
                },
                label = "DashaStateTransition",
                contentKey = { state -> state::class.simpleName }
            ) { state ->
                when (state) {
                    is DashaUiState.Loading -> {
                        DashaLoadingContent()
                    }
                    is DashaUiState.Success -> {
                        DashasTabContent(
                            timeline = state.timeline,
                            scrollToTodayEvent = viewModel.scrollToTodayEvent
                        )
                    }
                    is DashaUiState.Error -> {
                        DashaErrorContent(
                            message = state.message,
                            onRetry = { viewModel.loadDashaTimeline(chart) }
                        )
                    }
                    is DashaUiState.Idle -> {
                        DashaEmptyContent(onBack = onBack)
                    }
                }
            }
        }
    }
}

private fun VedicChart.generateUniqueKey(): String {
    return buildString {
        append(birthData.dateTime.toString())
        append("|")
        append(String.format("%.6f", birthData.latitude))
        append("|")
        append(String.format("%.6f", birthData.longitude))
    }
}

private data class CurrentPeriodInfo(
    val mahadasha: String?,
    val antardasha: String?,
    val isLoading: Boolean,
    val hasError: Boolean
)

private fun extractCurrentPeriodInfo(uiState: DashaUiState, language: Language): CurrentPeriodInfo {
    return when (uiState) {
        is DashaUiState.Success -> {
            val md = uiState.timeline.currentMahadasha
            val ad = uiState.timeline.currentAntardasha
            CurrentPeriodInfo(
                mahadasha = md?.planet?.getLocalizedName(language),
                antardasha = ad?.planet?.getLocalizedName(language),
                isLoading = false,
                hasError = false
            )
        }
        is DashaUiState.Loading -> CurrentPeriodInfo(null, null, true, false)
        is DashaUiState.Error -> CurrentPeriodInfo(null, null, false, true)
        is DashaUiState.Idle -> CurrentPeriodInfo(null, null, false, false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashasTopBar(
    chartName: String,
    currentPeriodInfo: CurrentPeriodInfo,
    onBack: () -> Unit,
    showJumpToToday: Boolean,
    onJumpToToday: () -> Unit
) {
    Surface(
        color = AppTheme.ScreenBackground,
        shadowElevation = 2.dp
    ) {
        TopAppBar(
            title = {
                Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                    Text(
                        text = stringResource(StringKey.DASHA_VIMSHOTTARI),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    TopBarSubtitle(
                        chartName = chartName,
                        periodInfo = currentPeriodInfo
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
                AnimatedVisibility(visible = showJumpToToday) {
                    IconButton(onClick = onJumpToToday) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = stringResource(StringKey.DASHA_JUMP_TO_TODAY),
                            tint = AppTheme.AccentPrimary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppTheme.ScreenBackground,
                scrolledContainerColor = AppTheme.ScreenBackground
            )
        )
    }
}

@Composable
private fun TopBarSubtitle(
    chartName: String,
    periodInfo: CurrentPeriodInfo
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            periodInfo.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = AppTheme.AccentPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(StringKey.DASHA_CALCULATING),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted,
                    fontSize = 12.sp
                )
            }
            periodInfo.hasError -> {
                Text(
                    text = "${stringResource(StringKey.DASHA_ERROR)} • $chartName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            periodInfo.mahadasha != null -> {
                Text(
                    text = buildString {
                        append(periodInfo.mahadasha)
                        periodInfo.antardasha?.let { append(" → $it") }
                        append(" • $chartName")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            else -> {
                Text(
                    text = chartName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DashaLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppTheme.AccentPrimary.copy(alpha = 0.15f),
                                AppTheme.AccentPrimary.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = AppTheme.AccentPrimary,
                    strokeWidth = 3.dp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(StringKey.DASHA_CALCULATING_TIMELINE),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(StringKey.DASHA_CALCULATING_DESC),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DashaErrorContent(
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(StringKey.DASHA_CALCULATION_FAILED),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.AccentPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(StringKey.BTN_TRY_AGAIN),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DashaEmptyContent(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppTheme.CardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppTheme.TextMuted
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(StringKey.DASHA_NO_CHART_SELECTED),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(StringKey.DASHA_NO_CHART_MESSAGE),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(StringKey.BTN_GO_BACK),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}