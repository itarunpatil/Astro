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
import com.astro.storm.ui.screen.AshtakavargaScreen
import com.astro.storm.ui.screen.BirthChartScreen
import com.astro.storm.ui.screen.ChartAnalysisScreen
import com.astro.storm.ui.screen.ChartInputScreen
import com.astro.storm.ui.screen.DashasScreen
import com.astro.storm.ui.screen.matchmaking.MatchmakingScreen
import com.astro.storm.ui.screen.MuhurtaScreen
import com.astro.storm.ui.screen.NakshatraScreen
import com.astro.storm.ui.screen.PanchangaScreen
import com.astro.storm.ui.screen.PlanetsScreen
import com.astro.storm.ui.screen.PrashnaScreen
import com.astro.storm.ui.screen.ProfileEditScreen
import com.astro.storm.ui.screen.RemediesScreen
import com.astro.storm.ui.screen.ShadbalaScreen
import com.astro.storm.ui.screen.SynastryScreen
import com.astro.storm.ui.screen.TransitsScreen
import com.astro.storm.ui.screen.VarshaphalaScreen
import com.astro.storm.ui.screen.YogasScreen
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
    object Prashna : Screen("prashna")

    // New advanced feature screens
    object Synastry : Screen("synastry")
    object Nakshatra : Screen("nakshatra/{chartId}") {
        fun createRoute(chartId: Long) = "nakshatra/$chartId"
    }
    object Shadbala : Screen("shadbala/{chartId}") {
        fun createRoute(chartId: Long) = "shadbala/$chartId"
    }

    // Individual chart analysis screens
    object BirthChart : Screen("birth_chart/{chartId}") {
        fun createRoute(chartId: Long) = "birth_chart/$chartId"
    }
    object Planets : Screen("planets/{chartId}") {
        fun createRoute(chartId: Long) = "planets/$chartId"
    }
    object Yogas : Screen("yogas/{chartId}") {
        fun createRoute(chartId: Long) = "yogas/$chartId"
    }
    object Dashas : Screen("dashas/{chartId}") {
        fun createRoute(chartId: Long) = "dashas/$chartId"
    }
    object Transits : Screen("transits/{chartId}") {
        fun createRoute(chartId: Long) = "transits/$chartId"
    }
    object Ashtakavarga : Screen("ashtakavarga/{chartId}") {
        fun createRoute(chartId: Long) = "ashtakavarga/$chartId"
    }
    object Panchanga : Screen("panchanga/{chartId}") {
        fun createRoute(chartId: Long) = "panchanga/$chartId"
    }
    object ProfileEdit : Screen("profile_edit/{chartId}") {
        fun createRoute(chartId: Long) = "profile_edit/$chartId"
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
                onNavigateToPrashna = {
                    navController.navigate(Screen.Prashna.route)
                },
                onNavigateToBirthChart = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.BirthChart.createRoute(chartId))
                    }
                },
                onNavigateToPlanets = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Planets.createRoute(chartId))
                    }
                },
                onNavigateToYogas = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Yogas.createRoute(chartId))
                    }
                },
                onNavigateToDashas = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Dashas.createRoute(chartId))
                    }
                },
                onNavigateToTransits = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Transits.createRoute(chartId))
                    }
                },
                onNavigateToAshtakavarga = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Ashtakavarga.createRoute(chartId))
                    }
                },
                onNavigateToPanchanga = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Panchanga.createRoute(chartId))
                    }
                },
                onNavigateToProfileEdit = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.ProfileEdit.createRoute(chartId))
                    }
                },
                onNavigateToSynastry = {
                    navController.navigate(Screen.Synastry.route)
                },
                onNavigateToNakshatra = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Nakshatra.createRoute(chartId))
                    }
                },
                onNavigateToShadbala = {
                    selectedChartId?.let { chartId ->
                        navController.navigate(Screen.Shadbala.createRoute(chartId))
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
                savedCharts = savedCharts,
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

        // Prashna (Horary) screen
        composable(Screen.Prashna.route) {
            PrashnaScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Birth Chart screen
        composable(
            route = Screen.BirthChart.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            BirthChartScreen(
                chart = currentChart,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Planets screen
        composable(
            route = Screen.Planets.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            PlanetsScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Yogas screen
        composable(
            route = Screen.Yogas.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            YogasScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Dashas screen
        composable(
            route = Screen.Dashas.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            DashasScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Transits screen
        composable(
            route = Screen.Transits.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            TransitsScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Ashtakavarga screen
        composable(
            route = Screen.Ashtakavarga.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            AshtakavargaScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Panchanga screen
        composable(
            route = Screen.Panchanga.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            PanchangaScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Profile Edit screen
        composable(
            route = Screen.ProfileEdit.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            ProfileEditScreen(
                chart = currentChart,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() }
            )
        }

        // Synastry (Chart Comparison) screen
        composable(Screen.Synastry.route) {
            SynastryScreen(
                savedCharts = savedCharts,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Nakshatra Analysis screen
        composable(
            route = Screen.Nakshatra.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            NakshatraScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }

        // Shadbala Analysis screen
        composable(
            route = Screen.Shadbala.route,
            arguments = listOf(navArgument("chartId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable

            LaunchedEffect(chartId) {
                if (selectedChartId != chartId) {
                    viewModel.loadChart(chartId)
                }
            }

            ShadbalaScreen(
                chart = currentChart,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
