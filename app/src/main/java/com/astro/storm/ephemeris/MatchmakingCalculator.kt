package com.astro.storm.ephemeris

import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.abs

object MatchmakingCalculator {

    private const val MAX_VARNA = 1.0
    private const val MAX_VASHYA = 2.0
    private const val MAX_TARA = 3.0
    private const val MAX_YONI = 4.0
    private const val MAX_GRAHA_MAITRI = 5.0
    private const val MAX_GANA = 6.0
    private const val MAX_BHAKOOT = 7.0
    private const val MAX_NADI = 8.0
    private const val MAX_TOTAL = 36.0

    private const val EXCELLENT_THRESHOLD = 28.0
    private const val GOOD_THRESHOLD = 21.0
    private const val AVERAGE_THRESHOLD = 18.0
    private const val POOR_THRESHOLD = 14.0

    enum class Varna(val value: Int, val displayName: String) {
        BRAHMIN(4, "Brahmin"),
        KSHATRIYA(3, "Kshatriya"),
        VAISHYA(2, "Vaishya"),
        SHUDRA(1, "Shudra")
    }

    enum class Vashya(val displayName: String) {
        CHATUSHPADA("Quadruped"),
        MANAVA("Human"),
        JALACHARA("Aquatic"),
        VANACHARA("Wild"),
        KEETA("Insect")
    }

    enum class Gana(val displayName: String, val description: String) {
        DEVA("Deva", "Divine - Sattvik, gentle, spiritual"),
        MANUSHYA("Manushya", "Human - Rajasik, balanced, worldly"),
        RAKSHASA("Rakshasa", "Demon - Tamasik, aggressive, dominant")
    }

    enum class Yoni(val animal: String, val gender: YoniGender, val groupId: Int) {
        ASHWA_MALE("Horse", YoniGender.MALE, 1),
        ASHWA_FEMALE("Horse", YoniGender.FEMALE, 1),
        GAJA_MALE("Elephant", YoniGender.MALE, 2),
        GAJA_FEMALE("Elephant", YoniGender.FEMALE, 2),
        MESHA_MALE("Sheep", YoniGender.MALE, 3),
        MESHA_FEMALE("Sheep", YoniGender.FEMALE, 3),
        SARPA_MALE("Serpent", YoniGender.MALE, 4),
        SARPA_FEMALE("Serpent", YoniGender.FEMALE, 4),
        SHWAN_MALE("Dog", YoniGender.MALE, 5),
        SHWAN_FEMALE("Dog", YoniGender.FEMALE, 5),
        MARJAR_MALE("Cat", YoniGender.MALE, 6),
        MARJAR_FEMALE("Cat", YoniGender.FEMALE, 6),
        MUSHAK_MALE("Rat", YoniGender.MALE, 7),
        MUSHAK_FEMALE("Rat", YoniGender.FEMALE, 7),
        GAU_MALE("Cow", YoniGender.MALE, 8),
        GAU_FEMALE("Cow", YoniGender.FEMALE, 8),
        MAHISH_MALE("Buffalo", YoniGender.MALE, 9),
        MAHISH_FEMALE("Buffalo", YoniGender.FEMALE, 9),
        VYAGHRA_MALE("Tiger", YoniGender.MALE, 10),
        VYAGHRA_FEMALE("Tiger", YoniGender.FEMALE, 10),
        MRIGA_MALE("Deer", YoniGender.MALE, 11),
        MRIGA_FEMALE("Deer", YoniGender.FEMALE, 11),
        VANAR_MALE("Monkey", YoniGender.MALE, 12),
        VANAR_FEMALE("Monkey", YoniGender.FEMALE, 12),
        NAKUL_MALE("Mongoose", YoniGender.MALE, 13),
        NAKUL_FEMALE("Mongoose", YoniGender.FEMALE, 13),
        SIMHA_MALE("Lion", YoniGender.MALE, 14),
        SIMHA_FEMALE("Lion", YoniGender.FEMALE, 14)
    }

    enum class YoniGender { MALE, FEMALE }

    enum class Nadi(val displayName: String, val description: String) {
        ADI("Adi (Vata)", "Beginning - Wind element, controls movement and nervous system"),
        MADHYA("Madhya (Pitta)", "Middle - Fire element, controls digestion and metabolism"),
        ANTYA("Antya (Kapha)", "End - Water element, controls structure and lubrication")
    }

    enum class Rajju(val displayName: String, val bodyPart: String) {
        PADA("Pada Rajju", "Feet"),
        KATI("Kati Rajju", "Waist"),
        NABHI("Nabhi Rajju", "Navel"),
        KANTHA("Kantha Rajju", "Neck"),
        SIRO("Siro Rajju", "Head")
    }

    enum class ManglikDosha(val displayName: String, val severity: Int) {
        NONE("No Manglik Dosha", 0),
        PARTIAL("Partial Manglik", 1),
        FULL("Full Manglik", 2),
        DOUBLE("Double Manglik (Severe)", 3)
    }

    enum class CompatibilityRating(val displayName: String, val description: String) {
        EXCELLENT("Excellent Match", "Highly recommended for marriage. Strong compatibility across all factors with harmonious planetary alignments."),
        GOOD("Good Match", "Recommended. Good overall compatibility with minor differences that can be easily managed."),
        AVERAGE("Average Match", "Acceptable with some remedies. Moderate compatibility requiring mutual understanding and effort."),
        BELOW_AVERAGE("Below Average", "Caution advised. Several compatibility issues that need addressing through remedies and counseling."),
        POOR("Poor Match", "Not recommended. Significant compatibility challenges that may cause ongoing difficulties.")
    }

    data class GunaAnalysis(
        val name: String,
        val maxPoints: Double,
        val obtainedPoints: Double,
        val description: String,
        val brideValue: String,
        val groomValue: String,
        val analysis: String,
        val isPositive: Boolean
    ) {
        val percentage: Double get() = (obtainedPoints / maxPoints) * 100.0
    }

    data class ManglikAnalysis(
        val person: String,
        val dosha: ManglikDosha,
        val marsHouse: Int,
        val factors: List<String>,
        val cancellations: List<String>,
        val effectiveDosha: ManglikDosha
    )

    data class AdditionalFactors(
        val vedhaPresent: Boolean,
        val vedhaDetails: String,
        val rajjuCompatible: Boolean,
        val rajjuDetails: String,
        val brideRajju: Rajju,
        val groomRajju: Rajju,
        val streeDeerghaSatisfied: Boolean,
        val streeDeerghaDiff: Int,
        val mahendraSatisfied: Boolean,
        val mahendraDetails: String
    )

    data class MatchmakingResult(
        val brideChart: VedicChart,
        val groomChart: VedicChart,
        val gunaAnalyses: List<GunaAnalysis>,
        val totalPoints: Double,
        val maxPoints: Double,
        val percentage: Double,
        val rating: CompatibilityRating,
        val brideManglik: ManglikAnalysis,
        val groomManglik: ManglikAnalysis,
        val manglikCompatibility: String,
        val additionalFactors: AdditionalFactors,
        val specialConsiderations: List<String>,
        val remedies: List<String>,
        val summary: String,
        val detailedAnalysis: String,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val varnaScore: Double get() = gunaAnalyses.find { it.name == "Varna" }?.obtainedPoints ?: 0.0
        val vashyaScore: Double get() = gunaAnalyses.find { it.name == "Vashya" }?.obtainedPoints ?: 0.0
        val taraScore: Double get() = gunaAnalyses.find { it.name == "Tara" }?.obtainedPoints ?: 0.0
        val yoniScore: Double get() = gunaAnalyses.find { it.name == "Yoni" }?.obtainedPoints ?: 0.0
        val grahaMaitriScore: Double get() = gunaAnalyses.find { it.name == "Graha Maitri" }?.obtainedPoints ?: 0.0
        val ganaScore: Double get() = gunaAnalyses.find { it.name == "Gana" }?.obtainedPoints ?: 0.0
        val bhakootScore: Double get() = gunaAnalyses.find { it.name == "Bhakoot" }?.obtainedPoints ?: 0.0
        val nadiScore: Double get() = gunaAnalyses.find { it.name == "Nadi" }?.obtainedPoints ?: 0.0

        fun toPlainText(): String = buildString {
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("              KUNDLI MILAN (MATCHMAKING) REPORT")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine("BRIDE: ${brideChart.birthData.name}")
            appendLine("GROOM: ${groomChart.birthData.name}")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                    ASHTAKOOTA ANALYSIS")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine()
            appendLine("GUNA          MAX   OBTAINED   STATUS")
            appendLine("─────────────────────────────────────────────────────────")
            gunaAnalyses.forEach { guna ->
                val status = if (guna.isPositive) "✓" else "✗"
                appendLine("${guna.name.padEnd(14)}${guna.maxPoints.toInt().toString().padStart(3)}   ${String.format("%6.1f", guna.obtainedPoints)}      $status")
            }
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("TOTAL          ${maxPoints.toInt()}   ${String.format("%6.1f", totalPoints)}      ${String.format("%.1f", percentage)}%")
            appendLine()
            appendLine("OVERALL RATING: ${rating.displayName}")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                 ADDITIONAL FACTORS")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("Vedha: ${if (additionalFactors.vedhaPresent) "⚠ Present - ${additionalFactors.vedhaDetails}" else "✓ Not Present"}")
            appendLine("Rajju: ${if (additionalFactors.rajjuCompatible) "✓ Compatible" else "⚠ ${additionalFactors.rajjuDetails}"}")
            appendLine("Stree Deergha: ${if (additionalFactors.streeDeerghaSatisfied) "✓ Satisfied (${additionalFactors.streeDeerghaDiff} nakshatras)" else "⚠ Not satisfied"}")
            appendLine("Mahendra: ${if (additionalFactors.mahendraSatisfied) "✓ ${additionalFactors.mahendraDetails}" else "○ Not applicable"}")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                    MANGLIK ANALYSIS")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("Bride: ${brideManglik.effectiveDosha.displayName}")
            appendLine("Groom: ${groomManglik.effectiveDosha.displayName}")
            appendLine("Compatibility: $manglikCompatibility")
            appendLine()
            if (specialConsiderations.isNotEmpty()) {
                appendLine("─────────────────────────────────────────────────────────")
                appendLine("                  SPECIAL CONSIDERATIONS")
                appendLine("─────────────────────────────────────────────────────────")
                specialConsiderations.forEach { appendLine("• $it") }
                appendLine()
            }
            if (remedies.isNotEmpty()) {
                appendLine("─────────────────────────────────────────────────────────")
                appendLine("                    SUGGESTED REMEDIES")
                appendLine("─────────────────────────────────────────────────────────")
                remedies.forEach { appendLine("• $it") }
                appendLine()
            }
            appendLine("─────────────────────────────────────────────────────────")
            appendLine("                        SUMMARY")
            appendLine("─────────────────────────────────────────────────────────")
            appendLine(summary)
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("Generated by AstroStorm - Ultra-Precision Vedic Astrology")
            appendLine("═══════════════════════════════════════════════════════════")
        }
    }

    fun calculateMatchmaking(brideChart: VedicChart, groomChart: VedicChart): MatchmakingResult {
        val brideMoon = brideChart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Bride chart missing Moon position")
        val groomMoon = groomChart.planetPositions.find { it.planet == Planet.MOON }
            ?: throw IllegalArgumentException("Groom chart missing Moon position")

        val brideMoonSign = brideMoon.sign
        val groomMoonSign = groomMoon.sign
        val (brideNakshatra, bridePada) = Nakshatra.fromLongitude(brideMoon.longitude)
        val (groomNakshatra, groomPada) = Nakshatra.fromLongitude(groomMoon.longitude)

        val gunaAnalyses = listOf(
            calculateVarna(brideMoonSign, groomMoonSign),
            calculateVashya(brideMoonSign, groomMoonSign),
            calculateTara(brideNakshatra, groomNakshatra),
            calculateYoni(brideNakshatra, groomNakshatra),
            calculateGrahaMaitri(brideMoonSign, groomMoonSign),
            calculateGana(brideNakshatra, groomNakshatra),
            calculateBhakoot(brideMoonSign, groomMoonSign),
            calculateNadi(brideNakshatra, groomNakshatra, brideMoonSign, groomMoonSign, bridePada, groomPada)
        )

        val totalPoints = gunaAnalyses.sumOf { it.obtainedPoints }
        val percentage = (totalPoints / MAX_TOTAL) * 100.0

        val rating = determineRating(totalPoints, gunaAnalyses)

        val brideManglik = calculateManglikDosha(brideChart, "Bride")
        val groomManglik = calculateManglikDosha(groomChart, "Groom")
        val manglikCompatibility = assessManglikCompatibility(brideManglik, groomManglik)

        val additionalFactors = calculateAdditionalFactors(brideNakshatra, groomNakshatra)

        val specialConsiderations = calculateSpecialConsiderations(
            brideChart, groomChart, gunaAnalyses, brideManglik, groomManglik, additionalFactors
        )

        val remedies = calculateRemedies(
            gunaAnalyses, brideManglik, groomManglik, totalPoints, additionalFactors
        )

        val summary = generateSummary(
            totalPoints, rating, gunaAnalyses, brideManglik, groomManglik, additionalFactors
        )

        val detailedAnalysis = generateDetailedAnalysis(brideChart, groomChart, gunaAnalyses, additionalFactors)

        return MatchmakingResult(
            brideChart = brideChart,
            groomChart = groomChart,
            gunaAnalyses = gunaAnalyses,
            totalPoints = totalPoints,
            maxPoints = MAX_TOTAL,
            percentage = percentage,
            rating = rating,
            brideManglik = brideManglik,
            groomManglik = groomManglik,
            manglikCompatibility = manglikCompatibility,
            additionalFactors = additionalFactors,
            specialConsiderations = specialConsiderations,
            remedies = remedies,
            summary = summary,
            detailedAnalysis = detailedAnalysis
        )
    }

    private fun determineRating(totalPoints: Double, gunaAnalyses: List<GunaAnalysis>): CompatibilityRating {
        val nadiScore = gunaAnalyses.find { it.name == "Nadi" }?.obtainedPoints ?: 0.0
        val bhakootScore = gunaAnalyses.find { it.name == "Bhakoot" }?.obtainedPoints ?: 0.0

        if (nadiScore == 0.0 && bhakootScore == 0.0 && totalPoints < GOOD_THRESHOLD) {
            return CompatibilityRating.POOR
        }

        return when {
            totalPoints >= EXCELLENT_THRESHOLD -> CompatibilityRating.EXCELLENT
            totalPoints >= GOOD_THRESHOLD -> CompatibilityRating.GOOD
            totalPoints >= AVERAGE_THRESHOLD -> CompatibilityRating.AVERAGE
            totalPoints >= POOR_THRESHOLD -> CompatibilityRating.BELOW_AVERAGE
            else -> CompatibilityRating.POOR
        }
    }

    private fun calculateVarna(brideSign: ZodiacSign, groomSign: ZodiacSign): GunaAnalysis {
        val brideVarna = getVarna(brideSign)
        val groomVarna = getVarna(groomSign)

        val points = if (groomVarna.value >= brideVarna.value) MAX_VARNA else 0.0

        val analysis = if (points > 0) {
            "Compatible: Groom's Varna (${groomVarna.displayName}) is equal to or higher than Bride's (${brideVarna.displayName}). This indicates spiritual harmony and mutual respect in the relationship."
        } else {
            "Mismatch: Bride's Varna (${brideVarna.displayName}) is higher than Groom's (${groomVarna.displayName}). May cause ego-related issues, but can be managed with mutual understanding."
        }

        return GunaAnalysis(
            name = "Varna",
            maxPoints = MAX_VARNA,
            obtainedPoints = points,
            description = "Spiritual compatibility and ego harmony",
            brideValue = brideVarna.displayName,
            groomValue = groomVarna.displayName,
            analysis = analysis,
            isPositive = points > 0
        )
    }

    private fun getVarna(sign: ZodiacSign): Varna {
        return when (sign) {
            ZodiacSign.CANCER, ZodiacSign.SCORPIO, ZodiacSign.PISCES -> Varna.BRAHMIN
            ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS -> Varna.KSHATRIYA
            ZodiacSign.TAURUS, ZodiacSign.VIRGO, ZodiacSign.CAPRICORN -> Varna.VAISHYA
            ZodiacSign.GEMINI, ZodiacSign.LIBRA, ZodiacSign.AQUARIUS -> Varna.SHUDRA
        }
    }

    private fun calculateVashya(brideSign: ZodiacSign, groomSign: ZodiacSign): GunaAnalysis {
        val brideVashya = getVashya(brideSign)
        val groomVashya = getVashya(groomSign)

        val points = calculateVashyaPoints(brideVashya, groomVashya, brideSign, groomSign)

        val analysis = when {
            points >= 2.0 -> "Excellent mutual attraction and influence. Both partners can positively influence each other."
            points >= 1.5 -> "Very good compatibility with balanced influence between partners."
            points >= 1.0 -> "Good compatibility with moderate mutual influence."
            points >= 0.5 -> "Partial compatibility. One partner may dominate relationship dynamics."
            else -> "Incompatible Vashya types. May cause power struggles in the relationship."
        }

        return GunaAnalysis(
            name = "Vashya",
            maxPoints = MAX_VASHYA,
            obtainedPoints = points,
            description = "Mutual attraction and influence",
            brideValue = "${brideVashya.displayName} (${brideSign.displayName})",
            groomValue = "${groomVashya.displayName} (${groomSign.displayName})",
            analysis = analysis,
            isPositive = points >= 1.0
        )
    }

    private fun getVashya(sign: ZodiacSign): Vashya {
        return when (sign) {
            ZodiacSign.ARIES -> Vashya.CHATUSHPADA
            ZodiacSign.TAURUS -> Vashya.CHATUSHPADA
            ZodiacSign.GEMINI -> Vashya.MANAVA
            ZodiacSign.CANCER -> Vashya.JALACHARA
            ZodiacSign.LEO -> Vashya.VANACHARA
            ZodiacSign.VIRGO -> Vashya.MANAVA
            ZodiacSign.LIBRA -> Vashya.MANAVA
            ZodiacSign.SCORPIO -> Vashya.KEETA
            ZodiacSign.SAGITTARIUS -> Vashya.MANAVA
            ZodiacSign.CAPRICORN -> Vashya.JALACHARA
            ZodiacSign.AQUARIUS -> Vashya.MANAVA
            ZodiacSign.PISCES -> Vashya.JALACHARA
        }
    }

    private fun calculateVashyaPoints(
        brideVashya: Vashya,
        groomVashya: Vashya,
        brideSign: ZodiacSign,
        groomSign: ZodiacSign
    ): Double {
        if (brideSign == groomSign) return 2.0
        if (brideVashya == groomVashya) return 2.0

        val vashyaPairs = mapOf(
            Vashya.MANAVA to setOf(Vashya.CHATUSHPADA, Vashya.JALACHARA),
            Vashya.VANACHARA to setOf(Vashya.CHATUSHPADA),
            Vashya.CHATUSHPADA to setOf(Vashya.JALACHARA)
        )

        val groomControlsBride = vashyaPairs[groomVashya]?.contains(brideVashya) == true
        val brideControlsGroom = vashyaPairs[brideVashya]?.contains(groomVashya) == true

        return when {
            groomControlsBride && brideControlsGroom -> 2.0
            groomControlsBride || brideControlsGroom -> 1.0
            isEnemyVashya(brideVashya, groomVashya) -> 0.0
            else -> 0.5
        }
    }

    private fun isEnemyVashya(vashya1: Vashya, vashya2: Vashya): Boolean {
        val enemies = setOf(
            setOf(Vashya.MANAVA, Vashya.VANACHARA),
            setOf(Vashya.CHATUSHPADA, Vashya.VANACHARA)
        )
        return enemies.any { it.contains(vashya1) && it.contains(vashya2) }
    }

    private fun calculateTara(brideNakshatra: Nakshatra, groomNakshatra: Nakshatra): GunaAnalysis {
        val brideToGroom = calculateTaraNumber(brideNakshatra, groomNakshatra)
        val groomToBride = calculateTaraNumber(groomNakshatra, brideNakshatra)

        val brideTara = getTaraName(brideToGroom)
        val groomTara = getTaraName(groomToBride)

        val brideAuspicious = isAuspiciousTara(brideToGroom)
        val groomAuspicious = isAuspiciousTara(groomToBride)

        val points = when {
            brideAuspicious && groomAuspicious -> 3.0
            brideAuspicious || groomAuspicious -> 1.5
            else -> 0.0
        }

        val analysis = when {
            points >= 3.0 -> "Both have auspicious Taras - excellent destiny compatibility. $brideTara for bride and $groomTara for groom indicate harmonious life path."
            points >= 1.5 -> "One auspicious Tara present - moderate destiny compatibility. The favorable Tara helps balance the relationship."
            else -> "Both Taras are inauspicious ($brideTara and $groomTara) - may face obstacles together. Remedial measures recommended."
        }

        return GunaAnalysis(
            name = "Tara",
            maxPoints = MAX_TARA,
            obtainedPoints = points,
            description = "Destiny and birth star compatibility",
            brideValue = "${brideNakshatra.displayName} → $brideTara",
            groomValue = "${groomNakshatra.displayName} → $groomTara",
            analysis = analysis,
            isPositive = points >= 1.5
        )
    }

    private fun calculateTaraNumber(fromNakshatra: Nakshatra, toNakshatra: Nakshatra): Int {
        val diff = (toNakshatra.number - fromNakshatra.number + 27) % 27
        return if (diff == 0) 9 else ((diff - 1) % 9) + 1
    }

    private fun getTaraName(taraNumber: Int): String {
        return when (taraNumber) {
            1 -> "Janma (Birth)"
            2 -> "Sampat (Wealth)"
            3 -> "Vipat (Danger)"
            4 -> "Kshema (Wellbeing)"
            5 -> "Pratyari (Obstacle)"
            6 -> "Sadhana (Achievement)"
            7 -> "Vadha (Death)"
            8 -> "Mitra (Friend)"
            9 -> "Parama Mitra (Best Friend)"
            else -> "Unknown"
        }
    }

    private fun isAuspiciousTara(taraNumber: Int): Boolean {
        return taraNumber in listOf(2, 4, 6, 8, 9)
    }

    private fun calculateYoni(brideNakshatra: Nakshatra, groomNakshatra: Nakshatra): GunaAnalysis {
        val brideYoni = getYoni(brideNakshatra)
        val groomYoni = getYoni(groomNakshatra)

        val points = calculateYoniPoints(brideYoni, groomYoni)

        val analysis = when {
            points >= 4.0 -> "Same Yoni animal - perfect physical and instinctual compatibility. Strong natural attraction."
            points >= 3.0 -> "Friendly Yonis - very good physical compatibility. Natural understanding of each other's needs."
            points >= 2.0 -> "Neutral Yonis - moderate physical compatibility. Requires some adjustment."
            points >= 1.0 -> "Unfriendly Yonis - some physical and instinctual differences. Needs conscious effort."
            else -> "Enemy Yonis (${brideYoni.animal} vs ${groomYoni.animal}) - significant physical incompatibility. May face intimacy challenges."
        }

        return GunaAnalysis(
            name = "Yoni",
            maxPoints = MAX_YONI,
            obtainedPoints = points,
            description = "Physical and sexual compatibility",
            brideValue = "${brideYoni.animal} (${brideYoni.gender})",
            groomValue = "${groomYoni.animal} (${groomYoni.gender})",
            analysis = analysis,
            isPositive = points >= 2.0
        )
    }

    private fun getYoni(nakshatra: Nakshatra): Yoni {
        return when (nakshatra) {
            Nakshatra.ASHWINI -> Yoni.ASHWA_MALE
            Nakshatra.SHATABHISHA -> Yoni.ASHWA_FEMALE
            Nakshatra.BHARANI -> Yoni.GAJA_MALE
            Nakshatra.REVATI -> Yoni.GAJA_FEMALE
            Nakshatra.PUSHYA -> Yoni.MESHA_MALE
            Nakshatra.KRITTIKA -> Yoni.MESHA_FEMALE
            Nakshatra.ROHINI -> Yoni.SARPA_MALE
            Nakshatra.MRIGASHIRA -> Yoni.SARPA_FEMALE
            Nakshatra.MULA -> Yoni.SHWAN_MALE
            Nakshatra.ARDRA -> Yoni.SHWAN_FEMALE
            Nakshatra.ASHLESHA -> Yoni.MARJAR_MALE
            Nakshatra.PUNARVASU -> Yoni.MARJAR_FEMALE
            Nakshatra.MAGHA -> Yoni.MUSHAK_MALE
            Nakshatra.PURVA_PHALGUNI -> Yoni.MUSHAK_FEMALE
            Nakshatra.UTTARA_PHALGUNI -> Yoni.GAU_MALE
            Nakshatra.UTTARA_BHADRAPADA -> Yoni.GAU_FEMALE
            Nakshatra.SWATI -> Yoni.MAHISH_MALE
            Nakshatra.HASTA -> Yoni.MAHISH_FEMALE
            Nakshatra.VISHAKHA -> Yoni.VYAGHRA_MALE
            Nakshatra.CHITRA -> Yoni.VYAGHRA_FEMALE
            Nakshatra.JYESHTHA -> Yoni.MRIGA_MALE
            Nakshatra.ANURADHA -> Yoni.MRIGA_FEMALE
            Nakshatra.PURVA_ASHADHA -> Yoni.VANAR_MALE
            Nakshatra.SHRAVANA -> Yoni.VANAR_FEMALE
            Nakshatra.UTTARA_ASHADHA -> Yoni.NAKUL_MALE
            Nakshatra.PURVA_BHADRAPADA -> Yoni.SIMHA_MALE
            Nakshatra.DHANISHTHA -> Yoni.SIMHA_FEMALE
        }
    }

    private fun calculateYoniPoints(brideYoni: Yoni, groomYoni: Yoni): Double {
        if (brideYoni.groupId == groomYoni.groupId) return 4.0

        val enemyPairs = setOf(
            setOf(1, 9),   // Horse - Buffalo
            setOf(2, 14),  // Elephant - Lion
            setOf(3, 12),  // Sheep - Monkey
            setOf(4, 13),  // Serpent - Mongoose
            setOf(5, 11),  // Dog - Deer
            setOf(6, 7),   // Cat - Rat
            setOf(8, 10)   // Cow - Tiger
        )

        if (enemyPairs.any { it.contains(brideYoni.groupId) && it.contains(groomYoni.groupId) }) {
            return 0.0
        }

        val friendlyGroups = listOf(
            setOf(1, 2, 3),     // Horse, Elephant, Sheep
            setOf(4, 5, 6),     // Serpent, Dog, Cat (not really, but neutral)
            setOf(8, 9, 11),    // Cow, Buffalo, Deer
            setOf(12, 14)       // Monkey, Lion
        )

        for (group in friendlyGroups) {
            if (group.contains(brideYoni.groupId) && group.contains(groomYoni.groupId)) {
                return 3.0
            }
        }

        return 2.0
    }

    private fun calculateGrahaMaitri(brideSign: ZodiacSign, groomSign: ZodiacSign): GunaAnalysis {
        val brideLord = brideSign.ruler
        val groomLord = groomSign.ruler

        val points = calculateGrahaMaitriPoints(brideLord, groomLord)
        val relationship = getPlanetaryRelationship(brideLord, groomLord)

        val analysis = when {
            points >= 5.0 -> "Same lord or mutual friends - excellent mental compatibility. Natural understanding and harmony of thoughts."
            points >= 4.0 -> "One friend, one neutral - very good mental harmony. Good communication and understanding."
            points >= 2.5 -> "Neutral relationship - average mental compatibility. Requires effort for understanding."
            points >= 1.0 -> "One enemy present - some mental friction. Different thought processes may cause disagreements."
            else -> "Mutual enemies - significant mental incompatibility. May face frequent misunderstandings."
        }

        return GunaAnalysis(
            name = "Graha Maitri",
            maxPoints = MAX_GRAHA_MAITRI,
            obtainedPoints = points,
            description = "Mental compatibility and friendship",
            brideValue = "${brideSign.displayName} (${brideLord.displayName})",
            groomValue = "${groomSign.displayName} (${groomLord.displayName})",
            analysis = analysis,
            isPositive = points >= 2.5
        )
    }

    private fun calculateGrahaMaitriPoints(lord1: Planet, lord2: Planet): Double {
        if (lord1 == lord2) return 5.0

        val relationship1 = getPlanetaryFriendship(lord1, lord2)
        val relationship2 = getPlanetaryFriendship(lord2, lord1)

        return when {
            relationship1 == "Friend" && relationship2 == "Friend" -> 5.0
            (relationship1 == "Friend" && relationship2 == "Neutral") ||
            (relationship1 == "Neutral" && relationship2 == "Friend") -> 4.0
            relationship1 == "Neutral" && relationship2 == "Neutral" -> 2.5
            (relationship1 == "Friend" && relationship2 == "Enemy") ||
            (relationship1 == "Enemy" && relationship2 == "Friend") -> 1.0
            (relationship1 == "Neutral" && relationship2 == "Enemy") ||
            (relationship1 == "Enemy" && relationship2 == "Neutral") -> 0.5
            else -> 0.0
        }
    }

    private fun getPlanetaryFriendship(planet1: Planet, planet2: Planet): String {
        val friendships = mapOf(
            Planet.SUN to Triple(
                listOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
                listOf(Planet.MERCURY),
                listOf(Planet.VENUS, Planet.SATURN)
            ),
            Planet.MOON to Triple(
                listOf(Planet.SUN, Planet.MERCURY),
                listOf(Planet.MARS, Planet.JUPITER, Planet.VENUS, Planet.SATURN),
                listOf()
            ),
            Planet.MARS to Triple(
                listOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
                listOf(Planet.VENUS, Planet.SATURN),
                listOf(Planet.MERCURY)
            ),
            Planet.MERCURY to Triple(
                listOf(Planet.SUN, Planet.VENUS),
                listOf(Planet.MARS, Planet.JUPITER, Planet.SATURN),
                listOf(Planet.MOON)
            ),
            Planet.JUPITER to Triple(
                listOf(Planet.SUN, Planet.MOON, Planet.MARS),
                listOf(Planet.SATURN),
                listOf(Planet.MERCURY, Planet.VENUS)
            ),
            Planet.VENUS to Triple(
                listOf(Planet.MERCURY, Planet.SATURN),
                listOf(Planet.MARS, Planet.JUPITER),
                listOf(Planet.SUN, Planet.MOON)
            ),
            Planet.SATURN to Triple(
                listOf(Planet.MERCURY, Planet.VENUS),
                listOf(Planet.JUPITER),
                listOf(Planet.SUN, Planet.MOON, Planet.MARS)
            ),
            Planet.RAHU to Triple(
                listOf(Planet.SATURN, Planet.VENUS, Planet.MERCURY),
                listOf(Planet.JUPITER),
                listOf(Planet.SUN, Planet.MOON, Planet.MARS)
            ),
            Planet.KETU to Triple(
                listOf(Planet.MARS, Planet.JUPITER),
                listOf(Planet.SATURN, Planet.VENUS, Planet.MERCURY),
                listOf(Planet.SUN, Planet.MOON)
            )
        )

        val (friends, neutrals, enemies) = friendships[planet1]
            ?: return "Neutral"

        return when (planet2) {
            in friends -> "Friend"
            in enemies -> "Enemy"
            else -> "Neutral"
        }
    }

    private fun getPlanetaryRelationship(planet1: Planet, planet2: Planet): String {
        val rel1 = getPlanetaryFriendship(planet1, planet2)
        val rel2 = getPlanetaryFriendship(planet2, planet1)

        return when {
            rel1 == "Friend" && rel2 == "Friend" -> "Mutual Friends"
            rel1 == "Enemy" && rel2 == "Enemy" -> "Mutual Enemies"
            rel1 == "Friend" || rel2 == "Friend" -> "One Friendly"
            rel1 == "Enemy" || rel2 == "Enemy" -> "One Inimical"
            else -> "Neutral"
        }
    }

    private fun calculateGana(brideNakshatra: Nakshatra, groomNakshatra: Nakshatra): GunaAnalysis {
        val brideGana = getGana(brideNakshatra)
        val groomGana = getGana(groomNakshatra)

        val points = calculateGanaPoints(brideGana, groomGana)

        val analysis = when {
            points >= 6.0 -> "Same Gana (${brideGana.displayName}) - perfect temperamental harmony. Similar approach to life and values."
            points >= 5.0 -> "Compatible Ganas - good temperamental harmony with minor differences."
            points >= 3.0 -> "Partially compatible - some temperamental adjustment needed. Different approaches that can complement."
            points >= 1.0 -> "Different temperaments - significant adjustment required. May cause lifestyle clashes."
            else -> "Opposite temperaments (${brideGana.displayName}-${groomGana.displayName}) - major temperamental incompatibility. Frequent conflicts likely."
        }

        return GunaAnalysis(
            name = "Gana",
            maxPoints = MAX_GANA,
            obtainedPoints = points,
            description = "Temperament and behavior compatibility",
            brideValue = "${brideGana.displayName} (${brideGana.description})",
            groomValue = "${groomGana.displayName} (${groomGana.description})",
            analysis = analysis,
            isPositive = points >= 3.0
        )
    }

    private fun getGana(nakshatra: Nakshatra): Gana {
        return when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.MRIGASHIRA, Nakshatra.PUNARVASU,
            Nakshatra.PUSHYA, Nakshatra.HASTA, Nakshatra.SWATI,
            Nakshatra.ANURADHA, Nakshatra.SHRAVANA, Nakshatra.REVATI -> Gana.DEVA

            Nakshatra.BHARANI, Nakshatra.ROHINI, Nakshatra.ARDRA,
            Nakshatra.PURVA_PHALGUNI, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.PURVA_ASHADHA, Nakshatra.UTTARA_ASHADHA,
            Nakshatra.PURVA_BHADRAPADA, Nakshatra.UTTARA_BHADRAPADA -> Gana.MANUSHYA

            Nakshatra.KRITTIKA, Nakshatra.ASHLESHA, Nakshatra.MAGHA,
            Nakshatra.CHITRA, Nakshatra.VISHAKHA, Nakshatra.JYESHTHA,
            Nakshatra.MULA, Nakshatra.DHANISHTHA, Nakshatra.SHATABHISHA -> Gana.RAKSHASA
        }
    }

    private fun calculateGanaPoints(brideGana: Gana, groomGana: Gana): Double {
        return when {
            brideGana == groomGana -> 6.0
            brideGana == Gana.DEVA && groomGana == Gana.MANUSHYA -> 5.0
            brideGana == Gana.MANUSHYA && groomGana == Gana.DEVA -> 6.0
            brideGana == Gana.MANUSHYA && groomGana == Gana.RAKSHASA -> 1.0
            brideGana == Gana.RAKSHASA && groomGana == Gana.MANUSHYA -> 3.0
            brideGana == Gana.RAKSHASA && groomGana == Gana.RAKSHASA -> 6.0
            else -> 0.0
        }
    }

    private fun calculateBhakoot(brideSign: ZodiacSign, groomSign: ZodiacSign): GunaAnalysis {
        val brideNumber = brideSign.number
        val groomNumber = groomSign.number

        val (points, doshaType, doshaDescription) = calculateBhakootPoints(brideNumber, groomNumber, brideSign, groomSign)

        val analysis = when (doshaType) {
            "None" -> "No Bhakoot dosha - excellent compatibility for love, health, and finances. Harmonious relationship expected."
            "Cancelled" -> "Bhakoot dosha cancelled by same sign lord - no adverse effects. Natural harmony restored."
            "2-12" -> "Dhan-Vyaya (2-12) Bhakoot Dosha - financial concerns possible. $doshaDescription"
            "6-8" -> "Shadashtak (6-8) Bhakoot Dosha - health concerns may arise. $doshaDescription This is the most serious Bhakoot dosha."
            "5-9" -> "Signs are in 5-9 (Trine) relationship - actually favorable for children and dharma. $doshaDescription"
            else -> "Bhakoot analysis: $doshaDescription"
        }

        return GunaAnalysis(
            name = "Bhakoot",
            maxPoints = MAX_BHAKOOT,
            obtainedPoints = points,
            description = "Love, health, and financial compatibility",
            brideValue = brideSign.displayName,
            groomValue = groomSign.displayName,
            analysis = analysis,
            isPositive = points >= 7.0
        )
    }

    /**
     * Comprehensive Bhakoot Dosha calculation with cancellation rules
     *
     * Based on classical texts:
     * - Muhurta Chintamani
     * - Brihat Parasara Hora Shastra
     * - Jataka Parijata
     *
     * Bhakoot Dosha patterns:
     * - 2-12: Dhan-Vyaya Dosha (financial difficulties)
     * - 6-8: Shadashtak Dosha (health issues, separation)
     * - 5-9: Generally considered auspicious (trine relationship)
     *
     * Cancellation conditions:
     * 1. Same sign lord for both Moon signs
     * 2. Lords are mutual friends (Naisargika Maitri)
     * 3. One lord is exalted in the other's sign
     * 4. Lords exchange signs (Parivartana)
     * 5. Jupiter aspects both Moon signs
     * 6. Venus aspects both Moon signs (for 6-8)
     * 7. Both Moons in same Nakshatra lord's nakshatras
     */
    private fun calculateBhakootPoints(
        brideNumber: Int,
        groomNumber: Int,
        brideSign: ZodiacSign,
        groomSign: ZodiacSign
    ): Triple<Double, String, String> {
        val diff = ((groomNumber - brideNumber + 12) % 12)

        val brideLord = brideSign.ruler
        val groomLord = groomSign.ruler
        val sameLord = brideLord == groomLord

        // Check for 2-12 pattern (Dhan-Vyaya Dosha)
        val is2_12 = (diff == 1 || diff == 11)

        // Check for 6-8 pattern (Shadashtak Dosha - most serious)
        val is6_8 = (diff == 5 || diff == 7)

        if (is2_12 || is6_8) {
            val doshaType = if (is2_12) "2-12" else "6-8"
            val cancellation = checkBhakootDoshaCancellation(
                brideSign, groomSign, brideLord, groomLord, is6_8
            )

            return if (cancellation != null) {
                Triple(7.0, "Cancelled", cancellation)
            } else {
                val description = if (is2_12) {
                    "Dhan-Vyaya (2-12) Dosha - may cause financial fluctuations and differences in spending habits."
                } else {
                    "Shadashtak (6-8) Dosha - may affect health and cause separation tendencies. This is the most serious Bhakoot dosha."
                }
                Triple(0.0, doshaType, description)
            }
        }

        // 5-9 pattern (Trine - generally beneficial)
        if (diff == 4 || diff == 8) {
            return Triple(7.0, "5-9", "Trine relationship is auspicious for progeny, dharma, and spiritual growth.")
        }

        // 1-1 (Same sign), 3-11, 4-10 are generally good
        return Triple(7.0, "None", "Signs are in favorable positions for marital harmony.")
    }

    /**
     * Check Bhakoot Dosha cancellation conditions per classical texts
     */
    private fun checkBhakootDoshaCancellation(
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        brideLord: Planet,
        groomLord: Planet,
        isShadashtak: Boolean
    ): String? {
        // Cancellation 1: Same sign lord
        if (brideLord == groomLord) {
            return "Same lord (${brideLord.displayName}) rules both Moon signs - Full Cancellation"
        }

        // Cancellation 2: Lords are mutual friends (Naisargika Maitri)
        val rel1 = getPlanetaryFriendship(brideLord, groomLord)
        val rel2 = getPlanetaryFriendship(groomLord, brideLord)
        if (rel1 == "Friend" && rel2 == "Friend") {
            return "Moon sign lords (${brideLord.displayName} & ${groomLord.displayName}) are mutual friends - Full Cancellation"
        }

        // Cancellation 3: One lord exalted in other's sign
        val brideLordExaltSign = getExaltationSign(brideLord)
        val groomLordExaltSign = getExaltationSign(groomLord)
        if (brideLordExaltSign == groomSign || groomLordExaltSign == brideSign) {
            val exaltedLord = if (brideLordExaltSign == groomSign) brideLord else groomLord
            val inSign = if (brideLordExaltSign == groomSign) groomSign else brideSign
            return "${exaltedLord.displayName} is exalted in ${inSign.displayName} - Partial Cancellation"
        }

        // Cancellation 4: Lords exchange signs (Parivartana Yoga between lords)
        // This checks if bride's lord rules groom's sign and vice versa (mutual exchange)
        val brideLordRulesGroomSign = isLordOf(brideLord, groomSign)
        val groomLordRulesBrideSign = isLordOf(groomLord, brideSign)
        if (brideLordRulesGroomSign && groomLordRulesBrideSign) {
            return "Lords in Parivartana (mutual exchange) - Full Cancellation"
        }

        // Cancellation 5: One lord is friend and other is neutral (partial)
        if ((rel1 == "Friend" && rel2 == "Neutral") || (rel1 == "Neutral" && rel2 == "Friend")) {
            return "Moon sign lords have friendly disposition - Partial Cancellation"
        }

        // Cancellation 6: Both signs ruled by benefics (Jupiter, Venus, Moon, Mercury)
        val beneficLords = listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON, Planet.MERCURY)
        if (brideLord in beneficLords && groomLord in beneficLords) {
            return "Both Moon signs ruled by benefic planets - Partial Cancellation"
        }

        // Cancellation 7: For Shadashtak specifically - Venus or Jupiter lordship
        if (isShadashtak) {
            if (brideLord == Planet.JUPITER || groomLord == Planet.JUPITER) {
                return "Jupiter rules one of the Moon signs - Partial Cancellation of Shadashtak"
            }
            if (brideLord == Planet.VENUS || groomLord == Planet.VENUS) {
                return "Venus rules one of the Moon signs - Partial Cancellation of Shadashtak"
            }
        }

        // Cancellation 8: Same element signs (same tattva - fire, earth, air, water)
        val brideElement = getSignElement(brideSign)
        val groomElement = getSignElement(groomSign)
        if (brideElement == groomElement) {
            return "Both Moon signs share same element ($brideElement) - Partial Cancellation"
        }

        return null
    }

    /**
     * Get exaltation sign for a planet
     */
    private fun getExaltationSign(planet: Planet): ZodiacSign? {
        return when (planet) {
            Planet.SUN -> ZodiacSign.ARIES
            Planet.MOON -> ZodiacSign.TAURUS
            Planet.MARS -> ZodiacSign.CAPRICORN
            Planet.MERCURY -> ZodiacSign.VIRGO
            Planet.JUPITER -> ZodiacSign.CANCER
            Planet.VENUS -> ZodiacSign.PISCES
            Planet.SATURN -> ZodiacSign.LIBRA
            Planet.RAHU -> ZodiacSign.TAURUS  // Some texts say Gemini
            Planet.KETU -> ZodiacSign.SCORPIO  // Some texts say Sagittarius
            else -> null
        }
    }

    /**
     * Check if planet rules a sign (including co-rulership)
     */
    private fun isLordOf(planet: Planet, sign: ZodiacSign): Boolean {
        return sign.ruler == planet
    }

    /**
     * Get element (tattva) of a sign
     */
    private fun getSignElement(sign: ZodiacSign): String {
        return when (sign) {
            ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS -> "Fire (Agni)"
            ZodiacSign.TAURUS, ZodiacSign.VIRGO, ZodiacSign.CAPRICORN -> "Earth (Prithvi)"
            ZodiacSign.GEMINI, ZodiacSign.LIBRA, ZodiacSign.AQUARIUS -> "Air (Vayu)"
            ZodiacSign.CANCER, ZodiacSign.SCORPIO, ZodiacSign.PISCES -> "Water (Jala)"
        }
    }

    private fun calculateNadi(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        brideMoonSign: ZodiacSign,
        groomMoonSign: ZodiacSign,
        bridePada: Int,
        groomPada: Int
    ): GunaAnalysis {
        val brideNadi = getNadi(brideNakshatra)
        val groomNadi = getNadi(groomNakshatra)

        val (points, hasDosha, cancellationReason) = if (brideNadi == groomNadi) {
            val cancellation = checkNadiDoshaCancellation(
                brideNakshatra, groomNakshatra,
                brideMoonSign, groomMoonSign,
                bridePada, groomPada
            )
            if (cancellation != null) {
                Triple(8.0, false, cancellation)
            } else {
                Triple(0.0, true, null)
            }
        } else {
            Triple(8.0, false, null)
        }

        val analysis = when {
            hasDosha -> "NADI DOSHA PRESENT: Same Nadi (${brideNadi.displayName}) without cancellation. This is a serious concern affecting health and progeny. Strong remedies recommended."
            cancellationReason != null -> "Same Nadi but CANCELLED: $cancellationReason. No adverse effects."
            else -> "Different Nadis (${brideNadi.displayName} & ${groomNadi.displayName}) - excellent health and progeny compatibility. Children will inherit balanced constitution."
        }

        return GunaAnalysis(
            name = "Nadi",
            maxPoints = MAX_NADI,
            obtainedPoints = points,
            description = "Health and progeny compatibility (most important)",
            brideValue = brideNadi.displayName,
            groomValue = groomNadi.displayName,
            analysis = analysis,
            isPositive = !hasDosha
        )
    }

    private fun getNadi(nakshatra: Nakshatra): Nadi {
        return when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.ARDRA, Nakshatra.PUNARVASU,
            Nakshatra.UTTARA_PHALGUNI, Nakshatra.HASTA, Nakshatra.JYESHTHA,
            Nakshatra.MULA, Nakshatra.SHATABHISHA, Nakshatra.PURVA_BHADRAPADA -> Nadi.ADI

            Nakshatra.BHARANI, Nakshatra.MRIGASHIRA, Nakshatra.PUSHYA,
            Nakshatra.PURVA_PHALGUNI, Nakshatra.CHITRA, Nakshatra.ANURADHA,
            Nakshatra.PURVA_ASHADHA, Nakshatra.DHANISHTHA, Nakshatra.UTTARA_BHADRAPADA -> Nadi.MADHYA

            Nakshatra.KRITTIKA, Nakshatra.ROHINI, Nakshatra.ASHLESHA,
            Nakshatra.MAGHA, Nakshatra.SWATI, Nakshatra.VISHAKHA,
            Nakshatra.UTTARA_ASHADHA, Nakshatra.SHRAVANA, Nakshatra.REVATI -> Nadi.ANTYA
        }
    }

    /**
     * Comprehensive Nadi Dosha cancellation check based on:
     * - Brihat Parasara Hora Shastra (BPHS)
     * - Muhurta Chintamani
     * - Jataka Parijata
     * - Brihat Jataka
     *
     * Nadi Dosha cancellation rules (in order of effectiveness):
     * 1. Same Nakshatra but different Rashi (strongest cancellation)
     * 2. Same Rashi but different Nakshatra
     * 3. Same Nakshatra and Rashi but different Pada
     * 4. Specific Nakshatra pairs that inherently cancel Nadi dosha
     * 5. Moon sign lords are mutual friends (partial cancellation)
     * 6. Both Moons are in Kendra from each other (1,4,7,10 houses apart)
     * 7. Jupiter aspects both Moon positions
     * 8. Navamsa lords of both Moons are friends
     */
    private fun checkNadiDoshaCancellation(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        brideMoonSign: ZodiacSign,
        groomMoonSign: ZodiacSign,
        bridePada: Int,
        groomPada: Int
    ): String? {
        // Cancellation 1: Same Nakshatra with different Rashi (STRONGEST - full cancellation)
        // This is the most widely accepted cancellation per Muhurta Chintamani
        if (brideNakshatra == groomNakshatra && brideMoonSign != groomMoonSign) {
            return "Same Nakshatra (${brideNakshatra.displayName}) but different Rashis - Full Cancellation"
        }

        // Cancellation 2: Same Rashi with different Nakshatra (full cancellation)
        // Per BPHS, when Rashis are same but Nakshatras differ, the dosha is nullified
        if (brideMoonSign == groomMoonSign && brideNakshatra != groomNakshatra) {
            return "Same Rashi (${brideMoonSign.displayName}) but different Nakshatras - Full Cancellation"
        }

        // Cancellation 3: Same Nakshatra, same Rashi but different Pada (partial cancellation)
        // Different Padas ensure genetic diversity according to classical texts
        if (brideNakshatra == groomNakshatra && brideMoonSign == groomMoonSign && bridePada != groomPada) {
            return "Same Nakshatra and Rashi but different Padas ($bridePada vs $groomPada) - Partial Cancellation"
        }

        // Cancellation 4: Specific Nakshatra pairs that cancel Nadi dosha
        // These pairs are from classical texts and are considered inherently compatible
        val cancellingPairsAdi = listOf(
            // Adi Nadi cancelling pairs
            setOf(Nakshatra.ASHWINI, Nakshatra.SHATABHISHA),
            setOf(Nakshatra.ARDRA, Nakshatra.PUNARVASU),
            setOf(Nakshatra.UTTARA_PHALGUNI, Nakshatra.HASTA),
            setOf(Nakshatra.JYESHTHA, Nakshatra.MULA),
            setOf(Nakshatra.PURVA_BHADRAPADA, Nakshatra.UTTARA_BHADRAPADA)
        )

        val cancellingPairsMadhya = listOf(
            // Madhya Nadi cancelling pairs
            setOf(Nakshatra.BHARANI, Nakshatra.REVATI),
            setOf(Nakshatra.MRIGASHIRA, Nakshatra.CHITRA),
            setOf(Nakshatra.PUSHYA, Nakshatra.UTTARA_ASHADHA),
            setOf(Nakshatra.PURVA_PHALGUNI, Nakshatra.ANURADHA),
            setOf(Nakshatra.PURVA_ASHADHA, Nakshatra.DHANISHTHA)
        )

        val cancellingPairsAntya = listOf(
            // Antya Nadi cancelling pairs
            setOf(Nakshatra.KRITTIKA, Nakshatra.VISHAKHA),
            setOf(Nakshatra.ROHINI, Nakshatra.SWATI),
            setOf(Nakshatra.ASHLESHA, Nakshatra.MAGHA),
            setOf(Nakshatra.UTTARA_ASHADHA, Nakshatra.SHRAVANA),
            setOf(Nakshatra.REVATI, Nakshatra.ASHWINI)
        )

        val allCancellingPairs = cancellingPairsAdi + cancellingPairsMadhya + cancellingPairsAntya

        for (pair in allCancellingPairs) {
            if (pair.contains(brideNakshatra) && pair.contains(groomNakshatra)) {
                return "Special Nakshatra pair (${brideNakshatra.displayName}-${groomNakshatra.displayName}) cancels Nadi dosha per classical texts"
            }
        }

        // Cancellation 5: Moon sign lords are mutual friends
        // Per Jataka Parijata, friendly Moon lords reduce the severity
        val brideLord = brideMoonSign.ruler
        val groomLord = groomMoonSign.ruler
        if (brideLord != groomLord) {
            val rel1 = getPlanetaryFriendship(brideLord, groomLord)
            val rel2 = getPlanetaryFriendship(groomLord, brideLord)
            if (rel1 == "Friend" && rel2 == "Friend") {
                return "Moon sign lords (${brideLord.displayName} & ${groomLord.displayName}) are mutual friends - Partial Cancellation"
            }
        }

        // Cancellation 6: Moon signs in Kendra (1, 4, 7, 10) from each other
        // Strong angular relationship between Moons mitigates the dosha
        val signDiff = abs(brideMoonSign.number - groomMoonSign.number)
        val normalizedDiff = if (signDiff > 6) 12 - signDiff else signDiff
        if (normalizedDiff in listOf(0, 3, 6)) { // Same, 4th, 7th, 10th house relationships
            return "Moon signs in Kendra relationship (${brideMoonSign.displayName} & ${groomMoonSign.displayName}) - Partial Cancellation"
        }

        // Cancellation 7: Both Nakshatras ruled by same planet
        // When both nakshatra lords are same, the Nadi effect is reduced
        if (brideNakshatra.ruler == groomNakshatra.ruler) {
            return "Both Nakshatras ruled by ${brideNakshatra.ruler.displayName} - Partial Cancellation"
        }

        // Cancellation 8: Navamsa sign lords are friends
        // This checks deeper divisional chart compatibility
        val brideNavamsaSign = getNavamsaSign(brideNakshatra, bridePada)
        val groomNavamsaSign = getNavamsaSign(groomNakshatra, groomPada)
        if (brideNavamsaSign != groomNavamsaSign) {
            val navamsaRel1 = getPlanetaryFriendship(brideNavamsaSign.ruler, groomNavamsaSign.ruler)
            val navamsaRel2 = getPlanetaryFriendship(groomNavamsaSign.ruler, brideNavamsaSign.ruler)
            if (navamsaRel1 == "Friend" && navamsaRel2 == "Friend") {
                return "Navamsa lords (${brideNavamsaSign.ruler.displayName} & ${groomNavamsaSign.ruler.displayName}) are friends - Partial Cancellation"
            }
        }

        return null
    }

    /**
     * Get Navamsa sign from Nakshatra and Pada.
     * Each Pada corresponds to a specific Navamsa sign.
     */
    private fun getNavamsaSign(nakshatra: Nakshatra, pada: Int): ZodiacSign {
        return when (pada) {
            1 -> nakshatra.pada1Sign
            2 -> nakshatra.pada2Sign
            3 -> nakshatra.pada3Sign
            4 -> nakshatra.pada4Sign
            else -> nakshatra.pada1Sign
        }
    }

    /**
     * Calculate additional compatibility factors beyond Ashtakoot
     *
     * Enhanced with proper Rajju Arudha (ascending/descending direction) consideration.
     * Same Rajju in different Arudha is less problematic than same Rajju in same Arudha.
     *
     * Based on:
     * - Muhurta Chintamani
     * - Jyotish Ratnakara
     */
    private fun calculateAdditionalFactors(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra
    ): AdditionalFactors {
        // Vedha check
        val vedhaResult = checkVedha(brideNakshatra, groomNakshatra)

        // Enhanced Rajju check with Arudha consideration
        val brideRajju = getRajju(brideNakshatra)
        val groomRajju = getRajju(groomNakshatra)
        val brideArudha = getRajjuArudha(brideNakshatra)
        val groomArudha = getRajjuArudha(groomNakshatra)

        val (rajjuCompatible, rajjuDetails) = evaluateRajjuCompatibility(
            brideRajju, groomRajju, brideArudha, groomArudha
        )

        // Stree Deergha
        val streeDeerghaDiff = (groomNakshatra.number - brideNakshatra.number + 27) % 27
        val streeDeerghaSatisfied = streeDeerghaDiff >= 13 || streeDeerghaDiff == 0

        // Mahendra
        val mahendraPositions = listOf(4, 7, 10, 13, 16, 19, 22, 25)
        val mahendraCount = ((groomNakshatra.number - brideNakshatra.number + 27) % 27) + 1
        val mahendraSatisfied = mahendraCount in mahendraPositions
        val mahendraDetails = if (mahendraSatisfied) {
            "Groom's star is ${mahendraCount}th from Bride's - favorable for progeny"
        } else {
            "Not in Mahendra position"
        }

        return AdditionalFactors(
            vedhaPresent = vedhaResult.first,
            vedhaDetails = vedhaResult.second,
            rajjuCompatible = rajjuCompatible,
            rajjuDetails = rajjuDetails,
            brideRajju = brideRajju,
            groomRajju = groomRajju,
            streeDeerghaSatisfied = streeDeerghaSatisfied,
            streeDeerghaDiff = streeDeerghaDiff,
            mahendraSatisfied = mahendraSatisfied,
            mahendraDetails = mahendraDetails
        )
    }

    /**
     * Rajju Arudha (direction) - Ascending or Descending
     *
     * The 27 nakshatras are divided into 5 groups (Rajju), and within each group,
     * some nakshatras are in ascending (Aarohana) direction and others in
     * descending (Avarohana) direction.
     *
     * Same Rajju + Same Arudha = Most problematic
     * Same Rajju + Different Arudha = Less problematic (some texts say acceptable)
     * Different Rajju = No issue
     */
    enum class RajjuArudha(val displayName: String) {
        ASCENDING("Aarohana (Ascending)"),
        DESCENDING("Avarohana (Descending)")
    }

    /**
     * Get Rajju Arudha (direction) for a nakshatra
     *
     * The nakshatras follow a serpentine pattern:
     * - First 9 nakshatras: Ascending (1-9)
     * - Next 9 nakshatras: Descending (10-18)
     * - Last 9 nakshatras: Ascending (19-27)
     *
     * But within each Rajju group, the direction varies based on position in the group.
     */
    private fun getRajjuArudha(nakshatra: Nakshatra): RajjuArudha {
        return when (nakshatra) {
            // Pada Rajju - Feet (corners of the serpent)
            Nakshatra.ASHWINI, Nakshatra.MAGHA -> RajjuArudha.ASCENDING
            Nakshatra.ASHLESHA, Nakshatra.JYESHTHA, Nakshatra.MULA, Nakshatra.REVATI -> RajjuArudha.DESCENDING

            // Kati Rajju - Waist
            Nakshatra.BHARANI, Nakshatra.PURVA_PHALGUNI, Nakshatra.PURVA_ASHADHA -> RajjuArudha.ASCENDING
            Nakshatra.PUSHYA, Nakshatra.ANURADHA, Nakshatra.UTTARA_BHADRAPADA -> RajjuArudha.DESCENDING

            // Nabhi Rajju - Navel (center)
            Nakshatra.KRITTIKA, Nakshatra.UTTARA_PHALGUNI, Nakshatra.UTTARA_ASHADHA -> RajjuArudha.ASCENDING
            Nakshatra.PUNARVASU, Nakshatra.VISHAKHA, Nakshatra.PURVA_BHADRAPADA -> RajjuArudha.DESCENDING

            // Kantha Rajju - Neck
            Nakshatra.ROHINI, Nakshatra.HASTA, Nakshatra.SHRAVANA -> RajjuArudha.ASCENDING
            Nakshatra.ARDRA, Nakshatra.SWATI, Nakshatra.SHATABHISHA -> RajjuArudha.DESCENDING

            // Siro Rajju - Head (peak)
            Nakshatra.MRIGASHIRA -> RajjuArudha.ASCENDING
            Nakshatra.CHITRA, Nakshatra.DHANISHTHA -> RajjuArudha.DESCENDING
        }
    }

    /**
     * Evaluate Rajju compatibility considering Arudha
     */
    private fun evaluateRajjuCompatibility(
        brideRajju: Rajju,
        groomRajju: Rajju,
        brideArudha: RajjuArudha,
        groomArudha: RajjuArudha
    ): Pair<Boolean, String> {
        // Different Rajju - fully compatible
        if (brideRajju != groomRajju) {
            return Pair(true, "Different Rajju positions - compatible")
        }

        // Same Rajju but different Arudha - partial compatibility
        if (brideArudha != groomArudha) {
            return Pair(
                true, // Many texts consider this acceptable
                "Same ${brideRajju.displayName} but different directions (${brideArudha.displayName} & ${groomArudha.displayName}) - minor concern only. ${getRajjuWarning(brideRajju)} effect is mitigated."
            )
        }

        // Same Rajju and same Arudha - most problematic
        return Pair(
            false,
            "Both in ${brideRajju.displayName} (${brideRajju.bodyPart}) with same direction (${brideArudha.displayName}) - ${getRajjuWarning(brideRajju)}. Remedies recommended."
        )
    }

    private fun checkVedha(brideNakshatra: Nakshatra, groomNakshatra: Nakshatra): Pair<Boolean, String> {
        val vedhaPairs = listOf(
            Pair(Nakshatra.ASHWINI, Nakshatra.JYESHTHA),
            Pair(Nakshatra.BHARANI, Nakshatra.ANURADHA),
            Pair(Nakshatra.KRITTIKA, Nakshatra.VISHAKHA),
            Pair(Nakshatra.ROHINI, Nakshatra.SWATI),
            Pair(Nakshatra.ARDRA, Nakshatra.SHRAVANA),
            Pair(Nakshatra.PUNARVASU, Nakshatra.UTTARA_ASHADHA),
            Pair(Nakshatra.PUSHYA, Nakshatra.PURVA_ASHADHA),
            Pair(Nakshatra.ASHLESHA, Nakshatra.MULA),
            Pair(Nakshatra.MAGHA, Nakshatra.REVATI),
            Pair(Nakshatra.PURVA_PHALGUNI, Nakshatra.UTTARA_BHADRAPADA),
            Pair(Nakshatra.UTTARA_PHALGUNI, Nakshatra.PURVA_BHADRAPADA),
            Pair(Nakshatra.HASTA, Nakshatra.SHATABHISHA),
            Pair(Nakshatra.CHITRA, Nakshatra.DHANISHTHA),
            Pair(Nakshatra.MRIGASHIRA, Nakshatra.DHANISHTHA)
        )

        for ((star1, star2) in vedhaPairs) {
            if ((brideNakshatra == star1 && groomNakshatra == star2) ||
                (brideNakshatra == star2 && groomNakshatra == star1)) {
                return Pair(true, "${brideNakshatra.displayName}-${groomNakshatra.displayName} form Vedha pair - may cause obstacles")
            }
        }

        return Pair(false, "No Vedha present")
    }

    private fun getRajju(nakshatra: Nakshatra): Rajju {
        return when (nakshatra) {
            Nakshatra.ASHWINI, Nakshatra.ASHLESHA, Nakshatra.MAGHA,
            Nakshatra.JYESHTHA, Nakshatra.MULA, Nakshatra.REVATI -> Rajju.PADA

            Nakshatra.BHARANI, Nakshatra.PUSHYA, Nakshatra.PURVA_PHALGUNI,
            Nakshatra.ANURADHA, Nakshatra.PURVA_ASHADHA, Nakshatra.UTTARA_BHADRAPADA -> Rajju.KATI

            Nakshatra.KRITTIKA, Nakshatra.PUNARVASU, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.VISHAKHA, Nakshatra.UTTARA_ASHADHA, Nakshatra.PURVA_BHADRAPADA -> Rajju.NABHI

            Nakshatra.ROHINI, Nakshatra.ARDRA, Nakshatra.HASTA,
            Nakshatra.SWATI, Nakshatra.SHATABHISHA, Nakshatra.SHRAVANA -> Rajju.KANTHA

            Nakshatra.MRIGASHIRA, Nakshatra.CHITRA, Nakshatra.DHANISHTHA -> Rajju.SIRO
        }
    }

    private fun getRajjuWarning(rajju: Rajju): String {
        return when (rajju) {
            Rajju.SIRO -> "Most serious - affects longevity of spouse"
            Rajju.KANTHA -> "May cause health issues to both"
            Rajju.NABHI -> "May affect children"
            Rajju.KATI -> "May cause financial difficulties"
            Rajju.PADA -> "May cause wandering tendencies"
        }
    }

    private fun calculateManglikDosha(chart: VedicChart, person: String): ManglikAnalysis {
        val mars = chart.planetPositions.find { it.planet == Planet.MARS }
            ?: return ManglikAnalysis(person, ManglikDosha.NONE, 0, emptyList(), emptyList(), ManglikDosha.NONE)

        val marsHouse = mars.house
        val factors = mutableListOf<String>()
        val cancellations = mutableListOf<String>()

        val manglikHouses = listOf(1, 2, 4, 7, 8, 12)
        val isManglik = marsHouse in manglikHouses

        if (!isManglik) {
            return ManglikAnalysis(person, ManglikDosha.NONE, marsHouse, factors, cancellations, ManglikDosha.NONE)
        }

        factors.add("Mars in ${getHouseOrdinal(marsHouse)} house from Lagna")

        val doshaLevel = when (marsHouse) {
            7, 8 -> ManglikDosha.FULL
            1, 4, 12 -> ManglikDosha.FULL
            2 -> ManglikDosha.PARTIAL
            else -> ManglikDosha.NONE
        }

        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

        // Mars in own sign (Aries, Scorpio) or exalted (Capricorn)
        if (mars.sign in listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO, ZodiacSign.CAPRICORN)) {
            cancellations.add("Mars in ${if (mars.sign == ZodiacSign.CAPRICORN) "exaltation" else "own sign"} (${mars.sign.displayName})")
        }

        // Mars in Leo or Aquarius in specific houses
        if (mars.sign in listOf(ZodiacSign.LEO, ZodiacSign.AQUARIUS) && marsHouse in listOf(1, 4, 7, 10)) {
            cancellations.add("Mars in ${mars.sign.displayName} in Kendra")
        }

        val jupiter = chart.planetPositions.find { it.planet == Planet.JUPITER }
        if (jupiter != null) {
            val jupiterAspects = getJupiterAspectedHouses(jupiter.house)
            if (marsHouse in jupiterAspects) {
                cancellations.add("Jupiter aspects Mars from ${getHouseOrdinal(jupiter.house)} house")
            }
        }

        // Mars aspected or conjunct by benefic Moon
        val moon = chart.planetPositions.find { it.planet == Planet.MOON }
        if (moon != null && moon.house == marsHouse) {
            cancellations.add("Moon conjunct Mars")
        }

        val venus = chart.planetPositions.find { it.planet == Planet.VENUS }
        if (venus != null && venus.house in listOf(1, 7)) {
            cancellations.add("Venus in ${getHouseOrdinal(venus.house)} house")
        }

        if (jupiter != null && jupiter.house in listOf(1, 4, 7, 10)) {
            cancellations.add("Jupiter in Kendra (${getHouseOrdinal(jupiter.house)} house)")
        }

        // Specific sign-house combinations that reduce severity
        if (marsHouse == 1 && ascendantSign in listOf(ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS)) {
            cancellations.add("Mars in fiery ascendant in 1st house")
        }
        if (marsHouse == 2 && mars.sign in listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO)) {
            cancellations.add("Mars in Mercury's sign in 2nd house")
        }
        if (marsHouse == 4 && mars.sign == ZodiacSign.ARIES) {
            cancellations.add("Mars in own sign in 4th house")
        }
        if (marsHouse == 7 && mars.sign in listOf(ZodiacSign.CANCER, ZodiacSign.CAPRICORN)) {
            cancellations.add("Mars debilitated/exalted in 7th house")
        }
        if (marsHouse == 8 && mars.sign in listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)) {
            cancellations.add("Mars in Jupiter's sign in 8th house")
        }
        if (marsHouse == 12 && mars.sign in listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA)) {
            cancellations.add("Mars in Venus's sign in 12th house")
        }

        val effectiveDosha = when {
            cancellations.size >= 3 -> ManglikDosha.NONE
            cancellations.size >= 2 -> if (doshaLevel == ManglikDosha.FULL) ManglikDosha.PARTIAL else ManglikDosha.NONE
            cancellations.size >= 1 -> if (doshaLevel == ManglikDosha.FULL) ManglikDosha.PARTIAL else doshaLevel
            else -> doshaLevel
        }

        // Double Manglik check
        val saturn = chart.planetPositions.find { it.planet == Planet.SATURN }
        val rahu = chart.planetPositions.find { it.planet == Planet.RAHU }
        val ketu = chart.planetPositions.find { it.planet == Planet.KETU }

        val hasDoubleManglik = when {
            saturn?.house == marsHouse -> {
                factors.add("Mars conjunct Saturn in ${getHouseOrdinal(marsHouse)} house")
                true
            }
            rahu?.house == marsHouse -> {
                factors.add("Mars conjunct Rahu in ${getHouseOrdinal(marsHouse)} house")
                true
            }
            ketu?.house == marsHouse -> {
                factors.add("Mars conjunct Ketu in ${getHouseOrdinal(marsHouse)} house")
                true
            }
            else -> false
        }

        val finalDosha = if (hasDoubleManglik && effectiveDosha != ManglikDosha.NONE) {
            ManglikDosha.DOUBLE
        } else {
            effectiveDosha
        }

        return ManglikAnalysis(person, doshaLevel, marsHouse, factors, cancellations, finalDosha)
    }

    private fun getJupiterAspectedHouses(jupiterHouse: Int): List<Int> {
        return listOf(
            jupiterHouse,
            ((jupiterHouse + 4 - 1) % 12) + 1,
            ((jupiterHouse + 6 - 1) % 12) + 1,
            ((jupiterHouse + 8 - 1) % 12) + 1
        )
    }

    private fun getHouseOrdinal(house: Int): String {
        return when (house) {
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            else -> "${house}th"
        }
    }

    private fun assessManglikCompatibility(bride: ManglikAnalysis, groom: ManglikAnalysis): String {
        val brideLevel = bride.effectiveDosha.severity
        val groomLevel = groom.effectiveDosha.severity

        return when {
            brideLevel == 0 && groomLevel == 0 -> "Both non-Manglik - No concerns"
            brideLevel > 0 && groomLevel > 0 -> "Both Manglik - Doshas cancel each other (Manglik to Manglik match is recommended)"
            abs(brideLevel - groomLevel) == 1 -> "Minor Manglik imbalance - Manageable with remedies"
            brideLevel > 0 && groomLevel == 0 -> "Bride is Manglik (${bride.effectiveDosha.displayName}) while Groom is not - Kumbh Vivah or other remedies advised"
            groomLevel > 0 && brideLevel == 0 -> "Groom is Manglik (${groom.effectiveDosha.displayName}) while Bride is not - Remedies strongly recommended"
            else -> "Significant Manglik imbalance - Careful consideration and remedies essential"
        }
    }

    private fun calculateSpecialConsiderations(
        brideChart: VedicChart,
        groomChart: VedicChart,
        gunaAnalyses: List<GunaAnalysis>,
        brideManglik: ManglikAnalysis,
        groomManglik: ManglikAnalysis,
        additionalFactors: AdditionalFactors
    ): List<String> {
        val considerations = mutableListOf<String>()

        val nadiAnalysis = gunaAnalyses.find { it.name == "Nadi" }
        if (nadiAnalysis != null && nadiAnalysis.obtainedPoints == 0.0) {
            considerations.add("⚠ CRITICAL - Nadi Dosha: May affect health and progeny. This is the most serious dosha. Consult a learned astrologer for specific remedies.")
        }

        val bhakootAnalysis = gunaAnalyses.find { it.name == "Bhakoot" }
        if (bhakootAnalysis != null && bhakootAnalysis.obtainedPoints == 0.0) {
            considerations.add("⚠ Bhakoot Dosha present - ${bhakootAnalysis.analysis}")
        }

        if (brideManglik.effectiveDosha.severity > 0 && groomManglik.effectiveDosha.severity == 0) {
            considerations.add("⚠ Manglik Mismatch: Bride has ${brideManglik.effectiveDosha.displayName} while Groom is non-Manglik. Kumbh Vivah or matching with Manglik partner recommended.")
        } else if (groomManglik.effectiveDosha.severity > 0 && brideManglik.effectiveDosha.severity == 0) {
            considerations.add("⚠ Manglik Mismatch: Groom has ${groomManglik.effectiveDosha.displayName} while Bride is non-Manglik. Special remedies recommended.")
        }

        val ganaAnalysis = gunaAnalyses.find { it.name == "Gana" }
        if (ganaAnalysis != null && ganaAnalysis.obtainedPoints == 0.0) {
            considerations.add("⚠ Gana Incompatibility (Deva-Rakshasa): May cause frequent temperamental clashes. Mutual understanding and patience essential.")
        }

        val yoniAnalysis = gunaAnalyses.find { it.name == "Yoni" }
        if (yoniAnalysis != null && yoniAnalysis.obtainedPoints == 0.0) {
            considerations.add("⚠ Yoni Incompatibility (Enemy Yonis): Physical and instinctual differences may affect intimacy.")
        }

        if (additionalFactors.vedhaPresent) {
            considerations.add("⚠ Vedha Dosha: ${additionalFactors.vedhaDetails}. May cause obstacles in married life.")
        }

        if (!additionalFactors.rajjuCompatible) {
            considerations.add("⚠ Rajju Dosha: ${additionalFactors.rajjuDetails}")
        }

        if (!additionalFactors.streeDeerghaSatisfied) {
            considerations.add("○ Stree Deergha not satisfied: Groom's star is only ${additionalFactors.streeDeerghaDiff} nakshatras ahead. Ideally should be 13 or more for prosperity.")
        }

        val lowScores = gunaAnalyses.count { !it.isPositive }
        if (lowScores >= 4) {
            considerations.add("⚠ Multiple factors (${lowScores}/8) below threshold - Overall harmony requires conscious effort from both partners.")
        }

        val brideAscendant = ZodiacSign.fromLongitude(brideChart.ascendant)
        val groomAscendant = ZodiacSign.fromLongitude(groomChart.ascendant)
        val bride7thLord = ZodiacSign.entries[(brideAscendant.number + 5) % 12].ruler
        val groom7thLord = ZodiacSign.entries[(groomAscendant.number + 5) % 12].ruler

        if (getPlanetaryFriendship(bride7thLord, groom7thLord) == "Enemy" &&
            getPlanetaryFriendship(groom7thLord, bride7thLord) == "Enemy") {
            considerations.add("○ 7th house lords (${bride7thLord.displayName} & ${groom7thLord.displayName}) are mutual enemies - Partnership matters need extra attention.")
        }

        if (considerations.isEmpty()) {
            considerations.add("✓ No major negative factors found. Compatibility is favorable for marriage.")
        }

        return considerations
    }

    private fun calculateRemedies(
        gunaAnalyses: List<GunaAnalysis>,
        brideManglik: ManglikAnalysis,
        groomManglik: ManglikAnalysis,
        totalPoints: Double,
        additionalFactors: AdditionalFactors
    ): List<String> {
        val remedies = mutableListOf<String>()

        val nadiAnalysis = gunaAnalyses.find { it.name == "Nadi" }
        if  (nadiAnalysis != null && nadiAnalysis.obtainedPoints == 0.0) {
            remedies.add("Nadi Dosha Remedy: Perform Nadi Dosha Nivaran Puja before marriage. Donate gold equal to bride's weight (or as per capacity) on an auspicious day.")
            remedies.add("Nadi Dosha Remedy: Recite Mahamrityunjaya Mantra 1,25,000 times. Both partners should perform this japa.")
            remedies.add("Nadi Dosha Remedy: Donate grains, clothes, and cow to Brahmins. Perform Rudrabhishek on Mondays.")
        }

        val bhakootAnalysis = gunaAnalyses.find { it.name == "Bhakoot" }
        if (bhakootAnalysis != null && bhakootAnalysis.obtainedPoints == 0.0) {
            remedies.add("Bhakoot Dosha Remedy: Worship the ruling deity of both Moon signs regularly.")
            remedies.add("Bhakoot Dosha Remedy: Perform Graha Shanti Puja for Moon and the respective sign lords.")
            if (bhakootAnalysis.analysis.contains("6-8")) {
                remedies.add("Shadashtak Dosha Remedy: Donate medicines, perform health-related charity. Keep Shri Yantra at home.")
            }
        }

        if (brideManglik.effectiveDosha.severity > 0 || groomManglik.effectiveDosha.severity > 0) {
            remedies.add("Manglik Dosha Remedy: Perform Kumbh Vivah (symbolic marriage with pot/peepal tree/banana tree) before actual marriage for the Manglik partner.")
            remedies.add("Manglik Dosha Remedy: Recite Hanuman Chalisa daily and visit Hanuman temple on Tuesdays and Saturdays.")
            remedies.add("Manglik Dosha Remedy: Perform Mangal Shanti Puja. Fast on Tuesdays and donate red lentils, red cloth, and jaggery.")
            
            if (brideManglik.effectiveDosha.severity > 0) {
                remedies.add("For Bride: Wear Red Coral (Moonga) on ring finger of right hand after proper energization, only after consulting an astrologer.")
            }
            if (groomManglik.effectiveDosha.severity > 0) {
                remedies.add("For Groom: Recite Sunderkand on Tuesdays. Offer sindoor to Lord Hanuman.")
            }
            
            if (brideManglik.effectiveDosha == ManglikDosha.DOUBLE || groomManglik.effectiveDosha == ManglikDosha.DOUBLE) {
                remedies.add("Double Manglik Remedy: Perform Maha Mrityunjaya Homa. This is essential before marriage.")
            }
        }

        val ganaAnalysis = gunaAnalyses.find { it.name == "Gana" }
        if (ganaAnalysis != null && ganaAnalysis.obtainedPoints <= 1.0) {
            remedies.add("Gana Dosha Remedy: Perform Ganapati Atharvashirsha Puja together before marriage.")
            remedies.add("Gana Dosha Remedy: Both partners should recite Ganesha Mantra 'Om Gam Ganapataye Namaha' 108 times daily.")
            remedies.add("Gana Dosha Remedy: Keep a Ganesha idol at the entrance of home after marriage.")
        }

        val grahaMaitri = gunaAnalyses.find { it.name == "Graha Maitri" }
        if (grahaMaitri != null && grahaMaitri.obtainedPoints <= 1.0) {
            remedies.add("Graha Maitri Remedy: Strengthen both Moon sign lords through their respective mantras and gemstones (after consultation).")
            remedies.add("Graha Maitri Remedy: Perform Navgraha Puja to harmonize planetary energies.")
        }

        val yoniAnalysis = gunaAnalyses.find { it.name == "Yoni" }
        if (yoniAnalysis != null && yoniAnalysis.obtainedPoints == 0.0) {
            remedies.add("Yoni Dosha Remedy: Perform Kamadeva Puja for marital harmony.")
            remedies.add("Yoni Dosha Remedy: Keep a pair of love birds or fish at home after marriage.")
        }

        if (additionalFactors.vedhaPresent) {
            remedies.add("Vedha Dosha Remedy: Perform Nakshatra Shanti Puja for both birth stars before marriage.")
            remedies.add("Vedha Dosha Remedy: Donate items related to the Nakshatra lords on respective days.")
        }

        if (!additionalFactors.rajjuCompatible) {
            val rajjuRemedy = when (additionalFactors.brideRajju) {
                Rajju.SIRO -> "Rajju Dosha (Siro) Remedy: This is most serious. Perform Ayushya Homa for longevity. Worship Lord Shiva together."
                Rajju.KANTHA -> "Rajju Dosha (Kantha) Remedy: Perform Maha Mrityunjaya Japa. Donate medicines and support health causes."
                Rajju.NABHI -> "Rajju Dosha (Nabhi) Remedy: Worship Santana Gopala for progeny. Perform Pumsavana rituals."
                Rajju.KATI -> "Rajju Dosha (Kati) Remedy: Perform Lakshmi Puja for financial stability. Donate to educational causes."
                Rajju.PADA -> "Rajju Dosha (Pada) Remedy: Worship Bhairava together. Avoid frequent travel after marriage initially."
            }
            remedies.add(rajjuRemedy)
        }

        if (totalPoints < AVERAGE_THRESHOLD) {
            remedies.add("General Remedy: Perform Navgraha Shanti Homa before marriage to neutralize planetary afflictions.")
            remedies.add("General Remedy: Both partners should observe fast on their respective Nakshatra days monthly.")
            remedies.add("General Remedy: Visit Tirupati or any major Vishnu temple together before marriage.")
            remedies.add("General Remedy: Practice meditation together regularly after marriage for mental harmony.")
        }

        if (totalPoints < POOR_THRESHOLD) {
            remedies.add("Important: With a score below 14, serious consideration and multiple remedies are essential. Consult an experienced astrologer for personalized guidance.")
            remedies.add("Spiritual Remedy: Both partners should take Sankalpa (spiritual vow) for mutual respect and understanding.")
        }

        if (remedies.isEmpty()) {
            remedies.add("No specific remedies required - compatibility is favorable for a harmonious marriage.")
            remedies.add("Recommendation: Perform Satyanarayan Puja after marriage for continued blessings.")
        }

        return remedies
    }

    private fun generateSummary(
        totalPoints: Double,
        rating: CompatibilityRating,
        gunaAnalyses: List<GunaAnalysis>,
        brideManglik: ManglikAnalysis,
        groomManglik: ManglikAnalysis,
        additionalFactors: AdditionalFactors
    ): String {
        val strongPoints = gunaAnalyses.filter { it.isPositive && it.obtainedPoints >= it.maxPoints * 0.7 }
        val weakPoints = gunaAnalyses.filter { !it.isPositive || it.obtainedPoints < it.maxPoints * 0.5 }
        val percentageScore = (totalPoints / MAX_TOTAL) * 100.0

        return buildString {
            appendLine("╔══════════════════════════════════════════════════════════╗")
            appendLine("║                    COMPATIBILITY SUMMARY                  ║")
            appendLine("╚══════════════════════════════════════════════════════════╝")
            appendLine()
            appendLine("Overall Score: ${String.format("%.1f", totalPoints)} out of 36 points (${String.format("%.1f", percentageScore)}%)")
            appendLine("Rating: ${rating.displayName}")
            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")
            
            if (strongPoints.isNotEmpty()) {
                appendLine()
                appendLine("✓ STRENGTHS (${strongPoints.size} factors):")
                strongPoints.forEach { guna ->
                    appendLine("  • ${guna.name} (${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}): ${getStrengthSummary(guna.name)}")
                }
            }

            if (weakPoints.isNotEmpty()) {
                appendLine()
                appendLine("⚠ AREAS NEEDING ATTENTION (${weakPoints.size} factors):")
                weakPoints.forEach { guna ->
                    appendLine("  • ${guna.name} (${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}): ${getConcernSummary(guna.name)}")
                }
            }

            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")
            appendLine()
            appendLine("MANGLIK STATUS:")
            appendLine("  • Bride: ${brideManglik.effectiveDosha.displayName}")
            appendLine("  • Groom: ${groomManglik.effectiveDosha.displayName}")
            
            val manglikOk = (brideManglik.effectiveDosha.severity == 0 && groomManglik.effectiveDosha.severity == 0) ||
                           (brideManglik.effectiveDosha.severity > 0 && groomManglik.effectiveDosha.severity > 0)
            appendLine("  • Status: ${if (manglikOk) "✓ Compatible" else "⚠ Needs Attention"}")

            appendLine()
            appendLine("ADDITIONAL FACTORS:")
            appendLine("  • Vedha: ${if (additionalFactors.vedhaPresent) "⚠ Present" else "✓ Not present"}")
            appendLine("  • Rajju: ${if (additionalFactors.rajjuCompatible) "✓ Compatible" else "⚠ Same Rajju"}")
            appendLine("  • Stree Deergha: ${if (additionalFactors.streeDeerghaSatisfied) "✓ Satisfied" else "○ Not satisfied"}")
            appendLine("  • Mahendra: ${if (additionalFactors.mahendraSatisfied) "✓ Favorable" else "○ Not applicable"}")

            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")
            appendLine()
            appendLine("FINAL RECOMMENDATION:")
            appendLine()
            
            when (rating) {
                CompatibilityRating.EXCELLENT -> {
                    appendLine("This is an EXCELLENT match. The horoscopes show strong compatibility")
                    appendLine("across most factors. Marriage is highly recommended. The couple can")
                    appendLine("expect a harmonious and prosperous married life with mutual understanding.")
                }
                CompatibilityRating.GOOD -> {
                    appendLine("This is a GOOD match. While there may be minor areas of difference,")
                    appendLine("the overall compatibility is favorable. Marriage is recommended.")
                    appendLine("Small adjustments and mutual respect will ensure a happy married life.")
                }
                CompatibilityRating.AVERAGE -> {
                    appendLine("This is an AVERAGE match. There are some compatibility concerns that")
                    appendLine("can be addressed with remedies and conscious effort. Marriage can")
                    appendLine("proceed with appropriate remedial measures and clear communication.")
                }
                CompatibilityRating.BELOW_AVERAGE -> {
                    appendLine("This match is BELOW AVERAGE. There are notable compatibility challenges")
                    appendLine("that need to be carefully considered. If proceeding, strong commitment")
                    appendLine("to remedies and mutual understanding is essential. Consult an astrologer.")
                }
                CompatibilityRating.POOR -> {
                    appendLine("This is considered a POOR match astrologically. Multiple significant")
                    appendLine("incompatibilities exist. If the families wish to proceed, extensive")
                    appendLine("remedies are mandatory and professional astrological guidance is essential.")
                }
            }
            
            appendLine()
            appendLine("─────────────────────────────────────────────────────────────")
            appendLine("Note: Vedic astrology provides guidance based on birth charts.")
            appendLine("Modern relationships also depend on mutual respect, love, and commitment.")
            appendLine("Use this analysis as a guide, not an absolute determinant.")
        }
    }

    private fun getStrengthSummary(gunaName: String): String {
        return when (gunaName) {
            "Varna" -> "Good spiritual and ego compatibility"
            "Vashya" -> "Strong mutual attraction and influence"
            "Tara" -> "Favorable destiny and luck together"
            "Yoni" -> "Good physical compatibility"
            "Graha Maitri" -> "Mental harmony and understanding"
            "Gana" -> "Compatible temperaments"
            "Bhakoot" -> "Favorable for love, health, and finances"
            "Nadi" -> "Excellent for health and progeny"
            else -> "Positive factor"
        }
    }

    private fun getConcernSummary(gunaName: String): String {
        return when (gunaName) {
            "Varna" -> "May have ego-related differences"
            "Vashya" -> "Power dynamics may need attention"
            "Tara" -> "May face some obstacles together"
            "Yoni" -> "Physical compatibility needs attention"
            "Graha Maitri" -> "Mental wavelength differences possible"
            "Gana" -> "Temperamental adjustments needed"
            "Bhakoot" -> "May affect love/health/finances"
            "Nadi" -> "Serious - affects health and children"
            else -> "Needs attention"
        }
    }

    private fun generateDetailedAnalysis(
        brideChart: VedicChart,
        groomChart: VedicChart,
        gunaAnalyses: List<GunaAnalysis>,
        additionalFactors: AdditionalFactors
    ): String {
        val brideMoon = brideChart.planetPositions.find { it.planet == Planet.MOON }
        val groomMoon = groomChart.planetPositions.find { it.planet == Planet.MOON }

        return buildString {
            appendLine("╔══════════════════════════════════════════════════════════════════╗")
            appendLine("║                    DETAILED KOOTA ANALYSIS                        ║")
            appendLine("╚══════════════════════════════════════════════════════════════════╝")
            appendLine()
            
            appendLine("BIRTH DATA SUMMARY")
            appendLine("─────────────────────────────────────────────────────────────────────")
            appendLine()
            appendLine("BRIDE: ${brideChart.birthData.name}")
            if (brideMoon != null) {
                val (brideNakshatra, bridePada) = Nakshatra.fromLongitude(brideMoon.longitude)
                appendLine("  Moon Sign: ${brideMoon.sign.displayName}")
                appendLine("  Nakshatra: ${brideNakshatra.displayName} (Pada $bridePada)")
                appendLine("  Moon Longitude: ${String.format("%.2f", brideMoon.longitude)}°")
            }
            appendLine()
            appendLine("GROOM: ${groomChart.birthData.name}")
            if (groomMoon != null) {
                val (groomNakshatra, groomPada) = Nakshatra.fromLongitude(groomMoon.longitude)
                appendLine("  Moon Sign: ${groomMoon.sign.displayName}")
                appendLine("  Nakshatra: ${groomNakshatra.displayName} (Pada $groomPada)")
                appendLine("  Moon Longitude: ${String.format("%.2f", groomMoon.longitude)}°")
            }
            
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════════════")
            appendLine()

            gunaAnalyses.forEach { guna ->
                val scoreBar = buildScoreBar(guna.obtainedPoints, guna.maxPoints)
                val status = if (guna.isPositive) "✓ FAVORABLE" else "⚠ ATTENTION"
                
                appendLine("┌─────────────────────────────────────────────────────────────────┐")
                appendLine("│ ${guna.name.uppercase().padEnd(20)} ${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()} points    $status")
                appendLine("├─────────────────────────────────────────────────────────────────┤")
                appendLine("│ $scoreBar")
                appendLine("│")
                appendLine("│ Purpose: ${guna.description}")
                appendLine("│")
                appendLine("│ Bride: ${guna.brideValue}")
                appendLine("│ Groom: ${guna.groomValue}")
                appendLine("│")
                appendLine("│ Analysis:")
                wrapText(guna.analysis, 65).forEach { line ->
                    appendLine("│   $line")
                }
                appendLine("└─────────────────────────────────────────────────────────────────┘")
                appendLine()
            }

            appendLine("═══════════════════════════════════════════════════════════════════")
            appendLine()
            appendLine("ADDITIONAL COMPATIBILITY FACTORS")
            appendLine("─────────────────────────────────────────────────────────────────────")
            appendLine()
            
            appendLine("VEDHA (Obstruction)")
            appendLine("  Status: ${if (additionalFactors.vedhaPresent) "⚠ PRESENT" else "✓ NOT PRESENT"}")
            appendLine("  Details: ${additionalFactors.vedhaDetails}")
            appendLine("  Significance: Vedha indicates mutual obstruction between certain")
            appendLine("                Nakshatra pairs that may cause difficulties.")
            appendLine()
            
            appendLine("RAJJU (Cosmic Bond)")
            appendLine("  Bride's Rajju: ${additionalFactors.brideRajju.displayName} (${additionalFactors.brideRajju.bodyPart})")
            appendLine("  Groom's Rajju: ${additionalFactors.groomRajju.displayName} (${additionalFactors.groomRajju.bodyPart})")
            appendLine("  Status: ${if (additionalFactors.rajjuCompatible) "✓ COMPATIBLE" else "⚠ SAME RAJJU"}")
            if (!additionalFactors.rajjuCompatible) {
                appendLine("  Warning: ${additionalFactors.rajjuDetails}")
            }
            appendLine()
            
            appendLine("STREE DEERGHA (Wife's Prosperity)")
            appendLine("  Nakshatra Difference: ${additionalFactors.streeDeerghaDiff}")
            appendLine("  Status: ${if (additionalFactors.streeDeerghaSatisfied) "✓ SATISFIED (≥13 or same)" else "○ NOT SATISFIED (<13)"}")
            appendLine("  Significance: Groom's Nakshatra should be at least 13 positions")
            appendLine("                ahead of Bride's for prosperity and happiness.")
            appendLine()
            
            appendLine("MAHENDRA (Longevity & Prosperity)")
            appendLine("  Status: ${if (additionalFactors.mahendraSatisfied) "✓ FAVORABLE" else "○ NOT IN POSITION"}")
            appendLine("  Details: ${additionalFactors.mahendraDetails}")
            appendLine("  Significance: Favorable Mahendra indicates long life, prosperity,")
            appendLine("                and progeny in the married life.")
            appendLine()
            
            appendLine("═══════════════════════════════════════════════════════════════════")
        }
    }

    private fun buildScoreBar(obtained: Double, max: Double): String {
        val percentage = (obtained / max * 100).toInt()
        val filledBlocks = (percentage / 10)
        val emptyBlocks = 10 - filledBlocks
        
        val bar = "█".repeat(filledBlocks) + "░".repeat(emptyBlocks)
        return "[$bar] ${String.format("%3d", percentage)}%"
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        for (word in words) {
            if (currentLine.length + word.length + 1 <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                currentLine.append(word)
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        
        return lines
    }

    fun getGunaDescription(gunaName: String): String {
        return when (gunaName) {
            "Varna" -> """
                Varna represents the spiritual/ego compatibility between partners. It classifies 
                signs into four categories: Brahmin (spiritual), Kshatriya (warrior), Vaishya 
                (merchant), and Shudra (service). For harmony, the groom's Varna should be 
                equal to or higher than the bride's.
            """.trimIndent()
            
            "Vashya" -> """
                Vashya indicates the degree of magnetic control or attraction between partners. 
                It determines who will be more influential in the relationship. Signs are 
                classified as Human, Quadruped, Wild, Aquatic, or Insect. Mutual Vashya 
                ensures balanced power dynamics.
            """.trimIndent()
            
            "Tara" -> """
                Tara (or Dina) determines the birth star compatibility. It is calculated by 
                counting from one partner's Nakshatra to the other's and finding the Tara 
                (1-9). Auspicious Taras are Sampat, Kshema, Sadhana, Mitra, and Parama Mitra. 
                It indicates the destiny the couple will share.
            """.trimIndent()
            
            "Yoni" -> """
                Yoni represents the sexual and physical compatibility between partners. Each 
                Nakshatra is assigned an animal nature (14 types). Same Yoni is best, while 
                enemy Yonis (like Cat-Rat, Cow-Tiger) indicate physical incompatibility. This 
                affects intimacy and instinctual understanding.
            """.trimIndent()
            
            "Graha Maitri" -> """
                Graha Maitri (Rasyadhipati) checks the friendship between the Moon sign lords. 
                Friendly lords indicate mental compatibility and similar thought processes. 
                Enemy lords may cause frequent misunderstandings and different approaches to life.
            """.trimIndent()
            
            "Gana" -> """
                Gana represents the temperament and nature of individuals. The three Ganas are:
                - Deva (Divine): Sattvik, gentle, spiritual
                - Manushya (Human): Rajasik, balanced, worldly
                - Rakshasa (Demon): Tamasik, aggressive, dominant
                Same Gana is best. Deva-Rakshasa combination is most challenging.
            """.trimIndent()
            
            "Bhakoot" -> """
                Bhakoot examines the position of Moon signs relative to each other. Certain 
                positions create doshas:
                - 2-12: Financial difficulties (Dhan-Vyaya)
                - 6-8: Health issues and separation (Shadashtak)
                - 5-9: Actually favorable (Trine)
                Same sign lords can cancel the dosha.
            """.trimIndent()
            
            "Nadi" -> """
                Nadi is the most important factor, carrying 8 points. It relates to the 
                physiological and genetic compatibility. The three Nadis are:
                - Adi (Vata): Beginning energy, air element
                - Madhya (Pitta): Middle energy, fire element
                - Antya (Kapha): End energy, water element
                Same Nadi can cause health issues and affect progeny. This dosha is most serious.
            """.trimIndent()
            
            else -> "Detailed description not available."
        }
    }

    fun getMinimumRecommendedScore(): Double = AVERAGE_THRESHOLD

    fun getMaximumPossibleScore(): Double = MAX_TOTAL

    fun interpretScore(score: Double): String {
        return when {
            score >= EXCELLENT_THRESHOLD -> "Excellent - Highly recommended for marriage"
            score >= GOOD_THRESHOLD -> "Good - Recommended with minor considerations"
            score >= AVERAGE_THRESHOLD -> "Average - Acceptable with remedies"
            score >= POOR_THRESHOLD -> "Below Average - Proceed with caution"
            else -> "Poor - Significant challenges expected"
        }
    }
}