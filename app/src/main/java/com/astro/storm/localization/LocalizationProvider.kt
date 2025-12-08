package com.astro.storm.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Composition local for accessing current language in Composable context
 */
val LocalLanguage = compositionLocalOf { Language.ENGLISH }

/**
 * Composition local for accessing current date system in Composable context
 */
val LocalDateSystem = compositionLocalOf { DateSystem.AD }

/**
 * Provider composable that makes localization settings available to the entire app
 */
@Composable
fun LocalizationProvider(
    content: @Composable () -> Unit
) {
    val language by LocalizationManager.currentLanguage.collectAsState()
    val dateSystem by LocalizationManager.currentDateSystem.collectAsState()

    CompositionLocalProvider(
        LocalLanguage provides language,
        LocalDateSystem provides dateSystem,
        content = content
    )
}

/**
 * Composable function to get a localized string
 * This function respects the current language setting from LocalLanguage
 *
 * Usage: val text = getString(StringKey.APP_NAME)
 */
@Composable
fun getString(key: StringKey): String {
    val language = LocalLanguage.current
    return remember(key, language) {
        Strings.getString(key, language)
    }
}

/**
 * Non-composable extension to get localized string directly from StringKey
 * Use this in non-Composable contexts like ViewModels or callbacks
 *
 * Usage: val text = StringKey.APP_NAME.localized()
 */
fun StringKey.localized(language: Language = LocalizationManager.currentLanguage.value): String {
    return Strings.getString(this, language)
}

/**
 * Get string with placeholder substitution
 * Use %s for string placeholders, %d for integer placeholders
 *
 * Usage: getFormattedString(StringKey.YOGAS_COUNT_DETECTED, "5") -> "5 yogas detected"
 */
@Composable
fun getFormattedString(key: StringKey, vararg args: Any): String {
    val language = LocalLanguage.current
    return remember(key, language, args) {
        val baseString = Strings.getString(key, language)
        String.format(baseString, *args)
    }
}

/**
 * Non-composable version of formatted string
 */
fun StringKey.localizedFormat(vararg args: Any, language: Language = LocalizationManager.currentLanguage.value): String {
    val baseString = Strings.getString(this, language)
    return try {
        String.format(baseString, *args)
    } catch (e: Exception) {
        baseString
    }
}
