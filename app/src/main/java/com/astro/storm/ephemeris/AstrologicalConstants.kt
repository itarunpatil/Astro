package com.astro.storm.ephemeris

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.ZodiacSign

/**
 * Centralized Astrological Constants for AstroStorm
 *
 * This object contains all fundamental constants used throughout the Vedic astrology
 * calculators. Using this centralized source ensures consistency and reduces code duplication.
 *
 * References:
 * - Brihat Parashara Hora Shastra (BPHS)
 * - Phaladeepika
 * - Jataka Parijata
 */
object AstrologicalConstants {

    // ============================================
    // FUNDAMENTAL ZODIAC CONSTANTS
    // ============================================

    /** Degrees in one zodiac sign */
    const val DEGREES_PER_SIGN = 30.0

    /** Total degrees in the zodiac circle */
    const val DEGREES_PER_CIRCLE = 360.0

    /** Total number of zodiac signs */
    const val TOTAL_SIGNS = 12

    /** Total number of nakshatras */
    const val TOTAL_NAKSHATRAS = 27

    /** Degrees per nakshatra (360/27 = 13.333...) */
    const val DEGREES_PER_NAKSHATRA = 13.333333333333334

    /** Degrees per nakshatra pada (13.333.../4 = 3.333...) */
    const val DEGREES_PER_PADA = 3.333333333333333

    /** Number of padas per nakshatra */
    const val PADAS_PER_NAKSHATRA = 4

    /** Total padas in zodiac */
    const val TOTAL_PADAS = 108

    // ============================================
    // SHADBALA CONSTANTS
    // ============================================

    /** Virupas per Rupa (60 Virupas = 1 Rupa) */
    const val VIRUPAS_PER_RUPA = 60.0

    /** Maximum Shadbala virupas (considered very strong) */
    const val MAX_SHADBALA_VIRUPAS = 600.0

    /** Minimum required Shadbala for planets (standard) */
    val MINIMUM_REQUIRED_SHADBALA = mapOf(
        Planet.SUN to 390.0,
        Planet.MOON to 360.0,
        Planet.MARS to 300.0,
        Planet.MERCURY to 420.0,
        Planet.JUPITER to 390.0,
        Planet.VENUS to 330.0,
        Planet.SATURN to 300.0
    )

    // ============================================
    // ASPECT CONSTANTS
    // ============================================

    /** Standard aspect (7th house) strength */
    const val FULL_ASPECT_STRENGTH = 1.0

    /** Three-quarter aspect strength */
    const val THREE_QUARTER_ASPECT = 0.75

    /** Half aspect strength */
    const val HALF_ASPECT = 0.5

    /** Quarter aspect strength */
    const val QUARTER_ASPECT = 0.25

    /** Orb for conjunctions (degrees) */
    const val CONJUNCTION_ORB = 10.0

    /** Orb for standard aspects (degrees) */
    const val STANDARD_ASPECT_ORB = 12.0

    /**
     * Special aspects for Mars, Jupiter, and Saturn as per BPHS
     * - Mars: Casts 3/4 aspect on 4th and 8th houses
     * - Jupiter: Casts full aspect on 5th and 9th houses
     * - Saturn: Casts 3/4 aspect on 3rd and 10th houses
     * All planets cast full aspect on 7th house
     */
    val SPECIAL_ASPECTS = mapOf(
        Planet.MARS to mapOf(4 to THREE_QUARTER_ASPECT, 8 to THREE_QUARTER_ASPECT),
        Planet.JUPITER to mapOf(5 to FULL_ASPECT_STRENGTH, 9 to FULL_ASPECT_STRENGTH),
        Planet.SATURN to mapOf(3 to THREE_QUARTER_ASPECT, 10 to THREE_QUARTER_ASPECT)
    )

    // ============================================
    // HOUSE CLASSIFICATIONS
    // ============================================

    /** Kendra houses (Angular/Quadrant) - 1, 4, 7, 10 */
    val KENDRA_HOUSES = setOf(1, 4, 7, 10)

    /** Trikona houses (Trine) - 1, 5, 9 */
    val TRIKONA_HOUSES = setOf(1, 5, 9)

    /** Dusthana houses (Malefic/Difficult) - 6, 8, 12 */
    val DUSTHANA_HOUSES = setOf(6, 8, 12)

    /** Upachaya houses (Growth) - 3, 6, 10, 11 */
    val UPACHAYA_HOUSES = setOf(3, 6, 10, 11)

    /** Panapara houses (Succedent) - 2, 5, 8, 11 */
    val PANAPARA_HOUSES = setOf(2, 5, 8, 11)

    /** Apoklima houses (Cadent) - 3, 6, 9, 12 */
    val APOKLIMA_HOUSES = setOf(3, 6, 9, 12)

    /** Maraka houses (Death-inflicting) - 2, 7 */
    val MARAKA_HOUSES = setOf(2, 7)

    // ============================================
    // PLANET CLASSIFICATIONS
    // ============================================

    /** Natural benefic planets */
    val NATURAL_BENEFICS = setOf(Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY)

    /** Natural malefic planets */
    val NATURAL_MALEFICS = setOf(Planet.SUN, Planet.MARS, Planet.SATURN, Planet.RAHU, Planet.KETU)

    /** Graha (planets) that rule signs */
    val SIGN_RULING_PLANETS = setOf(
        Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
        Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    /** Shadow planets (Chaya Grahas) */
    val SHADOW_PLANETS = setOf(Planet.RAHU, Planet.KETU)

    /** Outer planets (modern, not classical Vedic) */
    val OUTER_PLANETS = setOf(Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)

    // ============================================
    // PLANETARY DIGNITIES
    // ============================================

    /**
     * Exaltation signs for each planet
     * Reference: BPHS Chapter 3
     */
    val EXALTATION_SIGNS = mapOf(
        Planet.SUN to ZodiacSign.ARIES,
        Planet.MOON to ZodiacSign.TAURUS,
        Planet.MARS to ZodiacSign.CAPRICORN,
        Planet.MERCURY to ZodiacSign.VIRGO,
        Planet.JUPITER to ZodiacSign.CANCER,
        Planet.VENUS to ZodiacSign.PISCES,
        Planet.SATURN to ZodiacSign.LIBRA,
        Planet.RAHU to ZodiacSign.TAURUS,   // Some traditions
        Planet.KETU to ZodiacSign.SCORPIO   // Some traditions
    )

    /**
     * Exaltation degrees for each planet (exact point of maximum exaltation)
     * Reference: BPHS Chapter 3, Verse 49
     */
    val EXALTATION_DEGREES = mapOf(
        Planet.SUN to 10.0,      // 10° Aries
        Planet.MOON to 3.0,     // 3° Taurus
        Planet.MARS to 28.0,    // 28° Capricorn
        Planet.MERCURY to 15.0, // 15° Virgo
        Planet.JUPITER to 5.0,  // 5° Cancer
        Planet.VENUS to 27.0,   // 27° Pisces
        Planet.SATURN to 20.0   // 20° Libra
    )

    /**
     * Debilitation signs for each planet (opposite to exaltation)
     * Reference: BPHS Chapter 3
     */
    val DEBILITATION_SIGNS = mapOf(
        Planet.SUN to ZodiacSign.LIBRA,
        Planet.MOON to ZodiacSign.SCORPIO,
        Planet.MARS to ZodiacSign.CANCER,
        Planet.MERCURY to ZodiacSign.PISCES,
        Planet.JUPITER to ZodiacSign.CAPRICORN,
        Planet.VENUS to ZodiacSign.VIRGO,
        Planet.SATURN to ZodiacSign.ARIES,
        Planet.RAHU to ZodiacSign.SCORPIO,  // Some traditions
        Planet.KETU to ZodiacSign.TAURUS    // Some traditions
    )

    /**
     * Debilitation degrees (opposite to exaltation degree)
     */
    val DEBILITATION_DEGREES = mapOf(
        Planet.SUN to 10.0,     // 10° Libra
        Planet.MOON to 3.0,    // 3° Scorpio
        Planet.MARS to 28.0,   // 28° Cancer
        Planet.MERCURY to 15.0,// 15° Pisces
        Planet.JUPITER to 5.0, // 5° Capricorn
        Planet.VENUS to 27.0,  // 27° Virgo
        Planet.SATURN to 20.0  // 20° Aries
    )

    /**
     * Own signs for each planet
     * Reference: BPHS Chapter 3
     */
    val OWN_SIGNS = mapOf(
        Planet.SUN to setOf(ZodiacSign.LEO),
        Planet.MOON to setOf(ZodiacSign.CANCER),
        Planet.MARS to setOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO),
        Planet.MERCURY to setOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO),
        Planet.JUPITER to setOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES),
        Planet.VENUS to setOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA),
        Planet.SATURN to setOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS)
    )

    /**
     * Moolatrikona signs and degree ranges
     * Moolatrikona is stronger than own sign but weaker than exaltation
     * Reference: BPHS Chapter 3
     */
    data class MoolatrikonaRange(
        val sign: ZodiacSign,
        val startDegree: Double,
        val endDegree: Double
    )

    val MOOLATRIKONA_RANGES = mapOf(
        Planet.SUN to MoolatrikonaRange(ZodiacSign.LEO, 0.0, 20.0),
        Planet.MOON to MoolatrikonaRange(ZodiacSign.TAURUS, 4.0, 30.0),
        Planet.MARS to MoolatrikonaRange(ZodiacSign.ARIES, 0.0, 12.0),
        Planet.MERCURY to MoolatrikonaRange(ZodiacSign.VIRGO, 16.0, 20.0),
        Planet.JUPITER to MoolatrikonaRange(ZodiacSign.SAGITTARIUS, 0.0, 10.0),
        Planet.VENUS to MoolatrikonaRange(ZodiacSign.LIBRA, 0.0, 15.0),
        Planet.SATURN to MoolatrikonaRange(ZodiacSign.AQUARIUS, 0.0, 20.0)
    )

    // ============================================
    // PLANETARY RELATIONSHIPS (PERMANENT)
    // ============================================

    /**
     * Natural friends for each planet
     * Reference: BPHS Chapter 3
     */
    val NATURAL_FRIENDS = mapOf(
        Planet.SUN to setOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
        Planet.MOON to setOf(Planet.SUN, Planet.MERCURY),
        Planet.MARS to setOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
        Planet.MERCURY to setOf(Planet.SUN, Planet.VENUS),
        Planet.JUPITER to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.VENUS to setOf(Planet.MERCURY, Planet.SATURN),
        Planet.SATURN to setOf(Planet.MERCURY, Planet.VENUS)
    )

    /**
     * Natural enemies for each planet
     * Reference: BPHS Chapter 3
     */
    val NATURAL_ENEMIES = mapOf(
        Planet.SUN to setOf(Planet.SATURN, Planet.VENUS),
        Planet.MOON to emptySet<Planet>(),  // Moon has no natural enemies
        Planet.MARS to setOf(Planet.MERCURY),
        Planet.MERCURY to setOf(Planet.MOON),
        Planet.JUPITER to setOf(Planet.MERCURY, Planet.VENUS),
        Planet.VENUS to setOf(Planet.SUN, Planet.MOON),
        Planet.SATURN to setOf(Planet.SUN, Planet.MOON, Planet.MARS)
    )

    // ============================================
    // VIMSOTTARI DASHA PERIODS
    // ============================================

    /**
     * Vimsottari Mahadasha periods in years
     * Total cycle: 120 years
     * Reference: BPHS Chapter 46
     */
    val VIMSOTTARI_PERIODS = mapOf(
        Planet.KETU to 7.0,
        Planet.VENUS to 20.0,
        Planet.SUN to 6.0,
        Planet.MOON to 10.0,
        Planet.MARS to 7.0,
        Planet.RAHU to 18.0,
        Planet.JUPITER to 16.0,
        Planet.SATURN to 19.0,
        Planet.MERCURY to 17.0
    )

    /** Total Vimsottari Dasha cycle in years */
    const val VIMSOTTARI_TOTAL_YEARS = 120.0

    /**
     * Vimsottari Dasha sequence
     * Starts from birth nakshatra ruler
     */
    val VIMSOTTARI_SEQUENCE = listOf(
        Planet.KETU, Planet.VENUS, Planet.SUN, Planet.MOON,
        Planet.MARS, Planet.RAHU, Planet.JUPITER, Planet.SATURN, Planet.MERCURY
    )

    // ============================================
    // HORA CONSTANTS
    // ============================================

    /**
     * Hora rulers for each day
     * Reference: Traditional Muhurta texts
     */
    val DAY_HORA_LORDS = mapOf(
        java.time.DayOfWeek.SUNDAY to Planet.SUN,
        java.time.DayOfWeek.MONDAY to Planet.MOON,
        java.time.DayOfWeek.TUESDAY to Planet.MARS,
        java.time.DayOfWeek.WEDNESDAY to Planet.MERCURY,
        java.time.DayOfWeek.THURSDAY to Planet.JUPITER,
        java.time.DayOfWeek.FRIDAY to Planet.VENUS,
        java.time.DayOfWeek.SATURDAY to Planet.SATURN
    )

    /** Hora sequence for day hours (starting from day lord) */
    val HORA_SEQUENCE = listOf(
        Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON,
        Planet.SATURN, Planet.JUPITER, Planet.MARS
    )

    // ============================================
    // COMBUSTION THRESHOLDS
    // ============================================

    /**
     * Combustion degrees (distance from Sun below which planet is combust)
     * Reference: Phaladeepika, BPHS
     */
    val COMBUSTION_DEGREES = mapOf(
        Planet.MOON to 12.0,
        Planet.MARS to 17.0,
        Planet.MERCURY to 14.0,  // 12° when retrograde
        Planet.JUPITER to 11.0,
        Planet.VENUS to 10.0,    // 8° when retrograde
        Planet.SATURN to 15.0
    )

    /** Cazimi distance (heart of Sun - planet gains strength) */
    const val CAZIMI_DEGREE = 0.2833  // 17 arc minutes

    // ============================================
    // UTILITY FUNCTIONS
    // ============================================

    /**
     * Normalize an angle to 0-360 range
     */
    fun normalizeDegree(degree: Double): Double {
        var result = degree % DEGREES_PER_CIRCLE
        if (result < 0) result += DEGREES_PER_CIRCLE
        return result
    }

    /**
     * Calculate the degree within a sign (0-30)
     */
    fun getDegreeInSign(longitude: Double): Double {
        return normalizeDegree(longitude) % DEGREES_PER_SIGN
    }

    /**
     * Calculate the angular distance between two points (shortest path)
     */
    fun angularDistance(deg1: Double, deg2: Double): Double {
        val diff = kotlin.math.abs(normalizeDegree(deg1) - normalizeDegree(deg2))
        return kotlin.math.min(diff, DEGREES_PER_CIRCLE - diff)
    }

    /**
     * Check if a planet is in exaltation
     */
    fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return EXALTATION_SIGNS[planet] == sign
    }

    /**
     * Check if a planet is in debilitation
     */
    fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return DEBILITATION_SIGNS[planet] == sign
    }

    /**
     * Check if a planet is in its own sign
     */
    fun isInOwnSign(planet: Planet, sign: ZodiacSign): Boolean {
        return OWN_SIGNS[planet]?.contains(sign) == true
    }

    /**
     * Check if a planet is in moolatrikona
     */
    fun isInMoolatrikona(planet: Planet, sign: ZodiacSign, degreeInSign: Double): Boolean {
        val range = MOOLATRIKONA_RANGES[planet] ?: return false
        return sign == range.sign && degreeInSign >= range.startDegree && degreeInSign <= range.endDegree
    }

    /**
     * Check if a planet is a natural benefic
     */
    fun isNaturalBenefic(planet: Planet): Boolean {
        return planet in NATURAL_BENEFICS
    }

    /**
     * Check if a planet is a natural malefic
     */
    fun isNaturalMalefic(planet: Planet): Boolean {
        return planet in NATURAL_MALEFICS
    }

    /**
     * Check if a house is a Kendra
     */
    fun isKendra(house: Int): Boolean {
        return house in KENDRA_HOUSES
    }

    /**
     * Check if a house is a Trikona
     */
    fun isTrikona(house: Int): Boolean {
        return house in TRIKONA_HOUSES
    }

    /**
     * Check if a house is a Dusthana
     */
    fun isDusthana(house: Int): Boolean {
        return house in DUSTHANA_HOUSES
    }

    /**
     * Get special aspect strength for a planet aspecting a house
     * Returns the strength factor (0.25, 0.5, 0.75, or 1.0)
     */
    fun getSpecialAspectStrength(planet: Planet, aspectHouse: Int): Double {
        // All planets have full aspect on 7th house
        if (aspectHouse == 7) return FULL_ASPECT_STRENGTH

        // Check special aspects
        return SPECIAL_ASPECTS[planet]?.get(aspectHouse) ?: 0.0
    }

    /**
     * Get all houses a planet aspects with their strengths
     * Returns map of house number to aspect strength
     */
    fun getAllAspects(planet: Planet): Map<Int, Double> {
        val aspects = mutableMapOf<Int, Double>()

        // Full aspect on 7th for all planets
        aspects[7] = FULL_ASPECT_STRENGTH

        // Add special aspects if any
        SPECIAL_ASPECTS[planet]?.let { specialAspects ->
            aspects.putAll(specialAspects)
        }

        return aspects
    }

    /**
     * Get Vimsottari Dasha period for a planet
     */
    fun getVimsottariPeriod(planet: Planet): Double {
        return VIMSOTTARI_PERIODS[planet] ?: 0.0
    }

    /**
     * Check if a planet is combust (too close to Sun)
     */
    fun isCombust(planet: Planet, distanceFromSun: Double): Boolean {
        if (planet == Planet.SUN) return false // Sun cannot be combust
        val threshold = COMBUSTION_DEGREES[planet] ?: return false
        return distanceFromSun < threshold
    }

    /**
     * Check if a planet is in Cazimi (heart of Sun - empowered)
     */
    fun isCazimi(distanceFromSun: Double): Boolean {
        return distanceFromSun <= CAZIMI_DEGREE
    }
}
