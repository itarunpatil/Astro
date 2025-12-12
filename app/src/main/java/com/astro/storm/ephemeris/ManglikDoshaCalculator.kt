package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign

/**
 * Manglik Dosha (Mangal Dosha / Kuja Dosha) Calculator
 *
 * Manglik Dosha is one of the most important considerations in Vedic matchmaking (Kundali Milan).
 * It occurs when Mars (Mangal/Kuja) is placed in certain houses from:
 * - Lagna (Ascendant)
 * - Moon
 * - Venus
 *
 * Traditional houses causing Manglik Dosha: 1st, 2nd, 4th, 7th, 8th, 12th
 *
 * The dosha indicates potential challenges in married life including:
 * - Conflicts and arguments
 * - Health issues to spouse
 * - Delay in marriage
 * - Separation or divorce
 *
 * However, numerous cancellation factors can nullify or reduce the dosha.
 *
 * This calculator provides:
 * - Complete Manglik analysis from Lagna, Moon, and Venus
 * - Severity assessment with percentage
 * - All cancellation factors check
 * - Personalized remedies
 * - Compatibility analysis for Manglik matching
 *
 * References:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Muhurta Chintamani
 * - Phaladeepika
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
object ManglikDoshaCalculator {

    /** Houses that cause Manglik Dosha when Mars is placed there */
    private val MANGLIK_HOUSES = setOf(1, 2, 4, 7, 8, 12)

    /** High intensity houses - more severe Manglik effects */
    private val HIGH_INTENSITY_HOUSES = setOf(7, 8)

    /** Medium intensity houses */
    private val MEDIUM_INTENSITY_HOUSES = setOf(1, 4, 12)

    /** Lower intensity house */
    private val LOW_INTENSITY_HOUSES = setOf(2)

    /**
     * Manglik Dosha severity levels
     */
    enum class ManglikLevel {
        /** No Manglik Dosha */
        NONE,
        /** Mild Manglik - only from one reference, with cancellations */
        MILD,
        /** Partial Manglik - from one or two references, some cancellations */
        PARTIAL,
        /** Full Manglik - from multiple references, few cancellations */
        FULL,
        /** Severe Manglik - from all references, no cancellations */
        SEVERE;

        fun getLocalizedName(language: Language): String = when (this) {
            NONE -> StringResources.get(StringKey.MANGLIK_NONE, language)
            MILD -> StringResources.get(StringKey.MANGLIK_MILD, language)
            PARTIAL -> StringResources.get(StringKey.MANGLIK_PARTIAL, language)
            FULL -> StringResources.get(StringKey.MANGLIK_FULL, language)
            SEVERE -> StringResources.get(StringKey.MANGLIK_SEVERE, language)
        }
    }

    /**
     * Cancellation reason for Manglik Dosha
     */
    data class CancellationFactor(
        val titleKey: StringKey,
        val descriptionKey: StringKey,
        val strength: CancellationStrength
    ) {
        fun getTitle(language: Language): String = StringResources.get(titleKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class CancellationStrength {
        FULL,    // Completely cancels the dosha
        STRONG,  // Significantly reduces the dosha
        PARTIAL  // Partially reduces the dosha
    }

    /**
     * Mars position analysis for Manglik calculation
     */
    data class MarsPositionAnalysis(
        val referencePoint: String,
        val referenceSign: ZodiacSign,
        val marsHouse: Int,
        val isManglik: Boolean,
        val intensity: Double // 0.0 to 1.0
    )

    /**
     * Complete Manglik Dosha analysis result
     */
    data class ManglikAnalysis(
        val isManglik: Boolean,
        val level: ManglikLevel,
        val overallIntensity: Double, // 0-100 percentage
        val marsPosition: PlanetPosition?,
        val marsSign: ZodiacSign?,
        val analysisFromLagna: MarsPositionAnalysis,
        val analysisFromMoon: MarsPositionAnalysis,
        val analysisFromVenus: MarsPositionAnalysis,
        val cancellationFactors: List<CancellationFactor>,
        val remainingIntensityAfterCancellations: Double,
        val effectiveLevel: ManglikLevel,
        val remedies: List<ManglikRemedy>,
        val interpretation: String,
        val marriageConsiderations: String
    ) {
        /**
         * Get a summary suitable for display
         */
        fun getSummary(language: Language): String {
            return if (isManglik) {
                val levelName = effectiveLevel.getLocalizedName(language)
                StringResources.get(StringKey.MANGLIK_SUMMARY_PRESENT, language)
                    .replace("{level}", levelName)
                    .replace("{intensity}", "%.1f".format(remainingIntensityAfterCancellations))
            } else {
                StringResources.get(StringKey.MANGLIK_SUMMARY_ABSENT, language)
            }
        }
    }

    /**
     * Manglik Dosha remedy
     */
    data class ManglikRemedy(
        val type: RemedyType,
        val titleKey: StringKey,
        val descriptionKey: StringKey,
        val effectiveness: String
    ) {
        fun getTitle(language: Language): String = StringResources.get(titleKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class RemedyType {
        RITUAL, GEMSTONE, MANTRA, CHARITY, MARRIAGE_REMEDY
    }

    /**
     * Calculate complete Manglik Dosha analysis for a chart
     *
     * @param chart The Vedic birth chart
     * @return Complete Manglik analysis
     */
    fun calculateManglikDosha(chart: VedicChart): ManglikAnalysis {
        val marsPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.MARS)
        val moonPosition = VedicAstrologyUtils.getMoonPosition(chart)
        val venusPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.VENUS)
        val ascendantSign = VedicAstrologyUtils.getAscendantSign(chart)

        // Calculate Mars house from each reference point
        val analysisFromLagna = analyzeFromReference(
            "Lagna",
            ascendantSign,
            marsPosition,
            chart
        )

        val analysisFromMoon = analyzeFromReference(
            "Moon",
            moonPosition?.sign ?: ascendantSign,
            marsPosition,
            chart
        )

        val analysisFromVenus = analyzeFromReference(
            "Venus",
            venusPosition?.sign ?: ascendantSign,
            marsPosition,
            chart
        )

        // Determine initial Manglik status
        val isManglikFromAny = analysisFromLagna.isManglik ||
            analysisFromMoon.isManglik ||
            analysisFromVenus.isManglik

        // Calculate raw intensity (before cancellations)
        val rawIntensity = calculateRawIntensity(
            analysisFromLagna,
            analysisFromMoon,
            analysisFromVenus
        )

        // Determine initial level
        val initialLevel = determineLevel(rawIntensity, isManglikFromAny)

        // Find all cancellation factors
        val cancellationFactors = findCancellationFactors(
            chart = chart,
            marsPosition = marsPosition,
            ascendantSign = ascendantSign,
            analysisFromLagna = analysisFromLagna,
            analysisFromMoon = analysisFromMoon,
            analysisFromVenus = analysisFromVenus
        )

        // Calculate intensity after cancellations
        val cancellationReduction = calculateCancellationReduction(cancellationFactors)
        val remainingIntensity = (rawIntensity * (1.0 - cancellationReduction)).coerceAtLeast(0.0)

        // Determine effective level after cancellations
        val effectiveLevel = if (cancellationFactors.any { it.strength == CancellationStrength.FULL }) {
            ManglikLevel.NONE
        } else {
            determineLevel(remainingIntensity, isManglikFromAny && remainingIntensity > 10)
        }

        // Get remedies based on effective level
        val remedies = getRemedies(effectiveLevel, marsPosition)

        // Generate interpretation
        val interpretation = generateInterpretation(
            isManglik = isManglikFromAny,
            level = initialLevel,
            effectiveLevel = effectiveLevel,
            marsPosition = marsPosition,
            analysisFromLagna = analysisFromLagna,
            analysisFromMoon = analysisFromMoon,
            analysisFromVenus = analysisFromVenus,
            cancellationFactors = cancellationFactors
        )

        // Generate marriage considerations
        val marriageConsiderations = generateMarriageConsiderations(
            effectiveLevel = effectiveLevel,
            cancellationFactors = cancellationFactors
        )

        return ManglikAnalysis(
            isManglik = isManglikFromAny,
            level = initialLevel,
            overallIntensity = rawIntensity,
            marsPosition = marsPosition,
            marsSign = marsPosition?.sign,
            analysisFromLagna = analysisFromLagna,
            analysisFromMoon = analysisFromMoon,
            analysisFromVenus = analysisFromVenus,
            cancellationFactors = cancellationFactors,
            remainingIntensityAfterCancellations = remainingIntensity,
            effectiveLevel = effectiveLevel,
            remedies = remedies,
            interpretation = interpretation,
            marriageConsiderations = marriageConsiderations
        )
    }

    private fun analyzeFromReference(
        referenceName: String,
        referenceSign: ZodiacSign,
        marsPosition: PlanetPosition?,
        chart: VedicChart
    ): MarsPositionAnalysis {
        if (marsPosition == null) {
            return MarsPositionAnalysis(
                referencePoint = referenceName,
                referenceSign = referenceSign,
                marsHouse = 0,
                isManglik = false,
                intensity = 0.0
            )
        }

        val marsHouse = VedicAstrologyUtils.getHouseFromSigns(marsPosition.sign, referenceSign)
        val isManglik = marsHouse in MANGLIK_HOUSES

        val intensity = when {
            marsHouse in HIGH_INTENSITY_HOUSES -> 1.0
            marsHouse in MEDIUM_INTENSITY_HOUSES -> 0.7
            marsHouse in LOW_INTENSITY_HOUSES -> 0.4
            else -> 0.0
        }

        return MarsPositionAnalysis(
            referencePoint = referenceName,
            referenceSign = referenceSign,
            marsHouse = marsHouse,
            isManglik = isManglik,
            intensity = intensity
        )
    }

    private fun calculateRawIntensity(
        fromLagna: MarsPositionAnalysis,
        fromMoon: MarsPositionAnalysis,
        fromVenus: MarsPositionAnalysis
    ): Double {
        // Weight: Lagna = 40%, Moon = 35%, Venus = 25%
        val lagnaContribution = fromLagna.intensity * 40.0
        val moonContribution = fromMoon.intensity * 35.0
        val venusContribution = fromVenus.intensity * 25.0

        return lagnaContribution + moonContribution + venusContribution
    }

    private fun determineLevel(intensity: Double, isManglik: Boolean): ManglikLevel {
        if (!isManglik || intensity == 0.0) return ManglikLevel.NONE

        return when {
            intensity >= 80 -> ManglikLevel.SEVERE
            intensity >= 60 -> ManglikLevel.FULL
            intensity >= 35 -> ManglikLevel.PARTIAL
            intensity >= 15 -> ManglikLevel.MILD
            else -> ManglikLevel.NONE
        }
    }

    private fun findCancellationFactors(
        chart: VedicChart,
        marsPosition: PlanetPosition?,
        ascendantSign: ZodiacSign,
        analysisFromLagna: MarsPositionAnalysis,
        analysisFromMoon: MarsPositionAnalysis,
        analysisFromVenus: MarsPositionAnalysis
    ): List<CancellationFactor> {
        if (marsPosition == null) return emptyList()

        val factors = mutableListOf<CancellationFactor>()

        // 1. Mars in own sign (Aries or Scorpio)
        if (marsPosition.sign == ZodiacSign.ARIES || marsPosition.sign == ZodiacSign.SCORPIO) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_OWN_SIGN_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_OWN_SIGN_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 2. Mars in exaltation (Capricorn)
        if (marsPosition.sign == ZodiacSign.CAPRICORN) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_EXALTED_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_EXALTED_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 3. Mars conjunct or aspected by benefics (Jupiter, Venus)
        val jupiter = VedicAstrologyUtils.getPlanetPosition(chart, Planet.JUPITER)
        val venus = VedicAstrologyUtils.getPlanetPosition(chart, Planet.VENUS)

        if (jupiter != null && jupiter.house == marsPosition.house) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_JUPITER_CONJUNCT_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_JUPITER_CONJUNCT_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        if (venus != null && venus.house == marsPosition.house) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_VENUS_CONJUNCT_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_VENUS_CONJUNCT_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 4. Jupiter aspects Mars (5th, 7th, 9th aspect)
        if (jupiter != null) {
            val jupiterAspects = VedicAstrologyUtils.getAspectedHouses(Planet.JUPITER, jupiter.house)
            if (marsPosition.house in jupiterAspects) {
                factors.add(CancellationFactor(
                    titleKey = StringKey.MANGLIK_CANCEL_JUPITER_ASPECT_TITLE,
                    descriptionKey = StringKey.MANGLIK_CANCEL_JUPITER_ASPECT_DESC,
                    strength = CancellationStrength.STRONG
                ))
            }
        }

        // 5. Mars in 2nd house in specific signs (Gemini, Virgo)
        if (analysisFromLagna.marsHouse == 2 &&
            (marsPosition.sign == ZodiacSign.GEMINI || marsPosition.sign == ZodiacSign.VIRGO)) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_SECOND_MERCURY_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_SECOND_MERCURY_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 6. Mars in 4th house in Aries or Scorpio
        if (analysisFromLagna.marsHouse == 4 &&
            (marsPosition.sign == ZodiacSign.ARIES || marsPosition.sign == ZodiacSign.SCORPIO)) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_FOURTH_OWN_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_FOURTH_OWN_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 7. Mars in 7th house in Cancer or Capricorn
        if (analysisFromLagna.marsHouse == 7 &&
            (marsPosition.sign == ZodiacSign.CANCER || marsPosition.sign == ZodiacSign.CAPRICORN)) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_SEVENTH_SPECIAL_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_SEVENTH_SPECIAL_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 8. Mars in 8th house in Sagittarius or Pisces
        if (analysisFromLagna.marsHouse == 8 &&
            (marsPosition.sign == ZodiacSign.SAGITTARIUS || marsPosition.sign == ZodiacSign.PISCES)) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_EIGHTH_JUPITER_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_EIGHTH_JUPITER_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 9. Mars in 12th house in Taurus or Libra
        if (analysisFromLagna.marsHouse == 12 &&
            (marsPosition.sign == ZodiacSign.TAURUS || marsPosition.sign == ZodiacSign.LIBRA)) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_TWELFTH_VENUS_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_TWELFTH_VENUS_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 10. Specific Ascendants where Mars is benefic
        // For Aries, Cancer, Leo, and Scorpio ascendants, Mars is a benefic
        if (ascendantSign in listOf(ZodiacSign.ARIES, ZodiacSign.CANCER, ZodiacSign.LEO, ZodiacSign.SCORPIO)) {
            factors.add(CancellationFactor(
                titleKey = StringKey.MANGLIK_CANCEL_BENEFIC_ASC_TITLE,
                descriptionKey = StringKey.MANGLIK_CANCEL_BENEFIC_ASC_DESC,
                strength = CancellationStrength.PARTIAL
            ))
        }

        return factors
    }

    private fun calculateCancellationReduction(factors: List<CancellationFactor>): Double {
        if (factors.isEmpty()) return 0.0

        // Check for full cancellation first
        if (factors.any { it.strength == CancellationStrength.FULL }) {
            return 1.0
        }

        var totalReduction = 0.0

        for (factor in factors) {
            val reduction = when (factor.strength) {
                CancellationStrength.FULL -> 1.0
                CancellationStrength.STRONG -> 0.4
                CancellationStrength.PARTIAL -> 0.2
            }
            totalReduction += reduction
        }

        return totalReduction.coerceAtMost(0.95) // Maximum 95% reduction without full cancellation
    }

    private fun getRemedies(level: ManglikLevel, marsPosition: PlanetPosition?): List<ManglikRemedy> {
        if (level == ManglikLevel.NONE) return emptyList()

        val remedies = mutableListOf<ManglikRemedy>()

        // Kumbh Vivah - ceremonial marriage to a pot before actual marriage
        remedies.add(ManglikRemedy(
            type = RemedyType.MARRIAGE_REMEDY,
            titleKey = StringKey.REMEDY_KUMBH_VIVAH_TITLE,
            descriptionKey = StringKey.REMEDY_KUMBH_VIVAH_DESC,
            effectiveness = "Traditional remedy - highly effective"
        ))

        // Mangal Shanti Puja
        remedies.add(ManglikRemedy(
            type = RemedyType.RITUAL,
            titleKey = StringKey.REMEDY_MANGAL_SHANTI_TITLE,
            descriptionKey = StringKey.REMEDY_MANGAL_SHANTI_DESC,
            effectiveness = "Recommended for all Manglik levels"
        ))

        // Mars Mantra
        remedies.add(ManglikRemedy(
            type = RemedyType.MANTRA,
            titleKey = StringKey.REMEDY_MARS_MANTRA_TITLE,
            descriptionKey = StringKey.REMEDY_MARS_MANTRA_DESC,
            effectiveness = "Daily recitation on Tuesdays"
        ))

        // Coral gemstone
        if (level == ManglikLevel.FULL || level == ManglikLevel.SEVERE) {
            remedies.add(ManglikRemedy(
                type = RemedyType.GEMSTONE,
                titleKey = StringKey.REMEDY_CORAL_TITLE,
                descriptionKey = StringKey.REMEDY_CORAL_DESC,
                effectiveness = "Consult astrologer before wearing"
            ))
        }

        // Charity
        remedies.add(ManglikRemedy(
            type = RemedyType.CHARITY,
            titleKey = StringKey.REMEDY_TUESDAY_CHARITY_TITLE,
            descriptionKey = StringKey.REMEDY_TUESDAY_CHARITY_DESC,
            effectiveness = "Every Tuesday"
        ))

        return remedies
    }

    private fun generateInterpretation(
        isManglik: Boolean,
        level: ManglikLevel,
        effectiveLevel: ManglikLevel,
        marsPosition: PlanetPosition?,
        analysisFromLagna: MarsPositionAnalysis,
        analysisFromMoon: MarsPositionAnalysis,
        analysisFromVenus: MarsPositionAnalysis,
        cancellationFactors: List<CancellationFactor>
    ): String {
        return buildString {
            if (!isManglik) {
                appendLine("NO MANGLIK DOSHA")
                appendLine()
                appendLine("Mars is not placed in houses 1, 2, 4, 7, 8, or 12 from your Lagna, Moon, or Venus.")
                appendLine("There is no Manglik Dosha in your chart.")
                return@buildString
            }

            appendLine("MANGLIK DOSHA ANALYSIS")
            appendLine()

            marsPosition?.let {
                appendLine("Mars Position: ${it.sign.displayName} in House ${it.house}")
                appendLine()
            }

            appendLine("ANALYSIS FROM THREE REFERENCE POINTS:")
            appendLine()

            appendLine("From Lagna (${analysisFromLagna.referenceSign.displayName}):")
            appendLine("  Mars in ${analysisFromLagna.marsHouse}${VedicAstrologyUtils.getOrdinalSuffix(analysisFromLagna.marsHouse)} house")
            appendLine("  Manglik: ${if (analysisFromLagna.isManglik) "YES" else "NO"}")
            appendLine()

            appendLine("From Moon (${analysisFromMoon.referenceSign.displayName}):")
            appendLine("  Mars in ${analysisFromMoon.marsHouse}${VedicAstrologyUtils.getOrdinalSuffix(analysisFromMoon.marsHouse)} house")
            appendLine("  Manglik: ${if (analysisFromMoon.isManglik) "YES" else "NO"}")
            appendLine()

            appendLine("From Venus (${analysisFromVenus.referenceSign.displayName}):")
            appendLine("  Mars in ${analysisFromVenus.marsHouse}${VedicAstrologyUtils.getOrdinalSuffix(analysisFromVenus.marsHouse)} house")
            appendLine("  Manglik: ${if (analysisFromVenus.isManglik) "YES" else "NO"}")
            appendLine()

            appendLine("Initial Level: ${level.name}")

            if (cancellationFactors.isNotEmpty()) {
                appendLine()
                appendLine("CANCELLATION FACTORS PRESENT:")
                cancellationFactors.forEach { factor ->
                    appendLine("  - ${factor.strength.name}: Applied")
                }
                appendLine()
                appendLine("Effective Level After Cancellations: ${effectiveLevel.name}")
            }
        }
    }

    private fun generateMarriageConsiderations(
        effectiveLevel: ManglikLevel,
        cancellationFactors: List<CancellationFactor>
    ): String {
        return buildString {
            appendLine("MARRIAGE CONSIDERATIONS")
            appendLine()

            when (effectiveLevel) {
                ManglikLevel.NONE -> {
                    appendLine("- No restrictions based on Manglik Dosha")
                    appendLine("- Compatible with both Manglik and non-Manglik partners")
                }
                ManglikLevel.MILD -> {
                    appendLine("- Mild Manglik effects - marriage with non-Manglik is possible")
                    appendLine("- Simple remedies recommended before marriage")
                    appendLine("- Matching with another Manglik is beneficial but not essential")
                }
                ManglikLevel.PARTIAL -> {
                    appendLine("- Partial Manglik - remedies strongly recommended")
                    appendLine("- Marriage with Manglik partner is preferable")
                    appendLine("- If marrying non-Manglik, perform Kumbh Vivah")
                }
                ManglikLevel.FULL -> {
                    appendLine("- Full Manglik Dosha present")
                    appendLine("- Marriage with Manglik partner highly recommended")
                    appendLine("- Kumbh Vivah or equivalent ritual essential before marriage")
                    appendLine("- Regular Mars propitiation recommended")
                }
                ManglikLevel.SEVERE -> {
                    appendLine("- Severe Manglik Dosha - careful consideration required")
                    appendLine("- Only marry Manglik partner with similar intensity")
                    appendLine("- Multiple remedies required before and after marriage")
                    appendLine("- Consider delaying marriage until after age 28 (Mars maturity)")
                }
            }

            if (cancellationFactors.any { it.strength == CancellationStrength.FULL }) {
                appendLine()
                appendLine("NOTE: Full cancellation present - Manglik Dosha effectively nullified")
            }
        }
    }

    /**
     * Check Manglik compatibility between two charts
     *
     * @param chart1 First person's chart
     * @param chart2 Second person's chart
     * @return Compatibility assessment
     */
    fun checkManglikCompatibility(
        chart1: ManglikAnalysis,
        chart2: ManglikAnalysis
    ): ManglikCompatibility {
        val bothManglik = chart1.effectiveLevel != ManglikLevel.NONE &&
            chart2.effectiveLevel != ManglikLevel.NONE

        val onlyOneManglik = (chart1.effectiveLevel != ManglikLevel.NONE) xor
            (chart2.effectiveLevel != ManglikLevel.NONE)

        val neitherManglik = chart1.effectiveLevel == ManglikLevel.NONE &&
            chart2.effectiveLevel == ManglikLevel.NONE

        val compatibility = when {
            neitherManglik -> CompatibilityLevel.EXCELLENT
            bothManglik -> {
                // Both Manglik - doshas cancel each other
                val intensityDiff = kotlin.math.abs(
                    chart1.remainingIntensityAfterCancellations -
                        chart2.remainingIntensityAfterCancellations
                )
                when {
                    intensityDiff <= 20 -> CompatibilityLevel.EXCELLENT
                    intensityDiff <= 40 -> CompatibilityLevel.GOOD
                    else -> CompatibilityLevel.AVERAGE
                }
            }
            onlyOneManglik -> {
                val manglikChart = if (chart1.effectiveLevel != ManglikLevel.NONE) chart1 else chart2
                when (manglikChart.effectiveLevel) {
                    ManglikLevel.MILD -> CompatibilityLevel.GOOD
                    ManglikLevel.PARTIAL -> CompatibilityLevel.AVERAGE
                    ManglikLevel.FULL -> CompatibilityLevel.BELOW_AVERAGE
                    ManglikLevel.SEVERE -> CompatibilityLevel.POOR
                    else -> CompatibilityLevel.EXCELLENT
                }
            }
            else -> CompatibilityLevel.AVERAGE
        }

        val recommendation = when (compatibility) {
            CompatibilityLevel.EXCELLENT -> "Excellent Manglik compatibility - no concerns"
            CompatibilityLevel.GOOD -> "Good compatibility - minor remedies may help"
            CompatibilityLevel.AVERAGE -> "Average compatibility - remedies recommended"
            CompatibilityLevel.BELOW_AVERAGE -> "Below average - significant remedies required"
            CompatibilityLevel.POOR -> "Challenging combination - expert consultation advised"
        }

        return ManglikCompatibility(
            person1Level = chart1.effectiveLevel,
            person2Level = chart2.effectiveLevel,
            compatibilityLevel = compatibility,
            recommendation = recommendation
        )
    }

    data class ManglikCompatibility(
        val person1Level: ManglikLevel,
        val person2Level: ManglikLevel,
        val compatibilityLevel: CompatibilityLevel,
        val recommendation: String
    )

    enum class CompatibilityLevel {
        EXCELLENT, GOOD, AVERAGE, BELOW_AVERAGE, POOR
    }
}
