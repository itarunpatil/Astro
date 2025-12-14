package com.astro.storm.ephemeris

import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Bhrigu Bindu Calculator
 *
 * Bhrigu Bindu (BB) is a highly significant sensitive point in Vedic astrology,
 * derived from the mid-point of Rahu and Moon. This point is used extensively
 * in the Bhrigu Nandi Nadi system for timing events and understanding karmic patterns.
 *
 * Calculation: BB = (Rahu longitude + Moon longitude) / 2
 *
 * References:
 * - Bhrigu Samhita traditions
 * - Nadi astrology texts
 * - R. Santhanam's works on predictive astrology
 *
 * @author AstroStorm
 */
object BhriguBinduCalculator {

    /**
     * Data class representing the complete Bhrigu Bindu analysis
     */
    data class BhriguBinduAnalysis(
        val bhriguBindu: Double,
        val bhriguBinduSign: ZodiacSign,
        val bhriguBinduNakshatra: Nakshatra,
        val bhriguBinduPada: Int,
        val bhriguBinduHouse: Int,
        val bhriguBinduLord: Planet,
        val nakshatraLord: Planet,
        val rahuLongitude: Double,
        val moonLongitude: Double,
        val aspectingPlanets: List<AspectingPlanet>,
        val conjunctPlanets: List<Planet>,
        val strengthAssessment: StrengthAssessment,
        val transitAnalysis: TransitAnalysis?,
        val interpretation: BhriguBinduInterpretation
    )

    /**
     * Strength assessment of Bhrigu Bindu based on various factors
     */
    data class StrengthAssessment(
        val lordStrength: LordStrength,
        val nakshatraLordStrength: LordStrength,
        val beneficInfluence: Double,
        val maleficInfluence: Double,
        val overallStrength: OverallStrength,
        val factors: List<StrengthFactor>
    )

    /**
     * Individual strength factor with description
     */
    data class StrengthFactor(
        val factor: String,
        val influence: FactorInfluence,
        val description: String
    )

    enum class FactorInfluence {
        HIGHLY_POSITIVE,
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        HIGHLY_NEGATIVE
    }

    enum class LordStrength {
        VERY_STRONG,
        STRONG,
        MODERATE,
        WEAK,
        VERY_WEAK
    }

    enum class OverallStrength {
        EXCELLENT,
        GOOD,
        MODERATE,
        CHALLENGING,
        DIFFICULT
    }

    /**
     * Planet aspecting the Bhrigu Bindu
     */
    data class AspectingPlanet(
        val planet: Planet,
        val aspectType: AspectType,
        val aspectStrength: Double,
        val isApplying: Boolean
    )

    enum class AspectType {
        CONJUNCTION,
        OPPOSITION,
        TRINE,
        SQUARE,
        SEXTILE,
        SPECIAL_ASPECT
    }

    /**
     * Transit analysis for Bhrigu Bindu
     */
    data class TransitAnalysis(
        val currentTransits: List<TransitingPlanet>,
        val upcomingTransits: List<UpcomingTransit>,
        val significantPeriods: List<SignificantPeriod>
    )

    data class TransitingPlanet(
        val planet: Planet,
        val longitude: Double,
        val distanceFromBB: Double,
        val isConjunct: Boolean,
        val aspectType: AspectType?,
        val effectDescription: String
    )

    data class UpcomingTransit(
        val planet: Planet,
        val transitType: TransitType,
        val estimatedDate: LocalDate,
        val significance: TransitSignificance
    )

    enum class TransitType {
        CONJUNCTION,
        OPPOSITION,
        TRINE,
        SQUARE,
        ENTERING_SIGN
    }

    enum class TransitSignificance {
        HIGHLY_SIGNIFICANT,
        SIGNIFICANT,
        MODERATE,
        MINOR
    }

    data class SignificantPeriod(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val description: String,
        val nature: PeriodNature,
        val triggeringPlanet: Planet
    )

    enum class PeriodNature {
        HIGHLY_AUSPICIOUS,
        AUSPICIOUS,
        MIXED,
        CHALLENGING,
        TRANSFORMATIVE
    }

    /**
     * Comprehensive interpretation of Bhrigu Bindu
     */
    data class BhriguBinduInterpretation(
        val generalMeaning: String,
        val karmicSignificance: String,
        val lifeAreas: List<LifeAreaInfluence>,
        val recommendations: List<String>,
        val auspiciousDays: List<String>,
        val remedialMeasures: List<RemedialMeasure>
    )

    data class LifeAreaInfluence(
        val area: LifeArea,
        val influence: AreaInfluence,
        val description: String
    )

    enum class LifeArea {
        CAREER,
        RELATIONSHIPS,
        HEALTH,
        SPIRITUALITY,
        WEALTH,
        FAMILY,
        EDUCATION,
        FOREIGN_CONNECTIONS
    }

    enum class AreaInfluence {
        VERY_FAVORABLE,
        FAVORABLE,
        NEUTRAL,
        CHALLENGING,
        NEEDS_ATTENTION
    }

    data class RemedialMeasure(
        val category: RemedyCategory,
        val remedy: String,
        val timing: String,
        val priority: RemedyPriority
    )

    enum class RemedyCategory {
        MANTRA,
        CHARITY,
        GEMSTONE,
        FASTING,
        PILGRIMAGE,
        LIFESTYLE
    }

    enum class RemedyPriority {
        ESSENTIAL,
        RECOMMENDED,
        OPTIONAL
    }

    // ============================================
    // CORE CALCULATION METHODS
    // ============================================

    /**
     * Calculate the Bhrigu Bindu longitude from a Vedic chart
     *
     * The Bhrigu Bindu is calculated as the midpoint between Rahu and Moon.
     * Formula: BB = (Rahu + Moon) / 2
     *
     * If the result crosses 360°, it wraps around accordingly.
     *
     * @param chart The VedicChart containing planetary positions
     * @return The Bhrigu Bindu longitude in degrees (0-360)
     */
    fun calculateBhriguBindu(chart: VedicChart): Double {
        val rahuPosition = chart.planetPositions.find { it.planet == Planet.RAHU }
            ?: throw IllegalArgumentException("Rahu position not found in chart")
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found in chart")

        return calculateBhriguBinduFromLongitudes(rahuPosition.longitude, moonPosition.longitude)
    }

    /**
     * Calculate Bhrigu Bindu from raw longitudes
     *
     * @param rahuLongitude Rahu's longitude in degrees
     * @param moonLongitude Moon's longitude in degrees
     * @return Bhrigu Bindu longitude in degrees (0-360)
     */
    fun calculateBhriguBinduFromLongitudes(rahuLongitude: Double, moonLongitude: Double): Double {
        // Normalize inputs to 0-360 range
        val normalizedRahu = normalizeAngle(rahuLongitude)
        val normalizedMoon = normalizeAngle(moonLongitude)

        // Calculate the shorter arc midpoint
        val diff = Math.abs(normalizedRahu - normalizedMoon)

        return if (diff <= 180) {
            // Simple midpoint
            normalizeAngle((normalizedRahu + normalizedMoon) / 2)
        } else {
            // Midpoint across the 0/360 boundary
            normalizeAngle((normalizedRahu + normalizedMoon) / 2 + 180)
        }
    }

    /**
     * Perform complete Bhrigu Bindu analysis
     *
     * @param chart The VedicChart to analyze
     * @param currentDate Date for transit analysis (defaults to today)
     * @return Complete BhriguBinduAnalysis
     */
    fun analyzeBhriguBindu(
        chart: VedicChart,
        currentDate: LocalDate = LocalDate.now()
    ): BhriguBinduAnalysis {
        val rahuPosition = chart.planetPositions.find { it.planet == Planet.RAHU }
            ?: throw IllegalArgumentException("Rahu position not found in chart")
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found in chart")

        val bhriguBindu = calculateBhriguBinduFromLongitudes(rahuPosition.longitude, moonPosition.longitude)

        // Determine sign, nakshatra, and house
        val bbSign = VedicAstrologyUtils.getZodiacSign(bhriguBindu)
        val bbNakshatra = VedicAstrologyUtils.getNakshatra(bhriguBindu)
        val bbPada = VedicAstrologyUtils.getNakshatraPada(bhriguBindu)
        val bbHouse = calculateBhriguBinduHouse(chart, bhriguBindu)
        val bbLord = bbSign.rulingPlanet
        val nakshatraLord = bbNakshatra.rulingPlanet

        // Find aspects and conjunctions
        val aspectingPlanets = findAspectingPlanets(chart, bhriguBindu)
        val conjunctPlanets = findConjunctPlanets(chart, bhriguBindu)

        // Assess strength
        val strengthAssessment = assessStrength(chart, bhriguBindu, bbSign, bbNakshatra, bbHouse)

        // Transit analysis
        val transitAnalysis = analyzeTransits(chart, bhriguBindu, currentDate)

        // Generate interpretation
        val interpretation = generateInterpretation(
            bhriguBindu, bbSign, bbNakshatra, bbHouse,
            bbLord, nakshatraLord, aspectingPlanets, conjunctPlanets, strengthAssessment
        )

        return BhriguBinduAnalysis(
            bhriguBindu = bhriguBindu,
            bhriguBinduSign = bbSign,
            bhriguBinduNakshatra = bbNakshatra,
            bhriguBinduPada = bbPada,
            bhriguBinduHouse = bbHouse,
            bhriguBinduLord = bbLord,
            nakshatraLord = nakshatraLord,
            rahuLongitude = rahuPosition.longitude,
            moonLongitude = moonPosition.longitude,
            aspectingPlanets = aspectingPlanets,
            conjunctPlanets = conjunctPlanets,
            strengthAssessment = strengthAssessment,
            transitAnalysis = transitAnalysis,
            interpretation = interpretation
        )
    }

    /**
     * Calculate the house placement of Bhrigu Bindu
     */
    private fun calculateBhriguBinduHouse(chart: VedicChart, bhriguBindu: Double): Int {
        val ascendantLongitude = chart.ascendantLongitude
        val distance = normalizeAngle(bhriguBindu - ascendantLongitude)
        return ((distance / 30.0).toInt() % 12) + 1
    }

    /**
     * Find planets aspecting the Bhrigu Bindu
     */
    private fun findAspectingPlanets(chart: VedicChart, bhriguBindu: Double): List<AspectingPlanet> {
        val aspectingPlanets = mutableListOf<AspectingPlanet>()
        val classicalPlanets = listOf(
            Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
            Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.RAHU, Planet.KETU
        )

        for (position in chart.planetPositions) {
            if (position.planet !in classicalPlanets) continue

            val planetLong = position.longitude
            val distance = calculateAngularDistance(planetLong, bhriguBindu)

            // Check various aspects
            val aspectInfo = getAspectInfo(position.planet, distance)
            if (aspectInfo != null) {
                aspectingPlanets.add(
                    AspectingPlanet(
                        planet = position.planet,
                        aspectType = aspectInfo.first,
                        aspectStrength = aspectInfo.second,
                        isApplying = isAspectApplying(position.planet, planetLong, bhriguBindu, chart)
                    )
                )
            }
        }

        return aspectingPlanets.sortedByDescending { it.aspectStrength }
    }

    /**
     * Get aspect information based on angular distance
     */
    private fun getAspectInfo(planet: Planet, distance: Double): Pair<AspectType, Double>? {
        // Conjunction (within 10 degrees)
        if (distance <= 10) {
            return Pair(AspectType.CONJUNCTION, 1.0 - (distance / 10))
        }

        // Opposition (within 10 degrees of 180)
        if (Math.abs(distance - 180) <= 10) {
            return Pair(AspectType.OPPOSITION, 1.0 - Math.abs(distance - 180) / 10)
        }

        // Trine (within 10 degrees of 120 or 240)
        if (Math.abs(distance - 120) <= 10 || Math.abs(distance - 240) <= 10) {
            val triDist = minOf(Math.abs(distance - 120), Math.abs(distance - 240))
            return Pair(AspectType.TRINE, 1.0 - triDist / 10)
        }

        // Square (within 10 degrees of 90 or 270)
        if (Math.abs(distance - 90) <= 10 || Math.abs(distance - 270) <= 10) {
            val sqDist = minOf(Math.abs(distance - 90), Math.abs(distance - 270))
            return Pair(AspectType.SQUARE, 1.0 - sqDist / 10)
        }

        // Sextile (within 8 degrees of 60 or 300)
        if (Math.abs(distance - 60) <= 8 || Math.abs(distance - 300) <= 8) {
            val sexDist = minOf(Math.abs(distance - 60), Math.abs(distance - 300))
            return Pair(AspectType.SEXTILE, 1.0 - sexDist / 8)
        }

        // Special aspects for Mars, Jupiter, Saturn
        when (planet) {
            Planet.MARS -> {
                // Mars special aspect to 4th and 8th houses (90 and 210 degrees approximately)
                if (Math.abs(distance - 90) <= 12 || Math.abs(distance - 210) <= 12) {
                    val spDist = minOf(Math.abs(distance - 90), Math.abs(distance - 210))
                    return Pair(AspectType.SPECIAL_ASPECT, 0.75 * (1.0 - spDist / 12))
                }
            }
            Planet.JUPITER -> {
                // Jupiter special aspect to 5th and 9th houses (120 and 240 degrees)
                // Already covered by trine
            }
            Planet.SATURN -> {
                // Saturn special aspect to 3rd and 10th houses (60 and 270 degrees)
                if (Math.abs(distance - 60) <= 12 || Math.abs(distance - 270) <= 12) {
                    val spDist = minOf(Math.abs(distance - 60), Math.abs(distance - 270))
                    return Pair(AspectType.SPECIAL_ASPECT, 0.75 * (1.0 - spDist / 12))
                }
            }
            else -> {}
        }

        return null
    }

    /**
     * Check if an aspect is applying (planet moving toward exact aspect)
     */
    private fun isAspectApplying(
        planet: Planet,
        planetLong: Double,
        bhriguBindu: Double,
        chart: VedicChart
    ): Boolean {
        // Check if planet is retrograde
        val position = chart.planetPositions.find { it.planet == planet }
        if (position?.isRetrograde == true) {
            // Retrograde planets move backward
            return calculateAngularDistance(planetLong, bhriguBindu) < 5
        }
        // Direct motion planets generally apply when behind the point
        return normalizeAngle(bhriguBindu - planetLong) < 180
    }

    /**
     * Find planets conjunct with Bhrigu Bindu (within 10 degrees in same sign)
     */
    private fun findConjunctPlanets(chart: VedicChart, bhriguBindu: Double): List<Planet> {
        val bbSign = VedicAstrologyUtils.getZodiacSign(bhriguBindu)
        val conjunctPlanets = mutableListOf<Planet>()

        for (position in chart.planetPositions) {
            val planetSign = VedicAstrologyUtils.getZodiacSign(position.longitude)
            if (planetSign == bbSign) {
                val distance = calculateAngularDistance(position.longitude, bhriguBindu)
                if (distance <= 10) {
                    conjunctPlanets.add(position.planet)
                }
            }
        }

        return conjunctPlanets
    }

    /**
     * Assess the strength of Bhrigu Bindu
     */
    private fun assessStrength(
        chart: VedicChart,
        bhriguBindu: Double,
        bbSign: ZodiacSign,
        bbNakshatra: Nakshatra,
        bbHouse: Int
    ): StrengthAssessment {
        val factors = mutableListOf<StrengthFactor>()
        var beneficScore = 0.0
        var maleficScore = 0.0

        // 1. Assess lord strength
        val lord = bbSign.rulingPlanet
        val lordPosition = chart.planetPositions.find { it.planet == lord }
        val lordStrength = assessPlanetStrength(chart, lord, lordPosition)

        factors.add(
            StrengthFactor(
                factor = "Sign Lord ($lord)",
                influence = when (lordStrength) {
                    LordStrength.VERY_STRONG -> FactorInfluence.HIGHLY_POSITIVE
                    LordStrength.STRONG -> FactorInfluence.POSITIVE
                    LordStrength.MODERATE -> FactorInfluence.NEUTRAL
                    LordStrength.WEAK -> FactorInfluence.NEGATIVE
                    LordStrength.VERY_WEAK -> FactorInfluence.HIGHLY_NEGATIVE
                },
                description = "The lord of BB's sign affects its manifestation"
            )
        )

        // 2. Assess nakshatra lord strength
        val nakshatraLord = bbNakshatra.rulingPlanet
        val nakshatraLordPosition = chart.planetPositions.find { it.planet == nakshatraLord }
        val nakshatraLordStrength = assessPlanetStrength(chart, nakshatraLord, nakshatraLordPosition)

        factors.add(
            StrengthFactor(
                factor = "Nakshatra Lord ($nakshatraLord)",
                influence = when (nakshatraLordStrength) {
                    LordStrength.VERY_STRONG -> FactorInfluence.HIGHLY_POSITIVE
                    LordStrength.STRONG -> FactorInfluence.POSITIVE
                    LordStrength.MODERATE -> FactorInfluence.NEUTRAL
                    LordStrength.WEAK -> FactorInfluence.NEGATIVE
                    LordStrength.VERY_WEAK -> FactorInfluence.HIGHLY_NEGATIVE
                },
                description = "The nakshatra lord influences timing of events"
            )
        )

        // 3. House placement
        val houseInfluence = when (bbHouse) {
            1, 5, 9 -> FactorInfluence.HIGHLY_POSITIVE // Trikona
            4, 7, 10 -> FactorInfluence.POSITIVE // Kendra
            2, 11 -> FactorInfluence.POSITIVE // Wealth houses
            3 -> FactorInfluence.NEUTRAL // Upachaya
            6, 8, 12 -> FactorInfluence.CHALLENGING // Dusthana
            else -> FactorInfluence.NEUTRAL
        }
        factors.add(
            StrengthFactor(
                factor = "House $bbHouse Placement",
                influence = houseInfluence,
                description = getHousePlacementDescription(bbHouse)
            )
        )

        // 4. Check benefic influences
        val benefics = listOf(Planet.JUPITER, Planet.VENUS)
        for (position in chart.planetPositions) {
            if (position.planet in benefics) {
                val distance = calculateAngularDistance(position.longitude, bhriguBindu)
                if (distance <= 30) {
                    beneficScore += (30 - distance) / 30.0
                    factors.add(
                        StrengthFactor(
                            factor = "${position.planet} influence",
                            influence = FactorInfluence.POSITIVE,
                            description = "${position.planet} aspects/conjoins BB, providing protection and growth"
                        )
                    )
                }
            }
        }

        // 5. Check malefic influences
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)
        for (position in chart.planetPositions) {
            if (position.planet in malefics) {
                val distance = calculateAngularDistance(position.longitude, bhriguBindu)
                if (distance <= 30) {
                    maleficScore += (30 - distance) / 30.0
                    factors.add(
                        StrengthFactor(
                            factor = "${position.planet} influence",
                            influence = FactorInfluence.CHALLENGING,
                            description = "${position.planet} aspects/conjoins BB, indicating karmic lessons"
                        )
                    )
                }
            }
        }

        // Calculate overall strength
        val strengthScore = (
            when (lordStrength) {
                LordStrength.VERY_STRONG -> 2.0
                LordStrength.STRONG -> 1.5
                LordStrength.MODERATE -> 1.0
                LordStrength.WEAK -> 0.5
                LordStrength.VERY_WEAK -> 0.0
            } +
            when (nakshatraLordStrength) {
                LordStrength.VERY_STRONG -> 2.0
                LordStrength.STRONG -> 1.5
                LordStrength.MODERATE -> 1.0
                LordStrength.WEAK -> 0.5
                LordStrength.VERY_WEAK -> 0.0
            } +
            when (houseInfluence) {
                FactorInfluence.HIGHLY_POSITIVE -> 2.0
                FactorInfluence.POSITIVE -> 1.5
                FactorInfluence.NEUTRAL -> 1.0
                FactorInfluence.NEGATIVE -> 0.5
                FactorInfluence.HIGHLY_NEGATIVE -> 0.0
            } +
            beneficScore - (maleficScore * 0.5)
        )

        val overallStrength = when {
            strengthScore >= 6.0 -> OverallStrength.EXCELLENT
            strengthScore >= 4.5 -> OverallStrength.GOOD
            strengthScore >= 3.0 -> OverallStrength.MODERATE
            strengthScore >= 1.5 -> OverallStrength.CHALLENGING
            else -> OverallStrength.DIFFICULT
        }

        return StrengthAssessment(
            lordStrength = lordStrength,
            nakshatraLordStrength = nakshatraLordStrength,
            beneficInfluence = beneficScore,
            maleficInfluence = maleficScore,
            overallStrength = overallStrength,
            factors = factors
        )
    }

    /**
     * Assess individual planet strength
     */
    private fun assessPlanetStrength(
        chart: VedicChart,
        planet: Planet,
        position: com.astro.storm.data.model.PlanetPosition?
    ): LordStrength {
        if (position == null) return LordStrength.MODERATE

        var score = 0

        // Check dignity
        val sign = VedicAstrologyUtils.getZodiacSign(position.longitude)
        if (sign.rulingPlanet == planet) score += 2 // Own sign
        if (AstrologicalConstants.EXALTATION_SIGNS[planet] == sign) score += 3 // Exalted
        if (AstrologicalConstants.DEBILITATION_SIGNS[planet] == sign) score -= 2 // Debilitated

        // Check house placement
        val house = ((normalizeAngle(position.longitude - chart.ascendantLongitude) / 30.0).toInt() % 12) + 1
        if (house in AstrologicalConstants.KENDRA_HOUSES) score += 1
        if (house in AstrologicalConstants.TRIKONA_HOUSES) score += 1
        if (house in AstrologicalConstants.DUSTHANA_HOUSES) score -= 1

        // Check retrograde
        if (position.isRetrograde) {
            // Retrograde planets have different effects
            if (planet in listOf(Planet.MERCURY, Planet.VENUS)) score -= 1
            else score += 0 // Outer planets can be strong when retrograde
        }

        return when {
            score >= 4 -> LordStrength.VERY_STRONG
            score >= 2 -> LordStrength.STRONG
            score >= 0 -> LordStrength.MODERATE
            score >= -2 -> LordStrength.WEAK
            else -> LordStrength.VERY_WEAK
        }
    }

    /**
     * Analyze current and upcoming transits to Bhrigu Bindu
     */
    private fun analyzeTransits(
        chart: VedicChart,
        bhriguBindu: Double,
        currentDate: LocalDate
    ): TransitAnalysis {
        // Note: In a production app, you would get current planetary positions
        // from an ephemeris. Here we provide the structure for analysis.

        val currentTransits = mutableListOf<TransitingPlanet>()
        val upcomingTransits = mutableListOf<UpcomingTransit>()
        val significantPeriods = mutableListOf<SignificantPeriod>()

        // For demonstration, analyze natal positions as proxy for transits
        // In production, use SwissEphemerisEngine to get current positions
        for (position in chart.planetPositions) {
            val distance = calculateAngularDistance(position.longitude, bhriguBindu)
            val aspectType = getAspectInfo(position.planet, distance)?.first

            currentTransits.add(
                TransitingPlanet(
                    planet = position.planet,
                    longitude = position.longitude,
                    distanceFromBB = distance,
                    isConjunct = distance <= 10,
                    aspectType = aspectType,
                    effectDescription = getTransitEffectDescription(position.planet, distance, aspectType)
                )
            )
        }

        // Calculate upcoming significant transits
        // Jupiter transit over BB is highly significant (happens every ~12 years)
        upcomingTransits.add(
            UpcomingTransit(
                planet = Planet.JUPITER,
                transitType = TransitType.CONJUNCTION,
                estimatedDate = currentDate.plusMonths(6), // Placeholder
                significance = TransitSignificance.HIGHLY_SIGNIFICANT
            )
        )

        // Saturn transit is also significant
        upcomingTransits.add(
            UpcomingTransit(
                planet = Planet.SATURN,
                transitType = TransitType.CONJUNCTION,
                estimatedDate = currentDate.plusYears(1), // Placeholder
                significance = TransitSignificance.SIGNIFICANT
            )
        )

        return TransitAnalysis(
            currentTransits = currentTransits.sortedBy { it.distanceFromBB },
            upcomingTransits = upcomingTransits,
            significantPeriods = significantPeriods
        )
    }

    /**
     * Generate interpretation for Bhrigu Bindu analysis
     */
    private fun generateInterpretation(
        bhriguBindu: Double,
        bbSign: ZodiacSign,
        bbNakshatra: Nakshatra,
        bbHouse: Int,
        bbLord: Planet,
        nakshatraLord: Planet,
        aspectingPlanets: List<AspectingPlanet>,
        conjunctPlanets: List<Planet>,
        strengthAssessment: StrengthAssessment
    ): BhriguBinduInterpretation {

        val generalMeaning = buildString {
            append("Your Bhrigu Bindu is placed in ${bbSign.displayName} (${bbSign.element.lowercase()} sign) ")
            append("in the ${bbNakshatra.displayName} nakshatra, pada $bbNakshatra. ")
            append("It falls in your ${getHouseName(bbHouse)} house, ")
            append("indicating that karmic manifestations primarily occur through ${getHouseTheme(bbHouse)}. ")
            append("The sign lord is ${bbLord.displayName} and nakshatra lord is ${nakshatraLord.displayName}.")
        }

        val karmicSignificance = buildString {
            append("The Bhrigu Bindu represents your karmic destiny point, showing where ")
            append("significant life events tend to manifest. ")
            when (strengthAssessment.overallStrength) {
                OverallStrength.EXCELLENT -> append("With excellent strength, your BB indicates smooth karmic flow and favorable life events.")
                OverallStrength.GOOD -> append("Good strength suggests generally positive karmic patterns with periodic growth opportunities.")
                OverallStrength.MODERATE -> append("Moderate strength indicates a balanced karmic pattern requiring conscious effort for optimal results.")
                OverallStrength.CHALLENGING -> append("Challenging strength suggests karmic lessons that require patience and remedial measures.")
                OverallStrength.DIFFICULT -> append("This placement indicates significant karmic debts that need attention through spiritual practices and remedies.")
            }
        }

        val lifeAreas = generateLifeAreaInfluences(bbHouse, bbSign, strengthAssessment)

        val recommendations = generateRecommendations(bbSign, bbNakshatra, bbHouse, bbLord, nakshatraLord)

        val auspiciousDays = generateAuspiciousDays(bbLord, nakshatraLord)

        val remedialMeasures = generateRemedialMeasures(bbLord, nakshatraLord, strengthAssessment)

        return BhriguBinduInterpretation(
            generalMeaning = generalMeaning,
            karmicSignificance = karmicSignificance,
            lifeAreas = lifeAreas,
            recommendations = recommendations,
            auspiciousDays = auspiciousDays,
            remedialMeasures = remedialMeasures
        )
    }

    /**
     * Generate life area influences based on BB placement
     */
    private fun generateLifeAreaInfluences(
        bbHouse: Int,
        bbSign: ZodiacSign,
        strengthAssessment: StrengthAssessment
    ): List<LifeAreaInfluence> {
        val influences = mutableListOf<LifeAreaInfluence>()

        // Primary house influence
        when (bbHouse) {
            1 -> influences.add(LifeAreaInfluence(LifeArea.HEALTH, getInfluence(strengthAssessment), "BB in 1st house strongly influences personality, health, and overall life direction."))
            2 -> influences.add(LifeAreaInfluence(LifeArea.WEALTH, getInfluence(strengthAssessment), "BB in 2nd house emphasizes wealth accumulation, family values, and speech."))
            3 -> influences.add(LifeAreaInfluence(LifeArea.CAREER, getInfluence(strengthAssessment), "BB in 3rd house highlights courage, siblings, and communication skills."))
            4 -> influences.add(LifeAreaInfluence(LifeArea.FAMILY, getInfluence(strengthAssessment), "BB in 4th house emphasizes mother, property, vehicles, and domestic happiness."))
            5 -> influences.add(LifeAreaInfluence(LifeArea.EDUCATION, getInfluence(strengthAssessment), "BB in 5th house highlights children, creativity, romance, and education."))
            6 -> influences.add(LifeAreaInfluence(LifeArea.HEALTH, getInfluence(strengthAssessment), "BB in 6th house relates to health challenges, service, and overcoming enemies."))
            7 -> influences.add(LifeAreaInfluence(LifeArea.RELATIONSHIPS, getInfluence(strengthAssessment), "BB in 7th house strongly influences marriage, partnerships, and public dealings."))
            8 -> influences.add(LifeAreaInfluence(LifeArea.SPIRITUALITY, getInfluence(strengthAssessment), "BB in 8th house indicates transformation, inheritance, and occult interests."))
            9 -> influences.add(LifeAreaInfluence(LifeArea.SPIRITUALITY, getInfluence(strengthAssessment), "BB in 9th house highlights luck, higher education, and spiritual growth."))
            10 -> influences.add(LifeAreaInfluence(LifeArea.CAREER, getInfluence(strengthAssessment), "BB in 10th house strongly emphasizes career, status, and public reputation."))
            11 -> influences.add(LifeAreaInfluence(LifeArea.WEALTH, getInfluence(strengthAssessment), "BB in 11th house indicates gains, fulfillment of desires, and social networks."))
            12 -> influences.add(LifeAreaInfluence(LifeArea.FOREIGN_CONNECTIONS, getInfluence(strengthAssessment), "BB in 12th house relates to foreign lands, spirituality, and final liberation."))
        }

        // Add secondary influences based on sign element
        when (bbSign.element) {
            "Fire" -> influences.add(LifeAreaInfluence(LifeArea.CAREER, AreaInfluence.FAVORABLE, "Fire element adds energy and initiative to your endeavors."))
            "Earth" -> influences.add(LifeAreaInfluence(LifeArea.WEALTH, AreaInfluence.FAVORABLE, "Earth element supports material stability and practical achievements."))
            "Air" -> influences.add(LifeAreaInfluence(LifeArea.EDUCATION, AreaInfluence.FAVORABLE, "Air element enhances intellectual pursuits and social connections."))
            "Water" -> influences.add(LifeAreaInfluence(LifeArea.RELATIONSHIPS, AreaInfluence.FAVORABLE, "Water element deepens emotional connections and intuitive abilities."))
        }

        return influences
    }

    private fun getInfluence(strengthAssessment: StrengthAssessment): AreaInfluence {
        return when (strengthAssessment.overallStrength) {
            OverallStrength.EXCELLENT -> AreaInfluence.VERY_FAVORABLE
            OverallStrength.GOOD -> AreaInfluence.FAVORABLE
            OverallStrength.MODERATE -> AreaInfluence.NEUTRAL
            OverallStrength.CHALLENGING -> AreaInfluence.CHALLENGING
            OverallStrength.DIFFICULT -> AreaInfluence.NEEDS_ATTENTION
        }
    }

    /**
     * Generate recommendations based on BB placement
     */
    private fun generateRecommendations(
        bbSign: ZodiacSign,
        bbNakshatra: Nakshatra,
        bbHouse: Int,
        bbLord: Planet,
        nakshatraLord: Planet
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Lord-based recommendations
        recommendations.add("Strengthen ${bbLord.displayName} through its specific remedies for enhanced BB activation.")
        recommendations.add("Honor ${nakshatraLord.displayName} on its weekday for favorable nakshatra results.")

        // House-based recommendations
        when (bbHouse) {
            1, 5, 9 -> recommendations.add("Your BB is in a Trikona house - focus on dharmic activities for maximum benefit.")
            4, 7, 10 -> recommendations.add("Your BB is in a Kendra house - take action on important matters when planets transit here.")
            6, 8, 12 -> recommendations.add("Your BB is in a challenging house - regular spiritual practices and charity are recommended.")
        }

        // Sign-based recommendations
        when (bbSign.element) {
            "Fire" -> recommendations.add("Engage in activities during morning hours when fire element is strong.")
            "Earth" -> recommendations.add("Focus on practical, grounded activities for best results.")
            "Air" -> recommendations.add("Communication and networking will activate your BB positively.")
            "Water" -> recommendations.add("Trust your intuition, especially during emotionally significant times.")
        }

        return recommendations
    }

    /**
     * Generate auspicious days based on lords
     */
    private fun generateAuspiciousDays(bbLord: Planet, nakshatraLord: Planet): List<String> {
        val days = mutableListOf<String>()

        // Add lord's day
        days.add(getPlanetaryDay(bbLord) + " (BB Sign Lord's day)")
        if (nakshatraLord != bbLord) {
            days.add(getPlanetaryDay(nakshatraLord) + " (Nakshatra Lord's day)")
        }

        // Add Jupiter's day for general auspiciousness
        if (bbLord != Planet.JUPITER && nakshatraLord != Planet.JUPITER) {
            days.add("Thursday (Jupiter's day for wisdom and expansion)")
        }

        return days
    }

    /**
     * Generate remedial measures based on BB analysis
     */
    private fun generateRemedialMeasures(
        bbLord: Planet,
        nakshatraLord: Planet,
        strengthAssessment: StrengthAssessment
    ): List<RemedialMeasure> {
        val remedies = mutableListOf<RemedialMeasure>()

        // Mantra for sign lord
        remedies.add(
            RemedialMeasure(
                category = RemedyCategory.MANTRA,
                remedy = "Recite ${getPlanetaryMantra(bbLord)} 108 times",
                timing = "On ${getPlanetaryDay(bbLord)} during ${bbLord.displayName}'s hora",
                priority = if (strengthAssessment.lordStrength in listOf(LordStrength.WEAK, LordStrength.VERY_WEAK))
                    RemedyPriority.ESSENTIAL else RemedyPriority.RECOMMENDED
            )
        )

        // Charity recommendation
        remedies.add(
            RemedialMeasure(
                category = RemedyCategory.CHARITY,
                remedy = "Donate ${getPlanetaryCharity(bbLord)}",
                timing = "On ${getPlanetaryDay(bbLord)}",
                priority = RemedyPriority.RECOMMENDED
            )
        )

        // If nakshatra lord is different and weak
        if (nakshatraLord != bbLord && strengthAssessment.nakshatraLordStrength in listOf(LordStrength.WEAK, LordStrength.VERY_WEAK)) {
            remedies.add(
                RemedialMeasure(
                    category = RemedyCategory.MANTRA,
                    remedy = "Recite ${getPlanetaryMantra(nakshatraLord)} 108 times",
                    timing = "On ${getPlanetaryDay(nakshatraLord)}",
                    priority = RemedyPriority.RECOMMENDED
                )
            )
        }

        // General spiritual practice
        remedies.add(
            RemedialMeasure(
                category = RemedyCategory.LIFESTYLE,
                remedy = "Meditate during Brahma Muhurta (before sunrise)",
                timing = "Daily, especially on ${getPlanetaryDay(bbLord)}",
                priority = RemedyPriority.OPTIONAL
            )
        )

        return remedies
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Normalize angle to 0-360 range
     */
    private fun normalizeAngle(angle: Double): Double {
        var normalized = angle % 360.0
        if (normalized < 0) normalized += 360.0
        return normalized
    }

    /**
     * Calculate angular distance between two points
     */
    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = Math.abs(normalizeAngle(long1) - normalizeAngle(long2))
        return if (diff > 180) 360 - diff else diff
    }

    /**
     * Get house placement description
     */
    private fun getHousePlacementDescription(house: Int): String {
        return when (house) {
            1 -> "Excellent placement in Lagna (1st) - strong karmic activation for self-development"
            2 -> "2nd house placement emphasizes wealth and family matters"
            3 -> "3rd house placement activates courage, communication, and sibling relationships"
            4 -> "4th house placement focuses on home, mother, and emotional security"
            5 -> "Auspicious 5th house placement for creativity, children, and romance"
            6 -> "6th house placement indicates karmic lessons through obstacles and service"
            7 -> "7th house placement strongly influences partnerships and public dealings"
            8 -> "8th house placement indicates transformative experiences and research abilities"
            9 -> "Highly auspicious 9th house placement for luck, dharma, and higher learning"
            10 -> "10th house placement strongly activates career and public reputation"
            11 -> "11th house placement is excellent for gains and fulfillment of desires"
            12 -> "12th house placement emphasizes spiritual growth and foreign connections"
            else -> "House placement affects karmic manifestation in related life areas"
        }
    }

    /**
     * Get house name
     */
    private fun getHouseName(house: Int): String {
        return when (house) {
            1 -> "1st (Lagna)"
            2 -> "2nd (Dhana)"
            3 -> "3rd (Sahaja)"
            4 -> "4th (Sukha)"
            5 -> "5th (Putra)"
            6 -> "6th (Ripu)"
            7 -> "7th (Kalatra)"
            8 -> "8th (Ayu)"
            9 -> "9th (Dharma)"
            10 -> "10th (Karma)"
            11 -> "11th (Labha)"
            12 -> "12th (Vyaya)"
            else -> "${house}th"
        }
    }

    /**
     * Get house theme
     */
    private fun getHouseTheme(house: Int): String {
        return when (house) {
            1 -> "self, personality, and health"
            2 -> "wealth, family, and speech"
            3 -> "courage, siblings, and communication"
            4 -> "mother, home, and emotional happiness"
            5 -> "children, creativity, and romance"
            6 -> "health, service, and competition"
            7 -> "marriage, partnerships, and business"
            8 -> "transformation, inheritance, and research"
            9 -> "luck, higher learning, and spirituality"
            10 -> "career, status, and public life"
            11 -> "gains, friends, and fulfillment"
            12 -> "spirituality, foreign lands, and liberation"
            else -> "life events"
        }
    }

    /**
     * Get transit effect description
     */
    private fun getTransitEffectDescription(planet: Planet, distance: Double, aspectType: AspectType?): String {
        return when {
            distance <= 3 -> "${planet.displayName} is in very close proximity to BB - highly activated period"
            distance <= 10 -> "${planet.displayName} is conjunct BB - significant activation"
            aspectType == AspectType.TRINE -> "${planet.displayName} forms harmonious trine - favorable period"
            aspectType == AspectType.OPPOSITION -> "${planet.displayName} opposes BB - awareness and balance needed"
            aspectType == AspectType.SQUARE -> "${planet.displayName} squares BB - challenges promoting growth"
            else -> "${planet.displayName} at ${String.format("%.1f", distance)}° from BB"
        }
    }

    /**
     * Get planetary day
     */
    private fun getPlanetaryDay(planet: Planet): String {
        return when (planet) {
            Planet.SUN -> "Sunday"
            Planet.MOON -> "Monday"
            Planet.MARS -> "Tuesday"
            Planet.MERCURY -> "Wednesday"
            Planet.JUPITER -> "Thursday"
            Planet.VENUS -> "Friday"
            Planet.SATURN -> "Saturday"
            Planet.RAHU -> "Saturday"
            Planet.KETU -> "Tuesday"
            else -> "Appropriate day"
        }
    }

    /**
     * Get planetary mantra
     */
    private fun getPlanetaryMantra(planet: Planet): String {
        return when (planet) {
            Planet.SUN -> "Om Suryaya Namah"
            Planet.MOON -> "Om Chandraya Namah"
            Planet.MARS -> "Om Mangalaya Namah"
            Planet.MERCURY -> "Om Budhaya Namah"
            Planet.JUPITER -> "Om Gurave Namah"
            Planet.VENUS -> "Om Shukraya Namah"
            Planet.SATURN -> "Om Shanicharaya Namah"
            Planet.RAHU -> "Om Rahave Namah"
            Planet.KETU -> "Om Ketave Namah"
            else -> "appropriate planetary mantra"
        }
    }

    /**
     * Get planetary charity items
     */
    private fun getPlanetaryCharity(planet: Planet): String {
        return when (planet) {
            Planet.SUN -> "wheat, jaggery, or red cloth"
            Planet.MOON -> "rice, white cloth, or milk products"
            Planet.MARS -> "red lentils, red cloth, or copper items"
            Planet.MERCURY -> "green vegetables, moong dal, or green cloth"
            Planet.JUPITER -> "yellow items, turmeric, or banana"
            Planet.VENUS -> "white items, sugar, or dairy products"
            Planet.SATURN -> "black sesame, mustard oil, or iron items"
            Planet.RAHU -> "black/blue items or coconut"
            Planet.KETU -> "seven grains mixture or blanket"
            else -> "appropriate items"
        }
    }
}
