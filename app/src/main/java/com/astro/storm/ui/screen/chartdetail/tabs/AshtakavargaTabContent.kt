package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.AshtakavargaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors

/**
 * Ashtakavarga tab content displaying SAV and BAV analysis with interpretation.
 */
@Composable
fun AshtakavargaTabContent(chart: VedicChart) {
    val ashtakavarga = remember(chart) {
        AshtakavargaCalculator.calculateAshtakavarga(chart)
    }

    val expandedCardTitles = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AshtakavargaSummaryCard(ashtakavarga)
        }

        item {
            SarvashtakavargaCard(
                ashtakavarga = ashtakavarga,
                isExpanded = "Sarvashtakavarga" in expandedCardTitles,
                onToggleExpand = {
                    if (it) {
                        expandedCardTitles.add("Sarvashtakavarga")
                    } else {
                        expandedCardTitles.remove("Sarvashtakavarga")
                    }
                }
            )
        }

        item {
            BhinnashtakavargaCard(
                ashtakavarga = ashtakavarga,
                isExpanded = "Bhinnashtakavarga" in expandedCardTitles,
                onToggleExpand = {
                    if (it) {
                        expandedCardTitles.add("Bhinnashtakavarga")
                    } else {
                        expandedCardTitles.remove("Bhinnashtakavarga")
                    }
                }
            )
        }

        item {
            InterpretationGuideCard(
                isExpanded = "InterpretationGuide" in expandedCardTitles,
                onToggleExpand = {
                    if (it) {
                        expandedCardTitles.add("InterpretationGuide")
                    } else {
                        expandedCardTitles.remove("InterpretationGuide")
                    }
                }
            )
        }
    }
}

@Composable
private fun AshtakavargaSummaryCard(ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.GridOn,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(StringKey.ASHTAK_SUMMARY),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = stringResource(StringKey.ASHTAK_TOTAL_SAV),
                    value = ashtakavarga.sarvashtakavarga.totalBindus.toString(),
                    color = ChartDetailColors.AccentGold
                )
                SummaryItem(
                    label = stringResource(StringKey.ASHTAK_STRONGEST),
                    value = ashtakavarga.sarvashtakavarga.strongestSign.abbreviation,
                    color = ChartDetailColors.SuccessColor
                )
                SummaryItem(
                    label = stringResource(StringKey.ASHTAK_WEAKEST),
                    value = ashtakavarga.sarvashtakavarga.weakestSign.abbreviation,
                    color = ChartDetailColors.ErrorColor
                )
            }

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = stringResource(StringKey.ASHTAK_QUICK_ANALYSIS),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val strongSignCount = ZodiacSign.entries.count {
                ashtakavarga.sarvashtakavarga.getBindusForSign(it) >= 28
            }
            val weakSignCount = ZodiacSign.entries.count {
                ashtakavarga.sarvashtakavarga.getBindusForSign(it) < 25
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(StringKey.ASHTAK_FAVORABLE_SIGNS),
                    fontSize = 13.sp,
                    color = ChartDetailColors.TextMuted
                )
                Text(
                    text = stringResource(StringKey.ASHTAK_SIGNS_COUNT, strongSignCount),
                    fontSize = 13.sp,
                    color = ChartDetailColors.SuccessColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(StringKey.ASHTAK_CHALLENGING_SIGNS),
                    fontSize = 13.sp,
                    color = ChartDetailColors.TextMuted
                )
                Text(
                    text = stringResource(StringKey.ASHTAK_SIGNS_COUNT, weakSignCount),
                    fontSize = 13.sp,
                    color = ChartDetailColors.WarningColor
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun SarvashtakavargaCard(
    ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(!isExpanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(StringKey.ASHTAK_SAV_TITLE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = stringResource(StringKey.ASHTAK_SAV_COMBINED_DESC),
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ZodiacSign.entries.forEach { sign ->
                            val bindus = ashtakavarga.sarvashtakavarga.getBindusForSign(sign)
                            SAVSignBox(
                                sign = sign,
                                bindus = bindus
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SAVLegend()
                }
            }
        }
    }
}

@Composable
private fun SAVSignBox(
    sign: ZodiacSign,
    bindus: Int
) {
    val backgroundColor = when {
        bindus >= 30 -> ChartDetailColors.SuccessColor.copy(alpha = 0.2f)
        bindus >= 28 -> ChartDetailColors.AccentTeal.copy(alpha = 0.15f)
        bindus < 25 -> ChartDetailColors.ErrorColor.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        bindus >= 30 -> ChartDetailColors.SuccessColor
        bindus >= 28 -> ChartDetailColors.AccentTeal
        bindus < 25 -> ChartDetailColors.ErrorColor
        else -> ChartDetailColors.TextPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = sign.abbreviation,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ChartDetailColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bindus.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun SAVLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = ChartDetailColors.SuccessColor,
            label = stringResource(StringKey.ASHTAK_SAV_EXCELLENT)
        )
        LegendItem(
            color = ChartDetailColors.AccentTeal,
            label = stringResource(StringKey.ASHTAK_SAV_GOOD)
        )
        LegendItem(
            color = ChartDetailColors.TextMuted,
            label = stringResource(StringKey.ASHTAK_SAV_AVERAGE)
        )
        LegendItem(
            color = ChartDetailColors.ErrorColor,
            label = stringResource(StringKey.ASHTAK_SAV_WEAK)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun BhinnashtakavargaCard(
    ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(!isExpanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.GridOn,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(StringKey.ASHTAK_BAV_TITLE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = stringResource(StringKey.ASHTAK_BAV_INDIVIDUAL_DESC),
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        BAVTable(ashtakavarga = ashtakavarga)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    BAVLegend()
                }
            }
        }
    }
}

@Composable
private fun BAVTable(ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis) {
    Column {
        Row {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(0.5.dp, ChartDetailColors.DividerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "",
                    fontSize = 10.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
            ZodiacSign.entries.forEach { sign ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(0.5.dp, ChartDetailColors.DividerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sign.abbreviation,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.AccentTeal
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(36.dp)
                    .border(0.5.dp, ChartDetailColors.DividerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(StringKey.ASHTAK_TOTAL),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.AccentGold
                )
            }
        }

        ashtakavarga.bhinnashtakavarga.forEach { (planet, bav) ->
            Row {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(0.5.dp, ChartDetailColors.DividerColor)
                        .background(ChartDetailColors.getPlanetColor(planet).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = planet.symbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.getPlanetColor(planet)
                    )
                }
                ZodiacSign.entries.forEach { sign ->
                    val bindus = bav.getBindusForSign(sign)
                    BAVCell(bindus = bindus)
                }
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(36.dp)
                        .border(0.5.dp, ChartDetailColors.DividerColor)
                        .background(ChartDetailColors.AccentGold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bav.totalBindus.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.AccentGold
                    )
                }
            }
        }
    }
}

@Composable
private fun BAVCell(bindus: Int) {
    val backgroundColor = when {
        bindus >= 5 -> ChartDetailColors.SuccessColor.copy(alpha = 0.15f)
        bindus >= 4 -> ChartDetailColors.AccentTeal.copy(alpha = 0.1f)
        bindus <= 2 -> ChartDetailColors.ErrorColor.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        bindus >= 5 -> ChartDetailColors.SuccessColor
        bindus >= 4 -> ChartDetailColors.AccentTeal
        bindus <= 2 -> ChartDetailColors.ErrorColor
        else -> ChartDetailColors.TextPrimary
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .border(0.5.dp, ChartDetailColors.DividerColor)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bindus.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun BAVLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = ChartDetailColors.SuccessColor,
            label = stringResource(StringKey.ASHTAK_BAV_STRONG)
        )
        LegendItem(
            color = ChartDetailColors.AccentTeal,
            label = stringResource(StringKey.ASHTAK_BAV_GOOD)
        )
        LegendItem(
            color = ChartDetailColors.TextMuted,
            label = stringResource(StringKey.ASHTAK_BAV_AVERAGE)
        )
        LegendItem(
            color = ChartDetailColors.ErrorColor,
            label = stringResource(StringKey.ASHTAK_BAV_WEAK)
        )
    }
}

@Composable
private fun InterpretationGuideCard(isExpanded: Boolean, onToggleExpand: (Boolean) -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(!isExpanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(StringKey.ASHTAK_GUIDE_TITLE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    GuideSection(
                        title = stringResource(StringKey.ASHTAK_SAV_TITLE),
                        points = listOf(
                            stringResource(StringKey.ASHTAK_GUIDE_SAV_30),
                            stringResource(StringKey.ASHTAK_GUIDE_SAV_28),
                            stringResource(StringKey.ASHTAK_GUIDE_SAV_25),
                            stringResource(StringKey.ASHTAK_GUIDE_SAV_BELOW)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GuideSection(
                        title = stringResource(StringKey.ASHTAK_BAV_TITLE),
                        points = listOf(
                            stringResource(StringKey.ASHTAK_GUIDE_BAV_5),
                            stringResource(StringKey.ASHTAK_GUIDE_BAV_4),
                            stringResource(StringKey.ASHTAK_GUIDE_BAV_3),
                            stringResource(StringKey.ASHTAK_GUIDE_BAV_02)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GuideSection(
                        title = stringResource(StringKey.ASHTAK_GUIDE_TRANSIT_TITLE),
                        points = listOf(
                            stringResource(StringKey.ASHTAK_GUIDE_TRANSIT_1),
                            stringResource(StringKey.ASHTAK_GUIDE_TRANSIT_2),
                            stringResource(StringKey.ASHTAK_GUIDE_TRANSIT_3),
                            stringResource(StringKey.ASHTAK_GUIDE_TRANSIT_4)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    points: List<String>
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ChartDetailColors.AccentTeal,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        points.forEach { point ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = ChartDetailColors.TextMuted,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = point,
                    fontSize = 12.sp,
                    color = ChartDetailColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
