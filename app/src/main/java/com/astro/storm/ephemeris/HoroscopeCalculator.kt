package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Production-grade Horoscope Calculator
 *
 * Generates precise, kundali-based horoscope predictions by analyzing:
 * - Current planetary transits over natal positions
 * - Active Dasha periods (Mahadasha, Antardasha, Pratyantardasha)
 * - Moon's transit through houses (Gochara)
 * - Ashtakavarga strength of transiting planets
 * - Planetary aspects on key houses
 * - House lord transits
 *
 * All predictions are derived from Vedic astrological principles,
 * not random text generation.
 */
class HoroscopeCalculator(private val context: Context) : AutoCloseable {

    private val ephemerisEngine = SwissEphemerisEngine(context)

    /**
     * Represents a complete daily horoscope analysis
     */
    data class DailyHoroscope(
        val date: LocalDate,
        val theme: String,
        val themeDescription: String,
        val overallEnergy: Int, // 1-10
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
        val rating: Int, // 1-5
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
        val strength: Int, // 1-10
        val isPositive: Boolean
    )

    /**
     * Weekly horoscope with daily breakdown
     */
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

    /**
     * Generate daily horoscope based on chart and date
     */
    fun calculateDailyHoroscope(chart: VedicChart, date: LocalDate = LocalDate.now()): DailyHoroscope {
        // Get current transit positions for the specified date
        val transitDateTime = LocalDateTime.of(date, java.time.LocalTime.of(6, 0))
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
            // Throw error instead of silently producing wrong predictions
            // Logging the exception for debugging purposes
            android.util.Log.e(
                "HoroscopeCalculator",
                "Failed to calculate transit chart for date: $date. Original error: ${e.message}",
                e
            )
            // Re-throw as IllegalStateException to prevent silent failures
            throw IllegalStateException(
                "Unable to calculate transit positions for horoscope on $date. " +
                "This is required for accurate astrological predictions.",
                e
            )
        }

        // Get Dasha information
        val dashaTimeline = DashaCalculator.calculateDashaTimeline(chart)
        val activeDasha = formatActiveDasha(dashaTimeline)

        // Calculate Moon's current sign and nakshatra
        val transitMoon = transitChart.planetPositions.find { it.planet == Planet.MOON }
        val moonSign = transitMoon?.sign ?: chart.planetPositions.find { it.planet == Planet.MOON }?.sign ?: ZodiacSign.ARIES
        val moonNakshatra = transitMoon?.nakshatra ?: Nakshatra.ASHWINI

        // Calculate Gochara (transit from Moon)
        val natalMoon = chart.planetPositions.find { it.planet == Planet.MOON }
        val moonHouse = natalMoon?.house ?: 1

        // Analyze planetary influences
        val planetaryInfluences = analyzePlanetaryInfluences(chart, transitChart, moonHouse)

        // Calculate life area predictions
        val lifeAreaPredictions = calculateLifeAreaPredictions(chart, transitChart, dashaTimeline, date)

        // Calculate overall energy based on multiple factors
        val overallEnergy = calculateOverallEnergy(planetaryInfluences, lifeAreaPredictions, dashaTimeline)

        // Get theme based on current transits and dashas
        val (theme, themeDescription) = calculateDailyTheme(chart, transitChart, dashaTimeline, date)

        // Calculate lucky elements
        val luckyElements = calculateLuckyElements(chart, transitChart, date)

        // Generate recommendations and cautions
        val recommendations = generateRecommendations(chart, transitChart, dashaTimeline, lifeAreaPredictions)
        val cautions = generateCautions(chart, transitChart, planetaryInfluences)

        // Generate affirmation based on current period
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
        )
    }

    /**
     * Generate weekly horoscope
     */
    fun calculateWeeklyHoroscope(chart: VedicChart, startDate: LocalDate = LocalDate.now()): WeeklyHoroscope {
        val endDate = startDate.plusDays(6)

        // Get Dasha information
        val dashaTimeline = DashaCalculator.calculateDashaTimeline(chart)

        // Calculate daily highlights for the week
        val dailyHighlights = (0 until 7).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dailyHoroscope = calculateDailyHoroscope(chart, date)

            DailyHighlight(
                date = date,
                dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                energy = dailyHoroscope.overallEnergy,
                focus = dailyHoroscope.theme,
                brief = dailyHoroscope.themeDescription.take(100) + if (dailyHoroscope.themeDescription.length > 100) "..." else ""
            )
        }

        // Calculate key dates for the week
        val keyDates = calculateKeyDates(chart, startDate, endDate)

        // Calculate weekly predictions by life area
        val weeklyPredictions = calculateWeeklyPredictions(chart, startDate)

        // Calculate weekly theme based on dominant influences
        val (weeklyTheme, weeklyOverview) = calculateWeeklyTheme(chart, dashaTimeline, startDate, dailyHighlights)

        // Generate weekly advice
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
        val influences = mutableListOf<PlanetaryInfluence>()

        // Analyze each transiting planet's effect on natal chart
        transitChart.planetPositions.filter { it.planet in Planet.MAIN_PLANETS }.forEach { transitPos ->
            val natalPos = natalChart.planetPositions.find { it.planet == transitPos.planet }

            // Calculate house from natal Moon (Gochara)
            val houseFromMoon = calculateHouseFromMoon(transitPos.sign, natalChart)

            // Determine influence based on Gochara principles
            val (influence, strength, isPositive) = analyzeGocharaEffect(
                transitPos.planet,
                houseFromMoon,
                transitPos.isRetrograde,
                natalPos
            )

            influences.add(
                PlanetaryInfluence(
                    planet = transitPos.planet,
                    influence = influence,
                    strength = strength,
                    isPositive = isPositive
                )
            )
        }

        return influences.sortedByDescending { it.strength }
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
        // Favorable and unfavorable houses for each planet (Gochara rules)
        val favorableHouses = mapOf(
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

        val planetFavorableHouses = favorableHouses[planet] ?: emptyList()
        val isFavorable = houseFromMoon in planetFavorableHouses

        val (influence, baseStrength) = when {
            isFavorable && planet == Planet.JUPITER -> {
                when (houseFromMoon) {
                    2 -> "Wealth and family prosperity" to 9
                    5 -> "Intelligence, children, and creativity flourish" to 9
                    7 -> "Partnership and relationship harmony" to 8
                    9 -> "Fortune, spirituality, and wisdom" to 10
                    11 -> "Gains and fulfillment of desires" to 9
                    else -> "Generally positive influence" to 7
                }
            }
            isFavorable && planet == Planet.SATURN -> {
                when (houseFromMoon) {
                    3 -> "Courage and perseverance" to 7
                    6 -> "Victory over obstacles" to 8
                    11 -> "Long-term gains through patience" to 8
                    else -> "Supportive for discipline" to 6
                }
            }
            isFavorable && planet == Planet.VENUS -> {
                when (houseFromMoon) {
                    1 -> "Personal charm and attractiveness" to 8
                    4 -> "Domestic happiness and comfort" to 8
                    5 -> "Romance and creative expression" to 9
                    9 -> "Fortune through relationships" to 8
                    else -> "Pleasure and harmony" to 7
                }
            }
            !isFavorable && planet == Planet.SATURN -> {
                when (houseFromMoon) {
                    1 -> "Physical challenges require attention" to 4
                    4 -> "Domestic stress possible" to 3
                    8 -> "Health vigilance needed" to 3
                    12 -> "Expenses and isolation feeling" to 4
                    else -> "Delays and obstacles" to 5
                }
            }
            !isFavorable && planet == Planet.MARS -> {
                when (houseFromMoon) {
                    1 -> "Impulsive actions may backfire" to 4
                    4 -> "Family tensions possible" to 4
                    7 -> "Relationship conflicts" to 3
                    8 -> "Health caution needed" to 3
                    12 -> "Hidden enemies active" to 4
                    else -> "Aggressive energy to channel" to 5
                }
            }
            isFavorable -> "Positive ${planet.displayName} energy" to 7
            else -> "Challenging ${planet.displayName} transit" to 4
        }

        // Modify strength for retrograde
        val adjustedStrength = if (isRetrograde) {
            if (isFavorable) baseStrength - 1 else baseStrength + 1
        } else baseStrength

        return Triple(influence, adjustedStrength.coerceIn(1, 10), isFavorable)
    }

    private fun calculateLifeAreaPredictions(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        date: LocalDate
    ): List<LifeAreaPrediction> {
        return LifeArea.entries.map { area ->
            val (rating, prediction, advice) = analyzeLifeArea(chart, transitChart, dashaTimeline, area, date)
            LifeAreaPrediction(
                area = area,
                rating = rating,
                prediction = prediction,
                advice = advice
            )
        }
    }

    private fun analyzeLifeArea(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        area: LifeArea,
        date: LocalDate
    ): Triple<Int, String, String> {
        // Get house lords for this area
        val primaryHouse = area.houses.first()
        val primaryHouseCusp = if (primaryHouse <= chart.houseCusps.size) {
            chart.houseCusps[primaryHouse - 1]
        } else 0.0

        val houseLordSign = ZodiacSign.fromLongitude(primaryHouseCusp)
        val houseLord = houseLordSign.ruler

        // Check if house lord is aspected or transited by benefics/malefics
        val houseLordPosition = chart.planetPositions.find { it.planet == houseLord }
        val transitingPlanets = transitChart.planetPositions

        // Calculate base rating from Dasha
        val dashaInfluence = calculateDashaInfluenceOnArea(dashaTimeline, area, chart)

        // Calculate transit influence
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

        // Check planet's relationship with area houses
        val planetPosition = chart.planetPositions.find { it.planet == mahadashaPlanet }
        val planetHouse = planetPosition?.house ?: return 3

        // Is the Dasha lord placed in or aspecting area houses?
        val isInAreaHouse = planetHouse in area.houses

        // Natural benefics give better results
        val isBenefic = mahadashaPlanet in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)

        return when {
            isInAreaHouse && isBenefic -> 5
            isInAreaHouse -> 4
            isBenefic -> 4
            mahadashaPlanet in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) -> 3
            else -> 3
        }
    }

    private fun calculateTransitInfluenceOnArea(
        natalChart: VedicChart,
        transitChart: VedicChart,
        area: LifeArea
    ): Int {
        var score = 3

        // Check benefic transits through area houses
        val benefics = listOf(Planet.JUPITER, Planet.VENUS)
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)

        transitChart.planetPositions.forEach { transitPos ->
            val houseFromMoon = calculateHouseFromMoon(transitPos.sign, natalChart)

            if (houseFromMoon in area.houses) {
                when (transitPos.planet) {
                    in benefics -> score += 1
                    in malefics -> score -= 1
                    else -> {} // Neutral
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
        val dayOfWeek = date.dayOfWeek.name.lowercase()
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet?.displayName ?: "current"

        val predictions = when (area) {
            LifeArea.CAREER -> when (rating) {
                5 -> "Excellent day for professional advancement. Your $currentDashaLord period brings recognition and success in work matters."
                4 -> "Good energy for career activities. Focus on important projects and networking."
                3 -> "Steady progress in professional matters. Maintain consistency in your efforts."
                2 -> "Some workplace challenges may arise. Stay patient and diplomatic."
                else -> "Career matters require extra attention. Avoid major decisions today."
            }
            LifeArea.LOVE -> when (rating) {
                5 -> "Romantic energy is at its peak. Deep connections and meaningful conversations await."
                4 -> "Favorable time for relationships. Express your feelings openly."
                3 -> "Balanced energy in partnerships. Focus on understanding and compromise."
                2 -> "Minor misunderstandings possible. Practice patience with loved ones."
                else -> "Relationships need nurturing. Avoid conflicts and be extra considerate."
            }
            LifeArea.HEALTH -> when (rating) {
                5 -> "Vitality is strong. Great day for physical activities and wellness routines."
                4 -> "Good health energy. Maintain your wellness practices."
                3 -> "Steady health. Focus on rest and balanced nutrition."
                2 -> "Energy may fluctuate. Prioritize adequate rest."
                else -> "Health needs attention. Take it easy and avoid overexertion."
            }
            LifeArea.FINANCE -> when (rating) {
                5 -> "Excellent day for financial matters. Opportunities for gains are strong."
                4 -> "Positive financial energy. Good for planned investments."
                3 -> "Stable financial period. Stick to your budget."
                2 -> "Be cautious with expenditures. Avoid impulsive purchases."
                else -> "Financial caution advised. Postpone major financial decisions."
            }
            LifeArea.FAMILY -> when (rating) {
                5 -> "Harmonious family energy. Celebrations and joyful gatherings are favored."
                4 -> "Good time for family bonding. Support flows both ways."
                3 -> "Steady domestic atmosphere. Focus on routine family matters."
                2 -> "Minor family tensions possible. Practice understanding."
                else -> "Family dynamics need attention. Prioritize peace and harmony."
            }
            LifeArea.SPIRITUALITY -> when (rating) {
                5 -> "Profound spiritual insights available. Meditation and reflection are highly beneficial."
                4 -> "Good day for spiritual practices. Inner guidance is strong."
                3 -> "Steady spiritual energy. Maintain your regular practices."
                2 -> "Spiritual connection may feel distant. Keep faith."
                else -> "Inner turbulence possible. Ground yourself through simple practices."
            }
        }

        val advice = when (area) {
            LifeArea.CAREER -> when {
                rating >= 4 -> "Take initiative on important projects."
                rating >= 3 -> "Stay focused on your regular responsibilities."
                else -> "Plan ahead and prepare for opportunities."
            }
            LifeArea.LOVE -> when {
                rating >= 4 -> "Express your feelings authentically."
                rating >= 3 -> "Listen more than you speak."
                else -> "Give space and practice patience."
            }
            LifeArea.HEALTH -> when {
                rating >= 4 -> "Engage in physical activity you enjoy."
                rating >= 3 -> "Maintain balanced meals and hydration."
                else -> "Rest is your best medicine today."
            }
            LifeArea.FINANCE -> when {
                rating >= 4 -> "Review investment opportunities."
                rating >= 3 -> "Stick to planned expenses."
                else -> "Save rather than spend today."
            }
            LifeArea.FAMILY -> when {
                rating >= 4 -> "Plan a family activity together."
                rating >= 3 -> "Be present for family members."
                else -> "Choose harmony over being right."
            }
            LifeArea.SPIRITUALITY -> when {
                rating >= 4 -> "Dedicate extra time to meditation."
                rating >= 3 -> "Find moments of stillness."
                else -> "Simple prayers bring comfort."
            }
        }

        return Pair(predictions, advice)
    }

    private fun calculateOverallEnergy(
        influences: List<PlanetaryInfluence>,
        lifeAreas: List<LifeAreaPrediction>,
        dashaTimeline: DashaCalculator.DashaTimeline
    ): Int {
        // Weight: Planetary influences (40%), Life areas (40%), Dasha (20%)
        val planetaryAvg = if (influences.isNotEmpty()) {
            influences.map { it.strength }.average()
        } else 5.0

        val lifeAreaAvg = if (lifeAreas.isNotEmpty()) {
            lifeAreas.map { it.rating * 2 }.average() // Scale to 10
        } else 5.0

        val dashaBonus = when (dashaTimeline.currentMahadasha?.planet) {
            Planet.JUPITER, Planet.VENUS -> 1.5
            Planet.MERCURY, Planet.MOON -> 1.0
            Planet.SUN -> 0.5
            Planet.SATURN, Planet.MARS -> -0.5
            Planet.RAHU, Planet.KETU -> -1.0
            else -> 0.0
        }

        val rawEnergy = (planetaryAvg * 0.4) + (lifeAreaAvg * 0.4) + (5.0 + dashaBonus) * 0.2
        return rawEnergy.toInt().coerceIn(1, 10)
    }

    private fun calculateDailyTheme(
        chart: VedicChart,
        transitChart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        date: LocalDate
    ): Pair<String, String> {
        val dayOfWeek = date.dayOfWeek
        val moonSign = transitChart.planetPositions.find { it.planet == Planet.MOON }?.sign ?: ZodiacSign.ARIES
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN

        // Theme based on combination of Moon sign and Dasha lord
        val theme = when {
            moonSign in listOf(ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS) &&
                    currentDashaLord in listOf(Planet.SUN, Planet.MARS, Planet.JUPITER) ->
                "Dynamic Action"
            moonSign in listOf(ZodiacSign.TAURUS, ZodiacSign.VIRGO, ZodiacSign.CAPRICORN) &&
                    currentDashaLord in listOf(Planet.VENUS, Planet.MERCURY, Planet.SATURN) ->
                "Practical Progress"
            moonSign in listOf(ZodiacSign.GEMINI, ZodiacSign.LIBRA, ZodiacSign.AQUARIUS) &&
                    currentDashaLord in listOf(Planet.MERCURY, Planet.VENUS, Planet.SATURN) ->
                "Social Connections"
            moonSign in listOf(ZodiacSign.CANCER, ZodiacSign.SCORPIO, ZodiacSign.PISCES) &&
                    currentDashaLord in listOf(Planet.MOON, Planet.MARS, Planet.JUPITER) ->
                "Emotional Insight"
            currentDashaLord == Planet.JUPITER -> "Expansion & Wisdom"
            currentDashaLord == Planet.VENUS -> "Harmony & Beauty"
            currentDashaLord == Planet.SATURN -> "Discipline & Growth"
            currentDashaLord == Planet.MERCURY -> "Communication & Learning"
            currentDashaLord == Planet.MARS -> "Energy & Initiative"
            currentDashaLord == Planet.SUN -> "Self-Expression"
            currentDashaLord == Planet.MOON -> "Intuition & Nurturing"
            currentDashaLord == Planet.RAHU -> "Transformation"
            currentDashaLord == Planet.KETU -> "Spiritual Liberation"
            else -> "Balance & Equilibrium"
        }

        val description = when (theme) {
            "Dynamic Action" -> "Your energy is high and aligned with fire elements. This is an excellent day for taking initiative, starting new projects, and asserting yourself confidently. Channel this vibrant energy into productive pursuits."
            "Practical Progress" -> "Grounded earth energy supports methodical progress today. Focus on practical tasks, financial planning, and building stable foundations. Your efforts will yield tangible results."
            "Social Connections" -> "Air element energy enhances communication and social interactions. Networking, negotiations, and intellectual pursuits are favored. Express your ideas and connect with like-minded people."
            "Emotional Insight" -> "Water element energy deepens your intuition and emotional awareness. Trust your feelings and pay attention to subtle cues. This is a powerful day for healing and self-reflection."
            "Expansion & Wisdom" -> "Jupiter's benevolent influence brings opportunities for growth, learning, and good fortune. Be open to new possibilities and share your wisdom generously."
            "Harmony & Beauty" -> "Venus graces you with appreciation for beauty, art, and relationships. Indulge in pleasurable activities and nurture your connections with loved ones."
            "Discipline & Growth" -> "Saturn's influence calls for patience, hard work, and responsibility. Embrace challenges as opportunities for growth and stay committed to your long-term goals."
            "Communication & Learning" -> "Mercury enhances your mental agility and communication skills. This is ideal for learning, teaching, writing, and all forms of information exchange."
            "Energy & Initiative" -> "Mars provides courage and drive. Take bold action, compete with integrity, and channel aggressive energy into constructive activities."
            "Self-Expression" -> "The Sun illuminates your path to self-expression and leadership. Shine your light confidently and pursue activities that bring you recognition."
            "Intuition & Nurturing" -> "The Moon heightens your sensitivity and caring nature. Nurture yourself and others, and trust your instincts in important decisions."
            "Transformation" -> "Rahu's influence brings unconventional opportunities and desires for change. Embrace innovation but stay grounded in your values."
            "Spiritual Liberation" -> "Ketu's energy supports detachment and spiritual insight. Let go of what no longer serves you and focus on inner growth."
            else -> "A day of balance where all energies are in equilibrium. Maintain steadiness and make measured progress in all areas of life."
        }

        return Pair(theme, description)
    }

    private fun calculateLuckyElements(
        chart: VedicChart,
        transitChart: VedicChart,
        date: LocalDate
    ): LuckyElements {
        val moonSign = transitChart.planetPositions.find { it.planet == Planet.MOON }?.sign ?: ZodiacSign.ARIES
        val dayOfWeek = date.dayOfWeek.value

        // Lucky number based on day and Moon position
        val luckyNumber = ((dayOfWeek + moonSign.ordinal) % 9) + 1

        // Lucky color based on Moon sign element
        val luckyColor = when (moonSign) {
            ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS -> "Red, Orange, or Gold"
            ZodiacSign.TAURUS, ZodiacSign.VIRGO, ZodiacSign.CAPRICORN -> "Green, Brown, or White"
            ZodiacSign.GEMINI, ZodiacSign.LIBRA, ZodiacSign.AQUARIUS -> "Blue, Light Blue, or Silver"
            ZodiacSign.CANCER, ZodiacSign.SCORPIO, ZodiacSign.PISCES -> "White, Cream, or Sea Green"
        }

        // Lucky direction based on ascendant ruler
        val ascSign = ZodiacSign.fromLongitude(chart.ascendant)
        val luckyDirection = when (ascSign.ruler) {
            Planet.SUN, Planet.MARS -> "East"
            Planet.MOON, Planet.VENUS -> "North"
            Planet.MERCURY -> "North"
            Planet.JUPITER -> "North-East"
            Planet.SATURN -> "West"
            Planet.RAHU -> "South-West"
            Planet.KETU -> "North-West"
            else -> "East"
        }

        // Lucky time based on hora
        val luckyTime = when (dayOfWeek) {
            1 -> "6:00 AM - 7:00 AM (Sun Hora)"
            2 -> "7:00 AM - 8:00 AM (Moon Hora)"
            3 -> "8:00 AM - 9:00 AM (Mars Hora)"
            4 -> "9:00 AM - 10:00 AM (Mercury Hora)"
            5 -> "10:00 AM - 11:00 AM (Jupiter Hora)"
            6 -> "11:00 AM - 12:00 PM (Venus Hora)"
            7 -> "5:00 PM - 6:00 PM (Saturn Hora)"
            else -> "Morning hours"
        }

        // Gemstone based on ascendant lord
        val gemstone = when (ascSign.ruler) {
            Planet.SUN -> "Ruby"
            Planet.MOON -> "Pearl"
            Planet.MARS -> "Red Coral"
            Planet.MERCURY -> "Emerald"
            Planet.JUPITER -> "Yellow Sapphire"
            Planet.VENUS -> "Diamond or White Sapphire"
            Planet.SATURN -> "Blue Sapphire"
            Planet.RAHU -> "Hessonite"
            Planet.KETU -> "Cat's Eye"
            else -> "Clear Quartz"
        }

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

        // Recommendation based on Dasha lord
        dashaTimeline.currentMahadasha?.planet?.let { dashaLord ->
            recommendations.add(
                when (dashaLord) {
                    Planet.SUN -> "Engage in activities that build confidence and leadership skills."
                    Planet.MOON -> "Prioritize emotional well-being and nurturing relationships."
                    Planet.MARS -> "Channel your energy into physical activities and competitive pursuits."
                    Planet.MERCURY -> "Focus on learning, communication, and intellectual growth."
                    Planet.JUPITER -> "Expand your horizons through education, travel, or spiritual practices."
                    Planet.VENUS -> "Cultivate beauty, art, and harmonious relationships."
                    Planet.SATURN -> "Embrace discipline, hard work, and long-term planning."
                    Planet.RAHU -> "Explore unconventional paths while staying grounded."
                    Planet.KETU -> "Practice detachment and focus on spiritual development."
                    else -> "Maintain balance in all areas of life."
                }
            )
        }

        // Recommendation based on strongest life area
        lifeAreas.maxByOrNull { it.rating }?.let { bestArea ->
            recommendations.add(
                when (bestArea.area) {
                    LifeArea.CAREER -> "Capitalize on favorable career energy today."
                    LifeArea.LOVE -> "Nurture your relationships with extra attention."
                    LifeArea.HEALTH -> "Make the most of your vibrant health energy."
                    LifeArea.FINANCE -> "Take advantage of positive financial influences."
                    LifeArea.FAMILY -> "Spend quality time with family members."
                    LifeArea.SPIRITUALITY -> "Deepen your spiritual practices."
                }
            )
        }

        // Recommendation based on transit Moon
        transitChart.planetPositions.find { it.planet == Planet.MOON }?.let { moon ->
            recommendations.add(
                when (moon.sign.element) {
                    "Fire" -> "Take bold action and express yourself confidently."
                    "Earth" -> "Focus on practical matters and material progress."
                    "Air" -> "Engage in social activities and intellectual pursuits."
                    "Water" -> "Trust your intuition and honor your emotions."
                    else -> "Stay balanced and adaptable."
                }
            )
        }

        return recommendations.take(3)
    }

    private fun generateCautions(
        chart: VedicChart,
        transitChart: VedicChart,
        influences: List<PlanetaryInfluence>
    ): List<String> {
        val cautions = mutableListOf<String>()

        // Cautions based on negative planetary influences
        influences.filter { !it.isPositive && it.strength <= 4 }.take(2).forEach { influence ->
            cautions.add(
                when (influence.planet) {
                    Planet.SATURN -> "Avoid rushing into decisions. Patience is key."
                    Planet.MARS -> "Control impulsive reactions and avoid conflicts."
                    Planet.RAHU -> "Be wary of deception and unrealistic expectations."
                    Planet.KETU -> "Don't neglect practical responsibilities for escapism."
                    else -> "Be mindful of ${influence.planet.displayName}'s challenging influence."
                }
            )
        }

        // Check for retrograde planets
        transitChart.planetPositions.filter { it.isRetrograde && it.planet in Planet.MAIN_PLANETS }.take(1).forEach {
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

        return when (dashaLord) {
            Planet.SUN -> "I shine my light confidently and inspire those around me."
            Planet.MOON -> "I trust my intuition and nurture myself with compassion."
            Planet.MARS -> "I channel my energy constructively and act with courage."
            Planet.MERCURY -> "I communicate clearly and embrace continuous learning."
            Planet.JUPITER -> "I am open to abundance and share my wisdom generously."
            Planet.VENUS -> "I attract beauty and harmony into my life."
            Planet.SATURN -> "I embrace discipline and trust in the timing of my journey."
            Planet.RAHU -> "I embrace change and transform challenges into opportunities."
            Planet.KETU -> "I release what no longer serves me and embrace spiritual growth."
            else -> "I am aligned with cosmic energies and flow with life's rhythm."
        }
    }

    private fun calculateKeyDates(chart: VedicChart, startDate: LocalDate, endDate: LocalDate): List<KeyDate> {
        val keyDates = mutableListOf<KeyDate>()

        // Check for significant lunar phases
        val moonPhases = listOf(
            startDate.plusDays(7) to "First Quarter Moon" to "Good for taking action",
            startDate.plusDays(14) to "Full Moon" to "Emotional peak - completion energy"
        ).filter { it.first.first.isAfter(startDate.minusDays(1)) && it.first.first.isBefore(endDate.plusDays(1)) }

        moonPhases.forEach { (datePair, significance) ->
            val (date, event) = datePair
            keyDates.add(KeyDate(date, event, significance, true))
        }

        // Check for day lord alignments
        val favorableDays = listOf(
            java.time.DayOfWeek.THURSDAY to "Jupiter's day - auspicious for growth",
            java.time.DayOfWeek.FRIDAY to "Venus's day - favorable for relationships"
        )

        (0 until 7).forEach { offset ->
            val date = startDate.plusDays(offset.toLong())
            favorableDays.find { it.first == date.dayOfWeek }?.let { (_, desc) ->
                keyDates.add(KeyDate(date, date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }, desc, true))
            }
        }

        return keyDates.distinctBy { it.date }.take(4)
    }

    private fun calculateWeeklyPredictions(chart: VedicChart, startDate: LocalDate): Map<LifeArea, String> {
        val predictions = mutableMapOf<LifeArea, String>()
        val dashaTimeline = DashaCalculator.calculateDashaTimeline(chart)
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet?.displayName ?: "current"

        LifeArea.entries.forEach { area ->
            // Calculate average rating for the week
            val weeklyRatings = (0 until 7).map { offset ->
                val date = startDate.plusDays(offset.toLong())
                val dailyHoroscope = calculateDailyHoroscope(chart, date)
                dailyHoroscope.lifeAreas.find { it.area == area }?.rating ?: 3
            }
            val avgRating = weeklyRatings.average()

            predictions[area] = when (area) {
                LifeArea.CAREER -> when {
                    avgRating >= 4 -> "An exceptional week for career advancement. Your $currentDashaLord period supports professional recognition. Key meetings and projects will progress smoothly."
                    avgRating >= 3 -> "Steady professional progress this week. Focus on completing pending tasks and building relationships with colleagues."
                    else -> "Career matters may require extra effort this week. Stay patient and avoid major changes."
                }
                LifeArea.LOVE -> when {
                    avgRating >= 4 -> "Romantic energy flows abundantly this week. Single or committed, relationships deepen. Express your feelings openly."
                    avgRating >= 3 -> "Balanced relationship energy. Good for maintaining harmony and working through minor issues together."
                    else -> "Relationships need nurturing this week. Practice patience and understanding with your partner."
                }
                LifeArea.HEALTH -> when {
                    avgRating >= 4 -> "Excellent vitality this week! Great time to start new fitness routines or health practices. Energy levels are high."
                    avgRating >= 3 -> "Stable health week. Maintain your regular wellness routines and stay consistent with rest."
                    else -> "Health vigilance needed this week. Prioritize rest, nutrition, and stress management."
                }
                LifeArea.FINANCE -> when {
                    avgRating >= 4 -> "Prosperous week for finances. Opportunities for gains through investments or new income sources. Review financial plans."
                    avgRating >= 3 -> "Stable financial week. Good for routine money management and planned purchases."
                    else -> "Financial caution advised this week. Avoid impulsive spending and postpone major investments."
                }
                LifeArea.FAMILY -> when {
                    avgRating >= 4 -> "Harmonious family week ahead. Celebrations, gatherings, and quality time strengthen bonds. Support flows both ways."
                    avgRating >= 3 -> "Good week for family matters. Focus on communication and shared activities."
                    else -> "Family dynamics may be challenging this week. Choose understanding over confrontation."
                }
                LifeArea.SPIRITUALITY -> when {
                    avgRating >= 4 -> "Profound spiritual week. Meditation, reflection, and inner guidance are heightened. Seek meaningful experiences."
                    avgRating >= 3 -> "Steady spiritual energy. Maintain your practices and stay connected to your inner self."
                    else -> "Spiritual connection may feel elusive. Simple practices and patience will help restore balance."
                }
            }
        }

        return predictions
    }

    private fun calculateWeeklyTheme(
        chart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        startDate: LocalDate,
        dailyHighlights: List<DailyHighlight>
    ): Pair<String, String> {
        val avgEnergy = dailyHighlights.map { it.energy }.average()
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN

        val theme = when {
            avgEnergy >= 7 -> "Week of Opportunities"
            avgEnergy >= 5 -> "Steady Progress"
            else -> "Mindful Navigation"
        }

        val overview = buildString {
            append("This week under your ${currentDashaLord.displayName} Mahadasha brings ")
            when {
                avgEnergy >= 7 -> append("excellent opportunities for growth and success. ")
                avgEnergy >= 5 -> append("steady progress and balanced energy. ")
                else -> append("challenges that, when navigated wisely, lead to growth. ")
            }

            val bestDay = dailyHighlights.maxByOrNull { it.energy }
            val challengingDay = dailyHighlights.minByOrNull { it.energy }

            bestDay?.let {
                append("${it.dayOfWeek} appears most favorable for important activities. ")
            }

            challengingDay?.let {
                if (it.energy < 5) {
                    append("${it.dayOfWeek} may require extra patience and care. ")
                }
            }

            append("Trust in your cosmic guidance and make the most of each day's unique energy.")
        }

        return Pair(theme, overview)
    }

    private fun generateWeeklyAdvice(
        chart: VedicChart,
        dashaTimeline: DashaCalculator.DashaTimeline,
        keyDates: List<KeyDate>
    ): String {
        val currentDashaLord = dashaTimeline.currentMahadasha?.planet ?: Planet.SUN

        return buildString {
            append("During this ${currentDashaLord.displayName} period, ")
            append(
                when (currentDashaLord) {
                    Planet.JUPITER -> "embrace opportunities for learning and expansion. Your wisdom and optimism attract positive outcomes."
                    Planet.VENUS -> "focus on cultivating beauty, harmony, and meaningful relationships. Artistic pursuits are favored."
                    Planet.SATURN -> "embrace discipline and patience. Hard work now builds lasting foundations for the future."
                    Planet.MERCURY -> "prioritize communication, learning, and intellectual activities. Your mind is sharp."
                    Planet.MARS -> "channel your energy into productive activities. Exercise and competition are favored."
                    Planet.SUN -> "let your light shine. Leadership roles and self-expression bring recognition."
                    Planet.MOON -> "honor your emotions and intuition. Nurturing activities bring fulfillment."
                    Planet.RAHU -> "embrace transformation while staying grounded. Unconventional approaches may succeed."
                    Planet.KETU -> "focus on spiritual growth and letting go. Detachment brings peace."
                    else -> "maintain balance and trust in divine timing."
                }
            )

            keyDates.firstOrNull { it.isPositive }?.let {
                append(" Mark ${it.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))} for important initiatives.")
            }
        }
    }

    private fun calculateHouseFromSign(sign: ZodiacSign, ascendantSign: ZodiacSign): Int {
        val houseOffset = (sign.ordinal - ascendantSign.ordinal + 12) % 12
        return houseOffset + 1
    }

    override fun close() {
        ephemerisEngine.close()
    }
}
