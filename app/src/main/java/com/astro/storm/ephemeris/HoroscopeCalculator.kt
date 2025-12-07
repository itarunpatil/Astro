package com.astro.storm.ephemeris

import android.content.Context
import android.util.Log
import com.astro.storm.data.model.*
import kotlinx.coroutines.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class HoroscopeCalculator(context: Context) : AutoCloseable {

    private val appContext = context.applicationContext
    private val ephemerisEngine by lazy { SwissEphemerisEngine.create(appContext) }

    private val transitCache = LinkedHashMap<TransitCacheKey, VedicChart>(MAX_TRANSIT_CACHE_SIZE + 1, 0.75f, true)
    private val transitCacheLock = ReentrantReadWriteLock()

    private val dailyHoroscopeCache = LinkedHashMap<DailyHoroscopeCacheKey, DailyHoroscope>(MAX_HOROSCOPE_CACHE_SIZE + 1, 0.75f, true)
    private val horoscopeCacheLock = ReentrantReadWriteLock()

    private val dashaCache = ConcurrentHashMap<ChartIdentifier, DashaCalculator.DashaTimeline>()
    private val ashtakavargaCache = ConcurrentHashMap<ChartIdentifier, AshtakavargaResult>()

    @Volatile
    private var isClosed = false

    private data class ChartIdentifier(
        val birthDateTime: LocalDateTime,
        val latitude: Double,
        val longitude: Double,
        val timezone: String
    ) {
        companion object {
            fun from(chart: VedicChart): ChartIdentifier = ChartIdentifier(
                birthDateTime = chart.birthData.dateTime,
                latitude = chart.birthData.latitude,
                longitude = chart.birthData.longitude,
                timezone = chart.birthData.timezone
            )
        }
    }

    private sealed class AshtakavargaResult {
        data class Success(val analysis: AshtakavargaCalculator.AshtakavargaAnalysis) : AshtakavargaResult()
        data object Failed : AshtakavargaResult()
    }

    private data class TransitCacheKey(
        val date: LocalDate,
        val latitude: Double,
        val longitude: Double,
        val timezone: String
    )

    private data class DailyHoroscopeCacheKey(
        val chartId: ChartIdentifier,
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

    private data class VedhaInfo(
        val hasVedha: Boolean,
        val obstructingPlanet: Planet? = null,
        val obstructingHouse: Int? = null
    )

    private data class CalculationContext(
        val chart: VedicChart,
        val chartId: ChartIdentifier,
        val transitChart: VedicChart,
        val dashaTimeline: DashaCalculator.DashaTimeline,
        val ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis?,
        val natalMoonHouse: Int,
        val natalMoonSign: ZodiacSign
    )

    private fun ensureNotClosed() {
        if (isClosed) throw IllegalStateException("HoroscopeCalculator has been closed")
    }

    suspend fun calculateDailyHoroscopeAsync(
        chart: VedicChart,
        date: LocalDate = LocalDate.now()
    ): DailyHoroscope = withContext(Dispatchers.Default) {
        calculateDailyHoroscope(chart, date)
    }

    suspend fun calculateDailyHoroscopeSafeAsync(
        chart: VedicChart,
        date: LocalDate = LocalDate.now()
    ): HoroscopeResult<DailyHoroscope> = withContext(Dispatchers.Default) {
        calculateDailyHoroscopeSafe(chart, date)
    }

    suspend fun calculateWeeklyHoroscopeAsync(
        chart: VedicChart,
        startDate: LocalDate = LocalDate.now()
    ): WeeklyHoroscope = withContext(Dispatchers.Default) {
        calculateWeeklyHoroscope(chart, startDate)
    }

    suspend fun calculateWeeklyHoroscopeSafeAsync(
        chart: VedicChart,
        startDate: LocalDate = LocalDate.now()
    ): HoroscopeResult<WeeklyHoroscope> = withContext(Dispatchers.Default) {
        calculateWeeklyHoroscopeSafe(chart, startDate)
    }

    fun calculateDailyHoroscope(chart: VedicChart, date: LocalDate = LocalDate.now()): DailyHoroscope {
        ensureNotClosed()
        
        val chartId = ChartIdentifier.from(chart)
        val cacheKey = DailyHoroscopeCacheKey(chartId, date)

        horoscopeCacheLock.read {
            dailyHoroscopeCache[cacheKey]?.let { return it }
        }

        val context = buildCalculationContext(chart, chartId, date)
        
        val transitMoon = context.transitChart.planetPositions.find { it.planet == Planet.MOON }
        val moonSign = transitMoon?.sign ?: context.natalMoonSign
        val moonNakshatra = transitMoon?.nakshatra ?: Nakshatra.ASHWINI

        val planetaryInfluences = analyzePlanetaryInfluences(context)
        val lifeAreaPredictions = calculateLifeAreaPredictions(context, date)
        val overallEnergy = calculateOverallEnergy(planetaryInfluences, lifeAreaPredictions, context.dashaTimeline)
        val (theme, themeDescription) = calculateDailyTheme(context, date)
        val luckyElements = calculateLuckyElements(chart, context.transitChart, date)
        val recommendations = generateRecommendations(context, lifeAreaPredictions)
        val cautions = generateCautions(context.transitChart, planetaryInfluences)
        val affirmation = generateAffirmation(context.dashaTimeline, moonSign)

        val horoscope = DailyHoroscope(
            date = date,
            theme = theme,
            themeDescription = themeDescription,
            overallEnergy = overallEnergy,
            moonSign = moonSign,
            moonNakshatra = moonNakshatra,
            activeDasha = formatActiveDasha(context.dashaTimeline),
            lifeAreas = lifeAreaPredictions,
            luckyElements = luckyElements,
            planetaryInfluences = planetaryInfluences,
            recommendations = recommendations,
            cautions = cautions,
            affirmation = affirmation
        )

        horoscopeCacheLock.write {
            if (dailyHoroscopeCache.size >= MAX_HOROSCOPE_CACHE_SIZE) {
                val iterator = dailyHoroscopeCache.entries.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            dailyHoroscopeCache[cacheKey] = horoscope
        }

        return horoscope
    }

    fun calculateDailyHoroscopeSafe(
        chart: VedicChart,
        date: LocalDate = LocalDate.now()
    ): HoroscopeResult<DailyHoroscope> {
        return try {
            HoroscopeResult.Success(calculateDailyHoroscope(chart, date))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate daily horoscope for $date", e)
            HoroscopeResult.Error("Unable to calculate horoscope for $date: ${e.message}", e)
        }
    }

    fun calculateWeeklyHoroscope(
        chart: VedicChart,
        startDate: LocalDate = LocalDate.now()
    ): WeeklyHoroscope {
        ensureNotClosed()
        
        val endDate = startDate.plusDays(6)
        val chartId = ChartIdentifier.from(chart)
        val dashaTimeline = getOrCalculateDashaTimeline(chart, chartId)

        val dailyHoroscopes = calculateWeeklyDailyHoroscopes(chart, chartId, startDate, dashaTimeline)

        val dailyHighlights = if (dailyHoroscopes.isNotEmpty()) {
            dailyHoroscopes.map { horoscope ->
                DailyHighlight(
                    date = horoscope.date,
                    dayOfWeek = horoscope.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    energy = horoscope.overallEnergy,
                    focus = horoscope.theme,
                    brief = horoscope.themeDescription.take(100) +
                            if (horoscope.themeDescription.length > 100) "..." else ""
                )
            }
        } else {
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

        val keyDates = calculateKeyDates(startDate, endDate)
        val weeklyPredictions = calculateWeeklyPredictions(dailyHoroscopes, dashaTimeline)
        val (weeklyTheme, weeklyOverview) = calculateWeeklyTheme(dashaTimeline, dailyHighlights)
        val weeklyAdvice = generateWeeklyAdvice(dashaTimeline, keyDates)

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

    fun calculateWeeklyHoroscopeSafe(
        chart: VedicChart,
        startDate: LocalDate = LocalDate.now()
    ): HoroscopeResult<WeeklyHoroscope> {
        return try {
            HoroscopeResult.Success(calculateWeeklyHoroscope(chart, startDate))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate weekly horoscope starting $startDate", e)
            HoroscopeResult.Error("Unable to calculate weekly horoscope: ${e.message}", e)
        }
    }

    private fun buildCalculationContext(
        chart: VedicChart,
        chartId: ChartIdentifier,
        date: LocalDate
    ): CalculationContext {
        val transitChart = getOrCalculateTransitChart(chart, date)
        val dashaTimeline = getOrCalculateDashaTimeline(chart, chartId)
        val ashtakavarga = getOrCalculateAshtakavarga(chart, chartId)
        
        val natalMoon = chart.planetPositions.find { it.planet == Planet.MOON }
        val natalMoonSign = natalMoon?.sign ?: ZodiacSign.ARIES
        val natalMoonHouse = natalMoon?.house ?: 1

        return CalculationContext(
            chart = chart,
            chartId = chartId,
            transitChart = transitChart,
            dashaTimeline = dashaTimeline,
            ashtakavarga = ashtakavarga,
            natalMoonHouse = natalMoonHouse,
            natalMoonSign = natalMoonSign
        )
    }

    private fun calculateWeeklyDailyHoroscopes(
        chart: VedicChart,
        chartId: ChartIdentifier,
        startDate: LocalDate,
        dashaTimeline: DashaCalculator.DashaTimeline
    ): List<DailyHoroscope> {
        val ashtakavarga = getOrCalculateAshtakavarga(chart, chartId)
        val natalMoon = chart.planetPositions.find { it.planet == Planet.MOON }
        val natalMoonSign = natalMoon?.sign ?: ZodiacSign.ARIES
        val natalMoonHouse = natalMoon?.house ?: 1

        return (0 until 7).mapNotNull { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val cacheKey = DailyHoroscopeCacheKey(chartId, date)

            horoscopeCacheLock.read {
                dailyHoroscopeCache[cacheKey]?.let { return@mapNotNull it }
            }

            try {
                val transitChart = getOrCalculateTransitChart(chart, date)
                val context = CalculationContext(
                    chart = chart,
                    chartId = chartId,
                    transitChart = transitChart,
                    dashaTimeline = dashaTimeline,
                    ashtakavarga = ashtakavarga,
                    natalMoonHouse = natalMoonHouse,
                    natalMoonSign = natalMoonSign
                )

                val transitMoon = transitChart.planetPositions.find { it.planet == Planet.MOON }
                val moonSign = transitMoon?.sign ?: natalMoonSign
                val moonNakshatra = transitMoon?.nakshatra ?: Nakshatra.ASHWINI

                val planetaryInfluences = analyzePlanetaryInfluences(context)
                val lifeAreaPredictions = calculateLifeAreaPredictions(context, date)
                val overallEnergy = calculateOverallEnergy(planetaryInfluences, lifeAreaPredictions, dashaTimeline)
                val (theme, themeDescription) = calculateDailyTheme(context, date)
                val luckyElements = calculateLuckyElements(chart, transitChart, date)
                val recommendations = generateRecommendations(context, lifeAreaPredictions)
                val cautions = generateCautions(transitChart, planetaryInfluences)
                val affirmation = generateAffirmation(dashaTimeline, moonSign)

                val horoscope = DailyHoroscope(
                    date = date,
                    theme = theme,
                    themeDescription = themeDescription,
                    overallEnergy = overallEnergy,
                    moonSign = moonSign,
                    moonNakshatra = moonNakshatra,
                    activeDasha = formatActiveDasha(dashaTimeline),
                    lifeAreas = lifeAreaPredictions,
                    luckyElements = luckyElements,
                    planetaryInfluences = planetaryInfluences,
                    recommendations = recommendations,
                    cautions = cautions,
                    affirmation = affirmation
                )

                horoscopeCacheLock.write {
                    if (dailyHoroscopeCache.size >= MAX_HOROSCOPE_CACHE_SIZE) {
                        val iterator = dailyHoroscopeCache.entries.iterator()
                        if (iterator.hasNext()) {
                            iterator.next()
                            iterator.remove()
                        }
                    }
                    dailyHoroscopeCache[cacheKey] = horoscope
                }

                horoscope
            } catch (e: Exception) {
                Log.w(TAG, "Failed to calculate horoscope for $date, skipping", e)
                null
            }
        }
    }

    private fun getOrCalculateDashaTimeline(
        chart: VedicChart,
        chartId: ChartIdentifier
    ): DashaCalculator.DashaTimeline {
        return dashaCache.getOrPut(chartId) {
            DashaCalculator.calculateDashaTimeline(chart)
        }
    }

    private fun getOrCalculateAshtakavarga(
        chart: VedicChart,
        chartId: ChartIdentifier
    ): AshtakavargaCalculator.AshtakavargaAnalysis? {
        val result = ashtakavargaCache.getOrPut(chartId) {
            try {
                AshtakavargaResult.Success(AshtakavargaCalculator.calculateAshtakavarga(chart))
            } catch (e: Exception) {
                Log.w(TAG, "Ashtakavarga calculation failed", e)
                AshtakavargaResult.Failed
            }
        }
        return when (result) {
            is AshtakavargaResult.Success -> result.analysis
            is AshtakavargaResult.Failed -> null
        }
    }

    private fun getOrCalculateTransitChart(chart: VedicChart, date: LocalDate): VedicChart {
        val cacheKey = TransitCacheKey(
            date = date,
            latitude = chart.birthData.latitude,
            longitude = chart.birthData.longitude,
            timezone = chart.birthData.timezone
        )

        transitCacheLock.read {
            transitCache[cacheKey]?.let { return it }
        }

        val transitDateTime = LocalDateTime.of(date, LocalTime.of(6, 0))
        val transitBirthData = BirthData(
            name = "Transit",
            dateTime = transitDateTime,
            latitude = chart.birthData.latitude,
            longitude = chart.birthData.longitude,
            timezone = chart.birthData.timezone,
            location = chart.birthData.location
        )

        val transitChart = try {
            ephemerisEngine.calculateVedicChart(transitBirthData)
        } catch (e: Exception) {
            Log.e(TAG, "Transit calculation failed for $date: ${e.message}", e)
            throw HoroscopeCalculationException(
                "Unable to calculate planetary positions for $date. " +
                        "This may be due to ephemeris data limitations for this date range.",
                e
            )
        }

        transitCacheLock.write {
            if (transitCache.size >= MAX_TRANSIT_CACHE_SIZE) {
                val iterator = transitCache.entries.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            transitCache[cacheKey] = transitChart
        }

        return transitChart
    }

    private fun formatActiveDasha(timeline: DashaCalculator.DashaTimeline): String {
        val mahadasha = timeline.currentMahadasha ?: return "Calculating..."
        val antardasha = timeline.currentAntardasha
        return if (antardasha != null) {
            "${mahadasha.planet.displayName}-${antardasha.planet.displayName}"
        } else {
            mahadasha.planet.displayName
        }
    }

    private fun analyzePlanetaryInfluences(context: CalculationContext): List<PlanetaryInfluence> {
        val transitHousesFromMoon = mutableMapOf<Planet, Int>()
        
        context.transitChart.planetPositions
            .filter { it.planet in MAIN_PLANETS }
            .forEach { transitPos ->
                transitHousesFromMoon[transitPos.planet] = calculateHouseFromMoon(transitPos.sign, context.natalMoonSign)
            }

        return context.transitChart.planetPositions
            .filter { it.planet in MAIN_PLANETS }
            .map { transitPos ->
                val natalPos = context.chart.planetPositions.find { it.planet == transitPos.planet }
                val houseFromMoon = transitHousesFromMoon[transitPos.planet] ?: 1

                val vedhaInfo = checkGocharaVedha(
                    transitPos.planet,
                    houseFromMoon,
                    transitHousesFromMoon
                )

                val ashtakavargaScore = getAshtakavargaTransitScore(
                    transitPos.planet,
                    transitPos.sign,
                    context.ashtakavarga
                )

                val (influence, strength, isPositive) = analyzeGocharaEffectAdvanced(
                    planet = transitPos.planet,
                    houseFromMoon = houseFromMoon,
                    isRetrograde = transitPos.isRetrograde,
                    natalPosition = natalPos,
                    vedhaInfo = vedhaInfo,
                    ashtakavargaScore = ashtakavargaScore,
                    transitSign = transitPos.sign
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

    private fun checkGocharaVedha(
        planet: Planet,
        houseFromMoon: Int,
        transitHousesFromMoon: Map<Planet, Int>
    ): VedhaInfo {
        val vedhaPairs = GOCHARA_VEDHA_PAIRS[planet] ?: return VedhaInfo(false)
        val vedhaHouse = vedhaPairs[houseFromMoon] ?: return VedhaInfo(false)

        for ((otherPlanet, otherHouse) in transitHousesFromMoon) {
            if (otherPlanet == planet) continue
            if (otherHouse == vedhaHouse) {
                return VedhaInfo(
                    hasVedha = true,
                    obstructingPlanet = otherPlanet,
                    obstructingHouse = vedhaHouse
                )
            }
        }

        return VedhaInfo(false)
    }

    private fun getAshtakavargaTransitScore(
        planet: Planet,
        transitSign: ZodiacSign,
        ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis?
    ): Int? {
        if (ashtakavarga == null) return null
        val bav = ashtakavarga.bhinnashtakavarga[planet] ?: return null
        return bav.getBindusForSign(transitSign)
    }

    private fun calculateHouseFromMoon(transitSign: ZodiacSign, natalMoonSign: ZodiacSign): Int {
        val moonSignIndex = natalMoonSign.ordinal
        val transitSignIndex = transitSign.ordinal
        return ((transitSignIndex - moonSignIndex + 12) % 12) + 1
    }

    private fun analyzeGocharaEffectAdvanced(
        planet: Planet,
        houseFromMoon: Int,
        isRetrograde: Boolean,
        natalPosition: PlanetPosition?,
        vedhaInfo: VedhaInfo,
        ashtakavargaScore: Int?,
        transitSign: ZodiacSign
    ): Triple<String, Int, Boolean> {
        val favorableHouses = GOCHARA_FAVORABLE_HOUSES[planet] ?: emptyList()
        var isFavorable = houseFromMoon in favorableHouses

        var (influence, baseStrength) = getGocharaInfluenceDetailed(planet, houseFromMoon, isFavorable)

        if (vedhaInfo.hasVedha && isFavorable) {
            influence = "$influence However, ${vedhaInfo.obstructingPlanet?.displayName} creates Vedha obstruction, reducing benefits."
            baseStrength = (baseStrength * 0.5).toInt().coerceAtLeast(2)
            if (baseStrength <= 3) {
                isFavorable = false
            }
        }

        ashtakavargaScore?.let { score ->
            val ashtakavargaModifier = when {
                score >= 5 -> {
                    influence = "$influence Ashtakavarga ($score/8) strengthens results."
                    1.3
                }
                score == 4 -> 1.0
                score >= 2 -> {
                    influence = "$influence Ashtakavarga ($score/8) moderates results."
                    0.85
                }
                else -> {
                    influence = "$influence Low Ashtakavarga ($score/8) weakens results."
                    if (isFavorable) isFavorable = false
                    0.6
                }
            }
            baseStrength = (baseStrength * ashtakavargaModifier).toInt()
        }

        val retrogradeAdjustment = when {
            isRetrograde && isFavorable -> {
                influence = "$influence ${planet.displayName}'s retrograde motion delays manifestation."
                -1
            }
            isRetrograde && !isFavorable -> {
                influence = "$influence ${planet.displayName}'s retrograde provides some relief from challenges."
                1
            }
            else -> 0
        }

        val dignityModifier = when {
            isInOwnSign(planet, transitSign) -> {
                influence = "$influence Strong in own sign."
                if (isFavorable) 2 else 0
            }
            isExalted(planet, transitSign) -> {
                influence = "$influence Exalted - excellent results."
                if (isFavorable) 3 else 1
            }
            isDebilitated(planet, transitSign) -> {
                influence = "$influence Debilitated - results weakened."
                if (isFavorable) -2 else -1
            }
            else -> 0
        }

        val adjustedStrength = (baseStrength + retrogradeAdjustment + dignityModifier).coerceIn(1, 10)

        return Triple(influence, adjustedStrength, isFavorable)
    }

    private fun getGocharaInfluenceDetailed(
        planet: Planet,
        house: Int,
        isFavorable: Boolean
    ): Pair<String, Int> {
        return if (isFavorable) {
            FAVORABLE_GOCHARA_EFFECTS_DETAILED[planet]?.get(house)
                ?: FAVORABLE_GOCHARA_EFFECTS[planet]?.get(house)
                ?: ("Favorable ${planet.displayName} transit in house $house." to 7)
        } else {
            UNFAVORABLE_GOCHARA_EFFECTS_DETAILED[planet]?.get(house)
                ?: UNFAVORABLE_GOCHARA_EFFECTS[planet]?.get(house)
                ?: ("Challenging ${planet.displayName} transit in house $house." to 4)
        }
    }

    private fun isInOwnSign(planet: Planet, sign: ZodiacSign): Boolean {
        return OWN_SIGNS[planet]?.contains(sign) == true
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return EXALTATION_SIGNS[planet] == sign
    }

    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return DEBILITATION_SIGNS[planet] == sign
    }

    private fun calculateLifeAreaPredictions(
        context: CalculationContext,
        date: LocalDate
    ): List<LifeAreaPrediction> {
        return LifeArea.entries.map { area ->
            val (rating, prediction, advice) = analyzeLifeArea(context, area, date)
            LifeAreaPrediction(area = area, rating = rating, prediction = prediction, advice = advice)
        }
    }

    private fun analyzeLifeArea(
        context: CalculationContext,
        area: LifeArea,
        date: LocalDate
    ): Triple<Int, String, String> {
        val primaryHouse = area.houses.first()
        val primaryHouseCusp = context.chart.houseCusps.getOrElse(primaryHouse - 1) { 0.0 }
        val houseLordSign = ZodiacSign.fromLongitude(primaryHouseCusp)
        val houseLord = houseLordSign.ruler

        val dashaInfluence = calculateDashaInfluenceOnArea(context.dashaTimeline, area, context.chart)
        val transitInfluence = calculateTransitInfluenceOnArea(context.chart, context.transitChart, area)
        val rating = ((dashaInfluence + transitInfluence) / 2).coerceIn(1, 5)

        val (prediction, advice) = generateAreaPrediction(area, rating, context.dashaTimeline, houseLord, date)
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
        val natalMoon = natalChart.planetPositions.find { it.planet == Planet.MOON }
        val natalMoonSign = natalMoon?.sign ?: ZodiacSign.ARIES
        
        var score = 3
        transitChart.planetPositions.forEach { transitPos ->
            val houseFromMoon = calculateHouseFromMoon(transitPos.sign, natalMoonSign)
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
        val planetaryAvg = if (influences.isNotEmpty()) {
            influences.sumOf { it.strength }.toDouble() / influences.size
        } else 5.0
        
        val lifeAreaAvg = if (lifeAreas.isNotEmpty()) {
            lifeAreas.sumOf { it.rating * 2 }.toDouble() / lifeAreas.size
        } else 5.0

        val dashaBonus = DASHA_ENERGY_MODIFIERS[dashaTimeline.currentMahadasha?.planet] ?: 0.0
        val rawEnergy = (planetaryAvg * 0.4) + (lifeAreaAvg * 0.4) + (5.0 + dashaBonus) * 0.2

        return rawEnergy.toInt().coerceIn(1, 10)
    }

    private fun calculateDailyTheme(
        context: CalculationContext,
        date: LocalDate
    ): Pair<String, String> {
        val moonSign = context.transitChart.planetPositions.find { it.planet == Planet.MOON }?.sign
            ?: ZodiacSign.ARIES
        val currentDashaLord = context.dashaTimeline.currentMahadasha?.planet ?: Planet.SUN

        val theme = determineTheme(moonSign, currentDashaLord)
        val description = THEME_DESCRIPTIONS[theme] ?: DEFAULT_THEME_DESCRIPTION

        return Pair(theme, description)
    }

    private fun determineTheme(moonSign: ZodiacSign, dashaLord: Planet): String {
        val moonElement = moonSign.element

        return when {
            moonElement == "Fire" && dashaLord in FIRE_ALIGNED_PLANETS -> "Dynamic Action"
            moonElement == "Earth" && dashaLord in EARTH_ALIGNED_PLANETS -> "Practical Progress"
            moonElement == "Air" && dashaLord in AIR_ALIGNED_PLANETS -> "Social Connections"
            moonElement == "Water" && dashaLord in WATER_ALIGNED_PLANETS -> "Emotional Insight"
            else -> DASHA_LORD_THEMES[dashaLord] ?: "Balance & Equilibrium"
        }
    }

    private fun calculateLuckyElements(
        chart: VedicChart,
        transitChart: VedicChart,
        date: LocalDate
    ): LuckyElements {
        val moonSign = transitChart.planetPositions.find { it.planet == Planet.MOON }?.sign
            ?: ZodiacSign.ARIES
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
        context: CalculationContext,
        lifeAreas: List<LifeAreaPrediction>
    ): List<String> {
        val recommendations = ArrayList<String>(4)

        context.dashaTimeline.currentMahadasha?.planet?.let { dashaLord ->
            DASHA_RECOMMENDATIONS[dashaLord]?.let { recommendations.add(it) }
        }

        lifeAreas.maxByOrNull { it.rating }?.let { bestArea ->
            BEST_AREA_RECOMMENDATIONS[bestArea.area]?.let { recommendations.add(it) }
        }

        context.transitChart.planetPositions.find { it.planet == Planet.MOON }?.let { moon ->
            ELEMENT_RECOMMENDATIONS[moon.sign.element]?.let { recommendations.add(it) }
        }

        return if (recommendations.size > 3) recommendations.subList(0, 3) else recommendations
    }

    private fun generateCautions(
        transitChart: VedicChart,
        influences: List<PlanetaryInfluence>
    ): List<String> {
        val cautions = ArrayList<String>(3)

        var count = 0
        for (influence in influences) {
            if (!influence.isPositive && influence.strength <= 4) {
                PLANET_CAUTIONS[influence.planet]?.let {
                    cautions.add(it)
                    count++
                    if (count >= 2) break
                }
            }
        }

        for (pos in transitChart.planetPositions) {
            if (pos.isRetrograde && pos.planet in MAIN_PLANETS) {
                cautions.add("${pos.planet.displayName} is retrograde - review and reconsider rather than initiate.")
                break
            }
        }

        return if (cautions.size > 2) cautions.subList(0, 2) else cautions
    }

    private fun generateAffirmation(
        dashaTimeline: DashaCalculator.DashaTimeline,
        moonSign: ZodiacSign
    ): String {
        val dashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN
        return DASHA_AFFIRMATIONS[dashaLord]
            ?: "I am aligned with cosmic energies and flow with life's rhythm."
    }

    private fun calculateKeyDates(startDate: LocalDate, endDate: LocalDate): List<KeyDate> {
        val keyDates = ArrayList<KeyDate>(6)

        for ((dayOffset, event, significance) in LUNAR_PHASES) {
            val date = startDate.plusDays(dayOffset.toLong())
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                keyDates.add(KeyDate(date, event, significance, true))
            }
        }

        for (offset in 0 until 7) {
            val date = startDate.plusDays(offset.toLong())
            FAVORABLE_DAYS[date.dayOfWeek]?.let { desc ->
                keyDates.add(
                    KeyDate(
                        date = date,
                        event = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                        significance = desc,
                        isPositive = true
                    )
                )
            }
        }

        val distinctDates = keyDates.distinctBy { it.date }
        return if (distinctDates.size > 4) distinctDates.subList(0, 4) else distinctDates
    }

    private fun calculateWeeklyPredictions(
        dailyHoroscopes: List<DailyHoroscope>,
        dashaTimeline: DashaCalculator.DashaTimeline
    ): Map<LifeArea, String> {
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet?.displayName ?: "current"

        return LifeArea.entries.associateWith { area ->
            var totalRating = 0
            var count = 0
            for (horoscope in dailyHoroscopes) {
                val areaData = horoscope.lifeAreas.find { it.area == area }
                if (areaData != null) {
                    totalRating += areaData.rating
                    count++
                }
            }
            val avgRating = if (count > 0) totalRating.toDouble() / count else 3.0
            generateWeeklyAreaPrediction(area, avgRating, currentDashaLord)
        }
    }

    private fun generateWeeklyAreaPrediction(
        area: LifeArea,
        avgRating: Double,
        dashaLord: String
    ): String {
        val ratingCategory = when {
            avgRating >= 4 -> "excellent"
            avgRating >= 3 -> "steady"
            else -> "challenging"
        }

        return WEEKLY_PREDICTIONS[area]?.get(ratingCategory)?.replace("{dashaLord}", dashaLord)
            ?: "A balanced week for ${area.displayName.lowercase()}."
    }

    private fun calculateWeeklyTheme(
        dashaTimeline: DashaCalculator.DashaTimeline,
        dailyHighlights: List<DailyHighlight>
    ): Pair<String, String> {
        val avgEnergy = if (dailyHighlights.isNotEmpty()) {
            dailyHighlights.sumOf { it.energy }.toDouble() / dailyHighlights.size
        } else 5.0
        
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
        val builder = StringBuilder()
        builder.append("This week under your ${dashaLord.displayName} Mahadasha brings ")
        builder.append(
            when {
                avgEnergy >= 7 -> "excellent opportunities for growth and success. "
                avgEnergy >= 5 -> "steady progress and balanced energy. "
                else -> "challenges that, when navigated wisely, lead to growth. "
            }
        )

        dailyHighlights.maxByOrNull { it.energy }?.let {
            builder.append("${it.dayOfWeek} appears most favorable for important activities. ")
        }

        dailyHighlights.minByOrNull { it.energy }?.let {
            if (it.energy < 5) {
                builder.append("${it.dayOfWeek} may require extra patience and care. ")
            }
        }

        builder.append("Trust in your cosmic guidance and make the most of each day's unique energy.")
        return builder.toString()
    }

    private fun generateWeeklyAdvice(
        dashaTimeline: DashaCalculator.DashaTimeline,
        keyDates: List<KeyDate>
    ): String {
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN
        val baseAdvice = DASHA_WEEKLY_ADVICE[currentDashaLord]
            ?: "maintain balance and trust in divine timing."

        val builder = StringBuilder()
        builder.append("During this ${currentDashaLord.displayName} period, ")
        builder.append(baseAdvice)
        
        keyDates.firstOrNull { it.isPositive }?.let {
            val formattedDate = it.date.format(DATE_FORMATTER)
            builder.append(" Mark $formattedDate for important initiatives.")
        }

        return builder.toString()
    }

    fun clearCache() {
        transitCacheLock.write { transitCache.clear() }
        horoscopeCacheLock.write { dailyHoroscopeCache.clear() }
        dashaCache.clear()
        ashtakavargaCache.clear()
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        
        try {
            clearCache()
            if (this::ephemerisEngine.isInitialized) {
                ephemerisEngine.close()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error closing ephemeris engine", e)
        }
    }

    class HoroscopeCalculationException(
        message: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    companion object {
        private const val TAG = "HoroscopeCalculator"
        private const val MAX_TRANSIT_CACHE_SIZE = 30
        private const val MAX_HOROSCOPE_CACHE_SIZE = 50

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

        private val MAIN_PLANETS = setOf(
            Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
            Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.RAHU, Planet.KETU
        )

        private val NATURAL_BENEFICS = setOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
        private val NATURAL_MALEFICS = setOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)

        private val FIRE_ALIGNED_PLANETS = setOf(Planet.SUN, Planet.MARS, Planet.JUPITER)
        private val EARTH_ALIGNED_PLANETS = setOf(Planet.VENUS, Planet.MERCURY, Planet.SATURN)
        private val AIR_ALIGNED_PLANETS = setOf(Planet.MERCURY, Planet.VENUS, Planet.SATURN)
        private val WATER_ALIGNED_PLANETS = setOf(Planet.MOON, Planet.MARS, Planet.JUPITER)

        private val OWN_SIGNS = mapOf(
            Planet.SUN to setOf(ZodiacSign.LEO),
            Planet.MOON to setOf(ZodiacSign.CANCER),
            Planet.MARS to setOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO),
            Planet.MERCURY to setOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO),
            Planet.JUPITER to setOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES),
            Planet.VENUS to setOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA),
            Planet.SATURN to setOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS)
        )

        private val EXALTATION_SIGNS = mapOf(
            Planet.SUN to ZodiacSign.ARIES,
            Planet.MOON to ZodiacSign.TAURUS,
            Planet.MARS to ZodiacSign.CAPRICORN,
            Planet.MERCURY to ZodiacSign.VIRGO,
            Planet.JUPITER to ZodiacSign.CANCER,
            Planet.VENUS to ZodiacSign.PISCES,
            Planet.SATURN to ZodiacSign.LIBRA
        )

        private val DEBILITATION_SIGNS = mapOf(
            Planet.SUN to ZodiacSign.LIBRA,
            Planet.MOON to ZodiacSign.SCORPIO,
            Planet.MARS to ZodiacSign.CANCER,
            Planet.MERCURY to ZodiacSign.PISCES,
            Planet.JUPITER to ZodiacSign.CAPRICORN,
            Planet.VENUS to ZodiacSign.VIRGO,
            Planet.SATURN to ZodiacSign.ARIES
        )

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

        private val GOCHARA_VEDHA_PAIRS = mapOf(
            Planet.SUN to mapOf(3 to 9, 6 to 12, 10 to 4, 11 to 5),
            Planet.MOON to mapOf(1 to 5, 3 to 9, 6 to 12, 7 to 2, 10 to 4, 11 to 8),
            Planet.MARS to mapOf(3 to 12, 6 to 9, 11 to 5),
            Planet.MERCURY to mapOf(2 to 5, 4 to 3, 6 to 9, 8 to 1, 10 to 8, 11 to 12),
            Planet.JUPITER to mapOf(2 to 12, 5 to 4, 7 to 3, 9 to 10, 11 to 8),
            Planet.VENUS to mapOf(1 to 8, 2 to 7, 3 to 1, 4 to 10, 5 to 9, 8 to 5, 9 to 11, 11 to 6, 12 to 3),
            Planet.SATURN to mapOf(3 to 12, 6 to 9, 11 to 5),
            Planet.RAHU to mapOf(3 to 12, 6 to 9, 10 to 4, 11 to 5),
            Planet.KETU to mapOf(3 to 12, 6 to 9, 9 to 10, 11 to 5)
        )

        private val FAVORABLE_GOCHARA_EFFECTS_DETAILED = mapOf(
            Planet.SUN to mapOf(
                3 to ("Courage and valor increase. Victory over rivals." to 8),
                6 to ("Destruction of enemies. Health improves. Debts decrease." to 8),
                10 to ("Professional success and recognition. Authority increases." to 9),
                11 to ("Gains of wealth. Fulfillment of desires. Success in endeavors." to 9)
            ),
            Planet.MOON to mapOf(
                1 to ("Mental peace and satisfaction. Good health and comforts." to 8),
                3 to ("Courage increases. Success in short journeys. Good relations with siblings." to 7),
                6 to ("Victory over enemies. Relief from debts and diseases." to 8),
                7 to ("Pleasure through spouse. Partnership gains. Social happiness." to 8),
                10 to ("Success in profession. Recognition from superiors." to 8),
                11 to ("Financial gains. Fulfillment of desires. Social success." to 9)
            ),
            Planet.MARS to mapOf(
                3 to ("Courage and determination. Victory in competitions. Energy for initiatives." to 8),
                6 to ("Defeat of enemies. Success through effort. Good for legal matters." to 8),
                11 to ("Financial gains through effort. Achievement of goals. Success in ventures." to 8)
            ),
            Planet.MERCURY to mapOf(
                2 to ("Gains through speech and intellect. Family harmony. Financial gains." to 8),
                4 to ("Domestic happiness. Property matters favorable. Mental peace." to 7),
                6 to ("Victory over competitors. Success in studies. Sharp intellect." to 8),
                8 to ("Gains through research. Understanding occult matters." to 7),
                10 to ("Professional success. Recognition for intelligence. Business growth." to 8),
                11 to ("Financial gains through communication. Network expansion." to 8)
            ),
            Planet.JUPITER to mapOf(
                2 to ("Wealth increases. Family harmony. Sweet speech. Good food." to 9),
                5 to ("Intelligence flourishes. Good for children. Romance. Creativity." to 9),
                7 to ("Partnership success. Marriage prospects. Business partnerships." to 8),
                9 to ("Spiritual growth. Luck and fortune. Father's blessings. Pilgrimage." to 10),
                11 to ("Major gains. Fulfillment of desires. Eldest sibling's success." to 9)
            ),
            Planet.VENUS to mapOf(
                1 to ("Personal charm increases. Attraction and luxury. Good health." to 8),
                2 to ("Wealth and family happiness. Good food and comforts." to 8),
                3 to ("Artistic talents shine. Harmonious relations with siblings." to 7),
                4 to ("Domestic bliss. Vehicle and property gains. Mother's blessings." to 8),
                5 to ("Romance and creativity. Pleasure through children. Entertainment." to 9),
                8 to ("Unexpected gains. Inheritance matters favorable." to 7),
                9 to ("Fortune through relationships. Spiritual partnerships." to 8),
                11 to ("Major gains through arts/finance. Social success." to 9),
                12 to ("Pleasures of bed. Foreign connections favorable." to 7)
            ),
            Planet.SATURN to mapOf(
                3 to ("Perseverance pays off. Courage through discipline. Victory through patience." to 7),
                6 to ("Victory over enemies through persistence. Health through discipline." to 8),
                11 to ("Long-term gains materialize. Slow but steady prosperity." to 8)
            ),
            Planet.RAHU to mapOf(
                3 to ("Courage for unconventional paths. Success through innovation." to 7),
                6 to ("Victory over hidden enemies. Overcoming obstacles." to 7),
                10 to ("Sudden rise in career. Foreign opportunities." to 8),
                11 to ("Unexpected gains. Fulfillment of unusual desires." to 8)
            ),
            Planet.KETU to mapOf(
                3 to ("Spiritual courage. Success in research." to 7),
                6 to ("Healing abilities increase. Victory through spiritual means." to 7),
                9 to ("Spiritual insights. Pilgrimage. Blessings from teachers." to 8),
                11 to ("Gains through spiritual pursuits. Liberation from desires." to 7)
            )
        )

        private val UNFAVORABLE_GOCHARA_EFFECTS_DETAILED = mapOf(
            Planet.SUN to mapOf(
                1 to ("Health issues. Ego challenges. Conflicts with authority." to 4),
                2 to ("Financial difficulties. Family disputes. Speech issues." to 4),
                4 to ("Domestic unrest. Mental worry. Vehicle problems." to 4),
                5 to ("Obstacles to children. Poor decisions. Speculation loss." to 4),
                7 to ("Relationship strain. Partnership challenges." to 4),
                8 to ("Health concerns. Unexpected problems. Hidden enemies." to 3),
                9 to ("Obstacles in luck. Difficulties with father/teacher." to 4),
                12 to ("Expenses increase. Sleep disturbances. Hidden losses." to 4)
            ),
            Planet.MOON to mapOf(
                2 to ("Financial fluctuations. Emotional eating issues." to 4),
                4 to ("Mental restlessness. Domestic worries." to 4),
                5 to ("Emotional challenges with children. Poor speculation." to 4),
                8 to ("Emotional turmoil. Hidden anxieties. Health vulnerabilities." to 3),
                9 to ("Spiritual doubts. Emotional distance from teachers." to 4),
                12 to ("Sleep issues. Expenses. Emotional withdrawal." to 4)
            ),
            Planet.MARS to mapOf(
                1 to ("Impulsive actions. Accidents. Health issues. Anger." to 4),
                2 to ("Financial losses through haste. Family arguments." to 4),
                4 to ("Domestic conflicts. Property disputes. Mother's health." to 4),
                5 to ("Children's issues. Poor decisions. Speculation loss." to 4),
                7 to ("Relationship conflicts. Partnership disputes." to 3),
                8 to ("Accidents. Surgeries. Hidden enemies active." to 3),
                9 to ("Conflicts with teachers. Father's health." to 4),
                10 to ("Professional conflicts. Authority issues." to 4),
                12 to ("Hidden enemies. Expenses. Hospitalization risk." to 3)
            ),
            Planet.MERCURY to mapOf(
                1 to ("Nervous tension. Skin issues. Restless mind." to 4),
                3 to ("Communication problems. Sibling issues. Short trips troubled." to 4),
                5 to ("Poor decisions. Learning difficulties." to 4),
                7 to ("Partnership misunderstandings. Contract issues." to 4),
                9 to ("Educational obstacles. Communication with father strained." to 4),
                12 to ("Mental anxieties. Hidden worries. Poor sleep." to 4)
            ),
            Planet.JUPITER to mapOf(
                1 to ("Weight gain. Overconfidence. Health issues." to 4),
                3 to ("Reduced courage. Sibling issues." to 5),
                4 to ("Domestic expansion issues. Property disputes." to 4),
                6 to ("Debts may increase. Enemy problems." to 4),
                8 to ("Unexpected expenses. Health vulnerabilities." to 4),
                10 to ("Professional setbacks. Reputation challenges." to 4),
                12 to ("Expenses. Foreign troubles. Spiritual doubts." to 4)
            ),
            Planet.SATURN to mapOf(
                1 to ("Health issues. Depression. Physical weakness." to 3),
                2 to ("Financial constraints. Family separation. Speech issues." to 4),
                4 to ("Domestic stress. Mother's health. Property issues." to 3),
                5 to ("Children's problems. Poor decisions. Mental worry." to 4),
                7 to ("Relationship strain. Partnership challenges. Delays in marriage." to 3),
                8 to ("Chronic health issues. Hidden problems. Accidents." to 2),
                9 to ("Father's troubles. Spiritual obstacles. Bad luck phase." to 3),
                10 to ("Career setbacks. Authority conflicts. Reputation damage." to 4),
                12 to ("Isolation. Expenses. Sleep issues. Foreign troubles." to 3)
            ),
            Planet.RAHU to mapOf(
                1 to ("Confusion. Wrong decisions. Health anxieties." to 4),
                2 to ("Financial deception. Family disharmony." to 4),
                4 to ("Mental confusion. Domestic issues." to 4),
                5 to ("Children's concerns. Poor speculation." to 4),
                7 to ("Relationship deceptions. Partnership frauds." to 3),
                8 to ("Sudden problems. Hidden enemies. Health scares." to 3),
                9 to ("Spiritual confusion. Issues with teachers." to 4),
                12 to ("Hidden enemies. Expenses. Foreign troubles." to 3)
            ),
            Planet.KETU to mapOf(
                1 to ("Health vulnerabilities. Lack of direction." to 4),
                2 to ("Financial losses. Family separation." to 4),
                4 to ("Domestic detachment. Property losses." to 4),
                5 to ("Children's issues. Poor decisions." to 4),
                7 to ("Relationship detachment. Partnership dissolution." to 3),
                8 to ("Sudden health issues. Accidents. Hidden problems." to 3),
                10 to ("Career confusion. Direction loss." to 4),
                12 to ("Expenses. Spiritual confusion. Isolation." to 4)
            )
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

        private const val DEFAULT_THEME_DESCRIPTION =
            "A day of balance where all energies are in equilibrium. Maintain steadiness and make measured progress in all areas of life."

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
            DayOfWeek.THURSDAY to "Jupiter's day - auspicious for growth",
            DayOfWeek.FRIDAY to "Venus's day - favorable for relationships"
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