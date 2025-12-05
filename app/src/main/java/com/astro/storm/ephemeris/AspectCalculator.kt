package com.astro.storm.ephemeris

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import kotlin.math.abs
import kotlin.math.floor

object AspectCalculator {

    private const val DEGREES_PER_SIGN = 30.0
    private const val TOTAL_DEGREES = 360.0
    private const val TOTAL_SIGNS = 12

    enum class AspectType(
        val displayName: String,
        val houseDistance: Int,
        val degreeAngle: Double,
        val nature: AspectNature,
        val symbol: String,
        val classicalStrength: Double
    ) {
        CONJUNCTION("Conjunction", 1, 0.0, AspectNature.VARIABLE, "☌", 1.0),

        SEVENTH_HOUSE("7th Aspect", 7, 180.0, AspectNature.SIGNIFICANT, "☍", 1.0),

        MARS_4TH("Mars 4th Aspect", 4, 90.0, AspectNature.CHALLENGING, "♂₄", 0.75),
        MARS_8TH("Mars 8th Aspect", 8, 210.0, AspectNature.CHALLENGING, "♂₈", 1.0),

        JUPITER_5TH("Jupiter 5th Aspect", 5, 120.0, AspectNature.HARMONIOUS, "♃₅", 0.5),
        JUPITER_9TH("Jupiter 9th Aspect", 9, 240.0, AspectNature.HARMONIOUS, "♃₉", 1.0),

        SATURN_3RD("Saturn 3rd Aspect", 3, 60.0, AspectNature.CHALLENGING, "♄₃", 0.75),
        SATURN_10TH("Saturn 10th Aspect", 10, 270.0, AspectNature.CHALLENGING, "♄₁₀", 1.0);

        companion object {
            fun getSpecialAspects(planet: Planet): List<AspectType> {
                return when (planet) {
                    Planet.MARS -> listOf(MARS_4TH, MARS_8TH)
                    Planet.JUPITER -> listOf(JUPITER_5TH, JUPITER_9TH)
                    Planet.SATURN -> listOf(SATURN_3RD, SATURN_10TH)
                    else -> emptyList()
                }
            }
        }
    }

    enum class AspectNature(val displayName: String, val isPositive: Boolean?) {
        HARMONIOUS("Harmonious", true),
        CHALLENGING("Challenging", false),
        VARIABLE("Variable", null),
        SIGNIFICANT("Significant", null)
    }

    enum class AspectMode {
        SIGN_BASED,
        DEGREE_BASED,
        HYBRID
    }

    data class AspectConfiguration(
        val mode: AspectMode = AspectMode.HYBRID,
        val degreeOrb: Double = 12.0,
        val conjunctionOrb: Double = 10.0,
        val includeOuterPlanets: Boolean = false,
        val includeRahuKetuAspects: Boolean = false
    )

    data class AspectData(
        val aspectingPlanet: Planet,
        val aspectedPlanet: Planet,
        val aspectType: AspectType,
        val forwardAngle: Double,
        val exactOrb: Double,
        val isApplying: Boolean,
        val drishtiBala: Double,
        val signBasedAspect: Boolean,
        val aspectingSign: Int,
        val aspectedSign: Int
    ) {
        val strengthDescription: String
            get() = when {
                drishtiBala >= 0.95 -> "Exact (Purna)"
                drishtiBala >= 0.75 -> "Strong (Adhika)"
                drishtiBala >= 0.50 -> "Medium (Madhya)"
                drishtiBala >= 0.25 -> "Weak (Alpa)"
                else -> "Negligible (Sunya)"
            }

        val isFullAspect: Boolean
            get() = aspectType.classicalStrength >= 1.0

        val isSpecialAspect: Boolean
            get() = aspectType != AspectType.SEVENTH_HOUSE && aspectType != AspectType.CONJUNCTION
    }

    data class AspectMatrix(
        val aspects: List<AspectData>,
        val aspectsByAspectingPlanet: Map<Planet, List<AspectData>>,
        val aspectsByAspectedPlanet: Map<Planet, List<AspectData>>,
        val conjunctions: List<AspectData>,
        val seventhHouseAspects: List<AspectData>,
        val specialAspects: List<AspectData>,
        val mutualAspects: List<Pair<AspectData, AspectData>>
    ) {
        fun getAspectsCastBy(planet: Planet): List<AspectData> =
            aspectsByAspectingPlanet[planet] ?: emptyList()

        fun getAspectsReceivedBy(planet: Planet): List<AspectData> =
            aspectsByAspectedPlanet[planet] ?: emptyList()

        fun getAspectBetween(planet1: Planet, planet2: Planet): AspectData? =
            aspects.find { it.aspectingPlanet == planet1 && it.aspectedPlanet == planet2 }

        fun hasMutualAspect(planet1: Planet, planet2: Planet): Boolean =
            mutualAspects.any {
                (it.first.aspectingPlanet == planet1 && it.first.aspectedPlanet == planet2) ||
                        (it.first.aspectingPlanet == planet2 && it.first.aspectedPlanet == planet1)
            }

        fun getTotalDrishtiBalaOn(planet: Planet): Double =
            getAspectsReceivedBy(planet).sumOf { it.drishtiBala }
    }

    fun calculateAspectMatrix(
        chart: VedicChart,
        config: AspectConfiguration = AspectConfiguration()
    ): AspectMatrix {
        val allAspects = mutableListOf<AspectData>()
        val positions = chart.planetPositions.filter { position ->
            if (!config.includeOuterPlanets) {
                position.planet !in listOf(Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)
            } else true
        }

        for (aspectingPosition in positions) {
            if (!canCastAspect(aspectingPosition.planet)) continue

            for (aspectedPosition in positions) {
                if (aspectingPosition.planet == aspectedPosition.planet) continue

                val aspectsToCheck = getAllApplicableAspects(aspectingPosition.planet, config)

                for (aspectType in aspectsToCheck) {
                    val aspectData = calculateSingleAspect(
                        aspectingPosition,
                        aspectedPosition,
                        aspectType,
                        config
                    )
                    if (aspectData != null) {
                        allAspects.add(aspectData)
                    }
                }
            }
        }

        val sortedAspects = allAspects.sortedByDescending { it.drishtiBala }
        val mutualAspects = findMutualAspects(sortedAspects)

        return AspectMatrix(
            aspects = sortedAspects,
            aspectsByAspectingPlanet = sortedAspects.groupBy { it.aspectingPlanet },
            aspectsByAspectedPlanet = sortedAspects.groupBy { it.aspectedPlanet },
            conjunctions = sortedAspects.filter { it.aspectType == AspectType.CONJUNCTION },
            seventhHouseAspects = sortedAspects.filter { it.aspectType == AspectType.SEVENTH_HOUSE },
            specialAspects = sortedAspects.filter { it.isSpecialAspect },
            mutualAspects = mutualAspects
        )
    }

    private fun canCastAspect(planet: Planet): Boolean {
        return planet !in listOf(Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)
    }

    private fun getAllApplicableAspects(planet: Planet, config: AspectConfiguration): List<AspectType> {
        val aspects = mutableListOf<AspectType>()
        aspects.add(AspectType.CONJUNCTION)
        aspects.add(AspectType.SEVENTH_HOUSE)
        aspects.addAll(AspectType.getSpecialAspects(planet))

        if (config.includeRahuKetuAspects && planet in listOf(Planet.RAHU, Planet.KETU)) {
            aspects.add(AspectType.JUPITER_5TH)
            aspects.add(AspectType.JUPITER_9TH)
        }

        return aspects
    }

    private fun calculateSingleAspect(
        aspecting: PlanetPosition,
        aspected: PlanetPosition,
        aspectType: AspectType,
        config: AspectConfiguration
    ): AspectData? {
        val aspectingSign = getSignNumber(aspecting.longitude)
        val aspectedSign = getSignNumber(aspected.longitude)

        val signDistance = calculateSignDistance(aspectingSign, aspectedSign)
        val forwardAngle = calculateForwardAngle(aspecting.longitude, aspected.longitude)

        val isSignBasedMatch = signDistance == aspectType.houseDistance
        val orb = calculateOrb(forwardAngle, aspectType.degreeAngle)
        val effectiveOrb = if (aspectType == AspectType.CONJUNCTION) config.conjunctionOrb else config.degreeOrb
        val isDegreeBasedMatch = orb <= effectiveOrb

        val isValidAspect = when (config.mode) {
            AspectMode.SIGN_BASED -> isSignBasedMatch
            AspectMode.DEGREE_BASED -> isDegreeBasedMatch
            AspectMode.HYBRID -> isSignBasedMatch || (isDegreeBasedMatch && isWithinAdjacentSign(signDistance, aspectType.houseDistance))
        }

        if (!isValidAspect) return null

        val drishtiBala = calculateDrishtiBala(
            aspectType = aspectType,
            orb = orb,
            effectiveOrb = effectiveOrb,
            isSignBased = isSignBasedMatch,
            config = config
        )

        val isApplying = calculateIsApplying(aspecting, aspected, aspectType.degreeAngle)

        return AspectData(
            aspectingPlanet = aspecting.planet,
            aspectedPlanet = aspected.planet,
            aspectType = aspectType,
            forwardAngle = forwardAngle,
            exactOrb = orb,
            isApplying = isApplying,
            drishtiBala = drishtiBala,
            signBasedAspect = isSignBasedMatch,
            aspectingSign = aspectingSign,
            aspectedSign = aspectedSign
        )
    }

    private fun getSignNumber(longitude: Double): Int {
        return (floor(normalizeAngle(longitude) / DEGREES_PER_SIGN).toInt() % TOTAL_SIGNS) + 1
    }

    private fun calculateSignDistance(fromSign: Int, toSign: Int): Int {
        val distance = ((toSign - fromSign + TOTAL_SIGNS) % TOTAL_SIGNS)
        return if (distance == 0) 1 else distance + 1
    }

    private fun calculateForwardAngle(fromLongitude: Double, toLongitude: Double): Double {
        val from = normalizeAngle(fromLongitude)
        val to = normalizeAngle(toLongitude)
        return normalizeAngle(to - from)
    }

    private fun calculateOrb(actualAngle: Double, targetAngle: Double): Double {
        val diff = abs(actualAngle - targetAngle)
        return minOf(diff, TOTAL_DEGREES - diff)
    }

    private fun isWithinAdjacentSign(actualSignDistance: Int, targetHouseDistance: Int): Boolean {
        return abs(actualSignDistance - targetHouseDistance) <= 1 ||
                abs(actualSignDistance - targetHouseDistance) == 11
    }

    private fun calculateDrishtiBala(
        aspectType: AspectType,
        orb: Double,
        effectiveOrb: Double,
        isSignBased: Boolean,
        config: AspectConfiguration
    ): Double {
        val classicalStrength = aspectType.classicalStrength

        val orbFactor = when (config.mode) {
            AspectMode.SIGN_BASED -> 1.0
            AspectMode.DEGREE_BASED -> if (orb <= effectiveOrb) 1.0 - (orb / effectiveOrb) * 0.5 else 0.0
            AspectMode.HYBRID -> {
                if (isSignBased) {
                    if (orb <= effectiveOrb) 1.0 else 0.9
                } else {
                    if (orb <= effectiveOrb) 0.8 - (orb / effectiveOrb) * 0.3 else 0.0
                }
            }
        }

        return classicalStrength * orbFactor
    }

    private fun calculateIsApplying(
        aspecting: PlanetPosition,
        aspected: PlanetPosition,
        targetAngle: Double
    ): Boolean {
        val currentForwardAngle = calculateForwardAngle(aspecting.longitude, aspected.longitude)
        val currentOrb = calculateOrb(currentForwardAngle, targetAngle)

        val futureAspecting = normalizeAngle(aspecting.longitude + aspecting.speed)
        val futureAspected = normalizeAngle(aspected.longitude + aspected.speed)
        val futureForwardAngle = calculateForwardAngle(futureAspecting, futureAspected)
        val futureOrb = calculateOrb(futureForwardAngle, targetAngle)

        return futureOrb < currentOrb
    }

    private fun findMutualAspects(aspects: List<AspectData>): List<Pair<AspectData, AspectData>> {
        val mutualAspects = mutableListOf<Pair<AspectData, AspectData>>()
        val processed = mutableSetOf<Pair<Planet, Planet>>()

        for (aspect1 in aspects) {
            val key = if (aspect1.aspectingPlanet.ordinal < aspect1.aspectedPlanet.ordinal) {
                aspect1.aspectingPlanet to aspect1.aspectedPlanet
            } else {
                aspect1.aspectedPlanet to aspect1.aspectingPlanet
            }

            if (key in processed) continue

            val reverseAspect = aspects.find {
                it.aspectingPlanet == aspect1.aspectedPlanet &&
                        it.aspectedPlanet == aspect1.aspectingPlanet
            }

            if (reverseAspect != null) {
                mutualAspects.add(aspect1 to reverseAspect)
                processed.add(key)
            }
        }

        return mutualAspects
    }

    private fun normalizeAngle(angle: Double): Double {
        return ((angle % TOTAL_DEGREES) + TOTAL_DEGREES) % TOTAL_DEGREES
    }

    fun getAspectDescription(aspectData: AspectData): String {
        val aspecting = aspectData.aspectingPlanet.displayName
        val aspected = aspectData.aspectedPlanet.displayName
        val aspectName = aspectData.aspectType.displayName
        val strength = aspectData.strengthDescription

        return buildString {
            append("$aspecting casts $aspectName on $aspected")
            if (aspectData.isApplying) append(" (Applying)")
            else append(" (Separating)")
            append(" - $strength")
            append(" [${String.format("%.2f", aspectData.drishtiBala * 100)}% Drishti Bala]")
        }
    }

    fun calculatePlanetaryStrength(
        planet: Planet,
        chart: VedicChart,
        config: AspectConfiguration = AspectConfiguration()
    ): PlanetaryAspectStrength {
        val matrix = calculateAspectMatrix(chart, config)

        val aspectsCast = matrix.getAspectsCastBy(planet)
        val aspectsReceived = matrix.getAspectsReceivedBy(planet)

        val beneficAspects = aspectsReceived.filter { isBeneficAspect(it) }
        val maleficAspects = aspectsReceived.filter { isMaleficAspect(it) }

        val totalBeneficInfluence = beneficAspects.sumOf { it.drishtiBala }
        val totalMaleficInfluence = maleficAspects.sumOf { it.drishtiBala }

        val netInfluence = totalBeneficInfluence - totalMaleficInfluence

        return PlanetaryAspectStrength(
            planet = planet,
            aspectsCast = aspectsCast,
            aspectsReceived = aspectsReceived,
            beneficAspects = beneficAspects,
            maleficAspects = maleficAspects,
            totalDrishtiBalaReceived = matrix.getTotalDrishtiBalaOn(planet),
            netBeneficMaleficInfluence = netInfluence,
            isUnderBeneficInfluence = netInfluence > 0,
            strongestAspectReceived = aspectsReceived.maxByOrNull { it.drishtiBala }
        )
    }

    private fun isBeneficAspect(aspectData: AspectData): Boolean {
        val benefics = listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
        return aspectData.aspectingPlanet in benefics &&
                aspectData.aspectType.nature == AspectNature.HARMONIOUS
    }

    private fun isMaleficAspect(aspectData: AspectData): Boolean {
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU, Planet.SUN)
        return aspectData.aspectingPlanet in malefics &&
                aspectData.aspectType.nature == AspectNature.CHALLENGING
    }

    data class PlanetaryAspectStrength(
        val planet: Planet,
        val aspectsCast: List<AspectData>,
        val aspectsReceived: List<AspectData>,
        val beneficAspects: List<AspectData>,
        val maleficAspects: List<AspectData>,
        val totalDrishtiBalaReceived: Double,
        val netBeneficMaleficInfluence: Double,
        val isUnderBeneficInfluence: Boolean,
        val strongestAspectReceived: AspectData?
    )

    fun getHouseAspects(
        houseNumber: Int,
        houseCusp: Double,
        chart: VedicChart,
        config: AspectConfiguration = AspectConfiguration()
    ): List<AspectData> {
        val houseSign = getSignNumber(houseCusp)
        val aspects = mutableListOf<AspectData>()

        for (position in chart.planetPositions) {
            if (!canCastAspect(position.planet)) continue

            val planetSign = getSignNumber(position.longitude)
            val signDistance = calculateSignDistance(planetSign, houseSign)

            val applicableAspects = getAllApplicableAspects(position.planet, config)

            for (aspectType in applicableAspects) {
                if (signDistance == aspectType.houseDistance) {
                    val forwardAngle = calculateForwardAngle(position.longitude, houseCusp)
                    val orb = calculateOrb(forwardAngle, aspectType.degreeAngle)

                    aspects.add(
                        AspectData(
                            aspectingPlanet = position.planet,
                            aspectedPlanet = position.planet,
                            aspectType = aspectType,
                            forwardAngle = forwardAngle,
                            exactOrb = orb,
                            isApplying = false,
                            drishtiBala = aspectType.classicalStrength,
                            signBasedAspect = true,
                            aspectingSign = planetSign,
                            aspectedSign = houseSign
                        )
                    )
                }
            }
        }

        return aspects.sortedByDescending { it.drishtiBala }
    }

    fun calculateArgala(
        planet: Planet,
        chart: VedicChart
    ): ArgalaAnalysis {
        val position = chart.planetPositions.find { it.planet == planet }
            ?: return ArgalaAnalysis(planet, emptyList(), emptyList(), emptyList())

        val planetSign = getSignNumber(position.longitude)

        val primaryArgalaHouses = listOf(2, 4, 11)
        val secondaryArgalaHouses = listOf(5)
        val obstructionHouses = mapOf(2 to 12, 4 to 10, 11 to 3, 5 to 9)

        val primaryArgalas = mutableListOf<ArgalaData>()
        val secondaryArgalas = mutableListOf<ArgalaData>()
        val obstructions = mutableListOf<ArgalaData>()

        for (otherPosition in chart.planetPositions) {
            if (otherPosition.planet == planet) continue

            val otherSign = getSignNumber(otherPosition.longitude)
            val signDistance = calculateSignDistance(planetSign, otherSign)

            if (signDistance in primaryArgalaHouses) {
                primaryArgalas.add(
                    ArgalaData(
                        planet = otherPosition.planet,
                        houseFrom = signDistance,
                        isPrimary = true,
                        isObstructed = false
                    )
                )
            }

            if (signDistance in secondaryArgalas.map { it.houseFrom }) {
                secondaryArgalas.add(
                    ArgalaData(
                        planet = otherPosition.planet,
                        houseFrom = signDistance,
                        isPrimary = false,
                        isObstructed = false
                    )
                )
            }

            obstructionHouses.values.find { it == signDistance }?.let { obstHouse ->
                obstructions.add(
                    ArgalaData(
                        planet = otherPosition.planet,
                        houseFrom = signDistance,
                        isPrimary = false,
                        isObstructed = true
                    )
                )
            }
        }

        return ArgalaAnalysis(
            planet = planet,
            primaryArgalas = primaryArgalas,
            secondaryArgalas = secondaryArgalas,
            obstructions = obstructions
        )
    }

    data class ArgalaData(
        val planet: Planet,
        val houseFrom: Int,
        val isPrimary: Boolean,
        val isObstructed: Boolean
    )

    data class ArgalaAnalysis(
        val planet: Planet,
        val primaryArgalas: List<ArgalaData>,
        val secondaryArgalas: List<ArgalaData>,
        val obstructions: List<ArgalaData>
    ) {
        val netArgalaStrength: Int
            get() = primaryArgalas.size + secondaryArgalas.size - obstructions.size
    }
}