package com.astro.storm.data.model

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Gender options for astrological chart analysis.
 * Used for gender-specific interpretations in certain astrological traditions.
 */
enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    companion object {
        /**
         * Parse gender from string value, case-insensitive.
         * Returns OTHER as default for unknown values.
         */
        fun fromString(value: String?): Gender {
            if (value.isNullOrBlank()) return OTHER
            return entries.find { it.name.equals(value.trim(), ignoreCase = true) }
                ?: entries.find { it.displayName.equals(value.trim(), ignoreCase = true) }
                ?: OTHER
        }
    }
}

/**
 * Validation result containing any errors found during BirthData validation.
 */
sealed class BirthDataValidation {
    object Valid : BirthDataValidation()
    data class Invalid(val errors: List<String>) : BirthDataValidation() {
        val firstError: String get() = errors.firstOrNull() ?: "Unknown validation error"
    }
}

/**
 * Birth data for Vedic chart calculation.
 *
 * Contains all necessary information to calculate a natal chart:
 * - Name: The name of the person (for identification)
 * - DateTime: The exact date and time of birth
 * - Location: Geographic coordinates and timezone
 * - Gender: Used for gender-specific interpretations
 *
 * Coordinates use the WGS84 standard:
 * - Latitude: -90 to +90 (negative = South, positive = North)
 * - Longitude: -180 to +180 (negative = West, positive = East)
 *
 * @property name The name of the person (trimmed, non-empty)
 * @property dateTime The date and time of birth
 * @property latitude Geographic latitude in decimal degrees
 * @property longitude Geographic longitude in decimal degrees
 * @property timezone IANA timezone identifier (e.g., "Asia/Kathmandu", "America/New_York")
 * @property location Human-readable location description
 * @property gender Gender for interpretation purposes
 */
data class BirthData(
    val name: String,
    val dateTime: LocalDateTime,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val location: String,
    val gender: Gender = Gender.OTHER
) {
    init {
        // Name validation
        require(name.isNotBlank()) {
            "Name cannot be blank"
        }
        require(name.length <= MAX_NAME_LENGTH) {
            "Name exceeds maximum length of $MAX_NAME_LENGTH characters"
        }

        // Coordinate validation
        require(latitude.isFinite()) {
            "Latitude must be a finite number"
        }
        require(longitude.isFinite()) {
            "Longitude must be a finite number"
        }
        require(latitude in MIN_LATITUDE..MAX_LATITUDE) {
            "Latitude must be between $MIN_LATITUDE and $MAX_LATITUDE degrees, got: $latitude"
        }
        require(longitude in MIN_LONGITUDE..MAX_LONGITUDE) {
            "Longitude must be between $MIN_LONGITUDE and $MAX_LONGITUDE degrees, got: $longitude"
        }

        // Timezone validation
        require(timezone.isNotBlank()) {
            "Timezone cannot be blank"
        }
        require(isValidTimezone(timezone)) {
            "Invalid timezone identifier: $timezone"
        }

        // DateTime validation
        require(dateTime.year in MIN_YEAR..MAX_YEAR) {
            "Year must be between $MIN_YEAR and $MAX_YEAR, got: ${dateTime.year}"
        }

        // Location validation (can be empty but not excessively long)
        require(location.length <= MAX_LOCATION_LENGTH) {
            "Location exceeds maximum length of $MAX_LOCATION_LENGTH characters"
        }
    }

    companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
        const val MIN_YEAR = 1
        const val MAX_YEAR = 9999
        const val MAX_NAME_LENGTH = 200
        const val MAX_LOCATION_LENGTH = 500

        /**
         * Validates a timezone identifier.
         */
        private fun isValidTimezone(timezone: String): Boolean {
            return try {
                ZoneId.of(timezone)
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Validates BirthData parameters without throwing exceptions.
         * Use this for UI validation before creating a BirthData instance.
         *
         * @return BirthDataValidation.Valid if all parameters are valid,
         *         BirthDataValidation.Invalid with list of errors otherwise
         */
        fun validate(
            name: String?,
            dateTime: LocalDateTime?,
            latitude: Double?,
            longitude: Double?,
            timezone: String?,
            location: String?
        ): BirthDataValidation {
            val errors = mutableListOf<String>()

            // Name validation
            when {
                name.isNullOrBlank() -> errors.add("Name is required")
                name.length > MAX_NAME_LENGTH -> errors.add("Name exceeds maximum length")
            }

            // DateTime validation
            if (dateTime == null) {
                errors.add("Birth date and time are required")
            } else if (dateTime.year !in MIN_YEAR..MAX_YEAR) {
                errors.add("Year must be between $MIN_YEAR and $MAX_YEAR")
            }

            // Latitude validation
            when {
                latitude == null -> errors.add("Latitude is required")
                !latitude.isFinite() -> errors.add("Latitude must be a valid number")
                latitude !in MIN_LATITUDE..MAX_LATITUDE -> errors.add("Latitude must be between $MIN_LATITUDE° and $MAX_LATITUDE°")
            }

            // Longitude validation
            when {
                longitude == null -> errors.add("Longitude is required")
                !longitude.isFinite() -> errors.add("Longitude must be a valid number")
                longitude !in MIN_LONGITUDE..MAX_LONGITUDE -> errors.add("Longitude must be between $MIN_LONGITUDE° and $MAX_LONGITUDE°")
            }

            // Timezone validation
            when {
                timezone.isNullOrBlank() -> errors.add("Timezone is required")
                !isValidTimezone(timezone) -> errors.add("Invalid timezone: $timezone")
            }

            // Location validation
            if ((location?.length ?: 0) > MAX_LOCATION_LENGTH) {
                errors.add("Location exceeds maximum length")
            }

            return if (errors.isEmpty()) {
                BirthDataValidation.Valid
            } else {
                BirthDataValidation.Invalid(errors)
            }
        }

        /**
         * Creates a BirthData instance with safe defaults for testing.
         * NOT for production use - only for testing and debugging.
         */
        fun createForTesting(
            name: String = "Test",
            dateTime: LocalDateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude: Double = 0.0,
            longitude: Double = 0.0,
            timezone: String = "UTC",
            location: String = "Test Location",
            gender: Gender = Gender.OTHER
        ): BirthData {
            return BirthData(name, dateTime, latitude, longitude, timezone, location, gender)
        }
    }

    /**
     * Returns a formatted string representation of the birth date and time.
     */
    fun getFormattedDateTime(): String {
        return "${dateTime.dayOfMonth}/${dateTime.monthValue}/${dateTime.year} " +
               "${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * Returns formatted coordinates string.
     */
    fun getFormattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return "${kotlin.math.abs(latitude).format(4)}°$latDir, ${kotlin.math.abs(longitude).format(4)}°$lonDir"
    }

    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
}
