package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class VarshaphalaCalculator(context: Context) {

    private val swissEph = SwissEph()
    private val ephemerisPath: String

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED
        private const val TROPICAL_YEAR_DAYS = 365.24219
        private const val SIDEREAL_YEAR_DAYS = 365.256363

        private const val CONJUNCTION_ORB = 12.0
        private const val OPPOSITION_ORB = 9.0
        private const val TRINE_ORB = 8.0
        private const val SQUARE_ORB = 7.0
        private const val SEXTILE_ORB = 6.0

        private val VIMSHOTTARI_YEARS = mapOf(
            Planet.KETU to 7,
            Planet.VENUS to 20,
            Planet.SUN to 6,
            Planet.MOON to 10,
            Planet.MARS to 7,
            Planet.RAHU to 18,
            Planet.JUPITER to 16,
            Planet.SATURN to 19,
            Planet.MERCURY to 17
        )

        private val VIMSHOTTARI_ORDER = listOf(
            Planet.KETU, Planet.VENUS, Planet.SUN, Planet.MOON, Planet.MARS,
            Planet.RAHU, Planet.JUPITER, Planet.SATURN, Planet.MERCURY
        )

        private val DAY_LORDS = listOf(
            Planet.SUN,     // Sunday = 0
            Planet.MOON,    // Monday = 1
            Planet.MARS,    // Tuesday = 2
            Planet.MERCURY, // Wednesday = 3
            Planet.JUPITER, // Thursday = 4
            Planet.VENUS,   // Friday = 5
            Planet.SATURN   // Saturday = 6
        )

        private val HADDA_LORDS = mapOf(
            ZodiacSign.ARIES to listOf(
                Pair(0.0 to 6.0, Planet.JUPITER),
                Pair(6.0 to 12.0, Planet.VENUS),
                Pair(12.0 to 20.0, Planet.MERCURY),
                Pair(20.0 to 25.0, Planet.MARS),
                Pair(25.0 to 30.0, Planet.SATURN)
            ),
            ZodiacSign.TAURUS to listOf(
                Pair(0.0 to 8.0, Planet.VENUS),
                Pair(8.0 to 14.0, Planet.MERCURY),
                Pair(14.0 to 22.0, Planet.JUPITER),
                Pair(22.0 to 27.0, Planet.SATURN),
                Pair(27.0 to 30.0, Planet.MARS)
            ),
            ZodiacSign.GEMINI to listOf(
                Pair(0.0 to 6.0, Planet.MERCURY),
                Pair(6.0 to 12.0, Planet.JUPITER),
                Pair(12.0 to 17.0, Planet.VENUS),
                Pair(17.0 to 24.0, Planet.MARS),
                Pair(24.0 to 30.0, Planet.SATURN)
            ),
            ZodiacSign.CANCER to listOf(
                Pair(0.0 to 7.0, Planet.MARS),
                Pair(7.0 to 13.0, Planet.VENUS),
                Pair(13.0 to 19.0, Planet.MERCURY),
                Pair(19.0 to 26.0, Planet.JUPITER),
                Pair(26.0 to 30.0, Planet.SATURN)
            ),
            ZodiacSign.LEO to listOf(
                Pair(0.0 to 6.0, Planet.JUPITER),
                Pair(6.0 to 11.0, Planet.VENUS),
                Pair(11.0 to 18.0, Planet.SATURN),
                Pair(18.0 to 24.0, Planet.MERCURY),
                Pair(24.0 to 30.0, Planet.MARS)
            ),
            ZodiacSign.VIRGO to listOf(
                Pair(0.0 to 7.0, Planet.MERCURY),
                Pair(7.0 to 17.0, Planet.VENUS),
                Pair(17.0 to 21.0, Planet.JUPITER),
                Pair(21.0 to 28.0, Planet.MARS),
                Pair(28.0 to 30.0, Planet.SATURN)
            ),
            ZodiacSign.LIBRA to listOf(
                Pair(0.0 to 6.0, Planet.SATURN),
                Pair(6.0 to 14.0, Planet.MERCURY),
                Pair(14.0 to 21.0, Planet.JUPITER),
                Pair(21.0 to 28.0, Planet.VENUS),
                Pair(28.0 to 30.0, Planet.MARS)
            ),
            ZodiacSign.SCORPIO to listOf(
                Pair(0.0 to 7.0, Planet.MARS),
                Pair(7.0 to 11.0, Planet.VENUS),
                Pair(11.0 to 19.0, Planet.MERCURY),
                Pair(19.0 to 24.0, Planet.JUPITER),
                Pair(24.0 to 30.0, Planet.SATURN)
            ),
            ZodiacSign.SAGITTARIUS to listOf(
                Pair(0.0 to 12.0, Planet.JUPITER),
                Pair(12.0 to 17.0, Planet.VENUS),
                Pair(17.0 to 21.0, Planet.MERCURY),
                Pair(21.0 to 26.0, Planet.SATURN),
                Pair(26.0 to 30.0, Planet.MARS)
            ),
            ZodiacSign.CAPRICORN to listOf(
                Pair(0.0 to 7.0, Planet.MERCURY),
                Pair(7.0 to 14.0, Planet.JUPITER),
                Pair(14.0 to 22.0, Planet.VENUS),
                Pair(22.0 to 26.0, Planet.SATURN),
                Pair(26.0 to 30.0, Planet.MARS)
            ),
            ZodiacSign.AQUARIUS to listOf(
                Pair(0.0 to 7.0, Planet.MERCURY),
                Pair(7.0 to 13.0, Planet.VENUS),
                Pair(13.0 to 20.0, Planet.JUPITER),
                Pair(20.0 to 25.0, Planet.MARS),
                Pair(25.0 to 30.0, Planet.SATURN)
            ),
            ZodiacSign.PISCES to listOf(
                Pair(0.0 to 12.0, Planet.VENUS),
                Pair(12.0 to 16.0, Planet.JUPITER),
                Pair(16.0 to 19.0, Planet.MERCURY),
                Pair(19.0 to 28.0, Planet.MARS),
                Pair(28.0 to 30.0, Planet.SATURN)
            )
        )

        fun getHouseMeaning(house: Int): String {
            return when (house) {
                1 -> "Self/Personality"
                2 -> "Wealth/Family"
                3 -> "Siblings/Communication"
                4 -> "Home/Mother"
                5 -> "Children/Creativity"
                6 -> "Health/Service"
                7 -> "Partnership/Marriage"
                8 -> "Transformation/Longevity"
                9 -> "Fortune/Spirituality"
                10 -> "Career/Status"
                11 -> "Gains/Friends"
                12 -> "Losses/Spirituality"
                else -> "General"
            }
        }

        fun getHouseKeywords(house: Int): List<String> {
            return when (house) {
                1 -> listOf("Self", "Body", "Personality", "Appearance")
                2 -> listOf("Wealth", "Family", "Speech", "Food")
                3 -> listOf("Siblings", "Courage", "Short Travels", "Communication")
                4 -> listOf("Mother", "Home", "Property", "Happiness")
                5 -> listOf("Children", "Intelligence", "Romance", "Speculation")
                6 -> listOf("Enemies", "Disease", "Service", "Debts")
                7 -> listOf("Spouse", "Partnership", "Business", "Public")
                8 -> listOf("Death", "Inheritance", "Occult", "Transformation")
                9 -> listOf("Father", "Guru", "Religion", "Fortune")
                10 -> listOf("Career", "Fame", "Authority", "Government")
                11 -> listOf("Gains", "Friends", "Elder Sibling", "Desires")
                12 -> listOf("Losses", "Foreign", "Liberation", "Expenses")
                else -> listOf()
            }
        }
    }

    init {
        ephemerisPath = context.filesDir.absolutePath + "/ephe"
        swissEph.swe_set_ephe_path(ephemerisPath)
        swissEph.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
    }

    enum class TajikaAspect(val displayName: String, val description: String) {
        ITHASALA("Ithasala", "Applying aspect - promises fulfillment"),
        EASARAPHA("Easarapha", "Separating aspect - indicates past events"),
        NAKTA("Nakta", "Transfer of light - indirect completion"),
        YAMAYA("Yamaya", "Prohibition - obstruction in matters"),
        MANAU("Manau", "Frustration - denial of results"),
        KAMBOOLA("Kamboola", "Favorable reception - magnifies results"),
        GAIRI_KAMBOOLA("Gairi Kamboola", "Unfavorable reception - weakens results"),
        KHALASARA("Khalasara", "Mutual separation - dissolution"),
        RADDA("Radda", "Return/Retrograde aspect - reconsideration"),
        DUKPHALI("Dukphali", "Obstruction by malefic")
    }

    enum class Saham(val displayName: String, val description: String) {
        PUNYA("Punya Saham", "Fortune/Luck"),
        VIDYA("Vidya Saham", "Education/Learning"),
        YASHAS("Yashas Saham", "Fame/Reputation"),
        MITRA("Mitra Saham", "Friends"),
        MAHATMYA("Mahatmya Saham", "Greatness"),
        KARMA("Karma Saham", "Profession"),
        BANDHANA("Bandhana Saham", "Bondage/Imprisonment"),
        MRITYU("Mrityu Saham", "Death/Danger"),
        ASHA("Asha Saham", "Hopes/Desires"),
        SAMARTHA("Samartha Saham", "Capability"),
        VIVAHA("Vivaha Saham", "Marriage"),
        SANTANA("Santana Saham", "Children"),
        ROGA("Roga Saham", "Disease"),
        KARYASIDDHI("Karyasiddhi Saham", "Success in endeavors"),
        PARADESA("Paradesa Saham", "Foreign Travel"),
        PITRI("Pitri Saham", "Father"),
        MATRI("Matri Saham", "Mother"),
        DHANA("Dhana Saham", "Wealth"),
        RAJA("Raja Saham", "Authority/Power")
    }

    data class SolarReturnChart(
        val year: Int,
        val solarReturnTime: LocalDateTime,
        val solarReturnTimeUtc: LocalDateTime,
        val julianDay: Double,
        val planetPositions: List<PlanetPosition>,
        val ascendant: Double,
        val midheaven: Double,
        val houseCusps: List<Double>,
        val ayanamsa: Double,
        val isDayBirth: Boolean
    )

    data class MunthaInfo(
        val longitude: Double,
        val sign: ZodiacSign,
        val degreeInSign: Double,
        val house: Int,
        val lord: Planet,
        val lordStrength: Double,
        val lordHouse: Int,
        val interpretation: String
    )

    data class SahamResult(
        val saham: Saham,
        val longitude: Double,
        val sign: ZodiacSign,
        val degreeInSign: Double,
        val house: Int,
        val lord: Planet,
        val interpretation: String
    )

    data class TajikaAspectResult(
        val planet1: Planet,
        val planet2: Planet,
        val aspect: TajikaAspect,
        val aspectType: String,
        val orb: Double,
        val isApplying: Boolean,
        val daysToExact: Double,
        val interpretation: String
    )

    data class MuddaDashaPeriod(
        val planet: Planet,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val durationDays: Double,
        val isFirstPeriod: Boolean
    )

    data class HousePrediction(
        val house: Int,
        val signOnCusp: ZodiacSign,
        val cuspDegree: Double,
        val houseLord: Planet,
        val lordPosition: Int,
        val lordSign: ZodiacSign,
        val lordIsRetrograde: Boolean,
        val planetsInHouse: List<Planet>,
        val aspectingPlanets: List<Pair<Planet, String>>,
        val strength: String,
        val strengthScore: Int,
        val prediction: String,
        val keywords: List<String>
    )

    data class PanchaVargiyaBala(
        val planet: Planet,
        val haddaBala: Int,
        val drekkanaBala: Int,
        val navamsaBala: Int,
        val dwadasamsaBala: Int,
        val trimsamsaBala: Int,
        val totalBala: Int,
        val classification: String
    )

    data class TriPatakiChakra(
        val rashiGroups: Map<String, List<ZodiacSign>>,
        val planetPlacements: Map<String, List<Planet>>,
        val sthiraRashis: List<ZodiacSign>,
        val charRashis: List<ZodiacSign>,
        val dwiswabhavaRashis: List<ZodiacSign>,
        val dominantGroup: String,
        val interpretation: String
    )

    data class VarshaphalaResult(
        val natalChart: VedicChart,
        val year: Int,
        val age: Int,
        val solarReturnChart: SolarReturnChart,
        val muntha: MunthaInfo,
        val yearLord: Planet,
        val yearLordStrength: String,
        val yearLordBala: PanchaVargiyaBala,
        val allPlanetBalas: List<PanchaVargiyaBala>,
        val triPatakiChakra: TriPatakiChakra,
        val sahams: List<SahamResult>,
        val tajikaAspects: List<TajikaAspectResult>,
        val muddaDasha: List<MuddaDashaPeriod>,
        val housePredictions: List<HousePrediction>,
        val majorThemes: List<String>,
        val favorableMonths: List<Int>,
        val challengingMonths: List<Int>,
        val overallPrediction: String,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun toPlainText(): String = buildString {
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("            VARSHAPHALA (ANNUAL HOROSCOPE) REPORT")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine("Name: ${natalChart.birthData.name}")
            appendLine("Year: $year (Age: $age)")
            appendLine("Solar Return: ${solarReturnChart.solarReturnTime}")
            appendLine("Ayanamsa: ${String.format("%.6f", solarReturnChart.ayanamsa)}°")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                      YEAR LORD")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("Year Lord: ${yearLord.displayName} ($yearLordStrength)")
            appendLine("Pancha-Vargiya Bala: ${yearLordBala.totalBala}/20 (${yearLordBala.classification})")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                       MUNTHA")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("Muntha Position: ${String.format("%.2f", muntha.degreeInSign)}° ${muntha.sign.displayName}")
            appendLine("Muntha House: ${muntha.house}")
            appendLine("Muntha Lord: ${muntha.lord.displayName} in House ${muntha.lordHouse}")
            appendLine(muntha.interpretation)
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                  TRI-PATAKI CHAKRA")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("Dominant Group: ${triPatakiChakra.dominantGroup}")
            appendLine(triPatakiChakra.interpretation)
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("              PANCHA-VARGIYA BALA (All Planets)")
            appendLine("─────────────────────────────────────────────────────────")
            allPlanetBalas.forEach { bala ->
                appendLine("${bala.planet.displayName.padEnd(10)}: ${bala.totalBala}/20 (${bala.classification})")
            }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                    MAJOR THEMES")
            appendLine("─────────────────────────────────────────────────────────")
            majorThemes.forEach { appendLine("• $it") }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                     SAHAMS")
            appendLine("─────────────────────────────────────────────────────────")
            sahams.forEach { saham ->
                appendLine("${saham.saham.displayName}: ${String.format("%.2f", saham.degreeInSign)}° ${saham.sign.displayName} (House ${saham.house})")
            }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                   MUDDA DASHA")
            appendLine("─────────────────────────────────────────────────────────")
            muddaDasha.forEach { period ->
                val marker = if (period.isFirstPeriod) " (Balance)" else ""
                appendLine("${period.planet.displayName}: ${period.startDate.toLocalDate()} to ${period.endDate.toLocalDate()} (${String.format("%.1f", period.durationDays)} days)$marker")
            }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                  TAJIKA ASPECTS")
            appendLine("─────────────────────────────────────────────────────────")
            tajikaAspects.take(10).forEach { aspect ->
                appendLine("${aspect.planet1.displayName} - ${aspect.planet2.displayName}: ${aspect.aspect.displayName} (${aspect.aspectType}, orb ${String.format("%.2f", aspect.orb)}°)")
            }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                 HOUSE PREDICTIONS")
            appendLine("─────────────────────────────────────────────────────────")
            housePredictions.forEach { prediction ->
                appendLine()
                appendLine("House ${prediction.house} - ${prediction.signOnCusp.displayName} (${prediction.strength})")
                appendLine("Lord: ${prediction.houseLord.displayName} in House ${prediction.lordPosition}")
                if (prediction.planetsInHouse.isNotEmpty()) {
                    appendLine("Planets: ${prediction.planetsInHouse.joinToString { it.displayName }}")
                }
                appendLine(prediction.prediction)
            }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("              FAVORABLE MONTHS: ${favorableMonths.joinToString()}")
            appendLine("            CHALLENGING MONTHS: ${challengingMonths.joinToString()}")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine()
            appendLine("                   OVERALL PREDICTION")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine(overallPrediction)
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("Generated by AstroStorm - Ultra-Precision Vedic Astrology")
            appendLine("═══════════════════════════════════════════════════════════")
        }
    }

    fun calculateVarshaphala(natalChart: VedicChart, year: Int): VarshaphalaResult {
        val birthDateTime = natalChart.birthData.dateTime
        val birthYear = birthDateTime.year
        val age = year - birthYear

        require(age >= 0) { "Year must be after birth year" }

        val solarReturnTime = calculateSolarReturnTime(
            natalChart.birthData.dateTime,
            year,
            natalChart.birthData.latitude,
            natalChart.birthData.longitude,
            natalChart.birthData.timezone
        )

        val solarReturnChart = calculateSolarReturnChart(
            solarReturnTime,
            natalChart.birthData.latitude,
            natalChart.birthData.longitude,
            natalChart.birthData.timezone
        )

        val muntha = calculateMuntha(natalChart, age, solarReturnChart)
        val allPlanetBalas = calculateAllPanchaVargiyaBalas(solarReturnChart)
        val yearLord = determineYearLord(solarReturnChart, muntha, natalChart, allPlanetBalas)
        val yearLordStrength = evaluatePlanetStrengthDescription(yearLord, solarReturnChart)
        val yearLordBala = allPlanetBalas.find { it.planet == yearLord }
            ?: calculatePanchaVargiyaBala(yearLord, solarReturnChart)
        val triPatakiChakra = calculateTriPatakiChakra(solarReturnChart)
        val sahams = calculateSahams(solarReturnChart)
        val tajikaAspects = calculateTajikaAspects(solarReturnChart)
        val muddaDasha = calculateMuddaDasha(solarReturnTime, solarReturnChart)
        val housePredictions = generateHousePredictions(solarReturnChart, muntha, yearLord, natalChart)
        val majorThemes = identifyMajorThemes(solarReturnChart, muntha, yearLord, housePredictions, triPatakiChakra)
        val favorableMonths = determineFavorableMonths(muddaDasha, solarReturnChart)
        val challengingMonths = determineChallengingMonths(muddaDasha, solarReturnChart)
        val overallPrediction = generateOverallPrediction(
            yearLord, muntha, housePredictions, tajikaAspects, age, yearLordBala, triPatakiChakra
        )

        return VarshaphalaResult(
            natalChart = natalChart,
            year = year,
            age = age,
            solarReturnChart = solarReturnChart,
            muntha = muntha,
            yearLord = yearLord,
            yearLordStrength = yearLordStrength,
            yearLordBala = yearLordBala,
            allPlanetBalas = allPlanetBalas,
            triPatakiChakra = triPatakiChakra,
            sahams = sahams,
            tajikaAspects = tajikaAspects,
            muddaDasha = muddaDasha,
            housePredictions = housePredictions,
            majorThemes = majorThemes,
            favorableMonths = favorableMonths,
            challengingMonths = challengingMonths,
            overallPrediction = overallPrediction
        )
    }

    private fun calculateSolarReturnTime(
        birthDateTime: LocalDateTime,
        targetYear: Int,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val birthZoned = ZonedDateTime.of(birthDateTime, ZoneId.of(timezone))
        val birthUtc = birthZoned.withZoneSameInstant(ZoneId.of("UTC"))
        val birthJd = calculateJulianDay(birthUtc.toLocalDateTime())
        val natalSunLong = getPlanetLongitude(SweConst.SE_SUN, birthJd)

        val yearsElapsed = targetYear - birthDateTime.year
        val approximateJd = birthJd + (yearsElapsed * SIDEREAL_YEAR_DAYS)
        var currentJd = approximateJd

        repeat(50) {
            val currentSunLong = getPlanetLongitude(SweConst.SE_SUN, currentJd)
            var diff = natalSunLong - currentSunLong

            while (diff > 180.0) diff -= 360.0
            while (diff < -180.0) diff += 360.0

            if (abs(diff) < 0.0000001) {
                return jdToLocalDateTime(currentJd, timezone)
            }

            val sunSpeed = getSunSpeed(currentJd)
            val correction = diff / sunSpeed
            currentJd += correction

            if (abs(correction) < 0.00001) {
                return jdToLocalDateTime(currentJd, timezone)
            }
        }

        return jdToLocalDateTime(currentJd, timezone)
    }

    private fun getSunSpeed(julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swissEph.swe_calc_ut(julianDay, SweConst.SE_SUN, SEFLG_SIDEREAL or SEFLG_SPEED, xx, serr)
        return if (xx[3] != 0.0) xx[3] else 0.9856
    }

    private fun calculateSolarReturnChart(
        solarReturnTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): SolarReturnChart {
        val zonedDateTime = ZonedDateTime.of(solarReturnTime, ZoneId.of(timezone))
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())
        val ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay)

        val planetPositions = mutableListOf<PlanetPosition>()
        for (planet in Planet.MAIN_PLANETS) {
            val position = calculatePlanetPosition(planet, julianDay, latitude, longitude)
            planetPositions.add(position)
        }

        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swissEph.swe_houses(julianDay, SweConst.SEFLG_SIDEREAL, latitude, longitude, 'W'.code, cusps, ascmc)

        val houseCusps = (1..12).map { cusps[it] }
        val ascendant = cusps[1]
        val midheaven = ascmc[1]

        val sunPos = planetPositions.find { it.planet == Planet.SUN }?.longitude ?: 0.0
        val isDayBirth = isDayChart(sunPos, ascendant)

        val positionsWithHouses = planetPositions.map { pos ->
            pos.copy(house = calculateWholeSignHouse(pos.longitude, ascendant))
        }

        return SolarReturnChart(
            year = solarReturnTime.year,
            solarReturnTime = solarReturnTime,
            solarReturnTimeUtc = utcDateTime.toLocalDateTime(),
            julianDay = julianDay,
            planetPositions = positionsWithHouses,
            ascendant = ascendant,
            midheaven = midheaven,
            houseCusps = houseCusps,
            ayanamsa = ayanamsa,
            isDayBirth = isDayBirth
        )
    }

    private fun calculateWholeSignHouse(longitude: Double, ascendant: Double): Int {
        val ascSign = (ascendant / 30.0).toInt()
        val planetSign = (longitude / 30.0).toInt()
        val house = ((planetSign - ascSign + 12) % 12) + 1
        return house
    }

    private fun isDayChart(sunLongitude: Double, ascendant: Double): Boolean {
        val sunSign = (sunLongitude / 30.0).toInt()
        val ascSign = (ascendant / 30.0).toInt()
        val houseOfSun = ((sunSign - ascSign + 12) % 12) + 1
        return houseOfSun in listOf(7, 8, 9, 10, 11, 12, 1)
    }

    private fun calculateMuntha(
        natalChart: VedicChart,
        age: Int,
        solarReturnChart: SolarReturnChart
    ): MunthaInfo {
        val natalAscLongitude = natalChart.ascendant
        val progressedLongitude = normalizeAngle360(natalAscLongitude + (age * 30.0))
        val munthaSign = ZodiacSign.fromLongitude(progressedLongitude)
        val degreeInSign = progressedLongitude % 30.0
        val munthaHouse = calculateWholeSignHouse(progressedLongitude, solarReturnChart.ascendant)
        val munthaLord = munthaSign.ruler

        val lordPosition = solarReturnChart.planetPositions.find { it.planet == munthaLord }
        val lordHouse = lordPosition?.house ?: 1
        val lordStrength = evaluatePlanetStrengthScore(munthaLord, solarReturnChart)

        val interpretation = generateMunthaInterpretation(munthaSign, munthaHouse, munthaLord, lordHouse)

        return MunthaInfo(
            longitude = progressedLongitude,
            sign = munthaSign,
            degreeInSign = degreeInSign,
            house = munthaHouse,
            lord = munthaLord,
            lordStrength = lordStrength,
            lordHouse = lordHouse,
            interpretation = interpretation
        )
    }

    private fun determineYearLord(
        solarReturnChart: SolarReturnChart,
        muntha: MunthaInfo,
        natalChart: VedicChart,
        allBalas: List<PanchaVargiyaBala>
    ): Planet {
        val dayOfWeek = solarReturnChart.solarReturnTime.dayOfWeek
        val dayIndex = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
        val dinaPati = DAY_LORDS[dayIndex]

        val ascSign = ZodiacSign.fromLongitude(solarReturnChart.ascendant)
        val lagnaPati = ascSign.ruler

        val munthaPati = muntha.lord

        val natalMoonSign = natalChart.planetPositions.find { it.planet == Planet.MOON }?.sign
            ?: ZodiacSign.ARIES
        val janmaRashiPati = natalMoonSign.ruler

        val candidates = listOf(dinaPati, lagnaPati, munthaPati, janmaRashiPati).distinct()

        val candidatesWithStrength = candidates.map { planet ->
            val bala = allBalas.find { it.planet == planet }?.totalBala ?: 0
            val additionalStrength = calculateAdditionalStrength(planet, solarReturnChart)
            planet to (bala + additionalStrength)
        }

        return candidatesWithStrength.maxByOrNull { it.second }?.first ?: dinaPati
    }

    private fun calculateAdditionalStrength(planet: Planet, chart: SolarReturnChart): Int {
        var strength = 0
        val position = chart.planetPositions.find { it.planet == planet } ?: return 0

        if (position.house in listOf(1, 4, 7, 10)) strength += 5
        if (position.house in listOf(5, 9)) strength += 3
        if (position.house in listOf(6, 8, 12)) strength -= 3

        if (position.sign.ruler == planet) strength += 4
        if (isExalted(planet, position.sign)) strength += 5
        if (isDebilitated(planet, position.sign)) strength -= 5

        if (!position.isRetrograde) strength += 1

        return strength
    }

    private fun calculateAllPanchaVargiyaBalas(chart: SolarReturnChart): List<PanchaVargiyaBala> {
        return Planet.MAIN_PLANETS.filter { it != Planet.RAHU && it != Planet.KETU }
            .map { calculatePanchaVargiyaBala(it, chart) }
    }

    private fun calculatePanchaVargiyaBala(planet: Planet, chart: SolarReturnChart): PanchaVargiyaBala {
        val position = chart.planetPositions.find { it.planet == planet }
            ?: return PanchaVargiyaBala(planet, 0, 0, 0, 0, 0, 0, "Unknown")

        val longitude = position.longitude
        val sign = position.sign
        val degreeInSign = longitude % 30.0

        val haddaBala = calculateHaddaBala(planet, sign, degreeInSign)
        val drekkanaBala = calculateDrekkanaBala(planet, sign, degreeInSign)
        val navamsaBala = calculateNavamsaBala(planet, longitude)
        val dwadasamsaBala = calculateDwadasamsaBala(planet, longitude)
        val trimsamsaBala = calculateTrimsamsaBala(planet, sign, degreeInSign)

        val totalBala = haddaBala + drekkanaBala + navamsaBala + dwadasamsaBala + trimsamsaBala

        val classification = when {
            totalBala >= 15 -> "Poorna Bali (Full Strength)"
            totalBala >= 10 -> "Ardha Bali (Half Strength)"
            totalBala >= 5 -> "Alpa Bali (Low Strength)"
            else -> "Bala Hina (Weak)"
        }

        return PanchaVargiyaBala(
            planet = planet,
            haddaBala = haddaBala,
            drekkanaBala = drekkanaBala,
            navamsaBala = navamsaBala,
            dwadasamsaBala = dwadasamsaBala,
            trimsamsaBala = trimsamsaBala,
            totalBala = totalBala,
            classification = classification
        )
    }

    private fun calculateHaddaBala(planet: Planet, sign: ZodiacSign, degree: Double): Int {
        val haddaRanges = HADDA_LORDS[sign] ?: return 0

        for ((range, lord) in haddaRanges) {
            if (degree >= range.first && degree < range.second) {
                return when {
                    lord == planet -> 4
                    areFriends(planet, lord) -> 2
                    areNeutral(planet, lord) -> 1
                    else -> 0
                }
            }
        }
        return 0
    }

    private fun calculateDrekkanaBala(planet: Planet, sign: ZodiacSign, degree: Double): Int {
        val drekkanaNumber = when {
            degree < 10 -> 1
            degree < 20 -> 2
            else -> 3
        }

        val drekkanaSignIndex = (sign.number - 1 + (drekkanaNumber - 1) * 4) % 12
        val drekkanaSign = ZodiacSign.entries[drekkanaSignIndex]
        val drekkanaLord = drekkanaSign.ruler

        return when {
            drekkanaLord == planet -> 4
            areFriends(planet, drekkanaLord) -> 2
            areNeutral(planet, drekkanaLord) -> 1
            else -> 0
        }
    }

    private fun calculateNavamsaBala(planet: Planet, longitude: Double): Int {
        val navamsaIndex = ((longitude % 30.0) / 3.333333333).toInt()
        val signIndex = (longitude / 30.0).toInt()
        val navamsaSignIndex = (signIndex * 9 + navamsaIndex) % 12
        val navamsaSign = ZodiacSign.entries[navamsaSignIndex]
        val navamsaLord = navamsaSign.ruler

        return when {
            navamsaLord == planet -> 4
            areFriends(planet, navamsaLord) -> 2
            areNeutral(planet, navamsaLord) -> 1
            else -> 0
        }
    }

    private fun calculateDwadasamsaBala(planet: Planet, longitude: Double): Int {
        val d12Index = ((longitude % 30.0) / 2.5).toInt()
        val signIndex = (longitude / 30.0).toInt()
        val d12SignIndex = (signIndex + d12Index) % 12
        val d12Sign = ZodiacSign.entries[d12SignIndex]
        val d12Lord = d12Sign.ruler

        return when {
            d12Lord == planet -> 4
            areFriends(planet, d12Lord) -> 2
            areNeutral(planet, d12Lord) -> 1
            else -> 0
        }
    }

    private fun calculateTrimsamsaBala(planet: Planet, sign: ZodiacSign, degree: Double): Int {
        val isOddSign = sign.number % 2 == 1

        val trimsamsaLord = if (isOddSign) {
            when {
                degree < 5 -> Planet.MARS
                degree < 10 -> Planet.SATURN
                degree < 18 -> Planet.JUPITER
                degree < 25 -> Planet.MERCURY
                else -> Planet.VENUS
            }
        } else {
            when {
                degree < 5 -> Planet.VENUS
                degree < 12 -> Planet.MERCURY
                degree < 20 -> Planet.JUPITER
                degree < 25 -> Planet.SATURN
                else -> Planet.MARS
            }
        }

        return when {
            trimsamsaLord == planet -> 4
            areFriends(planet, trimsamsaLord) -> 2
            areNeutral(planet, trimsamsaLord) -> 1
            else -> 0
        }
    }

    private fun areFriends(planet1: Planet, planet2: Planet): Boolean {
        val friendships = mapOf(
            Planet.SUN to listOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
            Planet.MOON to listOf(Planet.SUN, Planet.MERCURY),
            Planet.MARS to listOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
            Planet.MERCURY to listOf(Planet.SUN, Planet.VENUS),
            Planet.JUPITER to listOf(Planet.SUN, Planet.MOON, Planet.MARS),
            Planet.VENUS to listOf(Planet.MERCURY, Planet.SATURN),
            Planet.SATURN to listOf(Planet.MERCURY, Planet.VENUS)
        )
        return friendships[planet1]?.contains(planet2) == true
    }

    private fun areNeutral(planet1: Planet, planet2: Planet): Boolean {
        val neutrals = mapOf(
            Planet.SUN to listOf(Planet.MERCURY),
            Planet.MOON to listOf(Planet.MARS, Planet.JUPITER, Planet.VENUS, Planet.SATURN),
            Planet.MARS to listOf(Planet.VENUS, Planet.SATURN),
            Planet.MERCURY to listOf(Planet.MARS, Planet.JUPITER, Planet.SATURN),
            Planet.JUPITER to listOf(Planet.SATURN, Planet.MERCURY),
            Planet.VENUS to listOf(Planet.MARS, Planet.JUPITER),
            Planet.SATURN to listOf(Planet.JUPITER, Planet.MARS)
        )
        return neutrals[planet1]?.contains(planet2) == true
    }

    private fun calculateTriPatakiChakra(chart: SolarReturnChart): TriPatakiChakra {
        val charRashis = listOf(ZodiacSign.ARIES, ZodiacSign.CANCER, ZodiacSign.LIBRA, ZodiacSign.CAPRICORN)
        val sthiraRashis = listOf(ZodiacSign.TAURUS, ZodiacSign.LEO, ZodiacSign.SCORPIO, ZodiacSign.AQUARIUS)
        val dwiswabhavaRashis = listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO, ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)

        val rashiGroups = mapOf(
            "Chara (Movable)" to charRashis,
            "Sthira (Fixed)" to sthiraRashis,
            "Dwiswabhava (Dual)" to dwiswabhavaRashis
        )

        val charPlanets = mutableListOf<Planet>()
        val sthiraPlanets = mutableListOf<Planet>()
        val dwiswabhavaPlanets = mutableListOf<Planet>()

        for (position in chart.planetPositions) {
            when (position.sign) {
                in charRashis -> charPlanets.add(position.planet)
                in sthiraRashis -> sthiraPlanets.add(position.planet)
                in dwiswabhavaRashis -> dwiswabhavaPlanets.add(position.planet)
                else -> {}
            }
        }

        val ascSign = ZodiacSign.fromLongitude(chart.ascendant)
        when (ascSign) {
            in charRashis -> charPlanets.add(Planet.SUN)
            in sthiraRashis -> sthiraPlanets.add(Planet.SUN)
            in dwiswabhavaRashis -> dwiswabhavaPlanets.add(Planet.SUN)
            else -> {}
        }

        val planetPlacements = mapOf(
            "Chara (Movable)" to charPlanets.toList(),
            "Sthira (Fixed)" to sthiraPlanets.toList(),
            "Dwiswabhava (Dual)" to dwiswabhavaPlanets.toList()
        )

        val dominantGroup = when {
            charPlanets.size >= sthiraPlanets.size && charPlanets.size >= dwiswabhavaPlanets.size -> "Chara (Movable)"
            sthiraPlanets.size >= charPlanets.size && sthiraPlanets.size >= dwiswabhavaPlanets.size -> "Sthira (Fixed)"
            else -> "Dwiswabhava (Dual)"
        }

        val interpretation = when (dominantGroup) {
            "Chara (Movable)" -> "Year emphasizes new beginnings, travel, changes in residence or career. Active, dynamic period with potential relocations."
            "Sthira (Fixed)" -> "Year emphasizes stability, consolidation, persistence. Good for completing long-term projects, building lasting foundations."
            else -> "Year emphasizes flexibility, learning, communication. Dual nature brings both changes and stability in different areas."
        }

        return TriPatakiChakra(
            rashiGroups = rashiGroups,
            planetPlacements = planetPlacements,
            sthiraRashis = sthiraRashis,
            charRashis = charRashis,
            dwiswabhavaRashis = dwiswabhavaRashis,
            dominantGroup = dominantGroup,
            interpretation = interpretation
        )
    }

    private fun calculateSahams(chart: SolarReturnChart): List<SahamResult> {
        val ascendant = chart.ascendant
        val sunPos = chart.planetPositions.find { it.planet == Planet.SUN }?.longitude ?: 0.0
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }?.longitude ?: 0.0
        val marsPos = chart.planetPositions.find { it.planet == Planet.MARS }?.longitude ?: 0.0
        val mercuryPos = chart.planetPositions.find { it.planet == Planet.MERCURY }?.longitude ?: 0.0
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }?.longitude ?: 0.0
        val venusPos = chart.planetPositions.find { it.planet == Planet.VENUS }?.longitude ?: 0.0
        val saturnPos = chart.planetPositions.find { it.planet == Planet.SATURN }?.longitude ?: 0.0

        val punyaLong = if (chart.isDayBirth) {
            normalizeAngle360(ascendant + moonPos - sunPos)
        } else {
            normalizeAngle360(ascendant + sunPos - moonPos)
        }

        return listOf(
            createSaham(Saham.PUNYA, punyaLong, chart),
            createSaham(Saham.VIDYA, normalizeAngle360(ascendant + mercuryPos - jupiterPos), chart),
            createSaham(Saham.YASHAS, normalizeAngle360(ascendant + jupiterPos - punyaLong), chart),
            createSaham(Saham.KARMA, normalizeAngle360(ascendant + saturnPos - sunPos), chart),
            createSaham(Saham.VIVAHA, normalizeAngle360(ascendant + saturnPos - venusPos), chart),
            createSaham(Saham.SANTANA, normalizeAngle360(ascendant + moonPos - jupiterPos), chart),
            createSaham(Saham.ROGA, normalizeAngle360(ascendant + saturnPos - marsPos), chart),
            createSaham(Saham.MITRA, normalizeAngle360(ascendant + jupiterPos - mercuryPos), chart),
            createSaham(Saham.PITRI, if (chart.isDayBirth) normalizeAngle360(ascendant + saturnPos - sunPos) else normalizeAngle360(ascendant + sunPos - saturnPos), chart),
            createSaham(Saham.MATRI, if (chart.isDayBirth) normalizeAngle360(ascendant + moonPos - venusPos) else normalizeAngle360(ascendant + venusPos - moonPos), chart),
            createSaham(Saham.PARADESA, normalizeAngle360(ascendant + saturnPos - moonPos), chart),
            createSaham(Saham.DHANA, normalizeAngle360(ascendant + moonPos - jupiterPos), chart),
            createSaham(Saham.MRITYU, normalizeAngle360(ascendant + moonPos + (8 * 30) - saturnPos), chart),
            createSaham(Saham.KARYASIDDHI, normalizeAngle360(ascendant + saturnPos - sunPos + moonPos), chart)
        )
    }

    private fun createSaham(saham: Saham, longitude: Double, chart: SolarReturnChart): SahamResult {
        val normalizedLong = normalizeAngle360(longitude)
        val sign = ZodiacSign.fromLongitude(normalizedLong)
        val degreeInSign = normalizedLong % 30.0
        val house = calculateWholeSignHouse(normalizedLong, chart.ascendant)
        val lord = sign.ruler

        return SahamResult(
            saham = saham,
            longitude = normalizedLong,
            sign = sign,
            degreeInSign = degreeInSign,
            house = house,
            lord = lord,
            interpretation = generateSahamInterpretation(saham, sign, house, lord)
        )
    }

    private fun calculateTajikaAspects(chart: SolarReturnChart): List<TajikaAspectResult> {
        val aspects = mutableListOf<TajikaAspectResult>()
        val planets = Planet.MAIN_PLANETS

        for (i in planets.indices) {
            for (j in (i + 1) until planets.size) {
                val planet1 = planets[i]
                val planet2 = planets[j]

                val pos1 = chart.planetPositions.find { it.planet == planet1 } ?: continue
                val pos2 = chart.planetPositions.find { it.planet == planet2 } ?: continue

                val aspectResult = analyzeTajikaAspect(pos1, pos2, chart)
                if (aspectResult != null) {
                    aspects.add(aspectResult)
                }
            }
        }

        return aspects.sortedBy { it.orb }
    }

    private fun analyzeTajikaAspect(
        pos1: PlanetPosition,
        pos2: PlanetPosition,
        chart: SolarReturnChart
    ): TajikaAspectResult? {
        val long1 = pos1.longitude
        val long2 = pos2.longitude
        var diff = abs(long1 - long2)
        if (diff > 180) diff = 360 - diff

        val aspectInfo = when {
            diff <= CONJUNCTION_ORB -> "Conjunction" to diff
            abs(diff - 60) <= SEXTILE_ORB -> "Sextile" to abs(diff - 60)
            abs(diff - 90) <= SQUARE_ORB -> "Square" to abs(diff - 90)
            abs(diff - 120) <= TRINE_ORB -> "Trine" to abs(diff - 120)
            abs(diff - 180) <= OPPOSITION_ORB -> "Opposition" to abs(diff - 180)
            else -> return null
        }

        val (aspectType, orb) = aspectInfo

        val speed1 = pos1.speed
        val speed2 = pos2.speed

        val fasterPlanet = if (abs(speed1) > abs(speed2)) pos1 else pos2
        val slowerPlanet = if (fasterPlanet == pos1) pos2 else pos1

        val fasterLong = fasterPlanet.longitude
        val slowerLong = slowerPlanet.longitude
        val fasterSpeed = fasterPlanet.speed
        val slowerSpeed = slowerPlanet.speed

        var angularDistance = slowerLong - fasterLong
        if (angularDistance < -180) angularDistance += 360
        if (angularDistance > 180) angularDistance -= 360

        val closingSpeed = fasterSpeed - slowerSpeed
        val isApplying = (angularDistance > 0 && closingSpeed > 0) || (angularDistance < 0 && closingSpeed < 0)

        val daysToExact = if (closingSpeed != 0.0) {
            abs(orb / closingSpeed)
        } else {
            365.0
        }

        val tajikaAspect = determineTajikaAspectType(pos1, pos2, isApplying, orb, chart)

        val interpretation = generateTajikaInterpretation(
            pos1.planet, pos2.planet, tajikaAspect, aspectType, isApplying
        )

        return TajikaAspectResult(
            planet1 = pos1.planet,
            planet2 = pos2.planet,
            aspect = tajikaAspect,
            aspectType = aspectType,
            orb = orb,
            isApplying = isApplying,
            daysToExact = daysToExact,
            interpretation = interpretation
        )
    }

    private fun determineTajikaAspectType(
        pos1: PlanetPosition,
        pos2: PlanetPosition,
        isApplying: Boolean,
        orb: Double,
        chart: SolarReturnChart
    ): TajikaAspect {
        if (pos1.isRetrograde && pos2.isRetrograde) {
            return TajikaAspect.KHALASARA
        }

        if (pos1.isRetrograde || pos2.isRetrograde) {
            return TajikaAspect.RADDA
        }

        if (!isApplying) {
            return TajikaAspect.EASARAPHA
        }

        val hasReception = checkMutualReception(pos1, pos2)
        if (hasReception) {
            return TajikaAspect.KAMBOOLA
        }

        val hasMaleficIntervention = checkMaleficIntervention(pos1, pos2, chart)
        if (hasMaleficIntervention) {
            return TajikaAspect.DUKPHALI
        }

        return TajikaAspect.ITHASALA
    }

    private fun checkMutualReception(pos1: PlanetPosition, pos2: PlanetPosition): Boolean {
        val sign1Lord = pos1.sign.ruler
        val sign2Lord = pos2.sign.ruler
        return (sign1Lord == pos2.planet && sign2Lord == pos1.planet)
    }

    private fun checkMaleficIntervention(
        pos1: PlanetPosition,
        pos2: PlanetPosition,
        chart: SolarReturnChart
    ): Boolean {
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)
        val minLong = minOf(pos1.longitude, pos2.longitude)
        val maxLong = maxOf(pos1.longitude, pos2.longitude)

        for (malefic in malefics) {
            if (malefic == pos1.planet || malefic == pos2.planet) continue
            val maleficPos = chart.planetPositions.find { it.planet == malefic } ?: continue
            val maleficLong = maleficPos.longitude

            if (maxLong - minLong <= 180) {
                if (maleficLong in minLong..maxLong) return true
            } else {
                if (maleficLong >= maxLong || maleficLong <= minLong) return true
            }
        }
        return false
    }

    private fun calculateMuddaDasha(
        solarReturnTime: LocalDateTime,
        chart: SolarReturnChart
    ): List<MuddaDashaPeriod> {
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: return emptyList()

        val moonLongitude = moonPos.longitude
        val (nakshatra, pada) = Nakshatra.fromLongitude(moonLongitude)
        val nakshatraLord = nakshatra.ruler

        val nakshatraStartDegree = nakshatra.startDegree
        val progressInNakshatra = moonLongitude - nakshatraStartDegree
        val nakshatraSpan = 13.333333333
        val progressRatio = progressInNakshatra / nakshatraSpan

        val lordYears = VIMSHOTTARI_YEARS[nakshatraLord] ?: 10
        val totalDashaYears = 120.0
        val yearDays = 365.25

        val lordTotalDays = (lordYears / totalDashaYears) * yearDays
        val elapsedDays = progressRatio * lordTotalDays
        val balanceDays = lordTotalDays - elapsedDays

        val lordIndex = VIMSHOTTARI_ORDER.indexOf(nakshatraLord)
        val orderedPlanets = (0 until 9).map { VIMSHOTTARI_ORDER[(lordIndex + it) % 9] }

        val periods = mutableListOf<MuddaDashaPeriod>()
        var currentDateTime = solarReturnTime
        var isFirstPeriod = true

        for (planet in orderedPlanets) {
            val planetYears = VIMSHOTTARI_YEARS[planet] ?: 10
            val fullDays = (planetYears / totalDashaYears) * yearDays

            val actualDays = if (isFirstPeriod) {
                val firstPlanetDays = if (planet == nakshatraLord) balanceDays else fullDays
                firstPlanetDays
            } else {
                fullDays
            }

            val endDateTime = currentDateTime.plusSeconds((actualDays * 86400).roundToLong())

            periods.add(
                MuddaDashaPeriod(
                    planet = planet,
                    startDate = currentDateTime,
                    endDate = endDateTime,
                    durationDays = actualDays,
                    isFirstPeriod = isFirstPeriod && planet == nakshatraLord
                )
            )

            currentDateTime = endDateTime

            if (isFirstPeriod && planet == nakshatraLord) {
                isFirstPeriod = false
            } else if (isFirstPeriod) {
                isFirstPeriod = false
            }
        }

        return periods
    }

    private fun generateHousePredictions(
        chart: SolarReturnChart,
        muntha: MunthaInfo,
        yearLord: Planet,
        natalChart: VedicChart
    ): List<HousePrediction> {
        val ascSign = ZodiacSign.fromLongitude(chart.ascendant)

        return (1..12).map { house ->
            val houseSignIndex = (ascSign.number - 1 + house - 1) % 12
            val houseSign = ZodiacSign.entries[houseSignIndex]
            val houseLord = houseSign.ruler
            val cuspDegree = ((house - 1) * 30.0 + chart.ascendant) % 360.0

            val lordPosition = chart.planetPositions.find { it.planet == houseLord }
            val lordHouse = lordPosition?.house ?: 1
            val lordSign = lordPosition?.sign ?: houseSign
            val lordIsRetrograde = lordPosition?.isRetrograde ?: false

            val planetsInHouse = chart.planetPositions
                .filter { it.house == house }
                .map { it.planet }

            val aspectingPlanets = findAspectingPlanets(house, chart)
            val strengthScore = calculateHouseStrengthScore(
                house, houseLord, planetsInHouse, aspectingPlanets.map { it.first }, chart, muntha
            )

            val strength = when {
                strengthScore >= 70 -> "Strong"
                strengthScore >= 50 -> "Moderate"
                strengthScore >= 30 -> "Weak"
                else -> "Challenged"
            }

            val prediction = generateHousePredictionText(
                house, houseSign, houseLord, lordHouse, lordIsRetrograde,
                planetsInHouse, aspectingPlanets, muntha, yearLord, strength
            )

            HousePrediction(
                house = house,
                signOnCusp = houseSign,
                cuspDegree = cuspDegree,
                houseLord = houseLord,
                lordPosition = lordHouse,
                lordSign = lordSign,
                lordIsRetrograde = lordIsRetrograde,
                planetsInHouse = planetsInHouse,
                aspectingPlanets = aspectingPlanets,
                strength = strength,
                strengthScore = strengthScore,
                prediction = prediction,
                keywords = getHouseKeywords(house)
            )
        }
    }

    private fun findAspectingPlanets(house: Int, chart: SolarReturnChart): List<Pair<Planet, String>> {
        val aspects = mutableListOf<Pair<Planet, String>>()
        val houseMidpoint = ((house - 1) * 30.0 + 15.0 + chart.ascendant) % 360.0

        for (position in chart.planetPositions) {
            if (position.house == house) continue

            val planetDegree = position.longitude
            var diff = abs(houseMidpoint - planetDegree)
            if (diff > 180) diff = 360 - diff

            val aspectType = when {
                abs(diff - 180) <= 12 -> "Opposition"
                abs(diff - 120) <= 10 || abs(diff - 240) <= 10 -> "Trine"
                abs(diff - 90) <= 10 || abs(diff - 270) <= 10 -> "Square"
                abs(diff - 60) <= 8 || abs(diff - 300) <= 8 -> "Sextile"
                else -> null
            }

            if (position.planet == Planet.MARS && position.house != house) {
                val houseDiff = (house - position.house + 12) % 12
                if (houseDiff == 4 || houseDiff == 7 || houseDiff == 8) {
                    aspects.add(position.planet to "Special(4,7,8)")
                }
            }

            if (position.planet == Planet.JUPITER && position.house != house) {
                val houseDiff = (house - position.house + 12) % 12
                if (houseDiff == 5 || houseDiff == 7 || houseDiff == 9) {
                    aspects.add(position.planet to "Special(5,7,9)")
                }
            }

            if (position.planet == Planet.SATURN && position.house != house) {
                val houseDiff = (house - position.house + 12) % 12
                if (houseDiff == 3 || houseDiff == 7 || houseDiff == 10) {
                    aspects.add(position.planet to "Special(3,7,10)")
                }
            }

            if (aspectType != null) {
                aspects.add(position.planet to aspectType)
            }
        }

        return aspects.distinctBy { it.first }
    }

    private fun calculateHouseStrengthScore(
        house: Int,
        houseLord: Planet,
        planetsInHouse: List<Planet>,
        aspectingPlanets: List<Planet>,
        chart: SolarReturnChart,
        muntha: MunthaInfo
    ): Int {
        var score = 50

        val lordPos = chart.planetPositions.find { it.planet == houseLord }
        if (lordPos != null) {
            when (lordPos.house) {
                1, 4, 5, 7, 9, 10 -> score += 15
                2, 11 -> score += 10
                3 -> score += 5
                6, 8, 12 -> score -= 15
            }

            if (lordPos.sign.ruler == houseLord) score += 10
            if (isExalted(houseLord, lordPos.sign)) score += 15
            if (isDebilitated(houseLord, lordPos.sign)) score -= 20

            if (lordPos.isRetrograde) score -= 5
        }

        val benefics = listOf(Planet.JUPITER, Planet.VENUS)
        val naturalBenefic = listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU, Planet.SUN)

        score += planetsInHouse.count { it in benefics } * 12
        score += planetsInHouse.count { it == Planet.MERCURY || it == Planet.MOON } * 6
        score -= planetsInHouse.count { it in malefics } * 8

        score += aspectingPlanets.count { it in naturalBenefic } * 5
        score -= aspectingPlanets.count { it in malefics } * 4

        if (muntha.house == house) {
            score += if (muntha.lordStrength >= 50) 10 else -5
        }

        return score.coerceIn(0, 100)
    }

    private fun generateHousePredictionText(
        house: Int,
        sign: ZodiacSign,
        lord: Planet,
        lordHouse: Int,
        lordRetrograde: Boolean,
        planets: List<Planet>,
        aspects: List<Pair<Planet, String>>,
        muntha: MunthaInfo,
        yearLord: Planet,
        strength: String
    ): String {
        val meaning = getHouseMeaning(house)
        val lordName = lord.displayName

        return buildString {
            append("$meaning matters are governed by $lordName placed in House $lordHouse. ")

            if (lordRetrograde) {
                append("Lord's retrograde status suggests revisiting past issues in this area. ")
            }

            if (planets.isNotEmpty()) {
                val benefics = planets.filter { it in listOf(Planet.JUPITER, Planet.VENUS) }
                val malefics = planets.filter { it in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) }

                if (benefics.isNotEmpty()) {
                    append("${benefics.joinToString { it.displayName }} here brings blessings and growth. ")
                }
                if (malefics.isNotEmpty()) {
                    append("${malefics.joinToString { it.displayName }} here requires patience and effort. ")
                }
            }

            if (muntha.house == house) {
                append("Muntha's presence makes this a focal area for the year. ")
            }

            if (yearLord == lord) {
                append("As Year Lord rules this house, expect significant developments here. ")
            }

            when (strength) {
                "Strong" -> append("Overall favorable indications for this domain.")
                "Moderate" -> append("Mixed results expected with effort yielding results.")
                "Weak" -> append("Challenges may arise requiring careful handling.")
                "Challenged" -> append("Difficult period for these matters; remedial measures recommended.")
            }
        }
    }

    private fun identifyMajorThemes(
        chart: SolarReturnChart,
        muntha: MunthaInfo,
        yearLord: Planet,
        housePredictions: List<HousePrediction>,
        triPataki: TriPatakiChakra
    ): List<String> {
        val themes = mutableListOf<String>()

        themes.add("Focus on ${getHouseMeaning(muntha.house)} - Muntha in ${muntha.sign.displayName}")

        val yearLordHouse = chart.planetPositions.find { it.planet == yearLord }?.house ?: 1
        themes.add("${yearLord.displayName} as Year Lord emphasizes ${getHouseMeaning(yearLordHouse)}")

        themes.add("Tri-Pataki: ${triPataki.dominantGroup} signs dominate - ${triPataki.interpretation.take(80)}...")

        housePredictions
            .filter { it.strength == "Strong" }
            .sortedByDescending { it.strengthScore }
            .take(2)
            .forEach { themes.add("Favorable: ${getHouseMeaning(it.house)} (Score: ${it.strengthScore})") }

        housePredictions
            .filter { it.strength == "Challenged" }
            .sortedBy { it.strengthScore }
            .take(1)
            .forEach { themes.add("Attention needed: ${getHouseMeaning(it.house)}") }

        val jupiterHouse = chart.planetPositions.find { it.planet == Planet.JUPITER }?.house
        if (jupiterHouse != null) {
            themes.add("Jupiter's blessings in ${getHouseMeaning(jupiterHouse)}")
        }

        val saturnHouse = chart.planetPositions.find { it.planet == Planet.SATURN }?.house
        if (saturnHouse != null) {
            themes.add("Saturn's lessons in ${getHouseMeaning(saturnHouse)}")
        }

        return themes
    }

    private fun determineFavorableMonths(
        muddaDasha: List<MuddaDashaPeriod>,
        chart: SolarReturnChart
    ): List<Int> {
        val beneficPlanets = listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)

        val favorableMonths = mutableSetOf<Int>()

        for (period in muddaDasha) {
            if (period.planet in beneficPlanets) {
                val position = chart.planetPositions.find { it.planet == period.planet }
                val isWellPlaced = position?.let {
                    it.house in listOf(1, 2, 4, 5, 7, 9, 10, 11) && !isDebilitated(it.planet, it.sign)
                } ?: true

                if (isWellPlaced) {
                    var current = period.startDate
                    while (current.isBefore(period.endDate)) {
                        favorableMonths.add(current.monthValue)
                        current = current.plusMonths(1)
                    }
                }
            }
        }

        return favorableMonths.sorted()
    }

    private fun determineChallengingMonths(
        muddaDasha: List<MuddaDashaPeriod>,
        chart: SolarReturnChart
    ): List<Int> {
        val maleficPlanets = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)

        val challengingMonths = mutableSetOf<Int>()

        for (period in muddaDasha) {
            if (period.planet in maleficPlanets) {
                val position = chart.planetPositions.find { it.planet == period.planet }
                val isIllPlaced = position?.let {
                    it.house in listOf(6, 8, 12) || isDebilitated(it.planet, it.sign)
                } ?: true

                if (isIllPlaced) {
                    var current = period.startDate
                    while (current.isBefore(period.endDate)) {
                        challengingMonths.add(current.monthValue)
                        current = current.plusMonths(1)
                    }
                }
            }
        }

        return challengingMonths.sorted()
    }

    private fun generateOverallPrediction(
        yearLord: Planet,
        muntha: MunthaInfo,
        housePredictions: List<HousePrediction>,
        aspects: List<TajikaAspectResult>,
        age: Int,         yearLordBala: PanchaVargiyaBala,
        triPatakiChakra: TriPatakiChakra
    ): String {
        return buildString {
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("                    YEAR OVERVIEW - Age $age")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()

            appendLine("YEAR LORD: ${yearLord.displayName}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("${yearLord.displayName} governs this year with ${yearLordBala.classification}.")
            appendLine("Pancha-Vargiya Bala: ${yearLordBala.totalBala}/20")

            val yearLordNature = when (yearLord) {
                Planet.JUPITER -> "wisdom, expansion, fortune, and spiritual growth"
                Planet.VENUS -> "love, beauty, comforts, creativity, and relationships"
                Planet.MERCURY -> "communication, intellect, commerce, and learning"
                Planet.MOON -> "emotions, mind, mother, nurturing, and public dealings"
                Planet.SUN -> "authority, father, government, health, and recognition"
                Planet.MARS -> "energy, courage, property, siblings, and competition"
                Planet.SATURN -> "discipline, hard work, delays, karma, and perseverance"
                else -> "transformative experiences"
            }
            appendLine("This year emphasizes themes of $yearLordNature.")
            appendLine()

            appendLine("MUNTHA ANALYSIS:")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Muntha in ${muntha.sign.displayName} (House ${muntha.house})")
            appendLine("Muntha Lord ${muntha.lord.displayName} in House ${muntha.lordHouse}")
            appendLine()
            appendLine(muntha.interpretation)
            appendLine()

            appendLine("TRI-PATAKI INFLUENCE:")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Dominant: ${triPatakiChakra.dominantGroup}")
            appendLine(triPatakiChakra.interpretation)
            appendLine()

            val strongHouses = housePredictions.filter { it.strength == "Strong" }.sortedByDescending { it.strengthScore }
            val moderateHouses = housePredictions.filter { it.strength == "Moderate" }
            val weakHouses = housePredictions.filter { it.strength == "Weak" }
            val challengedHouses = housePredictions.filter { it.strength == "Challenged" }.sortedBy { it.strengthScore }

            appendLine("LIFE AREAS ANALYSIS:")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            if (strongHouses.isNotEmpty()) {
                appendLine()
                appendLine("★ FAVORABLE AREAS (Strong):")
                strongHouses.forEach { house ->
                    appendLine("  • ${getHouseMeaning(house.house)} (House ${house.house})")
                    appendLine("    ${house.signOnCusp.displayName} ruled by ${house.houseLord.displayName}")
                }
            }

            if (moderateHouses.isNotEmpty()) {
                appendLine()
                appendLine("◆ MODERATE AREAS (Balanced):")
                moderateHouses.take(4).forEach { house ->
                    appendLine("  • ${getHouseMeaning(house.house)} (House ${house.house})")
                }
            }

            if (challengedHouses.isNotEmpty()) {
                appendLine()
                appendLine("⚠ AREAS REQUIRING ATTENTION (Challenged):")
                challengedHouses.forEach { house ->
                    appendLine("  • ${getHouseMeaning(house.house)} (House ${house.house})")
                    appendLine("    Remedial measures and patience recommended")
                }
            }

            appendLine()
            appendLine("TAJIKA YOGAS SUMMARY:")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val ithasalaAspects = aspects.filter { it.aspect == TajikaAspect.ITHASALA }
            val easaraphaAspects = aspects.filter { it.aspect == TajikaAspect.EASARAPHA }
            val kamboolaAspects = aspects.filter { it.aspect == TajikaAspect.KAMBOOLA }

            if (ithasalaAspects.isNotEmpty()) {
                appendLine("Ithasala Yogas (New Developments): ${ithasalaAspects.size}")
                ithasalaAspects.take(3).forEach {
                    appendLine("  • ${it.planet1.displayName}-${it.planet2.displayName}: ${it.interpretation.take(60)}...")
                }
            }

            if (kamboolaAspects.isNotEmpty()) {
                appendLine("Kamboola Yogas (Enhanced Results): ${kamboolaAspects.size}")
            }

            if (easaraphaAspects.isNotEmpty()) {
                appendLine("Easarapha Yogas (Completing Matters): ${easaraphaAspects.size}")
            }

            appendLine()
            appendLine("OVERALL ASSESSMENT:")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val overallScore = calculateOverallYearScore(housePredictions, yearLordBala, muntha, aspects)

            val overallAssessment = when {
                overallScore >= 75 -> "Highly favorable year with excellent opportunities for growth and success."
                overallScore >= 60 -> "Generally positive year with good prospects in multiple areas."
                overallScore >= 45 -> "Mixed year with both opportunities and challenges requiring balanced approach."
                overallScore >= 30 -> "Challenging year requiring patience, effort, and strategic planning."
                else -> "Difficult year emphasizing inner growth and karmic lessons. Remedies strongly recommended."
            }

            appendLine(overallAssessment)
            appendLine()

            if (ithasalaAspects.isNotEmpty()) {
                appendLine("This year holds new opportunities and developments, particularly involving ")
                val involvedPlanets = ithasalaAspects.flatMap { listOf(it.planet1, it.planet2) }.distinct().take(3)
                appendLine("${involvedPlanets.joinToString { it.displayName }} energies.")
            } else if (easaraphaAspects.isNotEmpty()) {
                appendLine("This year emphasizes completing and consolidating past efforts.")
            }

            appendLine()
            appendLine("Key Recommendations:")
            when (yearLord) {
                Planet.SUN -> appendLine("• Strengthen Sun through early morning practices, father's blessings, and leadership roles")
                Planet.MOON -> appendLine("• Strengthen Moon through meditation, mother's blessings, and emotional balance")
                Planet.MARS -> appendLine("• Channel Mars energy constructively through physical activity and disciplined action")
                Planet.MERCURY -> appendLine("• Enhance Mercury through learning, communication skills, and intellectual pursuits")
                Planet.JUPITER -> appendLine("• Honor Jupiter through spiritual practices, teaching, and ethical conduct")
                Planet.VENUS -> appendLine("• Harmonize Venus through arts, relationships, and appreciation of beauty")
                Planet.SATURN -> appendLine("• Appease Saturn through service, discipline, and accepting responsibilities")
                else -> appendLine("• Follow appropriate planetary remedies for ${yearLord.displayName}")
            }

            if (challengedHouses.isNotEmpty()) {
                appendLine("• Pay special attention to ${challengedHouses.joinToString { getHouseMeaning(it.house) }} matters")
            }
        }
    }

    private fun calculateOverallYearScore(
        housePredictions: List<HousePrediction>,
        yearLordBala: PanchaVargiyaBala,
        muntha: MunthaInfo,
        aspects: List<TajikaAspectResult>
    ): Int {
        var score = 50

        val avgHouseScore = housePredictions.map { it.strengthScore }.average()
        score += ((avgHouseScore - 50) * 0.4).toInt()

        score += (yearLordBala.totalBala - 10) * 2

        if (muntha.house in listOf(1, 2, 4, 5, 7, 9, 10, 11)) score += 5
        if (muntha.house in listOf(6, 8, 12)) score -= 5
        if (muntha.lordStrength >= 60) score += 5

        val ithasalaCount = aspects.count { it.aspect == TajikaAspect.ITHASALA }
        val kamboolaCount = aspects.count { it.aspect == TajikaAspect.KAMBOOLA }
        val dukphaliCount = aspects.count { it.aspect == TajikaAspect.DUKPHALI }

        score += ithasalaCount * 2
        score += kamboolaCount * 3
        score -= dukphaliCount * 2

        return score.coerceIn(0, 100)
    }

    private fun evaluatePlanetStrengthDescription(planet: Planet, chart: SolarReturnChart): String {
        val score = evaluatePlanetStrengthScore(planet, chart)
        return when {
            score >= 80 -> "Excellent"
            score >= 65 -> "Strong"
            score >= 50 -> "Moderate"
            score >= 35 -> "Weak"
            else -> "Debilitated"
        }
    }

    private fun evaluatePlanetStrengthScore(planet: Planet, chart: SolarReturnChart): Double {
        var score = 50.0
        val position = chart.planetPositions.find { it.planet == planet } ?: return score

        if (position.sign.ruler == planet) score += 15.0

        if (isExalted(planet, position.sign)) score += 20.0

        if (isDebilitated(planet, position.sign)) score -= 25.0

        when (position.house) {
            1, 4, 7, 10 -> score += 15.0
            5, 9 -> score += 12.0
            2, 11 -> score += 8.0
            3 -> score += 3.0
            6, 8, 12 -> score -= 12.0
        }

        if (position.isRetrograde) {
            score -= 5.0
        }

        val friendlyPlanets = when (planet) {
            Planet.SUN -> listOf(Planet.MOON, Planet.MARS, Planet.JUPITER)
            Planet.MOON -> listOf(Planet.SUN, Planet.MERCURY)
            Planet.MARS -> listOf(Planet.SUN, Planet.MOON, Planet.JUPITER)
            Planet.MERCURY -> listOf(Planet.SUN, Planet.VENUS)
            Planet.JUPITER -> listOf(Planet.SUN, Planet.MOON, Planet.MARS)
            Planet.VENUS -> listOf(Planet.MERCURY, Planet.SATURN)
            Planet.SATURN -> listOf(Planet.MERCURY, Planet.VENUS)
            else -> emptyList()
        }

        for (friendPos in chart.planetPositions) {
            if (friendPos.planet in friendlyPlanets && friendPos.house == position.house) {
                score += 5.0
            }
        }

        return score.coerceIn(0.0, 100.0)
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            Planet.RAHU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            Planet.KETU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            else -> false
        }
    }

    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LIBRA
            Planet.MOON -> sign == ZodiacSign.SCORPIO
            Planet.MARS -> sign == ZodiacSign.CANCER
            Planet.MERCURY -> sign == ZodiacSign.PISCES
            Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
            Planet.VENUS -> sign == ZodiacSign.VIRGO
            Planet.SATURN -> sign == ZodiacSign.ARIES
            Planet.RAHU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            Planet.KETU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            else -> false
        }
    }

    private fun generateMunthaInterpretation(
        sign: ZodiacSign,
        house: Int,
        lord: Planet,
        lordHouse: Int
    ): String {
        val houseInterpretation = when (house) {
            1 -> "Personal growth, health improvements, and enhanced self-confidence are highlighted. New beginnings in personal matters."
            2 -> "Focus on finances, family harmony, and accumulation of wealth. Good for savings and family gatherings."
            3 -> "Communication, courage, and short journeys emphasized. Sibling relationships gain importance."
            4 -> "Domestic happiness, property matters, and mother's well-being are priorities. Possible vehicle or property acquisition."
            5 -> "Children, creativity, romance, and speculative gains favored. Good for education and intellectual pursuits."
            6 -> "Health consciousness required. Victory over enemies possible but avoid unnecessary conflicts. Service and work emphasized."
            7 -> "Partnerships, marriage proposals, and public dealings prominent. Business collaborations favored."
            8 -> "Transformation, inheritance matters, and hidden knowledge surface. Health of spouse needs attention."
            9 -> "Fortune smiles. Long journeys, higher education, father's blessings, and spiritual advancement indicated."
            10 -> "Career advancement, public recognition, and professional success highlighted. Authority and status improve."
            11 -> "Gains from multiple sources, social circle expands, elder sibling relations improve. Wishes get fulfilled."
            12 -> "Expenses increase but for good causes. Foreign connections, spiritual retreats, and liberation themes prominent."
            else -> "Mixed influences throughout the year."
        }

        val lordInfluence = when {
            lordHouse in listOf(1, 5, 9) -> "The Muntha lord in a trine strengthens beneficial results."
            lordHouse in listOf(4, 7, 10) -> "The Muntha lord in a kendra provides stability and success."
            lordHouse in listOf(2, 11) -> "The Muntha lord in wealth houses supports financial growth."
            lordHouse in listOf(6, 8, 12) -> "The Muntha lord in dusthana requires caution and remedial measures."
            else -> "The Muntha lord's placement gives moderate support."
        }

        return "$houseInterpretation $lordInfluence"
    }

    private fun generateSahamInterpretation(
        saham: Saham,
        sign: ZodiacSign,
        house: Int,
        lord: Planet
    ): String {
        val signQuality = when (sign.element) {
            "Fire" -> "dynamic and action-oriented"
            "Earth" -> "practical and material"
            "Air" -> "intellectual and communicative"
            "Water" -> "emotional and intuitive"
            else -> "mixed"
        }

        val houseInfluence = when {
            house in listOf(1, 5, 9) -> "Favorable placement in trine supports this saham's significations."
            house in listOf(4, 7, 10) -> "Angular placement gives prominence to this saham."
            house in listOf(2, 11) -> "Placement supports material manifestation."
            house in listOf(6, 8, 12) -> "Challenging placement may create obstacles."
            else -> "Moderate influence on this saham."
        }

        return "${saham.description}: $signQuality energy through ${sign.displayName} in House $house. $houseInfluence Lord ${lord.displayName} governs outcomes."
    }

    private fun generateTajikaInterpretation(
        planet1: Planet,
        planet2: Planet,
        aspect: TajikaAspect,
        aspectType: String,
        isApplying: Boolean
    ): String {
        val p1Name = planet1.displayName
        val p2Name = planet2.displayName

        val aspectNature = when (aspectType) {
            "Conjunction" -> "united"
            "Trine" -> "harmoniously"
            "Sextile" -> "supportively"
            "Square" -> "with tension"
            "Opposition" -> "in polarity"
            else -> "connected"
        }

        val timing = if (isApplying) "developing and will manifest" else "completing from past influences"

        return when (aspect) {
            TajikaAspect.ITHASALA -> "$p1Name and $p2Name are $aspectNature $timing. Promises fulfillment of their combined significations."
            TajikaAspect.EASARAPHA -> "$p1Name and $p2Name separation indicates matters are $timing. Past efforts now showing results."
            TajikaAspect.KAMBOOLA -> "$p1Name and $p2Name in mutual reception greatly enhance each other. Excellent for their significations."
            TajikaAspect.NAKTA -> "$p1Name and $p2Name connection through intermediary. Results come through third party involvement."
            TajikaAspect.YAMAYA -> "$p1Name and $p2Name connection obstructed. Delays and obstacles in their combined matters."
            TajikaAspect.MANAU -> "$p1Name and $p2Name aspect frustrated. Denial or significant reduction of expected results."
            TajikaAspect.GAIRI_KAMBOOLA -> "$p1Name and $p2Name weak reception. Results diminished but not completely denied."
            TajikaAspect.KHALASARA -> "$p1Name and $p2Name both separating. Dissolution of their combined influence."
            TajikaAspect.RADDA -> "$p1Name and $p2Name retrograde influence. Reconsideration and review of matters."
            TajikaAspect.DUKPHALI -> "$p1Name and $p2Name connection obstructed by malefic. External obstacles create delays."
        }
    }

    private fun calculatePlanetPosition(
        planet: Planet,
        julianDay: Double,
        latitude: Double,
        longitude: Double
    ): PlanetPosition {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val planetId = when (planet) {
            Planet.KETU -> SweConst.SE_MEAN_NODE
            Planet.RAHU -> SweConst.SE_MEAN_NODE
            else -> planet.swissEphId
        }

        swissEph.swe_calc_ut(
            julianDay,
            planetId,
            SEFLG_SIDEREAL or SEFLG_SPEED,
            xx,
            serr
        )

        var planetLongitude = xx[0]

        if (planet == Planet.KETU) {
            planetLongitude = normalizeAngle360(planetLongitude + 180.0)
        }

        planetLongitude = normalizeAngle360(planetLongitude)

        val (nakshatra, pada) = Nakshatra.fromLongitude(planetLongitude)
        val signDegree = planetLongitude % 30.0
        val degree = floor(signDegree)
        val minutesFull = (signDegree - degree) * 60.0
        val minutes = floor(minutesFull)
        val seconds = (minutesFull - minutes) * 60.0

        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swissEph.swe_houses(julianDay, SweConst.SEFLG_SIDEREAL, latitude, longitude, 'W'.code, cusps, ascmc)
        val ascendant = cusps[1]
        val house = calculateWholeSignHouse(planetLongitude, ascendant)

        return PlanetPosition(
            planet = planet,
            longitude = planetLongitude,
            latitude = xx[1],
            distance = xx[2],
            speed = xx[3],
            isRetrograde = xx[3] < 0,
            sign = ZodiacSign.fromLongitude(planetLongitude),
            degree = degree,
            minutes = minutes,
            seconds = seconds,
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = house
        )
    }

    private fun getPlanetLongitude(planetId: Int, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swissEph.swe_calc_ut(
            julianDay,
            planetId,
            SEFLG_SIDEREAL or SEFLG_SPEED,
            xx,
            serr
        )

        return normalizeAngle360(xx[0])
    }

    private fun normalizeAngle360(angle: Double): Double {
        var normalized = angle % 360.0
        if (normalized < 0) normalized += 360.0
        return normalized
    }

    private fun calculateJulianDay(dateTime: LocalDateTime): Double {
        val decimalHours = dateTime.hour +
                (dateTime.minute / 60.0) +
                (dateTime.second / 3600.0) +
                (dateTime.nano / 3600000000000.0)

        val sweDate = SweDate(
            dateTime.year,
            dateTime.monthValue,
            dateTime.dayOfMonth,
            decimalHours,
            SweDate.SE_GREG_CAL
        )
        return sweDate.julDay
    }

    private fun jdToLocalDateTime(jd: Double, timezone: String): LocalDateTime {
        val sweDate = SweDate(jd)

        val year = sweDate.year
        val month = sweDate.month
        val day = sweDate.day
        val hourDecimal = sweDate.hour

        val hour = hourDecimal.toInt()
        val minuteDecimal = (hourDecimal - hour) * 60.0
        val minute = minuteDecimal.toInt()
        val secondDecimal = (minuteDecimal - minute) * 60.0
        val second = secondDecimal.toInt()
        val nano = ((secondDecimal - second) * 1_000_000_000).toLong()

        val utcDateTime = LocalDateTime.of(
            year,
            month.coerceIn(1, 12),
            day.coerceIn(1, 28),
            hour.coerceIn(0, 23),
            minute.coerceIn(0, 59),
            second.coerceIn(0, 59),
            nano.coerceIn(0, 999_999_999).toInt()
        )

        return try {
            val utcZoned = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
            val localZoned = utcZoned.withZoneSameInstant(ZoneId.of(timezone))
            localZoned.toLocalDateTime()
        } catch (e: Exception) {
            utcDateTime
        }
    }

    fun close() {
        swissEph.swe_close()
    }
}
        