package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.*
import kotlin.math.abs

/**
 * Main Matchmaking Calculator - Orchestrates Vedic compatibility analysis
 *
 * This is the entry point for Kundli Milan (Ashtakoota Guna matching).
 * Delegates to specialized calculators for actual computations:
 * - GunaMilanCalculator: 8 Guna (Koota) calculations
 * - ManglikDoshaCalculator: Manglik Dosha analysis
 *
 * Based on classical texts:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Muhurta Chintamani
 * - Jataka Parijata
 */
object MatchmakingCalculator {

    /**
     * Main entry point for matchmaking calculation
     *
     * @param brideChart The bride's Vedic chart
     * @param groomChart The groom's Vedic chart
     * @param language Language for localized analysis text (default: English)
     * @return Complete MatchmakingResult with all analyses
     */
    fun calculateMatchmaking(
        brideChart: VedicChart,
        groomChart: VedicChart,
        language: Language = Language.ENGLISH
    ): MatchmakingResult {
        val brideMoon = brideChart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Bride chart missing Moon position")
        val groomMoon = groomChart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Groom chart missing Moon position")

        val brideMoonSign = brideMoon.sign
        val groomMoonSign = groomMoon.sign
        val (brideNakshatra, bridePada) = Nakshatra.fromLongitude(brideMoon.longitude)
        val (groomNakshatra, groomPada) = Nakshatra.fromLongitude(groomMoon.longitude)

        // Delegate to GunaMilanCalculator for 8 Guna analysis
        val gunaAnalyses = GunaMilanCalculator.calculateAllGunas(
            brideMoonSign = brideMoonSign,
            groomMoonSign = groomMoonSign,
            brideNakshatra = brideNakshatra,
            groomNakshatra = groomNakshatra,
            bridePada = bridePada,
            groomPada = groomPada,
            language = language
        )

        val totalPoints = gunaAnalyses.sumOf { it.obtainedPoints }
        val percentage = (totalPoints / MatchmakingConstants.MAX_TOTAL) * 100.0

        val nadiScore = gunaAnalyses.find { it.name == "Nadi" }?.obtainedPoints ?: 0.0
        val bhakootScore = gunaAnalyses.find { it.name == "Bhakoot" }?.obtainedPoints ?: 0.0
        val rating = CompatibilityRating.fromScore(totalPoints, nadiScore, bhakootScore)

        // Delegate to ManglikDoshaCalculator for Manglik analysis
        val brideManglik = ManglikDoshaCalculator.calculate(brideChart, "Bride")
        val groomManglik = ManglikDoshaCalculator.calculate(groomChart, "Groom")
        val manglikCompatibility = ManglikDoshaCalculator.assessCompatibility(brideManglik, groomManglik, language)

        // Calculate additional factors
        val additionalFactors = calculateAdditionalFactors(brideNakshatra, groomNakshatra, language)

        // Generate special considerations
        val specialConsiderations = calculateSpecialConsiderations(
            brideChart, groomChart, gunaAnalyses, brideManglik, groomManglik, additionalFactors, language
        )

        // Generate remedies
        val remedies = calculateRemedies(
            gunaAnalyses, brideManglik, groomManglik, totalPoints, additionalFactors, language
        )

        // Generate summary
        val summary = generateSummary(
            totalPoints, rating, gunaAnalyses, brideManglik, groomManglik, additionalFactors, language
        )

        // Generate detailed analysis
        val detailedAnalysis = generateDetailedAnalysis(
            brideChart, groomChart, gunaAnalyses, additionalFactors, language
        )

        return MatchmakingResult(
            brideChart = brideChart,
            groomChart = groomChart,
            gunaAnalyses = gunaAnalyses,
            totalPoints = totalPoints,
            maxPoints = MatchmakingConstants.MAX_TOTAL,
            percentage = percentage,
            rating = rating,
            brideManglik = brideManglik,
            groomManglik = groomManglik,
            manglikCompatibility = manglikCompatibility,
            additionalFactors = additionalFactors,
            specialConsiderations = specialConsiderations,
            remedies = remedies,
            summary = summary,
            detailedAnalysis = detailedAnalysis
        )
    }

    // ============================================
    // ADDITIONAL FACTORS
    // ============================================

    /**
     * Calculate additional compatibility factors beyond Ashtakoot
     *
     * Includes:
     * - Vedha (Nakshatra obstruction)
     * - Rajju (Cosmic bond/body part)
     * - Stree Deergha (Wife's prosperity)
     * - Mahendra (Longevity & progeny)
     */
    private fun calculateAdditionalFactors(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        language: Language
    ): AdditionalFactors {
        // Vedha check
        val vedhaResult = checkVedha(brideNakshatra, groomNakshatra, language)

        // Enhanced Rajju check with Arudha consideration
        val brideRajju = Rajju.fromNakshatra(brideNakshatra)
        val groomRajju = Rajju.fromNakshatra(groomNakshatra)
        val brideArudha = RajjuArudha.fromNakshatra(brideNakshatra)
        val groomArudha = RajjuArudha.fromNakshatra(groomNakshatra)

        val (rajjuCompatible, rajjuDetails) = evaluateRajjuCompatibility(
            brideRajju, groomRajju, brideArudha, groomArudha, language
        )

        // Stree Deergha
        val streeDeerghaDiff = (groomNakshatra.number - brideNakshatra.number + 27) % 27
        val streeDeerghaSatisfied = streeDeerghaDiff >= 13 || streeDeerghaDiff == 0

        // Mahendra
        val mahendraCount = ((groomNakshatra.number - brideNakshatra.number + 27) % 27) + 1
        val mahendraSatisfied = mahendraCount in MatchmakingConstants.MAHENDRA_POSITIONS
        val mahendraDetails = if (mahendraSatisfied) {
            StringResources.get(StringKey.MAHENDRA_FAVORABLE, language)
                .replace("{count}", mahendraCount.toString())
        } else {
            StringResources.get(StringKey.MAHENDRA_NOT_APPLICABLE, language)
        }

        return AdditionalFactors(
            vedhaPresent = vedhaResult.first,
            vedhaDetails = vedhaResult.second,
            rajjuCompatible = rajjuCompatible,
            rajjuDetails = rajjuDetails,
            brideRajju = brideRajju,
            groomRajju = groomRajju,
            brideArudha = brideArudha,
            groomArudha = groomArudha,
            streeDeerghaSatisfied = streeDeerghaSatisfied,
            streeDeerghaDiff = streeDeerghaDiff,
            mahendraSatisfied = mahendraSatisfied,
            mahendraDetails = mahendraDetails
        )
    }

    private fun checkVedha(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        language: Language
    ): Pair<Boolean, String> {
        val hasVedha = VedhaPairs.hasVedha(brideNakshatra, groomNakshatra)

        return if (hasVedha) {
            Pair(
                true,
                StringResources.get(StringKey.VEDHA_PRESENT, language)
                    .replace("{nak1}", brideNakshatra.getLocalizedName(language))
                    .replace("{nak2}", groomNakshatra.getLocalizedName(language))
            )
        } else {
            Pair(false, StringResources.get(StringKey.VEDHA_NOT_PRESENT, language))
        }
    }

    /**
     * Evaluate Rajju compatibility considering Arudha
     */
    private fun evaluateRajjuCompatibility(
        brideRajju: Rajju,
        groomRajju: Rajju,
        brideArudha: RajjuArudha,
        groomArudha: RajjuArudha,
        language: Language
    ): Pair<Boolean, String> {
        // Different Rajju - fully compatible
        if (brideRajju != groomRajju) {
            return Pair(true, StringResources.get(StringKey.RAJJU_COMPATIBLE, language))
        }

        // Same Rajju but different Arudha - partial compatibility
        if (brideArudha != groomArudha) {
            return Pair(
                true,
                StringResources.get(StringKey.RAJJU_SAME_DIFF_ARUDHA, language)
                    .replace("{rajju}", brideRajju.getLocalizedName(language))
                    .replace("{arudha1}", brideArudha.displayName)
                    .replace("{arudha2}", groomArudha.displayName)
            )
        }

        // Same Rajju and same Arudha - most problematic
        return Pair(
            false,
            StringResources.get(StringKey.RAJJU_SAME_SAME_ARUDHA, language)
                .replace("{rajju}", brideRajju.getLocalizedName(language))
                .replace("{body}", brideRajju.getLocalizedBodyPart(language))
                .replace("{arudha}", brideArudha.displayName)
                .replace("{warning}", brideRajju.getWarning(language))
        )
    }

    // ============================================
    // SPECIAL CONSIDERATIONS
    // ============================================

    private fun calculateSpecialConsiderations(
        brideChart: VedicChart,
        groomChart: VedicChart,
        gunaAnalyses: List<GunaAnalysis>,
        brideManglik: ManglikAnalysis,
        groomManglik: ManglikAnalysis,
        additionalFactors: AdditionalFactors,
        language: Language
    ): List<String> {
        val considerations = mutableListOf<String>()

        val nadiAnalysis = gunaAnalyses.find { it.name == "Nadi" }
        if (nadiAnalysis != null && nadiAnalysis.obtainedPoints == 0.0) {
            considerations.add(StringResources.get(StringKey.SPECIAL_NADI_DOSHA, language))
        }

        val bhakootAnalysis = gunaAnalyses.find { it.name == "Bhakoot" }
        if (bhakootAnalysis != null && bhakootAnalysis.obtainedPoints == 0.0) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_BHAKOOT_DOSHA, language)
                    .replace("{analysis}", bhakootAnalysis.analysis)
            )
        }

        if (brideManglik.effectiveDosha.severity > 0 && groomManglik.effectiveDosha.severity == 0) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_BRIDE_MANGLIK, language)
                    .replace("{dosha}", brideManglik.effectiveDosha.getLocalizedName(language))
            )
        } else if (groomManglik.effectiveDosha.severity > 0 && brideManglik.effectiveDosha.severity == 0) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_GROOM_MANGLIK, language)
                    .replace("{dosha}", groomManglik.effectiveDosha.getLocalizedName(language))
            )
        }

        val ganaAnalysis = gunaAnalyses.find { it.name == "Gana" }
        if (ganaAnalysis != null && ganaAnalysis.obtainedPoints == 0.0) {
            considerations.add(StringResources.get(StringKey.SPECIAL_GANA_INCOMPAT, language))
        }

        val yoniAnalysis = gunaAnalyses.find { it.name == "Yoni" }
        if (yoniAnalysis != null && yoniAnalysis.obtainedPoints == 0.0) {
            considerations.add(StringResources.get(StringKey.SPECIAL_YONI_INCOMPAT, language))
        }

        if (additionalFactors.vedhaPresent) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_VEDHA, language)
                    .replace("{details}", additionalFactors.vedhaDetails)
            )
        }

        if (!additionalFactors.rajjuCompatible) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_RAJJU, language)
                    .replace("{details}", additionalFactors.rajjuDetails)
            )
        }

        if (!additionalFactors.streeDeerghaSatisfied) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_STREE_DEERGHA, language)
                    .replace("{diff}", additionalFactors.streeDeerghaDiff.toString())
            )
        }

        val lowScores = gunaAnalyses.count { !it.isPositive }
        if (lowScores >= 4) {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_MULTIPLE_LOW, language)
                    .replace("{count}", lowScores.toString())
            )
        }

        // Check 7th house lords
        val brideAscendant = ZodiacSign.fromLongitude(brideChart.ascendant)
        val groomAscendant = ZodiacSign.fromLongitude(groomChart.ascendant)
        val bride7thLord = ZodiacSign.entries[(brideAscendant.number + 5) % 12].ruler
        val groom7thLord = ZodiacSign.entries[(groomAscendant.number + 5) % 12].ruler

        if (PlanetaryFriendship.getRelationship(bride7thLord, groom7thLord) == "Enemy" &&
            PlanetaryFriendship.getRelationship(groom7thLord, bride7thLord) == "Enemy") {
            considerations.add(
                StringResources.get(StringKey.SPECIAL_7TH_LORDS_ENEMY, language)
                    .replace("{lord1}", bride7thLord.getLocalizedName(language))
                    .replace("{lord2}", groom7thLord.getLocalizedName(language))
            )
        }

        if (considerations.isEmpty()) {
            considerations.add(StringResources.get(StringKey.SPECIAL_NO_ISSUES, language))
        }

        return considerations
    }

    // ============================================
    // REMEDIES
    // ============================================

    private fun calculateRemedies(
        gunaAnalyses: List<GunaAnalysis>,
        brideManglik: ManglikAnalysis,
        groomManglik: ManglikAnalysis,
        totalPoints: Double,
        additionalFactors: AdditionalFactors,
        language: Language
    ): List<String> {
        val remedies = mutableListOf<String>()

        val nadiAnalysis = gunaAnalyses.find { it.name == "Nadi" }
        if (nadiAnalysis != null && nadiAnalysis.obtainedPoints == 0.0) {
            remedies.add(StringResources.get(StringKey.REMEDY_NADI_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_NADI_2, language))
            remedies.add(StringResources.get(StringKey.REMEDY_NADI_3, language))
        }

        val bhakootAnalysis = gunaAnalyses.find { it.name == "Bhakoot" }
        if (bhakootAnalysis != null && bhakootAnalysis.obtainedPoints == 0.0) {
            remedies.add(StringResources.get(StringKey.REMEDY_BHAKOOT_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_BHAKOOT_2, language))
            if (bhakootAnalysis.analysis.contains("6-8")) {
                remedies.add(StringResources.get(StringKey.REMEDY_SHADASHTAK, language))
            }
        }

        if (brideManglik.effectiveDosha.severity > 0 || groomManglik.effectiveDosha.severity > 0) {
            remedies.add(StringResources.get(StringKey.REMEDY_MANGLIK_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_MANGLIK_2, language))
            remedies.add(StringResources.get(StringKey.REMEDY_MANGLIK_3, language))

            if (brideManglik.effectiveDosha.severity > 0) {
                remedies.add(StringResources.get(StringKey.REMEDY_MANGLIK_BRIDE, language))
            }
            if (groomManglik.effectiveDosha.severity > 0) {
                remedies.add(StringResources.get(StringKey.REMEDY_MANGLIK_GROOM, language))
            }

            if (brideManglik.effectiveDosha == ManglikDosha.DOUBLE ||
                groomManglik.effectiveDosha == ManglikDosha.DOUBLE) {
                remedies.add(StringResources.get(StringKey.REMEDY_DOUBLE_MANGLIK, language))
            }
        }

        val ganaAnalysis = gunaAnalyses.find { it.name == "Gana" }
        if (ganaAnalysis != null && ganaAnalysis.obtainedPoints <= 1.0) {
            remedies.add(StringResources.get(StringKey.REMEDY_GANA_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_GANA_2, language))
            remedies.add(StringResources.get(StringKey.REMEDY_GANA_3, language))
        }

        val grahaMaitri = gunaAnalyses.find { it.name == "Graha Maitri" }
        if (grahaMaitri != null && grahaMaitri.obtainedPoints <= 1.0) {
            remedies.add(StringResources.get(StringKey.REMEDY_GRAHA_MAITRI_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_GRAHA_MAITRI_2, language))
        }

        val yoniAnalysis = gunaAnalyses.find { it.name == "Yoni" }
        if (yoniAnalysis != null && yoniAnalysis.obtainedPoints == 0.0) {
            remedies.add(StringResources.get(StringKey.REMEDY_YONI_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_YONI_2, language))
        }

        if (additionalFactors.vedhaPresent) {
            remedies.add(StringResources.get(StringKey.REMEDY_VEDHA_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_VEDHA_2, language))
        }

        if (!additionalFactors.rajjuCompatible) {
            val rajjuRemedy = when (additionalFactors.brideRajju) {
                Rajju.SIRO -> StringResources.get(StringKey.REMEDY_RAJJU_SIRO, language)
                Rajju.KANTHA -> StringResources.get(StringKey.REMEDY_RAJJU_KANTHA, language)
                Rajju.NABHI -> StringResources.get(StringKey.REMEDY_RAJJU_NABHI, language)
                Rajju.KATI -> StringResources.get(StringKey.REMEDY_RAJJU_KATI, language)
                Rajju.PADA -> StringResources.get(StringKey.REMEDY_RAJJU_PADA, language)
            }
            remedies.add(rajjuRemedy)
        }

        if (totalPoints < MatchmakingConstants.AVERAGE_THRESHOLD) {
            remedies.add(StringResources.get(StringKey.REMEDY_GENERAL_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_GENERAL_2, language))
            remedies.add(StringResources.get(StringKey.REMEDY_GENERAL_3, language))
            remedies.add(StringResources.get(StringKey.REMEDY_GENERAL_4, language))
        }

        if (totalPoints < MatchmakingConstants.POOR_THRESHOLD) {
            remedies.add(StringResources.get(StringKey.REMEDY_SERIOUS_1, language))
            remedies.add(StringResources.get(StringKey.REMEDY_SERIOUS_2, language))
        }

        if (remedies.isEmpty()) {
            remedies.add(StringResources.get(StringKey.REMEDY_NONE_NEEDED, language))
            remedies.add(StringResources.get(StringKey.REMEDY_SATYANARAYAN, language))
        }

        return remedies
    }

    // ============================================
    // SUMMARY GENERATION
    // ============================================

    private fun generateSummary(
        totalPoints: Double,
        rating: CompatibilityRating,
        gunaAnalyses: List<GunaAnalysis>,
        brideManglik: ManglikAnalysis,
        groomManglik: ManglikAnalysis,
        additionalFactors: AdditionalFactors,
        language: Language
    ): String {
        val strongPoints = gunaAnalyses.filter { it.isPositive && it.obtainedPoints >= it.maxPoints * 0.7 }
        val weakPoints = gunaAnalyses.filter { !it.isPositive || it.obtainedPoints < it.maxPoints * 0.5 }
        val percentageScore = (totalPoints / MatchmakingConstants.MAX_TOTAL) * 100.0

        return buildString {
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("                    ${StringResources.get(StringKey.SUMMARY_TITLE, language)}")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine("${StringResources.get(StringKey.SUMMARY_OVERALL_SCORE, language)}: ${String.format("%.1f", totalPoints)} / 36 (${String.format("%.1f", percentageScore)}%)")
            appendLine("${StringResources.get(StringKey.SUMMARY_RATING, language)}: ${rating.getLocalizedName(language)}")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")

            if (strongPoints.isNotEmpty()) {
                appendLine()
                appendLine("${StringResources.get(StringKey.SUMMARY_STRENGTHS, language)} (${strongPoints.size}):")
                strongPoints.forEach { guna ->
                    appendLine("  - ${guna.name} (${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()})")
                }
            }

            if (weakPoints.isNotEmpty()) {
                appendLine()
                appendLine("${StringResources.get(StringKey.SUMMARY_CONCERNS, language)} (${weakPoints.size}):")
                weakPoints.forEach { guna ->
                    appendLine("  - ${guna.name} (${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()})")
                }
            }

            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")
            appendLine()
            appendLine("${StringResources.get(StringKey.SUMMARY_MANGLIK, language)}:")
            appendLine("  ${StringResources.get(StringKey.BRIDE, language)}: ${brideManglik.effectiveDosha.getLocalizedName(language)}")
            appendLine("  ${StringResources.get(StringKey.GROOM, language)}: ${groomManglik.effectiveDosha.getLocalizedName(language)}")

            val manglikOk = (brideManglik.effectiveDosha.severity == 0 && groomManglik.effectiveDosha.severity == 0) ||
                (brideManglik.effectiveDosha.severity > 0 && groomManglik.effectiveDosha.severity > 0)
            appendLine("  ${StringResources.get(StringKey.STATUS, language)}: ${if (manglikOk) StringResources.get(StringKey.COMPATIBLE, language) else StringResources.get(StringKey.NEEDS_ATTENTION, language)}")

            appendLine()
            appendLine("${StringResources.get(StringKey.SUMMARY_ADDITIONAL, language)}:")
            appendLine("  Vedha: ${if (additionalFactors.vedhaPresent) StringResources.get(StringKey.PRESENT, language) else StringResources.get(StringKey.NOT_PRESENT, language)}")
            appendLine("  Rajju: ${if (additionalFactors.rajjuCompatible) StringResources.get(StringKey.COMPATIBLE, language) else StringResources.get(StringKey.SAME_RAJJU, language)}")
            appendLine("  Stree Deergha: ${if (additionalFactors.streeDeerghaSatisfied) StringResources.get(StringKey.SATISFIED, language) else StringResources.get(StringKey.NOT_SATISFIED, language)}")
            appendLine("  Mahendra: ${if (additionalFactors.mahendraSatisfied) StringResources.get(StringKey.FAVORABLE, language) else StringResources.get(StringKey.NOT_APPLICABLE, language)}")

            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")
            appendLine()
            appendLine("${StringResources.get(StringKey.SUMMARY_RECOMMENDATION, language)}:")
            appendLine()
            appendLine(rating.getLocalizedDescription(language))
        }
    }

    // ============================================
    // DETAILED ANALYSIS GENERATION
    // ============================================

    private fun generateDetailedAnalysis(
        brideChart: VedicChart,
        groomChart: VedicChart,
        gunaAnalyses: List<GunaAnalysis>,
        additionalFactors: AdditionalFactors,
        language: Language
    ): String {
        val brideMoon = brideChart.planetPositions.find { it.planet == Planet.MOON }
        val groomMoon = groomChart.planetPositions.find { it.planet == Planet.MOON }

        return buildString {
            appendLine("═══════════════════════════════════════════════════════════════════")
            appendLine("                    ${StringResources.get(StringKey.DETAILED_TITLE, language)}")
            appendLine("═══════════════════════════════════════════════════════════════════")
            appendLine()

            appendLine("${StringResources.get(StringKey.BIRTH_DATA_SUMMARY, language)}")
            appendLine("─────────────────────────────────────────────────────────────────────")
            appendLine()
            appendLine("${StringResources.get(StringKey.BRIDE, language).uppercase()}: ${brideChart.birthData.name}")
            if (brideMoon != null) {
                val (brideNakshatra, bridePada) = Nakshatra.fromLongitude(brideMoon.longitude)
                appendLine("  ${StringResources.get(StringKey.MOON_SIGN, language)}: ${brideMoon.sign.getLocalizedName(language)}")
                appendLine("  ${StringResources.get(StringKey.NAKSHATRA_LABEL, language)}: ${brideNakshatra.getLocalizedName(language)} (${StringResources.get(StringKey.PADA_LABEL, language)} $bridePada)")
                appendLine("  ${StringResources.get(StringKey.MOON_LONGITUDE, language)}: ${String.format("%.2f", brideMoon.longitude)}°")
            }
            appendLine()
            appendLine("${StringResources.get(StringKey.GROOM, language).uppercase()}: ${groomChart.birthData.name}")
            if (groomMoon != null) {
                val (groomNakshatra, groomPada) = Nakshatra.fromLongitude(groomMoon.longitude)
                appendLine("  ${StringResources.get(StringKey.MOON_SIGN, language)}: ${groomMoon.sign.getLocalizedName(language)}")
                appendLine("  ${StringResources.get(StringKey.NAKSHATRA_LABEL, language)}: ${groomNakshatra.getLocalizedName(language)} (${StringResources.get(StringKey.PADA_LABEL, language)} $groomPada)")
                appendLine("  ${StringResources.get(StringKey.MOON_LONGITUDE, language)}: ${String.format("%.2f", groomMoon.longitude)}°")
            }

            appendLine()
            appendLine("═══════════════════════════════════════════════════════════════════")
            appendLine()

            gunaAnalyses.forEach { guna ->
                val scoreBar = buildScoreBar(guna.obtainedPoints, guna.maxPoints)
                val status = if (guna.isPositive) StringResources.get(StringKey.FAVORABLE, language) else StringResources.get(StringKey.NEEDS_ATTENTION, language)

                appendLine("┌─────────────────────────────────────────────────────────────────┐")
                appendLine("│ ${guna.name.uppercase().padEnd(20)} ${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()} points    $status")
                appendLine("├─────────────────────────────────────────────────────────────────┤")
                appendLine("│ $scoreBar")
                appendLine("│")
                appendLine("│ ${StringResources.get(StringKey.PURPOSE, language)}: ${guna.description}")
                appendLine("│")
                appendLine("│ ${StringResources.get(StringKey.BRIDE, language)}: ${guna.brideValue}")
                appendLine("│ ${StringResources.get(StringKey.GROOM, language)}: ${guna.groomValue}")
                appendLine("│")
                appendLine("│ ${StringResources.get(StringKey.ANALYSIS_LABEL, language)}:")
                wrapText(guna.analysis, 65).forEach { line ->
                    appendLine("│   $line")
                }
                appendLine("└─────────────────────────────────────────────────────────────────┘")
                appendLine()
            }

            appendLine("═══════════════════════════════════════════════════════════════════")
            appendLine()
            appendLine("${StringResources.get(StringKey.ADDITIONAL_FACTORS_TITLE, language)}")
            appendLine("─────────────────────────────────────────────────────────────────────")
            appendLine()

            appendLine("VEDHA (${StringResources.get(StringKey.OBSTRUCTION, language)})")
            appendLine("  ${StringResources.get(StringKey.STATUS, language)}: ${if (additionalFactors.vedhaPresent) StringResources.get(StringKey.PRESENT, language) else StringResources.get(StringKey.NOT_PRESENT, language)}")
            appendLine("  ${StringResources.get(StringKey.DETAILS, language)}: ${additionalFactors.vedhaDetails}")
            appendLine()

            appendLine("RAJJU (${StringResources.get(StringKey.COSMIC_BOND, language)})")
            appendLine("  ${StringResources.get(StringKey.BRIDE, language)}: ${additionalFactors.brideRajju.getLocalizedName(language)} (${additionalFactors.brideRajju.getLocalizedBodyPart(language)})")
            appendLine("  ${StringResources.get(StringKey.GROOM, language)}: ${additionalFactors.groomRajju.getLocalizedName(language)} (${additionalFactors.groomRajju.getLocalizedBodyPart(language)})")
            appendLine("  ${StringResources.get(StringKey.STATUS, language)}: ${if (additionalFactors.rajjuCompatible) StringResources.get(StringKey.COMPATIBLE, language) else StringResources.get(StringKey.SAME_RAJJU, language)}")
            if (!additionalFactors.rajjuCompatible) {
                appendLine("  ${StringResources.get(StringKey.WARNING_LABEL, language)}: ${additionalFactors.rajjuDetails}")
            }
            appendLine()

            appendLine("STREE DEERGHA (${StringResources.get(StringKey.WIFE_PROSPERITY, language)})")
            appendLine("  ${StringResources.get(StringKey.NAKSHATRA_DIFF, language)}: ${additionalFactors.streeDeerghaDiff}")
            appendLine("  ${StringResources.get(StringKey.STATUS, language)}: ${if (additionalFactors.streeDeerghaSatisfied) StringResources.get(StringKey.SATISFIED, language) else StringResources.get(StringKey.NOT_SATISFIED, language)}")
            appendLine()

            appendLine("MAHENDRA (${StringResources.get(StringKey.LONGEVITY_PROSPERITY, language)})")
            appendLine("  ${StringResources.get(StringKey.STATUS, language)}: ${if (additionalFactors.mahendraSatisfied) StringResources.get(StringKey.FAVORABLE, language) else StringResources.get(StringKey.NOT_APPLICABLE, language)}")
            appendLine("  ${StringResources.get(StringKey.DETAILS, language)}: ${additionalFactors.mahendraDetails}")
            appendLine()

            appendLine("═══════════════════════════════════════════════════════════════════")
        }
    }

    private fun buildScoreBar(obtained: Double, max: Double): String {
        val percentage = (obtained / max * 100).toInt()
        val filledBlocks = (percentage / 10)
        val emptyBlocks = 10 - filledBlocks

        val bar = "█".repeat(filledBlocks) + "░".repeat(emptyBlocks)
        return "[$bar] ${String.format("%3d", percentage)}%"
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                currentLine.append(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    // ============================================
    // PUBLIC UTILITY METHODS
    // ============================================

    /**
     * Get detailed description of a Guna for educational purposes
     */
    fun getGunaDescription(gunaName: String, language: Language = Language.ENGLISH): String {
        return when (gunaName) {
            "Varna" -> StringResources.get(StringKey.GUNA_DESC_VARNA, language)
            "Vashya" -> StringResources.get(StringKey.GUNA_DESC_VASHYA, language)
            "Tara" -> StringResources.get(StringKey.GUNA_DESC_TARA, language)
            "Yoni" -> StringResources.get(StringKey.GUNA_DESC_YONI, language)
            "Graha Maitri" -> StringResources.get(StringKey.GUNA_DESC_GRAHA_MAITRI, language)
            "Gana" -> StringResources.get(StringKey.GUNA_DESC_GANA, language)
            "Bhakoot" -> StringResources.get(StringKey.GUNA_DESC_BHAKOOT, language)
            "Nadi" -> StringResources.get(StringKey.GUNA_DESC_NADI, language)
            else -> StringResources.get(StringKey.GUNA_DESC_NOT_AVAILABLE, language)
        }
    }

    fun getMinimumRecommendedScore(): Double = MatchmakingConstants.AVERAGE_THRESHOLD

    fun getMaximumPossibleScore(): Double = MatchmakingConstants.MAX_TOTAL

    fun interpretScore(score: Double, language: Language = Language.ENGLISH): String {
        return when {
            score >= MatchmakingConstants.EXCELLENT_THRESHOLD ->
                StringResources.get(StringKey.SCORE_EXCELLENT, language)
            score >= MatchmakingConstants.GOOD_THRESHOLD ->
                StringResources.get(StringKey.SCORE_GOOD, language)
            score >= MatchmakingConstants.AVERAGE_THRESHOLD ->
                StringResources.get(StringKey.SCORE_AVERAGE, language)
            score >= MatchmakingConstants.POOR_THRESHOLD ->
                StringResources.get(StringKey.SCORE_BELOW_AVERAGE, language)
            else ->
                StringResources.get(StringKey.SCORE_POOR, language)
        }
    }
}
