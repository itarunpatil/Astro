package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.ManglikAnalysis
import com.astro.storm.data.model.ManglikDosha
import com.astro.storm.data.model.MatchmakingConstants
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign

/**
 * Manglik Dosha Calculator
 *
 * Comprehensive analysis based on multiple classical texts:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Phaladeepika
 * - Saravali
 * - Jataka Parijata
 * - Muhurta Chintamani
 *
 * Key features:
 * 1. Analysis from three Lagnas (Ascendant, Moon, Venus) - South Indian tradition
 * 2. Degree-based intensity calculation (border degrees reduce severity)
 * 3. Mars retrograde consideration
 * 4. Comprehensive classical cancellation rules
 * 5. Mars-Saturn mutual aspect analysis
 * 6. House-wise severity weighting
 */
object ManglikDoshaCalculator {

    /**
     * Calculate Manglik Dosha for a chart
     *
     * @param chart The VedicChart to analyze
     * @param person Name/identifier ("Bride" or "Groom")
     * @return Comprehensive ManglikAnalysis with all factors
     */
    fun calculate(chart: VedicChart, person: String): ManglikAnalysis {
        val mars = chart.planetPositions.find { it.planet == Planet.MARS }
            ?: return createNoManglikResult(person)

        val moon = chart.planetPositions.find { it.planet == Planet.MOON }
        val venus = chart.planetPositions.find { it.planet == Planet.VENUS }
        val jupiter = chart.planetPositions.find { it.planet == Planet.JUPITER }
        val saturn = chart.planetPositions.find { it.planet == Planet.SATURN }
        val rahu = chart.planetPositions.find { it.planet == Planet.RAHU }
        val ketu = chart.planetPositions.find { it.planet == Planet.KETU }
        val sun = chart.planetPositions.find { it.planet == Planet.SUN }

        val marsHouseFromLagna = mars.house
        val marsDegreeInHouse = mars.longitude % 30.0
        val isRetrograde = mars.isRetrograde

        // Calculate Mars house from Moon (Chandra Lagna) - important in South Indian tradition
        val marsHouseFromMoon = if (moon != null) {
            calculateHouseFromPlanet(mars.sign, moon.sign)
        } else 0

        // Calculate Mars house from Venus (Shukra Lagna) - significant for marriage
        val marsHouseFromVenus = if (venus != null) {
            calculateHouseFromPlanet(mars.sign, venus.sign)
        } else 0

        val factors = mutableListOf<String>()
        val cancellations = mutableListOf<String>()

        // Check Manglik from all three lagnas
        val isManglikFromLagna = marsHouseFromLagna in MatchmakingConstants.MANGLIK_HOUSES
        val isManglikFromMoon = marsHouseFromMoon in MatchmakingConstants.MANGLIK_HOUSES
        val isManglikFromVenus = marsHouseFromVenus in MatchmakingConstants.MANGLIK_HOUSES

        // Person is Manglik if Mars is in Manglik houses from Lagna OR Moon
        val isManglik = isManglikFromLagna || isManglikFromMoon

        if (!isManglik) {
            return ManglikAnalysis(
                person = person,
                dosha = ManglikDosha.NONE,
                marsHouse = marsHouseFromLagna,
                marsHouseFromMoon = marsHouseFromMoon,
                marsHouseFromVenus = marsHouseFromVenus,
                marsDegreeInHouse = marsDegreeInHouse,
                isRetrograde = isRetrograde,
                factors = factors,
                cancellations = cancellations,
                effectiveDosha = ManglikDosha.NONE,
                intensity = 0,
                fromLagna = false,
                fromMoon = false,
                fromVenus = false
            )
        }

        // Add factors based on positions
        if (isManglikFromLagna) {
            factors.add("Mars in ${getHouseOrdinal(marsHouseFromLagna)} house from Ascendant (Lagna)")
        }
        if (isManglikFromMoon) {
            factors.add("Mars in ${getHouseOrdinal(marsHouseFromMoon)} house from Moon (Chandra Lagna)")
        }
        if (isManglikFromVenus) {
            factors.add("Mars in ${getHouseOrdinal(marsHouseFromVenus)} house from Venus (Shukra Lagna)")
        }

        // Determine base dosha level based on house severity
        val doshaLevel = calculateBaseDoshaLevel(marsHouseFromLagna, marsHouseFromMoon)

        // Calculate intensity based on Mars degree within the house
        val degreeBasedIntensity = calculateDegreeBasedIntensity(marsDegreeInHouse)

        // Check for Mars retrograde
        if (isRetrograde) {
            factors.add("Mars is retrograde - reduces intensity")
        }

        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

        // Apply cancellation rules
        applyCancellationRules(
            mars, moon, venus, jupiter, saturn, rahu, ketu, sun,
            marsHouseFromLagna, ascendantSign, chart, cancellations
        )

        // Check for double Manglik conditions
        val hasDoubleManglik = checkDoubleManglikConditions(
            mars, saturn, rahu, ketu, marsHouseFromLagna, marsHouseFromMoon, chart, factors
        )

        // Calculate effective dosha
        val effectiveDosha = calculateEffectiveDosha(doshaLevel, cancellations, hasDoubleManglik)

        // Calculate final intensity
        val finalIntensity = calculateFinalIntensity(
            effectiveDosha, degreeBasedIntensity, isRetrograde, cancellations.size
        )

        return ManglikAnalysis(
            person = person,
            dosha = doshaLevel,
            marsHouse = marsHouseFromLagna,
            marsHouseFromMoon = marsHouseFromMoon,
            marsHouseFromVenus = marsHouseFromVenus,
            marsDegreeInHouse = marsDegreeInHouse,
            isRetrograde = isRetrograde,
            factors = factors,
            cancellations = cancellations,
            effectiveDosha = effectiveDosha,
            intensity = finalIntensity,
            fromLagna = isManglikFromLagna,
            fromMoon = isManglikFromMoon,
            fromVenus = isManglikFromVenus
        )
    }

    /**
     * Assess Manglik compatibility between two charts
     */
    fun assessCompatibility(bride: ManglikAnalysis, groom: ManglikAnalysis, language: Language): String {
        val brideLevel = bride.effectiveDosha.severity
        val groomLevel = groom.effectiveDosha.severity

        return when {
            brideLevel == 0 && groomLevel == 0 ->
                StringResources.get(StringKey.MANGLIK_BOTH_NON, language)
            brideLevel > 0 && groomLevel > 0 ->
                StringResources.get(StringKey.MANGLIK_BOTH_MATCH, language)
            kotlin.math.abs(brideLevel - groomLevel) == 1 ->
                StringResources.get(StringKey.MANGLIK_MINOR_IMBALANCE, language)
            brideLevel > 0 && groomLevel == 0 ->
                "${StringResources.get(StringKey.MANGLIK_BRIDE_ONLY, language)} (${bride.effectiveDosha.getLocalizedName(language)})"
            groomLevel > 0 && brideLevel == 0 ->
                "${StringResources.get(StringKey.MANGLIK_GROOM_ONLY, language)} (${groom.effectiveDosha.getLocalizedName(language)})"
            else ->
                StringResources.get(StringKey.MANGLIK_SIGNIFICANT_IMBALANCE, language)
        }
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private fun createNoManglikResult(person: String): ManglikAnalysis {
        return ManglikAnalysis(
            person = person,
            dosha = ManglikDosha.NONE,
            marsHouse = 0,
            factors = emptyList(),
            cancellations = emptyList(),
            effectiveDosha = ManglikDosha.NONE
        )
    }

    private fun calculateHouseFromPlanet(targetSign: ZodiacSign, referenceSign: ZodiacSign): Int {
        val diff = targetSign.number - referenceSign.number
        return if (diff >= 0) diff + 1 else diff + 13
    }

    private fun getHouseOrdinal(house: Int): String = when (house) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${house}th"
    }

    /**
     * Calculate base dosha level based on house positions.
     * Houses 7 and 8 are most severe (affect marriage partner directly).
     * House 2 is least severe (affects family, but indirect).
     */
    private fun calculateBaseDoshaLevel(marsHouseFromLagna: Int, marsHouseFromMoon: Int): ManglikDosha {
        val lagnaLevel = when (marsHouseFromLagna) {
            in MatchmakingConstants.SEVERE_MANGLIK_HOUSES -> ManglikDosha.FULL
            in MatchmakingConstants.MODERATE_MANGLIK_HOUSES -> ManglikDosha.FULL
            in MatchmakingConstants.MILD_MANGLIK_HOUSES -> ManglikDosha.PARTIAL
            else -> ManglikDosha.NONE
        }

        val moonLevel = when (marsHouseFromMoon) {
            in MatchmakingConstants.SEVERE_MANGLIK_HOUSES -> ManglikDosha.FULL
            in MatchmakingConstants.MODERATE_MANGLIK_HOUSES -> ManglikDosha.PARTIAL
            in MatchmakingConstants.MILD_MANGLIK_HOUSES -> ManglikDosha.PARTIAL
            else -> ManglikDosha.NONE
        }

        return if (lagnaLevel.severity >= moonLevel.severity) lagnaLevel else moonLevel
    }

    /**
     * Calculate intensity reduction based on Mars degree within the house.
     * Mars at 0-5° or 25-30° has reduced intensity (border effect).
     */
    private fun calculateDegreeBasedIntensity(degreeInHouse: Double): Int = when {
        degreeInHouse < 3.0 -> 60
        degreeInHouse < 5.0 -> 75
        degreeInHouse < 10.0 -> 90
        degreeInHouse < 20.0 -> 100
        degreeInHouse < 25.0 -> 90
        degreeInHouse < 27.0 -> 75
        else -> 60
    }

    /**
     * Apply all cancellation rules from classical texts
     */
    private fun applyCancellationRules(
        mars: com.astro.storm.data.model.PlanetPosition,
        moon: com.astro.storm.data.model.PlanetPosition?,
        venus: com.astro.storm.data.model.PlanetPosition?,
        jupiter: com.astro.storm.data.model.PlanetPosition?,
        saturn: com.astro.storm.data.model.PlanetPosition?,
        rahu: com.astro.storm.data.model.PlanetPosition?,
        ketu: com.astro.storm.data.model.PlanetPosition?,
        sun: com.astro.storm.data.model.PlanetPosition?,
        marsHouseFromLagna: Int,
        ascendantSign: ZodiacSign,
        chart: VedicChart,
        cancellations: MutableList<String>
    ) {
        // Rule 1: Mars in own sign (Aries, Scorpio) - Full cancellation
        if (mars.sign == ZodiacSign.ARIES || mars.sign == ZodiacSign.SCORPIO) {
            cancellations.add("Mars in own sign (${mars.sign.displayName}) - Strong cancellation per BPHS")
        }

        // Rule 2: Mars exalted (Capricorn) - Full cancellation
        if (mars.sign == ZodiacSign.CAPRICORN) {
            cancellations.add("Mars exalted in Capricorn - Full cancellation")
        }

        // Rule 3: Mars debilitated (Cancer) - Dosha effect reduced
        if (mars.sign == ZodiacSign.CANCER) {
            cancellations.add("Mars debilitated in Cancer - Dosha effect weakened")
        }

        // Rule 4: Jupiter aspects Mars
        if (jupiter != null) {
            val jupiterAspects = getJupiterAspectedHouses(jupiter.house)
            if (marsHouseFromLagna in jupiterAspects) {
                cancellations.add("Jupiter aspects Mars from ${getHouseOrdinal(jupiter.house)} house - Strong benefic influence")
            }
        }

        // Rule 5: Jupiter in Kendra
        if (jupiter != null && jupiter.house in listOf(1, 4, 7, 10)) {
            cancellations.add("Jupiter in Kendra (${getHouseOrdinal(jupiter.house)} house) - Overall protection")
        }

        // Rule 6: Moon conjunct Mars (Chandra-Mangal Yoga)
        if (moon != null && moon.house == marsHouseFromLagna) {
            cancellations.add("Moon conjunct Mars (Chandra-Mangal Yoga) - Wealth yoga mitigates dosha")
        }

        // Rule 7: Venus in 1st or 7th house
        if (venus != null && venus.house in listOf(1, 7)) {
            cancellations.add("Venus in ${getHouseOrdinal(venus.house)} house - Marriage karaka strong")
        }

        // Rule 8: Mars in Leo or Aquarius in Kendra
        if (mars.sign in listOf(ZodiacSign.LEO, ZodiacSign.AQUARIUS) &&
            marsHouseFromLagna in listOf(1, 4, 7, 10)) {
            cancellations.add("Mars in ${mars.sign.displayName} in Kendra - Per Phaladeepika")
        }

        // Rule 9: Mars in Sagittarius or Pisces (Jupiter's signs)
        if (mars.sign in listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)) {
            cancellations.add("Mars in Jupiter's sign (${mars.sign.displayName}) - Guru's influence reduces harm")
        }

        // Rule 10: Sign-house specific cancellations
        applySignHouseCancellations(marsHouseFromLagna, mars.sign, ascendantSign, cancellations)

        // Rule 11: Mars in Navamsa of benefic
        val marsNavamsaSign = getNavamsaSignFromLongitude(mars.longitude)
        if (marsNavamsaSign.ruler in listOf(Planet.JUPITER, Planet.VENUS)) {
            cancellations.add("Mars in Navamsa of benefic (${marsNavamsaSign.ruler.displayName})")
        }

        // Rule 13: Venus aspects Mars
        if (venus != null) {
            val venusAspects = listOf(
                venus.house,
                ((venus.house + 6) % 12).let { if (it == 0) 12 else it }
            )
            if (marsHouseFromLagna in venusAspects) {
                cancellations.add("Venus aspects Mars - Benefic calming influence")
            }
        }

        // Rule 14: Mars in 2nd house with Gemini or Virgo
        if (marsHouseFromLagna == 2 && mars.sign in listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO)) {
            cancellations.add("Mars in Mercury's sign in 2nd house - Communicative resolution")
        }

        // Rule 15: Ascendant in fiery sign with Mars in 1st house
        if (marsHouseFromLagna == 1 &&
            ascendantSign in listOf(ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS)) {
            cancellations.add("Mars in 1st house with fiery Ascendant - Natural placement")
        }

        // Rule 16: Mars hemmed between benefics (Shubhakartari)
        val prevHouse = if (marsHouseFromLagna == 1) 12 else marsHouseFromLagna - 1
        val nextHouse = if (marsHouseFromLagna == 12) 1 else marsHouseFromLagna + 1
        val beneficPlanets = listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY)
        val beneficsInPrev = chart.planetPositions.filter { it.house == prevHouse && it.planet in beneficPlanets }
        val beneficsInNext = chart.planetPositions.filter { it.house == nextHouse && it.planet in beneficPlanets }
        if (beneficsInPrev.isNotEmpty() && beneficsInNext.isNotEmpty()) {
            cancellations.add("Mars hemmed between benefics (Shubhakartari Yoga)")
        }

        // Rule 17: Sun conjunct Mars in 1st or 7th house
        if (sun != null && sun.house == marsHouseFromLagna && marsHouseFromLagna in listOf(1, 7)) {
            cancellations.add("Sun-Mars conjunction in ${getHouseOrdinal(marsHouseFromLagna)} house - Strength yoga")
        }
    }

    private fun applySignHouseCancellations(
        marsHouse: Int,
        marsSign: ZodiacSign,
        ascendantSign: ZodiacSign,
        cancellations: MutableList<String>
    ) {
        // House 2: Mercury's signs reduce speech/family issues
        if (marsHouse == 2 && marsSign in listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO)) {
            cancellations.add("Mars in Mercury's sign in 2nd - Communication helps resolve issues")
        }

        // House 4: Own sign Aries
        if (marsHouse == 4 && marsSign == ZodiacSign.ARIES) {
            cancellations.add("Mars in own sign Aries in 4th - Domestic harmony through strength")
        }

        // House 7: Debilitated or exalted Mars
        if (marsHouse == 7) {
            if (marsSign == ZodiacSign.CANCER) {
                cancellations.add("Mars debilitated in 7th - Aggression towards partner is weakened")
            }
            if (marsSign == ZodiacSign.CAPRICORN) {
                cancellations.add("Mars exalted in 7th - Controlled assertiveness in partnership")
            }
        }

        // House 8: Jupiter's signs - Spiritual protection
        if (marsHouse == 8 && marsSign in listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)) {
            cancellations.add("Mars in Jupiter's sign in 8th - Spiritual protection from harm")
        }

        // House 12: Venus's signs
        if (marsHouse == 12 && marsSign in listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA)) {
            cancellations.add("Mars in Venus's sign in 12th - Losses transformed to spiritual gains")
        }

        // House 1: Fiery ascendants
        if (marsHouse == 1 && ascendantSign in listOf(ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS)) {
            cancellations.add("Mars in 1st with fiery Lagna - Natural warrior placement")
        }

        // House 1: Scorpio ascendant
        if (marsHouse == 1 && ascendantSign == ZodiacSign.SCORPIO) {
            cancellations.add("Mars in 1st for Scorpio Lagna - Lagna lord in Lagna is auspicious")
        }

        // House 7: Aries or Scorpio ascendant
        if (marsHouse == 7 && ascendantSign in listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO)) {
            cancellations.add("Mars (7th lord) in 7th - Lord in own house is strong")
        }
    }

    /**
     * Check for double Manglik conditions
     */
    private fun checkDoubleManglikConditions(
        mars: com.astro.storm.data.model.PlanetPosition,
        saturn: com.astro.storm.data.model.PlanetPosition?,
        rahu: com.astro.storm.data.model.PlanetPosition?,
        ketu: com.astro.storm.data.model.PlanetPosition?,
        marsHouseFromLagna: Int,
        marsHouseFromMoon: Int,
        chart: VedicChart,
        factors: MutableList<String>
    ): Boolean {
        var hasDoubleManglik = false

        // Mars conjunct Saturn (most serious)
        if (saturn != null && saturn.house == marsHouseFromLagna) {
            factors.add("Mars conjunct Saturn in ${getHouseOrdinal(marsHouseFromLagna)} house - Severe combination")
            hasDoubleManglik = true
        }

        // Mars conjunct Rahu (Angarak Yoga)
        if (rahu != null && rahu.house == marsHouseFromLagna) {
            factors.add("Mars conjunct Rahu (Angarak Yoga) in ${getHouseOrdinal(marsHouseFromLagna)} house")
            hasDoubleManglik = true
        }

        // Mars conjunct Ketu
        if (ketu != null && ketu.house == marsHouseFromLagna) {
            factors.add("Mars conjunct Ketu in ${getHouseOrdinal(marsHouseFromLagna)} house")
            hasDoubleManglik = true
        }

        // Mars-Saturn mutual aspect (even without conjunction)
        if (saturn != null && !hasDoubleManglik) {
            val saturnAspects = getSaturnAspectedHouses(saturn.house)
            val marsAspects = getMarsAspectedHouses(marsHouseFromLagna)
            if (saturn.house in marsAspects && marsHouseFromLagna in saturnAspects) {
                factors.add("Mars-Saturn mutual aspect - Adds severity")
            }
        }

        // Mars in 8th house from Moon (very problematic for longevity)
        if (marsHouseFromMoon == 8) {
            factors.add("Mars in 8th from Moon - Concerns for partner's health")
        }

        return hasDoubleManglik
    }

    private fun calculateEffectiveDosha(
        doshaLevel: ManglikDosha,
        cancellations: List<String>,
        hasDoubleManglik: Boolean
    ): ManglikDosha {
        val strongCancellations = cancellations.count {
            it.contains("Full") || it.contains("Strong") ||
            it.contains("own sign") || it.contains("exalted")
        }
        val partialCancellations = cancellations.size - strongCancellations

        val effectiveDosha = when {
            strongCancellations >= 2 -> ManglikDosha.NONE
            strongCancellations >= 1 && partialCancellations >= 2 -> ManglikDosha.NONE
            strongCancellations >= 1 -> if (doshaLevel == ManglikDosha.FULL) ManglikDosha.PARTIAL else ManglikDosha.NONE
            cancellations.size >= 4 -> ManglikDosha.NONE
            cancellations.size >= 3 -> if (doshaLevel == ManglikDosha.FULL) ManglikDosha.PARTIAL else ManglikDosha.NONE
            cancellations.size >= 2 -> if (doshaLevel == ManglikDosha.FULL) ManglikDosha.PARTIAL else doshaLevel
            cancellations.size >= 1 -> if (doshaLevel == ManglikDosha.FULL) ManglikDosha.PARTIAL else doshaLevel
            else -> doshaLevel
        }

        // Apply double Manglik upgrade if applicable
        return if (hasDoubleManglik && effectiveDosha != ManglikDosha.NONE) {
            ManglikDosha.DOUBLE
        } else {
            effectiveDosha
        }
    }

    private fun calculateFinalIntensity(
        effectiveDosha: ManglikDosha,
        degreeBasedIntensity: Int,
        isRetrograde: Boolean,
        cancellationCount: Int
    ): Int {
        if (effectiveDosha == ManglikDosha.NONE) return 0

        val baseIntensity = when (effectiveDosha) {
            ManglikDosha.NONE -> 0
            ManglikDosha.PARTIAL -> 50
            ManglikDosha.FULL -> 100
            ManglikDosha.DOUBLE -> 150
        }

        var intensity = baseIntensity
        intensity = (intensity * degreeBasedIntensity / 100)
        if (isRetrograde) intensity = (intensity * 0.85).toInt()
        intensity = (intensity * (1.0 - cancellationCount * 0.08)).toInt()

        return intensity.coerceIn(10, 150)
    }

    private fun getJupiterAspectedHouses(jupiterHouse: Int): List<Int> = listOf(
        jupiterHouse,
        ((jupiterHouse + 4 - 1) % 12) + 1,
        ((jupiterHouse + 6 - 1) % 12) + 1,
        ((jupiterHouse + 8 - 1) % 12) + 1
    )

    private fun getSaturnAspectedHouses(saturnHouse: Int): List<Int> = listOf(
        saturnHouse,
        ((saturnHouse + 2) % 12).let { if (it == 0) 12 else it },
        ((saturnHouse + 6) % 12).let { if (it == 0) 12 else it },
        ((saturnHouse + 9) % 12).let { if (it == 0) 12 else it }
    )

    private fun getMarsAspectedHouses(marsHouse: Int): List<Int> = listOf(
        marsHouse,
        ((marsHouse + 3) % 12).let { if (it == 0) 12 else it },
        ((marsHouse + 6) % 12).let { if (it == 0) 12 else it },
        ((marsHouse + 7) % 12).let { if (it == 0) 12 else it }
    )

    private fun getNavamsaSignFromLongitude(longitude: Double): ZodiacSign {
        val normalizedLongitude = ((longitude % 360.0) + 360.0) % 360.0
        val signIndex = (normalizedLongitude / 30.0).toInt()
        val degreeInSign = normalizedLongitude % 30.0
        val navamsaIndex = (degreeInSign / 3.333333).toInt()

        val startingNavamsa = when (signIndex % 4) {
            0 -> 0   // Fire signs start from Aries
            1 -> 9   // Earth signs start from Capricorn
            2 -> 4   // Air signs start from Libra
            3 -> 3   // Water signs start from Cancer
            else -> 0
        }

        val navamsaSignIndex = (startingNavamsa + navamsaIndex) % 12
        return ZodiacSign.entries[navamsaSignIndex]
    }
}
