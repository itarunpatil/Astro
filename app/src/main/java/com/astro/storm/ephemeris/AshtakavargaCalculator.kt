package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign

/**
 * Complete Ashtakavarga Calculator
 *
 * Ashtakavarga is one of the most important predictive tools in Vedic astrology.
 * It is a system of points (bindus) that measures the strength of planets in different signs.
 *
 * The system consists of:
 * 1. BHINNASHTAKAVARGA - Individual planet's contribution tables
 *    - Each planet (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn) and Ascendant
 *    - contributes points (bindus) based on specific positional rules from BPHS
 *
 * 2. SARVASHTAKAVARGA (SAV) - Combined total of all Bhinnashtakavarga tables
 *    - Shows overall strength of each sign
 *    - Maximum possible: 56 bindus (8 contributors × 7 max each, though actual max varies)
 *    - Signs with 28+ bindus are generally beneficial for transits
 *
 * 3. PRASTARASHTAKAVARGA - Detailed expansion showing individual contributions
 *    - Shows which planet contributes bindus in which sign
 *    - Used for advanced transit predictions
 *
 * 4. KAKSHA (Sub-division) - Each sign divided into 8 parts of 3°45' each
 *    - Ruled by Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon, Ascendant
 *    - Used for pinpointing exact timing within a transit
 *
 * Traditional Bindu Allocation Rules:
 * Based on Brihat Parasara Hora Shastra (BPHS) Chapter 66-72
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
object AshtakavargaCalculator {

    /**
     * Ashtakavarga contributing planets (7 planets + Ascendant)
     * Note: Rahu and Ketu are excluded from traditional Ashtakavarga
     */
    private val ASHTAKAVARGA_PLANETS = listOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    /**
     * Kaksha lords in traditional sequence (8 parts of each sign)
     * Each Kaksha spans 3°45' (3.75°)
     */
    private val KAKSHA_LORDS = listOf(
        Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.SUN,
        Planet.VENUS, Planet.MERCURY, Planet.MOON
    )
    private const val KAKSHA_ASCENDANT = "Ascendant"
    private const val KAKSHA_SIZE_DEGREES = 3.75 // 30° / 8 kakshas = 3.75° per kaksha

    /**
     * Bindu allocation rules from BPHS
     * Format: Map<ContributingPlanet, List<HousePositionsFromContributingPlanet>>
     * These are the houses from the contributing planet where it gives a bindu
     * to the planet being calculated.
     *
     * Example: SUN_BINDU_RULES[Planet.SUN] = listOf(1,2,4,7,8,9,10,11)
     * means Sun gives bindu to itself when in houses 1,2,4,7,8,9,10,11 from Sun's position
     */

    // SUN's Bhinnashtakavarga rules
    // From BPHS: Sun gives bindus when other planets are in specific houses from Sun
    private val SUN_BINDU_RULES = mapOf(
        Planet.SUN to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        Planet.MOON to listOf(3, 6, 10, 11),
        Planet.MARS to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        Planet.MERCURY to listOf(3, 5, 6, 9, 10, 11, 12),
        Planet.JUPITER to listOf(5, 6, 9, 11),
        Planet.VENUS to listOf(6, 7, 12),
        Planet.SATURN to listOf(1, 2, 4, 7, 8, 9, 10, 11)
    )
    private val SUN_ASCENDANT_BINDUS = listOf(3, 4, 6, 10, 11, 12)

    // MOON's Bhinnashtakavarga rules
    private val MOON_BINDU_RULES = mapOf(
        Planet.SUN to listOf(3, 6, 7, 8, 10, 11),
        Planet.MOON to listOf(1, 3, 6, 7, 10, 11),
        Planet.MARS to listOf(2, 3, 5, 6, 9, 10, 11),
        Planet.MERCURY to listOf(1, 3, 4, 5, 7, 8, 10, 11),
        Planet.JUPITER to listOf(1, 4, 7, 8, 10, 11, 12),
        Planet.VENUS to listOf(3, 4, 5, 7, 9, 10, 11),
        Planet.SATURN to listOf(3, 5, 6, 11)
    )
    private val MOON_ASCENDANT_BINDUS = listOf(3, 6, 10, 11)

    // MARS's Bhinnashtakavarga rules
    private val MARS_BINDU_RULES = mapOf(
        Planet.SUN to listOf(3, 5, 6, 10, 11),
        Planet.MOON to listOf(3, 6, 11),
        Planet.MARS to listOf(1, 2, 4, 7, 8, 10, 11),
        Planet.MERCURY to listOf(3, 5, 6, 11),
        Planet.JUPITER to listOf(6, 10, 11, 12),
        Planet.VENUS to listOf(6, 8, 11, 12),
        Planet.SATURN to listOf(1, 4, 7, 8, 9, 10, 11)
    )
    private val MARS_ASCENDANT_BINDUS = listOf(1, 3, 6, 10, 11)

    // MERCURY's Bhinnashtakavarga rules
    private val MERCURY_BINDU_RULES = mapOf(
        Planet.SUN to listOf(5, 6, 9, 11, 12),
        Planet.MOON to listOf(2, 4, 6, 8, 10, 11),
        Planet.MARS to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        Planet.MERCURY to listOf(1, 3, 5, 6, 9, 10, 11, 12),
        Planet.JUPITER to listOf(6, 8, 11, 12),
        Planet.VENUS to listOf(1, 2, 3, 4, 5, 8, 9, 11),
        Planet.SATURN to listOf(1, 2, 4, 7, 8, 9, 10, 11)
    )
    private val MERCURY_ASCENDANT_BINDUS = listOf(1, 2, 4, 6, 8, 10, 11)

    // JUPITER's Bhinnashtakavarga rules
    private val JUPITER_BINDU_RULES = mapOf(
        Planet.SUN to listOf(1, 2, 3, 4, 7, 8, 9, 10, 11),
        Planet.MOON to listOf(2, 5, 7, 9, 11),
        Planet.MARS to listOf(1, 2, 4, 7, 8, 10, 11),
        Planet.MERCURY to listOf(1, 2, 4, 5, 6, 9, 10, 11),
        Planet.JUPITER to listOf(1, 2, 3, 4, 7, 8, 10, 11),
        Planet.VENUS to listOf(2, 5, 6, 9, 10, 11),
        Planet.SATURN to listOf(3, 5, 6, 12)
    )
    private val JUPITER_ASCENDANT_BINDUS = listOf(1, 2, 4, 5, 6, 7, 9, 10, 11)

    // VENUS's Bhinnashtakavarga rules
    private val VENUS_BINDU_RULES = mapOf(
        Planet.SUN to listOf(8, 11, 12),
        Planet.MOON to listOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
        Planet.MARS to listOf(3, 5, 6, 9, 11, 12),
        Planet.MERCURY to listOf(3, 5, 6, 9, 11),
        Planet.JUPITER to listOf(5, 8, 9, 10, 11),
        Planet.VENUS to listOf(1, 2, 3, 4, 5, 8, 9, 10, 11),
        Planet.SATURN to listOf(3, 4, 5, 8, 9, 10, 11)
    )
    private val VENUS_ASCENDANT_BINDUS = listOf(1, 2, 3, 4, 5, 8, 9, 11)

    // SATURN's Bhinnashtakavarga rules
    private val SATURN_BINDU_RULES = mapOf(
        Planet.SUN to listOf(1, 2, 4, 7, 8, 10, 11),
        Planet.MOON to listOf(3, 6, 11),
        Planet.MARS to listOf(3, 5, 6, 10, 11, 12),
        Planet.MERCURY to listOf(6, 8, 9, 10, 11, 12),
        Planet.JUPITER to listOf(5, 6, 11, 12),
        Planet.VENUS to listOf(6, 11, 12),
        Planet.SATURN to listOf(3, 5, 6, 11)
    )
    private val SATURN_ASCENDANT_BINDUS = listOf(1, 3, 4, 6, 10, 11)

    /**
     * Complete Ashtakavarga analysis result
     */
    data class AshtakavargaAnalysis(
        val chart: VedicChart,
        val bhinnashtakavarga: Map<Planet, Bhinnashtakavarga>,
        val sarvashtakavarga: Sarvashtakavarga,
        val prastarashtakavarga: Map<Planet, Prastarashtakavarga>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        /**
         * Get transit prediction score for a planet transiting a sign
         */
        fun getTransitScore(planet: Planet, sign: ZodiacSign): TransitScore {
            val bav = bhinnashtakavarga[planet] ?: return TransitScore(
                planet = planet,
                sign = sign,
                binduScore = 0,
                savScore = sarvashtakavarga.getBindusForSign(sign),
                interpretation = "Unknown"
            )

            val binduScore = bav.getBindusForSign(sign)
            val savScore = sarvashtakavarga.getBindusForSign(sign)

            val interpretation = when {
                binduScore >= 5 && savScore >= 30 -> "Excellent - Highly favorable transit"
                binduScore >= 4 && savScore >= 28 -> "Good - Favorable results expected"
                binduScore >= 3 && savScore >= 25 -> "Average - Mixed results"
                binduScore >= 2 && savScore >= 22 -> "Below Average - Some challenges"
                else -> "Difficult - Careful navigation needed"
            }

            return TransitScore(
                planet = planet,
                sign = sign,
                binduScore = binduScore,
                savScore = savScore,
                interpretation = interpretation
            )
        }

        /**
         * Get localized transit prediction score for a planet transiting a sign
         */
        fun getLocalizedTransitScore(planet: Planet, sign: ZodiacSign, language: Language): TransitScore {
            val bav = bhinnashtakavarga[planet] ?: return TransitScore(
                planet = planet,
                sign = sign,
                binduScore = 0,
                savScore = sarvashtakavarga.getBindusForSign(sign),
                interpretation = StringResources.get(StringKey.LABEL_UNKNOWN, language)
            )

            val binduScore = bav.getBindusForSign(sign)
            val savScore = sarvashtakavarga.getBindusForSign(sign)

            val interpretation = when {
                binduScore >= 5 && savScore >= 30 -> StringResources.get(StringKey.TRANSIT_INTERP_EXCELLENT, language)
                binduScore >= 4 && savScore >= 28 -> StringResources.get(StringKey.TRANSIT_INTERP_GOOD, language)
                binduScore >= 3 && savScore >= 25 -> StringResources.get(StringKey.TRANSIT_INTERP_AVERAGE, language)
                binduScore >= 2 && savScore >= 22 -> StringResources.get(StringKey.TRANSIT_INTERP_BELOW_AVG, language)
                else -> StringResources.get(StringKey.TRANSIT_INTERP_DIFFICULT, language)
            }

            return TransitScore(
                planet = planet,
                sign = sign,
                binduScore = binduScore,
                savScore = savScore,
                interpretation = interpretation
            )
        }

        /**
         * Get plain text summary
         */
        fun toPlainText(): String = buildString {
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("                ASHTAKAVARGA ANALYSIS")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine("SARVASHTAKAVARGA (Combined Strength)")
            appendLine("─────────────────────────────────────────────────────────")
            ZodiacSign.entries.forEach { sign ->
                val bindus = sarvashtakavarga.getBindusForSign(sign)
                val bar = "█".repeat((bindus * 2).coerceAtMost(40))
                val strength = when {
                    bindus >= 30 -> "Strong"
                    bindus >= 25 -> "Good"
                    bindus >= 20 -> "Average"
                    else -> "Weak"
                }
                appendLine("${sign.displayName.padEnd(12)}: ${bindus.toString().padStart(2)} bindus $bar [$strength]")
            }
            appendLine()
            appendLine("Total SAV Bindus: ${sarvashtakavarga.totalBindus}")
            appendLine("Average per Sign: ${String.format("%.1f", sarvashtakavarga.totalBindus / 12.0)}")
            appendLine()
            appendLine("BHINNASHTAKAVARGA (Individual Planet Strengths)")
            appendLine("─────────────────────────────────────────────────────────")
            bhinnashtakavarga.forEach { (planet, bav) ->
                appendLine()
                appendLine("${planet.displayName.uppercase()} Ashtakavarga:")
                ZodiacSign.entries.forEach { sign ->
                    val bindus = bav.getBindusForSign(sign)
                    val bar = "▓".repeat(bindus)
                    appendLine("  ${sign.abbreviation}: ${bindus.toString().padStart(1)} $bar")
                }
                appendLine("  Total: ${bav.totalBindus} bindus")
            }
        }
    }

    /**
     * Bhinnashtakavarga for a single planet
     */
    data class Bhinnashtakavarga(
        val planet: Planet,
        val binduMatrix: Map<ZodiacSign, Int>,
        val contributorMatrix: Map<ZodiacSign, List<String>>,
        val totalBindus: Int
    ) {
        fun getBindusForSign(sign: ZodiacSign): Int = binduMatrix[sign] ?: 0

        fun getContributorsForSign(sign: ZodiacSign): List<String> =
            contributorMatrix[sign] ?: emptyList()
    }

    /**
     * Sarvashtakavarga (combined)
     */
    data class Sarvashtakavarga(
        val binduMatrix: Map<ZodiacSign, Int>,
        val totalBindus: Int,
        val strongestSign: ZodiacSign,
        val weakestSign: ZodiacSign
    ) {
        fun getBindusForSign(sign: ZodiacSign): Int = binduMatrix[sign] ?: 0

        /**
         * Check if a sign is favorable for transits (28+ bindus traditionally)
         */
        fun isFavorableForTransit(sign: ZodiacSign): Boolean =
            getBindusForSign(sign) >= 28
    }

    /**
     * Prastarashtakavarga - detailed expansion table
     */
    data class Prastarashtakavarga(
        val planet: Planet,
        val contributionMatrix: Map<ZodiacSign, Map<String, Boolean>>,
        val bindusByContributor: Map<String, Int>
    ) {
        /**
         * Check if a specific contributor gives bindu in a sign
         */
        fun doesContributorGiveBindu(sign: ZodiacSign, contributor: String): Boolean =
            contributionMatrix[sign]?.get(contributor) ?: false
    }

    /**
     * Transit score result
     */
    data class TransitScore(
        val planet: Planet,
        val sign: ZodiacSign,
        val binduScore: Int,
        val savScore: Int,
        val interpretation: String
    ) {
        val overallRating: Double
            get() = (binduScore / 8.0 * 0.6) + (savScore / 56.0 * 0.4)
    }

    /**
     * Kaksha (sub-division) result
     */
    data class KakshaPosition(
        val sign: ZodiacSign,
        val kakshaNumber: Int,      // 1-8
        val kakshaLord: String,      // Planet name or "Ascendant"
        val degreeStart: Double,
        val degreeEnd: Double,
        val isBeneficial: Boolean
    )

    /**
     * Calculate complete Ashtakavarga for a chart
     */
    fun calculateAshtakavarga(chart: VedicChart): AshtakavargaAnalysis {
        val bhinnashtakavarga = mutableMapOf<Planet, Bhinnashtakavarga>()
        val prastarashtakavarga = mutableMapOf<Planet, Prastarashtakavarga>()

        // Calculate Bhinnashtakavarga for each planet
        ASHTAKAVARGA_PLANETS.forEach { planet ->
            val (bav, prastara) = calculateBhinnashtakavarga(planet, chart)
            bhinnashtakavarga[planet] = bav
            prastarashtakavarga[planet] = prastara
        }

        // Calculate Sarvashtakavarga
        val sarvashtakavarga = calculateSarvashtakavarga(bhinnashtakavarga)

        return AshtakavargaAnalysis(
            chart = chart,
            bhinnashtakavarga = bhinnashtakavarga,
            sarvashtakavarga = sarvashtakavarga,
            prastarashtakavarga = prastarashtakavarga
        )
    }

    /**
     * Calculate Bhinnashtakavarga for a specific planet
     */
    private fun calculateBhinnashtakavarga(
        planet: Planet,
        chart: VedicChart
    ): Pair<Bhinnashtakavarga, Prastarashtakavarga> {
        val binduMatrix = mutableMapOf<ZodiacSign, Int>()
        val contributorMatrix = mutableMapOf<ZodiacSign, MutableList<String>>()
        val prastaraMatrix = mutableMapOf<ZodiacSign, MutableMap<String, Boolean>>()

        // Get bindu rules for this planet
        val binduRules = getBinduRulesForPlanet(planet)
        val ascendantBindus = getAscendantBindusForPlanet(planet)

        // Initialize matrices
        ZodiacSign.entries.forEach { sign ->
            binduMatrix[sign] = 0
            contributorMatrix[sign] = mutableListOf()
            prastaraMatrix[sign] = mutableMapOf()
        }

        // Calculate bindus from each contributing planet
        ASHTAKAVARGA_PLANETS.forEach { contributor ->
            val contributorPosition = chart.planetPositions.find { it.planet == contributor }
                ?: return@forEach

            val contributorSignNumber = contributorPosition.sign.number
            val bindusFromThisPlanet = binduRules[contributor] ?: emptyList()

            // For each house position that gives a bindu
            bindusFromThisPlanet.forEach { houseFromContributor ->
                // Calculate which sign this house falls in (0-indexed for array access, then find by 1-based number)
                val targetSignIndex = ((contributorSignNumber - 1 + houseFromContributor - 1) % 12)
                val targetSign = ZodiacSign.entries.getOrNull(targetSignIndex) ?: return@forEach

                binduMatrix[targetSign] = (binduMatrix[targetSign] ?: 0) + 1
                contributorMatrix[targetSign]?.add(contributor.displayName)
                prastaraMatrix[targetSign]?.set(contributor.displayName, true)
            }

            // Mark non-contributing positions
            ZodiacSign.entries.forEach { sign ->
                val prastaraSignMap = prastaraMatrix[sign]
                if (prastaraSignMap?.get(contributor.displayName) != true) {
                    prastaraSignMap?.set(contributor.displayName, false)
                }
            }
        }

        // Calculate bindus from Ascendant
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        val ascendantSignNumber = ascendantSign.number

        ascendantBindus.forEach { houseFromAscendant ->
            val targetSignIndex = ((ascendantSignNumber - 1 + houseFromAscendant - 1) % 12)
            val targetSign = ZodiacSign.entries.getOrNull(targetSignIndex) ?: return@forEach

            binduMatrix[targetSign] = (binduMatrix[targetSign] ?: 0) + 1
            contributorMatrix[targetSign]?.add("Ascendant")
            prastaraMatrix[targetSign]?.set("Ascendant", true)
        }

        // Mark non-contributing Ascendant positions
        ZodiacSign.entries.forEach { sign ->
            val prastaraSignMap = prastaraMatrix[sign]
            if (prastaraSignMap?.get("Ascendant") != true) {
                prastaraSignMap?.set("Ascendant", false)
            }
        }

        val totalBindus = binduMatrix.values.sum()

        val bav = Bhinnashtakavarga(
            planet = planet,
            binduMatrix = binduMatrix,
            contributorMatrix = contributorMatrix.mapValues { it.value.toList() },
            totalBindus = totalBindus
        )

        // Calculate bindu counts by contributor
        val bindusByContributor = mutableMapOf<String, Int>()
        ASHTAKAVARGA_PLANETS.forEach { contributor ->
            bindusByContributor[contributor.displayName] = prastaraMatrix.values
                .count { it[contributor.displayName] == true }
        }
        bindusByContributor["Ascendant"] = prastaraMatrix.values
            .count { it["Ascendant"] == true }

        val prastara = Prastarashtakavarga(
            planet = planet,
            contributionMatrix = prastaraMatrix.mapValues { it.value.toMap() },
            bindusByContributor = bindusByContributor
        )

        return Pair(bav, prastara)
    }

    /**
     * Calculate Sarvashtakavarga from all Bhinnashtakavarga tables
     */
    private fun calculateSarvashtakavarga(
        bhinnashtakavarga: Map<Planet, Bhinnashtakavarga>
    ): Sarvashtakavarga {
        val combinedBindus = mutableMapOf<ZodiacSign, Int>()

        ZodiacSign.entries.forEach { sign ->
            combinedBindus[sign] = 0
        }

        bhinnashtakavarga.values.forEach { bav ->
            ZodiacSign.entries.forEach { sign ->
                combinedBindus[sign] = (combinedBindus[sign] ?: 0) + bav.getBindusForSign(sign)
            }
        }

        val totalBindus = combinedBindus.values.sum()
        val strongestSign = combinedBindus.maxByOrNull { it.value }?.key ?: ZodiacSign.ARIES
        val weakestSign = combinedBindus.minByOrNull { it.value }?.key ?: ZodiacSign.ARIES

        return Sarvashtakavarga(
            binduMatrix = combinedBindus,
            totalBindus = totalBindus,
            strongestSign = strongestSign,
            weakestSign = weakestSign
        )
    }

    /**
     * Calculate Kaksha position for a given longitude
     * @param longitude The longitude to calculate kaksha position for
     * @param chart The VedicChart (used only if preCalculatedAnalysis is null)
     * @param preCalculatedAnalysis Optional pre-calculated analysis to avoid redundant computation
     */
    fun calculateKakshaPosition(
        longitude: Double,
        chart: VedicChart,
        preCalculatedAnalysis: AshtakavargaAnalysis? = null
    ): KakshaPosition {
        val normalizedLong = VedicAstrologyUtils.normalizeLongitude(longitude)
        val sign = ZodiacSign.fromLongitude(normalizedLong)
        val degreeInSign = normalizedLong % 30.0

        // Each Kaksha is exactly 3.75° (30° / 8) - this is a fixed constant in Vedic astrology
        val kakshaNumber = ((degreeInSign / KAKSHA_SIZE_DEGREES).toInt() + 1).coerceIn(1, 8)
        val degreeStart = (kakshaNumber - 1) * KAKSHA_SIZE_DEGREES
        val degreeEnd = kakshaNumber * KAKSHA_SIZE_DEGREES

        val kakshaLord = if (kakshaNumber <= 7) {
            KAKSHA_LORDS[kakshaNumber - 1].displayName
        } else {
            KAKSHA_ASCENDANT
        }

        // Use pre-calculated analysis if available, otherwise calculate (lazy evaluation)
        val analysis = preCalculatedAnalysis ?: calculateAshtakavarga(chart)
        val isBeneficial = if (kakshaNumber <= 7) {
            val lordPlanet = KAKSHA_LORDS[kakshaNumber - 1]
            val bavForLord = analysis.bhinnashtakavarga[lordPlanet]
            bavForLord?.getBindusForSign(sign)?.let { it >= 4 } ?: false
        } else {
            // Ascendant Kaksha - check SAV for the sign
            analysis.sarvashtakavarga.getBindusForSign(sign) >= 28
        }

        return KakshaPosition(
            sign = sign,
            kakshaNumber = kakshaNumber,
            kakshaLord = kakshaLord,
            degreeStart = degreeStart,
            degreeEnd = degreeEnd,
            isBeneficial = isBeneficial
        )
    }

    /**
     * Get detailed transit prediction using Ashtakavarga
     */
    fun getTransitPrediction(
        transitingPlanet: Planet,
        transitSign: ZodiacSign,
        chart: VedicChart
    ): TransitPrediction {
        if (transitingPlanet !in ASHTAKAVARGA_PLANETS) {
            return TransitPrediction(
                planet = transitingPlanet,
                sign = transitSign,
                bavBindus = 0,
                savBindus = 0,
                quality = TransitQuality.UNKNOWN,
                prediction = "Ashtakavarga not applicable for ${transitingPlanet.displayName}"
            )
        }

        val analysis = calculateAshtakavarga(chart)
        val bav = analysis.bhinnashtakavarga[transitingPlanet] ?: return TransitPrediction(
            planet = transitingPlanet,
            sign = transitSign,
            bavBindus = 0,
            savBindus = 0,
            quality = TransitQuality.UNKNOWN,
            prediction = "Bhinnashtakavarga not found for ${transitingPlanet.displayName}"
        )
        val sav = analysis.sarvashtakavarga

        val bavBindus = bav.getBindusForSign(transitSign)
        val savBindus = sav.getBindusForSign(transitSign)

        val quality = when {
            bavBindus >= 5 && savBindus >= 30 -> TransitQuality.EXCELLENT
            bavBindus >= 4 && savBindus >= 28 -> TransitQuality.GOOD
            bavBindus >= 3 && savBindus >= 25 -> TransitQuality.AVERAGE
            bavBindus >= 2 && savBindus >= 22 -> TransitQuality.BELOW_AVERAGE
            bavBindus == 1 && savBindus >= 20 -> TransitQuality.CHALLENGING
            else -> TransitQuality.DIFFICULT
        }

        val prediction = generateTransitPrediction(transitingPlanet, transitSign, bavBindus, savBindus, quality)

        return TransitPrediction(
            planet = transitingPlanet,
            sign = transitSign,
            bavBindus = bavBindus,
            savBindus = savBindus,
            quality = quality,
            prediction = prediction
        )
    }

    /**
     * Calculate Ashtakavarga Shodhana (reduction)
     * This is the process of reducing SAV to find Pinda (final value)
     */
    fun calculateShodhana(analysis: AshtakavargaAnalysis): ShodhanaResult {
        val sav = analysis.sarvashtakavarga
        val reductions = mutableMapOf<ZodiacSign, ShodhanaStep>()

        ZodiacSign.entries.forEach { sign ->
            val originalBindus = sav.getBindusForSign(sign)

            // Trikona Shodhana (reduction from trines)
            val trine5Sign = getSignAtHouse(sign, 5)
            val trine9Sign = getSignAtHouse(sign, 9)

            val trine5Bindus = sav.getBindusForSign(trine5Sign)
            val trine9Bindus = sav.getBindusForSign(trine9Sign)

            val minTrikona = minOf(originalBindus, trine5Bindus, trine9Bindus)
            val afterTrikona = originalBindus - minTrikona

            // Ekadhipatya Shodhana (reduction from same lord signs)
            val signLord = sign.ruler
            val otherSign = ZodiacSign.entries.find { it.ruler == signLord && it != sign }
            val afterEkadhipatya = if (otherSign != null) {
                val otherBindus = sav.getBindusForSign(otherSign)
                val minEkadhipatya = minOf(afterTrikona, otherBindus)
                afterTrikona - minEkadhipatya
            } else {
                afterTrikona
            }

            reductions[sign] = ShodhanaStep(
                originalBindus = originalBindus,
                afterTrikona = afterTrikona,
                afterEkadhipatya = afterEkadhipatya,
                finalPinda = afterEkadhipatya
            )
        }

        return ShodhanaResult(
            reductions = reductions,
            totalPinda = reductions.values.sumOf { it.finalPinda }
        )
    }

    /**
     * Generate transit prediction text
     */
    private fun generateTransitPrediction(
        planet: Planet,
        sign: ZodiacSign,
        bavBindus: Int,
        savBindus: Int,
        quality: TransitQuality
    ): String {
        val planetEffects = when (planet) {
            Planet.SUN -> "authority, father, health, government, career"
            Planet.MOON -> "mind, emotions, mother, public image"
            Planet.MARS -> "energy, siblings, property, courage"
            Planet.MERCURY -> "communication, intellect, business, education"
            Planet.JUPITER -> "wisdom, children, fortune, spirituality"
            Planet.VENUS -> "relationships, luxury, arts, vehicles"
            Planet.SATURN -> "career, longevity, discipline, challenges"
            else -> "general matters"
        }

        val signHouse = sign.number
        val houseMatters = when (signHouse) {
            1 -> "self, personality, health"
            2 -> "wealth, family, speech"
            3 -> "courage, siblings, communication"
            4 -> "home, mother, comfort"
            5 -> "children, intelligence, romance"
            6 -> "enemies, health issues, service"
            7 -> "partnership, marriage, business"
            8 -> "transformation, inheritance, occult"
            9 -> "luck, father, higher learning"
            10 -> "career, status, authority"
            11 -> "gains, friends, aspirations"
            12 -> "losses, spirituality, foreign"
            else -> "general areas"
        }

        return when (quality) {
            TransitQuality.EXCELLENT -> "Transit through ${sign.displayName} with $bavBindus BAV bindus and $savBindus SAV bindus indicates excellent results. " +
                    "Matters related to $planetEffects will flourish. Areas of $houseMatters receive strong positive influence."
            TransitQuality.GOOD -> "Transit through ${sign.displayName} brings favorable results with $bavBindus BAV and $savBindus SAV bindus. " +
                    "Good progress expected in $planetEffects. $houseMatters areas are positively influenced."
            TransitQuality.AVERAGE -> "Transit through ${sign.displayName} ($bavBindus BAV, $savBindus SAV) brings mixed results. " +
                    "Some progress in $planetEffects with occasional challenges. Balance needed in $houseMatters."
            TransitQuality.BELOW_AVERAGE -> "Transit through ${sign.displayName} ($bavBindus BAV, $savBindus SAV) suggests caution needed. " +
                    "$planetEffects matters may face delays. Extra effort required in $houseMatters areas."
            TransitQuality.CHALLENGING -> "Challenging transit through ${sign.displayName} with only $bavBindus BAV and $savBindus SAV bindus. " +
                    "Difficulties possible in $planetEffects. Patience needed for $houseMatters matters."
            TransitQuality.DIFFICULT -> "Difficult transit period through ${sign.displayName} ($bavBindus BAV, $savBindus SAV). " +
                    "Significant challenges in $planetEffects areas. Careful handling of $houseMatters required."
            TransitQuality.UNKNOWN -> "Transit analysis not available for this planet."
        }
    }

    /**
     * Get bindu rules for a planet
     */
    private fun getBinduRulesForPlanet(planet: Planet): Map<Planet, List<Int>> {
        return when (planet) {
            Planet.SUN -> SUN_BINDU_RULES
            Planet.MOON -> MOON_BINDU_RULES
            Planet.MARS -> MARS_BINDU_RULES
            Planet.MERCURY -> MERCURY_BINDU_RULES
            Planet.JUPITER -> JUPITER_BINDU_RULES
            Planet.VENUS -> VENUS_BINDU_RULES
            Planet.SATURN -> SATURN_BINDU_RULES
            else -> emptyMap()
        }
    }

    /**
     * Get Ascendant bindu rules for a planet
     */
    private fun getAscendantBindusForPlanet(planet: Planet): List<Int> {
        return when (planet) {
            Planet.SUN -> SUN_ASCENDANT_BINDUS
            Planet.MOON -> MOON_ASCENDANT_BINDUS
            Planet.MARS -> MARS_ASCENDANT_BINDUS
            Planet.MERCURY -> MERCURY_ASCENDANT_BINDUS
            Planet.JUPITER -> JUPITER_ASCENDANT_BINDUS
            Planet.VENUS -> VENUS_ASCENDANT_BINDUS
            Planet.SATURN -> SATURN_ASCENDANT_BINDUS
            else -> emptyList()
        }
    }

    /**
     * Get sign at specific house from a reference sign
     */
    private fun getSignAtHouse(referenceSign: ZodiacSign, house: Int): ZodiacSign {
        val targetIndex = (referenceSign.ordinal + house - 1) % 12
        return ZodiacSign.entries[targetIndex]
    }

    /**
     * Transit quality enumeration
     */
    enum class TransitQuality(val displayName: String, val score: Int) {
        EXCELLENT("Excellent", 6),
        GOOD("Good", 5),
        AVERAGE("Average", 4),
        BELOW_AVERAGE("Below Average", 3),
        CHALLENGING("Challenging", 2),
        DIFFICULT("Difficult", 1),
        UNKNOWN("Unknown", 0)
    }

    /**
     * Transit prediction result
     */
    data class TransitPrediction(
        val planet: Planet,
        val sign: ZodiacSign,
        val bavBindus: Int,
        val savBindus: Int,
        val quality: TransitQuality,
        val prediction: String
    )

    /**
     * Shodhana (reduction) step for a sign
     */
    data class ShodhanaStep(
        val originalBindus: Int,
        val afterTrikona: Int,
        val afterEkadhipatya: Int,
        val finalPinda: Int
    )

    /**
     * Shodhana result
     */
    data class ShodhanaResult(
        val reductions: Map<ZodiacSign, ShodhanaStep>,
        val totalPinda: Int
    )
}
