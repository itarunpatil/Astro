package com.astro.storm.ui.screen.matchmaking

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.CompatibilityRating
import com.astro.storm.data.model.GunaAnalysis
import com.astro.storm.data.model.ManglikAnalysis
import com.astro.storm.data.model.ManglikDosha
import com.astro.storm.data.model.MatchmakingResult
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.VedicAstrologyUtils
import com.astro.storm.ui.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * Reusable UI components for matchmaking screens.
 *
 * These components handle:
 * - Score visualization (circular progress, bars)
 * - Profile comparison displays
 * - Dosha analysis cards
 * - Guna analysis cards
 * - Color/icon utilities for ratings and severity
 */

// ============================================
// COLOR & ICON UTILITIES
// ============================================

/**
 * Get the appropriate color for a compatibility rating.
 */
fun getRatingColor(rating: CompatibilityRating): Color {
    return when (rating) {
        CompatibilityRating.EXCELLENT -> Color(0xFF2E7D32)
        CompatibilityRating.GOOD -> Color(0xFF558B2F)
        CompatibilityRating.AVERAGE -> Color(0xFFF9A825)
        CompatibilityRating.BELOW_AVERAGE -> Color(0xFFEF6C00)
        CompatibilityRating.POOR -> Color(0xFFC62828)
    }
}

/**
 * Get the appropriate icon for a compatibility rating.
 */
fun getRatingIcon(rating: CompatibilityRating): ImageVector {
    return when (rating) {
        CompatibilityRating.EXCELLENT -> Icons.Filled.Stars
        CompatibilityRating.GOOD -> Icons.Filled.ThumbUp
        CompatibilityRating.AVERAGE -> Icons.Outlined.Balance
        CompatibilityRating.BELOW_AVERAGE -> Icons.Outlined.TrendingDown
        CompatibilityRating.POOR -> Icons.Filled.Cancel
    }
}

/**
 * Get color based on Manglik status text.
 */
fun getManglikStatusColor(status: String): Color {
    return when {
        status.contains("No concerns", ignoreCase = true) -> AppTheme.SuccessColor
        status.contains("cancel", ignoreCase = true) -> AppTheme.SuccessColor
        status.contains("Both", ignoreCase = true) -> AppTheme.SuccessColor
        status.contains("Manageable", ignoreCase = true) -> AppTheme.WarningColor
        status.contains("Partial", ignoreCase = true) -> AppTheme.WarningColor
        else -> AppTheme.ErrorColor
    }
}

/**
 * Get color based on Manglik Dosha severity.
 */
fun getManglikSeverityColor(dosha: ManglikDosha): Color {
    return when (dosha) {
        ManglikDosha.NONE -> AppTheme.SuccessColor
        ManglikDosha.PARTIAL -> AppTheme.WarningColor
        ManglikDosha.FULL -> AppTheme.ErrorColor
        ManglikDosha.DOUBLE -> Color(0xFF8B0000) // Dark red for severe
    }
}

// ============================================
// PROFILE SELECTION COMPONENTS
// ============================================

/**
 * Enhanced profile card used in the profile selection section.
 */
@Composable
fun ProfileCard(
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
        border = if (chart != null) null else androidx.compose.foundation.BorderStroke(
            1.dp, AppTheme.BorderColor.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (chart != null) color.copy(alpha = 0.15f) else AppTheme.ChipBackground
                    )
                    .border(
                        width = 2.dp,
                        color = if (chart != null) color else AppTheme.BorderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (chart != null) color else AppTheme.TextMuted,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (chart != null) color else AppTheme.TextMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                chart?.birthData?.name ?: stringResource(StringKey.MATCH_TAP_TO_SELECT),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (chart != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (chart != null) AppTheme.TextPrimary else AppTheme.TextSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            chart?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    MatchmakingReportUtils.getNakshatraName(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Connection indicator showing heart icon between profiles.
 */
@Composable
fun ConnectionIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isConnected) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = stringResource(StringKey.MATCH_CONNECTED),
                tint = AppTheme.LifeAreaLove,
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale)
            )
        } else {
            Icon(
                Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(StringKey.MATCH_NOT_CONNECTED),
                tint = AppTheme.TextSubtle,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================
// SCORE DISPLAY COMPONENTS
// ============================================

/**
 * Circular compatibility score card with animated progress.
 */
@Composable
fun CompatibilityScoreCard(
    result: MatchmakingResult,
    animatedProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            getRatingColor(result.rating).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular progress indicator
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 14.dp,
                        color = AppTheme.ChipBackground,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 14.dp,
                        color = getRatingColor(result.rating),
                        strokeCap = StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", result.totalPoints),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            text = stringResource(StringKey.MATCH_OUT_OF, result.maxPoints.toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rating badge
                Surface(
                    color = getRatingColor(result.rating).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            getRatingIcon(result.rating),
                            contentDescription = null,
                            tint = getRatingColor(result.rating),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            result.rating.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = getRatingColor(result.rating)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Percentage text
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = getRatingColor(result.rating)
                        )) {
                            append(String.format("%.1f%%", result.percentage))
                        }
                        append(" ${stringResource(StringKey.MATCH_COMPATIBILITY)}")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rating description
                Text(
                    text = result.rating.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * Quick insight chip showing a label and value.
 */
@Composable
fun QuickInsightChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

/**
 * Enhanced score bar for individual Guna display.
 */
@Composable
fun GunaScoreBar(guna: GunaAnalysis) {
    val animatedProgress by animateFloatAsState(
        targetValue = (guna.obtainedPoints / guna.maxPoints).toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "guna_progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    guna.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "(${guna.description})",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
            Surface(
                color = if (guna.isPositive) AppTheme.SuccessColor.copy(alpha = 0.1f)
                else AppTheme.WarningColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (guna.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppTheme.ChipBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (guna.isPositive)
                                listOf(AppTheme.SuccessColor.copy(alpha = 0.7f), AppTheme.SuccessColor)
                            else
                                listOf(AppTheme.WarningColor.copy(alpha = 0.7f), AppTheme.WarningColor)
                        )
                    )
            )
        }
    }
}

// ============================================
// COMPARISON COMPONENTS
// ============================================

/**
 * Row showing comparison between bride and groom values.
 */
@Composable
fun ComparisonRow(
    label: String,
    brideValue: String,
    groomValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            brideValue,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.LifeAreaLove,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Surface(
            color = AppTheme.ChipBackground,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Text(
            groomValue,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.AccentTeal,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Nakshatra comparison row with centered label.
 */
@Composable
fun NakshatraComparisonRow(
    label: String,
    brideValue: String,
    groomValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                brideValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.LifeAreaLove
            )
        }
        Surface(
            color = AppTheme.ChipBackground,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                groomValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.AccentTeal
            )
        }
    }
}

// ============================================
// STATE COMPONENTS
// ============================================

/**
 * Calculating/loading state with animated icon.
 */
@Composable
fun CalculatingState() {
    val infiniteTransition = rememberInfiniteTransition(label = "calculating")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = AppTheme.AccentPrimary.copy(alpha = 0.3f),
                    strokeWidth = 4.dp
                )
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                stringResource(StringKey.MATCH_ANALYZING_COMPATIBILITY),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(StringKey.MATCH_CALCULATING_DOSHAS),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

/**
 * Error card with retry button.
 */
@Composable
fun ErrorCard(
    error: String,
    onRetry: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(StringKey.MATCH_CALCULATION_ERROR),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.ErrorColor
                )
                Text(
                    error,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.ErrorColor.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onRetry) {
                Text(stringResource(StringKey.BTN_RETRY), color = AppTheme.ErrorColor)
            }
        }
    }
}

// ============================================
// MANGLIK ANALYSIS COMPONENTS
// ============================================

/**
 * Enhanced Manglik person analysis card.
 */
@Composable
fun ManglikPersonCard(
    analysis: ManglikAnalysis,
    label: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (label == stringResource(StringKey.MATCH_BRIDE)) Icons.Filled.Female else Icons.Filled.Male,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            analysis.person,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                        if (analysis.marsHouse > 0) {
                            Text(
                                stringResource(StringKey.MATCH_MARS_IN_HOUSE, analysis.marsHouse),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                }

                Surface(
                    color = getManglikSeverityColor(analysis.effectiveDosha).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        analysis.effectiveDosha.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = getManglikSeverityColor(analysis.effectiveDosha),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Contributing factors
            if (analysis.factors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = AppTheme.WarningColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            stringResource(StringKey.MATCH_CONTRIBUTING_FACTORS),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.WarningColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        analysis.factors.forEach { factor ->
                            Text(
                                "• $factor",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.WarningColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Cancellation factors
            if (analysis.cancellations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = AppTheme.SuccessColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            stringResource(StringKey.MATCH_CANCELLATION_FACTORS),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.SuccessColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        analysis.cancellations.forEach { cancellation ->
                            Text(
                                "✓ $cancellation",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.SuccessColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// REMEDY COMPONENTS
// ============================================

/**
 * Individual remedy item with numbered indicator.
 */
@Composable
fun RemedyItem(number: Int, remedy: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AppTheme.AccentPrimary.copy(alpha = 0.2f),
                            AppTheme.AccentSecondary.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$number",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.AccentPrimary
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            remedy,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.TextSecondary,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
