package com.astro.storm.data.localization

import com.astro.storm.data.model.Gender
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.data.preferences.ThemeMode

/**
 * Extension functions for localized display names of various enums
 *
 * This provides a centralized way to get localized names for all enums
 * used throughout the app without modifying the original enum classes.
 */

/**
 * Get localized display name for Planet
 */
fun Planet.getLocalizedName(language: Language): String {
    return when (this) {
        Planet.SUN -> StringResources.get(StringKey.PLANET_SUN, language)
        Planet.MOON -> StringResources.get(StringKey.PLANET_MOON, language)
        Planet.MERCURY -> StringResources.get(StringKey.PLANET_MERCURY, language)
        Planet.VENUS -> StringResources.get(StringKey.PLANET_VENUS, language)
        Planet.MARS -> StringResources.get(StringKey.PLANET_MARS, language)
        Planet.JUPITER -> StringResources.get(StringKey.PLANET_JUPITER, language)
        Planet.SATURN -> StringResources.get(StringKey.PLANET_SATURN, language)
        Planet.RAHU -> StringResources.get(StringKey.PLANET_RAHU, language)
        Planet.KETU -> StringResources.get(StringKey.PLANET_KETU, language)
        Planet.URANUS -> StringResources.get(StringKey.PLANET_URANUS, language)
        Planet.NEPTUNE -> StringResources.get(StringKey.PLANET_NEPTUNE, language)
        Planet.PLUTO -> StringResources.get(StringKey.PLANET_PLUTO, language)
    }
}

/**
 * Get localized display name for ZodiacSign
 */
fun ZodiacSign.getLocalizedName(language: Language): String {
    return when (this) {
        ZodiacSign.ARIES -> StringResources.get(StringKey.SIGN_ARIES, language)
        ZodiacSign.TAURUS -> StringResources.get(StringKey.SIGN_TAURUS, language)
        ZodiacSign.GEMINI -> StringResources.get(StringKey.SIGN_GEMINI, language)
        ZodiacSign.CANCER -> StringResources.get(StringKey.SIGN_CANCER, language)
        ZodiacSign.LEO -> StringResources.get(StringKey.SIGN_LEO, language)
        ZodiacSign.VIRGO -> StringResources.get(StringKey.SIGN_VIRGO, language)
        ZodiacSign.LIBRA -> StringResources.get(StringKey.SIGN_LIBRA, language)
        ZodiacSign.SCORPIO -> StringResources.get(StringKey.SIGN_SCORPIO, language)
        ZodiacSign.SAGITTARIUS -> StringResources.get(StringKey.SIGN_SAGITTARIUS, language)
        ZodiacSign.CAPRICORN -> StringResources.get(StringKey.SIGN_CAPRICORN, language)
        ZodiacSign.AQUARIUS -> StringResources.get(StringKey.SIGN_AQUARIUS, language)
        ZodiacSign.PISCES -> StringResources.get(StringKey.SIGN_PISCES, language)
    }
}

/**
 * Get localized display name for Nakshatra
 */
fun Nakshatra.getLocalizedName(language: Language): String {
    return when (this) {
        Nakshatra.ASHWINI -> StringResources.get(StringKey.NAKSHATRA_ASHWINI, language)
        Nakshatra.BHARANI -> StringResources.get(StringKey.NAKSHATRA_BHARANI, language)
        Nakshatra.KRITTIKA -> StringResources.get(StringKey.NAKSHATRA_KRITTIKA, language)
        Nakshatra.ROHINI -> StringResources.get(StringKey.NAKSHATRA_ROHINI, language)
        Nakshatra.MRIGASHIRA -> StringResources.get(StringKey.NAKSHATRA_MRIGASHIRA, language)
        Nakshatra.ARDRA -> StringResources.get(StringKey.NAKSHATRA_ARDRA, language)
        Nakshatra.PUNARVASU -> StringResources.get(StringKey.NAKSHATRA_PUNARVASU, language)
        Nakshatra.PUSHYA -> StringResources.get(StringKey.NAKSHATRA_PUSHYA, language)
        Nakshatra.ASHLESHA -> StringResources.get(StringKey.NAKSHATRA_ASHLESHA, language)
        Nakshatra.MAGHA -> StringResources.get(StringKey.NAKSHATRA_MAGHA, language)
        Nakshatra.PURVA_PHALGUNI -> StringResources.get(StringKey.NAKSHATRA_PURVA_PHALGUNI, language)
        Nakshatra.UTTARA_PHALGUNI -> StringResources.get(StringKey.NAKSHATRA_UTTARA_PHALGUNI, language)
        Nakshatra.HASTA -> StringResources.get(StringKey.NAKSHATRA_HASTA, language)
        Nakshatra.CHITRA -> StringResources.get(StringKey.NAKSHATRA_CHITRA, language)
        Nakshatra.SWATI -> StringResources.get(StringKey.NAKSHATRA_SWATI, language)
        Nakshatra.VISHAKHA -> StringResources.get(StringKey.NAKSHATRA_VISHAKHA, language)
        Nakshatra.ANURADHA -> StringResources.get(StringKey.NAKSHATRA_ANURADHA, language)
        Nakshatra.JYESHTHA -> StringResources.get(StringKey.NAKSHATRA_JYESHTHA, language)
        Nakshatra.MULA -> StringResources.get(StringKey.NAKSHATRA_MULA, language)
        Nakshatra.PURVA_ASHADHA -> StringResources.get(StringKey.NAKSHATRA_PURVA_ASHADHA, language)
        Nakshatra.UTTARA_ASHADHA -> StringResources.get(StringKey.NAKSHATRA_UTTARA_ASHADHA, language)
        Nakshatra.SHRAVANA -> StringResources.get(StringKey.NAKSHATRA_SHRAVANA, language)
        Nakshatra.DHANISHTHA -> StringResources.get(StringKey.NAKSHATRA_DHANISHTHA, language)
        Nakshatra.SHATABHISHA -> StringResources.get(StringKey.NAKSHATRA_SHATABHISHA, language)
        Nakshatra.PURVA_BHADRAPADA -> StringResources.get(StringKey.NAKSHATRA_PURVA_BHADRAPADA, language)
        Nakshatra.UTTARA_BHADRAPADA -> StringResources.get(StringKey.NAKSHATRA_UTTARA_BHADRAPADA, language)
        Nakshatra.REVATI -> StringResources.get(StringKey.NAKSHATRA_REVATI, language)
    }
}

/**
 * Get localized display name for Gender
 */
fun Gender.getLocalizedName(language: Language): String {
    return when (this) {
        Gender.MALE -> StringResources.get(StringKey.GENDER_MALE, language)
        Gender.FEMALE -> StringResources.get(StringKey.GENDER_FEMALE, language)
        Gender.OTHER -> StringResources.get(StringKey.GENDER_OTHER, language)
    }
}

/**
 * Get localized display name for HouseSystem
 */
fun HouseSystem.getLocalizedName(language: Language): String {
    return when (this) {
        HouseSystem.PLACIDUS -> StringResources.get(StringKey.HOUSE_PLACIDUS, language)
        HouseSystem.KOCH -> StringResources.get(StringKey.HOUSE_KOCH, language)
        HouseSystem.PORPHYRIUS -> StringResources.get(StringKey.HOUSE_PORPHYRIUS, language)
        HouseSystem.REGIOMONTANUS -> StringResources.get(StringKey.HOUSE_REGIOMONTANUS, language)
        HouseSystem.CAMPANUS -> StringResources.get(StringKey.HOUSE_CAMPANUS, language)
        HouseSystem.EQUAL -> StringResources.get(StringKey.HOUSE_EQUAL, language)
        HouseSystem.WHOLE_SIGN -> StringResources.get(StringKey.HOUSE_WHOLE_SIGN, language)
        HouseSystem.VEHLOW -> StringResources.get(StringKey.HOUSE_VEHLOW, language)
        HouseSystem.MERIDIAN -> StringResources.get(StringKey.HOUSE_MERIDIAN, language)
        HouseSystem.MORINUS -> StringResources.get(StringKey.HOUSE_MORINUS, language)
        HouseSystem.ALCABITUS -> StringResources.get(StringKey.HOUSE_ALCABITUS, language)
    }
}

/**
 * Get localized display name for ThemeMode
 */
fun ThemeMode.getLocalizedName(language: Language): String {
    return when (this) {
        ThemeMode.LIGHT -> StringResources.get(StringKey.THEME_LIGHT, language)
        ThemeMode.DARK -> StringResources.get(StringKey.THEME_DARK, language)
        ThemeMode.SYSTEM -> StringResources.get(StringKey.THEME_SYSTEM, language)
    }
}

/**
 * Get localized description for ThemeMode
 */
fun ThemeMode.getLocalizedDescription(language: Language): String {
    return when (this) {
        ThemeMode.LIGHT -> StringResources.get(StringKey.THEME_LIGHT_DESC, language)
        ThemeMode.DARK -> StringResources.get(StringKey.THEME_DARK_DESC, language)
        ThemeMode.SYSTEM -> StringResources.get(StringKey.THEME_SYSTEM_DESC, language)
    }
}

/**
 * Get localized day name
 */
fun getDayName(dayOfWeek: Int, language: Language): String {
    return when (dayOfWeek) {
        1 -> StringResources.get(StringKey.DAY_MONDAY, language)
        2 -> StringResources.get(StringKey.DAY_TUESDAY, language)
        3 -> StringResources.get(StringKey.DAY_WEDNESDAY, language)
        4 -> StringResources.get(StringKey.DAY_THURSDAY, language)
        5 -> StringResources.get(StringKey.DAY_FRIDAY, language)
        6 -> StringResources.get(StringKey.DAY_SATURDAY, language)
        7 -> StringResources.get(StringKey.DAY_SUNDAY, language)
        else -> StringResources.get(StringKey.MISC_UNKNOWN, language)
    }
}

/**
 * Get energy description based on level
 */
fun getEnergyDescription(energy: Int, language: Language): String {
    return when {
        energy >= 9 -> StringResources.get(StringKey.ENERGY_EXCEPTIONAL, language)
        energy >= 8 -> StringResources.get(StringKey.ENERGY_EXCELLENT, language)
        energy >= 7 -> StringResources.get(StringKey.ENERGY_STRONG, language)
        energy >= 6 -> StringResources.get(StringKey.ENERGY_FAVORABLE, language)
        energy >= 5 -> StringResources.get(StringKey.ENERGY_BALANCED, language)
        energy >= 4 -> StringResources.get(StringKey.ENERGY_MODERATE, language)
        energy >= 3 -> StringResources.get(StringKey.ENERGY_LOWER, language)
        energy >= 2 -> StringResources.get(StringKey.ENERGY_CHALLENGING, language)
        else -> StringResources.get(StringKey.ENERGY_REST, language)
    }
}

/**
 * Get Ayanamsa localized name
 */
fun getAyanamsaLocalizedName(ayanamsa: String, language: Language): String {
    return when (ayanamsa.lowercase()) {
        "lahiri" -> StringResources.get(StringKey.AYANAMSA_LAHIRI, language)
        "raman" -> StringResources.get(StringKey.AYANAMSA_RAMAN, language)
        "krishnamurti" -> StringResources.get(StringKey.AYANAMSA_KRISHNAMURTI, language)
        "true chitrapaksha" -> StringResources.get(StringKey.AYANAMSA_TRUE_CHITRAPAKSHA, language)
        else -> ayanamsa
    }
}

/**
 * Format duration with localization
 */
fun formatLocalizedDuration(days: Long, language: Language): String {
    if (days <= 0) return "0d"

    return when {
        days < 7 -> StringResources.get(StringKey.TIME_DAYS, language, days)
        days < 30 -> {
            val weeks = days / 7
            StringResources.get(StringKey.TIME_WEEKS, language, weeks)
        }
        days < 365 -> {
            val months = days / 30
            StringResources.get(StringKey.TIME_MONTHS, language, months)
        }
        else -> {
            val years = days / 365
            StringResources.get(StringKey.TIME_YEARS, language, years)
        }
    }
}
