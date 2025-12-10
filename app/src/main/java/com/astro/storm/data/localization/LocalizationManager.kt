package com.astro.storm.data.localization

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Localization Manager for AstroStorm
 *
 * Manages:
 * - Language preference (English/Nepali)
 * - Date system is automatically derived from language (BS for Nepali, AD for English)
 *
 * Key Design Decision:
 * The date system is NO LONGER a separate user preference. Instead, it automatically
 * follows the selected language:
 * - English → AD (Gregorian) calendar
 * - Nepali → BS (Bikram Sambat) calendar
 *
 * This provides a more intuitive user experience where language and cultural context
 * (including calendar system) are unified.
 */
class LocalizationManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _language = MutableStateFlow(getPersistedLanguage())
    val language: StateFlow<Language> = _language.asStateFlow()

    // Keep track of the derived date system that updates when language changes
    private val _dateSystem = MutableStateFlow(getDateSystemForLanguage(_language.value))

    /**
     * Date system is now derived from language automatically
     * - English → AD (Gregorian)
     * - Nepali → BS (Bikram Sambat)
     *
     * This StateFlow properly reacts to language changes.
     */
    val dateSystem: StateFlow<DateSystem> = _dateSystem.asStateFlow()

    /**
     * Set the language and persist it.
     * Date system will automatically update to match the language.
     */
    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _language.value = language
        // Update the derived date system
        _dateSystem.value = getDateSystemForLanguage(language)
    }

    /**
     * Get the appropriate date system for a given language
     */
    private fun getDateSystemForLanguage(language: Language): DateSystem {
        return when (language) {
            Language.ENGLISH -> DateSystem.AD
            Language.NEPALI -> DateSystem.BS
        }
    }

    /**
     * @deprecated Date system is now automatically derived from language.
     * This method is kept for backwards compatibility but does nothing.
     */
    @Deprecated(
        message = "Date system is now automatically derived from language selection",
        replaceWith = ReplaceWith("setLanguage(Language.NEPALI)"),
        level = DeprecationLevel.WARNING
    )
    fun setDateSystem(dateSystem: DateSystem) {
        // No-op: Date system is now derived from language
        // Kept for backwards compatibility with existing code
    }

    /**
     * Get the persisted language from SharedPreferences
     */
    private fun getPersistedLanguage(): Language {
        val savedCode = prefs.getString(KEY_LANGUAGE, Language.DEFAULT.code)
        return Language.fromCode(savedCode ?: Language.DEFAULT.code)
    }

    /**
     * Get localized string for the current language
     */
    fun getString(key: StringKey): String {
        return StringResources.get(key, _language.value)
    }

    /**
     * Get localized string with format arguments
     */
    fun getString(key: StringKey, vararg args: Any): String {
        return StringResources.get(key, _language.value, *args)
    }

    /**
     * Check if current language is Nepali
     */
    fun isNepali(): Boolean = _language.value == Language.NEPALI

    /**
     * Check if current date system is BS
     * Now equivalent to checking if language is Nepali
     */
    fun isBSDateSystem(): Boolean = _language.value == Language.NEPALI

    /**
     * Get current date system based on language
     */
    fun getCurrentDateSystem(): DateSystem = getDateSystemForLanguage(_language.value)

    /**
     * Clean up old date system preference if it exists
     * Call this once during app initialization to migrate old preferences
     */
    fun migrateOldPreferences() {
        if (prefs.contains(KEY_DATE_SYSTEM_LEGACY)) {
            prefs.edit().remove(KEY_DATE_SYSTEM_LEGACY).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "astro_storm_localization_prefs"
        private const val KEY_LANGUAGE = "language_code"
        private const val KEY_DATE_SYSTEM_LEGACY = "date_system_code" // Legacy key, no longer used

        @Volatile
        private var INSTANCE: LocalizationManager? = null

        fun getInstance(context: Context): LocalizationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalizationManager(context.applicationContext).also { manager ->
                    INSTANCE = manager
                    // Migrate old preferences on first access
                    manager.migrateOldPreferences()
                }
            }
        }
    }
}
