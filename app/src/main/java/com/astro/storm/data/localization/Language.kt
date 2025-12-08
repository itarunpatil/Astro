package com.astro.storm.data.localization

/**
 * Supported languages in AstroStorm
 *
 * This enum represents the available languages for the application.
 * Each language has a code (ISO 639-1) and native name for display.
 */
enum class Language(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    ENGLISH("en", "English", "English"),
    NEPALI("ne", "नेपाली", "Nepali");

    companion object {
        val DEFAULT = ENGLISH

        fun fromCode(code: String): Language {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}

/**
 * Date calendar system selection
 */
enum class DateSystem(
    val code: String,
    val displayNameEn: String,
    val displayNameNe: String
) {
    AD("ad", "AD (Gregorian)", "ई.स. (ग्रेगोरियन)"),
    BS("bs", "BS (Bikram Sambat)", "वि.सं. (विक्रम सम्वत्)");

    fun getDisplayName(language: Language): String {
        return when (language) {
            Language.ENGLISH -> displayNameEn
            Language.NEPALI -> displayNameNe
        }
    }

    companion object {
        val DEFAULT = AD

        fun fromCode(code: String): DateSystem {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
