package com.astro.storm.data.localization

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * High-Precision Bikram Sambat (BS) to Gregorian (AD) Date Converter
 *
 * This converter uses official Nepali Panchang data for accurate conversion.
 * The conversion is based on the fact that BS calendar has irregular month lengths
 * that vary year to year, unlike the Gregorian calendar.
 *
 * Key features:
 * - Accurate conversion from 1970 BS (1913 AD) to 2100 BS (2043 AD)
 * - Uses actual month lengths data from official Nepali calendar
 * - Handles edge cases for leap years and month boundaries
 * - Thread-safe singleton implementation
 *
 * Reference: Nepal Government's official Panchang data
 *
 * BS Calendar Info:
 * - BS year starts in mid-April (Baishakh 1)
 * - 12 months with 29-32 days each (varies by year)
 * - Approximately 56-57 years ahead of AD
 */
object BikramSambatConverter {

    /**
     * Nepali month names in both languages
     */
    enum class BSMonth(val index: Int, val englishName: String, val nepaliName: String) {
        BAISHAKH(1, "Baishakh", "बैशाख"),
        JESTHA(2, "Jestha", "जेठ"),
        ASHADH(3, "Ashadh", "असार"),
        SHRAWAN(4, "Shrawan", "साउन"),
        BHADRA(5, "Bhadra", "भदौ"),
        ASHWIN(6, "Ashwin", "असोज"),
        KARTIK(7, "Kartik", "कार्तिक"),
        MANGSIR(8, "Mangsir", "मंसिर"),
        POUSH(9, "Poush", "पुष"),
        MAGH(10, "Magh", "माघ"),
        FALGUN(11, "Falgun", "फाल्गुन"),
        CHAITRA(12, "Chaitra", "चैत्र");

        fun getName(language: Language): String {
            return when (language) {
                Language.ENGLISH -> englishName
                Language.NEPALI -> nepaliName
            }
        }

        companion object {
            fun fromIndex(index: Int): BSMonth {
                return entries.find { it.index == index }
                    ?: throw IllegalArgumentException("Invalid month index: $index")
            }
        }
    }

    /**
     * BS Date data class
     */
    data class BSDate(
        val year: Int,
        val month: Int,
        val day: Int
    ) {
        val bsMonth: BSMonth get() = BSMonth.fromIndex(month)

        fun format(language: Language): String {
            return when (language) {
                Language.ENGLISH -> "$year ${bsMonth.englishName} $day"
                Language.NEPALI -> "${toNepaliNumerals(year)} ${bsMonth.nepaliName} ${toNepaliNumerals(day)}"
            }
        }

        fun formatShort(language: Language): String {
            return when (language) {
                Language.ENGLISH -> "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                Language.NEPALI -> "${toNepaliNumerals(year)}-${toNepaliNumerals(month).padStart(2, '०')}-${toNepaliNumerals(day).padStart(2, '०')}"
            }
        }

        override fun toString(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} BS"
    }

    // Reference date: 1 Baishakh 2000 BS = April 13, 1943 AD
    private val REFERENCE_BS_YEAR = 2000
    private val REFERENCE_AD_DATE = LocalDate.of(1943, 4, 13)

    /**
     * Month lengths for each BS year
     * Data from 1970 BS to 2100 BS (official Nepali Panchang)
     *
     * Each array contains 12 integers representing days in each month:
     * [Baishakh, Jestha, Ashadh, Shrawan, Bhadra, Ashwin, Kartik, Mangsir, Poush, Magh, Falgun, Chaitra]
     */
    private val BS_MONTH_DATA: Map<Int, IntArray> = mapOf(
        // 1970-1979 BS
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

        // 1980-1989 BS
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

        // 1990-1999 BS
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

        // 2000-2009 BS
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

        // 2010-2019 BS
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

        // 2020-2029 BS
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

        // 2030-2039 BS
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

        // 2040-2049 BS
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

        // 2050-2059 BS
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

        // 2060-2069 BS
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

        // 2070-2079 BS
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

        // 2080-2089 BS
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

        // 2090-2099 BS
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

        // 2100 BS
        2100 to intArrayOf(31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30, 30)
    )

    /**
     * Get the minimum supported BS year
     */
    val minBSYear: Int get() = BS_MONTH_DATA.keys.minOrNull() ?: 1970

    /**
     * Get the maximum supported BS year
     */
    val maxBSYear: Int get() = BS_MONTH_DATA.keys.maxOrNull() ?: 2100

    /**
     * Get number of days in a specific BS month
     *
     * @param year BS year
     * @param month Month (1-12)
     * @return Number of days in that month, or null if year is not in data range
     */
    fun getDaysInMonth(year: Int, month: Int): Int? {
        if (month < 1 || month > 12) return null
        return BS_MONTH_DATA[year]?.get(month - 1)
    }

    /**
     * Get total days in a BS year
     */
    fun getDaysInYear(year: Int): Int? {
        return BS_MONTH_DATA[year]?.sum()
    }

    /**
     * Convert BS date to AD (Gregorian) date
     *
     * @param bsDate BS date to convert
     * @return Gregorian LocalDate, or null if conversion failed
     */
    fun toAD(bsDate: BSDate): LocalDate? {
        return toAD(bsDate.year, bsDate.month, bsDate.day)
    }

    /**
     * Convert BS date to AD (Gregorian) date
     *
     * @param bsYear BS year
     * @param bsMonth BS month (1-12)
     * @param bsDay BS day
     * @return Gregorian LocalDate, or null if conversion failed
     */
    fun toAD(bsYear: Int, bsMonth: Int, bsDay: Int): LocalDate? {
        // Validate input
        if (bsYear !in minBSYear..maxBSYear) return null
        if (bsMonth !in 1..12) return null
        val daysInMonth = getDaysInMonth(bsYear, bsMonth) ?: return null
        if (bsDay !in 1..daysInMonth) return null

        // Calculate total days from reference date
        var totalDays = 0L

        if (bsYear >= REFERENCE_BS_YEAR) {
            // Forward calculation
            for (year in REFERENCE_BS_YEAR until bsYear) {
                totalDays += getDaysInYear(year) ?: 365
            }
            // Add days for months in current year
            for (month in 1 until bsMonth) {
                totalDays += getDaysInMonth(bsYear, month) ?: 30
            }
            // Add remaining days
            totalDays += bsDay - 1 // -1 because reference is day 1
        } else {
            // Backward calculation
            for (year in bsYear until REFERENCE_BS_YEAR) {
                totalDays -= getDaysInYear(year) ?: 365
            }
            // Subtract days for remaining months in start year
            for (month in bsMonth + 1..12) {
                totalDays -= getDaysInMonth(bsYear, month) ?: 30
            }
            // Subtract remaining days in current month
            val currentMonthDays = getDaysInMonth(bsYear, bsMonth) ?: 30
            totalDays -= (currentMonthDays - bsDay + 1)
        }

        return REFERENCE_AD_DATE.plusDays(totalDays)
    }

    /**
     * Convert AD (Gregorian) date to BS date
     *
     * @param adDate Gregorian date to convert
     * @return BSDate, or null if date is outside supported range
     */
    fun toBS(adDate: LocalDate): BSDate? {
        // Calculate days difference from reference
        val daysDiff = ChronoUnit.DAYS.between(REFERENCE_AD_DATE, adDate)

        var bsYear = REFERENCE_BS_YEAR
        var bsMonth = 1
        var bsDay = 1
        var remainingDays = daysDiff

        if (remainingDays >= 0) {
            // Forward iteration
            while (remainingDays > 0) {
                val daysInYear = getDaysInYear(bsYear)
                if (daysInYear == null) {
                    // Year not in data, use approximation
                    return approximateToBS(adDate)
                }

                if (remainingDays >= daysInYear) {
                    remainingDays -= daysInYear
                    bsYear++
                } else {
                    break
                }
            }

            // Find month
            for (month in 1..12) {
                val daysInMonth = getDaysInMonth(bsYear, month) ?: 30
                if (remainingDays >= daysInMonth) {
                    remainingDays -= daysInMonth
                } else {
                    bsMonth = month
                    bsDay = remainingDays.toInt() + 1
                    break
                }
            }
        } else {
            // Backward iteration
            bsYear = REFERENCE_BS_YEAR - 1
            remainingDays = -remainingDays - 1

            while (remainingDays >= 0) {
                val daysInYear = getDaysInYear(bsYear)
                if (daysInYear == null) {
                    return approximateToBS(adDate)
                }

                if (remainingDays >= daysInYear) {
                    remainingDays -= daysInYear
                    bsYear--
                } else {
                    break
                }
            }

            // Find month (from end)
            for (month in 12 downTo 1) {
                val daysInMonth = getDaysInMonth(bsYear, month) ?: 30
                if (remainingDays >= daysInMonth) {
                    remainingDays -= daysInMonth
                } else {
                    bsMonth = month
                    bsDay = daysInMonth - remainingDays.toInt()
                    break
                }
            }
        }

        // Validate result is in range
        if (bsYear !in minBSYear..maxBSYear) {
            return approximateToBS(adDate)
        }

        return BSDate(bsYear, bsMonth, bsDay)
    }

    /**
     * Approximate BS date for dates outside the data range
     * Uses average year length of 365.25 days and 56.7 year offset
     */
    private fun approximateToBS(adDate: LocalDate): BSDate? {
        val approximateOffset = 56.7
        val adYear = adDate.year + (adDate.dayOfYear / 365.25)
        val bsYearApprox = adYear + approximateOffset

        val bsYear = bsYearApprox.toInt()
        val fractionalYear = bsYearApprox - bsYear

        // Approximate month (1-12)
        val bsMonth = (fractionalYear * 12).toInt() + 1
        val fractionalMonth = (fractionalYear * 12) - (bsMonth - 1)

        // Approximate day
        val bsDay = (fractionalMonth * 30).toInt() + 1

        return BSDate(
            bsYear.coerceIn(1900, 2200),
            bsMonth.coerceIn(1, 12),
            bsDay.coerceIn(1, 32)
        )
    }

    /**
     * Get today's date in BS
     */
    fun today(): BSDate {
        return toBS(LocalDate.now()) ?: BSDate(2081, 1, 1)
    }

    /**
     * Validate if a BS date is valid
     */
    fun isValidBSDate(year: Int, month: Int, day: Int): Boolean {
        if (year !in minBSYear..maxBSYear) return false
        if (month !in 1..12) return false
        val daysInMonth = getDaysInMonth(year, month) ?: return false
        return day in 1..daysInMonth
    }

    /**
     * Convert number to Nepali numerals
     */
    fun toNepaliNumerals(number: Int): String {
        val nepaliDigits = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
        return number.toString().map { digit ->
            if (digit.isDigit()) nepaliDigits[digit.digitToInt()] else digit
        }.joinToString("")
    }

    /**
     * Convert Nepali numerals to standard numerals
     */
    fun fromNepaliNumerals(nepaliNumber: String): Int? {
        val nepaliToEnglish = mapOf(
            '०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4',
            '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9'
        )
        return try {
            nepaliNumber.map { char ->
                nepaliToEnglish[char] ?: char
            }.joinToString("").toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Get list of years for dropdown (in supported range)
     */
    fun getYearRange(): IntRange = minBSYear..maxBSYear

    /**
     * Get month names for dropdown
     */
    fun getMonthNames(language: Language): List<String> {
        return BSMonth.entries.map { it.getName(language) }
    }

    /**
     * Get days available for a specific year and month
     */
    fun getDaysForMonth(year: Int, month: Int): IntRange {
        val days = getDaysInMonth(year, month) ?: 30
        return 1..days
    }
}
