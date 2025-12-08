package com.astro.storm.data.localization

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Localization Manager for AstroStorm
 *
 * Manages:
 * - Language preference (English/Nepali)
 * - Date system preference (AD/BS)
 *
 * Follows the same pattern as ThemeManager for consistency.
 * Uses SharedPreferences for instant access on app startup.
 */
class LocalizationManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _language = MutableStateFlow(getPersistedLanguage())
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _dateSystem = MutableStateFlow(getPersistedDateSystem())
    val dateSystem: StateFlow<DateSystem> = _dateSystem.asStateFlow()

    /**
     * Set the language and persist it
     */
    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _language.value = language
    }

    /**
     * Set the date system and persist it
     */
    fun setDateSystem(dateSystem: DateSystem) {
        prefs.edit().putString(KEY_DATE_SYSTEM, dateSystem.code).apply()
        _dateSystem.value = dateSystem
    }

    /**
     * Get the persisted language from SharedPreferences
     */
    private fun getPersistedLanguage(): Language {
        val savedCode = prefs.getString(KEY_LANGUAGE, Language.DEFAULT.code)
        return Language.fromCode(savedCode ?: Language.DEFAULT.code)
    }

    /**
     * Get the persisted date system from SharedPreferences
     */
    private fun getPersistedDateSystem(): DateSystem {
        val savedCode = prefs.getString(KEY_DATE_SYSTEM, DateSystem.DEFAULT.code)
        return DateSystem.fromCode(savedCode ?: DateSystem.DEFAULT.code)
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
     */
    fun isBSDateSystem(): Boolean = _dateSystem.value == DateSystem.BS

    companion object {
        private const val PREFS_NAME = "astro_storm_localization_prefs"
        private const val KEY_LANGUAGE = "language_code"
        private const val KEY_DATE_SYSTEM = "date_system_code"

        @Volatile
        private var INSTANCE: LocalizationManager? = null

        fun getInstance(context: Context): LocalizationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalizationManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
