package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.DivisionalChartCalculator
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ephemeris.DivisionalChartType
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import com.astro.storm.ui.screen.chartdetail.ChartDetailUtils
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChartTabContent(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    onChartClick: (String, DivisionalChartData?) -> Unit,
    onPlanetClick: (PlanetPosition) -> Unit,
    onHouseClick: (Int) -> Unit
) {
    val divisionalCharts = remember(chart) {
        DivisionalChartCalculator.calculateAllDivisionalCharts(chart)
    }

    var selectedChartType by rememberSaveable { mutableStateOf("D1") }
    val expandedCardTitles = remember { mutableStateListOf<String>() }

    val currentChartData = remember(selectedChartType, divisionalCharts) {
        getChartDataForType(selectedChartType, divisionalCharts)
    }

    val chartInfo = getLocalizedChartInfo(selectedChartType)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ChartTypeSelector(
                selectedType = selectedChartType,
                onTypeSelected = { selectedChartType = it }
            )
        }

        item {
            MainChartCard(
                chart = chart,
                chartRenderer = chartRenderer,
                chartInfo = chartInfo,
                selectedChartType = selectedChartType,
                currentChartData = currentChartData,
                onChartClick = onChartClick
            )
        }

        item {
            ChartDetailsCard(
                chart = chart,
                currentChartData = currentChartData,
                selectedChartType = selectedChartType,
                onPlanetClick = onPlanetClick
            )
        }

        item {
            HouseCuspsCard(
                chart = chart,
                onHouseClick = onHouseClick,
                isExpanded = "HouseCusps" in expandedCardTitles,
                onToggleExpand = {
                    if (it) {
                        expandedCardTitles.add("HouseCusps")
                    } else {
                        expandedCardTitles.remove("HouseCusps")
                    }
                }
            )
        }

        item {
            BirthDetailsCard(
                chart = chart,
                isExpanded = "BirthDetails" in expandedCardTitles,
                onToggleExpand = {
                    if (it) {
                        expandedCardTitles.add("BirthDetails")
                    } else {
                        expandedCardTitles.remove("BirthDetails")
                    }
                }
            )
        }

        item {
            AstronomicalDataCard(
                chart = chart,
                isExpanded = "AstronomicalData" in expandedCardTitles,
                onToggleExpand = {
                    if (it) {
                        expandedCardTitles.add("AstronomicalData")
                    } else {
                        expandedCardTitles.remove("AstronomicalData")
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun getChartDataForType(
    type: String,
    divisionalCharts: List<DivisionalChartData>
): DivisionalChartData? {
    return when (type) {
        "D1" -> null
        "D2" -> divisionalCharts.find { it.chartType == DivisionalChartType.D2_HORA }
        "D3" -> divisionalCharts.find { it.chartType == DivisionalChartType.D3_DREKKANA }
        "D4" -> divisionalCharts.find { it.chartType == DivisionalChartType.D4_CHATURTHAMSA }
        "D7" -> divisionalCharts.find { it.chartType == DivisionalChartType.D7_SAPTAMSA }
        "D9" -> divisionalCharts.find { it.chartType == DivisionalChartType.D9_NAVAMSA }
        "D10" -> divisionalCharts.find { it.chartType == DivisionalChartType.D10_DASAMSA }
        "D12" -> divisionalCharts.find { it.chartType == DivisionalChartType.D12_DWADASAMSA }
        "D16" -> divisionalCharts.find { it.chartType == DivisionalChartType.D16_SHODASAMSA }
        "D20" -> divisionalCharts.find { it.chartType == DivisionalChartType.D20_VIMSAMSA }
        "D24" -> divisionalCharts.find { it.chartType == DivisionalChartType.D24_CHATURVIMSAMSA }
        "D27" -> divisionalCharts.find { it.chartType == DivisionalChartType.D27_SAPTAVIMSAMSA }
        "D30" -> divisionalCharts.find { it.chartType == DivisionalChartType.D30_TRIMSAMSA }
        "D60" -> divisionalCharts.find { it.chartType == DivisionalChartType.D60_SHASHTIAMSA }
        else -> null
    }
}

/**
 * Get chart info with StringKey pairs for localization
 * Returns Triple of (nameKey, descriptionKey, code)
 */
private fun getChartInfoKeys(type: String): Triple<StringKey?, StringKey?, String> {
    return when (type) {
        "D1" -> Triple(StringKey.VARGA_D1_NAME, StringKey.VARGA_D1_DESC, "D1")
        "D2" -> Triple(StringKey.VARGA_D2_NAME, StringKey.VARGA_D2_DESC, "D2")
        "D3" -> Triple(StringKey.VARGA_D3_NAME, StringKey.VARGA_D3_DESC_FULL, "D3")
        "D4" -> Triple(StringKey.VARGA_D4_NAME, StringKey.VARGA_D4_DESC, "D4")
        "D7" -> Triple(StringKey.VARGA_D7_NAME, StringKey.VARGA_D7_DESC, "D7")
        "D9" -> Triple(StringKey.VARGA_D9_NAME, StringKey.VARGA_D9_DESC_FULL, "D9")
        "D10" -> Triple(StringKey.VARGA_D10_NAME, StringKey.VARGA_D10_DESC_FULL, "D10")
        "D12" -> Triple(StringKey.VARGA_D12_NAME, StringKey.VARGA_D12_DESC_FULL, "D12")
        "D16" -> Triple(StringKey.VARGA_D16_NAME, StringKey.VARGA_D16_DESC_FULL, "D16")
        "D20" -> Triple(StringKey.VARGA_D20_NAME, StringKey.VARGA_D20_DESC_FULL, "D20")
        "D24" -> Triple(StringKey.VARGA_D24_NAME, StringKey.VARGA_D24_DESC_FULL, "D24")
        "D27" -> Triple(StringKey.VARGA_D27_NAME, StringKey.VARGA_D27_DESC_FULL, "D27")
        "D30" -> Triple(StringKey.VARGA_D30_NAME, StringKey.VARGA_D30_DESC_FULL, "D30")
        "D60" -> Triple(StringKey.VARGA_D60_NAME, StringKey.VARGA_D60_DESC_FULL, "D60")
        else -> Triple(null, null, type)
    }
}

/**
 * Composable helper to get localized chart info
 */
@Composable
private fun getLocalizedChartInfo(type: String): Triple<String, String, String> {
    val keys = getChartInfoKeys(type)
    val name = keys.first?.let { stringResource(it) } ?: "Chart"
    val desc = keys.second?.let { stringResource(it) } ?: ""
    return Triple(name, desc, keys.third)
}

/**
 * Chart type selector chip data with localization support
 */
private data class ChartTypeChip(
    val code: String,
    val stringKey: StringKey
)

private val chartTypeChips = listOf(
    ChartTypeChip("D1", StringKey.VARGA_LAGNA),
    ChartTypeChip("D2", StringKey.VARGA_HORA),
    ChartTypeChip("D3", StringKey.VARGA_DREKKANA),
    ChartTypeChip("D4", StringKey.VARGA_D4_NAME),
    ChartTypeChip("D7", StringKey.VARGA_SAPTAMSA),
    ChartTypeChip("D9", StringKey.VARGA_NAVAMSA),
    ChartTypeChip("D10", StringKey.VARGA_DASAMSA),
    ChartTypeChip("D12", StringKey.VARGA_D12_NAME),
    ChartTypeChip("D16", StringKey.VARGA_D16_NAME),
    ChartTypeChip("D20", StringKey.VARGA_D20_NAME),
    ChartTypeChip("D24", StringKey.VARGA_D24_NAME),
    ChartTypeChip("D27", StringKey.VARGA_BHAMSA),
    ChartTypeChip("D30", StringKey.VARGA_D30_NAME),
    ChartTypeChip("D60", StringKey.VARGA_D60_NAME)
)

@Composable
private fun ChartTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chartTypeChips) { chip ->
            FilterChip(
                selected = selectedType == chip.code,
                onClick = { onTypeSelected(chip.code) },
                label = { Text(text = stringResource(chip.stringKey), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ChartDetailColors.AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = ChartDetailColors.AccentGold,
                    containerColor = ChartDetailColors.CardBackground,
                    labelColor = ChartDetailColors.TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = ChartDetailColors.DividerColor,
                    selectedBorderColor = ChartDetailColors.AccentGold,
                    enabled = true,
                    selected = selectedType == chip.code
                )
            )
        }
    }
}

@Composable
private fun MainChartCard(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    chartInfo: Triple<String, String, String>,
    selectedChartType: String,
    currentChartData: DivisionalChartData?,
    onChartClick: (String, DivisionalChartData?) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChartClick(chartInfo.first, currentChartData) },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        color = ChartDetailColors.AccentGold
                    )
                    if (chartInfo.second.isNotEmpty()) {
                        Text(
                            text = chartInfo.second,
                            fontSize = 12.sp,
                            color = ChartDetailColors.TextMuted
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "View fullscreen",
                        tint = ChartDetailColors.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ChartDetailColors.AccentGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = chartInfo.third,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChartDetailColors.AccentGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))
            ChartLegend()

            Text(
                text = stringResource(StringKey.CHART_TAP_FULLSCREEN),
                fontSize = 11.sp,
                color = ChartDetailColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ChartLegend() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = ChartDetailColors.ChartBackground
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextLegendItem(symbol = "*", label = stringResource(StringKey.CHART_LEGEND_RETRO), color = ChartDetailColors.AccentGold)
                TextLegendItem(symbol = "^", label = stringResource(StringKey.CHART_LEGEND_COMBUST), color = ChartDetailColors.AccentGold)
                TextLegendItem(symbol = "\u00A4", label = stringResource(StringKey.CHART_LEGEND_VARGOTTAMA), color = ChartDetailColors.AccentGold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ArrowLegendItem(isExalted = true, label = stringResource(StringKey.CHART_LEGEND_EXALTED))
                ArrowLegendItem(isExalted = false, label = stringResource(StringKey.CHART_LEGEND_DEBILITATED))
                ShapeLegendItem(isOwnSign = true, label = stringResource(StringKey.CHART_LEGEND_OWN_SIGN))
                ShapeLegendItem(isOwnSign = false, label = stringResource(StringKey.CHART_LEGEND_MOOL_TRI))
            }
        }
    }
}

@Composable
private fun TextLegendItem(
    symbol: String,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun BirthDetailsCard(
    chart: VedicChart,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    val birthData = chart.birthData
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()) }

    val formattedDate = remember(birthData.dateTime) {
        try { birthData.dateTime.format(dateFormatter) } catch (e: Exception) { "N/A" }
    }
    val formattedTime = remember(birthData.dateTime) {
        try { birthData.dateTime.format(timeFormatter) } catch (e: Exception) { "N/A" }
    }
    val formattedLocation = remember(birthData.location, birthData.latitude, birthData.longitude) {
        birthData.location.takeIf { it.isNotBlank() }
            ?: "${String.format(Locale.US, "%.3f", birthData.latitude)} / ${String.format(Locale.US, "%.3f", birthData.longitude)}"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(StringKey.CHART_BIRTH_DETAILS),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = ChartDetailColors.TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BirthDataItem(
                                icon = Icons.Outlined.CalendarMonth,
                                label = stringResource(StringKey.CHART_DATE),
                                value = formattedDate
                            )
                            BirthDataItem(
                                icon = Icons.Outlined.LocationOn,
                                label = stringResource(StringKey.LOCATION),
                                value = formattedLocation
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BirthDataItem(
                                icon = Icons.Outlined.Schedule,
                                label = stringResource(StringKey.CHART_TIME),
                                value = formattedTime
                            )
                            BirthDataItem(
                                icon = Icons.Outlined.Star,
                                label = stringResource(StringKey.CHART_AYANAMSA),
                                value = chart.ayanamsaName
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BirthDataItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = ChartDetailColors.TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = ChartDetailColors.TextMuted,
                lineHeight = 12.sp
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ArrowLegendItem(
    isExalted: Boolean,
    label: String
) {
    val color = if (isExalted) Color(0xFF1E8449) else Color(0xFFC0392B)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val arrowSize = size.minDimension * 0.9f
            val centerX = size.width / 2
            val centerY = size.height / 2

            val path = Path().apply {
                if (isExalted) {
                    moveTo(centerX, centerY - arrowSize * 0.45f)
                    lineTo(centerX - arrowSize * 0.35f, centerY + arrowSize * 0.1f)
                    lineTo(centerX - arrowSize * 0.1f, centerY + arrowSize * 0.1f)
                    lineTo(centerX - arrowSize * 0.1f, centerY + arrowSize * 0.45f)
                    lineTo(centerX + arrowSize * 0.1f, centerY + arrowSize * 0.45f)
                    lineTo(centerX + arrowSize * 0.1f, centerY + arrowSize * 0.1f)
                    lineTo(centerX + arrowSize * 0.35f, centerY + arrowSize * 0.1f)
                } else {
                    moveTo(centerX, centerY + arrowSize * 0.45f)
                    lineTo(centerX - arrowSize * 0.35f, centerY - arrowSize * 0.1f)
                    lineTo(centerX - arrowSize * 0.1f, centerY - arrowSize * 0.1f)
                    lineTo(centerX - arrowSize * 0.1f, centerY - arrowSize * 0.45f)
                    lineTo(centerX + arrowSize * 0.1f, centerY - arrowSize * 0.45f)
                    lineTo(centerX + arrowSize * 0.1f, centerY - arrowSize * 0.1f)
                    lineTo(centerX + arrowSize * 0.35f, centerY - arrowSize * 0.1f)
                }
                close()
            }
            drawPath(path = path, color = color)
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun ShapeLegendItem(
    isOwnSign: Boolean,
    label: String
) {
    val color = if (isOwnSign) Color(0xFF2874A6) else Color(0xFF6C3483)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val shapeSize = size.minDimension * 0.85f
            val centerX = size.width / 2
            val centerY = size.height / 2

            val path = Path().apply {
                if (isOwnSign) {
                    moveTo(centerX - shapeSize * 0.4f, centerY + shapeSize * 0.35f)
                    lineTo(centerX - shapeSize * 0.4f, centerY - shapeSize * 0.15f)
                    lineTo(centerX - shapeSize * 0.2f, centerY - shapeSize * 0.35f)
                    lineTo(centerX, centerY - shapeSize * 0.45f)
                    lineTo(centerX + shapeSize * 0.2f, centerY - shapeSize * 0.35f)
                    lineTo(centerX + shapeSize * 0.4f, centerY - shapeSize * 0.15f)
                    lineTo(centerX + shapeSize * 0.4f, centerY + shapeSize * 0.35f)
                } else {
                    moveTo(centerX, centerY - shapeSize * 0.4f)
                    lineTo(centerX + shapeSize * 0.4f, centerY + shapeSize * 0.35f)
                    lineTo(centerX - shapeSize * 0.4f, centerY + shapeSize * 0.35f)
                }
                close()
            }
            drawPath(path = path, color = color)
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = ChartDetailColors.TextMuted
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
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(StringKey.CHART_PLANETARY_POSITIONS),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(StringKey.CHART_TAP_FOR_DETAILS),
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }

            if (selectedChartType == "D1") {
                AscendantRow(chart = chart)
                HorizontalDivider(
                    color = ChartDetailColors.DividerColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

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
private fun AscendantRow(chart: VedicChart) {
    val ascSign = ZodiacSign.fromLongitude(chart.ascendant)
    val ascDegree = chart.ascendant % 30.0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = ChartDetailColors.AccentGold.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(StringKey.CHART_ASCENDANT_LAGNA),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ChartDetailColors.AccentGold
            )
            Row {
                Text(
                    text = ascSign.displayName,
                    fontSize = 13.sp,
                    color = ChartDetailColors.AccentTeal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${ascDegree.toInt()}°",
                    fontSize = 13.sp,
                    color = ChartDetailColors.TextSecondary
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
    val color = ChartDetailColors.getPlanetColor(position.planet)

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
                        color = ChartDetailColors.WarningColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "R",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChartDetailColors.WarningColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Text(
                text = position.sign.displayName,
                fontSize = 13.sp,
                color = ChartDetailColors.AccentTeal,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${(position.longitude % 30.0).toInt()}°",
                fontSize = 13.sp,
                color = ChartDetailColors.TextSecondary,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "H${position.house}",
                fontSize = 12.sp,
                color = ChartDetailColors.TextMuted,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = ChartDetailColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HouseCuspsCard(
    chart: VedicChart,
    onHouseClick: (Int) -> Unit,
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(!isExpanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(StringKey.CHART_HOUSE_CUSPS),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) stringResource(StringKey.CHART_TAP_HOUSE_FOR_DETAILS) else stringResource(StringKey.CHART_TAP_TO_EXPAND),
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = ChartDetailColors.TextMuted,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
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
    val sign = ZodiacSign.fromLongitude(cusp)
    val degreeInSign = cusp % 30.0

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = ChartDetailColors.CardBackgroundElevated
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
                color = ChartDetailColors.AccentGold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = sign.abbreviation,
                    fontSize = 12.sp,
                    color = ChartDetailColors.AccentTeal
                )
                Text(
                    text = "${degreeInSign.toInt()}°",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun AstronomicalDataCard(
    chart: VedicChart,
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
        Column(modifier = Modifier.padding(16.dp)) {
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
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(StringKey.CHART_ASTRONOMICAL_DATA),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "" else stringResource(StringKey.CHART_TAP_TO_EXPAND),
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = ChartDetailColors.TextMuted,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    InfoRow(stringResource(StringKey.CHART_JULIAN_DAY), String.format("%.6f", chart.julianDay))
                    InfoRow(stringResource(StringKey.CHART_AYANAMSA), "${chart.ayanamsaName} (${ChartDetailUtils.formatDegree(chart.ayanamsa)})")
                    InfoRow(stringResource(StringKey.ASCENDANT), ChartDetailUtils.formatDegree(chart.ascendant))
                    InfoRow(stringResource(StringKey.CHART_MIDHEAVEN), ChartDetailUtils.formatDegree(chart.midheaven))
                    InfoRow(stringResource(StringKey.CHART_HOUSE_SYSTEM), chart.houseSystem.displayName)
                }
            }
        }
    }
}

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
            color = ChartDetailColors.TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = ChartDetailColors.TextPrimary
        )
    }
}