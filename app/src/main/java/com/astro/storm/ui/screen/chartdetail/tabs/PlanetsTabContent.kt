package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.RetrogradeCombustionCalculator
import com.astro.storm.ephemeris.ShadbalaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import com.astro.storm.ui.screen.chartdetail.ChartDetailUtils
import com.astro.storm.ui.screen.chartdetail.components.ConditionChip
import com.astro.storm.ui.screen.chartdetail.components.SectionCard
import com.astro.storm.ui.screen.chartdetail.components.StyledDivider

/**
 * Planets tab content displaying detailed planetary information including
 * positions, conditions, and strength analysis.
 */
@Composable
fun PlanetsTabContent(
    chart: VedicChart,
    onPlanetClick: (PlanetPosition) -> Unit,
    onNakshatraClick: (Nakshatra, Int) -> Unit = { _, _ -> },
    onShadbalaClick: () -> Unit
) {
    val planetConditions = remember(chart) {
        RetrogradeCombustionCalculator.analyzePlanetaryConditions(chart)
    }

    val shadbala = remember(chart) {
        ShadbalaCalculator.calculateShadbala(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PlanetaryConditionsSummary(planetConditions)
        }

        item {
            ShadbalaOverviewCard(
                shadbala = shadbala,
                onClick = onShadbalaClick
            )
        }

        items(chart.planetPositions) { position ->
            val planetShadbala = shadbala.planetaryStrengths[position.planet]
            val conditions = planetConditions.getCondition(position.planet)

            PlanetDetailCard(
                position = position,
                shadbala = planetShadbala,
                conditions = conditions,
                onClick = { onPlanetClick(position) },
                onNakshatraClick = { onNakshatraClick(position.nakshatra, position.nakshatraPada) }
            )
        }
    }
}

@Composable
private fun PlanetaryConditionsSummary(
    conditions: RetrogradeCombustionCalculator.PlanetaryConditionAnalysis
) {
    val retrogradeCount = conditions.retrogradePlanets.size
    val combustCount = conditions.combustPlanets.size
    val warCount = conditions.planetaryWars.size

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Planetary Conditions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConditionStatCard(
                    count = retrogradeCount,
                    label = "Retrograde",
                    color = ChartDetailColors.WarningColor
                )
                ConditionStatCard(
                    count = combustCount,
                    label = "Combust",
                    color = ChartDetailColors.ErrorColor
                )
                ConditionStatCard(
                    count = warCount,
                    label = "At War",
                    color = ChartDetailColors.AccentPurple
                )
            }
        }
    }
}

@Composable
private fun ConditionStatCard(
    count: Int,
    label: String,
    color: Color
) {
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
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun ShadbalaOverviewCard(
    shadbala: ShadbalaCalculator.ShadbalaAnalysis,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shadbala Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Details",
                        fontSize = 12.sp,
                        color = ChartDetailColors.AccentGold
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", shadbala.overallStrengthScore)}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.getStrengthColor(shadbala.overallStrengthScore)
                    )
                    Text(
                        text = "Overall",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = shadbala.strongestPlanet.symbol,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.SuccessColor
                    )
                    Text(
                        text = "Strongest",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = shadbala.weakestPlanet.symbol,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.ErrorColor
                    )
                    Text(
                        text = "Weakest",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanetDetailCard(
    position: PlanetPosition,
    shadbala: ShadbalaCalculator.PlanetaryShadbala?,
    conditions: RetrogradeCombustionCalculator.PlanetCondition?,
    onClick: () -> Unit,
    onNakshatraClick: () -> Unit
) {
    val planetColor = ChartDetailColors.getPlanetColor(position.planet)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
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
                            .background(planetColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = position.planet.symbol,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = position.planet.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartDetailColors.TextPrimary
                        )
                        Text(
                            text = "${position.sign.displayName} • House ${position.house}",
                            fontSize = 12.sp,
                            color = ChartDetailColors.TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ChartDetailUtils.formatDegreeInSign(position.longitude),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChartDetailColors.AccentTeal
                    )
                    // Nakshatra clickable
                    Surface(
                        modifier = Modifier.clickable { onNakshatraClick() },
                        shape = RoundedCornerShape(4.dp),
                        color = ChartDetailColors.CardBackgroundElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${position.nakshatra.displayName} ${position.nakshatraPada}",
                                fontSize = 11.sp,
                                color = ChartDetailColors.AccentPurple
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View nakshatra details",
                                tint = ChartDetailColors.AccentPurple,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (shadbala != null || conditions != null) {
                StyledDivider(verticalPadding = 12.dp)
            }

            if (shadbala != null) {
                ShadbalaRow(shadbala = shadbala)
                Spacer(modifier = Modifier.height(8.dp))
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val dignity = getDignityStatus(position.planet, position.sign)
                if (dignity != "Neutral") {
                    ConditionChip(
                        label = dignity,
                        color = getDignityColor(dignity)
                    )
                }

                if (position.isRetrograde) {
                    ConditionChip(
                        label = "Retrograde",
                        color = ChartDetailColors.WarningColor
                    )
                }

                conditions?.let { cond ->
                    if (cond.combustionStatus != RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST) {
                        ConditionChip(
                            label = cond.combustionStatus.displayName,
                            color = ChartDetailColors.ErrorColor
                        )
                    }

                    if (cond.isInPlanetaryWar) {
                        ConditionChip(
                            label = "Planetary War",
                            color = ChartDetailColors.AccentPurple
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap for full details",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ShadbalaRow(shadbala: ShadbalaCalculator.PlanetaryShadbala) {
    val progress = (shadbala.percentageOfRequired / 150.0).coerceIn(0.0, 1.0).toFloat()
    val color = ChartDetailColors.getStrengthColor(shadbala.percentageOfRequired)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shadbala",
                    fontSize = 12.sp,
                    color = ChartDetailColors.TextSecondary
                )
                Text(
                    text = shadbala.strengthRating.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = ChartDetailColors.DividerColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${String.format("%.2f", shadbala.totalRupas)} / ${String.format("%.2f", shadbala.requiredRupas)} rupas (${String.format("%.1f", shadbala.percentageOfRequired)}%)",
                fontSize = 10.sp,
                color = ChartDetailColors.TextMuted
            )
        }
    }
}

private fun getDignityStatus(planet: Planet, sign: ZodiacSign): String {
    val exalted = mapOf(
        Planet.SUN to ZodiacSign.ARIES,
        Planet.MOON to ZodiacSign.TAURUS,
        Planet.MARS to ZodiacSign.CAPRICORN,
        Planet.MERCURY to ZodiacSign.VIRGO,
        Planet.JUPITER to ZodiacSign.CANCER,
        Planet.VENUS to ZodiacSign.PISCES,
        Planet.SATURN to ZodiacSign.LIBRA
    )

    val debilitated = mapOf(
        Planet.SUN to ZodiacSign.LIBRA,
        Planet.MOON to ZodiacSign.SCORPIO,
        Planet.MARS to ZodiacSign.CANCER,
        Planet.MERCURY to ZodiacSign.PISCES,
        Planet.JUPITER to ZodiacSign.CAPRICORN,
        Planet.VENUS to ZodiacSign.VIRGO,
        Planet.SATURN to ZodiacSign.ARIES
    )

    return when {
        exalted[planet] == sign -> "Exalted"
        debilitated[planet] == sign -> "Debilitated"
        sign.ruler == planet -> "Own Sign"
        else -> "Neutral"
    }
}

private fun getDignityColor(dignity: String): Color = when (dignity) {
    "Exalted" -> ChartDetailColors.SuccessColor
    "Debilitated" -> ChartDetailColors.ErrorColor
    "Own Sign" -> ChartDetailColors.AccentGold
    else -> ChartDetailColors.TextSecondary
}
