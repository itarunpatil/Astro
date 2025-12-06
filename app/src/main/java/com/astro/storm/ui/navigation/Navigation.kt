package com.astro.storm.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.screen.ChartAnalysisScreen
import com.astro.storm.ui.screen.ChartInputScreen
import com.astro.storm.ui.screen.MatchmakingScreen
import com.astro.storm.ui.screen.MuhurtaScreen
import com.astro.storm.ui.screen.RemediesScreen
import com.astro.storm.ui.screen.VarshaphalaScreen
import com.astro.storm.ui.screen.main.ExportFormat
import com.astro.storm.ui.screen.main.InsightFeature
import com.astro.storm.ui.screen.main.MainScreen
import com.astro.storm.ui.viewmodel.ChartViewModel

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object ChartInput : Screen("chart_input")
    object ChartAnalysis : Screen("chart_analysis/{chartId}/{feature}") {
        fun createRoute(chartId: Long, feature: InsightFeature = InsightFeature.FULL_CHART) =
            "chart_analysis/$chartId/${feature.name}"
    }

    // New feature screens
    object Matchmaking : Screen("matchmaking")
    object Muhurta : Screen("muhurta")
    object Remedies : Screen("remedies/{chartId}") {
        fun createRoute(chartId: Long) = "remedies/$chartId"
    }
    object Varshaphala : Screen("varshaphala/{chartId}") {
        fun createRoute(chartId: Long) = "varshaphala/$chartId"
    }

    // Legacy routes for backward compatibility
    object Home : Screen("home")
    object ChartDetail : Screen("chart_detail/{chartId}") {
        fun createRoute(chartId: Long) = "chart_detail/$chartId"
    }
}

/**
 * Main navigation graph - Redesigned
 *
 * The new navigation structure:
 * - Main: Primary screen with Home, Insights, Settings tabs
 * - ChartInput: Birth data input (unchanged)
 * - ChartAnalysis: Detailed chart analysis with horizontal tabs
 */
@Composable
fun AstroStormNavigation(
    navController: NavHostController,
    viewModel: ChartViewModel = viewModel()
) {
    val savedCharts by viewModel.savedCharts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current
    val selectedChartId by viewModel.selectedChartId.collectAsState()

    var currentChart by remember { mutableStateOf<VedicChart?>(null) }

    // Update current chart from UI state
    LaunchedEffect(uiState) {
        if (uiState is com.astro.storm.ui.viewmodel.ChartUiState.Success) {
            currentChart = (uiState as com.astro.storm.ui.viewmodel.ChartUiState.Success).chart
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        // Main screen with bottom navigation
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                savedCharts = savedCharts,
                currentChart = currentChart,
                selectedChartId = selectedChartId,
                onChartSelected = viewModel::loadChart,
                onAddNewChart = {
                    navController.navigate(Screen.ChartInput.route)
                },
                onNavigateToChartAnalysis = { feature ->
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.ChartAnalysis.createRoute(chartId, feature))
                    }
                },
                onNavigateToMatchmaking = {
                    navController.navigate(Screen.Matchmaking.route)
                },
                onNavigateToMuhurta = {
                    navController.navigate(Screen.Muhurta.route)
                },
                onNavigateToRemedies = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Remedies.createRoute(chartId))
                    }
                },
                onNavigateToVarshaphala = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Varshaphala.createRoute(chartId))
                    }
                },
                onExportChart = { format ->
                    currentChart?.let { chart ->
                        when (format) {
                            ExportFormat.PDF -> viewModel.exportChartToPdf(chart, density)
                            ExportFormat.IMAGE -> viewModel.exportChartToImage(chart, density)
                            ExportFormat.JSON -> viewModel.exportChartToJson(chart)
                            ExportFormat.CSV -> viewModel.exportChartToCsv(chart)
                            ExportFormat.CLIPBOARD -> viewModel.copyChartToClipboard(chart)
                        }
                    }
                }
            )
        }

        // Chart input screen (unchanged functionality, same screen)
        composable(Screen.ChartInput.route) {
            ChartInputScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChartCalculated = {
                    // After chart is saved, navigate back and it will auto-select
                    navController.popBackStack()
                }
            )
        }

        // Chart analysis screen with feature parameter
        composable(
            route = Screen.ChartAnalysis.route,
            arguments = listOf(
                navArgument("chartId") { type = NavType.LongType },
                navArgument("feature") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable
            val featureName = backStackEntry.arguments?.getString("feature") ?: InsightFeature.FULL_CHART.name
            val feature = try {
                InsightFeature.valueOf(featureName)
            } catch (e: Exception) {
                InsightFeature.FULL_CHART
            }

            // Load chart if not already loaded
            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            currentChart?.let { chart ->
                ChartAnalysisScreen(
                    chart = chart,
                    initialFeature = feature,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Matchmaking screen
        composable(Screen.Matchmaking.route) {
            MatchmakingScreen(
                savedCharts = savedCharts.map { savedChart ->
                    com.astro.storm.data.local.ChartEntity(
                        id = savedChart.id,
                        name = savedChart.name,
                        dateTime = savedChart.dateTime,
                        latitude = 0.0,
                        longitude = 0.0,
                        timezone = "",
                        location = savedChart.location,
                        julianDay = 0.0,
                        ayanamsa = 0.0,
                        ayanamsaName = "",
                        ascendant = 0.0,
                        midheaven = 0.0,
                        planetPositionsJson = "",
                        houseCuspsJson = "",
                        houseSystem = ""
                    )
                },
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Muhurta screen
        composable(Screen.Muhurta.route) {
            MuhurtaScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Remedies screen
        composable(
            route = Screen.Remedies.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            RemediesScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Varshaphala screen
        composable(
            route = Screen.Varshaphala.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            VarshaphalaScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Legacy home route - redirect to main
        composable(Screen.Home.route) {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }

        // Legacy chart detail route - redirect to chart analysis
        composable(
            route = Screen.ChartDetail.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable
            LaunchedEffect(chartId) {
                navController.navigate(Screen.ChartAnalysis.createRoute(chartId, InsightFeature.FULL_CHART)) {
                    popUpTo(Screen.ChartDetail.route) { inclusive = true }
                }
            }
        }
    }
}
