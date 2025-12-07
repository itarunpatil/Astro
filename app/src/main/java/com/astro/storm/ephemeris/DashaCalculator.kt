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

private val MATH_CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)
private val DAYS_PER_YEAR_BD = BigDecimal("365.25")
private val NAKSHATRA_SPAN_BD = BigDecimal("13.333333333333333333")
private val TOTAL_CYCLE_YEARS_BD = BigDecimal("120")

private fun yearsToRoundedDays(years: Double): Long {
    return BigDecimal(years.toString())
        .multiply(DAYS_PER_YEAR_BD, MATH_CONTEXT)
        .setScale(0, RoundingMode.HALF_EVEN)
        .toLong()
        .coerceAtLeast(1L)
}

private fun yearsToRoundedDays(years: BigDecimal): Long {
    return years
        .multiply(DAYS_PER_YEAR_BD, MATH_CONTEXT)
        .setScale(0, RoundingMode.HALF_EVEN)
        .toLong()
        .coerceAtLeast(1L)
}

object DashaCalculator {

    private val DASHA_YEARS: Map<Planet, BigDecimal> = mapOf(
        Planet.KETU to BigDecimal("7"),
        Planet.VENUS to BigDecimal("20"),
        Planet.SUN to BigDecimal("6"),
        Planet.MOON to BigDecimal("10"),
        Planet.MARS to BigDecimal("7"),
        Planet.RAHU to BigDecimal("18"),
        Planet.JUPITER to BigDecimal("16"),
        Planet.SATURN to BigDecimal("19"),
        Planet.MERCURY to BigDecimal("17")
    )

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

    private const val MAX_MAHADASHAS = 12

    fun getDashaYears(planet: Planet): Double {
        return DASHA_YEARS[planet]?.toDouble() ?: 0.0
    }

    enum class CalculationDepth {
        MAHADASHA_ONLY,
        WITH_ANTARDASHA,
        WITH_PRATYANTARDASHA,
        WITH_SOOKSHMADASHA,
        FULL
    }

    data class Mahadasha(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationYears: Double,
        val antardashas: List<Antardasha>
    ) {
        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getActiveAntardasha(): Antardasha? = getAntardashaOn(LocalDate.now())

        fun getAntardashaOn(date: LocalDate): Antardasha? {
            return antardashas.find { it.isActiveOn(date) }
        }

        fun getElapsedYears(asOf: LocalDate = LocalDate.now()): Double {
            if (asOf.isBefore(startDate)) return 0.0
            if (asOf.isAfter(endDate)) return durationYears
            val elapsedDays = ChronoUnit.DAYS.between(startDate, asOf)
            return elapsedDays / 365.25
        }

        fun getRemainingYears(asOf: LocalDate = LocalDate.now()): Double {
            return (durationYears - getElapsedYears(asOf)).coerceAtLeast(0.0)
        }

        fun getProgressPercent(asOf: LocalDate = LocalDate.now()): Double {
            if (durationYears <= 0) return 0.0
            return ((getElapsedYears(asOf) / durationYears) * 100).coerceIn(0.0, 100.0)
        }
    }

    data class Antardasha(
        val planet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Long,
        val pratyantardashas: List<Pratyantardasha> = emptyList()
    ) {
        val durationYears: Double
            get() = durationDays / 365.25

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getActivePratyantardasha(): Pratyantardasha? = getPratyantardashaOn(LocalDate.now())

        fun getPratyantardashaOn(date: LocalDate): Pratyantardasha? {
            return pratyantardashas.find { it.isActiveOn(date) }
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

    data class Pratyantardasha(
        val planet: Planet,
        val antardashaPlanet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Long,
        val sookshmadashas: List<Sookshmadasha> = emptyList()
    ) {
        val durationYears: Double
            get() = durationDays / 365.25

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getActiveSookshmadasha(): Sookshmadasha? = getSookshmadashaOn(LocalDate.now())

        fun getSookshmadashaOn(date: LocalDate): Sookshmadasha? {
            return sookshmadashas.find { it.isActiveOn(date) }
        }
    }

    data class Sookshmadasha(
        val planet: Planet,
        val pratyantardashaPlanet: Planet,
        val antardashaPlanet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationDays: Long,
        val pranadashas: List<Pranadasha> = emptyList()
    ) {
        val durationHours: Double
            get() = durationDays * 24.0

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getActivePranadasha(): Pranadasha? = getPranadashaOn(LocalDate.now())

        fun getPranadashaOn(date: LocalDate): Pranadasha? {
            return pranadashas.find { it.isActiveOn(date) }
        }
    }

    data class Pranadasha(
        val planet: Planet,
        val sookshmadashaPlanet: Planet,
        val pratyantardashaPlanet: Planet,
        val antardashaPlanet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationMinutes: Long,
        val dehadashas: List<Dehadasha> = emptyList()
    ) {
        val durationHours: Double
            get() = durationMinutes / 60.0

        val durationDays: Double
            get() = durationMinutes / (60.0 * 24.0)

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getActiveDehadasha(): Dehadasha? = getDehadashaOn(LocalDate.now())

        fun getDehadashaOn(date: LocalDate): Dehadasha? {
            return dehadashas.find { it.isActiveOn(date) }
        }

        fun getDurationString(): String {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            return when {
                hours >= 24 -> {
                    val days = hours / 24
                    val remainingHours = hours % 24
                    "${days}d ${remainingHours}h ${mins}m"
                }
                hours > 0 -> "${hours}h ${mins}m"
                else -> "${mins}m"
            }
        }
    }

    data class Dehadasha(
        val planet: Planet,
        val pranadashaPlanet: Planet,
        val sookshmadashaPlanet: Planet,
        val pratyantardashaPlanet: Planet,
        val antardashaPlanet: Planet,
        val mahadashaPlanet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationMinutes: Long
    ) {
        val durationHours: Double
            get() = durationMinutes / 60.0

        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())

        fun getDurationString(): String {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        }
    }

    data class DashaSandhi(
        val fromPlanet: Planet,
        val toPlanet: Planet,
        val transitionDate: LocalDate,
        val level: DashaLevel,
        val sandhiStartDate: LocalDate,
        val sandhiEndDate: LocalDate
    ) {
        fun isWithinSandhi(date: LocalDate): Boolean {
            return !date.isBefore(sandhiStartDate) && !date.isAfter(sandhiEndDate)
        }
    }

    enum class DashaLevel(val displayName: String, val levelNumber: Int) {
        MAHADASHA("Mahadasha", 1),
        ANTARDASHA("Antardasha/Bhukti", 2),
        PRATYANTARDASHA("Pratyantardasha", 3),
        SOOKSHMADASHA("Sookshmadasha", 4),
        PRANADASHA("Pranadasha", 5),
        DEHADASHA("Dehadasha", 6)
    }

    data class DashaTimeline(
        val birthDate: LocalDate,
        val birthNakshatra: Nakshatra,
        val birthNakshatraPada: Int,
        val birthNakshatraLord: Planet,
        val nakshatraProgress: Double,
        val balanceOfFirstDasha: Double,
        val mahadashas: List<Mahadasha>,
        val currentMahadasha: Mahadasha?,
        val currentAntardasha: Antardasha?,
        val currentPratyantardasha: Pratyantardasha?,
        val currentSookshmadasha: Sookshmadasha?,
        val currentPranadasha: Pranadasha?,
        val currentDehadasha: Dehadasha?,
        val upcomingSandhis: List<DashaSandhi>
    ) {
        fun getCurrentPeriodDescription(): String {
            return buildString {
                currentMahadasha?.let { md ->
                    append("${md.planet.displayName} Mahadasha")
                    currentAntardasha?.let { ad ->
                        append(" → ${ad.planet.displayName} Bhukti")
                        currentPratyantardasha?.let { pd ->
                            append(" → ${pd.planet.displayName} Pratyantar")
                            currentSookshmadasha?.let { sd ->
                                append(" → ${sd.planet.displayName} Sookshma")
                            }
                        }
                    }
                } ?: append("No active Dasha period")
            }
        }

        fun getFullPeriodDescription(): String {
            return buildString {
                currentMahadasha?.let { md ->
                    append("${md.planet.displayName} Mahadasha")
                    currentAntardasha?.let { ad ->
                        append(" → ${ad.planet.displayName} Bhukti")
                        currentPratyantardasha?.let { pd ->
                            append(" → ${pd.planet.displayName} Pratyantar")
                            currentSookshmadasha?.let { sd ->
                                append(" → ${sd.planet.displayName} Sookshma")
                                currentPranadasha?.let { prd ->
                                    append(" → ${prd.planet.displayName} Prana")
                                    currentDehadasha?.let { dd ->
                                        append(" → ${dd.planet.displayName} Deha")
                                    }
                                }
                            }
                        }
                    }
                } ?: append("No active Dasha period")
            }
        }

        fun getShortDescription(): String {
            return buildString {
                currentMahadasha?.let { md ->
                    append(md.planet.symbol)
                    currentAntardasha?.let { ad ->
                        append("-${ad.planet.symbol}")
                        currentPratyantardasha?.let { pd ->
                            append("-${pd.planet.symbol}")
                        }
                    }
                } ?: append("--")
            }
        }

        fun getFullShortDescription(): String {
            return buildString {
                currentMahadasha?.let { md ->
                    append(md.planet.symbol)
                    currentAntardasha?.let { ad ->
                        append("-${ad.planet.symbol}")
                        currentPratyantardasha?.let { pd ->
                            append("-${pd.planet.symbol}")
                            currentSookshmadasha?.let { sd ->
                                append("-${sd.planet.symbol}")
                                currentPranadasha?.let { prd ->
                                    append("-${prd.planet.symbol}")
                                    currentDehadasha?.let { dd ->
                                        append("-${dd.planet.symbol}")
                                    }
                                }
                            }
                        }
                    }
                } ?: append("--")
            }
        }

        fun getDashaAtDate(date: LocalDate): DashaPeriodInfo {
            val mahadasha = mahadashas.find { it.isActiveOn(date) }
            val antardasha = mahadasha?.getAntardashaOn(date)
            val pratyantardasha = antardasha?.getPratyantardashaOn(date)
            val sookshmadasha = pratyantardasha?.getSookshmadashaOn(date)
            val pranadasha = sookshmadasha?.getPranadashaOn(date)
            val dehadasha = pranadasha?.getDehadashaOn(date)

            return DashaPeriodInfo(
                date = date,
                mahadasha = mahadasha,
                antardasha = antardasha,
                pratyantardasha = pratyantardasha,
                sookshmadasha = sookshmadasha,
                pranadasha = pranadasha,
                dehadasha = dehadasha
            )
        }

        fun getNextMahadasha(): Mahadasha? {
            val today = LocalDate.now()
            return mahadashas.find { it.startDate.isAfter(today) }
        }

        fun getUpcomingSandhisWithin(days: Int): List<DashaSandhi> {
            val today = LocalDate.now()
            val futureDate = today.plusDays(days.toLong())
            return upcomingSandhis.filter { sandhi ->
                !sandhi.transitionDate.isBefore(today) && !sandhi.transitionDate.isAfter(futureDate)
            }
        }

        fun getActiveDashaLords(): List<Pair<DashaLevel, Planet>> {
            val lords = mutableListOf<Pair<DashaLevel, Planet>>()
            currentMahadasha?.let { lords.add(DashaLevel.MAHADASHA to it.planet) }
            currentAntardasha?.let { lords.add(DashaLevel.ANTARDASHA to it.planet) }
            currentPratyantardasha?.let { lords.add(DashaLevel.PRATYANTARDASHA to it.planet) }
            currentSookshmadasha?.let { lords.add(DashaLevel.SOOKSHMADASHA to it.planet) }
            currentPranadasha?.let { lords.add(DashaLevel.PRANADASHA to it.planet) }
            currentDehadasha?.let { lords.add(DashaLevel.DEHADASHA to it.planet) }
            return lords
        }
    }

    data class DashaPeriodInfo(
        val date: LocalDate,
        val mahadasha: Mahadasha?,
        val antardasha: Antardasha?,
        val pratyantardasha: Pratyantardasha?,
        val sookshmadasha: Sookshmadasha?,
        val pranadasha: Pranadasha? = null,
        val dehadasha: Dehadasha? = null
    ) {
        fun getAllLords(): List<Planet> {
            return listOfNotNull(
                mahadasha?.planet,
                antardasha?.planet,
                pratyantardasha?.planet,
                sookshmadasha?.planet,
                pranadasha?.planet,
                dehadasha?.planet
            )
        }

        fun getCombinedPeriodString(): String {
            return getAllLords().joinToString("-") { it.displayName }
        }
    }

    fun calculateDashaTimeline(chart: VedicChart): DashaTimeline {
        val birthDate = chart.birthData.dateTime.toLocalDate()
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found in chart.")

        val moonLongitude = moonPosition.longitude
        val (birthNakshatra, pada) = Nakshatra.fromLongitude(moonLongitude)
        val nakshatraLord = birthNakshatra.ruler

        val moonLongitudeBd = BigDecimal(moonLongitude.toString())
        val nakshatraStartBd = BigDecimal(birthNakshatra.startDegree.toString())

        var degreesIntoNakshatra = moonLongitudeBd.subtract(nakshatraStartBd, MATH_CONTEXT)
            .remainder(NAKSHATRA_SPAN_BD, MATH_CONTEXT)

        if (degreesIntoNakshatra < BigDecimal.ZERO) {
            degreesIntoNakshatra = degreesIntoNakshatra.add(NAKSHATRA_SPAN_BD, MATH_CONTEXT)
        }

        val nakshatraProgressBd = degreesIntoNakshatra.divide(NAKSHATRA_SPAN_BD, MATH_CONTEXT)
        val nakshatraProgress = nakshatraProgressBd.toDouble().coerceIn(0.0, 1.0)

        val firstDashaYearsBd = DASHA_YEARS[nakshatraLord] ?: BigDecimal.ZERO
        val elapsedInFirstDashaBd = nakshatraProgressBd.multiply(firstDashaYearsBd, MATH_CONTEXT)
        val balanceOfFirstDashaBd = firstDashaYearsBd.subtract(elapsedInFirstDashaBd, MATH_CONTEXT)
        val balanceOfFirstDasha = balanceOfFirstDashaBd.toDouble().coerceAtLeast(0.0)

        val today = LocalDate.now()

        val mahadashas = calculateAllMahadashasOptimized(
            birthDate = birthDate,
            startingDashaLord = nakshatraLord,
            balanceOfFirstDashaBd = balanceOfFirstDashaBd,
            targetDate = today
        )

        val currentMahadasha = mahadashas.find { it.isActiveOn(today) }
        val currentAntardasha = currentMahadasha?.getAntardashaOn(today)

        var currentPratyantardasha: Pratyantardasha? = null
        var currentSookshmadasha: Sookshmadasha? = null
        var currentPranadasha: Pranadasha? = null
        var currentDehadasha: Dehadasha? = null

        if (currentAntardasha != null) {
            val pratyantardashas = calculatePratyantardashasForAntardasha(currentAntardasha)
            currentPratyantardasha = pratyantardashas.find { it.isActiveOn(today) }

            if (currentPratyantardasha != null) {
                val sookshmadashas = calculateSookshmadashasForPratyantardasha(currentPratyantardasha)
                currentSookshmadasha = sookshmadashas.find { it.isActiveOn(today) }

                if (currentSookshmadasha != null) {
                    val pranadashas = calculatePranadashasForSookshmadasha(currentSookshmadasha)
                    currentPranadasha = pranadashas.find { it.isActiveOn(today) }

                    if (currentPranadasha != null) {
                        val dehadashas = calculateDehadashasForPranadasha(currentPranadasha)
                        currentDehadasha = dehadashas.find { it.isActiveOn(today) }
                    }
                }
            }
        }

        val upcomingSandhis = calculateUpcomingSandhis(mahadashas, today, lookAheadDays = 365)

        return DashaTimeline(
            birthDate = birthDate,
            birthNakshatra = birthNakshatra,
            birthNakshatraPada = pada,
            birthNakshatraLord = nakshatraLord,
            nakshatraProgress = nakshatraProgress,
            balanceOfFirstDasha = balanceOfFirstDasha,
            mahadashas = mahadashas,
            currentMahadasha = currentMahadasha,
            currentAntardasha = currentAntardasha,
            currentPratyantardasha = currentPratyantardasha,
            currentSookshmadasha = currentSookshmadasha,
            currentPranadasha = currentPranadasha,
            currentDehadasha = currentDehadasha,
            upcomingSandhis = upcomingSandhis
        )
    }

    private fun calculateAllMahadashasOptimized(
        birthDate: LocalDate,
        startingDashaLord: Planet,
        balanceOfFirstDashaBd: BigDecimal,
        targetDate: LocalDate
    ): List<Mahadasha> {
        val mahadashas = mutableListOf<Mahadasha>()
        var currentStartDate = birthDate

        val startIndex = DASHA_SEQUENCE.indexOf(startingDashaLord)
        if (startIndex == -1) {
            throw IllegalArgumentException("Invalid starting dasha lord: $startingDashaLord")
        }

        val firstDashaDays = yearsToRoundedDays(balanceOfFirstDashaBd)
        val firstDashaEndDate = currentStartDate.plusDays(firstDashaDays)
        val isFirstActive = !targetDate.isBefore(currentStartDate) && !targetDate.isAfter(firstDashaEndDate)

        val firstAntardashas = calculateAntardashasOptimized(
            mahadashaPlanet = startingDashaLord,
            mahadashaStart = currentStartDate,
            mahadashaEnd = firstDashaEndDate,
            mahadashaDurationYearsBd = balanceOfFirstDashaBd,
            isCurrentMahadasha = isFirstActive,
            targetDate = targetDate
        )

        mahadashas.add(
            Mahadasha(
                planet = startingDashaLord,
                startDate = currentStartDate,
                endDate = firstDashaEndDate,
                durationYears = balanceOfFirstDashaBd.toDouble(),
                antardashas = firstAntardashas
            )
        )
        currentStartDate = firstDashaEndDate

        repeat(MAX_MAHADASHAS - 1) { cycle ->
            val planetIndex = (startIndex + 1 + cycle) % DASHA_SEQUENCE.size
            val planet = DASHA_SEQUENCE[planetIndex]
            val dashaYearsBd = DASHA_YEARS[planet] ?: BigDecimal.ZERO
            val dashaDays = yearsToRoundedDays(dashaYearsBd)
            val endDate = currentStartDate.plusDays(dashaDays)
            val isActive = !targetDate.isBefore(currentStartDate) && !targetDate.isAfter(endDate)

            val antardashas = calculateAntardashasOptimized(
                mahadashaPlanet = planet,
                mahadashaStart = currentStartDate,
                mahadashaEnd = endDate,
                mahadashaDurationYearsBd = dashaYearsBd,
                isCurrentMahadasha = isActive,
                targetDate = targetDate
            )

            mahadashas.add(
                Mahadasha(
                    planet = planet,
                    startDate = currentStartDate,
                    endDate = endDate,
                    durationYears = dashaYearsBd.toDouble(),
                    antardashas = antardashas
                )
            )
            currentStartDate = endDate
        }

        return mahadashas
    }

    private fun calculateAntardashasOptimized(
        mahadashaPlanet: Planet,
        mahadashaStart: LocalDate,
        mahadashaEnd: LocalDate,
        mahadashaDurationYearsBd: BigDecimal,
        isCurrentMahadasha: Boolean,
        targetDate: LocalDate
    ): List<Antardasha> {
        val antardashas = mutableListOf<Antardasha>()
        var currentStart = mahadashaStart

        val startIndex = DASHA_SEQUENCE.indexOf(mahadashaPlanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val antarPlanet = DASHA_SEQUENCE[planetIndex]

            val antarYearsBd = DASHA_YEARS[antarPlanet] ?: BigDecimal.ZERO
            val proportionalDurationBd = antarYearsBd
                .divide(TOTAL_CYCLE_YEARS_BD, MATH_CONTEXT)
                .multiply(mahadashaDurationYearsBd, MATH_CONTEXT)

            val antarDays = yearsToRoundedDays(proportionalDurationBd)
            val antarEnd = currentStart.plusDays(antarDays)

            val pratyantardashas = if (isCurrentMahadasha && 
                !targetDate.isBefore(currentStart) && !targetDate.isAfter(antarEnd)) {
                calculatePratyantardashasInternal(
                    mahadashaPlanet = mahadashaPlanet,
                    antardashaPlanet = antarPlanet,
                    antarStart = currentStart,
                    antarEnd = antarEnd,
                    antarDurationYearsBd = proportionalDurationBd
                )
            } else {
                emptyList()
            }

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

    fun calculatePratyantardashasForAntardasha(antardasha: Antardasha): List<Pratyantardasha> {
        if (antardasha.pratyantardashas.isNotEmpty()) {
            return antardasha.pratyantardashas
        }

        val antarDurationYearsBd = BigDecimal(antardasha.durationYears.toString())
        return calculatePratyantardashasInternal(
            mahadashaPlanet = antardasha.mahadashaPlanet,
            antardashaPlanet = antardasha.planet,
            antarStart = antardasha.startDate,
            antarEnd = antardasha.endDate,
            antarDurationYearsBd = antarDurationYearsBd
        )
    }

    private fun calculatePratyantardashasInternal(
        mahadashaPlanet: Planet,
        antardashaPlanet: Planet,
        antarStart: LocalDate,
        antarEnd: LocalDate,
        antarDurationYearsBd: BigDecimal
    ): List<Pratyantardasha> {
        val pratyantardashas = mutableListOf<Pratyantardasha>()
        var currentStart = antarStart

        val startIndex = DASHA_SEQUENCE.indexOf(antardashaPlanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val pratyantarPlanet = DASHA_SEQUENCE[planetIndex]

            val pratyantarYearsBd = DASHA_YEARS[pratyantarPlanet] ?: BigDecimal.ZERO
            val proportionalDurationBd = pratyantarYearsBd
                .divide(TOTAL_CYCLE_YEARS_BD, MATH_CONTEXT)
                .multiply(antarDurationYearsBd, MATH_CONTEXT)

            val pratyantarDays = yearsToRoundedDays(proportionalDurationBd)
            val pratyantarEnd = currentStart.plusDays(pratyantarDays)

            pratyantardashas.add(
                Pratyantardasha(
                    planet = pratyantarPlanet,
                    antardashaPlanet = antardashaPlanet,
                    mahadashaPlanet = mahadashaPlanet,
                    startDate = currentStart,
                    endDate = pratyantarEnd,
                    durationDays = pratyantarDays,
                    sookshmadashas = emptyList()
                )
            )
            currentStart = pratyantarEnd
        }

        return pratyantardashas
    }

    fun calculateSookshmadashasForPratyantardasha(pratyantardasha: Pratyantardasha): List<Sookshmadasha> {
        if (pratyantardasha.sookshmadashas.isNotEmpty()) {
            return pratyantardasha.sookshmadashas
        }

        val pratyantarDurationYearsBd = BigDecimal(pratyantardasha.durationYears.toString())
        return calculateSookshmadashasInternal(
            mahadashaPlanet = pratyantardasha.mahadashaPlanet,
            antardashaPlanet = pratyantardasha.antardashaPlanet,
            pratyantardashaPlanet = pratyantardasha.planet,
            pratyantarStart = pratyantardasha.startDate,
            pratyantarEnd = pratyantardasha.endDate,
            pratyantarDurationYearsBd = pratyantarDurationYearsBd
        )
    }

    private fun calculateSookshmadashasInternal(
        mahadashaPlanet: Planet,
        antardashaPlanet: Planet,
        pratyantardashaPlanet: Planet,
        pratyantarStart: LocalDate,
        pratyantarEnd: LocalDate,
        pratyantarDurationYearsBd: BigDecimal
    ): List<Sookshmadasha> {
        val sookshmadashas = mutableListOf<Sookshmadasha>()
        var currentStart = pratyantarStart

        val startIndex = DASHA_SEQUENCE.indexOf(pratyantardashaPlanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val sookshmaPlanet = DASHA_SEQUENCE[planetIndex]

            val sookshmaYearsBd = DASHA_YEARS[sookshmaPlanet] ?: BigDecimal.ZERO
            val proportionalDurationBd = sookshmaYearsBd
                .divide(TOTAL_CYCLE_YEARS_BD, MATH_CONTEXT)
                .multiply(pratyantarDurationYearsBd, MATH_CONTEXT)

            val sookshmaDays = yearsToRoundedDays(proportionalDurationBd).coerceAtLeast(1L)
            val sookshmaEnd = currentStart.plusDays(sookshmaDays)

            sookshmadashas.add(
                Sookshmadasha(
                    planet = sookshmaPlanet,
                    pratyantardashaPlanet = pratyantardashaPlanet,
                    antardashaPlanet = antardashaPlanet,
                    mahadashaPlanet = mahadashaPlanet,
                    startDate = currentStart,
                    endDate = sookshmaEnd,
                    durationDays = sookshmaDays,
                    pranadashas = emptyList()
                )
            )
            currentStart = sookshmaEnd
        }

        return sookshmadashas
    }

    fun calculatePranadashasForSookshmadasha(sookshmadasha: Sookshmadasha): List<Pranadasha> {
        if (sookshmadasha.pranadashas.isNotEmpty()) {
            return sookshmadasha.pranadashas
        }

        val sookshmaDurationMinutes = sookshmadasha.durationDays * 24 * 60
        return calculatePranadashasInternal(
            mahadashaPlanet = sookshmadasha.mahadashaPlanet,
            antardashaPlanet = sookshmadasha.antardashaPlanet,
            pratyantardashaPlanet = sookshmadasha.pratyantardashaPlanet,
            sookshmadashaPlanet = sookshmadasha.planet,
            sookshmaStart = sookshmadasha.startDate,
            sookshmaEnd = sookshmadasha.endDate,
            sookshmaDurationMinutes = sookshmaDurationMinutes
        )
    }

    private fun calculatePranadashasInternal(
        mahadashaPlanet: Planet,
        antardashaPlanet: Planet,
        pratyantardashaPlanet: Planet,
        sookshmadashaPlanet: Planet,
        sookshmaStart: LocalDate,
        sookshmaEnd: LocalDate,
        sookshmaDurationMinutes: Long
    ): List<Pranadasha> {
        val pranadashas = mutableListOf<Pranadasha>()
        var currentStart = sookshmaStart

        val startIndex = DASHA_SEQUENCE.indexOf(sookshmadashaPlanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val pranaPlanet = DASHA_SEQUENCE[planetIndex]

            val pranaYearsBd = DASHA_YEARS[pranaPlanet] ?: BigDecimal.ZERO
            val proportionalMinutes = pranaYearsBd
                .divide(TOTAL_CYCLE_YEARS_BD, MATH_CONTEXT)
                .multiply(BigDecimal(sookshmaDurationMinutes), MATH_CONTEXT)
                .toLong()
                .coerceAtLeast(1L)

            val pranaDays = (proportionalMinutes / (24 * 60)).coerceAtLeast(1L)
            val pranaEnd = currentStart.plusDays(pranaDays)

            pranadashas.add(
                Pranadasha(
                    planet = pranaPlanet,
                    sookshmadashaPlanet = sookshmadashaPlanet,
                    pratyantardashaPlanet = pratyantardashaPlanet,
                    antardashaPlanet = antardashaPlanet,
                    mahadashaPlanet = mahadashaPlanet,
                    startDate = currentStart,
                    endDate = pranaEnd,
                    durationMinutes = proportionalMinutes,
                    dehadashas = emptyList()
                )
            )
            currentStart = pranaEnd
        }

        return pranadashas
    }

    fun calculateDehadashasForPranadasha(pranadasha: Pranadasha): List<Dehadasha> {
        if (pranadasha.dehadashas.isNotEmpty()) {
            return pranadasha.dehadashas
        }

        return calculateDehadashasInternal(
            mahadashaPlanet = pranadasha.mahadashaPlanet,
            antardashaPlanet = pranadasha.antardashaPlanet,
            pratyantardashaPlanet = pranadasha.pratyantardashaPlanet,
            sookshmadashaPlanet = pranadasha.sookshmadashaPlanet,
            pranadashaPlanet = pranadasha.planet,
            pranaStart = pranadasha.startDate,
            pranaEnd = pranadasha.endDate,
            pranaDurationMinutes = pranadasha.durationMinutes
        )
    }

    private fun calculateDehadashasInternal(
        mahadashaPlanet: Planet,
        antardashaPlanet: Planet,
        pratyantardashaPlanet: Planet,
        sookshmadashaPlanet: Planet,
        pranadashaPlanet: Planet,
        pranaStart: LocalDate,
        pranaEnd: LocalDate,
        pranaDurationMinutes: Long
    ): List<Dehadasha> {
        val dehadashas = mutableListOf<Dehadasha>()
        var currentStart = pranaStart

        val startIndex = DASHA_SEQUENCE.indexOf(pranadashaPlanet)

        for (i in 0 until 9) {
            val planetIndex = (startIndex + i) % DASHA_SEQUENCE.size
            val dehaPlanet = DASHA_SEQUENCE[planetIndex]

            val dehaYearsBd = DASHA_YEARS[dehaPlanet] ?: BigDecimal.ZERO
            val proportionalMinutes = dehaYearsBd
                .divide(TOTAL_CYCLE_YEARS_BD, MATH_CONTEXT)
                .multiply(BigDecimal(pranaDurationMinutes), MATH_CONTEXT)
                .toLong()
                .coerceAtLeast(1L)

            val dehaDays = (proportionalMinutes / (24 * 60)).coerceAtLeast(1L)
            val dehaEnd = currentStart.plusDays(dehaDays)

            dehadashas.add(
                Dehadasha(
                    planet = dehaPlanet,
                    pranadashaPlanet = pranadashaPlanet,
                    sookshmadashaPlanet = sookshmadashaPlanet,
                    pratyantardashaPlanet = pratyantardashaPlanet,
                    antardashaPlanet = antardashaPlanet,
                    mahadashaPlanet = mahadashaPlanet,
                    startDate = currentStart,
                    endDate = dehaEnd,
                    durationMinutes = proportionalMinutes
                )
            )
            currentStart = dehaEnd
        }

        return dehadashas
    }

    private fun calculateUpcomingSandhis(
        mahadashas: List<Mahadasha>,
        fromDate: LocalDate,
        lookAheadDays: Int
    ): List<DashaSandhi> {
        val sandhis = mutableListOf<DashaSandhi>()
        val endDate = fromDate.plusDays(lookAheadDays.toLong())

        for (i in 0 until mahadashas.size - 1) {
            val currentMd = mahadashas[i]
            val nextMd = mahadashas[i + 1]

            if (currentMd.endDate.isAfter(fromDate) && currentMd.endDate.isBefore(endDate)) {
                val sandhiDays = calculateSandhiDuration(DashaLevel.MAHADASHA, currentMd.durationYears)
                sandhis.add(
                    DashaSandhi(
                        fromPlanet = currentMd.planet,
                        toPlanet = nextMd.planet,
                        transitionDate = currentMd.endDate,
                        level = DashaLevel.MAHADASHA,
                        sandhiStartDate = currentMd.endDate.minusDays(sandhiDays / 2),
                        sandhiEndDate = currentMd.endDate.plusDays(sandhiDays / 2)
                    )
                )
            }

            if (currentMd.isActiveOn(fromDate) || 
                (currentMd.startDate.isAfter(fromDate) && currentMd.startDate.isBefore(endDate))) {
                for (j in 0 until currentMd.antardashas.size - 1) {
                    val currentAd = currentMd.antardashas[j]
                    val nextAd = currentMd.antardashas[j + 1]

                    if (currentAd.endDate.isAfter(fromDate) && currentAd.endDate.isBefore(endDate)) {
                        val sandhiDays = calculateSandhiDuration(DashaLevel.ANTARDASHA, currentAd.durationYears)
                        sandhis.add(
                            DashaSandhi(
                                fromPlanet = currentAd.planet,
                                toPlanet = nextAd.planet,
                                transitionDate = currentAd.endDate,
                                level = DashaLevel.ANTARDASHA,
                                sandhiStartDate = currentAd.endDate.minusDays(sandhiDays / 2),
                                sandhiEndDate = currentAd.endDate.plusDays(sandhiDays / 2)
                            )
                        )
                    }
                }
            }
        }

        return sandhis.sortedBy { it.transitionDate }
    }

    private fun calculateSandhiDuration(level: DashaLevel, periodDurationYears: Double): Long {
        val sandhiPercentage = when (level) {
            DashaLevel.MAHADASHA -> 0.05
            DashaLevel.ANTARDASHA -> 0.10
            DashaLevel.PRATYANTARDASHA -> 0.15
            DashaLevel.SOOKSHMADASHA -> 0.20
            DashaLevel.PRANADASHA -> 0.20
            DashaLevel.DEHADASHA -> 0.20
        }

        val sandhiYears = periodDurationYears * sandhiPercentage
        return yearsToRoundedDays(sandhiYears).coerceIn(1L, 30L)
    }

    fun getDashaAtDate(timeline: DashaTimeline, date: LocalDate): DashaPeriodInfo {
        return timeline.getDashaAtDate(date)
    }

    fun formatDashaPeriod(mahadasha: Mahadasha): String {
        return buildString {
            appendLine("${mahadasha.planet.displayName} Mahadasha")
            appendLine("Duration: ${String.format("%.2f", mahadasha.durationYears)} years")
            appendLine("Period: ${mahadasha.startDate} to ${mahadasha.endDate}")
            if (mahadasha.isActive) {
                appendLine("Status: CURRENTLY ACTIVE")
                appendLine("Progress: ${String.format("%.1f", mahadasha.getProgressPercent())}%")
                appendLine("Remaining: ${String.format("%.2f", mahadasha.getRemainingYears())} years")
            }
        }
    }

    fun formatCurrentPeriod(timeline: DashaTimeline): String {
        return buildString {
            timeline.currentMahadasha?.let { md ->
                appendLine("Mahadasha: ${md.planet.displayName}")
                appendLine("  Progress: ${String.format("%.1f", md.getProgressPercent())}%")
                appendLine("  Remaining: ${String.format("%.1f", md.getRemainingYears())} years")

                timeline.currentAntardasha?.let { ad ->
                    appendLine("\nAntardasha: ${ad.planet.displayName}")
                    appendLine("  Progress: ${String.format("%.1f", ad.getProgressPercent())}%")
                    appendLine("  Remaining: ${ad.getRemainingDays()} days")

                    timeline.currentPratyantardasha?.let { pd ->
                        appendLine("\nPratyantardasha: ${pd.planet.displayName}")
                        appendLine("  Period: ${pd.startDate} to ${pd.endDate}")

                        timeline.currentSookshmadasha?.let { sd ->
                            appendLine("\nSookshmadasha: ${sd.planet.displayName}")
                            appendLine("  Period: ${sd.startDate} to ${sd.endDate}")
                            appendLine("  Duration: ${sd.durationDays} days")

                            timeline.currentPranadasha?.let { prd ->
                                appendLine("\nPranadasha: ${prd.planet.displayName}")
                                appendLine("  Period: ${prd.startDate} to ${prd.endDate}")
                                appendLine("  Duration: ${prd.getDurationString()}")

                                timeline.currentDehadasha?.let { dd ->
                                    appendLine("\nDehadasha: ${dd.planet.displayName}")
                                    appendLine("  Period: ${dd.startDate} to ${dd.endDate}")
                                    appendLine("  Duration: ${dd.getDurationString()}")
                                }
                            }
                        }
                    }
                }
            } ?: appendLine("No active Dasha period found")
        }
    }
}

object ConditionalDashaCalculator {

    data class YoginiDasha(
        val yogini: Yogini,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationYears: Double
    ) {
        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())
    }

    enum class Yogini(
        val displayName: String,
        val deity: String,
        val planet: Planet,
        val years: Int,
        val nature: YoginiNature
    ) {
        MANGALA("Mangala", "Chandra (Moon)", Planet.MOON, 1, YoginiNature.BENEFIC),
        PINGALA("Pingala", "Surya (Sun)", Planet.SUN, 2, YoginiNature.MIXED),
        DHANYA("Dhanya", "Guru (Jupiter)", Planet.JUPITER, 3, YoginiNature.BENEFIC),
        BHRAMARI("Bhramari", "Mangal (Mars)", Planet.MARS, 4, YoginiNature.MALEFIC),
        BHADRIKA("Bhadrika", "Budha (Mercury)", Planet.MERCURY, 5, YoginiNature.BENEFIC),
        ULKA("Ulka", "Shani (Saturn)", Planet.SATURN, 6, YoginiNature.MALEFIC),
        SIDDHA("Siddha", "Shukra (Venus)", Planet.VENUS, 7, YoginiNature.BENEFIC),
        SANKATA("Sankata", "Rahu", Planet.RAHU, 8, YoginiNature.MALEFIC)
    }

    enum class YoginiNature {
        BENEFIC, MALEFIC, MIXED
    }

    private const val YOGINI_CYCLE_YEARS = 36
    private val NAKSHATRA_SPAN = 360.0 / 27.0

    fun calculateYoginiDasha(chart: VedicChart): List<YoginiDasha> {
        val birthDate = chart.birthData.dateTime.toLocalDate()
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Moon position not found")

        val moonLongitude = moonPosition.longitude
        val (nakshatra, _) = Nakshatra.fromLongitude(moonLongitude)

        val yoginiIndex = ((nakshatra.number - 1 + 3) % Yogini.entries.size)
        val startingYogini = Yogini.entries[yoginiIndex]

        val nakshatraStart = nakshatra.startDegree
        val progressInNakshatra = ((moonLongitude - nakshatraStart) / NAKSHATRA_SPAN).coerceIn(0.0, 1.0)

        val yoginis = mutableListOf<YoginiDasha>()
        var currentStart = birthDate

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

    fun getCurrentYoginiDasha(yoginiDashas: List<YoginiDasha>): YoginiDasha? {
        return yoginiDashas.find { it.isActive }
    }

    data class AshtottariDasha(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val durationYears: Double
    ) {
        fun isActiveOn(date: LocalDate): Boolean {
            return !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val isActive: Boolean
            get() = isActiveOn(LocalDate.now())
    }

    private val ASHTOTTARI_YEARS: Map<Planet, Int> = mapOf(
        Planet.SUN to 6,
        Planet.MOON to 15,
        Planet.MARS to 8,
        Planet.MERCURY to 17,
        Planet.SATURN to 10,
        Planet.JUPITER to 19,
        Planet.RAHU to 12,
        Planet.VENUS to 21
    )

    private val ASHTOTTARI_SEQUENCE = listOf(
        Planet.SUN,
        Planet.MOON,
        Planet.MARS,
        Planet.MERCURY,
        Planet.SATURN,
        Planet.JUPITER,
        Planet.RAHU,
        Planet.VENUS
    )

    private const val ASHTOTTARI_CYCLE_YEARS = 108

    private val ASHTOTTARI_NAKSHATRA_LORDS: Map<Int, Planet> = mapOf(
        1 to Planet.SUN,
        2 to Planet.MOON,
        3 to Planet.MARS,
        4 to Planet.MERCURY,
        5 to Planet.SATURN,
        6 to Planet.JUPITER,
        7 to Planet.RAHU,
        8 to Planet.VENUS
    )

    fun shouldApplyAshtottari(chart: VedicChart): Boolean {
        if (chart.planetPositions.isEmpty()) return false

        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        val lagnaLord = ascendantSign.ruler
        val lagnaLordPosition = chart.planetPositions.find { it.planet == lagnaLord }
        val rahuPosition = chart.planetPositions.find { it.planet == Planet.RAHU }

        if (lagnaLordPosition == null || rahuPosition == null) return false

        val lagnaLordSign = ZodiacSign.fromLongitude(lagnaLordPosition.longitude)
        val rahuSign = ZodiacSign.fromLongitude(rahuPosition.longitude)

        val houseDistance = ((rahuSign.ordinal - lagnaLordSign.ordinal + 12) % 12) + 1

        return houseDistance in listOf(1, 4, 5, 7, 9, 10)
    }

    fun calculateAshtottariDasha(chart: VedicChart): List<AshtottariDasha>? {
        if (!shouldApplyAshtottari(chart)) return null

        val birthDate = chart.birthData.dateTime.toLocalDate()
        val moonPosition = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: return null

        val moonLongitude = moonPosition.longitude
        val (nakshatra, _) = Nakshatra.fromLongitude(moonLongitude)

        val nakshatraGroup = ((nakshatra.number - 6 + 27) % 27) / 3
        val startingLord = ASHTOTTARI_NAKSHATRA_LORDS[nakshatraGroup % 8 + 1] ?: Planet.SUN

        val nakshatraStart = nakshatra.startDegree
        val nakshatraSpan = 360.0 / 27.0
        val progressInNakshatra = ((moonLongitude - nakshatraStart) / nakshatraSpan).coerceIn(0.0, 1.0)

        val dashas = mutableListOf<AshtottariDasha>()
        var currentStart = birthDate

        val startIndex = ASHTOTTARI_SEQUENCE.indexOf(startingLord)
        val firstDashaYears = (ASHTOTTARI_YEARS[startingLord] ?: 0).toDouble()
        val balanceOfFirst = firstDashaYears * (1.0 - progressInNakshatra)

        val firstDays = yearsToRoundedDays(balanceOfFirst)
        val firstEnd = currentStart.plusDays(firstDays)

        dashas.add(
            AshtottariDasha(
                planet = startingLord,
                startDate = currentStart,
                endDate = firstEnd,
                durationYears = balanceOfFirst
            )
        )
        currentStart = firstEnd

        repeat(24) { cycle ->
            val planetIndex = (startIndex + 1 + cycle) % ASHTOTTARI_SEQUENCE.size
            val planet = ASHTOTTARI_SEQUENCE[planetIndex]
            val years = (ASHTOTTARI_YEARS[planet] ?: 0).toDouble()
            val days = yearsToRoundedDays(years)
            val endDate = currentStart.plusDays(days)

            dashas.add(
                AshtottariDasha(
                    planet = planet,
                    startDate = currentStart,
                    endDate = endDate,
                    durationYears = years
                )
            )
            currentStart = endDate
        }

        return dashas
    }
}