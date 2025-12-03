package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * Retrograde Motion and Combustion Calculator
 *
 * This calculator provides precise interval tables for:
 *
 * 1. RETROGRADE MOTION:
 *    - Planets appear to move backward in the sky due to relative orbital speeds
 *    - In Vedic astrology, retrograde planets (vakri grahas) have special significance
 *    - Effects vary by planet and house placement
 *
 * 2. COMBUSTION (ASTA):
 *    - Planets too close to the Sun become "combust" (invisible/weakened)
 *    - Each planet has specific combustion degrees:
 *      - Moon: 12° (invisible) to 17° (weak)
 *      - Mars: 17°
 *      - Mercury: 14° (direct), 12° (retrograde)
 *      - Jupiter: 11°
 *      - Venus: 10° (direct), 8° (retrograde)
 *      - Saturn: 15°
 *
 * 3. PLANETARY WAR (GRAHA YUDDHA):
 *    - When two planets are within 1° of each other (excluding Sun and Moon)
 *    - The planet with lower longitude loses the war
 */
object RetrogradeCombustionCalculator {

    /**
     * Combustion orbs for each planet (in degrees from Sun)
     */
    data class CombustionOrbs(
        val fullCombustion: Double,  // Planet is fully combust
        val partialCombustion: Double  // Planet is weakened but not fully combust
    )

    private val COMBUSTION_ORBS = mapOf(
        Planet.MOON to CombustionOrbs(12.0, 17.0),
        Planet.MARS to CombustionOrbs(17.0, 25.0),
        Planet.MERCURY to CombustionOrbs(14.0, 20.0),  // 12° when retrograde
        Planet.JUPITER to CombustionOrbs(11.0, 17.0),
        Planet.VENUS to CombustionOrbs(10.0, 16.0),    // 8° when retrograde
        Planet.SATURN to CombustionOrbs(15.0, 22.0)
    )

    /**
     * Retrograde status for a planet
     */
    enum class RetrogradeStatus(val displayName: String, val symbol: String) {
        DIRECT("Direct", "D"),
        RETROGRADE("Retrograde", "R"),
        STATIONARY_RETROGRADE("Stationary Retrograde", "SR"),
        STATIONARY_DIRECT("Stationary Direct", "SD")
    }

    /**
     * Combustion status for a planet
     */
    enum class CombustionStatus(val displayName: String, val symbol: String, val strength: Double) {
        NOT_COMBUST("Not Combust", "", 1.0),
        PARTIAL("Partial Combustion", "○", 0.5),
        FULL("Full Combustion", "●", 0.25),
        CAZIMI("Cazimi (Heart of Sun)", "☉", 1.2)  // Within 17' - actually strengthening
    }

    /**
     * Retrograde period data
     */
    data class RetrogradePeriod(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val stationaryRetroDate: LocalDate?,  // Date when planet stations to go retrograde
        val stationaryDirectDate: LocalDate?, // Date when planet stations to go direct
        val startLongitude: Double,
        val endLongitude: Double,
        val durationDays: Long
    )

    /**
     * Combustion period data
     */
    data class CombustionPeriod(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val peakDate: LocalDate,           // Date of closest approach to Sun
        val peakDistance: Double,          // Minimum distance from Sun
        val status: CombustionStatus,
        val durationDays: Long
    )

    /**
     * Complete retrograde/combustion analysis for a chart
     */
    data class PlanetaryConditionAnalysis(
        val chart: VedicChart,
        val planetConditions: List<PlanetCondition>,
        val currentRetrogrades: List<Planet>,
        val currentCombustions: List<Planet>,
        val upcomingRetrogradePeriods: List<RetrogradePeriod>,
        val upcomingCombustionPeriods: List<CombustionPeriod>,
        val planetaryWars: List<PlanetaryWar>
    )

    /**
     * Current condition of a planet
     */
    data class PlanetCondition(
        val planet: Planet,
        val retrogradeStatus: RetrogradeStatus,
        val combustionStatus: CombustionStatus,
        val distanceFromSun: Double?,        // Angular distance from Sun (null for Sun)
        val speed: Double,                   // Daily motion in degrees
        val isInPlanetaryWar: Boolean,
        val warOpponent: Planet?,
        val isWarWinner: Boolean?
    ) {
        val overallStrength: Double
            get() {
                var strength = combustionStatus.strength

                // Retrograde modifies strength (contextual in Vedic)
                when (retrogradeStatus) {
                    RetrogradeStatus.RETROGRADE -> strength *= 1.0  // Retrograde can be strong in Vedic
                    RetrogradeStatus.STATIONARY_RETROGRADE,
                    RetrogradeStatus.STATIONARY_DIRECT -> strength *= 1.2  // Stationary = powerful
                    else -> { }
                }

                // Planetary war
                if (isInPlanetaryWar && isWarWinner == false) {
                    strength *= 0.5
                }

                return strength
            }
    }

    /**
     * Planetary War data
     */
    data class PlanetaryWar(
        val planet1: Planet,
        val planet2: Planet,
        val angularSeparation: Double,
        val winner: Planet,
        val loser: Planet,
        val warType: String  // "Conjunction War"
    )

    /**
     * Calculate complete planetary condition analysis
     */
    fun analyzePlanetaryConditions(chart: VedicChart): PlanetaryConditionAnalysis {
        val planetConditions = mutableListOf<PlanetCondition>()
        val sunPosition = chart.planetPositions.find { it.planet == Planet.SUN }

        // Analyze each planet
        for (position in chart.planetPositions) {
            val condition = analyzeSinglePlanet(position, sunPosition, chart.planetPositions)
            planetConditions.add(condition)
        }

        // Detect planetary wars
        val planetaryWars = detectPlanetaryWars(chart.planetPositions)

        // Update conditions with war info
        val updatedConditions = planetConditions.map { condition ->
            val war = planetaryWars.find {
                it.planet1 == condition.planet || it.planet2 == condition.planet
            }
            if (war != null) {
                val opponent = if (war.planet1 == condition.planet) war.planet2 else war.planet1
                condition.copy(
                    isInPlanetaryWar = true,
                    warOpponent = opponent,
                    isWarWinner = war.winner == condition.planet
                )
            } else {
                condition
            }
        }

        return PlanetaryConditionAnalysis(
            chart = chart,
            planetConditions = updatedConditions,
            currentRetrogrades = updatedConditions.filter {
                it.retrogradeStatus == RetrogradeStatus.RETROGRADE ||
                        it.retrogradeStatus == RetrogradeStatus.STATIONARY_RETROGRADE
            }.map { it.planet },
            currentCombustions = updatedConditions.filter {
                it.combustionStatus == CombustionStatus.FULL ||
                        it.combustionStatus == CombustionStatus.PARTIAL
            }.map { it.planet },
            upcomingRetrogradePeriods = emptyList(),  // Would require ephemeris scanning
            upcomingCombustionPeriods = emptyList(),  // Would require ephemeris scanning
            planetaryWars = planetaryWars
        )
    }

    /**
     * Analyze a single planet's condition
     */
    private fun analyzeSinglePlanet(
        position: PlanetPosition,
        sunPosition: PlanetPosition?,
        allPositions: List<PlanetPosition>
    ): PlanetCondition {
        // Determine retrograde status
        val retrogradeStatus = getRetrogradeStatus(position.planet, position.speed, position.isRetrograde)

        // Calculate distance from Sun and combustion status
        val distanceFromSun = if (sunPosition != null && position.planet != Planet.SUN) {
            calculateAngularDistance(position.longitude, sunPosition.longitude)
        } else null

        val combustionStatus = if (distanceFromSun != null) {
            determineCombustionStatus(position.planet, distanceFromSun, position.isRetrograde)
        } else {
            CombustionStatus.NOT_COMBUST
        }

        return PlanetCondition(
            planet = position.planet,
            retrogradeStatus = retrogradeStatus,
            combustionStatus = combustionStatus,
            distanceFromSun = distanceFromSun,
            speed = position.speed,
            isInPlanetaryWar = false,
            warOpponent = null,
            isWarWinner = null
        )
    }

    /**
     * Determine combustion status based on distance from Sun
     */
    private fun determineCombustionStatus(
        planet: Planet,
        distanceFromSun: Double,
        isRetrograde: Boolean
    ): CombustionStatus {
        // Rahu, Ketu, and outer planets don't combust
        if (planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return CombustionStatus.NOT_COMBUST
        }

        val orbs = COMBUSTION_ORBS[planet] ?: return CombustionStatus.NOT_COMBUST

        // Adjust orbs for retrograde Mercury and Venus
        val effectiveFullOrb = when {
            planet == Planet.MERCURY && isRetrograde -> 12.0
            planet == Planet.VENUS && isRetrograde -> 8.0
            else -> orbs.fullCombustion
        }

        return when {
            // Cazimi - within 17 arcminutes (0.283°) of exact conjunction
            distanceFromSun <= 0.283 -> CombustionStatus.CAZIMI

            // Full combustion
            distanceFromSun <= effectiveFullOrb -> CombustionStatus.FULL

            // Partial combustion
            distanceFromSun <= orbs.partialCombustion -> CombustionStatus.PARTIAL

            else -> CombustionStatus.NOT_COMBUST
        }
    }

    /**
     * Detect planetary wars (Graha Yuddha)
     *
     * Occurs when two planets (excluding Sun and Moon) are within 1° of each other.
     * The planet with higher longitude wins (in sidereal zodiac).
     */
    private fun detectPlanetaryWars(positions: List<PlanetPosition>): List<PlanetaryWar> {
        val wars = mutableListOf<PlanetaryWar>()

        // Planets that can engage in war (not Sun, Moon, or nodes)
        val warCapablePlanets = positions.filter {
            it.planet !in listOf(Planet.SUN, Planet.MOON, Planet.RAHU, Planet.KETU)
        }

        for (i in warCapablePlanets.indices) {
            for (j in i + 1 until warCapablePlanets.size) {
                val planet1 = warCapablePlanets[i]
                val planet2 = warCapablePlanets[j]

                val distance = calculateAngularDistance(planet1.longitude, planet2.longitude)

                // Planetary war occurs within 1 degree
                if (distance <= 1.0) {
                    // Determine winner based on brightness (Venus > Jupiter > Mercury > Mars > Saturn)
                    val winner = getWarWinner(planet1.planet, planet2.planet)
                    val loser = if (winner == planet1.planet) planet2.planet else planet1.planet

                    wars.add(
                        PlanetaryWar(
                            planet1 = planet1.planet,
                            planet2 = planet2.planet,
                            angularSeparation = distance,
                            winner = winner,
                            loser = loser,
                            warType = "Conjunction War"
                        )
                    )
                }
            }
        }

        return wars
    }

    /**
     * Calculate angular distance between two longitudes
     */
    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    private fun getWarWinner(planet1: Planet, planet2: Planet): Planet {
        // Winner is determined by natural brightness, corrected order
        val brightness = mapOf(
            Planet.VENUS to 7,
            Planet.JUPITER to 6,
            Planet.MERCURY to 5,
            Planet.MARS to 4,
            Planet.SATURN to 3
        )

        val b1 = brightness[planet1] ?: 0
        val b2 = brightness[planet2] ?: 0

        return if (b1 >= b2) planet1 else planet2
    }

    private val STATIONARY_THRESHOLDS = mapOf(
        Planet.MERCURY to 0.05,
        Planet.VENUS to 0.04,
        Planet.MARS to 0.03,
        Planet.JUPITER to 0.02,
        Planet.SATURN to 0.01
    )

    private fun getRetrogradeStatus(planet: Planet, speed: Double, isRetrograde: Boolean): RetrogradeStatus {
        if (planet == Planet.SUN || planet == Planet.MOON) {
            return RetrogradeStatus.DIRECT
        }

        val threshold = STATIONARY_THRESHOLDS.getOrDefault(planet, 0.05)

        return when {
            abs(speed) < threshold && speed < 0 -> RetrogradeStatus.STATIONARY_RETROGRADE
            abs(speed) < threshold && speed >= 0 -> RetrogradeStatus.STATIONARY_DIRECT
            isRetrograde -> RetrogradeStatus.RETROGRADE
            else -> RetrogradeStatus.DIRECT
        }
    }

    /**
     * Get interpretation text for retrograde planet
     */
    fun getRetrogradeInterpretation(planet: Planet): String {
        return when (planet) {
            Planet.MERCURY -> "Mercury retrograde: Review, reflect, revise. Communication delays, travel disruptions. Good for completing unfinished work."
            Planet.VENUS -> "Venus retrograde: Reassess relationships and values. Not ideal for new romances or major purchases. Good for reconnecting with past loves."
            Planet.MARS -> "Mars retrograde: Frustration in actions, delays in initiatives. Channel energy inward. Review goals before acting."
            Planet.JUPITER -> "Jupiter retrograde: Internal spiritual growth. Wisdom comes from within. Review beliefs and philosophies."
            Planet.SATURN -> "Saturn retrograde: Karmic review period. Past responsibilities resurface. Good for restructuring and discipline."
            else -> "Retrograde motion intensifies the planet's energy inward."
        }
    }

    /**
     * Get interpretation text for combust planet
     */
    fun getCombustionInterpretation(planet: Planet, status: CombustionStatus): String {
        if (status == CombustionStatus.CAZIMI) {
            return "${planet.displayName} in Cazimi: Exceptionally empowered by the Sun's heart. The planet's significations are strengthened."
        }

        val baseInterpretation = when (planet) {
            Planet.MOON -> "Combust Moon: Emotional sensitivity, need for self-care. Mind may feel unclear or anxious."
            Planet.MARS -> "Combust Mars: Energy may feel suppressed. Avoid conflicts. Channel drive carefully."
            Planet.MERCURY -> "Combust Mercury: Communication challenges. Think before speaking. Good for internal processing."
            Planet.JUPITER -> "Combust Jupiter: Wisdom overshadowed. Seek guidance from within. Faith tested but strengthened."
            Planet.VENUS -> "Combust Venus: Relationship dynamics intensified. Self-worth review. Creative blocks possible."
            Planet.SATURN -> "Combust Saturn: Discipline challenged. Karmic lessons highlighted. Patience required."
            else -> "Planet weakened by Sun's rays."
        }

        return when (status) {
            CombustionStatus.FULL -> "$baseInterpretation (Full combustion - effects pronounced)"
            CombustionStatus.PARTIAL -> "$baseInterpretation (Partial combustion - mild effects)"
            else -> baseInterpretation
        }
    }
}

/**
 * Extended retrograde period calculator using Swiss Ephemeris
 * For calculating future retrograde periods
 */
class RetrogradeEphemerisCalculator(context: Context) : AutoCloseable {

    private val swissEph: SwissEph

    init {
        var swissEphInstance: SwissEph? = null
        try {
            swissEphInstance = SwissEph()
            val ephemerisPath = context.filesDir.absolutePath + "/ephe"
            swissEphInstance.swe_set_ephe_path(ephemerisPath)
            swissEphInstance.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
            swissEph = swissEphInstance
        } catch (e: Exception) {
            swissEphInstance?.swe_close()
            throw e
        }
    }

    /**
     * Find next retrograde period for a planet
     */
    fun findNextRetrograde(
        planet: Planet,
        fromDate: LocalDate,
        maxSearchDays: Int = 730  // 2 years
    ): RetrogradeCombustionCalculator.RetrogradePeriod? {
        if (planet in listOf(Planet.SUN, Planet.MOON, Planet.RAHU, Planet.KETU)) {
            return null  // These don't go retrograde in the traditional sense
        }

        var currentDate = fromDate
        var wasRetrograde = false
        var retroStartDate: LocalDate? = null
        var retroStartLongitude: Double? = null

        repeat(maxSearchDays) {
            val jd = dateToJulianDay(currentDate)
            val speed = getPlanetSpeed(planet, jd)

            val isRetrograde = speed < 0

            if (!wasRetrograde && isRetrograde) {
                // Started retrograde
                retroStartDate = currentDate
                retroStartLongitude = getPlanetLongitude(planet, jd)
            } else if (wasRetrograde && !isRetrograde && retroStartDate != null) {
                // Ended retrograde - return the period
                return RetrogradeCombustionCalculator.RetrogradePeriod(
                    planet = planet,
                    startDate = retroStartDate!!,
                    endDate = currentDate,
                    stationaryRetroDate = retroStartDate,
                    stationaryDirectDate = currentDate,
                    startLongitude = retroStartLongitude!!,
                    endLongitude = getPlanetLongitude(planet, jd),
                    durationDays = java.time.temporal.ChronoUnit.DAYS.between(retroStartDate, currentDate)
                )
            }

            wasRetrograde = isRetrograde
            currentDate = currentDate.plusDays(1)
        }

        return null
    }

    private fun getPlanetSpeed(planet: Planet, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swissEph.swe_calc_ut(
            julianDay,
            planet.swissEphId,
            SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED,
            xx,
            serr
        )

        return xx[3]  // Speed is in index 3
    }

    private fun getPlanetLongitude(planet: Planet, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swissEph.swe_calc_ut(
            julianDay,
            planet.swissEphId,
            SweConst.SEFLG_SIDEREAL,
            xx,
            serr
        )

        return ((xx[0] % 360.0) + 360.0) % 360.0
    }

    private fun dateToJulianDay(date: LocalDate): Double {
        val sweDate = SweDate(
            date.year,
            date.monthValue,
            date.dayOfMonth,
            12.0,  // Noon
            SweDate.SE_GREG_CAL
        )
        return sweDate.julDay
    }

    override fun close() {
        swissEph.swe_close()
    }
}
