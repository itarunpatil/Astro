package com.astro.storm.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.currentLanguage
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ephemeris.AspectCalculator
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.ChartViewModel
import com.astro.storm.ephemeris.VedicAstrologyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.floor

/**
 * Synastry/Chart Comparison Screen
 *
 * Advanced Vedic astrology synastry analysis comparing two birth charts to analyze:
 * - Inter-chart aspects between planets
 * - House overlay influences
 * - Overall relationship compatibility
 * - Key synastry indicators (Sun-Moon, Venus-Mars, Ascendant connections)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SynastryScreen(
    savedCharts: List<SavedChart>,
    viewModel: ChartViewModel,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val language = currentLanguage()

    var selectedChart1Id by remember { mutableStateOf<Long?>(null) }
    var selectedChart2Id by remember { mutableStateOf<Long?>(null) }
    var chart1 by remember { mutableStateOf<VedicChart?>(null) }
    var chart2 by remember { mutableStateOf<VedicChart?>(null) }

    var synastryResult by remember { mutableStateOf<SynastryAnalysisResult?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showChart1Selector by remember { mutableStateOf(false) }
    var showChart2Selector by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(StringKey.SYNASTRY_OVERVIEW),
        stringResource(StringKey.SYNASTRY_ASPECTS),
        stringResource(StringKey.SYNASTRY_HOUSES),
        stringResource(StringKey.SYNASTRY_COMPATIBILITY)
    )

    val animatedProgress by animateFloatAsState(
        targetValue = (synastryResult?.overallCompatibility?.div(100.0)?.toFloat() ?: 0f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Load charts when selected
    LaunchedEffect(selectedChart1Id) {
        selectedChart1Id?.let { id ->
            chart1 = withContext(Dispatchers.IO) { viewModel.getChartById(id) }
        } ?: run { chart1 = null }
    }

    LaunchedEffect(selectedChart2Id) {
        selectedChart2Id?.let { id ->
            chart2 = withContext(Dispatchers.IO) { viewModel.getChartById(id) }
        } ?: run { chart2 = null }
    }

    // Calculate synastry when both charts are selected
    LaunchedEffect(chart1, chart2) {
        if (chart1 != null && chart2 != null) {
            isCalculating = true
            errorMessage = null
            delay(300)
            try {
                synastryResult = withContext(Dispatchers.Default) {
                    calculateSynastry(chart1!!, chart2!!, language)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Calculation failed"
            }
            isCalculating = false
        } else {
            synastryResult = null
        }
    }

    if (showInfoDialog) {
        SynastryInfoDialog(onDismiss = { showInfoDialog = false })
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(StringKey.SYNASTRY_TITLE),
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary,
                            fontSize = 18.sp
                        )
                        AnimatedVisibility(visible = synastryResult != null) {
                            Text(
                                stringResource(StringKey.SYNASTRY_SUBTITLE),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(StringKey.BTN_BACK),
                            tint = AppTheme.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = stringResource(StringKey.SYNASTRY_INFO_TITLE),
                            tint = AppTheme.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.ScreenBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Chart Selection Section
            item {
                ChartSelectionSection(
                    savedCharts = savedCharts,
                    selectedChart1Id = selectedChart1Id,
                    selectedChart2Id = selectedChart2Id,
                    chart1 = chart1,
                    chart2 = chart2,
                    onSelectChart1 = { showChart1Selector = true },
                    onSelectChart2 = { showChart2Selector = true },
                    onSwapCharts = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val temp = selectedChart1Id
                        selectedChart1Id = selectedChart2Id
                        selectedChart2Id = temp
                    },
                    onClearSelection = {
                        selectedChart1Id = null
                        selectedChart2Id = null
                    }
                )
            }

            // Loading state
            if (isCalculating) {
                item { SynastryCalculatingState() }
            }

            // Error state
            errorMessage?.let { error ->
                item {
                    SynastryErrorCard(
                        error = error,
                        onRetry = {
                            errorMessage = null
                            // Trigger recalculation
                            val temp1 = chart1
                            val temp2 = chart2
                            chart1 = null
                            chart2 = null
                            chart1 = temp1
                            chart2 = temp2
                        }
                    )
                }
            }

            // Results
            synastryResult?.let { result ->
                // Tab selector
                item {
                    SynastryTabSelector(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                // Tab content
                when (selectedTab) {
                    0 -> item {
                        SynastryOverviewTab(
                            result = result,
                            chart1 = chart1!!,
                            chart2 = chart2!!,
                            animatedProgress = animatedProgress
                        )
                    }
                    1 -> item { SynastryAspectsTab(result = result) }
                    2 -> item { SynastryHouseOverlaysTab(result = result, chart1 = chart1!!, chart2 = chart2!!) }
                    3 -> item { SynastryCompatibilityTab(result = result) }
                }
            }
        }
    }

    // Chart selector bottom sheets
    if (showChart1Selector) {
        ChartSelectorBottomSheet(
            title = stringResource(StringKey.SYNASTRY_CHART_1),
            icon = Icons.Filled.Person,
            accentColor = AppTheme.AccentPrimary,
            charts = savedCharts,
            selectedId = selectedChart1Id,
            excludeId = selectedChart2Id,
            onSelect = { id ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedChart1Id = id
                showChart1Selector = false
            },
            onDismiss = { showChart1Selector = false }
        )
    }

    if (showChart2Selector) {
        ChartSelectorBottomSheet(
            title = stringResource(StringKey.SYNASTRY_CHART_2),
            icon = Icons.Filled.PersonOutline,
            accentColor = AppTheme.AccentTeal,
            charts = savedCharts,
            selectedId = selectedChart2Id,
            excludeId = selectedChart1Id,
            onSelect = { id ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedChart2Id = id
                showChart2Selector = false
            },
            onDismiss = { showChart2Selector = false }
        )
    }
}

// ============================================
// Data Classes for Synastry Analysis
// ============================================

data class SynastryAspect(
    val planet1: Planet,
    val planet1Chart: Int,
    val planet2: Planet,
    val planet2Chart: Int,
    val aspectType: SynastryAspectType,
    val orb: Double,
    val isApplying: Boolean,
    val strength: Double,
    val interpretation: String
)

enum class SynastryAspectType(
    val displayName: String,
    val angle: Double,
    val nature: AspectNature,
    val symbol: String,
    val maxOrb: Double
) {
    CONJUNCTION("Conjunction", 0.0, AspectNature.MAJOR, "☌", 10.0),
    OPPOSITION("Opposition", 180.0, AspectNature.CHALLENGING, "☍", 10.0),
    TRINE("Trine", 120.0, AspectNature.HARMONIOUS, "△", 8.0),
    SQUARE("Square", 90.0, AspectNature.CHALLENGING, "□", 8.0),
    SEXTILE("Sextile", 60.0, AspectNature.HARMONIOUS, "⚹", 6.0),
    QUINCUNX("Quincunx", 150.0, AspectNature.MINOR, "⚻", 3.0),
    SEMI_SEXTILE("Semi-Sextile", 30.0, AspectNature.MINOR, "⚺", 3.0);

    fun getLocalizedName(language: Language): String = when (this) {
        CONJUNCTION -> com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_CONJUNCTION, language)
        OPPOSITION -> com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_OPPOSITION, language)
        TRINE -> com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_TRINE, language)
        SQUARE -> com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_SQUARE, language)
        SEXTILE -> com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_SEXTILE, language)
        QUINCUNX -> displayName
        SEMI_SEXTILE -> displayName
    }
}

enum class AspectNature {
    MAJOR, HARMONIOUS, CHALLENGING, MINOR
}

data class HouseOverlay(
    val planet: Planet,
    val sourceChart: Int,
    val houseNumber: Int,
    val interpretation: String,
    val lifeArea: String
)

data class CompatibilityCategory(
    val name: String,
    val score: Double,
    val maxScore: Double,
    val description: String,
    val icon: ImageVector
)

data class SynastryAnalysisResult(
    val aspects: List<SynastryAspect>,
    val harmoniousAspects: List<SynastryAspect>,
    val challengingAspects: List<SynastryAspect>,
    val houseOverlays1In2: List<HouseOverlay>,
    val houseOverlays2In1: List<HouseOverlay>,
    val compatibilityCategories: List<CompatibilityCategory>,
    val overallCompatibility: Double,
    val keyFindings: List<String>,
    val sunMoonAspects: List<SynastryAspect>,
    val venusMarsAspects: List<SynastryAspect>,
    val ascendantConnections: List<SynastryAspect>
)

// ============================================
// Synastry Calculation Engine
// ============================================

private fun calculateSynastry(
    chart1: VedicChart,
    chart2: VedicChart,
    language: Language
): SynastryAnalysisResult {
    val aspects = mutableListOf<SynastryAspect>()

    // Calculate all inter-chart aspects
    val planetsToAnalyze = listOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN,
        Planet.RAHU, Planet.KETU
    )

    for (planet1 in planetsToAnalyze) {
        val pos1 = chart1.planetPositions.find { it.planet == planet1 } ?: continue

        for (planet2 in planetsToAnalyze) {
            val pos2 = chart2.planetPositions.find { it.planet == planet2 } ?: continue

            // Check for aspects
            for (aspectType in SynastryAspectType.entries) {
                val orb = calculateOrb(pos1.longitude, pos2.longitude, aspectType.angle)
                if (orb <= aspectType.maxOrb) {
                    val strength = calculateAspectStrength(orb, aspectType.maxOrb)
                    val isApplying = isAspectApplying(pos1, pos2, aspectType.angle)
                    val interpretation = generateAspectInterpretation(planet1, planet2, aspectType, language)

                    aspects.add(
                        SynastryAspect(
                            planet1 = planet1,
                            planet1Chart = 1,
                            planet2 = planet2,
                            planet2Chart = 2,
                            aspectType = aspectType,
                            orb = orb,
                            isApplying = isApplying,
                            strength = strength,
                            interpretation = interpretation
                        )
                    )
                }
            }
        }
    }

    // Sort by strength
    val sortedAspects = aspects.sortedByDescending { it.strength }

    // Categorize aspects
    val harmoniousAspects = sortedAspects.filter {
        it.aspectType.nature == AspectNature.HARMONIOUS ||
        (it.aspectType == SynastryAspectType.CONJUNCTION && isBeneficConjunction(it.planet1, it.planet2))
    }

    val challengingAspects = sortedAspects.filter {
        it.aspectType.nature == AspectNature.CHALLENGING ||
        (it.aspectType == SynastryAspectType.CONJUNCTION && isMaleficConjunction(it.planet1, it.planet2))
    }

    // Calculate house overlays
    val houseOverlays1In2 = calculateHouseOverlays(chart1, chart2, 1, language)
    val houseOverlays2In1 = calculateHouseOverlays(chart2, chart1, 2, language)

    // Extract special aspects
    val sunMoonAspects = sortedAspects.filter { aspect ->
        (aspect.planet1 == Planet.SUN && aspect.planet2 == Planet.MOON) ||
        (aspect.planet1 == Planet.MOON && aspect.planet2 == Planet.SUN)
    }

    val venusMarsAspects = sortedAspects.filter { aspect ->
        (aspect.planet1 == Planet.VENUS && aspect.planet2 == Planet.MARS) ||
        (aspect.planet1 == Planet.MARS && aspect.planet2 == Planet.VENUS)
    }

    // Ascendant connections - planets in close aspect to ascendant degree
    val asc1 = chart1.ascendant
    val asc2 = chart2.ascendant
    val ascendantConnections = mutableListOf<SynastryAspect>()

    for (pos in chart2.planetPositions) {
        val orb = calculateOrb(asc1, pos.longitude, 0.0)
        if (orb <= 10.0) {
            ascendantConnections.add(
                SynastryAspect(
                    planet1 = Planet.SUN, // Placeholder for Ascendant
                    planet1Chart = 1,
                    planet2 = pos.planet,
                    planet2Chart = 2,
                    aspectType = SynastryAspectType.CONJUNCTION,
                    orb = orb,
                    isApplying = false,
                    strength = calculateAspectStrength(orb, 10.0),
                    interpretation = generateAscendantInterpretation(pos.planet, 1, language)
                )
            )
        }
    }

    // Calculate compatibility categories
    val compatibilityCategories = calculateCompatibilityCategories(
        harmoniousAspects, challengingAspects, sunMoonAspects, venusMarsAspects, language
    )

    // Calculate overall compatibility
    val totalHarmonious = harmoniousAspects.sumOf { it.strength }
    val totalChallenging = challengingAspects.sumOf { it.strength }
    val overallCompatibility = ((totalHarmonious / (totalHarmonious + totalChallenging + 0.01)) * 100).coerceIn(0.0, 100.0)

    // Generate key findings
    val keyFindings = generateKeyFindings(sortedAspects, houseOverlays1In2, houseOverlays2In1, language)

    return SynastryAnalysisResult(
        aspects = sortedAspects,
        harmoniousAspects = harmoniousAspects,
        challengingAspects = challengingAspects,
        houseOverlays1In2 = houseOverlays1In2,
        houseOverlays2In1 = houseOverlays2In1,
        compatibilityCategories = compatibilityCategories,
        overallCompatibility = overallCompatibility,
        keyFindings = keyFindings,
        sunMoonAspects = sunMoonAspects,
        venusMarsAspects = venusMarsAspects,
        ascendantConnections = ascendantConnections
    )
}

private fun calculateOrb(longitude1: Double, longitude2: Double, targetAngle: Double): Double {
    val diff = abs(VedicAstrologyUtils.normalizeAngle(longitude1 - longitude2))
    val orb = abs(diff - targetAngle)
    return minOf(orb, 360.0 - orb)
}

private fun calculateAspectStrength(orb: Double, maxOrb: Double): Double {
    return ((maxOrb - orb) / maxOrb).coerceIn(0.0, 1.0)
}

private fun isAspectApplying(pos1: PlanetPosition, pos2: PlanetPosition, targetAngle: Double): Boolean {
    val currentDiff = VedicAstrologyUtils.normalizeAngle(pos2.longitude - pos1.longitude)
    val futurePos1 = VedicAstrologyUtils.normalizeAngle(pos1.longitude + pos1.speed)
    val futurePos2 = VedicAstrologyUtils.normalizeAngle(pos2.longitude + pos2.speed)
    val futureDiff = VedicAstrologyUtils.normalizeAngle(futurePos2 - futurePos1)
    val currentOrb = abs(currentDiff - targetAngle)
    val futureOrb = abs(futureDiff - targetAngle)
    return futureOrb < currentOrb
}

private fun isBeneficConjunction(planet1: Planet, planet2: Planet): Boolean {
    val benefics = setOf(Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY)
    return planet1 in benefics || planet2 in benefics
}

private fun isMaleficConjunction(planet1: Planet, planet2: Planet): Boolean {
    val malefics = setOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)
    return planet1 in malefics && planet2 in malefics
}

private fun calculateHouseOverlays(
    sourceChart: VedicChart,
    targetChart: VedicChart,
    sourceChartNum: Int,
    language: Language
): List<HouseOverlay> {
    val overlays = mutableListOf<HouseOverlay>()

    for (pos in sourceChart.planetPositions) {
        val houseNumber = getHouseForLongitude(pos.longitude, targetChart.houseCusps)
        val lifeArea = getLifeAreaForHouse(houseNumber)
        val interpretation = generateHouseOverlayInterpretation(pos.planet, houseNumber, sourceChartNum, language)

        overlays.add(
            HouseOverlay(
                planet = pos.planet,
                sourceChart = sourceChartNum,
                houseNumber = houseNumber,
                interpretation = interpretation,
                lifeArea = lifeArea
            )
        )
    }

    return overlays
}

private fun getHouseForLongitude(longitude: Double, houseCusps: List<Double>): Int {
    val normalizedLong = VedicAstrologyUtils.normalizeAngle(longitude)
    for (i in 0 until 12) {
        val nextIndex = (i + 1) % 12
        val cusp = houseCusps[i]
        val nextCusp = houseCusps[nextIndex]

        if (nextCusp > cusp) {
            if (normalizedLong >= cusp && normalizedLong < nextCusp) {
                return i + 1
            }
        } else {
            if (normalizedLong >= cusp || normalizedLong < nextCusp) {
                return i + 1
            }
        }
    }
    return 1
}

private fun getLifeAreaForHouse(house: Int): String {
    return when (house) {
        1 -> "Self, Identity, Appearance"
        2 -> "Wealth, Values, Family"
        3 -> "Communication, Siblings"
        4 -> "Home, Mother, Emotions"
        5 -> "Romance, Children, Creativity"
        6 -> "Health, Service, Enemies"
        7 -> "Partnership, Marriage"
        8 -> "Transformation, Joint Resources"
        9 -> "Higher Learning, Dharma"
        10 -> "Career, Status, Father"
        11 -> "Gains, Friends, Aspirations"
        12 -> "Spirituality, Loss, Liberation"
        else -> "General"
    }
}

private fun generateAspectInterpretation(
    planet1: Planet,
    planet2: Planet,
    aspectType: SynastryAspectType,
    language: Language
): String {
    val p1Name = planet1.getLocalizedName(language)
    val p2Name = planet2.getLocalizedName(language)

    return when (aspectType.nature) {
        AspectNature.HARMONIOUS -> "$p1Name and $p2Name work together harmoniously, creating mutual understanding and support."
        AspectNature.CHALLENGING -> "$p1Name and $p2Name create tension that requires conscious effort to integrate."
        AspectNature.MAJOR -> "$p1Name and $p2Name are closely connected, amplifying each other's energies."
        AspectNature.MINOR -> "$p1Name and $p2Name have a subtle connection that adds nuance to the relationship."
    }
}

private fun generateAscendantInterpretation(planet: Planet, chartNum: Int, language: Language): String {
    return "${planet.getLocalizedName(language)} conjunct Person $chartNum's Ascendant creates a strong personal connection."
}

private fun generateHouseOverlayInterpretation(planet: Planet, house: Int, chartNum: Int, language: Language): String {
    return "Person $chartNum's ${planet.getLocalizedName(language)} falls in the ${house}th house, influencing ${getLifeAreaForHouse(house).lowercase()}."
}

private fun calculateCompatibilityCategories(
    harmoniousAspects: List<SynastryAspect>,
    challengingAspects: List<SynastryAspect>,
    sunMoonAspects: List<SynastryAspect>,
    venusMarsAspects: List<SynastryAspect>,
    language: Language
): List<CompatibilityCategory> {
    // Emotional Bond (Sun-Moon, Moon-Moon aspects)
    val emotionalScore = sunMoonAspects.filter { it.aspectType.nature == AspectNature.HARMONIOUS || it.aspectType == SynastryAspectType.CONJUNCTION }
        .sumOf { it.strength * 10 }
        .coerceAtMost(10.0)

    // Romance & Attraction (Venus-Mars aspects)
    val romanceScore = venusMarsAspects.filter { it.aspectType.nature == AspectNature.HARMONIOUS || it.aspectType == SynastryAspectType.CONJUNCTION }
        .sumOf { it.strength * 10 }
        .coerceAtMost(10.0)

    // Communication (Mercury aspects)
    val mercuryAspects = harmoniousAspects.filter { it.planet1 == Planet.MERCURY || it.planet2 == Planet.MERCURY }
    val communicationScore = mercuryAspects.sumOf { it.strength * 5 }.coerceAtMost(10.0)

    // Long-term Stability (Saturn aspects)
    val saturnAspects = harmoniousAspects.filter { it.planet1 == Planet.SATURN || it.planet2 == Planet.SATURN }
    val stabilityScore = saturnAspects.sumOf { it.strength * 5 }.coerceAtMost(10.0)

    // Growth & Evolution (Jupiter aspects)
    val jupiterAspects = harmoniousAspects.filter { it.planet1 == Planet.JUPITER || it.planet2 == Planet.JUPITER }
    val growthScore = jupiterAspects.sumOf { it.strength * 5 }.coerceAtMost(10.0)

    return listOf(
        CompatibilityCategory(
            name = com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_EMOTIONAL_BOND, language),
            score = emotionalScore,
            maxScore = 10.0,
            description = "Emotional understanding and nurturing",
            icon = Icons.Filled.Favorite
        ),
        CompatibilityCategory(
            name = com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_ROMANCE, language),
            score = romanceScore,
            maxScore = 10.0,
            description = "Physical attraction and passion",
            icon = Icons.Filled.FavoriteBorder
        ),
        CompatibilityCategory(
            name = com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_COMMUNICATION, language),
            score = communicationScore,
            maxScore = 10.0,
            description = "Mental connection and dialogue",
            icon = Icons.Filled.ChatBubble
        ),
        CompatibilityCategory(
            name = com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_STABILITY, language),
            score = stabilityScore,
            maxScore = 10.0,
            description = "Commitment and endurance",
            icon = Icons.Filled.Shield
        ),
        CompatibilityCategory(
            name = com.astro.storm.data.localization.StringResources.get(StringKey.SYNASTRY_GROWTH, language),
            score = growthScore,
            maxScore = 10.0,
            description = "Mutual expansion and learning",
            icon = Icons.Filled.TrendingUp
        )
    )
}

private fun generateKeyFindings(
    aspects: List<SynastryAspect>,
    overlays1In2: List<HouseOverlay>,
    overlays2In1: List<HouseOverlay>,
    language: Language
): List<String> {
    val findings = mutableListOf<String>()

    // Find strongest aspects
    aspects.take(3).forEach { aspect ->
        findings.add("Strong ${aspect.aspectType.displayName} between ${aspect.planet1.displayName} and ${aspect.planet2.displayName}")
    }

    // Key house placements
    overlays1In2.filter { it.houseNumber in listOf(1, 5, 7, 10) }.take(2).forEach { overlay ->
        findings.add("${overlay.planet.displayName} activates the ${overlay.houseNumber}th house of ${overlay.lifeArea.lowercase()}")
    }

    return findings.take(5)
}

// ============================================
// UI Components
// ============================================

@Composable
private fun ChartSelectionSection(
    savedCharts: List<SavedChart>,
    selectedChart1Id: Long?,
    selectedChart2Id: Long?,
    chart1: VedicChart?,
    chart2: VedicChart?,
    onSelectChart1: () -> Unit,
    onSelectChart2: () -> Unit,
    onSwapCharts: () -> Unit,
    onClearSelection: () -> Unit
) {
    val hasSelection = chart1 != null || chart2 != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(StringKey.SYNASTRY_SELECT_CHARTS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )

                AnimatedVisibility(visible = hasSelection) {
                    Row {
                        if (chart1 != null && chart2 != null) {
                            IconButton(
                                onClick = onSwapCharts,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.SwapHoriz,
                                    contentDescription = stringResource(StringKey.SYNASTRY_SWAP),
                                    tint = AppTheme.AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onClearSelection,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(StringKey.SYNASTRY_CLEAR),
                                tint = AppTheme.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SynastryChartCard(
                    label = stringResource(StringKey.SYNASTRY_CHART_1),
                    chart = chart1,
                    icon = Icons.Filled.Person,
                    color = AppTheme.AccentPrimary,
                    onClick = onSelectChart1,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (chart1 != null && chart2 != null)
                                AppTheme.SuccessColor.copy(alpha = 0.15f)
                            else AppTheme.ChipBackground
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (chart1 != null && chart2 != null) Icons.Filled.CompareArrows else Icons.Outlined.Compare,
                        contentDescription = null,
                        tint = if (chart1 != null && chart2 != null) AppTheme.SuccessColor else AppTheme.TextSubtle,
                        modifier = Modifier.size(22.dp)
                    )
                }

                SynastryChartCard(
                    label = stringResource(StringKey.SYNASTRY_CHART_2),
                    chart = chart2,
                    icon = Icons.Filled.PersonOutline,
                    color = AppTheme.AccentTeal,
                    onClick = onSelectChart2,
                    modifier = Modifier.weight(1f)
                )
            }

            if (savedCharts.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = AppTheme.InfoColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppTheme.InfoColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(StringKey.MATCH_CREATE_CHARTS_FIRST),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.InfoColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SynastryChartCard(
    label: String,
    chart: VedicChart?,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (chart != null) 1f else 0.98f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (chart != null) color.copy(alpha = 0.08f) else AppTheme.ChipBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (chart != null)
            androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.3f))
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (chart != null) color.copy(alpha = 0.15f) else AppTheme.BorderColor),
                contentAlignment = Alignment.Center
            ) {
                if (chart != null) {
                    Text(
                        chart.birthData.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                } else {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = AppTheme.TextSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                chart?.birthData?.name ?: label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (chart != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (chart != null) AppTheme.TextPrimary else AppTheme.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (chart != null) {
                Text(
                    ZodiacSign.fromLongitude(chart.ascendant).displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextSubtle
                )
            } else {
                Text(
                    stringResource(StringKey.MATCH_TAP_TO_SELECT),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextSubtle
                )
            }
        }
    }
}

@Composable
private fun SynastryTabSelector(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs.size) { index ->
            val isSelected = selectedTab == index
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                label = {
                    Text(
                        tabs[index],
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppTheme.AccentPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = AppTheme.AccentPrimary,
                    containerColor = AppTheme.ChipBackground,
                    labelColor = AppTheme.TextSecondary
                )
            )
        }
    }
}

@Composable
private fun SynastryOverviewTab(
    result: SynastryAnalysisResult,
    chart1: VedicChart,
    chart2: VedicChart,
    animatedProgress: Float
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Overall Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(StringKey.SYNASTRY_OVERALL_SCORE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = AppTheme.ChipBackground,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = getCompatibilityColor(result.overallCompatibility),
                        strokeCap = StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.0f", result.overallCompatibility),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Profile comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            chart1.birthData.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.AccentPrimary
                        )
                        Text(
                            ZodiacSign.fromLongitude(chart1.ascendant).displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }

                    Icon(
                        Icons.Filled.CompareArrows,
                        contentDescription = null,
                        tint = AppTheme.TextSubtle,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            chart2.birthData.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.AccentTeal
                        )
                        Text(
                            ZodiacSign.fromLongitude(chart2.ascendant).displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compatibility categories
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                result.compatibilityCategories.forEach { category ->
                    CompatibilityCategoryRow(category = category)
                    if (category != result.compatibilityCategories.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = AppTheme.BorderColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aspect summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(StringKey.SYNASTRY_KEY_ASPECTS),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AspectCountChip(
                        label = stringResource(StringKey.SYNASTRY_HARMONIOUS),
                        count = result.harmoniousAspects.size,
                        color = AppTheme.SuccessColor
                    )
                    AspectCountChip(
                        label = stringResource(StringKey.SYNASTRY_CHALLENGING),
                        count = result.challengingAspects.size,
                        color = AppTheme.WarningColor
                    )
                }
            }
        }

        // Key findings
        if (result.keyFindings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppTheme.CardElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    result.keyFindings.forEach { finding ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Filled.Stars,
                                contentDescription = null,
                                tint = AppTheme.AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                finding,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatibilityCategoryRow(category: CompatibilityCategory) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppTheme.AccentPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = AppTheme.AccentPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            LinearProgressIndicator(
                progress = { (category.score / category.maxScore).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = getCompatibilityColor(category.score * 10),
                trackColor = AppTheme.ChipBackground
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            String.format("%.1f", category.score),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = getCompatibilityColor(category.score * 10)
        )
    }
}

@Composable
private fun AspectCountChip(label: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SynastryAspectsTab(result: SynastryAnalysisResult) {
    val language = currentLanguage()

    Column(modifier = Modifier.padding(16.dp)) {
        if (result.aspects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(StringKey.SYNASTRY_NO_ASPECTS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.TextMuted
                )
            }
        } else {
            result.aspects.take(20).forEach { aspect ->
                AspectCard(aspect = aspect, language = language)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AspectCard(aspect: SynastryAspect, language: Language) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (aspect.aspectType.nature) {
                AspectNature.HARMONIOUS -> AppTheme.SuccessColor.copy(alpha = 0.05f)
                AspectNature.CHALLENGING -> AppTheme.WarningColor.copy(alpha = 0.05f)
                else -> AppTheme.CardBackground
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aspect symbol
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when (aspect.aspectType.nature) {
                            AspectNature.HARMONIOUS -> AppTheme.SuccessColor.copy(alpha = 0.15f)
                            AspectNature.CHALLENGING -> AppTheme.WarningColor.copy(alpha = 0.15f)
                            else -> AppTheme.ChipBackground
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    aspect.aspectType.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    color = when (aspect.aspectType.nature) {
                        AspectNature.HARMONIOUS -> AppTheme.SuccessColor
                        AspectNature.CHALLENGING -> AppTheme.WarningColor
                        else -> AppTheme.TextPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${aspect.planet1.getLocalizedName(language)} ${aspect.aspectType.symbol} ${aspect.planet2.getLocalizedName(language)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    aspect.aspectType.getLocalizedName(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("%.1f°", aspect.orb),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextSecondary
                )
                Text(
                    if (aspect.isApplying) stringResource(StringKey.SYNASTRY_APPLYING)
                    else stringResource(StringKey.SYNASTRY_SEPARATING),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.TextSubtle
                )
            }
        }
    }
}

@Composable
private fun SynastryHouseOverlaysTab(
    result: SynastryAnalysisResult,
    chart1: VedicChart,
    chart2: VedicChart
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Chart 1 planets in Chart 2 houses
        Text(
            "${chart1.birthData.name}'s planets in ${chart2.birthData.name}'s houses",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        result.houseOverlays1In2.forEach { overlay ->
            HouseOverlayCard(overlay = overlay, chartName = chart2.birthData.name)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart 2 planets in Chart 1 houses
        Text(
            "${chart2.birthData.name}'s planets in ${chart1.birthData.name}'s houses",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        result.houseOverlays2In1.forEach { overlay ->
            HouseOverlayCard(overlay = overlay, chartName = chart1.birthData.name)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HouseOverlayCard(overlay: HouseOverlay, chartName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppTheme.AccentPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "H${overlay.houseNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.AccentPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    overlay.planet.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Text(
                    overlay.lifeArea,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
        }
    }
}

@Composable
private fun SynastryCompatibilityTab(result: SynastryAnalysisResult) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Detailed breakdown of each compatibility category
        result.compatibilityCategories.forEach { category ->
            CompatibilityDetailCard(category = category)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CompatibilityDetailCard(category: CompatibilityCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    String.format("%.1f/%.1f", category.score, category.maxScore),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = getCompatibilityColor(category.score * 10)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (category.score / category.maxScore).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = getCompatibilityColor(category.score * 10),
                trackColor = AppTheme.ChipBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                category.description,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun SynastryCalculatingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = AppTheme.AccentPrimary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(StringKey.SYNASTRY_ANALYZING),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun SynastryErrorCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.ErrorColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = AppTheme.ErrorColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.ErrorColor,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(StringKey.BTN_RETRY), color = AppTheme.ErrorColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartSelectorBottomSheet(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    charts: List<SavedChart>,
    selectedId: Long?,
    excludeId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val availableCharts = charts.filter { it.id != excludeId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "${availableCharts.size} charts available",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            if (availableCharts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(StringKey.MATCH_NO_CHARTS_AVAILABLE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCharts, key = { it.id }) { chart ->
                        val isSelected = chart.id == selectedId

                        Surface(
                            onClick = { onSelect(chart.id) },
                            color = if (isSelected) accentColor.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected)
                                androidx.compose.foundation.BorderStroke(1.5.dp, accentColor)
                            else
                                androidx.compose.foundation.BorderStroke(1.dp, AppTheme.BorderColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) accentColor.copy(alpha = 0.15f)
                                            else AppTheme.ChipBackground
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        chart.name.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) accentColor else AppTheme.TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        chart.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.TextPrimary
                                    )
                                    Text(
                                        chart.location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppTheme.TextMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SynastryInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(StringKey.SYNASTRY_INFO_TITLE),
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
        },
        text = {
            Text(
                stringResource(StringKey.SYNASTRY_INFO_DESC),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(StringKey.BTN_CLOSE), color = AppTheme.AccentGold)
            }
        },
        containerColor = AppTheme.CardBackground
    )
}

private fun getCompatibilityColor(score: Double): Color {
    return when {
        score >= 80 -> AppTheme.SuccessColor
        score >= 60 -> Color(0xFF8BC34A)
        score >= 40 -> AppTheme.WarningColor
        else -> AppTheme.ErrorColor
    }
}
