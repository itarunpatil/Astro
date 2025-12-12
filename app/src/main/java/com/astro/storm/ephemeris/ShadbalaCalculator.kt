package com.astro.storm.ephemeris

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ephemeris.DivisionalChartType
import com.astro.storm.ephemeris.VedicAstrologyUtils.PlanetaryRelationship
import java.text.DecimalFormat
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class StrengthRating(
    val displayName: String,
    val description: String,
    val minPercentage: Double
) {
    EXTREMELY_WEAK(
        "Extremely Weak",
        "Planet is severely debilitated and may cause significant challenges",
        0.0
    ),
    WEAK(
        "Weak",
        "Planet struggles to deliver its significations effectively",
        50.0
    ),
    BELOW_AVERAGE(
        "Below Average",
        "Planet has limited capacity to provide positive results",
        70.0
    ),
    AVERAGE(
        "Average",
        "Planet functions at a baseline level with mixed results",
        85.0
    ),
    ABOVE_AVERAGE(
        "Above Average",
        "Planet is reasonably strong and delivers good results",
        100.0
    ),
    STRONG(
        "Strong",
        "Planet is well-positioned and gives excellent results",
        115.0
    ),
    VERY_STRONG(
        "Very Strong",
        "Planet is highly potent and provides outstanding outcomes",
        130.0
    ),
    EXTREMELY_STRONG(
        "Extremely Strong",
        "Planet is exceptionally powerful and dominates the chart",
        150.0
    );

    companion object {
        fun fromPercentage(percentage: Double): StrengthRating {
            return entries.asReversed().firstOrNull { percentage >= it.minPercentage }
                ?: EXTREMELY_WEAK
        }
    }
}

data class SthanaBala(
    val ucchaBala: Double,
    val saptavargajaBala: Double,
    val ojhayugmarasyamsaBala: Double,
    val kendradiBala: Double,
    val drekkanaBala: Double
) {
    val total: Double = ucchaBala + saptavargajaBala + ojhayugmarasyamsaBala + kendradiBala + drekkanaBala
}

data class KalaBala(
    val nathonnathaBala: Double,
    val pakshaBala: Double,
    val tribhagaBala: Double,
    val horaAdiBala: Double,
    val ayanaBala: Double,
    val yuddhaBala: Double
) {
    val total: Double = nathonnathaBala + pakshaBala + tribhagaBala + horaAdiBala + ayanaBala + yuddhaBala
}

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
    val isStrong: Boolean = totalRupas >= requiredRupas

    fun getInterpretation(): String = buildString {
        appendLine("${planet.displayName} Shadbala Analysis")
        appendLine("═══════════════════════════════════════")
        appendLine()
        appendLine("Total Strength: ${FORMAT_TWO_DECIMAL.format(totalRupas)} Rupas")
        appendLine("Required Strength: ${FORMAT_TWO_DECIMAL.format(requiredRupas)} Rupas")
        appendLine("Percentage: ${FORMAT_ONE_DECIMAL.format(percentageOfRequired)}%")
        appendLine("Rating: ${strengthRating.displayName}")
        appendLine()
        appendLine("BREAKDOWN (in Virupas):")
        appendLine("───────────────────────────────────────")
        appendLine("1. Sthana Bala: ${FORMAT_TWO_DECIMAL.format(sthanaBala.total)}")
        appendLine("   • Uccha Bala: ${FORMAT_TWO_DECIMAL.format(sthanaBala.ucchaBala)}")
        appendLine("   • Saptavargaja Bala: ${FORMAT_TWO_DECIMAL.format(sthanaBala.saptavargajaBala)}")
        appendLine("   • Ojhayugmarasyamsa: ${FORMAT_TWO_DECIMAL.format(sthanaBala.ojhayugmarasyamsaBala)}")
        appendLine("   • Kendradi Bala: ${FORMAT_TWO_DECIMAL.format(sthanaBala.kendradiBala)}")
        appendLine("   • Drekkana Bala: ${FORMAT_TWO_DECIMAL.format(sthanaBala.drekkanaBala)}")
        appendLine()
        appendLine("2. Dig Bala: ${FORMAT_TWO_DECIMAL.format(digBala)}")
        appendLine()
        appendLine("3. Kala Bala: ${FORMAT_TWO_DECIMAL.format(kalaBala.total)}")
        appendLine("   • Nathonnatha: ${FORMAT_TWO_DECIMAL.format(kalaBala.nathonnathaBala)}")
        appendLine("   • Paksha Bala: ${FORMAT_TWO_DECIMAL.format(kalaBala.pakshaBala)}")
        appendLine("   • Tribhaga Bala: ${FORMAT_TWO_DECIMAL.format(kalaBala.tribhagaBala)}")
        appendLine("   • Hora/Dina/Masa/Varsha: ${FORMAT_TWO_DECIMAL.format(kalaBala.horaAdiBala)}")
        appendLine("   • Ayana Bala: ${FORMAT_TWO_DECIMAL.format(kalaBala.ayanaBala)}")
        appendLine("   • Yuddha Bala: ${FORMAT_TWO_DECIMAL.format(kalaBala.yuddhaBala)}")
        appendLine()
        appendLine("4. Chesta Bala: ${FORMAT_TWO_DECIMAL.format(chestaBala)}")
        appendLine()
        appendLine("5. Naisargika Bala: ${FORMAT_TWO_DECIMAL.format(naisargikaBala)}")
        appendLine()
        appendLine("6. Drik Bala: ${FORMAT_TWO_DECIMAL.format(drikBala)}")
    }

    companion object {
        private val FORMAT_TWO_DECIMAL = DecimalFormat("0.00")
        private val FORMAT_ONE_DECIMAL = DecimalFormat("0.0")
    }
}

data class ShadbalaAnalysis(
    val chartId: String,
    val planetaryStrengths: Map<Planet, PlanetaryShadbala>,
    val strongestPlanet: Planet,
    val weakestPlanet: Planet,
    val overallStrengthScore: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getPlanetsByStrength(): List<PlanetaryShadbala> =
        planetaryStrengths.values.sortedByDescending { it.totalRupas }

    fun getWeakPlanets(): List<PlanetaryShadbala> =
        planetaryStrengths.values.filter { !it.isStrong }

    fun getStrongPlanets(): List<PlanetaryShadbala> =
        planetaryStrengths.values.filter { it.isStrong }

    fun getSummaryInterpretation(): String {
        val strong = planetaryStrengths.values.count { it.isStrong }
        val weak = planetaryStrengths.values.size - strong

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
                val name = shadbala.planet.displayName.padEnd(10)
                val total = String.format("%.2f", shadbala.totalRupas)
                val required = String.format("%.2f", shadbala.requiredRupas)
                appendLine("$status $name: $total / $required")
            }
        }
    }
}

object ShadbalaCalculator {

    private const val VIRUPAS_PER_RUPA = 60.0
    private const val DEGREES_PER_CIRCLE = 360.0
    private const val DEGREES_PER_SIGN = 30.0

    private val SHADBALA_PLANETS = setOf(
        Planet.SUN, Planet.MOON, Planet.MARS,
        Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    private object ExaltationData {
        val degrees = mapOf(
            Planet.SUN to 10.0,
            Planet.MOON to 33.0,
            Planet.MARS to 298.0,
            Planet.MERCURY to 165.0,
            Planet.JUPITER to 95.0,
            Planet.VENUS to 357.0,
            Planet.SATURN to 200.0
        )

        val debilitationDegrees = degrees.mapValues { (_, deg) ->
            (deg + 180.0) % DEGREES_PER_CIRCLE
        }
    }

    private object MoolatrikonaData {
        data class Range(val sign: ZodiacSign, val startDegree: Double, val endDegree: Double)

        val ranges = mapOf(
            Planet.SUN to Range(ZodiacSign.LEO, 0.0, 20.0),
            Planet.MOON to Range(ZodiacSign.TAURUS, 4.0, 30.0),
            Planet.MARS to Range(ZodiacSign.ARIES, 0.0, 12.0),
            Planet.MERCURY to Range(ZodiacSign.VIRGO, 16.0, 20.0),
            Planet.JUPITER to Range(ZodiacSign.SAGITTARIUS, 0.0, 10.0),
            Planet.VENUS to Range(ZodiacSign.LIBRA, 0.0, 15.0),
            Planet.SATURN to Range(ZodiacSign.AQUARIUS, 0.0, 20.0)
        )

        fun isInMoolatrikona(planet: Planet, sign: ZodiacSign, degreeInSign: Double): Boolean {
            val range = ranges[planet] ?: return false
            return sign == range.sign && degreeInSign >= range.startDegree && degreeInSign <= range.endDegree
        }
    }

    private object NaturalStrength {
        val virupas = mapOf(
            Planet.SUN to 60.0,
            Planet.MOON to 51.43,
            Planet.VENUS to 42.86,
            Planet.JUPITER to 34.29,
            Planet.MERCURY to 25.71,
            Planet.MARS to 17.14,
            Planet.SATURN to 8.57
        )
    }

    private object RequiredStrength {
        val rupas = mapOf(
            Planet.SUN to 6.5,
            Planet.MOON to 6.0,
            Planet.MARS to 5.0,
            Planet.MERCURY to 7.0,
            Planet.JUPITER to 6.5,
            Planet.VENUS to 5.5,
            Planet.SATURN to 5.0
        )
    }

    private object DigBalaPositions {
        val strongestHouse = mapOf(
            Planet.SUN to 10,
            Planet.MOON to 4,
            Planet.MARS to 10,
            Planet.MERCURY to 1,
            Planet.JUPITER to 1,
            Planet.VENUS to 4,
            Planet.SATURN to 7
        )
    }

    private object SaptavargaWeights {
        const val D1_RASHI = 5.0
        const val D2_HORA = 2.5
        const val D3_DREKKANA = 3.0
        const val D7_SAPTAMSA = 2.5
        const val D9_NAVAMSA = 4.5
        const val D12_DWADASAMSA = 2.0
        const val D30_TRIMSAMSA = 1.0
    }

    private object VedicAspects {
        data class AspectInfo(val house: Int, val strength: Double)

        val specialAspects = mapOf(
            Planet.MARS to listOf(AspectInfo(4, 0.75), AspectInfo(8, 0.75)),
            Planet.JUPITER to listOf(AspectInfo(5, 1.0), AspectInfo(9, 1.0)),
            Planet.SATURN to listOf(AspectInfo(3, 0.75), AspectInfo(10, 0.75))
        )

        fun getAspectStrength(aspectingPlanet: Planet, houseDifference: Int): Double {
            if (houseDifference == 7) return 1.0

            val specialAspectList = specialAspects[aspectingPlanet] ?: return 0.0
            return specialAspectList.find { it.house == houseDifference }?.strength ?: 0.0
        }
    }

    private object PlanetaryWarBrightness {
        val order = listOf(Planet.VENUS, Planet.JUPITER, Planet.MERCURY, Planet.MARS, Planet.SATURN)

        fun getWinner(planet1: Planet, planet2: Planet): Planet {
            val index1 = order.indexOf(planet1)
            val index2 = order.indexOf(planet2)
            return if (index1 != -1 && index2 != -1 && index1 < index2) planet1 else planet2
        }
    }

    private class ChartContext(val chart: VedicChart) {
        val planetMap: Map<Planet, PlanetPosition> by lazy {
            chart.planetPositions.associateBy { it.planet }
        }

        val sunPosition: PlanetPosition? by lazy { planetMap[Planet.SUN] }
        val moonPosition: PlanetPosition? by lazy { planetMap[Planet.MOON] }

        val divisionalCharts: List<DivisionalChartData> by lazy {
            DivisionalChartCalculator.calculateAllDivisionalCharts(chart)
        }

        val divisionalChartMap: Map<DivisionalChartType, DivisionalChartData> by lazy {
            divisionalCharts.associateBy { it.chartType }
        }

        val lunarElongation: Double by lazy {
            val moonLong = moonPosition?.longitude
                ?: throw IllegalStateException("Moon position required for Shadbala calculation")
            val sunLong = sunPosition?.longitude
                ?: throw IllegalStateException("Sun position required for Shadbala calculation")
            normalizeDegree(moonLong - sunLong)
        }

        val isShuklaPacksha: Boolean by lazy { lunarElongation < 180.0 }

        val birthHour: Int = chart.birthData.dateTime.hour
        val isDay: Boolean = birthHour in 6..17

        val dayLord: Planet by lazy {
            getDayLordForWeekday(chart.birthData.dateTime.dayOfWeek.value)
        }

        val horaLord: Planet by lazy {
            calculateHoraLord(chart.birthData.dateTime, dayLord)
        }
    }

    fun calculateShadbala(chart: VedicChart): ShadbalaAnalysis {
        val context = ChartContext(chart)
        val strengths = mutableMapOf<Planet, PlanetaryShadbala>()

        for (position in chart.planetPositions) {
            if (position.planet in SHADBALA_PLANETS) {
                strengths[position.planet] = calculatePlanetShadbala(position, context)
            }
        }

        require(strengths.isNotEmpty()) { "No valid planets found for Shadbala calculation" }

        val sortedStrengths = strengths.values.sortedByDescending { it.totalRupas }
        val overallScore = strengths.values.map { it.percentageOfRequired }.average()

        return ShadbalaAnalysis(
            chartId = generateStableChartId(chart),
            planetaryStrengths = strengths.toMap(),
            strongestPlanet = sortedStrengths.first().planet,
            weakestPlanet = sortedStrengths.last().planet,
            overallStrengthScore = overallScore
        )
    }

    fun calculatePlanetShadbala(
        position: PlanetPosition,
        chart: VedicChart
    ): PlanetaryShadbala = calculatePlanetShadbala(position, ChartContext(chart))

    private fun calculatePlanetShadbala(
        position: PlanetPosition,
        context: ChartContext
    ): PlanetaryShadbala {
        val planet = position.planet

        val sthanaBala = calculateSthanaBala(position, context)
        val digBala = calculateDigBala(position)
        val kalaBala = calculateKalaBala(position, context)
        val chestaBala = calculateChestaBala(position)
        val naisargikaBala = NaturalStrength.virupas[planet] ?: 0.0
        val drikBala = calculateDrikBala(position, context)

        val totalVirupas = sthanaBala.total + digBala + kalaBala.total +
                chestaBala + naisargikaBala + drikBala
        val totalRupas = totalVirupas / VIRUPAS_PER_RUPA
        val requiredRupas = RequiredStrength.rupas[planet] ?: 5.0
        val percentage = (totalRupas / requiredRupas) * 100.0
        val rating = StrengthRating.fromPercentage(percentage)

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

    private fun calculateSthanaBala(position: PlanetPosition, context: ChartContext): SthanaBala {
        return SthanaBala(
            ucchaBala = calculateUcchaBala(position),
            saptavargajaBala = calculateSaptavargajaBala(position, context),
            ojhayugmarasyamsaBala = calculateOjhayugmarasyamsaBala(position),
            kendradiBala = calculateKendradiBala(position),
            drekkanaBala = calculateDrekkanaBala(position)
        )
    }

    private fun calculateUcchaBala(position: PlanetPosition): Double {
        val exaltDeg = ExaltationData.degrees[position.planet] ?: return 0.0
        val debilDeg = ExaltationData.debilitationDegrees[position.planet] ?: return 0.0

        var distance = normalizeDegree(position.longitude - debilDeg)
        if (distance > 180.0) distance = DEGREES_PER_CIRCLE - distance

        return (distance / 180.0) * 60.0
    }

    private fun calculateSaptavargajaBala(position: PlanetPosition, context: ChartContext): Double {
        val planet = position.planet
        var totalBala = 0.0

        totalBala += getVargaStrength(planet, position.sign, position.longitude % DEGREES_PER_SIGN) *
                SaptavargaWeights.D1_RASHI

        context.divisionalChartMap[DivisionalChartType.D2_HORA]?.let { chart ->
            chart.planetPositions.find { position: PlanetPosition -> position.planet == planet }?.let { pos ->
                totalBala += getVargaStrengthBasic(planet, pos.sign) * SaptavargaWeights.D2_HORA
            }
        }

        context.divisionalChartMap[DivisionalChartType.D3_DREKKANA]?.let { chart ->
            chart.planetPositions.find { position: PlanetPosition -> position.planet == planet }?.let { pos ->
                totalBala += getVargaStrengthBasic(planet, pos.sign) * SaptavargaWeights.D3_DREKKANA
            }
        }

        context.divisionalChartMap[DivisionalChartType.D7_SAPTAMSA]?.let { chart ->
            chart.planetPositions.find { position: PlanetPosition -> position.planet == planet }?.let { pos ->
                totalBala += getVargaStrengthBasic(planet, pos.sign) * SaptavargaWeights.D7_SAPTAMSA
            }
        }

        context.divisionalChartMap[DivisionalChartType.D9_NAVAMSA]?.let { chart ->
            chart.planetPositions.find { position: PlanetPosition -> position.planet == planet }?.let { pos ->
                totalBala += getVargaStrengthBasic(planet, pos.sign) * SaptavargaWeights.D9_NAVAMSA
            }
        }

        context.divisionalChartMap[DivisionalChartType.D12_DWADASAMSA]?.let { chart ->
            chart.planetPositions.find { position: PlanetPosition -> position.planet == planet }?.let { pos ->
                totalBala += getVargaStrengthBasic(planet, pos.sign) * SaptavargaWeights.D12_DWADASAMSA
            }
        }

        context.divisionalChartMap[DivisionalChartType.D30_TRIMSAMSA]?.let { chart ->
            chart.planetPositions.find { position: PlanetPosition -> position.planet == planet }?.let { pos ->
                totalBala += getVargaStrengthBasic(planet, pos.sign) * SaptavargaWeights.D30_TRIMSAMSA
            }
        }

        return totalBala
    }

    private fun getVargaStrength(planet: Planet, sign: ZodiacSign, degreeInSign: Double): Double {
        if (isExalted(planet, sign)) return 20.0
        if (MoolatrikonaData.isInMoolatrikona(planet, sign, degreeInSign)) return 22.5
        if (isOwnSign(planet, sign)) return 30.0

        return when (VedicAstrologyUtils.getNaturalRelationship(planet, sign.ruler)) {
            PlanetaryRelationship.FRIEND, PlanetaryRelationship.BEST_FRIEND -> 15.0
            PlanetaryRelationship.NEUTRAL -> 10.0
            PlanetaryRelationship.ENEMY, PlanetaryRelationship.BITTER_ENEMY -> 7.5
        }
    }

    private fun getVargaStrengthBasic(planet: Planet, sign: ZodiacSign): Double {
        if (isExalted(planet, sign)) return 20.0
        if (isOwnSign(planet, sign)) return 30.0

        return when (VedicAstrologyUtils.getNaturalRelationship(planet, sign.ruler)) {
            PlanetaryRelationship.FRIEND, PlanetaryRelationship.BEST_FRIEND -> 15.0
            PlanetaryRelationship.NEUTRAL -> 10.0
            PlanetaryRelationship.ENEMY, PlanetaryRelationship.BITTER_ENEMY -> 7.5
        }
    }

    private fun calculateOjhayugmarasyamsaBala(position: PlanetPosition): Double {
        val isOddSign = position.sign.number % 2 == 1

        return when (position.planet) {
            Planet.MOON, Planet.VENUS -> if (!isOddSign) 15.0 else 0.0
            else -> if (isOddSign) 15.0 else 0.0
        }
    }

    private fun calculateKendradiBala(position: PlanetPosition): Double {
        return when (position.house) {
            1, 4, 7, 10 -> 60.0
            2, 5, 8, 11 -> 30.0
            3, 6, 9, 12 -> 15.0
            else -> 0.0
        }
    }

    private fun calculateDrekkanaBala(position: PlanetPosition): Double {
        val degreeInSign = position.longitude % DEGREES_PER_SIGN
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

    private fun calculateDigBala(position: PlanetPosition): Double {
        val strongHouse = DigBalaPositions.strongestHouse[position.planet] ?: return 0.0
        val currentHouse = position.house

        var distance = abs(currentHouse - strongHouse)
        if (distance > 6) distance = 12 - distance

        return (6 - distance) * 10.0
    }

    private fun calculateKalaBala(position: PlanetPosition, context: ChartContext): KalaBala {
        return KalaBala(
            nathonnathaBala = calculateNathonnathaBala(position, context),
            pakshaBala = calculatePakshaBala(position, context),
            tribhagaBala = calculateTribhagaBala(position, context),
            horaAdiBala = calculateHoraAdiBala(position, context),
            ayanaBala = calculateAyanaBala(position),
            yuddhaBala = calculateYuddhaBala(position, context)
        )
    }

    private fun calculateNathonnathaBala(position: PlanetPosition, context: ChartContext): Double {
        return when (position.planet) {
            Planet.MERCURY -> 60.0
            Planet.SUN, Planet.JUPITER, Planet.VENUS -> if (context.isDay) 60.0 else 0.0
            Planet.MOON, Planet.MARS, Planet.SATURN -> if (!context.isDay) 60.0 else 0.0
            else -> 30.0
        }
    }

    private fun calculatePakshaBala(position: PlanetPosition, context: ChartContext): Double {
        val elongation = context.lunarElongation

        val phaseStrength = if (elongation < 180.0) {
            elongation / 180.0 * 60.0
        } else {
            (DEGREES_PER_CIRCLE - elongation) / 180.0 * 60.0
        }

        val isBenefic = position.planet in setOf(
            Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY
        )

        return if ((isBenefic && context.isShuklaPacksha) || (!isBenefic && !context.isShuklaPacksha)) {
            phaseStrength
        } else {
            60.0 - phaseStrength
        }
    }

    private fun calculateTribhagaBala(position: PlanetPosition, context: ChartContext): Double {
        val hour = context.birthHour

        val periodLord = if (context.isDay) {
            when {
                hour < 10 -> Planet.MERCURY
                hour < 14 -> Planet.SUN
                else -> Planet.SATURN
            }
        } else {
            when {
                hour in 18..21 -> Planet.MOON
                hour >= 22 || hour < 2 -> Planet.VENUS
                else -> Planet.MARS
            }
        }

        return if (position.planet == periodLord) 60.0 else 0.0
    }

    private fun calculateHoraAdiBala(position: PlanetPosition, context: ChartContext): Double {
        var bala = 0.0

        if (position.planet == context.dayLord) bala += 15.0
        if (position.planet == context.horaLord) bala += 15.0

        context.moonPosition?.let { moon ->
            if (position.planet == moon.sign.ruler) bala += 10.0
        }

        if (position.planet == Planet.SUN) bala += 5.0

        return bala
    }

    private fun calculateAyanaBala(position: PlanetPosition): Double {
        val declination = 23.45 * sin((position.longitude - 80.0) * PI / 180.0)

        return when (position.planet) {
            Planet.SUN, Planet.MARS, Planet.JUPITER ->
                (30.0 + declination).coerceIn(0.0, 60.0)
            Planet.MOON, Planet.VENUS, Planet.SATURN ->
                (30.0 - declination).coerceIn(0.0, 60.0)
            else -> 30.0
        }
    }

    private fun calculateYuddhaBala(position: PlanetPosition, context: ChartContext): Double {
        if (position.planet !in WAR_CAPABLE_PLANETS) return 0.0

        for ((planet, otherPos) in context.planetMap) {
            if (planet == position.planet) continue
            if (planet !in WAR_CAPABLE_PLANETS) continue

            val distance = angularDistance(position.longitude, otherPos.longitude)
            if (distance <= 1.0) {
                val winner = PlanetaryWarBrightness.getWinner(position.planet, planet)
                return if (winner == position.planet) 30.0 else -30.0
            }
        }

        return 0.0
    }

    private fun calculateChestaBala(position: PlanetPosition): Double {
        if (position.planet in setOf(Planet.SUN, Planet.MOON)) return 0.0

        return when {
            position.isRetrograde -> 60.0
            position.speed < 0.01 -> 50.0
            position.speed < 0.5 -> 40.0
            position.speed < 1.0 -> 30.0
            else -> 20.0
        }
    }

    private fun calculateDrikBala(position: PlanetPosition, context: ChartContext): Double {
        var bala = 0.0

        for ((planet, aspectingPos) in context.planetMap) {
            if (planet == position.planet) continue

            val houseDiff = calculateHouseDifference(aspectingPos.house, position.house)
            val aspectStrength = VedicAspects.getAspectStrength(planet, houseDiff)

            if (aspectStrength > 0.0) {
                val aspectValue = when (planet) {
                    Planet.JUPITER, Planet.VENUS -> aspectStrength * 15.0
                    Planet.MOON -> if (!aspectingPos.isRetrograde) aspectStrength * 10.0 else aspectStrength * 5.0
                    Planet.MERCURY -> aspectStrength * 8.0
                    Planet.SUN -> -aspectStrength * 5.0
                    Planet.MARS, Planet.SATURN -> -aspectStrength * 10.0
                    else -> 0.0
                }
                bala += aspectValue
            }
        }

        return bala.coerceIn(-30.0, 60.0)
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            else -> false
        }
    }

    private fun isOwnSign(planet: Planet, sign: ZodiacSign): Boolean = sign.ruler == planet

    private fun getDayLordForWeekday(dayOfWeek: Int): Planet {
        return when (dayOfWeek) {
            1 -> Planet.MOON
            2 -> Planet.MARS
            3 -> Planet.MERCURY
            4 -> Planet.JUPITER
            5 -> Planet.VENUS
            6 -> Planet.SATURN
            7 -> Planet.SUN
            else -> Planet.SUN
        }
    }

    private fun calculateHoraLord(dateTime: LocalDateTime, dayLord: Planet): Planet {
        val hourSequence = listOf(
            Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON,
            Planet.SATURN, Planet.JUPITER, Planet.MARS
        )

        val startIndex = hourSequence.indexOf(dayLord)
        val hour = dateTime.hour
        val horasSinceSunrise = if (hour >= 6) hour - 6 else hour + 18
        val horaIndex = (startIndex + horasSinceSunrise) % 7

        return hourSequence[horaIndex]
    }

    private fun calculateHouseDifference(fromHouse: Int, toHouse: Int): Int {
        var diff = toHouse - fromHouse
        if (diff <= 0) diff += 12
        return diff
    }

    private fun normalizeDegree(degree: Double): Double {
        var result = degree % DEGREES_PER_CIRCLE
        if (result < 0) result += DEGREES_PER_CIRCLE
        return result
    }

    private fun angularDistance(deg1: Double, deg2: Double): Double {
        val diff = abs(deg1 - deg2)
        return if (diff > 180.0) DEGREES_PER_CIRCLE - diff else diff
    }

    private val WAR_CAPABLE_PLANETS = setOf(
        Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN
    )

    private fun generateStableChartId(chart: VedicChart): String {
        val birthData = chart.birthData
        return "${birthData.name}-${birthData.dateTime}-${birthData.latitude}-${birthData.longitude}".replace(
            Regex("[^a-zA-Z0-9-]"),
            "_"
        )
    }
}
