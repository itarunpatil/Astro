package com.astro.storm.ui.screen.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ephemeris.HoroscopeCalculator
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.InsightsUiState
import com.astro.storm.ui.viewmodel.InsightsViewModel
import com.astro.storm.ui.viewmodel.InsightsData
import com.astro.storm.ui.viewmodel.InsightError
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class HoroscopePeriod(val displayName: String) {
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    WEEKLY("Weekly")
}

@Composable
fun InsightsTab(chart: VedicChart?, viewModel: InsightsViewModel = viewModel()) {
    LaunchedEffect(chart) {
        viewModel.loadInsights(chart)
    }

    val insightsState by viewModel.uiState.collectAsState()

    when (val state = insightsState) {
        is InsightsUiState.Loading -> InsightsLoadingSkeleton()
        is InsightsUiState.Error -> InsightsErrorState(
            message = state.message,
            onRetry = { viewModel.loadInsights(chart) }
        )
        is InsightsUiState.Success -> {
            var selectedPeriod by remember { mutableStateOf(HoroscopePeriod.TODAY) }
            InsightsContent(
                cache = state.data,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                onRetryFailed = { viewModel.loadInsights(chart) }
            )
        }
        is InsightsUiState.Idle -> EmptyInsightsState()
    }
}

@Composable
private fun InsightsContent(
    cache: InsightsData,
    selectedPeriod: HoroscopePeriod,
    onPeriodSelected: (HoroscopePeriod) -> Unit,
    onRetryFailed: () -> Unit
) {
    val listState = rememberLazyListState()

    val hasAnyContent = cache.dashaTimeline != null ||
            cache.todayHoroscope != null ||
            cache.tomorrowHoroscope != null ||
            cache.weeklyHoroscope != null

    if (!hasAnyContent && cache.errors.isNotEmpty()) {
        InsightsErrorState(
            message = "Unable to calculate astrological insights. Please ensure ephemeris data is properly initialized.",
            onRetry = onRetryFailed
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        if (cache.errors.isNotEmpty()) {
            item(key = "partial_error_banner") {
                PartialErrorBanner(
                    errors = cache.errors,
                    onRetry = onRetryFailed
                )
            }
        }

        cache.dashaTimeline?.let { timeline ->
            item(key = "dasha_current") {
                CurrentDashaCard(timeline)
            }

            item(key = "dasha_timeline") {
                DashaTimelinePreview(timeline)
            }
        }

        cache.planetaryInfluences.takeIf { it.isNotEmpty() }?.let { influences ->
            item(key = "transits") {
                PlanetaryTransitsSection(influences)
            }
        }

        val hasHoroscopeContent = cache.todayHoroscope != null ||
                cache.tomorrowHoroscope != null ||
                cache.weeklyHoroscope != null

        if (hasHoroscopeContent) {
            item(key = "section_divider") {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = AppTheme.DividerColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item(key = "period_selector") {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    todayAvailable = cache.todayHoroscope != null,
                    tomorrowAvailable = cache.tomorrowHoroscope != null,
                    weeklyAvailable = cache.weeklyHoroscope != null
                )
            }

            when (selectedPeriod) {
                HoroscopePeriod.TODAY -> {
                    cache.todayHoroscope?.let { horoscope ->
                        item(key = "today_header") {
                            DailyHoroscopeHeader(horoscope, isTomorrow = false)
                        }
                        item(key = "today_energy") {
                            EnergyCard(horoscope.overallEnergy)
                        }
                        item(key = "today_areas") {
                            LifeAreasSection(horoscope.lifeAreas)
                        }
                        item(key = "today_lucky") {
                            LuckyElementsCard(horoscope.luckyElements)
                        }
                        item(key = "today_recs") {
                            RecommendationsCard(horoscope.recommendations, horoscope.cautions)
                        }
                        item(key = "today_affirmation") {
                            AffirmationCard(horoscope.affirmation)
                        }
                    } ?: run {
                        item(key = "today_unavailable") {
                            HoroscopeUnavailableCard(
                                period = "today",
                                onRetry = onRetryFailed
                            )
                        }
                    }
                }
                HoroscopePeriod.TOMORROW -> {
                    cache.tomorrowHoroscope?.let { horoscope ->
                        item(key = "tomorrow_header") {
                            DailyHoroscopeHeader(horoscope, isTomorrow = true)
                        }
                        item(key = "tomorrow_energy") {
                            EnergyCard(horoscope.overallEnergy)
                        }
                        item(key = "tomorrow_areas") {
                            LifeAreasSection(horoscope.lifeAreas)
                        }
                        item(key = "tomorrow_lucky") {
                            LuckyElementsCard(horoscope.luckyElements)
                        }
                    } ?: run {
                        item(key = "tomorrow_unavailable") {
                            HoroscopeUnavailableCard(
                                period = "tomorrow",
                                onRetry = onRetryFailed
                            )
                        }
                    }
                }
                HoroscopePeriod.WEEKLY -> {
                    cache.weeklyHoroscope?.let { weekly ->
                        item(key = "weekly_overview") {
                            WeeklyOverviewHeader(weekly)
                        }
                        item(key = "weekly_chart") {
                            WeeklyEnergyChart(weekly.dailyHighlights)
                        }
                        item(key = "weekly_dates") {
                            KeyDatesSection(weekly.keyDates)
                        }
                        item(key = "weekly_predictions") {
                            WeeklyPredictionsSection(weekly.weeklyPredictions)
                        }
                        item(key = "weekly_advice") {
                            WeeklyAdviceCard(weekly.weeklyAdvice)
                        }
                    } ?: run {
                        item(key = "weekly_unavailable") {
                            HoroscopeUnavailableCard(
                                period = "weekly",
                                onRetry = onRetryFailed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartialErrorBanner(
    errors: List<InsightError>,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.WarningColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = AppTheme.WarningColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Some insights unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.WarningColor
                )
                Text(
                    text = "${errors.size} calculation(s) could not be completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }

            TextButton(onClick = onRetry) {
                Text(
                    text = "Retry",
                    color = AppTheme.WarningColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HoroscopeUnavailableCard(
    period: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppTheme.ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = AppTheme.TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${period.replaceFirstChar { it.uppercase() }}'s horoscope unavailable",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unable to calculate planetary positions for this period. This may be due to ephemeris data limitations.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.AccentPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(AppTheme.AccentPrimary, AppTheme.AccentPrimary)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun InsightsErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.ErrorColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = AppTheme.ErrorColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Unable to Load Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.AccentPrimary,
                    contentColor = AppTheme.ButtonText
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Try Again",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InsightsLoadingSkeleton() {
    val shimmerColors = listOf(
        AppTheme.CardBackground.copy(alpha = 0.9f),
        AppTheme.CardBackground.copy(alpha = 0.4f),
        AppTheme.CardBackground.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerCard(brush, 180.dp)
        ShimmerCard(brush, 140.dp)
        ShimmerCard(brush, 100.dp)
        ShimmerCard(brush, 48.dp)
        ShimmerCard(brush, 200.dp)
        ShimmerCard(brush, 120.dp)
    }
}

@Composable
private fun ShimmerCard(brush: Brush, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

@Composable
private fun PeriodSelector(
    selectedPeriod: HoroscopePeriod,
    onPeriodSelected: (HoroscopePeriod) -> Unit,
    todayAvailable: Boolean = true,
    tomorrowAvailable: Boolean = true,
    weeklyAvailable: Boolean = true
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
            val isAvailable = when (period) {
                HoroscopePeriod.TODAY -> todayAvailable
                HoroscopePeriod.TOMORROW -> tomorrowAvailable
                HoroscopePeriod.WEEKLY -> weeklyAvailable
            }
            val isSelected = period == selectedPeriod
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    isSelected && isAvailable -> AppTheme.AccentPrimary
                    isSelected && !isAvailable -> AppTheme.AccentPrimary.copy(alpha = 0.5f)
                    else -> Color.Transparent
                },
                animationSpec = tween(250),
                label = "period_bg_${period.name}"
            )
            val textColor by animateColorAsState(
                targetValue = when {
                    isSelected -> AppTheme.ButtonText
                    !isAvailable -> AppTheme.TextMuted.copy(alpha = 0.5f)
                    else -> AppTheme.TextSecondary
                },
                animationSpec = tween(250),
                label = "period_text_${period.name}"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true
                    ) { onPeriodSelected(period) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = period.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )
                    if (!isAvailable) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = "Unavailable",
                            tint = textColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyHoroscopeHeader(
    horoscope: HoroscopeCalculator.DailyHoroscope,
    isTomorrow: Boolean
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = horoscope.date.format(dateFormatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.TextMuted
                )
                if (isTomorrow) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppTheme.AccentPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.AccentPrimary
                        )
                    }
                }
            }

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(
                    icon = Icons.Outlined.NightlightRound,
                    label = "Moon in ${horoscope.moonSign.displayName}",
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    icon = Icons.Outlined.Schedule,
                    label = horoscope.activeDasha,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
            color = AppTheme.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EnergyCard(overallEnergy: Int) {
    val animatedEnergy by animateIntAsState(
        targetValue = overallEnergy,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "energy_anim"
    )

    val energyColor = remember(overallEnergy) {
        when {
            overallEnergy < 4 -> AppTheme.ErrorColor
            overallEnergy < 7 -> AppTheme.WarningColor
            else -> AppTheme.SuccessColor
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(10) { index ->
                        val isActive = index < animatedEnergy
                        val dotColor by animateColorAsState(
                            targetValue = if (isActive) {
                                when {
                                    index < 3 -> AppTheme.ErrorColor
                                    index < 6 -> AppTheme.WarningColor
                                    else -> AppTheme.SuccessColor
                                }
                            } else {
                                AppTheme.ChipBackground
                            },
                            animationSpec = tween(300, delayMillis = index * 50),
                            label = "dot_$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "$overallEnergy/10",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = energyColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = getEnergyDescription(overallEnergy),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

private fun getEnergyDescription(energy: Int): String = when {
    energy >= 9 -> "Exceptional cosmic alignment - seize every opportunity!"
    energy >= 8 -> "Excellent day ahead - favorable for important decisions"
    energy >= 7 -> "Strong positive energy - good for new initiatives"
    energy >= 6 -> "Favorable energy - maintain steady progress"
    energy >= 5 -> "Balanced energy - stay centered and focused"
    energy >= 4 -> "Moderate energy - pace yourself wisely"
    energy >= 3 -> "Lower energy day - prioritize rest and reflection"
    energy >= 2 -> "Challenging day - practice patience and self-care"
    else -> "Rest and recharge recommended - avoid major decisions"
}

@Composable
private fun LifeAreasSection(lifeAreas: List<HoroscopeCalculator.LifeAreaPrediction>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Life Areas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        lifeAreas.forEachIndexed { index, prediction ->
            key(prediction.area.name) {
                LifeAreaCard(prediction)
                if (index < lifeAreas.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LifeAreaCard(prediction: HoroscopeCalculator.LifeAreaPrediction) {
    var expanded by remember { mutableStateOf(false) }

    val areaConfig = remember(prediction.area) {
        getLifeAreaConfig(prediction.area)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expanded = !expanded },
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
                        .background(areaConfig.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = areaConfig.icon,
                        contentDescription = null,
                        tint = areaConfig.color,
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
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < prediction.rating)
                                    Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (index < prediction.rating)
                                    areaConfig.color else AppTheme.TextSubtle,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "expand_rotation"
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = AppTheme.TextMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(300)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = prediction.prediction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(areaConfig.color.copy(alpha = 0.1f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = areaConfig.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = prediction.advice,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private data class LifeAreaConfig(val color: Color, val icon: ImageVector)

private fun getLifeAreaConfig(area: HoroscopeCalculator.LifeArea): LifeAreaConfig {
    return when (area) {
        HoroscopeCalculator.LifeArea.CAREER -> LifeAreaConfig(AppTheme.LifeAreaCareer, Icons.Outlined.Work)
        HoroscopeCalculator.LifeArea.LOVE -> LifeAreaConfig(AppTheme.LifeAreaLove, Icons.Outlined.Favorite)
        HoroscopeCalculator.LifeArea.HEALTH -> LifeAreaConfig(AppTheme.LifeAreaHealth, Icons.Outlined.FavoriteBorder)
        HoroscopeCalculator.LifeArea.FINANCE -> LifeAreaConfig(AppTheme.LifeAreaFinance, Icons.Outlined.AccountBalance)
        HoroscopeCalculator.LifeArea.FAMILY -> LifeAreaConfig(AppTheme.AccentTeal, Icons.Outlined.Home)
        HoroscopeCalculator.LifeArea.SPIRITUALITY -> LifeAreaConfig(AppTheme.LifeAreaSpiritual, Icons.Outlined.Star)
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
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
    if (recommendations.isEmpty() && cautions.isEmpty()) return

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
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = AppTheme.SuccessColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (cautions.isNotEmpty()) {
                if (recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = "Cautions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.WarningColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                cautions.forEach { caution ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
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
                            color = AppTheme.TextSecondary,
                            lineHeight = 20.sp
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
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.AccentPrimary.copy(alpha = 0.15f)
        ),
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
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }

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
                    key(highlight.dayOfWeek) {
                        DailyEnergyBar(highlight)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyEnergyBar(highlight: HoroscopeCalculator.DailyHighlight) {
    val animatedHeight by animateFloatAsState(
        targetValue = highlight.energy / 10f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "energy_bar_${highlight.dayOfWeek}"
    )

    val barColor = remember(highlight.energy) {
        when {
            highlight.energy < 4 -> AppTheme.ErrorColor
            highlight.energy < 7 -> AppTheme.WarningColor
            else -> AppTheme.SuccessColor
        }
    }

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
                    .fillMaxHeight(animatedHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
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

@Composable
private fun KeyDatesSection(keyDates: List<HoroscopeCalculator.KeyDate>) {
    if (keyDates.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Key Dates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        keyDates.forEachIndexed { index, keyDate ->
            key(keyDate.date.toString()) {
                KeyDateCard(keyDate)
                if (index < keyDates.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun KeyDateCard(keyDate: HoroscopeCalculator.KeyDate) {
    val indicatorColor = if (keyDate.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .background(indicatorColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = keyDate.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor
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
                    color = AppTheme.TextMuted,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = if (keyDate.isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                contentDescription = null,
                tint = indicatorColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WeeklyPredictionsSection(predictions: Map<HoroscopeCalculator.LifeArea, String>) {
    if (predictions.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Weekly Overview by Area",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        predictions.entries.forEachIndexed { index, (area, prediction) ->
            key(area.name) {
                WeeklyAreaCard(area, prediction)
                if (index < predictions.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun WeeklyAreaCard(area: HoroscopeCalculator.LifeArea, prediction: String) {
    var expanded by remember { mutableStateOf(false) }
    val areaConfig = remember(area) { getLifeAreaConfig(area) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expanded = !expanded },
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
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(areaConfig.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = areaConfig.icon,
                        contentDescription = null,
                        tint = areaConfig.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = area.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "weekly_expand_${area.name}"
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.TextMuted,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(300)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
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

@Composable
private fun WeeklyAdviceCard(advice: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.AccentPrimary.copy(alpha = 0.15f)
        ),
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
    val currentMahadasha = timeline.currentMahadasha ?: return
    val currentAntardasha = timeline.currentAntardasha

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Planetary Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )

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

            DashaPeriodRow(
                label = "Mahadasha",
                planet = currentMahadasha.planet,
                startDate = currentMahadasha.startDate,
                endDate = currentMahadasha.endDate,
                isPrimary = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            val mahadashaProgress = remember(currentMahadasha) {
                calculateProgress(currentMahadasha.startDate, currentMahadasha.endDate)
            }
            DashaProgressBar(
                progress = mahadashaProgress,
                color = getPlanetColor(currentMahadasha.planet)
            )

            currentAntardasha?.let { antardasha ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = AppTheme.DividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                DashaPeriodRow(
                    label = "Antardasha",
                    planet = antardasha.planet,
                    startDate = antardasha.startDate,
                    endDate = antardasha.endDate,
                    isPrimary = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                val antardashaProgress = remember(antardasha) {
                    calculateProgress(antardasha.startDate, antardasha.endDate)
                }
                DashaProgressBar(
                    progress = antardashaProgress,
                    color = getPlanetColor(antardasha.planet),
                    height = 6
                )

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
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(getPlanetColor(pratyantardasha.planet).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pratyantardasha.planet.symbol,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = getPlanetColor(pratyantardasha.planet),
                                fontSize = 8.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
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

@Composable
private fun DashaPeriodRow(
    label: String,
    planet: Planet,
    startDate: LocalDate,
    endDate: LocalDate,
    isPrimary: Boolean
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") }
    val planetColor = getPlanetColor(planet)
    val daysRemaining = remember(endDate) {
        ChronoUnit.DAYS.between(LocalDate.now(), endDate)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isPrimary) 44.dp else 36.dp)
                .clip(CircleShape)
                .background(planetColor.copy(alpha = 0.15f))
                .border(width = 2.dp, color = planetColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = planet.symbol,
                style = if (isPrimary) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = planetColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$label: ${planet.displayName}",
                style = if (isPrimary) MaterialTheme.typography.titleSmall
                else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Text(
                text = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }

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
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "dasha_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height / 2))
            .background(AppTheme.ChipBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

@Composable
private fun DashaTimelinePreview(timeline: DashaCalculator.DashaTimeline) {
    val currentMahadasha = timeline.currentMahadasha ?: return
    val currentAntardasha = timeline.currentAntardasha ?: return

    val currentIndex = currentMahadasha.antardashas.indexOf(currentAntardasha)
    val upcomingAntardashas = remember(currentMahadasha, currentIndex) {
        currentMahadasha.antardashas.drop(currentIndex + 1).take(3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Upcoming Periods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (upcomingAntardashas.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.ChipBackground)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Antardasha is the last in this Mahadasha",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                }
            } else {
                upcomingAntardashas.forEachIndexed { index, antardasha ->
                    key(antardasha.planet.name + antardasha.startDate.toString()) {
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
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val daysUntil = remember(startDate) {
        ChronoUnit.DAYS.between(LocalDate.now(), startDate)
    }
    val planetColor = getPlanetColor(planet)

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
                .background(planetColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = planet.symbol,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = planetColor
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
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Current Transits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(
                items = influences.take(6),
                key = { it.planet.name }
            ) { influence ->
                TransitCard(influence)
            }
        }
    }
}

@Composable
private fun TransitCard(influence: HoroscopeCalculator.PlanetaryInfluence) {
    val planetColor = getPlanetColor(influence.planet)
    val trendColor = if (influence.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor

    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(planetColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = influence.planet.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = planetColor
                    )
                }

                Icon(
                    imageVector = if (influence.isPositive) Icons.Filled.TrendingUp
                    else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = trendColor,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                val strengthDots = (influence.strength / 2).coerceIn(0, 5)
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < strengthDots) trendColor else AppTheme.ChipBackground
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Insights,
                    contentDescription = null,
                    tint = AppTheme.TextMuted,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Profile Selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select or create a profile to view your personalized astrological insights and predictions",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

private fun calculateProgress(startDate: LocalDate, endDate: LocalDate): Float {
    val today = LocalDate.now()
    if (today.isBefore(startDate)) return 0f
    if (today.isAfter(endDate)) return 1f

    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
    if (totalDays <= 0) return 1f

    val elapsedDays = ChronoUnit.DAYS.between(startDate, today).toFloat()
    return (elapsedDays / totalDays).coerceIn(0f, 1f)
}

private fun formatDuration(days: Long): String {
    if (days <= 0) return "0d"

    return when {
        days < 7 -> "${days}d"
        days < 30 -> {
            val weeks = days / 7
            val remainingDays = days % 7
            if (remainingDays == 0L) "${weeks}w" else "${weeks}w ${remainingDays}d"
        }
        days < 365 -> {
            val months = days / 30
            val remainingDays = days % 30
            when {
                remainingDays == 0L -> "${months}m"
                remainingDays < 7 -> "${months}m ${remainingDays}d"
                else -> "${months}m ${remainingDays / 7}w"
            }
        }
        else -> {
            val years = days / 365
            val remainingDays = days % 365
            val months = remainingDays / 30
            if (months == 0L) "${years}y" else "${years}y ${months}m"
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
