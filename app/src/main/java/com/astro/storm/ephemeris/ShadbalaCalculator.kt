package com.astro.storm.ephemeris

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.data.model.Nakshatra
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Comprehensive Shadbala (Six Strengths) Calculator
 *
 * Shadbala is a system of calculating planetary strength in Vedic astrology.
 * It consists of six main components (Shad = Six, Bala = Strength):
 *
 * 1. STHANA BALA (Positional Strength) - Based on planet's zodiacal position
 *    - Uccha Bala (Exaltation strength)
 *    - Saptavargaja Bala (Strength from 7 divisional charts)
 *    - Ojhayugmarasyamsa Bala (Odd-Even sign strength)
 *    - Kendradi Bala (Angular house strength)
 *    - Drekkana Bala (Decanate strength)
 *
 * 2. DIG BALA (Directional Strength) - Based on planet's house position
 *    - Jupiter/Mercury strongest in 1st house (East)
 *    - Sun/Mars strongest in 10th house (South)
 *    - Saturn strongest in 7th house (West)
 *    - Moon/Venus strongest in 4th house (North)
 *
 * 3. KALA BALA (Temporal Strength) - Based on time factors
 *    - Nathonnatha Bala (Day/Night strength)
 *    - Paksha Bala (Lunar fortnight strength)
 *    - Tribhaga Bala (Third of day/night strength)
 *    - Varsha/Masa/Dina/Hora Bala (Year/Month/Day/Hour lords)
 *    - Ayana Bala (Solstice strength)
 *    - Yuddha Bala (Planetary war strength)
 *
 * 4. CHESTA BALA (Motional Strength) - Based on planet's motion
 *    - Retrograde motion gives maximum strength
 *    - Direct motion with slow speed gives moderate strength
 *    - Direct motion with fast speed gives minimum strength
 *
 * 5. NAISARGIKA BALA (Natural Strength) - Inherent strength of planets
 *    - Fixed values: Sun > Moon > Venus > Jupiter > Mercury > Mars > Saturn
 *
 * 6. DRIK BALA (Aspectual Strength) - Based on aspects from other planets
 *    - Benefic aspects increase strength
 *    - Malefic aspects decrease strength
 *
 * The total strength is measured in "Rupas" (1 Rupa = 60 Virupas)
 * Required minimum strength varies by planet for it to give good results.
 */
object ShadbalaCalculator {

    // Constants for calculations
    private const val VIRUPAS_PER_RUPA = 60.0
    private const val DEGREES_PER_SIGN = 30.0

    // Exaltation degrees for each planet (traditional values)
    private val EXALTATION_DEGREES = mapOf(
        Planet.SUN to 10.0,      // 10° Aries
        Planet.MOON to 33.0,     // 3° Taurus (33° from Aries start)
        Planet.MARS to 298.0,    // 28° Capricorn
        Planet.MERCURY to 165.0, // 15° Virgo
        Planet.JUPITER to 95.0,  // 5° Cancer
        Planet.VENUS to 357.0,   // 27° Pisces
        Planet.SATURN to 200.0,  // 20° Libra
        Planet.RAHU to 50.0,     // 20° Taurus
        Planet.KETU to 230.0     // 20° Scorpio
    )

    // Debilitation degrees (exactly opposite to exaltation)
    private val DEBILITATION_DEGREES = mapOf(
        Planet.SUN to 190.0,     // 10° Libra
        Planet.MOON to 213.0,    // 3° Scorpio
        Planet.MARS to 118.0,    // 28° Cancer
        Planet.MERCURY to 345.0, // 15° Pisces
        Planet.JUPITER to 275.0, // 5° Capricorn
        Planet.VENUS to 177.0,   // 27° Virgo
        Planet.SATURN to 20.0,   // 20° Aries
        Planet.RAHU to 230.0,    // 20° Scorpio
        Planet.KETU to 50.0      // 20° Taurus
    )

    // Natural strength (Naisargika Bala) in Virupas
    private val NATURAL_STRENGTH = mapOf(
        Planet.SUN to 60.0,
        Planet.MOON to 51.43,
        Planet.VENUS to 42.86,
        Planet.JUPITER to 34.29,
        Planet.MERCURY to 25.71,
        Planet.MARS to 17.14,
        Planet.SATURN to 8.57,
        Planet.RAHU to 8.57,
        Planet.KETU to 8.57
    )

    // Required minimum Shadbala in Rupas for each planet to give good results
    private val REQUIRED_STRENGTH = mapOf(
        Planet.SUN to 6.5,
        Planet.MOON to 6.0,
        Planet.MARS to 5.0,
        Planet.MERCURY to 7.0,
        Planet.JUPITER to 6.5,
        Planet.VENUS to 5.5,
        Planet.SATURN to 5.0,
        Planet.RAHU to 4.0,
        Planet.KETU to 4.0
    )

    // Directional strength positions (house number where planet gets full Dig Bala)
    private val DIG_BALA_POSITIONS = mapOf(
        Planet.SUN to 10,     // South (10th house)
        Planet.MOON to 4,     // North (4th house)
        Planet.MARS to 10,    // South (10th house)
        Planet.MERCURY to 1,  // East (1st house)
        Planet.JUPITER to 1,  // East (1st house)
        Planet.VENUS to 4,    // North (4th house)
        Planet.SATURN to 7,   // West (7th house)
        Planet.RAHU to 10,    // South (10th house)
        Planet.KETU to 4      // North (4th house)
    )

    /**
     * Complete Shadbala calculation result for a planet
     */
    data class PlanetaryShadbala(
        val planet: Planet,
        val sthanaBala: SthanaBala,
        val digBala: Double,
        val kalaBala: KalaBala,
        val chestaBala: Double,
        val naisargikaBala: Double,
        val drikBala: Double,
        val totalVirupas: Double,
        val totalRupas: Double,
        val requiredRupas: Double,
        val percentageOfRequired: Double,
        val strengthRating: StrengthRating
    ) {
        val isStrong: Boolean get() = totalRupas >= requiredRupas

        /**
         * Get detailed interpretation of the Shadbala
         */
        fun getInterpretation(): String {
            return buildString {
                appendLine("${planet.displayName} Shadbala Analysis")
                appendLine("═══════════════════════════════════════")
                appendLine()
                appendLine("Total Strength: ${String.format("%.2f", totalRupas)} Rupas")
                appendLine("Required Strength: ${String.format("%.2f", requiredRupas)} Rupas")
                appendLine("Percentage: ${String.format("%.1f", percentageOfRequired)}%")
                appendLine("Rating: ${strengthRating.displayName}")
                appendLine()
                appendLine("BREAKDOWN (in Virupas):")
                appendLine("───────────────────────────────────────")
                appendLine("1. Sthana Bala: ${String.format("%.2f", sthanaBala.total)}")
                appendLine("   - Uccha Bala: ${String.format("%.2f", sthanaBala.ucchaBala)}")
                appendLine("   - Saptavargaja Bala: ${String.format("%.2f", sthanaBala.saptavargajaBala)}")
                appendLine("   - Ojhayugmarasyamsa: ${String.format("%.2f", sthanaBala.ojhayugmarasyamsaBala)}")
                appendLine("   - Kendradi Bala: ${String.format("%.2f", sthanaBala.kendradiBala)}")
                appendLine("   - Drekkana Bala: ${String.format("%.2f", sthanaBala.drekkanaBala)}")
                appendLine()
                appendLine("2. Dig Bala: ${String.format("%.2f", digBala)}")
                appendLine()
                appendLine("3. Kala Bala: ${String.format("%.2f", kalaBala.total)}")
                appendLine("   - Nathonnatha: ${String.format("%.2f", kalaBala.nathonnathaBala)}")
                appendLine("   - Paksha Bala: ${String.format("%.2f", kalaBala.pakshaBala)}")
                appendLine("   - Tribhaga Bala: ${String.format("%.2f", kalaBala.tribhagaBala)}")
                appendLine("   - Hora/Dina/Masa/Varsha: ${String.format("%.2f", kalaBala.horaAdiBalance)}")
                appendLine("   - Ayana Bala: ${String.format("%.2f", kalaBala.ayanaBala)}")
                appendLine()
                appendLine("4. Chesta Bala: ${String.format("%.2f", chestaBala)}")
                appendLine()
                appendLine("5. Naisargika Bala: ${String.format("%.2f", naisargikaBala)}")
                appendLine()
                appendLine("6. Drik Bala: ${String.format("%.2f", drikBala)}")
            }
        }
    }

    /**
     * Sthana Bala (Positional Strength) components
     */
    data class SthanaBala(
        val ucchaBala: Double,              // Exaltation strength
        val saptavargajaBala: Double,       // Strength from 7 divisional charts
        val ojhayugmarasyamsaBala: Double,  // Odd-Even sign strength
        val kendradiBala: Double,           // Angular house strength
        val drekkanaBala: Double,           // Decanate strength
        val total: Double
    )

    /**
     * Kala Bala (Temporal Strength) components
     */
    data class KalaBala(
        val nathonnathaBala: Double,   // Day/Night strength
        val pakshaBala: Double,        // Lunar fortnight strength
        val tribhagaBala: Double,      // Third of day/night strength
        val horaAdiBalance: Double,    // Hora/Dina/Masa/Varsha lords combined
        val ayanaBala: Double,         // Solstice strength
        val yuddhaBala: Double,        // Planetary war strength (usually 0)
        val total: Double
    )

    /**
     * Strength rating based on percentage of required strength
     */
    enum class StrengthRating(val displayName: String, val description: String) {
        EXTREMELY_WEAK("Extremely Weak", "Planet is severely debilitated and may cause significant challenges"),
        WEAK("Weak", "Planet struggles to deliver its significations effectively"),
        BELOW_AVERAGE("Below Average", "Planet has limited capacity to provide positive results"),
        AVERAGE("Average", "Planet functions at a baseline level with mixed results"),
        ABOVE_AVERAGE("Above Average", "Planet is reasonably strong and delivers good results"),
        STRONG("Strong", "Planet is well-positioned and gives excellent results"),
        VERY_STRONG("Very Strong", "Planet is highly potent and provides outstanding outcomes"),
        EXTREMELY_STRONG("Extremely Strong", "Planet is exceptionally powerful and dominates the chart")
    }

    /**
     * Complete Shadbala analysis for all planets in a chart
     */
    data class ShadbalaAnalysis(
        val chart: VedicChart,
        val planetaryStrengths: Map<Planet, PlanetaryShadbala>,
        val strongestPlanet: Planet,
        val weakestPlanet: Planet,
        val overallStrengthScore: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        /**
         * Get planets sorted by strength (strongest first)
         */
        fun getPlanetsByStrength(): List<PlanetaryShadbala> {
            return planetaryStrengths.values.sortedByDescending { it.totalRupas }
        }

        /**
         * Get planets that are below required strength
         */
        fun getWeakPlanets(): List<PlanetaryShadbala> {
            return planetaryStrengths.values.filter { !it.isStrong }
        }

        /**
         * Get summary interpretation
         */
        fun getSummaryInterpretation(): String {
            val strong = planetaryStrengths.values.count { it.isStrong }
            val weak = planetaryStrengths.values.count { !it.isStrong }

            return buildString {
                appendLine("SHADBALA SUMMARY")
                appendLine("═══════════════════════════════════════")
                appendLine()
                appendLine("Overall Chart Strength: ${String.format("%.1f", overallStrengthScore)}%")
                appendLine("Strong Planets: $strong")
                appendLine("Weak Planets: $weak")
                appendLine()
                appendLine("Strongest Planet: ${strongestPlanet.displayName}")
                appendLine("Weakest Planet: ${weakestPlanet.displayName}")
                appendLine()
                appendLine("INDIVIDUAL STRENGTHS (in Rupas):")
                appendLine("───────────────────────────────────────")
                getPlanetsByStrength().forEach { shadbala ->
                    val status = if (shadbala.isStrong) "✓" else "✗"
                    appendLine("$status ${shadbala.planet.displayName.padEnd(10)}: ${String.format("%.2f", shadbala.totalRupas)} / ${String.format("%.2f", shadbala.requiredRupas)}")
                }
            }
        }
    }

    /**
     * Calculate complete Shadbala for all planets in a chart
     */
    fun calculateShadbala(chart: VedicChart): ShadbalaAnalysis {
        val planetaryStrengths = mutableMapOf<Planet, PlanetaryShadbala>()

        // Calculate Shadbala for each main planet (excluding outer planets)
        for (position in chart.planetPositions) {
            if (position.planet in Planet.MAIN_PLANETS) {
                planetaryStrengths[position.planet] = calculatePlanetShadbala(
                    position,
                    chart
                )
            }
        }

        // Find strongest and weakest planets
        val sortedByStrength = planetaryStrengths.values.sortedByDescending { it.totalRupas }
        val strongestPlanet = sortedByStrength.first().planet
        val weakestPlanet = sortedByStrength.last().planet

        // Calculate overall strength score (average percentage of required)
        val overallScore = planetaryStrengths.values.map { it.percentageOfRequired }.average()

        return ShadbalaAnalysis(
            chart = chart,
            planetaryStrengths = planetaryStrengths,
            strongestPlanet = strongestPlanet,
            weakestPlanet = weakestPlanet,
            overallStrengthScore = overallScore
        )
    }

    /**
     * Calculate Shadbala for a single planet
     */
    fun calculatePlanetShadbala(
        position: PlanetPosition,
        chart: VedicChart
    ): PlanetaryShadbala {
        val planet = position.planet

        // 1. Calculate Sthana Bala
        val sthanaBala = calculateSthanaBala(position, chart)

        // 2. Calculate Dig Bala
        val digBala = calculateDigBala(position)

        // 3. Calculate Kala Bala
        val kalaBala = calculateKalaBala(position, chart)

        // 4. Calculate Chesta Bala
        val chestaBala = calculateChestaBala(position)

        // 5. Get Naisargika Bala (fixed value)
        val naisargikaBala = NATURAL_STRENGTH[planet] ?: 0.0

        // 6. Calculate Drik Bala
        val drikBala = calculateDrikBala(position, chart)

        // Calculate totals
        val totalVirupas = sthanaBala.total + digBala + kalaBala.total +
                chestaBala + naisargikaBala + drikBala
        val totalRupas = totalVirupas / VIRUPAS_PER_RUPA
        val requiredRupas = REQUIRED_STRENGTH[planet] ?: 5.0
        val percentage = (totalRupas / requiredRupas) * 100.0

        // Determine strength rating
        val rating = when {
            percentage < 50.0 -> StrengthRating.EXTREMELY_WEAK
            percentage < 70.0 -> StrengthRating.WEAK
            percentage < 85.0 -> StrengthRating.BELOW_AVERAGE
            percentage < 100.0 -> StrengthRating.AVERAGE
            percentage < 115.0 -> StrengthRating.ABOVE_AVERAGE
            percentage < 130.0 -> StrengthRating.STRONG
            percentage < 150.0 -> StrengthRating.VERY_STRONG
            else -> StrengthRating.EXTREMELY_STRONG
        }

        return PlanetaryShadbala(
            planet = planet,
            sthanaBala = sthanaBala,
            digBala = digBala,
            kalaBala = kalaBala,
            chestaBala = chestaBala,
            naisargikaBala = naisargikaBala,
            drikBala = drikBala,
            totalVirupas = totalVirupas,
            totalRupas = totalRupas,
            requiredRupas = requiredRupas,
            percentageOfRequired = percentage,
            strengthRating = rating
        )
    }

    // ======================== STHANA BALA ========================

    /**
     * Calculate Sthana Bala (Positional Strength)
     */
    private fun calculateSthanaBala(position: PlanetPosition, chart: VedicChart): SthanaBala {
        val ucchaBala = calculateUcchaBala(position)
        val saptavargajaBala = calculateSaptavargajaBala(position, chart)
        val ojhayugmarasyamsaBala = calculateOjhayugmarasyamsaBala(position)
        val kendradiBala = calculateKendradiBala(position)
        val drekkanaBala = calculateDrekkanaBala(position)

        val total = ucchaBala + saptavargajaBala + ojhayugmarasyamsaBala +
                kendradiBala + drekkanaBala

        return SthanaBala(
            ucchaBala = ucchaBala,
            saptavargajaBala = saptavargajaBala,
            ojhayugmarasyamsaBala = ojhayugmarasyamsaBala,
            kendradiBala = kendradiBala,
            drekkanaBala = drekkanaBala,
            total = total
        )
    }

    /**
     * Calculate Uccha Bala (Exaltation Strength)
     * Maximum 60 Virupas at exact exaltation degree
     * Minimum 0 Virupas at exact debilitation degree
     */
    private fun calculateUcchaBala(position: PlanetPosition): Double {
        val planet = position.planet
        val longitude = position.longitude

        val exaltationDegree = EXALTATION_DEGREES[planet] ?: return 0.0
        val debilitationDegree = DEBILITATION_DEGREES[planet] ?: return 0.0

        // Calculate distance from debilitation point
        var distanceFromDebilitation = longitude - debilitationDegree
        if (distanceFromDebilitation < 0) distanceFromDebilitation += 360.0
        if (distanceFromDebilitation > 180.0) distanceFromDebilitation = 360.0 - distanceFromDebilitation

        // Convert to Virupas (max 60 at exaltation, 0 at debilitation)
        return (distanceFromDebilitation / 180.0) * 60.0
    }

    /**
     * Calculate Saptavargaja Bala (Strength from 7 Divisional Charts)
     * Based on placement in:
     * - Own sign: 30 Virupas
     * - Moolatrikona: 22.5 Virupas
     * - Exaltation: 20 Virupas
     * - Friendly sign: 15 Virupas
     * - Neutral sign: 10 Virupas
     * - Enemy sign: 7.5 Virupas
     */
    private fun calculateSaptavargajaBala(position: PlanetPosition, chart: VedicChart): Double {
        var totalBala = 0.0
        val planet = position.planet

        // D1 - Rashi
        totalBala += getVargaStrength(planet, position.sign)

        // Calculate for divisional charts (simplified - using D9 as primary)
        val divisionalCharts = DivisionalChartCalculator.calculateAllDivisionalCharts(chart)

        // D2 - Hora
        divisionalCharts.find { it.chartType == DivisionalChartType.D2_HORA }?.let { d2 ->
            d2.planetPositions.find { it.planet == planet }?.let { d2Pos ->
                totalBala += getVargaStrength(planet, d2Pos.sign) * 0.5
            }
        }

        // D3 - Drekkana
        divisionalCharts.find { it.chartType == DivisionalChartType.D3_DREKKANA }?.let { d3 ->
            d3.planetPositions.find { it.planet == planet }?.let { d3Pos ->
                totalBala += getVargaStrength(planet, d3Pos.sign) * 0.5
            }
        }

        // D9 - Navamsa (most important)
        divisionalCharts.find { it.chartType == DivisionalChartType.D9_NAVAMSA }?.let { d9 ->
            d9.planetPositions.find { it.planet == planet }?.let { d9Pos ->
                totalBala += getVargaStrength(planet, d9Pos.sign)
            }
        }

        // D12 - Dwadasamsa
        divisionalCharts.find { it.chartType == DivisionalChartType.D12_DWADASAMSA }?.let { d12 ->
            d12.planetPositions.find { it.planet == planet }?.let { d12Pos ->
                totalBala += getVargaStrength(planet, d12Pos.sign) * 0.5
            }
        }

        // D30 - Trimsamsa
        divisionalCharts.find { it.chartType == DivisionalChartType.D30_TRIMSAMSA }?.let { d30 ->
            d30.planetPositions.find { it.planet == planet }?.let { d30Pos ->
                totalBala += getVargaStrength(planet, d30Pos.sign) * 0.5
            }
        }

        return totalBala
    }

    /**
     * Get strength for a planet in a particular sign (for Saptavargaja Bala)
     */
    private fun getVargaStrength(planet: Planet, sign: ZodiacSign): Double {
        // Check for exaltation
        if (isExalted(planet, sign)) return 20.0

        // Check for own sign
        if (isOwnSign(planet, sign)) return 30.0

        // Check for moolatrikona (simplified)
        if (isMoolatrikona(planet, sign)) return 22.5

        // Check friendships
        return when (getPlanetRelationship(planet, sign.ruler)) {
            com.astro.storm.util.PlanetaryRelationships.Relationship.FRIEND -> 15.0
            com.astro.storm.util.PlanetaryRelationships.Relationship.NEUTRAL -> 10.0
            com.astro.storm.util.PlanetaryRelationships.Relationship.ENEMY -> 7.5
        }
    }

    /**
     * Calculate Ojhayugmarasyamsa Bala (Odd-Even Sign Strength)
     * Moon/Venus: 15 Virupas in even signs
     * Other planets: 15 Virupas in odd signs
     */
    private fun calculateOjhayugmarasyamsaBala(position: PlanetPosition): Double {
        val signNumber = position.sign.number
        val isOddSign = signNumber % 2 == 1

        return when (position.planet) {
            Planet.MOON, Planet.VENUS -> if (!isOddSign) 15.0 else 0.0
            else -> if (isOddSign) 15.0 else 0.0
        }
    }

    /**
     * Calculate Kendradi Bala (Angular House Strength)
     * Kendra (1,4,7,10): 60 Virupas
     * Panapara (2,5,8,11): 30 Virupas
     * Apoklima (3,6,9,12): 15 Virupas
     */
    private fun calculateKendradiBala(position: PlanetPosition): Double {
        return when (position.house) {
            1, 4, 7, 10 -> 60.0  // Kendra houses
            2, 5, 8, 11 -> 30.0  // Panapara houses
            3, 6, 9, 12 -> 15.0  // Apoklima houses
            else -> 0.0
        }
    }

    /**
     * Calculate Drekkana Bala (Decanate Strength)
     * Male planets (Sun, Mars, Jupiter): 15 Virupas in first decanate
     * Female planets (Moon, Venus): 15 Virupas in last decanate
     * Neutral (Mercury, Saturn): 15 Virupas in middle decanate
     */
    private fun calculateDrekkanaBala(position: PlanetPosition): Double {
        val degreeInSign = position.longitude % 30.0
        val decanate = when {
            degreeInSign < 10.0 -> 1
            degreeInSign < 20.0 -> 2
            else -> 3
        }

        return when (position.planet) {
            Planet.SUN, Planet.MARS, Planet.JUPITER -> if (decanate == 1) 15.0 else 0.0
            Planet.MOON, Planet.VENUS -> if (decanate == 3) 15.0 else 0.0
            Planet.MERCURY, Planet.SATURN -> if (decanate == 2) 15.0 else 0.0
            else -> 0.0
        }
    }

    // ======================== DIG BALA ========================

    /**
     * Calculate Dig Bala (Directional Strength)
     * Maximum 60 Virupas when in strongest direction
     * Minimum 0 Virupas when in weakest direction (opposite house)
     */
    private fun calculateDigBala(position: PlanetPosition): Double {
        val strongHouse = DIG_BALA_POSITIONS[position.planet] ?: return 0.0
        val currentHouse = position.house

        // Calculate distance from strong house
        var distance = abs(currentHouse - strongHouse)
        if (distance > 6) distance = 12 - distance

        // Maximum at strong house (0 distance), minimum at opposite (6 distance)
        return (6 - distance) * 10.0
    }

    // ======================== KALA BALA ========================

    /**
     * Calculate Kala Bala (Temporal Strength)
     */
    private fun calculateKalaBala(position: PlanetPosition, chart: VedicChart): KalaBala {
        val nathonnathaBala = calculateNathonnathaBala(position, chart)
        val pakshaBala = calculatePakshaBala(position, chart)
        val tribhagaBala = calculateTribhagaBala(position, chart)
        val horaAdiBalance = calculateHoraAdiBala(position, chart)
        val ayanaBala = calculateAyanaBala(position, chart)
        val yuddhaBala = calculateYuddhaBala(position, chart)

        val total = nathonnathaBala + pakshaBala + tribhagaBala +
                horaAdiBalance + ayanaBala + yuddhaBala

        return KalaBala(
            nathonnathaBala = nathonnathaBala,
            pakshaBala = pakshaBala,
            tribhagaBala = tribhagaBala,
            horaAdiBalance = horaAdiBalance,
            ayanaBala = ayanaBala,
            yuddhaBala = yuddhaBala,
            total = total
        )
    }

    /**
     * Calculate Nathonnatha Bala (Day/Night Strength)
     * Diurnal planets (Sun, Jupiter, Venus) strong during day
     * Nocturnal planets (Moon, Mars, Saturn) strong during night
     * Mercury always gets 60 Virupas
     */
    private fun calculateNathonnathaBala(position: PlanetPosition, chart: VedicChart): Double {
        val hour = chart.birthData.dateTime.hour
        val isDay = hour in 6..18

        return when (position.planet) {
            Planet.MERCURY -> 60.0  // Mercury is always strong
            Planet.SUN, Planet.JUPITER, Planet.VENUS -> if (isDay) 60.0 else 0.0
            Planet.MOON, Planet.MARS, Planet.SATURN -> if (!isDay) 60.0 else 0.0
            Planet.RAHU -> if (!isDay) 60.0 else 0.0
            Planet.KETU -> if (isDay) 60.0 else 0.0
            else -> 30.0
        }
    }

    /**
     * Calculate Paksha Bala (Lunar Fortnight Strength)
     * Based on Moon's phase - benefics stronger in Shukla Paksha
     * Malefics stronger in Krishna Paksha
     */
    private fun calculatePakshaBala(position: PlanetPosition, chart: VedicChart): Double {
        // Get Moon's position
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
            ?: return 30.0

        // Get Sun's position
        val sunPos = chart.planetPositions.find { it.planet == Planet.SUN }
            ?: return 30.0

        // Calculate Moon-Sun elongation to determine paksha
        var elongation = moonPos.longitude - sunPos.longitude
        if (elongation < 0) elongation += 360.0

        // Shukla Paksha: 0-180° (Moon ahead of Sun)
        // Krishna Paksha: 180-360°
        val isShukla = elongation < 180.0

        // Paksha strength varies from 0-60 based on Moon's distance from New/Full Moon
        val phaseStrength = if (elongation < 180.0) {
            elongation / 180.0 * 60.0
        } else {
            (360.0 - elongation) / 180.0 * 60.0
        }

        val isBenefic = when (position.planet) {
            Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY -> true
            else -> false
        }

        return if ((isBenefic && isShukla) || (!isBenefic && !isShukla)) {
            phaseStrength
        } else {
            60.0 - phaseStrength
        }
    }

    /**
     * Calculate Tribhaga Bala (Third of Day/Night Strength)
     * Day divided into 3 parts: Mercury rules 1st, Sun rules 2nd, Saturn rules 3rd
     * Night divided into 3 parts: Moon rules 1st, Venus rules 2nd, Mars rules 3rd
     */
    private fun calculateTribhagaBala(position: PlanetPosition, chart: VedicChart): Double {
        val hour = chart.birthData.dateTime.hour
        val isDay = hour in 6..18

        // Simplified calculation - determine which third of day/night
        val periodLord = if (isDay) {
            when {
                hour < 10 -> Planet.MERCURY
                hour < 14 -> Planet.SUN
                else -> Planet.SATURN
            }
        } else {
            when {
                hour < 22 && hour >= 18 -> Planet.MOON
                hour >= 22 || hour < 2 -> Planet.VENUS
                else -> Planet.MARS
            }
        }

        return if (position.planet == periodLord) 60.0 else 0.0
    }

    /**
     * Calculate Hora/Dina/Masa/Varsha Adi Bala
     * Combined strength from being lord of hour, day, month, or year
     */
    private fun calculateHoraAdiBala(position: PlanetPosition, chart: VedicChart): Double {
        var bala = 0.0
        val dateTime = chart.birthData.dateTime

        // Day lord (Vara lord)
        val dayLord = getDayLord(dateTime.dayOfWeek.value)
        if (position.planet == dayLord) bala += 15.0

        // Hora lord (Hour lord)
        val horaLord = getHoraLord(dateTime)
        if (position.planet == horaLord) bala += 15.0

        // Month lord (simplified - based on Moon's sign lord)
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        if (moonPos != null && position.planet == moonPos.sign.ruler) bala += 10.0

        // Year lord (simplified - Sun's sign lord at solar new year)
        if (position.planet == Planet.SUN) bala += 5.0

        return bala
    }

    /**
     * Calculate Ayana Bala (Solstice Strength)
     * Based on declination and hemisphere considerations
     */
    private fun calculateAyanaBala(position: PlanetPosition, chart: VedicChart): Double {
        // Simplified calculation based on longitude
        val longitude = position.longitude
        val declination = 23.45 * sin((longitude - 80.0) * PI / 180.0)

        // Northern declination favors Sun, Mars, Jupiter
        // Southern declination favors Moon, Venus, Saturn
        return when (position.planet) {
            Planet.SUN, Planet.MARS, Planet.JUPITER -> if (declination > 0) 30.0 + declination else 30.0 - abs(declination)
            Planet.MOON, Planet.VENUS, Planet.SATURN -> if (declination < 0) 30.0 + abs(declination) else 30.0 - declination
            else -> 30.0
        }
    }

    /**
     * Calculate Yuddha Bala (Planetary War Strength)
     * When two planets are within 1 degree, the brighter one wins
     */
    private fun calculateYuddhaBala(position: PlanetPosition, chart: VedicChart): Double {
        // Check for planetary wars (planets within 1 degree)
        for (otherPos in chart.planetPositions) {
            if (otherPos.planet == position.planet) continue
            if (otherPos.planet !in listOf(Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN)) continue
            if (position.planet !in listOf(Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN)) continue

            val distance = abs(position.longitude - otherPos.longitude)
            if (distance <= 1.0 || distance >= 359.0) {
                // Planetary war detected - winner gets bonus, loser gets penalty
                val winner = getWarWinner(position.planet, otherPos.planet)
                return if (winner == position.planet) 30.0 else -30.0
            }
        }
        return 0.0
    }

    // ======================== CHESTA BALA ========================

    /**
     * Calculate Chesta Bala (Motional Strength)
     * Based on planet's apparent motion
     * Retrograde: Maximum strength
     * Stationary: High strength
     * Slow direct: Moderate strength
     * Fast direct: Low strength
     */
    private fun calculateChestaBala(position: PlanetPosition): Double {
        // Sun and Moon don't have Chesta Bala (they're never retrograde)
        if (position.planet == Planet.SUN || position.planet == Planet.MOON) {
            return 0.0
        }

        return when {
            position.isRetrograde -> 60.0  // Retrograde - maximum
            position.speed < 0.01 -> 50.0  // Nearly stationary
            position.speed < 0.5 -> 40.0   // Slow direct
            position.speed < 1.0 -> 30.0   // Moderate direct
            else -> 20.0                    // Fast direct
        }
    }

    // ======================== DRIK BALA ========================

    /**
     * Calculate Drik Bala (Aspectual Strength)
     * Based on aspects received from other planets
     * Benefic aspects add strength, malefic aspects reduce it
     */
    private fun calculateDrikBala(position: PlanetPosition, chart: VedicChart): Double {
        var bala = 0.0

        for (aspectingPlanet in chart.planetPositions) {
            if (aspectingPlanet.planet == position.planet) continue

            // Calculate aspect angle
            var angle = abs(position.longitude - aspectingPlanet.longitude)
            if (angle > 180.0) angle = 360.0 - angle

            // Check for significant aspects
            val aspectStrength = when {
                angle <= 10.0 -> 1.0        // Conjunction
                angle in 55.0..65.0 -> 0.25  // Sextile
                angle in 85.0..95.0 -> 0.5   // Square
                angle in 115.0..125.0 -> 0.75 // Trine
                angle in 170.0..180.0 -> 0.5  // Opposition
                else -> 0.0
            }

            if (aspectStrength > 0) {
                val isBenefic = when (aspectingPlanet.planet) {
                    Planet.JUPITER, Planet.VENUS -> true
                    Planet.MOON -> !aspectingPlanet.isRetrograde
                    Planet.MERCURY -> true  // Mercury is conditionally benefic
                    else -> false
                }

                bala += if (isBenefic) {
                    aspectStrength * 15.0
                } else {
                    -aspectStrength * 10.0
                }
            }
        }

        // Drik Bala can be negative but is usually capped
        return bala.coerceIn(-30.0, 60.0)
    }

    // ======================== HELPER FUNCTIONS ========================

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            Planet.RAHU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            Planet.KETU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            else -> false
        }
    }

    private fun isOwnSign(planet: Planet, sign: ZodiacSign): Boolean {
        return sign.ruler == planet
    }

    private fun isMoolatrikona(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LEO
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.ARIES
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.SAGITTARIUS
            Planet.VENUS -> sign == ZodiacSign.LIBRA
            Planet.SATURN -> sign == ZodiacSign.AQUARIUS
            else -> false
        }
    }

    private fun getPlanetRelationship(planet: Planet, signLord: Planet): com.astro.storm.util.PlanetaryRelationships.Relationship {
        return com.astro.storm.util.PlanetaryRelationships.getRelationship(planet, signLord)
    }

    private fun getDayLord(dayOfWeek: Int): Planet {
        return when (dayOfWeek) {
            1 -> Planet.MOON    // Monday
            2 -> Planet.MARS    // Tuesday
            3 -> Planet.MERCURY // Wednesday
            4 -> Planet.JUPITER // Thursday
            5 -> Planet.VENUS   // Friday
            6 -> Planet.SATURN  // Saturday
            7 -> Planet.SUN     // Sunday
            else -> Planet.SUN
        }
    }

    private fun getHoraLord(dateTime: LocalDateTime): Planet {
        // Planetary hour sequence starting from sunrise
        val hourSequence = listOf(
            Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON,
            Planet.SATURN, Planet.JUPITER, Planet.MARS
        )

        val dayLord = getDayLord(dateTime.dayOfWeek.value)
        val startIndex = hourSequence.indexOf(dayLord)
        val hour = dateTime.hour

        // Calculate hora index (simplified - assuming 6 AM sunrise)
        val horasSinceSunrise = if (hour >= 6) hour - 6 else hour + 18
        val horaIndex = (startIndex + horasSinceSunrise) % 7

        return hourSequence[horaIndex]
    }

    private fun getWarWinner(planet1: Planet, planet2: Planet): Planet {
        // Winner is determined by natural brightness, corrected order
        val brightness = mapOf(
            Planet.VENUS to 7,
            Planet.JUPITER to 6,
            Planet.MERCURY to 5,
            Planet.MARS to 4,
            Planet.SATURN to 3
        )

        val b1 = brightness[planet1] ?: 0
        val b2 = brightness[planet2] ?: 0

        return if (b1 >= b2) planet1 else planet2
    }
}
