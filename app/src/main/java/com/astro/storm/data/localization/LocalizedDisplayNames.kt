package com.astro.storm.data.localization

import com.astro.storm.data.model.Gender
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.data.preferences.ThemeMode
import com.astro.storm.ephemeris.YogaCalculator
import com.astro.storm.ephemeris.RemediesCalculator
import com.astro.storm.data.model.Yoni
import com.astro.storm.ephemeris.Tithi
import com.astro.storm.ephemeris.TithiGroup
import com.astro.storm.ephemeris.Yoga
import com.astro.storm.ephemeris.YogaNature
import com.astro.storm.ephemeris.Karana
import com.astro.storm.ephemeris.KaranaType
import com.astro.storm.ephemeris.Vara
import com.astro.storm.ephemeris.Paksha
import com.astro.storm.ephemeris.StrengthRating
import com.astro.storm.ephemeris.RetrogradeCombustionCalculator

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

// ============================================
// PANCHANGA EXTENSIONS
// ============================================

/**
 * Get localized display name for Tithi
 */
fun Tithi.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> this.sanskrit
    }
}

/**
 * Get localized display name for TithiGroup
 */
fun TithiGroup.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> when (this) {
            TithiGroup.NANDA -> "नन्दा"
            TithiGroup.BHADRA -> "भद्रा"
            TithiGroup.JAYA -> "जया"
            TithiGroup.RIKTA -> "रिक्ता"
            TithiGroup.PURNA -> "पूर्णा"
        }
    }
}

/**
 * Get localized nature description for TithiGroup
 */
fun TithiGroup.getLocalizedNature(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.nature
        Language.NEPALI -> when (this) {
            TithiGroup.NANDA -> "आनन्दमय"
            TithiGroup.BHADRA -> "शुभ"
            TithiGroup.JAYA -> "विजयी"
            TithiGroup.RIKTA -> "रिक्त"
            TithiGroup.PURNA -> "पूर्ण"
        }
    }
}

/**
 * Get localized display name for Yoga (Panchanga)
 */
fun Yoga.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> this.sanskrit
    }
}

/**
 * Get localized display name for YogaNature
 */
fun YogaNature.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> when (this) {
            YogaNature.AUSPICIOUS -> "शुभ"
            YogaNature.INAUSPICIOUS -> "अशुभ"
            YogaNature.MIXED -> "मिश्रित"
        }
    }
}

/**
 * Get localized display name for Karana
 */
fun Karana.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> this.sanskrit
    }
}

/**
 * Get localized display name for KaranaType
 */
fun KaranaType.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> when (this) {
            KaranaType.FIXED -> "स्थिर"
            KaranaType.MOVABLE -> "चर"
        }
    }
}

/**
 * Get localized display name for Vara
 */
fun Vara.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> this.sanskrit
    }
}

/**
 * Get localized display name for Paksha
 */
fun Paksha.getLocalizedName(language: Language): String {
    return when (language) {
        Language.ENGLISH -> this.displayName
        Language.NEPALI -> this.sanskrit
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

// ============================================
// YOGA CALCULATOR EXTENSIONS
// ============================================

/**
 * Get localized display name for YogaCategory
 */
fun YogaCalculator.YogaCategory.getLocalizedName(language: Language): String {
    return when (this) {
        YogaCalculator.YogaCategory.RAJA_YOGA -> StringResources.get(StringKey.YOGA_CAT_RAJA, language)
        YogaCalculator.YogaCategory.DHANA_YOGA -> StringResources.get(StringKey.YOGA_CAT_DHANA, language)
        YogaCalculator.YogaCategory.MAHAPURUSHA_YOGA -> StringResources.get(StringKey.YOGA_CAT_PANCHA_MAHAPURUSHA, language)
        YogaCalculator.YogaCategory.NABHASA_YOGA -> StringResources.get(StringKey.YOGA_CAT_NABHASA, language)
        YogaCalculator.YogaCategory.CHANDRA_YOGA -> StringResources.get(StringKey.YOGA_CAT_CHANDRA, language)
        YogaCalculator.YogaCategory.SOLAR_YOGA -> StringResources.get(StringKey.YOGA_CAT_SOLAR, language)
        YogaCalculator.YogaCategory.NEGATIVE_YOGA -> StringResources.get(StringKey.YOGA_CAT_NEGATIVE, language)
        YogaCalculator.YogaCategory.SPECIAL_YOGA -> StringResources.get(StringKey.YOGA_CAT_SPECIAL, language)
    }
}

/**
 * Get localized description for YogaCategory
 */
fun YogaCalculator.YogaCategory.getLocalizedDescription(language: Language): String {
    return when (this) {
        YogaCalculator.YogaCategory.RAJA_YOGA -> StringResources.get(StringKey.YOGA_CAT_RAJA_DESC, language)
        YogaCalculator.YogaCategory.DHANA_YOGA -> StringResources.get(StringKey.YOGA_CAT_DHANA_DESC, language)
        YogaCalculator.YogaCategory.MAHAPURUSHA_YOGA -> StringResources.get(StringKey.YOGA_CAT_PANCHA_MAHAPURUSHA_DESC, language)
        YogaCalculator.YogaCategory.NABHASA_YOGA -> StringResources.get(StringKey.YOGA_CAT_NABHASA_DESC, language)
        YogaCalculator.YogaCategory.CHANDRA_YOGA -> StringResources.get(StringKey.YOGA_CAT_CHANDRA_DESC, language)
        YogaCalculator.YogaCategory.SOLAR_YOGA -> StringResources.get(StringKey.YOGA_CAT_SOLAR_DESC, language)
        YogaCalculator.YogaCategory.NEGATIVE_YOGA -> StringResources.get(StringKey.YOGA_CAT_NEGATIVE_DESC, language)
        YogaCalculator.YogaCategory.SPECIAL_YOGA -> StringResources.get(StringKey.YOGA_CAT_SPECIAL_DESC, language)
    }
}

/**
 * Get localized display name for YogaStrength
 */
fun YogaCalculator.YogaStrength.getLocalizedName(language: Language): String {
    return when (this) {
        YogaCalculator.YogaStrength.EXTREMELY_STRONG -> StringResources.get(StringKey.YOGA_STRENGTH_EXTREMELY_STRONG, language)
        YogaCalculator.YogaStrength.STRONG -> StringResources.get(StringKey.YOGA_STRENGTH_STRONG, language)
        YogaCalculator.YogaStrength.MODERATE -> StringResources.get(StringKey.YOGA_STRENGTH_MODERATE, language)
        YogaCalculator.YogaStrength.WEAK -> StringResources.get(StringKey.YOGA_STRENGTH_WEAK, language)
        YogaCalculator.YogaStrength.VERY_WEAK -> StringResources.get(StringKey.YOGA_STRENGTH_VERY_WEAK, language)
    }
}

// ============================================
// REMEDIES CALCULATOR EXTENSIONS
// ============================================

/**
 * Get localized display name for PlanetaryStrength
 */
fun RemediesCalculator.PlanetaryStrength.getLocalizedName(language: Language): String {
    return when (this) {
        RemediesCalculator.PlanetaryStrength.VERY_STRONG -> StringResources.get(StringKey.PLANETARY_STRENGTH_VERY_STRONG, language)
        RemediesCalculator.PlanetaryStrength.STRONG -> StringResources.get(StringKey.PLANETARY_STRENGTH_STRONG, language)
        RemediesCalculator.PlanetaryStrength.MODERATE -> StringResources.get(StringKey.PLANETARY_STRENGTH_MODERATE, language)
        RemediesCalculator.PlanetaryStrength.WEAK -> StringResources.get(StringKey.PLANETARY_STRENGTH_WEAK, language)
        RemediesCalculator.PlanetaryStrength.VERY_WEAK -> StringResources.get(StringKey.PLANETARY_STRENGTH_VERY_WEAK, language)
        RemediesCalculator.PlanetaryStrength.AFFLICTED -> StringResources.get(StringKey.PLANETARY_STRENGTH_AFFLICTED, language)
    }
}

/**
 * Get localized display name for Shadbala StrengthRating
 */
fun StrengthRating.getLocalizedName(language: Language): String {
    return when (this) {
        StrengthRating.EXTREMELY_WEAK -> StringResources.get(StringKey.SHADBALA_EXTREMELY_WEAK, language)
        StrengthRating.WEAK -> StringResources.get(StringKey.SHADBALA_WEAK, language)
        StrengthRating.BELOW_AVERAGE -> StringResources.get(StringKey.SHADBALA_BELOW_AVERAGE, language)
        StrengthRating.AVERAGE -> StringResources.get(StringKey.SHADBALA_AVERAGE, language)
        StrengthRating.ABOVE_AVERAGE -> StringResources.get(StringKey.SHADBALA_ABOVE_AVERAGE, language)
        StrengthRating.STRONG -> StringResources.get(StringKey.SHADBALA_STRONG, language)
        StrengthRating.VERY_STRONG -> StringResources.get(StringKey.SHADBALA_VERY_STRONG, language)
        StrengthRating.EXTREMELY_STRONG -> StringResources.get(StringKey.SHADBALA_EXTREMELY_STRONG, language)
    }
}

/**
 * Get localized display name for CombustionStatus
 */
fun RetrogradeCombustionCalculator.CombustionStatus.getLocalizedName(language: Language): String {
    return when (this) {
        RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST -> StringResources.get(StringKey.COMBUSTION_NOT_COMBUST, language)
        RetrogradeCombustionCalculator.CombustionStatus.APPROACHING -> StringResources.get(StringKey.COMBUSTION_APPROACHING, language)
        RetrogradeCombustionCalculator.CombustionStatus.COMBUST -> StringResources.get(StringKey.COMBUSTION_COMBUST, language)
        RetrogradeCombustionCalculator.CombustionStatus.DEEP_COMBUST -> StringResources.get(StringKey.COMBUSTION_DEEP_COMBUST, language)
        RetrogradeCombustionCalculator.CombustionStatus.CAZIMI -> StringResources.get(StringKey.COMBUSTION_CAZIMI, language)
        RetrogradeCombustionCalculator.CombustionStatus.SEPARATING -> StringResources.get(StringKey.COMBUSTION_SEPARATING, language)
    }
}

/**
 * Get localized display name for RemedyCategory
 */
fun RemediesCalculator.RemedyCategory.getLocalizedName(language: Language): String {
    return when (this) {
        RemediesCalculator.RemedyCategory.GEMSTONE -> StringResources.get(StringKey.REMEDY_CAT_GEMSTONE, language)
        RemediesCalculator.RemedyCategory.MANTRA -> StringResources.get(StringKey.REMEDY_CAT_MANTRA, language)
        RemediesCalculator.RemedyCategory.YANTRA -> StringResources.get(StringKey.REMEDY_CAT_YANTRA, language)
        RemediesCalculator.RemedyCategory.CHARITY -> StringResources.get(StringKey.REMEDY_CAT_CHARITY, language)
        RemediesCalculator.RemedyCategory.FASTING -> StringResources.get(StringKey.REMEDY_CAT_FASTING, language)
        RemediesCalculator.RemedyCategory.COLOR -> StringResources.get(StringKey.REMEDY_CAT_COLOR, language)
        RemediesCalculator.RemedyCategory.METAL -> StringResources.get(StringKey.REMEDY_CAT_METAL, language)
        RemediesCalculator.RemedyCategory.RUDRAKSHA -> StringResources.get(StringKey.REMEDY_CAT_RUDRAKSHA, language)
        RemediesCalculator.RemedyCategory.DEITY -> StringResources.get(StringKey.REMEDY_CAT_DEITY, language)
        RemediesCalculator.RemedyCategory.LIFESTYLE -> StringResources.get(StringKey.REMEDY_CAT_LIFESTYLE, language)
    }
}

/**
 * Get localized display name for RemedyPriority
 */
fun RemediesCalculator.RemedyPriority.getLocalizedName(language: Language): String {
    return when (this) {
        RemediesCalculator.RemedyPriority.ESSENTIAL -> StringResources.get(StringKey.REMEDY_PRIORITY_ESSENTIAL, language)
        RemediesCalculator.RemedyPriority.HIGHLY_RECOMMENDED -> StringResources.get(StringKey.REMEDY_PRIORITY_HIGHLY_RECOMMENDED, language)
        RemediesCalculator.RemedyPriority.RECOMMENDED -> StringResources.get(StringKey.REMEDY_PRIORITY_RECOMMENDED, language)
        RemediesCalculator.RemedyPriority.OPTIONAL -> StringResources.get(StringKey.REMEDY_PRIORITY_OPTIONAL, language)
    }
}

// ============================================
// MATCHMAKING CALCULATOR EXTENSIONS
// ============================================

// NOTE: Varna, Vashya, Gana, Yoni, Nadi, Rajju, ManglikDosha, and CompatibilityRating
// already have getLocalizedName() and getLocalizedDescription() methods defined
// as member functions in MatchmakingModels.kt. Do not duplicate them here to avoid
// overload resolution ambiguity errors.

/**
 * Get localized display name for Yoni animal
 * This is a utility function since Yoni doesn't have a member function for localized animal names.
 */
fun getYoniLocalizedAnimalName(yoni: Yoni, language: Language): String {
    return when (yoni.animal) {
        "Horse" -> StringResources.get(StringKey.YONI_HORSE, language)
        "Elephant" -> StringResources.get(StringKey.YONI_ELEPHANT, language)
        "Sheep" -> StringResources.get(StringKey.YONI_SHEEP, language)
        "Serpent" -> StringResources.get(StringKey.YONI_SERPENT, language)
        "Dog" -> StringResources.get(StringKey.YONI_DOG, language)
        "Cat" -> StringResources.get(StringKey.YONI_CAT, language)
        "Rat" -> StringResources.get(StringKey.YONI_RAT, language)
        "Cow" -> StringResources.get(StringKey.YONI_COW, language)
        "Buffalo" -> StringResources.get(StringKey.YONI_BUFFALO, language)
        "Tiger" -> StringResources.get(StringKey.YONI_TIGER, language)
        "Deer" -> StringResources.get(StringKey.YONI_DEER, language)
        "Monkey" -> StringResources.get(StringKey.YONI_MONKEY, language)
        "Mongoose" -> StringResources.get(StringKey.YONI_MONGOOSE, language)
        "Lion" -> StringResources.get(StringKey.YONI_LION, language)
        else -> yoni.animal
    }
}

/**
 * Get localized house signification
 */
fun getHouseSignification(house: Int, language: Language): String {
    return when (house) {
        1 -> StringResources.get(StringKey.HOUSE_1_SIGNIFICATION, language)
        2 -> StringResources.get(StringKey.HOUSE_2_SIGNIFICATION, language)
        3 -> StringResources.get(StringKey.HOUSE_3_SIGNIFICATION, language)
        4 -> StringResources.get(StringKey.HOUSE_4_SIGNIFICATION, language)
        5 -> StringResources.get(StringKey.HOUSE_5_SIGNIFICATION, language)
        6 -> StringResources.get(StringKey.HOUSE_6_SIGNIFICATION, language)
        7 -> StringResources.get(StringKey.HOUSE_7_SIGNIFICATION, language)
        8 -> StringResources.get(StringKey.HOUSE_8_SIGNIFICATION, language)
        9 -> StringResources.get(StringKey.HOUSE_9_SIGNIFICATION, language)
        10 -> StringResources.get(StringKey.HOUSE_10_SIGNIFICATION, language)
        11 -> StringResources.get(StringKey.HOUSE_11_SIGNIFICATION, language)
        12 -> StringResources.get(StringKey.HOUSE_12_SIGNIFICATION, language)
        else -> ""
    }
}

/**
 * Get localized Choghadiya name
 */
fun getChoghadiyaName(choghadiya: String, language: Language): String {
    return when (choghadiya.lowercase()) {
        "amrit" -> StringResources.get(StringKey.CHOGHADIYA_AMRIT, language)
        "shubh" -> StringResources.get(StringKey.CHOGHADIYA_SHUBH, language)
        "labh" -> StringResources.get(StringKey.CHOGHADIYA_LABH, language)
        "char" -> StringResources.get(StringKey.CHOGHADIYA_CHAR, language)
        "rog" -> StringResources.get(StringKey.CHOGHADIYA_ROG, language)
        "kaal" -> StringResources.get(StringKey.CHOGHADIYA_KAAL, language)
        "udveg" -> StringResources.get(StringKey.CHOGHADIYA_UDVEG, language)
        else -> choghadiya
    }
}
