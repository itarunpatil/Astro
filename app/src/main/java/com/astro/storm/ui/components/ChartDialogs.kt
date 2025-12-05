package com.astro.storm.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.astro.storm.data.model.*
import com.astro.storm.ephemeris.*
import com.astro.storm.ui.chart.ChartRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

// Color palette for dialogs
private val DialogBackground = Color(0xFF1A1A1A)
private val DialogSurface = Color(0xFF252525)
private val DialogSurfaceElevated = Color(0xFF2D2D2D)
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

// Planet colors (including outer planets for complete coverage)
private val planetColors = mapOf(
    Planet.SUN to Color(0xFFD2691E),
    Planet.MOON to Color(0xFFDC143C),
    Planet.MARS to Color(0xFFDC143C),
    Planet.MERCURY to Color(0xFF228B22),
    Planet.JUPITER to Color(0xFFDAA520),
    Planet.VENUS to Color(0xFF9370DB),
    Planet.SATURN to Color(0xFF4169E1),
    Planet.RAHU to Color(0xFF8B0000),
    Planet.KETU to Color(0xFF8B0000),
    Planet.URANUS to Color(0xFF20B2AA),
    Planet.NEPTUNE to Color(0xFF4682B4),
    Planet.PLUTO to Color(0xFF800080)
)

/**
 * Full-screen chart dialog with zoom, pan, and download functionality
 */
@Composable
fun FullScreenChartDialog(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    chartTitle: String,
    divisionalChartData: DivisionalChartData? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadSuccess by remember { mutableStateOf<Boolean?>(null) }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DialogBackground)
        ) {
            // Chart canvas with zoom/pan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(
                        state = rememberTransformableState { zoomChange, panChange, _ ->
                            scale = (scale * zoomChange).coerceIn(0.5f, 3f)
                            offsetX += panChange.x
                            offsetY += panChange.y
                        }
                    )
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp)
                ) {
                    if (divisionalChartData != null) {
                        // Pass original chart for vargottama and combust status checking
                        chartRenderer.drawDivisionalChart(
                            drawScope = this,
                            planetPositions = divisionalChartData.planetPositions,
                            ascendantLongitude = divisionalChartData.ascendantLongitude,
                            size = size.minDimension,
                            chartTitle = chartTitle,
                            originalChart = chart
                        )
                    } else {
                        chartRenderer.drawNorthIndianChart(
                            drawScope = this,
                            chart = chart,
                            size = size.minDimension,
                            chartTitle = chartTitle
                        )
                    }
                }
            }

            // Top bar with title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DialogBackground.copy(alpha = 0.9f))
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chartTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary
                    )
                }
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DialogBackground.copy(alpha = 0.9f))
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset zoom button
                ActionButton(
                    icon = Icons.Default.CenterFocusStrong,
                    label = "Reset",
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )

                // Zoom in button
                ActionButton(
                    icon = Icons.Default.ZoomIn,
                    label = "Zoom In",
                    onClick = { scale = (scale * 1.2f).coerceAtMost(3f) }
                )

                // Zoom out button
                ActionButton(
                    icon = Icons.Default.ZoomOut,
                    label = "Zoom Out",
                    onClick = { scale = (scale / 1.2f).coerceAtLeast(0.5f) }
                )

                // Download button
                ActionButton(
                    icon = if (isDownloading) Icons.Default.HourglassEmpty else Icons.Default.Download,
                    label = if (isDownloading) "Saving..." else "Download",
                    onClick = {
                        if (!isDownloading) {
                            isDownloading = true
                            scope.launch {
                                val success = saveChartToGallery(
                                    context = context,
                                    chartRenderer = chartRenderer,
                                    chart = chart,
                                    divisionalChartData = divisionalChartData,
                                    chartTitle = chartTitle,
                                    density = density
                                )
                                downloadSuccess = success
                                isDownloading = false

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        if (success) "Chart saved to gallery!" else "Failed to save chart",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = !isDownloading
                )
            }

            // Zoom indicator
            AnimatedVisibility(
                visible = scale != 1f,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 72.dp, end = 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DialogSurface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (enabled) AccentGold else TextMuted,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) TextSecondary else TextMuted
        )
    }
}

/**
 * Save chart to device gallery
 */
private suspend fun saveChartToGallery(
    context: Context,
    chartRenderer: ChartRenderer,
    chart: VedicChart,
    divisionalChartData: DivisionalChartData?,
    chartTitle: String,
    density: androidx.compose.ui.unit.Density
): Boolean = withContext(Dispatchers.IO) {
    try {
        // Create high-resolution bitmap
        val size = 2048
        val bitmap = if (divisionalChartData != null) {
            chartRenderer.createDivisionalChartBitmap(
                planetPositions = divisionalChartData.planetPositions,
                ascendantLongitude = divisionalChartData.ascendantLongitude,
                chartTitle = chartTitle,
                width = size,
                height = size,
                density = density
            )
        } else {
            chartRenderer.createChartBitmap(chart, size, size, density)
        }

        // Save to gallery
        val filename = "AstroStorm_${chartTitle.replace(" ", "_")}_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AstroStorm")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        } else {
            val directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/AstroStorm"
            )
            if (!directory.exists()) directory.mkdirs()

            val file = java.io.File(directory, filename)
            java.io.FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            // Notify gallery
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, file.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }

        bitmap.recycle()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

/**
 * Planet detail dialog with comprehensive information and predictions
 */
@Composable
fun PlanetDetailDialog(
    planetPosition: PlanetPosition,
    chart: VedicChart,
    onDismiss: () -> Unit
) {
    val planet = planetPosition.planet
    val shadbala = remember(chart) {
        ShadbalaCalculator.calculatePlanetShadbala(planetPosition, chart)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = DialogBackground
        ) {
            Column {
                // Header
                PlanetDialogHeader(planetPosition, onDismiss)

                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Position Details
                    item {
                        PlanetPositionCard(planetPosition)
                    }

                    // Strength Analysis (Shadbala)
                    item {
                        ShadbalaCard(shadbala)
                    }

                    // Significations
                    item {
                        SignificationsCard(planet)
                    }

                    // House Placement Interpretation
                    item {
                        HousePlacementCard(planetPosition)
                    }

                    // Status & Conditions
                    item {
                        PlanetStatusCard(planetPosition, chart)
                    }

                    // Predictions & Insights
                    item {
                        PredictionsCard(planetPosition, shadbala, chart)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanetDialogHeader(
    planetPosition: PlanetPosition,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DialogSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            planetColors[planetPosition.planet] ?: AccentGold,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = planetPosition.planet.symbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = planetPosition.planet.displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${planetPosition.sign.displayName} • House ${planetPosition.house}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }
    }
}

@Composable
private fun PlanetPositionCard(position: PlanetPosition) {
    DialogCard(title = "Position Details", icon = Icons.Outlined.LocationOn) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailRow("Zodiac Sign", position.sign.displayName, AccentTeal)
            DetailRow("Degree", formatDegree(position.longitude), TextPrimary)
            DetailRow("House", "House ${position.house}", AccentGold)
            DetailRow("Nakshatra", "${position.nakshatra.displayName} (Pada ${position.nakshatraPada})", AccentPurple)
            DetailRow("Nakshatra Lord", position.nakshatra.ruler.displayName, TextSecondary)
            DetailRow("Nakshatra Deity", position.nakshatra.deity, TextSecondary)
            if (position.isRetrograde) {
                DetailRow("Motion", "Retrograde", AccentOrange)
            }
        }
    }
}

@Composable
private fun ShadbalaCard(shadbala: ShadbalaCalculator.PlanetaryShadbala) {
    DialogCard(title = "Strength Analysis (Shadbala)", icon = Icons.Outlined.TrendingUp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Overall strength bar
            val strengthPercentage = (shadbala.percentageOfRequired / 150.0).coerceIn(0.0, 1.0).toFloat()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Overall: ${String.format("%.2f", shadbala.totalRupas)} / ${String.format("%.2f", shadbala.requiredRupas)} Rupas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = shadbala.strengthRating.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            shadbala.percentageOfRequired >= 100 -> AccentGreen
                            shadbala.percentageOfRequired >= 85 -> AccentOrange
                            else -> AccentRose
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { strengthPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        shadbala.percentageOfRequired >= 100 -> AccentGreen
                        shadbala.percentageOfRequired >= 85 -> AccentOrange
                        else -> AccentRose
                    },
                    trackColor = DividerColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%.1f", shadbala.percentageOfRequired)}% of required strength",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            HorizontalDivider(color = DividerColor)

            // Breakdown
            Text("Strength Breakdown (Virupas)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)

            StrengthRow("Sthana Bala (Positional)", shadbala.sthanaBala.total, 180.0)
            StrengthRow("Dig Bala (Directional)", shadbala.digBala, 60.0)
            StrengthRow("Kala Bala (Temporal)", shadbala.kalaBala.total, 180.0)
            StrengthRow("Chesta Bala (Motional)", shadbala.chestaBala, 60.0)
            StrengthRow("Naisargika Bala (Natural)", shadbala.naisargikaBala, 60.0)
            StrengthRow("Drik Bala (Aspectual)", shadbala.drikBala, 60.0)
        }
    }
}

@Composable
private fun StrengthRow(label: String, value: Double, maxValue: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { (value / maxValue).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AccentTeal,
                trackColor = DividerColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", value),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun SignificationsCard(planet: Planet) {
    val significations = getPlanetSignifications(planet)

    DialogCard(title = "Significations & Nature", icon = Icons.Outlined.Info) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Nature
            DetailRow("Nature", significations.nature, when (significations.nature) {
                "Benefic" -> AccentGreen
                "Malefic" -> AccentRose
                else -> AccentOrange
            })

            // Element
            DetailRow("Element", significations.element, TextSecondary)

            // Represents
            Text("Represents:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            significations.represents.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AccentGold, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, fontSize = 13.sp, color = TextPrimary)
                }
            }

            // Body Parts
            Text("Body Parts:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Text(text = significations.bodyParts, fontSize = 13.sp, color = TextPrimary)

            // Professions
            Text("Professions:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Text(text = significations.professions, fontSize = 13.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun HousePlacementCard(position: PlanetPosition) {
    val interpretation = getHousePlacementInterpretation(position.planet, position.house)

    DialogCard(title = "House ${position.house} Placement", icon = Icons.Outlined.Home) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = interpretation.houseName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentGold
            )
            Text(
                text = interpretation.houseSignification,
                fontSize = 13.sp,
                color = TextSecondary
            )
            HorizontalDivider(color = DividerColor)
            Text(
                text = interpretation.interpretation,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun PlanetStatusCard(position: PlanetPosition, chart: VedicChart) {
    val conditions = remember(chart) {
        RetrogradeCombustionCalculator.analyzePlanetaryConditions(chart)
    }
    val planetCondition = conditions.getCondition(position.planet)

    DialogCard(title = "Status & Conditions", icon = Icons.Outlined.FactCheck) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dignity
            val dignity = getDignity(position.planet, position.sign)
            StatusChip(
                label = "Dignity",
                value = dignity.status,
                color = dignity.color
            )

            // Retrograde
            if (position.isRetrograde) {
                StatusChip(
                    label = "Motion",
                    value = "Retrograde",
                    color = AccentOrange
                )
            }

            // Combustion
            planetCondition?.let { cond ->
                if (cond.combustionStatus != RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST) {
                    StatusChip(
                        label = "Combustion",
                        value = cond.combustionStatus.displayName,
                        color = AccentRose
                    )
                }

                // Planetary War
                if (cond.isInPlanetaryWar) {
                    StatusChip(
                        label = "Planetary War",
                        value = "At war with ${cond.warData?.loser?.displayName}",
                        color = AccentPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionsCard(
    position: PlanetPosition,
    shadbala: ShadbalaCalculator.PlanetaryShadbala,
    chart: VedicChart
) {
    val predictions = getPlanetPredictions(position, shadbala, chart)

    DialogCard(title = "Insights & Predictions", icon = Icons.Outlined.AutoAwesome) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            predictions.forEach { prediction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        when (prediction.type) {
                            PredictionType.POSITIVE -> Icons.Default.CheckCircle
                            PredictionType.NEGATIVE -> Icons.Default.Warning
                            PredictionType.NEUTRAL -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (prediction.type) {
                            PredictionType.POSITIVE -> AccentGreen
                            PredictionType.NEGATIVE -> AccentOrange
                            PredictionType.NEUTRAL -> AccentBlue
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = prediction.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = prediction.description,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DialogCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DialogSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

/**
 * Nakshatra detail dialog
 */
@Composable
fun NakshatraDetailDialog(
    nakshatra: Nakshatra,
    pada: Int,
    onDismiss: () -> Unit
) {
    val details = getNakshatraDetails(nakshatra)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = DialogBackground
        ) {
            Column {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DialogSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = nakshatra.displayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pada $pada • ${nakshatra.ruler.displayName} ruled",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DialogCard(title = "Basic Information", icon = Icons.Outlined.Info) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailRow("Number", "${nakshatra.number} of 27", TextPrimary)
                                DetailRow("Degree Range", "${String.format("%.2f", nakshatra.startDegree)}° - ${String.format("%.2f", nakshatra.endDegree)}°", AccentTeal)
                                DetailRow("Ruling Planet", nakshatra.ruler.displayName, AccentGold)
                                DetailRow("Deity", nakshatra.deity, AccentPurple)
                            }
                        }
                    }

                    item {
                        DialogCard(title = "Nakshatra Nature", icon = Icons.Outlined.Psychology) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailRow("Symbol", details.symbol, TextPrimary)
                                DetailRow("Nature", details.nature, when(details.nature) {
                                    "Fixed (Dhruva)" -> AccentBlue
                                    "Movable (Chara)" -> AccentGreen
                                    "Sharp (Tikshna)" -> AccentRose
                                    else -> TextSecondary
                                })
                                DetailRow("Gender", details.gender, TextSecondary)
                                DetailRow("Gana", details.gana, TextSecondary)
                                DetailRow("Guna", details.guna, TextSecondary)
                                DetailRow("Element", details.element, TextSecondary)
                            }
                        }
                    }

                    item {
                        DialogCard(title = "Pada ${pada} Characteristics", icon = Icons.Outlined.Star) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val padaSign = when(pada) {
                                    1 -> nakshatra.pada1Sign
                                    2 -> nakshatra.pada2Sign
                                    3 -> nakshatra.pada3Sign
                                    4 -> nakshatra.pada4Sign
                                    else -> nakshatra.pada1Sign
                                }
                                DetailRow("Navamsa Sign", padaSign.displayName, AccentTeal)
                                Text(
                                    text = getPadaDescription(nakshatra, pada),
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    item {
                        DialogCard(title = "General Characteristics", icon = Icons.Outlined.Description) {
                            Text(
                                text = details.characteristics,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    item {
                        DialogCard(title = "Career Indications", icon = Icons.Outlined.Work) {
                            Text(
                                text = details.careers,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * House detail dialog
 */
@Composable
fun HouseDetailDialog(
    houseNumber: Int,
    houseCusp: Double,
    planetsInHouse: List<PlanetPosition>,
    chart: VedicChart,
    onDismiss: () -> Unit
) {
    val houseDetails = getHouseDetails(houseNumber)
    val sign = ZodiacSign.fromLongitude(houseCusp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = DialogBackground
        ) {
            Column {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DialogSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "House $houseNumber",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = houseDetails.name,
                                fontSize = 14.sp,
                                color = AccentGold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DialogCard(title = "House Information", icon = Icons.Outlined.Home) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailRow("Sign", sign.displayName, AccentTeal)
                                DetailRow("Cusp Degree", formatDegree(houseCusp), TextPrimary)
                                DetailRow("Sign Lord", sign.ruler.displayName, AccentGold)
                                DetailRow("House Type", houseDetails.type, TextSecondary)
                            }
                        }
                    }

                    item {
                        DialogCard(title = "Significations", icon = Icons.Outlined.ListAlt) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                houseDetails.significations.forEach { signification ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(AccentGold, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = signification, fontSize = 13.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    if (planetsInHouse.isNotEmpty()) {
                        item {
                            DialogCard(title = "Planets in House", icon = Icons.Outlined.Star) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    planetsInHouse.forEach { planet ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            planetColors[planet.planet] ?: AccentGold,
                                                            CircleShape
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = planet.planet.displayName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                            Text(
                                                text = formatDegreeInSign(planet.longitude),
                                                fontSize = 13.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        DialogCard(title = "Detailed Interpretation", icon = Icons.Outlined.Description) {
                            Text(
                                text = houseDetails.interpretation,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shadbala summary dialog
 */
@Composable
fun ShadbalaDialog(
    chart: VedicChart,
    onDismiss: () -> Unit
) {
    val shadbalaAnalysis = remember(chart) {
        ShadbalaCalculator.calculateShadbala(chart)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = DialogBackground
        ) {
            Column {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DialogSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shadbala Analysis",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Six-fold Planetary Strength",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overall summary
                    item {
                        DialogCard(title = "Overall Summary", icon = Icons.Outlined.Analytics) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    SummaryBadge(
                                        label = "Chart Strength",
                                        value = "${String.format("%.1f", shadbalaAnalysis.overallStrengthScore)}%",
                                        color = when {
                                            shadbalaAnalysis.overallStrengthScore >= 100 -> AccentGreen
                                            shadbalaAnalysis.overallStrengthScore >= 85 -> AccentOrange
                                            else -> AccentRose
                                        }
                                    )
                                    SummaryBadge(
                                        label = "Strongest",
                                        value = shadbalaAnalysis.strongestPlanet.displayName,
                                        color = AccentGold
                                    )
                                    SummaryBadge(
                                        label = "Weakest",
                                        value = shadbalaAnalysis.weakestPlanet.displayName,
                                        color = AccentPurple
                                    )
                                }
                            }
                        }
                    }

                    // Individual planet strengths
                    items(shadbalaAnalysis.getPlanetsByStrength()) { shadbala ->
                        PlanetStrengthCard(shadbala)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun PlanetStrengthCard(shadbala: ShadbalaCalculator.PlanetaryShadbala) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DialogSurface
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
                            .background(
                                planetColors[shadbala.planet] ?: AccentGold,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shadbala.planet.symbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = shadbala.planet.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = shadbala.strengthRating.displayName,
                            fontSize = 12.sp,
                            color = when {
                                shadbala.isStrong -> AccentGreen
                                shadbala.percentageOfRequired >= 85 -> AccentOrange
                                else -> AccentRose
                            }
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.2f", shadbala.totalRupas)} Rupas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Required: ${String.format("%.2f", shadbala.requiredRupas)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progress = (shadbala.percentageOfRequired / 150.0).coerceIn(0.0, 1.0).toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    shadbala.isStrong -> AccentGreen
                    shadbala.percentageOfRequired >= 85 -> AccentOrange
                    else -> AccentRose
                },
                trackColor = DividerColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${String.format("%.1f", shadbala.percentageOfRequired)}% of required",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

// Helper functions
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
    return "$deg° $min'"
}

// Data classes for interpretations
data class PlanetSignifications(
    val nature: String,
    val element: String,
    val represents: List<String>,
    val bodyParts: String,
    val professions: String
)

data class HousePlacementInterpretation(
    val houseName: String,
    val houseSignification: String,
    val interpretation: String
)

data class Dignity(
    val status: String,
    val color: Color
)

data class Prediction(
    val type: PredictionType,
    val title: String,
    val description: String
)

enum class PredictionType { POSITIVE, NEGATIVE, NEUTRAL }

data class NakshatraDetails(
    val symbol: String,
    val nature: String,
    val gender: String,
    val gana: String,
    val guna: String,
    val element: String,
    val characteristics: String,
    val careers: String
)

data class HouseDetails(
    val name: String,
    val type: String,
    val significations: List<String>,
    val interpretation: String
)

// Helper functions for getting interpretations (comprehensive data)
private fun getPlanetSignifications(planet: Planet): PlanetSignifications {
    return when (planet) {
        Planet.SUN -> PlanetSignifications(
            nature = "Malefic",
            element = "Fire",
            represents = listOf("Soul, Self, Ego", "Father, Authority Figures", "Government, Power", "Health, Vitality", "Fame, Recognition"),
            bodyParts = "Heart, Spine, Right Eye, Bones",
            professions = "Government jobs, Politics, Medicine, Administration, Leadership roles"
        )
        Planet.MOON -> PlanetSignifications(
            nature = "Benefic",
            element = "Water",
            represents = listOf("Mind, Emotions", "Mother, Nurturing", "Public, Masses", "Comforts, Happiness", "Memory, Imagination"),
            bodyParts = "Mind, Left Eye, Breast, Blood, Fluids",
            professions = "Nursing, Hotel industry, Shipping, Agriculture, Psychology"
        )
        Planet.MARS -> PlanetSignifications(
            nature = "Malefic",
            element = "Fire",
            represents = listOf("Energy, Action, Courage", "Siblings, Younger Brothers", "Property, Land", "Competition, Sports", "Technical Skills"),
            bodyParts = "Blood, Muscles, Marrow, Head injuries",
            professions = "Military, Police, Surgery, Engineering, Sports, Real Estate"
        )
        Planet.MERCURY -> PlanetSignifications(
            nature = "Benefic",
            element = "Earth",
            represents = listOf("Intelligence, Communication", "Learning, Education", "Business, Trade", "Writing, Speech", "Siblings, Friends"),
            bodyParts = "Nervous system, Skin, Speech, Hands",
            professions = "Writing, Teaching, Accounting, Trading, IT, Media"
        )
        Planet.JUPITER -> PlanetSignifications(
            nature = "Benefic",
            element = "Ether",
            represents = listOf("Wisdom, Knowledge", "Teachers, Gurus", "Fortune, Luck", "Children, Dharma", "Expansion, Growth"),
            bodyParts = "Liver, Fat tissue, Ears, Thighs",
            professions = "Teaching, Law, Priesthood, Banking, Counseling"
        )
        Planet.VENUS -> PlanetSignifications(
            nature = "Benefic",
            element = "Water",
            represents = listOf("Love, Beauty, Art", "Marriage, Relationships", "Luxuries, Comforts", "Vehicles, Pleasures", "Creativity"),
            bodyParts = "Reproductive system, Face, Skin, Throat",
            professions = "Entertainment, Fashion, Art, Hospitality, Beauty industry"
        )
        Planet.SATURN -> PlanetSignifications(
            nature = "Malefic",
            element = "Air",
            represents = listOf("Discipline, Hard work", "Karma, Delays", "Longevity, Service", "Laborers, Servants", "Chronic issues"),
            bodyParts = "Bones, Teeth, Knees, Joints, Nerves",
            professions = "Mining, Agriculture, Labor, Judiciary, Real Estate"
        )
        Planet.RAHU -> PlanetSignifications(
            nature = "Malefic",
            element = "Air",
            represents = listOf("Obsession, Illusion", "Foreign lands, Travel", "Technology, Innovation", "Unconventional paths", "Material desires"),
            bodyParts = "Skin diseases, Nervous disorders",
            professions = "Technology, Foreign affairs, Aviation, Politics, Research"
        )
        Planet.KETU -> PlanetSignifications(
            nature = "Malefic",
            element = "Fire",
            represents = listOf("Spirituality, Liberation", "Past life karma", "Detachment, Isolation", "Occult, Mysticism", "Healing abilities"),
            bodyParts = "Skin, Spine, Nervous system",
            professions = "Spirituality, Research, Healing, Astrology, Philosophy"
        )
        else -> PlanetSignifications("", "", emptyList(), "", "")
    }
}

private fun getHousePlacementInterpretation(planet: Planet, house: Int): HousePlacementInterpretation {
    val houseNames = listOf(
        "", "First House (Lagna)", "Second House (Dhana)", "Third House (Sahaja)",
        "Fourth House (Sukha)", "Fifth House (Putra)", "Sixth House (Ripu)",
        "Seventh House (Kalatra)", "Eighth House (Ayur)", "Ninth House (Dharma)",
        "Tenth House (Karma)", "Eleventh House (Labha)", "Twelfth House (Vyaya)"
    )

    val houseSignifications = listOf(
        "", "Self, Body, Personality", "Wealth, Family, Speech", "Siblings, Courage, Communication",
        "Home, Mother, Happiness", "Children, Intelligence, Romance", "Enemies, Health, Service",
        "Marriage, Partnerships, Business", "Longevity, Transformation, Occult", "Fortune, Dharma, Father",
        "Career, Status, Public Image", "Gains, Income, Desires", "Losses, Expenses, Liberation"
    )

    val interpretation = when {
        planet == Planet.SUN && house == 1 -> "Strong personality with natural leadership abilities. You have a prominent presence and strong willpower. May indicate good health and vitality."
        planet == Planet.SUN && house == 10 -> "Excellent position for career success and recognition. Natural authority in professional life. Government positions or leadership roles favored."
        planet == Planet.MOON && house == 4 -> "Strong emotional foundation and attachment to home. Good relationship with mother. Domestic happiness and property gains likely."
        planet == Planet.MOON && house == 1 -> "Emotional and intuitive personality. Strong connection to feelings. Popular with the public and adaptable nature."
        planet == Planet.MARS && house == 10 -> "Dynamic career with technical or engineering success. Leadership in competitive fields. Achievement through bold actions."
        planet == Planet.MARS && house == 1 -> "Energetic and courageous personality. Athletic abilities. Can be aggressive or impulsive. Strong drive for success."
        planet == Planet.MERCURY && house == 1 -> "Intelligent and communicative personality. Good business sense. Quick thinking and versatile nature."
        planet == Planet.MERCURY && house == 5 -> "Creative intelligence and good with children. Success in speculation and education. Artistic communication skills."
        planet == Planet.JUPITER && house == 1 -> "Wise and optimistic personality. Natural teacher or advisor. Good fortune and ethical nature. Respected by others."
        planet == Planet.JUPITER && house == 9 -> "Excellent position for spiritual growth and higher learning. Good fortune with father and long journeys. Success in teaching or law."
        planet == Planet.VENUS && house == 7 -> "Beautiful spouse and harmonious marriage. Success in partnerships and business. Diplomatic abilities."
        planet == Planet.VENUS && house == 4 -> "Luxurious home and vehicles. Good relationship with mother. Domestic happiness and artistic home environment."
        planet == Planet.SATURN && house == 10 -> "Slow but steady rise in career. Success through hard work and persistence. Authority gained through discipline."
        planet == Planet.SATURN && house == 7 -> "Delayed marriage but stable. Serious approach to partnerships. May marry someone older or more mature."
        planet == Planet.RAHU && house == 10 -> "Unconventional career path. Success in foreign lands or technology. Ambitious and worldly."
        planet == Planet.KETU && house == 12 -> "Strong spiritual inclinations. Interest in meditation and liberation. May spend time in foreign lands or ashrams."
        else -> "The ${planet.displayName} in the ${house}th house influences the areas of ${houseSignifications[house]}. Results depend on the sign placement, aspects, and overall chart strength."
    }

    return HousePlacementInterpretation(
        houseName = houseNames[house],
        houseSignification = houseSignifications[house],
        interpretation = interpretation
    )
}

private fun getDignity(planet: Planet, sign: ZodiacSign): Dignity {
    // Exaltation check
    val exalted = when (planet) {
        Planet.SUN -> sign == ZodiacSign.ARIES
        Planet.MOON -> sign == ZodiacSign.TAURUS
        Planet.MARS -> sign == ZodiacSign.CAPRICORN
        Planet.MERCURY -> sign == ZodiacSign.VIRGO
        Planet.JUPITER -> sign == ZodiacSign.CANCER
        Planet.VENUS -> sign == ZodiacSign.PISCES
        Planet.SATURN -> sign == ZodiacSign.LIBRA
        else -> false
    }
    if (exalted) return Dignity("Exalted", AccentGreen)

    // Debilitation check
    val debilitated = when (planet) {
        Planet.SUN -> sign == ZodiacSign.LIBRA
        Planet.MOON -> sign == ZodiacSign.SCORPIO
        Planet.MARS -> sign == ZodiacSign.CANCER
        Planet.MERCURY -> sign == ZodiacSign.PISCES
        Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
        Planet.VENUS -> sign == ZodiacSign.VIRGO
        Planet.SATURN -> sign == ZodiacSign.ARIES
        else -> false
    }
    if (debilitated) return Dignity("Debilitated", AccentRose)

    // Own sign check
    if (sign.ruler == planet) return Dignity("Own Sign", AccentGold)

    // Moolatrikona check
    val moolatrikona = when (planet) {
        Planet.SUN -> sign == ZodiacSign.LEO
        Planet.MOON -> sign == ZodiacSign.TAURUS
        Planet.MARS -> sign == ZodiacSign.ARIES
        Planet.MERCURY -> sign == ZodiacSign.VIRGO
        Planet.JUPITER -> sign == ZodiacSign.SAGITTARIUS
        Planet.VENUS -> sign == ZodiacSign.LIBRA
        Planet.SATURN -> sign == ZodiacSign.AQUARIUS
        else -> false
    }
    if (moolatrikona) return Dignity("Moolatrikona", AccentTeal)

    return Dignity("Neutral", TextSecondary)
}

private fun getPlanetPredictions(
    position: PlanetPosition,
    shadbala: ShadbalaCalculator.PlanetaryShadbala,
    chart: VedicChart
): List<Prediction> {
    val predictions = mutableListOf<Prediction>()
    val planet = position.planet

    // Strength-based predictions
    if (shadbala.isStrong) {
        predictions.add(Prediction(
            PredictionType.POSITIVE,
            "Strong ${planet.displayName}",
            "This planet has sufficient strength to deliver positive results. Its significations will manifest more easily in your life."
        ))
    } else {
        predictions.add(Prediction(
            PredictionType.NEGATIVE,
            "Weak ${planet.displayName}",
            "This planet lacks sufficient strength. You may face challenges in areas it governs. Remedial measures may help."
        ))
    }

    // Dignity-based predictions
    val dignity = getDignity(planet, position.sign)
    when (dignity.status) {
        "Exalted" -> predictions.add(Prediction(
            PredictionType.POSITIVE,
            "Exalted Planet",
            "${planet.displayName} is in its sign of exaltation, giving exceptional results in its significations."
        ))
        "Debilitated" -> predictions.add(Prediction(
            PredictionType.NEGATIVE,
            "Debilitated Planet",
            "${planet.displayName} is in its fall. Its positive significations may be reduced or delayed."
        ))
        "Own Sign" -> predictions.add(Prediction(
            PredictionType.POSITIVE,
            "Planet in Own Sign",
            "${planet.displayName} is comfortable in its own sign, giving stable and reliable results."
        ))
    }

    // Retrograde prediction
    if (position.isRetrograde) {
        predictions.add(Prediction(
            PredictionType.NEUTRAL,
            "Retrograde Motion",
            "Retrograde planets work on an internal level. Results may be delayed but often more profound."
        ))
    }

    // House-specific predictions
    when (position.house) {
        1, 5, 9 -> predictions.add(Prediction(
            PredictionType.POSITIVE,
            "Trikona Placement",
            "${planet.displayName} in house ${position.house} (Trikona) is auspicious for fortune and dharma."
        ))
        6, 8, 12 -> predictions.add(Prediction(
            PredictionType.NEUTRAL,
            "Dusthana Placement",
            "${planet.displayName} in house ${position.house} may face obstacles but can also give transformative experiences."
        ))
        1, 4, 7, 10 -> predictions.add(Prediction(
            PredictionType.POSITIVE,
            "Kendra Placement",
            "${planet.displayName} in house ${position.house} (Kendra) gains strength and visibility."
        ))
    }

    return predictions
}

private fun getNakshatraDetails(nakshatra: Nakshatra): NakshatraDetails {
    return when (nakshatra) {
        Nakshatra.ASHWINI -> NakshatraDetails(
            symbol = "Horse's Head",
            nature = "Swift (Kshipra)",
            gender = "Male",
            gana = "Deva (Divine)",
            guna = "Rajas",
            element = "Earth",
            characteristics = "Ashwini natives are quick, energetic, and pioneering. They have natural healing abilities and are often the first to try new things. Speed and initiative are their hallmarks.",
            careers = "Medical field, Emergency services, Sports, Transportation, Veterinary science"
        )
        Nakshatra.BHARANI -> NakshatraDetails(
            symbol = "Yoni (Female reproductive organ)",
            nature = "Fierce (Ugra)",
            gender = "Female",
            gana = "Manushya (Human)",
            guna = "Rajas",
            element = "Earth",
            characteristics = "Bharani natives are creative, responsible, and can bear heavy burdens. They understand life's transformative nature and often work with matters of birth, death, and transformation.",
            careers = "Midwifery, Funeral services, Entertainment, Creative arts, Psychology"
        )
        Nakshatra.ROHINI -> NakshatraDetails(
            symbol = "Ox Cart / Chariot",
            nature = "Fixed (Dhruva)",
            gender = "Female",
            gana = "Manushya (Human)",
            guna = "Rajas",
            element = "Earth",
            characteristics = "Rohini natives are attractive, artistic, and materialistic in a positive way. They appreciate beauty and luxury. Strong creative and productive abilities.",
            careers = "Fashion, Beauty industry, Agriculture, Music, Hospitality"
        )
        else -> NakshatraDetails(
            symbol = nakshatra.deity,
            nature = "Mixed",
            gender = "Neutral",
            gana = "Mixed",
            guna = "Mixed",
            element = "Mixed",
            characteristics = "${nakshatra.displayName} is ruled by ${nakshatra.ruler.displayName}. Natives are influenced by the deity ${nakshatra.deity}.",
            careers = "Various fields depending on overall chart analysis"
        )
    }
}

private fun getPadaDescription(nakshatra: Nakshatra, pada: Int): String {
    val padaSigns = listOf(
        nakshatra.pada1Sign,
        nakshatra.pada2Sign,
        nakshatra.pada3Sign,
        nakshatra.pada4Sign
    )
    val padaSign = padaSigns[pada - 1]

    return "Pada $pada falls in ${padaSign.displayName} Navamsa, ruled by ${padaSign.ruler.displayName}. " +
            "This pada emphasizes the ${padaSign.element} element qualities combined with the main nakshatra characteristics."
}

private fun getHouseDetails(house: Int): HouseDetails {
    return when (house) {
        1 -> HouseDetails(
            name = "Lagna Bhava (Ascendant)",
            type = "Kendra (Angular) & Trikona (Trine)",
            significations = listOf("Physical body", "Personality", "Self-identity", "Head and brain", "General health", "Beginning of life", "Appearance"),
            interpretation = "The First House is the most important house, representing you as a whole. It shows your physical constitution, personality traits, and how you present yourself to the world. A strong 1st house gives good health, confidence, and success in self-started ventures."
        )
        2 -> HouseDetails(
            name = "Dhana Bhava (Wealth)",
            type = "Maraka (Death-inflicting) & Panapara",
            significations = listOf("Wealth & Possessions", "Family", "Speech", "Right eye", "Face", "Food intake", "Early childhood"),
            interpretation = "The Second House governs accumulated wealth, family values, and speech. It shows how you earn and save money, your relationship with family, and your communication style. A strong 2nd house indicates financial stability and sweet speech."
        )
        3 -> HouseDetails(
            name = "Sahaja Bhava (Siblings)",
            type = "Upachaya (Growth) & Apoklima",
            significations = listOf("Siblings", "Courage", "Short journeys", "Communication", "Arms and shoulders", "Neighbors", "Hobbies"),
            interpretation = "The Third House represents courage, initiative, and self-effort. It governs siblings (especially younger), short travels, and all forms of communication. A strong 3rd house gives courage, good relationships with siblings, and success through personal effort."
        )
        4 -> HouseDetails(
            name = "Sukha Bhava (Happiness)",
            type = "Kendra (Angular)",
            significations = listOf("Mother", "Home & Property", "Vehicles", "Education", "Chest & Heart", "Inner peace", "Emotional foundation"),
            interpretation = "The Fourth House is the foundation of your life. It represents your mother, home environment, and emotional security. It also governs formal education and landed property. A strong 4th house gives domestic happiness, property ownership, and mental peace."
        )
        5 -> HouseDetails(
            name = "Putra Bhava (Children)",
            type = "Trikona (Trine) & Panapara",
            significations = listOf("Children", "Intelligence", "Creativity", "Romance", "Past life merit", "Speculation", "Higher education"),
            interpretation = "The Fifth House is the house of creativity and Purva Punya (past life merits). It governs children, intelligence, romance, and speculative gains. A strong 5th house gives intelligent children, creative talents, and success in speculation."
        )
        6 -> HouseDetails(
            name = "Ripu Bhava (Enemies)",
            type = "Dusthana (Malefic) & Upachaya",
            significations = listOf("Enemies", "Diseases", "Debts", "Service", "Competition", "Daily work", "Maternal uncle"),
            interpretation = "The Sixth House governs obstacles, health issues, and service. While considered malefic, it also shows the ability to overcome challenges. A well-placed 6th house gives victory over enemies, good health practices, and success in competitive fields."
        )
        7 -> HouseDetails(
            name = "Kalatra Bhava (Spouse)",
            type = "Kendra (Angular) & Maraka",
            significations = listOf("Marriage", "Spouse", "Business partnerships", "Foreign travel", "Public dealing", "Lower abdomen", "Sexual organs"),
            interpretation = "The Seventh House is the house of partnerships and marriage. It shows your spouse's nature and quality of marriage. It also governs business partnerships and public dealings. A strong 7th house gives a good spouse and success in partnerships."
        )
        8 -> HouseDetails(
            name = "Ayur Bhava (Longevity)",
            type = "Dusthana (Malefic) & Panapara",
            significations = listOf("Longevity", "Transformation", "Occult", "Inheritance", "Hidden matters", "Chronic diseases", "In-laws' wealth"),
            interpretation = "The Eighth House governs transformation, death, and rebirth (metaphorical). It shows hidden matters, inheritance, and occult interests. While considered difficult, a well-placed 8th house gives longevity, research abilities, and unexpected gains."
        )
        9 -> HouseDetails(
            name = "Dharma Bhava (Fortune)",
            type = "Trikona (Trine) & Apoklima",
            significations = listOf("Fortune & Luck", "Father", "Higher learning", "Long journeys", "Religion & Philosophy", "Guru/Teacher", "Righteousness"),
            interpretation = "The Ninth House is the most auspicious house of fortune and dharma. It represents your father, teachers, and higher wisdom. A strong 9th house gives good fortune, philosophical inclinations, and blessings from elders and teachers."
        )
        10 -> HouseDetails(
            name = "Karma Bhava (Career)",
            type = "Kendra (Angular) & Upachaya",
            significations = listOf("Career", "Profession", "Status & Fame", "Authority", "Government", "Father", "Knees"),
            interpretation = "The Tenth House is the house of career and public image. It shows your profession, status in society, and relationship with authority. A strong 10th house gives career success, fame, and high position in society."
        )
        11 -> HouseDetails(
            name = "Labha Bhava (Gains)",
            type = "Upachaya (Growth) & Panapara",
            significations = listOf("Income & Gains", "Elder siblings", "Friends", "Hopes & Wishes", "Social network", "Left ear", "Ankles"),
            interpretation = "The Eleventh House is the house of gains and fulfillment of desires. It governs income, elder siblings, and friendships. A strong 11th house gives multiple sources of income, supportive friends, and fulfillment of hopes."
        )
        12 -> HouseDetails(
            name = "Vyaya Bhava (Loss)",
            type = "Dusthana (Malefic) & Apoklima",
            significations = listOf("Losses & Expenses", "Liberation (Moksha)", "Foreign lands", "Isolation", "Feet", "Sleep", "Subconscious"),
            interpretation = "The Twelfth House governs losses, expenses, and liberation. While it shows material losses, it also represents spiritual gains and final liberation. A strong 12th house gives spiritual inclinations, success abroad, and peaceful sleep."
        )
        else -> HouseDetails("", "", emptyList(), "")
    }
}
