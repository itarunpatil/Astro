package com.astro.storm.data.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Composition Local for accessing current language
 */
val LocalLanguage = compositionLocalOf { Language.DEFAULT }

/**
 * Composition Local for accessing current date system
 */
val LocalDateSystem = compositionLocalOf { DateSystem.DEFAULT }

/**
 * Composition Local for accessing localization manager
 */
val LocalLocalizationManager = compositionLocalOf<LocalizationManager?> { null }

/**
 * Provider composable that supplies localization context to the app
 *
 * Usage:
 * ```kotlin
 * LocalizationProvider {
 *     // Your app content
 *     Text(text = stringResource(StringKey.HOME_TAB))
 * }
 * ```
 */
@Composable
fun LocalizationProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }

    val language by localizationManager.language.collectAsState()
    val dateSystem by localizationManager.dateSystem.collectAsState()

    CompositionLocalProvider(
        LocalLanguage provides language,
        LocalDateSystem provides dateSystem,
        LocalLocalizationManager provides localizationManager
    ) {
        content()
    }
}

/**
 * Get localized string for a key using current language from Composition Local
 *
 * Usage:
 * ```kotlin
 * Text(text = stringResource(StringKey.HOME_TAB))
 * ```
 */
@Composable
@ReadOnlyComposable
fun stringResource(key: StringKey): String {
    val language = LocalLanguage.current
    return StringResources.get(key, language)
}

/**
 * Get localized string with format arguments
 *
 * Usage:
 * ```kotlin
 * Text(text = stringResource(StringKey.ERROR_CALCULATIONS_FAILED, errorCount))
 * ```
 */
@Composable
@ReadOnlyComposable
fun stringResource(key: StringKey, vararg args: Any): String {
    val language = LocalLanguage.current
    return StringResources.get(key, language, *args)
}

/**
 * Get current language
 */
@Composable
@ReadOnlyComposable
fun currentLanguage(): Language = LocalLanguage.current

/**
 * Get current date system
 */
@Composable
@ReadOnlyComposable
fun currentDateSystem(): DateSystem = LocalDateSystem.current

/**
 * Check if current language is Nepali
 */
@Composable
@ReadOnlyComposable
fun isNepali(): Boolean = LocalLanguage.current == Language.NEPALI

/**
 * Check if current date system is BS
 */
@Composable
@ReadOnlyComposable
fun isBSDateSystem(): Boolean = LocalDateSystem.current == DateSystem.BS

/**
 * Extension function for StringKey to get localized value
 */
@Composable
@ReadOnlyComposable
fun StringKey.localized(): String = stringResource(this)

/**
 * Extension function for StringKey to get localized value with args
 */
@Composable
@ReadOnlyComposable
fun StringKey.localized(vararg args: Any): String = stringResource(this, *args)
