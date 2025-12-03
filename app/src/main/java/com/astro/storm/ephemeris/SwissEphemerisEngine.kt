package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * High-precision Swiss Ephemeris calculation engine for Vedic astrology.
 * Uses JPL ephemeris files for maximum accuracy.
 */
class SwissEphemerisEngine(context: Context) : AutoCloseable {

    private val swissEph: SwissEph
    private val ephemerisPath: String

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED
        private const val SEFLG_JPLEPH = SweConst.SEFLG_JPLEPH

        // Combined flags for maximum precision
        private const val CALC_FLAGS = SEFLG_SIDEREAL or SEFLG_SPEED or SEFLG_JPLEPH
    }

    init {
        var swissEphInstance: SwissEph? = null
        try {
            swissEphInstance = SwissEph()
            // Set ephemeris path to app's files directory
            ephemerisPath = context.filesDir.absolutePath + "/ephe"
            File(ephemerisPath).mkdirs()

            // Initialize Swiss Ephemeris with JPL mode
            swissEphInstance.swe_set_ephe_path(ephemerisPath)
            swissEphInstance.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
            swissEph = swissEphInstance
        } catch (e: Exception) {
            android.util.Log.e("SwissEphemerisEngine", "Failed to initialize Swiss Ephemeris", e)
            // Close any partially initialized resources
            swissEphInstance?.swe_close()
            throw e
        }
    }

    fun copyEphemerisFiles(context: Context) {
        try {
            val assetManager = context.assets
            val ephemerisFiles = try {
                assetManager.list("ephe") ?: emptyArray()
            } catch (e: java.io.IOException) {
                android.util.Log.e("SwissEphemeris", "Could not list ephemeris assets", e)
                emptyArray()
            }

            ephemerisFiles.forEach { filename ->
                val outFile = File(ephemerisPath, filename)
                if (!outFile.exists()) {
                    assetManager.open("ephe/$filename").use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: java.io.IOException) {
            // Ephemeris files are optional; calculations will use less precise methods if unavailable
            android.util.Log.e("SwissEphemeris", "Could not copy ephemeris files", e)
        }
    }

    /**
     * Calculate complete Vedic chart with maximum precision
     */
    fun calculateVedicChart(
        birthData: BirthData,
        houseSystem: HouseSystem = HouseSystem.DEFAULT
    ): VedicChart {
        // Convert local time to UTC
        val zonedDateTime = ZonedDateTime.of(
            birthData.dateTime,
            ZoneId.of(birthData.timezone)
        )
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))

        // Calculate Julian Day with full precision
        val julianDay = calculateJulianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour,
            utcDateTime.minute,
            utcDateTime.second
        )

        // Get ayanamsa value
        val ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay)

        // Calculate house cusps and ascendant
        val houseCusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swissEph.swe_houses(
            julianDay,
            SEFLG_SIDEREAL,
            birthData.latitude.toDouble(),
            birthData.longitude.toDouble(),
            houseSystem.code.code,
            houseCusps,
            ascmc
        )

        val ascendant = ascmc[0]
        val midheaven = ascmc[1]

        // Calculate planetary positions for all planets (including outer planets)
        // This matches AstroSage's display which shows Uranus, Neptune, and Pluto
        val planetPositions = Planet.ALL_PLANETS.map { planet ->
            calculatePlanetPosition(planet, julianDay, houseCusps)
        }

        return VedicChart(
            birthData = birthData,
            julianDay = julianDay,
            ayanamsa = ayanamsa,
            ayanamsaName = "Lahiri",
            ascendant = ascendant,
            midheaven = midheaven,
            planetPositions = planetPositions,
            houseCusps = houseCusps.drop(1).toList(), // Drop first element (unused by Swiss Ephemeris)
            houseSystem = houseSystem
        )
    }

    /**
     * Calculate planet position with full precision
     */
    private fun calculatePlanetPosition(
        planet: Planet,
        julianDay: Double,
        houseCusps: DoubleArray
    ): PlanetPosition {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val planetId = if (planet == Planet.KETU) Planet.RAHU.swissEphId else planet.swissEphId

        val iflgret = swissEph.swe_calc_ut(
            julianDay,
            planetId,
            CALC_FLAGS,
            xx,
            serr
        )

        if (iflgret < 0) {
            throw RuntimeException("Swiss Ephemeris calculation error: $serr")
        }

        var longitude = xx[0]
        val latitude = xx[1]
        val distance = xx[2]
        val speed = xx[3]

        // Adjust Ketu to be 180° from Rahu
        if (planet == Planet.KETU) {
            longitude = (longitude + 180.0) % 360.0
        }

        // Normalize longitude
        longitude = com.astro.storm.util.AstrologicalUtils.normalizeLongitude(longitude)

        // Determine sign and degree within sign
        val sign = ZodiacSign.fromLongitude(longitude)
        val degreeInSign = longitude % 30.0
        val degree = degreeInSign.toInt().toDouble()
        val minutes = ((degreeInSign - degree) * 60.0)
        val seconds = ((minutes - minutes.toInt()) * 60.0)

        // Check if retrograde (negative speed)
        val isRetrograde = speed < 0.0

        // Get nakshatra
        val (nakshatra, pada) = Nakshatra.fromLongitude(longitude)

        // Determine house
        val house = determineHouse(longitude, houseCusps)

        return PlanetPosition(
            planet = planet,
            longitude = longitude,
            latitude = latitude,
            distance = distance,
            speed = speed,
            sign = sign,
            degree = degree,
            minutes = minutes.toInt().toDouble(),
            seconds = seconds,
            isRetrograde = isRetrograde,
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = house
        )
    }

    /**
     * Determine which house a planet is in using a robust, modular arithmetic approach.
     * This method correctly handles house boundaries that cross over 0° Aries.
     */
    private fun determineHouse(
        longitude: Double,
        houseCusps: DoubleArray
    ): Int {
        // houseCusps is 1-indexed from Swiss Ephemeris, where houseCusps[1] is the Ascendant.
        for (i in 1..12) {
            val cuspStart = houseCusps[i]
            val cuspEnd = if (i == 12) houseCusps[1] else houseCusps[i + 1]

            // By normalizing all longitudes relative to the starting cusp of the house,
            // we can handle the 0-degree crossover seamlessly without special conditions.
            // A planet is in a house if its normalized position is less than or equal to the house's size.
            val normalizedPlanetLon = (longitude - cuspStart + 360) % 360
            val normalizedCuspEnd = (cuspEnd - cuspStart + 360) % 360

            if (normalizedPlanetLon <= normalizedCuspEnd) {
                return i
            }
        }

        // Fallback for edge cases, such as a planet sitting exactly on a cusp.
        // It should logically be placed in the house that starts on that cusp,
        // which the loop would handle. This is a safeguard.
        return 12
    }

    /**
     * Calculate Julian Day with full precision
     * Ensures no rounding errors in time conversion
     */
    private fun calculateJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Double {
        // Convert time to decimal hours with full precision
        val decimalHours = hour + (minute / 60.0) + (second / 3600.0)

        // Use Swiss Ephemeris date conversion for maximum accuracy
        val sweDate = SweDate(year, month, day, decimalHours, SweDate.SE_GREG_CAL)
        return sweDate.julDay
    }

    /**
     * Get current ayanamsa value
     */
    fun getAyanamsa(julianDay: Double): Double {
        return swissEph.swe_get_ayanamsa_ut(julianDay)
    }

    /**
     * Clean up resources
     */
    override fun close() {
        swissEph.swe_close()
    }
}
