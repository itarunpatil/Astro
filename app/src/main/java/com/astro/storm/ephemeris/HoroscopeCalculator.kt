package com.astro.storm.ephemeris

import android.content.Context
import android.util.Log
import com.astro.storm.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

class HoroscopeCalculator(private val context: Context) : AutoCloseable {

    private val ephemerisEngine = SwissEphemerisEngine(context)
    private val transitCache = ConcurrentHashMap<TransitCacheKey, VedicChart>()
    private val dailyHoroscopeCache = ConcurrentHashMap<DailyHoroscopeCacheKey, DailyHoroscope>()

    private data class TransitCacheKey(
        val date: LocalDate,
        val latitude: Double,
        val longitude: Double,
        val timezone: String
    )

    private data class DailyHoroscopeCacheKey(
        val chartHashCode: Int,
        val date: LocalDate
    )

    data class DailyHoroscope(
        val date: LocalDate,
        val theme: String,
        val themeDescription: String,
        val overallEnergy: Int,
        val moonSign: ZodiacSign,
        val moonNakshatra: Nakshatra,
        val activeDasha: String,
        val lifeAreas: List<LifeAreaPrediction>,
        val luckyElements: LuckyElements,
        val planetaryInfluences: List<PlanetaryInfluence>,
        val recommendations: List<String>,
        val cautions: List<String>,
        val affirmation: String
    )

    data class LifeAreaPrediction(
        val area: LifeArea,
        val rating: Int,
        val prediction: String,
        val advice: String
    )

    enum class LifeArea(val displayName: String, val houses: List<Int>) {
        CAREER("Career", listOf(10, 6, 2)),
        LOVE("Love & Relationships", listOf(7, 5, 11)),
        HEALTH("Health & Vitality", listOf(1, 6, 8)),
        FINANCE("Finance & Wealth", listOf(2, 11, 5)),
        FAMILY("Family & Home", listOf(4, 2, 12)),
        SPIRITUALITY("Spiritual Growth", listOf(9, 12, 5))
    }

    data class LuckyElements(
        val number: Int,
        val color: String,
        val direction: String,
        val time: String,
        val gemstone: String
    )

    data class PlanetaryInfluence(
        val planet: Planet,
        val influence: String,
        val strength: Int,
        val isPositive: Boolean
    )

    data class WeeklyHoroscope(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val weeklyTheme: String,
        val weeklyOverview: String,
        val keyDates: List<KeyDate>,
        val weeklyPredictions: Map<LifeArea, String>,
        val dailyHighlights: List<DailyHighlight>,
        val weeklyAdvice: String
    )

    data class KeyDate(
        val date: LocalDate,
        val event: String,
        val significance: String,
        val isPositive: Boolean
    )

    data class DailyHighlight(
        val date: LocalDate,
        val dayOfWeek: String,
        val energy: Int,
        val focus: String,
        val brief: String
    )

    sealed class HoroscopeResult<out T> {
        data class Success<T>(val data: T) : HoroscopeResult<T>()
        data class Error(val message: String, val cause: Throwable? = null) : HoroscopeResult<Nothing>()
    }

    fun calculateDailyHoroscope(chart: VedicChart, date: LocalDate = LocalDate.now()): DailyHoroscope {
        val cacheKey = DailyHoroscopeCacheKey(chart.hashCode(), date)
        dailyHoroscopeCache[cacheKey]?.let { return it }

        val transitChart = getOrCalculateTransitChart(chart, date)
        val dashaTimeline = DashaCalculator.calculateDashaTimeline(chart)
        val activeDasha = formatActiveDasha(dashaTimeline)

        val transitMoon = transitChart.planetPositions.find { it.planet == Planet.MOON }
        val natalMoon = chart.planetPositions.find { it.planet == Planet.MOON }
        
        val moonSign = transitMoon?.sign ?: natalMoon?.sign ?: ZodiacSign.ARIES
        val moonNakshatra = transitMoon?.nakshatra ?: Nakshatra.ASHWINI
        val moonHouse = natalMoon?.house ?: 1

        val planetaryInfluences = analyzePlanetaryInfluences(chart, transitChart, moonHouse)
        val lifeAreaPredictions = calculateLifeAreaPredictions(chart, transitChart, dashaTimeline, date)
        val overallEnergy = calculateOverallEnergy(planetaryInfluences, lifeAreaPredictions, dashaTimeline)
        val (theme, themeDescription) = calculateDailyTheme(chart, transitChart, dashaTimeline, date)
        val luckyElements = calculateLuckyElements(chart, transitChart, date)
        val recommendations = generateRecommendations(chart, transitChart, dashaTimeline, lifeAreaPredictions)
        val cautions = generateCautions(chart, transitChart, planetaryInfluences)
        val affirmation = generateAffirmation(dashaTimeline, moonSign, date)

        return DailyHoroscope(
            date = date,
            theme = theme,
            themeDescription = themeDescription,
            overallEnergy = overallEnergy,
            moonSign = moonSign,
            moonNakshatra = moonNakshatra,
            activeDasha = activeDasha,
            lifeAreas = lifeAreaPredictions,
            luckyElements = luckyElements,
            planetaryInfluences = planetaryInfluences,
            recommendations = recommendations,
            cautions = cautions,
            affirmation = affirmation
        ).also { dailyHoroscopeCache[cacheKey] = it }
    }

    fun calculateDailyHoroscopeSafe(chart: VedicChart, date: LocalDate = LocalDate.now()): HoroscopeResult<DailyHoroscope> {
        return try {
            HoroscopeResult.Success(calculateDailyHoroscope(chart, date))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate daily horoscope for $date", e)
            HoroscopeResult.Error("Unable to calculate horoscope for $date: ${e.message}", e)
        }
    }

    fun calculateWeeklyHoroscope(chart: VedicChart, startDate: LocalDate = LocalDate.now()): WeeklyHoroscope {
        val endDate = startDate.plusDays(6)
        val dashaTimeline = DashaCalculator.calculateDashaTimeline(chart)

        val dailyHoroscopes = (0 until 7).mapNotNull { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            try {
                calculateDailyHoroscope(chart, date)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to calculate horoscope for $date, using fallback", e)
                null
            }
        }

        val dailyHighlights = dailyHoroscopes.map { horoscope ->
            DailyHighlight(
                date = horoscope.date,
                dayOfWeek = horoscope.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                energy = horoscope.overallEnergy,
                focus = horoscope.theme,
                brief = horoscope.themeDescription.take(100) + 
                        if (horoscope.themeDescription.length > 100) "..." else ""
            )
        }.ifEmpty {
            (0 until 7).map { dayOffset ->
                val date = startDate.plusDays(dayOffset.toLong())
                DailyHighlight(
                    date = date,
                    dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    energy = 5,
                    focus = "Balance",
                    brief = "Steady energy expected"
                )
            }
        }

        val keyDates = calculateKeyDates(chart, startDate, endDate)
        val weeklyPredictions = calculateWeeklyPredictionsOptimized(dailyHoroscopes, dashaTimeline)
        val (weeklyTheme, weeklyOverview) = calculateWeeklyTheme(chart, dashaTimeline, startDate, dailyHighlights)
        val weeklyAdvice = generateWeeklyAdvice(chart, dashaTimeline, keyDates)

        return WeeklyHoroscope(
            startDate = startDate,
            endDate = endDate,
            weeklyTheme = weeklyTheme,
            weeklyOverview = weeklyOverview,
            keyDates = keyDates,
            weeklyPredictions = weeklyPredictions,
            dailyHighlights = dailyHighlights,
            weeklyAdvice = weeklyAdvice
        )
    }

    fun calculateWeeklyHoroscopeSafe(chart: VedicChart, startDate: LocalDate = LocalDate.now()): HoroscopeResult<WeeklyHoroscope> {
        return try {
            HoroscopeResult.Success(calculateWeeklyHoroscope(chart, startDate))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate weekly horoscope starting $startDate", e)
            HoroscopeResult.Error("Unable to calculate weekly horoscope: ${e.message}", e)
        }
    }

    private fun getOrCalculateTransitChart(chart: VedicChart, date: LocalDate): VedicChart {
        val cacheKey = TransitCacheKey(
            date = date,
            latitude = chart.birthData.latitude,
            longitude = chart.birthData.longitude,
            timezone = chart.birthData.timezone
        )

        transitCache[cacheKey]?.let { return it }

        val transitDateTime = LocalDateTime.of(date, LocalTime.of(6, 0))
        val transitBirthData = BirthData(
            name = "Transit",
            dateTime = transitDateTime,
            latitude = chart.birthData.latitude,
            longitude = chart.birthData.longitude,
            timezone = chart.birthData.timezone,
            location = chart.birthData.location
        )

        return try {
            ephemerisEngine.calculateVedicChart(transitBirthData).also {
                transitCache[cacheKey] = it
                cleanupCacheIfNeeded()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Transit calculation failed for $date: ${e.message}", e)
            throw HoroscopeCalculationException(
                "Unable to calculate planetary positions for $date. " +
                "This may be due to ephemeris data limitations for this date range.",
                e
            )
        }
    }

    private fun cleanupCacheIfNeeded() {
        if (transitCache.size > MAX_TRANSIT_CACHE_SIZE) {
            val today = LocalDate.now()
            transitCache.keys.filter { it.date.isBefore(today.minusDays(7)) }
                .forEach { transitCache.remove(it) }
        }
        if (dailyHoroscopeCache.size > MAX_HOROSCOPE_CACHE_SIZE) {
            val today = LocalDate.now()
            dailyHoroscopeCache.keys.filter { it.date.isBefore(today.minusDays(7)) }
                .forEach { dailyHoroscopeCache.remove(it) }
        }
    }

    private fun formatActiveDasha(timeline: DashaCalculator.DashaTimeline): String {
        return buildString {
            timeline.currentMahadasha?.let { md ->
                append(md.planet.displayName)
                timeline.currentAntardasha?.let { ad ->
                    append("-${ad.planet.displayName}")
                }
            } ?: append("Calculating...")
        }
    }

    private fun analyzePlanetaryInfluences(
        natalChart: VedicChart,
        transitChart: VedicChart,
        natalMoonHouse: Int
    ): List<PlanetaryInfluence> {
        return transitChart.planetPositions
            .filter { it.planet in Planet.MAIN_PLANETS }
            .map { transitPos ->
                val natalPos = natalChart.planetPositions.find { it.planet == transitPos.planet }
                val houseFromMoon = calculateHouseFromMoon(transitPos.sign, natalChart)
                val (influence, strength, isPositive) = analyzeGocharaEffect(
                    transitPos.planet,
                    houseFromMoon,
                    transitPos.isRetrograde,
                    natalPos
                )
                PlanetaryInfluence(
                    planet = transitPos.planet,
                    influence = influence,
                    strength = strength,
                    isPositive = isPositive
                )
            }
            .sortedByDescending { it.strength }
    }

    private fun calculateHouseFromMoon(transitSign: ZodiacSign, natalChart: VedicChart): Int {
        val natalMoon = natalChart.planetPositions.find { it.planet == Planet.MOON }
        val natalMoonSign = natalMoon?.sign ?: ZodiacSign.ARIES
        val moonSignIndex = natalMoonSign.ordinal
        val transitSignIndex = transitSign.ordinal
        return ((transitSignIndex - moonSignIndex + 12) % 12) + 1
    }

    private fun analyzeGocharaEffect(
        planet: Planet,
        houseFromMoon: Int,
        isRetrograde: Boolean,
        natalPosition: PlanetPosition?
    ): Triple<String, Int, Boolean> {
        val favorableHouses = GOCHARA_FAVORABLE_HOUSES[planet] ?: emptyList()
        val isFavorable = houseFromMoon in favorableHouses

        val (influence, baseStrength) = getGocharaInfluence(planet, houseFromMoon, isFavorable)
        
        val adjustedStrength = when {
            isRetrograde && isFavorable -> baseStrength - 1
            isRetrograde && !isFavorable -> baseStrength + 1
            else -> baseStrength
        }.coerceIn(1, 10)

        return Triple(influence, adjustedStrength, isFavorable)
    }

    private fun getGocharaInfluence(planet: Planet, house: Int, isFavorable: Boolean): Pair<String, Int> {
        return when {
            isFavorable -> FAVORABLE_GOCHARA_EFFECTS[planet]?.get(house)
                ?: ("Positive ${planet.displayName} energy" to 7)
            else -> UNFAVORABLE_GOCHARA_EFFECTS[planet]?.get(house)
                ?: ("Challenging ${planet.displayName} transit" to 4)
        }
    }

    private fun calculateLifeAreaPredictions(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        date: LocalDate
    ): List<LifeAreaPrediction> {
        return LifeArea.entries.map { area ->
            val (rating, prediction, advice) = analyzeLifeArea(chart, transitChart, dashaTimeline, area, date)
            LifeAreaPrediction(area = area, rating = rating, prediction = prediction, advice = advice)
        }
    }

    private fun analyzeLifeArea(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        area: LifeArea,
        date: LocalDate
    ): Triple<Int, String, String> {
        val primaryHouse = area.houses.first()
        val primaryHouseCusp = chart.houseCusps.getOrElse(primaryHouse - 1) { 0.0 }
        val houseLordSign = ZodiacSign.fromLongitude(primaryHouseCusp)
        val houseLord = houseLordSign.ruler

        val dashaInfluence = calculateDashaInfluenceOnArea(dashaTimeline, area, chart)
        val transitInfluence = calculateTransitInfluenceOnArea(chart, transitChart, area)
        val rating = ((dashaInfluence + transitInfluence) / 2).coerceIn(1, 5)

        val (prediction, advice) = generateAreaPrediction(area, rating, dashaTimeline, houseLord, date)
        return Triple(rating, prediction, advice)
    }

    private fun calculateDashaInfluenceOnArea(
        timeline: DashaCalculator.DashaTimeline,
        area: LifeArea,
        chart: VedicChart
    ): Int {
        val currentMahadasha = timeline.currentMahadasha ?: return 3
        val mahadashaPlanet = currentMahadasha.planet
        val planetPosition = chart.planetPositions.find { it.planet == mahadashaPlanet }
        val planetHouse = planetPosition?.house ?: return 3
        val isInAreaHouse = planetHouse in area.houses
        val isBenefic = mahadashaPlanet in NATURAL_BENEFICS

        return when {
            isInAreaHouse && isBenefic -> 5
            isInAreaHouse -> 4
            isBenefic -> 4
            mahadashaPlanet in NATURAL_MALEFICS -> 3
            else -> 3
        }
    }

    private fun calculateTransitInfluenceOnArea(
        natalChart: VedicChart,
        transitChart: VedicChart,
        area: LifeArea
    ): Int {
        var score = 3
        transitChart.planetPositions.forEach { transitPos ->
            val houseFromMoon = calculateHouseFromMoon(transitPos.sign, natalChart)
            if (houseFromMoon in area.houses) {
                score += when (transitPos.planet) {
                    in NATURAL_BENEFICS -> 1
                    in NATURAL_MALEFICS -> -1
                    else -> 0
                }
            }
        }
        return score.coerceIn(1, 5)
    }

    private fun generateAreaPrediction(
        area: LifeArea,
        rating: Int,
        dashaTimeline: DashaCalculator.DashaTimeline,
        houseLord: Planet,
        date: LocalDate
    ): Pair<String, String> {
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet?.displayName ?: "current"
        val predictions = LIFE_AREA_PREDICTIONS[area] ?: emptyMap()
        val advices = LIFE_AREA_ADVICES[area] ?: emptyMap()
        
        val prediction = predictions[rating]?.replace("{dashaLord}", currentDashaLord)
            ?: "Balanced energy in this area."
        val advice = advices[getRatingCategory(rating)] ?: "Stay mindful and balanced."
        
        return Pair(prediction, advice)
    }

    private fun getRatingCategory(rating: Int): String = when {
        rating >= 4 -> "high"
        rating >= 3 -> "medium"
        else -> "low"
    }

    private fun calculateOverallEnergy(
        influences: List<PlanetaryInfluence>,
        lifeAreas: List<LifeAreaPrediction>,
        dashaTimeline: DashaCalculator.DashaTimeline
    ): Int {
        val planetaryAvg = influences.map { it.strength }.average().takeIf { !it.isNaN() } ?: 5.0
        val lifeAreaAvg = lifeAreas.map { it.rating * 2.0 }.average().takeIf { !it.isNaN() } ?: 5.0

        val dashaBonus = DASHA_ENERGY_MODIFIERS[dashaTimeline.currentMahadasha?.planet] ?: 0.0
        val rawEnergy = (planetaryAvg * 0.4) + (lifeAreaAvg * 0.4) + (5.0 + dashaBonus) * 0.2
        
        return rawEnergy.toInt().coerceIn(1, 10)
    }

    private fun calculateDailyTheme(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        date: LocalDate
    ): Pair<String, String> {
        val moonSign = transitChart.planetPositions.find { it.planet == Planet.MOON }?.sign ?: ZodiacSign.ARIES
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN

        val theme = determineTheme(moonSign, currentDashaLord)
        val description = THEME_DESCRIPTIONS[theme] ?: DEFAULT_THEME_DESCRIPTION
        
        return Pair(theme, description)
    }

    private fun determineTheme(moonSign: ZodiacSign, dashaLord: Planet): String {
        val moonElement = moonSign.element
        
        return when {
            moonElement == "Fire" && dashaLord in listOf(Planet.SUN, Planet.MARS, Planet.JUPITER) -> "Dynamic Action"
            moonElement == "Earth" && dashaLord in listOf(Planet.VENUS, Planet.MERCURY, Planet.SATURN) -> "Practical Progress"
            moonElement == "Air" && dashaLord in listOf(Planet.MERCURY, Planet.VENUS, Planet.SATURN) -> "Social Connections"
            moonElement == "Water" && dashaLord in listOf(Planet.MOON, Planet.MARS, Planet.JUPITER) -> "Emotional Insight"
            else -> DASHA_LORD_THEMES[dashaLord] ?: "Balance & Equilibrium"
        }
    }

    private fun calculateLuckyElements(
        chart: VedicChart,
        transitChart: VedicChart,
        date: LocalDate
    ): LuckyElements {
        val moonSign = transitChart.planetPositions.find { it.planet == Planet.MOON }?.sign ?: ZodiacSign.ARIES
        val dayOfWeek = date.dayOfWeek.value
        val ascSign = ZodiacSign.fromLongitude(chart.ascendant)

        val luckyNumber = ((dayOfWeek + moonSign.ordinal) % 9) + 1
        val luckyColor = ELEMENT_COLORS[moonSign.element] ?: "White"
        val luckyDirection = PLANET_DIRECTIONS[ascSign.ruler] ?: "East"
        val luckyTime = DAY_HORA_TIMES[dayOfWeek] ?: "Morning hours"
        val gemstone = PLANET_GEMSTONES[ascSign.ruler] ?: "Clear Quartz"

        return LuckyElements(
            number = luckyNumber,
            color = luckyColor,
            direction = luckyDirection,
            time = luckyTime,
            gemstone = gemstone
        )
    }

    private fun generateRecommendations(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        lifeAreas: List<LifeAreaPrediction>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        dashaTimeline.currentMahadasha?.planet?.let { dashaLord ->
            DASHA_RECOMMENDATIONS[dashaLord]?.let { recommendations.add(it) }
        }

        lifeAreas.maxByOrNull { it.rating }?.let { bestArea ->
            BEST_AREA_RECOMMENDATIONS[bestArea.area]?.let { recommendations.add(it) }
        }

        transitChart.planetPositions.find { it.planet == Planet.MOON }?.let { moon ->
            ELEMENT_RECOMMENDATIONS[moon.sign.element]?.let { recommendations.add(it) }
        }

        return recommendations.take(3)
    }

    private fun generateCautions(
        chart: VedicChart,
        transitChart: VedicChart,
        influences: List<PlanetaryInfluence>
    ): List<String> {
        val cautions = mutableListOf<String>()

        influences.filter { !it.isPositive && it.strength <= 4 }
            .take(2)
            .forEach { influence ->
                PLANET_CAUTIONS[influence.planet]?.let { cautions.add(it) }
            }

        transitChart.planetPositions
            .filter { it.isRetrograde && it.planet in Planet.MAIN_PLANETS }
            .take(1)
            .forEach {
                cautions.add("${it.planet.displayName} is retrograde - review and reconsider rather than initiate.")
            }

        return cautions.take(2)
    }

    private fun generateAffirmation(
        dashaTimeline: DashaCalculator.DashaTimeline,
        moonSign: ZodiacSign,
        date: LocalDate
    ): String {
        val dashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN
        return DASHA_AFFIRMATIONS[dashaLord] ?: "I am aligned with cosmic energies and flow with life's rhythm."
    }

    private fun calculateKeyDates(chart: VedicChart, startDate: LocalDate, endDate: LocalDate): List<KeyDate> {
        val keyDates = mutableListOf<KeyDate>()

        LUNAR_PHASES.forEach { (dayOffset, event, significance) ->
            val date = startDate.plusDays(dayOffset.toLong())
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                keyDates.add(KeyDate(date, event, significance, true))
            }
        }

        (0 until 7).forEach { offset ->
            val date = startDate.plusDays(offset.toLong())
            FAVORABLE_DAYS[date.dayOfWeek]?.let { desc ->
                keyDates.add(KeyDate(
                    date = date,
                    event = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    significance = desc,
                    isPositive = true
                ))
            }
        }

        return keyDates.distinctBy { it.date }.take(4)
    }

    private fun calculateWeeklyPredictionsOptimized(
        dailyHoroscopes: List<DailyHoroscope>,
        dashaTimeline: DashaCalculator.DashaTimeline
    ): Map<LifeArea, String> {
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet?.displayName ?: "current"
        
        return LifeArea.entries.associateWith { area ->
            val weeklyRatings = dailyHoroscopes.mapNotNull { horoscope ->
                horoscope.lifeAreas.find { it.area == area }?.rating
            }
            val avgRating = if (weeklyRatings.isNotEmpty()) weeklyRatings.average() else 3.0
            
            generateWeeklyAreaPrediction(area, avgRating, currentDashaLord)
        }
    }

    private fun generateWeeklyAreaPrediction(area: LifeArea, avgRating: Double, dashaLord: String): String {
        val ratingCategory = when {
            avgRating >= 4 -> "excellent"
            avgRating >= 3 -> "steady"
            else -> "challenging"
        }
        
        return WEEKLY_PREDICTIONS[area]?.get(ratingCategory)?.replace("{dashaLord}", dashaLord)
            ?: "A balanced week for ${area.displayName.lowercase()}."
    }

    private fun calculateWeeklyTheme(
        chart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        startDate: LocalDate,
        dailyHighlights: List<DailyHighlight>
    ): Pair<String, String> {
        val avgEnergy = dailyHighlights.map { it.energy }.average().takeIf { !it.isNaN() } ?: 5.0
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN

        val theme = when {
            avgEnergy >= 7 -> "Week of Opportunities"
            avgEnergy >= 5 -> "Steady Progress"
            else -> "Mindful Navigation"
        }

        val overview = buildWeeklyOverview(currentDashaLord, avgEnergy, dailyHighlights)
        return Pair(theme, overview)
    }

    private fun buildWeeklyOverview(
        dashaLord: Planet,
        avgEnergy: Double,
        dailyHighlights: List<DailyHighlight>
    ): String {
        return buildString {
            append("This week under your ${dashaLord.displayName} Mahadasha brings ")
            append(when {
                avgEnergy >= 7 -> "excellent opportunities for growth and success. "
                avgEnergy >= 5 -> "steady progress and balanced energy. "
                else -> "challenges that, when navigated wisely, lead to growth. "
            })

            dailyHighlights.maxByOrNull { it.energy }?.let {
                append("${it.dayOfWeek} appears most favorable for important activities. ")
            }

            dailyHighlights.minByOrNull { it.energy }?.let {
                if (it.energy < 5) {
                    append("${it.dayOfWeek} may require extra patience and care. ")
                }
            }

            append("Trust in your cosmic guidance and make the most of each day's unique energy.")
        }
    }

    private fun generateWeeklyAdvice(
        chart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        keyDates: List<KeyDate>
    ): String {
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN
        val baseAdvice = DASHA_WEEKLY_ADVICE[currentDashaLord] ?: "maintain balance and trust in divine timing."

        return buildString {
            append("During this ${currentDashaLord.displayName} period, ")
            append(baseAdvice)
            keyDates.firstOrNull { it.isPositive }?.let {
                append(" Mark ${it.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))} for important initiatives.")
            }
        }
    }

    fun clearCache() {
        transitCache.clear()
        dailyHoroscopeCache.clear()
    }

    override fun close() {
        try {
            clearCache()
            ephemerisEngine.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing ephemeris engine", e)
        }
    }

    class HoroscopeCalculationException(message: String, cause: Throwable? = null) : Exception(message, cause)

    companion object {
        private const val TAG = "HoroscopeCalculator"
        private const val MAX_TRANSIT_CACHE_SIZE = 30
        private const val MAX_HOROSCOPE_CACHE_SIZE = 50

        private val NATURAL_BENEFICS = listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
        private val NATURAL_MALEFICS = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)

        private val GOCHARA_FAVORABLE_HOUSES = mapOf(
            Planet.SUN to listOf(3, 6, 10, 11),
            Planet.MOON to listOf(1, 3, 6, 7, 10, 11),
            Planet.MARS to listOf(3, 6, 11),
            Planet.MERCURY to listOf(2, 4, 6, 8, 10, 11),
            Planet.JUPITER to listOf(2, 5, 7, 9, 11),
            Planet.VENUS to listOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
            Planet.SATURN to listOf(3, 6, 11),
            Planet.RAHU to listOf(3, 6, 10, 11),
            Planet.KETU to listOf(3, 6, 9, 11)
        )

        private val FAVORABLE_GOCHARA_EFFECTS = mapOf(
            Planet.JUPITER to mapOf(
                2 to ("Wealth and family prosperity" to 9),
                5 to ("Intelligence, children, and creativity flourish" to 9),
                7 to ("Partnership and relationship harmony" to 8),
                9 to ("Fortune, spirituality, and wisdom" to 10),
                11 to ("Gains and fulfillment of desires" to 9)
            ),
            Planet.SATURN to mapOf(
                3 to ("Courage and perseverance" to 7),
                6 to ("Victory over obstacles" to 8),
                11 to ("Long-term gains through patience" to 8)
            ),
            Planet.VENUS to mapOf(
                1 to ("Personal charm and attractiveness" to 8),
                4 to ("Domestic happiness and comfort" to 8),
                5 to ("Romance and creative expression" to 9),
                9 to ("Fortune through relationships" to 8)
            )
        )

        private val UNFAVORABLE_GOCHARA_EFFECTS = mapOf(
            Planet.SATURN to mapOf(
                1 to ("Physical challenges require attention" to 4),
                4 to ("Domestic stress possible" to 3),
                8 to ("Health vigilance needed" to 3),
                12 to ("Expenses and isolation feeling" to 4)
            ),
            Planet.MARS to mapOf(
                1 to ("Impulsive actions may backfire" to 4),
                4 to ("Family tensions possible" to 4),
                7 to ("Relationship conflicts" to 3),
                8 to ("Health caution needed" to 3),
                12 to ("Hidden enemies active" to 4)
            )
        )

        private val DASHA_ENERGY_MODIFIERS = mapOf(
            Planet.JUPITER to 1.5,
            Planet.VENUS to 1.5,
            Planet.MERCURY to 1.0,
            Planet.MOON to 1.0,
            Planet.SUN to 0.5,
            Planet.SATURN to -0.5,
            Planet.MARS to -0.5,
            Planet.RAHU to -1.0,
            Planet.KETU to -1.0
        )

        private val DASHA_LORD_THEMES = mapOf(
            Planet.JUPITER to "Expansion & Wisdom",
            Planet.VENUS to "Harmony & Beauty",
            Planet.SATURN to "Discipline & Growth",
            Planet.MERCURY to "Communication & Learning",
            Planet.MARS to "Energy & Initiative",
            Planet.SUN to "Self-Expression",
            Planet.MOON to "Intuition & Nurturing",
            Planet.RAHU to "Transformation",
            Planet.KETU to "Spiritual Liberation"
        )

        private val THEME_DESCRIPTIONS = mapOf(
            "Dynamic Action" to "Your energy is high and aligned with fire elements. This is an excellent day for taking initiative, starting new projects, and asserting yourself confidently. Channel this vibrant energy into productive pursuits.",
            "Practical Progress" to "Grounded earth energy supports methodical progress today. Focus on practical tasks, financial planning, and building stable foundations. Your efforts will yield tangible results.",
            "Social Connections" to "Air element energy enhances communication and social interactions. Networking, negotiations, and intellectual pursuits are favored. Express your ideas and connect with like-minded people.",
            "Emotional Insight" to "Water element energy deepens your intuition and emotional awareness. Trust your feelings and pay attention to subtle cues. This is a powerful day for healing and self-reflection.",
            "Expansion & Wisdom" to "Jupiter's benevolent influence brings opportunities for growth, learning, and good fortune. Be open to new possibilities and share your wisdom generously.",
            "Harmony & Beauty" to "Venus graces you with appreciation for beauty, art, and relationships. Indulge in pleasurable activities and nurture your connections with loved ones.",
            "Discipline & Growth" to "Saturn's influence calls for patience, hard work, and responsibility. Embrace challenges as opportunities for growth and stay committed to your long-term goals.",
            "Communication & Learning" to "Mercury enhances your mental agility and communication skills. This is ideal for learning, teaching, writing, and all forms of information exchange.",
            "Energy & Initiative" to "Mars provides courage and drive. Take bold action, compete with integrity, and channel aggressive energy into constructive activities.",
            "Self-Expression" to "The Sun illuminates your path to self-expression and leadership. Shine your light confidently and pursue activities that bring you recognition.",
            "Intuition & Nurturing" to "The Moon heightens your sensitivity and caring nature. Nurture yourself and others, and trust your instincts in important decisions.",
            "Transformation" to "Rahu's influence brings unconventional opportunities and desires for change. Embrace innovation but stay grounded in your values.",
            "Spiritual Liberation" to "Ketu's energy supports detachment and spiritual insight. Let go of what no longer serves you and focus on inner growth.",
            "Balance & Equilibrium" to "A day of balance where all energies are in equilibrium. Maintain steadiness and make measured progress in all areas of life."
        )

        private const val DEFAULT_THEME_DESCRIPTION = "A day of balance where all energies are in equilibrium. Maintain steadiness and make measured progress in all areas of life."

        private val ELEMENT_COLORS = mapOf(
            "Fire" to "Red, Orange, or Gold",
            "Earth" to "Green, Brown, or White",
            "Air" to "Blue, Light Blue, or Silver",
            "Water" to "White, Cream, or Sea Green"
        )

        private val PLANET_DIRECTIONS = mapOf(
            Planet.SUN to "East",
            Planet.MARS to "East",
            Planet.MOON to "North-West",
            Planet.VENUS to "South-East",
            Planet.MERCURY to "North",
            Planet.JUPITER to "North-East",
            Planet.SATURN to "West",
            Planet.RAHU to "South-West",
            Planet.KETU to "North-West"
        )

        private val DAY_HORA_TIMES = mapOf(
            1 to "6:00 AM - 7:00 AM (Sun Hora)",
            2 to "7:00 AM - 8:00 AM (Moon Hora)",
            3 to "8:00 AM - 9:00 AM (Mars Hora)",
            4 to "9:00 AM - 10:00 AM (Mercury Hora)",
            5 to "10:00 AM - 11:00 AM (Jupiter Hora)",
            6 to "11:00 AM - 12:00 PM (Venus Hora)",
            7 to "5:00 PM - 6:00 PM (Saturn Hora)"
        )

        private val PLANET_GEMSTONES = mapOf(
            Planet.SUN to "Ruby",
            Planet.MOON to "Pearl",
            Planet.MARS to "Red Coral",
            Planet.MERCURY to "Emerald",
            Planet.JUPITER to "Yellow Sapphire",
            Planet.VENUS to "Diamond or White Sapphire",
            Planet.SATURN to "Blue Sapphire",
            Planet.RAHU to "Hessonite",
            Planet.KETU to "Cat's Eye"
        )

        private val DASHA_RECOMMENDATIONS = mapOf(
            Planet.SUN to "Engage in activities that build confidence and leadership skills.",
            Planet.MOON to "Prioritize emotional well-being and nurturing relationships.",
            Planet.MARS to "Channel your energy into physical activities and competitive pursuits.",
            Planet.MERCURY to "Focus on learning, communication, and intellectual growth.",
            Planet.JUPITER to "Expand your horizons through education, travel, or spiritual practices.",
            Planet.VENUS to "Cultivate beauty, art, and harmonious relationships.",
            Planet.SATURN to "Embrace discipline, hard work, and long-term planning.",
            Planet.RAHU to "Explore unconventional paths while staying grounded.",
            Planet.KETU to "Practice detachment and focus on spiritual development."
        )

        private val BEST_AREA_RECOMMENDATIONS = mapOf(
            LifeArea.CAREER to "Capitalize on favorable career energy today.",
            LifeArea.LOVE to "Nurture your relationships with extra attention.",
            LifeArea.HEALTH to "Make the most of your vibrant health energy.",
            LifeArea.FINANCE to "Take advantage of positive financial influences.",
            LifeArea.FAMILY to "Spend quality time with family members.",
            LifeArea.SPIRITUALITY to "Deepen your spiritual practices."
        )

        private val ELEMENT_RECOMMENDATIONS = mapOf(
            "Fire" to "Take bold action and express yourself confidently.",
            "Earth" to "Focus on practical matters and material progress.",
            "Air" to "Engage in social activities and intellectual pursuits.",
            "Water" to "Trust your intuition and honor your emotions."
        )

        private val PLANET_CAUTIONS = mapOf(
            Planet.SATURN to "Avoid rushing into decisions. Patience is key.",
            Planet.MARS to "Control impulsive reactions and avoid conflicts.",
            Planet.RAHU to "Be wary of deception and unrealistic expectations.",
            Planet.KETU to "Don't neglect practical responsibilities for escapism."
        )

        private val DASHA_AFFIRMATIONS = mapOf(
            Planet.SUN to "I shine my light confidently and inspire those around me.",
            Planet.MOON to "I trust my intuition and nurture myself with compassion.",
            Planet.MARS to "I channel my energy constructively and act with courage.",
            Planet.MERCURY to "I communicate clearly and embrace continuous learning.",
            Planet.JUPITER to "I am open to abundance and share my wisdom generously.",
            Planet.VENUS to "I attract beauty and harmony into my life.",
            Planet.SATURN to "I embrace discipline and trust in the timing of my journey.",
            Planet.RAHU to "I embrace change and transform challenges into opportunities.",
            Planet.KETU to "I release what no longer serves me and embrace spiritual growth."
        )

        private val LUNAR_PHASES = listOf(
            Triple(7, "First Quarter Moon", "Good for taking action"),
            Triple(14, "Full Moon", "Emotional peak - completion energy")
        )

        private val FAVORABLE_DAYS = mapOf(
            java.time.DayOfWeek.THURSDAY to "Jupiter's day - auspicious for growth",
            java.time.DayOfWeek.FRIDAY to "Venus's day - favorable for relationships"
        )

        private val DASHA_WEEKLY_ADVICE = mapOf(
            Planet.JUPITER to "embrace opportunities for learning and expansion. Your wisdom and optimism attract positive outcomes.",
            Planet.VENUS to "focus on cultivating beauty, harmony, and meaningful relationships. Artistic pursuits are favored.",
            Planet.SATURN to "embrace discipline and patience. Hard work now builds lasting foundations for the future.",
            Planet.MERCURY to "prioritize communication, learning, and intellectual activities. Your mind is sharp.",
            Planet.MARS to "channel your energy into productive activities. Exercise and competition are favored.",
            Planet.SUN to "let your light shine. Leadership roles and self-expression bring recognition.",
            Planet.MOON to "honor your emotions and intuition. Nurturing activities bring fulfillment.",
            Planet.RAHU to "embrace transformation while staying grounded. Unconventional approaches may succeed.",
            Planet.KETU to "focus on spiritual growth and letting go. Detachment brings peace."
        )

        private val LIFE_AREA_PREDICTIONS = mapOf(
            LifeArea.CAREER to mapOf(
                5 to "Excellent day for professional advancement. Your {dashaLord} period brings recognition and success in work matters.",
                4 to "Good energy for career activities. Focus on important projects and networking.",
                3 to "Steady progress in professional matters. Maintain consistency in your efforts.",
                2 to "Some workplace challenges may arise. Stay patient and diplomatic.",
                1 to "Career matters require extra attention. Avoid major decisions today."
            ),
            LifeArea.LOVE to mapOf(
                5 to "Romantic energy is at its peak. Deep connections and meaningful conversations await.",
                4 to "Favorable time for relationships. Express your feelings openly.",
                3 to "Balanced energy in partnerships. Focus on understanding and compromise.",
                2 to "Minor misunderstandings possible. Practice patience with loved ones.",
                1 to "Relationships need nurturing. Avoid conflicts and be extra considerate."
            ),
            LifeArea.HEALTH to mapOf(
                5 to "Vitality is strong. Great day for physical activities and wellness routines.",
                4 to "Good health energy. Maintain your wellness practices.",
                3 to "Steady health. Focus on rest and balanced nutrition.",
                2 to "Energy may fluctuate. Prioritize adequate rest.",
                1 to "Health needs attention. Take it easy and avoid overexertion."
            ),
            LifeArea.FINANCE to mapOf(
                5 to "Excellent day for financial matters. Opportunities for gains are strong.",
                4 to "Positive financial energy. Good for planned investments.",
                3 to "Stable financial period. Stick to your budget.",
                2 to "Be cautious with expenditures. Avoid impulsive purchases.",
                1 to "Financial caution advised. Postpone major financial decisions."
            ),
            LifeArea.FAMILY to mapOf(
                5 to "Harmonious family energy. Celebrations and joyful gatherings are favored.",
                4 to "Good time for family bonding. Support flows both ways.",
                3 to "Steady domestic atmosphere. Focus on routine family matters.",
                2 to "Minor family tensions possible. Practice understanding.",
                1 to "Family dynamics need attention. Prioritize peace and harmony."
            ),
            LifeArea.SPIRITUALITY to mapOf(
                5 to "Profound spiritual insights available. Meditation and reflection are highly beneficial.",
                4 to "Good day for spiritual practices. Inner guidance is strong.",
                3 to "Steady spiritual energy. Maintain your regular practices.",
                2 to "Spiritual connection may feel distant. Keep faith.",
                1 to "Inner turbulence possible. Ground yourself through simple practices."
            )
        )

        private val LIFE_AREA_ADVICES = mapOf(
            LifeArea.CAREER to mapOf(
                "high" to "Take initiative on important projects.",
                "medium" to "Stay focused on your regular responsibilities.",
                "low" to "Plan ahead and prepare for opportunities."
            ),
            LifeArea.LOVE to mapOf(
                "high" to "Express your feelings authentically.",
                "medium" to "Listen more than you speak.",
                "low" to "Give space and practice patience."
            ),
            LifeArea.HEALTH to mapOf(
                "high" to "Engage in physical activity you enjoy.",
                "medium" to "Maintain balanced meals and hydration.",
                "low" to "Rest is your best medicine today."
            ),
            LifeArea.FINANCE to mapOf(
                "high" to "Review investment opportunities.",
                "medium" to "Stick to planned expenses.",
                "low" to "Save rather than spend today."
            ),
            LifeArea.FAMILY to mapOf(
                "high" to "Plan a family activity together.",
                "medium" to "Be present for family members.",
                "low" to "Choose harmony over being right."
            ),
            LifeArea.SPIRITUALITY to mapOf(
                "high" to "Dedicate extra time to meditation.",
                "medium" to "Find moments of stillness.",
                "low" to "Simple prayers bring comfort."
            )
        )

        private val WEEKLY_PREDICTIONS = mapOf(
            LifeArea.CAREER to mapOf(
                "excellent" to "An exceptional week for career advancement. Your {dashaLord} period supports professional recognition. Key meetings and projects will progress smoothly.",
                "steady" to "Steady professional progress this week. Focus on completing pending tasks and building relationships with colleagues.",
                "challenging" to "Career matters may require extra effort this week. Stay patient and avoid major changes."
            ),
            LifeArea.LOVE to mapOf(
                "excellent" to "Romantic energy flows abundantly this week. Single or committed, relationships deepen. Express your feelings openly.",
                "steady" to "Balanced relationship energy. Good for maintaining harmony and working through minor issues together.",
                "challenging" to "Relationships need nurturing this week. Practice patience and understanding with your partner."
            ),
            LifeArea.HEALTH to mapOf(
                "excellent" to "Excellent vitality this week! Great time to start new fitness routines or health practices. Energy levels are high.",
                "steady" to "Stable health week. Maintain your regular wellness routines and stay consistent with rest.",
                "challenging" to "Health vigilance needed this week. Prioritize rest, nutrition, and stress management."
            ),
            LifeArea.FINANCE to mapOf(
                "excellent" to "Prosperous week for finances. Opportunities for gains through investments or new income sources. Review financial plans.",
                "steady" to "Stable financial week. Good for routine money management and planned purchases.",
                "challenging" to "Financial caution advised this week. Avoid impulsive spending and postpone major investments."
            ),
            LifeArea.FAMILY to mapOf(
                "excellent" to "Harmonious family week ahead. Celebrations, gatherings, and quality time strengthen bonds. Support flows both ways.",
                "steady" to "Good week for family matters. Focus on communication and shared activities.",
                "challenging" to "Family dynamics may be challenging this week. Choose understanding over confrontation."
            ),
            LifeArea.SPIRITUALITY to mapOf(
                "excellent" to "Profound spiritual week. Meditation, reflection, and inner guidance are heightened. Seek meaningful experiences.",
                "steady" to "Steady spiritual energy. Maintain your practices and stay connected to your inner self.",
                "challenging" to "Spiritual connection may feel elusive. Simple practices and patience will help restore balance."
            )
        )
    }
}