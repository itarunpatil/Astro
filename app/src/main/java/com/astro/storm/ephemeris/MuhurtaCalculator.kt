package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.Nakshatra
import swisseph.DblObj
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.floor

class MuhurtaCalculator(context: Context) {

    private val swissEph = SwissEph()
    private val ephemerisPath: String

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED

        private const val DEGREES_PER_TITHI = 12.0
        private const val DEGREES_PER_NAKSHATRA = 360.0 / 27.0
        private const val DEGREES_PER_YOGA = 360.0 / 27.0
        private const val DEGREES_PER_KARANA = 6.0

        private const val TOTAL_TITHIS = 30
        private const val TOTAL_YOGAS = 27
        private const val TOTAL_KARANAS = 60

        private const val DAY_MUHURTAS = 15
        private const val DAY_CHOGHADIYAS = 8
        private const val NIGHT_CHOGHADIYAS = 8
        private const val DAY_HORAS = 12
        private const val NIGHT_HORAS = 12

        private val CHALDEAN_ORDER = listOf(
            Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.SUN,
            Planet.VENUS, Planet.MERCURY, Planet.MOON
        )

        private val MOVABLE_KARANAS = listOf(
            "Bava", "Balava", "Kaulava", "Taitila", "Garija", "Vanija", "Vishti"
        )

        private val YOGA_NAMES = listOf(
            "Vishkumbha", "Priti", "Ayushman", "Saubhagya", "Shobhana",
            "Atiganda", "Sukarma", "Dhriti", "Shoola", "Ganda",
            "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
            "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva",
            "Siddha", "Sadhya", "Shubha", "Shukla", "Brahma",
            "Indra", "Vaidhriti"
        )

        private val TITHI_NAMES = listOf(
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
            "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
            "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima"
        )

        private val INAUSPICIOUS_YOGAS = setOf(1, 6, 9, 10, 13, 15, 17, 19, 27)

        private val RIKTA_TITHIS = setOf(4, 9, 14, 19, 24, 29)
        private val NANDA_TITHIS = setOf(1, 6, 11, 16, 21, 26)
        private val BHADRA_TITHIS = setOf(2, 7, 12, 17, 22, 27)
        private val JAYA_TITHIS = setOf(3, 8, 13, 18, 23, 28)
        private val PURNA_TITHIS = setOf(5, 10, 15, 20, 25, 30)
    }

    init {
        ephemerisPath = context.filesDir.absolutePath + "/ephe"
        swissEph.swe_set_ephe_path(ephemerisPath)
        swissEph.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
    }

    enum class ActivityType(
        val displayName: String,
        val description: String,
        val icon: String,
        val favorableNakshatras: List<Nakshatra>,
        val favorableTithis: List<Int>,
        val favorableVaras: List<Vara>,
        val avoidNakshatras: List<Nakshatra>
    ) {
        MARRIAGE(
            "Marriage",
            "Wedding ceremonies and engagements",
            "ring",
            listOf(
                Nakshatra.ROHINI, Nakshatra.MRIGASHIRA, Nakshatra.MAGHA,
                Nakshatra.UTTARA_PHALGUNI, Nakshatra.HASTA, Nakshatra.SWATI,
                Nakshatra.ANURADHA, Nakshatra.MULA, Nakshatra.UTTARA_ASHADHA,
                Nakshatra.UTTARA_BHADRAPADA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.BHARANI, Nakshatra.KRITTIKA, Nakshatra.ARDRA, 
                   Nakshatra.ASHLESHA, Nakshatra.PURVA_PHALGUNI, Nakshatra.VISHAKHA,
                   Nakshatra.JYESHTHA, Nakshatra.PURVA_ASHADHA, Nakshatra.PURVA_BHADRAPADA)
        ),
        TRAVEL(
            "Travel",
            "Journey and trips",
            "flight",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.MRIGASHIRA, Nakshatra.PUNARVASU,
                Nakshatra.PUSHYA, Nakshatra.HASTA, Nakshatra.ANURADHA,
                Nakshatra.SHRAVANA, Nakshatra.DHANISHTHA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.ARDRA, Nakshatra.ASHLESHA, Nakshatra.JYESHTHA, 
                   Nakshatra.MULA, Nakshatra.BHARANI)
        ),
        BUSINESS(
            "Business",
            "New ventures, contracts, deals",
            "business",
            listOf(
                Nakshatra.ROHINI, Nakshatra.PUSHYA, Nakshatra.HASTA,
                Nakshatra.CHITRA, Nakshatra.SWATI, Nakshatra.ANURADHA,
                Nakshatra.SHRAVANA, Nakshatra.DHANISHTHA, Nakshatra.REVATI,
                Nakshatra.UTTARA_PHALGUNI, Nakshatra.UTTARA_ASHADHA, Nakshatra.UTTARA_BHADRAPADA
            ),
            listOf(1, 2, 3, 5, 7, 10, 11, 13),
            listOf(Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.BHARANI, Nakshatra.ASHLESHA, Nakshatra.MULA, 
                   Nakshatra.JYESHTHA, Nakshatra.ARDRA)
        ),
        PROPERTY(
            "Property",
            "Buying/selling property, house entry",
            "home",
            listOf(
                Nakshatra.ROHINI, Nakshatra.MRIGASHIRA, Nakshatra.UTTARA_PHALGUNI,
                Nakshatra.HASTA, Nakshatra.CHITRA, Nakshatra.SWATI,
                Nakshatra.ANURADHA, Nakshatra.UTTARA_ASHADHA, Nakshatra.SHRAVANA,
                Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA,
                Nakshatra.UTTARA_BHADRAPADA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.ARDRA, Nakshatra.ASHLESHA, Nakshatra.JYESHTHA, 
                   Nakshatra.MULA, Nakshatra.BHARANI, Nakshatra.KRITTIKA)
        ),
        EDUCATION(
            "Education",
            "Starting studies, examinations, Vidyarambha",
            "school",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.ROHINI, Nakshatra.MRIGASHIRA,
                Nakshatra.PUNARVASU, Nakshatra.PUSHYA, Nakshatra.HASTA,
                Nakshatra.CHITRA, Nakshatra.SWATI, Nakshatra.SHRAVANA, 
                Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12),
            listOf(Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY, Vara.MONDAY),
            listOf(Nakshatra.KRITTIKA, Nakshatra.ARDRA, Nakshatra.ASHLESHA,
                   Nakshatra.BHARANI, Nakshatra.MULA)
        ),
        MEDICAL(
            "Medical",
            "Surgery, treatments, health procedures",
            "medical",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.ROHINI, Nakshatra.MRIGASHIRA,
                Nakshatra.PUNARVASU, Nakshatra.PUSHYA, Nakshatra.UTTARA_PHALGUNI,
                Nakshatra.HASTA, Nakshatra.CHITRA, Nakshatra.SHRAVANA, 
                Nakshatra.DHANISHTHA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 6, 7, 10, 11, 12),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.BHARANI, Nakshatra.KRITTIKA, Nakshatra.ARDRA, 
                   Nakshatra.ASHLESHA, Nakshatra.JYESHTHA, Nakshatra.MULA)
        ),
        VEHICLE(
            "Vehicle",
            "Purchasing or first drive of vehicle",
            "car",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.ROHINI, Nakshatra.PUSHYA,
                Nakshatra.HASTA, Nakshatra.SWATI, Nakshatra.ANURADHA,
                Nakshatra.SHRAVANA, Nakshatra.DHANISHTHA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.BHARANI, Nakshatra.MULA, Nakshatra.VISHAKHA,
                   Nakshatra.ARDRA, Nakshatra.ASHLESHA)
        ),
        SPIRITUAL(
            "Spiritual",
            "Religious ceremonies, puja, initiation, Upanayana",
            "temple",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.PUNARVASU, Nakshatra.PUSHYA,
                Nakshatra.HASTA, Nakshatra.SWATI, Nakshatra.ANURADHA,
                Nakshatra.SHRAVANA, Nakshatra.UTTARA_BHADRAPADA, Nakshatra.REVATI,
                Nakshatra.MRIGASHIRA, Nakshatra.CHITRA
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13, 15),
            listOf(Vara.MONDAY, Vara.THURSDAY, Vara.FRIDAY, Vara.SUNDAY),
            listOf(Nakshatra.KRITTIKA, Nakshatra.ARDRA, Nakshatra.ASHLESHA,
                   Nakshatra.BHARANI, Nakshatra.MULA, Nakshatra.JYESHTHA)
        ),
        GRIHA_PRAVESHA(
            "Griha Pravesha",
            "House warming ceremony",
            "home_work",
            listOf(
                Nakshatra.ROHINI, Nakshatra.MRIGASHIRA, Nakshatra.UTTARA_PHALGUNI,
                Nakshatra.HASTA, Nakshatra.CHITRA, Nakshatra.SWATI,
                Nakshatra.ANURADHA, Nakshatra.UTTARA_ASHADHA, Nakshatra.SHRAVANA,
                Nakshatra.DHANISHTHA, Nakshatra.UTTARA_BHADRAPADA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.ARDRA, Nakshatra.ASHLESHA, Nakshatra.JYESHTHA, 
                   Nakshatra.MULA, Nakshatra.BHARANI, Nakshatra.KRITTIKA,
                   Nakshatra.PURVA_PHALGUNI, Nakshatra.PURVA_ASHADHA, 
                   Nakshatra.PURVA_BHADRAPADA)
        ),
        NAMING_CEREMONY(
            "Naming Ceremony",
            "Namakarana - naming a child",
            "child_care",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.ROHINI, Nakshatra.MRIGASHIRA,
                Nakshatra.PUNARVASU, Nakshatra.PUSHYA, Nakshatra.UTTARA_PHALGUNI,
                Nakshatra.HASTA, Nakshatra.CHITRA, Nakshatra.SWATI,
                Nakshatra.ANURADHA, Nakshatra.SHRAVANA, Nakshatra.DHANISHTHA,
                Nakshatra.SHATABHISHA, Nakshatra.UTTARA_BHADRAPADA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.BHARANI, Nakshatra.KRITTIKA, Nakshatra.ARDRA, 
                   Nakshatra.ASHLESHA, Nakshatra.MULA, Nakshatra.JYESHTHA)
        ),
        GENERAL(
            "General",
            "General auspicious activities",
            "star",
            listOf(
                Nakshatra.ASHWINI, Nakshatra.ROHINI, Nakshatra.MRIGASHIRA,
                Nakshatra.PUNARVASU, Nakshatra.PUSHYA, Nakshatra.UTTARA_PHALGUNI,
                Nakshatra.HASTA, Nakshatra.CHITRA, Nakshatra.SWATI,
                Nakshatra.ANURADHA, Nakshatra.SHRAVANA, Nakshatra.DHANISHTHA,
                Nakshatra.UTTARA_BHADRAPADA, Nakshatra.REVATI
            ),
            listOf(2, 3, 5, 7, 10, 11, 12, 13),
            listOf(Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY),
            listOf(Nakshatra.BHARANI, Nakshatra.KRITTIKA, Nakshatra.ARDRA, 
                   Nakshatra.ASHLESHA, Nakshatra.MULA, Nakshatra.JYESHTHA)
        )
    }

    enum class Vara(val dayNumber: Int, val displayName: String, val lord: Planet) {
        SUNDAY(0, "Ravivara", Planet.SUN),
        MONDAY(1, "Somavara", Planet.MOON),
        TUESDAY(2, "Mangalavara", Planet.MARS),
        WEDNESDAY(3, "Budhavara", Planet.MERCURY),
        THURSDAY(4, "Guruvara", Planet.JUPITER),
        FRIDAY(5, "Shukravara", Planet.VENUS),
        SATURDAY(6, "Shanivara", Planet.SATURN)
    }

    enum class Choghadiya(
        val displayName: String,
        val nature: ChoghadiyaNature,
        val lord: Planet
    ) {
        UDVEG("Udveg", ChoghadiyaNature.INAUSPICIOUS, Planet.SUN),
        CHAR("Char", ChoghadiyaNature.GOOD, Planet.VENUS),
        LABH("Labh", ChoghadiyaNature.VERY_GOOD, Planet.MERCURY),
        AMRIT("Amrit", ChoghadiyaNature.EXCELLENT, Planet.MOON),
        KAAL("Kaal", ChoghadiyaNature.INAUSPICIOUS, Planet.SATURN),
        SHUBH("Shubh", ChoghadiyaNature.VERY_GOOD, Planet.JUPITER),
        ROG("Rog", ChoghadiyaNature.INAUSPICIOUS, Planet.MARS)
    }

    enum class ChoghadiyaNature(val displayName: String, val score: Int) {
        EXCELLENT("Excellent", 4),
        VERY_GOOD("Very Good", 3),
        GOOD("Good", 2),
        NEUTRAL("Neutral", 1),
        INAUSPICIOUS("Inauspicious", 0)
    }

    enum class NakshatraNature(val displayName: String) {
        DHRUVA("Fixed/Dhruva"),
        CHARA("Movable/Chara"),
        TIKSHNA("Sharp/Tikshna"),
        UGRA("Fierce/Ugra"),
        MRIDU("Soft/Mridu"),
        KSHIPRA("Swift/Kshipra"),
        MISHRA("Mixed/Mishra")
    }

    data class Hora(
        val lord: Planet,
        val horaNumber: Int,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val isDay: Boolean,
        val nature: HoraNature
    )

    enum class HoraNature(val displayName: String) {
        BENEFIC("Benefic"),
        MALEFIC("Malefic"),
        NEUTRAL("Neutral")
    }

    data class InauspiciousPeriods(
        val rahukala: TimePeriod,
        val yamaghanta: TimePeriod,
        val gulikaKala: TimePeriod,
        val durmuhurtas: List<TimePeriod>
    )

    data class TimePeriod(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val name: String = ""
    ) {
        fun contains(time: LocalTime): Boolean {
            return if (startTime <= endTime) {
                time >= startTime && time < endTime
            } else {
                time >= startTime || time < endTime
            }
        }
    }

    data class AbhijitMuhurta(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val isActive: Boolean
    )

    data class MuhurtaDetails(
        val dateTime: LocalDateTime,
        val vara: Vara,
        val tithi: TithiInfo,
        val nakshatra: NakshatraInfo,
        val yoga: YogaInfo,
        val karana: KaranaInfo,
        val choghadiya: ChoghadiyaInfo,
        val hora: Hora,
        val inauspiciousPeriods: InauspiciousPeriods,
        val abhijitMuhurta: AbhijitMuhurta,
        val sunrise: LocalTime,
        val sunset: LocalTime,
        val overallScore: Int,
        val suitableActivities: List<ActivityType>,
        val avoidActivities: List<ActivityType>,
        val recommendations: List<String>,
        val specialYogas: List<SpecialYoga>
    ) {
        val isAuspicious: Boolean get() = overallScore >= 60
        val isExcellent: Boolean get() = overallScore >= 80
    }

    data class TithiInfo(
        val number: Int,
        val displayNumber: Int,
        val name: String,
        val paksha: String,
        val lord: Planet,
        val nature: TithiNature,
        val isAuspicious: Boolean
    )

    enum class TithiNature(val displayName: String) {
        NANDA("Nanda - Joy"),
        BHADRA("Bhadra - Auspicious"),
        JAYA("Jaya - Victory"),
        RIKTA("Rikta - Empty"),
        PURNA("Purna - Full")
    }

    data class NakshatraInfo(
        val nakshatra: Nakshatra,
        val pada: Int,
        val lord: Planet,
        val nature: NakshatraNature,
        val gana: NakshatraGana,
        val element: NakshatraElement
    )

    enum class NakshatraGana(val displayName: String) {
        DEVA("Divine"),
        MANUSHYA("Human"),
        RAKSHASA("Demonic")
    }

    enum class NakshatraElement(val displayName: String) {
        VAYU("Air"),
        AGNI("Fire"),
        PRITHVI("Earth"),
        JALA("Water"),
        AKASHA("Ether")
    }

    data class YogaInfo(
        val number: Int,
        val name: String,
        val nature: String,
        val isAuspicious: Boolean
    )

    data class KaranaInfo(
        val number: Int,
        val name: String,
        val type: KaranaType,
        val isAuspicious: Boolean
    )

    enum class KaranaType(val displayName: String) {
        STHIRA("Fixed"),
        CHARA("Movable")
    }

    data class ChoghadiyaInfo(
        val choghadiya: Choghadiya,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val isDay: Boolean
    )

    data class SpecialYoga(
        val name: String,
        val description: String,
        val isAuspicious: Boolean
    )

    data class MuhurtaSearchResult(
        val dateTime: LocalDateTime,
        val score: Int,
        val vara: Vara,
        val nakshatra: Nakshatra,
        val tithi: String,
        val choghadiya: Choghadiya,
        val reasons: List<String>,
        val warnings: List<String>,
        val specialYogas: List<SpecialYoga>
    )

    fun calculateMuhurta(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): MuhurtaDetails {
        val zoneId = ZoneId.of(timezone)
        val zonedDateTime = ZonedDateTime.of(dateTime, zoneId)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())

        val sunLong = getPlanetLongitude(SweConst.SE_SUN, julianDay)
        val moonLong = getPlanetLongitude(SweConst.SE_MOON, julianDay)

        val (sunriseJd, sunsetJd) = calculateSunriseSunsetJD(julianDay, latitude, longitude)
        val sunrise = jdToLocalTime(sunriseJd, zoneId)
        val sunset = jdToLocalTime(sunsetJd, zoneId)

        val vara = calculateVara(dateTime.toLocalDate())
        val tithi = calculateTithi(sunLong, moonLong)
        val nakshatra = calculateNakshatra(moonLong)
        val yoga = calculateYoga(sunLong, moonLong)
        val karana = calculateKarana(sunLong, moonLong)
        val choghadiya = calculateChoghadiya(dateTime.toLocalTime(), vara, sunrise, sunset)
        val hora = calculateHora(dateTime.toLocalTime(), vara, sunrise, sunset)
        val inauspiciousPeriods = calculateInauspiciousPeriods(vara, sunrise, sunset)
        val abhijitMuhurta = calculateAbhijitMuhurta(sunrise, sunset, dateTime.toLocalTime())
        val specialYogas = calculateSpecialYogas(vara, tithi, nakshatra)

        val (score, suitable, avoid, recommendations) = evaluateMuhurta(
            vara, tithi, nakshatra, yoga, karana, choghadiya, hora,
            dateTime.toLocalTime(), inauspiciousPeriods, abhijitMuhurta, specialYogas
        )

        return MuhurtaDetails(
            dateTime = dateTime,
            vara = vara,
            tithi = tithi,
            nakshatra = nakshatra,
            yoga = yoga,
            karana = karana,
            choghadiya = choghadiya,
            hora = hora,
            inauspiciousPeriods = inauspiciousPeriods,
            abhijitMuhurta = abhijitMuhurta,
            sunrise = sunrise,
            sunset = sunset,
            overallScore = score,
            suitableActivities = suitable,
            avoidActivities = avoid,
            recommendations = recommendations,
            specialYogas = specialYogas
        )
    }

    fun findAuspiciousMuhurtas(
        activity: ActivityType,
        startDate: LocalDate,
        endDate: LocalDate,
        latitude: Double,
        longitude: Double,
        timezone: String,
        minScore: Int = 60
    ): List<MuhurtaSearchResult> {
        val results = mutableListOf<MuhurtaSearchResult>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val dateTime = LocalDateTime.of(currentDate, LocalTime.of(6, 0))
            val zoneId = ZoneId.of(timezone)
            val zonedDateTime = ZonedDateTime.of(dateTime, zoneId)
            val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
            val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())
            
            val (sunriseJd, sunsetJd) = calculateSunriseSunsetJD(julianDay, latitude, longitude)
            val sunrise = jdToLocalTime(sunriseJd, zoneId)
            val sunset = jdToLocalTime(sunsetJd, zoneId)

            val timeSlots = generateTimeSlots(sunrise, sunset)

            for (time in timeSlots) {
                val slotDateTime = LocalDateTime.of(currentDate, time)
                val muhurta = calculateMuhurta(slotDateTime, latitude, longitude, timezone)
                val (score, reasons, warnings) = evaluateForActivity(muhurta, activity)

                if (score >= minScore) {
                    results.add(
                        MuhurtaSearchResult(
                            dateTime = slotDateTime,
                            score = score,
                            vara = muhurta.vara,
                            nakshatra = muhurta.nakshatra.nakshatra,
                            tithi = muhurta.tithi.name,
                            choghadiya = muhurta.choghadiya.choghadiya,
                            reasons = reasons,
                            warnings = warnings,
                            specialYogas = muhurta.specialYogas
                        )
                    )
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        return results.sortedByDescending { it.score }.take(20)
    }

    private fun generateTimeSlots(sunrise: LocalTime, sunset: LocalTime): List<LocalTime> {
        val slots = mutableListOf<LocalTime>()
        var current = sunrise
        while (current.isBefore(sunset)) {
            slots.add(current)
            current = current.plusMinutes(30)
        }
        return slots
    }

    fun getDailyChoghadiya(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): Pair<List<ChoghadiyaInfo>, List<ChoghadiyaInfo>> {
        val dateTime = LocalDateTime.of(date, LocalTime.NOON)
        val zoneId = ZoneId.of(timezone)
        val zonedDateTime = ZonedDateTime.of(dateTime, zoneId)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())

        val (sunriseJd, sunsetJd) = calculateSunriseSunsetJD(julianDay, latitude, longitude)
        val sunrise = jdToLocalTime(sunriseJd, zoneId)
        val sunset = jdToLocalTime(sunsetJd, zoneId)
        val vara = calculateVara(date)

        val nextSunriseJd = sunriseJd + 1.0
        val nextSunrise = jdToLocalTime(nextSunriseJd, zoneId)

        val dayChoghadiyas = calculateAllDayChoghadiya(vara, sunrise, sunset)
        val nightChoghadiyas = calculateAllNightChoghadiya(vara, sunset, nextSunrise)

        return Pair(dayChoghadiyas, nightChoghadiyas)
    }

    fun getDailyHoras(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): List<Hora> {
        val dateTime = LocalDateTime.of(date, LocalTime.NOON)
        val zoneId = ZoneId.of(timezone)
        val zonedDateTime = ZonedDateTime.of(dateTime, zoneId)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())

        val (sunriseJd, sunsetJd) = calculateSunriseSunsetJD(julianDay, latitude, longitude)
        val sunrise = jdToLocalTime(sunriseJd, zoneId)
        val sunset = jdToLocalTime(sunsetJd, zoneId)
        val vara = calculateVara(date)

        val nextSunriseJd = sunriseJd + 1.0
        val nextSunrise = jdToLocalTime(nextSunriseJd, zoneId)

        return calculateAllHoras(vara, sunrise, sunset, nextSunrise)
    }

    private fun calculateVara(date: LocalDate): Vara {
        val dayOfWeek = date.dayOfWeek.value % 7
        return Vara.entries.find { it.dayNumber == dayOfWeek } ?: Vara.SUNDAY
    }

    private fun calculateTithi(sunLong: Double, moonLong: Double): TithiInfo {
        val diff = normalizeDegrees(moonLong - sunLong)
        val tithiIndex = floor(diff / DEGREES_PER_TITHI).toInt()
        val tithiNumber = (tithiIndex % TOTAL_TITHIS) + 1

        val paksha = if (tithiNumber <= 15) "Shukla" else "Krishna"
        val displayNumber = if (tithiNumber <= 15) tithiNumber else tithiNumber - 15

        val name = when {
            tithiNumber == 15 -> "Purnima"
            tithiNumber == 30 -> "Amavasya"
            else -> TITHI_NAMES.getOrElse(displayNumber - 1) { "Unknown" }
        }

        val fullName = if (tithiNumber == 15 || tithiNumber == 30) name else "$paksha $name"

        val tithiLords = listOf(
            Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
            Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.RAHU, Planet.KETU
        )
        val lord = tithiLords[(tithiNumber - 1) % 9]

        val nature = when {
            tithiNumber in NANDA_TITHIS -> TithiNature.NANDA
            tithiNumber in BHADRA_TITHIS -> TithiNature.BHADRA
            tithiNumber in JAYA_TITHIS -> TithiNature.JAYA
            tithiNumber in RIKTA_TITHIS -> TithiNature.RIKTA
            tithiNumber in PURNA_TITHIS -> TithiNature.PURNA
            else -> TithiNature.NANDA
        }

        val isAuspicious = nature != TithiNature.RIKTA &&
                tithiNumber != 4 && tithiNumber != 9 && tithiNumber != 14 &&
                tithiNumber != 19 && tithiNumber != 24 && tithiNumber != 29 &&
                tithiNumber != 8 && tithiNumber != 23

        return TithiInfo(
            number = tithiNumber,
            displayNumber = displayNumber,
            name = fullName,
            paksha = paksha,
            lord = lord,
            nature = nature,
            isAuspicious = isAuspicious
        )
    }

    private fun calculateNakshatra(moonLong: Double): NakshatraInfo {
        val (nakshatra, pada) = Nakshatra.fromLongitude(moonLong)

        val nature = getNakshatraNature(nakshatra)
        val gana = getNakshatraGana(nakshatra)
        val element = getNakshatraElement(nakshatra)

        return NakshatraInfo(
            nakshatra = nakshatra,
            pada = pada,
            lord = nakshatra.ruler,
            nature = nature,
            gana = gana,
            element = element
        )
    }

    private fun getNakshatraNature(nakshatra: Nakshatra): NakshatraNature {
        return when (nakshatra) {
            Nakshatra.UTTARA_PHALGUNI, Nakshatra.UTTARA_ASHADHA,
            Nakshatra.UTTARA_BHADRAPADA, Nakshatra.ROHINI -> NakshatraNature.DHRUVA

            Nakshatra.PUNARVASU, Nakshatra.SWATI, Nakshatra.SHRAVANA,
            Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA -> NakshatraNature.CHARA

            Nakshatra.MULA, Nakshatra.ARDRA, Nakshatra.JYESHTHA,
            Nakshatra.ASHLESHA -> NakshatraNature.TIKSHNA

            Nakshatra.PURVA_PHALGUNI, Nakshatra.PURVA_ASHADHA,
            Nakshatra.PURVA_BHADRAPADA, Nakshatra.BHARANI,
            Nakshatra.MAGHA -> NakshatraNature.UGRA

            Nakshatra.MRIGASHIRA, Nakshatra.CHITRA,
            Nakshatra.ANURADHA, Nakshatra.REVATI -> NakshatraNature.MRIDU

            Nakshatra.ASHWINI, Nakshatra.PUSHYA,
            Nakshatra.HASTA -> NakshatraNature.KSHIPRA

            Nakshatra.VISHAKHA, Nakshatra.KRITTIKA -> NakshatraNature.MISHRA
        }
    }

    private fun getNakshatraGana(nakshatra: Nakshatra): NakshatraGana {
        return when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.MRIGASHIRA, Nakshatra.PUNARVASU,
            Nakshatra.PUSHYA, Nakshatra.HASTA, Nakshatra.SWATI,
            Nakshatra.ANURADHA, Nakshatra.SHRAVANA, Nakshatra.REVATI -> NakshatraGana.DEVA

            Nakshatra.BHARANI, Nakshatra.ROHINI, Nakshatra.ARDRA,
            Nakshatra.PURVA_PHALGUNI, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.PURVA_ASHADHA, Nakshatra.UTTARA_ASHADHA,
            Nakshatra.PURVA_BHADRAPADA, Nakshatra.UTTARA_BHADRAPADA -> NakshatraGana.MANUSHYA

            Nakshatra.KRITTIKA, Nakshatra.ASHLESHA, Nakshatra.MAGHA,
            Nakshatra.CHITRA, Nakshatra.VISHAKHA, Nakshatra.JYESHTHA,
            Nakshatra.MULA, Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA -> NakshatraGana.RAKSHASA
        }
    }

    private fun getNakshatraElement(nakshatra: Nakshatra): NakshatraElement {
        return when (nakshatra) {
            Nakshatra.SWATI, Nakshatra.PUNARVASU, Nakshatra.HASTA,
            Nakshatra.ANURADHA, Nakshatra.SHRAVANA -> NakshatraElement.VAYU

            Nakshatra.KRITTIKA, Nakshatra.BHARANI, Nakshatra.PUSHYA,
            Nakshatra.PURVA_PHALGUNI, Nakshatra.VISHAKHA,
            Nakshatra.PURVA_ASHADHA -> NakshatraElement.AGNI

            Nakshatra.ASHWINI, Nakshatra.MRIGASHIRA, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.CHITRA, Nakshatra.UTTARA_ASHADHA,
            Nakshatra.UTTARA_BHADRAPADA -> NakshatraElement.PRITHVI

            Nakshatra.ROHINI, Nakshatra.ARDRA, Nakshatra.ASHLESHA,
            Nakshatra.MAGHA, Nakshatra.JYESHTHA, Nakshatra.MULA,
            Nakshatra.PURVA_BHADRAPADA, Nakshatra.REVATI -> NakshatraElement.JALA

            Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA -> NakshatraElement.AKASHA
        }
    }

    private fun calculateYoga(sunLong: Double, moonLong: Double): YogaInfo {
        val sum = normalizeDegrees(sunLong + moonLong)
        val yogaIndex = floor(sum / DEGREES_PER_YOGA).toInt()
        val yogaNumber = (yogaIndex % TOTAL_YOGAS) + 1

        val name = YOGA_NAMES.getOrElse(yogaNumber - 1) { "Unknown" }
        val isAuspicious = yogaNumber !in INAUSPICIOUS_YOGAS
        val nature = if (isAuspicious) "Auspicious" else "Inauspicious"

        return YogaInfo(
            number = yogaNumber,
            name = name,
            nature = nature,
            isAuspicious = isAuspicious
        )
    }

    private fun calculateKarana(sunLong: Double, moonLong: Double): KaranaInfo {
        val diff = normalizeDegrees(moonLong - sunLong)
        val karanaIndex = floor(diff / DEGREES_PER_KARANA).toInt()
        val karanaNumber = (karanaIndex % TOTAL_KARANAS) + 1

        val (name, type, isAuspicious) = when (karanaNumber) {
            1 -> Triple("Kimstughna", KaranaType.STHIRA, true)
            58 -> Triple("Shakuni", KaranaType.STHIRA, false)
            59 -> Triple("Chatushpada", KaranaType.STHIRA, false)
            60 -> Triple("Nagava", KaranaType.STHIRA, false)
            else -> {
                val movableIndex = (karanaNumber - 2) % 7
                val movableName = MOVABLE_KARANAS[movableIndex]
                val movableAuspicious = movableName != "Vishti"
                Triple(movableName, KaranaType.CHARA, movableAuspicious)
            }
        }

        return KaranaInfo(
            number = karanaNumber,
            name = name,
            type = type,
            isAuspicious = isAuspicious
        )
    }

    private fun calculateChoghadiya(
        time: LocalTime,
        vara: Vara,
        sunrise: LocalTime,
        sunset: LocalTime
    ): ChoghadiyaInfo {
        val isDay = !time.isBefore(sunrise) && time.isBefore(sunset)

        return if (isDay) {
            val dayChoghadiyas = calculateAllDayChoghadiya(vara, sunrise, sunset)
            dayChoghadiyas.find { time >= it.startTime && time < it.endTime }
                ?: dayChoghadiyas.first()
        } else {
            val nightChoghadiyas = calculateAllNightChoghadiya(
                vara,
                sunset,
                sunrise.plusHours(24 - ChronoUnit.HOURS.between(sunrise, sunset))
            )
            nightChoghadiyas.find {
                if (time >= sunset) {
                    time >= it.startTime && time < it.endTime
                } else {
                    it.startTime > sunset || (time >= it.startTime && time < it.endTime)
                }
            } ?: nightChoghadiyas.first()
        }
    }

    private fun calculateAllDayChoghadiya(
        vara: Vara,
        sunrise: LocalTime,
        sunset: LocalTime
    ): List<ChoghadiyaInfo> {
        val dayMinutes = ChronoUnit.MINUTES.between(sunrise, sunset)
        val choghadiyaDuration = dayMinutes / DAY_CHOGHADIYAS

        val daySequences = mapOf(
            Vara.SUNDAY to listOf(Choghadiya.UDVEG, Choghadiya.CHAR, Choghadiya.LABH, Choghadiya.AMRIT, Choghadiya.KAAL, Choghadiya.SHUBH, Choghadiya.ROG, Choghadiya.UDVEG),
            Vara.MONDAY to listOf(Choghadiya.AMRIT, Choghadiya.KAAL, Choghadiya.SHUBH, Choghadiya.ROG, Choghadiya.UDVEG, Choghadiya.CHAR, Choghadiya.LABH, Choghadiya.AMRIT),
            Vara.TUESDAY to listOf(Choghadiya.ROG, Choghadiya.UDVEG, Choghadiya.CHAR, Choghadiya.LABH, Choghadiya.AMRIT, Choghadiya.KAAL, Choghadiya.SHUBH, Choghadiya.ROG),
            Vara.WEDNESDAY to listOf(Choghadiya.LABH, Choghadiya.AMRIT, Choghadiya.KAAL, Choghadiya.SHUBH, Choghadiya.ROG, Choghadiya.UDVEG, Choghadiya.CHAR, Choghadiya.LABH),
            Vara.THURSDAY to listOf(Choghadiya.SHUBH, Choghadiya.ROG, Choghadiya.UDVEG, Choghadiya.CHAR, Choghadiya.LABH, Choghadiya.AMRIT, Choghadiya.KAAL, Choghadiya.SHUBH),
            Vara.FRIDAY to listOf(Choghadiya.CHAR, Choghadiya.LABH, Choghadiya.AMRIT, Choghadiya.KAAL, Choghadiya.SHUBH, Choghadiya.ROG, Choghadiya.UDVEG, Choghadiya.CHAR),
            Vara.SATURDAY to listOf(Choghadiya.KAAL, Choghadiya.SHUBH, Choghadiya.ROG, Choghadiya.UDVEG, Choghadiya.CHAR, Choghadiya.LABH, Choghadiya.AMRIT, Choghadiya.KAAL)
        )

        // Safe fallback - Sunday sequence is guaranteed to exist
        val sequence = daySequences[vara] ?: daySequences.getValue(Vara.SUNDAY)

        return sequence.mapIndexed { index, choghadiya ->
            ChoghadiyaInfo(
                choghadiya = choghadiya,
                startTime = sunrise.plusMinutes(index * choghadiyaDuration),
                endTime = sunrise.plusMinutes((index + 1) * choghadiyaDuration),
                isDay = true
            )
        }
    }

    private fun calculateAllNightChoghadiya(
        vara: Vara,
        sunset: LocalTime,
        nextSunrise: LocalTime
    ): List<ChoghadiyaInfo> {
        val nightMinutes = if (nextSunrise.isAfter(sunset)) {
            ChronoUnit.MINUTES.between(sunset, nextSunrise)
        } else {
            ChronoUnit.MINUTES.between(sunset, LocalTime.MAX) + 
            ChronoUnit.MINUTES.between(LocalTime.MIN, nextSunrise) + 1
        }
        val choghadiyaDuration = nightMinutes / NIGHT_CHOGHADIYAS

        val nightSequences = mapOf(
            Vara.SUNDAY to listOf(Choghadiya.SHUBH, Choghadiya.AMRIT, Choghadiya.CHAR, Choghadiya.ROG, Choghadiya.KAAL, Choghadiya.LABH, Choghadiya.UDVEG, Choghadiya.SHUBH),
            Vara.MONDAY to listOf(Choghadiya.CHAR, Choghadiya.ROG, Choghadiya.KAAL, Choghadiya.LABH, Choghadiya.UDVEG, Choghadiya.SHUBH, Choghadiya.AMRIT, Choghadiya.CHAR),
            Vara.TUESDAY to listOf(Choghadiya.KAAL, Choghadiya.LABH, Choghadiya.UDVEG, Choghadiya.SHUBH, Choghadiya.AMRIT, Choghadiya.CHAR, Choghadiya.ROG, Choghadiya.KAAL),
            Vara.WEDNESDAY to listOf(Choghadiya.UDVEG, Choghadiya.SHUBH, Choghadiya.AMRIT, Choghadiya.CHAR, Choghadiya.ROG, Choghadiya.KAAL, Choghadiya.LABH, Choghadiya.UDVEG),
            Vara.THURSDAY to listOf(Choghadiya.AMRIT, Choghadiya.CHAR, Choghadiya.ROG, Choghadiya.KAAL, Choghadiya.LABH, Choghadiya.UDVEG, Choghadiya.SHUBH, Choghadiya.AMRIT),
            Vara.FRIDAY to listOf(Choghadiya.ROG, Choghadiya.KAAL, Choghadiya.LABH, Choghadiya.UDVEG, Choghadiya.SHUBH, Choghadiya.AMRIT, Choghadiya.CHAR, Choghadiya.ROG),
            Vara.SATURDAY to listOf(Choghadiya.LABH, Choghadiya.UDVEG, Choghadiya.SHUBH, Choghadiya.AMRIT, Choghadiya.CHAR, Choghadiya.ROG, Choghadiya.KAAL, Choghadiya.LABH)
        )

        // Safe fallback - Sunday sequence is guaranteed to exist
        val sequence = nightSequences[vara] ?: nightSequences.getValue(Vara.SUNDAY)

        return sequence.mapIndexed { index, choghadiya ->
            var startTime = sunset.plusMinutes(index * choghadiyaDuration)
            var endTime = sunset.plusMinutes((index + 1) * choghadiyaDuration)

            if (startTime.isBefore(sunset) && index > 0) {
                startTime = startTime.plusHours(24)
            }
            if (endTime.isBefore(startTime) || endTime.isBefore(sunset)) {
                endTime = endTime.plusHours(24)
            }

            ChoghadiyaInfo(
                choghadiya = choghadiya,
                startTime = startTime,
                endTime = endTime,
                isDay = false
            )
        }
    }

    private fun calculateHora(
        time: LocalTime,
        vara: Vara,
        sunrise: LocalTime,
        sunset: LocalTime
    ): Hora {
        val isDay = !time.isBefore(sunrise) && time.isBefore(sunset)

        val dayMinutes = ChronoUnit.MINUTES.between(sunrise, sunset)
        val nightMinutes = 24 * 60 - dayMinutes
        val dayHoraDuration = dayMinutes / DAY_HORAS
        val nightHoraDuration = nightMinutes / NIGHT_HORAS

        val horaNumber: Int
        val startTime: LocalTime
        val endTime: LocalTime

        if (isDay) {
            val minutesSinceSunrise = ChronoUnit.MINUTES.between(sunrise, time)
            horaNumber = (minutesSinceSunrise / dayHoraDuration).toInt().coerceIn(0, DAY_HORAS - 1)
            startTime = sunrise.plusMinutes(horaNumber * dayHoraDuration)
            endTime = sunrise.plusMinutes((horaNumber + 1) * dayHoraDuration)
        } else {
            val minutesSinceSunset = if (time >= sunset) {
                ChronoUnit.MINUTES.between(sunset, time)
            } else {
                ChronoUnit.MINUTES.between(sunset, LocalTime.MAX) +
                        ChronoUnit.MINUTES.between(LocalTime.MIN, time) + 1
            }
            horaNumber = DAY_HORAS + (minutesSinceSunset / nightHoraDuration).toInt().coerceIn(0, NIGHT_HORAS - 1)
            val nightHoraIndex = horaNumber - DAY_HORAS
            startTime = sunset.plusMinutes(nightHoraIndex * nightHoraDuration)
            endTime = sunset.plusMinutes((nightHoraIndex + 1) * nightHoraDuration)
        }

        val dayLordIndex = CHALDEAN_ORDER.indexOf(vara.lord)
        val horaLordIndex = (dayLordIndex + horaNumber) % 7
        val horaLord = CHALDEAN_ORDER[horaLordIndex]

        val nature = when (horaLord) {
            Planet.JUPITER, Planet.VENUS, Planet.MOON -> HoraNature.BENEFIC
            Planet.MERCURY -> HoraNature.NEUTRAL
            else -> HoraNature.MALEFIC
        }

        return Hora(
            lord = horaLord,
            horaNumber = horaNumber + 1,
            startTime = startTime,
            endTime = endTime,
            isDay = isDay,
            nature = nature
        )
    }

    private fun calculateAllHoras(
        vara: Vara,
        sunrise: LocalTime,
        sunset: LocalTime,
        nextSunrise: LocalTime
    ): List<Hora> {
        val horas = mutableListOf<Hora>()

        val dayMinutes = ChronoUnit.MINUTES.between(sunrise, sunset)
        val nightMinutes = if (nextSunrise.isAfter(sunset)) {
            ChronoUnit.MINUTES.between(sunset, nextSunrise)
        } else {
            ChronoUnit.MINUTES.between(sunset, LocalTime.MAX) +
                    ChronoUnit.MINUTES.between(LocalTime.MIN, nextSunrise) + 1
        }

        val dayHoraDuration = dayMinutes / DAY_HORAS
        val nightHoraDuration = nightMinutes / NIGHT_HORAS

        val dayLordIndex = CHALDEAN_ORDER.indexOf(vara.lord)

        for (i in 0 until DAY_HORAS) {
            val horaLordIndex = (dayLordIndex + i) % 7
            val horaLord = CHALDEAN_ORDER[horaLordIndex]
            val nature = when (horaLord) {
                Planet.JUPITER, Planet.VENUS, Planet.MOON -> HoraNature.BENEFIC
                Planet.MERCURY -> HoraNature.NEUTRAL
                else -> HoraNature.MALEFIC
            }

            horas.add(
                Hora(
                    lord = horaLord,
                    horaNumber = i + 1,
                    startTime = sunrise.plusMinutes(i * dayHoraDuration),
                    endTime = sunrise.plusMinutes((i + 1) * dayHoraDuration),
                    isDay = true,
                    nature = nature
                )
            )
        }

        for (i in 0 until NIGHT_HORAS) {
            val horaLordIndex = (dayLordIndex + DAY_HORAS + i) % 7
            val horaLord = CHALDEAN_ORDER[horaLordIndex]
            val nature = when (horaLord) {
                Planet.JUPITER, Planet.VENUS, Planet.MOON -> HoraNature.BENEFIC
                Planet.MERCURY -> HoraNature.NEUTRAL
                else -> HoraNature.MALEFIC
            }

            horas.add(
                Hora(
                    lord = horaLord,
                    horaNumber = DAY_HORAS + i + 1,
                    startTime = sunset.plusMinutes(i * nightHoraDuration),
                    endTime = sunset.plusMinutes((i + 1) * nightHoraDuration),
                    isDay = false,
                    nature = nature
                )
            )
        }

        return horas
    }

    private fun calculateInauspiciousPeriods(
        vara: Vara,
        sunrise: LocalTime,
        sunset: LocalTime
    ): InauspiciousPeriods {
        val dayMinutes = ChronoUnit.MINUTES.between(sunrise, sunset)
        val muhurtaDuration = dayMinutes / 8

        val rahukalaPositions = mapOf(
            Vara.SUNDAY to 8, Vara.MONDAY to 2, Vara.TUESDAY to 7,
            Vara.WEDNESDAY to 5, Vara.THURSDAY to 6, Vara.FRIDAY to 4,
            Vara.SATURDAY to 3
        )

        val yamaghantaPositions = mapOf(
            Vara.SUNDAY to 5, Vara.MONDAY to 4, Vara.TUESDAY to 3,
            Vara.WEDNESDAY to 2, Vara.THURSDAY to 1, Vara.FRIDAY to 7,
            Vara.SATURDAY to 6
        )

        val gulikaPositions = mapOf(
            Vara.SUNDAY to 7, Vara.MONDAY to 6, Vara.TUESDAY to 5,
            Vara.WEDNESDAY to 4, Vara.THURSDAY to 3, Vara.FRIDAY to 2,
            Vara.SATURDAY to 1
        )

        val rahukalaPos = rahukalaPositions[vara] ?: 1
        val yamaghantaPos = yamaghantaPositions[vara] ?: 1
        val gulikaPos = gulikaPositions[vara] ?: 1

        val rahukala = TimePeriod(
            startTime = sunrise.plusMinutes((rahukalaPos - 1) * muhurtaDuration),
            endTime = sunrise.plusMinutes(rahukalaPos * muhurtaDuration),
            name = "Rahukala"
        )

        val yamaghanta = TimePeriod(
            startTime = sunrise.plusMinutes((yamaghantaPos - 1) * muhurtaDuration),
            endTime = sunrise.plusMinutes(yamaghantaPos * muhurtaDuration),
            name = "Yamaghanta"
        )

        val gulikaKala = TimePeriod(
            startTime = sunrise.plusMinutes((gulikaPos - 1) * muhurtaDuration),
            endTime = sunrise.plusMinutes(gulikaPos * muhurtaDuration),
            name = "Gulika Kala"
        )

        val durmuhurtas = calculateDurmuhurtas(vara, sunrise, sunset)

        return InauspiciousPeriods(
            rahukala = rahukala,
            yamaghanta = yamaghanta,
            gulikaKala = gulikaKala,
            durmuhurtas = durmuhurtas
        )
    }

    private fun calculateDurmuhurtas(
        vara: Vara,
        sunrise: LocalTime,
        sunset: LocalTime
    ): List<TimePeriod> {
        val dayMinutes = ChronoUnit.MINUTES.between(sunrise, sunset)
        val muhurtaDuration = dayMinutes / DAY_MUHURTAS

        val durmuhurtaPositions = when (vara) {
            Vara.SUNDAY -> listOf(14)
            Vara.MONDAY -> listOf(10, 14)
            Vara.TUESDAY -> listOf(4, 11)
            Vara.WEDNESDAY -> listOf(8, 13)
            Vara.THURSDAY -> listOf(7, 12)
            Vara.FRIDAY -> listOf(6, 11)
            Vara.SATURDAY -> listOf(1, 2)
        }

        return durmuhurtaPositions.map { pos ->
            TimePeriod(
                startTime = sunrise.plusMinutes((pos - 1) * muhurtaDuration),
                endTime = sunrise.plusMinutes(pos * muhurtaDuration),
                name = "Durmuhurta"
            )
        }
    }

    private fun calculateAbhijitMuhurta(
        sunrise: LocalTime,
        sunset: LocalTime,
        currentTime: LocalTime
    ): AbhijitMuhurta {
        val dayMinutes = ChronoUnit.MINUTES.between(sunrise, sunset)
        val muhurtaDuration = dayMinutes / DAY_MUHURTAS

        val abhijitStart = sunrise.plusMinutes(7 * muhurtaDuration)
        val abhijitEnd = sunrise.plusMinutes(8 * muhurtaDuration)

        val isActive = currentTime >= abhijitStart && currentTime < abhijitEnd

        return AbhijitMuhurta(
            startTime = abhijitStart,
            endTime = abhijitEnd,
            isActive = isActive
        )
    }

    private fun calculateSpecialYogas(
        vara: Vara,
        tithi: TithiInfo,
        nakshatra: NakshatraInfo
    ): List<SpecialYoga> {
        val yogas = mutableListOf<SpecialYoga>()

        if (isAmritaSiddhiYoga(vara, tithi, nakshatra.nakshatra)) {
            yogas.add(
                SpecialYoga(
                    name = "Amrita Siddhi Yoga",
                    description = "Extremely auspicious combination for all activities",
                    isAuspicious = true
                )
            )
        }

        if (isSarvarthaSiddhiYoga(vara, tithi, nakshatra.nakshatra)) {
            yogas.add(
                SpecialYoga(
                    name = "Sarvartha Siddhi Yoga",
                    description = "Success in all undertakings",
                    isAuspicious = true
                )
            )
        }

        if (isRaviPushyaYoga(vara, nakshatra.nakshatra)) {
            yogas.add(
                SpecialYoga(
                    name = "Ravi Pushya Yoga",
                    description = "Highly auspicious for purchases and new ventures",
                    isAuspicious = true
                )
            )
        }

        if (isGuruPushyaYoga(vara, nakshatra.nakshatra)) {
            yogas.add(
                SpecialYoga(
                    name = "Guru Pushya Yoga",
                    description = "Excellent for education, spirituality, and investments",
                    isAuspicious = true
                )
            )
        }

        if (isDagdhaTithi(vara, tithi.displayNumber)) {
            yogas.add(
                SpecialYoga(
                    name = "Dagdha Tithi",
                    description = "Burnt tithi - avoid auspicious activities",
                    isAuspicious = false
                )
            )
        }

        return yogas
    }

    private fun isAmritaSiddhiYoga(vara: Vara, tithi: TithiInfo, nakshatra: Nakshatra): Boolean {
        val combinations = mapOf(
            Vara.SUNDAY to Pair(listOf(Nakshatra.HASTA, Nakshatra.MULA, Nakshatra.UTTARA_ASHADHA), listOf(2, 7, 12)),
            Vara.MONDAY to Pair(listOf(Nakshatra.MRIGASHIRA, Nakshatra.SHRAVANA, Nakshatra.ROHINI), listOf(2, 7, 12)),
            Vara.TUESDAY to Pair(listOf(Nakshatra.ASHWINI, Nakshatra.UTTARA_PHALGUNI), listOf(3, 8, 13)),
            Vara.WEDNESDAY to Pair(listOf(Nakshatra.ANURADHA, Nakshatra.REVATI), listOf(2, 7, 12)),
            Vara.THURSDAY to Pair(listOf(Nakshatra.PUNARVASU, Nakshatra.PUSHYA, Nakshatra.ASHWINI), listOf(5, 10, 15)),
            Vara.FRIDAY to Pair(listOf(Nakshatra.REVATI, Nakshatra.ANURADHA, Nakshatra.SWATI), listOf(1, 6, 11)),
            Vara.SATURDAY to Pair(listOf(Nakshatra.ROHINI, Nakshatra.SWATI), listOf(3, 8, 13))
        )

        val combo = combinations[vara] ?: return false
        return nakshatra in combo.first && tithi.displayNumber in combo.second
    }

    private fun isSarvarthaSiddhiYoga(vara: Vara, tithi: TithiInfo, nakshatra: Nakshatra): Boolean {
        val nakshatraCombinations = mapOf(
            Vara.SUNDAY to listOf(Nakshatra.PUSHYA, Nakshatra.HASTA, Nakshatra.UTTARA_BHADRAPADA, Nakshatra.UTTARA_ASHADHA, Nakshatra.UTTARA_PHALGUNI, Nakshatra.MULA, Nakshatra.ASHWINI),
            Vara.MONDAY to listOf(Nakshatra.ROHINI, Nakshatra.MRIGASHIRA, Nakshatra.PUSHYA, Nakshatra.ANURADHA, Nakshatra.SHRAVANA),
            Vara.TUESDAY to listOf(Nakshatra.ASHWINI, Nakshatra.UTTARA_PHALGUNI, Nakshatra.KRITTIKA, Nakshatra.CHITRA),
            Vara.WEDNESDAY to listOf(Nakshatra.ROHINI, Nakshatra.ANURADHA, Nakshatra.HASTA, Nakshatra.KRITTIKA),
            Vara.THURSDAY to listOf(Nakshatra.ASHWINI, Nakshatra.PUNARVASU, Nakshatra.PUSHYA, Nakshatra.SWATI, Nakshatra.REVATI),
            Vara.FRIDAY to listOf(Nakshatra.ASHWINI, Nakshatra.PUNARVASU, Nakshatra.ANURADHA, Nakshatra.REVATI, Nakshatra.SHRAVANA),
            Vara.SATURDAY to listOf(Nakshatra.ROHINI, Nakshatra.SWATI, Nakshatra.SHRAVANA)
        )

        val nakshatras = nakshatraCombinations[vara] ?: return false
        return nakshatra in nakshatras && tithi.isAuspicious
    }

    private fun isRaviPushyaYoga(vara: Vara, nakshatra: Nakshatra): Boolean {
        return vara == Vara.SUNDAY && nakshatra == Nakshatra.PUSHYA
    }

    private fun isGuruPushyaYoga(vara: Vara, nakshatra: Nakshatra): Boolean {
        return vara == Vara.THURSDAY && nakshatra == Nakshatra.PUSHYA
    }

    private fun isDagdhaTithi(vara: Vara, displayNumber: Int): Boolean {
        val dagdhaCombinations = mapOf(
            Vara.SUNDAY to 12,
            Vara.MONDAY to 11,
            Vara.TUESDAY to 5,
            Vara.WEDNESDAY to 3,
            Vara.THURSDAY to 6,
            Vara.FRIDAY to 8,
            Vara.SATURDAY to 9
        )
        return dagdhaCombinations[vara] == displayNumber
    }

    private fun evaluateMuhurta(
        vara: Vara,
        tithi: TithiInfo,
        nakshatra: NakshatraInfo,
        yoga: YogaInfo,
        karana: KaranaInfo,
        choghadiya: ChoghadiyaInfo,
        hora: Hora,
        time: LocalTime,
        inauspiciousPeriods: InauspiciousPeriods,
        abhijitMuhurta: AbhijitMuhurta,
        specialYogas: List<SpecialYoga>
    ): Quadruple<Int, List<ActivityType>, List<ActivityType>, List<String>> {
        var score = 50
        val recommendations = mutableListOf<String>()
        val suitableActivities = mutableListOf<ActivityType>()
        val avoidActivities = mutableListOf<ActivityType>()

        when (vara) {
            Vara.MONDAY, Vara.WEDNESDAY, Vara.THURSDAY, Vara.FRIDAY -> {
                score += 10
            }
            Vara.TUESDAY, Vara.SATURDAY -> {
                score -= 5
                recommendations.add("${vara.displayName} requires caution for new beginnings")
            }
            Vara.SUNDAY -> {
                score += 5
            }
        }

        if (tithi.isAuspicious) {
            score += 15
            if (tithi.nature == TithiNature.PURNA) {
                score += 5
                recommendations.add("Purna Tithi - excellent for completion of tasks")
            }
        } else {
            score -= 10
            recommendations.add("${tithi.name} is not ideal for new beginnings")
            if (tithi.nature == TithiNature.RIKTA) {
                score -= 5
                recommendations.add("Rikta Tithi - avoid financial matters")
            }
        }

        val generalActivity = ActivityType.GENERAL
        if (nakshatra.nakshatra in generalActivity.favorableNakshatras) {
            score += 20
            recommendations.add("${nakshatra.nakshatra.displayName} is auspicious")
        } else if (nakshatra.nakshatra in generalActivity.avoidNakshatras) {
            score -= 15
            recommendations.add("${nakshatra.nakshatra.displayName} requires caution")
        } else {
            score += 5
        }

        when (nakshatra.nature) {
            NakshatraNature.DHRUVA -> {
                score += 5
                recommendations.add("Fixed nakshatra - good for permanent activities")
            }
            NakshatraNature.KSHIPRA -> {
                score += 3
                recommendations.add("Swift nakshatra - good for quick results")
            }
            NakshatraNature.MRIDU -> {
                score += 3
                recommendations.add("Soft nakshatra - good for gentle activities")
            }
            NakshatraNature.TIKSHNA, NakshatraNature.UGRA -> {
                score -= 5
            }
            else -> {}
        }

        if (yoga.isAuspicious) {
            score += 10
        } else {
            score -= 10
            recommendations.add("${yoga.name} yoga is inauspicious")
        }

        if (karana.isAuspicious) {
            score += 5
        } else {
            score -= 8
            if (karana.name == "Vishti") {
                recommendations.add("Vishti (Bhadra) Karana - avoid important activities")
            }
        }

        score += when (choghadiya.choghadiya.nature) {
            ChoghadiyaNature.EXCELLENT -> {
                recommendations.add("${choghadiya.choghadiya.displayName} Choghadiya - excellent period")
                15
            }
            ChoghadiyaNature.VERY_GOOD -> 10
            ChoghadiyaNature.GOOD -> 5
            ChoghadiyaNature.NEUTRAL -> 0
            ChoghadiyaNature.INAUSPICIOUS -> {
                recommendations.add("${choghadiya.choghadiya.displayName} Choghadiya - inauspicious period")
                -10
            }
        }

        score += when (hora.nature) {
            HoraNature.BENEFIC -> {
                recommendations.add("${hora.lord.displayName} Hora - benefic influence")
                10
            }
            HoraNature.MALEFIC -> -8
            HoraNature.NEUTRAL -> 2
        }

        if (abhijitMuhurta.isActive) {
            score += 15
            recommendations.add("Abhijit Muhurta active - highly auspicious midday period")
        }

        for (specialYoga in specialYogas) {
            if (specialYoga.isAuspicious) {
                score += 15
                recommendations.add("${specialYoga.name}: ${specialYoga.description}")
            } else {
                score -= 15
                recommendations.add("Warning: ${specialYoga.name}")
            }
        }

        if (inauspiciousPeriods.rahukala.contains(time)) {
            score -= 25
            recommendations.add("Currently in Rahukala - strongly avoid new beginnings")
        }

        if (inauspiciousPeriods.yamaghanta.contains(time)) {
            score -= 15
            recommendations.add("Currently in Yamaghanta - avoid travel and important work")
        }

        if (inauspiciousPeriods.gulikaKala.contains(time)) {
            score -= 10
            recommendations.add("Currently in Gulika Kala - minor caution advised")
        }

        for (durmuhurta in inauspiciousPeriods.durmuhurtas) {
            if (durmuhurta.contains(time)) {
                score -= 12
                recommendations.add("Currently in Durmuhurta - avoid auspicious activities")
                break
            }
        }

        ActivityType.entries.forEach { activity ->
            var activityScore = 0
            var isAvoid = false

            if (nakshatra.nakshatra in activity.favorableNakshatras) activityScore += 3
            if (nakshatra.nakshatra in activity.avoidNakshatras) {
                activityScore -= 5
                isAvoid = true
            }
            if (vara in activity.favorableVaras) activityScore += 2
            if (tithi.displayNumber in activity.favorableTithis) activityScore += 2
            if (tithi.isAuspicious) activityScore += 1

            if (activityScore >= 5 && !isAvoid && score >= 50) {
                suitableActivities.add(activity)
            } else if (isAvoid || activityScore <= -2) {
                avoidActivities.add(activity)
            }
        }

        return Quadruple(
            score.coerceIn(0, 100),
            suitableActivities.distinct(),
            avoidActivities.distinct(),
            recommendations.distinct()
        )
    }

    private fun evaluateForActivity(
        muhurta: MuhurtaDetails,
        activity: ActivityType
    ): Triple<Int, List<String>, List<String>> {
        var score = 50
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (muhurta.nakshatra.nakshatra in activity.favorableNakshatras) {
            score += 20
            reasons.add("Excellent Nakshatra: ${muhurta.nakshatra.nakshatra.displayName}")
        } else if (muhurta.nakshatra.nakshatra in activity.avoidNakshatras) {
            score -= 25
            warnings.add("Unfavorable Nakshatra: ${muhurta.nakshatra.nakshatra.displayName}")
        }

        if (muhurta.vara in activity.favorableVaras) {
            score += 15
            reasons.add("Favorable day: ${muhurta.vara.displayName}")
        } else if (muhurta.vara == Vara.TUESDAY || muhurta.vara == Vara.SATURDAY) {
            if (activity != ActivityType.MEDICAL) {
                score -= 5
                warnings.add("${muhurta.vara.displayName} is not ideal for ${activity.displayName}")
            }
        }

        if (muhurta.tithi.displayNumber in activity.favorableTithis) {
            score += 12
            reasons.add("Favorable Tithi: ${muhurta.tithi.name}")
        }

        if (!muhurta.tithi.isAuspicious) {
            score -= 10
            warnings.add("Tithi (${muhurta.tithi.name}) may not be ideal")
        }

        if (muhurta.tithi.number == 15) {
            if (activity == ActivityType.SPIRITUAL) {
                score += 10
                reasons.add("Purnima - excellent for spiritual activities")
            }
        }

        when (muhurta.choghadiya.choghadiya.nature) {
            ChoghadiyaNature.EXCELLENT, ChoghadiyaNature.VERY_GOOD -> {
                score += 10
                reasons.add("Auspicious Choghadiya: ${muhurta.choghadiya.choghadiya.displayName}")
            }
            ChoghadiyaNature.INAUSPICIOUS -> {
                score -= 12
                warnings.add("Inauspicious Choghadiya: ${muhurta.choghadiya.choghadiya.displayName}")
            }
            else -> {}
        }

        if (muhurta.hora.nature == HoraNature.BENEFIC) {
            score += 5
            reasons.add("Benefic Hora: ${muhurta.hora.lord.displayName}")
        } else if (muhurta.hora.nature == HoraNature.MALEFIC) {
            score -= 5
        }

        if (muhurta.abhijitMuhurta.isActive) {
            score += 15
            reasons.add("Abhijit Muhurta - highly auspicious period")
        }

        for (yoga in muhurta.specialYogas) {
            if (yoga.isAuspicious) {
                score += 15
                reasons.add(yoga.name)
            } else {
                score -= 15
                warnings.add(yoga.name)
            }
        }

        val time = muhurta.dateTime.toLocalTime()

        if (muhurta.inauspiciousPeriods.rahukala.contains(time)) {
            score -= 30
            warnings.add("Rahukala period - strongly avoid")
        }

        if (muhurta.inauspiciousPeriods.yamaghanta.contains(time)) {
            score -= 15
            warnings.add("Yamaghanta period")
        }

        if (muhurta.inauspiciousPeriods.gulikaKala.contains(time)) {
            score -= 10
            warnings.add("Gulika Kala period")
        }

        for (durmuhurta in muhurta.inauspiciousPeriods.durmuhurtas) {
            if (durmuhurta.contains(time)) {
                score -= 12
                warnings.add("Durmuhurta period")
                break
            }
        }

        if (!muhurta.yoga.isAuspicious) {
            score -= 8
            warnings.add("Inauspicious Yoga: ${muhurta.yoga.name}")
        }

        if (!muhurta.karana.isAuspicious) {
            score -= 8
            warnings.add("Inauspicious Karana: ${muhurta.karana.name}")
        }

        return Triple(score.coerceIn(0, 100), reasons.distinct(), warnings.distinct())
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

        return normalizeDegrees(xx[0])
    }

    private fun calculateSunriseSunsetJD(
        julianDay: Double,
        latitude: Double,
        longitude: Double
    ): Pair<Double, Double> {
        val geopos = doubleArrayOf(longitude, latitude, 0.0)
        val tret = DblObj()
        val serr = StringBuffer()

        val jdMidnight = floor(julianDay - 0.5) + 0.5

        swissEph.swe_rise_trans(
            jdMidnight,
            SweConst.SE_SUN,
            null,
            SweConst.SEFLG_SWIEPH,
            SweConst.SE_CALC_RISE or SweConst.SE_BIT_DISC_CENTER,
            geopos,
            0.0,
            0.0,
            tret,
            serr
        )
        val sunriseJD = tret.`val`

        swissEph.swe_rise_trans(
            jdMidnight,
            SweConst.SE_SUN,
            null,
            SweConst.SEFLG_SWIEPH,
            SweConst.SE_CALC_SET or SweConst.SE_BIT_DISC_CENTER,
            geopos,
            0.0,
            0.0,
            tret,
            serr
        )
        val sunsetJD = tret.`val`

        return Pair(sunriseJD, sunsetJD)
    }

    private fun jdToLocalTime(jd: Double, zoneId: ZoneId): LocalTime {
        val sweDate = SweDate(jd)
        val utcHour = sweDate.hour

        val hourInt = utcHour.toInt()
        val minuteDouble = (utcHour - hourInt) * 60
        val minuteInt = minuteDouble.toInt()
        val secondDouble = (minuteDouble - minuteInt) * 60
        val secondInt = secondDouble.toInt().coerceIn(0, 59)

        val utcDateTime = LocalDateTime.of(
            sweDate.year,
            sweDate.month,
            sweDate.day,
            hourInt.coerceIn(0, 23),
            minuteInt.coerceIn(0, 59),
            secondInt
        )

        val utcZoned = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
        val localZoned = utcZoned.withZoneSameInstant(zoneId)

        return localZoned.toLocalTime()
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

    private fun normalizeDegrees(degrees: Double): Double {
        var result = degrees % 360.0
        if (result < 0) result += 360.0
        return result
    }

    fun getTithiEndTime(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val zoneId = ZoneId.of(timezone)
        var currentDateTime = dateTime
        val initialTithi = calculateMuhurta(currentDateTime, latitude, longitude, timezone).tithi.number

        var stepMinutes = 60L
        var iterations = 0
        val maxIterations = 2000

        while (iterations < maxIterations) {
            val nextDateTime = currentDateTime.plusMinutes(stepMinutes)
            val nextTithi = calculateMuhurta(nextDateTime, latitude, longitude, timezone).tithi.number

            if (nextTithi != initialTithi) {
                if (stepMinutes <= 1) {
                    return nextDateTime
                }
                stepMinutes = maxOf(1, stepMinutes / 2)
            } else {
                currentDateTime = nextDateTime
            }
            iterations++
        }

        return currentDateTime.plusHours(24)
    }

    fun getNakshatraEndTime(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val zoneId = ZoneId.of(timezone)
        var currentDateTime = dateTime
        val initialNakshatra = calculateMuhurta(currentDateTime, latitude, longitude, timezone).nakshatra.nakshatra

        var stepMinutes = 60L
        var iterations = 0
        val maxIterations = 2000

        while (iterations < maxIterations) {
            val nextDateTime = currentDateTime.plusMinutes(stepMinutes)
            val nextNakshatra = calculateMuhurta(nextDateTime, latitude, longitude, timezone).nakshatra.nakshatra

            if (nextNakshatra != initialNakshatra) {
                if (stepMinutes <= 1) {
                    return nextDateTime
                }
                stepMinutes = maxOf(1, stepMinutes / 2)
            } else {
                currentDateTime = nextDateTime
            }
            iterations++
        }

        return currentDateTime.plusHours(24)
    }

    fun getYogaEndTime(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val zoneId = ZoneId.of(timezone)
        var currentDateTime = dateTime
        val initialYoga = calculateMuhurta(currentDateTime, latitude, longitude, timezone).yoga.number

        var stepMinutes = 60L
        var iterations = 0
        val maxIterations = 2000

        while (iterations < maxIterations) {
            val nextDateTime = currentDateTime.plusMinutes(stepMinutes)
            val nextYoga = calculateMuhurta(nextDateTime, latitude, longitude, timezone).yoga.number

            if (nextYoga != initialYoga) {
                if (stepMinutes <= 1) {
                    return nextDateTime
                }
                stepMinutes = maxOf(1, stepMinutes / 2)
            } else {
                currentDateTime = nextDateTime
            }
            iterations++
        }

        return currentDateTime.plusHours(24)
    }

    fun getKaranaEndTime(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val zoneId = ZoneId.of(timezone)
        var currentDateTime = dateTime
        val initialKarana = calculateMuhurta(currentDateTime, latitude, longitude, timezone).karana.number

        var stepMinutes = 30L
        var iterations = 0
        val maxIterations = 2000

        while (iterations < maxIterations) {
            val nextDateTime = currentDateTime.plusMinutes(stepMinutes)
            val nextKarana = calculateMuhurta(nextDateTime, latitude, longitude, timezone).karana.number

            if (nextKarana != initialKarana) {
                if (stepMinutes <= 1) {
                    return nextDateTime
                }
                stepMinutes = maxOf(1, stepMinutes / 2)
            } else {
                currentDateTime = nextDateTime
            }
            iterations++
        }

        return currentDateTime.plusHours(12)
    }

    fun getPanchangaForDate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): PanchangaData {
        val sunriseTime = getSunriseTime(date, latitude, longitude, timezone)
        val sunriseDateTime = LocalDateTime.of(date, sunriseTime)

        val muhurta = calculateMuhurta(sunriseDateTime, latitude, longitude, timezone)

        val tithiEnd = getTithiEndTime(sunriseDateTime, latitude, longitude, timezone)
        val nakshatraEnd = getNakshatraEndTime(sunriseDateTime, latitude, longitude, timezone)
        val yogaEnd = getYogaEndTime(sunriseDateTime, latitude, longitude, timezone)
        val karanaEnd = getKaranaEndTime(sunriseDateTime, latitude, longitude, timezone)

        return PanchangaData(
            date = date,
            vara = muhurta.vara,
            tithi = muhurta.tithi,
            tithiEndTime = tithiEnd,
            nakshatra = muhurta.nakshatra,
            nakshatraEndTime = nakshatraEnd,
            yoga = muhurta.yoga,
            yogaEndTime = yogaEnd,
            karana = muhurta.karana,
            karanaEndTime = karanaEnd,
            sunrise = muhurta.sunrise,
            sunset = muhurta.sunset,
            rahukala = muhurta.inauspiciousPeriods.rahukala,
            yamaghanta = muhurta.inauspiciousPeriods.yamaghanta,
            gulikaKala = muhurta.inauspiciousPeriods.gulikaKala,
            abhijitMuhurta = muhurta.abhijitMuhurta,
            specialYogas = muhurta.specialYogas
        )
    }

    private fun getSunriseTime(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalTime {
        val dateTime = LocalDateTime.of(date, LocalTime.NOON)
        val zoneId = ZoneId.of(timezone)
        val zonedDateTime = ZonedDateTime.of(dateTime, zoneId)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(utcDateTime.toLocalDateTime())

        val (sunriseJd, _) = calculateSunriseSunsetJD(julianDay, latitude, longitude)
        return jdToLocalTime(sunriseJd, zoneId)
    }

    data class PanchangaData(
        val date: LocalDate,
        val vara: Vara,
        val tithi: TithiInfo,
        val tithiEndTime: LocalDateTime,
        val nakshatra: NakshatraInfo,
        val nakshatraEndTime: LocalDateTime,
        val yoga: YogaInfo,
        val yogaEndTime: LocalDateTime,
        val karana: KaranaInfo,
        val karanaEndTime: LocalDateTime,
        val sunrise: LocalTime,
        val sunset: LocalTime,
        val rahukala: TimePeriod,
        val yamaghanta: TimePeriod,
        val gulikaKala: TimePeriod,
        val abhijitMuhurta: AbhijitMuhurta,
        val specialYogas: List<SpecialYoga>
    )

    fun close() {
        swissEph.swe_close()
    }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}