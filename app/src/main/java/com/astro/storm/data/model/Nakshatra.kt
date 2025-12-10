package com.astro.storm.data.model

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources

/**
 * 27 Nakshatras in Vedic astrology
 */
enum class Nakshatra(
    val number: Int,
    val displayName: String,
    val ruler: Planet,
    val deity: String,
    val pada1Sign: ZodiacSign,
    val pada2Sign: ZodiacSign,
    val pada3Sign: ZodiacSign,
    val pada4Sign: ZodiacSign
) {
    ASHWINI(1, "Ashwini", Planet.KETU, "Ashwini Kumaras",
        ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES),
    BHARANI(2, "Bharani", Planet.VENUS, "Yama",
        ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES),
    KRITTIKA(3, "Krittika", Planet.SUN, "Agni",
        ZodiacSign.ARIES, ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS),
    ROHINI(4, "Rohini", Planet.MOON, "Brahma",
        ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS),
    MRIGASHIRA(5, "Mrigashira", Planet.MARS, "Soma",
        ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.GEMINI, ZodiacSign.GEMINI),
    ARDRA(6, "Ardra", Planet.RAHU, "Rudra",
        ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI),
    PUNARVASU(7, "Punarvasu", Planet.JUPITER, "Aditi",
        ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.CANCER),
    PUSHYA(8, "Pushya", Planet.SATURN, "Brihaspati",
        ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER),
    ASHLESHA(9, "Ashlesha", Planet.MERCURY, "Sarpa",
        ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER),
    MAGHA(10, "Magha", Planet.KETU, "Pitris",
        ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO),
    PURVA_PHALGUNI(11, "Purva Phalguni", Planet.VENUS, "Bhaga",
        ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO),
    UTTARA_PHALGUNI(12, "Uttara Phalguni", Planet.SUN, "Aryaman",
        ZodiacSign.LEO, ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO),
    HASTA(13, "Hasta", Planet.MOON, "Savitar",
        ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO),
    CHITRA(14, "Chitra", Planet.MARS, "Tvashtar",
        ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.LIBRA, ZodiacSign.LIBRA),
    SWATI(15, "Swati", Planet.RAHU, "Vayu",
        ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA),
    VISHAKHA(16, "Vishakha", Planet.JUPITER, "Indra-Agni",
        ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.SCORPIO),
    ANURADHA(17, "Anuradha", Planet.SATURN, "Mitra",
        ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO),
    JYESHTHA(18, "Jyeshtha", Planet.MERCURY, "Indra",
        ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO),
    MULA(19, "Mula", Planet.KETU, "Nirriti",
        ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS),
    PURVA_ASHADHA(20, "Purva Ashadha", Planet.VENUS, "Apas",
        ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS),
    UTTARA_ASHADHA(21, "Uttara Ashadha", Planet.SUN, "Vishwadevas",
        ZodiacSign.SAGITTARIUS, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN),
    SHRAVANA(22, "Shravana", Planet.MOON, "Vishnu",
        ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN),
    DHANISHTHA(23, "Dhanishtha", Planet.MARS, "Vasus",
        ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS),
    SHATABHISHA(24, "Shatabhisha", Planet.RAHU, "Varuna",
        ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS),
    PURVA_BHADRAPADA(25, "Purva Bhadrapada", Planet.JUPITER, "Aja Ekapada",
        ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.PISCES),
    UTTARA_BHADRAPADA(26, "Uttara Bhadrapada", Planet.SATURN, "Ahir Budhnya",
        ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES),
    REVATI(27, "Revati", Planet.MERCURY, "Pushan",
        ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES);

    val startDegree: Double get() = (number - 1) * NAKSHATRA_SPAN
    val endDegree: Double get() = number * NAKSHATRA_SPAN

    /**
     * Get localized nakshatra name based on current language
     */
    fun getLocalizedName(language: Language): String {
        val key = when (this) {
            ASHWINI -> StringKey.NAKSHATRA_ASHWINI
            BHARANI -> StringKey.NAKSHATRA_BHARANI
            KRITTIKA -> StringKey.NAKSHATRA_KRITTIKA
            ROHINI -> StringKey.NAKSHATRA_ROHINI
            MRIGASHIRA -> StringKey.NAKSHATRA_MRIGASHIRA
            ARDRA -> StringKey.NAKSHATRA_ARDRA
            PUNARVASU -> StringKey.NAKSHATRA_PUNARVASU
            PUSHYA -> StringKey.NAKSHATRA_PUSHYA
            ASHLESHA -> StringKey.NAKSHATRA_ASHLESHA
            MAGHA -> StringKey.NAKSHATRA_MAGHA
            PURVA_PHALGUNI -> StringKey.NAKSHATRA_PURVA_PHALGUNI
            UTTARA_PHALGUNI -> StringKey.NAKSHATRA_UTTARA_PHALGUNI
            HASTA -> StringKey.NAKSHATRA_HASTA
            CHITRA -> StringKey.NAKSHATRA_CHITRA
            SWATI -> StringKey.NAKSHATRA_SWATI
            VISHAKHA -> StringKey.NAKSHATRA_VISHAKHA
            ANURADHA -> StringKey.NAKSHATRA_ANURADHA
            JYESHTHA -> StringKey.NAKSHATRA_JYESHTHA
            MULA -> StringKey.NAKSHATRA_MULA
            PURVA_ASHADHA -> StringKey.NAKSHATRA_PURVA_ASHADHA
            UTTARA_ASHADHA -> StringKey.NAKSHATRA_UTTARA_ASHADHA
            SHRAVANA -> StringKey.NAKSHATRA_SHRAVANA
            DHANISHTHA -> StringKey.NAKSHATRA_DHANISHTHA
            SHATABHISHA -> StringKey.NAKSHATRA_SHATABHISHA
            PURVA_BHADRAPADA -> StringKey.NAKSHATRA_PURVA_BHADRAPADA
            UTTARA_BHADRAPADA -> StringKey.NAKSHATRA_UTTARA_BHADRAPADA
            REVATI -> StringKey.NAKSHATRA_REVATI
        }
        return StringResources.get(key, language)
    }

    companion object {
        /**
         * Precise nakshatra span using BigDecimal for maximum accuracy.
         * Each nakshatra spans exactly 360/27 = 13.333... degrees (13°20').
         * Using BigDecimal avoids floating-point precision errors that could
         * cause incorrect nakshatra determination at boundary degrees.
         */
        private val NAKSHATRA_SPAN_BD = java.math.BigDecimal("360")
            .divide(java.math.BigDecimal("27"), java.math.MathContext(20, java.math.RoundingMode.HALF_EVEN))

        /**
         * Each pada (quarter) spans exactly 1/4 of a nakshatra = 3.333... degrees (3°20').
         */
        private val PADA_SPAN_BD = NAKSHATRA_SPAN_BD
            .divide(java.math.BigDecimal("4"), java.math.MathContext(20, java.math.RoundingMode.HALF_EVEN))

        private val CIRCLE_BD = java.math.BigDecimal("360")

        // Cache entries for fast lookup
        private val NAKSHATRA_ENTRIES = entries.toTypedArray()

        /**
         * Determines the nakshatra and pada for a given sidereal longitude.
         *
         * Uses high-precision BigDecimal arithmetic to ensure accurate
         * determination even at nakshatra boundaries where floating-point
         * errors could cause incorrect results.
         *
         * @param longitude Sidereal longitude in degrees (0-360)
         * @return Pair of (Nakshatra, pada) where pada is 1-4
         */
        fun fromLongitude(longitude: Double): Pair<Nakshatra, Int> {
            // Handle edge cases
            if (longitude.isNaN() || longitude.isInfinite()) {
                return ASHWINI to 1
            }

            // Convert to BigDecimal for precise calculation
            val longitudeBd = java.math.BigDecimal(longitude.toString())

            // Normalize to 0-360 range using proper modulo
            var normalizedLongitude = longitudeBd.remainder(CIRCLE_BD, java.math.MathContext.DECIMAL128)
            if (normalizedLongitude < java.math.BigDecimal.ZERO) {
                normalizedLongitude = normalizedLongitude.add(CIRCLE_BD)
            }

            // Calculate nakshatra index (0-26)
            val nakshatraIndexBd = normalizedLongitude.divide(NAKSHATRA_SPAN_BD, java.math.MathContext(20, java.math.RoundingMode.FLOOR))
            val nakshatraIndex = nakshatraIndexBd.toInt().coerceIn(0, 26)
            val nakshatra = NAKSHATRA_ENTRIES[nakshatraIndex]

            // Calculate position within nakshatra
            val nakshatraStartBd = java.math.BigDecimal(nakshatra.startDegree.toString())
            var positionInNakshatra = normalizedLongitude.subtract(nakshatraStartBd)

            // Handle edge case where position might be slightly negative due to precision
            if (positionInNakshatra < java.math.BigDecimal.ZERO) {
                positionInNakshatra = java.math.BigDecimal.ZERO
            }

            // Calculate pada (1-4)
            // Pada 1: 0° to 3°20', Pada 2: 3°20' to 6°40', etc.
            val padaIndexBd = positionInNakshatra.divide(PADA_SPAN_BD, java.math.MathContext(20, java.math.RoundingMode.FLOOR))
            val pada = (padaIndexBd.toInt() + 1).coerceIn(1, 4)

            return nakshatra to pada
        }

        /**
         * Gets the navamsha (D9) sign for a given longitude.
         * Each pada corresponds to a specific navamsha sign.
         *
         * @param longitude Sidereal longitude in degrees
         * @return The navamsha sign for this longitude
         */
        fun getNavamshaSign(longitude: Double): ZodiacSign {
            val (nakshatra, pada) = fromLongitude(longitude)
            return when (pada) {
                1 -> nakshatra.pada1Sign
                2 -> nakshatra.pada2Sign
                3 -> nakshatra.pada3Sign
                4 -> nakshatra.pada4Sign
                else -> nakshatra.pada1Sign
            }
        }
    }
}
