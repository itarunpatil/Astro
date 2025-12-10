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
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ui.components.ProfileHeaderRow
import com.astro.storm.ui.components.ProfileSwitcherBottomSheet
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.ChartUiState
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
    onNavigateToPrashna: () -> Unit = {},
    onNavigateToBirthChart: () -> Unit = {},
    onNavigateToPlanets: () -> Unit = {},
    onNavigateToYogas: () -> Unit = {},
    onNavigateToDashas: () -> Unit = {},
    onNavigateToTransits: () -> Unit = {},
    onNavigateToAshtakavarga: () -> Unit = {},
    onNavigateToPanchanga: () -> Unit = {},
    onNavigateToProfileEdit: () -> Unit = {},
    onExportChart: (ExportFormat) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var showProfileSwitcher by remember { mutableStateOf(false) }
    val profileSheetState = rememberModalBottomSheetState()
    val language = LocalLanguage.current

    // Observe UI state for export feedback
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track the last export state for proper snackbar styling
    var lastExportState by remember { mutableStateOf<ChartUiState?>(null) }

    // Show snackbar for export states
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ChartUiState.Exporting -> {
                lastExportState = state
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Indefinite
                )
            }
            is ChartUiState.Exported -> {
                lastExportState = state
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                // Reset state after showing the snackbar (delay to avoid race condition)
                kotlinx.coroutines.delay(100)
                viewModel.resetState()
            }
            is ChartUiState.Error -> {
                lastExportState = state
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {
                // Dismiss any existing snackbar when state changes to something else
                snackbarHostState.currentSnackbarData?.dismiss()
                // Clear last export state when we're back to normal
                if (uiState is ChartUiState.Success || uiState is ChartUiState.Initial) {
                    lastExportState = null
                }
            }
        }
    }

    // Find the current SavedChart for display
    val currentSavedChart = savedCharts.find { it.id == selectedChartId }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Use lastExportState for styling to avoid race conditions with state reset
                val effectiveState = lastExportState ?: uiState
                Snackbar(
                    snackbarData = data,
                    containerColor = when (effectiveState) {
                        is ChartUiState.Error -> AppTheme.ErrorColor
                        is ChartUiState.Exported -> AppTheme.SuccessColor
                        else -> AppTheme.CardBackground
                    },
                    contentColor = when (effectiveState) {
                        is ChartUiState.Error, is ChartUiState.Exported -> AppTheme.TextPrimary
                        else -> AppTheme.TextSecondary
                    },
                    actionColor = AppTheme.AccentPrimary
                )
            }
        },
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
                                        InsightFeature.PRASHNA -> onNavigateToPrashna()
                                        // Features that require a chart - navigate to individual screens
                                        InsightFeature.FULL_CHART -> if (currentChart != null) onNavigateToBirthChart()
                                        InsightFeature.PLANETS -> if (currentChart != null) onNavigateToPlanets()
                                        InsightFeature.YOGAS -> if (currentChart != null) onNavigateToYogas()
                                        InsightFeature.DASHAS -> if (currentChart != null) onNavigateToDashas()
                                        InsightFeature.TRANSITS -> if (currentChart != null) onNavigateToTransits()
                                        InsightFeature.ASHTAKAVARGA -> if (currentChart != null) onNavigateToAshtakavarga()
                                        InsightFeature.PANCHANGA -> if (currentChart != null) onNavigateToPanchanga()
                                        InsightFeature.REMEDIES -> if (currentChart != null) onNavigateToRemedies()
                                        InsightFeature.VARSHAPHALA -> if (currentChart != null) onNavigateToVarshaphala()
                                        // Fallback to chart analysis for any remaining features
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
                            onEditProfile = onNavigateToProfileEdit,
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
    val language = LocalLanguage.current
    TopAppBar(
        title = {
            Text(
                text = currentTab.getLocalizedTitle(language),
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
    val language = LocalLanguage.current
    NavigationBar(
        containerColor = AppTheme.NavBarBackground,
        contentColor = AppTheme.TextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val localizedTitle = tab.getLocalizedTitle(language)

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = localizedTitle,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = localizedTitle,
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
    val titleKey: StringKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        titleKey = StringKey.TAB_HOME,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    INSIGHTS(
        titleKey = StringKey.TAB_INSIGHTS,
        selectedIcon = Icons.Filled.Insights,
        unselectedIcon = Icons.Outlined.Insights
    ),
    SETTINGS(
        titleKey = StringKey.TAB_SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    );

    /**
     * Get localized title for the tab
     */
    fun getLocalizedTitle(language: com.astro.storm.data.localization.Language): String {
        return StringResources.get(titleKey, language)
    }
}
