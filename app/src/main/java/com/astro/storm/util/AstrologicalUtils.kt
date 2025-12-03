package com.astro.storm.util

object AstrologicalUtils {

    /**
     * Normalizes a longitude to the range [0, 360).
     *
     * @param longitude The longitude in degrees.
     * @return The normalized longitude.
     */
    fun normalizeLongitude(longitude: Double): Double {
        return (longitude % 360.0 + 360.0) % 360.0
    }
}
