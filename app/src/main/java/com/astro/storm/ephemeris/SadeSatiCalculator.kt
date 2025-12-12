package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Sade Sati Calculator - Saturn's 7.5 Year Transit Over Moon
 *
 * Sade Sati (साढ़े साती) is one of the most significant transit phenomena in Vedic astrology.
 * It occurs when Saturn transits through:
 * - 12th house from Moon (Rising Phase) - ~2.5 years
 * - 1st house from Moon (Peak Phase) - ~2.5 years
 * - 2nd house from Moon (Setting Phase) - ~2.5 years
 *
 * Total duration: approximately 7.5 years (Sade = 7, Sati = half)
 *
 * Effects vary based on:
 * - Saturn's relationship with Moon sign lord
 * - Saturn's dignity in transit sign
 * - Natal Saturn's position and strength
 * - Ashtakavarga bindus in transit sign
 * - Current Dasha period
 *
 * This calculator provides:
 * - Detection of Sade Sati presence
 * - Current phase identification
 * - Severity assessment
 * - Approximate timeline calculation
 * - Personalized remedies
 *
 * References:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Phaladeepika
 * - Jataka Parijata
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
object SadeSatiCalculator {

    /**
     * Saturn's average transit duration through one sign (in days)
     * Saturn takes approximately 29.5 years to complete one zodiac cycle
     * = 29.5 * 365.25 / 12 ≈ 898 days per sign
     */
    private const val SATURN_TRANSIT_DAYS_PER_SIGN = 898.0

    /**
     * Sade Sati phases
     */
    enum class SadeSatiPhase(val phaseNumber: Int) {
        /** Rising phase - Saturn in 12th from Moon */
        RISING(1),
        /** Peak phase - Saturn conjunct Moon sign */
        PEAK(2),
        /** Setting phase - Saturn in 2nd from Moon */
        SETTING(3);

        fun getLocalizedName(language: Language): String = when (this) {
            RISING -> StringResources.get(StringKey.SADE_SATI_PHASE_RISING, language)
            PEAK -> StringResources.get(StringKey.SADE_SATI_PHASE_PEAK, language)
            SETTING -> StringResources.get(StringKey.SADE_SATI_PHASE_SETTING, language)
        }

        fun getDescription(language: Language): String = when (this) {
            RISING -> StringResources.get(StringKey.SADE_SATI_RISING_DESC, language)
            PEAK -> StringResources.get(StringKey.SADE_SATI_PEAK_DESC, language)
            SETTING -> StringResources.get(StringKey.SADE_SATI_SETTING_DESC, language)
        }
    }

    /**
     * Severity levels for Sade Sati effects
     */
    enum class Severity {
        /** Minimal effects - Saturn well placed, good Ashtakavarga */
        MILD,
        /** Moderate effects - neutral placement */
        MODERATE,
        /** Significant effects - challenging placement */
        SIGNIFICANT,
        /** Intense effects - Saturn debilitated or heavily afflicted */
        INTENSE;

        fun getLocalizedName(language: Language): String = when (this) {
            MILD -> StringResources.get(StringKey.SEVERITY_MILD, language)
            MODERATE -> StringResources.get(StringKey.SEVERITY_MODERATE, language)
            SIGNIFICANT -> StringResources.get(StringKey.SEVERITY_SIGNIFICANT, language)
            INTENSE -> StringResources.get(StringKey.SEVERITY_INTENSE, language)
        }
    }

    /**
     * Small Panoti (Dhaiya) - Saturn in 4th or 8th from Moon
     * A lesser but significant Saturn transit affecting ~2.5 years each
     */
    enum class SmallPanotiPhase {
        /** Saturn in 4th from Moon - Ashtama Shani precursor */
        FOURTH_FROM_MOON,
        /** Saturn in 8th from Moon - Ashtama Shani (most challenging) */
        EIGHTH_FROM_MOON;

        fun getLocalizedName(language: Language): String = when (this) {
            FOURTH_FROM_MOON -> StringResources.get(StringKey.SMALL_PANOTI_FOURTH, language)
            EIGHTH_FROM_MOON -> StringResources.get(StringKey.SMALL_PANOTI_EIGHTH, language)
        }
    }

    /**
     * Complete Sade Sati analysis result
     */
    data class SadeSatiAnalysis(
        val isActive: Boolean,
        val currentPhase: SadeSatiPhase?,
        val severity: Severity,
        val moonSign: ZodiacSign,
        val saturnSign: ZodiacSign,
        val saturnLongitude: Double,
        val progressInCurrentPhase: Double,
        val approximateDaysRemaining: Int,
        val approximateEndDate: LocalDate?,
        val smallPanoti: SmallPanotiPhase?,
        val isSmallPanotiActive: Boolean,
        val favorableFactors: List<String>,
        val challengingFactors: List<String>,
        val remedies: List<Remedy>,
        val interpretation: String
    ) {
        /**
         * Get localized summary of the Sade Sati status
         */
        fun getSummary(language: Language): String {
            return if (isActive && currentPhase != null) {
                val phaseName = currentPhase.getLocalizedName(language)
                val severityName = severity.getLocalizedName(language)
                StringResources.get(StringKey.SADE_SATI_ACTIVE_SUMMARY, language)
                    .replace("{phase}", phaseName)
                    .replace("{severity}", severityName)
            } else if (isSmallPanotiActive && smallPanoti != null) {
                val panotiName = smallPanoti.getLocalizedName(language)
                StringResources.get(StringKey.SMALL_PANOTI_ACTIVE_SUMMARY, language)
                    .replace("{type}", panotiName)
            } else {
                StringResources.get(StringKey.SADE_SATI_NOT_ACTIVE, language)
            }
        }
    }

    /**
     * Remedy for Sade Sati mitigation
     */
    data class Remedy(
        val type: RemedyType,
        val titleKey: StringKey,
        val descriptionKey: StringKey,
        val timing: String?,
        val mantra: String?
    ) {
        fun getTitle(language: Language): String = StringResources.get(titleKey, language)
        fun getDescription(language: Language): String = StringResources.get(descriptionKey, language)
    }

    enum class RemedyType {
        MANTRA, GEMSTONE, CHARITY, RITUAL, FASTING, WORSHIP
    }

    /**
     * Calculate Sade Sati analysis for a birth chart with current Saturn position
     *
     * @param natalChart The birth chart
     * @param currentSaturnLongitude Current transit Saturn's sidereal longitude
     * @param currentDate Current date for timeline calculations
     * @return Complete Sade Sati analysis
     */
    fun calculateSadeSati(
        natalChart: VedicChart,
        currentSaturnLongitude: Double,
        currentDate: LocalDate = LocalDate.now()
    ): SadeSatiAnalysis {
        val moonPosition = VedicAstrologyUtils.getMoonPosition(natalChart)
            ?: return createInactiveSadeSati(currentSaturnLongitude)

        val moonSign = moonPosition.sign
        val saturnSign = ZodiacSign.fromLongitude(currentSaturnLongitude)

        // Calculate house position of Saturn from Moon
        val houseFromMoon = calculateHouseFromMoon(saturnSign, moonSign)

        // Determine Sade Sati phase
        val sadeSatiPhase = when (houseFromMoon) {
            12 -> SadeSatiPhase.RISING
            1 -> SadeSatiPhase.PEAK
            2 -> SadeSatiPhase.SETTING
            else -> null
        }

        val isActive = sadeSatiPhase != null

        // Determine Small Panoti
        val smallPanotiPhase = when (houseFromMoon) {
            4 -> SmallPanotiPhase.FOURTH_FROM_MOON
            8 -> SmallPanotiPhase.EIGHTH_FROM_MOON
            else -> null
        }
        val isSmallPanotiActive = smallPanotiPhase != null

        // Calculate severity
        val severity = calculateSeverity(
            natalChart = natalChart,
            moonSign = moonSign,
            saturnSign = saturnSign,
            currentSaturnLongitude = currentSaturnLongitude,
            phase = sadeSatiPhase
        )

        // Calculate progress in current phase
        val progressInPhase = calculateProgressInPhase(
            currentSaturnLongitude = currentSaturnLongitude,
            saturnSign = saturnSign
        )

        // Calculate timeline
        val (daysRemaining, endDate) = calculateTimeline(
            currentSaturnLongitude = currentSaturnLongitude,
            saturnSign = saturnSign,
            moonSign = moonSign,
            sadeSatiPhase = sadeSatiPhase,
            currentDate = currentDate
        )

        // Gather factors
        val favorableFactors = gatherFavorableFactors(natalChart, moonSign, saturnSign)
        val challengingFactors = gatherChallengingFactors(natalChart, moonSign, saturnSign)

        // Get remedies
        val remedies = getRemedies(severity, sadeSatiPhase, smallPanotiPhase)

        // Generate interpretation
        val interpretation = generateInterpretation(
            isActive = isActive,
            phase = sadeSatiPhase,
            severity = severity,
            moonSign = moonSign,
            saturnSign = saturnSign,
            smallPanoti = smallPanotiPhase,
            natalChart = natalChart
        )

        return SadeSatiAnalysis(
            isActive = isActive,
            currentPhase = sadeSatiPhase,
            severity = severity,
            moonSign = moonSign,
            saturnSign = saturnSign,
            saturnLongitude = currentSaturnLongitude,
            progressInCurrentPhase = progressInPhase,
            approximateDaysRemaining = daysRemaining,
            approximateEndDate = endDate,
            smallPanoti = smallPanotiPhase,
            isSmallPanotiActive = isSmallPanotiActive,
            favorableFactors = favorableFactors,
            challengingFactors = challengingFactors,
            remedies = remedies,
            interpretation = interpretation
        )
    }

    private fun calculateHouseFromMoon(saturnSign: ZodiacSign, moonSign: ZodiacSign): Int {
        val diff = saturnSign.number - moonSign.number
        return if (diff >= 0) diff + 1 else diff + 13
    }

    private fun calculateSeverity(
        natalChart: VedicChart,
        moonSign: ZodiacSign,
        saturnSign: ZodiacSign,
        currentSaturnLongitude: Double,
        phase: SadeSatiPhase?
    ): Severity {
        var severityScore = 50 // Start with moderate

        // Check Saturn's dignity in transit sign
        when {
            VedicAstrologyUtils.isExalted(Planet.SATURN, saturnSign) -> severityScore -= 20
            VedicAstrologyUtils.isDebilitated(Planet.SATURN, saturnSign) -> severityScore += 20
            VedicAstrologyUtils.isInOwnSign(Planet.SATURN, saturnSign) -> severityScore -= 15
            VedicAstrologyUtils.isInFriendSign(Planet.SATURN, saturnSign) -> severityScore -= 10
            VedicAstrologyUtils.isInEnemySign(Planet.SATURN, saturnSign) -> severityScore += 10
        }

        // Peak phase is more intense
        if (phase == SadeSatiPhase.PEAK) {
            severityScore += 10
        }

        // Check natal Saturn's condition
        val natalSaturn = VedicAstrologyUtils.getPlanetPosition(natalChart, Planet.SATURN)
        if (natalSaturn != null) {
            when {
                VedicAstrologyUtils.isExalted(natalSaturn) -> severityScore -= 10
                VedicAstrologyUtils.isDebilitated(natalSaturn) -> severityScore += 10
                natalSaturn.isRetrograde -> severityScore += 5
            }
        }

        // Check Moon's condition (weak Moon = harder Sade Sati)
        val natalMoon = VedicAstrologyUtils.getMoonPosition(natalChart)
        if (natalMoon != null) {
            when {
                VedicAstrologyUtils.isExalted(natalMoon) -> severityScore -= 10
                VedicAstrologyUtils.isDebilitated(natalMoon) -> severityScore += 15
            }
        }

        // Check if Saturn is natural Yogakaraka for the ascendant
        val ascendantSign = VedicAstrologyUtils.getAscendantSign(natalChart)
        if (isSaturnYogakaraka(ascendantSign)) {
            severityScore -= 20
        }

        return when {
            severityScore <= 30 -> Severity.MILD
            severityScore <= 50 -> Severity.MODERATE
            severityScore <= 70 -> Severity.SIGNIFICANT
            else -> Severity.INTENSE
        }
    }

    private fun isSaturnYogakaraka(ascendant: ZodiacSign): Boolean {
        // Saturn is Yogakaraka for Taurus and Libra ascendants
        // (Rules a Kendra and a Trikona)
        return ascendant == ZodiacSign.TAURUS || ascendant == ZodiacSign.LIBRA
    }

    private fun calculateProgressInPhase(
        currentSaturnLongitude: Double,
        saturnSign: ZodiacSign
    ): Double {
        val degreeInSign = currentSaturnLongitude % 30.0
        return (degreeInSign / 30.0) * 100.0
    }

    private fun calculateTimeline(
        currentSaturnLongitude: Double,
        saturnSign: ZodiacSign,
        moonSign: ZodiacSign,
        sadeSatiPhase: SadeSatiPhase?,
        currentDate: LocalDate
    ): Pair<Int, LocalDate?> {
        if (sadeSatiPhase == null) {
            return Pair(0, null)
        }

        val degreeInSign = currentSaturnLongitude % 30.0
        val remainingDegreesInSign = 30.0 - degreeInSign

        // Calculate days to complete current sign
        val daysPerDegree = SATURN_TRANSIT_DAYS_PER_SIGN / 30.0
        val daysToCompleteCurrentSign = (remainingDegreesInSign * daysPerDegree).toInt()

        // Calculate remaining signs in Sade Sati
        val remainingSigns = when (sadeSatiPhase) {
            SadeSatiPhase.RISING -> 2 // Current + Peak + Setting signs remaining
            SadeSatiPhase.PEAK -> 1   // Current + Setting sign remaining
            SadeSatiPhase.SETTING -> 0 // Only current sign remaining
        }

        val totalDaysRemaining = daysToCompleteCurrentSign +
            (remainingSigns * SATURN_TRANSIT_DAYS_PER_SIGN).toInt()

        val endDate = currentDate.plusDays(totalDaysRemaining.toLong())

        return Pair(totalDaysRemaining, endDate)
    }

    private fun gatherFavorableFactors(
        natalChart: VedicChart,
        moonSign: ZodiacSign,
        saturnSign: ZodiacSign
    ): List<String> {
        val factors = mutableListOf<String>()

        if (VedicAstrologyUtils.isExalted(Planet.SATURN, saturnSign)) {
            factors.add("Saturn is exalted in transit - effects significantly reduced")
        }

        if (VedicAstrologyUtils.isInOwnSign(Planet.SATURN, saturnSign)) {
            factors.add("Saturn is in own sign - effects well-managed")
        }

        val ascendant = VedicAstrologyUtils.getAscendantSign(natalChart)
        if (isSaturnYogakaraka(ascendant)) {
            factors.add("Saturn is Yogakaraka for your ascendant - may bring positive results")
        }

        val natalSaturn = VedicAstrologyUtils.getPlanetPosition(natalChart, Planet.SATURN)
        if (natalSaturn != null && VedicAstrologyUtils.isExalted(natalSaturn)) {
            factors.add("Natal Saturn is strong - better equipped to handle transit")
        }

        return factors
    }

    private fun gatherChallengingFactors(
        natalChart: VedicChart,
        moonSign: ZodiacSign,
        saturnSign: ZodiacSign
    ): List<String> {
        val factors = mutableListOf<String>()

        if (VedicAstrologyUtils.isDebilitated(Planet.SATURN, saturnSign)) {
            factors.add("Saturn is debilitated in transit - effects may be more challenging")
        }

        val natalMoon = VedicAstrologyUtils.getMoonPosition(natalChart)
        if (natalMoon != null && VedicAstrologyUtils.isDebilitated(natalMoon)) {
            factors.add("Natal Moon is weak - emotional resilience may be tested")
        }

        val natalSaturn = VedicAstrologyUtils.getPlanetPosition(natalChart, Planet.SATURN)
        if (natalSaturn != null) {
            if (VedicAstrologyUtils.isDebilitated(natalSaturn)) {
                factors.add("Natal Saturn is weak - transit effects may be more pronounced")
            }
            if (natalSaturn.isRetrograde) {
                factors.add("Natal Saturn is retrograde - internal processing of karmic lessons")
            }
        }

        return factors
    }

    private fun getRemedies(
        severity: Severity,
        phase: SadeSatiPhase?,
        smallPanoti: SmallPanotiPhase?
    ): List<Remedy> {
        val remedies = mutableListOf<Remedy>()

        // Universal Saturn remedies
        remedies.add(Remedy(
            type = RemedyType.MANTRA,
            titleKey = StringKey.REMEDY_SHANI_MANTRA_TITLE,
            descriptionKey = StringKey.REMEDY_SHANI_MANTRA_DESC,
            timing = "Saturday during Saturn Hora",
            mantra = "ॐ शं शनैश्चराय नमः"
        ))

        remedies.add(Remedy(
            type = RemedyType.CHARITY,
            titleKey = StringKey.REMEDY_SATURDAY_CHARITY_TITLE,
            descriptionKey = StringKey.REMEDY_SATURDAY_CHARITY_DESC,
            timing = "Every Saturday",
            mantra = null
        ))

        remedies.add(Remedy(
            type = RemedyType.FASTING,
            titleKey = StringKey.REMEDY_SATURDAY_FAST_TITLE,
            descriptionKey = StringKey.REMEDY_SATURDAY_FAST_DESC,
            timing = "Every Saturday",
            mantra = null
        ))

        if (severity == Severity.SIGNIFICANT || severity == Severity.INTENSE) {
            remedies.add(Remedy(
                type = RemedyType.WORSHIP,
                titleKey = StringKey.REMEDY_HANUMAN_WORSHIP_TITLE,
                descriptionKey = StringKey.REMEDY_HANUMAN_WORSHIP_DESC,
                timing = "Tuesday and Saturday",
                mantra = "ॐ हनुमते नमः"
            ))

            remedies.add(Remedy(
                type = RemedyType.GEMSTONE,
                titleKey = StringKey.REMEDY_BLUE_SAPPHIRE_TITLE,
                descriptionKey = StringKey.REMEDY_BLUE_SAPPHIRE_DESC,
                timing = "Consult astrologer before wearing",
                mantra = null
            ))
        }

        return remedies
    }

    private fun generateInterpretation(
        isActive: Boolean,
        phase: SadeSatiPhase?,
        severity: Severity,
        moonSign: ZodiacSign,
        saturnSign: ZodiacSign,
        smallPanoti: SmallPanotiPhase?,
        natalChart: VedicChart
    ): String {
        return buildString {
            if (isActive && phase != null) {
                appendLine("SADE SATI ACTIVE - ${phase.name} PHASE")
                appendLine()
                appendLine("Saturn is currently transiting ${saturnSign.displayName}, which is the ${
                    when (phase) {
                        SadeSatiPhase.RISING -> "12th house"
                        SadeSatiPhase.PEAK -> "same sign"
                        SadeSatiPhase.SETTING -> "2nd house"
                    }
                } from your natal Moon in ${moonSign.displayName}.")
                appendLine()

                when (phase) {
                    SadeSatiPhase.RISING -> {
                        appendLine("RISING PHASE CHARACTERISTICS:")
                        appendLine("• Beginning of Sade Sati period")
                        appendLine("• Focus on expenses and losses (12th house)")
                        appendLine("• Sleep disturbances possible")
                        appendLine("• Hidden enemies may become active")
                        appendLine("• Spiritual growth opportunities")
                    }
                    SadeSatiPhase.PEAK -> {
                        appendLine("PEAK PHASE CHARACTERISTICS:")
                        appendLine("• Most intense phase of Sade Sati")
                        appendLine("• Direct impact on mind and emotions")
                        appendLine("• Health may need attention")
                        appendLine("• Self-image transformation")
                        appendLine("• Major life restructuring possible")
                    }
                    SadeSatiPhase.SETTING -> {
                        appendLine("SETTING PHASE CHARACTERISTICS:")
                        appendLine("• Final phase of Sade Sati")
                        appendLine("• Focus on finances and family (2nd house)")
                        appendLine("• Speech and communication impacted")
                        appendLine("• Accumulated wealth may fluctuate")
                        appendLine("• Integration of lessons learned")
                    }
                }

                appendLine()
                appendLine("SEVERITY: ${severity.name}")
            } else if (smallPanoti != null) {
                appendLine("SMALL PANOTI (DHAIYA) ACTIVE")
                appendLine()
                when (smallPanoti) {
                    SmallPanotiPhase.FOURTH_FROM_MOON -> {
                        appendLine("Saturn is transiting the 4th house from your Moon.")
                        appendLine("• Domestic peace may be disturbed")
                        appendLine("• Mother's health may need attention")
                        appendLine("• Property matters require caution")
                        appendLine("• Mental peace may fluctuate")
                    }
                    SmallPanotiPhase.EIGHTH_FROM_MOON -> {
                        appendLine("ASHTAMA SHANI - Saturn in 8th from Moon")
                        appendLine("This is considered one of the most challenging Saturn transits.")
                        appendLine("• Sudden changes and transformations")
                        appendLine("• Health requires vigilance")
                        appendLine("• Obstacles in ventures")
                        appendLine("• Deep psychological transformation")
                    }
                }
            } else {
                appendLine("SADE SATI NOT ACTIVE")
                appendLine()
                appendLine("Saturn is currently transiting ${saturnSign.displayName}, which does not form Sade Sati or Small Panoti with your natal Moon in ${moonSign.displayName}.")
                appendLine()
                appendLine("This is generally a favorable period regarding Saturn's influence on emotional and mental well-being.")
            }
        }
    }

    private fun createInactiveSadeSati(saturnLongitude: Double): SadeSatiAnalysis {
        val saturnSign = ZodiacSign.fromLongitude(saturnLongitude)
        return SadeSatiAnalysis(
            isActive = false,
            currentPhase = null,
            severity = Severity.MILD,
            moonSign = ZodiacSign.ARIES, // Default, will be overridden
            saturnSign = saturnSign,
            saturnLongitude = saturnLongitude,
            progressInCurrentPhase = 0.0,
            approximateDaysRemaining = 0,
            approximateEndDate = null,
            smallPanoti = null,
            isSmallPanotiActive = false,
            favorableFactors = emptyList(),
            challengingFactors = listOf("Unable to calculate - Moon position not found"),
            remedies = emptyList(),
            interpretation = "Unable to calculate Sade Sati - Moon position not available in chart."
        )
    }

    /**
     * Check if Sade Sati will start soon
     *
     * @param natalChart Birth chart
     * @param currentSaturnLongitude Current Saturn position
     * @param daysAhead Number of days to look ahead
     * @return True if Sade Sati will begin within the specified days
     */
    fun willSadeSatiStartSoon(
        natalChart: VedicChart,
        currentSaturnLongitude: Double,
        daysAhead: Int = 180
    ): Boolean {
        val moonPosition = VedicAstrologyUtils.getMoonPosition(natalChart) ?: return false
        val moonSign = moonPosition.sign

        // Calculate the sign 12th from Moon (where Sade Sati begins)
        // Note: ZodiacSign.number is 1-based, entries is 0-indexed
        val risingSignIndex = (moonSign.number - 1 + 11) % 12 // 12th from Moon = 11 signs ahead (0-indexed)
        val risingSign = ZodiacSign.entries[risingSignIndex]
        val currentSaturnSign = ZodiacSign.fromLongitude(currentSaturnLongitude)

        // Check if Saturn is in the sign just before the rising sign
        val preRisingSignIndex = (risingSign.number - 1 + 11) % 12 // 12th from rising = sign before
        val preRisingSign = ZodiacSign.entries[preRisingSignIndex]

        if (currentSaturnSign == preRisingSign) {
            val degreeInSign = currentSaturnLongitude % 30.0
            val degreesToNextSign = 30.0 - degreeInSign
            val daysPerDegree = SATURN_TRANSIT_DAYS_PER_SIGN / 30.0
            val daysToRisingSign = degreesToNextSign * daysPerDegree

            return daysToRisingSign <= daysAhead
        }

        return false
    }

    /**
     * Get the count of how many times a person has/will experience Sade Sati
     * based on their age
     *
     * @param birthYear Year of birth
     * @param moonSign Natal Moon sign
     * @return Estimated Sade Sati occurrence number
     */
    fun getSadeSatiOccurrence(birthYear: Int, moonSign: ZodiacSign): Int {
        val currentYear = LocalDate.now().year
        val age = currentYear - birthYear

        // Saturn takes ~29.5 years to complete one cycle
        // So Sade Sati occurs roughly every 29-30 years
        return (age / 29.5).toInt() + 1
    }
}
