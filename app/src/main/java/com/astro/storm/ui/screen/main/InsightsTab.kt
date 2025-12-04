package com.astro.storm.ui.screen.main

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ephemeris.HoroscopeCalculator
import com.astro.storm.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Insights Tab - Daily Horoscope, Planetary Periods & Transits
 *
 * Displays:
 * - Daily, weekly, and monthly horoscope predictions
 * - Current planetary period (Dasha) with progress
 * - Upcoming transits and their effects
 */
@Composable
fun InsightsTab(
    chart: VedicChart?,
) {
    val context = LocalContext.current
    var dashaTimeline by remember { mutableStateOf<DashaCalculator.DashaTimeline?>(null) }
    var planetaryInfluences by remember { mutableStateOf<List<HoroscopeCalculator.PlanetaryInfluence>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf(HoroscopePeriod.TODAY) }
    var dailyHoroscope by remember { mutableStateOf<HoroscopeCalculator.DailyHoroscope?>(null) }
    var tomorrowHoroscope by remember { mutableStateOf<HoroscopeCalculator.DailyHoroscope?>(null) }
    var weeklyHoroscope by remember { mutableStateOf<HoroscopeCalculator.WeeklyHoroscope?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Calculate insights and horoscopes when chart changes
    LaunchedEffect(chart) {
        if (chart != null) {
            isLoading = true
            withContext(Dispatchers.Default) {
                dashaTimeline = DashaCalculator.calculateDashaTimeline(chart)
                val calculator = HoroscopeCalculator(context)
                try {
                    val horoscope = calculator.calculateDailyHoroscope(chart)
                    planetaryInfluences = horoscope.planetaryInfluences
                    dailyHoroscope = calculator.calculateDailyHoroscope(chart, LocalDate.now())
                    tomorrowHoroscope = calculator.calculateDailyHoroscope(chart, LocalDate.now().plusDays(1))
                    weeklyHoroscope = calculator.calculateWeeklyHoroscope(chart, LocalDate.now())
                } finally {
                    calculator.close()
                }
            }
            isLoading = false
        }
    }

    if (chart == null) {
        EmptyInsightsState()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Loading state
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.AccentPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            return@LazyColumn
        }

        // Current Dasha Period
        item {
            dashaTimeline?.let { timeline ->
                CurrentDashaCard(timeline)
            }
        }

        // Dasha Timeline Preview
        item {
            dashaTimeline?.let { timeline ->
                DashaTimelinePreview(timeline)
            }
        }

        // Planetary Transits
        item {
            if (planetaryInfluences.isNotEmpty()) {
                PlanetaryTransitsSection(planetaryInfluences)
            }
        }

        // Spacer to separate sections
        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AppTheme.DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Period Selector for Horoscopes
        item {
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
        }

        // Horoscope Content based on selected period
        when (selectedPeriod) {
            HoroscopePeriod.TODAY -> {
                dailyHoroscope?.let { horoscope ->
                    item { DailyHoroscopeHeader(horoscope) }
                    item { EnergyCard(horoscope) }
                    item { LifeAreasSection(horoscope.lifeAreas) }
                    item { LuckyElementsCard(horoscope.luckyElements) }
                    item { RecommendationsCard(horoscope.recommendations, horoscope.cautions) }
                    item { AffirmationCard(horoscope.affirmation) }
                }
            }
            HoroscopePeriod.TOMORROW -> {
                tomorrowHoroscope?.let { horoscope ->
                    item { DailyHoroscopeHeader(horoscope, isTomorrow = true) }
                    item { EnergyCard(horoscope) }
                    item { LifeAreasSection(horoscope.lifeAreas) }
                    item { LuckyElementsCard(horoscope.luckyElements) }
                }
            }
            HoroscopePeriod.WEEKLY -> {
                weeklyHoroscope?.let { weekly ->
                    item { WeeklyOverviewHeader(weekly) }
                    item { WeeklyEnergyChart(weekly.dailyHighlights) }
                    item { KeyDatesSection(weekly.keyDates) }
                    item { WeeklyPredictionsSection(weekly.weeklyPredictions) }
                    item { WeeklyAdviceCard(weekly.weeklyAdvice) }
                }
            }
        }
    }
}

enum class HoroscopePeriod(val displayName: String) {
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    WEEKLY("Weekly")
}

@Composable
private fun PeriodSelector(
    selectedPeriod: HoroscopePeriod,
    onPeriodSelected: (HoroscopePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.CardBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HoroscopePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AppTheme.AccentPrimary else Color.Transparent)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) AppTheme.ButtonText else AppTheme.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyHoroscopeHeader(
    horoscope: HoroscopeCalculator.DailyHoroscope,
    isTomorrow: Boolean = false
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = horoscope.date.format(dateFormatter),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.TextMuted
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = horoscope.theme,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = horoscope.themeDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Outlined.NightlightRound,
                    label = "Moon in ${horoscope.moonSign.displayName}"
                )
                InfoChip(
                    icon = Icons.Outlined.Schedule,
                    label = horoscope.activeDasha
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.ChipBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.AccentPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextSecondary
        )
    }
}

@Composable
private fun EnergyCard(horoscope: HoroscopeCalculator.DailyHoroscope) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Overall Energy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(10) { index ->
                        val isActive = index < horoscope.overallEnergy
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) {
                                        when {
                                            index < 3 -> AppTheme.ErrorColor
                                            index < 6 -> AppTheme.WarningColor
                                            else -> AppTheme.SuccessColor
                                        }
                                    } else {
                                        AppTheme.ChipBackground
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${horoscope.overallEnergy}/10",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        horoscope.overallEnergy < 4 -> AppTheme.ErrorColor
                        horoscope.overallEnergy < 7 -> AppTheme.WarningColor
                        else -> AppTheme.SuccessColor
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    horoscope.overallEnergy >= 8 -> "Excellent day ahead!"
                    horoscope.overallEnergy >= 6 -> "Favorable energy today"
                    horoscope.overallEnergy >= 4 -> "Balanced energy - stay steady"
                    else -> "Take it easy today"
                },
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun LifeAreasSection(lifeAreas: List<HoroscopeCalculator.LifeAreaPrediction>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Life Areas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        lifeAreas.forEach { prediction ->
            LifeAreaCard(prediction)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LifeAreaCard(prediction: HoroscopeCalculator.LifeAreaPrediction) {
    var expanded by remember { mutableStateOf(false) }
    val areaColor = when (prediction.area) {
        HoroscopeCalculator.LifeArea.CAREER -> AppTheme.LifeAreaCareer
        HoroscopeCalculator.LifeArea.LOVE -> AppTheme.LifeAreaLove
        HoroscopeCalculator.LifeArea.HEALTH -> AppTheme.LifeAreaHealth
        HoroscopeCalculator.LifeArea.FINANCE -> AppTheme.LifeAreaFinance
        HoroscopeCalculator.LifeArea.FAMILY -> AppTheme.AccentTeal
        HoroscopeCalculator.LifeArea.SPIRITUALITY -> AppTheme.LifeAreaSpiritual
    }
    val areaIcon = when (prediction.area) {
        HoroscopeCalculator.LifeArea.CAREER -> Icons.Outlined.Work
        HoroscopeCalculator.LifeArea.LOVE -> Icons.Outlined.Favorite
        HoroscopeCalculator.LifeArea.HEALTH -> Icons.Outlined.FavoriteBorder
        HoroscopeCalculator.LifeArea.FINANCE -> Icons.Outlined.AccountBalance
        HoroscopeCalculator.LifeArea.FAMILY -> Icons.Outlined.Home
        HoroscopeCalculator.LifeArea.SPIRITUALITY -> Icons.Outlined.Star
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(areaColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = areaIcon,
                        contentDescription = null,
                        tint = areaColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prediction.area.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < prediction.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (index < prediction.rating) areaColor else AppTheme.TextSubtle,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = prediction.prediction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(areaColor.copy(alpha = 0.1f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = areaColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = prediction.advice,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LuckyElementsCard(lucky: HoroscopeCalculator.LuckyElements) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Lucky Elements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LuckyElement(Icons.Outlined.Numbers, "Number", lucky.number.toString())
                LuckyElement(Icons.Outlined.Palette, "Color", lucky.color.split(",").first().trim())
                LuckyElement(Icons.Outlined.Explore, "Direction", lucky.direction)
                LuckyElement(Icons.Outlined.Diamond, "Gemstone", lucky.gemstone)
            }
        }
    }
}

@Composable
private fun LuckyElement(icon: ImageVector, label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.ChipBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.AccentGold,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppTheme.TextPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>, cautions: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (recommendations.isNotEmpty()) {
                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.SuccessColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                recommendations.forEach { rec ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = AppTheme.SuccessColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = rec, style = MaterialTheme.typography.bodyMedium, color = AppTheme.TextSecondary)
                    }
                }
            }
            if (cautions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cautions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.WarningColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                cautions.forEach { caution ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = AppTheme.WarningColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = caution,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AffirmationCard(affirmation: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.AccentPrimary.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatQuote,
                contentDescription = null,
                tint = AppTheme.AccentPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = affirmation,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Today's Affirmation",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.AccentPrimary
            )
        }
    }
}

@Composable
private fun WeeklyOverviewHeader(weekly: HoroscopeCalculator.WeeklyHoroscope) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${weekly.startDate.format(dateFormatter)} - ${weekly.endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = weekly.weeklyTheme,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = weekly.weeklyOverview,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun WeeklyEnergyChart(dailyHighlights: List<HoroscopeCalculator.DailyHighlight>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Weekly Energy Flow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dailyHighlights.forEach { highlight ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppTheme.ChipBackground),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(highlight.energy / 10f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            highlight.energy < 4 -> AppTheme.ErrorColor
                                            highlight.energy < 7 -> AppTheme.WarningColor
                                            else -> AppTheme.SuccessColor
                                        }
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = highlight.dayOfWeek.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyDatesSection(keyDates: List<HoroscopeCalculator.KeyDate>) {
    if (keyDates.isEmpty()) return
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Key Dates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        keyDates.forEach { keyDate ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (keyDate.isPositive) AppTheme.SuccessColor.copy(alpha = 0.15f)
                                else AppTheme.WarningColor.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = keyDate.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (keyDate.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = keyDate.event,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            text = keyDate.significance,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyPredictionsSection(predictions: Map<HoroscopeCalculator.LifeArea, String>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Weekly Overview by Area",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        predictions.entries.forEach { (area, prediction) ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = area.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AppTheme.TextMuted
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = prediction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.TextSecondary,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyAdviceCard(advice: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.AccentPrimary.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Advice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.AccentPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = advice,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun CurrentDashaCard(timeline: DashaCalculator.DashaTimeline) {
    val currentMahadasha = timeline.currentMahadasha
    val currentAntardasha = timeline.currentAntardasha

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Planetary Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )

                // Status indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppTheme.SuccessColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            currentMahadasha?.let { mahadasha ->
                // Mahadasha
                DashaPeriodRow(
                    label = "Mahadasha",
                    planet = mahadasha.planet,
                    startDate = mahadasha.startDate,
                    endDate = mahadasha.endDate,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar for Mahadasha
                val mahadashaProgress = calculateProgress(mahadasha.startDate, mahadasha.endDate)
                DashaProgressBar(
                    progress = mahadashaProgress,
                    color = getPlanetColor(mahadasha.planet)
                )

                currentAntardasha?.let { antardasha ->
                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = AppTheme.DividerColor)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Antardasha (Bhukti)
                    DashaPeriodRow(
                        label = "Antardasha",
                        planet = antardasha.planet,
                        startDate = antardasha.startDate,
                        endDate = antardasha.endDate,
                        isPrimary = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val antardashaProgress = calculateProgress(antardasha.startDate, antardasha.endDate)
                    DashaProgressBar(
                        progress = antardashaProgress,
                        color = getPlanetColor(antardasha.planet),
                        height = 6
                    )

                    // Pratyantardasha if available
                    timeline.currentPratyantardasha?.let { pratyantardasha ->
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pratyantardasha:",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pratyantardasha.planet.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = getPlanetColor(pratyantardasha.planet)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashaPeriodRow(
    label: String,
    planet: Planet,
    startDate: LocalDate,
    endDate: LocalDate,
    isPrimary: Boolean
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Planet indicator
        Box(
            modifier = Modifier
                .size(if (isPrimary) 44.dp else 36.dp)
                .clip(CircleShape)
                .background(getPlanetColor(planet).copy(alpha = 0.15f))
                .border(
                    width = 2.dp,
                    color = getPlanetColor(planet),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = planet.symbol,
                style = if (isPrimary) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getPlanetColor(planet)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$label: ${planet.displayName}",
                style = if (isPrimary) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Text(
                text = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }

        // Days remaining
        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate)
        if (daysRemaining > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDuration(daysRemaining),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.AccentPrimary
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.TextMuted
                )
            }
        }
    }
}

@Composable
private fun DashaProgressBar(
    progress: Float,
    color: Color,
    height: Int = 8
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height / 2))
            .background(AppTheme.ChipBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

@Composable
private fun DashaTimelinePreview(timeline: DashaCalculator.DashaTimeline) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Upcoming Periods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show next 3 Antardashas
            val currentMahadasha = timeline.currentMahadasha
            val currentAntardasha = timeline.currentAntardasha

            if (currentMahadasha != null && currentAntardasha != null) {
                val currentIndex = currentMahadasha.antardashas.indexOf(currentAntardasha)
                val upcomingAntardashas = currentMahadasha.antardashas
                    .drop(currentIndex + 1)
                    .take(3)

                if (upcomingAntardashas.isEmpty()) {
                    Text(
                        text = "Current Antardasha is the last in this Mahadasha",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                } else {
                    upcomingAntardashas.forEachIndexed { index, antardasha ->
                        UpcomingPeriodItem(
                            planet = antardasha.planet,
                            mahadashaPlanet = currentMahadasha.planet,
                            startDate = antardasha.startDate,
                            isFirst = index == 0
                        )
                        if (index < upcomingAntardashas.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingPeriodItem(
    planet: Planet,
    mahadashaPlanet: Planet,
    startDate: LocalDate,
    isFirst: Boolean
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), startDate)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFirst) AppTheme.ChipBackground else Color.Transparent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(getPlanetColor(planet).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = planet.symbol,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = getPlanetColor(planet)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${mahadashaPlanet.displayName}-${planet.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Text(
                text = "Starts ${startDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }

        if (daysUntil > 0) {
            Text(
                text = "in ${formatDuration(daysUntil)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isFirst) AppTheme.AccentPrimary else AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun PlanetaryTransitsSection(influences: List<HoroscopeCalculator.PlanetaryInfluence>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Current Transits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Horizontal scrolling transit cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(influences.take(6)) { influence ->
                TransitCard(influence)
            }
        }
    }
}

@Composable
private fun TransitCard(influence: HoroscopeCalculator.PlanetaryInfluence) {
    Card(
        modifier = Modifier
            .width(160.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(getPlanetColor(influence.planet).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = influence.planet.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = getPlanetColor(influence.planet)
                    )
                }

                // Positive/Negative indicator
                Icon(
                    imageVector = if (influence.isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (influence.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = influence.planet.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = influence.influence,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Strength indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < (influence.strength / 2)) {
                                    if (influence.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor
                                } else {
                                    AppTheme.ChipBackground
                                }
                            )
                    )
                    if (index < 4) Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}


@Composable
private fun EmptyInsightsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Insights,
                contentDescription = null,
                tint = AppTheme.TextMuted,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Profile Selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select or create a profile to view your astrological insights",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions
private fun calculateProgress(startDate: LocalDate, endDate: LocalDate): Float {
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
    val elapsedDays = ChronoUnit.DAYS.between(startDate, today).toFloat()
    return (elapsedDays / totalDays).coerceIn(0f, 1f)
}

private fun formatDuration(days: Long): String {
    return when {
        days < 30 -> "$days days"
        days < 365 -> "${days / 30}m ${days % 30}d"
        else -> {
            val years = days / 365
            val months = (days % 365) / 30
            if (months > 0) "${years}y ${months}m" else "${years}y"
        }
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
