package com.astro.storm.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.theme.AppTheme

/**
 * Home Tab - Chart Analysis & Tools
 *
 * Displays:
 * - Quick access to detailed chart analysis features
 * - Feature grid for various astrological tools
 */
@Composable
fun HomeTab(
    chart: VedicChart?,
    onFeatureClick: (InsightFeature) -> Unit
) {
    if (chart == null) {
        EmptyHomeState()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Quick Actions Header
        item {
            Text(
                text = "Chart Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 12.dp)
            )
        }

        // Feature Grid - Implemented features
        item {
            FeatureGrid(
                features = InsightFeature.implementedFeatures,
                onFeatureClick = onFeatureClick
            )
        }

        // Coming Soon Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Coming Soon Features
        item {
            FeatureGrid(
                features = InsightFeature.comingSoonFeatures,
                onFeatureClick = { /* Non-functional */ },
                isDisabled = true
            )
        }
    }
}

@Composable
private fun FeatureGrid(
    features: List<InsightFeature>,
    onFeatureClick: (InsightFeature) -> Unit,
    isDisabled: Boolean = false
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        features.chunked(2).forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = { onFeatureClick(feature) },
                        isDisabled = isDisabled,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of features
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    feature: InsightFeature,
    onClick: () -> Unit,
    isDisabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(enabled = !isDisabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDisabled) AppTheme.CardBackground.copy(alpha = 0.5f) else AppTheme.CardBackground
        ),
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDisabled) AppTheme.TextSubtle.copy(alpha = 0.1f)
                            else feature.color.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = if (isDisabled) AppTheme.TextSubtle else feature.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (isDisabled) {
                    Text(
                        text = "Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDisabled) AppTheme.TextSubtle else AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDisabled) AppTheme.TextSubtle.copy(alpha = 0.7f) else AppTheme.TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Available insight features
 */
enum class InsightFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isImplemented: Boolean
) {
    // Implemented features
    FULL_CHART(
        title = "Birth Chart",
        description = "View your complete Vedic birth chart",
        icon = Icons.Outlined.GridView,
        color = AppTheme.AccentPrimary,
        isImplemented = true
    ),
    PLANETS(
        title = "Planets",
        description = "Detailed planetary positions",
        icon = Icons.Outlined.Public,
        color = AppTheme.LifeAreaCareer,
        isImplemented = true
    ),
    YOGAS(
        title = "Yogas",
        description = "Planetary combinations & effects",
        icon = Icons.Outlined.AutoAwesome,
        color = AppTheme.AccentGold,
        isImplemented = true
    ),
    DASHAS(
        title = "Dashas",
        description = "Planetary period timeline",
        icon = Icons.Outlined.Timeline,
        color = AppTheme.LifeAreaSpiritual,
        isImplemented = true
    ),
    TRANSITS(
        title = "Transits",
        description = "Current planetary movements",
        icon = Icons.Outlined.Sync,
        color = AppTheme.AccentTeal,
        isImplemented = true
    ),
    ASHTAKAVARGA(
        title = "Ashtakavarga",
        description = "Strength analysis by house",
        icon = Icons.Outlined.BarChart,
        color = AppTheme.SuccessColor,
        isImplemented = true
    ),
    PANCHANGA(
        title = "Panchanga",
        description = "Vedic calendar elements",
        icon = Icons.Outlined.CalendarMonth,
        color = AppTheme.LifeAreaFinance,
        isImplemented = true
    ),

    // Newly implemented features
    MATCHMAKING(
        title = "Matchmaking",
        description = "Kundli Milan compatibility",
        icon = Icons.Outlined.Favorite,
        color = AppTheme.LifeAreaLove,
        isImplemented = true
    ),
    MUHURTA(
        title = "Muhurta",
        description = "Auspicious timing finder",
        icon = Icons.Outlined.AccessTime,
        color = AppTheme.WarningColor,
        isImplemented = true
    ),
    REMEDIES(
        title = "Remedies",
        description = "Personalized remedies",
        icon = Icons.Outlined.Spa,
        color = AppTheme.LifeAreaHealth,
        isImplemented = true
    ),
    VARSHAPHALA(
        title = "Varshaphala",
        description = "Solar return horoscope",
        icon = Icons.Outlined.Cake,
        color = AppTheme.LifeAreaCareer,
        isImplemented = true
    ),

    // Coming soon features
    PRASHNA(
        title = "Prashna",
        description = "Horary astrology",
        icon = Icons.Outlined.HelpOutline,
        color = AppTheme.AccentTeal,
        isImplemented = false
    ),
    CHART_COMPARISON(
        title = "Synastry",
        description = "Chart comparison",
        icon = Icons.Outlined.CompareArrows,
        color = AppTheme.LifeAreaFinance,
        isImplemented = false
    ),
    NAKSHATRA_ANALYSIS(
        title = "Nakshatras",
        description = "Deep nakshatra analysis",
        icon = Icons.Outlined.Stars,
        color = AppTheme.AccentGold,
        isImplemented = false
    ),
    SHADBALA(
        title = "Shadbala",
        description = "Six-fold strength",
        icon = Icons.Outlined.Speed,
        color = AppTheme.SuccessColor,
        isImplemented = false
    );

    companion object {
        val implementedFeatures = entries.filter { it.isImplemented }
        val comingSoonFeatures = entries.filter { !it.isImplemented }
    }
}


@Composable
private fun EmptyHomeState() {
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
                imageVector = Icons.Outlined.PersonAddAlt,
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
                text = "Select or create a profile to view your personalized astrological insights.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
