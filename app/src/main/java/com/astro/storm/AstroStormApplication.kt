package com.astro.storm

import android.app.Application
import android.util.Log
import com.astro.storm.ephemeris.SwissEphemerisEngine
import com.astro.storm.util.GlobalExceptionHandler
import java.io.IOException

/**
 * Application class for AstroStorm
 */
class AstroStormApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the global exception handler
        GlobalExceptionHandler.initialize(this)

        // Initialize the ephemeris engine on app startup
        // This also handles copying the necessary ephemeris files
        try {
            SwissEphemerisEngine.create(this)
        } catch (e: Exception) {
            Log.e("AstroStormApplication", "Failed to initialize SwissEphemerisEngine", e)
        }
    }
}
