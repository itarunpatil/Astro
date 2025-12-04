package com.astro.storm.ui.screen.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ui.components.ProfileHeaderRow
import com.astro.storm.ui.components.ProfileSwitcherBottomSheet
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.ChartViewModel
import kotlinx.coroutines.launch

/**
 * Main Screen with Bottom Navigation
 *
 * This is the primary screen of the redesigned AstroStorm app.
 * It contains three tabs:
 * - Home: Daily/Weekly horoscope predictions
 * - Insights: Planetary periods, transits, and chart analysis options
 * - Settings: Profile management and app settings
 *
 * Features a GitHub-style profile switcher in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ChartViewModel,
    savedCharts: List<SavedChart>,
    currentChart: VedicChart?,
    selectedChartId: Long?,
    onChartSelected: (Long) -> Unit,
    onAddNewChart: () -> Unit,
    onNavigateToChartAnalysis: (InsightFeature) -> Unit,
    onNavigateToMatchmaking: () -> Unit = {},
    onNavigateToMuhurta: () -> Unit = {},
    onNavigateToRemedies: () -> Unit = {},
    onNavigateToVarshaphala: () -> Unit = {},
    onExportChart: (ExportFormat) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var showProfileSwitcher by remember { mutableStateOf(false) }
    val profileSheetState = rememberModalBottomSheetState()

    // Find the current SavedChart for display
    val currentSavedChart = savedCharts.find { it.id == selectedChartId }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            MainTopBar(
                currentTab = selectedTab,
                currentChart = currentSavedChart,
                onProfileClick = { showProfileSwitcher = true }
            )
        },
        bottomBar = {
            MainBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab content with crossfade animation
            Crossfade(
                targetState = selectedTab,
                label = "tab_crossfade"
            ) { tab ->
                when (tab) {
                    MainTab.HOME -> {
                        HomeTab(
                            chart = currentChart,
                            onFeatureClick = { feature ->
                                if (feature.isImplemented) {
                                    when (feature) {
                                        // Features that don't require a chart
                                        InsightFeature.MATCHMAKING -> onNavigateToMatchmaking()
                                        InsightFeature.MUHURTA -> onNavigateToMuhurta()
                                        // Features that require a chart
                                        InsightFeature.REMEDIES -> if (currentChart != null) onNavigateToRemedies()
                                        InsightFeature.VARSHAPHALA -> if (currentChart != null) onNavigateToVarshaphala()
                                        // Standard chart analysis features
                                        else -> if (currentChart != null) onNavigateToChartAnalysis(feature)
                                    }
                                }
                            }
                        )
                    }
                    MainTab.INSIGHTS -> {
                        InsightsTab(
                            chart = currentChart,
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsTab(
                            currentChart = currentChart,
                            savedCharts = savedCharts,
                            onEditProfile = { /* Edit profile */ },
                            onDeleteProfile = { chartId ->
                                viewModel.deleteChart(chartId)
                            },
                            onExportChart = onExportChart,
                            onManageProfiles = { showProfileSwitcher = true }
                        )
                    }
                }
            }
        }
    }

    // Profile Switcher Bottom Sheet
    if (showProfileSwitcher) {
        ProfileSwitcherBottomSheet(
            savedCharts = savedCharts,
            selectedChartId = selectedChartId,
            onChartSelected = { chart ->
                onChartSelected(chart.id)
                scope.launch {
                    profileSheetState.hide()
                    showProfileSwitcher = false
                }
            },
            onAddNewChart = {
                scope.launch {
                    profileSheetState.hide()
                    showProfileSwitcher = false
                }
                onAddNewChart()
            },
            onDismiss = { showProfileSwitcher = false },
            sheetState = profileSheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    currentTab: MainTab,
    currentChart: SavedChart?,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = currentTab.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
        },
        actions = {
            ProfileHeaderRow(
                currentChart = currentChart,
                onProfileClick = onProfileClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground,
            titleContentColor = AppTheme.TextPrimary
        )
    )
}

@Composable
private fun MainBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = AppTheme.NavBarBackground,
        contentColor = AppTheme.TextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppTheme.NavItemSelected,
                    selectedTextColor = AppTheme.NavItemSelected,
                    unselectedIconColor = AppTheme.NavItemUnselected,
                    unselectedTextColor = AppTheme.NavItemUnselected,
                    indicatorColor = AppTheme.NavIndicator
                )
            )
        }
    }
}

/**
 * Main navigation tabs
 */
enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    INSIGHTS(
        title = "Insights",
        selectedIcon = Icons.Filled.Insights,
        unselectedIcon = Icons.Outlined.Insights
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}
