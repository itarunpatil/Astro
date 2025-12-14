package com.astro.storm.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringKeyMatch
import com.astro.storm.data.localization.StringKeyDosha
import com.astro.storm.data.localization.StringKeyAnalysis
import com.astro.storm.data.localization.StringResources

/**
 * Matchmaking (Kundli Milan) Data Models
 *
 * Contains all enums and data classes used in Vedic matchmaking analysis
 * based on Ashtakoota (8 Guna) system from classical texts:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Muhurta Chintamani
 * - Jataka Parijata
 */

// ============================================
// Varna (Spiritual/Ego Classification)
// ============================================

/**
 * Varna represents spiritual compatibility and ego harmony.
 * Based on the element of the Moon sign.
 *
 * @property value Hierarchical value (4=highest, 1=lowest)
 * @property displayName English display name
 */
enum class Varna(val value: Int, val displayName: String) {
    BRAHMIN(4, "Brahmin"),
    KSHATRIYA(3, "Kshatriya"),
    VAISHYA(2, "Vaishya"),
    SHUDRA(1, "Shudra");

    fun getLocalizedName(language: Language): String = when (this) {
        BRAHMIN -> StringResources.get(StringKeyMatch.VARNA_BRAHMIN, language)
        KSHATRIYA -> StringResources.get(StringKeyMatch.VARNA_KSHATRIYA, language)
        VAISHYA -> StringResources.get(StringKeyMatch.VARNA_VAISHYA, language)
        SHUDRA -> StringResources.get(StringKeyMatch.VARNA_SHUDRA, language)
    }

    companion object {
        /**
         * Get Varna from zodiac sign based on classical mapping
         * Water signs: Brahmin, Fire signs: Kshatriya, Earth signs: Vaishya, Air signs: Shudra
         */
        fun fromZodiacSign(sign: ZodiacSign): Varna = when (sign) {
            ZodiacSign.CANCER, ZodiacSign.SCORPIO, ZodiacSign.PISCES -> BRAHMIN
            ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS -> KSHATRIYA
            ZodiacSign.TAURUS, ZodiacSign.VIRGO, ZodiacSign.CAPRICORN -> VAISHYA
            ZodiacSign.GEMINI, ZodiacSign.LIBRA, ZodiacSign.AQUARIUS -> SHUDRA
        }
    }
}

// ============================================
// Vashya (Mutual Attraction/Control)
// ============================================

/**
 * Vashya indicates the degree of magnetic control or attraction.
 * Determines influence dynamics in the relationship.
 */
enum class Vashya(val displayName: String) {
    CHATUSHPADA("Quadruped"),
    MANAVA("Human"),
    JALACHARA("Aquatic"),
    VANACHARA("Wild"),
    KEETA("Insect");

    fun getLocalizedName(language: Language): String = when (this) {
        CHATUSHPADA -> StringResources.get(StringKeyMatch.VASHYA_CHATUSHPADA, language)
        MANAVA -> StringResources.get(StringKeyMatch.VASHYA_MANAVA, language)
        JALACHARA -> StringResources.get(StringKeyMatch.VASHYA_JALACHARA, language)
        VANACHARA -> StringResources.get(StringKeyMatch.VASHYA_VANACHARA, language)
        KEETA -> StringResources.get(StringKeyMatch.VASHYA_KEETA, language)
    }

    companion object {
        fun fromZodiacSign(sign: ZodiacSign): Vashya = when (sign) {
            ZodiacSign.ARIES -> CHATUSHPADA
            ZodiacSign.TAURUS -> CHATUSHPADA
            ZodiacSign.GEMINI -> MANAVA
            ZodiacSign.CANCER -> JALACHARA
            ZodiacSign.LEO -> VANACHARA
            ZodiacSign.VIRGO -> MANAVA
            ZodiacSign.LIBRA -> MANAVA
            ZodiacSign.SCORPIO -> KEETA
            ZodiacSign.SAGITTARIUS -> MANAVA
            ZodiacSign.CAPRICORN -> JALACHARA
            ZodiacSign.AQUARIUS -> MANAVA
            ZodiacSign.PISCES -> JALACHARA
        }

        /** Vashya control relationships */
        val controlPairs: Map<Vashya, Set<Vashya>> = mapOf(
            MANAVA to setOf(CHATUSHPADA, JALACHARA),
            VANACHARA to setOf(CHATUSHPADA),
            CHATUSHPADA to setOf(JALACHARA)
        )

        /** Enemy Vashya pairs */
        val enemyPairs: Set<Set<Vashya>> = setOf(
            setOf(MANAVA, VANACHARA),
            setOf(CHATUSHPADA, VANACHARA)
        )
    }
}

// ============================================
// Gana (Temperament)
// ============================================

/**
 * Gana represents temperament and fundamental nature.
 * Based on Nakshatra classification.
 */
enum class Gana(val displayName: String, val description: String) {
    DEVA("Deva", "Divine - Sattvik, gentle, spiritual"),
    MANUSHYA("Manushya", "Human - Rajasik, balanced, worldly"),
    RAKSHASA("Rakshasa", "Demon - Tamasik, aggressive, dominant");

    fun getLocalizedName(language: Language): String = when (this) {
        DEVA -> StringResources.get(StringKeyMatch.GANA_DEVA, language)
        MANUSHYA -> StringResources.get(StringKeyMatch.GANA_MANUSHYA, language)
        RAKSHASA -> StringResources.get(StringKeyMatch.GANA_RAKSHASA, language)
    }

    fun getLocalizedDescription(language: Language): String = when (this) {
        DEVA -> StringResources.get(StringKeyMatch.GANA_DEVA_DESC, language)
        MANUSHYA -> StringResources.get(StringKeyMatch.GANA_MANUSHYA_DESC, language)
        RAKSHASA -> StringResources.get(StringKeyMatch.GANA_RAKSHASA_DESC, language)
    }

    companion object {
        fun fromNakshatra(nakshatra: Nakshatra): Gana = when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.MRIGASHIRA, Nakshatra.PUNARVASU,
            Nakshatra.PUSHYA, Nakshatra.HASTA, Nakshatra.SWATI,
            Nakshatra.ANURADHA, Nakshatra.SHRAVANA, Nakshatra.REVATI -> DEVA

            Nakshatra.BHARANI, Nakshatra.ROHINI, Nakshatra.ARDRA,
            Nakshatra.PURVA_PHALGUNI, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.PURVA_ASHADHA, Nakshatra.UTTARA_ASHADHA,
            Nakshatra.PURVA_BHADRAPADA, Nakshatra.UTTARA_BHADRAPADA -> MANUSHYA

            Nakshatra.KRITTIKA, Nakshatra.ASHLESHA, Nakshatra.MAGHA,
            Nakshatra.CHITRA, Nakshatra.VISHAKHA, Nakshatra.JYESHTHA,
            Nakshatra.MULA, Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA -> RAKSHASA
        }
    }
}

// ============================================
// Yoni (Physical/Sexual Compatibility)
// ============================================

enum class YoniGender { MALE, FEMALE }

/**
 * Yoni represents sexual and physical compatibility.
 * Each Nakshatra is assigned an animal nature with male/female classification.
 */
enum class Yoni(val animal: String, val gender: YoniGender, val groupId: Int) {
    ASHWA_MALE("Horse", YoniGender.MALE, 1),
    ASHWA_FEMALE("Horse", YoniGender.FEMALE, 1),
    GAJA_MALE("Elephant", YoniGender.MALE, 2),
    GAJA_FEMALE("Elephant", YoniGender.FEMALE, 2),
    MESHA_MALE("Sheep", YoniGender.MALE, 3),
    MESHA_FEMALE("Sheep", YoniGender.FEMALE, 3),
    SARPA_MALE("Serpent", YoniGender.MALE, 4),
    SARPA_FEMALE("Serpent", YoniGender.FEMALE, 4),
    SHWAN_MALE("Dog", YoniGender.MALE, 5),
    SHWAN_FEMALE("Dog", YoniGender.FEMALE, 5),
    MARJAR_MALE("Cat", YoniGender.MALE, 6),
    MARJAR_FEMALE("Cat", YoniGender.FEMALE, 6),
    MUSHAK_MALE("Rat", YoniGender.MALE, 7),
    MUSHAK_FEMALE("Rat", YoniGender.FEMALE, 7),
    GAU_MALE("Cow", YoniGender.MALE, 8),
    GAU_FEMALE("Cow", YoniGender.FEMALE, 8),
    MAHISH_MALE("Buffalo", YoniGender.MALE, 9),
    MAHISH_FEMALE("Buffalo", YoniGender.FEMALE, 9),
    VYAGHRA_MALE("Tiger", YoniGender.MALE, 10),
    VYAGHRA_FEMALE("Tiger", YoniGender.FEMALE, 10),
    MRIGA_MALE("Deer", YoniGender.MALE, 11),
    MRIGA_FEMALE("Deer", YoniGender.FEMALE, 11),
    VANAR_MALE("Monkey", YoniGender.MALE, 12),
    VANAR_FEMALE("Monkey", YoniGender.FEMALE, 12),
    NAKUL_MALE("Mongoose", YoniGender.MALE, 13),
    NAKUL_FEMALE("Mongoose", YoniGender.FEMALE, 13),
    SIMHA_MALE("Lion", YoniGender.MALE, 14),
    SIMHA_FEMALE("Lion", YoniGender.FEMALE, 14);

    companion object {
        fun fromNakshatra(nakshatra: Nakshatra): Yoni = when (nakshatra) {
            Nakshatra.ASHWINI -> ASHWA_MALE
            Nakshatra.SHATABHISHA -> ASHWA_FEMALE
            Nakshatra.BHARANI -> GAJA_MALE
            Nakshatra.REVATI -> GAJA_FEMALE
            Nakshatra.PUSHYA -> MESHA_MALE
            Nakshatra.KRITTIKA -> MESHA_FEMALE
            Nakshatra.ROHINI -> SARPA_MALE
            Nakshatra.MRIGASHIRA -> SARPA_FEMALE
            Nakshatra.MULA -> SHWAN_MALE
            Nakshatra.ARDRA -> SHWAN_FEMALE
            Nakshatra.ASHLESHA -> MARJAR_MALE
            Nakshatra.PUNARVASU -> MARJAR_FEMALE
            Nakshatra.MAGHA -> MUSHAK_MALE
            Nakshatra.PURVA_PHALGUNI -> MUSHAK_FEMALE
            Nakshatra.UTTARA_PHALGUNI -> GAU_MALE
            Nakshatra.UTTARA_BHADRAPADA -> GAU_FEMALE
            Nakshatra.SWATI -> MAHISH_MALE
            Nakshatra.HASTA -> MAHISH_FEMALE
            Nakshatra.VISHAKHA -> VYAGHRA_MALE
            Nakshatra.CHITRA -> VYAGHRA_FEMALE
            Nakshatra.JYESHTHA -> MRIGA_MALE
            Nakshatra.ANURADHA -> MRIGA_FEMALE
            Nakshatra.PURVA_ASHADHA -> VANAR_MALE
            Nakshatra.SHRAVANA -> VANAR_FEMALE
            Nakshatra.UTTARA_ASHADHA -> NAKUL_MALE
            Nakshatra.PURVA_BHADRAPADA -> SIMHA_MALE
            Nakshatra.DHANISHTHA -> SIMHA_FEMALE
        }

        /** Enemy Yoni pairs (group IDs) */
        val enemyPairs: Set<Set<Int>> = setOf(
            setOf(1, 9),   // Horse - Buffalo
            setOf(2, 14),  // Elephant - Lion
            setOf(3, 12),  // Sheep - Monkey
            setOf(4, 13),  // Serpent - Mongoose
            setOf(5, 11),  // Dog - Deer
            setOf(6, 7),   // Cat - Rat
            setOf(8, 10)   // Cow - Tiger
        )

        /** Friendly Yoni groups (group IDs) */
        val friendlyGroups: List<Set<Int>> = listOf(
            setOf(1, 2, 3),     // Horse, Elephant, Sheep
            setOf(4, 5, 6),     // Serpent, Dog, Cat
            setOf(8, 9, 11),    // Cow, Buffalo, Deer
            setOf(12, 14)       // Monkey, Lion
        )
    }
}

// ============================================
// Nadi (Health/Genetic Compatibility)
// ============================================

/**
 * Nadi represents health and genetic compatibility.
 * Most important factor in Ashtakoota carrying 8 points.
 * Same Nadi can cause health issues and affect progeny.
 */
enum class Nadi(val displayName: String, val description: String) {
    ADI("Adi (Vata)", "Beginning - Wind element, controls movement and nervous system"),
    MADHYA("Madhya (Pitta)", "Middle - Fire element, controls digestion and metabolism"),
    ANTYA("Antya (Kapha)", "End - Water element, controls structure and lubrication");

    fun getLocalizedName(language: Language): String = when (this) {
        ADI -> StringResources.get(StringKeyMatch.NADI_ADI, language)
        MADHYA -> StringResources.get(StringKeyMatch.NADI_MADHYA, language)
        ANTYA -> StringResources.get(StringKeyMatch.NADI_ANTYA, language)
    }

    fun getLocalizedDescription(language: Language): String = when (this) {
        ADI -> StringResources.get(StringKeyMatch.NADI_ADI_DESC, language)
        MADHYA -> StringResources.get(StringKeyMatch.NADI_MADHYA_DESC, language)
        ANTYA -> StringResources.get(StringKeyMatch.NADI_ANTYA_DESC, language)
    }

    companion object {
        fun fromNakshatra(nakshatra: Nakshatra): Nadi = when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.ARDRA, Nakshatra.PUNARVASU,
            Nakshatra.UTTARA_PHALGUNI, Nakshatra.HASTA, Nakshatra.JYESHTHA,
            Nakshatra.MULA, Nakshatra.SHATABHISHA, Nakshatra.PURVA_BHADRAPADA -> ADI

            Nakshatra.BHARANI, Nakshatra.MRIGASHIRA, Nakshatra.PUSHYA,
            Nakshatra.PURVA_PHALGUNI, Nakshatra.CHITRA, Nakshatra.ANURADHA,
            Nakshatra.PURVA_ASHADHA, Nakshatra.DHANISHTHA, Nakshatra.UTTARA_BHADRAPADA -> MADHYA

            Nakshatra.KRITTIKA, Nakshatra.ROHINI, Nakshatra.ASHLESHA,
            Nakshatra.MAGHA, Nakshatra.SWATI, Nakshatra.VISHAKHA,
            Nakshatra.UTTARA_ASHADHA, Nakshatra.SHRAVANA, Nakshatra.REVATI -> ANTYA
        }

        /**
         * Nakshatra pairs that cancel Nadi dosha per classical texts
         */
        val cancellingPairs: List<Set<Nakshatra>> = listOf(
            // Adi Nadi cancelling pairs
            setOf(Nakshatra.ASHWINI, Nakshatra.SHATABHISHA),
            setOf(Nakshatra.ARDRA, Nakshatra.PUNARVASU),
            setOf(Nakshatra.UTTARA_PHALGUNI, Nakshatra.HASTA),
            setOf(Nakshatra.JYESHTHA, Nakshatra.MULA),
            setOf(Nakshatra.PURVA_BHADRAPADA, Nakshatra.UTTARA_BHADRAPADA),
            // Madhya Nadi cancelling pairs
            setOf(Nakshatra.BHARANI, Nakshatra.REVATI),
            setOf(Nakshatra.MRIGASHIRA, Nakshatra.CHITRA),
            setOf(Nakshatra.PUSHYA, Nakshatra.UTTARA_ASHADHA),
            setOf(Nakshatra.PURVA_PHALGUNI, Nakshatra.ANURADHA),
            setOf(Nakshatra.PURVA_ASHADHA, Nakshatra.DHANISHTHA),
            // Antya Nadi cancelling pairs
            setOf(Nakshatra.KRITTIKA, Nakshatra.VISHAKHA),
            setOf(Nakshatra.ROHINI, Nakshatra.SWATI),
            setOf(Nakshatra.ASHLESHA, Nakshatra.MAGHA),
            setOf(Nakshatra.UTTARA_ASHADHA, Nakshatra.SHRAVANA),
            setOf(Nakshatra.REVATI, Nakshatra.ASHWINI)
        )
    }
}

// ============================================
// Rajju (Cosmic Bond/Body Part)
// ============================================

/**
 * Rajju represents the cosmic bond, associated with body parts.
 * Same Rajju can cause problems related to that body part.
 */
enum class Rajju(val displayName: String, val bodyPart: String) {
    PADA("Pada Rajju", "Feet"),
    KATI("Kati Rajju", "Waist"),
    NABHI("Nabhi Rajju", "Navel"),
    KANTHA("Kantha Rajju", "Neck"),
    SIRO("Siro Rajju", "Head");

    fun getLocalizedName(language: Language): String = when (this) {
        PADA -> StringResources.get(StringKeyMatch.RAJJU_PADA, language)
        KATI -> StringResources.get(StringKeyMatch.RAJJU_KATI, language)
        NABHI -> StringResources.get(StringKeyMatch.RAJJU_NABHI, language)
        KANTHA -> StringResources.get(StringKeyMatch.RAJJU_KANTHA, language)
        SIRO -> StringResources.get(StringKeyMatch.RAJJU_SIRO, language)
    }

    fun getLocalizedBodyPart(language: Language): String = when (this) {
        PADA -> StringResources.get(StringKeyMatch.RAJJU_PADA_BODY, language)
        KATI -> StringResources.get(StringKeyMatch.RAJJU_KATI_BODY, language)
        NABHI -> StringResources.get(StringKeyMatch.RAJJU_NABHI_BODY, language)
        KANTHA -> StringResources.get(StringKeyMatch.RAJJU_KANTHA_BODY, language)
        SIRO -> StringResources.get(StringKeyMatch.RAJJU_SIRO_BODY, language)
    }

    /**
     * Get warning message for same Rajju dosha
     */
    fun getWarning(language: Language): String = when (this) {
        SIRO -> StringResources.get(StringKeyMatch.RAJJU_SIRO_WARNING, language)
        KANTHA -> StringResources.get(StringKeyMatch.RAJJU_KANTHA_WARNING, language)
        NABHI -> StringResources.get(StringKeyMatch.RAJJU_NABHI_WARNING, language)
        KATI -> StringResources.get(StringKeyMatch.RAJJU_KATI_WARNING, language)
        PADA -> StringResources.get(StringKeyMatch.RAJJU_PADA_WARNING, language)
    }

    /**
     * Get warning message (English fallback)
     */
    fun getWarningEnglish(): String = when (this) {
        SIRO -> "Most serious - affects longevity of spouse"
        KANTHA -> "May cause health issues to both"
        NABHI -> "May affect children"
        KATI -> "May cause financial difficulties"
        PADA -> "May cause wandering tendencies"
    }

    companion object {
        fun fromNakshatra(nakshatra: Nakshatra): Rajju = when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.ASHLESHA, Nakshatra.MAGHA,
            Nakshatra.JYESHTHA, Nakshatra.MULA, Nakshatra.REVATI -> PADA

            Nakshatra.BHARANI, Nakshatra.PUSHYA, Nakshatra.PURVA_PHALGUNI,
            Nakshatra.ANURADHA, Nakshatra.PURVA_ASHADHA, Nakshatra.UTTARA_BHADRAPADA -> KATI

            Nakshatra.KRITTIKA, Nakshatra.PUNARVASU, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.VISHAKHA, Nakshatra.UTTARA_ASHADHA, Nakshatra.PURVA_BHADRAPADA -> NABHI

            Nakshatra.ROHINI, Nakshatra.ARDRA, Nakshatra.HASTA,
            Nakshatra.SWATI, Nakshatra.SHATABHISHA, Nakshatra.SHRAVANA -> KANTHA

            Nakshatra.MRIGASHIRA, Nakshatra.CHITRA, Nakshatra.DHANISHTHA -> SIRO
        }
    }
}

/**
 * Rajju Arudha (direction) - Ascending or Descending
 * Same Rajju + Same Arudha is most problematic
 */
enum class RajjuArudha(val displayName: String) {
    ASCENDING("Aarohana (Ascending)"),
    DESCENDING("Avarohana (Descending)");

    companion object {
        fun fromNakshatra(nakshatra: Nakshatra): RajjuArudha = when (nakshatra) {
            // Pada Rajju
            Nakshatra.ASHWINI, Nakshatra.MAGHA -> ASCENDING
            Nakshatra.ASHLESHA, Nakshatra.JYESHTHA, Nakshatra.MULA, Nakshatra.REVATI -> DESCENDING
            // Kati Rajju
            Nakshatra.BHARANI, Nakshatra.PURVA_PHALGUNI, Nakshatra.PURVA_ASHADHA -> ASCENDING
            Nakshatra.PUSHYA, Nakshatra.ANURADHA, Nakshatra.UTTARA_BHADRAPADA -> DESCENDING
            // Nabhi Rajju
            Nakshatra.KRITTIKA, Nakshatra.UTTARA_PHALGUNI, Nakshatra.UTTARA_ASHADHA -> ASCENDING
            Nakshatra.PUNARVASU, Nakshatra.VISHAKHA, Nakshatra.PURVA_BHADRAPADA -> DESCENDING
            // Kantha Rajju
            Nakshatra.ROHINI, Nakshatra.HASTA, Nakshatra.SHRAVANA -> ASCENDING
            Nakshatra.ARDRA, Nakshatra.SWATI, Nakshatra.SHATABHISHA -> DESCENDING
            // Siro Rajju
            Nakshatra.MRIGASHIRA -> ASCENDING
            Nakshatra.CHITRA, Nakshatra.DHANISHTHA -> DESCENDING
        }
    }
}

// ============================================
// Manglik Dosha
// ============================================

/**
 * Manglik Dosha severity levels
 */
enum class ManglikDosha(val displayName: String, val severity: Int) {
    NONE("No Manglik Dosha", 0),
    PARTIAL("Partial Manglik", 1),
    FULL("Full Manglik", 2),
    DOUBLE("Double Manglik (Severe)", 3);

    fun getLocalizedName(language: Language): String = when (this) {
        NONE -> StringResources.get(StringKeyMatch.MANGLIK_NONE, language)
        PARTIAL -> StringResources.get(StringKeyMatch.MANGLIK_PARTIAL, language)
        FULL -> StringResources.get(StringKeyMatch.MANGLIK_FULL, language)
        DOUBLE -> StringResources.get(StringKeyMatch.MANGLIK_DOUBLE, language)
    }
}

// ============================================
// Compatibility Rating
// ============================================

/**
 * Overall compatibility rating based on total Guna score
 */
enum class CompatibilityRating(val displayName: String, val description: String) {
    EXCELLENT("Excellent Match", "Highly recommended for marriage. Strong compatibility across all factors with harmonious planetary alignments."),
    GOOD("Good Match", "Recommended. Good overall compatibility with minor differences that can be easily managed."),
    AVERAGE("Average Match", "Acceptable with some remedies. Moderate compatibility requiring mutual understanding and effort."),
    BELOW_AVERAGE("Below Average", "Caution advised. Several compatibility issues that need addressing through remedies and counseling."),
    POOR("Poor Match", "Not recommended. Significant compatibility challenges that may cause ongoing difficulties.");

    fun getLocalizedName(language: Language): String = when (this) {
        EXCELLENT -> StringResources.get(StringKeyMatch.COMPAT_EXCELLENT, language)
        GOOD -> StringResources.get(StringKeyMatch.COMPAT_GOOD, language)
        AVERAGE -> StringResources.get(StringKeyMatch.COMPAT_AVERAGE, language)
        BELOW_AVERAGE -> StringResources.get(StringKeyDosha.COMPAT_BELOW_AVG, language)
        POOR -> StringResources.get(StringKeyMatch.COMPAT_POOR, language)
    }

    fun getLocalizedDescription(language: Language): String = when (this) {
        EXCELLENT -> StringResources.get(StringKeyMatch.COMPAT_EXCELLENT_DESC, language)
        GOOD -> StringResources.get(StringKeyMatch.COMPAT_GOOD_DESC, language)
        AVERAGE -> StringResources.get(StringKeyMatch.COMPAT_AVERAGE_DESC, language)
        BELOW_AVERAGE -> StringResources.get(StringKeyDosha.COMPAT_BELOW_AVG_DESC, language)
        POOR -> StringResources.get(StringKeyMatch.COMPAT_POOR_DESC, language)
    }

    companion object {
        const val EXCELLENT_THRESHOLD = 28.0
        const val GOOD_THRESHOLD = 21.0
        const val AVERAGE_THRESHOLD = 18.0
        const val POOR_THRESHOLD = 14.0

        fun fromScore(score: Double, nadiScore: Double = 8.0, bhakootScore: Double = 7.0): CompatibilityRating {
            // Both Nadi and Bhakoot zero is very serious
            if (nadiScore == 0.0 && bhakootScore == 0.0 && score < GOOD_THRESHOLD) {
                return POOR
            }
            return when {
                score >= EXCELLENT_THRESHOLD -> EXCELLENT
                score >= GOOD_THRESHOLD -> GOOD
                score >= AVERAGE_THRESHOLD -> AVERAGE
                score >= POOR_THRESHOLD -> BELOW_AVERAGE
                else -> POOR
            }
        }
    }
}

// ============================================
// Data Classes for Analysis Results
// ============================================

/**
 * Enumeration of the 8 Gunas (Ashtakoota) for type-safe matching
 * This replaces string-based matching for better code safety
 */
enum class GunaType(val displayName: String, val maxPoints: Double) {
    VARNA("Varna", 1.0),
    VASHYA("Vashya", 2.0),
    TARA("Tara", 3.0),
    YONI("Yoni", 4.0),
    GRAHA_MAITRI("Graha Maitri", 5.0),
    GANA("Gana", 6.0),
    BHAKOOT("Bhakoot", 7.0),
    NADI("Nadi", 8.0);

    fun getLocalizedName(language: Language): String = when (this) {
        VARNA -> StringResources.get(StringKeyMatch.GUNA_VARNA, language)
        VASHYA -> StringResources.get(StringKeyMatch.GUNA_VASHYA, language)
        TARA -> StringResources.get(StringKeyMatch.GUNA_TARA, language)
        YONI -> StringResources.get(StringKeyMatch.GUNA_YONI, language)
        GRAHA_MAITRI -> StringResources.get(StringKeyMatch.GUNA_GRAHA_MAITRI, language)
        GANA -> StringResources.get(StringKeyMatch.GUNA_GANA, language)
        BHAKOOT -> StringResources.get(StringKeyMatch.GUNA_BHAKOOT, language)
        NADI -> StringResources.get(StringKeyMatch.GUNA_NADI, language)
    }

    companion object {
        /**
         * Find GunaType from name string (for backward compatibility)
         */
        fun fromName(name: String): GunaType? {
            return entries.find {
                it.displayName.equals(name, ignoreCase = true) ||
                it.name.equals(name, ignoreCase = true)
            }
        }
    }
}

/**
 * Result of individual Guna analysis
 */
data class GunaAnalysis(
    val gunaType: GunaType,
    val name: String,
    val maxPoints: Double,
    val obtainedPoints: Double,
    val description: String,
    val brideValue: String,
    val groomValue: String,
    val analysis: String,
    val isPositive: Boolean
) {
    val percentage: Double get() = (obtainedPoints / maxPoints) * 100.0

    /**
     * Check if this is a specific Guna type (type-safe)
     */
    fun isType(type: GunaType): Boolean = gunaType == type

    /**
     * Check if this Guna has dosha (0 points)
     */
    val hasDosha: Boolean get() = obtainedPoints == 0.0
}

/**
 * Comprehensive Manglik Dosha analysis result
 */
data class ManglikAnalysis(
    val person: String,
    val dosha: ManglikDosha,
    val marsHouse: Int,
    val marsHouseFromMoon: Int = 0,
    val marsHouseFromVenus: Int = 0,
    val marsDegreeInHouse: Double = 0.0,
    val isRetrograde: Boolean = false,
    val factors: List<String>,
    val cancellations: List<String>,
    val effectiveDosha: ManglikDosha,
    val intensity: Int = 100,
    val fromLagna: Boolean = false,
    val fromMoon: Boolean = false,
    val fromVenus: Boolean = false
) {
    fun getDetailedDescription(language: Language): String {
        if (effectiveDosha == ManglikDosha.NONE) {
            return StringResources.get(StringKeyMatch.MANGLIK_NO_DOSHA_DESC, language)
        }

        return buildString {
            append("${effectiveDosha.getLocalizedName(language)} ")
            append(StringResources.get(StringKeyMatch.MANGLIK_DETECTED, language))
            if (intensity < 100) {
                append(" (${intensity}% ${StringResources.get(StringKeyMatch.MANGLIK_INTENSITY, language)})")
            }
            append(". ${StringResources.get(StringKeyMatch.MANGLIK_MARS_IN, language)} ")
            val sources = mutableListOf<String>()
            if (fromLagna) sources.add("${StringResources.get(StringKeyAnalysis.HOUSE, language)} $marsHouse ${StringResources.get(StringKeyMatch.FROM_LAGNA, language)}")
            if (fromMoon) sources.add("${StringResources.get(StringKeyAnalysis.HOUSE, language)} $marsHouseFromMoon ${StringResources.get(StringKeyMatch.FROM_MOON, language)}")
            if (fromVenus) sources.add("${StringResources.get(StringKeyAnalysis.HOUSE, language)} $marsHouseFromVenus ${StringResources.get(StringKeyMatch.FROM_VENUS, language)}")
            append(sources.joinToString(", "))
            append(".")
        }
    }
}

/**
 * Additional compatibility factors beyond Ashtakoota
 */
data class AdditionalFactors(
    val vedhaPresent: Boolean,
    val vedhaDetails: String,
    val rajjuCompatible: Boolean,
    val rajjuDetails: String,
    val brideRajju: Rajju,
    val groomRajju: Rajju,
    val brideArudha: RajjuArudha,
    val groomArudha: RajjuArudha,
    val streeDeerghaSatisfied: Boolean,
    val streeDeerghaDiff: Int,
    val mahendraSatisfied: Boolean,
    val mahendraDetails: String
)

/**
 * Complete matchmaking result
 */
data class MatchmakingResult(
    val brideChart: VedicChart,
    val groomChart: VedicChart,
    val gunaAnalyses: List<GunaAnalysis>,
    val totalPoints: Double,
    val maxPoints: Double,
    val percentage: Double,
    val rating: CompatibilityRating,
    val brideManglik: ManglikAnalysis,
    val groomManglik: ManglikAnalysis,
    val manglikCompatibility: String,
    val additionalFactors: AdditionalFactors,
    val specialConsiderations: List<String>,
    val remedies: List<String>,
    val summary: String,
    val detailedAnalysis: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val varnaScore: Double get() = gunaAnalyses.find { it.name == "Varna" }?.obtainedPoints ?: 0.0
    val vashyaScore: Double get() = gunaAnalyses.find { it.name == "Vashya" }?.obtainedPoints ?: 0.0
    val taraScore: Double get() = gunaAnalyses.find { it.name == "Tara" }?.obtainedPoints ?: 0.0
    val yoniScore: Double get() = gunaAnalyses.find { it.name == "Yoni" }?.obtainedPoints ?: 0.0
    val grahaMaitriScore: Double get() = gunaAnalyses.find { it.name == "Graha Maitri" }?.obtainedPoints ?: 0.0
    val ganaScore: Double get() = gunaAnalyses.find { it.name == "Gana" }?.obtainedPoints ?: 0.0
    val bhakootScore: Double get() = gunaAnalyses.find { it.name == "Bhakoot" }?.obtainedPoints ?: 0.0
    val nadiScore: Double get() = gunaAnalyses.find { it.name == "Nadi" }?.obtainedPoints ?: 0.0
}

// ============================================
// Vedha Pairs (Nakshatra Obstruction)
// ============================================

/**
 * Vedha Nakshatra pairs that cause obstruction
 */
object VedhaPairs {
    val pairs: List<Pair<Nakshatra, Nakshatra>> = listOf(
        Pair(Nakshatra.ASHWINI, Nakshatra.JYESHTHA),
        Pair(Nakshatra.BHARANI, Nakshatra.ANURADHA),
        Pair(Nakshatra.KRITTIKA, Nakshatra.VISHAKHA),
        Pair(Nakshatra.ROHINI, Nakshatra.SWATI),
        Pair(Nakshatra.ARDRA, Nakshatra.SHRAVANA),
        Pair(Nakshatra.PUNARVASU, Nakshatra.UTTARA_ASHADHA),
        Pair(Nakshatra.PUSHYA, Nakshatra.PURVA_ASHADHA),
        Pair(Nakshatra.ASHLESHA, Nakshatra.MULA),
        Pair(Nakshatra.MAGHA, Nakshatra.REVATI),
        Pair(Nakshatra.PURVA_PHALGUNI, Nakshatra.UTTARA_BHADRAPADA),
        Pair(Nakshatra.UTTARA_PHALGUNI, Nakshatra.PURVA_BHADRAPADA),
        Pair(Nakshatra.HASTA, Nakshatra.SHATABHISHA),
        Pair(Nakshatra.CHITRA, Nakshatra.DHANISHTHA),
        Pair(Nakshatra.MRIGASHIRA, Nakshatra.DHANISHTHA)
    )

    fun hasVedha(nakshatra1: Nakshatra, nakshatra2: Nakshatra): Boolean {
        return pairs.any { (star1, star2) ->
            (nakshatra1 == star1 && nakshatra2 == star2) ||
            (nakshatra1 == star2 && nakshatra2 == star1)
        }
    }
}

// ============================================
// Scoring Constants
// ============================================

object MatchmakingConstants {
    const val MAX_VARNA = 1.0
    const val MAX_VASHYA = 2.0
    const val MAX_TARA = 3.0
    const val MAX_YONI = 4.0
    const val MAX_GRAHA_MAITRI = 5.0
    const val MAX_GANA = 6.0
    const val MAX_BHAKOOT = 7.0
    const val MAX_NADI = 8.0
    const val MAX_TOTAL = 36.0

    // Compatibility rating thresholds (based on guna points out of 36)
    const val EXCELLENT_THRESHOLD = 31.0  // 31+ points
    const val GOOD_THRESHOLD = 26.0       // 26-30 points
    const val AVERAGE_THRESHOLD = 21.0    // 21-25 points
    const val POOR_THRESHOLD = 16.0       // 16-20 points
    // Below 16 is BELOW_AVERAGE

    /** Manglik houses from Lagna: 1, 2, 4, 7, 8, 12 */
    val MANGLIK_HOUSES = listOf(1, 2, 4, 7, 8, 12)

    /** Most severe Manglik houses */
    val SEVERE_MANGLIK_HOUSES = listOf(7, 8)

    /** Moderate Manglik houses */
    val MODERATE_MANGLIK_HOUSES = listOf(1, 4, 12)

    /** Mild Manglik houses */
    val MILD_MANGLIK_HOUSES = listOf(2)

    /** Mahendra favorable positions */
    val MAHENDRA_POSITIONS = listOf(4, 7, 10, 13, 16, 19, 22, 25)
}
