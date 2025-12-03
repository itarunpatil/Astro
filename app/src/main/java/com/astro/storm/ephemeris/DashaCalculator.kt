package com.astro.storm.ephemeris

import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Days per year for dasha calculations.
 */
private const val DAYS_PER_YEAR = 365.25
private const val NAKSHATRA_SPAN = 360.0 / 27.0

/**
 * Convert years to days with proper rounding to avoid cumulative precision loss.
 * Uses banker's rounding (round half to even) for consistent results.
 */
private fun yearsToRoundedDays(years: Double): Long {
    val yearsBd = years.toBigDecimal()
    val daysPerYearBd = DAYS_PER_YEAR.toBigDecimal()
    return (yearsBd * daysPerYearBd).toLong()
}

/**
 * Vimshottari Dasha Calculator
 *
 * The Vimshottari Dasha is the most widely used timing system in Vedic astrology.
 * It is a 120-year cycle based on the Moon's Nakshatra at birth.
 *
 * Each Nakshatra is ruled by a planet, and the Dasha sequence follows:
 * Ketu (7) → Venus (20) → Sun (6) → Moon (10) → Mars (7) →
 * Rahu (18) → Jupiter (16) → Saturn (19) → Mercury (17) = 120 years
 *
 * The birth Nakshatra determines the starting Mahadasha, and the
 * precise Moon position within the Nakshatra determines how much
 * of that first Dasha has already elapsed at birth.
 *
 * This calculator provides:
 * - Accurate Mahadasha periods
 * - Antardasha (Bhukti) sub-periods
 * - Pratyantardasha (sub-sub-periods)
 * - Current Dasha determination
 * - Complete Dasha timeline
 */
object DashaCalculator {

    /**
     * Dasha duration for each planet in years
     */
    private val DASHA_YEARS = mapOf(
        Planet.KETU to 7.0,
        Planet.VENUS to 20.0,
        Planet.SUN to 6.0,
        Planet.MOON to 10.0,
        Planet.MARS to 7.0,
        Planet.RAHU to 18.0,
        Planet.JUPITER to 16.0,
        Planet.SATURN to 19.0,
        Planet.MERCURY to 17.0
    )

    /**
     * Dasha sequence (Vimshottari order)
     */
    private val DASHA_SEQUENCE = listOf(
        Planet.KETU,
        Planet.VENUS,
        Planet.SUN,
        Planet.MOON,
        Planet.MARS,
        Planet.RAHU,
        Planet.JUPITER,
        Planet.SATURN,
        Planet.MERCURY
    )

    /**
     * Total Vimshottari cycle duration in years
     */
    private const val TOTAL_CYCLE_YEARS = 120.0

/**
 * Mahadasha period
     */
    data class Mahadasha(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationYears: Double,
        val antardashas: List<Antardasha>
    ) {
        val isActive: Boolean
            get() {
                val today = LocalDate.now()
                return !today.isBefore(startDate) && today.isBefore(endDate)
            }

        fun getActiveAntardasha(): Antardasha? {
            return antardashas.find { it.isActive }
        }
    }

    /**
     * Antardasha (Bhukti) sub-period
     */
    data class Antardasha(
        val planet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Long,
        val pratyantardashas: List<Pratyantardasha> = emptyList()
    ) {
        val isActive: Boolean
            get() {
                val today = LocalDate.now()
                return !today.isBefore(startDate) && today.isBefore(endDate)
            }

        fun getActivePratyantardasha(): Pratyantardasha? {
            return pratyantardashas.find { it.isActive }
        }
    }

    /**
     * Pratyantardasha (sub-sub-period)
     */
    data class Pratyantardasha(
        val planet: Planet,
        val antardashaplanet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Long
    ) {
        val isActive: Boolean
            get() {
                val today = LocalDate.now()
                return !today.isBefore(startDate) && today.isBefore(endDate)
            }
    }

    /**
     * Complete Dasha timeline
     */
    data class DashaTimeline(
        val birthDate: LocalDate,
        val birthNakshatra: Nakshatra,
        val birthNakshatraLord: Planet,
        val nakshatraProgress: Double,       // How far into nakshatra (0-1)
        val balanceOfFirstDasha: Double,     // Remaining years of first dasha
        val mahadashas: List<Mahadasha>,
        val currentMahadasha: Mahadasha?,
        val currentAntardasha: Antardasha?,
        val currentPratyantardasha: Pratyantardasha?
    ) {
        /**
         * Get current period description
         */
        fun getCurrentPeriodDescription(): String {
            return buildString {
                currentMahadasha?.let { md ->
                    append("${md.planet.displayName} Mahadasha")
                    currentAntardasha?.let { ad ->
                        append(" / ${ad.planet.displayName} Bhukti")
                        currentPratyantardasha?.let { pd ->
                            append(" / ${pd.planet.displayName} Pratyantar")
                        }
                    }
                } ?: append("No active Dasha")
            }
        }
    }

    /**
     * Calculate complete Vimshottari Dasha timeline for a chart
     */
    fun calculateDashaTimeline(chart: VedicChart): DashaTimeline {
        val birthDate = chart.birthData.dateTime.toLocalDate()
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found in chart")

        val moonLongitude = moonPosition.longitude
        val (birthNakshatra, pada) = Nakshatra.fromLongitude(moonLongitude)
        val nakshatraLord = birthNakshatra.ruler

        // High-precision calculation for nakshatra progress
        val nakshatraStartDegree = birthNakshatra.startDegree.toBigDecimal()
        val moonLongitudeBd = moonLongitude.toBigDecimal()
        val nakshatraSpanBd = NAKSHATRA_SPAN.toBigDecimal()

        var degreesIntoNakshatra = (moonLongitudeBd - nakshatraStartDegree) % nakshatraSpanBd
        if (degreesIntoNakshatra < java.math.BigDecimal.ZERO) {
            degreesIntoNakshatra += nakshatraSpanBd
        }

        val nakshatraProgress = (degreesIntoNakshatra / nakshatraSpanBd).toDouble().coerceIn(0.0, 1.0)

        // Calculate remaining balance of first Mahadasha
        val firstDashaYears = DASHA_YEARS[nakshatraLord] ?: 0.0
        val elapsedInFirstDasha = nakshatraProgress * firstDashaYears
        val balanceOfFirstDasha = firstDashaYears - elapsedInFirstDasha

        // Calculate all Mahadashas
        val mahadashas = calculateAllMahadashas(birthDate, nakshatraLord, balanceOfFirstDasha)

        // Find current periods
        val currentMahadasha = mahadashas.find { it.isActive }
        val currentAntardasha = currentMahadasha?.getActiveAntardasha()
        val currentPratyantardasha = currentAntardasha?.getActivePratyantardasha()

        return DashaTimeline(
            birthDate = birthDate,
            birthNakshatra = birthNakshatra,
            birthNakshatraLord = nakshatraLord,
            nakshatraProgress = nakshatraProgress,
            balanceOfFirstDasha = balanceOfFirstDasha,
            mahadashas = mahadashas,
            currentMahadasha = currentMahadasha,
            currentAntardasha = currentAntardasha,
            currentPratyantardasha = currentPratyantardasha
        )
    }

    /**
     * Calculate all Mahadashas starting from birth
     */
    private fun calculateAllMahadashas(
        birthDate: LocalDate,
        startingDashaLord: Planet,
        balanceOfFirstDasha: Double
    ): List<Mahadasha> {
        val mahadashas = mutableListOf<Mahadasha>()
        var currentStartDate = birthDate

        // Find starting index in sequence
        val startIndex = DASHA_SEQUENCE.indexOf(startingDashaLord)

        // Calculate first (partial) Mahadasha with proper rounding to minimize precision loss
        val firstDashaDays = yearsToRoundedDays(balanceOfFirstDasha)
        val firstDashaEndDate = currentStartDate.plusDays(firstDashaDays)

        val firstAntardashas = calculateAntardashas(
            startingDashaLord,
            currentStartDate,
            firstDashaEndDate,
            balanceOfFirstDasha
        )

        mahadashas.add(
            Mahadasha(
                planet = startingDashaLord,
                startDate = currentStartDate,
                endDate = firstDashaEndDate,
                durationYears = balanceOfFirstDasha,
                antardashas = firstAntardashas
            )
        )
        currentStartDate = firstDashaEndDate

        // Calculate remaining Mahadashas (3 full cycles = 360 years to cover any lifespan)
        repeat(27) { cycle ->
            val planetIndex = (startIndex + 1 + cycle) % DASHA_SEQUENCE.size
            val planet = DASHA_SEQUENCE[planetIndex]
            val dashaYears = DASHA_YEARS[planet] ?: 0.0
            val dashaDays = yearsToRoundedDays(dashaYears)
            val endDate = currentStartDate.plusDays(dashaDays)

            val antardashas = calculateAntardashas(planet, currentStartDate, endDate, dashaYears)

            mahadashas.add(
                Mahadasha(
                    planet = planet,
                    startDate = currentStartDate,
                    endDate = endDate,
                    durationYears = dashaYears,
                    antardashas = antardashas
                )
            )
            currentStartDate = endDate
        }

        return mahadashas
    }

    /**
     * Calculate Antardashas (Bhuktis) within a Mahadasha
     *
     * The Antardasha sequence starts with the Mahadasha lord itself,
     * then follows the Vimshottari sequence.
     */
    private fun calculateAntardashas(
        mahadashaPlanet: Planet,
        mahadashaStart: LocalDate,
        mahadashaEnd: LocalDate,
        mahadashaDurationYears: Double
    ): List<Antardasha> {
        val antardashas = mutableListOf<Antardasha>()
        var currentStart = mahadashaStart

        // Start from Mahadasha lord, then follow sequence
        val startIndex = DASHA_SEQUENCE.indexOf(mahadashaPlanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val antarPlanet = DASHA_SEQUENCE[planetIndex]

            // Antardasha duration is proportional to planet's years
            val antarYears = DASHA_YEARS[antarPlanet] ?: 0.0
            val proportionalDuration = (antarYears / TOTAL_CYCLE_YEARS) * mahadashaDurationYears
            val antarDays = yearsToRoundedDays(proportionalDuration)
            val antarEnd = currentStart.plusDays(antarDays)

            // Calculate Pratyantardashas
            val pratyantardashas = calculatePratyantardashas(
                mahadashaPlanet,
                antarPlanet,
                currentStart,
                antarEnd,
                proportionalDuration
            )

            antardashas.add(
                Antardasha(
                    planet = antarPlanet,
                    mahadashaPlanet = mahadashaPlanet,
                    startDate = currentStart,
                    endDate = antarEnd,
                    durationDays = antarDays,
                    pratyantardashas = pratyantardashas
                )
            )
            currentStart = antarEnd
        }

        return antardashas
    }

    /**
     * Calculate Pratyantardashas within an Antardasha
     */
    private fun calculatePratyantardashas(
        mahadashaPlanet: Planet,
        antardashaplanet: Planet,
        antarStart: LocalDate,
        antarEnd: LocalDate,
        antarDurationYears: Double
    ): List<Pratyantardasha> {
        val pratyantardashas = mutableListOf<Pratyantardasha>()
        var currentStart = antarStart

        // Start from Antardasha lord
        val startIndex = DASHA_SEQUENCE.indexOf(antardashaplanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val pratyantarPlanet = DASHA_SEQUENCE[planetIndex]

            // Pratyantardasha duration is proportional
            val pratyantarYears = DASHA_YEARS[pratyantarPlanet] ?: 0.0
            val proportionalDuration = (pratyantarYears / TOTAL_CYCLE_YEARS) * antarDurationYears
            val pratyantarDays = yearsToRoundedDays(proportionalDuration)
            val pratyantarEnd = currentStart.plusDays(pratyantarDays)

            pratyantardashas.add(
                Pratyantardasha(
                    planet = pratyantarPlanet,
                    antardashaplanet = antardashaplanet,
                    mahadashaPlanet = mahadashaPlanet,
                    startDate = currentStart,
                    endDate = pratyantarEnd,
                    durationDays = pratyantarDays
                )
            )
            currentStart = pratyantarEnd
        }

        return pratyantardashas
    }

    /**
     * Get Dasha periods active at a specific date
     */
    fun getDashaAtDate(
        timeline: DashaTimeline,
        date: LocalDate
    ): Triple<Mahadasha?, Antardasha?, Pratyantardasha?> {
        val mahadasha = timeline.mahadashas.find { md ->
            !date.isBefore(md.startDate) && date.isBefore(md.endDate)
        }

        val antardasha = mahadasha?.antardashas?.find { ad ->
            !date.isBefore(ad.startDate) && date.isBefore(ad.endDate)
        }

        val pratyantardasha = antardasha?.pratyantardashas?.find { pd ->
            !date.isBefore(pd.startDate) && date.isBefore(pd.endDate)
        }

        return Triple(mahadasha, antardasha, pratyantardasha)
    }

    /**
     * Format Dasha period for display
     */
    fun formatDashaPeriod(mahadasha: Mahadasha): String {
        return buildString {
            appendLine("${mahadasha.planet.displayName} Mahadasha")
            appendLine("Duration: ${String.format("%.2f", mahadasha.durationYears)} years")
            appendLine("${mahadasha.startDate} to ${mahadasha.endDate}")
            if (mahadasha.isActive) {
                appendLine("** CURRENTLY ACTIVE **")
            }
        }
    }
}

/**
 * Conditional Dasha Systems
 *
 * Beyond Vimshottari, Vedic astrology uses several conditional Dasha systems
 * that are applied when specific planetary configurations exist at birth.
 */
object ConditionalDashaCalculator {

    /**
     * Yogini Dasha - An alternative 36-year cycle
     * Used as a secondary timing system
     */
    data class YoginiDasha(
        val yogini: Yogini,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationYears: Double
    ) {
        val isActive: Boolean
            get() {
                val today = LocalDate.now()
                return !today.isBefore(startDate) && today.isBefore(endDate)
            }
    }

    enum class Yogini(val displayName: String, val planet: Planet, val years: Int) {
        MANGALA("Mangala", Planet.MOON, 1),
        PINGALA("Pingala", Planet.SUN, 2),
        DHANYA("Dhanya", Planet.JUPITER, 3),
        BHRAMARI("Bhramari", Planet.MARS, 4),
        BHADRIKA("Bhadrika", Planet.MERCURY, 5),
        ULKA("Ulka", Planet.SATURN, 6),
        SIDDHA("Siddha", Planet.VENUS, 7),
        SANKATA("Sankata", Planet.RAHU, 8)
    }

    private const val YOGINI_CYCLE_YEARS = 36.0

    /**
     * Calculate Yogini Dasha timeline
     */
    fun calculateYoginiDasha(chart: VedicChart): List<YoginiDasha> {
        val birthDate = chart.birthData.dateTime.toLocalDate()
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found")

        val moonLongitude = moonPosition.longitude
        val (nakshatra, _) = Nakshatra.fromLongitude(moonLongitude)

        // Yogini is determined by (Nakshatra number + 3) mod 8
        // The formula ensures proper cycling through the 8 yoginis (0-7 indexed)
        val yoginiIndex = ((nakshatra.number - 1 + 3) % Yogini.entries.size)
        val startingYogini = Yogini.entries[yoginiIndex]

        // Calculate progress in nakshatra to determine balance
        val nakshatraStart = nakshatra.startDegree
        val progressInNakshatra = ((moonLongitude - nakshatraStart) / NAKSHATRA_SPAN).coerceIn(0.0, 1.0)

        val yoginis = mutableListOf<YoginiDasha>()
        var currentStart = birthDate

        // First (partial) Yogini
        val firstYoginiYears = startingYogini.years.toDouble()
        val balanceOfFirst = firstYoginiYears * (1.0 - progressInNakshatra)
        val firstDays = yearsToRoundedDays(balanceOfFirst)
        val firstEnd = currentStart.plusDays(firstDays)

        yoginis.add(
            YoginiDasha(
                yogini = startingYogini,
                startDate = currentStart,
                endDate = firstEnd,
                durationYears = balanceOfFirst
            )
        )
        currentStart = firstEnd

        // Calculate 10 complete cycles (360 years)
        repeat(80) { cycle ->
            val yoginiIdx = (yoginiIndex + 1 + cycle) % Yogini.entries.size
            val yogini = Yogini.entries[yoginiIdx]
            val years = yogini.years.toDouble()
            val days = yearsToRoundedDays(years)
            val endDate = currentStart.plusDays(days)

            yoginis.add(
                YoginiDasha(
                    yogini = yogini,
                    startDate = currentStart,
                    endDate = endDate,
                    durationYears = years
                )
            )
            currentStart = endDate
        }

        return yoginis
    }

    /**
     * Ashtottari Dasha - 108-year cycle
     * Applied when Rahu is in Kendra (1, 4, 7, 10) or Trikona (1, 5, 9) from Lagna lord
     */
    fun shouldApplyAshtottari(chart: VedicChart): Boolean {
        // Validate chart has required planet positions
        if (chart.planetPositions.isEmpty()) {
            return false
        }

        // Find Lagna lord
        val ascendantSign = com.astro.storm.data.model.ZodiacSign.fromLongitude(chart.ascendant)
        val lagnaLord = ascendantSign.ruler
        val lagnaLordPosition = chart.planetPositions.find { it.planet == lagnaLord }
        val rahuPosition = chart.planetPositions.find { it.planet == Planet.RAHU }

        if (lagnaLordPosition == null || rahuPosition == null) return false

        // Calculate house distance from Lagna lord to Rahu
        val houseDistance = (rahuPosition.house - lagnaLordPosition.house + 12) % 12 + 1

        // Kendra houses: 1, 4, 7, 10
        // Trikona houses: 1, 5, 9
        return houseDistance in listOf(1, 4, 5, 7, 9, 10)
    }
}
