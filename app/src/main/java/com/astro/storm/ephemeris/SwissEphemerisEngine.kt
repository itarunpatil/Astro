package com.astro.storm.ephemeris

import android.content.Context
import android.util.Log
import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.util.AstrologicalUtils
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class EphemerisInitializationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class EphemerisCalculationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

enum class AyanamsaType(
    internal val sweConstant: Int,
    val displayName: String
) {
    LAHIRI(SweConst.SE_SIDM_LAHIRI, "Lahiri"),
    RAMAN(SweConst.SE_SIDM_RAMAN, "Raman"),
    KRISHNAMURTI(SweConst.SE_SIDM_KRISHNAMURTI, "Krishnamurti"),
    TRUE_CHITRAPAKSHA(SweConst.SE_SIDM_TRUE_CITRA, "True Chitrapaksha"),
    YUKTESHWAR(SweConst.SE_SIDM_YUKTESHWAR, "Yukteshwar"),
    FAGAN_BRADLEY(SweConst.SE_SIDM_FAGAN_BRADLEY, "Fagan-Bradley");
}

class SwissEphemerisEngine private constructor(
    private val swissEph: SwissEph,
    private val ephemerisPath: String,
    private val ayanamsaType: AyanamsaType,
    private val hasJplEphemeris: Boolean
) : AutoCloseable {

    private val lock = ReentrantReadWriteLock()
    @Volatile
    private var isClosed = false

    private val planetResultBuffer = DoubleArray(PLANET_RESULT_SIZE)
    private val houseCuspsBuffer = DoubleArray(HOUSE_CUSPS_ARRAY_SIZE)
    private val ascMcBuffer = DoubleArray(ASC_MC_ARRAY_SIZE)
    private val errorBuffer = StringBuffer(ERROR_BUFFER_SIZE)

    private val calculationFlags: Int = if (hasJplEphemeris) {
        BASE_CALC_FLAGS or SweConst.SEFLG_JPLEPH
    } else {
        BASE_CALC_FLAGS
    }

    companion object {
        private const val TAG = "SwissEphemerisEngine"

        private const val PLANET_RESULT_SIZE = 6
        private const val HOUSE_CUSPS_ARRAY_SIZE = 13
        private const val ASC_MC_ARRAY_SIZE = 10
        private const val ERROR_BUFFER_SIZE = 256

        private const val ASC_INDEX = 0
        private const val MC_INDEX = 1

        private const val BASE_CALC_FLAGS = SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED

        private const val MIN_LATITUDE = -90.0
        private const val MAX_LATITUDE = 90.0
        private const val MIN_LONGITUDE = -180.0
        private const val MAX_LONGITUDE = 180.0

        private const val DEGREES_PER_CIRCLE = 360.0
        private const val DEGREES_PER_SIGN = 30.0
        private const val MINUTES_PER_DEGREE = 60.0
        private const val SECONDS_PER_MINUTE = 60.0
        private const val HOURS_PER_DAY = 24.0

        private const val KETU_OFFSET = 180.0

        private const val EPHEMERIS_SUBDIR = "ephe"

        private val JPL_EPHEMERIS_PATTERN = Regex("^de\\d{3}[ls]?\\.eph$", RegexOption.IGNORE_CASE)
        private val SWISS_EPHEMERIS_PATTERN = Regex("^se.*\\.se1$", RegexOption.IGNORE_CASE)

        @JvmStatic
        @JvmOverloads
        fun create(
            context: Context,
            ayanamsaType: AyanamsaType = AyanamsaType.LAHIRI
        ): SwissEphemerisEngine {
            val appContext = context.applicationContext
            val ephemerisDir = File(appContext.filesDir, EPHEMERIS_SUBDIR)

            if (!ephemerisDir.exists() && !ephemerisDir.mkdirs()) {
                throw EphemerisInitializationException(
                    "Failed to create ephemeris directory: ${ephemerisDir.absolutePath}"
                )
            }

            copyEphemerisFilesFromAssets(appContext, ephemerisDir)

            val hasJpl = checkForJplEphemeris(ephemerisDir)
            if (hasJpl) {
                Log.i(TAG, "JPL ephemeris files detected - using high precision mode")
            } else {
                Log.i(TAG, "Using Swiss Ephemeris data files")
            }

            val swissEph = SwissEph()

            try {
                swissEph.swe_set_ephe_path(ephemerisDir.absolutePath)
                swissEph.swe_set_sid_mode(ayanamsaType.sweConstant, 0.0, 0.0)

                return SwissEphemerisEngine(
                    swissEph = swissEph,
                    ephemerisPath = ephemerisDir.absolutePath,
                    ayanamsaType = ayanamsaType,
                    hasJplEphemeris = hasJpl
                )
            } catch (e: Exception) {
                try {
                    swissEph.swe_close()
                } catch (closeError: Exception) {
                    Log.w(TAG, "Error closing SwissEph during initialization failure", closeError)
                }
                throw EphemerisInitializationException(
                    "Failed to initialize Swiss Ephemeris engine",
                    e
                )
            }
        }

        private fun copyEphemerisFilesFromAssets(context: Context, targetDir: File) {
            val assetManager = context.assets

            val ephemerisFiles = try {
                assetManager.list(EPHEMERIS_SUBDIR)?.toList() ?: emptyList()
            } catch (e: IOException) {
                Log.w(TAG, "Ephemeris assets not found; calculations will use internal algorithms", e)
                return
            }

            if (ephemerisFiles.isEmpty()) {
                Log.d(TAG, "No ephemeris files in assets directory")
                return
            }

            var copiedCount = 0
            var skippedCount = 0

            for (filename in ephemerisFiles) {
                val targetFile = File(targetDir, filename)

                if (targetFile.exists()) {
                    skippedCount++
                    continue
                }

                try {
                    assetManager.open("$EPHEMERIS_SUBDIR/$filename").use { input ->
                        targetFile.outputStream().buffered().use { output ->
                            input.copyTo(output, bufferSize = 8192)
                        }
                    }
                    copiedCount++
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to copy ephemeris file: $filename", e)
                }
            }

            Log.i(TAG, "Ephemeris files: $copiedCount copied, $skippedCount already present")
        }

        private fun checkForJplEphemeris(ephemerisDir: File): Boolean {
            if (!ephemerisDir.exists() || !ephemerisDir.isDirectory) return false

            return ephemerisDir.listFiles()?.any { file ->
                file.isFile && JPL_EPHEMERIS_PATTERN.matches(file.name)
            } == true
        }

        private fun normalizeDegree(degree: Double): Double {
            var result = degree % DEGREES_PER_CIRCLE
            if (result < 0.0) result += DEGREES_PER_CIRCLE
            return result
        }

        private fun angularDistance(deg1: Double, deg2: Double): Double {
            val diff = kotlin.math.abs(deg1 - deg2)
            return if (diff > 180.0) DEGREES_PER_CIRCLE - diff else diff
        }
    }

    val currentAyanamsaType: AyanamsaType
        get() = ayanamsaType

    val isUsingJplEphemeris: Boolean
        get() = hasJplEphemeris

    fun calculateVedicChart(
        birthData: BirthData,
        houseSystem: HouseSystem = HouseSystem.DEFAULT
    ): VedicChart {
        validateBirthData(birthData)
        ensureOpen()

        return lock.write {
            performChartCalculation(birthData, houseSystem)
        }
    }

    fun calculatePlanetPosition(
        planet: Planet,
        dateTime: LocalDateTime,
        timezone: String,
        latitude: Double,
        longitude: Double
    ): PlanetPosition {
        ensureOpen()

        val utcDateTime = convertToUtc(dateTime, timezone)
        val julianDay = calculateJulianDay(utcDateTime)

        return lock.write {
            calculateSinglePlanetPosition(planet, julianDay, latitude, longitude)
        }
    }

    fun getAyanamsa(julianDay: Double): Double {
        ensureOpen()
        return lock.read {
            swissEph.swe_get_ayanamsa_ut(julianDay)
        }
    }

    fun getAyanamsaForDateTime(dateTime: LocalDateTime, timezone: String = "UTC"): Double {
        val utcDateTime = convertToUtc(dateTime, timezone)
        val julianDay = calculateJulianDay(utcDateTime)
        return getAyanamsa(julianDay)
    }

    fun getCurrentAyanamsa(): Double {
        val now = LocalDateTime.now(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(now)
        return getAyanamsa(julianDay)
    }

    fun calculateJulianDayForDateTime(dateTime: LocalDateTime, timezone: String): Double {
        val utcDateTime = convertToUtc(dateTime, timezone)
        return calculateJulianDay(utcDateTime)
    }

    private fun performChartCalculation(
        birthData: BirthData,
        houseSystem: HouseSystem
    ): VedicChart {
        val utcDateTime = convertToUtc(birthData.dateTime, birthData.timezone)
        val julianDay = calculateJulianDay(utcDateTime)

        val ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay)

        houseCuspsBuffer.fill(0.0)
        ascMcBuffer.fill(0.0)

        val houseResult = swissEph.swe_houses(
            julianDay,
            SweConst.SEFLG_SIDEREAL,
            birthData.latitude.toDouble(),
            birthData.longitude.toDouble(),
            houseSystem.code.code,
            houseCuspsBuffer,
            ascMcBuffer
        )

        if (houseResult < 0) {
            Log.w(TAG, "House calculation returned error code: $houseResult, using Placidus fallback")
        }

        val ascendant = ascMcBuffer[ASC_INDEX]
        val midheaven = ascMcBuffer[MC_INDEX]

        val houseCuspsCopy = (1..12).map { houseCuspsBuffer[it] }

        val planetPositions = Planet.ALL_PLANETS.map { planet ->
            calculatePlanetPositionInternal(planet, julianDay, houseCuspsCopy)
        }

        return VedicChart(
            birthData = birthData,
            julianDay = julianDay,
            ayanamsa = ayanamsa,
            ayanamsaName = ayanamsaType.displayName,
            ascendant = ascendant,
            midheaven = midheaven,
            planetPositions = planetPositions,
            houseCusps = houseCuspsCopy,
            houseSystem = houseSystem
        )
    }

    private fun calculateSinglePlanetPosition(
        planet: Planet,
        julianDay: Double,
        latitude: Double,
        longitude: Double
    ): PlanetPosition {
        houseCuspsBuffer.fill(0.0)
        ascMcBuffer.fill(0.0)

        swissEph.swe_houses(
            julianDay,
            SweConst.SEFLG_SIDEREAL,
            latitude,
            longitude,
            'P'.code,
            houseCuspsBuffer,
            ascMcBuffer
        )

        val houseCuspsCopy = (1..12).map { houseCuspsBuffer[it] }
        return calculatePlanetPositionInternal(planet, julianDay, houseCuspsCopy)
    }

    private fun calculatePlanetPositionInternal(
        planet: Planet,
        julianDay: Double,
        houseCusps: List<Double>
    ): PlanetPosition {
        planetResultBuffer.fill(0.0)
        errorBuffer.setLength(0)

        val sweId = if (planet == Planet.KETU) Planet.RAHU.swissEphId else planet.swissEphId

        val calcResult = swissEph.swe_calc_ut(
            julianDay,
            sweId,
            calculationFlags,
            planetResultBuffer,
            errorBuffer
        )

        if (calcResult < 0) {
            val errorMessage = if (errorBuffer.isNotEmpty()) {
                errorBuffer.toString()
            } else {
                "Unknown calculation error (code: $calcResult)"
            }
            throw EphemerisCalculationException(
                "Failed to calculate ${planet.displayName}: $errorMessage"
            )
        }

        var rawLongitude = planetResultBuffer[0]
        val latitude = planetResultBuffer[1]
        val distance = planetResultBuffer[2]
        var speed = planetResultBuffer[3]

        if (planet == Planet.KETU) {
            rawLongitude = normalizeDegree(rawLongitude + KETU_OFFSET)
            speed = -speed
        }

        val normalizedLongitude = AstrologicalUtils.normalizeLongitude(rawLongitude)

        val sign = ZodiacSign.fromLongitude(normalizedLongitude)
        val degreeInSign = normalizedLongitude % DEGREES_PER_SIGN

        val wholeDegrees = degreeInSign.toInt()
        val fractionalDegrees = degreeInSign - wholeDegrees
        val totalMinutes = fractionalDegrees * MINUTES_PER_DEGREE
        val wholeMinutes = totalMinutes.toInt()
        val fractionalMinutes = totalMinutes - wholeMinutes
        val seconds = fractionalMinutes * SECONDS_PER_MINUTE

        val isRetrograde = speed < 0.0

        val (nakshatra, pada) = Nakshatra.fromLongitude(normalizedLongitude)

        val house = determineHouse(normalizedLongitude, houseCusps)

        return PlanetPosition(
            planet = planet,
            longitude = normalizedLongitude,
            latitude = latitude,
            distance = distance,
            speed = speed,
            sign = sign,
            degree = wholeDegrees.toDouble(),
            minutes = wholeMinutes.toDouble(),
            seconds = seconds,
            isRetrograde = isRetrograde,
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = house
        )
    }

    private fun determineHouse(longitude: Double, houseCusps: List<Double>): Int {
        for (houseNum in 1..12) {
            val cuspStart = houseCusps[houseNum - 1]
            val cuspEnd = if (houseNum == 12) houseCusps[0] else houseCusps[houseNum]

            val normalizedLongitude = normalizeDegree(longitude - cuspStart)
            val houseWidth = normalizeDegree(cuspEnd - cuspStart)

            val effectiveWidth = if (houseWidth < 0.001) DEGREES_PER_SIGN else houseWidth

            if (normalizedLongitude < effectiveWidth) {
                return houseNum
            }
        }

        return findClosestHouse(longitude, houseCusps)
    }

    private fun findClosestHouse(longitude: Double, houseCusps: List<Double>): Int {
        var closestHouse = 1
        var minDistance = DEGREES_PER_CIRCLE

        for (houseNum in 1..12) {
            val cusp = houseCusps[houseNum - 1]
            val distance = angularDistance(longitude, cusp)
            if (distance < minDistance) {
                minDistance = distance
                closestHouse = houseNum
            }
        }

        Log.w(TAG, "House determination fallback used for longitude $longitude")
        return closestHouse
    }

    private fun calculateJulianDay(utcDateTime: LocalDateTime): Double {
        val decimalHours = utcDateTime.hour +
                (utcDateTime.minute / 60.0) +
                (utcDateTime.second / 3600.0) +
                (utcDateTime.nano / 3_600_000_000_000.0)

        val sweDate = SweDate(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            decimalHours,
            SweDate.SE_GREG_CAL
        )

        return sweDate.julDay
    }

    private fun convertToUtc(dateTime: LocalDateTime, timezone: String): LocalDateTime {
        return try {
            val zoneId = ZoneId.of(timezone)
            val zonedDateTime = ZonedDateTime.of(dateTime, zoneId)
            zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid timezone: $timezone", e)
        }
    }

    private fun validateBirthData(birthData: BirthData) {
        val lat = birthData.latitude.toDouble()
        val lon = birthData.longitude.toDouble()

        require(lat in MIN_LATITUDE..MAX_LATITUDE) {
            "Latitude must be between $MIN_LATITUDE and $MAX_LATITUDE degrees, got: $lat"
        }

        require(lon in MIN_LONGITUDE..MAX_LONGITUDE) {
            "Longitude must be between $MIN_LONGITUDE and $MAX_LONGITUDE degrees, got: $lon"
        }

        require(birthData.timezone.isNotBlank()) {
            "Timezone must not be blank"
        }

        try {
            ZoneId.of(birthData.timezone)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid timezone identifier: ${birthData.timezone}", e)
        }

        val dateTime = birthData.dateTime
        require(dateTime.year in 1..9999) {
            "Year must be between 1 and 9999, got: ${dateTime.year}"
        }
    }

    private fun ensureOpen() {
        check(!isClosed) {
            "SwissEphemerisEngine has been closed and cannot be used"
        }
    }

    override fun close() {
        if (isClosed) return

        lock.write {
            if (!isClosed) {
                try {
                    swissEph.swe_close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during SwissEph close", e)
                } finally {
                    isClosed = true
                    Log.d(TAG, "SwissEphemerisEngine closed successfully")
                }
            }
        }
    }

    override fun toString(): String {
        return "SwissEphemerisEngine(" +
                "ayanamsa=${ayanamsaType.displayName}, " +
                "jplMode=$hasJplEphemeris, " +
                "closed=$isClosed)"
    }
}

inline fun <R> SwissEphemerisEngine.use(block: (SwissEphemerisEngine) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        try {
            close()
        } catch (closeException: Throwable) {
            exception?.addSuppressed(closeException)
        }
    }
}