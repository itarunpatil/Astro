package com.astro.storm.localization

import java.time.LocalDate

/**
 * High-precision Bikram Sambat (BS) to Gregorian (AD) date converter
 *
 * Bikram Sambat is the official calendar of Nepal, approximately 56.7 years ahead of Gregorian.
 * This converter uses lookup tables for precise conversion as BS months have variable lengths.
 *
 * Supported range: BS 1970-2100 (AD 1913-2044 approximately)
 */
object BikramSambatConverter {

    /**
     * Days in each month for BS years 1970-2100
     * Each array contains [baisakh, jestha, ashadh, shrawan, bhadra, ashwin, kartik, mangsir, poush, magh, falgun, chaitra]
     */
    private val BS_MONTH_DAYS: Map<Int, IntArray> = mapOf(
        1970 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1971 to intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
        1972 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        1973 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        1974 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1975 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        1976 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        1977 to intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 29, 30, 29, 31),
        1978 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1979 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        1980 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        1981 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        1982 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1983 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        1984 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        1985 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        1986 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1987 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        1988 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        1989 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        1990 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1991 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        1992 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        1993 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        1994 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1995 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        1996 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        1997 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1998 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        1999 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2000 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2001 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2002 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2003 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2004 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2005 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2006 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2007 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2008 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31),
        2009 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2010 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2011 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2012 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        2013 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2014 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2015 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2016 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        2017 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2018 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2019 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2020 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2021 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2022 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2023 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2024 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2025 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2026 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2027 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2028 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2029 to intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
        2030 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2031 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2032 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2033 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2034 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2035 to intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31),
        2036 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2037 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2038 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2039 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        2040 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2041 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2042 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2043 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        2044 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2045 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2046 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2047 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2048 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2049 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2050 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2051 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2052 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2053 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2054 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2055 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2056 to intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
        2057 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2058 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2059 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2060 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2061 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2062 to intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 29, 30, 29, 31),
        2063 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2064 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2065 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2066 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31),
        2067 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2068 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2069 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2070 to intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
        2071 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2072 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2073 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2074 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2075 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2076 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2077 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2078 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2079 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2080 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2081 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2082 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2083 to intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30),
        2084 to intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30),
        2085 to intArrayOf(31, 32, 31, 32, 30, 31, 30, 30, 29, 30, 30, 30),
        2086 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2087 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30),
        2088 to intArrayOf(30, 31, 32, 32, 30, 31, 30, 30, 29, 30, 30, 30),
        2089 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2090 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2091 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30),
        2092 to intArrayOf(30, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2093 to intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2094 to intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30),
        2095 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 30, 30, 30),
        2096 to intArrayOf(30, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2097 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
        2098 to intArrayOf(31, 31, 32, 31, 31, 31, 29, 30, 29, 30, 29, 31),
        2099 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 29, 30, 30, 30),
        2100 to intArrayOf(31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30, 30)
    )

    // Reference point: 1 Baisakh 1970 BS = 14 April 1913 AD
    private val BS_REFERENCE_YEAR = 1970
    private val AD_REFERENCE_DATE = LocalDate.of(1913, 4, 14)

    val MIN_BS_YEAR = 1970
    val MAX_BS_YEAR = 2100

    /**
     * Nepali month names in English
     */
    val NEPALI_MONTHS_EN = listOf(
        "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )

    /**
     * Nepali month names in Nepali
     */
    val NEPALI_MONTHS_NE = listOf(
        "बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज",
        "कात्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत्र"
    )

    /**
     * Get days in a specific BS month
     *
     * @param year BS year
     * @param month Month (1-12)
     * @return Number of days in the month, or 30 if year not in table
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        if (month !in 1..12) return 30
        return BS_MONTH_DAYS[year]?.get(month - 1) ?: 30
    }

    /**
     * Get total days in a BS year
     */
    fun getDaysInYear(year: Int): Int {
        return BS_MONTH_DAYS[year]?.sum() ?: 365
    }

    /**
     * Convert BS date to AD (Gregorian) date
     *
     * @param bsYear BS year (1970-2100)
     * @param bsMonth BS month (1-12)
     * @param bsDay BS day
     * @return LocalDate in AD, or null if conversion fails
     */
    fun bsToAd(bsYear: Int, bsMonth: Int, bsDay: Int): LocalDate? {
        if (bsYear !in MIN_BS_YEAR..MAX_BS_YEAR) return null
        if (bsMonth !in 1..12) return null
        if (bsDay !in 1..getDaysInMonth(bsYear, bsMonth)) return null

        var totalDays = 0L

        // Add days for complete years from reference year
        for (year in BS_REFERENCE_YEAR until bsYear) {
            totalDays += getDaysInYear(year)
        }

        // Add days for complete months in current year
        val monthDays = BS_MONTH_DAYS[bsYear] ?: return null
        for (month in 0 until (bsMonth - 1)) {
            totalDays += monthDays[month]
        }

        // Add remaining days (subtract 1 because we start from day 1, not day 0)
        totalDays += (bsDay - 1)

        return AD_REFERENCE_DATE.plusDays(totalDays)
    }

    /**
     * Convert AD (Gregorian) date to BS date
     *
     * @param adDate LocalDate in AD
     * @return BSDate object, or null if date is out of supported range
     */
    fun adToBs(adDate: LocalDate): BSDate? {
        // Calculate days from reference date
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(AD_REFERENCE_DATE, adDate)

        if (daysDiff < 0) return null // Before supported range

        var remainingDays = daysDiff
        var bsYear = BS_REFERENCE_YEAR
        var bsMonth = 1
        var bsDay = 1

        // Find the year
        while (bsYear <= MAX_BS_YEAR) {
            val daysInYear = getDaysInYear(bsYear)
            if (remainingDays < daysInYear) break
            remainingDays -= daysInYear
            bsYear++
        }

        if (bsYear > MAX_BS_YEAR) return null // After supported range

        // Find the month
        val monthDays = BS_MONTH_DAYS[bsYear] ?: return null
        for (i in 0 until 12) {
            if (remainingDays < monthDays[i]) {
                bsMonth = i + 1
                break
            }
            remainingDays -= monthDays[i]
        }

        // Remaining days + 1 is the day of month
        bsDay = remainingDays.toInt() + 1

        return BSDate(bsYear, bsMonth, bsDay)
    }

    /**
     * Validate if a BS date is valid
     */
    fun isValidBSDate(year: Int, month: Int, day: Int): Boolean {
        if (year !in MIN_BS_YEAR..MAX_BS_YEAR) return false
        if (month !in 1..12) return false
        val daysInMonth = getDaysInMonth(year, month)
        return day in 1..daysInMonth
    }

    /**
     * Get current date in BS
     */
    fun getCurrentBSDate(): BSDate? {
        return adToBs(LocalDate.now())
    }

    /**
     * Format BS date string
     *
     * @param bsDate BSDate object
     * @param language Language for month name
     * @param format Format pattern: "yyyy-MM-dd", "dd MMMM yyyy", etc.
     */
    fun formatBSDate(bsDate: BSDate, language: Language = Language.ENGLISH, format: String = "yyyy-MM-dd"): String {
        val monthName = when (language) {
            Language.ENGLISH -> NEPALI_MONTHS_EN[bsDate.month - 1]
            Language.NEPALI -> NEPALI_MONTHS_NE[bsDate.month - 1]
        }

        return when (format) {
            "yyyy-MM-dd" -> "${bsDate.year}-${bsDate.month.toString().padStart(2, '0')}-${bsDate.day.toString().padStart(2, '0')}"
            "dd MMMM yyyy" -> "${bsDate.day} $monthName ${bsDate.year}"
            "MMMM dd, yyyy" -> "$monthName ${bsDate.day}, ${bsDate.year}"
            "dd/MM/yyyy" -> "${bsDate.day.toString().padStart(2, '0')}/${bsDate.month.toString().padStart(2, '0')}/${bsDate.year}"
            else -> "${bsDate.year}-${bsDate.month.toString().padStart(2, '0')}-${bsDate.day.toString().padStart(2, '0')}"
        }
    }

    /**
     * Convert Nepali numerals to Arabic numerals
     */
    fun nepaliToArabicNumerals(nepaliNumber: String): String {
        val nepaliDigits = "०१२३४५६७८९"
        val arabicDigits = "0123456789"
        var result = nepaliNumber
        for (i in 0..9) {
            result = result.replace(nepaliDigits[i], arabicDigits[i])
        }
        return result
    }

    /**
     * Convert Arabic numerals to Nepali numerals
     */
    fun arabicToNepaliNumerals(arabicNumber: String): String {
        val nepaliDigits = "०१२३४५६७८९"
        val arabicDigits = "0123456789"
        var result = arabicNumber
        for (i in 0..9) {
            result = result.replace(arabicDigits[i], nepaliDigits[i])
        }
        return result
    }

    /**
     * Format number in Nepali if language is Nepali
     */
    fun formatNumber(number: Int, language: Language): String {
        return when (language) {
            Language.ENGLISH -> number.toString()
            Language.NEPALI -> arabicToNepaliNumerals(number.toString())
        }
    }
}

/**
 * Data class representing a Bikram Sambat date
 */
data class BSDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    /**
     * Convert to AD LocalDate
     */
    fun toAD(): LocalDate? = BikramSambatConverter.bsToAd(year, month, day)

    /**
     * Get month name
     */
    fun getMonthName(language: Language = Language.ENGLISH): String {
        return when (language) {
            Language.ENGLISH -> BikramSambatConverter.NEPALI_MONTHS_EN.getOrNull(month - 1) ?: ""
            Language.NEPALI -> BikramSambatConverter.NEPALI_MONTHS_NE.getOrNull(month - 1) ?: ""
        }
    }

    /**
     * Format the date
     */
    fun format(language: Language = Language.ENGLISH, pattern: String = "yyyy-MM-dd"): String {
        return BikramSambatConverter.formatBSDate(this, language, pattern)
    }

    override fun toString(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    companion object {
        /**
         * Create BSDate from AD LocalDate
         */
        fun fromAD(localDate: LocalDate): BSDate? = BikramSambatConverter.adToBs(localDate)

        /**
         * Get today's date in BS
         */
        fun today(): BSDate? = BikramSambatConverter.getCurrentBSDate()

        /**
         * Parse BS date from string (yyyy-MM-dd format)
         */
        fun parse(dateString: String): BSDate? {
            return try {
                val parts = dateString.split("-")
                if (parts.size != 3) return null
                BSDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } catch (e: Exception) {
                null
            }
        }
    }
}
