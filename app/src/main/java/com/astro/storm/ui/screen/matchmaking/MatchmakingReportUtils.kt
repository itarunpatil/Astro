package com.astro.storm.ui.screen.matchmaking

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.CompatibilityRating
import com.astro.storm.data.model.ManglikDosha
import com.astro.storm.data.model.MatchmakingResult
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart

/**
 * Utility functions for generating matchmaking reports.
 *
 * Handles text report generation for:
 * - Full detailed reports
 * - Summary reports
 * - Score-only reports
 *
 * These functions are used by the share/copy functionality in MatchmakingScreen.
 */
object MatchmakingReportUtils {

    /**
     * Generate a full detailed text report of the matchmaking analysis.
     *
     * Includes:
     * - Profile information (names, moon signs, nakshatras)
     * - Compatibility score and rating
     * - All 8 Guna analyses with details
     * - Manglik Dosha analysis
     * - Special considerations
     * - Suggested remedies
     */
    fun generateFullReport(
        result: MatchmakingResult,
        brideChart: VedicChart?,
        groomChart: VedicChart?,
        language: Language
    ): String {
        val naText = StringResources.get(StringKey.REPORT_NA, language)
        val brideLabel = StringResources.get(StringKey.REPORT_BRIDE_LABEL, language)
        val groomLabel = StringResources.get(StringKey.REPORT_GROOM_LABEL, language)
        val moonSignLabel = StringResources.get(StringKey.REPORT_MOON_SIGN_LABEL, language)
        val nakshatraLabel = StringResources.get(StringKey.REPORT_NAKSHATRA_LABEL, language)
        val cancellationText = StringResources.get(StringKey.REPORT_CANCELLATION, language)

        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("       ${StringResources.get(StringKey.REPORT_MATCHMAKING_TITLE, language)}")
            appendLine("         ${StringResources.get(StringKey.REPORT_ASTROSTORM_ANALYSIS, language)}")
            appendLine("═══════════════════════════════════════")
            appendLine()

            appendLine(StringResources.get(StringKey.REPORT_PROFILES, language))
            appendLine("─────────────────────────────────────")
            brideChart?.let {
                appendLine("$brideLabel ${it.birthData.name ?: naText}")
                appendLine("  $moonSignLabel ${getRashiName(it)}")
                appendLine("  $nakshatraLabel ${getNakshatraName(it)} (${getPada(it)})")
            }
            groomChart?.let {
                appendLine("$groomLabel ${it.birthData.name ?: naText}")
                appendLine("  $moonSignLabel ${getRashiName(it)}")
                appendLine("  $nakshatraLabel ${getNakshatraName(it)} (${getPada(it)})")
            }
            appendLine()

            appendLine(StringResources.get(StringKey.REPORT_COMPATIBILITY_SCORE, language))
            appendLine("─────────────────────────────────────")
            appendLine("${StringResources.get(StringKey.REPORT_TOTAL_POINTS, language)} ${String.format("%.1f", result.totalPoints)} / ${result.maxPoints.toInt()}")
            appendLine("${StringResources.get(StringKey.REPORT_PERCENTAGE, language)} ${String.format("%.1f", result.percentage)}%")
            appendLine("${StringResources.get(StringKey.REPORT_RATING_LABEL, language)} ${result.rating.displayName}")
            appendLine()

            appendLine(StringResources.get(StringKey.REPORT_ASHTAKOOTA_8_GUNA, language))
            appendLine("─────────────────────────────────────")
            result.gunaAnalyses.forEach { guna ->
                val status = if (guna.isPositive) "✓" else "✗"
                appendLine("$status ${guna.name} (${guna.description})")
                appendLine("  ${StringResources.get(StringKey.REPORT_SCORE_LABEL, language)} ${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}")
                appendLine("  ${StringResources.get(StringKey.BRIDE, language)}: ${guna.brideValue} | ${StringResources.get(StringKey.GROOM, language)}: ${guna.groomValue}")
                appendLine("  ${guna.analysis}")
                appendLine()
            }

            appendLine(StringResources.get(StringKey.REPORT_MANGLIK_DOSHA_ANALYSIS, language))
            appendLine("─────────────────────────────────────")
            appendLine("${StringResources.get(StringKey.REPORT_COMPATIBILITY_LABEL, language)} ${result.manglikCompatibility}")
            appendLine()
            appendLine("${StringResources.get(StringKey.BRIDE, language)}: ${result.brideManglik.effectiveDosha.displayName}")
            if (result.brideManglik.marsHouse > 0) {
                appendLine("  ${String.format(StringResources.get(StringKey.REPORT_MARS_IN_HOUSE, language), result.brideManglik.marsHouse)}")
            }
            result.brideManglik.factors.forEach { appendLine("  • $it") }
            result.brideManglik.cancellations.forEach { appendLine("  ✓ $it $cancellationText") }
            appendLine()
            appendLine("${StringResources.get(StringKey.GROOM, language)}: ${result.groomManglik.effectiveDosha.displayName}")
            if (result.groomManglik.marsHouse > 0) {
                appendLine("  ${String.format(StringResources.get(StringKey.REPORT_MARS_IN_HOUSE, language), result.groomManglik.marsHouse)}")
            }
            result.groomManglik.factors.forEach { appendLine("  • $it") }
            result.groomManglik.cancellations.forEach { appendLine("  ✓ $it $cancellationText") }
            appendLine()

            if (result.specialConsiderations.isNotEmpty()) {
                appendLine(StringResources.get(StringKey.REPORT_SPECIAL_CONSIDERATIONS, language))
                appendLine("─────────────────────────────────────")
                result.specialConsiderations.forEach { appendLine("• $it") }
                appendLine()
            }

            appendLine(StringResources.get(StringKey.REPORT_SUGGESTED_REMEDIES, language))
            appendLine("─────────────────────────────────────")
            result.remedies.forEachIndexed { index, remedy ->
                appendLine("${index + 1}. $remedy")
            }
            appendLine()

            appendLine("═══════════════════════════════════════")
            appendLine(StringResources.get(StringKey.REPORT_GENERATED_BY, language))
            appendLine(StringResources.get(StringKey.REPORT_ASTROSTORM_VEDIC, language))
            appendLine("═══════════════════════════════════════")
        }
    }

    /**
     * Generate a brief summary report suitable for quick sharing.
     *
     * Includes:
     * - Names of bride and groom
     * - Overall score and percentage
     * - Rating with description
     * - Manglik compatibility status
     */
    fun generateSummaryReport(
        result: MatchmakingResult,
        brideChart: VedicChart?,
        groomChart: VedicChart?,
        language: Language
    ): String {
        val naText = StringResources.get(StringKey.REPORT_NA, language)
        val brideLabel = StringResources.get(StringKey.BRIDE, language)
        val groomLabel = StringResources.get(StringKey.GROOM, language)

        return buildString {
            appendLine("🔮 ${StringResources.get(StringKey.REPORT_KUNDLI_MILAN_SUMMARY, language)}")
            appendLine()
            brideChart?.let { appendLine("👰 $brideLabel: ${it.birthData.name ?: naText}") }
            groomChart?.let { appendLine("🤵 $groomLabel: ${it.birthData.name ?: naText}") }
            appendLine()
            appendLine("⭐ ${StringResources.get(StringKey.REPORT_SCORE_LABEL, language)} ${String.format("%.1f", result.totalPoints)}/${result.maxPoints.toInt()} (${String.format("%.1f", result.percentage)}%)")
            appendLine("📊 ${StringResources.get(StringKey.REPORT_RATING_LABEL, language)} ${result.rating.displayName}")
            appendLine()
            appendLine("${result.rating.description}")
            appendLine()
            appendLine("🔴 ${StringResources.get(StringKey.REPORT_MANGLIK_LABEL, language)} ${result.manglikCompatibility}")
            appendLine()
            appendLine("— ${StringResources.get(StringKey.REPORT_GENERATED_BY, language)}")
        }
    }

    /**
     * Generate a scores-only report showing just the Guna points.
     *
     * Useful for quick reference of individual scores.
     */
    fun generateScoresReport(
        result: MatchmakingResult,
        language: Language
    ): String {
        return buildString {
            appendLine(StringResources.get(StringKey.REPORT_ASHTAKOOTA_GUNA_SCORES, language))
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            result.gunaAnalyses.forEach { guna ->
                val emoji = if (guna.isPositive) "✅" else "⚠️"
                appendLine("$emoji ${guna.name}: ${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("${StringResources.get(StringKey.REPORT_TOTAL, language)}: ${String.format("%.1f", result.totalPoints)}/${result.maxPoints.toInt()}")
            appendLine()
            appendLine("— AstroStorm")
        }
    }

    // ============================================
    // CHART DATA EXTRACTION HELPERS
    // ============================================

    private fun getMoonPosition(chart: VedicChart) = chart.planetPositions.find {
        it.planet == Planet.MOON
    }

    /**
     * Get the nakshatra name from a chart's Moon position.
     */
    fun getNakshatraName(chart: VedicChart): String {
        return getMoonPosition(chart)?.nakshatra?.displayName ?: "N/A"
    }

    /**
     * Get the rashi (moon sign) name from a chart.
     */
    fun getRashiName(chart: VedicChart): String {
        val moonPosition = getMoonPosition(chart) ?: return "N/A"
        return moonPosition.sign.displayName
    }

    /**
     * Get the pada (quarter) from a chart's Moon position.
     */
    fun getPada(chart: VedicChart): String {
        val moonPosition = getMoonPosition(chart) ?: return "N/A"
        return "Pada ${moonPosition.nakshatraPada}"
    }

    /**
     * Get the nakshatra lord from a chart's Moon position.
     */
    fun getNakshatraLord(chart: VedicChart): String {
        val moonPosition = getMoonPosition(chart) ?: return "N/A"
        return moonPosition.nakshatra.ruler.displayName
    }
}

/**
 * Extension functions for quick Manglik status determination.
 */
fun MatchmakingResult.getManglikQuickStatus(): String {
    val brideStatus = brideManglik.effectiveDosha
    val groomStatus = groomManglik.effectiveDosha

    return when {
        brideStatus == ManglikDosha.NONE && groomStatus == ManglikDosha.NONE -> "No Dosha"
        brideStatus != ManglikDosha.NONE && groomStatus != ManglikDosha.NONE -> "Both Manglik"
        brideStatus != ManglikDosha.NONE -> "Bride Only"
        groomStatus != ManglikDosha.NONE -> "Groom Only"
        else -> "Check Details"
    }
}
