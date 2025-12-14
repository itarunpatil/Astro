package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKeyMatch
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.*
import kotlin.math.abs

/**
 * Guna Milan (Ashtakoota) Calculator
 *
 * Calculates the 8 Gunas for Vedic matchmaking based on:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Muhurta Chintamani
 * - Jataka Parijata
 *
 * The 8 Gunas (Kootas) are:
 * 1. Varna (1 point) - Spiritual compatibility
 * 2. Vashya (2 points) - Mutual attraction
 * 3. Tara (3 points) - Destiny compatibility
 * 4. Yoni (4 points) - Physical compatibility
 * 5. Graha Maitri (5 points) - Mental compatibility
 * 6. Gana (6 points) - Temperament
 * 7. Bhakoot (7 points) - Love, health, finances
 * 8. Nadi (8 points) - Health and progeny
 *
 * Total: 36 points maximum
 */
object GunaMilanCalculator {

    /**
     * Calculate all 8 Guna analyses
     */
    fun calculateAllGunas(
        brideMoonSign: ZodiacSign,
        groomMoonSign: ZodiacSign,
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        bridePada: Int,
        groomPada: Int,
        language: Language = Language.ENGLISH
    ): List<GunaAnalysis> {
        return listOf(
            calculateVarna(brideMoonSign, groomMoonSign, language),
            calculateVashya(brideMoonSign, groomMoonSign, language),
            calculateTara(brideNakshatra, groomNakshatra, language),
            calculateYoni(brideNakshatra, groomNakshatra, language),
            calculateGrahaMaitri(brideMoonSign, groomMoonSign, language),
            calculateGana(brideNakshatra, groomNakshatra, language),
            calculateBhakoot(brideMoonSign, groomMoonSign, language),
            calculateNadi(brideNakshatra, groomNakshatra, brideMoonSign, groomMoonSign, bridePada, groomPada, language)
        )
    }

    // ============================================
    // VARNA KOOTA (1 Point)
    // ============================================

    fun calculateVarna(
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideVarna = Varna.fromZodiacSign(brideSign)
        val groomVarna = Varna.fromZodiacSign(groomSign)

        val points = if (groomVarna.value >= brideVarna.value) MatchmakingConstants.MAX_VARNA else 0.0

        val analysis = if (points > 0) {
            StringResources.get(StringKeyMatch.VARNA_COMPATIBLE, language)
                .replace("{groom}", groomVarna.getLocalizedName(language))
                .replace("{bride}", brideVarna.getLocalizedName(language))
        } else {
            StringResources.get(StringKeyMatch.VARNA_INCOMPATIBLE, language)
                .replace("{bride}", brideVarna.getLocalizedName(language))
                .replace("{groom}", groomVarna.getLocalizedName(language))
        }

        return GunaAnalysis(
            gunaType = GunaType.VARNA,
            name = "Varna",
            maxPoints = MatchmakingConstants.MAX_VARNA,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.VARNA_DESC, language),
            brideValue = brideVarna.getLocalizedName(language),
            groomValue = groomVarna.getLocalizedName(language),
            analysis = analysis,
            isPositive = points > 0
        )
    }

    // ============================================
    // VASHYA KOOTA (2 Points)
    // ============================================

    fun calculateVashya(
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideVashya = Vashya.fromZodiacSign(brideSign)
        val groomVashya = Vashya.fromZodiacSign(groomSign)

        val points = calculateVashyaPoints(brideVashya, groomVashya, brideSign, groomSign)

        val analysisKey = when {
            points >= 2.0 -> StringKeyMatch.VASHYA_EXCELLENT
            points >= 1.5 -> StringKeyMatch.VASHYA_VERY_GOOD
            points >= 1.0 -> StringKeyMatch.VASHYA_GOOD
            points >= 0.5 -> StringKeyMatch.VASHYA_PARTIAL
            else -> StringKeyMatch.VASHYA_INCOMPATIBLE
        }

        return GunaAnalysis(
            gunaType = GunaType.VASHYA,
            name = "Vashya",
            maxPoints = MatchmakingConstants.MAX_VASHYA,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.VASHYA_DESC, language),
            brideValue = "${brideVashya.getLocalizedName(language)} (${brideSign.getLocalizedName(language)})",
            groomValue = "${groomVashya.getLocalizedName(language)} (${groomSign.getLocalizedName(language)})",
            analysis = StringResources.get(analysisKey, language),
            isPositive = points >= 1.0
        )
    }

    private fun calculateVashyaPoints(
        brideVashya: Vashya,
        groomVashya: Vashya,
        brideSign: ZodiacSign,
        groomSign: ZodiacSign
    ): Double {
        if (brideSign == groomSign) return 2.0
        if (brideVashya == groomVashya) return 2.0

        val groomControlsBride = Vashya.controlPairs[groomVashya]?.contains(brideVashya) == true
        val brideControlsGroom = Vashya.controlPairs[brideVashya]?.contains(groomVashya) == true

        return when {
            groomControlsBride && brideControlsGroom -> 2.0
            groomControlsBride || brideControlsGroom -> 1.0
            Vashya.enemyPairs.any { it.contains(brideVashya) && it.contains(groomVashya) } -> 0.0
            else -> 0.5
        }
    }

    // ============================================
    // TARA KOOTA (3 Points)
    // ============================================

    fun calculateTara(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideToGroom = calculateTaraNumber(brideNakshatra, groomNakshatra)
        val groomToBride = calculateTaraNumber(groomNakshatra, brideNakshatra)

        val brideTara = getTaraName(brideToGroom, language)
        val groomTara = getTaraName(groomToBride, language)

        val brideAuspicious = isAuspiciousTara(brideToGroom)
        val groomAuspicious = isAuspiciousTara(groomToBride)

        val points = when {
            brideAuspicious && groomAuspicious -> 3.0
            brideAuspicious || groomAuspicious -> 1.5
            else -> 0.0
        }

        val analysisKey = when {
            points >= 3.0 -> StringKeyMatch.TARA_EXCELLENT
            points >= 1.5 -> StringKeyMatch.TARA_MODERATE
            else -> StringKeyMatch.TARA_INAUSPICIOUS
        }

        return GunaAnalysis(
            gunaType = GunaType.TARA,
            name = "Tara",
            maxPoints = MatchmakingConstants.MAX_TARA,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.TARA_DESC, language),
            brideValue = "${brideNakshatra.getLocalizedName(language)} → $brideTara",
            groomValue = "${groomNakshatra.getLocalizedName(language)} → $groomTara",
            analysis = StringResources.get(analysisKey, language),
            isPositive = points >= 1.5
        )
    }

    private fun calculateTaraNumber(fromNakshatra: Nakshatra, toNakshatra: Nakshatra): Int {
        val diff = (toNakshatra.number - fromNakshatra.number + 27) % 27
        return if (diff == 0) 9 else ((diff - 1) % 9) + 1
    }

    private fun getTaraName(taraNumber: Int, language: Language): String = when (taraNumber) {
        1 -> StringResources.get(StringKeyMatch.TARA_JANMA, language)
        2 -> StringResources.get(StringKeyMatch.TARA_SAMPAT, language)
        3 -> StringResources.get(StringKeyMatch.TARA_VIPAT, language)
        4 -> StringResources.get(StringKeyMatch.TARA_KSHEMA, language)
        5 -> StringResources.get(StringKeyMatch.TARA_PRATYARI, language)
        6 -> StringResources.get(StringKeyMatch.TARA_SADHANA, language)
        7 -> StringResources.get(StringKeyMatch.TARA_VADHA, language)
        8 -> StringResources.get(StringKeyMatch.TARA_MITRA, language)
        9 -> StringResources.get(StringKeyMatch.TARA_PARAMA_MITRA, language)
        else -> StringResources.get(StringKeyMatch.TARA_JANMA, language)
    }

    private fun isAuspiciousTara(taraNumber: Int): Boolean = taraNumber in listOf(2, 4, 6, 8, 9)

    // ============================================
    // YONI KOOTA (4 Points)
    // ============================================

    fun calculateYoni(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideYoni = Yoni.fromNakshatra(brideNakshatra)
        val groomYoni = Yoni.fromNakshatra(groomNakshatra)

        val points = calculateYoniPoints(brideYoni, groomYoni)

        val analysisKey = when {
            points >= 4.0 -> StringKeyMatch.YONI_SAME
            points >= 3.0 -> StringKeyMatch.YONI_FRIENDLY
            points >= 2.0 -> StringKeyMatch.YONI_NEUTRAL
            points >= 1.0 -> StringKeyMatch.YONI_UNFRIENDLY
            else -> StringKeyMatch.YONI_ENEMY
        }

        return GunaAnalysis(
            gunaType = GunaType.YONI,
            name = "Yoni",
            maxPoints = MatchmakingConstants.MAX_YONI,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.YONI_DESC, language),
            brideValue = "${brideYoni.animal} (${brideYoni.gender})",
            groomValue = "${groomYoni.animal} (${groomYoni.gender})",
            analysis = StringResources.get(analysisKey, language),
            isPositive = points >= 2.0
        )
    }

    private fun calculateYoniPoints(brideYoni: Yoni, groomYoni: Yoni): Double {
        if (brideYoni.groupId == groomYoni.groupId) return 4.0

        if (Yoni.enemyPairs.any { it.contains(brideYoni.groupId) && it.contains(groomYoni.groupId) }) {
            return 0.0
        }

        for (group in Yoni.friendlyGroups) {
            if (group.contains(brideYoni.groupId) && group.contains(groomYoni.groupId)) {
                return 3.0
            }
        }

        return 2.0
    }

    // ============================================
    // GRAHA MAITRI KOOTA (5 Points)
    // ============================================

    fun calculateGrahaMaitri(
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideLord = brideSign.ruler
        val groomLord = groomSign.ruler

        val points = calculateGrahaMaitriPoints(brideLord, groomLord)

        val analysisKey = when {
            points >= 5.0 -> StringKeyMatch.GRAHA_MAITRI_EXCELLENT
            points >= 4.0 -> StringKeyMatch.GRAHA_MAITRI_VERY_GOOD
            points >= 2.5 -> StringKeyMatch.GRAHA_MAITRI_AVERAGE
            points >= 1.0 -> StringKeyMatch.GRAHA_MAITRI_FRICTION
            else -> StringKeyMatch.GRAHA_MAITRI_INCOMPATIBLE
        }

        return GunaAnalysis(
            gunaType = GunaType.GRAHA_MAITRI,
            name = "Graha Maitri",
            maxPoints = MatchmakingConstants.MAX_GRAHA_MAITRI,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.GRAHA_MAITRI_DESC, language),
            brideValue = "${brideSign.getLocalizedName(language)} (${brideLord.getLocalizedName(language)})",
            groomValue = "${groomSign.getLocalizedName(language)} (${groomLord.getLocalizedName(language)})",
            analysis = StringResources.get(analysisKey, language),
            isPositive = points >= 2.5
        )
    }

    private fun calculateGrahaMaitriPoints(lord1: Planet, lord2: Planet): Double {
        if (lord1 == lord2) return 5.0

        val relationship1 = PlanetaryFriendship.getRelationship(lord1, lord2)
        val relationship2 = PlanetaryFriendship.getRelationship(lord2, lord1)

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

    // ============================================
    // GANA KOOTA (6 Points)
    // ============================================

    fun calculateGana(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideGana = Gana.fromNakshatra(brideNakshatra)
        val groomGana = Gana.fromNakshatra(groomNakshatra)

        val points = calculateGanaPoints(brideGana, groomGana)

        val analysisKey = when {
            points >= 6.0 -> StringKeyMatch.GANA_SAME
            points >= 5.0 -> StringKeyMatch.GANA_COMPATIBLE
            points >= 3.0 -> StringKeyMatch.GANA_PARTIAL
            points >= 1.0 -> StringKeyMatch.GANA_DIFFERENT
            else -> StringKeyMatch.GANA_OPPOSITE
        }

        return GunaAnalysis(
            gunaType = GunaType.GANA,
            name = "Gana",
            maxPoints = MatchmakingConstants.MAX_GANA,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.GANA_DESC, language),
            brideValue = "${brideGana.getLocalizedName(language)} (${brideGana.getLocalizedDescription(language)})",
            groomValue = "${groomGana.getLocalizedName(language)} (${groomGana.getLocalizedDescription(language)})",
            analysis = StringResources.get(analysisKey, language),
            isPositive = points >= 3.0
        )
    }

    private fun calculateGanaPoints(brideGana: Gana, groomGana: Gana): Double = when {
        brideGana == groomGana -> 6.0
        brideGana == Gana.DEVA && groomGana == Gana.MANUSHYA -> 5.0
        brideGana == Gana.MANUSHYA && groomGana == Gana.DEVA -> 6.0
        brideGana == Gana.MANUSHYA && groomGana == Gana.RAKSHASA -> 1.0
        brideGana == Gana.RAKSHASA && groomGana == Gana.MANUSHYA -> 3.0
        brideGana == Gana.RAKSHASA && groomGana == Gana.RAKSHASA -> 6.0
        else -> 0.0  // Deva-Rakshasa combination
    }

    // ============================================
    // BHAKOOT KOOTA (7 Points)
    // ============================================

    fun calculateBhakoot(
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideNumber = brideSign.number
        val groomNumber = groomSign.number

        val (points, doshaType, doshaDescription) = calculateBhakootPoints(
            brideNumber, groomNumber, brideSign, groomSign, language
        )

        val analysis = when (doshaType) {
            "None" -> StringResources.get(StringKeyMatch.BHAKOOT_NO_DOSHA, language)
            "Cancelled" -> "${StringResources.get(StringKeyMatch.BHAKOOT_CANCELLED, language)} - $doshaDescription"
            "2-12" -> "${StringResources.get(StringKeyMatch.BHAKOOT_2_12, language)} $doshaDescription"
            "6-8" -> "${StringResources.get(StringKeyMatch.BHAKOOT_6_8, language)} $doshaDescription"
            "5-9" -> "${StringResources.get(StringKeyMatch.BHAKOOT_5_9, language)} $doshaDescription"
            else -> doshaDescription
        }

        return GunaAnalysis(
            gunaType = GunaType.BHAKOOT,
            name = "Bhakoot",
            maxPoints = MatchmakingConstants.MAX_BHAKOOT,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.BHAKOOT_DESC, language),
            brideValue = brideSign.getLocalizedName(language),
            groomValue = groomSign.getLocalizedName(language),
            analysis = analysis,
            isPositive = points >= 7.0
        )
    }

    private fun calculateBhakootPoints(
        brideNumber: Int,
        groomNumber: Int,
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        language: Language
    ): Triple<Double, String, String> {
        val diff = ((groomNumber - brideNumber + 12) % 12)

        val brideLord = brideSign.ruler
        val groomLord = groomSign.ruler
        val sameLord = brideLord == groomLord

        val is2_12 = (diff == 1 || diff == 11)
        val is6_8 = (diff == 5 || diff == 7)

        if (is2_12 || is6_8) {
            val cancellation = checkBhakootDoshaCancellation(brideSign, groomSign, brideLord, groomLord, is6_8, language)

            return if (cancellation != null) {
                Triple(7.0, "Cancelled", cancellation)
            } else {
                val description = if (is2_12) {
                    StringResources.get(StringKeyMatch.BHAKOOT_2_12_DESC, language)
                } else {
                    StringResources.get(StringKeyMatch.BHAKOOT_6_8_DESC, language)
                }
                Triple(0.0, if (is2_12) "2-12" else "6-8", description)
            }
        }

        // 5-9 pattern (Trine - generally beneficial)
        if (diff == 4 || diff == 8) {
            return Triple(7.0, "5-9", StringResources.get(StringKeyMatch.BHAKOOT_5_9_DESC, language))
        }

        return Triple(7.0, "None", StringResources.get(StringKeyMatch.BHAKOOT_FAVORABLE, language))
    }

    private fun checkBhakootDoshaCancellation(
        brideSign: ZodiacSign,
        groomSign: ZodiacSign,
        brideLord: Planet,
        groomLord: Planet,
        isShadashtak: Boolean,
        language: Language
    ): String? {
        // Same lord cancels dosha
        if (brideLord == groomLord) {
            return StringResources.get(StringKeyMatch.BHAKOOT_CANCEL_SAME_LORD, language)
                .replace("{lord}", brideLord.getLocalizedName(language))
        }

        // Mutual friends cancel dosha
        val rel1 = PlanetaryFriendship.getRelationship(brideLord, groomLord)
        val rel2 = PlanetaryFriendship.getRelationship(groomLord, brideLord)
        if (rel1 == "Friend" && rel2 == "Friend") {
            return StringResources.get(StringKeyMatch.BHAKOOT_CANCEL_MUTUAL_FRIENDS, language)
                .replace("{lord1}", brideLord.getLocalizedName(language))
                .replace("{lord2}", groomLord.getLocalizedName(language))
        }

        // Exaltation cancellation
        val brideLordExaltSign = getExaltationSign(brideLord)
        val groomLordExaltSign = getExaltationSign(groomLord)
        if (brideLordExaltSign == groomSign || groomLordExaltSign == brideSign) {
            return StringResources.get(StringKeyMatch.BHAKOOT_CANCEL_EXALTATION, language)
        }

        // Friendly disposition (partial)
        if ((rel1 == "Friend" && rel2 == "Neutral") || (rel1 == "Neutral" && rel2 == "Friend")) {
            return StringResources.get(StringKeyMatch.BHAKOOT_CANCEL_FRIENDLY, language)
        }

        // Same element cancellation
        val brideElement = getSignElement(brideSign)
        val groomElement = getSignElement(groomSign)
        if (brideElement == groomElement) {
            return StringResources.get(StringKeyMatch.BHAKOOT_CANCEL_ELEMENT, language).replace("{element}", brideElement)
        }

        return null
    }

    private fun getExaltationSign(planet: Planet): ZodiacSign? = when (planet) {
        Planet.SUN -> ZodiacSign.ARIES
        Planet.MOON -> ZodiacSign.TAURUS
        Planet.MARS -> ZodiacSign.CAPRICORN
        Planet.MERCURY -> ZodiacSign.VIRGO
        Planet.JUPITER -> ZodiacSign.CANCER
        Planet.VENUS -> ZodiacSign.PISCES
        Planet.SATURN -> ZodiacSign.LIBRA
        Planet.RAHU -> ZodiacSign.TAURUS
        Planet.KETU -> ZodiacSign.SCORPIO
        else -> null
    }

    private fun getSignElement(sign: ZodiacSign): String = when (sign) {
        ZodiacSign.ARIES, ZodiacSign.LEO, ZodiacSign.SAGITTARIUS -> "Fire"
        ZodiacSign.TAURUS, ZodiacSign.VIRGO, ZodiacSign.CAPRICORN -> "Earth"
        ZodiacSign.GEMINI, ZodiacSign.LIBRA, ZodiacSign.AQUARIUS -> "Air"
        ZodiacSign.CANCER, ZodiacSign.SCORPIO, ZodiacSign.PISCES -> "Water"
    }

    // ============================================
    // NADI KOOTA (8 Points)
    // ============================================

    fun calculateNadi(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        brideMoonSign: ZodiacSign,
        groomMoonSign: ZodiacSign,
        bridePada: Int,
        groomPada: Int,
        language: Language = Language.ENGLISH
    ): GunaAnalysis {
        val brideNadi = Nadi.fromNakshatra(brideNakshatra)
        val groomNadi = Nadi.fromNakshatra(groomNakshatra)

        val (points, hasDosha, cancellationReason) = if (brideNadi == groomNadi) {
            val cancellation = checkNadiDoshaCancellation(
                brideNakshatra, groomNakshatra, brideMoonSign, groomMoonSign, bridePada, groomPada, language
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
            hasDosha -> StringResources.get(StringKeyMatch.NADI_DOSHA_PRESENT, language)
                .replace("{nadi}", brideNadi.getLocalizedName(language))
            cancellationReason != null -> "${StringResources.get(StringKeyMatch.NADI_DOSHA_CANCELLED, language)} $cancellationReason"
            else -> StringResources.get(StringKeyMatch.NADI_DIFFERENT, language)
                .replace("{nadi1}", brideNadi.getLocalizedName(language))
                .replace("{nadi2}", groomNadi.getLocalizedName(language))
        }

        return GunaAnalysis(
            gunaType = GunaType.NADI,
            name = "Nadi",
            maxPoints = MatchmakingConstants.MAX_NADI,
            obtainedPoints = points,
            description = StringResources.get(StringKeyMatch.NADI_DESC, language),
            brideValue = brideNadi.getLocalizedName(language),
            groomValue = groomNadi.getLocalizedName(language),
            analysis = analysis,
            isPositive = !hasDosha
        )
    }

    private fun checkNadiDoshaCancellation(
        brideNakshatra: Nakshatra,
        groomNakshatra: Nakshatra,
        brideMoonSign: ZodiacSign,
        groomMoonSign: ZodiacSign,
        bridePada: Int,
        groomPada: Int,
        language: Language
    ): String? {
        // Same Nakshatra with different Rashi - strongest cancellation
        if (brideNakshatra == groomNakshatra && brideMoonSign != groomMoonSign) {
            return StringResources.get(StringKeyMatch.NADI_CANCEL_SAME_NAK_DIFF_RASHI, language)
                .replace("{nakshatra}", brideNakshatra.getLocalizedName(language))
        }

        // Same Rashi with different Nakshatra
        if (brideMoonSign == groomMoonSign && brideNakshatra != groomNakshatra) {
            return StringResources.get(StringKeyMatch.NADI_CANCEL_SAME_RASHI_DIFF_NAK, language)
                .replace("{rashi}", brideMoonSign.getLocalizedName(language))
        }

        // Same Nakshatra, same Rashi but different Pada
        if (brideNakshatra == groomNakshatra && brideMoonSign == groomMoonSign && bridePada != groomPada) {
            return StringResources.get(StringKeyMatch.NADI_CANCEL_DIFF_PADA, language)
                .replace("{pada1}", bridePada.toString())
                .replace("{pada2}", groomPada.toString())
        }

        // Special Nakshatra pairs that cancel
        for (pair in Nadi.cancellingPairs) {
            if (pair.contains(brideNakshatra) && pair.contains(groomNakshatra)) {
                return StringResources.get(StringKeyMatch.NADI_CANCEL_SPECIAL_PAIR, language)
                    .replace("{nak1}", brideNakshatra.getLocalizedName(language))
                    .replace("{nak2}", groomNakshatra.getLocalizedName(language))
            }
        }

        // Moon sign lords are mutual friends
        val brideLord = brideMoonSign.ruler
        val groomLord = groomMoonSign.ruler
        if (brideLord != groomLord) {
            val rel1 = PlanetaryFriendship.getRelationship(brideLord, groomLord)
            val rel2 = PlanetaryFriendship.getRelationship(groomLord, brideLord)
            if (rel1 == "Friend" && rel2 == "Friend") {
                return StringResources.get(StringKeyMatch.NADI_CANCEL_LORDS_FRIENDS, language)
                    .replace("{lord1}", brideLord.getLocalizedName(language))
                    .replace("{lord2}", groomLord.getLocalizedName(language))
            }
        }

        // Same Nakshatra ruler
        if (brideNakshatra.ruler == groomNakshatra.ruler) {
            return StringResources.get(StringKeyMatch.NADI_CANCEL_SAME_NAK_LORD, language)
                .replace("{lord}", brideNakshatra.ruler.getLocalizedName(language))
        }

        return null
    }
}

/**
 * Planetary friendship relationships for Graha Maitri calculation
 */
object PlanetaryFriendship {
    private val friendships = mapOf(
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

    fun getRelationship(planet1: Planet, planet2: Planet): String {
        val (friends, neutrals, enemies) = friendships[planet1] ?: return "Neutral"
        return when (planet2) {
            in friends -> "Friend"
            in enemies -> "Enemy"
            else -> "Neutral"
        }
    }
}
