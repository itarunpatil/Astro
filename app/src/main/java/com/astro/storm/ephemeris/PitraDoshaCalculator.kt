package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign

/**
 * Pitra Dosha Calculator - Ancestral Karma Analysis
 *
 * Pitra Dosha (पितृ दोष) is a karmic debt from ancestors that manifests in the horoscope.
 * It indicates unfulfilled obligations or unresolved issues from previous generations
 * that the native must address in this lifetime.
 *
 * Primary indicators of Pitra Dosha:
 * 1. Sun conjunct with Rahu - Most common indicator
 * 2. Sun conjunct with Ketu
 * 3. Sun afflicted by Saturn (conjunction or aspect)
 * 4. 9th house affliction (house of father and ancestors)
 * 5. 9th lord afflicted by malefics
 * 6. Rahu in 9th house
 * 7. Saturn in 9th house aspecting Sun
 *
 * Effects of Pitra Dosha:
 * - Obstacles in career and life progress
 * - Delays in marriage and childbirth
 * - Health issues (especially hereditary)
 * - Financial instability
 * - Family disharmony
 * - Recurring problems despite efforts
 *
 * Traditional remedies include:
 * - Pitra Tarpan (ancestral offerings)
 * - Shraddha ceremonies
 * - Narayan Bali / Nagbali
 * - Pind Daan at sacred places
 *
 * References:
 * - Brihat Parasara Hora Shastra
 * - Garuda Purana
 * - Markandeya Purana
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
object PitraDoshaCalculator {

    /**
     * Pitra Dosha severity levels
     */
    enum class PitraDoshaLevel {
        /** No Pitra Dosha detected */
        NONE,
        /** Minor ancestral karma present */
        MINOR,
        /** Moderate ancestral debt */
        MODERATE,
        /** Significant Pitra Dosha */
        SIGNIFICANT,
        /** Severe ancestral karma requiring attention */
        SEVERE;

        fun getLocalizedName(language: Language): String = when (this) {
            NONE -> StringResources.get(StringKey.PITRA_DOSHA_NONE, language)
            MINOR -> StringResources.get(StringKey.PITRA_DOSHA_MINOR, language)
            MODERATE -> StringResources.get(StringKey.PITRA_DOSHA_MODERATE, language)
            SIGNIFICANT -> StringResources.get(StringKey.PITRA_DOSHA_SIGNIFICANT, language)
            SEVERE -> StringResources.get(StringKey.PITRA_DOSHA_SEVERE, language)
        }
    }

    /**
     * Types of Pitra Dosha based on causative factors
     */
    enum class PitraDoshaType {
        /** Sun-Rahu conjunction */
        SURYA_RAHU_YOGA,
        /** Sun-Ketu conjunction */
        SURYA_KETU_YOGA,
        /** Sun-Saturn affliction */
        SURYA_SHANI_YOGA,
        /** 9th house affliction */
        NINTH_HOUSE_AFFLICTION,
        /** 9th lord afflicted */
        NINTH_LORD_AFFLICTION,
        /** Rahu in 9th house */
        RAHU_IN_NINTH,
        /** Multiple malefics affecting Sun/9th */
        COMBINED_AFFLICTION;

        fun getLocalizedName(language: Language): String = when (this) {
            SURYA_RAHU_YOGA -> StringResources.get(StringKey.PITRA_TYPE_SURYA_RAHU, language)
            SURYA_KETU_YOGA -> StringResources.get(StringKey.PITRA_TYPE_SURYA_KETU, language)
            SURYA_SHANI_YOGA -> StringResources.get(StringKey.PITRA_TYPE_SURYA_SHANI, language)
            NINTH_HOUSE_AFFLICTION -> StringResources.get(StringKey.PITRA_TYPE_NINTH_HOUSE, language)
            NINTH_LORD_AFFLICTION -> StringResources.get(StringKey.PITRA_TYPE_NINTH_LORD, language)
            RAHU_IN_NINTH -> StringResources.get(StringKey.PITRA_TYPE_RAHU_NINTH, language)
            COMBINED_AFFLICTION -> StringResources.get(StringKey.PITRA_TYPE_COMBINED, language)
        }

        fun getDescription(language: Language): String = when (this) {
            SURYA_RAHU_YOGA -> StringResources.get(StringKey.PITRA_DESC_SURYA_RAHU, language)
            SURYA_KETU_YOGA -> StringResources.get(StringKey.PITRA_DESC_SURYA_KETU, language)
            SURYA_SHANI_YOGA -> StringResources.get(StringKey.PITRA_DESC_SURYA_SHANI, language)
            NINTH_HOUSE_AFFLICTION -> StringResources.get(StringKey.PITRA_DESC_NINTH_HOUSE, language)
            NINTH_LORD_AFFLICTION -> StringResources.get(StringKey.PITRA_DESC_NINTH_LORD, language)
            RAHU_IN_NINTH -> StringResources.get(StringKey.PITRA_DESC_RAHU_NINTH, language)
            COMBINED_AFFLICTION -> StringResources.get(StringKey.PITRA_DESC_COMBINED, language)
        }
    }

    /**
     * Individual Pitra Dosha indicator found in chart
     */
    data class PitraDoshaIndicator(
        val type: PitraDoshaType,
        val involvedPlanets: List<Planet>,
        val involvedHouse: Int?,
        val severity: Double, // 0.0 to 1.0
        val description: String
    )

    /**
     * Pitra Dosha remedy
     */
    data class PitraRemedy(
        val titleKey: StringKey,
        val descriptionKey: StringKey,
        val type: RemedyType,
        val timing: String?,
        val mantra: String?
    ) {
        fun getTitle(language: Language): String = StringResources.get(titleKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class RemedyType {
        TARPAN,     // Ancestral offerings
        SHRADDHA,   // Memorial ceremonies
        PUJA,       // Worship rituals
        DAAN,       // Charitable donations
        MANTRA,     // Sacred chanting
        PILGRIMAGE  // Sacred place visit
    }

    /**
     * Complete Pitra Dosha analysis result
     */
    data class PitraDoshaAnalysis(
        val isPresent: Boolean,
        val level: PitraDoshaLevel,
        val overallSeverity: Double, // 0-100 percentage
        val indicators: List<PitraDoshaIndicator>,
        val primaryType: PitraDoshaType?,
        val sunPosition: PlanetPosition?,
        val ninthHouseAnalysis: NinthHouseAnalysis,
        val affectedLifeAreas: List<String>,
        val remedies: List<PitraRemedy>,
        val interpretation: String,
        val auspiciousPeriods: List<String>
    ) {
        fun getSummary(language: Language): String {
            return if (isPresent) {
                val levelName = level.getLocalizedName(language)
                StringResources.get(StringKey.PITRA_DOSHA_PRESENT_SUMMARY, language)
                    .replace("{level}", levelName)
            } else {
                StringResources.get(StringKey.PITRA_DOSHA_ABSENT_SUMMARY, language)
            }
        }
    }

    /**
     * Analysis of the 9th house (house of father and ancestors)
     */
    data class NinthHouseAnalysis(
        val lord: Planet,
        val lordPosition: PlanetPosition?,
        val planetsIn9th: List<PlanetPosition>,
        val isLordAfflicted: Boolean,
        val isHouseAfflicted: Boolean,
        val beneficInfluence: Boolean
    )

    /**
     * Calculate complete Pitra Dosha analysis for a chart
     *
     * @param chart The Vedic birth chart
     * @return Complete Pitra Dosha analysis
     */
    fun calculatePitraDosha(chart: VedicChart): PitraDoshaAnalysis {
        val sunPosition = VedicAstrologyUtils.getSunPosition(chart)
        val rahuPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.RAHU)
        val ketuPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.KETU)
        val saturnPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.SATURN)
        val ascendantSign = VedicAstrologyUtils.getAscendantSign(chart)

        // Analyze 9th house
        val ninthHouseAnalysis = analyzeNinthHouse(chart, ascendantSign)

        // Find all Pitra Dosha indicators
        val indicators = findPitraDoshaIndicators(
            chart = chart,
            sunPosition = sunPosition,
            rahuPosition = rahuPosition,
            ketuPosition = ketuPosition,
            saturnPosition = saturnPosition,
            ninthHouseAnalysis = ninthHouseAnalysis
        )

        // Calculate overall severity
        val overallSeverity = calculateOverallSeverity(indicators)

        // Determine level
        val level = determineLevel(overallSeverity, indicators.isNotEmpty())

        // Get primary type (most severe indicator)
        val primaryType = indicators.maxByOrNull { it.severity }?.type

        // Identify affected life areas
        val affectedAreas = identifyAffectedLifeAreas(indicators, ninthHouseAnalysis)

        // Get remedies
        val remedies = getRemedies(level, indicators)

        // Generate interpretation
        val interpretation = generateInterpretation(
            level = level,
            indicators = indicators,
            sunPosition = sunPosition,
            ninthHouseAnalysis = ninthHouseAnalysis
        )

        // Get auspicious periods for remedies
        val auspiciousPeriods = getAuspiciousPeriods()

        return PitraDoshaAnalysis(
            isPresent = indicators.isNotEmpty(),
            level = level,
            overallSeverity = overallSeverity,
            indicators = indicators,
            primaryType = primaryType,
            sunPosition = sunPosition,
            ninthHouseAnalysis = ninthHouseAnalysis,
            affectedLifeAreas = affectedAreas,
            remedies = remedies,
            interpretation = interpretation,
            auspiciousPeriods = auspiciousPeriods
        )
    }

    private fun analyzeNinthHouse(chart: VedicChart, ascendantSign: ZodiacSign): NinthHouseAnalysis {
        // Get 9th house sign and lord
        val ninthSignNumber = ((ascendantSign.number + 7) % 12) + 1
        val ninthSign = ZodiacSign.entries.find { it.number == ninthSignNumber } ?: ZodiacSign.SAGITTARIUS
        val ninthLord = ninthSign.ruler

        val ninthLordPosition = VedicAstrologyUtils.getPlanetPosition(chart, ninthLord)
        val planetsIn9th = VedicAstrologyUtils.getPlanetsInHouse(chart, 9)

        // Check if 9th lord is afflicted
        val isLordAfflicted = ninthLordPosition?.let { pos ->
            val saturnPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.SATURN)
            val rahuPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.RAHU)
            val marsPosition = VedicAstrologyUtils.getPlanetPosition(chart, Planet.MARS)

            // Check conjunctions with malefics
            val conjunctMalefic = listOf(saturnPosition, rahuPosition, marsPosition)
                .filterNotNull()
                .any { it.house == pos.house }

            // Check aspects from malefics
            val aspectedByMalefic = listOf(Planet.SATURN, Planet.MARS).any { malefic ->
                val maleficPos = VedicAstrologyUtils.getPlanetPosition(chart, malefic)
                maleficPos != null && pos.house in VedicAstrologyUtils.getAspectedHouses(malefic, maleficPos.house)
            }

            // Check if in Dusthana
            val inDusthana = pos.house in VedicAstrologyUtils.DUSTHANA_HOUSES

            conjunctMalefic || aspectedByMalefic || inDusthana
        } ?: false

        // Check if 9th house is afflicted
        val maleficsIn9th = planetsIn9th.filter {
            it.planet in VedicAstrologyUtils.NATURAL_MALEFICS
        }
        val isHouseAfflicted = maleficsIn9th.isNotEmpty()

        // Check for benefic influence
        val beneficInfluence = planetsIn9th.any {
            it.planet in VedicAstrologyUtils.NATURAL_BENEFICS
        }

        return NinthHouseAnalysis(
            lord = ninthLord,
            lordPosition = ninthLordPosition,
            planetsIn9th = planetsIn9th,
            isLordAfflicted = isLordAfflicted,
            isHouseAfflicted = isHouseAfflicted,
            beneficInfluence = beneficInfluence
        )
    }

    private fun findPitraDoshaIndicators(
        chart: VedicChart,
        sunPosition: PlanetPosition?,
        rahuPosition: PlanetPosition?,
        ketuPosition: PlanetPosition?,
        saturnPosition: PlanetPosition?,
        ninthHouseAnalysis: NinthHouseAnalysis
    ): List<PitraDoshaIndicator> {
        val indicators = mutableListOf<PitraDoshaIndicator>()

        // 1. Check Sun-Rahu conjunction (Surya Grahan Dosha)
        if (sunPosition != null && rahuPosition != null) {
            if (sunPosition.house == rahuPosition.house) {
                val orbDistance = VedicAstrologyUtils.angularDistance(
                    sunPosition.longitude,
                    rahuPosition.longitude
                )
                if (orbDistance <= 15.0) {
                    val severity = 1.0 - (orbDistance / 15.0) * 0.3 // Closer = more severe
                    indicators.add(PitraDoshaIndicator(
                        type = PitraDoshaType.SURYA_RAHU_YOGA,
                        involvedPlanets = listOf(Planet.SUN, Planet.RAHU),
                        involvedHouse = sunPosition.house,
                        severity = severity.coerceIn(0.6, 1.0),
                        description = "Sun conjunct Rahu in House ${sunPosition.house} - Primary Pitra Dosha indicator"
                    ))
                }
            }
        }

        // 2. Check Sun-Ketu conjunction
        if (sunPosition != null && ketuPosition != null) {
            if (sunPosition.house == ketuPosition.house) {
                val orbDistance = VedicAstrologyUtils.angularDistance(
                    sunPosition.longitude,
                    ketuPosition.longitude
                )
                if (orbDistance <= 12.0) {
                    val severity = 0.8 - (orbDistance / 12.0) * 0.2
                    indicators.add(PitraDoshaIndicator(
                        type = PitraDoshaType.SURYA_KETU_YOGA,
                        involvedPlanets = listOf(Planet.SUN, Planet.KETU),
                        involvedHouse = sunPosition.house,
                        severity = severity.coerceIn(0.5, 0.8),
                        description = "Sun conjunct Ketu in House ${sunPosition.house} - Indicates past-life ancestral karma"
                    ))
                }
            }
        }

        // 3. Check Sun-Saturn affliction
        if (sunPosition != null && saturnPosition != null) {
            // Conjunction
            if (sunPosition.house == saturnPosition.house) {
                indicators.add(PitraDoshaIndicator(
                    type = PitraDoshaType.SURYA_SHANI_YOGA,
                    involvedPlanets = listOf(Planet.SUN, Planet.SATURN),
                    involvedHouse = sunPosition.house,
                    severity = 0.7,
                    description = "Sun conjunct Saturn in House ${sunPosition.house} - Father-related karmic issues"
                ))
            }
            // Saturn's aspect on Sun
            val saturnAspects = VedicAstrologyUtils.getAspectedHouses(Planet.SATURN, saturnPosition.house)
            if (sunPosition.house in saturnAspects) {
                indicators.add(PitraDoshaIndicator(
                    type = PitraDoshaType.SURYA_SHANI_YOGA,
                    involvedPlanets = listOf(Planet.SUN, Planet.SATURN),
                    involvedHouse = sunPosition.house,
                    severity = 0.5,
                    description = "Saturn aspects Sun from House ${saturnPosition.house} - Delayed results due to ancestral karma"
                ))
            }
        }

        // 4. Check 9th house affliction
        if (ninthHouseAnalysis.isHouseAfflicted) {
            val maleficsIn9th = ninthHouseAnalysis.planetsIn9th.filter {
                it.planet in VedicAstrologyUtils.NATURAL_MALEFICS
            }
            indicators.add(PitraDoshaIndicator(
                type = PitraDoshaType.NINTH_HOUSE_AFFLICTION,
                involvedPlanets = maleficsIn9th.map { it.planet },
                involvedHouse = 9,
                severity = 0.5 + (maleficsIn9th.size * 0.15),
                description = "Malefics in 9th house - Ancestral blessings blocked"
            ))
        }

        // 5. Check Rahu in 9th house specifically
        if (rahuPosition != null && rahuPosition.house == 9) {
            indicators.add(PitraDoshaIndicator(
                type = PitraDoshaType.RAHU_IN_NINTH,
                involvedPlanets = listOf(Planet.RAHU),
                involvedHouse = 9,
                severity = 0.75,
                description = "Rahu in 9th house - Strong Pitra Dosha, past-life debts to ancestors"
            ))
        }

        // 6. Check 9th lord affliction
        if (ninthHouseAnalysis.isLordAfflicted) {
            indicators.add(PitraDoshaIndicator(
                type = PitraDoshaType.NINTH_LORD_AFFLICTION,
                involvedPlanets = listOf(ninthHouseAnalysis.lord),
                involvedHouse = ninthHouseAnalysis.lordPosition?.house,
                severity = 0.6,
                description = "9th lord ${ninthHouseAnalysis.lord.displayName} is afflicted - Ancestral lineage karma"
            ))
        }

        return indicators
    }

    private fun calculateOverallSeverity(indicators: List<PitraDoshaIndicator>): Double {
        if (indicators.isEmpty()) return 0.0

        // Take weighted average of all indicators
        val totalSeverity = indicators.sumOf { it.severity }
        val averageSeverity = totalSeverity / indicators.size

        // Bonus for multiple indicators
        val multiplicityBonus = kotlin.math.min(indicators.size * 5.0, 20.0)

        return ((averageSeverity * 100.0) + multiplicityBonus).coerceIn(0.0, 100.0)
    }

    private fun determineLevel(severity: Double, hasIndicators: Boolean): PitraDoshaLevel {
        if (!hasIndicators || severity == 0.0) return PitraDoshaLevel.NONE

        return when {
            severity >= 80 -> PitraDoshaLevel.SEVERE
            severity >= 60 -> PitraDoshaLevel.SIGNIFICANT
            severity >= 40 -> PitraDoshaLevel.MODERATE
            severity >= 20 -> PitraDoshaLevel.MINOR
            else -> PitraDoshaLevel.NONE
        }
    }

    private fun identifyAffectedLifeAreas(
        indicators: List<PitraDoshaIndicator>,
        ninthHouseAnalysis: NinthHouseAnalysis
    ): List<String> {
        val areas = mutableListOf<String>()

        // Based on house placement of afflicted Sun or 9th house issues
        areas.add("Father and paternal lineage")
        areas.add("Spiritual progress and dharma")

        for (indicator in indicators) {
            when (indicator.involvedHouse) {
                1 -> areas.add("Self, health, and overall life direction")
                2 -> areas.add("Family wealth and accumulated assets")
                3 -> areas.add("Siblings and communication")
                4 -> areas.add("Mother, property, and domestic peace")
                5 -> areas.add("Children, education, and creativity")
                6 -> areas.add("Health, debts, and service")
                7 -> areas.add("Marriage and partnerships")
                8 -> areas.add("Longevity and inherited wealth")
                9 -> areas.add("Fortune, higher learning, and spirituality")
                10 -> areas.add("Career and public reputation")
                11 -> areas.add("Gains and social network")
                12 -> areas.add("Spiritual liberation and foreign lands")
            }
        }

        return areas.distinct()
    }

    private fun getRemedies(level: PitraDoshaLevel, indicators: List<PitraDoshaIndicator>): List<PitraRemedy> {
        if (level == PitraDoshaLevel.NONE) return emptyList()

        val remedies = mutableListOf<PitraRemedy>()

        // Universal Pitra remedies
        remedies.add(PitraRemedy(
            titleKey = StringKey.REMEDY_PITRA_TARPAN_TITLE,
            descriptionKey = StringKey.REMEDY_PITRA_TARPAN_DESC,
            type = RemedyType.TARPAN,
            timing = "Amavasya (New Moon) or Pitru Paksha",
            mantra = "ॐ पितृभ्यो नमः"
        ))

        remedies.add(PitraRemedy(
            titleKey = StringKey.REMEDY_SHRADDHA_TITLE,
            descriptionKey = StringKey.REMEDY_SHRADDHA_DESC,
            type = RemedyType.SHRADDHA,
            timing = "Father's death anniversary or Pitru Paksha",
            mantra = null
        ))

        remedies.add(PitraRemedy(
            titleKey = StringKey.REMEDY_CROW_FEEDING_TITLE,
            descriptionKey = StringKey.REMEDY_CROW_FEEDING_DESC,
            type = RemedyType.DAAN,
            timing = "Daily, especially during Pitru Paksha",
            mantra = null
        ))

        if (level == PitraDoshaLevel.SIGNIFICANT || level == PitraDoshaLevel.SEVERE) {
            remedies.add(PitraRemedy(
                titleKey = StringKey.REMEDY_NARAYAN_BALI_TITLE,
                descriptionKey = StringKey.REMEDY_NARAYAN_BALI_DESC,
                type = RemedyType.PUJA,
                timing = "Once in lifetime at Trimbakeshwar or Gaya",
                mantra = null
            ))

            remedies.add(PitraRemedy(
                titleKey = StringKey.REMEDY_PIND_DAAN_TITLE,
                descriptionKey = StringKey.REMEDY_PIND_DAAN_DESC,
                type = RemedyType.PILGRIMAGE,
                timing = "Pitru Paksha at Gaya",
                mantra = null
            ))
        }

        // Mantra remedy
        remedies.add(PitraRemedy(
            titleKey = StringKey.REMEDY_PITRA_GAYATRI_TITLE,
            descriptionKey = StringKey.REMEDY_PITRA_GAYATRI_DESC,
            type = RemedyType.MANTRA,
            timing = "Daily during Brahma Muhurta",
            mantra = "ॐ देवताभ्यः पितृभ्यश्च महायोगिभ्य एव च। नमः स्वधायै स्वाहायै नित्यमेव नमो नमः।।"
        ))

        return remedies
    }

    private fun generateInterpretation(
        level: PitraDoshaLevel,
        indicators: List<PitraDoshaIndicator>,
        sunPosition: PlanetPosition?,
        ninthHouseAnalysis: NinthHouseAnalysis
    ): String {
        return buildString {
            if (level == PitraDoshaLevel.NONE) {
                appendLine("NO SIGNIFICANT PITRA DOSHA")
                appendLine()
                appendLine("Your chart does not show significant indicators of Pitra Dosha.")
                appendLine("The ancestral lineage appears supportive of your life journey.")
                appendLine()
                appendLine("However, performing regular ancestral offerings (Shraddha) is always")
                appendLine("beneficial for maintaining positive ancestral blessings.")
                return@buildString
            }

            appendLine("PITRA DOSHA ANALYSIS")
            appendLine()
            appendLine("Level: ${level.name}")
            appendLine()

            appendLine("INDICATORS FOUND:")
            indicators.forEachIndexed { index, indicator ->
                appendLine()
                appendLine("${index + 1}. ${indicator.type.name.replace("_", " ")}")
                appendLine("   Severity: ${"%.0f".format(indicator.severity * 100)}%")
                appendLine("   ${indicator.description}")
            }

            appendLine()
            appendLine("9TH HOUSE ANALYSIS (House of Ancestors):")
            appendLine("9th Lord: ${ninthHouseAnalysis.lord.displayName}")
            ninthHouseAnalysis.lordPosition?.let {
                appendLine("9th Lord Position: House ${it.house} in ${it.sign.displayName}")
            }
            appendLine("9th Lord Afflicted: ${if (ninthHouseAnalysis.isLordAfflicted) "Yes" else "No"}")
            appendLine("9th House Afflicted: ${if (ninthHouseAnalysis.isHouseAfflicted) "Yes" else "No"}")
            appendLine("Benefic Influence: ${if (ninthHouseAnalysis.beneficInfluence) "Yes - Mitigating" else "No"}")

            appendLine()
            appendLine("INTERPRETATION:")
            when (level) {
                PitraDoshaLevel.MINOR -> {
                    appendLine("Minor ancestral karma is indicated. This may manifest as occasional")
                    appendLine("obstacles or delays that seem unexplained. Regular ancestral prayers")
                    appendLine("and offerings during Pitru Paksha should be sufficient.")
                }
                PitraDoshaLevel.MODERATE -> {
                    appendLine("Moderate Pitra Dosha suggests unresolved ancestral obligations.")
                    appendLine("You may experience recurring challenges in life that feel karmic.")
                    appendLine("Regular Tarpan and Shraddha ceremonies are recommended.")
                }
                PitraDoshaLevel.SIGNIFICANT -> {
                    appendLine("Significant ancestral karma is present. This may manifest as:")
                    appendLine("- Delayed marriage or relationship issues")
                    appendLine("- Difficulties with children or progeny")
                    appendLine("- Career obstacles despite qualifications")
                    appendLine("- Family disharmony")
                    appendLine()
                    appendLine("Comprehensive remedies including Narayan Bali may be beneficial.")
                }
                PitraDoshaLevel.SEVERE -> {
                    appendLine("Severe Pitra Dosha indicates deep ancestral karma that requires")
                    appendLine("serious attention and remedial measures.")
                    appendLine()
                    appendLine("This level of dosha often indicates:")
                    appendLine("- Ancestors who departed with unfulfilled wishes")
                    appendLine("- Interrupted or improper last rites in the lineage")
                    appendLine("- Significant karmic debts carried forward")
                    appendLine()
                    appendLine("Consult a qualified priest for Narayan Bali/Nagbali and")
                    appendLine("Pind Daan at sacred places like Gaya.")
                }
                else -> {}
            }
        }
    }

    private fun getAuspiciousPeriods(): List<String> {
        return listOf(
            "Pitru Paksha (15-day period in Bhadrapada month)",
            "Amavasya (New Moon days)",
            "Solar/Lunar eclipses",
            "Father's death anniversary",
            "Mahalaya Amavasya",
            "Akshaya Tritiya",
            "Gaya Shraddha periods"
        )
    }

    /**
     * Check if Pitra Dosha will be triggered during specific transits
     *
     * @param natalChart Birth chart
     * @param transitSaturnLongitude Current Saturn position
     * @return True if Saturn is transiting over natal Sun or 9th house
     */
    fun isPitraDoshaTriggered(
        natalChart: VedicChart,
        transitSaturnLongitude: Double
    ): Boolean {
        val sunPosition = VedicAstrologyUtils.getSunPosition(natalChart) ?: return false
        val ascendantSign = VedicAstrologyUtils.getAscendantSign(natalChart)

        val transitSaturnSign = ZodiacSign.fromLongitude(transitSaturnLongitude)
        val transitSaturnHouse = VedicAstrologyUtils.getHouseFromSigns(transitSaturnSign, ascendantSign)

        // Check if Saturn is transiting Sun's house or 9th house
        return transitSaturnHouse == sunPosition.house || transitSaturnHouse == 9
    }
}
