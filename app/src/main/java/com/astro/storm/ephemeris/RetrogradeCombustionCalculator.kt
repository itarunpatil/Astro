package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object RetrogradeCombustionCalculator {

    private const val DEGREES_PER_SIGN = 30.0
    private const val TOTAL_SIGNS = 12
    private const val GRAHA_YUDDHA_ORB = 1.0
    private const val CAZIMI_ORB = 17.0 / 60.0

    private val COMBUSTION_DEGREES = mapOf(
        Planet.MOON to CombustionDegrees(12.0, 12.0),
        Planet.MARS to CombustionDegrees(17.0, 17.0),
        Planet.MERCURY to CombustionDegrees(14.0, 12.0),
        Planet.JUPITER to CombustionDegrees(11.0, 11.0),
        Planet.VENUS to CombustionDegrees(10.0, 8.0),
        Planet.SATURN to CombustionDegrees(15.0, 15.0)
    )

    private val STATIONARY_THRESHOLDS = mapOf(
        Planet.MERCURY to 0.08,
        Planet.VENUS to 0.06,
        Planet.MARS to 0.04,
        Planet.JUPITER to 0.015,
        Planet.SATURN to 0.008
    )

    private val AVERAGE_DAILY_MOTION = mapOf(
        Planet.SUN to 0.9856,
        Planet.MOON to 13.1764,
        Planet.MERCURY to 1.383,
        Planet.VENUS to 1.2,
        Planet.MARS to 0.524,
        Planet.JUPITER to 0.083,
        Planet.SATURN to 0.034,
        Planet.RAHU to -0.053,
        Planet.KETU to -0.053
    )

    private val PLANETARY_BRIGHTNESS_RANK = mapOf(
        Planet.VENUS to 6,
        Planet.JUPITER to 5,
        Planet.MARS to 4,
        Planet.MERCURY to 3,
        Planet.SATURN to 2
    )

    data class CombustionDegrees(
        val direct: Double,
        val retrograde: Double
    ) {
        fun getEffective(isRetrograde: Boolean): Double =
            if (isRetrograde) retrograde else direct
    }

    enum class RetrogradeStatus(val displayName: String, val symbol: String, val strengthMultiplier: Double) {
        DIRECT("Direct", "D", 1.0),
        RETROGRADE("Retrograde", "R", 1.25),
        STATIONARY_RETROGRADE("Stationary Retrograde", "SR", 1.75),
        STATIONARY_DIRECT("Stationary Direct", "SD", 1.75),
        ALWAYS_RETROGRADE("Perpetual Retrograde", "℞", 1.0)
    }

    enum class CombustionStatus(val displayName: String, val symbol: String, val strengthFactor: Double) {
        NOT_COMBUST("Not Combust", "", 1.0),
        APPROACHING("Approaching Combustion", "→☉", 0.7),
        COMBUST("Combust", "☉", 0.25),
        DEEP_COMBUST("Deep Combustion", "●☉", 0.1),
        CAZIMI("Cazimi", "♡", 1.5),
        SEPARATING("Separating", "☉→", 0.55)
    }

    enum class SpeedStatus(val displayName: String, val strengthFactor: Double) {
        ATHI_SHEEGHRA("Very Fast", 1.25),
        SHEEGHRA("Fast", 1.1),
        SAMA("Normal", 1.0),
        MANDA("Slow", 0.85),
        ATHI_MANDA("Very Slow", 0.7),
        STHIRA("Stationary", 1.5),
        VAKRA("Retrograde Motion", 1.0)
    }

    enum class WarAdvantage(val displayName: String) {
        NORTHERN_LATITUDE("Northern Latitude"),
        BRIGHTNESS("Greater Brightness"),
        COMBINED("Both Factors"),
        INDETERMINATE("Evenly Matched")
    }

    data class PlanetCondition(
        val planet: Planet,
        val retrogradeStatus: RetrogradeStatus,
        val combustionStatus: CombustionStatus,
        val speedStatus: SpeedStatus,
        val distanceFromSun: Double?,
        val dailyMotion: Double,
        val relativeSpeed: Double,
        val isInPlanetaryWar: Boolean,
        val warData: PlanetaryWarResult?,
        val degreesInSign: Double,
        val signNumber: Int,
        val nakshatra: Int,
        val nakshatraPada: Int
    ) {
        val combinedStrengthFactor: Double
            get() {
                var factor = combustionStatus.strengthFactor
                factor *= speedStatus.strengthFactor

                when (retrogradeStatus) {
                    RetrogradeStatus.STATIONARY_RETROGRADE,
                    RetrogradeStatus.STATIONARY_DIRECT -> factor *= retrogradeStatus.strengthMultiplier
                    RetrogradeStatus.RETROGRADE -> factor *= retrogradeStatus.strengthMultiplier
                    else -> {}
                }

                if (isInPlanetaryWar) {
                    factor *= if (warData?.isWinner(planet) == true) 1.1 else 0.5
                }

                return factor.coerceIn(0.05, 2.5)
            }

        val isAfflicted: Boolean
            get() = combustionStatus in listOf(CombustionStatus.COMBUST, CombustionStatus.DEEP_COMBUST) ||
                    (isInPlanetaryWar && warData?.isLoser(planet) == true)

        val isStrong: Boolean
            get() = combustionStatus == CombustionStatus.CAZIMI ||
                    retrogradeStatus in listOf(RetrogradeStatus.STATIONARY_RETROGRADE, RetrogradeStatus.STATIONARY_DIRECT) ||
                    speedStatus == SpeedStatus.STHIRA ||
                    (isInPlanetaryWar && warData?.isWinner(planet) == true)
    }

    data class PlanetaryWarResult(
        val planet1: Planet,
        val planet2: Planet,
        val angularSeparation: Double,
        val planet1Latitude: Double?,
        val planet2Latitude: Double?,
        val winner: Planet,
        val loser: Planet,
        val winnerAdvantage: WarAdvantage,
        val intensity: Double
    ) {
        fun isWinner(planet: Planet): Boolean = planet == winner
        fun isLoser(planet: Planet): Boolean = planet == loser

        val isIntense: Boolean get() = intensity >= 0.7
    }

    data class RetrogradePeriod(
        val planet: Planet,
        val startDate: LocalDate,
        val stationRetroDate: LocalDate,
        val stationDirectDate: LocalDate,
        val endDate: LocalDate,
        val entryLongitude: Double,
        val stationRetroLongitude: Double,
        val stationDirectLongitude: Double,
        val exitLongitude: Double,
        val durationDays: Long,
        val signsCovered: List<Int>,
        val degreesTraversed: Double
    )

    data class CombustionPeriod(
        val planet: Planet,
        val enterDate: LocalDate,
        val peakDate: LocalDate,
        val exitDate: LocalDate,
        val minimumSeparation: Double,
        val peakStatus: CombustionStatus,
        val durationDays: Long,
        val wasRetrogradeDuring: Boolean,
        val entrySign: Int,
        val exitSign: Int
    )

    data class PlanetaryConditionAnalysis(
        val analysisDateTime: LocalDateTime,
        val planetConditions: Map<Planet, PlanetCondition>,
        val retrogradePlanets: List<Planet>,
        val combustPlanets: List<Planet>,
        val stationaryPlanets: List<Planet>,
        val planetaryWars: List<PlanetaryWarResult>,
        val strongestPlanet: Planet?,
        val weakestPlanet: Planet?,
        val overallMaleficPressure: Double
    ) {
        fun getCondition(planet: Planet): PlanetCondition? = planetConditions[planet]
        fun isRetrograde(planet: Planet): Boolean = retrogradePlanets.contains(planet)
        fun isCombust(planet: Planet): Boolean = combustPlanets.contains(planet)
        fun isStationary(planet: Planet): Boolean = stationaryPlanets.contains(planet)
    }

    fun analyzePlanetaryConditions(chart: VedicChart): PlanetaryConditionAnalysis {
        val conditions = mutableMapOf<Planet, PlanetCondition>()
        val sunPosition = chart.planetPositions.find { it.planet == Planet.SUN }

        for (position in chart.planetPositions) {
            val condition = analyzeIndividualPlanet(position, sunPosition)
            conditions[position.planet] = condition
        }

        val planetaryWars = detectPlanetaryWars(chart.planetPositions)

        val finalConditions = conditions.mapValues { (planet, condition) ->
            val warInvolvement = planetaryWars.find { it.planet1 == planet || it.planet2 == planet }
            if (warInvolvement != null) {
                condition.copy(isInPlanetaryWar = true, warData = warInvolvement)
            } else {
                condition
            }
        }

        val rankedPlanets = finalConditions.values
            .filter { it.planet !in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO) }
            .sortedByDescending { it.combinedStrengthFactor }

        val maleficPressure = calculateMaleficPressure(finalConditions.values.toList())

        return PlanetaryConditionAnalysis(
            analysisDateTime = chart.birthData.dateTime,
            planetConditions = finalConditions,
            retrogradePlanets = finalConditions.values
                .filter { it.retrogradeStatus in listOf(RetrogradeStatus.RETROGRADE, RetrogradeStatus.STATIONARY_RETROGRADE) }
                .map { it.planet },
            combustPlanets = finalConditions.values
                .filter { it.combustionStatus in listOf(CombustionStatus.COMBUST, CombustionStatus.DEEP_COMBUST) }
                .map { it.planet },
            stationaryPlanets = finalConditions.values
                .filter { it.retrogradeStatus in listOf(RetrogradeStatus.STATIONARY_RETROGRADE, RetrogradeStatus.STATIONARY_DIRECT) }
                .map { it.planet },
            planetaryWars = planetaryWars,
            strongestPlanet = rankedPlanets.firstOrNull()?.planet,
            weakestPlanet = rankedPlanets.lastOrNull()?.planet,
            overallMaleficPressure = maleficPressure
        )
    }

    private fun analyzeIndividualPlanet(
        position: PlanetPosition,
        sunPosition: PlanetPosition?
    ): PlanetCondition {
        val retrogradeStatus = determineRetrogradeStatus(position)
        val speedStatus = determineSpeedStatus(position)

        val distanceFromSun = if (sunPosition != null && position.planet != Planet.SUN) {
            calculateAngularDistance(position.longitude, sunPosition.longitude)
        } else null

        val combustionStatus = if (distanceFromSun != null) {
            determineCombustionStatus(
                planet = position.planet,
                distance = distanceFromSun,
                isRetrograde = position.isRetrograde,
                planetSpeed = position.speed,
                sunSpeed = sunPosition?.speed ?: 0.9856
            )
        } else {
            CombustionStatus.NOT_COMBUST
        }

        val signNumber = getSignNumber(position.longitude)
        val degreesInSign = position.longitude % DEGREES_PER_SIGN
        val nakshatra = getNakshatraNumber(position.longitude)
        val nakshatraPada = getNakshatraPada(position.longitude)
        val relativeSpeed = calculateRelativeSpeed(position)

        return PlanetCondition(
            planet = position.planet,
            retrogradeStatus = retrogradeStatus,
            combustionStatus = combustionStatus,
            speedStatus = speedStatus,
            distanceFromSun = distanceFromSun,
            dailyMotion = position.speed,
            relativeSpeed = relativeSpeed,
            isInPlanetaryWar = false,
            warData = null,
            degreesInSign = degreesInSign,
            signNumber = signNumber,
            nakshatra = nakshatra,
            nakshatraPada = nakshatraPada
        )
    }

    private fun determineRetrogradeStatus(position: PlanetPosition): RetrogradeStatus {
        when (position.planet) {
            Planet.SUN, Planet.MOON -> return RetrogradeStatus.DIRECT
            Planet.RAHU, Planet.KETU -> return RetrogradeStatus.ALWAYS_RETROGRADE
            else -> {}
        }

        val threshold = STATIONARY_THRESHOLDS[position.planet] ?: 0.05
        val absSpeed = abs(position.speed)

        return when {
            absSpeed <= threshold && position.speed <= 0 -> RetrogradeStatus.STATIONARY_RETROGRADE
            absSpeed <= threshold && position.speed > 0 -> RetrogradeStatus.STATIONARY_DIRECT
            position.speed < 0 || position.isRetrograde -> RetrogradeStatus.RETROGRADE
            else -> RetrogradeStatus.DIRECT
        }
    }

    private fun determineSpeedStatus(position: PlanetPosition): SpeedStatus {
        when (position.planet) {
            Planet.SUN, Planet.MOON -> return SpeedStatus.SAMA
            Planet.RAHU, Planet.KETU -> return SpeedStatus.SAMA
            else -> {}
        }

        val avgMotion = AVERAGE_DAILY_MOTION[position.planet] ?: return SpeedStatus.SAMA
        val threshold = STATIONARY_THRESHOLDS[position.planet] ?: 0.05
        val absSpeed = abs(position.speed)

        if (absSpeed <= threshold) {
            return SpeedStatus.STHIRA
        }

        if (position.speed < 0) {
            return SpeedStatus.VAKRA
        }

        val ratio = absSpeed / abs(avgMotion)

        return when {
            ratio >= 1.25 -> SpeedStatus.ATHI_SHEEGHRA
            ratio >= 1.08 -> SpeedStatus.SHEEGHRA
            ratio >= 0.92 -> SpeedStatus.SAMA
            ratio >= 0.75 -> SpeedStatus.MANDA
            else -> SpeedStatus.ATHI_MANDA
        }
    }

    private fun calculateRelativeSpeed(position: PlanetPosition): Double {
        val avgMotion = AVERAGE_DAILY_MOTION[position.planet] ?: return 1.0
        return if (avgMotion != 0.0) position.speed / avgMotion else 1.0
    }

    private fun determineCombustionStatus(
        planet: Planet,
        distance: Double,
        isRetrograde: Boolean,
        planetSpeed: Double,
        sunSpeed: Double
    ): CombustionStatus {
        if (planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return CombustionStatus.NOT_COMBUST
        }

        val combustionDegrees = COMBUSTION_DEGREES[planet] ?: return CombustionStatus.NOT_COMBUST
        val effectiveOrb = combustionDegrees.getEffective(isRetrograde)

        if (distance <= CAZIMI_ORB) {
            return CombustionStatus.CAZIMI
        }

        val halfOrb = effectiveOrb / 2.0
        val outerOrb = effectiveOrb * 1.4

        val isApproaching = (planetSpeed - sunSpeed) > 0 && distance > CAZIMI_ORB

        return when {
            distance <= halfOrb -> CombustionStatus.DEEP_COMBUST
            distance <= effectiveOrb -> CombustionStatus.COMBUST
            distance <= outerOrb && isApproaching -> CombustionStatus.APPROACHING
            distance <= outerOrb && !isApproaching -> CombustionStatus.SEPARATING
            else -> CombustionStatus.NOT_COMBUST
        }
    }

    private fun detectPlanetaryWars(positions: List<PlanetPosition>): List<PlanetaryWarResult> {
        val wars = mutableListOf<PlanetaryWarResult>()

        val warCapable = positions.filter {
            it.planet in listOf(Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN)
        }

        for (i in warCapable.indices) {
            for (j in i + 1 until warCapable.size) {
                val p1 = warCapable[i]
                val p2 = warCapable[j]

                val separation = calculateAngularDistance(p1.longitude, p2.longitude)

                if (separation <= GRAHA_YUDDHA_ORB) {
                    val warResult = resolveWarOutcome(p1, p2, separation)
                    wars.add(warResult)
                }
            }
        }

        return wars
    }

    private fun resolveWarOutcome(
        pos1: PlanetPosition,
        pos2: PlanetPosition,
        separation: Double
    ): PlanetaryWarResult {
        val lat1 = pos1.latitude ?: 0.0
        val lat2 = pos2.latitude ?: 0.0

        val hasValidLatitudes = pos1.latitude != null && pos2.latitude != null
        val latitudeDifference = abs(lat1 - lat2)

        val brightness1 = PLANETARY_BRIGHTNESS_RANK[pos1.planet] ?: 0
        val brightness2 = PLANETARY_BRIGHTNESS_RANK[pos2.planet] ?: 0

        val winner: Planet
        val advantage: WarAdvantage

        when {
            hasValidLatitudes && latitudeDifference > 0.05 -> {
                val northernPlanet = if (lat1 > lat2) pos1.planet else pos2.planet
                val brighterWins = (lat1 > lat2) == (brightness1 > brightness2)

                winner = northernPlanet
                advantage = if (brighterWins && brightness1 != brightness2) {
                    WarAdvantage.COMBINED
                } else {
                    WarAdvantage.NORTHERN_LATITUDE
                }
            }
            brightness1 != brightness2 -> {
                winner = if (brightness1 > brightness2) pos1.planet else pos2.planet
                advantage = WarAdvantage.BRIGHTNESS
            }
            else -> {
                winner = if (pos1.longitude > pos2.longitude) pos1.planet else pos2.planet
                advantage = WarAdvantage.INDETERMINATE
            }
        }

        val loser = if (winner == pos1.planet) pos2.planet else pos1.planet
        val intensity = (1.0 - separation / GRAHA_YUDDHA_ORB).coerceIn(0.0, 1.0)

        return PlanetaryWarResult(
            planet1 = pos1.planet,
            planet2 = pos2.planet,
            angularSeparation = separation,
            planet1Latitude = pos1.latitude,
            planet2Latitude = pos2.latitude,
            winner = winner,
            loser = loser,
            winnerAdvantage = advantage,
            intensity = intensity
        )
    }

    private fun calculateMaleficPressure(conditions: List<PlanetCondition>): Double {
        var pressure = 0.0

        for (condition in conditions) {
            when (condition.combustionStatus) {
                CombustionStatus.DEEP_COMBUST -> pressure += 0.25
                CombustionStatus.COMBUST -> pressure += 0.15
                else -> {}
            }

            if (condition.isInPlanetaryWar && condition.warData?.isLoser(condition.planet) == true) {
                pressure += 0.2
            }

            if (condition.speedStatus in listOf(SpeedStatus.MANDA, SpeedStatus.ATHI_MANDA)) {
                pressure += 0.05
            }
        }

        return pressure.coerceIn(0.0, 1.0)
    }

    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(normalizeAngle(long1) - normalizeAngle(long2))
        return if (diff > 180.0) 360.0 - diff else diff
    }

    private fun normalizeAngle(angle: Double): Double {
        return ((angle % 360.0) + 360.0) % 360.0
    }

    private fun getSignNumber(longitude: Double): Int {
        return ((normalizeAngle(longitude) / DEGREES_PER_SIGN).toInt() % TOTAL_SIGNS) + 1
    }

    private fun getNakshatraNumber(longitude: Double): Int {
        return ((normalizeAngle(longitude) / (360.0 / 27.0)).toInt() % 27) + 1
    }

    private fun getNakshatraPada(longitude: Double): Int {
        val nakshatraSpan = 360.0 / 27.0
        val positionInNakshatra = normalizeAngle(longitude) % nakshatraSpan
        return ((positionInNakshatra / (nakshatraSpan / 4.0)).toInt() % 4) + 1
    }

    fun getRetrogradeInterpretation(planet: Planet, status: RetrogradeStatus): String {
        if (status == RetrogradeStatus.DIRECT) {
            return "${planet.displayName} moves direct, expressing its significations naturally and externally."
        }

        if (status == RetrogradeStatus.ALWAYS_RETROGRADE) {
            return "${planet.displayName} maintains perpetual retrograde motion as a shadow planet (Chhaya Graha)."
        }

        val interpretation = when (planet) {
            Planet.MERCURY -> "Mercury retrograde turns communication, intellect, and commerce inward. Review contracts, revisit past connections, refine rather than initiate. Travel requires extra planning."
            Planet.VENUS -> "Venus retrograde invokes reassessment of relationships, values, and pleasures. Past loves may resurface. Avoid major relationship commitments or luxury purchases."
            Planet.MARS -> "Mars retrograde internalizes energy and drive. Past conflicts demand resolution. Direct action faces obstacles. Strategic patience over force."
            Planet.JUPITER -> "Jupiter retrograde deepens internal wisdom and spiritual seeking. External expansion pauses while inner growth accelerates. Review beliefs and philosophies."
            Planet.SATURN -> "Saturn retrograde intensifies karmic review. Past responsibilities resurface demanding attention. Structure and discipline must come from within."
            else -> "Planetary energy is internalized and intensified during retrograde."
        }

        val stationaryAddition = when (status) {
            RetrogradeStatus.STATIONARY_RETROGRADE -> " Currently stationary before retrograde - energy concentrates powerfully at this zodiacal degree."
            RetrogradeStatus.STATIONARY_DIRECT -> " Currently stationary before direct motion - a pivotal turning point where decisions crystallize."
            else -> ""
        }

        return interpretation + stationaryAddition
    }

    fun getCombustionInterpretation(planet: Planet, status: CombustionStatus): String {
        return when (status) {
            CombustionStatus.CAZIMI -> {
                "${planet.displayName} sits in the heart of the Sun (Cazimi). This exceptional position grants royal favor and heightened power. The planet's significations are blessed and strengthened rather than diminished."
            }
            CombustionStatus.DEEP_COMBUST -> {
                val meaning = getBaseCombustionMeaning(planet)
                "$meaning Deep combustion severely impairs the planet's ability to deliver its significations. Effects require conscious attention and remedial measures."
            }
            CombustionStatus.COMBUST -> {
                val meaning = getBaseCombustionMeaning(planet)
                "$meaning The Sun's brilliance overpowers this planet, weakening its natural expression."
            }
            CombustionStatus.APPROACHING -> {
                "${planet.displayName} approaches combustion. Its significations begin dimming. Prepare for a period of internalization in matters ruled by this planet."
            }
            CombustionStatus.SEPARATING -> {
                "${planet.displayName} separates from the Sun's rays. Strength gradually returns. Matters ruled by this planet emerge from a period of obscuration."
            }
            CombustionStatus.NOT_COMBUST -> {
                "${planet.displayName} stands clear of the Sun's burning rays, free to express its full potential."
            }
        }
    }

    private fun getBaseCombustionMeaning(planet: Planet): String {
        return when (planet) {
            Planet.MOON -> "Moon combust disturbs emotional equilibrium and mental peace. Relationship with mother may face challenges. Mind seeks clarity through introspection."
            Planet.MARS -> "Mars combust suppresses courage, vitality, and initiative. Anger may smolder beneath the surface. Physical energy requires careful channeling."
            Planet.MERCURY -> "Mercury combust challenges clear thinking, communication, and commercial success. Words require extra care. Analysis turns inward."
            Planet.JUPITER -> "Jupiter combust diminishes wisdom, fortune, and spiritual guidance. Teachers and guides may seem distant. Faith develops through personal trial."
            Planet.VENUS -> "Venus combust tests relationships, creativity, and material comforts. Self-worth undergoes internal reassessment. Beauty must be found within."
            Planet.SATURN -> "Saturn combust challenges discipline, structure, and karmic navigation. Lessons intensify while tools seem weakened. Patience becomes paramount."
            else -> "This planet's significations face temporary obscuration by solar intensity."
        }
    }

    fun getPlanetaryWarInterpretation(war: PlanetaryWarResult): String {
        val winner = war.winner.displayName
        val loser = war.loser.displayName

        val intensityDesc = when {
            war.intensity >= 0.8 -> "extremely close and intense"
            war.intensity >= 0.5 -> "significant"
            else -> "notable but moderate"
        }

        val advantageDesc = when (war.winnerAdvantage) {
            WarAdvantage.NORTHERN_LATITUDE -> "by virtue of higher northern celestial latitude"
            WarAdvantage.BRIGHTNESS -> "through greater natural luminosity"
            WarAdvantage.COMBINED -> "with advantages in both celestial position and brightness"
            WarAdvantage.INDETERMINATE -> "though the contest remains finely balanced"
        }

        return buildString {
            append("Graha Yuddha (Planetary War): $winner versus $loser.\n")
            append("Separation: ${String.format("%.3f", war.angularSeparation)}° - $intensityDesc combat.\n")
            append("$winner prevails $advantageDesc.\n\n")
            append("$loser's significations face obstruction and may manifest with difficulty, delay, or distortion. ")
            append("$winner gains dominance but absorbs martial qualities during this period, ")
            append("potentially expressing its significations more aggressively or forcefully.")
        }
    }

    fun getSpeedInterpretation(planet: Planet, status: SpeedStatus): String {
        return when (status) {
            SpeedStatus.ATHI_SHEEGHRA -> "${planet.displayName} moves very fast (Athi Sheeghra). Its significations manifest rapidly and may pass quickly. Events unfold with urgency."
            SpeedStatus.SHEEGHRA -> "${planet.displayName} moves swiftly (Sheeghra). Its results come readily and with momentum."
            SpeedStatus.SAMA -> "${planet.displayName} moves at normal speed (Sama Gati). Its significations unfold in natural, expected timeframes."
            SpeedStatus.MANDA -> "${planet.displayName} moves slowly (Manda). Its results require patience. Matters take longer to manifest but may prove more enduring."
            SpeedStatus.ATHI_MANDA -> "${planet.displayName} moves very slowly (Athi Manda). Significant delays in its significations. Patience and persistence essential."
            SpeedStatus.STHIRA -> "${planet.displayName} stands virtually stationary (Sthira). Tremendous concentration of energy at this degree. Events crystallize with lasting significance."
            SpeedStatus.VAKRA -> "${planet.displayName} moves in retrograde (Vakra). Energy directed inward. Review and revision of its significations."
        }
    }
}

class RetrogradeEphemerisCalculator(context: Context) : AutoCloseable {

    private val swissEph: SwissEph
    private val ephePath: String

    init {
        ephePath = context.filesDir.absolutePath + "/ephe"
        swissEph = SwissEph()
        swissEph.swe_set_ephe_path(ephePath)
        swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
    }

    fun findNextRetrogradePeriod(
        planet: Planet,
        fromDate: LocalDate,
        maxSearchDays: Int = 800
    ): RetrogradeCombustionCalculator.RetrogradePeriod? {
        if (planet in listOf(Planet.SUN, Planet.MOON, Planet.RAHU, Planet.KETU)) {
            return null
        }

        var currentDate = fromDate
        var previousSpeed = getSpeed(planet, toJulianDay(currentDate))
        var retroStart: LocalDate? = null
        var retroStartLong: Double? = null
        var stationRetro: LocalDate? = null
        var stationRetroLong: Double? = null
        val signsCovered = mutableSetOf<Int>()

        repeat(maxSearchDays) {
            val jd = toJulianDay(currentDate)
            val speed = getSpeed(planet, jd)
            val longitude = getLongitude(planet, jd)

            if (previousSpeed >= 0 && speed < 0) {
                stationRetro = refineStationDate(planet, currentDate.minusDays(5), currentDate.plusDays(2))
                stationRetroLong = getLongitude(planet, toJulianDay(stationRetro ?: currentDate))
                retroStart = currentDate
                retroStartLong = longitude
                signsCovered.clear()
                signsCovered.add(getSign(longitude))
            }

            if (retroStart != null) {
                signsCovered.add(getSign(longitude))
            }

            if (previousSpeed < 0 && speed >= 0 && retroStart != null) {
                val stationDirect = refineStationDate(planet, currentDate.minusDays(5), currentDate.plusDays(2))
                val stationDirectLong = getLongitude(planet, toJulianDay(stationDirect ?: currentDate))

                val startLong = stationRetroLong ?: retroStartLong!!
                val endLong = stationDirectLong
                val traversed = if (startLong > endLong) startLong - endLong else 360.0 - endLong + startLong

                return RetrogradeCombustionCalculator.RetrogradePeriod(
                    planet = planet,
                    startDate = retroStart!!,
                    stationRetroDate = stationRetro ?: retroStart!!,
                    stationDirectDate = stationDirect ?: currentDate,
                    endDate = currentDate,
                    entryLongitude = retroStartLong!!,
                    stationRetroLongitude = stationRetroLong ?: retroStartLong!!,
                    stationDirectLongitude = stationDirectLong,
                    exitLongitude = longitude,
                    durationDays = ChronoUnit.DAYS.between(retroStart, currentDate),
                    signsCovered = signsCovered.sorted(),
                    degreesTraversed = traversed
                )
            }

            previousSpeed = speed
            currentDate = currentDate.plusDays(1)
        }

        return null
    }

    fun findAllRetrogradePeriods(
        planet: Planet,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<RetrogradeCombustionCalculator.RetrogradePeriod> {
        val periods = mutableListOf<RetrogradeCombustionCalculator.RetrogradePeriod>()
        var searchFrom = fromDate

        while (searchFrom.isBefore(toDate)) {
            val period = findNextRetrogradePeriod(planet, searchFrom, ChronoUnit.DAYS.between(searchFrom, toDate).toInt())
            if (period != null && period.endDate.isBefore(toDate)) {
                periods.add(period)
                searchFrom = period.endDate.plusDays(1)
            } else {
                break
            }
        }

        return periods
    }

    fun findNextCombustionPeriod(
        planet: Planet,
        fromDate: LocalDate,
        maxSearchDays: Int = 450
    ): RetrogradeCombustionCalculator.CombustionPeriod? {
        if (planet in listOf(Planet.SUN, Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return null
        }

        val combustOrb = getCombustOrb(planet)
        val searchOrb = combustOrb * 1.5

        var currentDate = fromDate
        var enterDate: LocalDate? = null
        var enterSign: Int? = null
        var peakDate: LocalDate? = null
        var minDistance = Double.MAX_VALUE
        var wasRetro = false

        repeat(maxSearchDays) {
            val jd = toJulianDay(currentDate)
            val planetLong = getLongitude(planet, jd)
            val sunLong = getLongitude(Planet.SUN, jd)
            val distance = angularDistance(planetLong, sunLong)
            val isRetro = getSpeed(planet, jd) < 0

            if (enterDate == null && distance <= searchOrb) {
                enterDate = currentDate
                enterSign = getSign(planetLong)
            }

            if (enterDate != null) {
                wasRetro = wasRetro || isRetro
                if (distance < minDistance) {
                    minDistance = distance
                    peakDate = currentDate
                }
            }

            if (enterDate != null && distance > searchOrb && peakDate != null) {
                val exitSign = getSign(planetLong)

                val peakStatus = when {
                    minDistance <= 17.0 / 60.0 -> RetrogradeCombustionCalculator.CombustionStatus.CAZIMI
                    minDistance <= combustOrb / 2.0 -> RetrogradeCombustionCalculator.CombustionStatus.DEEP_COMBUST
                    minDistance <= combustOrb -> RetrogradeCombustionCalculator.CombustionStatus.COMBUST
                    else -> RetrogradeCombustionCalculator.CombustionStatus.APPROACHING
                }

                return RetrogradeCombustionCalculator.CombustionPeriod(
                    planet = planet,
                    enterDate = enterDate!!,
                    peakDate = peakDate!!,
                    exitDate = currentDate,
                    minimumSeparation = minDistance,
                    peakStatus = peakStatus,
                    durationDays = ChronoUnit.DAYS.between(enterDate, currentDate),
                    wasRetrogradeDuring = wasRetro,
                    entrySign = enterSign!!,
                    exitSign = exitSign
                )
            }

            currentDate = currentDate.plusDays(1)
        }

        return null
    }

    private fun refineStationDate(planet: Planet, start: LocalDate, end: LocalDate): LocalDate? {
        var bestDate = start
        var minSpeed = Double.MAX_VALUE

        var date = start
        while (!date.isAfter(end)) {
            val speed = abs(getSpeed(planet, toJulianDay(date)))
            if (speed < minSpeed) {
                minSpeed = speed
                bestDate = date
            }
            date = date.plusDays(1)
        }

        var hourOffset = -12.0
        var refinedJd = toJulianDay(bestDate)
        var refinedMinSpeed = minSpeed

        while (hourOffset <= 12.0) {
            val testJd = toJulianDay(bestDate) + hourOffset / 24.0
            val speed = abs(getSpeed(planet, testJd))
            if (speed < refinedMinSpeed) {
                refinedMinSpeed = speed
                refinedJd = testJd
            }
            hourOffset += 2.0
        }

        return bestDate
    }

    private fun getSpeed(planet: Planet, jd: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swissEph.swe_calc_ut(jd, planet.swissEphId, SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED, xx, serr)
        return xx[3]
    }

    private fun getLongitude(planet: Planet, jd: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swissEph.swe_calc_ut(jd, planet.swissEphId, SweConst.SEFLG_SIDEREAL, xx, serr)
        return ((xx[0] % 360.0) + 360.0) % 360.0
    }

    private fun getSign(longitude: Double): Int {
        return ((longitude / 30.0).toInt() % 12) + 1
    }

    private fun getCombustOrb(planet: Planet): Double {
        return when (planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> 10.0
            Planet.SATURN -> 15.0
            else -> 12.0
        }
    }

    private fun angularDistance(l1: Double, l2: Double): Double {
        val diff = abs(l1 - l2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    private fun toJulianDay(date: LocalDate): Double {
        return SweDate(date.year, date.monthValue, date.dayOfMonth, 12.0, SweDate.SE_GREG_CAL).julDay
    }

    override fun close() {
        swissEph.swe_close()
    }
}