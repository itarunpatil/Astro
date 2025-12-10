package com.astro.storm.data.repository

import android.util.Log
import com.astro.storm.data.local.ChartDao
import com.astro.storm.data.local.ChartEntity
import com.astro.storm.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Result wrapper for chart loading operations.
 * Provides detailed error information for debugging and user feedback.
 */
sealed class ChartLoadResult {
    data class Success(val chart: VedicChart) : ChartLoadResult()
    data class ParseError(val chartId: Long, val message: String, val cause: Throwable? = null) : ChartLoadResult()
    data class NotFound(val chartId: Long) : ChartLoadResult()
}

/**
 * Repository for chart data operations.
 *
 * Provides defensive null checks and error handling for all database operations:
 * - Safe JSON parsing with fallback defaults
 * - Flow error handling to prevent stream breaks
 * - Input sanitization to prevent storage of invalid data
 * - Detailed logging for debugging
 */
class ChartRepository(private val chartDao: ChartDao) {

    companion object {
        private const val TAG = "ChartRepository"
        private const val MAX_QUERY_LENGTH = 200
    }

    fun getAllCharts(): Flow<List<SavedChart>> {
        return chartDao.getAllCharts()
            .map { entities ->
                entities.mapNotNull { entity ->
                    try {
                        entity.toSavedChart()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert entity ${entity.id} to SavedChart", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error loading charts from database", e)
                emit(emptyList())
            }
    }

    suspend fun getChartById(id: Long): VedicChart? {
        return try {
            chartDao.getChartById(id)?.toVedicChart()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load chart with id $id", e)
            null
        }
    }

    /**
     * Safe version of getChartById that returns detailed error information.
     * Use this when you need to distinguish between "not found" and "parse error".
     */
    suspend fun getChartByIdSafe(id: Long): ChartLoadResult {
        return try {
            val entity = chartDao.getChartById(id)
            if (entity == null) {
                ChartLoadResult.NotFound(id)
            } else {
                try {
                    ChartLoadResult.Success(entity.toVedicChart())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse chart with id $id", e)
                    ChartLoadResult.ParseError(id, "Failed to parse chart data: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Database error loading chart with id $id", e)
            ChartLoadResult.ParseError(id, "Database error: ${e.message}", e)
        }
    }

    suspend fun saveChart(chart: VedicChart): Long {
        return try {
            chartDao.insertChart(chart.toEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save chart: ${chart.birthData.name}", e)
            throw e
        }
    }

    suspend fun deleteChart(id: Long) {
        try {
            chartDao.deleteChartById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete chart with id $id", e)
            throw e
        }
    }

    fun searchCharts(query: String): Flow<List<SavedChart>> {
        // Sanitize query to prevent issues
        val sanitizedQuery = query.take(MAX_QUERY_LENGTH).trim()
        if (sanitizedQuery.isEmpty()) {
            return getAllCharts()
        }

        return chartDao.searchCharts(sanitizedQuery)
            .map { entities ->
                entities.mapNotNull { entity ->
                    try {
                        entity.toSavedChart()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert entity ${entity.id} to SavedChart in search", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error searching charts with query: $sanitizedQuery", e)
                emit(emptyList())
            }
    }

    /**
     * Sanitizes a double value to prevent NaN or Infinite being stored.
     * Returns 0.0 as fallback for invalid values.
     */
    private fun sanitizeDouble(value: Double, fallback: Double = 0.0): Double {
        return if (value.isNaN() || value.isInfinite()) fallback else value
    }

    private fun VedicChart.toEntity(): ChartEntity {
        val planetPositionsJson = JSONArray().apply {
            planetPositions.forEach { position ->
                put(JSONObject().apply {
                    put("planet", position.planet.name)
                    put("longitude", sanitizeDouble(position.longitude))
                    put("latitude", sanitizeDouble(position.latitude))
                    put("distance", sanitizeDouble(position.distance, 1.0))
                    put("speed", sanitizeDouble(position.speed))
                    put("sign", position.sign.name)
                    put("degree", sanitizeDouble(position.degree))
                    put("minutes", sanitizeDouble(position.minutes))
                    put("seconds", sanitizeDouble(position.seconds))
                    put("isRetrograde", position.isRetrograde)
                    put("nakshatra", position.nakshatra.name)
                    put("nakshatraPada", position.nakshatraPada.coerceIn(1, 4))
                    put("house", position.house.coerceIn(1, 12))
                })
            }
        }.toString()

        val houseCuspsJson = JSONArray().apply {
            houseCusps.forEach { put(sanitizeDouble(it)) }
        }.toString()

        return ChartEntity(
            name = birthData.name.take(BirthData.MAX_NAME_LENGTH),
            dateTime = birthData.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            latitude = sanitizeDouble(birthData.latitude),
            longitude = sanitizeDouble(birthData.longitude),
            timezone = birthData.timezone,
            location = birthData.location.take(BirthData.MAX_LOCATION_LENGTH),
            julianDay = sanitizeDouble(julianDay),
            ayanamsa = sanitizeDouble(ayanamsa),
            ayanamsaName = ayanamsaName,
            ascendant = sanitizeDouble(ascendant),
            midheaven = sanitizeDouble(midheaven),
            planetPositionsJson = planetPositionsJson,
            houseCuspsJson = houseCuspsJson,
            houseSystem = houseSystem.name,
            gender = birthData.gender.name
        )
    }

    private fun ChartEntity.toVedicChart(): VedicChart {
        val planetPositions = parsePlanetPositions(planetPositionsJson)
        val houseCusps = parseHouseCusps(houseCuspsJson)
        val parsedDateTime = parseDateTime(dateTime)

        return VedicChart(
            birthData = BirthData(
                name = name.ifBlank { "Unknown" },
                dateTime = parsedDateTime,
                latitude = latitude.coerceIn(BirthData.MIN_LATITUDE, BirthData.MAX_LATITUDE),
                longitude = longitude.coerceIn(BirthData.MIN_LONGITUDE, BirthData.MAX_LONGITUDE),
                timezone = timezone.ifBlank { "UTC" },
                location = location,
                gender = Gender.fromString(gender)
            ),
            julianDay = julianDay,
            ayanamsa = ayanamsa,
            ayanamsaName = ayanamsaName.ifBlank { "Lahiri" },
            ascendant = ascendant,
            midheaven = midheaven,
            planetPositions = planetPositions,
            houseCusps = houseCusps,
            houseSystem = parseHouseSystem(houseSystem),
            calculationTime = createdAt
        )
    }

    /**
     * Safely parses planet positions from JSON with fallback handling.
     */
    private fun parsePlanetPositions(json: String): List<PlanetPosition> {
        if (json.isBlank()) {
            Log.w(TAG, "Empty planet positions JSON")
            return emptyList()
        }

        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                try {
                    val obj = array.optJSONObject(i) ?: return@mapNotNull null
                    parsePlanetPosition(obj)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse planet position at index $i", e)
                    null
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid planet positions JSON", e)
            emptyList()
        }
    }

    /**
     * Parses a single planet position from JSON with safe defaults.
     */
    private fun parsePlanetPosition(obj: JSONObject): PlanetPosition? {
        val planetName = obj.optString("planet", "")
        if (planetName.isBlank()) {
            Log.w(TAG, "Missing planet name in position JSON")
            return null
        }

        val planet = try {
            Planet.valueOf(planetName)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unknown planet: $planetName")
            return null
        }

        val signName = obj.optString("sign", "ARIES")
        val sign = try {
            ZodiacSign.valueOf(signName)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unknown sign: $signName, defaulting to ARIES")
            ZodiacSign.ARIES
        }

        val nakshatraName = obj.optString("nakshatra", "ASHWINI")
        val nakshatra = try {
            Nakshatra.valueOf(nakshatraName)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unknown nakshatra: $nakshatraName, defaulting to ASHWINI")
            Nakshatra.ASHWINI
        }

        return PlanetPosition(
            planet = planet,
            longitude = obj.optDouble("longitude", 0.0).coerceIn(0.0, 360.0),
            latitude = obj.optDouble("latitude", 0.0).coerceIn(-90.0, 90.0),
            distance = obj.optDouble("distance", 1.0).coerceAtLeast(0.0),
            speed = obj.optDouble("speed", 0.0),
            sign = sign,
            degree = obj.optDouble("degree", 0.0).coerceIn(0.0, 30.0),
            minutes = obj.optDouble("minutes", 0.0).coerceIn(0.0, 60.0),
            seconds = obj.optDouble("seconds", 0.0).coerceIn(0.0, 60.0),
            isRetrograde = obj.optBoolean("isRetrograde", false),
            nakshatra = nakshatra,
            nakshatraPada = obj.optInt("nakshatraPada", 1).coerceIn(1, 4),
            house = obj.optInt("house", 1).coerceIn(1, 12)
        )
    }

    /**
     * Safely parses house cusps from JSON.
     */
    private fun parseHouseCusps(json: String): List<Double> {
        if (json.isBlank()) {
            Log.w(TAG, "Empty house cusps JSON, returning defaults")
            return List(12) { it * 30.0 }
        }

        return try {
            val array = JSONArray(json)
            val cusps = (0 until array.length()).map { i ->
                val value = array.optDouble(i, i * 30.0)
                if (value.isNaN() || value.isInfinite()) i * 30.0 else value
            }
            // Ensure we have 12 cusps
            if (cusps.size < 12) {
                Log.w(TAG, "Insufficient house cusps (${cusps.size}), padding with defaults")
                cusps + List(12 - cusps.size) { (cusps.size + it) * 30.0 }
            } else {
                cusps.take(12)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid house cusps JSON, using defaults", e)
            List(12) { it * 30.0 }
        }
    }

    /**
     * Safely parses date time string with fallback.
     */
    private fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Failed to parse datetime: $dateTimeStr, using epoch", e)
            LocalDateTime.of(2000, 1, 1, 12, 0)
        }
    }

    /**
     * Safely parses house system enum with fallback.
     */
    private fun parseHouseSystem(systemName: String): HouseSystem {
        return try {
            HouseSystem.valueOf(systemName)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unknown house system: $systemName, defaulting to PLACIDUS")
            HouseSystem.PLACIDUS
        }
    }

    private fun ChartEntity.toSavedChart(): SavedChart {
        return SavedChart(
            id = id,
            name = name,
            dateTime = dateTime,
            location = location,
            createdAt = createdAt
        )
    }
}

/**
 * Simplified chart data for list display
 */
data class SavedChart(
    val id: Long,
    val name: String,
    val dateTime: String,
    val location: String,
    val createdAt: Long
)
