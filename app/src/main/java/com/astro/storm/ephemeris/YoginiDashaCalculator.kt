package com.astro.storm.ephemeris

import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Yogini Dasha Calculator
 *
 * Yogini Dasha is a nakshatra-based dasha system with a total cycle of 36 years.
 * It is particularly effective for:
 * - Female horoscopes
 * - Timing of relationships and marriage
 * - Specific event prediction
 * - Short-term predictions
 *
 * The Eight Yoginis:
 * 1. Mangala (Moon) - 1 year
 * 2. Pingala (Sun) - 2 years
 * 3. Dhanya (Jupiter) - 3 years
 * 4. Bhramari (Mars) - 4 years
 * 5. Bhadrika (Mercury) - 5 years
 * 6. Ulka (Saturn) - 6 years
 * 7. Siddha (Venus) - 7 years
 * 8. Sankata (Rahu) - 8 years
 * Total: 36 years
 *
 * Nakshatra Assignment:
 * Nakshatras are assigned to Yoginis based on their sequence:
 * - Nakshatra number modulo 8 determines the starting Yogini
 *
 * References:
 * - Tantra texts on Yogini Dasha
 * - Traditional paramparas on female chart timing
 * - Brihat Parashara Hora Shastra (conditional dasha systems)
 *
 * @author AstroStorm
 */
object YoginiDashaCalculator {

    private val MATH_CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)
    private val DAYS_PER_YEAR = BigDecimal("365.25")
    private val NAKSHATRA_SPAN_DEGREES = BigDecimal("13.333333333333333333")

    /**
     * Yogini enumeration with their planets, years, and characteristics
     */
    enum class Yogini(
        val planet: Planet,
        val years: Int,
        val displayName: String,
        val sanskrit: String,
        val nature: YoginiNature,
        val description: String
    ) {
        MANGALA(
            planet = Planet.MOON,
            years = 1,
            displayName = "Mangala",
            sanskrit = "मंगला",
            nature = YoginiNature.AUSPICIOUS,
            description = "Auspicious beginnings, prosperity, and happiness. Moon's nurturing energy brings emotional fulfillment."
        ),
        PINGALA(
            planet = Planet.SUN,
            years = 2,
            displayName = "Pingala",
            sanskrit = "पिंगला",
            nature = YoginiNature.MIXED,
            description = "Authority, father-related matters, government dealings. Can bring recognition but also ego challenges."
        ),
        DHANYA(
            planet = Planet.JUPITER,
            years = 3,
            displayName = "Dhanya",
            sanskrit = "धान्या",
            nature = YoginiNature.HIGHLY_AUSPICIOUS,
            description = "Wealth, wisdom, children, and spiritual growth. Jupiter's grace brings expansion and good fortune."
        ),
        BHRAMARI(
            planet = Planet.MARS,
            years = 4,
            displayName = "Bhramari",
            sanskrit = "भ्रामरी",
            nature = YoginiNature.CHALLENGING,
            description = "Energy, conflicts, property matters, siblings. Mars brings action but requires careful handling."
        ),
        BHADRIKA(
            planet = Planet.MERCURY,
            years = 5,
            displayName = "Bhadrika",
            sanskrit = "भद्रिका",
            nature = YoginiNature.AUSPICIOUS,
            description = "Intelligence, communication, business success. Mercury's wit brings learning and commercial gains."
        ),
        ULKA(
            planet = Planet.SATURN,
            years = 6,
            displayName = "Ulka",
            sanskrit = "उल्का",
            nature = YoginiNature.CHALLENGING,
            description = "Hardship, discipline, delays, but eventual success through perseverance. Saturn teaches patience."
        ),
        SIDDHA(
            planet = Planet.VENUS,
            years = 7,
            displayName = "Siddha",
            sanskrit = "सिद्धा",
            nature = YoginiNature.HIGHLY_AUSPICIOUS,
            description = "Success, luxury, marriage, artistic achievements. Venus brings pleasure, love, and material comforts."
        ),
        SANKATA(
            planet = Planet.RAHU,
            years = 8,
            displayName = "Sankata",
            sanskrit = "संकटा",
            nature = YoginiNature.DIFFICULT,
            description = "Obstacles, foreign influences, unconventional experiences. Rahu brings sudden changes and karmic lessons."
        );

        companion object {
            /**
             * Get Yogini by index (0-7)
             */
            fun fromIndex(index: Int): Yogini {
                val normalizedIndex = ((index % 8) + 8) % 8
                return entries[normalizedIndex]
            }

            /**
             * Get starting Yogini for a nakshatra
             * Formula: (Nakshatra number + 3) mod 8
             */
            fun getStartingYogini(nakshatra: Nakshatra): Yogini {
                val nakshatraIndex = nakshatra.ordinal + 1 // 1-based index
                val yoginiIndex = (nakshatraIndex + 3) % 8
                return fromIndex(yoginiIndex)
            }
        }
    }

    enum class YoginiNature {
        HIGHLY_AUSPICIOUS,
        AUSPICIOUS,
        MIXED,
        CHALLENGING,
        DIFFICULT
    }

    /**
     * Yogini Mahadasha period
     */
    data class YoginiMahadasha(
        val yogini: Yogini,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationYears: Int,
        val antardashas: List<YoginiAntardasha>,
        val interpretation: YoginiInterpretation
    ) {
        val durationDays: Long
            get() = ChronoUnit.DAYS.between(startDate, endDate)

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getActiveAntardasha(): YoginiAntardasha? = getAntardashaOn(LocalDate.now())

        fun getAntardashaOn(date: LocalDate): YoginiAntardasha? {
            return antardashas.find { it.isActiveOn(date) }
        }

        fun getElapsedDays(asOf: LocalDate = LocalDate.now()): Long {
            if (asOf.isBefore(startDate)) return 0
            if (asOf.isAfter(endDate)) return durationDays
            return ChronoUnit.DAYS.between(startDate, asOf)
        }

        fun getRemainingDays(asOf: LocalDate = LocalDate.now()): Long {
            return (durationDays - getElapsedDays(asOf)).coerceAtLeast(0)
        }

        fun getProgressPercent(asOf: LocalDate = LocalDate.now()): Double {
            if (durationDays <= 0) return 0.0
            return ((getElapsedDays(asOf).toDouble() / durationDays) * 100).coerceIn(0.0, 100.0)
        }
    }

    /**
     * Yogini Antardasha (sub-period)
     */
    data class YoginiAntardasha(
        val yogini: Yogini,
        val mahadashaYogini: Yogini,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Long,
        val interpretation: String
    ) {
        val durationMonths: Double
            get() = durationDays / 30.4375

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getElapsedDays(asOf: LocalDate = LocalDate.now()): Long {
            if (asOf.isBefore(startDate)) return 0
            if (asOf.isAfter(endDate)) return durationDays
            return ChronoUnit.DAYS.between(startDate, asOf)
        }

        fun getRemainingDays(asOf: LocalDate = LocalDate.now()): Long {
            return (durationDays - getElapsedDays(asOf)).coerceAtLeast(0)
        }

        fun getProgressPercent(asOf: LocalDate = LocalDate.now()): Double {
            if (durationDays <= 0) return 0.0
            return ((getElapsedDays(asOf).toDouble() / durationDays) * 100).coerceIn(0.0, 100.0)
        }
    }

    /**
     * Interpretation for Yogini period
     */
    data class YoginiInterpretation(
        val generalEffects: String,
        val careerEffects: String,
        val relationshipEffects: String,
        val healthEffects: String,
        val spiritualEffects: String,
        val recommendations: List<String>,
        val cautionAreas: List<String>
    )

    /**
     * Complete Yogini Dasha analysis result
     */
    data class YoginiDashaResult(
        val birthNakshatra: Nakshatra,
        val moonLongitude: Double,
        val positionInNakshatra: Double,
        val startingYogini: Yogini,
        val balanceAtBirth: BalanceAtBirth,
        val mahadashas: List<YoginiMahadasha>,
        val currentMahadasha: YoginiMahadasha?,
        val currentAntardasha: YoginiAntardasha?,
        val applicability: Applicability
    )

    /**
     * Balance of first Yogini at birth
     */
    data class BalanceAtBirth(
        val yogini: Yogini,
        val totalYears: Int,
        val balanceYears: Double,
        val balanceDays: Long,
        val elapsed: Double
    )

    /**
     * Applicability assessment for Yogini Dasha
     */
    data class Applicability(
        val isRecommended: Boolean,
        val applicabilityScore: Double,
        val reasons: List<String>
    )

    // ============================================
    // TOTAL CYCLE YEARS
    // ============================================
    private const val TOTAL_CYCLE_YEARS = 36

    // ============================================
    // MAIN CALCULATION METHODS
    // ============================================

    /**
     * Calculate complete Yogini Dasha from a VedicChart
     *
     * @param chart The VedicChart containing birth information
     * @param numberOfCycles Number of complete cycles to calculate (default: 3 = 108 years)
     * @return Complete YoginiDashaResult
     */
    fun calculateYoginiDasha(
        chart: VedicChart,
        numberOfCycles: Int = 3
    ): YoginiDashaResult {
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found in chart")

        val birthDate = chart.birthData.birthDate
        val moonLongitude = moonPosition.longitude

        return calculateYoginiDashaFromMoon(moonLongitude, birthDate, numberOfCycles, chart)
    }

    /**
     * Calculate Yogini Dasha from Moon longitude and birth date
     *
     * @param moonLongitude Moon's longitude in degrees (0-360)
     * @param birthDate Date of birth
     * @param numberOfCycles Number of 36-year cycles to calculate
     * @param chart Optional chart for additional analysis
     * @return Complete YoginiDashaResult
     */
    fun calculateYoginiDashaFromMoon(
        moonLongitude: Double,
        birthDate: LocalDate,
        numberOfCycles: Int = 3,
        chart: VedicChart? = null
    ): YoginiDashaResult {
        // Get birth nakshatra
        val birthNakshatra = VedicAstrologyUtils.getNakshatra(moonLongitude)

        // Calculate position within nakshatra (0-1 range)
        val positionInNakshatra = calculatePositionInNakshatra(moonLongitude)

        // Get starting Yogini
        val startingYogini = Yogini.getStartingYogini(birthNakshatra)

        // Calculate balance at birth
        val balanceAtBirth = calculateBalanceAtBirth(startingYogini, positionInNakshatra)

        // Calculate all Mahadashas
        val mahadashas = calculateMahadashas(
            startingYogini,
            balanceAtBirth,
            birthDate,
            numberOfCycles,
            chart
        )

        // Find current periods
        val today = LocalDate.now()
        val currentMahadasha = mahadashas.find { it.isActiveOn(today) }
        val currentAntardasha = currentMahadasha?.getAntardashaOn(today)

        // Assess applicability
        val applicability = assessApplicability(chart)

        return YoginiDashaResult(
            birthNakshatra = birthNakshatra,
            moonLongitude = moonLongitude,
            positionInNakshatra = positionInNakshatra,
            startingYogini = startingYogini,
            balanceAtBirth = balanceAtBirth,
            mahadashas = mahadashas,
            currentMahadasha = currentMahadasha,
            currentAntardasha = currentAntardasha,
            applicability = applicability
        )
    }

    /**
     * Calculate position within nakshatra (0-1 range)
     */
    private fun calculatePositionInNakshatra(moonLongitude: Double): Double {
        val normalizedLong = ((moonLongitude % 360.0) + 360.0) % 360.0
        val positionWithinZodiac = BigDecimal(normalizedLong.toString())
        val nakshatraIndex = positionWithinZodiac.divide(NAKSHATRA_SPAN_DEGREES, MATH_CONTEXT)
        return nakshatraIndex.remainder(BigDecimal.ONE, MATH_CONTEXT).toDouble()
    }

    /**
     * Calculate balance of first Yogini at birth
     */
    private fun calculateBalanceAtBirth(
        startingYogini: Yogini,
        positionInNakshatra: Double
    ): BalanceAtBirth {
        val totalYears = startingYogini.years
        val elapsedPortion = positionInNakshatra
        val balanceYears = totalYears * (1.0 - elapsedPortion)
        val balanceDays = yearsToRoundedDays(balanceYears)

        return BalanceAtBirth(
            yogini = startingYogini,
            totalYears = totalYears,
            balanceYears = balanceYears,
            balanceDays = balanceDays,
            elapsed = totalYears * elapsedPortion
        )
    }

    /**
     * Convert years to days with proper rounding
     */
    private fun yearsToRoundedDays(years: Double): Long {
        return BigDecimal(years.toString())
            .multiply(DAYS_PER_YEAR, MATH_CONTEXT)
            .setScale(0, RoundingMode.HALF_EVEN)
            .toLong()
            .coerceAtLeast(1L)
    }

    /**
     * Calculate all Mahadashas for the specified number of cycles
     */
    private fun calculateMahadashas(
        startingYogini: Yogini,
        balanceAtBirth: BalanceAtBirth,
        birthDate: LocalDate,
        numberOfCycles: Int,
        chart: VedicChart?
    ): List<YoginiMahadasha> {
        val mahadashas = mutableListOf<YoginiMahadasha>()

        var currentDate = birthDate
        val totalMahadashas = numberOfCycles * 8 // 8 Yoginis per cycle

        // First, add the balance of starting Yogini
        if (balanceAtBirth.balanceDays > 0) {
            val endDate = currentDate.plusDays(balanceAtBirth.balanceDays)
            val antardashas = calculateAntardashas(
                balanceAtBirth.yogini,
                currentDate,
                endDate,
                chart
            )
            val interpretation = generateInterpretation(balanceAtBirth.yogini, chart)

            mahadashas.add(
                YoginiMahadasha(
                    yogini = balanceAtBirth.yogini,
                    startDate = currentDate,
                    endDate = endDate,
                    durationYears = balanceAtBirth.totalYears,
                    antardashas = antardashas,
                    interpretation = interpretation
                )
            )
            currentDate = endDate.plusDays(1)
        }

        // Calculate subsequent Mahadashas
        var yoginiIndex = (startingYogini.ordinal + 1) % 8
        var count = 1 // Already added balance period

        while (count < totalMahadashas) {
            val yogini = Yogini.fromIndex(yoginiIndex)
            val durationDays = yearsToRoundedDays(yogini.years.toDouble())
            val endDate = currentDate.plusDays(durationDays)

            val antardashas = calculateAntardashas(yogini, currentDate, endDate, chart)
            val interpretation = generateInterpretation(yogini, chart)

            mahadashas.add(
                YoginiMahadasha(
                    yogini = yogini,
                    startDate = currentDate,
                    endDate = endDate,
                    durationYears = yogini.years,
                    antardashas = antardashas,
                    interpretation = interpretation
                )
            )

            currentDate = endDate.plusDays(1)
            yoginiIndex = (yoginiIndex + 1) % 8
            count++
        }

        return mahadashas
    }

    /**
     * Calculate Antardashas within a Mahadasha
     *
     * In Yogini Dasha, Antardashas follow the same sequence as Mahadashas,
     * starting from the Mahadasha lord, with proportionate duration.
     */
    private fun calculateAntardashas(
        mahadashaYogini: Yogini,
        mahadashaStart: LocalDate,
        mahadashaEnd: LocalDate,
        chart: VedicChart?
    ): List<YoginiAntardasha> {
        val antardashas = mutableListOf<YoginiAntardasha>()
        val mahaDurationDays = ChronoUnit.DAYS.between(mahadashaStart, mahadashaEnd)

        if (mahaDurationDays <= 0) return antardashas

        // Calculate total years for proportion calculation
        val totalAntardashaYears = TOTAL_CYCLE_YEARS.toDouble()

        var currentDate = mahadashaStart
        var yoginiIndex = mahadashaYogini.ordinal

        // Calculate 8 antardashas, starting from mahadasha yogini
        for (i in 0 until 8) {
            val antardashaYogini = Yogini.fromIndex(yoginiIndex)

            // Proportionate duration: (Antardasha Yogini years / 36) * Mahadasha duration
            val proportion = antardashaYogini.years.toDouble() / totalAntardashaYears
            val antarDurationDays = (mahaDurationDays * proportion).toLong().coerceAtLeast(1L)

            val endDate = if (i == 7) {
                // Last antardasha ends exactly at mahadasha end
                mahadashaEnd
            } else {
                currentDate.plusDays(antarDurationDays).let {
                    if (it.isAfter(mahadashaEnd)) mahadashaEnd else it
                }
            }

            val interpretation = generateAntardashaInterpretation(
                mahadashaYogini,
                antardashaYogini,
                chart
            )

            antardashas.add(
                YoginiAntardasha(
                    yogini = antardashaYogini,
                    mahadashaYogini = mahadashaYogini,
                    startDate = currentDate,
                    endDate = endDate,
                    durationDays = ChronoUnit.DAYS.between(currentDate, endDate).coerceAtLeast(1L),
                    interpretation = interpretation
                )
            )

            if (endDate >= mahadashaEnd) break

            currentDate = endDate.plusDays(1)
            yoginiIndex = (yoginiIndex + 1) % 8
        }

        return antardashas
    }

    /**
     * Generate interpretation for Yogini Mahadasha
     */
    private fun generateInterpretation(yogini: Yogini, chart: VedicChart?): YoginiInterpretation {
        val generalEffects = yogini.description

        val careerEffects = when (yogini) {
            Yogini.MANGALA -> "Career brings emotional satisfaction. Good for nurturing professions, public relations, and hospitality industry."
            Yogini.PINGALA -> "Leadership opportunities arise. Government jobs, authority positions, and recognition in career. Father may influence career."
            Yogini.DHANYA -> "Expansion in career, promotions, and higher learning. Excellent for teaching, consulting, and advisory roles."
            Yogini.BHRAMARI -> "Active period in career with competition. Good for technical, engineering, and defense-related fields. Property dealings."
            Yogini.BHADRIKA -> "Business acumen peaks. Excellent for trade, communication, writing, and intellectual pursuits. Good for learning new skills."
            Yogini.ULKA -> "Slow but steady career progress. Hard work brings delayed rewards. Good for perseverance in long-term projects."
            Yogini.SIDDHA -> "Career success through creativity and charm. Excellent for arts, entertainment, luxury goods, and beauty industries."
            Yogini.SANKATA -> "Unconventional career paths, foreign opportunities. May bring sudden changes in profession. Research and technology favored."
        }

        val relationshipEffects = when (yogini) {
            Yogini.MANGALA -> "Emotional connections deepen. Good for starting relationships. Mother's influence prominent. Nurturing partnerships."
            Yogini.PINGALA -> "Relationships with authority figures. Father-related matters in marriage. May face ego conflicts with partners."
            Yogini.DHANYA -> "Excellent for marriage and childbirth. Spiritual connections in relationships. Teachers and mentors become important."
            Yogini.BHRAMARI -> "Passionate but potentially conflicting relationships. Brothers/sisters prominent. Physical attraction strong."
            Yogini.BHADRIKA -> "Communication improves relationships. Intellectual compatibility matters. Good for understanding partners better."
            Yogini.ULKA -> "Delays or challenges in relationships. Karmic partners appear. Long-distance relationships possible."
            Yogini.SIDDHA -> "Excellent for romance, marriage, and love. Harmonious relationships. Beauty and pleasure in partnerships."
            Yogini.SANKATA -> "Unusual or foreign partners. Sudden attractions or separations. Need to address karmic relationship patterns."
        }

        val healthEffects = when (yogini) {
            Yogini.MANGALA -> "Generally good health. Focus on emotional and mental well-being. Water-related activities beneficial."
            Yogini.PINGALA -> "Watch heart, eyes, and overall vitality. Morning sun exposure beneficial. Maintain healthy ego."
            Yogini.DHANYA -> "Good health generally. Watch liver and weight. Spiritual practices enhance well-being."
            Yogini.BHRAMARI -> "Watch for injuries, inflammations, and accidents. Physical exercise important but avoid overexertion."
            Yogini.BHADRIKA -> "Nervous system needs attention. Skin issues possible. Mental relaxation techniques helpful."
            Yogini.ULKA -> "Chronic issues may arise. Bones, joints, and teeth need care. Patience in recovery."
            Yogini.SIDDHA -> "Generally good health. Watch reproductive system. Beauty treatments beneficial."
            Yogini.SANKATA -> "Unusual or hard-to-diagnose health issues. Mental health important. Alternative therapies may help."
        }

        val spiritualEffects = when (yogini) {
            Yogini.MANGALA -> "Devotional practices strengthen. Mother goddess worship beneficial. Emotional cleansing through meditation."
            Yogini.PINGALA -> "Solar meditation and surya namaskar beneficial. Connection with divine father principle. Self-realization focus."
            Yogini.DHANYA -> "Excellent for spiritual growth and higher learning. Guru connection strengthens. Sacred knowledge flows."
            Yogini.BHRAMARI -> "Active spiritual practices like yoga and pranayama. Kundalini may activate. Mars-related deity worship helps."
            Yogini.BHADRIKA -> "Intellectual approach to spirituality. Study of scriptures beneficial. Mantra practice effective."
            Yogini.ULKA -> "Deep karmic cleansing period. Meditation on impermanence. Service-oriented spirituality."
            Yogini.SIDDHA -> "Tantric practices may attract. Beauty in spirituality. Heart-centered practices beneficial."
            Yogini.SANKATA -> "Deep transformation possible. Occult interests may arise. Breaking free from illusions."
        }

        val recommendations = when (yogini) {
            Yogini.MANGALA -> listOf(
                "Wear pearl or moonstone",
                "Honor your mother and maternal figures",
                "Practice moon salutations",
                "Donate white items on Mondays"
            )
            Yogini.PINGALA -> listOf(
                "Wear ruby if suitable",
                "Practice Surya Namaskar at sunrise",
                "Honor your father",
                "Donate wheat or jaggery on Sundays"
            )
            Yogini.DHANYA -> listOf(
                "Wear yellow sapphire if suitable",
                "Seek blessings from teachers",
                "Study sacred texts",
                "Donate to educational causes on Thursdays"
            )
            Yogini.BHRAMARI -> listOf(
                "Wear red coral if suitable",
                "Practice physical exercise regularly",
                "Channel energy constructively",
                "Donate red items on Tuesdays"
            )
            Yogini.BHADRIKA -> listOf(
                "Wear emerald if suitable",
                "Engage in learning and communication",
                "Write and express yourself",
                "Donate green items on Wednesdays"
            )
            Yogini.ULKA -> listOf(
                "Wear blue sapphire with caution",
                "Practice patience and discipline",
                "Serve the elderly and disadvantaged",
                "Donate oil and black items on Saturdays"
            )
            Yogini.SIDDHA -> listOf(
                "Wear diamond or white sapphire if suitable",
                "Engage in arts and creativity",
                "Cultivate harmonious relationships",
                "Donate white items on Fridays"
            )
            Yogini.SANKATA -> listOf(
                "Wear hessonite after careful analysis",
                "Stay grounded during sudden changes",
                "Address past-life patterns through meditation",
                "Donate blue/black items on Saturdays"
            )
        }

        val cautionAreas = when (yogini) {
            Yogini.MANGALA -> listOf("Emotional volatility", "Over-attachment", "Water-related issues")
            Yogini.PINGALA -> listOf("Ego conflicts", "Eye problems", "Father's health")
            Yogini.DHANYA -> listOf("Over-expansion", "Weight issues", "Over-optimism")
            Yogini.BHRAMARI -> listOf("Anger management", "Accidents", "Property disputes")
            Yogini.BHADRIKA -> listOf("Nervousness", "Overthinking", "Skin issues")
            Yogini.ULKA -> listOf("Depression", "Delays", "Chronic health issues")
            Yogini.SIDDHA -> listOf("Indulgence", "Relationship complications", "Luxury overspending")
            Yogini.SANKATA -> listOf("Sudden obstacles", "Confusion", "Unusual diseases")
        }

        return YoginiInterpretation(
            generalEffects = generalEffects,
            careerEffects = careerEffects,
            relationshipEffects = relationshipEffects,
            healthEffects = healthEffects,
            spiritualEffects = spiritualEffects,
            recommendations = recommendations,
            cautionAreas = cautionAreas
        )
    }

    /**
     * Generate interpretation for Antardasha
     */
    private fun generateAntardashaInterpretation(
        mahadashaYogini: Yogini,
        antardashaYogini: Yogini,
        chart: VedicChart?
    ): String {
        val mahaPlanet = mahadashaYogini.planet
        val antarPlanet = antardashaYogini.planet

        // Natural friendship/enmity affects interpretation
        val relationship = getPlanetaryRelationship(mahaPlanet, antarPlanet)

        return when (relationship) {
            PlanetaryRelationship.FRIEND -> buildString {
                append("${mahadashaYogini.displayName}-${antardashaYogini.displayName}: ")
                append("Harmonious sub-period with complementary energies. ")
                append("${antarPlanet.displayName}'s significations blend well with ")
                append("${mahaPlanet.displayName}'s ongoing influence. ")
                append("Good for collaborative efforts and relationship building.")
            }
            PlanetaryRelationship.NEUTRAL -> buildString {
                append("${mahadashaYogini.displayName}-${antardashaYogini.displayName}: ")
                append("Balanced sub-period requiring conscious integration. ")
                append("${antarPlanet.displayName}'s themes activate within ")
                append("${mahaPlanet.displayName}'s framework. ")
                append("Results depend on individual chart placements.")
            }
            PlanetaryRelationship.ENEMY -> buildString {
                append("${mahadashaYogini.displayName}-${antardashaYogini.displayName}: ")
                append("Potentially challenging sub-period with conflicting energies. ")
                append("${antarPlanet.displayName} may create tension with ")
                append("${mahaPlanet.displayName}'s ongoing themes. ")
                append("Requires patience, remedies, and conscious effort for harmony.")
            }
        }
    }

    /**
     * Get planetary relationship (simplified)
     */
    private fun getPlanetaryRelationship(planet1: Planet, planet2: Planet): PlanetaryRelationship {
        if (planet1 == planet2) return PlanetaryRelationship.FRIEND

        val friendships = mapOf(
            Planet.SUN to setOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
            Planet.MOON to setOf(Planet.SUN, Planet.MERCURY),
            Planet.MARS to setOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
            Planet.MERCURY to setOf(Planet.SUN, Planet.VENUS),
            Planet.JUPITER to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
            Planet.VENUS to setOf(Planet.MERCURY, Planet.SATURN),
            Planet.SATURN to setOf(Planet.MERCURY, Planet.VENUS),
            Planet.RAHU to setOf(Planet.SATURN, Planet.VENUS, Planet.MERCURY),
            Planet.KETU to setOf(Planet.MARS, Planet.JUPITER)
        )

        val enemies = mapOf(
            Planet.SUN to setOf(Planet.SATURN, Planet.VENUS, Planet.RAHU),
            Planet.MOON to setOf(Planet.RAHU, Planet.KETU),
            Planet.MARS to setOf(Planet.MERCURY),
            Planet.MERCURY to setOf(Planet.MOON),
            Planet.JUPITER to setOf(Planet.MERCURY, Planet.VENUS),
            Planet.VENUS to setOf(Planet.SUN, Planet.MOON),
            Planet.SATURN to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
            Planet.RAHU to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
            Planet.KETU to setOf(Planet.MOON)
        )

        return when {
            friendships[planet1]?.contains(planet2) == true -> PlanetaryRelationship.FRIEND
            enemies[planet1]?.contains(planet2) == true -> PlanetaryRelationship.ENEMY
            else -> PlanetaryRelationship.NEUTRAL
        }
    }

    private enum class PlanetaryRelationship {
        FRIEND, NEUTRAL, ENEMY
    }

    /**
     * Assess applicability of Yogini Dasha for a chart
     *
     * Yogini Dasha is particularly recommended for:
     * - Female horoscopes
     * - Charts with strong Moon
     * - Night births
     * - When Vimsottari gives conflicting results
     */
    private fun assessApplicability(chart: VedicChart?): Applicability {
        if (chart == null) {
            return Applicability(
                isRecommended = true,
                applicabilityScore = 0.7,
                reasons = listOf("Yogini Dasha is universally applicable for timing specific events")
            )
        }

        val reasons = mutableListOf<String>()
        var score = 0.5 // Base score

        // Check gender (if available - traditionally more applicable for females)
        if (chart.birthData.gender == com.astro.storm.data.model.Gender.FEMALE) {
            score += 0.2
            reasons.add("Yogini Dasha is traditionally considered more accurate for female horoscopes")
        }

        // Check Moon strength
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
        if (moonPosition != null) {
            val moonSign = VedicAstrologyUtils.getZodiacSign(moonPosition.longitude)
            // Moon strong in Taurus (exalted) or Cancer (own sign)
            if (moonSign == ZodiacSign.TAURUS || moonSign == ZodiacSign.CANCER) {
                score += 0.15
                reasons.add("Strong Moon in ${moonSign.displayName} enhances Yogini Dasha applicability")
            }
        }

        // Night birth gives preference to Yogini Dasha (simplified check)
        // In production, you would check actual sunrise/sunset times
        if (chart.birthData.birthTime.hour < 6 || chart.birthData.birthTime.hour >= 18) {
            score += 0.1
            reasons.add("Night birth traditionally favors Yogini Dasha")
        }

        // Always applicable for relationship timing
        reasons.add("Yogini Dasha excels at timing relationship and marriage events")

        // Add general applicability statement
        reasons.add("Can be used alongside Vimsottari Dasha for validation")

        return Applicability(
            isRecommended = score >= 0.6,
            applicabilityScore = score.coerceIn(0.0, 1.0),
            reasons = reasons
        )
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Get current Yogini period summary
     */
    fun getCurrentPeriodSummary(result: YoginiDashaResult): String {
        val maha = result.currentMahadasha ?: return "No active Yogini Dasha period"
        val antar = result.currentAntardasha

        return buildString {
            append("Current Yogini Mahadasha: ${maha.yogini.displayName} (${maha.yogini.sanskrit})\n")
            append("Planet: ${maha.yogini.planet.displayName}\n")
            append("Duration: ${maha.durationYears} years\n")
            append("Progress: ${String.format("%.1f", maha.getProgressPercent())}%\n")
            append("Remaining: ${maha.getRemainingDays()} days\n")

            if (antar != null) {
                append("\nCurrent Antardasha: ${antar.yogini.displayName}\n")
                append("Progress: ${String.format("%.1f", antar.getProgressPercent())}%\n")
                append("Remaining: ${antar.getRemainingDays()} days")
            }
        }
    }

    /**
     * Get Yogini sequence starting from a given Yogini
     */
    fun getYoginiSequence(startingYogini: Yogini): List<Yogini> {
        return (0 until 8).map { Yogini.fromIndex(startingYogini.ordinal + it) }
    }
}
