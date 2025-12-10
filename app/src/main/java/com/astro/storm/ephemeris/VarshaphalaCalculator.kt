package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.*
import swisseph.SweConst
import swisseph.SwissEph
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

class VarshaphalaCalculator(context: Context) {

    private val swissEph = SwissEph()
    private val ephemerisPath: String

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED
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
            Planet.SUN,
            Planet.MOON,
            Planet.MARS,
            Planet.MERCURY,
            Planet.JUPITER,
            Planet.VENUS,
            Planet.SATURN
        )

        private val HADDA_LORDS = mapOf(
            ZodiacSign.ARIES to listOf(
                Triple(0.0, 6.0, Planet.JUPITER),
                Triple(6.0, 12.0, Planet.VENUS),
                Triple(12.0, 20.0, Planet.MERCURY),
                Triple(20.0, 25.0, Planet.MARS),
                Triple(25.0, 30.0, Planet.SATURN)
            ),
            ZodiacSign.TAURUS to listOf(
                Triple(0.0, 8.0, Planet.VENUS),
                Triple(8.0, 14.0, Planet.MERCURY),
                Triple(14.0, 22.0, Planet.JUPITER),
                Triple(22.0, 27.0, Planet.SATURN),
                Triple(27.0, 30.0, Planet.MARS)
            ),
            ZodiacSign.GEMINI to listOf(
                Triple(0.0, 6.0, Planet.MERCURY),
                Triple(6.0, 12.0, Planet.JUPITER),
                Triple(12.0, 17.0, Planet.VENUS),
                Triple(17.0, 24.0, Planet.MARS),
                Triple(24.0, 30.0, Planet.SATURN)
            ),
            ZodiacSign.CANCER to listOf(
                Triple(0.0, 7.0, Planet.MARS),
                Triple(7.0, 13.0, Planet.VENUS),
                Triple(13.0, 19.0, Planet.MERCURY),
                Triple(19.0, 26.0, Planet.JUPITER),
                Triple(26.0, 30.0, Planet.SATURN)
            ),
            ZodiacSign.LEO to listOf(
                Triple(0.0, 6.0, Planet.JUPITER),
                Triple(6.0, 11.0, Planet.VENUS),
                Triple(11.0, 18.0, Planet.SATURN),
                Triple(18.0, 24.0, Planet.MERCURY),
                Triple(24.0, 30.0, Planet.MARS)
            ),
            ZodiacSign.VIRGO to listOf(
                Triple(0.0, 7.0, Planet.MERCURY),
                Triple(7.0, 17.0, Planet.VENUS),
                Triple(17.0, 21.0, Planet.JUPITER),
                Triple(21.0, 28.0, Planet.MARS),
                Triple(28.0, 30.0, Planet.SATURN)
            ),
            ZodiacSign.LIBRA to listOf(
                Triple(0.0, 6.0, Planet.SATURN),
                Triple(6.0, 14.0, Planet.MERCURY),
                Triple(14.0, 21.0, Planet.JUPITER),
                Triple(21.0, 28.0, Planet.VENUS),
                Triple(28.0, 30.0, Planet.MARS)
            ),
            ZodiacSign.SCORPIO to listOf(
                Triple(0.0, 7.0, Planet.MARS),
                Triple(7.0, 11.0, Planet.VENUS),
                Triple(11.0, 19.0, Planet.MERCURY),
                Triple(19.0, 24.0, Planet.JUPITER),
                Triple(24.0, 30.0, Planet.SATURN)
            ),
            ZodiacSign.SAGITTARIUS to listOf(
                Triple(0.0, 12.0, Planet.JUPITER),
                Triple(12.0, 17.0, Planet.VENUS),
                Triple(17.0, 21.0, Planet.MERCURY),
                Triple(21.0, 26.0, Planet.SATURN),
                Triple(26.0, 30.0, Planet.MARS)
            ),
            ZodiacSign.CAPRICORN to listOf(
                Triple(0.0, 7.0, Planet.MERCURY),
                Triple(7.0, 14.0, Planet.JUPITER),
                Triple(14.0, 22.0, Planet.VENUS),
                Triple(22.0, 26.0, Planet.SATURN),
                Triple(26.0, 30.0, Planet.MARS)
            ),
            ZodiacSign.AQUARIUS to listOf(
                Triple(0.0, 7.0, Planet.MERCURY),
                Triple(7.0, 13.0, Planet.VENUS),
                Triple(13.0, 20.0, Planet.JUPITER),
                Triple(20.0, 25.0, Planet.MARS),
                Triple(25.0, 30.0, Planet.SATURN)
            ),
            ZodiacSign.PISCES to listOf(
                Triple(0.0, 12.0, Planet.VENUS),
                Triple(12.0, 16.0, Planet.JUPITER),
                Triple(16.0, 19.0, Planet.MERCURY),
                Triple(19.0, 28.0, Planet.MARS),
                Triple(28.0, 30.0, Planet.SATURN)
            )
        )

        private val EXALTATION_DEGREES = mapOf(
            Planet.SUN to 10.0,
            Planet.MOON to 33.0,
            Planet.MARS to 298.0,
            Planet.MERCURY to 165.0,
            Planet.JUPITER to 95.0,
            Planet.VENUS to 357.0,
            Planet.SATURN to 200.0
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

        private val OWN_SIGNS = mapOf(
            Planet.SUN to listOf(ZodiacSign.LEO),
            Planet.MOON to listOf(ZodiacSign.CANCER),
            Planet.MARS to listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO),
            Planet.MERCURY to listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO),
            Planet.JUPITER to listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES),
            Planet.VENUS to listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA),
            Planet.SATURN to listOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS)
        )

        private val FRIENDSHIPS = mapOf(
            Planet.SUN to listOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
            Planet.MOON to listOf(Planet.SUN, Planet.MERCURY),
            Planet.MARS to listOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
            Planet.MERCURY to listOf(Planet.SUN, Planet.VENUS),
            Planet.JUPITER to listOf(Planet.SUN, Planet.MOON, Planet.MARS),
            Planet.VENUS to listOf(Planet.MERCURY, Planet.SATURN),
            Planet.SATURN to listOf(Planet.MERCURY, Planet.VENUS)
        )

        private val NEUTRALS = mapOf(
            Planet.SUN to listOf(Planet.MERCURY),
            Planet.MOON to listOf(Planet.MARS, Planet.JUPITER, Planet.VENUS, Planet.SATURN),
            Planet.MARS to listOf(Planet.MERCURY, Planet.VENUS, Planet.SATURN),
            Planet.MERCURY to listOf(Planet.MARS, Planet.JUPITER, Planet.SATURN),
            Planet.JUPITER to listOf(Planet.MERCURY, Planet.SATURN),
            Planet.VENUS to listOf(Planet.MARS, Planet.JUPITER),
            Planet.SATURN to listOf(Planet.MARS, Planet.JUPITER)
        )

        private val MUDDA_DASHA_PLANETS = listOf(
            Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
            Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.RAHU, Planet.KETU
        )

        private val MUDDA_DASHA_DAYS = mapOf(
            Planet.SUN to 110,
            Planet.MOON to 60,
            Planet.MARS to 32,
            Planet.MERCURY to 40,
            Planet.JUPITER to 48,
            Planet.VENUS to 56,
            Planet.SATURN to 4,
            Planet.RAHU to 5,
            Planet.KETU to 5
        )

        private val STANDARD_ZODIAC_SIGNS = listOf(
            ZodiacSign.ARIES, ZodiacSign.TAURUS, ZodiacSign.GEMINI, ZodiacSign.CANCER,
            ZodiacSign.LEO, ZodiacSign.VIRGO, ZodiacSign.LIBRA, ZodiacSign.SCORPIO,
            ZodiacSign.SAGITTARIUS, ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS, ZodiacSign.PISCES
        )
    }

    init {
        ephemerisPath = context.filesDir.absolutePath + "/ephe"
        swissEph.swe_set_ephe_path(ephemerisPath)
        swissEph.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
    }

    enum class TajikaAspectType(
        val displayNameKey: StringKey,
        val descriptionKey: StringKey,
        val isPositive: Boolean
    ) {
        ITHASALA(StringKey.TAJIKA_ITHASALA, StringKey.TAJIKA_ITHASALA_DESC, true),
        EASARAPHA(StringKey.TAJIKA_EASARAPHA, StringKey.TAJIKA_EASARAPHA_DESC, false),
        NAKTA(StringKey.TAJIKA_NAKTA, StringKey.TAJIKA_NAKTA_DESC, true),
        YAMAYA(StringKey.TAJIKA_YAMAYA, StringKey.TAJIKA_YAMAYA_DESC, true),
        MANAU(StringKey.TAJIKA_MANAU, StringKey.TAJIKA_MANAU_DESC, false),
        KAMBOOLA(StringKey.TAJIKA_KAMBOOLA, StringKey.TAJIKA_KAMBOOLA_DESC, true),
        GAIRI_KAMBOOLA(StringKey.TAJIKA_GAIRI_KAMBOOLA, StringKey.TAJIKA_GAIRI_KAMBOOLA_DESC, true),
        KHALASARA(StringKey.TAJIKA_KHALASARA, StringKey.TAJIKA_KHALASARA_DESC, false),
        RADDA(StringKey.TAJIKA_RADDA, StringKey.TAJIKA_RADDA_DESC, false),
        DUHPHALI_KUTTHA(StringKey.TAJIKA_DUHPHALI_KUTTHA, StringKey.TAJIKA_DUHPHALI_KUTTHA_DESC, false),
        TAMBIRA(StringKey.TAJIKA_TAMBIRA, StringKey.TAJIKA_TAMBIRA_DESC, true),
        KUTTHA(StringKey.TAJIKA_KUTTHA, StringKey.TAJIKA_KUTTHA_DESC, false),
        DURAPHA(StringKey.TAJIKA_DURAPHA, StringKey.TAJIKA_DURAPHA_DESC, false),
        MUTHASHILA(StringKey.TAJIKA_MUTHASHILA, StringKey.TAJIKA_MUTHASHILA_DESC, true),
        IKKABALA(StringKey.TAJIKA_IKKABALA, StringKey.TAJIKA_IKKABALA_DESC, true);

        fun getDisplayName(language: Language): String = StringResources.get(displayNameKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class AspectStrength(val displayNameKey: StringKey, val weight: Double) {
        VERY_STRONG(StringKey.ASPECT_VERY_STRONG, 1.0),
        STRONG(StringKey.ASPECT_STRONG, 0.8),
        MODERATE(StringKey.ASPECT_MODERATE, 0.6),
        WEAK(StringKey.ASPECT_WEAK, 0.4),
        VERY_WEAK(StringKey.ASPECT_VERY_WEAK, 0.2);

        fun getDisplayName(language: Language): String = StringResources.get(displayNameKey, language)
    }

    enum class SahamType(
        val displayNameKey: StringKey,
        val sanskritNameKey: StringKey,
        val descriptionKey: StringKey
    ) {
        PUNYA(StringKey.SAHAM_PUNYA, StringKey.SAHAM_PUNYA_SANSKRIT, StringKey.SAHAM_PUNYA_DESC),
        VIDYA(StringKey.SAHAM_VIDYA, StringKey.SAHAM_VIDYA_SANSKRIT, StringKey.SAHAM_VIDYA_DESC),
        YASHAS(StringKey.SAHAM_YASHAS, StringKey.SAHAM_YASHAS_SANSKRIT, StringKey.SAHAM_YASHAS_DESC),
        MITRA(StringKey.SAHAM_MITRA, StringKey.SAHAM_MITRA_SANSKRIT, StringKey.SAHAM_MITRA_DESC),
        MAHATMYA(StringKey.SAHAM_MAHATMYA, StringKey.SAHAM_MAHATMYA_SANSKRIT, StringKey.SAHAM_MAHATMYA_DESC),
        ASHA(StringKey.SAHAM_ASHA, StringKey.SAHAM_ASHA_SANSKRIT, StringKey.SAHAM_ASHA_DESC),
        SAMARTHA(StringKey.SAHAM_SAMARTHA, StringKey.SAHAM_SAMARTHA_SANSKRIT, StringKey.SAHAM_SAMARTHA_DESC),
        BHRATRI(StringKey.SAHAM_BHRATRI, StringKey.SAHAM_BHRATRI_SANSKRIT, StringKey.SAHAM_BHRATRI_DESC),
        PITRI(StringKey.SAHAM_PITRI, StringKey.SAHAM_PITRI_SANSKRIT, StringKey.SAHAM_PITRI_DESC),
        MATRI(StringKey.SAHAM_MATRI, StringKey.SAHAM_MATRI_SANSKRIT, StringKey.SAHAM_MATRI_DESC),
        PUTRA(StringKey.SAHAM_PUTRA, StringKey.SAHAM_PUTRA_SANSKRIT, StringKey.SAHAM_PUTRA_DESC),
        VIVAHA(StringKey.SAHAM_VIVAHA, StringKey.SAHAM_VIVAHA_SANSKRIT, StringKey.SAHAM_VIVAHA_DESC),
        KARMA(StringKey.SAHAM_KARMA, StringKey.SAHAM_KARMA_SANSKRIT, StringKey.SAHAM_KARMA_DESC),
        ROGA(StringKey.SAHAM_ROGA, StringKey.SAHAM_ROGA_SANSKRIT, StringKey.SAHAM_ROGA_DESC),
        MRITYU(StringKey.SAHAM_MRITYU, StringKey.SAHAM_MRITYU_SANSKRIT, StringKey.SAHAM_MRITYU_DESC),
        PARADESA(StringKey.SAHAM_PARADESA, StringKey.SAHAM_PARADESA_SANSKRIT, StringKey.SAHAM_PARADESA_DESC),
        DHANA(StringKey.SAHAM_DHANA, StringKey.SAHAM_DHANA_SANSKRIT, StringKey.SAHAM_DHANA_DESC),
        RAJA(StringKey.SAHAM_RAJA, StringKey.SAHAM_RAJA_SANSKRIT, StringKey.SAHAM_RAJA_DESC),
        BANDHANA(StringKey.SAHAM_BANDHANA, StringKey.SAHAM_BANDHANA_SANSKRIT, StringKey.SAHAM_BANDHANA_DESC),
        KARYASIDDHI(StringKey.SAHAM_KARYASIDDHI_TYPE, StringKey.SAHAM_KARYASIDDHI_TYPE_SANSKRIT, StringKey.SAHAM_KARYASIDDHI_TYPE_DESC);

        fun getDisplayName(language: Language): String = StringResources.get(displayNameKey, language)
        fun getSanskritName(language: Language): String = StringResources.get(sanskritNameKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class KeyDateType(val displayNameKey: StringKey) {
        FAVORABLE(StringKey.KEY_DATE_FAVORABLE),
        CHALLENGING(StringKey.KEY_DATE_CHALLENGING),
        IMPORTANT(StringKey.KEY_DATE_IMPORTANT),
        TRANSIT(StringKey.KEY_DATE_TRANSIT);

        fun getDisplayName(language: Language): String = StringResources.get(displayNameKey, language)
    }

    data class SolarReturnChart(
        val year: Int,
        val solarReturnTime: LocalDateTime,
        val solarReturnTimeUtc: LocalDateTime,
        val julianDay: Double,
        val planetPositions: Map<Planet, SolarReturnPlanetPosition>,
        val ascendant: ZodiacSign,
        val ascendantDegree: Double,
        val midheaven: Double,
        val houseCusps: List<Double>,
        val ayanamsa: Double,
        val isDayBirth: Boolean,
        val moonSign: ZodiacSign,
        val moonNakshatra: String
    )

    data class SolarReturnPlanetPosition(
        val longitude: Double,
        val sign: ZodiacSign,
        val house: Int,
        val degree: Double,
        val nakshatra: String,
        val nakshatraPada: Int,
        val isRetrograde: Boolean,
        val speed: Double
    )

    data class MunthaResult(
        val longitude: Double,
        val sign: ZodiacSign,
        val house: Int,
        val degree: Double,
        val lord: Planet,
        val lordHouse: Int,
        val lordStrength: String,
        val interpretation: String,
        val themes: List<String>
    )

    data class SahamResult(
        val type: SahamType,
        val name: String,
        val sanskritName: String,
        val formula: String,
        val longitude: Double,
        val sign: ZodiacSign,
        val house: Int,
        val degree: Double,
        val lord: Planet,
        val lordHouse: Int,
        val lordStrength: String,
        val interpretation: String,
        val isActive: Boolean,
        val activationPeriods: List<String>
    )

    data class TajikaAspectResult(
        val type: TajikaAspectType,
        val planet1: Planet,
        val planet2: Planet,
        val planet1Longitude: Double,
        val planet2Longitude: Double,
        val aspectAngle: Int,
        val orb: Double,
        val isApplying: Boolean,
        val strength: AspectStrength,
        val relatedHouses: List<Int>,
        val effectDescription: String,
        val prediction: String
    )

    data class MuddaDashaPeriod(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val days: Int,
        val subPeriods: List<MuddaAntardasha>,
        val planetStrength: String,
        val houseRuled: List<Int>,
        val prediction: String,
        val keywords: List<String>,
        val isCurrent: Boolean,
        val progressPercent: Float
    )

    data class MuddaAntardasha(
        val planet: Planet,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val days: Int,
        val interpretation: String
    )

    data class PanchaVargiyaBala(
        val planet: Planet,
        val uchcha: Double,
        val hadda: Double,
        val dreshkana: Double,
        val navamsha: Double,
        val dwadashamsha: Double,
        val total: Double,
        val category: String
    )

    data class TriPatakiSector(
        val name: String,
        val signs: List<ZodiacSign>,
        val planets: List<Planet>,
        val influence: String
    )

    data class TriPatakiChakra(
        val risingSign: ZodiacSign,
        val sectors: List<TriPatakiSector>,
        val dominantInfluence: String,
        val interpretation: String
    )

    data class HousePrediction(
        val house: Int,
        val signOnCusp: ZodiacSign,
        val houseLord: Planet,
        val lordPosition: Int,
        val planetsInHouse: List<Planet>,
        val strength: String,
        val keywords: List<String>,
        val prediction: String,
        val rating: Float,
        val specificEvents: List<String>
    )

    data class KeyDate(
        val date: LocalDate,
        val event: String,
        val type: KeyDateType,
        val description: String
    )

    data class VarshaphalaResult(
        val natalChart: VedicChart,
        val year: Int,
        val age: Int,
        val solarReturnChart: SolarReturnChart,
        val yearLord: Planet,
        val yearLordStrength: String,
        val yearLordHouse: Int,
        val yearLordDignity: String,
        val muntha: MunthaResult,
        val panchaVargiyaBala: List<PanchaVargiyaBala>,
        val triPatakiChakra: TriPatakiChakra,
        val sahams: List<SahamResult>,
        val tajikaAspects: List<TajikaAspectResult>,
        val muddaDasha: List<MuddaDashaPeriod>,
        val housePredictions: List<HousePrediction>,
        val majorThemes: List<String>,
        val favorableMonths: List<Int>,
        val challengingMonths: List<Int>,
        val overallPrediction: String,
        val yearRating: Float,
        val keyDates: List<KeyDate>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun toPlainText(language: Language = Language.ENGLISH): String = buildString {
            val reportTitle = StringResources.get(StringKey.VARSHA_REPORT_TITLE, language)
            val yearLordSection = StringResources.get(StringKey.VARSHA_REPORT_SECTION_YEARLORD, language)
            val munthaSection = StringResources.get(StringKey.VARSHA_REPORT_SECTION_MUNTHA, language)
            val themesSection = StringResources.get(StringKey.VARSHA_REPORT_SECTION_THEMES, language)
            val muddaDashaSection = StringResources.get(StringKey.VARSHA_REPORT_SECTION_MUDDA, language)
            val predictionSection = StringResources.get(StringKey.VARSHA_REPORT_SECTION_PREDICTION, language)
            val currentMarker = StringResources.get(StringKey.VARSHA_REPORT_CURRENT_MARKER, language)
            val footer = StringResources.get(StringKey.VARSHA_REPORT_FOOTER, language)

            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("            $reportTitle")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_NAME, language, natalChart.birthData.name))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_YEAR, language, year, age))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_SOLAR_RETURN, language, solarReturnChart.solarReturnTime.toString()))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_YEAR_RATING, language, String.format("%.1f", yearRating)))
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                      $yearLordSection")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_YEARLORD_LINE, language, yearLord.getLocalizedName(language), yearLordStrength))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_POSITION, language, yearLordHouse))
            appendLine(yearLordDignity)
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                       $munthaSection")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_MUNTHA_POSITION, language, String.format("%.2f", muntha.degree), muntha.sign.getLocalizedName(language)))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_MUNTHA_HOUSE, language, muntha.house))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_MUNTHA_LORD, language, muntha.lord.getLocalizedName(language), muntha.lordHouse))
            appendLine(muntha.interpretation)
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                    $themesSection")
            appendLine("─────────────────────────────────────────────────────────")
            majorThemes.forEach { appendLine("• $it") }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                   $muddaDashaSection")
            appendLine("─────────────────────────────────────────────────────────")
            muddaDasha.forEach { period ->
                val marker = if (period.isCurrent) currentMarker else ""
                appendLine(StringResources.get(StringKey.VARSHA_REPORT_DASHA_LINE, language,
                    period.planet.getLocalizedName(language),
                    period.startDate.toString(),
                    period.endDate.toString(),
                    period.days,
                    marker))
            }
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_FAVORABLE_MONTHS, language, favorableMonths.joinToString()))
            appendLine(StringResources.get(StringKey.VARSHA_REPORT_CHALLENGING_MONTHS, language, challengingMonths.joinToString()))
            appendLine("─────────────────────────────────────────────────────────")
            appendLine()
            appendLine("                   $predictionSection")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine(overallPrediction)
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine(footer)
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
            natalChart.birthData.timezone,
            year
        )

        val panchaVargiyaBala = calculateAllPanchaVargiyaBalas(solarReturnChart)
        val muntha = calculateMuntha(natalChart, age, solarReturnChart)
        val yearLord = determineYearLord(solarReturnChart, muntha, natalChart, panchaVargiyaBala)
        val yearLordHouse = solarReturnChart.planetPositions[yearLord]?.house ?: 1
        val yearLordStrength = evaluatePlanetStrengthDescription(yearLord, solarReturnChart)
        val yearLordDignity = getYearLordDignityDescription(yearLord, solarReturnChart)
        val triPatakiChakra = calculateTriPatakiChakra(solarReturnChart)
        val sahams = calculateSahams(solarReturnChart)
        val tajikaAspects = calculateTajikaAspects(solarReturnChart)
        val muddaDasha = calculateMuddaDasha(solarReturnChart, solarReturnTime.toLocalDate())
        val housePredictions = generateHousePredictions(solarReturnChart, muntha, yearLord)
        val majorThemes = identifyMajorThemes(solarReturnChart, muntha, yearLord, housePredictions, triPatakiChakra, tajikaAspects)
        val (favorableMonths, challengingMonths) = calculateMonthlyInfluences(solarReturnChart, solarReturnTime)
        val keyDates = calculateKeyDates(solarReturnChart, solarReturnTime, muddaDasha)
        val overallPrediction = generateOverallPrediction(solarReturnChart, yearLord, muntha, tajikaAspects, housePredictions)
        val yearRating = calculateYearRating(solarReturnChart, yearLord, muntha, tajikaAspects, housePredictions)

        return VarshaphalaResult(
            natalChart = natalChart,
            year = year,
            age = age,
            solarReturnChart = solarReturnChart,
            yearLord = yearLord,
            yearLordStrength = yearLordStrength,
            yearLordHouse = yearLordHouse,
            yearLordDignity = yearLordDignity,
            muntha = muntha,
            panchaVargiyaBala = panchaVargiyaBala,
            triPatakiChakra = triPatakiChakra,
            sahams = sahams,
            tajikaAspects = tajikaAspects,
            muddaDasha = muddaDasha,
            housePredictions = housePredictions,
            majorThemes = majorThemes,
            favorableMonths = favorableMonths,
            challengingMonths = challengingMonths,
            overallPrediction = overallPrediction,
            yearRating = yearRating,
            keyDates = keyDates
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

    private fun getPlanetLongitude(planetId: Int, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swissEph.swe_calc_ut(julianDay, planetId, SEFLG_SIDEREAL or SEFLG_SPEED, xx, serr)
        return normalizeAngle(xx[0])
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
        timezone: String,
        year: Int
    ): SolarReturnChart {
        val zonedDateTime = ZonedDateTime.of(solarReturnTime, ZoneId.of(timezone))
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())
        val ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay)

        val cusps = DoubleArray(14)
        val ascmc = DoubleArray(10)
        swissEph.swe_houses(julianDay, SEFLG_SIDEREAL, latitude, longitude, 'W'.code, cusps, ascmc)

        val houseCusps = (1..12).map { normalizeAngle(cusps[it]) }
        val ascendantDegree = normalizeAngle(cusps[1])
        val ascendant = getZodiacSignFromLongitude(ascendantDegree)
        val midheaven = normalizeAngle(ascmc[1])

        val planetPositions = mutableMapOf<Planet, SolarReturnPlanetPosition>()
        for (planet in Planet.MAIN_PLANETS) {
            val position = calculateSolarReturnPlanetPosition(planet, julianDay, ascendantDegree)
            planetPositions[planet] = position
        }

        val sunPos = planetPositions[Planet.SUN]?.longitude ?: 0.0
        val isDayBirth = isDayChart(sunPos, ascendantDegree)

        val moonLong = planetPositions[Planet.MOON]?.longitude ?: 0.0
        val moonSign = getZodiacSignFromLongitude(moonLong)
        val (moonNakshatra, _) = Nakshatra.fromLongitude(moonLong)

        return SolarReturnChart(
            year = year,
            solarReturnTime = solarReturnTime,
            solarReturnTimeUtc = utcDateTime.toLocalDateTime(),
            julianDay = julianDay,
            planetPositions = planetPositions,
            ascendant = ascendant,
            ascendantDegree = ascendantDegree % 30.0,
            midheaven = midheaven,
            houseCusps = houseCusps,
            ayanamsa = ayanamsa,
            isDayBirth = isDayBirth,
            moonSign = moonSign,
            moonNakshatra = moonNakshatra.displayName
        )
    }

    private fun calculateSolarReturnPlanetPosition(
        planet: Planet,
        julianDay: Double,
        ascendantLongitude: Double
    ): SolarReturnPlanetPosition {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val planetId = when (planet) {
            Planet.KETU -> -1
            else -> planet.swissEphId
        }

        if (planetId >= 0) {
            swissEph.swe_calc_ut(julianDay, planetId, SEFLG_SIDEREAL or SEFLG_SPEED, xx, serr)
        } else {
            swissEph.swe_calc_ut(julianDay, SweConst.SE_MEAN_NODE, SEFLG_SIDEREAL or SEFLG_SPEED, xx, serr)
            xx[0] = normalizeAngle(xx[0] + 180.0)
            xx[3] = -xx[3]
        }

        val longitude = normalizeAngle(xx[0])
        val sign = getZodiacSignFromLongitude(longitude)
        val house = calculateWholeSignHouse(longitude, ascendantLongitude)
        val degree = longitude % 30.0
        val (nakshatra, pada) = Nakshatra.fromLongitude(longitude)
        val isRetrograde = xx[3] < 0
        val speed = xx[3]

        return SolarReturnPlanetPosition(
            longitude = longitude,
            sign = sign,
            house = house,
            degree = degree,
            nakshatra = nakshatra.displayName,
            nakshatraPada = pada,
            isRetrograde = isRetrograde,
            speed = speed
        )
    }

    private fun determineYearLord(
        solarReturnChart: SolarReturnChart,
        muntha: MunthaResult,
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
        val lagnaPati = solarReturnChart.ascendant.ruler
        val munthaPati = muntha.lord
        val natalMoonSign = natalChart.planetPositions.find { it.planet == Planet.MOON }?.sign
            ?: ZodiacSign.ARIES
        val janmaRashiPati = natalMoonSign.ruler

        val candidates = listOf(dinaPati, lagnaPati, munthaPati, janmaRashiPati).distinct()

        val candidatesWithStrength = candidates.map { planet ->
            val bala = allBalas.find { it.planet == planet }?.total ?: 0.0
            val additionalStrength = calculateAdditionalStrength(planet, solarReturnChart)
            planet to (bala + additionalStrength)
        }

        return candidatesWithStrength.maxByOrNull { it.second }?.first ?: dinaPati
    }

    private fun calculateAdditionalStrength(planet: Planet, chart: SolarReturnChart): Double {
        var strength = 0.0
        val position = chart.planetPositions[planet] ?: return 0.0

        when (position.house) {
            1, 4, 7, 10 -> strength += 5.0
            5, 9 -> strength += 3.0
            6, 8, 12 -> strength -= 3.0
        }

        if (position.sign.ruler == planet) strength += 4.0
        if (isExalted(planet, position.sign)) strength += 5.0
        if (isDebilitated(planet, position.sign)) strength -= 5.0
        if (!position.isRetrograde) strength += 1.0

        return strength
    }

    private fun calculateMuntha(
        natalChart: VedicChart,
        age: Int,
        solarReturnChart: SolarReturnChart
    ): MunthaResult {
        val natalAscLongitude = normalizeAngle(natalChart.ascendant)
        val progressedLongitude = normalizeAngle(natalAscLongitude + (age * 30.0))
        val munthaSign = getZodiacSignFromLongitude(progressedLongitude)
        val degreeInSign = progressedLongitude % 30.0
        val ascendantLongitude = getStandardZodiacIndex(solarReturnChart.ascendant) * 30.0 + solarReturnChart.ascendantDegree
        val munthaHouse = calculateWholeSignHouse(progressedLongitude, ascendantLongitude)
        val munthaLord = munthaSign.ruler

        val lordPosition = solarReturnChart.planetPositions[munthaLord]
        val lordHouse = lordPosition?.house ?: 1
        val lordStrength = evaluatePlanetStrengthDescription(munthaLord, solarReturnChart)

        val themes = getMunthaThemes(munthaHouse)
        val interpretation = generateMunthaInterpretation(munthaSign, munthaHouse, munthaLord, lordHouse, lordStrength)

        return MunthaResult(
            longitude = progressedLongitude,
            sign = munthaSign,
            house = munthaHouse,
            degree = degreeInSign,
            lord = munthaLord,
            lordHouse = lordHouse,
            lordStrength = lordStrength,
            interpretation = interpretation,
            themes = themes
        )
    }

    private fun getMunthaThemes(house: Int): List<String> {
        return when (house) {
            1 -> listOf("Personal Growth", "New Beginnings", "Health Focus")
            2 -> listOf("Financial Gains", "Family Matters", "Speech")
            3 -> listOf("Communication", "Short Travels", "Siblings")
            4 -> listOf("Home Affairs", "Property", "Inner Peace")
            5 -> listOf("Creativity", "Romance", "Children")
            6 -> listOf("Service", "Health Issues", "Competition")
            7 -> listOf("Partnerships", "Marriage", "Business")
            8 -> listOf("Transformation", "Research", "Inheritance")
            9 -> listOf("Fortune", "Long Travel", "Higher Learning")
            10 -> listOf("Career Advancement", "Recognition", "Authority")
            11 -> listOf("Gains", "Friends", "Fulfilled Wishes")
            12 -> listOf("Spirituality", "Foreign Lands", "Expenses")
            else -> listOf("General Growth")
        }
    }

    private fun generateMunthaInterpretation(
        sign: ZodiacSign,
        house: Int,
        lord: Planet,
        lordHouse: Int,
        lordStrength: String
    ): String {
        val houseSignificance = getHouseSignificance(house)
        val lordQuality = when (lordStrength) {
            "Exalted", "Strong" -> "excellent"
            "Moderate", "Angular" -> "favorable"
            "Debilitated" -> "challenging but growth-oriented"
            else -> "variable"
        }

        return "Muntha in ${sign.displayName} in the ${house}${getOrdinalSuffix(house)} house focuses the year's energy on $houseSignificance. " +
                "The Muntha lord ${lord.displayName} in house $lordHouse provides $lordQuality support for these matters."
    }

    private fun calculateAllPanchaVargiyaBalas(chart: SolarReturnChart): List<PanchaVargiyaBala> {
        return Planet.MAIN_PLANETS.filter { it != Planet.RAHU && it != Planet.KETU }
            .map { calculatePanchaVargiyaBala(it, chart) }
    }

    private fun calculatePanchaVargiyaBala(planet: Planet, chart: SolarReturnChart): PanchaVargiyaBala {
        val position = chart.planetPositions[planet]
            ?: return PanchaVargiyaBala(planet, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "Unknown")

        val longitude = normalizeAngle(position.longitude)

        val uchcha = calculateUchchaBala(planet, longitude)
        val hadda = calculateHaddaBala(planet, position.sign, position.degree)
        val dreshkana = calculateDrekkanaBala(planet, position.sign, position.degree)
        val navamsha = calculateNavamshaBala(planet, longitude)
        val dwadashamsha = calculateDwadashamshabala(planet, longitude)

        val total = uchcha + hadda + dreshkana + navamsha + dwadashamsha

        val category = when {
            total >= 15 -> "Excellent"
            total >= 12 -> "Good"
            total >= 8 -> "Average"
            total >= 5 -> "Below Average"
            else -> "Weak"
        }

        return PanchaVargiyaBala(
            planet = planet,
            uchcha = uchcha,
            hadda = hadda,
            dreshkana = dreshkana,
            navamsha = navamsha,
            dwadashamsha = dwadashamsha,
            total = total,
            category = category
        )
    }

    private fun calculateUchchaBala(planet: Planet, longitude: Double): Double {
        val exaltationPoint = EXALTATION_DEGREES[planet] ?: return 0.0
        val normalizedLong = normalizeAngle(longitude)
        val diff = abs(normalizeAngle(normalizedLong - exaltationPoint))
        val adjustedDiff = if (diff > 180) 360 - diff else diff
        return ((180 - adjustedDiff) / 180.0 * 5.0).coerceIn(0.0, 5.0)
    }

    private fun calculateHaddaBala(planet: Planet, sign: ZodiacSign, degree: Double): Double {
        val haddaRanges = HADDA_LORDS[sign] ?: return 2.0

        for ((start, end, lord) in haddaRanges) {
            if (degree >= start && degree < end) {
                return when {
                    lord == planet -> 4.0
                    areFriends(planet, lord) -> 3.0
                    areNeutral(planet, lord) -> 2.0
                    else -> 1.0
                }
            }
        }
        return 2.0
    }

        private fun calculateDrekkanaBala(planet: Planet, sign: ZodiacSign, degree: Double): Double {
        val drekkanaNumber = when {
            degree < 10 -> 1
            degree < 20 -> 2
            else -> 3
        }

        val signIndex = getStandardZodiacIndex(sign)
        val drekkanaSignIndex = (signIndex + (drekkanaNumber - 1) * 4) % 12
        val drekkanaSign = STANDARD_ZODIAC_SIGNS[drekkanaSignIndex]
        val drekkanaLord = drekkanaSign.ruler

        return when {
            drekkanaLord == planet -> 4.0
            areFriends(planet, drekkanaLord) -> 3.0
            areNeutral(planet, drekkanaLord) -> 2.0
            else -> 1.0
        }
    }

    private fun calculateNavamshaBala(planet: Planet, longitude: Double): Double {
        val normalizedLong = normalizeAngle(longitude)
        val degreeInSign = normalizedLong % 30.0
        val navamshaIndex = (degreeInSign / 3.333333).toInt().coerceIn(0, 8)
        val signIndex = (normalizedLong / 30.0).toInt().coerceIn(0, 11)

        val startSign = when (signIndex % 4) {
            0 -> 0
            1 -> 9
            2 -> 6
            else -> 3
        }

        val navamshaSignIndex = (startSign + navamshaIndex) % 12
        val navamshaLord = STANDARD_ZODIAC_SIGNS[navamshaSignIndex].ruler

        return when {
            navamshaLord == planet -> 4.0
            areFriends(planet, navamshaLord) -> 3.0
            areNeutral(planet, navamshaLord) -> 2.0
            else -> 1.0
        }
    }

    private fun calculateDwadashamshabala(planet: Planet, longitude: Double): Double {
        val normalizedLong = normalizeAngle(longitude)
        val degreeInSign = normalizedLong % 30.0
        val d12Index = (degreeInSign / 2.5).toInt().coerceIn(0, 11)
        val signIndex = (normalizedLong / 30.0).toInt().coerceIn(0, 11)
        val d12SignIndex = (signIndex + d12Index) % 12
        val d12Lord = STANDARD_ZODIAC_SIGNS[d12SignIndex].ruler

        return when {
            d12Lord == planet -> 3.0
            areFriends(planet, d12Lord) -> 2.5
            areNeutral(planet, d12Lord) -> 1.5
            else -> 1.0
        }
    }

    private fun calculateTriPatakiChakra(chart: SolarReturnChart): TriPatakiChakra {
        val ascIndex = getStandardZodiacIndex(chart.ascendant)

        val dharmaSigns = listOf(
            STANDARD_ZODIAC_SIGNS[ascIndex],
            STANDARD_ZODIAC_SIGNS[(ascIndex + 4) % 12],
            STANDARD_ZODIAC_SIGNS[(ascIndex + 8) % 12]
        )

        val arthaSigns = listOf(
            STANDARD_ZODIAC_SIGNS[(ascIndex + 1) % 12],
            STANDARD_ZODIAC_SIGNS[(ascIndex + 5) % 12],
            STANDARD_ZODIAC_SIGNS[(ascIndex + 9) % 12]
        )

        val kamaSigns = listOf(
            STANDARD_ZODIAC_SIGNS[(ascIndex + 2) % 12],
            STANDARD_ZODIAC_SIGNS[(ascIndex + 6) % 12],
            STANDARD_ZODIAC_SIGNS[(ascIndex + 10) % 12]
        )

        fun getPlanetsInSector(signs: List<ZodiacSign>): List<Planet> {
            return chart.planetPositions.filter { (_, pos) -> pos.sign in signs }.keys.toList()
        }

        val dharmaPlanets = getPlanetsInSector(dharmaSigns)
        val arthaPlanets = getPlanetsInSector(arthaSigns)
        val kamaPlanets = getPlanetsInSector(kamaSigns)

        val sectors = listOf(
            TriPatakiSector(
                name = "Dharma (1, 5, 9)",
                signs = dharmaSigns,
                planets = dharmaPlanets,
                influence = generateSectorInfluence("Dharma", dharmaPlanets)
            ),
            TriPatakiSector(
                name = "Artha (2, 6, 10)",
                signs = arthaSigns,
                planets = arthaPlanets,
                influence = generateSectorInfluence("Artha", arthaPlanets)
            ),
            TriPatakiSector(
                name = "Kama (3, 7, 11)",
                signs = kamaSigns,
                planets = kamaPlanets,
                influence = generateSectorInfluence("Kama", kamaPlanets)
            )
        )

        val dominantSector = sectors.maxByOrNull { it.planets.size }
        val dominantInfluence = when (dominantSector?.name?.take(6)) {
            "Dharma" -> "Spiritual growth and righteous pursuits dominate"
            "Artha" -> "Material prosperity and career emphasis"
            "Kama" -> "Relationships and desires take center stage"
            else -> "Balanced influences across all areas"
        }

        val interpretation = buildTriPatakiInterpretation(sectors)

        return TriPatakiChakra(
            risingSign = chart.ascendant,
            sectors = sectors,
            dominantInfluence = dominantInfluence,
            interpretation = interpretation
        )
    }

    private fun generateSectorInfluence(sectorName: String, planets: List<Planet>): String {
        if (planets.isEmpty()) {
            return "No planets in $sectorName sector - quieter year for these matters."
        }

        val benefics = planets.filter { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY) }
        val malefics = planets.filter { it in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) }

        return when {
            benefics.size > malefics.size -> "Benefic ${benefics.joinToString { it.displayName }} bring favorable influences."
            malefics.size > benefics.size -> "Malefic ${malefics.joinToString { it.displayName }} bring challenges requiring effort."
            else -> "Mixed influences suggest variable results."
        }
    }

    private fun buildTriPatakiInterpretation(sectors: List<TriPatakiSector>): String {
        val interpretations = mutableListOf<String>()

        sectors.forEach { sector ->
            if (sector.planets.isNotEmpty()) {
                val areaName = when {
                    sector.name.startsWith("Dharma") -> "righteousness, fortune, and higher learning"
                    sector.name.startsWith("Artha") -> "wealth, career, and practical achievements"
                    else -> "relationships, desires, and social connections"
                }
                interpretations.add("${sector.planets.size} planet(s) in ${sector.name.take(6)} trikona emphasizes $areaName.")
            }
        }

        return if (interpretations.isNotEmpty()) {
            interpretations.joinToString(" ")
        } else {
            "Balanced distribution of planetary energies across all life sectors."
        }
    }

    private fun calculateSahams(chart: SolarReturnChart): List<SahamResult> {
        val sahams = mutableListOf<SahamResult>()
        val isDayBirth = chart.isDayBirth

        val sunLong = chart.planetPositions[Planet.SUN]?.longitude ?: 0.0
        val moonLong = chart.planetPositions[Planet.MOON]?.longitude ?: 0.0
        val marsLong = chart.planetPositions[Planet.MARS]?.longitude ?: 0.0
        val mercuryLong = chart.planetPositions[Planet.MERCURY]?.longitude ?: 0.0
        val jupiterLong = chart.planetPositions[Planet.JUPITER]?.longitude ?: 0.0
        val venusLong = chart.planetPositions[Planet.VENUS]?.longitude ?: 0.0
        val saturnLong = chart.planetPositions[Planet.SATURN]?.longitude ?: 0.0
        val ascLong = getStandardZodiacIndex(chart.ascendant) * 30.0 + chart.ascendantDegree

        val sahamFormulas = listOf(
            Triple(SahamType.PUNYA, { if (isDayBirth) moonLong + ascLong - sunLong else sunLong + ascLong - moonLong }, "Moon + Asc - Sun"),
            Triple(SahamType.VIDYA, { if (isDayBirth) mercuryLong + ascLong - sunLong else sunLong + ascLong - mercuryLong }, "Mercury + Asc - Sun"),
            Triple(SahamType.YASHAS, { if (isDayBirth) jupiterLong + ascLong - sunLong else sunLong + ascLong - jupiterLong }, "Jupiter + Asc - Sun"),
            Triple(SahamType.MITRA, { if (isDayBirth) moonLong + ascLong - mercuryLong else mercuryLong + ascLong - moonLong }, "Moon + Asc - Mercury"),
            Triple(SahamType.DHANA, { if (isDayBirth) jupiterLong + ascLong - moonLong else moonLong + ascLong - jupiterLong }, "Jupiter + Asc - Moon"),
            Triple(SahamType.KARMA, { if (isDayBirth) saturnLong + ascLong - sunLong else sunLong + ascLong - saturnLong }, "Saturn + Asc - Sun"),
            Triple(SahamType.VIVAHA, { if (isDayBirth) venusLong + ascLong - saturnLong else saturnLong + ascLong - venusLong }, "Venus + Asc - Saturn"),
            Triple(SahamType.PUTRA, { if (isDayBirth) jupiterLong + ascLong - moonLong else moonLong + ascLong - jupiterLong }, "Jupiter + Asc - Moon"),
            Triple(SahamType.PITRI, { if (isDayBirth) saturnLong + ascLong - sunLong else sunLong + ascLong - saturnLong }, "Saturn + Asc - Sun"),
            Triple(SahamType.MATRI, { if (isDayBirth) moonLong + ascLong - venusLong else venusLong + ascLong - moonLong }, "Moon + Asc - Venus"),
            Triple(SahamType.SAMARTHA, { if (isDayBirth) marsLong + ascLong - saturnLong else saturnLong + ascLong - marsLong }, "Mars + Asc - Saturn"),
            Triple(SahamType.ASHA, { if (isDayBirth) saturnLong + ascLong - venusLong else venusLong + ascLong - saturnLong }, "Saturn + Asc - Venus"),
            Triple(SahamType.ROGA, { if (isDayBirth) saturnLong + ascLong - marsLong else marsLong + ascLong - saturnLong }, "Saturn + Asc - Mars"),
            Triple(SahamType.RAJA, { if (isDayBirth) sunLong + ascLong - saturnLong else saturnLong + ascLong - sunLong }, "Sun + Asc - Saturn"),
            Triple(SahamType.MRITYU, { if (isDayBirth) saturnLong + ascLong - moonLong else moonLong + ascLong - saturnLong }, "Saturn + Asc - Moon"),
            Triple(SahamType.BHRATRI, { if (isDayBirth) jupiterLong + ascLong - saturnLong else saturnLong + ascLong - jupiterLong }, "Jupiter + Asc - Saturn"),
            Triple(SahamType.MAHATMYA, { if (isDayBirth) jupiterLong + ascLong - moonLong else moonLong + ascLong - jupiterLong }, "Jupiter + Asc - Moon"),
            Triple(SahamType.KARYASIDDHI, { if (isDayBirth) saturnLong + ascLong - sunLong else sunLong + ascLong - saturnLong }, "Saturn + Asc - Sun")
        )

        for ((type, formula, formulaStr) in sahamFormulas) {
            try {
                val longitude = normalizeAngle(formula())
                val sign = getZodiacSignFromLongitude(longitude)
                val house = calculateWholeSignHouse(longitude, ascLong)
                val degree = longitude % 30.0
                val lord = sign.ruler
                val lordHouse = chart.planetPositions[lord]?.house ?: 1
                val lordStrength = evaluatePlanetStrengthDescription(lord, chart)

                val isActive = isSahamActive(lord, chart, house)
                val interpretation = generateSahamInterpretation(type, sign, house, lord, lordHouse, lordStrength)
                val activationPeriods = getSahamActivationPeriods(lord)

                sahams.add(
                    SahamResult(
                        type = type,
                        name = type.getDisplayName(Language.ENGLISH),
                        sanskritName = type.getSanskritName(Language.ENGLISH),
                        formula = formulaStr,
                        longitude = longitude,
                        sign = sign,
                        house = house,
                        degree = degree,
                        lord = lord,
                        lordHouse = lordHouse,
                        lordStrength = lordStrength,
                        interpretation = interpretation,
                        isActive = isActive,
                        activationPeriods = activationPeriods
                    )
                )
            } catch (e: Exception) {
                // Skip sahams that can't be calculated
            }
        }

        return sahams.sortedByDescending { it.isActive }
    }

    private fun isSahamActive(lord: Planet, chart: SolarReturnChart, house: Int): Boolean {
        val lordPosition = chart.planetPositions[lord] ?: return false
        val lordStrength = evaluatePlanetStrengthDescription(lord, chart)

        val isLordStrong = lordStrength in listOf("Exalted", "Strong", "Angular")
        val isInGoodHouse = house in listOf(1, 2, 4, 5, 7, 9, 10, 11)
        val isLordWellPlaced = lordPosition.house in listOf(1, 4, 5, 7, 9, 10, 11)

        return (isLordStrong && isInGoodHouse) || (isLordWellPlaced && !lordPosition.isRetrograde)
    }

    private fun generateSahamInterpretation(
        type: SahamType,
        sign: ZodiacSign,
        house: Int,
        lord: Planet,
        lordHouse: Int,
        lordStrength: String
    ): String {
        // Interpretation stored in English for data, UI will localize on display
        val lordQuality = when (lordStrength) {
            "Exalted", "Strong" -> "well-placed, promising positive outcomes"
            "Moderate", "Angular" -> "providing reasonable support"
            "Debilitated", "Weak" -> "requiring attention and effort"
            else -> "influencing matters variably"
        }

        return "The ${type.getDisplayName(Language.ENGLISH)} Saham in ${sign.displayName} (House $house) relates to ${type.getDescription(Language.ENGLISH).lowercase()} this year. " +
                "Its lord ${lord.displayName} in House $lordHouse is $lordQuality."
    }

    private fun getSahamActivationPeriods(lord: Planet): List<String> {
        val periods = mutableListOf<String>()
        if (lord in MUDDA_DASHA_PLANETS) {
            periods.add("${lord.displayName} Mudda Dasha")
        }
        return periods
    }

    private fun calculateTajikaAspects(chart: SolarReturnChart): List<TajikaAspectResult> {
        val aspects = mutableListOf<TajikaAspectResult>()
        val planets = listOf(
            Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
            Planet.JUPITER, Planet.VENUS, Planet.SATURN
        )

        val aspectAngles = listOf(0, 60, 90, 120, 180)

        for (i in planets.indices) {
            for (j in (i + 1) until planets.size) {
                val planet1 = planets[i]
                val planet2 = planets[j]

                val pos1 = chart.planetPositions[planet1] ?: continue
                val pos2 = chart.planetPositions[planet2] ?: continue

                val diff = abs(normalizeAngle(pos1.longitude - pos2.longitude))

                for (angle in aspectAngles) {
                    val maxOrb = when (angle) {
                        0 -> CONJUNCTION_ORB
                        60 -> SEXTILE_ORB
                        90 -> SQUARE_ORB
                        120 -> TRINE_ORB
                        180 -> OPPOSITION_ORB
                        else -> 5.0
                    }

                    val actualOrb = abs(diff - angle)
                    val reverseOrb = abs(diff - (360 - angle))
                    val effectiveOrb = min(actualOrb, reverseOrb)

                    if (effectiveOrb <= maxOrb) {
                        val isApplying = determineTajikaApplication(pos1.longitude, pos2.longitude, pos1.speed, pos2.speed)

                        val aspectType = determineTajikaAspectType(
                            planet1, planet2, pos1, pos2, isApplying, effectiveOrb, angle, chart
                        )

                        val strength = calculateAspectStrength(effectiveOrb, maxOrb, angle, isApplying)
                        val relatedHouses = listOf(pos1.house, pos2.house).distinct()
                        val effectDescription = getAspectEffectDescription(aspectType, planet1, planet2)
                        val prediction = generateAspectPrediction(aspectType, planet1, planet2, relatedHouses)

                        aspects.add(
                            TajikaAspectResult(
                                type = aspectType,
                                planet1 = planet1,
                                planet2 = planet2,
                                planet1Longitude = pos1.longitude,
                                planet2Longitude = pos2.longitude,
                                aspectAngle = angle,
                                orb = effectiveOrb,
                                isApplying = isApplying,
                                strength = strength,
                                relatedHouses = relatedHouses,
                                effectDescription = effectDescription,
                                prediction = prediction
                            )
                        )
                    }
                }
            }
        }

        return aspects.sortedByDescending { it.strength.weight }
    }

    private fun determineTajikaApplication(long1: Double, long2: Double, speed1: Double, speed2: Double): Boolean {
        val diff = normalizeAngle(long2 - long1)
        return if (diff < 180) speed1 > speed2 else speed2 > speed1
    }

    private fun determineTajikaAspectType(
        planet1: Planet,
        planet2: Planet,
        pos1: SolarReturnPlanetPosition,
        pos2: SolarReturnPlanetPosition,
        isApplying: Boolean,
        orb: Double,
        angle: Int,
        chart: SolarReturnChart
    ): TajikaAspectType {
        val isAngular1 = pos1.house in listOf(1, 4, 7, 10)
        val isAngular2 = pos2.house in listOf(1, 4, 7, 10)
        val hasReception = checkMutualReception(planet1, planet2, pos1.sign, pos2.sign)

        return when {
            isApplying && angle == 0 && orb < 3 -> {
                if (isAngular1 || isAngular2) TajikaAspectType.KAMBOOLA else TajikaAspectType.ITHASALA
            }
            isApplying && orb < 5 -> {
                if (hasReception) TajikaAspectType.NAKTA else TajikaAspectType.ITHASALA
            }
            !isApplying && orb < 5 -> TajikaAspectType.EASARAPHA
            pos1.isRetrograde || pos2.isRetrograde -> TajikaAspectType.RADDA
            pos1.speed < pos2.speed && isApplying -> TajikaAspectType.MANAU
            isApplying && hasReception -> TajikaAspectType.MUTHASHILA
            angle == 90 || angle == 180 -> TajikaAspectType.DURAPHA
            isAngular1 && isAngular2 && !isApplying -> TajikaAspectType.GAIRI_KAMBOOLA
            else -> if (isApplying) TajikaAspectType.ITHASALA else TajikaAspectType.EASARAPHA
        }
    }

    private fun checkMutualReception(planet1: Planet, planet2: Planet, sign1: ZodiacSign, sign2: ZodiacSign): Boolean {
        return sign1.ruler == planet2 && sign2.ruler == planet1
    }

    private fun calculateAspectStrength(orb: Double, maxOrb: Double, angle: Int, isApplying: Boolean): AspectStrength {
        val orbRatio = orb / maxOrb
        val angleBonus = when (angle) {
            0, 120 -> 0.2
            60 -> 0.1
            90, 180 -> -0.1
            else -> 0.0
        }
        val applyingBonus = if (isApplying) 0.1 else 0.0

        val strength = 1.0 - orbRatio + angleBonus + applyingBonus

        return when {
            strength >= 0.9 -> AspectStrength.VERY_STRONG
            strength >= 0.7 -> AspectStrength.STRONG
            strength >= 0.5 -> AspectStrength.MODERATE
            strength >= 0.3 -> AspectStrength.WEAK
            else -> AspectStrength.VERY_WEAK
        }
    }

    private fun getAspectEffectDescription(type: TajikaAspectType, planet1: Planet, planet2: Planet): String {
        // Effect descriptions stored in English for data, UI will localize on display
        return when (type) {
            TajikaAspectType.ITHASALA -> "${planet1.displayName} applying to ${planet2.displayName} promises fulfillment"
            TajikaAspectType.EASARAPHA -> "Separating aspect suggests matters are concluding"
            TajikaAspectType.KAMBOOLA -> "Powerful angular conjunction promises prominent success"
            TajikaAspectType.RADDA -> "Retrograde motion causes delays or reversals"
            TajikaAspectType.DURAPHA -> "Hard aspect creates challenges that strengthen through difficulty"
            else -> "${type.getDisplayName(Language.ENGLISH)} influences matters with ${if (type.isPositive) "supportive" else "challenging"} energy"
        }
    }

    private fun generateAspectPrediction(type: TajikaAspectType, planet1: Planet, planet2: Planet, houses: List<Int>): String {
        // Predictions stored in English for data, UI will localize on display
        val houseStr = houses.joinToString(" and ") { "House $it" }
        val quality = if (type.isPositive) "favorable" else "requiring attention"

        return "The ${type.getDisplayName(Language.ENGLISH)} between ${planet1.displayName} and ${planet2.displayName} is $quality for matters of $houseStr."
    }

    private fun calculateMuddaDasha(chart: SolarReturnChart, startDate: LocalDate): List<MuddaDashaPeriod> {
        val totalDays = 360
        val today = LocalDate.now()

        val moonLong = chart.planetPositions[Planet.MOON]?.longitude ?: 0.0
        val (nakshatra, _) = Nakshatra.fromLongitude(moonLong)
        val startingLord = nakshatra.ruler

        val startIndex = MUDDA_DASHA_PLANETS.indexOf(startingLord).let { 
            if (it >= 0) it else 0 
        }

        val periods = mutableListOf<MuddaDashaPeriod>()
        var currentDate = startDate

        for (i in MUDDA_DASHA_PLANETS.indices) {
            val planetIndex = (startIndex + i) % MUDDA_DASHA_PLANETS.size
            val planet = MUDDA_DASHA_PLANETS[planetIndex]
            val baseDays = MUDDA_DASHA_DAYS[planet] ?: 30
            val days = (baseDays * totalDays / 360).coerceAtLeast(1)

            val endDate = currentDate.plusDays(days.toLong() - 1)
            val isCurrent = !today.isBefore(currentDate) && !today.isAfter(endDate)

            val progressPercent = if (isCurrent) {
                val daysPassed = ChronoUnit.DAYS.between(currentDate, today).toFloat()
                (daysPassed / days).coerceIn(0f, 1f)
            } else if (today.isAfter(endDate)) {
                1f
            } else {
                0f
            }

            val subPeriods = calculateMuddaAntardasha(planet, currentDate, endDate)
            val planetStrength = evaluatePlanetStrengthDescription(planet, chart)
            val houseRuled = getHousesRuledBy(planet, chart)
            val prediction = generateDashaPrediction(planet, chart, planetStrength)
            val keywords = getDashaKeywords(planet, chart)

            periods.add(
                MuddaDashaPeriod(
                    planet = planet,
                    startDate = currentDate,
                    endDate = endDate,
                    days = days,
                    subPeriods = subPeriods,
                    planetStrength = planetStrength,
                    houseRuled = houseRuled,
                    prediction = prediction,
                    keywords = keywords,
                    isCurrent = isCurrent,
                    progressPercent = progressPercent
                )
            )

            currentDate = endDate.plusDays(1)
        }

        return periods
    }

    private fun calculateMuddaAntardasha(
        mainPlanet: Planet,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MuddaAntardasha> {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt().coerceAtLeast(1)
        val subPeriods = mutableListOf<MuddaAntardasha>()

        val startIndex = MUDDA_DASHA_PLANETS.indexOf(mainPlanet).let {
            if (it >= 0) it else 0
        }
        
        var currentDate = startDate
        val subDays = (totalDays / MUDDA_DASHA_PLANETS.size).coerceAtLeast(1)

        for (i in MUDDA_DASHA_PLANETS.indices) {
            val planetIndex = (startIndex + i) % MUDDA_DASHA_PLANETS.size
            val planet = MUDDA_DASHA_PLANETS[planetIndex]

            val actualSubDays = if (i == MUDDA_DASHA_PLANETS.size - 1) {
                ChronoUnit.DAYS.between(currentDate, endDate).toInt().coerceAtLeast(1)
            } else {
                subDays
            }

            val subEndDate = currentDate.plusDays(actualSubDays.toLong() - 1).let {
                if (it.isAfter(endDate)) endDate else it
            }

            subPeriods.add(
                MuddaAntardasha(
                    planet = planet,
                    startDate = currentDate,
                    endDate = subEndDate,
                    days = actualSubDays,
                    interpretation = "${mainPlanet.displayName}-${planet.displayName} period"
                )
            )

            currentDate = subEndDate.plusDays(1)
            if (currentDate.isAfter(endDate)) break
        }

        return subPeriods
    }

    private fun getHousesRuledBy(planet: Planet, chart: SolarReturnChart): List<Int> {
        val houses = mutableListOf<Int>()
        val ascendantIndex = getStandardZodiacIndex(chart.ascendant)

        for (i in 0..11) {
            val signIndex = (ascendantIndex + i) % 12
            val sign = STANDARD_ZODIAC_SIGNS[signIndex]
            if (sign.ruler == planet) {
                houses.add(i + 1)
            }
        }

        return houses
    }

    private fun generateDashaPrediction(planet: Planet, chart: SolarReturnChart, strength: String): String {
        val position = chart.planetPositions[planet]
        val house = position?.house ?: 1

        val planetNature = when (planet) {
            Planet.SUN -> "vitality, authority, and self-expression"
            Planet.MOON -> "emotions, nurturing, and public connections"
            Planet.MARS -> "energy, initiative, and competitive drive"
            Planet.MERCURY -> "communication, learning, and business"
            Planet.JUPITER -> "wisdom, expansion, and good fortune"
            Planet.VENUS -> "relationships, creativity, and pleasures"
            Planet.SATURN -> "discipline, responsibility, and long-term goals"
            Planet.RAHU -> "ambition, innovation, and unconventional paths"
            Planet.KETU -> "spirituality, detachment, and past karma"
            else -> "general influences"
        }

        val houseArea = getHouseSignificance(house)

        val strengthQuality = when (strength) {
            "Exalted" -> "This period promises exceptional results"
            "Strong" -> "This period is well-supported for success"
            "Debilitated" -> "This period requires extra effort and patience"
            else -> "This period brings mixed but manageable influences"
        }

        return "During this ${planet.displayName} period, focus shifts to $planetNature, particularly affecting $houseArea. $strengthQuality."
    }

    private fun getDashaKeywords(planet: Planet, chart: SolarReturnChart): List<String> {
        val position = chart.planetPositions[planet]
        val house = position?.house ?: 1

        val planetKeywords = when (planet) {
            Planet.SUN -> listOf("Leadership", "Vitality", "Father")
            Planet.MOON -> listOf("Emotions", "Mother", "Public")
            Planet.MARS -> listOf("Action", "Energy", "Courage")
            Planet.MERCURY -> listOf("Communication", "Learning", "Business")
            Planet.JUPITER -> listOf("Wisdom", "Growth", "Fortune")
            Planet.VENUS -> listOf("Love", "Art", "Comfort")
            Planet.SATURN -> listOf("Discipline", "Karma", "Delays")
            Planet.RAHU -> listOf("Ambition", "Innovation", "Foreign")
            Planet.KETU -> listOf("Spirituality", "Detachment", "Past")
            else -> listOf("General")
        }

        val houseKeywords = when (house) {
            1 -> listOf("Self", "Body")
            2 -> listOf("Wealth", "Speech")
            3 -> listOf("Siblings", "Courage")
            4 -> listOf("Home", "Peace")
            5 -> listOf("Children", "Romance")
            6 -> listOf("Health", "Service")
            7 -> listOf("Marriage", "Business")
            8 -> listOf("Transformation", "Research")
            9 -> listOf("Luck", "Travel")
            10 -> listOf("Career", "Status")
            11 -> listOf("Gains", "Friends")
            12 -> listOf("Spirituality", "Losses")
            else -> listOf()
        }

        return (planetKeywords + houseKeywords).take(5)
    }

    private fun generateHousePredictions(
        chart: SolarReturnChart,
        muntha: MunthaResult,
        yearLord: Planet
    ): List<HousePrediction> {
        val predictions = mutableListOf<HousePrediction>()
        val ascIndex = getStandardZodiacIndex(chart.ascendant)

        for (house in 1..12) {
            val signIndex = (ascIndex + house - 1) % 12
            val sign = STANDARD_ZODIAC_SIGNS[signIndex]
            val houseLord = sign.ruler
            val lordPosition = chart.planetPositions[houseLord]?.house ?: 1

            val planetsInHouse = chart.planetPositions.filter { (_, pos) -> pos.house == house }.keys.toList()

            val strength = calculateHouseStrength(house, houseLord, lordPosition, planetsInHouse, chart, muntha, yearLord)
            val keywords = getHouseKeywords(house)
            val prediction = generateHousePrediction(house, sign, houseLord, lordPosition, planetsInHouse, chart, muntha, yearLord)
            val rating = calculateHouseRating(house, houseLord, lordPosition, planetsInHouse, chart, muntha, yearLord)
            val specificEvents = generateSpecificEvents(house, houseLord, lordPosition, planetsInHouse, chart)

            predictions.add(
                HousePrediction(
                    house = house,
                    signOnCusp = sign,
                    houseLord = houseLord,
                    lordPosition = lordPosition,
                    planetsInHouse = planetsInHouse,
                    strength = strength,
                    keywords = keywords,
                    prediction = prediction,
                    rating = rating,
                    specificEvents = specificEvents
                )
            )
        }

        return predictions
    }

    private fun calculateHouseStrength(
        house: Int,
        lord: Planet,
        lordPosition: Int,
        planetsInHouse: List<Planet>,
        chart: SolarReturnChart,
        muntha: MunthaResult,
        yearLord: Planet
    ): String {
        var score = 0

        val beneficPositions = listOf(1, 2, 4, 5, 7, 9, 10, 11)
        if (lordPosition in beneficPositions) score += 2

        val lordStrength = evaluatePlanetStrengthDescription(lord, chart)
        when (lordStrength) {
            "Exalted" -> score += 3
            "Strong" -> score += 2
            "Angular" -> score += 1
            "Debilitated" -> score -= 2
        }

        val benefics = listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON)
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU)

        planetsInHouse.forEach { planet ->
            if (planet in benefics) score += 1
            if (planet in malefics) score -= 1
        }

        if (muntha.house == house) score += 2
        if (yearLord == lord) score += 1

        return when {
            score >= 5 -> "Excellent"
            score >= 3 -> "Strong"
            score >= 1 -> "Moderate"
            score >= -1 -> "Weak"
            else -> "Challenged"
        }
    }

    private fun getHouseKeywords(house: Int): List<String> {
        return when (house) {
            1 -> listOf("Self", "Personality", "Health", "Appearance", "New Beginnings")
            2 -> listOf("Wealth", "Family", "Speech", "Values", "Food")
            3 -> listOf("Siblings", "Courage", "Communication", "Short Travel", "Skills")
            4 -> listOf("Home", "Mother", "Property", "Vehicles", "Inner Peace")
            5 -> listOf("Children", "Intelligence", "Romance", "Creativity", "Investments")
            6 -> listOf("Enemies", "Health Issues", "Service", "Debts", "Competition")
            7 -> listOf("Marriage", "Partnership", "Business", "Public Dealings", "Contracts")
            8 -> listOf("Longevity", "Transformation", "Research", "Inheritance", "Hidden Matters")
            9 -> listOf("Fortune", "Father", "Religion", "Higher Education", "Long Travel")
            10 -> listOf("Career", "Status", "Authority", "Government", "Fame")
            11 -> listOf("Gains", "Income", "Friends", "Elder Siblings", "Aspirations")
            12 -> listOf("Losses", "Expenses", "Spirituality", "Foreign Lands", "Liberation")
            else -> listOf("General")
        }
    }

    private fun generateHousePrediction(
        house: Int,
        sign: ZodiacSign,
        lord: Planet,
        lordPosition: Int,
        planetsInHouse: List<Planet>,
        chart: SolarReturnChart,
        muntha: MunthaResult,
        yearLord: Planet
    ): String {
        val houseArea = getHouseSignificance(house)
        val lordStrength = evaluatePlanetStrengthDescription(lord, chart)

        val lordAnalysis = buildString {
            append("The lord ${lord.displayName} in house $lordPosition ")
            append(
                when (lordStrength) {
                    "Exalted" -> "is excellently placed for positive outcomes."
                    "Strong" -> "is well-positioned for success."
                    "Moderate" -> "provides moderate support."
                    "Debilitated" -> "faces challenges requiring attention."
                    else -> "influences results variably."
                }
            )
        }

        val planetaryInfluence = if (planetsInHouse.isNotEmpty()) {
            val benefics = planetsInHouse.filter { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON) }
            val malefics = planetsInHouse.filter { it in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) }

            when {
                benefics.isNotEmpty() && malefics.isEmpty() ->
                    " ${benefics.joinToString { it.displayName }} enhance positive outcomes."
                malefics.isNotEmpty() && benefics.isEmpty() ->
                    " ${malefics.joinToString { it.displayName }} may bring challenges."
                benefics.isNotEmpty() && malefics.isNotEmpty() ->
                    " Mixed influences from ${planetsInHouse.joinToString { it.displayName }}."
                else -> ""
            }
        } else {
            " Results depend primarily on the lord's position."
        }

        val specialIndications = buildString {
            if (muntha.house == house) append(" Muntha emphasizes these matters this year.")
            if (yearLord == lord) append(" Year Lord rules this house - significant developments expected.")
        }

        return "House $house in ${sign.displayName} governs $houseArea. $lordAnalysis$planetaryInfluence$specialIndications".trim()
    }

    private fun calculateHouseRating(
        house: Int,
        lord: Planet,
        lordPosition: Int,
        planetsInHouse: List<Planet>,
        chart: SolarReturnChart,
        muntha: MunthaResult,
        yearLord: Planet
    ): Float {
        var rating = 3.0f

        val beneficLordPositions = listOf(1, 2, 4, 5, 7, 9, 10, 11)
        if (lordPosition in beneficLordPositions) rating += 0.5f

        val lordStrength = evaluatePlanetStrengthDescription(lord, chart)
        rating += when (lordStrength) {
            "Exalted" -> 1.0f
            "Strong" -> 0.7f
            "Angular" -> 0.3f
            "Debilitated" -> -0.8f
            else -> 0.0f
        }

        planetsInHouse.forEach { planet ->
            when (planet) {
                Planet.JUPITER -> rating += 0.5f
                Planet.VENUS -> rating += 0.4f
                Planet.MOON -> rating += 0.2f
                Planet.SATURN -> rating -= 0.3f
                Planet.MARS -> rating -= 0.2f
                Planet.RAHU, Planet.KETU -> rating -= 0.2f
                else -> {}
            }
        }

        if (muntha.house == house) rating += 0.5f
        if (yearLord == lord) rating += 0.3f

        return rating.coerceIn(1.0f, 5.0f)
    }

    private fun generateSpecificEvents(
        house: Int,
        lord: Planet,
        lordPosition: Int,
        planetsInHouse: List<Planet>,
        chart: SolarReturnChart
    ): List<String> {
        val events = mutableListOf<String>()
        val lordStrength = evaluatePlanetStrengthDescription(lord, chart)
        val isLordStrong = lordStrength in listOf("Exalted", "Strong")

        when (house) {
            1 -> {
                if (isLordStrong) {
                    events.add("Increased vitality and personal confidence")
                    events.add("Favorable for starting new ventures")
                }
                if (Planet.JUPITER in planetsInHouse) events.add("Spiritual growth and wisdom")
                if (Planet.MARS in planetsInHouse) events.add("Increased energy - watch for accidents")
            }
            2 -> {
                if (isLordStrong) {
                    events.add("Financial gains and wealth accumulation")
                    events.add("Improvement in family relationships")
                }
                if (Planet.VENUS in planetsInHouse) events.add("Acquisition of luxury items")
            }
            5 -> {
                if (isLordStrong) {
                    events.add("Creative success and recognition")
                    events.add("Favorable for children's matters")
                }
                if (Planet.JUPITER in planetsInHouse) events.add("Academic success or childbirth possible")
                if (Planet.VENUS in planetsInHouse) events.add("Romantic happiness")
            }
            7 -> {
                if (isLordStrong) {
                    events.add("Strengthening of partnerships")
                    events.add("Favorable for marriage or business")
                }
                if (Planet.VENUS in planetsInHouse) events.add("Romantic fulfillment")
            }
            10 -> {
                if (isLordStrong) {
                    events.add("Career advancement or promotion")
                    events.add("Recognition from authorities")
                }
                if (Planet.SUN in planetsInHouse) events.add("Government favor or leadership role")
            }
            11 -> {
                if (isLordStrong) {
                    events.add("Fulfillment of desires and wishes")
                    events.add("Gains from multiple sources")
                }
            }
        }

        return events.take(4)
    }

    private fun identifyMajorThemes(
        chart: SolarReturnChart,
        muntha: MunthaResult,
        yearLord: Planet,
        housePredictions: List<HousePrediction>,
        triPataki: TriPatakiChakra,
        tajikaAspects: List<TajikaAspectResult>
    ): List<String> {
        val themes = mutableListOf<String>()

        val yearLordHouse = chart.planetPositions[yearLord]?.house ?: 1
        themes.add("Year Lord ${yearLord.displayName} emphasizes ${getHouseSignificance(yearLordHouse)}")

        themes.add("Muntha in House ${muntha.house} focuses on ${muntha.themes.firstOrNull() ?: "personal growth"}")

        themes.add("Tri-Pataki: ${triPataki.dominantInfluence}")

        housePredictions.filter { it.strength in listOf("Excellent", "Strong") }
            .sortedByDescending { it.rating }
            .take(2)
            .forEach { themes.add("Favorable: ${getHouseSignificance(it.house)} (House ${it.house})") }

        val positiveAspects = tajikaAspects.count { it.type.isPositive }
        val totalAspects = tajikaAspects.size
        if (totalAspects > 0) {
            val aspectQuality = if (positiveAspects > totalAspects / 2) "supportive" else "challenging"
            themes.add("Tajika yogas are predominantly $aspectQuality ($positiveAspects/$totalAspects positive)")
        }

        return themes.take(6)
    }

    private fun calculateMonthlyInfluences(
        chart: SolarReturnChart,
        solarReturnTime: LocalDateTime
    ): Pair<List<Int>, List<Int>> {
        val favorableMonths = mutableListOf<Int>()
        val challengingMonths = mutableListOf<Int>()

        val yearLord = chart.ascendant.ruler
        val yearLordHouse = chart.planetPositions[yearLord]?.house ?: 1

        for (monthOffset in 0..11) {
            val month = ((solarReturnTime.monthValue - 1 + monthOffset) % 12) + 1
            val transitHouse = (yearLordHouse + monthOffset - 1) % 12 + 1

            val isFavorable = transitHouse in listOf(1, 2, 4, 5, 7, 9, 10, 11)

            if (isFavorable && favorableMonths.size < 4) {
                favorableMonths.add(month)
            } else if (!isFavorable && challengingMonths.size < 3) {
                challengingMonths.add(month)
            }
        }

        return Pair(favorableMonths, challengingMonths)
    }

    private fun calculateKeyDates(
        chart: SolarReturnChart,
        solarReturnTime: LocalDateTime,
        muddaDasha: List<MuddaDashaPeriod>
    ): List<KeyDate> {
        val keyDates = mutableListOf<KeyDate>()

        keyDates.add(
            KeyDate(
                date = solarReturnTime.toLocalDate(),
                event = "Solar Return",
                type = KeyDateType.IMPORTANT,
                description = "Beginning of the annual horoscope year"
            )
        )

        muddaDasha.forEach { period ->
            keyDates.add(
                KeyDate(
                    date = period.startDate,
                    event = "${period.planet.displayName} Dasha Begins",
                    type = if (period.planetStrength in listOf("Exalted", "Strong"))
                        KeyDateType.FAVORABLE else KeyDateType.IMPORTANT,
                    description = "Start of ${period.planet.displayName} period (${period.days} days)"
                )
            )
        }

        return keyDates.sortedBy { it.date }.take(15)
    }

    private fun generateOverallPrediction(
        chart: SolarReturnChart,
        yearLord: Planet,
        muntha: MunthaResult,
        tajikaAspects: List<TajikaAspectResult>,
        housePredictions: List<HousePrediction>
    ): String {
        val yearLordStrength = evaluatePlanetStrengthDescription(yearLord, chart)
        val yearLordHouse = chart.planetPositions[yearLord]?.house ?: 1

        val strongHouses = housePredictions.filter { it.strength in listOf("Excellent", "Strong") }
        val weakHouses = housePredictions.filter { it.strength in listOf("Weak", "Challenged") }

        val positiveAspects = tajikaAspects.count { it.type.isPositive }
        val challengingAspects = tajikaAspects.size - positiveAspects

        val overallTone = when {
            yearLordStrength in listOf("Exalted", "Strong") && strongHouses.size >= 6 -> "excellent"
            yearLordStrength in listOf("Exalted", "Strong") && strongHouses.size >= 4 -> "favorable"
            strongHouses.size > weakHouses.size -> "positive"
            weakHouses.size > strongHouses.size -> "challenging but growth-oriented"
            else -> "balanced"
        }

        val yearLordInfluence = when (yearLord) {
            Planet.SUN -> "Year Lord Sun brings focus on leadership, authority, and self-expression."
            Planet.MOON -> "Year Lord Moon emphasizes emotional wellbeing and public connections."
            Planet.MARS -> "Year Lord Mars energizes initiatives and competitive endeavors."
            Planet.MERCURY -> "Year Lord Mercury enhances communication and business activities."
            Planet.JUPITER -> "Year Lord Jupiter bestows wisdom, expansion, and good fortune."
            Planet.VENUS -> "Year Lord Venus brings harmony to relationships and creativity."
            Planet.SATURN -> "Year Lord Saturn teaches discipline and responsibility."
            else -> "The Year Lord influences various aspects with balanced energy."
        }

        val munthaInfluence = "Muntha in House ${muntha.house} (${muntha.sign.displayName}) " +
                "directs attention to ${muntha.themes.firstOrNull()?.lowercase() ?: "personal development"}."

        return buildString {
            append("This Varshaphala year presents an overall $overallTone outlook. ")
            append(yearLordInfluence)
            append(" ")
            append(munthaInfluence)
            append(" The Tajika aspects show $positiveAspects favorable and $challengingAspects challenging configurations. ")
            append("By understanding these influences, the year's potential can be maximized.")
        }
    }

    private fun calculateYearRating(
        chart: SolarReturnChart,
        yearLord: Planet,
        muntha: MunthaResult,
        tajikaAspects: List<TajikaAspectResult>,
        housePredictions: List<HousePrediction>
    ): Float {
        var rating = 3.0f

        val yearLordStrength = evaluatePlanetStrengthDescription(yearLord, chart)
        rating += when (yearLordStrength) {
            "Exalted" -> 0.8f
            "Strong" -> 0.5f
            "Angular" -> 0.3f
            "Debilitated" -> -0.5f
            else -> 0.0f
        }

        rating += when (muntha.lordStrength) {
            "Exalted", "Strong" -> 0.3f
            "Moderate" -> 0.1f
            "Debilitated" -> -0.3f
            else -> 0.0f
        }

        if (muntha.house in listOf(1, 2, 4, 5, 9, 10, 11)) rating += 0.2f

        val positiveAspects = tajikaAspects.count { it.type.isPositive && it.strength.weight >= 0.6 }
        val negativeAspects = tajikaAspects.count { !it.type.isPositive && it.strength.weight >= 0.6 }
        rating += (positiveAspects * 0.1f - negativeAspects * 0.1f).coerceIn(-0.5f, 0.5f)

        val averageHouseRating = housePredictions.map { it.rating }.average().toFloat()
        rating += (averageHouseRating - 3.0f) * 0.3f

        val beneficsAngular = chart.planetPositions.count { (planet, pos) ->
            planet in listOf(Planet.JUPITER, Planet.VENUS) && pos.house in listOf(1, 4, 7, 10)
        }
        rating += beneficsAngular * 0.15f

        return rating.coerceIn(1.0f, 5.0f)
    }

    private fun calculateJulianDay(dateTime: LocalDateTime): Double {
        var y = dateTime.year
        var m = dateTime.monthValue
        val d = dateTime.dayOfMonth + dateTime.hour / 24.0 + dateTime.minute / 1440.0 + dateTime.second / 86400.0

        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a = y / 100
        val b = 2 - a + a / 4

        return (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + d + b - 1524.5
    }

    private fun jdToLocalDateTime(julianDay: Double, timezone: String): LocalDateTime {
        val z = floor(julianDay + 0.5).toLong()
        val f = julianDay + 0.5 - z

        val a = if (z < 2299161) z else {
            val alpha = floor((z - 1867216.25) / 36524.25).toLong()
            z + 1 + alpha - alpha / 4
        }

        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toLong()
        val d = floor(365.25 * c).toLong()
        val e = floor((b - d) / 30.6001).toLong()

        val day = (b - d - floor(30.6001 * e)).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715

        val totalHours = f * 24.0
        val hour = totalHours.toInt()
        val totalMinutes = (totalHours - hour) * 60.0
        val minute = totalMinutes.toInt()
        val second = ((totalMinutes - minute) * 60.0).toInt()

        val utcDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day, hour, minute, second)
        val utcZoned = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.of(timezone))

        return localZoned.toLocalDateTime()
    }

    private fun normalizeAngle(angle: Double): Double {
        var result = angle % 360.0
        if (result < 0) result += 360.0
        return result
    }

    private fun getZodiacSignFromLongitude(longitude: Double): ZodiacSign {
        val normalizedLong = normalizeAngle(longitude)
        val signIndex = (normalizedLong / 30.0).toInt().coerceIn(0, 11)
        return STANDARD_ZODIAC_SIGNS[signIndex]
    }

    private fun getStandardZodiacIndex(sign: ZodiacSign): Int {
        val index = STANDARD_ZODIAC_SIGNS.indexOf(sign)
        return if (index >= 0) index else 0
    }

    private fun calculateWholeSignHouse(longitude: Double, ascendantLongitude: Double): Int {
        val normalizedLong = normalizeAngle(longitude)
        val normalizedAsc = normalizeAngle(ascendantLongitude)
        val ascSign = (normalizedAsc / 30.0).toInt().coerceIn(0, 11)
        val planetSign = (normalizedLong / 30.0).toInt().coerceIn(0, 11)
        val house = ((planetSign - ascSign + 12) % 12) + 1
        return house.coerceIn(1, 12)
    }

    private fun isDayChart(sunLongitude: Double, ascendant: Double): Boolean {
        val normalizedSun = normalizeAngle(sunLongitude)
        val normalizedAsc = normalizeAngle(ascendant)
        val sunSign = (normalizedSun / 30.0).toInt().coerceIn(0, 11)
        val ascSign = (normalizedAsc / 30.0).toInt().coerceIn(0, 11)
        val houseOfSun = ((sunSign - ascSign + 12) % 12) + 1
        return houseOfSun in listOf(7, 8, 9, 10, 11, 12, 1)
    }

    private fun getYearLordDignityDescription(planet: Planet, chart: SolarReturnChart): String {
        val position = chart.planetPositions[planet] ?: return "Year Lord's position is undefined."

        val dignityDetails = mutableListOf<String>()

        when {
            isExalted(planet, position.sign) -> dignityDetails.add("exalted in ${position.sign.displayName}")
            OWN_SIGNS[planet]?.contains(position.sign) == true -> dignityDetails.add("in its own sign of ${position.sign.displayName}")
            isDebilitated(planet, position.sign) -> dignityDetails.add("debilitated in ${position.sign.displayName}")
            else -> {
                val signLord = position.sign.ruler
                when {
                    areFriends(planet, signLord) -> dignityDetails.add("in the friendly sign of ${position.sign.displayName}")
                    areNeutral(planet, signLord) -> dignityDetails.add("in the neutral sign of ${position.sign.displayName}")
                    else -> dignityDetails.add("in the enemy sign of ${position.sign.displayName}")
                }
            }
        }

        when (position.house) {
            1, 4, 7, 10 -> dignityDetails.add("in an angular house (Kendra)")
            5, 9 -> dignityDetails.add("in a trine house (Trikona)")
            2, 11 -> dignityDetails.add("in a house of gains")
            3, 6 -> dignityDetails.add("in an upachaya house")
            8, 12 -> dignityDetails.add("in a challenging house (Dusthana)")
        }

        if (position.isRetrograde) {
            dignityDetails.add("and is retrograde")
        }

        return "The Year Lord ${planet.displayName} is ${dignityDetails.joinToString(", ")}. This suggests its influence will be potent and its results will manifest clearly throughout the year."
    }

    private fun evaluatePlanetStrengthDescription(planet: Planet, chart: SolarReturnChart): String {
        val position = chart.planetPositions[planet] ?: return "Unknown"
        val sign = position.sign

        return when {
            isExalted(planet, sign) -> "Exalted"
            isDebilitated(planet, sign) -> "Debilitated"
            OWN_SIGNS[planet]?.contains(sign) == true -> "Strong"
            position.house in listOf(1, 4, 7, 10) -> "Angular"
            position.isRetrograde -> "Retrograde"
            else -> "Moderate"
        }
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        val exaltationDegree = EXALTATION_DEGREES[planet] ?: return false
        val exaltationSign = getZodiacSignFromLongitude(exaltationDegree)
        return sign == exaltationSign
    }

    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return DEBILITATION_SIGNS[planet] == sign
    }

    private fun areFriends(planet1: Planet, planet2: Planet): Boolean {
        return FRIENDSHIPS[planet1]?.contains(planet2) == true
    }

    private fun areNeutral(planet1: Planet, planet2: Planet): Boolean {
        return NEUTRALS[planet1]?.contains(planet2) == true
    }

    private fun getHouseSignificance(house: Int): String {
        return when (house) {
            1 -> "personal development and health"
            2 -> "finances and family"
            3 -> "communication and siblings"
            4 -> "home and property"
            5 -> "creativity and children"
            6 -> "health and service"
            7 -> "partnerships and marriage"
            8 -> "transformation and inheritance"
            9 -> "fortune and higher learning"
            10 -> "career and status"
            11 -> "gains and friendships"
            12 -> "spirituality and foreign matters"
            else -> "various life areas"
        }
    }

    private fun getOrdinalSuffix(n: Int): String {
        return when {
            n in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    fun getHouseMeaning(house: Int): String = getHouseSignificance(house)

    fun getHouseKeywordsExternal(house: Int): List<String> = getHouseKeywords(house)
}
