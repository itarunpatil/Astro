package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.HouseSystem
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs

/**
 * Comprehensive Transit Analysis System
 *
 * This class provides complete transit analysis including:
 * 1. Current planetary positions (real-time)
 * 2. Transit-to-natal aspect calculations
 * 3. Gochara (transit) analysis with Vedha points
 * 4. Ashtakavarga-based transit scoring
 * 5. Transit timeline for significant periods
 *
 * Gochara Rules (Transit from Moon Sign):
 * Based on classical Vedic texts including Phaladeepika and Brihat Samhita
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
class TransitAnalyzer(private val context: Context) {

    private val ephemerisEngine = SwissEphemerisEngine.create(context)

    companion object {
        /**
         * Vedha (Obstruction) Points
         * When a planet transiting a favorable house has another planet in its Vedha point,
         * the favorable effects are obstructed/nullified.
         *
         * Format: Map<FavorableHouse, VedhaHouse>
         * Note: Some combinations have mutual Vedha
         */
        private val SUN_VEDHA = mapOf(
            3 to 9, 9 to 3,  // Sun in 3rd blocked by planet in 9th and vice versa
            6 to 12, 12 to 6,
            10 to 4, 4 to 10,
            11 to 5, 5 to 11
        )

        private val MOON_VEDHA = mapOf(
            1 to 5, 5 to 1,
            3 to 9, 9 to 3,
            6 to 12, 12 to 6,
            7 to 2, 2 to 7,
            10 to 4, 4 to 10,
            11 to 8, 8 to 11
        )

        private val MARS_VEDHA = mapOf(
            3 to 12, 12 to 3,
            6 to 9, 9 to 6,
            11 to 5, 5 to 11
        )

        private val MERCURY_VEDHA = mapOf(
            2 to 5, 5 to 2,
            4 to 3, 3 to 4,
            6 to 9, 9 to 6,
            8 to 1, 1 to 8,
            10 to 8, // 10 blocked by 8
            11 to 12, 12 to 11
        )

        private val JUPITER_VEDHA = mapOf(
            2 to 12, 12 to 2,
            5 to 4, 4 to 5,
            7 to 3, 3 to 7,
            9 to 10, 10 to 9,
            11 to 8, 8 to 11
        )

        private val VENUS_VEDHA = mapOf(
            1 to 8, 8 to 1,
            2 to 7, 7 to 2,
            3 to 1, 1 to 3,
            4 to 10, 10 to 4,
            5 to 9, 9 to 5,
            8 to 5, 5 to 8,
            9 to 11, 11 to 9,
            11 to 6, 6 to 11,
            12 to 3, 3 to 12
        )

        private val SATURN_VEDHA = mapOf(
            3 to 12, 12 to 3,
            6 to 9, 9 to 6,
            11 to 5, 5 to 11
        )

        /**
         * Favorable transit houses from Moon (without Vedha consideration)
         * Based on classical Gochara rules
         */
        private val FAVORABLE_TRANSITS = mapOf(
            Planet.SUN to listOf(3, 6, 10, 11),
            Planet.MOON to listOf(1, 3, 6, 7, 10, 11),
            Planet.MARS to listOf(3, 6, 11),
            Planet.MERCURY to listOf(2, 4, 6, 8, 10, 11),
            Planet.JUPITER to listOf(2, 5, 7, 9, 11),
            Planet.VENUS to listOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
            Planet.SATURN to listOf(3, 6, 11),
            Planet.RAHU to listOf(3, 6, 10, 11),
            Planet.KETU to listOf(3, 6, 10, 11)
        )

        /**
         * Neutral transit houses from Moon
         */
        private val NEUTRAL_TRANSITS = mapOf(
            Planet.SUN to listOf(1, 2, 5),
            Planet.MOON to listOf(2, 5),
            Planet.MARS to listOf(1, 10),
            Planet.MERCURY to listOf(1, 3, 5),
            Planet.JUPITER to listOf(1, 4, 6, 8, 10),
            Planet.VENUS to listOf(6, 7, 10),
            Planet.SATURN to listOf(1, 2, 10),
            Planet.RAHU to listOf(1, 2, 5),
            Planet.KETU to listOf(1, 2, 5)
        )

        /**
         * Difficult transit houses from Moon
         */
        private val DIFFICULT_TRANSITS = mapOf(
            Planet.SUN to listOf(4, 7, 8, 9, 12),
            Planet.MOON to listOf(4, 8, 9, 12),
            Planet.MARS to listOf(2, 4, 5, 7, 8, 9, 12),
            Planet.MERCURY to listOf(7, 9, 12),
            Planet.JUPITER to listOf(3, 12),
            Planet.VENUS to listOf(6, 7, 10),
            Planet.SATURN to listOf(4, 5, 7, 8, 9, 12),
            Planet.RAHU to listOf(4, 7, 8, 9, 12),
            Planet.KETU to listOf(4, 7, 8, 9, 12)
        )

        /**
         * Aspect angles and their names
         */
        private val ASPECT_ANGLES = mapOf(
            0.0 to "Conjunction",
            60.0 to "Sextile",
            90.0 to "Square",
            120.0 to "Trine",
            180.0 to "Opposition"
        )

        /**
         * Orb allowances for transit aspects
         */
        private val TRANSIT_ORBS = mapOf(
            Planet.SUN to 8.0,
            Planet.MOON to 8.0,
            Planet.MERCURY to 6.0,
            Planet.VENUS to 6.0,
            Planet.MARS to 6.0,
            Planet.JUPITER to 8.0,
            Planet.SATURN to 8.0,
            Planet.RAHU to 5.0,
            Planet.KETU to 5.0
        )
    }

    /**
     * Complete transit analysis result
     */
    data class TransitAnalysis(
        val natalChart: VedicChart,
        val transitDateTime: LocalDateTime,
        val transitPositions: List<PlanetPosition>,
        val gocharaResults: List<GocharaResult>,
        val transitAspects: List<TransitAspect>,
        val ashtakavargaScores: Map<Planet, AshtakavargaCalculator.TransitScore>,
        val overallAssessment: OverallTransitAssessment,
        val significantPeriods: List<SignificantPeriod>
    ) {
        fun toPlainText(): String = buildString {
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("              TRANSIT ANALYSIS REPORT")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine("Transit Date/Time: $transitDateTime")
            appendLine()

            appendLine("CURRENT PLANETARY POSITIONS")
            appendLine("─────────────────────────────────────────────────────────")
            transitPositions.forEach { pos ->
                val retro = if (pos.isRetrograde) " (R)" else ""
                appendLine("${pos.planet.displayName.padEnd(10)}: ${pos.sign.displayName.padEnd(12)} ${formatDegree(pos.longitude)}$retro")
            }
            appendLine()

            appendLine("GOCHARA ANALYSIS (Transit from Moon)")
            appendLine("─────────────────────────────────────────────────────────")
            gocharaResults.forEach { result ->
                val vedhaStr = if (result.isVedhaAffected) " [VEDHA]" else ""
                appendLine("${result.planet.displayName.padEnd(10)}: House ${result.houseFromMoon.toString().padStart(2)} - ${result.effect.displayName}$vedhaStr")
                if (result.isVedhaAffected && result.vedhaSource != null) {
                    appendLine("             └─ Vedha from ${result.vedhaSource.displayName}")
                }
            }
            appendLine()

            appendLine("TRANSIT ASPECTS TO NATAL PLANETS")
            appendLine("─────────────────────────────────────────────────────────")
            if (transitAspects.isEmpty()) {
                appendLine("No significant aspects currently active.")
            } else {
                transitAspects.sortedByDescending { it.strength }.take(10).forEach { aspect ->
                    val applying = if (aspect.isApplying) "Applying" else "Separating"
                    appendLine("Transit ${aspect.transitingPlanet.displayName} ${aspect.aspectType} Natal ${aspect.natalPlanet.displayName}")
                    appendLine("  Orb: ${String.format("%.2f", aspect.orb)}° ($applying) | Strength: ${String.format("%.0f", aspect.strength * 100)}%")
                }
            }
            appendLine()

            appendLine("ASHTAKAVARGA TRANSIT SCORES")
            appendLine("─────────────────────────────────────────────────────────")
            ashtakavargaScores.forEach { (planet, score) ->
                appendLine("${planet.displayName.padEnd(10)}: BAV=${score.binduScore}, SAV=${score.savScore} - ${score.interpretation}")
            }
            appendLine()

            appendLine("OVERALL ASSESSMENT")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("Period Quality: ${overallAssessment.quality.displayName}")
            appendLine("Score: ${String.format("%.1f", overallAssessment.score)}/100")
            appendLine()
            appendLine("Summary: ${overallAssessment.summary}")
            appendLine()
            appendLine("Key Areas of Focus:")
            overallAssessment.focusAreas.forEachIndexed { index, area ->
                appendLine("${index + 1}. $area")
            }
        }

        private fun formatDegree(longitude: Double): String {
            val degInSign = longitude % 30.0
            val deg = degInSign.toInt()
            val min = ((degInSign - deg) * 60).toInt()
            return "${deg}° ${min}'"
        }
    }

    /**
     * Gochara (transit) result for a single planet
     */
    data class GocharaResult(
        val planet: Planet,
        val transitSign: ZodiacSign,
        val houseFromMoon: Int,
        val effect: TransitEffect,
        val isVedhaAffected: Boolean,
        val vedhaSource: Planet?,
        val interpretation: String
    )

    /**
     * Transit aspect to natal planet
     */
    data class TransitAspect(
        val transitingPlanet: Planet,
        val natalPlanet: Planet,
        val aspectType: String,
        val exactAngle: Double,
        val orb: Double,
        val isApplying: Boolean,
        val strength: Double,
        val interpretation: String
    )

    /**
     * Transit effect enumeration
     */
    enum class TransitEffect(val displayName: String, val score: Int) {
        EXCELLENT("Excellent", 5),
        GOOD("Good", 4),
        NEUTRAL("Neutral", 3),
        CHALLENGING("Challenging", 2),
        DIFFICULT("Difficult", 1)
    }

    /**
     * Overall transit assessment
     */
    data class OverallTransitAssessment(
        val quality: TransitQuality,
        val score: Double,
        val summary: String,
        val focusAreas: List<String>
    )

    /**
     * Transit quality
     */
    enum class TransitQuality(val displayName: String) {
        EXCELLENT("Excellent Period"),
        GOOD("Good Period"),
        MIXED("Mixed Period"),
        CHALLENGING("Challenging Period"),
        DIFFICULT("Difficult Period")
    }

    /**
     * Significant period in transit timeline
     */
    data class SignificantPeriod(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val description: String,
        val planets: List<Planet>,
        val intensity: Int // 1-5
    )

    /**
     * Calculate current transit positions
     */
    fun getCurrentTransitPositions(
        timezone: String = "UTC"
    ): List<PlanetPosition> {
        val now = LocalDateTime.now(ZoneId.of(timezone))
        return getTransitPositionsForDateTime(now, timezone)
    }

    /**
     * Get transit positions for a specific date/time
     */
    fun getTransitPositionsForDateTime(
        dateTime: LocalDateTime,
        timezone: String = "UTC"
    ): List<PlanetPosition> {
        val transitBirthData = BirthData(
            name = "Transit",
            dateTime = dateTime,
            latitude = 0.0,
            longitude = 0.0,
            timezone = timezone,
            location = "Transit Chart"
        )

        val transitChart = ephemerisEngine.calculateVedicChart(transitBirthData, HouseSystem.DEFAULT)
        return transitChart.planetPositions
    }

    /**
     * Perform complete transit analysis
     */
    fun analyzeTransits(
        natalChart: VedicChart,
        transitDateTime: LocalDateTime = LocalDateTime.now()
    ): TransitAnalysis {
        // Get transit positions
        val transitPositions = getTransitPositionsForDateTime(
            transitDateTime,
            natalChart.birthData.timezone
        )

        // Get natal Moon position for Gochara
        val natalMoon = natalChart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalStateException("Natal Moon position not found")

        // Calculate Gochara results
        val gocharaResults = calculateGochara(natalMoon, transitPositions)

        // Calculate transit aspects
        val transitAspects = calculateTransitAspects(natalChart, transitPositions)

        // Calculate Ashtakavarga scores
        val ashtakavargaAnalysis = AshtakavargaCalculator.calculateAshtakavarga(natalChart)
        val ashtakavargaScores = mutableMapOf<Planet, AshtakavargaCalculator.TransitScore>()

        transitPositions.filter { it.planet in Planet.MAIN_PLANETS && it.planet != Planet.RAHU && it.planet != Planet.KETU }
            .forEach { transitPos ->
                ashtakavargaScores[transitPos.planet] = ashtakavargaAnalysis.getTransitScore(
                    transitPos.planet,
                    transitPos.sign
                )
            }

        // Calculate overall assessment
        val overallAssessment = calculateOverallAssessment(gocharaResults, transitAspects, ashtakavargaScores)

        // Find significant periods
        val significantPeriods = findSignificantPeriods(natalChart, transitDateTime)

        return TransitAnalysis(
            natalChart = natalChart,
            transitDateTime = transitDateTime,
            transitPositions = transitPositions,
            gocharaResults = gocharaResults,
            transitAspects = transitAspects,
            ashtakavargaScores = ashtakavargaScores,
            overallAssessment = overallAssessment,
            significantPeriods = significantPeriods
        )
    }

    /**
     * Calculate Gochara (transit from Moon) for all planets
     */
    private fun calculateGochara(
        natalMoon: PlanetPosition,
        transitPositions: List<PlanetPosition>
    ): List<GocharaResult> {
        val results = mutableListOf<GocharaResult>()
        val natalMoonSign = natalMoon.sign

        transitPositions.forEach { transitPos ->
            val planet = transitPos.planet
            if (planet !in Planet.MAIN_PLANETS) return@forEach

            // Calculate house from Moon
            val houseFromMoon = calculateHouseFromSign(transitPos.sign, natalMoonSign)

            // Determine effect based on classical rules
            val baseEffect = when (planet) {
                in FAVORABLE_TRANSITS.keys -> {
                    when (houseFromMoon) {
                        in (FAVORABLE_TRANSITS[planet] ?: emptyList()) -> TransitEffect.GOOD
                        in (NEUTRAL_TRANSITS[planet] ?: emptyList()) -> TransitEffect.NEUTRAL
                        else -> TransitEffect.CHALLENGING
                    }
                }
                else -> TransitEffect.NEUTRAL
            }

            // Check for Vedha
            val (isVedhaAffected, vedhaSource) = checkVedha(planet, houseFromMoon, transitPositions, natalMoonSign)

            // Adjust effect if Vedha is present
            val finalEffect = if (isVedhaAffected && baseEffect == TransitEffect.GOOD) {
                TransitEffect.NEUTRAL
            } else {
                baseEffect
            }

            val interpretation = generateGocharaInterpretation(planet, houseFromMoon, finalEffect, isVedhaAffected)

            results.add(
                GocharaResult(
                    planet = planet,
                    transitSign = transitPos.sign,
                    houseFromMoon = houseFromMoon,
                    effect = finalEffect,
                    isVedhaAffected = isVedhaAffected,
                    vedhaSource = vedhaSource,
                    interpretation = interpretation
                )
            )
        }

        return results
    }

    /**
     * Check if a transit has Vedha (obstruction)
     */
    private fun checkVedha(
        planet: Planet,
        houseFromMoon: Int,
        transitPositions: List<PlanetPosition>,
        natalMoonSign: ZodiacSign
    ): Pair<Boolean, Planet?> {
        val vedhaMap = when (planet) {
            Planet.SUN -> SUN_VEDHA
            Planet.MOON -> MOON_VEDHA
            Planet.MARS -> MARS_VEDHA
            Planet.MERCURY -> MERCURY_VEDHA
            Planet.JUPITER -> JUPITER_VEDHA
            Planet.VENUS -> VENUS_VEDHA
            Planet.SATURN -> SATURN_VEDHA
            else -> emptyMap()
        }

        val vedhaHouse = vedhaMap[houseFromMoon] ?: return Pair(false, null)

        // Check if any planet is in the Vedha house
        transitPositions.forEach { otherTransit ->
            if (otherTransit.planet != planet && otherTransit.planet in Planet.MAIN_PLANETS) {
                val otherHouseFromMoon = calculateHouseFromSign(otherTransit.sign, natalMoonSign)
                if (otherHouseFromMoon == vedhaHouse) {
                    return Pair(true, otherTransit.planet)
                }
            }
        }

        return Pair(false, null)
    }

    /**
     * Calculate transit aspects to natal planets
     */
    private fun calculateTransitAspects(
        natalChart: VedicChart,
        transitPositions: List<PlanetPosition>
    ): List<TransitAspect> {
        val aspects = mutableListOf<TransitAspect>()

        transitPositions.forEach { transitPos ->
            natalChart.planetPositions.forEach { natalPos ->
                // Calculate angular separation
                val angularSeparation = calculateAngularSeparation(
                    transitPos.longitude,
                    natalPos.longitude
                )

                // Check each aspect angle
                ASPECT_ANGLES.forEach { (aspectAngle, aspectName) ->
                    val orb = calculateOrb(angularSeparation, aspectAngle)
                    val maxOrb = TRANSIT_ORBS[transitPos.planet] ?: 6.0

                    if (orb <= maxOrb) {
                        val strength = 1.0 - (orb / maxOrb)
                        val isApplying = isAspectApplying(transitPos, natalPos, aspectAngle)

                        val interpretation = generateAspectInterpretation(
                            transitPos.planet,
                            natalPos.planet,
                            aspectName,
                            isApplying
                        )

                        aspects.add(
                            TransitAspect(
                                transitingPlanet = transitPos.planet,
                                natalPlanet = natalPos.planet,
                                aspectType = aspectName,
                                exactAngle = angularSeparation,
                                orb = orb,
                                isApplying = isApplying,
                                strength = strength,
                                interpretation = interpretation
                            )
                        )
                    }
                }
            }
        }

        return aspects.sortedByDescending { it.strength }
    }

    /**
     * Calculate overall transit assessment
     */
    private fun calculateOverallAssessment(
        gocharaResults: List<GocharaResult>,
        transitAspects: List<TransitAspect>,
        ashtakavargaScores: Map<Planet, AshtakavargaCalculator.TransitScore>
    ): OverallTransitAssessment {
        // Score from Gochara
        val gocharaScore = gocharaResults.map { it.effect.score }.average() * 20

        // Score from strong aspects
        val aspectScore = if (transitAspects.isNotEmpty()) {
            val beneficAspects = transitAspects.count { aspect ->
                (aspect.aspectType in listOf("Trine", "Sextile") ||
                        (aspect.aspectType == "Conjunction" && aspect.transitingPlanet in listOf(Planet.JUPITER, Planet.VENUS)))
            }
            val maleficAspects = transitAspects.count { aspect ->
                aspect.aspectType in listOf("Square", "Opposition") &&
                        aspect.transitingPlanet in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)
            }
            ((beneficAspects * 10) - (maleficAspects * 5) + 50).coerceIn(0, 100).toDouble()
        } else 50.0

        // Score from Ashtakavarga
        val ashtakavargaScore = if (ashtakavargaScores.isNotEmpty()) {
            ashtakavargaScores.values.map { it.overallRating }.average() * 100
        } else 50.0

        // Combined score
        val combinedScore = (gocharaScore * 0.4 + aspectScore * 0.3 + ashtakavargaScore * 0.3)

        val quality = when {
            combinedScore >= 75 -> TransitQuality.EXCELLENT
            combinedScore >= 60 -> TransitQuality.GOOD
            combinedScore >= 45 -> TransitQuality.MIXED
            combinedScore >= 30 -> TransitQuality.CHALLENGING
            else -> TransitQuality.DIFFICULT
        }

        val summary = generateOverallSummary(quality, gocharaResults, transitAspects)
        val focusAreas = generateFocusAreas(gocharaResults, transitAspects)

        return OverallTransitAssessment(
            quality = quality,
            score = combinedScore,
            summary = summary,
            focusAreas = focusAreas
        )
    }

    /**
     * Find significant transit periods in the next 30 days
     */
    private fun findSignificantPeriods(
        natalChart: VedicChart,
        startDate: LocalDateTime
    ): List<SignificantPeriod> {
        val periods = mutableListOf<SignificantPeriod>()

        // Check for significant Saturn, Jupiter, and Rahu/Ketu transits
        val significantPlanets = listOf(Planet.SATURN, Planet.JUPITER, Planet.RAHU, Planet.KETU)

        val natalMoon = natalChart.planetPositions.find { it.planet == Planet.MOON } ?: return periods

        // Analyze transit for next 30 days (sample every 7 days)
        for (dayOffset in listOf(0, 7, 14, 21, 28)) {
            val checkDate = startDate.plusDays(dayOffset.toLong())
            val transitPositions = getTransitPositionsForDateTime(checkDate, natalChart.birthData.timezone)

            significantPlanets.forEach { planet ->
                val transitPos = transitPositions.find { it.planet == planet } ?: return@forEach
                val houseFromMoon = calculateHouseFromSign(transitPos.sign, natalMoon.sign)

                // Check for significant house transits
                val isSignificant = when (planet) {
                    Planet.SATURN -> houseFromMoon in listOf(1, 4, 7, 8, 10, 12) // Sade Sati, Ashtama Shani, etc.
                    Planet.JUPITER -> houseFromMoon in listOf(1, 5, 9) // Guru in Kendra/Trikona
                    Planet.RAHU, Planet.KETU -> houseFromMoon in listOf(1, 7) // Node on axis
                    else -> false
                }

                if (isSignificant) {
                    val description = generatePeriodDescription(planet, houseFromMoon)
                    val intensity = when {
                        planet == Planet.SATURN && houseFromMoon == 8 -> 5 // Ashtama Shani
                        planet == Planet.SATURN && houseFromMoon in listOf(1, 12) -> 4 // Sade Sati peak/end
                        planet == Planet.JUPITER && houseFromMoon in listOf(1, 5, 9) -> 4
                        else -> 3
                    }

                    // Find period duration (simplified - actual implementation would track sign changes)
                    val endDate = checkDate.plusDays(7)

                    periods.add(
                        SignificantPeriod(
                            startDate = checkDate,
                            endDate = endDate,
                            description = description,
                            planets = listOf(planet),
                            intensity = intensity
                        )
                    )
                }
            }
        }

        return periods.distinctBy { it.description }
    }

    /**
     * Calculate house from one sign to another
     */
    private fun calculateHouseFromSign(targetSign: ZodiacSign, referenceSign: ZodiacSign): Int {
        val diff = targetSign.number - referenceSign.number
        return if (diff >= 0) diff + 1 else diff + 13
    }

    /**
     * Calculate angular separation
     */
    private fun calculateAngularSeparation(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    /**
     * Calculate orb from exact aspect
     */
    private fun calculateOrb(actualAngle: Double, aspectAngle: Double): Double {
        val diff = abs(actualAngle - aspectAngle)
        return minOf(diff, 360.0 - diff)
    }

    /**
     * Determine if aspect is applying
     */
    private fun isAspectApplying(
        transitPos: PlanetPosition,
        natalPos: PlanetPosition,
        aspectAngle: Double
    ): Boolean {
        // If transit planet is moving faster than natal (always true for transits to natal),
        // check if the orb is decreasing
        val currentOrb = calculateOrb(
            calculateAngularSeparation(transitPos.longitude, natalPos.longitude),
            aspectAngle
        )

        // Estimate future position based on speed
        val futureLong = transitPos.longitude + transitPos.speed
        val futureOrb = calculateOrb(
            calculateAngularSeparation(futureLong, natalPos.longitude),
            aspectAngle
        )

        return futureOrb < currentOrb
    }

    /**
     * Generate Gochara interpretation
     */
    private fun generateGocharaInterpretation(
        planet: Planet,
        houseFromMoon: Int,
        effect: TransitEffect,
        isVedhaAffected: Boolean
    ): String {
        val houseMatters = when (houseFromMoon) {
            1 -> "self, health, personality"
            2 -> "wealth, family, speech"
            3 -> "courage, siblings, short journeys"
            4 -> "home, mother, mental peace"
            5 -> "children, creativity, romance"
            6 -> "enemies, health issues, debts"
            7 -> "marriage, partnerships, business"
            8 -> "obstacles, longevity, occult"
            9 -> "fortune, father, religion"
            10 -> "career, status, government"
            11 -> "gains, friends, elder siblings"
            12 -> "expenses, spirituality, foreign"
            else -> "general matters"
        }

        val vedhaNote = if (isVedhaAffected) " Effects may be diminished due to Vedha." else ""

        return when (effect) {
            TransitEffect.EXCELLENT -> "${planet.displayName} transit in ${houseFromMoon}th house brings excellent results for $houseMatters.$vedhaNote"
            TransitEffect.GOOD -> "${planet.displayName} transit in ${houseFromMoon}th house supports $houseMatters.$vedhaNote"
            TransitEffect.NEUTRAL -> "${planet.displayName} transit in ${houseFromMoon}th house has neutral effects on $houseMatters.$vedhaNote"
            TransitEffect.CHALLENGING -> "${planet.displayName} transit in ${houseFromMoon}th house may challenge $houseMatters.$vedhaNote"
            TransitEffect.DIFFICULT -> "${planet.displayName} transit in ${houseFromMoon}th house requires caution in $houseMatters.$vedhaNote"
        }
    }

    /**
     * Generate aspect interpretation
     */
    private fun generateAspectInterpretation(
        transitingPlanet: Planet,
        natalPlanet: Planet,
        aspectType: String,
        isApplying: Boolean
    ): String {
        val applyingStr = if (isApplying) "becoming exact" else "separating"
        val beneficTransit = transitingPlanet in listOf(Planet.JUPITER, Planet.VENUS)
        val harmonicAspect = aspectType in listOf("Trine", "Sextile")

        return when {
            beneficTransit && harmonicAspect -> "Favorable: Transit ${transitingPlanet.displayName} $aspectType natal ${natalPlanet.displayName} ($applyingStr) - beneficial influence"
            beneficTransit -> "Transit ${transitingPlanet.displayName} $aspectType natal ${natalPlanet.displayName} ($applyingStr) - mixed but generally supportive"
            harmonicAspect -> "Transit ${transitingPlanet.displayName} $aspectType natal ${natalPlanet.displayName} ($applyingStr) - harmonious connection"
            else -> "Transit ${transitingPlanet.displayName} $aspectType natal ${natalPlanet.displayName} ($applyingStr) - requires attention"
        }
    }

    /**
     * Generate overall summary
     */
    private fun generateOverallSummary(
        quality: TransitQuality,
        gocharaResults: List<GocharaResult>,
        transitAspects: List<TransitAspect>
    ): String {
        val favorablePlanets = gocharaResults.filter { it.effect in listOf(TransitEffect.EXCELLENT, TransitEffect.GOOD) }
            .map { it.planet.displayName }
        val challengingPlanets = gocharaResults.filter { it.effect in listOf(TransitEffect.CHALLENGING, TransitEffect.DIFFICULT) }
            .map { it.planet.displayName }

        return when (quality) {
            TransitQuality.EXCELLENT -> "This is an excellent transit period. ${favorablePlanets.joinToString(", ")} are well-placed from Moon, supporting growth and positive developments."
            TransitQuality.GOOD -> "Overall favorable transit period. ${favorablePlanets.joinToString(", ")} provide support. Good time for important initiatives."
            TransitQuality.MIXED -> "Mixed transit influences present. Balance ${favorablePlanets.joinToString(", ")} positives against ${challengingPlanets.joinToString(", ")} challenges."
            TransitQuality.CHALLENGING -> "Challenging period requiring patience. ${challengingPlanets.joinToString(", ")} may create obstacles. Focus on steady progress."
            TransitQuality.DIFFICULT -> "Difficult transit period. ${challengingPlanets.joinToString(", ")} create significant challenges. Exercise caution and avoid major decisions."
        }
    }

    /**
     * Generate focus areas based on transit analysis
     */
    private fun generateFocusAreas(
        gocharaResults: List<GocharaResult>,
        transitAspects: List<TransitAspect>
    ): List<String> {
        val areas = mutableListOf<String>()

        // Check Saturn transit
        gocharaResults.find { it.planet == Planet.SATURN }?.let { saturnResult ->
            when (saturnResult.houseFromMoon) {
                1, 12 -> areas.add("Sade Sati period - focus on patience, health, and spiritual growth")
                8 -> areas.add("Ashtama Shani - be cautious about health, unexpected challenges")
                4 -> areas.add("Saturn transiting 4th - attention to home, mother, mental peace")
                10 -> areas.add("Saturn transiting 10th - career responsibilities, hard work pays off")
                else -> { /* No specific focus area for other houses */ }
            }
        }

        // Check Jupiter transit
        gocharaResults.find { it.planet == Planet.JUPITER }?.let { jupiterResult ->
            when (jupiterResult.houseFromMoon) {
                1, 5, 9 -> areas.add("Jupiter in trine houses - excellent for expansion, learning, spirituality")
                2 -> areas.add("Jupiter transiting 2nd - favorable for wealth accumulation")
                11 -> areas.add("Jupiter transiting 11th - gains through networking, fulfillment of desires")
                else -> { /* No specific focus area for other houses */ }
            }
        }

        // Check strong aspects
        transitAspects.filter { it.strength > 0.8 }.take(3).forEach { aspect ->
            areas.add("Strong ${aspect.aspectType} from transit ${aspect.transitingPlanet.displayName} to natal ${aspect.natalPlanet.displayName}")
        }

        return areas.take(5)
    }

    /**
     * Generate period description
     */
    private fun generatePeriodDescription(planet: Planet, houseFromMoon: Int): String {
        return when (planet) {
            Planet.SATURN -> when (houseFromMoon) {
                12 -> "Sade Sati beginning phase (Saturn in 12th from Moon)"
                1 -> "Sade Sati peak phase (Saturn over natal Moon)"
                2 -> "Sade Sati ending phase (Saturn in 2nd from Moon)"
                8 -> "Ashtama Shani (Saturn in 8th from Moon)"
                4 -> "Kantak Shani (Saturn in 4th from Moon)"
                7 -> "Saturn in 7th from Moon - relationship focus"
                10 -> "Saturn in 10th from Moon - career challenges and growth"
                else -> "Saturn transit in ${houseFromMoon}th from Moon"
            }
            Planet.JUPITER -> when (houseFromMoon) {
                1 -> "Jupiter over natal Moon - expansion and growth"
                5 -> "Jupiter in 5th from Moon - creativity and children"
                9 -> "Jupiter in 9th from Moon - fortune and dharma"
                else -> "Jupiter transit in ${houseFromMoon}th from Moon"
            }
            Planet.RAHU -> "Rahu transit in ${houseFromMoon}th from Moon - worldly desires amplified"
            Planet.KETU -> "Ketu transit in ${houseFromMoon}th from Moon - spiritual detachment"
            else -> "${planet.displayName} transit in ${houseFromMoon}th from Moon"
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        ephemerisEngine.close()
    }
}
