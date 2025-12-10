package com.astro.storm.ui.screen

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.DivisionalChartCalculator
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ephemeris.DivisionalChartType
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.components.FullScreenChartDialog
import com.astro.storm.ui.components.HouseDetailDialog
import com.astro.storm.ui.components.NakshatraDetailDialog
import com.astro.storm.ui.components.PlanetDetailDialog
import com.astro.storm.ui.components.ShadbalaDialog
import com.astro.storm.ui.screen.chartdetail.tabs.*
import com.astro.storm.ui.screen.main.InsightFeature
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.ChartViewModel

/**
 * Chart Analysis Screen - Clean Navigation for Chart Details
 *
 * This screen provides access to all the detailed chart analysis features
 * with a modern, clean navigation approach using horizontal scrolling tabs
 * instead of the cluttered bottom navigation.
 *
 * Features:
 * - Horizontal scrolling tab bar for feature navigation
 * - Clean top app bar with back navigation
 * - Full-screen content area for each analysis type
 * - Maintains all existing functionality with improved UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartAnalysisScreen(
    chart: VedicChart,
    initialFeature: InsightFeature = InsightFeature.FULL_CHART,
    viewModel: ChartViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(mapFeatureToTab(initialFeature)) }
    val context = LocalContext.current
    val chartRenderer = remember { ChartRenderer() }

    // Dialog states - managed at the top level for all tabs
    var showFullScreenChart by remember { mutableStateOf(false) }
    var fullScreenChartTitle by remember { mutableStateOf("Lagna") }
    var fullScreenDivisionalData by remember { mutableStateOf<DivisionalChartData?>(null) }
    var showShadbalaDialog by remember { mutableStateOf(false) }
    var selectedPlanetPosition by remember { mutableStateOf<PlanetPosition?>(null) }
    var selectedNakshatra by remember { mutableStateOf<Pair<Nakshatra, Int>?>(null) }
    var selectedHouse by remember { mutableStateOf<Int?>(null) }

    // Render dialogs
    if (showFullScreenChart) {
        FullScreenChartDialog(
            chart = chart,
            chartRenderer = chartRenderer,
            chartTitle = fullScreenChartTitle,
            divisionalChartData = fullScreenDivisionalData,
            onDismiss = { showFullScreenChart = false }
        )
    }

    if (showShadbalaDialog) {
        ShadbalaDialog(
            chart = chart,
            onDismiss = { showShadbalaDialog = false }
        )
    }

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

    selectedHouse?.let { houseNum ->
        val houseCusp = if (houseNum in 1..chart.houseCusps.size) chart.houseCusps[houseNum - 1] else 0.0
        val planetsInHouse = chart.planetPositions.filter { it.house == houseNum }
        HouseDetailDialog(
            houseNumber = houseNum,
            houseCusp = houseCusp,
            planetsInHouse = planetsInHouse,
            chart = chart,
            onDismiss = { selectedHouse = null }
        )
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            ChartAnalysisTopBar(
                chartName = chart.birthData.name,
                onBack = onBack,
                onExport = {
                    // Export functionality
                    viewModel.copyChartToClipboard(chart)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Tab Bar
            AnalysisTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.ScreenBackground)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    label = "analysis_content"
                ) { tab ->
                    when (tab) {
                        AnalysisTab.CHART -> ChartTabContentWrapper(
                            chart = chart,
                            chartRenderer = chartRenderer,
                            onChartClick = { title, divisionalData ->
                                fullScreenChartTitle = title
                                fullScreenDivisionalData = divisionalData
                                showFullScreenChart = true
                            },
                            onPlanetClick = { selectedPlanetPosition = it },
                            onHouseClick = { selectedHouse = it }
                        )
                        AnalysisTab.PLANETS -> PlanetsTabContentWrapper(
                            chart = chart,
                            onPlanetClick = { selectedPlanetPosition = it },
                            onNakshatraClick = { nakshatra, pada -> selectedNakshatra = nakshatra to pada },
                            onShadbalaClick = { showShadbalaDialog = true }
                        )
                        AnalysisTab.YOGAS -> YogasTabContentWrapper(chart)
                        AnalysisTab.DASHAS -> DashasTabContentWrapper(chart)
                        AnalysisTab.TRANSITS -> TransitsTabContentWrapper(chart)
                        AnalysisTab.ASHTAKAVARGA -> AshtakavargaTabContentWrapper(chart)
                        AnalysisTab.PANCHANGA -> PanchangaTabContentWrapper(chart)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartAnalysisTopBar(
    chartName: String,
    onBack: () -> Unit,
    onExport: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(StringKey.ANALYSIS_CHART_ANALYSIS),
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(StringKey.NAV_BACK),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(StringKey.ACTION_EXPORT),
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
private fun AnalysisTabBar(
    selectedTab: AnalysisTab,
    onTabSelected: (AnalysisTab) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(AppTheme.ScreenBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalysisTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = tab.localizedTitle(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AppTheme.CardBackground,
                    labelColor = AppTheme.TextSecondary,
                    iconColor = AppTheme.TextMuted,
                    selectedContainerColor = AppTheme.AccentPrimary,
                    selectedLabelColor = AppTheme.ButtonText,
                    selectedLeadingIconColor = AppTheme.ButtonText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = AppTheme.BorderColor,
                    selectedBorderColor = AppTheme.AccentPrimary,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

/**
 * Analysis tabs available in the chart analysis screen
 */
enum class AnalysisTab(
    val stringKey: StringKey,
    val icon: ImageVector
) {
    CHART(StringKey.ANALYSIS_TAB_CHART, Icons.Outlined.GridView),
    PLANETS(StringKey.ANALYSIS_TAB_PLANETS, Icons.Outlined.Public),
    YOGAS(StringKey.ANALYSIS_TAB_YOGAS, Icons.Outlined.AutoAwesome),
    DASHAS(StringKey.ANALYSIS_TAB_DASHAS, Icons.Outlined.Timeline),
    TRANSITS(StringKey.ANALYSIS_TAB_TRANSITS, Icons.Outlined.Sync),
    ASHTAKAVARGA(StringKey.ANALYSIS_TAB_ASHTAKAVARGA, Icons.Outlined.BarChart),
    PANCHANGA(StringKey.ANALYSIS_TAB_PANCHANGA, Icons.Outlined.CalendarMonth)
}

/**
 * Get localized title for analysis tab
 */
@Composable
fun AnalysisTab.localizedTitle(): String = stringResource(stringKey)

private fun mapFeatureToTab(feature: InsightFeature): AnalysisTab {
    return when (feature) {
        InsightFeature.FULL_CHART -> AnalysisTab.CHART
        InsightFeature.PLANETS -> AnalysisTab.PLANETS
        InsightFeature.YOGAS -> AnalysisTab.YOGAS
        InsightFeature.DASHAS -> AnalysisTab.DASHAS
        InsightFeature.TRANSITS -> AnalysisTab.TRANSITS
        InsightFeature.ASHTAKAVARGA -> AnalysisTab.ASHTAKAVARGA
        InsightFeature.PANCHANGA -> AnalysisTab.PANCHANGA
        else -> AnalysisTab.CHART
    }
}

// Content wrappers that integrate with existing tab content components

@Composable
private fun ChartTabContentWrapper(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    onChartClick: (String, DivisionalChartData?) -> Unit,
    onPlanetClick: (PlanetPosition) -> Unit,
    onHouseClick: (Int) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        ChartTabContent(
            chart = chart,
            chartRenderer = chartRenderer,
            onChartClick = onChartClick,
            onPlanetClick = onPlanetClick,
            onHouseClick = onHouseClick
        )
    }
}

@Composable
private fun PlanetsTabContentWrapper(
    chart: VedicChart,
    onPlanetClick: (PlanetPosition) -> Unit,
    onNakshatraClick: (Nakshatra, Int) -> Unit,
    onShadbalaClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        PlanetsTabContent(
            chart = chart,
            onPlanetClick = onPlanetClick,
            onNakshatraClick = onNakshatraClick,
            onShadbalaClick = onShadbalaClick
        )
    }
}

@Composable
private fun YogasTabContentWrapper(chart: VedicChart) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        YogasTabContent(chart = chart)
    }
}

@Composable
private fun DashasTabContentWrapper(chart: VedicChart) {
    val timeline = remember(chart) { DashaCalculator.calculateDashaTimeline(chart) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        DashasTabContent(timeline = timeline)
    }
}

@Composable
private fun TransitsTabContentWrapper(chart: VedicChart) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        TransitsTabContent(chart = chart)
    }
}

@Composable
private fun AshtakavargaTabContentWrapper(chart: VedicChart) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        AshtakavargaTabContent(chart = chart)
    }
}

@Composable
private fun PanchangaTabContentWrapper(chart: VedicChart) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
    ) {
        PanchangaTabContent(chart = chart)
    }
}
