package com.astro.storm.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class TajikaAspectType(val displayName: String, val description: String, val isPositive: Boolean) {
    ITHASALA("Ithasala", "Applying conjunction/aspect - promises fulfillment", true),
    EASARAPHA("Easarapha", "Separating aspect - event has passed or is fading", false),
    NAKTA("Nakta", "Transmission of light with reception", true),
    YAMAYA("Yamaya", "Translation of light between significators", true),
    MANAU("Manau", "Reverse application - slower applies to faster", false),
    KAMBOOLA("Kamboola", "Powerful Ithasala with angular placement", true),
    GAIRI_KAMBOOLA("Gairi-Kamboola", "Weaker form of Kamboola", true),
    KHALASARA("Khalasara", "Frustration - application prevented", false),
    RADDA("Radda", "Refranation - retrograde breaks aspect", false),
    DUHPHALI_KUTTHA("Duhphali-Kuttha", "Malefic intervention breaks yoga", false),
    TAMBIRA("Tambira", "Indirect aspect through intermediary", true),
    KUTTHA("Kuttha", "Impediment to aspect completion", false),
    DURAPHA("Durapha", "Hard aspect causing difficulties", false),
    MUTHASHILA("Muthashila", "Mutual application between planets", true),
    IKKABALA("Ikkabala", "Unity of strength between planets", true)
}

data class TajikaAspect(
    val type: TajikaAspectType,
    val planet1: Planet,
    val planet2: Planet,
    val planet1Longitude: Double,
    val planet2Longitude: Double,
    val orb: Double,
    val aspectAngle: Int,
    val isApplying: Boolean,
    val effectDescription: String,
    val strength: AspectStrength,
    val relatedHouses: List<Int>,
    val prediction: String
)

enum class AspectStrength(val displayName: String, val weight: Double) {
    VERY_STRONG("Very Strong", 1.0),
    STRONG("Strong", 0.8),
    MODERATE("Moderate", 0.6),
    WEAK("Weak", 0.4),
    VERY_WEAK("Very Weak", 0.2)
}

data class Saham(
    val name: String,
    val sanskritName: String,
    val formula: String,
    val longitude: Double,
    val sign: ZodiacSign,
    val house: Int,
    val degree: Double,
    val lord: Planet,
    val lordHouse: Int,
    val lordStrength: String,
    val interpretation: String,
    val isActive: Boolean,
    val activationPeriods: List<String>
)

enum class SahamType(
    val displayName: String,
    val sanskritName: String,
    val description: String,
    val dayFormula: String,
    val nightFormula: String
) {
    PUNYA("Fortune", "Punya Saham", "Overall luck and prosperity", "Moon + Asc - Sun", "Sun + Asc - Moon"),
    VIDYA("Education", "Vidya Saham", "Learning and knowledge", "Mercury + Asc - Sun", "Sun + Asc - Mercury"),
    YASHAS("Fame", "Yashas Saham", "Reputation and recognition", "Jupiter + Asc - Sun", "Sun + Asc - Jupiter"),
    MITRA("Friends", "Mitra Saham", "Friendship and alliances", "Moon + Asc - Mercury", "Mercury + Asc - Moon"),
    MAHATMYA("Greatness", "Mahatmya Saham", "Spiritual achievement", "Jupiter + Asc - Moon", "Moon + Asc - Jupiter"),
    ASHA("Hope", "Asha Saham", "Aspirations and wishes", "Saturn + Asc - Venus", "Venus + Asc - Saturn"),
    SAMARTHA("Capability", "Samartha Saham", "Ability and competence", "Mars + Asc - Saturn", "Saturn + Asc - Mars"),
    BHRATRI("Siblings", "Bhratri Saham", "Brothers and sisters", "Jupiter + Asc - Saturn", "Saturn + Asc - Jupiter"),
    PITRI("Father", "Pitri Saham", "Father's welfare", "Saturn + Asc - Sun", "Sun + Asc - Saturn"),
    MATRI("Mother", "Matri Saham", "Mother's welfare", "Moon + Asc - Venus", "Venus + Asc - Moon"),
    PUTRA("Children", "Putra Saham", "Offspring and progeny", "Jupiter + Asc - Moon", "Moon + Asc - Jupiter"),
    VIVAHA("Marriage", "Vivaha Saham", "Matrimony and partnership", "Venus + Asc - Saturn", "Saturn + Asc - Venus"),
    KARMA("Career", "Karma Saham", "Profession and livelihood", "Saturn + Asc - Sun", "Sun + Asc - Saturn"),
    ROGA("Disease", "Roga Saham", "Health challenges", "Saturn + Asc - Mars", "Mars + Asc - Saturn"),
    MRITYU("Longevity", "Mrityu Saham", "Life span indicators", "Saturn + Asc - Moon", "Moon + Asc - Saturn"),
    PARADESA("Foreign", "Paradesa Saham", "Travel and foreign lands", "Saturn + Asc - 9th Lord", "9th Lord + Asc - Saturn"),
    DHANA("Wealth", "Dhana Saham", "Financial prosperity", "2nd Lord + Asc - 2nd Cusp", "Reverse"),
    RAJA("Power", "Raja Saham", "Authority and position", "Sun + Asc - Saturn", "Saturn + Asc - Sun"),
    BANDHANA("Bondage", "Bandhana Saham", "Restrictions and obstacles", "Saturn + Asc - Rahu", "Rahu + Asc - Saturn"),
    KARYASIDDHI("Success", "Karyasiddhi Saham", "Accomplishment of goals", "Saturn + Asc - Sun", "Sun + Asc - Saturn")
}

data class PanchaVargiyaBala(
    val planet: Planet,
    val uchcha: Double,
    val hadda: Double,
    val dreshkana: Double,
    val navamsha: Double,
    val dwadashamsha: Double,
    val total: Double,
    val category: String
)

data class TriPatakiChakra(
    val risingSign: ZodiacSign,
    val sectors: List<TriPatakiSector>,
    val dominantInfluence: String,
    val interpretation: String
)

data class TriPatakiSector(
    val name: String,
    val signs: List<ZodiacSign>,
    val planets: List<Planet>,
    val influence: String
)

data class MuddaDashaPeriod(
    val planet: Planet,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: Int,
    val subPeriods: List<MuddaAntardasha>,
    val planetStrength: String,
    val houseRuled: List<Int>,
    val prediction: String,
    val keywords: List<String>,
    val isCurrent: Boolean,
    val progressPercent: Float
)

data class MuddaAntardasha(
    val planet: Planet,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: Int,
    val interpretation: String
)

data class ExtendedVarshaphalaResult(
    val year: Int,
    val age: Int,
    val solarReturnChart: SolarReturnChart,
    val yearLord: Planet,
    val yearLordStrength: String,
    val yearLordHouse: Int,
    val yearLordDignity: String,
    val muntha: MunthaResult,
    val muddaDasha: List<MuddaDashaPeriod>,
    val tajikaAspects: List<TajikaAspect>,
    val sahams: List<Saham>,
    val panchaVargiyaBala: List<PanchaVargiyaBala>,
    val triPatakiChakra: TriPatakiChakra,
    val housePredictions: List<HousePrediction>,
    val majorThemes: List<String>,
    val favorableMonths: List<Int>,
    val challengingMonths: List<Int>,
    val overallPrediction: String,
    val yearRating: Float,
    val keyDates: List<KeyDate>
)

data class SolarReturnChart(
    val solarReturnTime: LocalDateTime,
    val sunLongitude: Double,
    val ascendant: ZodiacSign,
    val ascendantDegree: Double,
    val moonSign: ZodiacSign,
    val moonNakshatra: String,
    val planetPositions: Map<Planet, PlanetPosition>
)

data class PlanetPosition(
    val longitude: Double,
    val sign: ZodiacSign,
    val house: Int,
    val degree: Double,
    val nakshatra: String,
    val nakshatraPada: Int,
    val isRetrograde: Boolean,
    val speed: Double
)

data class MunthaResult(
    val sign: ZodiacSign,
    val house: Int,
    val degree: Double,
    val lord: Planet,
    val lordHouse: Int,
    val lordStrength: String,
    val interpretation: String,
    val themes: List<String>
)

data class HousePrediction(
    val house: Int,
    val signOnCusp: ZodiacSign,
    val houseLord: Planet,
    val lordPosition: Int,
    val planetsInHouse: List<Planet>,
    val strength: String,
    val keywords: List<String>,
    val prediction: String,
    val rating: Float,
    val specificEvents: List<String>
)

data class KeyDate(
    val date: LocalDate,
    val event: String,
    val type: KeyDateType,
    val description: String
)

enum class KeyDateType {
    FAVORABLE, CHALLENGING, IMPORTANT, TRANSIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarshaphalaScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val currentYear = LocalDate.now().year
    val birthYear = chart?.birthData?.dateTime?.year ?: currentYear
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var varshaphalaResult by remember { mutableStateOf<ExtendedVarshaphalaResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Overview", "Tajika", "Sahams", "Dasha", "Houses")

    LaunchedEffect(chart, selectedYear) {
        if (chart != null && selectedYear >= birthYear) {
            isLoading = true
            error = null
            withContext(Dispatchers.IO) {
                try {
                    varshaphalaResult = calculateExtendedVarshaphala(chart, selectedYear)
                } catch (e: Exception) {
                    error = "Calculation error: ${e.message ?: "Unknown error"}"
                }
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Varshaphala",
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        varshaphalaResult?.let {
                            Text(
                                "Annual Horoscope • Age ${it.age}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.TextPrimary
                        )
                    }
                },
                actions = {
                    varshaphalaResult?.let { result ->
                        YearRatingBadge(result.yearRating)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.ScreenBackground
                )
            )
        },
        containerColor = AppTheme.ScreenBackground
    ) { paddingValues ->
        if (chart == null) {
            EmptyState()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            YearSelector(
                currentYear = selectedYear,
                birthYear = birthYear,
                onYearChange = { selectedYear = it }
            )

            if (isLoading) {
                LoadingState()
                return@Scaffold
            }

            error?.let { errorMsg ->
                ErrorState(errorMsg) { selectedYear = currentYear }
                return@Scaffold
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = AppTheme.AccentPrimary,
                divider = { HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.3f)) },
                edgePadding = 8.dp,
indicator = @Composable { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.BottomStart)
                .offset(x = tabPositions[selectedTab].left)
                .width(tabPositions[selectedTab].width),
                            color = AppTheme.AccentPrimary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) AppTheme.AccentPrimary else AppTheme.TextMuted
                            )
                        }
                    )
                }
            }

            varshaphalaResult?.let { result ->
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        slideInHorizontally { direction * it / 4 } + fadeIn() togetherWith
                                slideOutHorizontally { -direction * it / 4 } + fadeOut()
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> OverviewTab(result)
                        1 -> TajikaAspectsTab(result)
                        2 -> SahamsTab(result)
                        3 -> DashaTab(result)
                        4 -> HousesTab(result)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearRatingBadge(rating: Float) {
    val color = when {
        rating >= 4.0f -> AppTheme.SuccessColor
        rating >= 3.0f -> AppTheme.AccentGold
        rating >= 2.0f -> AppTheme.WarningColor
        else -> AppTheme.ErrorColor
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                String.format("%.1f", rating),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun YearSelector(
    currentYear: Int,
    birthYear: Int,
    onYearChange: (Int) -> Unit
) {
    val maxYear = LocalDate.now().year + 10
    val years = (birthYear..maxYear).toList()
    val scrollState = rememberScrollState()
    val currentYearIndex = years.indexOf(currentYear)

    LaunchedEffect(currentYearIndex) {
        if (currentYearIndex >= 0) {
            scrollState.animateScrollTo(currentYearIndex * 80)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentYear > birthYear) onYearChange(currentYear - 1) },
                    enabled = currentYear > birthYear
                ) {
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = "Previous year",
                        tint = if (currentYear > birthYear) AppTheme.TextPrimary else AppTheme.TextSubtle
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$currentYear - ${currentYear + 1}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Year ${currentYear - birthYear + 1} of life",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }

                IconButton(
                    onClick = { if (currentYear < maxYear) onYearChange(currentYear + 1) },
                    enabled = currentYear < maxYear
                ) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Next year",
                        tint = if (currentYear < maxYear) AppTheme.TextPrimary else AppTheme.TextSubtle
                    )
                }
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                years.forEach { year ->
                    val isSelected = year == currentYear
                    val isFuture = year > LocalDate.now().year

                    FilterChip(
                        selected = isSelected,
                        onClick = { onYearChange(year) },
                        label = {
                            Text(
                                year.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.AccentPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = AppTheme.AccentPrimary
                        ),
                        border = if (isFuture) FilterChipDefaults.filterChipBorder(
                            borderColor = AppTheme.AccentGold.copy(alpha = 0.5f),
                            enabled = true,
                            selected = isSelected
                        ) else null
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = AppTheme.AccentPrimary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Computing Annual Horoscope...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Calculating Tajika aspects, Sahams & Mudda Dasha",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = AppTheme.ErrorColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Calculation Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.ErrorColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.AccentPrimary)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Current Year")
            }
        }
    }
}

@Composable
private fun OverviewTab(result: ExtendedVarshaphalaResult) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { SolarReturnCard(result) }
        item { YearLordMunthaCard(result) }
        item { AnnualChartVisualization(result) }
        item { PanchaVargiyaBalaCard(result) }
        item { TriPatakiChakraCard(result) }
        item { MajorThemesCard(result) }
        item { MonthsCard(result) }
        item { KeyDatesCard(result) }
        item { OverallPredictionCard(result) }
    }
}

@Composable
private fun SolarReturnCard(result: ExtendedVarshaphalaResult) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AppTheme.PlanetSun,
                                    AppTheme.PlanetSun.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.WbSunny,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Solar Return",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Sun returns to natal position",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Age ${result.age}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.AccentPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Return Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        result.solarReturnChart.solarReturnTime.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        result.solarReturnChart.solarReturnTime.format(timeFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoChip(
                    label = "Ascendant",
                    value = "${result.solarReturnChart.ascendant.displayName} ${String.format("%.1f", result.solarReturnChart.ascendantDegree)}°",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip(
                    label = "Moon",
                    value = "${result.solarReturnChart.moonSign.displayName}",
                    subValue = result.solarReturnChart.moonNakshatra,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AppTheme.ChipBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            subValue?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun YearLordMunthaCard(result: ExtendedVarshaphalaResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(getPlanetColor(result.yearLord).copy(alpha = 0.15f))
                            .border(
                                width = 3.dp,
                                color = getPlanetColor(result.yearLord),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            result.yearLord.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = getPlanetColor(result.yearLord)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "Year Lord",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                        Text(
                            result.yearLord.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StrengthBadge(result.yearLordStrength)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "House ${result.yearLordHouse}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppTheme.TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                result.yearLordDignity,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    AppTheme.AccentGold,
                                    AppTheme.AccentGold.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Adjust,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "Muntha",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Text(
                                result.muntha.sign.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.TextPrimary
                            )
                            Text(
                                "House ${result.muntha.house} • ${String.format("%.1f", result.muntha.degree)}°",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppTheme.TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Lord: ${result.muntha.lord.displayName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = getPlanetColor(result.muntha.lord),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "in House ${result.muntha.lordHouse}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(result.muntha.themes) { theme ->
                            Surface(
                                color = AppTheme.AccentGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    theme,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.AccentGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        result.muntha.interpretation,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StrengthBadge(strength: String) {
    Surface(
        color = getStrengthColor(strength).copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            strength,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = getStrengthColor(strength),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun AnnualChartVisualization(result: ExtendedVarshaphalaResult) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Tajika Annual Chart",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                }

                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    SouthIndianChart(
                        planetPositions = result.solarReturnChart.planetPositions,
                        ascendantSign = result.solarReturnChart.ascendant,
                        munthaSign = result.muntha.sign,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ChartLegendItem("Asc", AppTheme.AccentPrimary)
                        ChartLegendItem("Muntha", AppTheme.AccentGold)
                        ChartLegendItem("Benefic", AppTheme.SuccessColor)
                        ChartLegendItem("Malefic", AppTheme.ErrorColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun SouthIndianChart(
    planetPositions: Map<Planet, PlanetPosition>,
    ascendantSign: ZodiacSign,
    munthaSign: ZodiacSign,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val cellSize = size.width / 4
        val strokeWidth = 2.dp.toPx()

        drawRect(
            color = AppTheme.CardBackgroundElevated,
            size = size
        )

        for (i in 0..4) {
            drawLine(
                color = AppTheme.DividerColor,
                start = Offset(i * cellSize, 0f),
                end = Offset(i * cellSize, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = AppTheme.DividerColor,
                start = Offset(0f, i * cellSize),
                end = Offset(size.width, i * cellSize),
                strokeWidth = strokeWidth
            )
        }

        val housePositions = listOf(
            Pair(1, Offset(1.5f * cellSize, 0.5f * cellSize)),
            Pair(2, Offset(0.5f * cellSize, 0.5f * cellSize)),
            Pair(3, Offset(0.5f * cellSize, 1.5f * cellSize)),
            Pair(4, Offset(0.5f * cellSize, 2.5f * cellSize)),
            Pair(5, Offset(0.5f * cellSize, 3.5f * cellSize)),
            Pair(6, Offset(1.5f * cellSize, 3.5f * cellSize)),
            Pair(7, Offset(2.5f * cellSize, 3.5f * cellSize)),
            Pair(8, Offset(3.5f * cellSize, 3.5f * cellSize)),
            Pair(9, Offset(3.5f * cellSize, 2.5f * cellSize)),
            Pair(10, Offset(3.5f * cellSize, 1.5f * cellSize)),
            Pair(11, Offset(3.5f * cellSize, 0.5f * cellSize)),
            Pair(12, Offset(2.5f * cellSize, 0.5f * cellSize))
        )

        val signOrder = ZodiacSign.entries
        val ascIndex = signOrder.indexOf(ascendantSign)

        housePositions.forEach { (house, position) ->
            val signIndex = (ascIndex + house - 1) % 12
            val sign = signOrder[signIndex]

            val textLayout = textMeasurer.measure(
                text = getZodiacSymbol(sign),
                style = TextStyle(
                    fontSize = 10.sp,
                    color = AppTheme.TextMuted
                )
            )

            val textX = when (house) {
                1, 6, 7, 12 -> position.x - cellSize / 2 + 8.dp.toPx()
                else -> position.x - cellSize / 2 + 8.dp.toPx()
            }
            val textY = when (house) {
                in 1..2, 11, 12 -> position.y - cellSize / 2 + 8.dp.toPx()
                else -> position.y - cellSize / 2 + 8.dp.toPx()
            }

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(textX, textY)
            )

            if (sign == ascendantSign) {
                drawCircle(
                    color = AppTheme.AccentPrimary.copy(alpha = 0.2f),
                    radius = cellSize / 3,
                    center = position
                )
            }

            if (sign == munthaSign) {
                drawCircle(
                    color = AppTheme.AccentGold,
                    radius = 6.dp.toPx(),
                    center = Offset(position.x + cellSize / 3, position.y - cellSize / 3),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        planetPositions.forEach { (planet, pos) ->
            val houseIndex = pos.house - 1
            if (houseIndex in 0..11) {
                val basePosition = housePositions[houseIndex].second
                val planetCount = planetPositions.count { it.value.house == pos.house }
                val planetIndex = planetPositions.filter { it.value.house == pos.house }
                    .keys.toList().indexOf(planet)

                val offsetX = (planetIndex % 2) * 24.dp.toPx() - 12.dp.toPx()
                val offsetY = (planetIndex / 2) * 18.dp.toPx()

                val planetPos = Offset(
                    basePosition.x + offsetX,
                    basePosition.y + offsetY
                )

                val textLayout = textMeasurer.measure(
                    text = planet.symbol,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = getPlanetColor(planet)
                    )
                )

                if (pos.isRetrograde) {
                    drawCircle(
                        color = AppTheme.WarningColor.copy(alpha = 0.3f),
                        radius = 12.dp.toPx(),
                        center = planetPos
                    )
                }

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        planetPos.x - textLayout.size.width / 2,
                        planetPos.y - textLayout.size.height / 2
                    )
                )
            }
        }

        drawRect(
            color = AppTheme.CardBackground,
            topLeft = Offset(cellSize, cellSize),
            size = Size(cellSize * 2, cellSize * 2)
        )

        val titleLayout = textMeasurer.measure(
            text = "Varshaphala",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
        )
        drawText(
            textLayoutResult = titleLayout,
            topLeft = Offset(
                (size.width - titleLayout.size.width) / 2,
                size.height / 2 - titleLayout.size.height
            )
        )
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
    }
}

@Composable
private fun PanchaVargiyaBalaCard(result: ExtendedVarshaphalaResult) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Assessment,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Pancha Vargiya Bala",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Five-fold Planetary Strength",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Spacer(modifier = Modifier.width(80.dp))
                        listOf("Uchcha", "Hadda", "Drekkana", "Navamsha", "Dwadashamsha", "Total").forEach { header ->
                            Text(
                                header.take(5),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.TextMuted,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    result.panchaVargiyaBala.forEach { bala ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.width(80.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    bala.planet.symbol,
                                    color = getPlanetColor(bala.planet),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    bala.planet.displayName.take(4),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.TextPrimary
                                )
                            }

                            listOf(bala.uchcha, bala.hadda, bala.dreshkana, bala.navamsha, bala.dwadashamsha).forEach { value ->
                                BalaValueCell(value)
                            }

                            Surface(
                                color = getBalaColor(bala.total).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.width(48.dp)
                            ) {
                                Text(
                                    String.format("%.1f", bala.total),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getBalaColor(bala.total),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BalaLegend("Excellent", Color(0xFF4CAF50))
                        BalaLegend("Good", Color(0xFF8BC34A))
                        BalaLegend("Medium", Color(0xFFFFC107))
                        BalaLegend("Weak", Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
private fun BalaValueCell(value: Double) {
    Text(
        String.format("%.1f", value),
        style = MaterialTheme.typography.labelSmall,
        color = getBalaColor(value),
        textAlign = TextAlign.Center,
        modifier = Modifier.width(48.dp)
    )
}

@Composable
private fun BalaLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
    }
}

private fun getBalaColor(value: Double): Color {
    return when {
        value >= 15 -> Color(0xFF4CAF50)
        value >= 10 -> Color(0xFF8BC34A)
        value >= 5 -> Color(0xFFFFC107)
        else -> Color(0xFFFF9800)
    }
}

@Composable
private fun TriPatakiChakraCard(result: ExtendedVarshaphalaResult) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Pentagon,
                        contentDescription = null,
                        tint = AppTheme.AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Tri-Pataki Chakra",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Three-Flag Diagram",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    result.triPatakiChakra.sectors.forEachIndexed { index, sector ->
                        val sectorColor = when (index) {
                            0 -> AppTheme.SuccessColor
                            1 -> AppTheme.WarningColor
                            else -> AppTheme.ErrorColor
                        }

                        Surface(
                            color = sectorColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        sector.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = sectorColor
                                    )
                                    Row {
                                        sector.signs.forEach { sign ->
                                            Text(
                                                getZodiacSymbol(sign),
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (sector.planets.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        Text(
                                            "Planets: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppTheme.TextMuted
                                        )
                                        sector.planets.forEach { planet ->
                                            Text(
                                                planet.symbol,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = getPlanetColor(planet),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    sector.influence,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = AppTheme.ChipBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            result.triPatakiChakra.interpretation,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextSecondary,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MajorThemesCard(result: ExtendedVarshaphalaResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Major Themes for ${result.year}-${result.year + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            result.majorThemes.forEachIndexed { index, theme ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                AppTheme.AccentGold.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.AccentGold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        theme,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthsCard(result: ExtendedVarshaphalaResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Monthly Energy Flow",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            val months = (1..12).map { month ->
                val adjustedMonth = ((result.solarReturnChart.solarReturnTime.monthValue - 1 + month - 1) % 12) + 1
                adjustedMonth
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                months.forEachIndexed { index, month ->
                    val isFavorable = month in result.favorableMonths
                    val isChallenging = month in result.challengingMonths

                    val color = when {
                        isFavorable -> AppTheme.SuccessColor
                        isChallenging -> AppTheme.ErrorColor
                        else -> AppTheme.TextMuted
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Text(
                            getMonthName(month).take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f))
                                .border(
                                    width = if (isFavorable || isChallenging) 2.dp else 1.dp,
                                    color = color,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when {
                                    isFavorable -> Icons.Filled.ThumbUp
                                    isChallenging -> Icons.Filled.Warning
                                    else -> Icons.Filled.Remove
                                },
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "M${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextSubtle,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(Icons.Filled.ThumbUp, "Favorable", AppTheme.SuccessColor)
                LegendItem(Icons.Filled.Warning, "Challenging", AppTheme.ErrorColor)
                LegendItem(Icons.Filled.Remove, "Neutral", AppTheme.TextMuted)
            }
        }
    }
}

@Composable
private fun LegendItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
    }
}

@Composable
private fun KeyDatesCard(result: ExtendedVarshaphalaResult) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Event,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Key Dates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "${result.keyDates.size} important dates",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    result.keyDates.take(10).forEach { keyDate ->
                        val color = when (keyDate.type) {
                            KeyDateType.FAVORABLE -> AppTheme.SuccessColor
                            KeyDateType.CHALLENGING -> AppTheme.ErrorColor
                            KeyDateType.IMPORTANT -> AppTheme.AccentGold
                            KeyDateType.TRANSIT -> AppTheme.AccentPrimary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, CircleShape)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    keyDate.date.format(dateFormatter),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.TextPrimary
                                )
                                Text(
                                    keyDate.event,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = color,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    keyDate.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallPredictionCard(result: ExtendedVarshaphalaResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.CardBackgroundElevated
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Summarize,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Year Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                result.overallPrediction,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun TajikaAspectsTab(result: ExtendedVarshaphalaResult) {
    val positiveAspects = result.tajikaAspects.filter { it.type.isPositive }
    val negativeAspects = result.tajikaAspects.filter { !it.type.isPositive }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Positive", "Challenging")

    val displayedAspects = when (selectedFilter) {
        "Positive" -> positiveAspects
        "Challenging" -> negativeAspects
        else -> result.tajikaAspects
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            TajikaOverviewCard(result.tajikaAspects)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                filter,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = if (selectedFilter == filter) {
                            {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.AccentPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = AppTheme.AccentPrimary,
                            selectedLeadingIconColor = AppTheme.AccentPrimary
                        )
                    )
                }
            }
        }

        if (displayedAspects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${selectedFilter.lowercase()} Tajika aspects found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.TextMuted
                    )
                }
            }
        } else {
            items(displayedAspects) { aspect ->
                TajikaAspectCard(aspect)
            }
        }
    }
}

@Composable
private fun TajikaOverviewCard(aspects: List<TajikaAspect>) {
    val aspectCounts = aspects.groupBy { it.type }.mapValues { it.value.size }
    val positiveCount = aspects.count { it.type.isPositive }
    val negativeCount = aspects.size - positiveCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Insights,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Tajika Yogas Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Annual planetary aspects and combinations",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = "${aspects.size}",
                    label = "Total Aspects",
                    color = AppTheme.AccentPrimary
                )
                StatBox(
                    value = "$positiveCount",
                    label = "Favorable",
                    color = AppTheme.SuccessColor
                )
                StatBox(
                    value = "$negativeCount",
                    label = "Challenging",
                    color = AppTheme.ErrorColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            val dominantAspect = aspectCounts.maxByOrNull { it.value }
            dominantAspect?.let { (type, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (type.isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (type.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Dominant: ${type.displayName} ($count occurrences)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.TextSecondary
                    )
                }
            }

            val hasIthasala = aspects.any { it.type == TajikaAspectType.ITHASALA }
            val hasEasarapha = aspects.any { it.type == TajikaAspectType.EASARAPHA }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                buildString {
                    if (hasIthasala && !hasEasarapha) {
                        append("Strong Ithasala yogas indicate excellent potential for new beginnings and fulfillment of desires this year.")
                    } else if (hasEasarapha && !hasIthasala) {
                        append("Predominant Easarapha patterns suggest completion of pending matters and gradual conclusion of past endeavors.")
                    } else if (hasIthasala && hasEasarapha) {
                        append("Mixed Ithasala and Easarapha patterns indicate both new opportunities and resolution of past matters.")
                    } else {
                        append("The Tajika yogas this year suggest a period of stability with gradual developments.")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
    }
}

@Composable
private fun TajikaAspectCard(aspect: TajikaAspect) {
    var isExpanded by remember { mutableStateOf(false) }

    val aspectColor = if (aspect.type.isPositive) AppTheme.SuccessColor else AppTheme.ErrorColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(aspectColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row {
                            Text(
                                aspect.planet1.symbol,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = getPlanetColor(aspect.planet1)
                            )
                            Text(
                                "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                            Text(
                                aspect.planet2.symbol,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = getPlanetColor(aspect.planet2)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            aspect.type.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "${aspect.planet1.displayName} - ${aspect.planet2.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = aspectColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (aspect.type.isPositive) "Favorable" else "Challenging",
                            style = MaterialTheme.typography.labelSmall,
                            color = aspectColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${aspect.aspectAngle}° | ${String.format("%.1f", aspect.orb)}° orb",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        aspect.type.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Strength",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            StrengthBadge(aspect.strength.displayName)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Related Houses",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Text(
                                aspect.relatedHouses.joinToString(", ") { "H$it" },
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Effect",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        aspect.effectDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = AppTheme.ChipBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            aspect.prediction,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextPrimary,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SahamsTab(result: ExtendedVarshaphalaResult) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Personal", "Family", "Wealth", "Spiritual")

    val categorizedSahams = result.sahams.groupBy { saham ->
        when (saham.name) {
            "Fortune", "Capability", "Fame", "Hope" -> "Personal"
            "Father", "Mother", "Siblings", "Children", "Marriage" -> "Family"
            "Wealth", "Career", "Power" -> "Wealth"
            "Education", "Greatness", "Longevity" -> "Spiritual"
            else -> "Other"
        }
    }

    val displayedSahams = if (selectedCategory == "All") {
        result.sahams
    } else {
        categorizedSahams[selectedCategory] ?: emptyList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SahamsOverviewCard(result.sahams)
        }

        item {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                category,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.AccentGold.copy(alpha = 0.2f),
                            selectedLabelColor = AppTheme.AccentGold
                        )
                    )
                }
            }
        }

        items(displayedSahams) { saham ->
            SahamCard(saham)
        }
    }
}

@Composable
private fun SahamsOverviewCard(sahams: List<Saham>) {
    val activeSahams = sahams.count { it.isActive }
    val strongSahams = sahams.filter { it.lordStrength in listOf("Strong", "Very Strong") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Token,
                    contentDescription = null,
                    tint = AppTheme.AccentGold,
                    modifier =                      Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Sahams (Arabic Parts)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Sensitive points for specific life areas",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = "${sahams.size}",
                    label = "Total Sahams",
                    color = AppTheme.AccentGold
                )
                StatBox(
                    value = "$activeSahams",
                    label = "Active",
                    color = AppTheme.SuccessColor
                )
                StatBox(
                    value = "${strongSahams.size}",
                    label = "Strong",
                    color = AppTheme.AccentPrimary
                )
            }

            if (strongSahams.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Prominent Sahams This Year:",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(strongSahams.take(5)) { saham ->
                        Surface(
                            color = AppTheme.AccentGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    saham.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.AccentGold
                                )
                                Text(
                                    getZodiacSymbol(saham.sign),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SahamCard(saham: Saham) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (saham.isActive)
                AppTheme.AccentGold.copy(alpha = 0.05f)
            else AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (saham.isActive)
                                    AppTheme.AccentGold.copy(alpha = 0.15f)
                                else AppTheme.ChipBackground
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                                getZodiacSymbol(saham.sign),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (saham.isActive) AppTheme.AccentGold else AppTheme.TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                saham.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.TextPrimary
                            )
                            if (saham.isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AppTheme.SuccessColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.SuccessColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            saham.sanskritName,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "House ${saham.house}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextSecondary
                    )
                    Text(
                        "${String.format("%.1f", saham.degree)}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Formula",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Text(
                                saham.formula,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Lord",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    saham.lord.symbol,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = getPlanetColor(saham.lord)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "in H${saham.lordHouse}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Lord Strength",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                        StrengthBadge(saham.lordStrength)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Interpretation",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        saham.interpretation,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (saham.activationPeriods.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Activation Periods",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(saham.activationPeriods) { period ->
                                Surface(
                                    color = AppTheme.ChipBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        period,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
private fun DashaTab(result: ExtendedVarshaphalaResult) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    var expandedPeriod by remember { mutableStateOf<Planet?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            MuddaDashaOverviewCard(result.muddaDasha)
        }

        item {
            Text(
                "Mudda Dasha Periods",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        itemsIndexed(result.muddaDasha) { index, period ->
            MuddaDashaPeriodCard(
                period = period,
                index = index,
                isExpanded = expandedPeriod == period.planet,
                onExpand = {
                    expandedPeriod = if (expandedPeriod == period.planet) null else period.planet
                }
            )
        }
    }
}

@Composable
private fun MuddaDashaOverviewCard(periods: List<MuddaDashaPeriod>) {
    val currentPeriod = periods.find { it.isCurrent }
    val today = LocalDate.now()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Mudda Dasha (Annual Periods)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Tajika annual planetary periods",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            currentPeriod?.let { period ->
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = getPlanetColor(period.planet).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(getPlanetColor(period.planet).copy(alpha = 0.2f))
                                        .border(
                                            width = 3.dp,
                                            color = getPlanetColor(period.planet),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        period.planet.symbol,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = getPlanetColor(period.planet)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        "Current Period",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.TextMuted
                                    )
                                    Text(
                                        period.planet.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.TextPrimary
                                    )
                                    StrengthBadge(period.planetStrength)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${period.days} days",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getPlanetColor(period.planet)
                                )
                                val daysRemaining = ChronoUnit.DAYS.between(today, period.endDate)
                                Text(
                                    "$daysRemaining days left",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { period.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = getPlanetColor(period.planet),
                            trackColor = getPlanetColor(period.planet).copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(period.keywords) { keyword ->
                                Surface(
                                    color = getPlanetColor(period.planet).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        keyword,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = getPlanetColor(period.planet),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
private fun MuddaDashaPeriodCard(
    period: MuddaDashaPeriod,
    index: Int,
    isExpanded: Boolean,
    onExpand: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
    val fullDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onExpand),
        colors = CardDefaults.cardColors(
            containerColor = if (period.isCurrent)
                getPlanetColor(period.planet).copy(alpha = 0.08f)
            else AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getPlanetColor(period.planet).copy(alpha = 0.15f))
                            .border(
                                width = if (period.isCurrent) 2.dp else 1.dp,
                                color = getPlanetColor(period.planet),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            period.planet.symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = getPlanetColor(period.planet)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                period.planet.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.TextPrimary
                            )
                            if (period.isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AppTheme.SuccessColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "Current",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.SuccessColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            "${period.startDate.format(dateFormatter)} - ${period.endDate.format(dateFormatter)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${period.days}d",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.TextSecondary
                        )
                        StrengthBadge(period.planetStrength)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = AppTheme.TextMuted
                    )
                }
            }

            if (period.isCurrent) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { period.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = getPlanetColor(period.planet),
                    trackColor = getPlanetColor(period.planet).copy(alpha = 0.2f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Houses Ruled",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Text(
                                period.houseRuled.joinToString(", ") { "H$it" },
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Full Period",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Text(
                                "${period.startDate.format(fullDateFormatter)} - ${period.endDate.format(fullDateFormatter)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(period.keywords) { keyword ->
                            Surface(
                                color = AppTheme.ChipBackground,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    keyword,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        period.prediction,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (period.subPeriods.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Sub-Periods (Antardasha)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        period.subPeriods.forEach { subPeriod ->
                            val isCurrentSub = LocalDate.now() >= subPeriod.startDate &&
                                    LocalDate.now() <= subPeriod.endDate

                            Surface(
                                color = if (isCurrentSub)
                                    getPlanetColor(subPeriod.planet).copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            subPeriod.planet.symbol,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = getPlanetColor(subPeriod.planet)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            subPeriod.planet.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppTheme.TextPrimary
                                        )
                                        if (isCurrentSub) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(AppTheme.SuccessColor, CircleShape)
                                            )
                                        }
                                    }
                                    Text(
                                        "${subPeriod.startDate.format(dateFormatter)} - ${subPeriod.endDate.format(dateFormatter)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.TextMuted
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
private fun HousesTab(result: ExtendedVarshaphalaResult) {
    var expandedHouse by remember { mutableStateOf<Int?>(null) }
    var sortBy by remember { mutableStateOf("Number") }

    val sortedHouses = when (sortBy) {
        "Rating" -> result.housePredictions.sortedByDescending { it.rating }
        "Strength" -> result.housePredictions.sortedByDescending {
            when (it.strength) {
                "Excellent" -> 5
                "Strong" -> 4
                "Moderate" -> 3
                "Weak" -> 2
                else -> 1
            }
        }
        else -> result.housePredictions.sortedBy { it.house }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            HouseOverviewCard(result.housePredictions)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "House Predictions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )

                Row {
                    listOf("Number", "Rating", "Strength").forEach { option ->
                        TextButton(
                            onClick = { sortBy = option },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (sortBy == option) AppTheme.AccentPrimary else AppTheme.TextMuted
                            )
                        ) {
                            Text(
                                option,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (sortBy == option) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        items(sortedHouses) { prediction ->
            HousePredictionCard(
                prediction = prediction,
                isExpanded = expandedHouse == prediction.house,
                onExpand = {
                    expandedHouse = if (expandedHouse == prediction.house) null else prediction.house
                }
            )
        }
    }
}

@Composable
private fun HouseOverviewCard(predictions: List<HousePrediction>) {
    val strongHouses = predictions.filter { it.strength in listOf("Excellent", "Strong") }
    val weakHouses = predictions.filter { it.strength in listOf("Weak", "Challenged") }
    val averageRating = predictions.map { it.rating }.average()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Home,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Annual House Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Varshaphala house-wise predictions",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        String.format("%.1f", averageRating),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.AccentPrimary
                    )
                    Text(
                        "Avg Rating",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${strongHouses.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.SuccessColor
                    )
                    Text(
                        "Strong",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${weakHouses.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.WarningColor
                    )
                    Text(
                        "Weak",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                predictions.sortedBy { it.house }.forEach { prediction ->
                    val barColor = when {
                        prediction.rating >= 4.0f -> AppTheme.SuccessColor
                        prediction.rating >= 3.0f -> AppTheme.AccentGold
                        prediction.rating >= 2.0f -> AppTheme.WarningColor
                        else -> AppTheme.ErrorColor
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height((prediction.rating * 12).dp)
                                .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${prediction.house}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HousePredictionCard(
    prediction: HousePrediction,
    isExpanded: Boolean,
    onExpand: () -> Unit
) {
    val houseSignificance = getHouseSignificance(prediction.house)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onExpand),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.AccentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${prediction.house}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.AccentPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "House ${prediction.house}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "${prediction.signOnCusp.displayName} • ${houseSignificance}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = AppTheme.AccentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                String.format("%.1f", prediction.rating),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.TextPrimary
                            )
                        }
                        StrengthBadge(prediction.strength)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = AppTheme.TextMuted
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AppTheme.DividerColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(prediction.keywords) { keyword ->
                            Surface(
                                color = AppTheme.ChipBackground,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    keyword,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "House Lord",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    prediction.houseLord.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getPlanetColor(prediction.houseLord)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${prediction.houseLord.displayName} in House ${prediction.lordPosition}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.TextSecondary
                                )
                            }
                        }

                        if (prediction.planetsInHouse.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Planets in House",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.TextMuted
                                )
                                Row {
                                    prediction.planetsInHouse.forEach { planet ->
                                        Text(
                                            planet.symbol,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = getPlanetColor(planet),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        prediction.prediction,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (prediction.specificEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Specific Indications",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        prediction.specificEvents.forEach { event ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Filled.FiberManualRecord,
                                    contentDescription = null,
                                    tint = AppTheme.AccentPrimary,
                                    modifier = Modifier.size(8.dp).padding(top = 6.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    event,
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
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.Cake,
                contentDescription = null,
                tint = AppTheme.TextSubtle,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No Birth Chart Selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please select a birth chart to view\nannual Varshaphala predictions",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getHouseSignificance(house: Int): String {
    return when (house) {
        1 -> "Self, Personality"
        2 -> "Wealth, Family"
        3 -> "Siblings, Courage"
        4 -> "Home, Mother"
        5 -> "Children, Intelligence"
        6 -> "Enemies, Health"
        7 -> "Marriage, Partnership"
        8 -> "Longevity, Transformation"
        9 -> "Fortune, Father"
        10 -> "Career, Status"
        11 -> "Gains, Friends"
        12 -> "Losses, Liberation"
        else -> ""
    }
}

private fun getPlanetColor(planet: Planet): Color {
    return when (planet) {
        Planet.SUN -> AppTheme.PlanetSun
        Planet.MOON -> AppTheme.PlanetMoon
        Planet.MARS -> AppTheme.PlanetMars
        Planet.MERCURY -> AppTheme.PlanetMercury
        Planet.JUPITER -> AppTheme.PlanetJupiter
        Planet.VENUS -> AppTheme.PlanetVenus
        Planet.SATURN -> AppTheme.PlanetSaturn
        Planet.RAHU -> AppTheme.PlanetRahu
        Planet.KETU -> AppTheme.PlanetKetu
        else -> AppTheme.AccentPrimary
    }
}

private fun getStrengthColor(strength: String): Color {
    return when (strength.lowercase()) {
        "excellent", "very strong" -> Color(0xFF2E7D32)
        "strong" -> Color(0xFF4CAF50)
        "moderate", "medium" -> Color(0xFFFFC107)
        "weak" -> Color(0xFFFF9800)
        "very weak", "challenged", "debilitated" -> Color(0xFFF44336)
        else -> AppTheme.TextMuted
    }
}

private fun getMonthName(month: Int): String {
    return java.time.Month.of(month).name.lowercase().replaceFirstChar { it.uppercase() }
}

private fun getZodiacSymbol(sign: ZodiacSign): String {
    return when (sign) {
        ZodiacSign.ARIES -> "♈"
        ZodiacSign.TAURUS -> "♉"
        ZodiacSign.GEMINI -> "♊"
        ZodiacSign.CANCER -> "♋"
        ZodiacSign.LEO -> "♌"
        ZodiacSign.VIRGO -> "♍"
        ZodiacSign.LIBRA -> "♎"
        ZodiacSign.SCORPIO -> "♏"
        ZodiacSign.SAGITTARIUS -> "♐"
        ZodiacSign.CAPRICORN -> "♑"
        ZodiacSign.AQUARIUS -> "♒"
        ZodiacSign.PISCES -> "♓"
    }
}

private suspend fun calculateExtendedVarshaphala(
    chart: VedicChart,
    year: Int
): ExtendedVarshaphalaResult {
    val birthDateTime = chart.birthData.dateTime
    val birthYear = birthDateTime.year
    val age = year - birthYear

    val solarReturnTime = calculateSolarReturnTime(chart, year)
    val solarReturnChart = calculateSolarReturnChart(chart, solarReturnTime)

    val yearLord = calculateYearLord(chart, year)
    val yearLordHouse = getHousePosition(yearLord, solarReturnChart)
    val yearLordStrength = calculatePlanetStrength(yearLord, solarReturnChart)
    val yearLordDignity = getYearLordDignityDescription(yearLord, solarReturnChart)

    val muntha = calculateMuntha(chart, year, solarReturnChart)
    val muddaDasha = calculateMuddaDasha(solarReturnChart, solarReturnTime.toLocalDate())
    val tajikaAspects = calculateTajikaAspects(solarReturnChart)
    val sahams = calculateSahams(solarReturnChart, solarReturnTime)
    val panchaVargiyaBala = calculatePanchaVargiyaBala(solarReturnChart)
    val triPatakiChakra = calculateTriPatakiChakra(solarReturnChart)
    val housePredictions = calculateHousePredictions(solarReturnChart, muntha, yearLord)
    val majorThemes = generateMajorThemes(solarReturnChart, yearLord, muntha, tajikaAspects)
    val (favorableMonths, challengingMonths) = calculateMonthlyInfluences(solarReturnChart, solarReturnTime)
    val keyDates = calculateKeyDates(solarReturnChart, solarReturnTime, muddaDasha)
    val overallPrediction = generateOverallPrediction(solarReturnChart, yearLord, muntha, tajikaAspects, housePredictions)
    val yearRating = calculateYearRating(solarReturnChart, yearLord, muntha, tajikaAspects, housePredictions)

    return ExtendedVarshaphalaResult(
        year = year,
        age = age,
        solarReturnChart = solarReturnChart,
        yearLord = yearLord,
        yearLordStrength = yearLordStrength,
        yearLordHouse = yearLordHouse,
        yearLordDignity = yearLordDignity,
        muntha = muntha,
        muddaDasha = muddaDasha,
        tajikaAspects = tajikaAspects,
        sahams = sahams,
        panchaVargiyaBala = panchaVargiyaBala,
        triPatakiChakra = triPatakiChakra,
        housePredictions = housePredictions,
        majorThemes = majorThemes,
        favorableMonths = favorableMonths,
        challengingMonths = challengingMonths,
        overallPrediction = overallPrediction,
        yearRating = yearRating,
        keyDates = keyDates
    )
}

private fun calculateSolarReturnTime(chart: VedicChart, year: Int): LocalDateTime {
    val natalSunLongitude = chart.planetPositions.find { it.planet == Planet.SUN }?.longitude ?: 0.0
    val birthDate = chart.birthData.dateTime

    var searchDate = LocalDateTime.of(year, birthDate.monthValue, birthDate.dayOfMonth, 12, 0)

    repeat(10) {
        val currentSunLong = calculateSunLongitude(searchDate)
        val diff = normalizeAngle(natalSunLongitude - currentSunLong)

        if (abs(diff) < 0.0001) return searchDate

        val adjustment = diff / 0.9856
        searchDate = searchDate.plusMinutes((adjustment * 24 * 60).toLong())
    }

    return searchDate
}

private fun calculateSunLongitude(dateTime: LocalDateTime): Double {
    val jd = calculateJulianDay(dateTime)
    val t = (jd - 2451545.0) / 36525.0

    val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
    val m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
    val e = 0.016708634 - 0.000042037 * t - 0.0000001267 * t * t

    val mRad = Math.toRadians(m)
    val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2 * mRad) +
            0.000289 * sin(3 * mRad)

    val sunLong = l0 + c
    val omega = 125.04 - 1934.136 * t
    val lambda = sunLong - 0.00569 - 0.00478 * sin(Math.toRadians(omega))

    val ayanamsa = calculateAyanamsa(jd)
    return normalizeAngle(lambda - ayanamsa)
}

private fun calculateJulianDay(dateTime: LocalDateTime): Double {
    var y = dateTime.year
    var m = dateTime.monthValue
    val d = dateTime.dayOfMonth + dateTime.hour / 24.0 + dateTime.minute / 1440.0 + dateTime.second / 86400.0

    if (m <= 2) {
        y -= 1
        m += 12
    }

    val a = y / 100
    val b = 2 - a + a / 4

    return (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + d + b - 1524.5
}

private fun calculateAyanamsa(jd: Double): Double {
    val t = (jd - 2451545.0) / 36525.0
    return 23.85 + 0.0137 * (jd - 2415020.0) / 365.25
}

private fun normalizeAngle(angle: Double): Double {
    var result = angle % 360.0
    if (result < 0) result += 360.0
    return result
}

private fun calculateSolarReturnChart(chart: VedicChart, solarReturnTime: LocalDateTime): SolarReturnChart {
    val sunLongitude = chart.planetPositions.find { it.planet == Planet.SUN }?.longitude ?: 0.0
    val ascendantDegree = calculateAscendant(solarReturnTime, chart.birthData.latitude, chart.birthData.longitude)
    val ascendant = ZodiacSign.fromLongitude(ascendantDegree)

    val planetPositions = mutableMapOf<Planet, PlanetPosition>()

    listOf(Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.RAHU, Planet.KETU).forEach { planet ->
        val longitude = calculatePlanetLongitude(planet, solarReturnTime)
        val sign = ZodiacSign.fromLongitude(longitude)
        val house = calculateHouse(longitude, ascendantDegree)
        val degree = longitude % 30.0
        val nakshatra = calculateNakshatra(longitude)
        val nakshatraPada = calculateNakshatraPada(longitude)
        val isRetrograde = isPlanetRetrograde(planet, solarReturnTime)
        val speed = calculatePlanetSpeed(planet, solarReturnTime)

        planetPositions[planet] = PlanetPosition(
            longitude = longitude,
            sign = sign,
            house = house,
            degree = degree,
            nakshatra = nakshatra,
            nakshatraPada = nakshatraPada,
            isRetrograde = isRetrograde,
            speed = speed
        )
    }

    val moonLongitude = planetPositions[Planet.MOON]?.longitude ?: 0.0
    val moonSign = ZodiacSign.fromLongitude(moonLongitude)
    val moonNakshatra = calculateNakshatra(moonLongitude)

    return SolarReturnChart(
        solarReturnTime = solarReturnTime,
        sunLongitude = sunLongitude,
        ascendant = ascendant,
        ascendantDegree = ascendantDegree % 30.0,
        moonSign = moonSign,
        moonNakshatra = moonNakshatra,
        planetPositions = planetPositions
    )
}

private fun calculateAscendant(dateTime: LocalDateTime, latitude: Double, longitude: Double): Double {
    val jd = calculateJulianDay(dateTime)
    val t = (jd - 2451545.0) / 36525.0

    val gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * t * t
    val lst = normalizeAngle(gmst + longitude)

    val obliquity = 23.439291 - 0.0130042 * t
    val obliquityRad = Math.toRadians(obliquity)
    val latRad = Math.toRadians(latitude)
    val lstRad = Math.toRadians(lst)

    val tanAsc = cos(lstRad) / (-sin(lstRad) * cos(obliquityRad) - kotlin.math.tan(latRad) * sin(obliquityRad))
    var ascendant = Math.toDegrees(kotlin.math.atan(tanAsc))

    if (cos(lstRad) < 0) ascendant += 180.0
    if (ascendant < 0) ascendant += 360.0

    val ayanamsa = calculateAyanamsa(jd)
    return normalizeAngle(ascendant - ayanamsa)
}

private fun calculatePlanetLongitude(planet: Planet, dateTime: LocalDateTime): Double {
    val jd = calculateJulianDay(dateTime)
    val t = (jd - 2451545.0) / 36525.0
    val ayanamsa = calculateAyanamsa(jd)

    val tropicalLong = when (planet) {
        Planet.SUN -> calculateSunTropical(t)
        Planet.MOON -> calculateMoonTropical(t)
        Planet.MARS -> calculateMarsTropical(t)
        Planet.MERCURY -> calculateMercuryTropical(t)
        Planet.JUPITER -> calculateJupiterTropical(t)
        Planet.VENUS -> calculateVenusTropical(t)
        Planet.SATURN -> calculateSaturnTropical(t)
        Planet.RAHU -> calculateRahuTropical(t)
        Planet.KETU -> normalizeAngle(calculateRahuTropical(t) + 180.0)
        else -> 0.0
    }

    return normalizeAngle(tropicalLong - ayanamsa)
}

private fun calculateSunTropical(t: Double): Double {
    val l0 = 280.46646 + 36000.76983 * t
    val m = 357.52911 + 35999.05029 * t
    val mRad = Math.toRadians(m)
    val c = (1.914602 - 0.004817 * t) * sin(mRad) + 0.019993 * sin(2 * mRad)
    return normalizeAngle(l0 + c)
}

private fun calculateMoonTropical(t: Double): Double {
    val l = 218.3164477 + 481267.88123421 * t
    val d = 297.8501921 + 445267.1114034 * t
    val m = 357.5291092 + 35999.0502909 * t
    val mp = 134.9633964 + 477198.8675055 * t
    val f = 93.272095 + 483202.0175233 * t

    val dRad = Math.toRadians(d)
    val mRad = Math.toRadians(m)
    val mpRad = Math.toRadians(mp)
    val fRad = Math.toRadians(f)

    val longitude = l +
            6.288774 * sin(mpRad) +
            1.274027 * sin(2 * dRad - mpRad) +
            0.658314 * sin(2 * dRad) +
            0.213618 * sin(2 * mpRad) -
            0.185116 * sin(mRad)

    return normalizeAngle(longitude)
}

private fun calculateMarsTropical(t: Double): Double {
    val l = 355.433275 + 19141.6964746 * t
    val m = 19.373 + 19140.0 * t
    val mRad = Math.toRadians(m)
    val c = 10.691 * sin(mRad) + 0.623 * sin(2 * mRad)
    return normalizeAngle(l + c)
}

private fun calculateMercuryTropical(t: Double): Double {
    val l = 252.250906 + 149474.0722491 * t
    val m = 174.7948 + 149472.5153 * t
    val mRad = Math.toRadians(m)
    val c = 23.4400 * sin(mRad) + 2.9818 * sin(2 * mRad)
    return normalizeAngle(l + c)
}

private fun calculateJupiterTropical(t: Double): Double {
    val l = 34.351484 + 3036.3027889 * t
    val m = 20.020 + 3034.9057 * t
    val mRad = Math.toRadians(m)
    val c = 5.555 * sin(mRad) + 0.168 * sin(2 * mRad)
    return normalizeAngle(l + c)
}

private fun calculateVenusTropical(t: Double): Double {
    val l = 181.979801 + 58519.2130302 * t
    val m = 50.4161 + 58517.8039 * t
    val mRad = Math.toRadians(m)
    val c = 0.7758 * sin(mRad) + 0.0033 * sin(2 * mRad)
    return normalizeAngle(l + c)
}

private fun calculateSaturnTropical(t: Double): Double {
    val l = 50.077471 + 1223.5110141 * t
    val m = 317.020 + 1222.1138 * t
    val mRad = Math.toRadians(m)
    val c = 6.406 * sin(mRad) + 0.257 * sin(2 * mRad)
    return normalizeAngle(l + c)
}

private fun calculateRahuTropical(t: Double): Double {
    val omega = 125.04452 - 1934.136261 * t
    return normalizeAngle(omega)
}

private fun calculateHouse(planetLongitude: Double, ascendantLongitude: Double): Int {
    val diff = normalizeAngle(planetLongitude - ascendantLongitude + 30.0)
    return ((diff / 30.0).toInt() % 12) + 1
}

private fun calculateNakshatra(longitude: Double): String {
    val nakshatras = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )
    val index = (longitude / 13.333333).toInt() % 27
    return nakshatras[index]
}

private fun calculateNakshatraPada(longitude: Double): Int {
    val nakshatraPosition = longitude % 13.333333
    return ((nakshatraPosition / 3.333333).toInt() % 4) + 1
}

private fun isPlanetRetrograde(planet: Planet, dateTime: LocalDateTime): Boolean {
    if (planet == Planet.SUN || planet == Planet.MOON || planet == Planet.RAHU || planet == Planet.KETU) {
        return planet == Planet.RAHU || planet == Planet.KETU
    }

    val speed = calculatePlanetSpeed(planet, dateTime)
    return speed < 0
}

private fun calculatePlanetSpeed(planet: Planet, dateTime: LocalDateTime): Double {
    val dt1 = dateTime.minusHours(12)
    val dt2 = dateTime.plusHours(12)

    val long1 = calculatePlanetLongitude(planet, dt1)
    val long2 = calculatePlanetLongitude(planet, dt2)

    var diff = long2 - long1
    if (diff > 180) diff -= 360
    if (diff < -180) diff += 360

    return diff
}

private fun calculateYearLord(chart: VedicChart, year: Int): Planet {
    val birthYear = chart.birthData.dateTime.year
    val yearsElapsed = year - birthYear

    val weekdayOrder = listOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    val birthDayOfWeek = chart.birthData.dateTime.dayOfWeek.value % 7
    val yearLordIndex = (birthDayOfWeek + yearsElapsed) % 7

    return weekdayOrder[yearLordIndex]
}

private fun getHousePosition(planet: Planet, chart: SolarReturnChart): Int {
    return chart.planetPositions[planet]?.house ?: 1
}

private fun calculatePlanetStrength(planet: Planet, chart: SolarReturnChart): String {
    val position = chart.planetPositions[planet] ?: return "Unknown"
    val sign = position.sign

    val exaltationSigns = mapOf(
        Planet.SUN to ZodiacSign.ARIES,
        Planet.MOON to ZodiacSign.TAURUS,
        Planet.MARS to ZodiacSign.CAPRICORN,
        Planet.MERCURY to ZodiacSign.VIRGO,
        Planet.JUPITER to ZodiacSign.CANCER,
        Planet.VENUS to ZodiacSign.PISCES,
        Planet.SATURN to ZodiacSign.LIBRA
    )

    val debilitationSigns = mapOf(
        Planet.SUN to ZodiacSign.LIBRA,
        Planet.MOON to ZodiacSign.SCORPIO,
        Planet.MARS to ZodiacSign.CANCER,
        Planet.MERCURY to ZodiacSign.PISCES,
        Planet.JUPITER to ZodiacSign.CAPRICORN,
        Planet.VENUS to ZodiacSign.VIRGO,
        Planet.SATURN to ZodiacSign.ARIES
    )

    val ownSigns = mapOf(
        Planet.SUN to listOf(ZodiacSign.LEO),
        Planet.MOON to listOf(ZodiacSign.CANCER),
        Planet.MARS to listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO),
        Planet.MERCURY to listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO),
        Planet.JUPITER to listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES),
        Planet.VENUS to listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA),
        Planet.SATURN to listOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS)
    )

    return when {
        exaltationSigns[planet] == sign -> "Exalted"
        debilitationSigns[planet] == sign -> "Debilitated"
        ownSigns[planet]?.contains(sign) == true -> "Strong"
        position.house in listOf(1, 4, 7, 10) -> "Angular"
        position.isRetrograde -> "Retrograde"
        else -> "Moderate"
    }
}

private fun getYearLordDignityDescription(yearLord: Planet, chart: SolarReturnChart): String {
    val position = chart.planetPositions[yearLord] ?: return "Year lord position unknown."
    val strength = calculatePlanetStrength(yearLord, chart)
    val house = position.house
    val sign = position.sign

    val houseDescription = when (house) {
        1 -> "placed in the ascendant, giving prominence and personal focus"
        2 -> "in the house of wealth, emphasizing financial matters and family"
        3 -> "in the house of courage, bringing initiative and communication"
        4 -> "in the house of home, highlighting domestic life and property"
        5 -> "in the house of intelligence, favoring creativity and children"
        6 -> "in the house of challenges, requiring effort to overcome obstacles"
        7 -> "in the house of partnership, emphasizing relationships and alliances"
        8 -> "in the house of transformation, bringing deep changes and research"
        9 -> "in the house of fortune, bestowing luck and higher learning"
        10 -> "in the house of career, focusing on professional achievements"
        11 -> "in the house of gains, promising fulfillment of desires"
        12 -> "in the house of expenses, indicating spiritual growth and foreign connections"
        else -> "in a significant position"
    }

    val strengthDescription = when (strength) {
        "Exalted" -> "The year lord is exalted, indicating excellent potential for success and achievement."
        "Debilitated" -> "The year lord is debilitated, suggesting challenges that require careful navigation."
        "Strong" -> "The year lord is in its own sign, providing stability and self-reliance."
        "Angular" -> "The year lord in an angular position gives prominence to its significations."
        "Retrograde" -> "The retrograde year lord may bring revisiting of past matters and introspection."
        else -> "The year lord is in a moderate position, giving balanced results."
    }

    return "The year lord ${yearLord.displayName} is $houseDescription in ${sign.displayName}. $strengthDescription"
}

private fun calculateMuntha(chart: VedicChart, year: Int, solarReturnChart: SolarReturnChart): MunthaResult {
    val birthYear = chart.birthData.dateTime.year
    val yearsElapsed = year - birthYear

    val natalAscendantIndex = ZodiacSign.entries.indexOf(ZodiacSign.fromLongitude(chart.ascendant))
    val munthaSignIndex = (natalAscendantIndex + yearsElapsed) % 12
    val munthaSign = ZodiacSign.entries[munthaSignIndex]

    val ascendantLongitude = solarReturnChart.ascendant.ordinal * 30.0
    val munthaLongitude = munthaSignIndex * 30.0 + 15.0

    val munthaHouse = calculateHouse(munthaLongitude, ascendantLongitude)

    val lord = getSignLord(munthaSign)
    val lordHouse = getHousePosition(lord, solarReturnChart)
    val lordStrength = calculatePlanetStrength(lord, solarReturnChart)

    val themes = getMunthaThemes(munthaHouse, lord, lordHouse)
    val interpretation = generateMunthaInterpretation(munthaSign, munthaHouse, lord, lordHouse, lordStrength)

    return MunthaResult(
        sign = munthaSign,
        house = munthaHouse,
        degree = 15.0,
        lord = lord,
        lordHouse = lordHouse,
        lordStrength = lordStrength,
        interpretation = interpretation,
        themes = themes
    )
}

private fun getSignLord(sign: ZodiacSign): Planet {
    return when (sign) {
        ZodiacSign.ARIES, ZodiacSign.SCORPIO -> Planet.MARS
        ZodiacSign.TAURUS, ZodiacSign.LIBRA -> Planet.VENUS
        ZodiacSign.GEMINI, ZodiacSign.VIRGO -> Planet.MERCURY
        ZodiacSign.CANCER -> Planet.MOON
        ZodiacSign.LEO -> Planet.SUN
        ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES -> Planet.JUPITER
        ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS -> Planet.SATURN
    }
}

private fun getMunthaThemes(house: Int, lord: Planet, lordHouse: Int): List<String> {
    val houseThemes = when (house) {
        1 -> listOf("Personal Growth", "New Beginnings", "Health Focus")
        2 -> listOf("Financial Gains", "Family Matters", "Speech")
        3 -> listOf("Communication", "Short Travels", "Siblings")
        4 -> listOf("Home Affairs", "Property", "Inner Peace")
        5 -> listOf("Creativity", "Romance", "Children")
        6 -> listOf("Service", "Health Issues", "Competition")
        7 -> listOf("Partnerships", "Marriage", "Business")
        8 -> listOf("Transformation", "Research", "Inheritance")
        9 -> listOf("Fortune", "Long Travel", "Higher Learning")
        10 -> listOf("Career Advancement", "Recognition", "Authority")
        11 -> listOf("Gains", "Friends", "Fulfilled Wishes")
        12 -> listOf("Spirituality", "Foreign Lands", "Expenses")
        else -> listOf("General Growth")
    }

    return houseThemes
}

private fun generateMunthaInterpretation(
    sign: ZodiacSign,
    house: Int,
    lord: Planet,
    lordHouse: Int,
    lordStrength: String
): String {
    val houseSignificance = when (house) {
        1 -> "personal development and health"
        2 -> "financial stability and family relationships"
        3 -> "communication, courage, and siblings"
        4 -> "home environment, property, and emotional well-being"
        5 -> "creativity, children, and romantic pursuits"
        6 -> "overcoming obstacles, health management, and service"
        7 -> "partnerships, marriage, and public dealings"
        8 -> "transformation, joint resources, and deep research"
        9 -> "fortune, higher learning, and long-distance travel"
        10 -> "career advancement and public recognition"
        11 -> "fulfillment of desires and gains from various sources"
        12 -> "spiritual growth, foreign connections, and letting go"
        else -> "general life progress"
    }

    val lordQuality = when (lordStrength) {
        "Exalted", "Strong" -> "excellent"
        "Moderate", "Angular" -> "favorable"
        "Debilitated" -> "challenging but growth-oriented"
        else -> "variable"
    }

    return "Muntha in ${sign.displayName} in the ${house}${getOrdinalSuffix(house)} house focuses the year's energy on $houseSignificance. " +
            "The Muntha lord ${lord.displayName} in house $lordHouse provides $lordQuality support for these matters. " +
            "This placement suggests that attention to ${houseSignificance.split(" and ").first()} will be particularly rewarding this year."
}

private fun getOrdinalSuffix(n: Int): String {
    return when {
        n in 11..13 -> "th"
        n % 10 == 1 -> "st"
        n % 10 == 2 -> "nd"
        n % 10 == 3 -> "rd"
        else -> "th"
    }
}

private fun calculateMuddaDasha(chart: SolarReturnChart, startDate: LocalDate): List<MuddaDashaPeriod> {
    val dashaOrder = listOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.RAHU, Planet.KETU
    )

    val dashaDays = mapOf(
        Planet.SUN to 110,
        Planet.MOON to 60,
        Planet.MARS to 32,
        Planet.MERCURY to 40,
        Planet.JUPITER to 48,
        Planet.VENUS to 56,
        Planet.SATURN to 4,
        Planet.RAHU to 5,
        Planet.KETU to 5
    )

    val totalDays = 360
    val today = LocalDate.now()

    val nakshatraIndex = (chart.planetPositions[Planet.MOON]?.longitude?.div(13.333333))?.toInt() ?: 0
    val nakshatraLords = listOf(
        Planet.KETU, Planet.VENUS, Planet.SUN, Planet.MOON, Planet.MARS,
        Planet.RAHU, Planet.JUPITER, Planet.SATURN, Planet.MERCURY
    )
    val startingLordIndex = nakshatraIndex % 9
    val startingLord = nakshatraLords[startingLordIndex]
    val startIndex = dashaOrder.indexOf(startingLord)

    val periods = mutableListOf<MuddaDashaPeriod>()
    var currentDate = startDate

    for (i in dashaOrder.indices) {
        val planetIndex = (startIndex + i) % dashaOrder.size
        val planet = dashaOrder[planetIndex]
        val days = (dashaDays[planet] ?: 30) * totalDays / 360

        val endDate = currentDate.plusDays(days.toLong() - 1)
        val isCurrent = today >= currentDate && today <= endDate

        val progressPercent = if (isCurrent) {
            val daysPassed = ChronoUnit.DAYS.between(currentDate, today).toFloat()
            (daysPassed / days).coerceIn(0f, 1f)
        } else if (today > endDate) {
            1f
        } else {
            0f
        }

        val subPeriods = calculateMuddaAntardasha(planet, currentDate, endDate, dashaOrder)

        val planetStrength = calculatePlanetStrength(planet, chart)
        val houseRuled = getHousesRuledBy(planet, chart)
        val prediction = generateDashaPrediction(planet, chart, planetStrength)
        val keywords = getDashaKeywords(planet, chart)

        periods.add(
            MuddaDashaPeriod(
                planet = planet,
                startDate = currentDate,
                endDate = endDate,
                days = days,
                subPeriods = subPeriods,
                planetStrength = planetStrength,
                houseRuled = houseRuled,
                prediction = prediction,
                keywords = keywords,
                isCurrent = isCurrent,
                progressPercent = progressPercent
            )
        )

        currentDate = endDate.plusDays(1)
    }

    return periods
}

private fun calculateMuddaAntardasha(
    mainPlanet: Planet,
    startDate: LocalDate,
    endDate: LocalDate,
    dashaOrder: List<Planet>
): List<MuddaAntardasha> {
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt()
    val subPeriods = mutableListOf<MuddaAntardasha>()

    val startIndex = dashaOrder.indexOf(mainPlanet)
    var currentDate = startDate
    val subDays = totalDays / 9

    for (i in dashaOrder.indices) {
        val planetIndex = (startIndex + i) % dashaOrder.size
        val planet = dashaOrder[planetIndex]

        val actualSubDays = if (i == dashaOrder.size - 1) {
            ChronoUnit.DAYS.between(currentDate, endDate).toInt() + 1
        } else {
            subDays
        }

        val subEndDate = currentDate.plusDays(actualSubDays.toLong() - 1)

        subPeriods.add(
            MuddaAntardasha(
                planet = planet,
                startDate = currentDate,
                endDate = subEndDate,
                days = actualSubDays,
                interpretation = "${mainPlanet.displayName}-${planet.displayName} period"
            )
        )

        currentDate = subEndDate.plusDays(1)
        if (currentDate > endDate) break
    }

    return subPeriods
}

private fun getHousesRuledBy(planet: Planet, chart: SolarReturnChart): List<Int> {
    val houses = mutableListOf<Int>()
    val ascendantIndex = chart.ascendant.ordinal

    for (i in 0..11) {
        val signIndex = (ascendantIndex + i) % 12
        val sign = ZodiacSign.entries[signIndex]
        if (getSignLord(sign) == planet) {
            houses.add(i + 1)
        }
    }

    return houses
}

private fun generateDashaPrediction(planet: Planet, chart: SolarReturnChart, strength: String): String {
    val position = chart.planetPositions[planet]
    val house = position?.house ?: 1

    val planetNature = when (planet) {
        Planet.SUN -> "vitality, authority, and self-expression"
        Planet.MOON -> "emotions, nurturing, and public connections"
        Planet.MARS -> "energy, initiative, and competitive drive"
        Planet.MERCURY -> "communication, learning, and business"
        Planet.JUPITER -> "wisdom, expansion, and good fortune"
        Planet.VENUS -> "relationships, creativity, and pleasures"
        Planet.SATURN -> "discipline, responsibility, and long-term goals"
        Planet.RAHU -> "ambition, innovation, and unconventional paths"
        Planet.KETU -> "spirituality, detachment, and past karma"
        else -> "general influences"
    }

    val houseArea = when (house) {
        1 -> "personal development"
        2 -> "financial matters"
        3 -> "communication and siblings"
        4 -> "home and property"
        5 -> "creativity and children"
        6 -> "health and service"
        7 -> "partnerships"
        8 -> "transformation"
        9 -> "fortune and learning"
        10 -> "career"
        11 -> "gains and friends"
        12 -> "spirituality"
        else -> "various life areas"
    }

    val strengthQuality = when (strength) {
        "Exalted" -> "This period promises exceptional results"
        "Strong" -> "This period is well-supported for success"
        "Debilitated" -> "This period requires extra effort and patience"
        else -> "This period brings mixed but manageable influences"
    }

    return "During this ${planet.displayName} period, focus shifts to $planetNature, particularly affecting $houseArea. $strengthQuality in the significations of ${planet.displayName}."
}

private fun getDashaKeywords(planet: Planet, chart: SolarReturnChart): List<String> {
    val position = chart.planetPositions[planet]
    val house = position?.house ?: 1

    val planetKeywords = when (planet) {
        Planet.SUN -> listOf("Leadership", "Vitality", "Father")
        Planet.MOON -> listOf("Emotions", "Mother", "Public")
        Planet.MARS -> listOf("Action", "Energy", "Courage")
        Planet.MERCURY -> listOf("Communication", "Learning", "Business")
        Planet.JUPITER -> listOf("Wisdom", "Growth", "Fortune")
        Planet.VENUS -> listOf("Love", "Art", "Comfort")
        Planet.SATURN -> listOf("Discipline", "Karma", "Delays")
        Planet.RAHU -> listOf("Ambition", "Innovation", "Foreign")
        Planet.KETU -> listOf("Spirituality", "Detachment", "Past")
        else -> listOf("General")
    }

    val houseKeywords = when (house) {
        1 -> listOf("Self", "Body")
        2 -> listOf("Wealth", "Speech")
        3 -> listOf("Siblings", "Courage")
        4 -> listOf("Home", "Peace")
        5 -> listOf("Children", "Romance")
        6 -> listOf("Health", "Service")
        7 -> listOf("Marriage", "Business")
        8 -> listOf("Transformation", "Research")
        9 -> listOf("Luck", "Travel")
        10 -> listOf("Career", "Status")
        11 -> listOf("Gains", "Friends")
        12 -> listOf("Spirituality", "Losses")
        else -> listOf()
    }

    return (planetKeywords + houseKeywords).take(5)
}

private fun calculateTajikaAspects(chart: SolarReturnChart): List<TajikaAspect> {
    val aspects = mutableListOf<TajikaAspect>()
    val planets = listOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    val aspectAngles = listOf(0, 60, 90, 120, 180)

    for (i in planets.indices) {
        for (j in (i + 1) until planets.size) {
            val planet1 = planets[i]
            val planet2 = planets[j]

            val pos1 = chart.planetPositions[planet1] ?: continue
            val pos2 = chart.planetPositions[planet2] ?: continue

            val diff = abs(normalizeAngle(pos1.longitude - pos2.longitude))

            for (angle in aspectAngles) {
                val orb = when (angle) {
                    0 -> 8.0
                    60, 120 -> 6.0
                    90, 180 -> 7.0
                    else -> 5.0
                }

                val actualOrb = abs(diff - angle)
                val reverseOrb = abs(diff - (360 - angle))
                val effectiveOrb = min(actualOrb, reverseOrb)

                if (effectiveOrb <= orb) {
                    val speed1 = pos1.speed
                    val speed2 = pos2.speed
                    val isApplying = determineTajikaApplication(pos1.longitude, pos2.longitude, speed1, speed2)

                    val aspectType = determineTajikaAspectType(
                        planet1, planet2, pos1, pos2,
                        isApplying, effectiveOrb, angle, chart
                    )

                    val strength = calculateAspectStrength(effectiveOrb, orb, angle, isApplying)
                    val relatedHouses = listOf(pos1.house, pos2.house).distinct()
                    val effectDescription = getAspectEffectDescription(aspectType, planet1, planet2)
                    val prediction = generateAspectPrediction(aspectType, planet1, planet2, relatedHouses)

                    aspects.add(
                        TajikaAspect(
                            type = aspectType,
                            planet1 = planet1,
                            planet2 = planet2,
                            planet1Longitude = pos1.longitude,
                            planet2Longitude = pos2.longitude,
                            orb = effectiveOrb,
                            aspectAngle = angle,
                            isApplying = isApplying,
                            effectDescription = effectDescription,
                            strength = strength,
                            relatedHouses = relatedHouses,
                            prediction = prediction
                        )
                    )
                }
            }
        }
    }

    return aspects.sortedByDescending { it.strength.weight }
}

private fun determineTajikaApplication(
    long1: Double, long2: Double,
    speed1: Double, speed2: Double
): Boolean {
    val diff = normalizeAngle(long2 - long1)
    return if (diff < 180) {
        speed1 > speed2
    } else {
        speed2 > speed1
    }
}

private fun determineTajikaAspectType(
    planet1: Planet, planet2: Planet,
    pos1: PlanetPosition, pos2: PlanetPosition,
    isApplying: Boolean, orb: Double, angle: Int,
    chart: SolarReturnChart
): TajikaAspectType {
    val isAngular1 = pos1.house in listOf(1, 4, 7, 10)
    val isAngular2 = pos2.house in listOf(1, 4, 7, 10)

    val hasReception = checkMutualReception(planet1, planet2, pos1.sign, pos2.sign)

    return when {
        isApplying && angle == 0 && orb < 3 -> {
            if (isAngular1 || isAngular2) TajikaAspectType.KAMBOOLA
            else TajikaAspectType.ITHASALA
        }
        isApplying && orb < 5 -> {
            if (hasReception) TajikaAspectType.NAKTA
            else TajikaAspectType.ITHASALA
        }
        !isApplying && orb < 5 -> TajikaAspectType.EASARAPHA
        pos1.isRetrograde || pos2.isRetrograde -> TajikaAspectType.RADDA
        pos1.speed < pos2.speed && isApplying -> TajikaAspectType.MANAU
        isApplying && hasReception -> TajikaAspectType.MUTHASHILA
        angle == 90 || angle == 180 -> TajikaAspectType.DURAPHA
        isAngular1 && isAngular2 && !isApplying -> TajikaAspectType.GAIRI_KAMBOOLA
        else -> if (isApplying) TajikaAspectType.ITHASALA else TajikaAspectType.EASARAPHA
    }
}

private fun checkMutualReception(planet1: Planet, planet2: Planet, sign1: ZodiacSign, sign2: ZodiacSign): Boolean {
    val lord1 = getSignLord(sign1)
    val lord2 = getSignLord(sign2)
    return (lord1 == planet2 && lord2 == planet1)
}

private fun calculateAspectStrength(orb: Double, maxOrb: Double, angle: Int, isApplying: Boolean): AspectStrength {
    val orbRatio = orb / maxOrb
    val angleBonus = when (angle) {
        0, 120 -> 0.2
        60 -> 0.1
        90, 180 -> -0.1
        else -> 0.0
    }
    val applyingBonus = if (isApplying) 0.1 else 0.0

    val strength = 1.0 - orbRatio + angleBonus + applyingBonus

    return when {
        strength >= 0.9 -> AspectStrength.VERY_STRONG
        strength >= 0.7 -> AspectStrength.STRONG
        strength >= 0.5 -> AspectStrength.MODERATE
        strength >= 0.3 -> AspectStrength.WEAK
        else -> AspectStrength.VERY_WEAK
    }
}

private fun getAspectEffectDescription(type: TajikaAspectType, planet1: Planet, planet2: Planet): String {
    return when (type) {
        TajikaAspectType.ITHASALA -> "${planet1.displayName} applying to ${planet2.displayName} promises fulfillment of matters related to both planets"
        TajikaAspectType.EASARAPHA -> "The separating aspect suggests matters related to these planets are concluding or have already manifested"
        TajikaAspectType.NAKTA -> "Light transmission with reception creates a favorable connection through an intermediary"
        TajikaAspectType.YAMAYA -> "Translation of light brings matters together through a third-party influence"
        TajikaAspectType.MANAU -> "Reverse application suggests outcomes through persistent effort"
        TajikaAspectType.KAMBOOLA -> "Angular conjunction creates powerful and prominent results"
        TajikaAspectType.GAIRI_KAMBOOLA -> "Modified Kamboola gives moderately strong angular influences"
        TajikaAspectType.KHALASARA -> "Application is prevented, indicating obstacles to desired outcomes"
        TajikaAspectType.RADDA -> "Retrograde motion interrupts the aspect, causing delays or reversals"
        TajikaAspectType.DUHPHALI_KUTTHA -> "Malefic intervention disrupts the yoga's positive effects"
        TajikaAspectType.TAMBIRA -> "Indirect connection through intermediary brings gradual results"
        TajikaAspectType.KUTTHA -> "Impediment to completion suggests partial or delayed outcomes"
        TajikaAspectType.DURAPHA -> "Hard aspect creates challenges that strengthen through difficulty"
        TajikaAspectType.MUTHASHILA -> "Mutual application ensures both parties actively contribute to outcomes"
        TajikaAspectType.IKKABALA -> "Unity of strength between planets enhances both their significations"
    }
}

private fun generateAspectPrediction(type: TajikaAspectType, planet1: Planet, planet2: Planet, houses: List<Int>): String {
    val houseStr = houses.joinToString(" and ") { "House $it" }

    return when (type) {
        TajikaAspectType.ITHASALA -> "The ${planet1.displayName}-${planet2.displayName} Ithasala yoga is highly favorable for matters of $houseStr. Expect positive developments and achievement of goals in these areas during the year."
        TajikaAspectType.EASARAPHA -> "The Easarapha between ${planet1.displayName} and ${planet2.displayName} indicates that significant events related to $houseStr may have already occurred or are in their final stages. Focus on consolidation rather than new initiatives."
        TajikaAspectType.KAMBOOLA -> "The powerful Kamboola yoga between ${planet1.displayName} and ${planet2.displayName} promises prominent success and recognition in matters of $houseStr. This is one of the most auspicious configurations."
        TajikaAspectType.RADDA -> "The Radda yoga suggests some delays or need to revisit matters related to $houseStr. Patience and review of past approaches will be beneficial."
        TajikaAspectType.DURAPHA -> "The challenging Durapha aspect between ${planet1.displayName} and ${planet2.displayName} indicates obstacles in $houseStr matters that will ultimately strengthen your resolve and skills."
        else -> "The ${type.displayName} yoga between ${planet1.displayName} and ${planet2.displayName} influences matters of $houseStr with ${if (type.isPositive) "supportive" else "challenging"} energy throughout the year."
    }
}

private fun calculateSahams(chart: SolarReturnChart, solarReturnTime: LocalDateTime): List<Saham> {
    val sahams = mutableListOf<Saham>()
    val isDayBirth = solarReturnTime.hour in 6..18

    val sunLong = chart.planetPositions[Planet.SUN]?.longitude ?: 0.0
    val moonLong = chart.planetPositions[Planet.MOON]?.longitude ?: 0.0
    val marsLong = chart.planetPositions[Planet.MARS]?.longitude ?: 0.0
    val mercuryLong = chart.planetPositions[Planet.MERCURY]?.longitude ?: 0.0
    val jupiterLong = chart.planetPositions[Planet.JUPITER]?.longitude ?: 0.0
    val venusLong = chart.planetPositions[Planet.VENUS]?.longitude ?: 0.0
    val saturnLong = chart.planetPositions[Planet.SATURN]?.longitude ?: 0.0
    val ascLong = chart.ascendant.ordinal * 30.0 + chart.ascendantDegree

    val sahamDefinitions = listOf(
        SahamDefinition("Fortune", "Punya Saham", { if (isDayBirth) moonLong + ascLong - sunLong else sunLong + ascLong - moonLong }),
        SahamDefinition("Education", "Vidya Saham", { if (isDayBirth) mercuryLong + ascLong - sunLong else sunLong + ascLong - mercuryLong }),
        SahamDefinition("Fame", "Yashas Saham", { if (isDayBirth) jupiterLong + ascLong - sunLong else sunLong + ascLong - jupiterLong }),
        SahamDefinition("Friends", "Mitra Saham", { if (isDayBirth) moonLong + ascLong - mercuryLong else mercuryLong + ascLong - moonLong }),
        SahamDefinition("Wealth", "Dhana Saham", { if (isDayBirth) jupiterLong + ascLong - moonLong else moonLong + ascLong - jupiterLong }),
        SahamDefinition("Career", "Karma Saham", { if (isDayBirth) saturnLong + ascLong - sunLong else sunLong + ascLong - saturnLong }),
        SahamDefinition("Marriage", "Vivaha Saham", { if (isDayBirth) venusLong + ascLong - saturnLong else saturnLong + ascLong - venusLong }),
        SahamDefinition("Children", "Putra Saham", { if (isDayBirth) jupiterLong + ascLong - moonLong else moonLong + ascLong - jupiterLong }),
        SahamDefinition("Father", "Pitri Saham", { if (isDayBirth) saturnLong + ascLong - sunLong else sunLong + ascLong - saturnLong }),
        SahamDefinition("Mother", "Matri Saham", { if (isDayBirth) moonLong + ascLong - venusLong else venusLong + ascLong - moonLong }),
        SahamDefinition("Capability", "Samartha Saham", { if (isDayBirth) marsLong + ascLong - saturnLong else saturnLong + ascLong - marsLong }),
        SahamDefinition("Hope", "Asha Saham", { if (isDayBirth) saturnLong + ascLong - venusLong else venusLong + ascLong - saturnLong }),
        SahamDefinition("Disease", "Roga Saham", { if (isDayBirth) saturnLong + ascLong - marsLong else marsLong + ascLong - saturnLong }),
        SahamDefinition("Power", "Raja Saham", { if (isDayBirth) sunLong + ascLong - saturnLong else saturnLong + ascLong - sunLong }),
        SahamDefinition("Foreign", "Paradesa Saham", { saturnLong + ascLong - chart.planetPositions[getSignLord(ZodiacSign.entries[(chart.ascendant.ordinal + 8) % 12])]!!.longitude }),
        SahamDefinition("Longevity", "Mrityu Saham", { if (isDayBirth) saturnLong + ascLong - moonLong else moonLong + ascLong - saturnLong }),
        SahamDefinition("Siblings", "Bhratri Saham", { if (isDayBirth) jupiterLong + ascLong - saturnLong else saturnLong + ascLong - jupiterLong }),
        SahamDefinition("Greatness", "Mahatmya Saham", { if (isDayBirth) jupiterLong + ascLong - moonLong else moonLong + ascLong - jupiterLong }),
        SahamDefinition("Success", "Karyasiddhi Saham", { if (isDayBirth) saturnLong + ascLong - sunLong else sunLong + ascLong - saturnLong })
    )

    sahamDefinitions.forEach { definition ->
        try {
            val longitude = normalizeAngle(definition.formula())
            val sign = ZodiacSign.fromLongitude(longitude)
            val house = calculateHouse(longitude, ascLong)
            val degree = longitude % 30.0
            val lord = getSignLord(sign)
            val lordHouse = chart.planetPositions[lord]?.house ?: 1
            val lordStrength = calculatePlanetStrength(lord, chart)

            val isActive = isSahamActive(lord, chart, house)
            val interpretation = generateSahamInterpretation(definition.name, sign, house, lord, lordHouse, lordStrength)
            val activationPeriods = getSahamActivationPeriods(lord, chart)
            val formula = if (isDayBirth) definition.name + " (Day)" else definition.name + " (Night)"

            sahams.add(
                Saham(
                    name = definition.name,
                    sanskritName = definition.sanskritName,
                    formula = formula,
                    longitude = longitude,
                    sign = sign,
                    house = house,
                    degree = degree,
                    lord = lord,
                    lordHouse = lordHouse,
                    lordStrength = lordStrength,
                    interpretation = interpretation,
                    isActive = isActive,
                    activationPeriods = activationPeriods
                )
            )
        } catch (e: Exception) {
            // Skip sahams that can't be calculated
        }
    }

    return sahams.sortedByDescending { it.isActive }
}

private data class SahamDefinition(
    val name: String,
    val sanskritName: String,
    val formula: () -> Double
)

private fun isSahamActive(lord: Planet, chart: SolarReturnChart, house: Int): Boolean {
    val lordPosition = chart.planetPositions[lord] ?: return false
    val lordStrength = calculatePlanetStrength(lord, chart)

    val isLordStrong = lordStrength in listOf("Exalted", "Strong", "Angular")
    val isInGoodHouse = house in listOf(1, 2, 4, 5, 7, 9, 10, 11)
    val isLordWellPlaced = lordPosition.house in listOf(1, 4, 5, 7, 9, 10, 11)

    return (isLordStrong && isInGoodHouse) || (isLordWellPlaced && !lordPosition.isRetrograde)
}

private fun generateSahamInterpretation(
    name: String,
    sign: ZodiacSign,
    house: Int,
    lord: Planet,
    lordHouse: Int,
    lordStrength: String
): String {
    val areaDescription = when (name) {
        "Fortune" -> "overall luck and prosperity"
        "Education" -> "learning, intellectual pursuits, and academic success"
        "Fame" -> "recognition, reputation, and public image"
        "Friends" -> "friendships, social networks, and alliances"
        "Wealth" -> "financial prosperity and material gains"
        "Career" -> "professional advancement and career success"
        "Marriage" -> "matrimonial happiness and partnerships"
        "Children" -> "progeny, creativity, and matters related to children"
        "Father" -> "father's welfare and paternal relationships"
        "Mother" -> "mother's welfare and maternal relationships"
        "Capability" -> "personal abilities, skills, and competence"
        "Hope" -> "aspirations, wishes, and future plans"
        "Disease" -> "health challenges and recovery"
        "Power" -> "authority, influence, and leadership"
        "Foreign" -> "overseas opportunities and travel"
        "Longevity" -> "vitality and life force"
        "Siblings" -> "relationships with brothers and sisters"
        "Greatness" -> "spiritual growth and higher achievements"
        "Success" -> "accomplishment of goals and endeavors"
        else -> "related matters"
    }

    val houseInfluence = when (house) {
        1 -> "The Saham's placement in the ascendant brings these matters to personal focus."
        2 -> "Financial dimensions of $areaDescription are highlighted."
        3 -> "Communication and initiative play key roles in $areaDescription."
        4 -> "Home environment and inner peace affect $areaDescription."
        5 -> "Creativity and intelligence support $areaDescription."
        6 -> "Some obstacles may need to be overcome regarding $areaDescription."
        7 -> "Partnerships and relationships influence $areaDescription."
        8 -> "Transformation and deep changes affect $areaDescription."
        9 -> "Fortune and higher guidance support $areaDescription."
        10 -> "Career and public life connect with $areaDescription."
        11 -> "Gains and fulfillment of wishes enhance $areaDescription."
        12 -> "Spiritual dimensions and foreign connections relate to $areaDescription."
        else -> "Various factors influence $areaDescription."
    }

    val lordInfluence = when (lordStrength) {
        "Exalted", "Strong" -> "The Saham lord ${lord.displayName} is well-placed in house $lordHouse, promising positive outcomes."
        "Moderate", "Angular" -> "The Saham lord ${lord.displayName} in house $lordHouse provides reasonable support."
        "Debilitated", "Weak" -> "The Saham lord ${lord.displayName} requires attention as it faces some challenges in house $lordHouse."
        else -> "The Saham lord ${lord.displayName} in house $lordHouse influences these matters variably."
    }

    return "The $name Saham in ${sign.displayName} in house $house relates to $areaDescription this year. $houseInfluence $lordInfluence"
}

private fun getSahamActivationPeriods(lord: Planet, chart: SolarReturnChart): List<String> {
    val periods = mutableListOf<String>()

    val nakshatraLords = listOf(
        Planet.KETU, Planet.VENUS, Planet.SUN, Planet.MOON, Planet.MARS,
        Planet.RAHU, Planet.JUPITER, Planet.SATURN, Planet.MERCURY
    )

    if (lord in nakshatraLords) {
        periods.add("${lord.displayName} Mudda Dasha")
    }

    val lordPosition = chart.planetPositions[lord]
    if (lordPosition != null) {
        val transitMonth = (lordPosition.house + 3) % 12 + 1
        periods.add("Month $transitMonth (Transit)")
    }

    return periods
}

private fun calculatePanchaVargiyaBala(chart: SolarReturnChart): List<PanchaVargiyaBala> {
    val planets = listOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    return planets.map { planet ->
        val position = chart.planetPositions[planet]
        val longitude = position?.longitude ?: 0.0

        val uchcha = calculateUchchaBala(planet, longitude)
        val hadda = calculateHaddaBala(planet, longitude)
        val dreshkana = calculateDreshkanaBala(planet, longitude)
        val navamsha = calculateNavamshaBala(planet, longitude)
        val dwadashamsha = calculateDwadashamshabala(planet, longitude)

        val total = uchcha + hadda + dreshkana + navamsha + dwadashamsha

        val category = when {
            total >= 15 -> "Excellent"
            total >= 12 -> "Good"
            total >= 8 -> "Average"
            total >= 5 -> "Below Average"
            else -> "Weak"
        }

        PanchaVargiyaBala(
            planet = planet,
            uchcha = uchcha,
            hadda = hadda,
            dreshkana = dreshkana,
            navamsha = navamsha,
            dwadashamsha = dwadashamsha,
            total = total,
            category = category
        )
    }
}

private fun calculateUchchaBala(planet: Planet, longitude: Double): Double {
    val exaltationDegrees = mapOf(
        Planet.SUN to 10.0,
        Planet.MOON to 33.0,
        Planet.MARS to 298.0,
        Planet.MERCURY to 165.0,
        Planet.JUPITER to 95.0,
        Planet.VENUS to 357.0,
        Planet.SATURN to 200.0
    )

    val exaltationPoint = exaltationDegrees[planet] ?: return 0.0
    val diff = abs(normalizeAngle(longitude - exaltationPoint))
    val adjustedDiff = if (diff > 180) 360 - diff else diff

    return ((180 - adjustedDiff) / 180.0 * 5.0).coerceIn(0.0, 5.0)
}

private fun calculateHaddaBala(planet: Planet, longitude: Double): Double {
    val signIndex = (longitude / 30).toInt()
    val degreeInSign = longitude % 30

    val haddaRulers = getHaddaRulers(signIndex)
    var currentDegree = 0.0

    for ((ruler, degrees) in haddaRulers) {
        if (degreeInSign >= currentDegree && degreeInSign < currentDegree + degrees) {
            return if (ruler == planet) 4.0
            else if (areFriends(planet, ruler)) 3.0
            else if (areNeutral(planet, ruler)) 2.0
            else 1.0
        }
        currentDegree += degrees
    }

    return 2.0
}

private fun getHaddaRulers(signIndex: Int): List<Pair<Planet, Double>> {
    return when (signIndex % 4) {
        0 -> listOf(
            Planet.JUPITER to 6.0, Planet.VENUS to 6.0, Planet.MERCURY to 8.0,
            Planet.MARS to 5.0, Planet.SATURN to 5.0
        )
        1 -> listOf(
            Planet.VENUS to 8.0, Planet.MERCURY to 6.0, Planet.JUPITER to 8.0,
            Planet.SATURN to 5.0, Planet.MARS to 3.0
        )
        2 -> listOf(
            Planet.MERCURY to 6.0, Planet.JUPITER to 6.0, Planet.VENUS to 5.0,
            Planet.MARS to 7.0, Planet.SATURN to 6.0
        )
        else -> listOf(
            Planet.MARS to 7.0, Planet.VENUS to 6.0, Planet.MERCURY to 4.0,
            Planet.JUPITER to 7.0, Planet.SATURN to 6.0
        )
    }
}

private fun areFriends(planet1: Planet, planet2: Planet): Boolean {
    val friendships = mapOf(
        Planet.SUN to listOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
        Planet.MOON to listOf(Planet.SUN, Planet.MERCURY),
        Planet.MARS to listOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
        Planet.MERCURY to listOf(Planet.SUN, Planet.VENUS),
        Planet.JUPITER to listOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.VENUS to listOf(Planet.MERCURY, Planet.SATURN),
        Planet.SATURN to listOf(Planet.MERCURY, Planet.VENUS)
    )

    return friendships[planet1]?.contains(planet2) == true
}

private fun areNeutral(planet1: Planet, planet2: Planet): Boolean {
    val neutrals = mapOf(
        Planet.SUN to listOf(Planet.MERCURY),
        Planet.MOON to listOf(Planet.MARS, Planet.JUPITER, Planet.VENUS, Planet.SATURN),
        Planet.MARS to listOf(Planet.MERCURY, Planet.VENUS, Planet.SATURN),
        Planet.MERCURY to listOf(Planet.MARS, Planet.JUPITER, Planet.SATURN),
        Planet.JUPITER to listOf(Planet.MERCURY, Planet.SATURN),
        Planet.VENUS to listOf(Planet.MARS, Planet.JUPITER),
        Planet.SATURN to listOf(Planet.MARS, Planet.JUPITER)
    )

    return neutrals[planet1]?.contains(planet2) == true
}

private fun calculateDreshkanaBala(planet: Planet, longitude: Double): Double {
    val degreeInSign = longitude % 30
    val dreshkana = (degreeInSign / 10).toInt()

    val signIndex = (longitude / 30).toInt()
    val dreshkanaSign = when (dreshkana) {
        0 -> signIndex
        1 -> (signIndex + 4) % 12
        else -> (signIndex + 8) % 12
    }

    val dreshkanaLord = getSignLord(ZodiacSign.entries[dreshkanaSign])

    return when {
        dreshkanaLord == planet -> 4.0
        areFriends(planet, dreshkanaLord) -> 3.0
        areNeutral(planet, dreshkanaLord) -> 2.0
        else -> 1.0
    }
}

private fun calculateNavamshaBala(planet: Planet, longitude: Double): Double {
    val degreeInSign = longitude % 30
    val navamshaIndex = (degreeInSign / 3.333333).toInt()
    val signIndex = (longitude / 30).toInt()

    val startSign = when (signIndex % 4) {
        0 -> 0
        1 -> 9
        2 -> 6
        else -> 3
    }

    val navamshaSign = (startSign + navamshaIndex) % 12
    val navamshaLord = getSignLord(ZodiacSign.entries[navamshaSign])

    return when {
        navamshaLord == planet -> 4.0
        areFriends(planet, navamshaLord) -> 3.0
        areNeutral(planet, navamshaLord) -> 2.0
        else -> 1.0
    }
}

private fun calculateDwadashamshabala(planet: Planet, longitude: Double): Double {
    val degreeInSign = longitude % 30
    val dwadashamshaIndex = (degreeInSign / 2.5).toInt()
    val signIndex = (longitude / 30).toInt()

    val dwadashamshaSign = (signIndex + dwadashamshaIndex) % 12
    val dwadashamshaLord = getSignLord(ZodiacSign.entries[dwadashamshaSign])

    return when {
        dwadashamshaLord == planet -> 3.0
        areFriends(planet, dwadashamshaLord) -> 2.5
        areNeutral(planet, dwadashamshaLord) -> 1.5
        else -> 1.0
    }
}

private fun calculateTriPatakiChakra(chart: SolarReturnChart): TriPatakiChakra {
    val ascIndex = chart.ascendant.ordinal

    val sector1Signs = listOf(
        ZodiacSign.entries[ascIndex],
        ZodiacSign.entries[(ascIndex + 4) % 12],
        ZodiacSign.entries[(ascIndex + 8) % 12]
    )

    val sector2Signs = listOf(
        ZodiacSign.entries[(ascIndex + 1) % 12],
        ZodiacSign.entries[(ascIndex + 5) % 12],
        ZodiacSign.entries[(ascIndex + 9) % 12]
    )

    val sector3Signs = listOf(
        ZodiacSign.entries[(ascIndex + 2) % 12],
        ZodiacSign.entries[(ascIndex + 6) % 12],
        ZodiacSign.entries[(ascIndex + 10) % 12]
    )

    fun getPlanetsInSector(signs: List<ZodiacSign>): List<Planet> {
        return chart.planetPositions.filter { (_, pos) ->
            pos.sign in signs
        }.keys.toList()
    }

    val sector1Planets = getPlanetsInSector(sector1Signs)
    val sector2Planets = getPlanetsInSector(sector2Signs)
    val sector3Planets = getPlanetsInSector(sector3Signs)

    val sector1Influence = generateSectorInfluence("First Flag (Dharma Trikona)", sector1Planets)
    val sector2Influence = generateSectorInfluence("Second Flag (Artha Trikona)", sector2Planets)
    val sector3Influence = generateSectorInfluence("Third Flag (Kama Trikona)", sector3Planets)

    val sectors = listOf(
        TriPatakiSector(
            name = "Dharma (1, 5, 9)",
            signs = sector1Signs,
            planets = sector1Planets,
            influence = sector1Influence
        ),
        TriPatakiSector(
            name = "Artha (2, 6, 10)",
            signs = sector2Signs,
            planets = sector2Planets,
            influence = sector2Influence
        ),
        TriPatakiSector(
            name = "Kama (3, 7, 11)",
            signs = sector3Signs,
            planets = sector3Planets,
            influence = sector3Influence
        )
    )

    val dominantSector = sectors.maxByOrNull { it.planets.size }
    val dominantInfluence = when (dominantSector?.name?.take(6)) {
        "Dharma" -> "Spiritual growth and righteous pursuits dominate the year"
        "Artha" -> "Material prosperity and career advancement are emphasized"
        "Kama" -> "Relationships and desires take center stage"
        else -> "Balanced influences across all life areas"
    }

    val interpretation = buildTriPatakiInterpretation(sectors)

    return TriPatakiChakra(
        risingSign = chart.ascendant,
        sectors = sectors,
        dominantInfluence = dominantInfluence,
        interpretation = interpretation
    )
}

private fun generateSectorInfluence(sectorName: String, planets: List<Planet>): String {
    if (planets.isEmpty()) {
        return "No planets occupy this sector, indicating a quieter year for these matters."
    }

    val benefics = planets.filter { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY) }
    val malefics = planets.filter { it in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) }

    return when {
        benefics.size > malefics.size -> "Benefic planets ${benefics.joinToString { it.displayName }} bring favorable influences to this sector."
        malefics.size > benefics.size -> "Malefic planets ${malefics.joinToString { it.displayName }} bring challenges requiring effort in this sector."
        else -> "Mixed planetary influences in this sector suggest variable results."
    }
}

private fun buildTriPatakiInterpretation(sectors: List<TriPatakiSector>): String {
    val dharmaSector = sectors.find { it.name.startsWith("Dharma") }
    val arthaSector = sectors.find { it.name.startsWith("Artha") }
    val kamaSector = sectors.find { it.name.startsWith("Kama") }

    val interpretations = mutableListOf<String>()

    dharmaSector?.let {
        if (it.planets.isNotEmpty()) {
            interpretations.add("The Dharma trikona is activated with ${it.planets.size} planet(s), bringing focus to righteousness, fortune, and higher learning.")
        }
    }

    arthaSector?.let {
        if (it.planets.isNotEmpty()) {
            interpretations.add("The Artha trikona with ${it.planets.size} planet(s) emphasizes wealth accumulation, career progress, and practical achievements.")
        }
    }

    kamaSector?.let {
        if (it.planets.isNotEmpty()) {
            interpretations.add("The Kama trikona holding ${it.planets.size} planet(s) highlights relationships, desires, and social connections.")
        }
    }

    return if (interpretations.isNotEmpty()) {
        interpretations.joinToString(" ")
    } else {
        "The Tri-Pataki Chakra shows a balanced distribution of planetary energies across all life sectors."
    }
}

private fun calculateHousePredictions(
    chart: SolarReturnChart,
    muntha: MunthaResult,
    yearLord: Planet
): List<HousePrediction> {
    val predictions = mutableListOf<HousePrediction>()
    val ascIndex = chart.ascendant.ordinal

    for (house in 1..12) {
        val signIndex = (ascIndex + house - 1) % 12
        val sign = ZodiacSign.entries[signIndex]
        val houseLord = getSignLord(sign)
        val lordPosition = chart.planetPositions[houseLord]?.house ?: 1

        val planetsInHouse = chart.planetPositions.filter { (_, pos) ->
            pos.house == house
        }.keys.toList()

        val strength = calculateHouseStrength(house, houseLord, lordPosition, planetsInHouse, chart, muntha, yearLord)
        val keywords = getHouseKeywords(house)
        val prediction = generateHousePrediction(house, sign, houseLord, lordPosition, planetsInHouse, chart, muntha, yearLord)
        val rating = calculateHouseRating(house, houseLord, lordPosition, planetsInHouse, chart, muntha, yearLord)
        val specificEvents = generateSpecificEvents(house, houseLord, lordPosition, planetsInHouse, chart)

        predictions.add(
            HousePrediction(
                house = house,
                signOnCusp = sign,
                houseLord = houseLord,
                lordPosition = lordPosition,
                planetsInHouse = planetsInHouse,
                strength = strength,
                keywords = keywords,
                prediction = prediction,
                rating = rating,
                specificEvents = specificEvents
            )
        )
    }

    return predictions
}

private fun calculateHouseStrength(
    house: Int,
    lord: Planet,
    lordPosition: Int,
    planetsInHouse: List<Planet>,
    chart: SolarReturnChart,
    muntha: MunthaResult,
    yearLord: Planet
): String {
    var score = 0

    val beneficPositions = listOf(1, 2, 4, 5, 7, 9, 10, 11)
    if (lordPosition in beneficPositions) score += 2

    val lordStrength = calculatePlanetStrength(lord, chart)
    when (lordStrength) {
        "Exalted" -> score += 3
        "Strong" -> score += 2
        "Angular" -> score += 1
        "Debilitated" -> score -= 2
    }

    val benefics = listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON)
    val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)

    planetsInHouse.forEach { planet ->
        if (planet in benefics) score += 1
        if (planet in malefics) score -= 1
    }

    if (muntha.house == house) score += 2
    if (yearLord == lord) score += 1

    return when {
        score >= 5 -> "Excellent"
        score >= 3 -> "Strong"
        score >= 1 -> "Moderate"
        score >= -1 -> "Weak"
        else -> "Challenged"
    }
}

private fun getHouseKeywords(house: Int): List<String> {
    return when (house) {
        1 -> listOf("Self", "Personality", "Health", "Appearance", "New Beginnings")
        2 -> listOf("Wealth", "Family", "Speech", "Values", "Food")
        3 -> listOf("Siblings", "Courage", "Communication", "Short Travel", "Skills")
        4 -> listOf("Home", "Mother", "Property", "Vehicles", "Inner Peace")
        5 -> listOf("Children", "Intelligence", "Romance", "Creativity", "Investments")
        6 -> listOf("Enemies", "Health Issues", "Service", "Debts", "Competition")
        7 -> listOf("Marriage", "Partnership", "Business", "Public Dealings", "Contracts")
        8 -> listOf("Longevity", "Transformation", "Research", "Inheritance", "Hidden Matters")
        9 -> listOf("Fortune", "Father", "Religion", "Higher Education", "Long Travel")
        10 -> listOf("Career", "Status", "Authority", "Government", "Fame")
        11 -> listOf("Gains", "Income", "Friends", "Elder Siblings", "Aspirations")
        12 -> listOf("Losses", "Expenses", "Spirituality", "Foreign Lands", "Liberation")
        else -> listOf("General")
    }
}

private fun generateHousePrediction(
    house: Int,
    sign: ZodiacSign,
    lord: Planet,
    lordPosition: Int,
    planetsInHouse: List<Planet>,
    chart: SolarReturnChart,
    muntha: MunthaResult,
    yearLord: Planet
): String {
    val houseArea = when (house) {
        1 -> "personal development, health, and new initiatives"
        2 -> "finances, family relationships, and speech"
        3 -> "communication, courage, and short journeys"
        4 -> "home environment, property matters, and inner peace"
        5 -> "creativity, children, romance, and investments"
        6 -> "health management, overcoming obstacles, and service"
        7 -> "partnerships, marriage, and business relationships"
        8 -> "transformation, research, and handling of joint resources"
        9 -> "fortune, spiritual growth, and higher learning"
        10 -> "career advancement, public recognition, and authority"
        11 -> "gains, friendships, and fulfillment of desires"
        12 -> "spiritual development, foreign connections, and liberation"
        else -> "various life matters"
    }

    val lordAnalysis = buildString {
        append("The lord ${lord.displayName} in house $lordPosition ")
        val lordStrength = calculatePlanetStrength(lord, chart)
        append(
            when (lordStrength) {
                "Exalted" -> "is excellently placed, promising outstanding results."
                "Strong" -> "is well-positioned for positive outcomes."
                "Moderate" -> "provides moderate support for house matters."
                "Debilitated" -> "faces challenges requiring extra attention."
                else -> "influences results variably."
            }
        )
    }

    val planetaryInfluence = if (planetsInHouse.isNotEmpty()) {
        val benefics = planetsInHouse.filter { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON) }
        val malefics = planetsInHouse.filter { it in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) }

        when {
            benefics.isNotEmpty() && malefics.isEmpty() ->
                "Benefic ${benefics.joinToString { it.displayName }} in this house enhances positive outcomes."
            malefics.isNotEmpty() && benefics.isEmpty() ->
                "${malefics.joinToString { it.displayName }} in this house may bring challenges requiring patience."
            benefics.isNotEmpty() && malefics.isNotEmpty() ->
                "Mixed influences from ${planetsInHouse.joinToString { it.displayName }} create a dynamic situation."
            else -> ""
        }
    } else {
        "No planets occupy this house, so results depend primarily on the lord's position."
    }

    val specialIndications = buildString {
        if (muntha.house == house) {
            append("Muntha's presence here emphasizes these matters strongly this year. ")
        }
        if (yearLord == lord) {
            append("As the Year Lord rules this house, its significations are particularly prominent. ")
        }
    }

    return "House $house in ${sign.displayName} governs $houseArea. $lordAnalysis $planetaryInfluence $specialIndications".trim()
}

private fun calculateHouseRating(
    house: Int,
    lord: Planet,
    lordPosition: Int,
    planetsInHouse: List<Planet>,
    chart: SolarReturnChart,
    muntha: MunthaResult,
    yearLord: Planet
): Float {
    var rating = 3.0f

    val beneficLordPositions = listOf(1, 2, 4, 5, 7, 9, 10, 11)
    if (lordPosition in beneficLordPositions) rating += 0.5f

    val lordStrength = calculatePlanetStrength(lord, chart)
    rating += when (lordStrength) {
        "Exalted" -> 1.0f
        "Strong" -> 0.7f
        "Angular" -> 0.3f
        "Debilitated" -> -0.8f
        else -> 0.0f
    }

    planetsInHouse.forEach { planet ->
        when (planet) {
            Planet.JUPITER -> rating += 0.5f
            Planet.VENUS -> rating += 0.4f
            Planet.MOON -> rating += 0.2f
            Planet.MERCURY -> rating += 0.1f
            Planet.SUN -> rating += 0.1f
            Planet.SATURN -> rating -= 0.3f
            Planet.MARS -> rating -= 0.2f
            Planet.RAHU -> rating -= 0.2f
            Planet.KETU -> rating -= 0.3f
            else -> {}
        }
    }

    if (muntha.house == house) rating += 0.5f
    if (yearLord == lord) rating += 0.3f

    return rating.coerceIn(1.0f, 5.0f)
}

private fun generateSpecificEvents(
    house: Int,
    lord: Planet,
    lordPosition: Int,
    planetsInHouse: List<Planet>,
    chart: SolarReturnChart
): List<String> {
    val events = mutableListOf<String>()
    val lordStrength = calculatePlanetStrength(lord, chart)

    when (house) {
        1 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Increased vitality and personal confidence")
                events.add("Favorable for starting new ventures")
            }
            if (Planet.JUPITER in planetsInHouse) events.add("Spiritual growth and wisdom enhancement")
            if (Planet.MARS in planetsInHouse) events.add("Increased energy but watch for accidents")
        }
        2 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Financial gains and wealth accumulation")
                events.add("Improvement in family relationships")
            }
            if (Planet.VENUS in planetsInHouse) events.add("Acquisition of luxury items")
            if (Planet.SATURN in planetsInHouse) events.add("Need for careful financial planning")
        }
        3 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Success in communication and writing")
                events.add("Favorable short journeys")
            }
            if (Planet.MERCURY in planetsInHouse) events.add("Intellectual achievements")
        }
        4 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Property gains or home improvement")
                events.add("Happiness from mother")
            }
            if (Planet.MOON in planetsInHouse) events.add("Emotional contentment at home")
            if (Planet.SATURN in planetsInHouse) events.add("Possible property repairs or delays")
        }
        5 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Creative success and recognition")
                events.add("Favorable for children's matters")
            }
            if (Planet.JUPITER in planetsInHouse) events.add("Possible childbirth or academic success")
            if (Planet.VENUS in planetsInHouse) events.add("Romantic happiness")
        }
        6 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Victory over enemies and competitors")
                events.add("Improvement in health issues")
            }
            if (Planet.MARS in planetsInHouse) events.add("Success in competition but watch health")
        }
        7 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Strengthening of partnerships")
                events.add("Favorable for marriage or business")
            }
            if (Planet.VENUS in planetsInHouse) events.add("Romantic fulfillment in marriage")
            if (Planet.SATURN in planetsInHouse) events.add("Need for patience in relationships")
        }
        8 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Possible inheritance or insurance gains")
                events.add("Deep transformation and research success")
            }
            if (Planet.JUPITER in planetsInHouse) events.add("Protection from sudden troubles")
        }
        9 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Long-distance travel opportunities")
                events.add("Fortune and luck in endeavors")
            }
            if (Planet.JUPITER in planetsInHouse) events.add("Spiritual advancement and guru's blessings")
        }
        10 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Career advancement or promotion")
                events.add("Recognition from authorities")
            }
            if (Planet.SUN in planetsInHouse) events.add("Government favor or leadership role")
            if (Planet.SATURN in planetsInHouse) events.add("Hard work leading to eventual success")
        }
        11 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Fulfillment of desires and wishes")
                events.add("Gains from multiple sources")
            }
            if (Planet.JUPITER in planetsInHouse) events.add("Expansion of social network")
        }
        12 -> {
            if (lordStrength in listOf("Exalted", "Strong")) {
                events.add("Spiritual progress and meditation success")
                events.add("Favorable for foreign travel")
            }
            if (Planet.KETU in planetsInHouse) events.add("Deepening spiritual practices")
            if (Planet.SATURN in planetsInHouse) events.add("Need to manage expenses carefully")
        }
    }

    return events.take(4)
}

private fun generateMajorThemes(
    chart: SolarReturnChart,
    yearLord: Planet,
    muntha: MunthaResult,
    tajikaAspects: List<TajikaAspect>
): List<String> {
    val themes = mutableListOf<String>()

    themes.add(generateYearLordTheme(yearLord, chart))
    themes.add(generateMunthaTheme(muntha))

    val strongAspects = tajikaAspects.filter { it.strength in listOf(AspectStrength.VERY_STRONG, AspectStrength.STRONG) }
    if (strongAspects.isNotEmpty()) {
        val positiveAspects = strongAspects.count { it.type.isPositive }
        val negativeAspects = strongAspects.size - positiveAspects

        themes.add(
            when {
                positiveAspects > negativeAspects * 2 ->
                    "Strong positive Tajika yogas indicate excellent potential for success and fulfillment of goals"
                negativeAspects > positiveAspects * 2 ->
                    "Challenging planetary configurations require patience and strategic approach to overcome obstacles"
                else ->
                    "Mixed Tajika aspects suggest a year of both opportunities and challenges requiring balanced approach"
            }
        )
    }

    val angularPlanets = chart.planetPositions.filter { (_, pos) -> pos.house in listOf(1, 4, 7, 10) }
    if (angularPlanets.isNotEmpty()) {
        val planets = angularPlanets.keys.joinToString(", ") { it.displayName }
        themes.add("Angular placement of $planets brings prominence and activity in career, relationships, and personal development")
    }

    val beneficsInTrines = chart.planetPositions.filter { (planet, pos) ->
        planet in listOf(Planet.JUPITER, Planet.VENUS) && pos.house in listOf(1, 5, 9)
    }
    if (beneficsInTrines.isNotEmpty()) {
        themes.add("Benefic planets in trinal houses promise spiritual growth, good fortune, and creative expression")
    }

    val ascendantLord = getSignLord(chart.ascendant)
    val ascLordPos = chart.planetPositions[ascendantLord]
    if (ascLordPos != null && ascLordPos.house in listOf(1, 4, 5, 7, 9, 10, 11)) {
        themes.add("Well-placed ascendant lord ${ascendantLord.displayName} supports overall success and personal wellbeing")
    }

    return themes.take(6)
}

private fun generateYearLordTheme(yearLord: Planet, chart: SolarReturnChart): String {
    val position = chart.planetPositions[yearLord]
    val house = position?.house ?: 1
    val strength = calculatePlanetStrength(yearLord, chart)

    val lordNature = when (yearLord) {
        Planet.SUN -> "leadership, authority, and self-expression"
        Planet.MOON -> "emotional wellbeing, public connections, and nurturing"
        Planet.MARS -> "energy, courage, and competitive endeavors"
        Planet.MERCURY -> "communication, learning, and business activities"
        Planet.JUPITER -> "wisdom, expansion, and spiritual growth"
        Planet.VENUS -> "relationships, creativity, and material comforts"
        Planet.SATURN -> "discipline, responsibility, and long-term achievements"
        else -> "various life aspects"
    }

    val houseInfluence = when (house) {
        1 -> "with personal focus and self-development"
        2 -> "connected to financial and family matters"
        3 -> "emphasizing communication and courage"
        4 -> "centered on home and emotional security"
        5 -> "highlighting creativity and children"
        6 -> "requiring attention to health and service"
        7 -> "focused on partnerships and relationships"
        8 -> "involving transformation and deep changes"
        9 -> "blessed with fortune and higher learning"
        10 -> "driving career and public achievements"
        11 -> "promising gains and fulfilled desires"
        12 -> "emphasizing spiritual growth and foreign matters"
        else -> "influencing multiple areas"
    }

    val strengthStatement = when (strength) {
        "Exalted" -> "The exalted year lord promises exceptional results"
        "Strong" -> "The strong year lord supports positive outcomes"
        "Debilitated" -> "The challenged year lord requires extra effort"
        else -> "The year lord provides moderate support"
    }

    return "Year Lord ${yearLord.displayName} emphasizes $lordNature $houseInfluence. $strengthStatement."
}

private fun generateMunthaTheme(muntha: MunthaResult): String {
    val houseArea = when (muntha.house) {
        1 -> "personal development and new beginnings"
        2 -> "financial growth and family harmony"
        3 -> "communication, siblings, and short travels"
        4 -> "home, property, and inner peace"
        5 -> "creativity, children, and romance"
        6 -> "overcoming obstacles and health improvement"
        7 -> "partnerships and relationship developments"
        8 -> "transformation and handling of shared resources"
        9 -> "fortune, travel, and higher wisdom"
        10 -> "career advancement and public recognition"
        11 -> "gains, friendships, and wish fulfillment"
        12 -> "spirituality, foreign lands, and inner growth"
        else -> "various life aspects"
    }

    return "Muntha in ${muntha.sign.displayName} (House ${muntha.house}) directs annual focus toward $houseArea with ${muntha.lord.displayName}'s influence."
}

private fun calculateMonthlyInfluences(
    chart: SolarReturnChart,
    solarReturnTime: LocalDateTime
): Pair<List<Int>, List<Int>> {
    val favorableMonths = mutableListOf<Int>()
    val challengingMonths = mutableListOf<Int>()

    val yearLord = getSignLord(chart.ascendant)
    val yearLordHouse = chart.planetPositions[yearLord]?.house ?: 1

    for (monthOffset in 0..11) {
        val month = ((solarReturnTime.monthValue - 1 + monthOffset) % 12) + 1

        val transitHouse = (yearLordHouse + monthOffset) % 12 + 1

        val isFavorable = when (transitHouse) {
            1, 2, 4, 5, 7, 9, 10, 11 -> true
            else -> false
        }

        val beneficsInHouse = chart.planetPositions.filter { (planet, pos) ->
            planet in listOf(Planet.JUPITER, Planet.VENUS) &&
                    ((pos.house + monthOffset - 1) % 12 + 1) in listOf(1, 4, 7, 10)
        }.isNotEmpty()

        val maleficsActive = chart.planetPositions.filter { (planet, pos) ->
            planet in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU) &&
                    ((pos.house + monthOffset - 1) % 12 + 1) in listOf(1, 4, 7, 10)
        }.isNotEmpty()

        when {
            isFavorable && beneficsInHouse -> favorableMonths.add(month)
            !isFavorable && maleficsActive -> challengingMonths.add(month)
            isFavorable -> if (favorableMonths.size < 4) favorableMonths.add(month)
            else -> if (challengingMonths.size < 3) challengingMonths.add(month)
        }
    }

    return Pair(favorableMonths.take(4), challengingMonths.take(3))
}

private fun calculateKeyDates(
    chart: SolarReturnChart,
    solarReturnTime: LocalDateTime,
    muddaDasha: List<MuddaDashaPeriod>
): List<KeyDate> {
    val keyDates = mutableListOf<KeyDate>()

    keyDates.add(
        KeyDate(
            date = solarReturnTime.toLocalDate(),
            event = "Solar Return",
            type = KeyDateType.IMPORTANT,
            description = "Beginning of the annual horoscope year - Sun returns to natal position"
        )
    )

    muddaDasha.forEach { period ->
        keyDates.add(
            KeyDate(
                date = period.startDate,
                event = "${period.planet.displayName} Dasha Begins",
                type = if (period.planetStrength in listOf("Exalted", "Strong"))
                    KeyDateType.FAVORABLE else KeyDateType.IMPORTANT,
                description = "Start of ${period.planet.displayName} period lasting ${period.days} days"
            )
        )
    }

    val yearStart = solarReturnTime.toLocalDate()

    val jupiterTransits = listOf(
        yearStart.plusMonths(2) to "Jupiter transit activates fortune sector",
        yearStart.plusMonths(6) to "Jupiter aspects career house"
    )

    jupiterTransits.forEach { (date, description) ->
        if (date.isBefore(yearStart.plusYears(1))) {
            keyDates.add(
                KeyDate(
                    date = date,
                    event = "Jupiter Transit",
                    type = KeyDateType.FAVORABLE,
                    description = description
                )
            )
        }
    }

    val saturnTransits = listOf(
        yearStart.plusMonths(4) to "Saturn aspects requiring patience",
        yearStart.plusMonths(9) to "Saturn transit emphasizes discipline"
    )

    saturnTransits.forEach { (date, description) ->
        if (date.isBefore(yearStart.plusYears(1))) {
            keyDates.add(
                KeyDate(
                    date = date,
                    event = "Saturn Transit",
                    type = KeyDateType.CHALLENGING,
                    description = description
                )
            )
        }
    }

    val eclipseApprox = listOf(
        yearStart.plusMonths(3) to Pair("Lunar Eclipse Period", "Time for introspection and release"),
        yearStart.plusMonths(9) to Pair("Solar Eclipse Period", "Potential for new beginnings")
    )

    eclipseApprox.forEach { (date, info) ->
        if (date.isBefore(yearStart.plusYears(1))) {
            keyDates.add(
                KeyDate(
                    date = date,
                    event = info.first,
                    type = KeyDateType.IMPORTANT,
                    description = info.second
                )
            )
        }
    }

    return keyDates.sortedBy { it.date }.take(15)
}

private fun generateOverallPrediction(
    chart: SolarReturnChart,
    yearLord: Planet,
    muntha: MunthaResult,
    tajikaAspects: List<TajikaAspect>,
    housePredictions: List<HousePrediction>
): String {
    val yearLordStrength = calculatePlanetStrength(yearLord, chart)
    val yearLordHouse = chart.planetPositions[yearLord]?.house ?: 1

    val strongHouses = housePredictions.filter { it.strength in listOf("Excellent", "Strong") }
    val weakHouses = housePredictions.filter { it.strength in listOf("Weak", "Challenged") }

    val positiveAspects = tajikaAspects.count { it.type.isPositive }
    val challengingAspects = tajikaAspects.size - positiveAspects

    val overallTone = when {
        yearLordStrength in listOf("Exalted", "Strong") && strongHouses.size >= 6 -> "excellent"
        yearLordStrength in listOf("Exalted", "Strong") && strongHouses.size >= 4 -> "favorable"
        strongHouses.size > weakHouses.size -> "positive"
        weakHouses.size > strongHouses.size -> "challenging but growth-oriented"
        else -> "balanced"
    }

    val yearLordInfluence = when (yearLord) {
        Planet.SUN -> "Year Lord Sun brings focus on leadership, authority, and personal expression. This is a year to shine and take charge of important matters."
        Planet.MOON -> "Year Lord Moon emphasizes emotional wellbeing, public connections, and intuitive decision-making. Nurturing relationships and home life are highlighted."
        Planet.MARS -> "Year Lord Mars energizes initiatives, competitive endeavors, and courage. This is a year for action, but patience in conflicts is advised."
        Planet.MERCURY -> "Year Lord Mercury enhances communication, learning, and business activities. Intellectual pursuits and networking bring rewards."
        Planet.JUPITER -> "Year Lord Jupiter bestows wisdom, expansion, and good fortune. This is an auspicious year for growth in all areas."
        Planet.VENUS -> "Year Lord Venus brings harmony to relationships, enhances creativity, and attracts material comforts. Artistic and romantic pursuits flourish."
        Planet.SATURN -> "Year Lord Saturn teaches discipline, responsibility, and patience. Hard work this year lays foundation for lasting achievements."
        else -> "The Year Lord influences various aspects of life with balanced energy."
    }

    val munthaInfluence = "Muntha in the ${muntha.house}${getOrdinalSuffix(muntha.house)} house in ${muntha.sign.displayName} " +
            "directs special attention to ${getMunthaHouseFocus(muntha.house)}. " +
            "With its lord ${muntha.lord.displayName} ${if (muntha.lordStrength in listOf("Strong", "Exalted")) "well-placed" else "requiring attention"}, " +
            "these areas ${if (muntha.lordStrength in listOf("Strong", "Exalted")) "promise positive developments" else "need careful cultivation"}."

    val aspectSummary = when {
        positiveAspects > challengingAspects * 2 ->
            "The Tajika aspects are predominantly favorable, with $positiveAspects positive yogas supporting success and achievement."
        challengingAspects > positiveAspects * 2 ->
            "The challenging Tajika aspects ($challengingAspects) indicate areas requiring patience and strategic effort."
        else ->
            "The balanced mix of Tajika aspects ($positiveAspects favorable, $challengingAspects challenging) suggests a dynamic year with varied experiences."
    }

    val focusAreas = buildString {
        append("Key focus areas include: ")
        val areas = mutableListOf<String>()
        if (strongHouses.any { it.house == 1 }) areas.add("personal development")
        if (strongHouses.any { it.house == 2 }) areas.add("financial growth")
        if (strongHouses.any { it.house == 4 }) areas.add("home and property")
        if (strongHouses.any { it.house == 5 }) areas.add("creativity and children")
        if (strongHouses.any { it.house == 7 }) areas.add("partnerships")
        if (strongHouses.any { it.house == 10 }) areas.add("career advancement")
        if (strongHouses.any { it.house == 11 }) areas.add("gains and achievements")

        if (areas.isEmpty()) areas.add("balanced development across all areas")
        append(areas.joinToString(", "))
        append(".")
    }

    val cautionAreas = if (weakHouses.isNotEmpty()) {
        val areas = weakHouses.take(3).map { prediction ->
            when (prediction.house) {
                1 -> "health"
                2 -> "finances"
                6 -> "health issues"
                8 -> "sudden changes"
                12 -> "expenses"
                else -> getHouseSignificance(prediction.house).lowercase()
            }
        }.distinct()
        "Areas requiring attention include ${areas.joinToString(", ")}. "
    } else ""

    return buildString {
        append("This Varshaphala year presents an overall $overallTone outlook. ")
        append(yearLordInfluence)
        append(" ")
        append(munthaInfluence)
        append(" ")
        append(aspectSummary)
        append(" ")
        append(focusAreas)
        append(" ")
        append(cautionAreas)
        append("By understanding these planetary influences and working with them consciously, the year's potential can be maximized while navigating challenges with wisdom.")
    }
}

private fun getMunthaHouseFocus(house: Int): String {
    return when (house) {
        1 -> "self-development, health, and personal initiatives"
        2 -> "wealth accumulation, family matters, and speech"
        3 -> "communication, courage, siblings, and short travels"
        4 -> "home environment, property, mother, and inner peace"
        5 -> "creativity, children, romance, and investments"
        6 -> "health management, service, and overcoming obstacles"
        7 -> "marriage, partnerships, and business relationships"
        8 -> "transformation, inheritance, and deep research"
        9 -> "fortune, higher learning, spirituality, and long journeys"
        10 -> "career advancement, public recognition, and authority"
        11 -> "gains from various sources, friendships, and wish fulfillment"
        12 -> "spiritual growth, foreign connections, and liberation"
        else -> "various life matters"
    }
}

private fun calculateYearRating(
    chart: SolarReturnChart,
    yearLord: Planet,
    muntha: MunthaResult,
    tajikaAspects: List<TajikaAspect>,
    housePredictions: List<HousePrediction>
): Float {
    var rating = 3.0f

    val yearLordStrength = calculatePlanetStrength(yearLord, chart)
    rating += when (yearLordStrength) {
        "Exalted" -> 0.8f
        "Strong" -> 0.5f
        "Angular" -> 0.3f
        "Moderate" -> 0.0f
        "Debilitated" -> -0.5f
        else -> 0.0f
    }

    rating += when (muntha.lordStrength) {
        "Exalted", "Strong" -> 0.3f
        "Moderate" -> 0.1f
        "Debilitated" -> -0.3f
        else -> 0.0f
    }

    if (muntha.house in listOf(1, 2, 4, 5, 9, 10, 11)) rating += 0.2f

    val positiveAspects = tajikaAspects.count { it.type.isPositive && it.strength.weight >= 0.6 }
    val negativeAspects = tajikaAspects.count { !it.type.isPositive && it.strength.weight >= 0.6 }

    rating += (positiveAspects * 0.1f - negativeAspects * 0.1f).coerceIn(-0.5f, 0.5f)

    val averageHouseRating = housePredictions.map { it.rating }.average().toFloat()
    rating += (averageHouseRating - 3.0f) * 0.3f

    val beneficsAngular = chart.planetPositions.count { (planet, pos) ->
        planet in listOf(Planet.JUPITER, Planet.VENUS) && pos.house in listOf(1, 4, 7, 10)
    }
    rating += beneficsAngular * 0.15f

    val maleficsAngular = chart.planetPositions.count { (planet, pos) ->
        planet in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU) && pos.house in listOf(1, 4, 7, 10)
    }
    rating -= maleficsAngular * 0.1f

    return rating.coerceIn(1.0f, 5.0f)
}