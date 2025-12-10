package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.BikramSambatConverter
import com.astro.storm.data.localization.DateFormat
import com.astro.storm.data.localization.DateSystem
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalDateSystem
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.formatDate
import com.astro.storm.data.localization.formatDateRange
import com.astro.storm.data.localization.formatDurationYearsMonths
import com.astro.storm.data.localization.formatLocalized
import com.astro.storm.data.localization.formatRemainingDuration
import com.astro.storm.data.localization.getLocalizedName
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.Planet
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Immutable
private data class VimshottariPeriod(
    val planet: Planet,
    val years: Int
)

private val VIMSHOTTARI_SEQUENCE: List<VimshottariPeriod> = listOf(
    VimshottariPeriod(Planet.KETU, 7),
    VimshottariPeriod(Planet.VENUS, 20),
    VimshottariPeriod(Planet.SUN, 6),
    VimshottariPeriod(Planet.MOON, 10),
    VimshottariPeriod(Planet.MARS, 7),
    VimshottariPeriod(Planet.RAHU, 18),
    VimshottariPeriod(Planet.JUPITER, 16),
    VimshottariPeriod(Planet.SATURN, 19),
    VimshottariPeriod(Planet.MERCURY, 17)
)

private enum class DashaLevel {
    MAHADASHA, ANTARDASHA, PRATYANTARDASHA, SOOKSHMADASHA, PRANADASHA, DEHADASHA
}

@Stable
private data class DashaSizes(
    val circleSize: Dp,
    val mainFontSize: TextUnit,
    val subFontSize: TextUnit,
    val symbolSize: TextUnit,
    val progressHeight: Dp
)

private fun getDashaSizes(level: DashaLevel): DashaSizes = when (level) {
    DashaLevel.MAHADASHA -> DashaSizes(44.dp, 16.sp, 12.sp, 17.sp, 6.dp)
    DashaLevel.ANTARDASHA -> DashaSizes(36.dp, 14.sp, 11.sp, 14.sp, 5.dp)
    DashaLevel.PRATYANTARDASHA -> DashaSizes(28.dp, 12.sp, 10.sp, 11.sp, 4.dp)
    DashaLevel.SOOKSHMADASHA -> DashaSizes(24.dp, 11.sp, 9.sp, 10.sp, 3.dp)
    DashaLevel.PRANADASHA -> DashaSizes(20.dp, 10.sp, 8.sp, 9.sp, 2.dp)
    DashaLevel.DEHADASHA -> DashaSizes(18.dp, 9.sp, 7.sp, 8.sp, 2.dp)
}

@Composable
fun DashasTabContent(
    timeline: DashaCalculator.DashaTimeline,
    scrollToTodayEvent: SharedFlow<Unit>? = null
) {
    val listState = rememberLazyListState()

    var expandedMahadashaKeys by rememberSaveable { mutableStateOf(setOf<String>()) }
    var isDashaInfoExpanded by rememberSaveable { mutableStateOf(false) }
    var isSandhiSectionExpanded by rememberSaveable { mutableStateOf(true) }

    val upcomingSandhis = remember(timeline) {
        timeline.getUpcomingSandhisWithin(90)
    }

    val currentMahadashaIndex = remember(timeline) {
        timeline.mahadashas.indexOfFirst { it == timeline.currentMahadasha }
    }

    LaunchedEffect(scrollToTodayEvent) {
        scrollToTodayEvent?.collectLatest {
            if (currentMahadashaIndex >= 0) {
                val headerItemsCount = if (upcomingSandhis.isNotEmpty()) 3 else 2
                listState.animateScrollToItem(headerItemsCount + currentMahadashaIndex)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "current_period_header") {
            CurrentPeriodCard(timeline = timeline)
        }

        if (upcomingSandhis.isNotEmpty()) {
            item(key = "sandhi_alerts_section") {
                SandhiAlertsCard(
                    sandhis = upcomingSandhis,
                    isExpanded = isSandhiSectionExpanded,
                    onToggleExpand = { isSandhiSectionExpanded = it }
                )
            }
        }

        item(key = "timeline_overview") {
            DashaTimelineCard(timeline = timeline)
        }

        itemsIndexed(
            items = timeline.mahadashas,
            key = { index, mahadasha ->
                "mahadasha_${mahadasha.planet.symbol}_${mahadasha.startDate.toEpochDay()}_$index"
            }
        ) { index, mahadasha ->
            val mahadashaKey = "${mahadasha.planet.symbol}_${mahadasha.startDate.toEpochDay()}"
            val isCurrentMahadasha = mahadasha == timeline.currentMahadasha
            val isExpanded = mahadashaKey in expandedMahadashaKeys

            MahadashaCard(
                mahadasha = mahadasha,
                currentAntardasha = if (isCurrentMahadasha) timeline.currentAntardasha else null,
                isCurrentMahadasha = isCurrentMahadasha,
                isExpanded = isExpanded,
                onToggleExpand = { expanded ->
                    expandedMahadashaKeys = if (expanded) {
                        expandedMahadashaKeys + mahadashaKey
                    } else {
                        expandedMahadashaKeys - mahadashaKey
                    }
                }
            )
        }

        item(key = "dasha_info_footer") {
            DashaInfoCard(
                isExpanded = isDashaInfoExpanded,
                onToggleExpand = { isDashaInfoExpanded = it }
            )
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CurrentPeriodCard(timeline: DashaCalculator.DashaTimeline) {
    val currentMahadasha = timeline.currentMahadasha
    val currentAntardasha = timeline.currentAntardasha
    val currentPratyantardasha = timeline.currentPratyantardasha
    val currentSookshmadasha = timeline.currentSookshmadasha
    val currentPranadasha = timeline.currentPranadasha
    val currentDehadasha = timeline.currentDehadasha

    val language = LocalLanguage.current
    val dateSystem = LocalDateSystem.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ChartDetailColors.AccentGold.copy(alpha = 0.1f),
                spotColor = ChartDetailColors.AccentGold.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ChartDetailColors.AccentGold.copy(alpha = 0.2f),
                                    ChartDetailColors.AccentGold.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(StringKey.DASHA_CURRENT_DASHA_PERIOD),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.TextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = getLocalizedShortDescription(timeline, language),
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            BirthNakshatraInfo(timeline = timeline)

            Spacer(modifier = Modifier.height(20.dp))

            if (currentMahadasha != null) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashaPeriodRow(
                        label = stringResource(StringKey.DASHA_LEVEL_MAHADASHA),
                        planet = currentMahadasha.planet,
                        startDate = currentMahadasha.startDate,
                        endDate = currentMahadasha.endDate,
                        progress = currentMahadasha.getProgressPercent().toFloat() / 100f,
                        remainingText = formatRemainingYears(currentMahadasha.getRemainingYears()),
                        level = DashaLevel.MAHADASHA
                    )

                    currentAntardasha?.let { ad ->
                        DashaPeriodRow(
                            label = stringResource(StringKey.DASHA_LEVEL_ANTARDASHA),
                            planet = ad.planet,
                            startDate = ad.startDate,
                            endDate = ad.endDate,
                            progress = ad.getProgressPercent().toFloat() / 100f,
                            remainingText = formatRemainingDays(ad.getRemainingDays()),
                            level = DashaLevel.ANTARDASHA
                        )
                    }

                    currentPratyantardasha?.let { pd ->
                        DashaPeriodRow(
                            label = stringResource(StringKey.DASHA_LEVEL_PRATYANTARDASHA),
                            planet = pd.planet,
                            startDate = pd.startDate,
                            endDate = pd.endDate,
                            progress = calculateProgress(pd.startDate, pd.endDate),
                            remainingText = formatRemainingTime(LocalDate.now(), pd.endDate),
                            level = DashaLevel.PRATYANTARDASHA
                        )
                    }

                    currentSookshmadasha?.let { sd ->
                        DashaPeriodRow(
                            label = stringResource(StringKey.DASHA_LEVEL_SOOKSHMADASHA),
                            planet = sd.planet,
                            startDate = sd.startDate,
                            endDate = sd.endDate,
                            progress = calculateProgress(sd.startDate, sd.endDate),
                            remainingText = "",
                            level = DashaLevel.SOOKSHMADASHA
                        )
                    }

                    currentPranadasha?.let { prd ->
                        DashaPeriodRow(
                            label = stringResource(StringKey.DASHA_LEVEL_PRANADASHA),
                            planet = prd.planet,
                            startDate = prd.startDate,
                            endDate = prd.endDate,
                            progress = calculateProgress(prd.startDate, prd.endDate),
                            remainingText = formatPranadashaDuration(prd.durationMinutes),
                            level = DashaLevel.PRANADASHA
                        )
                    }

                    currentDehadasha?.let { dd ->
                        DashaPeriodRow(
                            label = stringResource(StringKey.DASHA_LEVEL_DEHADASHA),
                            planet = dd.planet,
                            startDate = dd.startDate,
                            endDate = dd.endDate,
                            progress = calculateProgress(dd.startDate, dd.endDate),
                            remainingText = formatDehadashaDuration(dd.durationMinutes),
                            level = DashaLevel.DEHADASHA
                        )
                    }
                }

                HorizontalDivider(
                    color = ChartDetailColors.DividerColor,
                    modifier = Modifier.padding(vertical = 18.dp)
                )

                CurrentPeriodSummary(
                    mahadasha = currentMahadasha,
                    antardasha = currentAntardasha
                )
            } else {
                EmptyDashaState()
            }
        }
    }
}

@Composable
private fun BirthNakshatraInfo(timeline: DashaCalculator.DashaTimeline) {
    val language = LocalLanguage.current
    val nakshatraLordColor = ChartDetailColors.getPlanetColor(timeline.birthNakshatraLord)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        nakshatraLordColor.copy(alpha = 0.15f),
                        CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = nakshatraLordColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeline.birthNakshatraLord.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = nakshatraLordColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(StringKey.DASHA_BIRTH_NAKSHATRA),
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${timeline.birthNakshatra.getLocalizedName(language)} (${stringResource(StringKey.DASHA_PADA)} ${formatNumber(timeline.birthNakshatraPada, language)})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(StringKey.DASHA_LORD),
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeline.birthNakshatraLord.getLocalizedName(language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = nakshatraLordColor
                )
            }
        }
    }
}

@Composable
private fun SandhiAlertsCard(
    sandhis: List<DashaCalculator.DashaSandhi>,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "sandhi_rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = ChartDetailColors.AccentOrange)
            ) { onToggleExpand(!isExpanded) },
        shape = RoundedCornerShape(20.dp),
        color = ChartDetailColors.AccentOrange.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 250))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                ChartDetailColors.AccentOrange.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = ChartDetailColors.AccentOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(StringKey.DASHA_SANDHI_ALERTS),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartDetailColors.TextPrimary,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(StringKey.DASHA_UPCOMING_TRANSITIONS, sandhis.size, 90),
                            fontSize = 12.sp,
                            color = ChartDetailColors.TextMuted
                        )
                    }
                }

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(StringKey.DASHA_COLLAPSE) else stringResource(StringKey.DASHA_EXPAND),
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = stringResource(StringKey.DASHA_SANDHI_EXPLANATION),
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    sandhis.forEachIndexed { index, sandhi ->
                        SandhiAlertRow(
                            sandhi = sandhi,
                            modifier = Modifier.padding(
                                top = if (index == 0) 0.dp else 6.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SandhiAlertRow(
    sandhi: DashaCalculator.DashaSandhi,
    modifier: Modifier = Modifier
) {
    val fromColor = ChartDetailColors.getPlanetColor(sandhi.fromPlanet)
    val toColor = ChartDetailColors.getPlanetColor(sandhi.toPlanet)
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, sandhi.transitionDate)
    val isImminent = daysUntil in 0..7
    val isWithinSandhi = sandhi.isWithinSandhi(today)

    val language = LocalLanguage.current
    val levelLabel = getDashaLevelName(sandhi.level, language)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isWithinSandhi -> ChartDetailColors.AccentOrange.copy(alpha = 0.15f)
            isImminent -> ChartDetailColors.AccentOrange.copy(alpha = 0.1f)
            else -> ChartDetailColors.CardBackgroundElevated
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(fromColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sandhi.fromPlanet.symbol,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = fromColor
                )
            }

            Icon(
                Icons.Outlined.SwapHoriz,
                contentDescription = null,
                tint = ChartDetailColors.TextMuted,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(18.dp)
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(toColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sandhi.toPlanet.symbol,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = toColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${sandhi.fromPlanet.getLocalizedName(language)} → ${sandhi.toPlanet.getLocalizedName(language)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "$levelLabel ${stringResource(StringKey.DASHA_TRANSITION)}",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when {
                        isWithinSandhi -> stringResource(StringKey.DASHA_ACTIVE_NOW)
                        daysUntil == 0L -> stringResource(StringKey.DASHA_TODAY)
                        daysUntil == 1L -> stringResource(StringKey.DASHA_TOMORROW)
                        isImminent -> stringResource(StringKey.DASHA_IN_DAYS, daysUntil.toInt())
                        else -> formatDate(sandhi.transitionDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.MONTH_YEAR)
                    },
                    fontSize = 12.sp,
                    fontWeight = if (isWithinSandhi || isImminent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isWithinSandhi || isImminent) {
                        ChartDetailColors.AccentOrange
                    } else {
                        ChartDetailColors.TextMuted
                    }
                )
                if (!isWithinSandhi && daysUntil > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatDate(sandhi.sandhiStartDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.MONTH_YEAR)} – ${formatDate(sandhi.sandhiEndDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.MONTH_YEAR)}",
                        fontSize = 10.sp,
                        color = ChartDetailColors.TextMuted.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDashaState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = ChartDetailColors.TextMuted,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(StringKey.DASHA_UNABLE_CALCULATE),
                fontSize = 14.sp,
                color = ChartDetailColors.TextMuted
            )
        }
    }
}

@Composable
private fun DashaPeriodRow(
    label: String,
    planet: Planet,
    startDate: LocalDate,
    endDate: LocalDate,
    progress: Float,
    remainingText: String,
    level: DashaLevel
) {
    val language = LocalLanguage.current
    val dateSystem = LocalDateSystem.current
    val planetColor = ChartDetailColors.getPlanetColor(planet)
    val sizes = getDashaSizes(level)
    val clampedProgress = progress.coerceIn(0f, 1f)

    // Format dates according to current date system
    val startDateFormatted = startDate.formatLocalized(DateFormat.FULL)
    val endDateFormatted = endDate.formatLocalized(DateFormat.FULL)
    val percentComplete = formatNumber((clampedProgress * 100).toInt(), language)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$label: ${planet.getLocalizedName(language)}, $percentComplete percent complete"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(sizes.circleSize)
                    .background(planetColor, CircleShape)
                    .border(
                        width = 2.dp,
                        color = planetColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = planet.symbol,
                    fontSize = sizes.symbolSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        fontSize = sizes.subFontSize,
                        color = ChartDetailColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = planet.getLocalizedName(language),
                        fontSize = sizes.mainFontSize,
                        fontWeight = when (level) {
                            DashaLevel.MAHADASHA -> FontWeight.Bold
                            DashaLevel.ANTARDASHA -> FontWeight.SemiBold
                            else -> FontWeight.Medium
                        },
                        color = planetColor
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "$startDateFormatted – $endDateFormatted",
                    fontSize = (sizes.subFontSize.value - 1).sp,
                    color = ChartDetailColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (remainingText.isNotEmpty() && level in listOf(
                        DashaLevel.MAHADASHA,
                        DashaLevel.ANTARDASHA,
                        DashaLevel.PRATYANTARDASHA
                    )
                ) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = remainingText,
                        fontSize = (sizes.subFontSize.value - 1).sp,
                        color = ChartDetailColors.AccentTeal,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(70.dp)
        ) {
            Text(
                text = "$percentComplete%",
                fontSize = sizes.subFontSize,
                fontWeight = FontWeight.Bold,
                color = planetColor
            )
            Spacer(modifier = Modifier.height(5.dp))
            LinearProgressIndicator(
                progress = { clampedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizes.progressHeight)
                    .clip(RoundedCornerShape(sizes.progressHeight / 2)),
                color = planetColor,
                trackColor = ChartDetailColors.DividerColor
            )
        }
    }
}

private fun calculateProgress(startDate: LocalDate, endDate: LocalDate): Float {
    val today = LocalDate.now()
    if (endDate.isBefore(startDate) || endDate == startDate) return 1f
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
    val elapsedDays = ChronoUnit.DAYS.between(startDate, today).coerceIn(0L, totalDays.toLong()).toFloat()
    return (elapsedDays / totalDays).coerceIn(0f, 1f)
}

private fun formatRemainingYears(years: Double): String {
    if (years <= 0) return ""
    val wholeYears = years.toInt()
    val remainingMonths = ((years - wholeYears) * 12).toInt()
    return when {
        wholeYears > 0 && remainingMonths > 0 -> "${wholeYears}y ${remainingMonths}m remaining"
        wholeYears > 0 -> "${wholeYears}y remaining"
        remainingMonths > 0 -> "${remainingMonths}m remaining"
        else -> ""
    }
}

private fun formatRemainingDays(days: Long): String {
    if (days <= 0) return ""
    val months = days / 30
    val remainingDays = days % 30
    return when {
        months > 0 && remainingDays > 0 -> "${months}m ${remainingDays}d remaining"
        months > 0 -> "${months}m remaining"
        else -> "${remainingDays}d remaining"
    }
}

private fun formatRemainingTime(today: LocalDate, endDate: LocalDate): String {
    if (!endDate.isAfter(today)) return ""
    val totalDays = ChronoUnit.DAYS.between(today, endDate)
    val years = totalDays / 365
    val remainingDaysAfterYears = totalDays % 365
    val months = remainingDaysAfterYears / 30
    val days = remainingDaysAfterYears % 30
    return when {
        years > 0 -> "${years}y ${months}m remaining"
        months > 0 -> "${months}m ${days}d remaining"
        else -> "${days}d remaining"
    }
}

private fun formatPranadashaDuration(durationMinutes: Long): String {
    if (durationMinutes <= 0) return ""
    val hours = durationMinutes / 60
    val mins = durationMinutes % 60
    return when {
        hours >= 24 -> {
            val days = hours / 24
            val remainingHours = hours % 24
            if (remainingHours > 0) "${days}d ${remainingHours}h" else "${days}d"
        }
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}

private fun formatDehadashaDuration(durationMinutes: Long): String {
    if (durationMinutes <= 0) return ""
    val hours = durationMinutes / 60
    val mins = durationMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}

@Composable
private fun CurrentPeriodSummary(
    mahadasha: DashaCalculator.Mahadasha,
    antardasha: DashaCalculator.Antardasha?
) {
    val interpretation = remember(mahadasha, antardasha) {
        getDashaPeriodInterpretation(
            mahadasha.planet,
            antardasha?.planet
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(StringKey.DASHA_PERIOD_INSIGHTS),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.AccentGold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = interpretation,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun DashaTimelineCard(timeline: DashaCalculator.DashaTimeline) {
    val today = LocalDate.now()
    val language = LocalLanguage.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ChartDetailColors.AccentTeal.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            ChartDetailColors.AccentTeal.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Timeline,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(StringKey.DASHA_TIMELINE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(StringKey.DASHA_COMPLETE_CYCLE),
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
            }

            timeline.mahadashas.forEachIndexed { index, dasha ->
                val isPast = dasha.endDate.isBefore(today)
                val isCurrent = dasha.isActiveOn(today)
                val planetColor = ChartDetailColors.getPlanetColor(dasha.planet)
                val isLast = index == timeline.mahadashas.lastIndex

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 32.dp else 26.dp)
                                .background(
                                    color = when {
                                        isCurrent -> planetColor
                                        isPast -> planetColor.copy(alpha = 0.35f)
                                        else -> planetColor.copy(alpha = 0.6f)
                                    },
                                    shape = CircleShape
                                )
                                .then(
                                    if (isCurrent) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = planetColor.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dasha.planet.symbol,
                                fontSize = if (isCurrent) 12.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(22.dp)
                                    .background(
                                        if (isPast || isCurrent) {
                                            ChartDetailColors.DividerColor
                                        } else {
                                            ChartDetailColors.DividerColor.copy(alpha = 0.4f)
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (isLast) 0.dp else 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dasha.planet.getLocalizedName(language),
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent -> planetColor
                                isPast -> ChartDetailColors.TextMuted
                                else -> ChartDetailColors.TextSecondary
                            },
                            modifier = Modifier.width(72.dp)
                        )

                        Text(
                            text = "${formatDate(dasha.startDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.YEAR_ONLY)} – ${formatDate(dasha.endDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.YEAR_ONLY)}",
                            fontSize = 12.sp,
                            color = if (isCurrent) ChartDetailColors.TextPrimary else ChartDetailColors.TextMuted,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrent) planetColor.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = formatDurationYearsLocalized(dasha.durationYears, language),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCurrent) planetColor else ChartDetailColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MahadashaCard(
    mahadasha: DashaCalculator.Mahadasha,
    currentAntardasha: DashaCalculator.Antardasha?,
    isCurrentMahadasha: Boolean,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "expand_rotation"
    )

    val planetColor = ChartDetailColors.getPlanetColor(mahadasha.planet)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = planetColor.copy(alpha = 0.3f))
            ) { onToggleExpand(!isExpanded) },
        shape = RoundedCornerShape(18.dp),
        color = if (isCurrentMahadasha) {
            planetColor.copy(alpha = 0.08f)
        } else {
            ChartDetailColors.CardBackground
        },
        tonalElevation = if (isCurrentMahadasha) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 250))
        ) {
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
                            .size(48.dp)
                            .background(planetColor, CircleShape)
                            .then(
                                if (isCurrentMahadasha) {
                                    Modifier.border(
                                        width = 2.5.dp,
                                        color = planetColor.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mahadasha.planet.symbol,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        val language = LocalLanguage.current
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${mahadasha.planet.getLocalizedName(language)} ${stringResource(StringKey.DASHA_LEVEL_MAHADASHA)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChartDetailColors.TextPrimary,
                                letterSpacing = (-0.2).sp
                            )
                            if (isCurrentMahadasha) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = planetColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = stringResource(StringKey.DASHA_ACTIVE),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = planetColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${formatDurationYearsLocalized(mahadasha.durationYears, language)} • ${formatDate(mahadasha.startDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.FULL)} – ${formatDate(mahadasha.endDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.FULL)}",
                            fontSize = 11.sp,
                            color = ChartDetailColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentMahadasha) {
                            Spacer(modifier = Modifier.height(3.dp))
                            val percentComplete = String.format(java.util.Locale.ENGLISH, "%.1f", mahadasha.getProgressPercent())
                            Text(
                                text = "${stringResource(StringKey.DASHA_PERCENT_COMPLETE, percentComplete)} • ${formatRemainingYearsLocalized(mahadasha.getRemainingYears(), language)}",
                                fontSize = 10.sp,
                                color = ChartDetailColors.AccentTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(StringKey.DASHA_COLLAPSE) else stringResource(StringKey.DASHA_EXPAND),
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier
                        .size(26.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        color = ChartDetailColors.DividerColor,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = stringResource(StringKey.DASHA_LEVEL_ANTARDASHA),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartDetailColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = ChartDetailColors.CardBackgroundElevated
                        ) {
                            Text(
                                text = stringResource(StringKey.DASHA_SUB_PERIODS, mahadasha.antardashas.size),
                                fontSize = 10.sp,
                                color = ChartDetailColors.TextMuted,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    mahadasha.antardashas.forEachIndexed { index, antardasha ->
                        val isCurrentAD = antardasha == currentAntardasha
                        AntardashaRow(
                            antardasha = antardasha,
                            mahadashaPlanet = mahadasha.planet,
                            isCurrent = isCurrentAD,
                            modifier = Modifier.padding(top = if (index == 0) 0.dp else 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AntardashaRow(
    antardasha: DashaCalculator.Antardasha,
    mahadashaPlanet: Planet,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val planetColor = ChartDetailColors.getPlanetColor(antardasha.planet)
    val today = LocalDate.now()
    val isPast = antardasha.endDate.isBefore(today)
    val language = LocalLanguage.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = when {
                    isCurrent -> planetColor.copy(alpha = 0.12f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = when {
                            isPast -> planetColor.copy(alpha = 0.4f)
                            else -> planetColor
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = antardasha.planet.symbol,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${mahadashaPlanet.symbol}–${antardasha.planet.getLocalizedName(language)}",
                        fontSize = 13.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCurrent -> planetColor
                            isPast -> ChartDetailColors.TextMuted
                            else -> ChartDetailColors.TextPrimary
                        }
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format(java.util.Locale.ENGLISH, "%.0f", antardasha.getProgressPercent())}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = planetColor.copy(alpha = 0.9f)
                        )
                    }
                }
                if (isCurrent) {
                    val remaining = formatRemainingDaysLocalized(antardasha.getRemainingDays(), language)
                    if (remaining.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = remaining,
                            fontSize = 10.sp,
                            color = ChartDetailColors.AccentTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${formatDate(antardasha.startDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.MONTH_YEAR)} – ${formatDate(antardasha.endDate, LocalDateSystem.current, LocalLanguage.current, DateFormat.MONTH_YEAR)}",
                fontSize = 11.sp,
                color = ChartDetailColors.TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatDurationYearsLocalized(antardasha.durationYears, language),
                fontSize = 10.sp,
                color = ChartDetailColors.TextMuted.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DashaInfoCard(
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "info_rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ) { onToggleExpand(!isExpanded) },
        shape = RoundedCornerShape(18.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 250))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                ChartDetailColors.AccentPurple.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = ChartDetailColors.AccentPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(StringKey.DASHA_ABOUT_VIMSHOTTARI),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary,
                        letterSpacing = (-0.2).sp
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(StringKey.DASHA_COLLAPSE) else stringResource(StringKey.DASHA_EXPAND),
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    Text(
                        text = stringResource(StringKey.DASHA_VIMSHOTTARI_DESC),
                        fontSize = 13.sp,
                        color = ChartDetailColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ChartDetailColors.CardBackgroundElevated
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(StringKey.DASHA_PERIODS_SEQUENCE),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChartDetailColors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    VIMSHOTTARI_SEQUENCE.take(5).forEach { period ->
                                        DashaDurationRow(
                                            planet = period.planet,
                                            years = period.years
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    VIMSHOTTARI_SEQUENCE.drop(5).forEach { period ->
                                        DashaDurationRow(
                                            planet = period.planet,
                                            years = period.years
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = ChartDetailColors.DividerColor)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(StringKey.DASHA_TOTAL_CYCLE),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChartDetailColors.AccentGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DashaLevelsInfo()

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(StringKey.DASHA_SANDHI_NOTE),
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DashaLevelsInfo() {
    val language = LocalLanguage.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(StringKey.DASHA_HIERARCHY),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val levels = listOf(
                stringResource(StringKey.DASHA_LEVEL_MAHADASHA) to stringResource(StringKey.DASHA_MAJOR_PERIOD_YEARS),
                "${stringResource(StringKey.DASHA_LEVEL_ANTARDASHA)} (${stringResource(StringKey.DASHA_BHUKTI)})" to stringResource(StringKey.DASHA_SUB_PERIOD_MONTHS),
                stringResource(StringKey.DASHA_LEVEL_PRATYANTARDASHA) to stringResource(StringKey.DASHA_SUB_SUB_PERIOD_WEEKS),
                stringResource(StringKey.DASHA_LEVEL_SOOKSHMADASHA) to stringResource(StringKey.DASHA_SUBTLE_PERIOD_DAYS),
                stringResource(StringKey.DASHA_LEVEL_PRANADASHA) to stringResource(StringKey.DASHA_BREATH_PERIOD_HOURS),
                stringResource(StringKey.DASHA_LEVEL_DEHADASHA) to stringResource(StringKey.DASHA_BODY_PERIOD_MINUTES)
            )

            levels.forEachIndexed { index, (name, description) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                ChartDetailColors.AccentPurple.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChartDetailColors.AccentPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChartDetailColors.TextPrimary
                        )
                        Text(
                            text = description,
                            fontSize = 10.sp,
                            color = ChartDetailColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashaDurationRow(planet: Planet, years: Int) {
    val language = LocalLanguage.current
    val planetColor = ChartDetailColors.getPlanetColor(planet)

    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(planetColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = planet.symbol,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = planet.getLocalizedName(language),
            fontSize = 12.sp,
            color = ChartDetailColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatNumber(years, language)} ${stringResource(StringKey.DASHA_YEARS_ABBR)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ChartDetailColors.TextMuted
        )
    }
}

private fun getDashaPeriodInterpretation(
    mahadashaPlanet: Planet,
    antardashaPlanet: Planet?
): String {
    val mahaInterpretation = getMahadashaInterpretation(mahadashaPlanet)
    return if (antardashaPlanet != null && antardashaPlanet != mahadashaPlanet) {
        val antarInterpretation = getAntardashaInterpretation(antardashaPlanet)
        "$mahaInterpretation\n\n$antarInterpretation"
    } else {
        mahaInterpretation
    }
}

private fun getMahadashaInterpretation(planet: Planet): String = when (planet) {
    Planet.SUN -> "A period of heightened self-expression, authority, and recognition. Focus turns to career advancement, leadership roles, government dealings, and matters related to father. Soul purpose becomes clearer. Health of heart and vitality gains prominence. Good for developing confidence and establishing one's identity in the world."

    Planet.MOON -> "An emotionally rich and intuitive period emphasizing mental peace, nurturing, and receptivity. Focus on mother, home life, public image, travel across water, and emotional well-being. Creativity and imagination flourish. Memory and connection to the past strengthen. Relationships with women and the public become significant."

    Planet.MARS -> "A period of heightened energy, courage, initiative, and competitive drive. Focus on property matters, real estate, siblings, technical and engineering pursuits, sports, and surgery. Decisive action is favored. Physical vitality increases. Good for tackling challenges requiring strength and determination."

    Planet.MERCURY -> "A period of enhanced learning, communication, analytical thinking, and commerce. Focus on education, writing, publishing, accounting, trade, and intellectual pursuits. Social connections expand through skillful communication. Good for developing skills, starting businesses, and mastering information."

    Planet.JUPITER -> "A period of wisdom, expansion, prosperity, and divine grace (Guru's blessings). Focus on spirituality, higher learning, teaching, children, law, and philosophical pursuits. Fortune favors righteous endeavors. Faith and optimism increase. Excellent for marriage, progeny, and spiritual advancement."

    Planet.VENUS -> "A period of luxury, beauty, relationships, artistic expression, and material comforts. Focus on marriage, partnerships, arts, music, dance, vehicles, jewelry, and sensory pleasures. Creativity and romance blossom. Refinement in all areas of life. Good for enhancing beauty, wealth, and experiencing life's pleasures."

    Planet.SATURN -> "A period of discipline, karmic lessons, perseverance, and structural growth. Focus on service, responsibility, hard work, long-term projects, and lessons through patience. Delays and obstacles ultimately lead to lasting success and maturity. Time to build solid foundations and pay karmic debts."

    Planet.RAHU -> "A period of intense worldly ambition, unconventional paths, and material desires. Focus on foreign connections, technology, innovation, and breaking traditional boundaries. Sudden opportunities and unexpected changes arise. Material gains through unusual or non-traditional means. Beware of illusions."

    Planet.KETU -> "A period of spirituality, detachment, and profound inner transformation. Focus on liberation (moksha), occult research, healing practices, and resolving past-life karma. Deep introspection yields spiritual insights. Material attachments may dissolve. Excellent for meditation, research, and spiritual practices."

    else -> "A period of transformation and karmic unfolding according to planetary influences."
}

private fun getAntardashaInterpretation(planet: Planet): String = when (planet) {
    Planet.SUN -> "Current sub-period (Bhukti) activates themes of authority, self-confidence, recognition, and dealings with father figures or government. Leadership opportunities may arise."

    Planet.MOON -> "Current sub-period emphasizes emotional matters, mental peace, mother, public image, domestic affairs, and connection with women. Intuition heightens."

    Planet.MARS -> "Current sub-period brings increased energy, drive for action, courage, and matters involving property, siblings, competition, or technical endeavors."

    Planet.MERCURY -> "Current sub-period emphasizes communication, learning, business transactions, intellectual activities, and connections with younger people or merchants."

    Planet.JUPITER -> "Current sub-period brings wisdom, expansion, good fortune, and focus on spirituality, teachers, children, higher education, or legal matters."

    Planet.VENUS -> "Current sub-period emphasizes relationships, romance, creativity, luxury, artistic pursuits, material comforts, and partnership matters."

    Planet.SATURN -> "Current sub-period brings discipline, responsibility, hard work, delays, and lessons requiring patience. Focus on service and long-term efforts."

    Planet.RAHU -> "Current sub-period emphasizes worldly ambitions, unconventional approaches, foreign matters, technology, and sudden changes or opportunities."

    Planet.KETU -> "Current sub-period brings spiritual insights, detachment, introspection, research, and resolution of past karmic patterns. Material concerns recede."

    else -> "Current sub-period brings mixed planetary influences requiring careful navigation."
}

// ============================================
// LOCALIZATION HELPER FUNCTIONS
// ============================================

/**
 * Format a number according to the current language
 */
private fun formatNumber(number: Int, language: Language): String {
    return when (language) {
        Language.ENGLISH -> number.toString()
        Language.NEPALI -> BikramSambatConverter.toNepaliNumerals(number)
    }
}

/**
 * Get localized short description for the dasha timeline
 */
private fun getLocalizedShortDescription(
    timeline: DashaCalculator.DashaTimeline,
    language: Language
): String {
    val mahadasha = timeline.currentMahadasha
    val antardasha = timeline.currentAntardasha

    return if (mahadasha != null && antardasha != null) {
        "${mahadasha.planet.getLocalizedName(language)}-${antardasha.planet.getLocalizedName(language)}"
    } else if (mahadasha != null) {
        mahadasha.planet.getLocalizedName(language)
    } else {
        ""
    }
}

/**
 * Get localized dasha level name
 */
private fun getDashaLevelName(level: DashaCalculator.DashaLevel, language: Language): String {
    return when (level) {
        DashaCalculator.DashaLevel.MAHADASHA -> when (language) {
            Language.ENGLISH -> "Mahadasha"
            Language.NEPALI -> "महादशा"
        }
        DashaCalculator.DashaLevel.ANTARDASHA -> when (language) {
            Language.ENGLISH -> "Antardasha"
            Language.NEPALI -> "अन्तर्दशा"
        }
        DashaCalculator.DashaLevel.PRATYANTARDASHA -> when (language) {
            Language.ENGLISH -> "Pratyantardasha"
            Language.NEPALI -> "प्रत्यन्तर्दशा"
        }
        DashaCalculator.DashaLevel.SOOKSHMADASHA -> when (language) {
            Language.ENGLISH -> "Sookshmadasha"
            Language.NEPALI -> "सूक्ष्मदशा"
        }
        DashaCalculator.DashaLevel.PRANADASHA -> when (language) {
            Language.ENGLISH -> "Pranadasha"
            Language.NEPALI -> "प्राणदशा"
        }
        DashaCalculator.DashaLevel.DEHADASHA -> when (language) {
            Language.ENGLISH -> "Dehadasha"
            Language.NEPALI -> "देहदशा"
        }
    }
}

/**
 * Format localized remaining years string
 */
private fun formatRemainingYearsLocalized(years: Double, language: Language): String {
    if (years <= 0) return ""
    val wholeYears = years.toInt()
    val remainingMonths = ((years - wholeYears) * 12).toInt()

    return when (language) {
        Language.ENGLISH -> when {
            wholeYears > 0 && remainingMonths > 0 -> "${wholeYears}y ${remainingMonths}m remaining"
            wholeYears > 0 -> "${wholeYears}y remaining"
            remainingMonths > 0 -> "${remainingMonths}m remaining"
            else -> ""
        }
        Language.NEPALI -> when {
            wholeYears > 0 && remainingMonths > 0 -> "${BikramSambatConverter.toNepaliNumerals(wholeYears)} वर्ष ${BikramSambatConverter.toNepaliNumerals(remainingMonths)} महिना बाँकी"
            wholeYears > 0 -> "${BikramSambatConverter.toNepaliNumerals(wholeYears)} वर्ष बाँकी"
            remainingMonths > 0 -> "${BikramSambatConverter.toNepaliNumerals(remainingMonths)} महिना बाँकी"
            else -> ""
        }
    }
}

/**
 * Format localized remaining days string
 */
private fun formatRemainingDaysLocalized(days: Long, language: Language): String {
    if (days <= 0) return ""
    val months = days / 30
    val remainingDays = days % 30

    return when (language) {
        Language.ENGLISH -> when {
            months > 0 && remainingDays > 0 -> "${months}m ${remainingDays}d remaining"
            months > 0 -> "${months}m remaining"
            else -> "${remainingDays}d remaining"
        }
        Language.NEPALI -> when {
            months > 0 && remainingDays > 0 -> "${BikramSambatConverter.toNepaliNumerals(months.toInt())} महिना ${BikramSambatConverter.toNepaliNumerals(remainingDays.toInt())} दिन बाँकी"
            months > 0 -> "${BikramSambatConverter.toNepaliNumerals(months.toInt())} महिना बाँकी"
            else -> "${BikramSambatConverter.toNepaliNumerals(remainingDays.toInt())} दिन बाँकी"
        }
    }
}

/**
 * Format localized time remaining (for pratyantardasha level)
 */
private fun formatRemainingTimeLocalized(today: LocalDate, endDate: LocalDate, language: Language): String {
    if (!endDate.isAfter(today)) return ""
    val totalDays = ChronoUnit.DAYS.between(today, endDate)
    val years = totalDays / 365
    val remainingDaysAfterYears = totalDays % 365
    val months = remainingDaysAfterYears / 30
    val days = remainingDaysAfterYears % 30

    return when (language) {
        Language.ENGLISH -> when {
            years > 0 -> "${years}y ${months}m remaining"
            months > 0 -> "${months}m ${days}d remaining"
            else -> "${days}d remaining"
        }
        Language.NEPALI -> when {
            years > 0 -> "${BikramSambatConverter.toNepaliNumerals(years.toInt())} वर्ष ${BikramSambatConverter.toNepaliNumerals(months.toInt())} महिना बाँकी"
            months > 0 -> "${BikramSambatConverter.toNepaliNumerals(months.toInt())} महिना ${BikramSambatConverter.toNepaliNumerals(days.toInt())} दिन बाँकी"
            else -> "${BikramSambatConverter.toNepaliNumerals(days.toInt())} दिन बाँकी"
        }
    }
}

/**
 * Format duration in years and months (localized)
 */
private fun formatDurationYearsLocalized(years: Double, language: Language): String {
    val wholeYears = years.toInt()
    val months = ((years - wholeYears) * 12).toInt()

    return when (language) {
        Language.ENGLISH -> when {
            months > 0 -> "${wholeYears}y ${months}m"
            else -> "$wholeYears yrs"
        }
        Language.NEPALI -> when {
            months > 0 -> "${BikramSambatConverter.toNepaliNumerals(wholeYears)} वर्ष ${BikramSambatConverter.toNepaliNumerals(months)} महिना"
            else -> "${BikramSambatConverter.toNepaliNumerals(wholeYears)} वर्ष"
        }
    }
}

/**
 * Format pranadasha duration
 */
private fun formatPranadashaDurationLocalized(durationMinutes: Long, language: Language): String {
    if (durationMinutes <= 0) return ""
    val hours = durationMinutes / 60
    val mins = durationMinutes % 60

    return when (language) {
        Language.ENGLISH -> when {
            hours >= 24 -> {
                val days = hours / 24
                val remainingHours = hours % 24
                if (remainingHours > 0) "${days}d ${remainingHours}h" else "${days}d"
            }
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
        Language.NEPALI -> when {
            hours >= 24 -> {
                val days = hours / 24
                val remainingHours = hours % 24
                if (remainingHours > 0) "${BikramSambatConverter.toNepaliNumerals(days.toInt())} दिन ${BikramSambatConverter.toNepaliNumerals(remainingHours.toInt())} घण्टा"
                else "${BikramSambatConverter.toNepaliNumerals(days.toInt())} दिन"
            }
            hours > 0 && mins > 0 -> "${BikramSambatConverter.toNepaliNumerals(hours.toInt())} घण्टा ${BikramSambatConverter.toNepaliNumerals(mins.toInt())} मिनेट"
            hours > 0 -> "${BikramSambatConverter.toNepaliNumerals(hours.toInt())} घण्टा"
            else -> "${BikramSambatConverter.toNepaliNumerals(mins.toInt())} मिनेट"
        }
    }
}

/**
 * Format dehadasha duration
 */
private fun formatDehadashaDurationLocalized(durationMinutes: Long, language: Language): String {
    if (durationMinutes <= 0) return ""
    val hours = durationMinutes / 60
    val mins = durationMinutes % 60

    return when (language) {
        Language.ENGLISH -> when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
        Language.NEPALI -> when {
            hours > 0 && mins > 0 -> "${BikramSambatConverter.toNepaliNumerals(hours.toInt())} घण्टा ${BikramSambatConverter.toNepaliNumerals(mins.toInt())} मिनेट"
            hours > 0 -> "${BikramSambatConverter.toNepaliNumerals(hours.toInt())} घण्टा"
            else -> "${BikramSambatConverter.toNepaliNumerals(mins.toInt())} मिनेट"
        }
    }
}