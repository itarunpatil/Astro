package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKeyDosha
import com.astro.storm.data.localization.StringKeyInterface
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign

object ManglikDoshaCalculator {

    private val MANGLIK_HOUSES = setOf(1, 2, 4, 7, 8, 12)
    private val HIGH_INTENSITY_HOUSES = setOf(7, 8)
    private val MEDIUM_INTENSITY_HOUSES = setOf(1, 4, 12)
    private val LOW_INTENSITY_HOUSES = setOf(2)

    enum class ManglikLevel {
        NONE,
        MILD,
        PARTIAL,
        FULL,
        SEVERE;

        fun getLocalizedName(language: Language): String = when (this) {
            NONE -> StringResources.get(StringKeyDosha.MANGLIK_NONE_LEVEL, language)
            MILD -> StringResources.get(StringKeyDosha.MANGLIK_MILD, language)
            PARTIAL -> StringResources.get(StringKeyDosha.MANGLIK_PARTIAL_LEVEL, language)
            FULL -> StringResources.get(StringKeyDosha.MANGLIK_FULL_LEVEL, language)
            SEVERE -> StringResources.get(StringKeyDosha.MANGLIK_SEVERE, language)
        }
    }

    data class CancellationFactor(
        val titleKey: StringKeyInterface,
        val descriptionKey: StringKeyInterface,
        val strength: CancellationStrength
    ) {
        fun getTitle(language: Language): String = StringResources.get(titleKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class CancellationStrength {
        FULL,
        STRONG,
        PARTIAL
    }

    data class MarsPositionAnalysis(
        val referencePoint: String,
        val referencePointKey: StringKeyInterface,
        val referenceSign: ZodiacSign,
        val marsHouse: Int,
        val isManglik: Boolean,
        val intensity: Double
    )

    data class ManglikAnalysis(
        val isManglik: Boolean,
        val level: ManglikLevel,
        val overallIntensity: Double,
        val marsPosition: PlanetPosition?,
        val marsSign: ZodiacSign?,
        val isMarsRetrograde: Boolean,
        val analysisFromLagna: MarsPositionAnalysis,
        val analysisFromMoon: MarsPositionAnalysis,
        val analysisFromVenus: MarsPositionAnalysis,
        val cancellationFactors: List<CancellationFactor>,
        val remainingIntensityAfterCancellations: Double,
        val effectiveLevel: ManglikLevel,
        val remedies: List<ManglikRemedy>
    ) {
        fun getSummary(language: Language): String {
            return if (isManglik && effectiveLevel != ManglikLevel.NONE) {
                val levelName = effectiveLevel.getLocalizedName(language)
                StringResources.get(StringKeyDosha.MANGLIK_SUMMARY_PRESENT, language)
                    .replace("{level}", levelName)
                    .replace("{intensity}", "%.1f".format(remainingIntensityAfterCancellations))
            } else {
                StringResources.get(StringKeyDosha.MANGLIK_SUMMARY_ABSENT, language)
            }
        }

        fun getInterpretation(language: Language): String {
            return buildString {
                if (!isManglik || effectiveLevel == ManglikLevel.NONE) {
                    appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_NO_DOSHA_TITLE, language))
                    appendLine()
                    appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_NO_DOSHA_DESC, language))
                    return@buildString
                }

                appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_TITLE, language))
                appendLine()

                marsPosition?.let {
                    appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_MARS_POSITION, language)
                        .replace("{sign}", it.sign.getLocalizedName(language))
                        .replace("{house}", it.house.toString()))
                    if (isMarsRetrograde) {
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_MARS_RETROGRADE, language))
                    }
                    appendLine()
                }

                appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_THREE_REF, language))
                appendLine()

                appendLine(StringResources.get(StringKeyDosha.MANGLIK_FROM_LAGNA, language) + " (${analysisFromLagna.referenceSign.getLocalizedName(language)}):")
                appendLine("  ${StringResources.get(StringKeyDosha.MANGLIK_INTERP_MARS_HOUSE, language).replace("{house}", analysisFromLagna.marsHouse.toString())}")
                appendLine("  ${StringResources.get(StringKeyDosha.MANGLIK_INTERP_IS_MANGLIK, language)}: ${if (analysisFromLagna.isManglik) StringResources.get(StringKeyDosha.YES, language) else StringResources.get(StringKeyDosha.NO, language)}")
                appendLine()

                appendLine(StringResources.get(StringKeyDosha.MANGLIK_FROM_MOON, language) + " (${analysisFromMoon.referenceSign.getLocalizedName(language)}):")
                appendLine("  ${StringResources.get(StringKeyDosha.MANGLIK_INTERP_MARS_HOUSE, language).replace("{house}", analysisFromMoon.marsHouse.toString())}")
                appendLine("  ${StringResources.get(StringKeyDosha.MANGLIK_INTERP_IS_MANGLIK, language)}: ${if (analysisFromMoon.isManglik) StringResources.get(StringKeyDosha.YES, language) else StringResources.get(StringKeyDosha.NO, language)}")
                appendLine()

                appendLine(StringResources.get(StringKeyDosha.MANGLIK_FROM_VENUS, language) + " (${analysisFromVenus.referenceSign.getLocalizedName(language)}):")
                appendLine("  ${StringResources.get(StringKeyDosha.MANGLIK_INTERP_MARS_HOUSE, language).replace("{house}", analysisFromVenus.marsHouse.toString())}")
                appendLine("  ${StringResources.get(StringKeyDosha.MANGLIK_INTERP_IS_MANGLIK, language)}: ${if (analysisFromVenus.isManglik) StringResources.get(StringKeyDosha.YES, language) else StringResources.get(StringKeyDosha.NO, language)}")
                appendLine()

                appendLine(StringResources.get(StringKeyDosha.MANGLIK_INTERP_INITIAL_LEVEL, language).replace("{level}", level.getLocalizedName(language)))

                if (cancellationFactors.isNotEmpty()) {
                    appendLine()
                    appendLine(StringResources.get(StringKeyDosha.MANGLIK_CANCELLATIONS, language) + ":")
                    cancellationFactors.forEach { factor ->
                        appendLine("  - ${factor.getTitle(language)} (${factor.strength.name})")
                    }
                    appendLine()
                    appendLine(StringResources.get(StringKeyDosha.MANGLIK_EFFECTIVE_LEVEL, language) + ": ${effectiveLevel.getLocalizedName(language)}")
                }
            }
        }

        fun getMarriageConsiderations(language: Language): String {
            return buildString {
                appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_TITLE, language))
                appendLine()

                when (effectiveLevel) {
                    ManglikLevel.NONE -> {
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_NONE_1, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_NONE_2, language))
                    }
                    ManglikLevel.MILD -> {
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_MILD_1, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_MILD_2, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_MILD_3, language))
                    }
                    ManglikLevel.PARTIAL -> {
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_PARTIAL_1, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_PARTIAL_2, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_PARTIAL_3, language))
                    }
                    ManglikLevel.FULL -> {
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_FULL_1, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_FULL_2, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_FULL_3, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_FULL_4, language))
                    }
                    ManglikLevel.SEVERE -> {
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_SEVERE_1, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_SEVERE_2, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_SEVERE_3, language))
                        appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_SEVERE_4, language))
                    }
                }

                if (cancellationFactors.any { it.strength == CancellationStrength.FULL }) {
                    appendLine()
                    appendLine(StringResources.get(StringKeyDosha.MANGLIK_MARRIAGE_FULL_CANCEL_NOTE, language))
                }
            }
        }
    }

    data class ManglikRemedy(
        val type: RemedyType,
        val titleKey: StringKeyInterface,
        val descriptionKey: StringKeyInterface,
        val effectivenessKey: StringKeyInterface
    ) {
        fun getTitle(language: Language): String = StringResources.get(titleKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
        fun getEffectiveness(language: Language): String = StringResources.get(effectivenessKey, language)
    }

    enum class RemedyType {
        RITUAL, GEMSTONE, MANTRA, CHARITY, MARRIAGE_REMEDY
    }

    fun calculateManglikDosha(chart: VedicChart): ManglikAnalysis {
        val marsPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.MARS)
        val moonPosition = VedicAstrologyUtils.getMoonPosition(chart)
        val venusPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.VENUS)
        val ascendantSign = VedicAstrologyUtils.getAscendantSign(chart)

        val analysisFromLagna = analyzeFromReference(
            StringKeyDosha.MANGLIK_REF_LAGNA,
            ascendantSign,
            marsPosition
        )

        val analysisFromMoon = analyzeFromReference(
            StringKeyDosha.MANGLIK_REF_MOON,
            moonPosition?.sign ?: ascendantSign,
            marsPosition
        )

        val analysisFromVenus = analyzeFromReference(
            StringKeyDosha.MANGLIK_REF_VENUS,
            venusPosition?.sign ?: ascendantSign,
            marsPosition
        )

        val isManglikFromAny = analysisFromLagna.isManglik ||
            analysisFromMoon.isManglik ||
            analysisFromVenus.isManglik

        val rawIntensity = calculateRawIntensity(
            analysisFromLagna,
            analysisFromMoon,
            analysisFromVenus
        )

        val initialLevel = determineLevel(rawIntensity, isManglikFromAny)

        val cancellationFactors = findCancellationFactors(
            chart = chart,
            marsPosition = marsPosition,
            ascendantSign = ascendantSign,
            analysisFromLagna = analysisFromLagna,
            analysisFromMoon = analysisFromMoon,
            analysisFromVenus = analysisFromVenus
        )

        val cancellationReduction = calculateCancellationReduction(cancellationFactors)
        val remainingIntensity = (rawIntensity * (1.0 - cancellationReduction)).coerceAtLeast(0.0)

        val effectiveLevel = if (cancellationFactors.any { it.strength == CancellationStrength.FULL }) {
            ManglikLevel.NONE
        } else {
            determineLevel(remainingIntensity, isManglikFromAny && remainingIntensity > 10)
        }

        val remedies = getRemedies(effectiveLevel)

        return ManglikAnalysis(
            isManglik = isManglikFromAny,
            level = initialLevel,
            overallIntensity = rawIntensity,
            marsPosition = marsPosition,
            marsSign = marsPosition?.sign,
            isMarsRetrograde = marsPosition?.isRetrograde ?: false,
            analysisFromLagna = analysisFromLagna,
            analysisFromMoon = analysisFromMoon,
            analysisFromVenus = analysisFromVenus,
            cancellationFactors = cancellationFactors,
            remainingIntensityAfterCancellations = remainingIntensity,
            effectiveLevel = effectiveLevel,
            remedies = remedies
        )
    }

    private fun analyzeFromReference(
        referenceKey: StringKeyInterface,
        referenceSign: ZodiacSign,
        marsPosition: PlanetPosition?
    ): MarsPositionAnalysis {
        if (marsPosition == null) {
            return MarsPositionAnalysis(
                referencePoint = "Unknown",
                referencePointKey = referenceKey,
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
            referencePoint = referenceKey.en,
            referencePointKey = referenceKey,
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
                titleKey = StringKeyDosha.MANGLIK_CANCEL_OWN_SIGN_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_OWN_SIGN_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 2. Mars in exaltation (Capricorn)
        if (marsPosition.sign == ZodiacSign.CAPRICORN) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_EXALTED_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_EXALTED_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 3. Mars conjunct or aspected by benefics (Jupiter, Venus)
        val jupiter = VedicAstrologyUtils.getPlanetPosition(chart, Planet.JUPITER)
        val venus = VedicAstrologyUtils.getPlanetPosition(chart, Planet.VENUS)

        if (jupiter != null && jupiter.house == marsPosition.house) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_JUPITER_CONJUNCT_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_JUPITER_CONJUNCT_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        if (venus != null && venus.house == marsPosition.house) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_VENUS_CONJUNCT_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_VENUS_CONJUNCT_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 4. Jupiter aspects Mars (5th, 7th, 9th aspect)
        if (jupiter != null) {
            val jupiterAspects = VedicAstrologyUtils.getAspectedHouses(Planet.JUPITER, jupiter.house)
            if (marsPosition.house in jupiterAspects) {
                factors.add(CancellationFactor(
                    titleKey = StringKeyDosha.MANGLIK_CANCEL_JUPITER_ASPECT_TITLE,
                    descriptionKey = StringKeyDosha.MANGLIK_CANCEL_JUPITER_ASPECT_DESC,
                    strength = CancellationStrength.STRONG
                ))
            }
        }

        // 5. Mars in 2nd house in specific signs (Gemini, Virgo)
        if (analysisFromLagna.marsHouse == 2 &&
            (marsPosition.sign == ZodiacSign.GEMINI || marsPosition.sign == ZodiacSign.VIRGO)) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_SECOND_MERCURY_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_SECOND_MERCURY_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 6. Mars in 4th house in Aries or Scorpio
        if (analysisFromLagna.marsHouse == 4 &&
            (marsPosition.sign == ZodiacSign.ARIES || marsPosition.sign == ZodiacSign.SCORPIO)) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_FOURTH_OWN_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_FOURTH_OWN_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 7. Mars in 7th house in Cancer or Capricorn
        if (analysisFromLagna.marsHouse == 7 &&
            (marsPosition.sign == ZodiacSign.CANCER || marsPosition.sign == ZodiacSign.CAPRICORN)) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_SEVENTH_SPECIAL_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_SEVENTH_SPECIAL_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 8. Mars in 8th house in Sagittarius or Pisces
        if (analysisFromLagna.marsHouse == 8 &&
            (marsPosition.sign == ZodiacSign.SAGITTARIUS || marsPosition.sign == ZodiacSign.PISCES)) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_EIGHTH_JUPITER_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_EIGHTH_JUPITER_DESC,
                strength = CancellationStrength.STRONG
            ))
        }

        // 9. Mars in 12th house in Taurus or Libra
        if (analysisFromLagna.marsHouse == 12 &&
            (marsPosition.sign == ZodiacSign.TAURUS || marsPosition.sign == ZodiacSign.LIBRA)) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_TWELFTH_VENUS_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_TWELFTH_VENUS_DESC,
                strength = CancellationStrength.FULL
            ))
        }

        // 10. Specific Ascendants where Mars is benefic
        // For Aries, Cancer, Leo, and Scorpio ascendants, Mars is a benefic
        if (ascendantSign in listOf(ZodiacSign.ARIES, ZodiacSign.CANCER, ZodiacSign.LEO, ZodiacSign.SCORPIO)) {
            factors.add(CancellationFactor(
                titleKey = StringKeyDosha.MANGLIK_CANCEL_BENEFIC_ASC_TITLE,
                descriptionKey = StringKeyDosha.MANGLIK_CANCEL_BENEFIC_ASC_DESC,
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

    private fun getRemedies(level: ManglikLevel): List<ManglikRemedy> {
        if (level == ManglikLevel.NONE) return emptyList()

        val remedies = mutableListOf<ManglikRemedy>()

        remedies.add(ManglikRemedy(
            type = RemedyType.MARRIAGE_REMEDY,
            titleKey = StringKeyDosha.REMEDY_KUMBH_VIVAH_TITLE,
            descriptionKey = StringKeyDosha.REMEDY_KUMBH_VIVAH_DESC,
            effectivenessKey = StringKeyDosha.REMEDY_EFFECTIVENESS_TRADITIONAL
        ))

        remedies.add(ManglikRemedy(
            type = RemedyType.RITUAL,
            titleKey = StringKeyDosha.REMEDY_MANGAL_SHANTI_TITLE,
            descriptionKey = StringKeyDosha.REMEDY_MANGAL_SHANTI_DESC,
            effectivenessKey = StringKeyDosha.REMEDY_EFFECTIVENESS_ALL_LEVELS
        ))

        remedies.add(ManglikRemedy(
            type = RemedyType.MANTRA,
            titleKey = StringKeyDosha.REMEDY_MARS_MANTRA_TITLE,
            descriptionKey = StringKeyDosha.REMEDY_MARS_MANTRA_DESC,
            effectivenessKey = StringKeyDosha.REMEDY_EFFECTIVENESS_TUESDAY
        ))

        if (level == ManglikLevel.FULL || level == ManglikLevel.SEVERE) {
            remedies.add(ManglikRemedy(
                type = RemedyType.GEMSTONE,
                titleKey = StringKeyDosha.REMEDY_CORAL_TITLE,
                descriptionKey = StringKeyDosha.REMEDY_CORAL_DESC,
                effectivenessKey = StringKeyDosha.REMEDY_EFFECTIVENESS_CONSULT
            ))
        }

        remedies.add(ManglikRemedy(
            type = RemedyType.CHARITY,
            titleKey = StringKeyDosha.REMEDY_TUESDAY_CHARITY_TITLE,
            descriptionKey = StringKeyDosha.REMEDY_TUESDAY_CHARITY_DESC,
            effectivenessKey = StringKeyDosha.REMEDY_EFFECTIVENESS_EVERY_TUESDAY
        ))

        return remedies
    }

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

        val recommendationKey = when (compatibility) {
            CompatibilityLevel.EXCELLENT -> StringKeyDosha.MANGLIK_COMPAT_EXCELLENT
            CompatibilityLevel.GOOD -> StringKeyDosha.MANGLIK_COMPAT_GOOD
            CompatibilityLevel.AVERAGE -> StringKeyDosha.MANGLIK_COMPAT_AVERAGE
            CompatibilityLevel.BELOW_AVERAGE -> StringKeyDosha.MANGLIK_COMPAT_BELOW_AVG
            CompatibilityLevel.POOR -> StringKeyDosha.MANGLIK_COMPAT_POOR
        }

        return ManglikCompatibility(
            person1Level = chart1.effectiveLevel,
            person2Level = chart2.effectiveLevel,
            compatibilityLevel = compatibility,
            recommendationKey = recommendationKey
        )
    }

    data class ManglikCompatibility(
        val person1Level: ManglikLevel,
        val person2Level: ManglikLevel,
        val compatibilityLevel: CompatibilityLevel,
        val recommendationKey: StringKeyInterface
    ) {
        fun getRecommendation(language: Language): String =
            StringResources.get(recommendationKey, language)
    }

    enum class CompatibilityLevel {
        EXCELLENT, GOOD, AVERAGE, BELOW_AVERAGE, POOR
    }
}
