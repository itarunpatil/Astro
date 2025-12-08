package com.astro.storm.localization

/**
 * Supported languages for the AstroStorm app
 *
 * @property code ISO language code
 * @property displayName Native display name of the language
 * @property englishName English name of the language
 */
enum class Language(
    val code: String,
    val displayName: String,
    val englishName: String
) {
    ENGLISH("en", "English", "English"),
    NEPALI("ne", "नेपाली", "Nepali");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
