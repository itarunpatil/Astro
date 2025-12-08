package com.astro.storm.ui.screen.main

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.theme.AppTheme

private val GridSpacing = 12.dp
private val CardCornerRadius = 12.dp
private val IconContainerSize = 40.dp
private val IconContainerCornerRadius = 10.dp
private val IconSize = 22.dp
private val CardContentPadding = 16.dp

@Composable
fun HomeTab(
    chart: VedicChart?,
    onFeatureClick: (InsightFeature) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 100.dp)
) {
    if (chart == null) {
        EmptyHomeState(modifier = modifier)
        return
    }

    val implementedFeatures = remember { InsightFeature.implementedFeatures }
    val comingSoonFeatures = remember { InsightFeature.comingSoonFeatures }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground),
        contentPadding = contentPadding
    ) {
        item(key = "header_chart_analysis") {
            SectionHeader(
                textKey = StringKey.HOME_CHART_ANALYSIS,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 12.dp
                )
            )
        }

        item(key = "grid_implemented") {
            FeatureGrid(
                features = implementedFeatures,
                onFeatureClick = onFeatureClick,
                isDisabled = false
            )
        }

        item(key = "header_coming_soon") {
            SectionHeader(
                textKey = StringKey.HOME_COMING_SOON,
                isMuted = true,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 24.dp,
                    end = 16.dp,
                    bottom = 12.dp
                )
            )
        }

        item(key = "grid_coming_soon") {
            FeatureGrid(
                features = comingSoonFeatures,
                onFeatureClick = {},
                isDisabled = true
            )
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    textKey: StringKey,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false
) {
    Text(
        text = stringResource(textKey),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (isMuted) AppTheme.TextMuted else AppTheme.TextPrimary,
        modifier = modifier
    )
}

@Composable
private fun FeatureGrid(
    features: List<InsightFeature>,
    onFeatureClick: (InsightFeature) -> Unit,
    isDisabled: Boolean,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    horizontalSpacing: Dp = GridSpacing,
    verticalSpacing: Dp = GridSpacing
) {
    val chunkedFeatures = remember(features, columns) {
        features.chunked(columns)
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        chunkedFeatures.forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = { onFeatureClick(feature) },
                        isDisabled = isDisabled,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowFeatures.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun FeatureCard(
    feature: InsightFeature,
    onClick: () -> Unit,
    isDisabled: Boolean,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val containerColor = remember(isDisabled) {
        if (isDisabled) AppTheme.CardBackground.copy(alpha = 0.5f)
        else AppTheme.CardBackground
    }

    val iconBackgroundColor = remember(isDisabled, feature.color) {
        if (isDisabled) AppTheme.TextSubtle.copy(alpha = 0.1f)
        else feature.color.copy(alpha = 0.15f)
    }

    val iconTint = if (isDisabled) AppTheme.TextSubtle else feature.color
    val titleColor = if (isDisabled) AppTheme.TextSubtle else AppTheme.TextPrimary
    val descriptionColor = if (isDisabled) {
        AppTheme.TextSubtle.copy(alpha = 0.7f)
    } else {
        AppTheme.TextMuted
    }

    val interactionSource = remember { MutableInteractionSource() }
    val rippleIndication = rememberRipple(color = feature.color)

    val title = feature.getLocalizedTitle(language)
    val description = feature.getLocalizedDescription(language)
    val comingSoonText = stringResource(StringKey.HOME_COMING_SOON)

    Card(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append(title)
                    append(": ")
                    append(description)
                    if (isDisabled) append(". $comingSoonText")
                }
            }
            .clip(RoundedCornerShape(CardCornerRadius))
            .clickable(
                enabled = !isDisabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rippleIndication,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(CardCornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDisabled) 0.dp else 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardContentPadding)
        ) {
            FeatureCardHeader(
                icon = feature.icon,
                iconBackgroundColor = iconBackgroundColor,
                iconTint = iconTint,
                showComingSoonBadge = isDisabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clearAndSetSemantics { }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }
}

@Composable
private fun FeatureCardHeader(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    showComingSoonBadge: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(IconContainerSize)
                .clip(RoundedCornerShape(IconContainerCornerRadius))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(IconSize)
            )
        }

        if (showComingSoonBadge) {
            ComingSoonBadge()
        }
    }
}

@Composable
private fun ComingSoonBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = AppTheme.TextSubtle.copy(alpha = 0.12f)
    ) {
        Text(
            text = stringResource(StringKey.HOME_SOON_BADGE),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextSubtle,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Stable
enum class InsightFeature(
    val titleKey: StringKey,
    val descriptionKey: StringKey,
    val icon: ImageVector,
    val color: Color,
    val isImplemented: Boolean
) {
    FULL_CHART(
        titleKey = StringKey.FEATURE_BIRTH_CHART,
        descriptionKey = StringKey.FEATURE_BIRTH_CHART_DESC,
        icon = Icons.Outlined.GridView,
        color = AppTheme.AccentPrimary,
        isImplemented = true
    ),
    PLANETS(
        titleKey = StringKey.FEATURE_PLANETS,
        descriptionKey = StringKey.FEATURE_PLANETS_DESC,
        icon = Icons.Outlined.Public,
        color = AppTheme.LifeAreaCareer,
        isImplemented = true
    ),
    YOGAS(
        titleKey = StringKey.FEATURE_YOGAS,
        descriptionKey = StringKey.FEATURE_YOGAS_DESC,
        icon = Icons.Outlined.AutoAwesome,
        color = AppTheme.AccentGold,
        isImplemented = true
    ),
    DASHAS(
        titleKey = StringKey.FEATURE_DASHAS,
        descriptionKey = StringKey.FEATURE_DASHAS_DESC,
        icon = Icons.Outlined.Timeline,
        color = AppTheme.LifeAreaSpiritual,
        isImplemented = true
    ),
    TRANSITS(
        titleKey = StringKey.FEATURE_TRANSITS,
        descriptionKey = StringKey.FEATURE_TRANSITS_DESC,
        icon = Icons.Outlined.Sync,
        color = AppTheme.AccentTeal,
        isImplemented = true
    ),
    ASHTAKAVARGA(
        titleKey = StringKey.FEATURE_ASHTAKAVARGA,
        descriptionKey = StringKey.FEATURE_ASHTAKAVARGA_DESC,
        icon = Icons.Outlined.BarChart,
        color = AppTheme.SuccessColor,
        isImplemented = true
    ),
    PANCHANGA(
        titleKey = StringKey.FEATURE_PANCHANGA,
        descriptionKey = StringKey.FEATURE_PANCHANGA_DESC,
        icon = Icons.Outlined.CalendarMonth,
        color = AppTheme.LifeAreaFinance,
        isImplemented = true
    ),
    MATCHMAKING(
        titleKey = StringKey.FEATURE_MATCHMAKING,
        descriptionKey = StringKey.FEATURE_MATCHMAKING_DESC,
        icon = Icons.Outlined.Favorite,
        color = AppTheme.LifeAreaLove,
        isImplemented = true
    ),
    MUHURTA(
        titleKey = StringKey.FEATURE_MUHURTA,
        descriptionKey = StringKey.FEATURE_MUHURTA_DESC,
        icon = Icons.Outlined.AccessTime,
        color = AppTheme.WarningColor,
        isImplemented = true
    ),
    REMEDIES(
        titleKey = StringKey.FEATURE_REMEDIES,
        descriptionKey = StringKey.FEATURE_REMEDIES_DESC,
        icon = Icons.Outlined.Spa,
        color = AppTheme.LifeAreaHealth,
        isImplemented = true
    ),
    VARSHAPHALA(
        titleKey = StringKey.FEATURE_VARSHAPHALA,
        descriptionKey = StringKey.FEATURE_VARSHAPHALA_DESC,
        icon = Icons.Outlined.Cake,
        color = AppTheme.LifeAreaCareer,
        isImplemented = true
    ),
    PRASHNA(
        titleKey = StringKey.FEATURE_PRASHNA,
        descriptionKey = StringKey.FEATURE_PRASHNA_DESC,
        icon = Icons.Outlined.HelpOutline,
        color = AppTheme.AccentTeal,
        isImplemented = true
    ),
    CHART_COMPARISON(
        titleKey = StringKey.FEATURE_SYNASTRY,
        descriptionKey = StringKey.FEATURE_SYNASTRY_DESC,
        icon = Icons.Outlined.CompareArrows,
        color = AppTheme.LifeAreaFinance,
        isImplemented = false
    ),
    NAKSHATRA_ANALYSIS(
        titleKey = StringKey.FEATURE_NAKSHATRAS,
        descriptionKey = StringKey.FEATURE_NAKSHATRAS_DESC,
        icon = Icons.Outlined.Stars,
        color = AppTheme.AccentGold,
        isImplemented = false
    ),
    SHADBALA(
        titleKey = StringKey.FEATURE_SHADBALA,
        descriptionKey = StringKey.FEATURE_SHADBALA_DESC,
        icon = Icons.Outlined.Speed,
        color = AppTheme.SuccessColor,
        isImplemented = false
    );

    /**
     * Get localized title
     */
    fun getLocalizedTitle(language: Language): String {
        return StringResources.get(titleKey, language)
    }

    /**
     * Get localized description
     */
    fun getLocalizedDescription(language: Language): String {
        return StringResources.get(descriptionKey, language)
    }

    companion object {
        val implementedFeatures: List<InsightFeature> by lazy(LazyThreadSafetyMode.PUBLICATION) {
            entries.filter { it.isImplemented }
        }

        val comingSoonFeatures: List<InsightFeature> by lazy(LazyThreadSafetyMode.PUBLICATION) {
            entries.filter { !it.isImplemented }
        }
    }
}

@Composable
private fun EmptyHomeState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.ScreenBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonAddAlt,
                contentDescription = null,
                tint = AppTheme.TextMuted,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(StringKey.NO_PROFILE_SELECTED),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(StringKey.NO_PROFILE_MESSAGE),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}