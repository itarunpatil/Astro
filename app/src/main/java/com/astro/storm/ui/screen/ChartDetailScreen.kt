package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.*
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.components.*
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

// Professional dark theme color palette
private val ScreenBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val CardBackground = Color(0xFF252525)
private val CardBackgroundElevated = Color(0xFF2D2D2D)
private val AccentGold = Color(0xFFD4AF37)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentPurple = Color(0xFF9575CD)
private val AccentRose = Color(0xFFE57373)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentGreen = Color(0xFF81C784)
private val AccentOrange = Color(0xFFFFB74D)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFB0B0B0)
private val TextMuted = Color(0xFF757575)
private val DividerColor = Color(0xFF333333)
private val SuccessColor = Color(0xFF81C784)
private val WarningColor = Color(0xFFFFB74D)
private val ErrorColor = Color(0xFFE57373)
private val ChartBackground = Color(0xFF1A1512)

// Planet colors (including outer planets for complete coverage)
private val planetColors = mapOf(
    com.astro.storm.data.model.Planet.SUN to Color(0xFFD2691E),
    com.astro.storm.data.model.Planet.MOON to Color(0xFFDC143C),
    com.astro.storm.data.model.Planet.MARS to Color(0xFFDC143C),
    com.astro.storm.data.model.Planet.MERCURY to Color(0xFF228B22),
    com.astro.storm.data.model.Planet.JUPITER to Color(0xFFDAA520),
    com.astro.storm.data.model.Planet.VENUS to Color(0xFF9370DB),
    com.astro.storm.data.model.Planet.SATURN to Color(0xFF4169E1),
    com.astro.storm.data.model.Planet.RAHU to Color(0xFF8B0000),
    com.astro.storm.data.model.Planet.KETU to Color(0xFF8B0000),
    com.astro.storm.data.model.Planet.URANUS to Color(0xFF20B2AA),
    com.astro.storm.data.model.Planet.NEPTUNE to Color(0xFF4682B4),
    com.astro.storm.data.model.Planet.PLUTO to Color(0xFF800080)
)

// Navigation tabs - Extended with new features
enum class ChartTab(val title: String, val icon: ImageVector) {
    CHART("Chart", Icons.Outlined.GridView),
    PLANETS("Planets", Icons.Outlined.Star),
    YOGAS("Yogas", Icons.Outlined.AutoAwesome),
    ASHTAKAVARGA("Ashtakavarga", Icons.Outlined.GridOn),
    ASPECTS("Aspects", Icons.Outlined.Hub),
    TRANSITS("Transits", Icons.Outlined.Schedule),
    DASHAS("Dashas", Icons.Outlined.Timeline),
    PANCHANGA("Panchanga", Icons.Outlined.WbSunny)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChartDetailScreen(
    viewModel: ChartViewModel,
    chartId: Long,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val chartRenderer = remember { ChartRenderer() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current

    var currentChart by remember { mutableStateOf<VedicChart?>(null) }
    var selectedTab by remember { mutableStateOf(ChartTab.CHART) }

    // Dialog states
    var showFullScreenChart by remember { mutableStateOf(false) }
    var fullScreenChartTitle by remember { mutableStateOf("Lagna") }
    var fullScreenDivisionalData by remember { mutableStateOf<DivisionalChartData?>(null) }
    var showShadbalaDialog by remember { mutableStateOf(false) }
    var selectedPlanetPosition by remember { mutableStateOf<PlanetPosition?>(null) }
    var selectedNakshatra by remember { mutableStateOf<Pair<com.astro.storm.data.model.Nakshatra, Int>?>(null) }
    var selectedHouse by remember { mutableStateOf<Int?>(null) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            listOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    )

    LaunchedEffect(chartId) {
        viewModel.loadChart(chartId)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ChartUiState.Success -> currentChart = state.chart
            is ChartUiState.Exporting -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Indefinite
                )
            }
            is ChartUiState.Exported -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                delay(100)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Show dialogs
    val displayChart = currentChart ?: (uiState as? ChartUiState.Success)?.chart

    if (showFullScreenChart && displayChart != null) {
        FullScreenChartDialog(
            chart = displayChart,
            chartRenderer = chartRenderer,
            chartTitle = fullScreenChartTitle,
            divisionalChartData = fullScreenDivisionalData,
            onDismiss = { showFullScreenChart = false }
        )
    }

    if (showShadbalaDialog && displayChart != null) {
        ShadbalaDialog(
            chart = displayChart,
            onDismiss = { showShadbalaDialog = false }
        )
    }

    selectedPlanetPosition?.let { position ->
        displayChart?.let { chart ->
            PlanetDetailDialog(
                planetPosition = position,
                chart = chart,
                onDismiss = { selectedPlanetPosition = null }
            )
        }
    }

    selectedNakshatra?.let { (nakshatra, pada) ->
        NakshatraDetailDialog(
            nakshatra = nakshatra,
            pada = pada,
            onDismiss = { selectedNakshatra = null }
        )
    }

    selectedHouse?.let { houseNum ->
        displayChart?.let { chart ->
            val houseCusp = if (houseNum <= chart.houseCusps.size) chart.houseCusps[houseNum - 1] else 0.0
            val planetsInHouse = chart.planetPositions.filter { it.house == houseNum }
            HouseDetailDialog(
                houseNumber = houseNum,
                houseCusp = houseCusp,
                planetsInHouse = planetsInHouse,
                chart = chart,
                onDismiss = { selectedHouse = null }
            )
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardBackgroundElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            ChartTopBar(
                chartName = currentChart?.birthData?.name ?: "Chart Details",
                onNavigateBack = onNavigateBack,
                onExportPdf = {
                    currentChart?.let { chart ->
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartToPdf(chart, density)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                },
                onExportImage = {
                    currentChart?.let { chart ->
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartToImage(chart, density)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                },
                onExportJson = {
                    currentChart?.let { chart ->
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartToJson(chart)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                },
                onExportCsv = {
                    currentChart?.let { chart ->
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartToCsv(chart)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                },
                onExportText = {
                    currentChart?.let { chart ->
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartToText(chart)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                },
                onCopy = {
                    currentChart?.let { chart ->
                        viewModel.copyChartToClipboard(chart)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Chart data copied to clipboard",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                onShadbala = {
                    showShadbalaDialog = true
                }
            )
        },
        bottomBar = {
            ChartBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        when {
            uiState is ChartUiState.Loading && displayChart == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGold)
                }
            }
            displayChart != null -> {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() + slideInVertically { it / 4 } togetherWith
                                fadeOut() + slideOutVertically { -it / 4 }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        ChartTab.CHART -> ChartTabContent(
                            chart = displayChart,
                            chartRenderer = chartRenderer,
                            context = context,
                            onChartClick = { title, divisionalData ->
                                fullScreenChartTitle = title
                                fullScreenDivisionalData = divisionalData
                                showFullScreenChart = true
                            },
                            onPlanetClick = { selectedPlanetPosition = it },
                            onHouseClick = { selectedHouse = it }
                        )
                        ChartTab.PLANETS -> PlanetsTabContent(
                            chart = displayChart,
                            onPlanetClick = { selectedPlanetPosition = it },
                            onNakshatraClick = { nakshatra, pada -> selectedNakshatra = nakshatra to pada },
                            onShadbalaClick = { showShadbalaDialog = true }
                        )
                        ChartTab.YOGAS -> YogasTabContent(displayChart)
                        ChartTab.ASHTAKAVARGA -> AshtakavargaTabContent(displayChart)
                        ChartTab.ASPECTS -> AspectsTabContent(displayChart)
                        ChartTab.TRANSITS -> TransitsTabContent(displayChart, context)
                        ChartTab.DASHAS -> DashasTabContent(displayChart)
                        ChartTab.PANCHANGA -> PanchangaTabContent(displayChart, context)
                    }
                }
            }
            uiState is ChartUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as ChartUiState.Error).message,
                        color = ErrorColor,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartTopBar(
    chartName: String,
    onNavigateBack: () -> Unit,
    onExportPdf: () -> Unit,
    onExportImage: () -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onExportText: () -> Unit,
    onCopy: () -> Unit,
    onShadbala: () -> Unit
) {
    var showExportMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = chartName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            // Shadbala button
            IconButton(onClick = onShadbala) {
                Icon(
                    Icons.Outlined.TrendingUp,
                    contentDescription = "Shadbala",
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Export dropdown menu
            Box {
                IconButton(onClick = { showExportMenu = true }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Export",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false },
                    modifier = Modifier.background(CardBackground)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("PDF Report", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportPdf()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("PNG Image", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportImage()
                        }
                    )
                    HorizontalDivider(color = DividerColor)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Code,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("JSON Data", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportJson()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.TableChart,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("CSV Spreadsheet", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportCsv()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Text Report", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportText()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceColor
        )
    )
}

@Composable
private fun ChartBottomNavigation(
    selectedTab: ChartTab,
    onTabSelected: (ChartTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceColor,
        contentColor = TextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        ChartTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(if (selectedTab == tab) 26.dp else 24.dp)
                    )
                },
                label = null,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentGold,
                    unselectedIconColor = TextMuted,
                    indicatorColor = AccentGold.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ============ CHART TAB ============

@Composable
private fun ChartTabContent(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    context: android.content.Context,
    onChartClick: (String, DivisionalChartData?) -> Unit,
    onPlanetClick: (PlanetPosition) -> Unit,
    onHouseClick: (Int) -> Unit
) {
    val divisionalCharts = remember(chart) {
        DivisionalChartCalculator.calculateAllDivisionalCharts(chart)
    }

    var selectedChartType by remember { mutableStateOf("D1") }

    // Get current chart data based on selection
    val currentChartData = remember(selectedChartType, divisionalCharts) {
        when (selectedChartType) {
            "D1" -> null
            "D2" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D2_HORA }
            "D3" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D3_DREKKANA }
            "D4" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D4_CHATURTHAMSA }
            "D7" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D7_SAPTAMSA }
            "D9" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D9_NAVAMSA }
            "D10" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D10_DASAMSA }
            "D12" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D12_DWADASAMSA }
            "D16" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D16_SHODASAMSA }
            "D20" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D20_VIMSAMSA }
            "D24" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D24_CHATURVIMSAMSA }
            "D27" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D27_SAPTAVIMSAMSA }
            "D30" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D30_TRIMSAMSA }
            "D60" -> divisionalCharts.find { it.chartType == com.astro.storm.ephemeris.DivisionalChartType.D60_SHASHTIAMSA }
            else -> null
        }
    }

    // Get chart title and description
    val chartInfo = remember(selectedChartType) {
        when (selectedChartType) {
            "D1" -> Triple("Lagna Chart (Rashi)", "Physical Body, General Life", "D1")
            "D2" -> Triple("Hora Chart", "Wealth, Prosperity", "D2")
            "D3" -> Triple("Drekkana Chart", "Siblings, Courage, Vitality", "D3")
            "D4" -> Triple("Chaturthamsa Chart", "Fortune, Property", "D4")
            "D7" -> Triple("Saptamsa Chart", "Children, Progeny", "D7")
            "D9" -> Triple("Navamsa Chart", "Marriage, Dharma, Fortune", "D9")
            "D10" -> Triple("Dasamsa Chart", "Career, Profession", "D10")
            "D12" -> Triple("Dwadasamsa Chart", "Parents, Ancestry", "D12")
            "D16" -> Triple("Shodasamsa Chart", "Vehicles, Pleasures", "D16")
            "D20" -> Triple("Vimsamsa Chart", "Spiritual Life", "D20")
            "D24" -> Triple("Siddhamsa Chart", "Education, Learning", "D24")
            "D27" -> Triple("Bhamsa Chart", "Strength, Weakness", "D27")
            "D30" -> Triple("Trimsamsa Chart", "Evils, Misfortunes", "D30")
            "D60" -> Triple("Shashtiamsa Chart", "Past Life Karma", "D60")
            else -> Triple("Chart", "", selectedChartType)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chart type selector
        item {
            ChartTypeSelector(
                selectedType = selectedChartType,
                onTypeSelected = { selectedChartType = it }
            )
        }

        // Main chart display - CLICKABLE for full-screen view
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onChartClick(chartInfo.first, currentChartData)
                    },
                shape = RoundedCornerShape(16.dp),
                color = CardBackground
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Chart title with description
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = chartInfo.first,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentGold
                            )
                            if (chartInfo.second.isNotEmpty()) {
                                Text(
                                    text = chartInfo.second,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "View fullscreen",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = chartInfo.third,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chart canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (selectedChartType == "D1") {
                                chartRenderer.drawNorthIndianChart(
                                    drawScope = this,
                                    chart = chart,
                                    size = size.minDimension,
                                    chartTitle = "Lagna"
                                )
                            } else {
                                currentChartData?.let {
                                    // Pass original chart for vargottama and combust status checking
                                    chartRenderer.drawDivisionalChart(
                                        drawScope = this,
                                        planetPositions = it.planetPositions,
                                        ascendantLongitude = it.ascendantLongitude,
                                        size = size.minDimension,
                                        chartTitle = chartInfo.third,
                                        originalChart = chart
                                    )
                                }
                            }
                        }
                    }

                    // Legend and tap hint
                    Spacer(modifier = Modifier.height(12.dp))
                    ChartLegend()

                    // Tap hint
                    Text(
                        text = "Tap chart to view fullscreen with download option",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }

        // Chart Details - Planetary positions for selected chart - CLICKABLE
        item {
            ChartDetailsCard(
                chart = chart,
                currentChartData = currentChartData,
                selectedChartType = selectedChartType,
                onPlanetClick = onPlanetClick
            )
        }

        // House cusps - CLICKABLE
        item {
            HouseCuspsCard(
                chart = chart,
                onHouseClick = onHouseClick
            )
        }

        // Birth information
        item {
            BirthInfoCard(chart)
        }

        // Astronomical data
        item {
            AstronomicalDataCard(chart)
        }
    }
}

@Composable
/**
 * Chart legend matching AstroSage symbols:
 * - * Retrograde
 * - ^ Combust
 * - ¤ Vargottama
 * - ↑ Exalted
 * - ↓ Debilitated
 */
private fun ChartLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1512),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // First row: Retrograde, Combust, Vargottama
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("*", "Retrograde")
            LegendItem("^", "Combust")
            LegendItem("\u00A4", "Vargottama")
        }
        // Second row: Exalted, Debilitated
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("\u2191", "Exalted")
            LegendItem("\u2193", "Debilitated")
        }
    }
}

@Composable
private fun LegendItem(symbol: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ChartDetailsCard(
    chart: VedicChart,
    currentChartData: DivisionalChartData?,
    selectedChartType: String,
    onPlanetClick: (PlanetPosition) -> Unit
) {
    val planetPositions = if (selectedChartType == "D1") {
        chart.planetPositions
    } else {
        currentChartData?.planetPositions ?: emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Planetary Positions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tap for details",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Ascendant info
            if (selectedChartType == "D1") {
                val ascSign = com.astro.storm.data.model.ZodiacSign.fromLongitude(chart.ascendant)
                val ascDegree = chart.ascendant % 30.0
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGold.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ascendant (Lagna)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentGold
                        )
                        Row {
                            Text(
                                text = ascSign.displayName,
                                fontSize = 13.sp,
                                color = AccentTeal
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${ascDegree.toInt()}°",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
            }

            // Planet positions - CLICKABLE
            planetPositions.forEach { position ->
                ClickablePlanetPositionRow(
                    position = position,
                    onClick = { onPlanetClick(position) }
                )
            }
        }
    }
}

@Composable
private fun ClickablePlanetPositionRow(
    position: PlanetPosition,
    onClick: () -> Unit
) {
    val color = planetColors[position.planet] ?: TextPrimary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = position.planet.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    modifier = Modifier.width(70.dp)
                )
                if (position.isRetrograde) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = WarningColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "R",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Text(
                text = position.sign.displayName,
                fontSize = 13.sp,
                color = AccentTeal,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${(position.longitude % 30.0).toInt()}°",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "H${position.house}",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HouseCuspsCard(
    chart: VedicChart,
    onHouseClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "House Cusps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tap house for details",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        label = "rotation"
                    )
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Display houses in a 2x6 grid
                    for (row in 0..5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val house1 = row + 1
                            val house2 = row + 7

                            HouseCuspItem(
                                houseNumber = house1,
                                cusp = chart.houseCusps.getOrNull(house1 - 1) ?: 0.0,
                                modifier = Modifier.weight(1f),
                                onClick = { onHouseClick(house1) }
                            )
                            HouseCuspItem(
                                houseNumber = house2,
                                cusp = chart.houseCusps.getOrNull(house2 - 1) ?: 0.0,
                                modifier = Modifier.weight(1f),
                                onClick = { onHouseClick(house2) }
                            )
                        }
                        if (row < 5) Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseCuspItem(
    houseNumber: Int,
    cusp: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val sign = com.astro.storm.data.model.ZodiacSign.fromLongitude(cusp)
    val degreeInSign = cusp % 30.0

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = CardBackgroundElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "H$houseNumber",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = sign.abbreviation,
                    fontSize = 12.sp,
                    color = AccentTeal
                )
                Text(
                    text = "${degreeInSign.toInt()}°",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ChartTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val chartTypes = listOf(
        "D1" to "Lagna",
        "D2" to "Hora",
        "D3" to "Drekkana",
        "D4" to "D4",
        "D7" to "Saptamsa",
        "D9" to "Navamsa",
        "D10" to "Dasamsa",
        "D12" to "D12",
        "D16" to "D16",
        "D20" to "D20",
        "D24" to "D24",
        "D27" to "Bhamsa",
        "D30" to "D30",
        "D60" to "D60"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chartTypes) { (type, name) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = name,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGold,
                    containerColor = CardBackground,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = DividerColor,
                    selectedBorderColor = AccentGold,
                    enabled = true,
                    selected = selectedType == type
                )
            )
        }
    }
}

@Composable
private fun BirthInfoCard(chart: VedicChart) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Birth Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            InfoRow("Name", chart.birthData.name)
            InfoRow(
                "Date & Time",
                chart.birthData.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm:ss a"))
            )
            InfoRow("Location", chart.birthData.location)
            InfoRow(
                "Coordinates",
                "${formatCoordinate(chart.birthData.latitude, true)}, ${formatCoordinate(chart.birthData.longitude, false)}"
            )
            InfoRow("Timezone", chart.birthData.timezone)
        }
    }
}

@Composable
private fun AstronomicalDataCard(chart: VedicChart) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Astronomical Data",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "rotation"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    InfoRow("Julian Day", String.format("%.6f", chart.julianDay))
                    InfoRow("Ayanamsa", "${chart.ayanamsaName} (${formatDegree(chart.ayanamsa)})")
                    InfoRow("Ascendant", formatDegree(chart.ascendant))
                    InfoRow("Midheaven", formatDegree(chart.midheaven))
                    InfoRow("House System", chart.houseSystem.displayName)
                }
            }
        }
    }
}

// ============ PLANETS TAB ============

@Composable
private fun PlanetsTabContent(
    chart: VedicChart,
    onPlanetClick: (PlanetPosition) -> Unit,
    onNakshatraClick: (com.astro.storm.data.model.Nakshatra, Int) -> Unit,
    onShadbalaClick: () -> Unit
) {
    val conditionAnalysis = remember(chart) {
        RetrogradeCombustionCalculator.analyzePlanetaryConditions(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status summary
        item {
            PlanetaryStatusSummary(conditionAnalysis)
        }

        // Individual planet cards - CLICKABLE
        items(chart.planetPositions) { position ->
            val condition = conditionAnalysis.getCondition(position.planet)
            PlanetDetailCard(
                position = position,
                condition = condition,
                onClick = { onPlanetClick(position) },
                onNakshatraClick = { onNakshatraClick(position.nakshatra, position.nakshatraPada) }
            )
        }
    }
}

@Composable
private fun PlanetaryStatusSummary(analysis: RetrogradeCombustionCalculator.PlanetaryConditionAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusBadge(
                count = analysis.retrogradePlanets.size,
                label = "Retrograde",
                color = WarningColor
            )
            StatusBadge(
                count = analysis.combustPlanets.size,
                label = "Combust",
                color = ErrorColor
            )
            StatusBadge(
                count = analysis.planetaryWars.size,
                label = "In War",
                color = AccentPurple
            )
        }
    }
}

@Composable
private fun StatusBadge(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun PlanetDetailCard(
    position: PlanetPosition,
    condition: RetrogradeCombustionCalculator.PlanetCondition?,
    onClick: () -> Unit,
    onNakshatraClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Planet header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                planetColors[position.planet] ?: AccentGold,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = position.planet.symbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = position.planet.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (position.isRetrograde) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = WarningColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "R",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "House ${position.house}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = position.sign.displayName,
                        fontSize = 14.sp,
                        color = AccentTeal
                    )
                    Text(
                        text = formatDegreeInSign(position.longitude),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Nakshatra info - CLICKABLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNakshatraClick() }
                    .background(CardBackgroundElevated, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Nakshatra", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "${position.nakshatra.displayName} • Pada ${position.nakshatraPada}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Lord", fontSize = 11.sp, color = TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = position.nakshatra.ruler.displayName,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Condition info
            condition?.let { cond ->
                if (cond.combustionStatus != RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST ||
                    cond.isInPlanetaryWar) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (cond.combustionStatus != RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST) {
                            ConditionChip(
                                label = cond.combustionStatus.displayName,
                                color = ErrorColor
                            )
                        }
                        if (cond.isInPlanetaryWar) {
                            ConditionChip(
                                label = "War with ${cond.warData?.loser?.displayName}",
                                color = AccentPurple
                            )
                        }
                    }
                }
            }

            // Tap for more hint
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap for detailed analysis & predictions",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConditionChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============ DASHAS TAB ============

@Composable
private fun DashasTabContent(chart: VedicChart) {
    val dashaTimeline = remember(chart) {
        DashaCalculator.calculateDashaTimeline(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current period
        item {
            CurrentDashaPeriodCard(dashaTimeline)
        }

        // Birth nakshatra info
        item {
            DashaBirthInfoCard(dashaTimeline)
        }

        // Mahadasha timeline
        item {
            Text(
                text = "Mahadasha Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(dashaTimeline.mahadashas.take(12)) { mahadasha ->
            MahadashaCard(mahadasha, dashaTimeline.currentMahadasha == mahadasha)
        }
    }
}

@Composable
private fun CurrentDashaPeriodCard(timeline: DashaCalculator.DashaTimeline) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Period",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            timeline.currentMahadasha?.let { md ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${md.planet.displayName} Mahadasha",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                        timeline.currentAntardasha?.let { ad ->
                            Text(
                                text = "${ad.planet.displayName} Bhukti",
                                fontSize = 14.sp,
                                color = AccentTeal
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Until",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = md.endDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashaBirthInfoCard(timeline: DashaCalculator.DashaTimeline) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackgroundElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Birth Nakshatra", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = timeline.birthNakshatra.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nakshatra Lord", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = timeline.birthNakshatraLord.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Balance at Birth", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "${String.format("%.2f", timeline.balanceOfFirstDasha)} yrs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun MahadashaCard(mahadasha: DashaCalculator.Mahadasha, isActive: Boolean) {
    var expanded by remember { mutableStateOf(isActive) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) AccentGold.copy(alpha = 0.1f) else CardBackground,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, AccentGold) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mahadasha.planet.symbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) AccentGold else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mahadasha.planet.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SuccessColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${String.format("%.1f", mahadasha.durationYears)} years",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = mahadasha.startDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = mahadasha.endDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Antardashas
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Bhuktis (Sub-periods)",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    mahadasha.antardashas.forEach { antardasha ->
                        val isActiveAntardasha = antardasha.isActive
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (isActiveAntardasha) AccentTeal.copy(alpha = 0.1f)
                                    else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = antardasha.planet.displayName,
                                fontSize = 13.sp,
                                color = if (isActiveAntardasha) AccentTeal else TextSecondary
                            )
                            Text(
                                text = "${antardasha.startDate.format(DateTimeFormatter.ofPattern("MM/yy"))} - ${antardasha.endDate.format(DateTimeFormatter.ofPattern("MM/yy"))}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============ YOGAS TAB (COMPREHENSIVE) ============

@Composable
private fun YogasTabContent(chart: VedicChart) {
    var yogaAnalysis by remember { mutableStateOf<YogaCalculator.YogaAnalysis?>(null) }
    var selectedCategory by remember { mutableStateOf<YogaCalculator.YogaCategory?>(null) }

    LaunchedEffect(chart) {
        withContext(Dispatchers.Default) {
            yogaAnalysis = YogaCalculator.calculateYogas(chart)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        yogaAnalysis?.let { analysis ->
            // Overview card
            item {
                YogaOverviewCard(analysis)
            }

            // Category selector
            item {
                YogaCategorySelector(
                    analysis = analysis,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // Display yogas based on selection
            val yogasToDisplay = when (selectedCategory) {
                YogaCalculator.YogaCategory.RAJA_YOGA -> analysis.rajaYogas
                YogaCalculator.YogaCategory.DHANA_YOGA -> analysis.dhanaYogas
                YogaCalculator.YogaCategory.MAHAPURUSHA_YOGA -> analysis.mahapurushaYogas
                YogaCalculator.YogaCategory.NABHASA_YOGA -> analysis.nabhasaYogas
                YogaCalculator.YogaCategory.CHANDRA_YOGA -> analysis.chandraYogas
                YogaCalculator.YogaCategory.SOLAR_YOGA -> analysis.solarYogas
                YogaCalculator.YogaCategory.NEGATIVE_YOGA -> analysis.negativeYogas
                YogaCalculator.YogaCategory.SPECIAL_YOGA -> analysis.specialYogas
                null -> analysis.allYogas
            }

            if (yogasToDisplay.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CardBackground
                    ) {
                        Text(
                            text = "No yogas found in this category",
                            color = TextMuted,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(yogasToDisplay) { yoga ->
                    ExpandedYogaCard(yoga)
                }
            }
        } ?: item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGold)
            }
        }
    }
}

@Composable
private fun YogaOverviewCard(analysis: YogaCalculator.YogaAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Yoga Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${analysis.allYogas.size} yogas detected",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Strength indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Yoga Strength",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${String.format("%.1f", analysis.overallYogaStrength)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        analysis.overallYogaStrength >= 70 -> SuccessColor
                        analysis.overallYogaStrength >= 40 -> WarningColor
                        else -> ErrorColor
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (analysis.overallYogaStrength / 100f).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    analysis.overallYogaStrength >= 70 -> SuccessColor
                    analysis.overallYogaStrength >= 40 -> WarningColor
                    else -> ErrorColor
                },
                trackColor = DividerColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dominant category
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentGold.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dominant: ",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = analysis.dominantYogaCategory.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGold
                    )
                }
            }
        }
    }
}

@Composable
private fun YogaCategorySelector(
    analysis: YogaCalculator.YogaAnalysis,
    selectedCategory: YogaCalculator.YogaCategory?,
    onCategorySelected: (YogaCalculator.YogaCategory?) -> Unit
) {
    val categories = listOf(
        null to "All (${analysis.allYogas.size})",
        YogaCalculator.YogaCategory.MAHAPURUSHA_YOGA to "Mahapurusha (${analysis.mahapurushaYogas.size})",
        YogaCalculator.YogaCategory.RAJA_YOGA to "Raja (${analysis.rajaYogas.size})",
        YogaCalculator.YogaCategory.DHANA_YOGA to "Dhana (${analysis.dhanaYogas.size})",
        YogaCalculator.YogaCategory.CHANDRA_YOGA to "Chandra (${analysis.chandraYogas.size})",
        YogaCalculator.YogaCategory.SOLAR_YOGA to "Solar (${analysis.solarYogas.size})",
        YogaCalculator.YogaCategory.NABHASA_YOGA to "Nabhasa (${analysis.nabhasaYogas.size})",
        YogaCalculator.YogaCategory.NEGATIVE_YOGA to "Negative (${analysis.negativeYogas.size})",
        YogaCalculator.YogaCategory.SPECIAL_YOGA to "Special (${analysis.specialYogas.size})"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (category, label) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGold,
                    containerColor = CardBackground,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = DividerColor,
                    selectedBorderColor = AccentGold,
                    enabled = true,
                    selected = selectedCategory == category
                )
            )
        }
    }
}

@Composable
private fun ExpandedYogaCard(yoga: YogaCalculator.Yoga) {
    var expanded by remember { mutableStateOf(false) }

    val categoryColor = when (yoga.category) {
        YogaCalculator.YogaCategory.RAJA_YOGA -> AccentGold
        YogaCalculator.YogaCategory.DHANA_YOGA -> SuccessColor
        YogaCalculator.YogaCategory.MAHAPURUSHA_YOGA -> AccentPurple
        YogaCalculator.YogaCategory.CHANDRA_YOGA -> AccentBlue
        YogaCalculator.YogaCategory.SOLAR_YOGA -> AccentOrange
        YogaCalculator.YogaCategory.NABHASA_YOGA -> AccentTeal
        YogaCalculator.YogaCategory.NEGATIVE_YOGA -> ErrorColor
        YogaCalculator.YogaCategory.SPECIAL_YOGA -> AccentGreen
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row - clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Yoga indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (yoga.isAuspicious) categoryColor else ErrorColor,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = yoga.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = yoga.sanskritName,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Strength badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = yoga.strength.displayName,
                            fontSize = 10.sp,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        label = "rotation"
                    )
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = yoga.description,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Effects
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = CardBackgroundElevated
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Effects",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = yoga.effects,
                                fontSize = 13.sp,
                                color = if (yoga.isAuspicious) SuccessColor else ErrorColor
                            )
                        }
                    }

                    // Planets involved
                    if (yoga.planets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(
                                text = "Planets: ",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = yoga.planets.joinToString(", ") { it.displayName },
                                fontSize = 12.sp,
                                color = AccentTeal
                            )
                        }
                    }

                    // Houses involved
                    if (yoga.houses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text(
                                text = "Houses: ",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = yoga.houses.joinToString(", "),
                                fontSize = 12.sp,
                                color = AccentPurple
                            )
                        }
                    }

                    // Activation period
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text(
                            text = "Activation: ",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = yoga.activationPeriod,
                            fontSize = 12.sp,
                            color = AccentGold
                        )
                    }

                    // Cancellation factors
                    if (yoga.cancellationFactors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cancellation Factors:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                        yoga.cancellationFactors.forEach { factor ->
                            Text(
                                text = "• $factor",
                                fontSize = 11.sp,
                                color = WarningColor,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }

                    // Strength bar
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Strength",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${String.format("%.0f", yoga.strengthPercentage)}%",
                            fontSize = 11.sp,
                            color = categoryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (yoga.strengthPercentage / 100f).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = categoryColor,
                        trackColor = categoryColor.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

// ============ ASHTAKAVARGA TAB ============

@Composable
private fun AshtakavargaTabContent(chart: VedicChart) {
    var ashtakavargaAnalysis by remember { mutableStateOf<AshtakavargaCalculator.AshtakavargaAnalysis?>(null) }
    var selectedPlanet by remember { mutableStateOf<com.astro.storm.data.model.Planet?>(null) }

    LaunchedEffect(chart) {
        withContext(Dispatchers.Default) {
            ashtakavargaAnalysis = AshtakavargaCalculator.calculateAshtakavarga(chart)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ashtakavargaAnalysis?.let { analysis ->
            // Sarvashtakavarga overview
            item {
                SarvashtakavargaCard(analysis.sarvashtakavarga)
            }

            // Planet selector for Bhinnashtakavarga
            item {
                BhinnashtakavargaPlanetSelector(
                    selectedPlanet = selectedPlanet,
                    onPlanetSelected = { selectedPlanet = it }
                )
            }

            // Display Bhinnashtakavarga
            selectedPlanet?.let { planet ->
                analysis.bhinnashtakavarga[planet]?.let { bav ->
                    item {
                        BhinnashtakavargaCard(bav)
                    }
                }
            } ?: item {
                // Show SAV grid when no planet selected
                SarvashtakavargaGridCard(analysis.sarvashtakavarga)
            }

            // Transit predictions based on Ashtakavarga
            item {
                AshtakavargaTransitPredictionsCard(chart, analysis)
            }
        } ?: item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGold)
            }
        }
    }
}

@Composable
private fun SarvashtakavargaCard(sav: AshtakavargaCalculator.Sarvashtakavarga) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.GridOn,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sarvashtakavarga",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Combined Strength Analysis",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Total and average
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = sav.totalBindus.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    Text(
                        text = "Total Bindus",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", sav.totalBindus / 12.0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentTeal
                    )
                    Text(
                        text = "Avg per Sign",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Strongest and weakest signs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessColor.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Strongest",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = sav.strongestSign.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessColor
                        )
                        Text(
                            text = "${sav.getBindusForSign(sav.strongestSign)} bindus",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorColor.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Weakest",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = sav.weakestSign.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorColor
                        )
                        Text(
                            text = "${sav.getBindusForSign(sav.weakestSign)} bindus",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BhinnashtakavargaPlanetSelector(
    selectedPlanet: com.astro.storm.data.model.Planet?,
    onPlanetSelected: (com.astro.storm.data.model.Planet?) -> Unit
) {
    val planets = listOf(
        null to "SAV Grid",
        com.astro.storm.data.model.Planet.SUN to "Sun",
        com.astro.storm.data.model.Planet.MOON to "Moon",
        com.astro.storm.data.model.Planet.MARS to "Mars",
        com.astro.storm.data.model.Planet.MERCURY to "Mercury",
        com.astro.storm.data.model.Planet.JUPITER to "Jupiter",
        com.astro.storm.data.model.Planet.VENUS to "Venus",
        com.astro.storm.data.model.Planet.SATURN to "Saturn"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(planets) { (planet, label) ->
            FilterChip(
                selected = selectedPlanet == planet,
                onClick = { onPlanetSelected(planet) },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGold,
                    containerColor = CardBackground,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = DividerColor,
                    selectedBorderColor = AccentGold,
                    enabled = true,
                    selected = selectedPlanet == planet
                )
            )
        }
    }
}

@Composable
private fun SarvashtakavargaGridCard(sav: AshtakavargaCalculator.Sarvashtakavarga) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SAV by Sign",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid of signs with bindus
            com.astro.storm.data.model.ZodiacSign.entries.chunked(3).forEach { rowSigns ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSigns.forEach { sign ->
                        val bindus = sav.getBindusForSign(sign)
                        val isFavorable = sav.isFavorableForTransit(sign)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFavorable) SuccessColor.copy(alpha = 0.1f) else CardBackgroundElevated
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sign.abbreviation,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = bindus.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFavorable) SuccessColor else TextPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Legend
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Green = 28+ bindus (favorable for transits)",
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BhinnashtakavargaCard(bav: AshtakavargaCalculator.Bhinnashtakavarga) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bav.planet.displayName} Bhinnashtakavarga",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Total: ${bav.totalBindus}",
                    fontSize = 12.sp,
                    color = AccentGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of signs with bindus
            com.astro.storm.data.model.ZodiacSign.entries.chunked(4).forEach { rowSigns ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowSigns.forEach { sign ->
                        val bindus = bav.getBindusForSign(sign)
                        val isStrong = bindus >= 4
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                bindus >= 5 -> SuccessColor.copy(alpha = 0.15f)
                                bindus >= 4 -> AccentTeal.copy(alpha = 0.1f)
                                bindus <= 2 -> ErrorColor.copy(alpha = 0.1f)
                                else -> CardBackgroundElevated
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sign.abbreviation,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = bindus.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        bindus >= 5 -> SuccessColor
                                        bindus >= 4 -> AccentTeal
                                        bindus <= 2 -> ErrorColor
                                        else -> TextPrimary
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Legend
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendChip("5+", SuccessColor)
                LegendChip("4", AccentTeal)
                LegendChip("3", TextPrimary)
                LegendChip("0-2", ErrorColor)
            }
        }
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun AshtakavargaTransitPredictionsCard(
    chart: VedicChart,
    analysis: AshtakavargaCalculator.AshtakavargaAnalysis
) {
    val currentTransitPositions = remember(chart) {
        // Get current planetary positions for transit analysis
        chart.planetPositions.map { it.sign }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transit Quality by Sign",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Text(
                text = "Based on Sarvashtakavarga scores (28+ = Favorable)",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Show all signs with their transit quality
            com.astro.storm.data.model.ZodiacSign.entries.forEach { sign ->
                val bindus = analysis.sarvashtakavarga.getBindusForSign(sign)
                val isFavorable = analysis.sarvashtakavarga.isFavorableForTransit(sign)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sign.displayName,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        modifier = Modifier.width(90.dp)
                    )

                    // Progress bar
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        LinearProgressIndicator(
                            progress = { (bindus / 56f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isFavorable) SuccessColor else WarningColor,
                            trackColor = DividerColor
                        )
                    }

                    Text(
                        text = "$bindus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isFavorable) SuccessColor else TextMuted,
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// ============ TRANSITS TAB ============

@Composable
private fun TransitsTabContent(chart: VedicChart, context: android.content.Context) {
    var transitAnalysis by remember { mutableStateOf<TransitAnalyzer.TransitAnalysis?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(chart) {
        withContext(Dispatchers.Default) {
            try {
                val analyzer = TransitAnalyzer(context)
                transitAnalysis = analyzer.analyzeTransits(chart)
                analyzer.close()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Calculating real-time transits...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            transitAnalysis?.let { analysis ->
                // Overall assessment
                item {
                    TransitOverallAssessmentCard(analysis.overallAssessment)
                }

                // Current transit positions
                item {
                    CurrentTransitPositionsCard(analysis.transitPositions)
                }

                // Gochara analysis
                item {
                    GocharaAnalysisCard(analysis.gocharaResults)
                }

                // Significant aspects
                if (analysis.transitAspects.isNotEmpty()) {
                    item {
                        TransitAspectsCard(analysis.transitAspects.take(8))
                    }
                }

                // Ashtakavarga scores
                if (analysis.ashtakavargaScores.isNotEmpty()) {
                    item {
                        TransitAshtakavargaScoresCard(analysis.ashtakavargaScores)
                    }
                }

                // Significant periods
                if (analysis.significantPeriods.isNotEmpty()) {
                    item {
                        SignificantPeriodsCard(analysis.significantPeriods)
                    }
                }
            } ?: item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground
                ) {
                    Text(
                        text = "Unable to calculate transit analysis",
                        color = ErrorColor,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TransitOverallAssessmentCard(assessment: TransitAnalyzer.OverallTransitAssessment) {
    val qualityColor = when (assessment.quality) {
        TransitAnalyzer.TransitQuality.EXCELLENT -> SuccessColor
        TransitAnalyzer.TransitQuality.GOOD -> AccentGreen
        TransitAnalyzer.TransitQuality.MIXED -> WarningColor
        TransitAnalyzer.TransitQuality.CHALLENGING -> AccentOrange
        TransitAnalyzer.TransitQuality.DIFFICULT -> ErrorColor
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Current Transit Period",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time analysis",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Quality badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = qualityColor.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assessment.quality.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = qualityColor
                    )
                    Text(
                        text = "Score: ${String.format("%.0f", assessment.score)}/100",
                        fontSize = 14.sp,
                        color = qualityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary
            Text(
                text = assessment.summary,
                fontSize = 13.sp,
                color = TextSecondary
            )

            // Focus areas
            if (assessment.focusAreas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Key Focus Areas:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                assessment.focusAreas.forEach { area ->
                    Text(
                        text = "• $area",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentTransitPositionsCard(positions: List<PlanetPosition>) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Public,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Planetary Positions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "rotation"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    positions.filter { it.planet in com.astro.storm.data.model.Planet.MAIN_PLANETS }.forEach { pos ->
                        val retroText = if (pos.isRetrograde) " (R)" else ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            planetColors[pos.planet] ?: AccentGold,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pos.planet.displayName + retroText,
                                    fontSize = 13.sp,
                                    color = if (pos.isRetrograde) WarningColor else TextPrimary
                                )
                            }
                            Text(
                                text = "${pos.sign.displayName} ${(pos.longitude % 30).toInt()}°",
                                fontSize = 13.sp,
                                color = AccentTeal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GocharaAnalysisCard(gocharaResults: List<TransitAnalyzer.GocharaResult>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Brightness2,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Gochara (Transit from Moon)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Traditional Vedic transit analysis",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            gocharaResults.filter { it.planet in com.astro.storm.data.model.Planet.MAIN_PLANETS }.forEach { result ->
                val effectColor = when (result.effect) {
                    TransitAnalyzer.TransitEffect.EXCELLENT -> SuccessColor
                    TransitAnalyzer.TransitEffect.GOOD -> AccentGreen
                    TransitAnalyzer.TransitEffect.NEUTRAL -> TextSecondary
                    TransitAnalyzer.TransitEffect.CHALLENGING -> WarningColor
                    TransitAnalyzer.TransitEffect.DIFFICULT -> ErrorColor
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = effectColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = result.planet.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (result.isVedhaAffected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = WarningColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Vedha",
                                            fontSize = 9.sp,
                                            color = WarningColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "House ${result.houseFromMoon} from Moon",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = result.effect.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = effectColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitAspectsCard(aspects: List<TransitAnalyzer.TransitAspect>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Hub,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Transit Aspects",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            aspects.forEach { aspect ->
                val isHarmonic = aspect.aspectType in listOf("Trine", "Sextile")
                val isBenefic = aspect.transitingPlanet in listOf(
                    com.astro.storm.data.model.Planet.JUPITER,
                    com.astro.storm.data.model.Planet.VENUS
                )
                val aspectColor = when {
                    isHarmonic && isBenefic -> SuccessColor
                    isHarmonic -> AccentGreen
                    aspect.aspectType == "Conjunction" -> AccentGold
                    else -> WarningColor
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tr. ${aspect.transitingPlanet.displayName} ${aspect.aspectType} ${aspect.natalPlanet.displayName}",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (aspect.isApplying) "Applying" else "Separating",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Orb: ${String.format("%.1f", aspect.orb)}°",
                            fontSize = 11.sp,
                            color = aspectColor
                        )
                        Text(
                            text = "${(aspect.strength * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitAshtakavargaScoresCard(scores: Map<com.astro.storm.data.model.Planet, AshtakavargaCalculator.TransitScore>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.GridOn,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transit Ashtakavarga Scores",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            scores.forEach { (planet, score) ->
                val scoreColor = when {
                    score.binduScore >= 5 -> SuccessColor
                    score.binduScore >= 4 -> AccentGreen
                    score.binduScore >= 3 -> WarningColor
                    else -> ErrorColor
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    planetColors[planet] ?: AccentGold,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = planet.displayName,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BAV: ${score.binduScore}",
                            fontSize = 12.sp,
                            color = scoreColor,
                            modifier = Modifier.width(55.dp)
                        )
                        Text(
                            text = "SAV: ${score.savScore}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.width(55.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignificantPeriodsCard(periods: List<TransitAnalyzer.SignificantPeriod>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Event,
                    contentDescription = null,
                    tint = AccentRose,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Significant Upcoming Periods",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            periods.take(5).forEach { period ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = CardBackgroundElevated
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Intensity indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(40.dp)
                        ) {
                            repeat(5) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (index < period.intensity) AccentOrange
                                            else DividerColor,
                                            CircleShape
                                        )
                                )
                                if (index < 4) Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = period.description,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${period.startDate.monthValue}/${period.startDate.dayOfMonth} - ${period.endDate.monthValue}/${period.endDate.dayOfMonth}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============ ASPECTS TAB (Simplified - aspects only) ============

@Composable
private fun AspectsTabContent(chart: VedicChart) {
    val aspectMatrix = remember(chart) {
        AspectCalculator.calculateAspectMatrix(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Aspect summary
        item {
            AspectSummaryCard(aspectMatrix)
        }

        // Major aspects
        item {
            Text(
                text = "Planetary Aspects",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(aspectMatrix.aspects.take(15)) { aspect ->
            AspectCard(aspect)
        }
    }
}

@Composable
private fun AspectSummaryCard(matrix: AspectCalculator.AspectMatrix) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AspectCountBadge("Conjunctions", matrix.conjunctions.size, AccentGold)
            AspectCountBadge("Harmonious", matrix.aspects.filter { it.aspectType.nature == AspectCalculator.AspectNature.HARMONIOUS }.size, SuccessColor)
            AspectCountBadge("Challenging", matrix.aspects.filter { it.aspectType.nature == AspectCalculator.AspectNature.CHALLENGING }.size, ErrorColor)
            AspectCountBadge("Special", matrix.specialAspects.size, AccentPurple)
        }
    }
}

@Composable
private fun AspectCountBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun AspectCard(aspect: AspectCalculator.AspectData) {
    val natureColor = when (aspect.aspectType.nature) {
        AspectCalculator.AspectNature.HARMONIOUS -> SuccessColor
        AspectCalculator.AspectNature.CHALLENGING -> ErrorColor
        AspectCalculator.AspectNature.VARIABLE -> WarningColor
        AspectCalculator.AspectNature.SIGNIFICANT -> AccentPurple
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${aspect.aspectingPlanet.symbol} ${aspect.aspectType.symbol} ${aspect.aspectedPlanet.symbol}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${aspect.aspectingPlanet.displayName} - ${aspect.aspectedPlanet.displayName}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = aspect.aspectType.displayName,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = aspect.strengthDescription,
                    fontSize = 12.sp,
                    color = natureColor
                )
                Text(
                    text = "Orb: ${String.format("%.1f", aspect.exactOrb)}°",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// ============ PANCHANGA TAB ============

@Composable
private fun PanchangaTabContent(chart: VedicChart, context: android.content.Context) {
    var panchangaData by remember { mutableStateOf<PanchangaData?>(null) }

    LaunchedEffect(chart) {
        withContext(Dispatchers.Default) {
            try {
                val calculator = PanchangaCalculator(context)
                panchangaData = calculator.calculatePanchanga(
                    dateTime = chart.birthData.dateTime,
                    latitude = chart.birthData.latitude,
                    longitude = chart.birthData.longitude,
                    timezone = chart.birthData.timezone
                )
                calculator.close()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        panchangaData?.let { panchanga ->
            // Main Panchanga elements
            item {
                PanchangaMainCard(panchanga)
            }

            // Tithi
            item {
                PanchangaElementCard(
                    title = "Tithi",
                    icon = Icons.Outlined.Brightness2,
                    mainValue = panchanga.tithi.tithi.displayName,
                    subValue = panchanga.paksha.displayName,
                    detail = "Lord: ${panchanga.tithi.lord.displayName}",
                    progress = panchanga.tithi.progress.toFloat() / 100f,
                    color = AccentGold
                )
            }

            // Nakshatra
            item {
                PanchangaElementCard(
                    title = "Nakshatra",
                    icon = Icons.Outlined.Star,
                    mainValue = panchanga.nakshatra.nakshatra.displayName,
                    subValue = "Pada ${panchanga.nakshatra.pada}",
                    detail = "Lord: ${panchanga.nakshatra.lord.displayName}",
                    progress = panchanga.nakshatra.progress.toFloat() / 100f,
                    color = AccentTeal
                )
            }

            // Yoga
            item {
                PanchangaElementCard(
                    title = "Yoga",
                    icon = Icons.Outlined.AllInclusive,
                    mainValue = panchanga.yoga.yoga.displayName,
                    subValue = panchanga.yoga.yoga.nature,
                    detail = null,
                    progress = panchanga.yoga.progress.toFloat() / 100f,
                    color = AccentPurple
                )
            }

            // Karana
            item {
                PanchangaElementCard(
                    title = "Karana",
                    icon = Icons.Outlined.HourglassEmpty,
                    mainValue = panchanga.karana.karana.displayName,
                    subValue = panchanga.karana.karana.nature,
                    detail = null,
                    progress = panchanga.karana.progress.toFloat() / 100f,
                    color = AccentBlue
                )
            }

            // Vara
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vara (Day)", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = panchanga.vara.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Day Lord", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = panchanga.vara.lord.displayName,
                                fontSize = 14.sp,
                                color = AccentGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangaMainCard(panchanga: PanchangaData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sunrise", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = panchanga.sunrise,
                        fontSize = 16.sp,
                        color = WarningColor
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Moon Phase", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = "${String.format("%.0f", panchanga.moonPhase)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Sunset", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = panchanga.sunset,
                        fontSize = 16.sp,
                        color = AccentPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun PanchangaElementCard(
    title: String,
    icon: ImageVector,
    mainValue: String,
    subValue: String,
    detail: String?,
    progress: Float,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = mainValue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = subValue,
                        fontSize = 13.sp,
                        color = color
                    )
                    detail?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )

            Text(
                text = "${(progress * 100).toInt()}% complete",
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ============ UTILITY FUNCTIONS ============

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}

private fun formatDegree(degree: Double): String {
    val normalizedDegree = (degree % 360.0 + 360.0) % 360.0
    val deg = normalizedDegree.toInt()
    val min = ((normalizedDegree - deg) * 60).toInt()
    val sec = ((((normalizedDegree - deg) * 60) - min) * 60).toInt()
    return "$deg° $min' $sec\""
}

private fun formatDegreeInSign(longitude: Double): String {
    val degreeInSign = longitude % 30.0
    val deg = degreeInSign.toInt()
    val min = ((degreeInSign - deg) * 60).toInt()
    val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()
    return "$deg° $min' $sec\""
}

private fun formatCoordinate(value: Double, isLatitude: Boolean): String {
    val abs = kotlin.math.abs(value)
    val degrees = abs.toInt()
    val minutes = ((abs - degrees) * 60).toInt()
    val direction = if (isLatitude) {
        if (value >= 0) "N" else "S"
    } else {
        if (value >= 0) "E" else "W"
    }
    return "$degrees° $minutes' $direction"
}
