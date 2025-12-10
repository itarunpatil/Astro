package com.astro.storm.data.model

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources

/**
 * Vedic zodiac signs (Rashis)
 */
enum class ZodiacSign(
    val number: Int,
    val displayName: String,
    val abbreviation: String,
    val element: String,
    val ruler: Planet,
    val quality: Quality
) {
    ARIES(1, "Aries", "Ar", "Fire", Planet.MARS, Quality.CARDINAL),
    TAURUS(2, "Taurus", "Ta", "Earth", Planet.VENUS, Quality.FIXED),
    GEMINI(3, "Gemini", "Ge", "Air", Planet.MERCURY, Quality.MUTABLE),
    CANCER(4, "Cancer", "Ca", "Water", Planet.MOON, Quality.CARDINAL),
    LEO(5, "Leo", "Le", "Fire", Planet.SUN, Quality.FIXED),
    VIRGO(6, "Virgo", "Vi", "Earth", Planet.MERCURY, Quality.MUTABLE),
    LIBRA(7, "Libra", "Li", "Air", Planet.VENUS, Quality.CARDINAL),
    SCORPIO(8, "Scorpio", "Sc", "Water", Planet.MARS, Quality.FIXED),
    SAGITTARIUS(9, "Sagittarius", "Sg", "Fire", Planet.JUPITER, Quality.MUTABLE),
    CAPRICORN(10, "Capricorn", "Cp", "Earth", Planet.SATURN, Quality.CARDINAL),
    AQUARIUS(11, "Aquarius", "Aq", "Air", Planet.SATURN, Quality.FIXED),
    PISCES(12, "Pisces", "Pi", "Water", Planet.JUPITER, Quality.MUTABLE);

    val startDegree: Double get() = (number - 1) * ZODIAC_SIGN_SPAN
    val endDegree: Double get() = number * ZODIAC_SIGN_SPAN

    /**
     * Get localized sign name based on current language
     */
    fun getLocalizedName(language: Language): String {
        val key = when (this) {
            ARIES -> StringKey.SIGN_ARIES
            TAURUS -> StringKey.SIGN_TAURUS
            GEMINI -> StringKey.SIGN_GEMINI
            CANCER -> StringKey.SIGN_CANCER
            LEO -> StringKey.SIGN_LEO
            VIRGO -> StringKey.SIGN_VIRGO
            LIBRA -> StringKey.SIGN_LIBRA
            SCORPIO -> StringKey.SIGN_SCORPIO
            SAGITTARIUS -> StringKey.SIGN_SAGITTARIUS
            CAPRICORN -> StringKey.SIGN_CAPRICORN
            AQUARIUS -> StringKey.SIGN_AQUARIUS
            PISCES -> StringKey.SIGN_PISCES
        }
        return StringResources.get(key, language)
    }

    companion object {
        private const val ZODIAC_SIGN_SPAN = 30.0
        private val ZODIAC_SIGNS = values()

        fun fromLongitude(longitude: Double): ZodiacSign {
            val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0
            val signIndex = (normalizedLongitude / ZODIAC_SIGN_SPAN).toInt()
            return ZODIAC_SIGNS[signIndex.coerceIn(0, 11)]
        }
    }
}

enum class Quality {
    CARDINAL,
    FIXED,
    MUTABLE
}
