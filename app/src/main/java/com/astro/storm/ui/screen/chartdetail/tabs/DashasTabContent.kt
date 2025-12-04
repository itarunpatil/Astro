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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val DATE_FORMATTER_FULL: DateTimeFormatter = 
    DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
private val DATE_FORMATTER_MONTH_YEAR: DateTimeFormatter = 
    DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
private val DATE_FORMATTER_YEAR: DateTimeFormatter = 
    DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH)

private val VIMSHOTTARI_SEQUENCE: List<Pair<Planet, Int>> = listOf(
    Planet.KETU to 7,
    Planet.VENUS to 20,
    Planet.SUN to 6,
    Planet.MOON to 10,
    Planet.MARS to 7,
    Planet.RAHU to 18,
    Planet.JUPITER to 16,
    Planet.SATURN to 19,
    Planet.MERCURY to 17
)

@Composable
fun DashasTabContent(chart: VedicChart) {
    val dashaTimeline = remember(chart) {
        DashaCalculator.calculateDashaTimeline(chart)
    }

    val expandedMahadashaSymbols = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { mutableStateListOf<String>().apply { addAll(it) } }
        )
    ) { mutableStateListOf<String>() }

    var isDashaInfoExpanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "current_period") {
            CurrentPeriodCard(dashaTimeline)
        }

        item(key = "timeline") {
            DashaTimelineCard(dashaTimeline)
        }

        items(
            items = dashaTimeline.mahadashas,
            key = { "mahadasha_${it.planet.symbol}" }
        ) { mahadasha ->
            val planetSymbol = mahadasha.planet.symbol
            MahadashaCard(
                mahadasha = mahadasha,
                isCurrentMahadasha = mahadasha == dashaTimeline.currentMahadasha,
                isExpanded = planetSymbol in expandedMahadashaSymbols,
                onToggleExpand = { expanded ->
                    if (expanded) {
                        expandedMahadashaSymbols.add(planetSymbol)
                    } else {
                        expandedMahadashaSymbols.remove(planetSymbol)
                    }
                }
            )
        }

        item(key = "info") {
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ChartDetailColors.AccentGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = ChartDetailColors.AccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Current Dasha Period",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.TextPrimary
                    )
                    if (currentMahadasha != null) {
                        Text(
                            text = buildCurrentPeriodSubtitle(currentMahadasha, currentAntardasha, currentPratyantardasha),
                            fontSize = 12.sp,
                            color = ChartDetailColors.TextMuted
                        )
                    }
                }
            }

            if (currentMahadasha != null) {
                DashaPeriodRow(
                    label = "Mahadasha",
                    planet = currentMahadasha.planet,
                    startDate = currentMahadasha.startDate,
                    endDate = currentMahadasha.endDate,
                    level = DashaLevel.MAHADASHA
                )

                if (currentAntardasha != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    DashaPeriodRow(
                        label = "Antardasha",
                        planet = currentAntardasha.planet,
                        startDate = currentAntardasha.startDate,
                        endDate = currentAntardasha.endDate,
                        level = DashaLevel.ANTARDASHA
                    )
                }

                if (currentPratyantardasha != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    DashaPeriodRow(
                        label = "Pratyantardasha",
                        planet = currentPratyantardasha.planet,
                        startDate = currentPratyantardasha.startDate,
                        endDate = currentPratyantardasha.endDate,
                        level = DashaLevel.PRATYANTARDASHA
                    )
                }

                HorizontalDivider(
                    color = ChartDetailColors.DividerColor,
                    modifier = Modifier.padding(vertical = 16.dp)
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

private fun buildCurrentPeriodSubtitle(
    mahadasha: DashaCalculator.Mahadasha,
    antardasha: DashaCalculator.Antardasha?,
    pratyantardasha: DashaCalculator.Pratyantardasha?
): String {
    return buildString {
        append(mahadasha.planet.displayName)
        if (antardasha != null) {
            append(" → ")
            append(antardasha.planet.displayName)
        }
        if (pratyantardasha != null) {
            append(" → ")
            append(pratyantardasha.planet.displayName)
        }
    }
}

@Composable
private fun EmptyDashaState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = ChartDetailColors.TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Unable to calculate current dasha period",
                fontSize = 14.sp,
                color = ChartDetailColors.TextMuted
            )
        }
    }
}

private enum class DashaLevel {
    MAHADASHA, ANTARDASHA, PRATYANTARDASHA
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
}

@Composable
private fun DashaPeriodRow(
    label: String,
    planet: Planet,
    startDate: LocalDate,
    endDate: LocalDate,
    level: DashaLevel
) {
    val planetColor = ChartDetailColors.getPlanetColor(planet)
    val today = LocalDate.now()

    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(1L).toFloat()
    val elapsedDays = ChronoUnit.DAYS.between(startDate, today)
        .coerceIn(0L, totalDays.toLong())
        .toFloat()
    val progress = elapsedDays / totalDays

    val remainingTimeText = formatRemainingTime(today, endDate)
    val sizes = getDashaSizes(level)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$label: ${planet.displayName}, ${(progress * 100).toInt()} percent complete" },
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
                    .background(planetColor, CircleShape),
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
                        color = ChartDetailColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = planet.displayName,
                        fontSize = sizes.mainFontSize,
                        fontWeight = when (level) {
                            DashaLevel.MAHADASHA -> FontWeight.Bold
                            else -> FontWeight.SemiBold
                        },
                        color = planetColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${startDate.format(DATE_FORMATTER_FULL)} – ${endDate.format(DATE_FORMATTER_FULL)}",
                    fontSize = (sizes.subFontSize.value - 1).sp,
                    color = ChartDetailColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (remainingTimeText.isNotEmpty() && level != DashaLevel.PRATYANTARDASHA) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = remainingTimeText,
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
            modifier = Modifier.width(68.dp)
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = sizes.subFontSize,
                fontWeight = FontWeight.Bold,
                color = planetColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
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

private fun formatRemainingTime(today: LocalDate, endDate: LocalDate): String {
    if (!endDate.isAfter(today)) return ""

    val totalDays = ChronoUnit.DAYS.between(today, endDate)
    val years = totalDays / 365
    val remainingDaysAfterYears = totalDays % 365
    val months = remainingDaysAfterYears / 30
    val days = remainingDaysAfterYears % 30

    return buildString {
        when {
            years > 0 -> {
                append("${years}y ${months}m remaining")
            }
            months > 0 -> {
                append("${months}m ${days}d remaining")
            }
            else -> {
                append("${days}d remaining")
            }
        }
    }
}

@Composable
private fun CurrentPeriodSummary(
    mahadasha: DashaCalculator.Mahadasha,
    antardasha: DashaCalculator.Antardasha?
) {
    val interpretation = getDashaPeriodInterpretation(
        mahadasha.planet,
        antardasha?.planet
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Period Insights",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.AccentGold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ChartDetailColors.AccentTeal.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Outlined.Timeline,
                            contentDescription = null,
                            tint = ChartDetailColors.AccentTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Dasha Timeline",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                    Text(
                        text = "Complete 120-year Vimshottari cycle",
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
            }

            timeline.mahadashas.forEachIndexed { index, dasha ->
                val isPast = dasha.endDate.isBefore(today)
                val isCurrent = !dasha.startDate.isAfter(today) && !dasha.endDate.isBefore(today)
                val planetColor = ChartDetailColors.getPlanetColor(dasha.planet)
                val isLast = index == timeline.mahadashas.lastIndex

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 30.dp else 26.dp)
                                .background(
                                    color = when {
                                        isCurrent -> planetColor
                                        isPast -> planetColor.copy(alpha = 0.35f)
                                        else -> planetColor.copy(alpha = 0.6f)
                                    },
                                    shape = CircleShape
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
                                    .height(20.dp)
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
                            .padding(bottom = if (isLast) 0.dp else 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dasha.planet.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent -> planetColor
                                isPast -> ChartDetailColors.TextMuted
                                else -> ChartDetailColors.TextSecondary
                            },
                            modifier = Modifier.width(68.dp)
                        )

                        Text(
                            text = "${dasha.startDate.format(DATE_FORMATTER_YEAR)} – ${dasha.endDate.format(DATE_FORMATTER_YEAR)}",
                            fontSize = 12.sp,
                            color = if (isCurrent) ChartDetailColors.TextPrimary else ChartDetailColors.TextMuted,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCurrent) planetColor.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = formatDurationYears(dasha.durationYears),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCurrent) planetColor else ChartDetailColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationYears(years: Double): String {
    val wholeYears = years.toInt()
    val months = ((years - wholeYears) * 12).toInt()
    return when {
        months > 0 -> "${wholeYears}y ${months}m"
        else -> "${wholeYears} yrs"
    }
}

@Composable
private fun MahadashaCard(
    mahadasha: DashaCalculator.Mahadasha,
    isCurrentMahadasha: Boolean,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "expand_rotation"
    )

    val planetColor = ChartDetailColors.getPlanetColor(mahadasha.planet)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = planetColor.copy(alpha = 0.3f))
            ) { onToggleExpand(!isExpanded) },
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentMahadasha) {
            planetColor.copy(alpha = 0.08f)
        } else {
            ChartDetailColors.CardBackground
        },
        tonalElevation = if (isCurrentMahadasha) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 300))
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
                            .size(46.dp)
                            .background(planetColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mahadasha.planet.symbol,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${mahadasha.planet.displayName} Mahadasha",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChartDetailColors.TextPrimary
                            )
                            if (isCurrentMahadasha) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = planetColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = planetColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${formatDurationYears(mahadasha.durationYears)} • ${mahadasha.startDate.format(DATE_FORMATTER_FULL)} – ${mahadasha.endDate.format(DATE_FORMATTER_FULL)}",
                            fontSize = 11.sp,
                            color = ChartDetailColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse antardashas" else "Expand antardashas",
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200))
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
                            text = "Antardashas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartDetailColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ChartDetailColors.CardBackgroundElevated
                        ) {
                            Text(
                                text = "${mahadasha.antardashas.size} sub-periods",
                                fontSize = 10.sp,
                                color = ChartDetailColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    mahadasha.antardashas.forEach { antardasha ->
                        AntardashaRow(
                            antardasha = antardasha,
                            mahadashaPlanet = mahadasha.planet
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
    mahadashaPlanet: Planet
) {
    val planetColor = ChartDetailColors.getPlanetColor(antardasha.planet)
    val today = LocalDate.now()
    val isCurrent = !antardasha.startDate.isAfter(today) && !antardasha.endDate.isBefore(today)
    val isPast = antardasha.endDate.isBefore(today)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(
                color = when {
                    isCurrent -> planetColor.copy(alpha = 0.12f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
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
                Text(
                    text = "${mahadashaPlanet.symbol}–${antardasha.planet.displayName}",
                    fontSize = 13.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> planetColor
                        isPast -> ChartDetailColors.TextMuted
                        else -> ChartDetailColors.TextPrimary
                    }
                )
                if (isCurrent) {
                    val remaining = formatRemainingTime(today, antardasha.endDate)
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

        Text(
            text = "${antardasha.startDate.format(DATE_FORMATTER_MONTH_YEAR)} – ${antardasha.endDate.format(DATE_FORMATTER_MONTH_YEAR)}",
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun DashaInfoCard(
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "info_rotation"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple()
            ) { onToggleExpand(!isExpanded) },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 300))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ChartDetailColors.AccentPurple.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = ChartDetailColors.AccentPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "About Vimshottari Dasha",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "The Vimshottari Dasha is the most widely used planetary period system in Vedic astrology (Jyotish). Derived from the Moon's nakshatra (lunar mansion) at birth, it divides the 120-year human lifespan into major planetary periods (Mahadashas), each ruled by one of the nine Grahas.",
                        fontSize = 13.sp,
                        color = ChartDetailColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ChartDetailColors.CardBackgroundElevated
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Dasha Periods (Vimshottari Sequence)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChartDetailColors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    VIMSHOTTARI_SEQUENCE.take(5).forEach { (planet, years) ->
                                        DashaDurationRow(planet = planet, years = years)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    VIMSHOTTARI_SEQUENCE.drop(5).forEach { (planet, years) ->
                                        DashaDurationRow(planet = planet, years = years)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ChartDetailColors.DividerColor)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Total Cycle: 120 Years",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChartDetailColors.AccentGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Each Mahadasha is subdivided into Antardashas (sub-periods), Pratyantardashas (sub-sub-periods), and further divisions for precise timing of events.",
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
private fun DashaDurationRow(planet: Planet, years: Int) {
    val planetColor = ChartDetailColors.getPlanetColor(planet)

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = planet.displayName,
            fontSize = 12.sp,
            color = ChartDetailColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$years yrs",
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
    Planet.SUN -> "A period of heightened self-expression, authority, and recognition. " +
            "Focus turns to career advancement, leadership roles, government dealings, and matters related to father. " +
            "Soul purpose becomes clearer. Health of heart and vitality gains prominence. " +
            "Good for developing confidence and establishing one's identity in the world."

    Planet.MOON -> "An emotionally rich and intuitive period emphasizing mental peace, nurturing, and receptivity. " +
            "Focus on mother, home life, public image, travel across water, and emotional well-being. " +
            "Creativity and imagination flourish. Memory and connection to the past strengthen. " +
            "Relationships with women and the public become significant."

    Planet.MARS -> "A period of heightened energy, courage, initiative, and competitive drive. " +
            "Focus on property matters, real estate, siblings, technical and engineering pursuits, sports, and surgery. " +
            "Decisive action is favored. Physical vitality increases. " +
            "Good for tackling challenges requiring strength and determination."

    Planet.MERCURY -> "A period of enhanced learning, communication, analytical thinking, and commerce. " +
            "Focus on education, writing, publishing, accounting, trade, and intellectual pursuits. " +
            "Social connections expand through skillful communication. " +
            "Good for developing skills, starting businesses, and mastering information."

    Planet.JUPITER -> "A period of wisdom, expansion, prosperity, and divine grace (Guru's blessings). " +
            "Focus on spirituality, higher learning, teaching, children, law, and philosophical pursuits. " +
            "Fortune favors righteous endeavors. Faith and optimism increase. " +
            "Excellent for marriage, progeny, and spiritual advancement."

    Planet.VENUS -> "A period of luxury, beauty, relationships, artistic expression, and material comforts. " +
            "Focus on marriage, partnerships, arts, music, dance, vehicles, jewelry, and sensory pleasures. " +
            "Creativity and romance blossom. Refinement in all areas of life. " +
            "Good for enhancing beauty, wealth, and experiencing life's pleasures."

    Planet.SATURN -> "A period of discipline, karmic lessons, perseverance, and structural growth. " +
            "Focus on service, responsibility, hard work, long-term projects, and lessons through patience. " +
            "Delays and obstacles ultimately lead to lasting success and maturity. " +
            "Time to build solid foundations and pay karmic debts."

    Planet.RAHU -> "A period of intense worldly ambition, unconventional paths, and material desires. " +
            "Focus on foreign connections, technology, innovation, and breaking traditional boundaries. " +
            "Sudden opportunities and unexpected changes arise. " +
            "Material gains through unusual or non-traditional means. Beware of illusions."

    Planet.KETU -> "A period of spirituality, detachment, and profound inner transformation. " +
            "Focus on liberation (moksha), occult research, healing practices, and resolving past-life karma. " +
            "Deep introspection yields spiritual insights. Material attachments may dissolve. " +
            "Excellent for meditation, research, and spiritual practices."

    else -> "A period of transformation and karmic unfolding according to planetary influences."
}

private fun getAntardashaInterpretation(planet: Planet): String = when (planet) {
    Planet.SUN -> "Current sub-period (Bhukti) activates themes of authority, self-confidence, recognition, " +
            "and dealings with father figures or government. Leadership opportunities may arise."

    Planet.MOON -> "Current sub-period emphasizes emotional matters, mental peace, mother, public image, " +
            "domestic affairs, and connection with women. Intuition heightens."

    Planet.MARS -> "Current sub-period brings increased energy, drive for action, courage, " +
            "and matters involving property, siblings, competition, or technical endeavors."

    Planet.MERCURY -> "Current sub-period emphasizes communication, learning, business transactions, " +
            "intellectual activities, and connections with younger people or merchants."

    Planet.JUPITER -> "Current sub-period brings wisdom, expansion, good fortune, and focus on " +
            "spirituality, teachers, children, higher education, or legal matters."

    Planet.VENUS -> "Current sub-period emphasizes relationships, romance, creativity, luxury, " +
            "artistic pursuits, material comforts, and partnership matters."

    Planet.SATURN -> "Current sub-period brings discipline, responsibility, hard work, delays, " +
            "and lessons requiring patience. Focus on service and long-term efforts."

    Planet.RAHU -> "Current sub-period emphasizes worldly ambitions, unconventional approaches, " +
            "foreign matters, technology, and sudden changes or opportunities."

    Planet.KETU -> "Current sub-period brings spiritual insights, detachment, introspection, " +
            "research, and resolution of past karmic patterns. Material concerns recede."

    else -> "Current sub-period brings mixed planetary influences requiring careful navigation."
}
