package com.astro.storm.data.repository

import com.astro.storm.data.local.ChartDao
import com.astro.storm.data.local.ChartEntity
import com.astro.storm.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository for chart data operations
 */
class ChartRepository(private val chartDao: ChartDao) {

    fun getAllCharts(): Flow<List<SavedChart>> {
        return chartDao.getAllCharts().map { entities ->
            entities.map { it.toSavedChart() }
        }
    }

    suspend fun getChartById(id: Long): VedicChart? {
        return chartDao.getChartById(id)?.toVedicChart()
    }

    suspend fun saveChart(chart: VedicChart): Long {
        return chartDao.insertChart(chart.toEntity())
    }

    suspend fun deleteChart(id: Long) {
        chartDao.deleteChartById(id)
    }

    fun searchCharts(query: String): Flow<List<SavedChart>> {
        return chartDao.searchCharts(query).map { entities ->
            entities.map { it.toSavedChart() }
        }
    }

    private fun VedicChart.toEntity(): ChartEntity {
        val planetPositionsJson = JSONArray().apply {
            planetPositions.forEach { position ->
                put(JSONObject().apply {
                    put("planet", position.planet.name)
                    put("longitude", position.longitude)
                    put("latitude", position.latitude)
                    put("distance", position.distance)
                    put("speed", position.speed)
                    put("sign", position.sign.name)
                    put("degree", position.degree)
                    put("minutes", position.minutes)
                    put("seconds", position.seconds)
                    put("isRetrograde", position.isRetrograde)
                    put("nakshatra", position.nakshatra.name)
                    put("nakshatraPada", position.nakshatraPada)
                    put("house", position.house)
                })
            }
        }.toString()

        val houseCuspsJson = JSONArray().apply {
            houseCusps.forEach { put(it) }
        }.toString()

        return ChartEntity(
            name = birthData.name,
            dateTime = birthData.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            latitude = birthData.latitude,
            longitude = birthData.longitude,
            timezone = birthData.timezone,
            location = birthData.location,
            julianDay = julianDay,
            ayanamsa = ayanamsa,
            ayanamsaName = ayanamsaName,
            ascendant = ascendant,
            midheaven = midheaven,
            planetPositionsJson = planetPositionsJson,
            houseCuspsJson = houseCuspsJson,
            houseSystem = houseSystem.name
        )
    }

    private fun ChartEntity.toVedicChart(): VedicChart {
        try {
            val planetPositions = JSONArray(planetPositionsJson).let { array ->
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    PlanetPosition(
                        planet = Planet.valueOf(obj.getString("planet")),
                        longitude = obj.getDouble("longitude"),
                        latitude = obj.getDouble("latitude"),
                        distance = obj.getDouble("distance"),
                        speed = obj.getDouble("speed"),
                        sign = ZodiacSign.valueOf(obj.getString("sign")),
                        degree = obj.getDouble("degree"),
                        minutes = obj.getDouble("minutes"),
                        seconds = obj.getDouble("seconds"),
                        isRetrograde = obj.getBoolean("isRetrograde"),
                        nakshatra = Nakshatra.valueOf(obj.getString("nakshatra")),
                        nakshatraPada = obj.getInt("nakshatraPada"),
                        house = obj.getInt("house")
                    )
                }
            }

            val houseCusps = JSONArray(houseCuspsJson).let { array ->
                (0 until array.length()).map { i ->
                    array.getDouble(i)
                }
            }
            return VedicChart(
                birthData = BirthData(
                    name = name,
                    dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    latitude = latitude,
                    longitude = longitude,
                    timezone = timezone,
                    location = location
                ),
                julianDay = julianDay,
                ayanamsa = ayanamsa,
                ayanamsaName = ayanamsaName,
                ascendant = ascendant,
                midheaven = midheaven,
                planetPositions = planetPositions,
                houseCusps = houseCusps,
                houseSystem = HouseSystem.valueOf(houseSystem),
                calculationTime = createdAt
            )
        } catch (e: Exception) {
            android.util.Log.e("ChartRepository", "Failed to parse chart entity", e)
            throw IllegalStateException("Failed to parse chart data from database", e)
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
