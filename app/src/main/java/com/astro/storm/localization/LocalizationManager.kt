package com.astro.storm.localization

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton manager for language and date system preferences
 * Provides centralized access to localization settings with persistence
 */
object LocalizationManager {
    private const val PREFS_NAME = "astrostorm_localization"
    private const val KEY_LANGUAGE = "language_code"
    private const val KEY_DATE_SYSTEM = "date_system"

    private var sharedPreferences: SharedPreferences? = null

    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _currentDateSystem = MutableStateFlow(DateSystem.AD)
    val currentDateSystem: StateFlow<DateSystem> = _currentDateSystem.asStateFlow()

    /**
     * Initialize the LocalizationManager with application context
     * Must be called once during app startup (e.g., in Application.onCreate())
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSavedPreferences()
    }

    private fun loadSavedPreferences() {
        sharedPreferences?.let { prefs ->
            // Load language
            val languageCode = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code)
            _currentLanguage.value = Language.fromCode(languageCode ?: Language.ENGLISH.code)

            // Load date system
            val dateSystemName = prefs.getString(KEY_DATE_SYSTEM, DateSystem.AD.name)
            _currentDateSystem.value = try {
                DateSystem.valueOf(dateSystemName ?: DateSystem.AD.name)
            } catch (e: IllegalArgumentException) {
                DateSystem.AD
            }
        }
    }

    /**
     * Set the current language and persist the preference
     */
    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        sharedPreferences?.edit()?.putString(KEY_LANGUAGE, language.code)?.apply()
    }

    /**
     * Set the current date system and persist the preference
     */
    fun setDateSystem(dateSystem: DateSystem) {
        _currentDateSystem.value = dateSystem
        sharedPreferences?.edit()?.putString(KEY_DATE_SYSTEM, dateSystem.name)?.apply()
    }

    /**
     * Get a localized string for the current language
     */
    fun getString(key: StringKey): String {
        return Strings.getString(key, _currentLanguage.value)
    }

    /**
     * Get a localized string for a specific language
     */
    fun getString(key: StringKey, language: Language): String {
        return Strings.getString(key, language)
    }
}

/**
 * Date system enumeration
 */
enum class DateSystem(val displayKey: StringKey) {
    AD(StringKey.SETTINGS_DATE_SYSTEM_AD),
    BS(StringKey.SETTINGS_DATE_SYSTEM_BS)
}
