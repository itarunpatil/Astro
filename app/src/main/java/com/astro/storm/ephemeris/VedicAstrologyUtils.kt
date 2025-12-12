package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.abs

/**
 * Centralized utility object for common Vedic astrology calculations.
 *
 * This module provides reusable functions for:
 * - Planetary dignity (exaltation, debilitation, own sign, Moolatrikona)
 * - Planetary relationships and friendships
 * - House calculations (Kendra, Trikona, Dusthana, etc.)
 * - Nakshatra attributes (Gana, Yoni, Nadi, Varna, etc.)
 * - Combustion and affliction calculations
 * - Aspect calculations
 *
 * All calculations follow traditional Vedic (Parashari) astrology principles.
 *
 * @see MatchmakingCalculator for Kundali matching calculations
 * @see YogaCalculator for yoga detection
 */
object VedicAstrologyUtils {

    // ============================================================================
    // PLANETARY DIGNITY - EXALTATION, DEBILITATION, MOOLATRIKONA, OWN SIGN
    // ============================================================================

    /**
     * Exaltation signs for each planet.
     * A planet in its exaltation sign is at its strongest state.
     *
     * Classical references:
     * - Sun: Aries (10 degrees - exact exaltation point)
     * - Moon: Taurus (3 degrees)
     * - Mars: Capricorn (28 degrees)
     * - Mercury: Virgo (15 degrees)
     * - Jupiter: Cancer (5 degrees)
     * - Venus: Pisces (27 degrees)
     * - Saturn: Libra (20 degrees)
     * - Rahu: Gemini/Taurus (varies by tradition)
     * - Ketu: Sagittarius/Scorpio (varies by tradition)
     */
    private val exaltationSigns = mapOf(
        Planet.SUN to ZodiacSign.ARIES,
        Planet.MOON to ZodiacSign.TAURUS,
        Planet.MARS to ZodiacSign.CAPRICORN,
        Planet.MERCURY to ZodiacSign.VIRGO,
        Planet.JUPITER to ZodiacSign.CANCER,
        Planet.VENUS to ZodiacSign.PISCES,
        Planet.SATURN to ZodiacSign.LIBRA,
        Planet.RAHU to ZodiacSign.GEMINI,    // Some texts use Taurus
        Planet.KETU to ZodiacSign.SAGITTARIUS // Some texts use Scorpio
    )

    /**
     * Debilitation signs for each planet (opposite of exaltation).
     * A planet in its debilitation sign is at its weakest state.
     */
    private val debilitationSigns = mapOf(
        Planet.SUN to ZodiacSign.LIBRA,
        Planet.MOON to ZodiacSign.SCORPIO,
        Planet.MARS to ZodiacSign.CANCER,
        Planet.MERCURY to ZodiacSign.PISCES,
        Planet.JUPITER to ZodiacSign.CAPRICORN,
        Planet.VENUS to ZodiacSign.VIRGO,
        Planet.SATURN to ZodiacSign.ARIES,
        Planet.RAHU to ZodiacSign.SAGITTARIUS,
        Planet.KETU to ZodiacSign.GEMINI
    )

    /**
     * Moolatrikona signs and degree ranges.
     * Moolatrikona is a special dignity between exaltation and own sign.
     * The planet functions optimally in these degrees.
     */
    data class MoolatrikonaRange(val sign: ZodiacSign, val startDegree: Double, val endDegree: Double)

    private val moolatrikonaSigns = mapOf(
        Planet.SUN to MoolatrikonaRange(ZodiacSign.LEO, 0.0, 20.0),
        Planet.MOON to MoolatrikonaRange(ZodiacSign.TAURUS, 4.0, 20.0),
        Planet.MARS to MoolatrikonaRange(ZodiacSign.ARIES, 0.0, 12.0),
        Planet.MERCURY to MoolatrikonaRange(ZodiacSign.VIRGO, 16.0, 20.0),
        Planet.JUPITER to MoolatrikonaRange(ZodiacSign.SAGITTARIUS, 0.0, 10.0),
        Planet.VENUS to MoolatrikonaRange(ZodiacSign.LIBRA, 0.0, 15.0),
        Planet.SATURN to MoolatrikonaRange(ZodiacSign.AQUARIUS, 0.0, 20.0)
    )

    /**
     * Own signs for each planet (Swakshetra).
     * Each planet rules one or two signs.
     */
    private val ownSigns = mapOf(
        Planet.SUN to listOf(ZodiacSign.LEO),
        Planet.MOON to listOf(ZodiacSign.CANCER),
        Planet.MARS to listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO),
        Planet.MERCURY to listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO),
        Planet.JUPITER to listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES),
        Planet.VENUS to listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA),
        Planet.SATURN to listOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS),
        Planet.RAHU to listOf(ZodiacSign.AQUARIUS),  // Co-rulership
        Planet.KETU to listOf(ZodiacSign.SCORPIO)    // Co-rulership
    )

    /**
     * Check if a planet is exalted in its current sign.
     */
    fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return exaltationSigns[planet] == sign
    }

    /**
     * Check if a planet position is exalted.
     */
    fun isExalted(pos: PlanetPosition): Boolean {
        return isExalted(pos.planet, pos.sign)
    }

    /**
     * Check if a planet is debilitated in its current sign.
     */
    fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return debilitationSigns[planet] == sign
    }

    /**
     * Check if a planet position is debilitated.
     */
    fun isDebilitated(pos: PlanetPosition): Boolean {
        return isDebilitated(pos.planet, pos.sign)
    }

    /**
     * Check if a planet is in its own sign (Swakshetra).
     */
    fun isInOwnSign(planet: Planet, sign: ZodiacSign): Boolean {
        return ownSigns[planet]?.contains(sign) == true || sign.ruler == planet
    }

    /**
     * Check if a planet position is in its own sign.
     */
    fun isInOwnSign(pos: PlanetPosition): Boolean {
        return isInOwnSign(pos.planet, pos.sign)
    }

    /**
     * Check if a planet is in its Moolatrikona sign and degree range.
     */
    fun isInMoolatrikona(planet: Planet, sign: ZodiacSign, degreeInSign: Double): Boolean {
        val moolatrikona = moolatrikonaSigns[planet] ?: return false
        return sign == moolatrikona.sign &&
               degreeInSign >= moolatrikona.startDegree &&
               degreeInSign <= moolatrikona.endDegree
    }

    /**
     * Check if a planet position is in Moolatrikona.
     */
    fun isInMoolatrikona(pos: PlanetPosition): Boolean {
        val degreeInSign = pos.longitude % 30.0
        return isInMoolatrikona(pos.planet, pos.sign, degreeInSign)
    }

    /**
     * Get the dignity status of a planet.
     */
    enum class PlanetaryDignity {
        EXALTED,           // Highest dignity
        MOOLATRIKONA,      // Second highest
        OWN_SIGN,          // Strong
        FRIEND_SIGN,       // Good
        NEUTRAL_SIGN,      // Neutral
        ENEMY_SIGN,        // Weak
        DEBILITATED        // Weakest
    }

    /**
     * Determine the complete dignity status of a planet position.
     */
    fun getDignity(pos: PlanetPosition): PlanetaryDignity {
        return when {
            isExalted(pos) -> PlanetaryDignity.EXALTED
            isDebilitated(pos) -> PlanetaryDignity.DEBILITATED
            isInMoolatrikona(pos) -> PlanetaryDignity.MOOLATRIKONA
            isInOwnSign(pos) -> PlanetaryDignity.OWN_SIGN
            isInFriendSign(pos) -> PlanetaryDignity.FRIEND_SIGN
            isInEnemySign(pos) -> PlanetaryDignity.ENEMY_SIGN
            else -> PlanetaryDignity.NEUTRAL_SIGN
        }
    }

    // ============================================================================
    // PLANETARY RELATIONSHIPS - FRIENDSHIPS
    // ============================================================================

    /**
     * Natural friendships between planets (Naisargika Mitra).
     * Based on Parashari system.
     */
    private val naturalFriends = mapOf(
        Planet.SUN to setOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
        Planet.MOON to setOf(Planet.SUN, Planet.MERCURY),
        Planet.MARS to setOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
        Planet.MERCURY to setOf(Planet.SUN, Planet.VENUS),
        Planet.JUPITER to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.VENUS to setOf(Planet.MERCURY, Planet.SATURN),
        Planet.SATURN to setOf(Planet.MERCURY, Planet.VENUS),
        Planet.RAHU to setOf(Planet.MERCURY, Planet.VENUS, Planet.SATURN),
        Planet.KETU to setOf(Planet.MARS, Planet.VENUS, Planet.SATURN)
    )

    /**
     * Natural enemies between planets (Naisargika Shatru).
     */
    private val naturalEnemies = mapOf(
        Planet.SUN to setOf(Planet.SATURN, Planet.VENUS),
        Planet.MOON to setOf<Planet>(), // Moon has no natural enemies
        Planet.MARS to setOf(Planet.MERCURY),
        Planet.MERCURY to setOf(Planet.MOON),
        Planet.JUPITER to setOf(Planet.MERCURY, Planet.VENUS),
        Planet.VENUS to setOf(Planet.SUN, Planet.MOON),
        Planet.SATURN to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.RAHU to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.KETU to setOf(Planet.SUN, Planet.MOON)
    )

    /**
     * Relationship types between planets.
     */
    enum class PlanetaryRelationship {
        BEST_FRIEND,      // Adhimitra - Natural friend + Temporary friend
        FRIEND,           // Mitra - Natural friend
        NEUTRAL,          // Sama - Neutral
        ENEMY,            // Shatru - Natural enemy
        BITTER_ENEMY      // Adhishatru - Natural enemy + Temporary enemy
    }

    /**
     * Check if two planets are natural friends.
     */
    fun areNaturalFriends(planet1: Planet, planet2: Planet): Boolean {
        return naturalFriends[planet1]?.contains(planet2) == true
    }

    /**
     * Check if two planets are natural enemies.
     */
    fun areNaturalEnemies(planet1: Planet, planet2: Planet): Boolean {
        return naturalEnemies[planet1]?.contains(planet2) == true
    }

    /**
     * Get the natural relationship between two planets.
     */
    fun getNaturalRelationship(planet1: Planet, planet2: Planet): PlanetaryRelationship {
        return when {
            areNaturalFriends(planet1, planet2) -> PlanetaryRelationship.FRIEND
            areNaturalEnemies(planet1, planet2) -> PlanetaryRelationship.ENEMY
            else -> PlanetaryRelationship.NEUTRAL
        }
    }

    /**
     * Check if a planet is in a friendly sign.
     */
    fun isInFriendSign(planet: Planet, sign: ZodiacSign): Boolean {
        val signLord = sign.ruler
        return areNaturalFriends(planet, signLord)
    }

    /**
     * Check if a planet position is in a friendly sign.
     */
    fun isInFriendSign(pos: PlanetPosition): Boolean {
        return isInFriendSign(pos.planet, pos.sign)
    }

    /**
     * Check if a planet is in an enemy sign.
     */
    fun isInEnemySign(planet: Planet, sign: ZodiacSign): Boolean {
        val signLord = sign.ruler
        return areNaturalEnemies(planet, signLord)
    }

    /**
     * Check if a planet position is in an enemy sign.
     */
    fun isInEnemySign(pos: PlanetPosition): Boolean {
        return isInEnemySign(pos.planet, pos.sign)
    }

    /**
     * Calculate Panchada (5-fold) relationship considering temporary friendship.
     * Temporary friendship is based on house positions in a chart.
     */
    fun getComprehensiveRelationship(
        planet1: Planet,
        planet2: Planet,
        chart: VedicChart
    ): PlanetaryRelationship {
        val pos1 = chart.planetPositions.find { it.planet == planet1 }
        val pos2 = chart.planetPositions.find { it.planet == planet2 }

        if (pos1 == null || pos2 == null) {
            return getNaturalRelationship(planet1, planet2)
        }

        val houseDiff = abs(pos1.house - pos2.house)
        val isTemporaryFriend = houseDiff in listOf(0, 1, 2, 3, 4, 10, 11, 12) // 2,3,4,10,11,12 from each other
        val isTemporaryEnemy = houseDiff in listOf(5, 6, 7, 8, 9)

        val naturalRel = getNaturalRelationship(planet1, planet2)

        return when {
            naturalRel == PlanetaryRelationship.FRIEND && isTemporaryFriend ->
                PlanetaryRelationship.BEST_FRIEND
            naturalRel == PlanetaryRelationship.ENEMY && isTemporaryEnemy ->
                PlanetaryRelationship.BITTER_ENEMY
            naturalRel == PlanetaryRelationship.FRIEND || isTemporaryFriend ->
                PlanetaryRelationship.FRIEND
            naturalRel == PlanetaryRelationship.ENEMY || isTemporaryEnemy ->
                PlanetaryRelationship.ENEMY
            else -> PlanetaryRelationship.NEUTRAL
        }
    }

    // ============================================================================
    // HOUSE CLASSIFICATIONS
    // ============================================================================

    /** Kendra houses (Angular/Quadrant) - 1, 4, 7, 10 */
    val KENDRA_HOUSES = setOf(1, 4, 7, 10)

    /** Trikona houses (Trine) - 1, 5, 9 */
    val TRIKONA_HOUSES = setOf(1, 5, 9)

    /** Dusthana houses (Malefic) - 6, 8, 12 */
    val DUSTHANA_HOUSES = setOf(6, 8, 12)

    /** Upachaya houses (Growth) - 3, 6, 10, 11 */
    val UPACHAYA_HOUSES = setOf(3, 6, 10, 11)

    /** Maraka houses (Death-inflicting) - 2, 7 */
    val MARAKA_HOUSES = setOf(2, 7)

    /** Dharma houses - 1, 5, 9 */
    val DHARMA_HOUSES = setOf(1, 5, 9)

    /** Artha houses - 2, 6, 10 */
    val ARTHA_HOUSES = setOf(2, 6, 10)

    /** Kama houses - 3, 7, 11 */
    val KAMA_HOUSES = setOf(3, 7, 11)

    /** Moksha houses - 4, 8, 12 */
    val MOKSHA_HOUSES = setOf(4, 8, 12)

    /**
     * Check if a house is a Kendra (angular) house.
     */
    fun isKendra(house: Int): Boolean = house in KENDRA_HOUSES

    /**
     * Check if a house is a Trikona (trine) house.
     */
    fun isTrikona(house: Int): Boolean = house in TRIKONA_HOUSES

    /**
     * Check if a house is a Dusthana (malefic) house.
     */
    fun isDusthana(house: Int): Boolean = house in DUSTHANA_HOUSES

    /**
     * Check if a house is an Upachaya (growth) house.
     */
    fun isUpachaya(house: Int): Boolean = house in UPACHAYA_HOUSES

    /**
     * Calculate house number from reference (1-indexed).
     * @param targetSign The sign to calculate house for
     * @param referenceSign The sign of the ascendant or reference point
     * @return House number (1-12)
     */
    fun getHouseFromSigns(targetSign: ZodiacSign, referenceSign: ZodiacSign): Int {
        val diff = targetSign.number - referenceSign.number
        return if (diff >= 0) diff + 1 else diff + 13
    }

    // ============================================================================
    // NAKSHATRA ATTRIBUTES
    // ============================================================================

    /**
     * Gana (Temperament) for each Nakshatra.
     * - Deva: Divine, gentle, spiritual nature
     * - Manushya: Human, balanced, worldly nature
     * - Rakshasa: Demonic, aggressive, dominant nature
     */
    enum class Gana { DEVA, MANUSHYA, RAKSHASA }

    private val nakshatraGanaMap = mapOf(
        Nakshatra.ASHWINI to Gana.DEVA,
        Nakshatra.BHARANI to Gana.MANUSHYA,
        Nakshatra.KRITTIKA to Gana.RAKSHASA,
        Nakshatra.ROHINI to Gana.MANUSHYA,
        Nakshatra.MRIGASHIRA to Gana.DEVA,
        Nakshatra.ARDRA to Gana.MANUSHYA,
        Nakshatra.PUNARVASU to Gana.DEVA,
        Nakshatra.PUSHYA to Gana.DEVA,
        Nakshatra.ASHLESHA to Gana.RAKSHASA,
        Nakshatra.MAGHA to Gana.RAKSHASA,
        Nakshatra.PURVA_PHALGUNI to Gana.MANUSHYA,
        Nakshatra.UTTARA_PHALGUNI to Gana.MANUSHYA,
        Nakshatra.HASTA to Gana.DEVA,
        Nakshatra.CHITRA to Gana.RAKSHASA,
        Nakshatra.SWATI to Gana.DEVA,
        Nakshatra.VISHAKHA to Gana.RAKSHASA,
        Nakshatra.ANURADHA to Gana.DEVA,
        Nakshatra.JYESHTHA to Gana.RAKSHASA,
        Nakshatra.MULA to Gana.RAKSHASA,
        Nakshatra.PURVA_ASHADHA to Gana.MANUSHYA,
        Nakshatra.UTTARA_ASHADHA to Gana.MANUSHYA,
        Nakshatra.SHRAVANA to Gana.DEVA,
        Nakshatra.DHANISHTHA to Gana.RAKSHASA,
        Nakshatra.SHATABHISHA to Gana.RAKSHASA,
        Nakshatra.PURVA_BHADRAPADA to Gana.MANUSHYA,
        Nakshatra.UTTARA_BHADRAPADA to Gana.MANUSHYA,
        Nakshatra.REVATI to Gana.DEVA
    )

    /**
     * Get the Gana (temperament) for a Nakshatra.
     */
    fun getGana(nakshatra: Nakshatra): Gana {
        return nakshatraGanaMap[nakshatra] ?: Gana.MANUSHYA
    }

    /**
     * Get localized Gana name.
     */
    fun getGanaName(gana: Gana, language: Language): String {
        return when (gana) {
            Gana.DEVA -> StringResources.get(StringKey.GANA_DEVA, language)
            Gana.MANUSHYA -> StringResources.get(StringKey.GANA_MANUSHYA, language)
            Gana.RAKSHASA -> StringResources.get(StringKey.GANA_RAKSHASA, language)
        }
    }

    /**
     * Yoni (Sexual compatibility) data for each Nakshatra.
     */
    data class YoniInfo(
        val animal: String,
        val animalKey: StringKey,
        val gender: Gender,
        val groupId: Int
    )

    enum class Gender { MALE, FEMALE }

    private val nakshatraYoniMap = mapOf(
        Nakshatra.ASHWINI to YoniInfo("Horse", StringKey.YONI_HORSE, Gender.MALE, 1),
        Nakshatra.BHARANI to YoniInfo("Elephant", StringKey.YONI_ELEPHANT, Gender.MALE, 2),
        Nakshatra.KRITTIKA to YoniInfo("Sheep", StringKey.YONI_SHEEP, Gender.FEMALE, 3),
        Nakshatra.ROHINI to YoniInfo("Serpent", StringKey.YONI_SERPENT, Gender.MALE, 4),
        Nakshatra.MRIGASHIRA to YoniInfo("Serpent", StringKey.YONI_SERPENT, Gender.FEMALE, 4),
        Nakshatra.ARDRA to YoniInfo("Dog", StringKey.YONI_DOG, Gender.FEMALE, 5),
        Nakshatra.PUNARVASU to YoniInfo("Cat", StringKey.YONI_CAT, Gender.FEMALE, 6),
        Nakshatra.PUSHYA to YoniInfo("Sheep", StringKey.YONI_SHEEP, Gender.MALE, 3),
        Nakshatra.ASHLESHA to YoniInfo("Cat", StringKey.YONI_CAT, Gender.MALE, 6),
        Nakshatra.MAGHA to YoniInfo("Rat", StringKey.YONI_RAT, Gender.MALE, 7),
        Nakshatra.PURVA_PHALGUNI to YoniInfo("Rat", StringKey.YONI_RAT, Gender.FEMALE, 7),
        Nakshatra.UTTARA_PHALGUNI to YoniInfo("Cow", StringKey.YONI_COW, Gender.MALE, 8),
        Nakshatra.HASTA to YoniInfo("Buffalo", StringKey.YONI_BUFFALO, Gender.FEMALE, 9),
        Nakshatra.CHITRA to YoniInfo("Tiger", StringKey.YONI_TIGER, Gender.FEMALE, 10),
        Nakshatra.SWATI to YoniInfo("Buffalo", StringKey.YONI_BUFFALO, Gender.MALE, 9),
        Nakshatra.VISHAKHA to YoniInfo("Tiger", StringKey.YONI_TIGER, Gender.MALE, 10),
        Nakshatra.ANURADHA to YoniInfo("Deer", StringKey.YONI_DEER, Gender.FEMALE, 11),
        Nakshatra.JYESHTHA to YoniInfo("Deer", StringKey.YONI_DEER, Gender.MALE, 11),
        Nakshatra.MULA to YoniInfo("Dog", StringKey.YONI_DOG, Gender.MALE, 5),
        Nakshatra.PURVA_ASHADHA to YoniInfo("Monkey", StringKey.YONI_MONKEY, Gender.MALE, 12),
        Nakshatra.UTTARA_ASHADHA to YoniInfo("Mongoose", StringKey.YONI_MONGOOSE, Gender.MALE, 13),
        Nakshatra.SHRAVANA to YoniInfo("Monkey", StringKey.YONI_MONKEY, Gender.FEMALE, 12),
        Nakshatra.DHANISHTHA to YoniInfo("Lion", StringKey.YONI_LION, Gender.FEMALE, 14),
        Nakshatra.SHATABHISHA to YoniInfo("Horse", StringKey.YONI_HORSE, Gender.FEMALE, 1),
        Nakshatra.PURVA_BHADRAPADA to YoniInfo("Lion", StringKey.YONI_LION, Gender.MALE, 14),
        Nakshatra.UTTARA_BHADRAPADA to YoniInfo("Cow", StringKey.YONI_COW, Gender.FEMALE, 8),
        Nakshatra.REVATI to YoniInfo("Elephant", StringKey.YONI_ELEPHANT, Gender.FEMALE, 2)
    )

    /**
     * Get the Yoni info for a Nakshatra.
     */
    fun getYoni(nakshatra: Nakshatra): YoniInfo? {
        return nakshatraYoniMap[nakshatra]
    }

    /**
     * Get Yoni display string (English format).
     */
    fun getYoniDisplayName(nakshatra: Nakshatra): String {
        val yoni = nakshatraYoniMap[nakshatra] ?: return "Unknown"
        val genderStr = if (yoni.gender == Gender.MALE) "Male" else "Female"
        return "${yoni.animal} ($genderStr)"
    }

    /**
     * Get localized Yoni display string.
     */
    fun getYoniDisplayName(nakshatra: Nakshatra, language: Language): String {
        val yoni = nakshatraYoniMap[nakshatra] ?: return "Unknown"
        val animalName = StringResources.get(yoni.animalKey, language)
        val genderStr = if (yoni.gender == Gender.MALE)
            StringResources.get(StringKey.GENDER_MALE, language)
        else
            StringResources.get(StringKey.GENDER_FEMALE, language)
        return "$animalName ($genderStr)"
    }

    /**
     * Get Gana display name (English).
     */
    fun getGanaDisplayName(nakshatra: Nakshatra): String {
        return when (getGana(nakshatra)) {
            Gana.DEVA -> "Deva"
            Gana.MANUSHYA -> "Manushya"
            Gana.RAKSHASA -> "Rakshasa"
        }
    }

    /**
     * Get localized Gana display name.
     */
    fun getGanaDisplayName(nakshatra: Nakshatra, language: Language): String {
        return getGanaName(getGana(nakshatra), language)
    }

    /**
     * Nadi (Health compatibility) for each Nakshatra.
     * - Adi (Vata): Wind element, beginning
     * - Madhya (Pitta): Fire element, middle
     * - Antya (Kapha): Water element, end
     */
    enum class Nadi { ADI, MADHYA, ANTYA }

    private val nakshatraNadiMap = mapOf(
        Nakshatra.ASHWINI to Nadi.ADI,
        Nakshatra.BHARANI to Nadi.MADHYA,
        Nakshatra.KRITTIKA to Nadi.ANTYA,
        Nakshatra.ROHINI to Nadi.ANTYA,
        Nakshatra.MRIGASHIRA to Nadi.MADHYA,
        Nakshatra.ARDRA to Nadi.ADI,
        Nakshatra.PUNARVASU to Nadi.ADI,
        Nakshatra.PUSHYA to Nadi.MADHYA,
        Nakshatra.ASHLESHA to Nadi.ANTYA,
        Nakshatra.MAGHA to Nadi.ANTYA,
        Nakshatra.PURVA_PHALGUNI to Nadi.MADHYA,
        Nakshatra.UTTARA_PHALGUNI to Nadi.ADI,
        Nakshatra.HASTA to Nadi.ADI,
        Nakshatra.CHITRA to Nadi.MADHYA,
        Nakshatra.SWATI to Nadi.ANTYA,
        Nakshatra.VISHAKHA to Nadi.ANTYA,
        Nakshatra.ANURADHA to Nadi.MADHYA,
        Nakshatra.JYESHTHA to Nadi.ADI,
        Nakshatra.MULA to Nadi.ADI,
        Nakshatra.PURVA_ASHADHA to Nadi.MADHYA,
        Nakshatra.UTTARA_ASHADHA to Nadi.ANTYA,
        Nakshatra.SHRAVANA to Nadi.ANTYA,
        Nakshatra.DHANISHTHA to Nadi.MADHYA,
        Nakshatra.SHATABHISHA to Nadi.ADI,
        Nakshatra.PURVA_BHADRAPADA to Nadi.ADI,
        Nakshatra.UTTARA_BHADRAPADA to Nadi.MADHYA,
        Nakshatra.REVATI to Nadi.ANTYA
    )

    /**
     * Get the Nadi for a Nakshatra.
     */
    fun getNadi(nakshatra: Nakshatra): Nadi {
        return nakshatraNadiMap[nakshatra] ?: Nadi.MADHYA
    }

    /**
     * Varna (Social class) for each Nakshatra.
     * Used in Varna Koot matching.
     */
    enum class Varna(val value: Int) { BRAHMIN(4), KSHATRIYA(3), VAISHYA(2), SHUDRA(1) }

    private val nakshatraVarnaMap = mapOf(
        Nakshatra.ASHWINI to Varna.VAISHYA,
        Nakshatra.BHARANI to Varna.SHUDRA,
        Nakshatra.KRITTIKA to Varna.BRAHMIN,
        Nakshatra.ROHINI to Varna.SHUDRA,
        Nakshatra.MRIGASHIRA to Varna.SHUDRA,
        Nakshatra.ARDRA to Varna.SHUDRA,
        Nakshatra.PUNARVASU to Varna.VAISHYA,
        Nakshatra.PUSHYA to Varna.KSHATRIYA,
        Nakshatra.ASHLESHA to Varna.SHUDRA,
        Nakshatra.MAGHA to Varna.SHUDRA,
        Nakshatra.PURVA_PHALGUNI to Varna.BRAHMIN,
        Nakshatra.UTTARA_PHALGUNI to Varna.KSHATRIYA,
        Nakshatra.HASTA to Varna.VAISHYA,
        Nakshatra.CHITRA to Varna.SHUDRA,
        Nakshatra.SWATI to Varna.SHUDRA,
        Nakshatra.VISHAKHA to Varna.BRAHMIN,
        Nakshatra.ANURADHA to Varna.SHUDRA,
        Nakshatra.JYESHTHA to Varna.SHUDRA,
        Nakshatra.MULA to Varna.SHUDRA,
        Nakshatra.PURVA_ASHADHA to Varna.BRAHMIN,
        Nakshatra.UTTARA_ASHADHA to Varna.KSHATRIYA,
        Nakshatra.SHRAVANA to Varna.SHUDRA,
        Nakshatra.DHANISHTHA to Varna.SHUDRA,
        Nakshatra.SHATABHISHA to Varna.SHUDRA,
        Nakshatra.PURVA_BHADRAPADA to Varna.BRAHMIN,
        Nakshatra.UTTARA_BHADRAPADA to Varna.KSHATRIYA,
        Nakshatra.REVATI to Varna.SHUDRA
    )

    /**
     * Get the Varna for a Nakshatra.
     */
    fun getVarna(nakshatra: Nakshatra): Varna {
        return nakshatraVarnaMap[nakshatra] ?: Varna.SHUDRA
    }

    /**
     * Rajju (Body part) classification for Nakshatras.
     * Used in Rajju Koot matching for longevity of spouse.
     */
    enum class Rajju { PADA, KATI, NABHI, KANTHA, SIRO }

    /**
     * Rajju direction for enhanced matching.
     */
    enum class RajjuDirection { ASCENDING, DESCENDING }

    data class RajjuInfo(val rajju: Rajju, val direction: RajjuDirection)

    private val nakshatraRajjuMap = mapOf(
        // Pada Rajju (Feet)
        Nakshatra.ASHWINI to RajjuInfo(Rajju.PADA, RajjuDirection.ASCENDING),
        Nakshatra.ASHLESHA to RajjuInfo(Rajju.PADA, RajjuDirection.DESCENDING),
        Nakshatra.MAGHA to RajjuInfo(Rajju.PADA, RajjuDirection.ASCENDING),
        Nakshatra.JYESHTHA to RajjuInfo(Rajju.PADA, RajjuDirection.DESCENDING),
        Nakshatra.MULA to RajjuInfo(Rajju.PADA, RajjuDirection.ASCENDING),
        Nakshatra.REVATI to RajjuInfo(Rajju.PADA, RajjuDirection.DESCENDING),

        // Kati Rajju (Waist)
        Nakshatra.BHARANI to RajjuInfo(Rajju.KATI, RajjuDirection.ASCENDING),
        Nakshatra.PUSHYA to RajjuInfo(Rajju.KATI, RajjuDirection.DESCENDING),
        Nakshatra.PURVA_PHALGUNI to RajjuInfo(Rajju.KATI, RajjuDirection.ASCENDING),
        Nakshatra.ANURADHA to RajjuInfo(Rajju.KATI, RajjuDirection.DESCENDING),
        Nakshatra.PURVA_ASHADHA to RajjuInfo(Rajju.KATI, RajjuDirection.ASCENDING),
        Nakshatra.UTTARA_BHADRAPADA to RajjuInfo(Rajju.KATI, RajjuDirection.DESCENDING),

        // Nabhi Rajju (Navel)
        Nakshatra.KRITTIKA to RajjuInfo(Rajju.NABHI, RajjuDirection.ASCENDING),
        Nakshatra.PUNARVASU to RajjuInfo(Rajju.NABHI, RajjuDirection.DESCENDING),
        Nakshatra.UTTARA_PHALGUNI to RajjuInfo(Rajju.NABHI, RajjuDirection.ASCENDING),
        Nakshatra.VISHAKHA to RajjuInfo(Rajju.NABHI, RajjuDirection.DESCENDING),
        Nakshatra.UTTARA_ASHADHA to RajjuInfo(Rajju.NABHI, RajjuDirection.ASCENDING),
        Nakshatra.PURVA_BHADRAPADA to RajjuInfo(Rajju.NABHI, RajjuDirection.DESCENDING),

        // Kantha Rajju (Neck)
        Nakshatra.ROHINI to RajjuInfo(Rajju.KANTHA, RajjuDirection.ASCENDING),
        Nakshatra.ARDRA to RajjuInfo(Rajju.KANTHA, RajjuDirection.DESCENDING),
        Nakshatra.HASTA to RajjuInfo(Rajju.KANTHA, RajjuDirection.ASCENDING),
        Nakshatra.SWATI to RajjuInfo(Rajju.KANTHA, RajjuDirection.DESCENDING),
        Nakshatra.SHRAVANA to RajjuInfo(Rajju.KANTHA, RajjuDirection.ASCENDING),
        Nakshatra.SHATABHISHA to RajjuInfo(Rajju.KANTHA, RajjuDirection.DESCENDING),

        // Siro Rajju (Head)
        Nakshatra.MRIGASHIRA to RajjuInfo(Rajju.SIRO, RajjuDirection.ASCENDING),
        Nakshatra.CHITRA to RajjuInfo(Rajju.SIRO, RajjuDirection.ASCENDING),
        Nakshatra.DHANISHTHA to RajjuInfo(Rajju.SIRO, RajjuDirection.ASCENDING)
    )

    /**
     * Get the Rajju info for a Nakshatra.
     */
    fun getRajju(nakshatra: Nakshatra): RajjuInfo {
        return nakshatraRajjuMap[nakshatra] ?: RajjuInfo(Rajju.NABHI, RajjuDirection.ASCENDING)
    }

    // ============================================================================
    // COMBUSTION (ASTA) CALCULATIONS
    // ============================================================================

    /**
     * Combustion orbs for each planet (degrees from Sun).
     * When a planet is within these degrees of the Sun, it's considered combust.
     */
    private val combustionOrbs = mapOf(
        Planet.MOON to 12.0,
        Planet.MARS to 17.0,
        Planet.MERCURY to 14.0,  // 12 when retrograde
        Planet.JUPITER to 11.0,
        Planet.VENUS to 10.0,    // 8 when retrograde
        Planet.SATURN to 15.0
    )

    /**
     * Check if a planet is combust (too close to Sun).
     * Combust planets lose strength and their significations suffer.
     *
     * @param planet The planet to check
     * @param planetLongitude The longitude of the planet
     * @param sunLongitude The longitude of the Sun
     * @param isRetrograde Whether the planet is retrograde
     * @return True if the planet is combust
     */
    fun isCombust(
        planet: Planet,
        planetLongitude: Double,
        sunLongitude: Double,
        isRetrograde: Boolean = false
    ): Boolean {
        // Sun, Rahu, Ketu cannot be combust
        if (planet in listOf(Planet.SUN, Planet.RAHU, Planet.KETU)) return false

        val orb = combustionOrbs[planet] ?: return false
        val adjustedOrb = when {
            planet == Planet.MERCURY && isRetrograde -> 12.0
            planet == Planet.VENUS && isRetrograde -> 8.0
            else -> orb
        }

        val diff = abs(normalizeAngle(planetLongitude - sunLongitude))
        return diff <= adjustedOrb || diff >= (360.0 - adjustedOrb)
    }

    /**
     * Check if a planet position is combust.
     */
    fun isCombust(pos: PlanetPosition, sunPosition: PlanetPosition): Boolean {
        return isCombust(pos.planet, pos.longitude, sunPosition.longitude, pos.isRetrograde)
    }

    // ============================================================================
    // DIG BALA (DIRECTIONAL STRENGTH)
    // ============================================================================

    /**
     * Houses where planets have maximum Dig Bala (directional strength).
     */
    private val digBalaHouses = mapOf(
        Planet.SUN to 10,      // 10th house (South/Midheaven)
        Planet.MARS to 10,     // 10th house
        Planet.JUPITER to 1,   // 1st house (East/Ascendant)
        Planet.MERCURY to 1,   // 1st house
        Planet.MOON to 4,      // 4th house (North/IC)
        Planet.VENUS to 4,     // 4th house
        Planet.SATURN to 7     // 7th house (West/Descendant)
    )

    /**
     * Check if a planet has Dig Bala (directional strength).
     */
    fun hasDigBala(planet: Planet, house: Int): Boolean {
        return digBalaHouses[planet] == house
    }

    /**
     * Check if a planet position has Dig Bala.
     */
    fun hasDigBala(pos: PlanetPosition): Boolean {
        return hasDigBala(pos.planet, pos.house)
    }

    // ============================================================================
    // BENEFIC/MALEFIC CLASSIFICATION
    // ============================================================================

    /** Natural benefics */
    val NATURAL_BENEFICS = setOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)

    /** Natural malefics */
    val NATURAL_MALEFICS = setOf(Planet.SUN, Planet.MARS, Planet.SATURN, Planet.RAHU, Planet.KETU)

    /**
     * Check if a planet is naturally benefic.
     * Note: Mercury becomes malefic when conjunct malefics.
     * Note: Moon becomes malefic when waning (Krishna Paksha).
     */
    fun isNaturalBenefic(planet: Planet): Boolean {
        return planet in NATURAL_BENEFICS
    }

    /**
     * Check if a planet is naturally malefic.
     */
    fun isNaturalMalefic(planet: Planet): Boolean {
        return planet in NATURAL_MALEFICS
    }

    /**
     * Check functional benefic status based on ascendant.
     * A planet ruling Kendra or Trikona becomes benefic for that ascendant.
     */
    fun isFunctionalBenefic(planet: Planet, ascendantSign: ZodiacSign): Boolean {
        val ruledSigns = ownSigns[planet] ?: emptyList()

        return ruledSigns.any { sign ->
            val houseFromAsc = getHouseFromSigns(sign, ascendantSign)
            houseFromAsc in KENDRA_HOUSES || houseFromAsc in TRIKONA_HOUSES
        }
    }

    /**
     * Check functional malefic status based on ascendant.
     * A planet ruling Dusthana becomes malefic for that ascendant.
     */
    fun isFunctionalMalefic(planet: Planet, ascendantSign: ZodiacSign): Boolean {
        val ruledSigns = ownSigns[planet] ?: emptyList()

        return ruledSigns.any { sign ->
            val houseFromAsc = getHouseFromSigns(sign, ascendantSign)
            houseFromAsc in DUSTHANA_HOUSES
        }
    }

    // ============================================================================
    // ASPECT CALCULATIONS
    // ============================================================================

    /**
     * Standard aspects that all planets have (7th house from their position).
     * Special aspects are handled separately.
     */
    private val specialAspects = mapOf(
        Planet.MARS to listOf(4, 8),         // Mars aspects 4th and 8th additionally
        Planet.JUPITER to listOf(5, 9),      // Jupiter aspects 5th and 9th additionally
        Planet.SATURN to listOf(3, 10),      // Saturn aspects 3rd and 10th additionally
        Planet.RAHU to listOf(5, 9),         // Rahu aspects like Jupiter (some schools)
        Planet.KETU to listOf(5, 9)          // Ketu aspects like Jupiter (some schools)
    )

    /**
     * Get all houses a planet aspects from its current house.
     * @param planet The planet
     * @param fromHouse The house the planet is in (1-12)
     * @return List of houses (1-12) the planet aspects
     */
    fun getAspectedHouses(planet: Planet, fromHouse: Int): List<Int> {
        val aspects = mutableListOf<Int>()

        // All planets aspect the 7th house from themselves
        val seventhHouse = ((fromHouse + 6) % 12).let { if (it == 0) 12 else it }
        aspects.add(seventhHouse)

        // Add special aspects
        specialAspects[planet]?.forEach { offset ->
            val aspectedHouse = ((fromHouse + offset - 1) % 12).let { if (it == 0) 12 else it }
            aspects.add(aspectedHouse)
        }

        return aspects.distinct().sorted()
    }

    /**
     * Check if a planet aspects a specific house.
     */
    fun aspectsHouse(planet: Planet, fromHouse: Int, targetHouse: Int): Boolean {
        return targetHouse in getAspectedHouses(planet, fromHouse)
    }

    // ============================================================================
    // UTILITY FUNCTIONS
    // ============================================================================

    /**
     * Normalize an angle to 0-360 range.
     */
    fun normalizeAngle(angle: Double): Double {
        return ((angle % 360.0) + 360.0) % 360.0
    }

    /**
     * Alias for normalizeAngle for semantic clarity when working with longitudes.
     */
    fun normalizeLongitude(longitude: Double): Double = normalizeAngle(longitude)

    /**
     * Alias for normalizeAngle for semantic clarity when working with degrees.
     */
    fun normalizeDegree(degree: Double): Double = normalizeAngle(degree)

    /**
     * Get zodiac sign from longitude.
     * @param longitude The sidereal longitude in degrees.
     * @return The corresponding zodiac sign.
     */
    fun getSignFromLongitude(longitude: Double): ZodiacSign {
        return ZodiacSign.fromLongitude(normalizeLongitude(longitude))
    }

    /**
     * Get the degree within the sign (0-30).
     * @param longitude The sidereal longitude in degrees.
     * @return The degree within the sign (0-30).
     */
    fun getDegreeInSign(longitude: Double): Double {
        return normalizeLongitude(longitude) % 30.0
    }

    /**
     * Calculate the angular distance between two longitudes.
     * Returns the shortest distance (0-180).
     * @param long1 First longitude.
     * @param long2 Second longitude.
     * @return The angular distance (0-180).
     */
    fun angularDistance(long1: Double, long2: Double): Double {
        val diff = abs(normalizeLongitude(long1) - normalizeLongitude(long2))
        return if (diff > 180.0) 360.0 - diff else diff
    }

    /**
     * Calculate whole sign house from longitude and ascendant.
     * @param longitude The planet's longitude.
     * @param ascendantLongitude The ascendant longitude.
     * @return The house number (1-12).
     */
    fun calculateWholeSignHouse(longitude: Double, ascendantLongitude: Double): Int {
        val normalizedLong = normalizeLongitude(longitude)
        val normalizedAsc = normalizeLongitude(ascendantLongitude)
        val ascSign = (normalizedAsc / 30.0).toInt().coerceIn(0, 11)
        val planetSign = (normalizedLong / 30.0).toInt().coerceIn(0, 11)
        val house = ((planetSign - ascSign + 12) % 12) + 1
        return house.coerceIn(1, 12)
    }

    /**
     * Get ordinal suffix for a number (1st, 2nd, 3rd, etc.)
     * @param n The number.
     * @return The ordinal suffix.
     */
    fun getOrdinalSuffix(n: Int): String {
        return when {
            n in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    /**
     * Get Moon position from a chart.
     */
    fun getMoonPosition(chart: VedicChart): PlanetPosition? {
        return chart.planetPositions.find { it.planet == Planet.MOON }
    }

    /**
     * Get Sun position from a chart.
     */
    fun getSunPosition(chart: VedicChart): PlanetPosition? {
        return chart.planetPositions.find { it.planet == Planet.SUN }
    }

    /**
     * Get a specific planet's position from a chart.
     */
    fun getPlanetPosition(chart: VedicChart, planet: Planet): PlanetPosition? {
        return chart.planetPositions.find { it.planet == planet }
    }

    /**
     * Get the ascendant sign from a chart.
     */
    fun getAscendantSign(chart: VedicChart): ZodiacSign {
        return ZodiacSign.fromLongitude(chart.ascendant)
    }

    /**
     * Get all planets in a specific house.
     */
    fun getPlanetsInHouse(chart: VedicChart, house: Int): List<PlanetPosition> {
        return chart.planetPositions.filter { it.house == house }
    }

    /**
     * Get the lord of a house.
     */
    fun getHouseLord(chart: VedicChart, house: Int): Planet {
        val ascSign = getAscendantSign(chart)
        val houseSignNumber = ((ascSign.number + house - 2) % 12) + 1
        val houseSign = ZodiacSign.entries.find { it.number == houseSignNumber } ?: ZodiacSign.ARIES
        return houseSign.ruler
    }

    /**
     * Check if two planets are in conjunction (same house or within orb).
     */
    fun areInConjunction(pos1: PlanetPosition, pos2: PlanetPosition, orb: Double = 10.0): Boolean {
        if (pos1.house != pos2.house) return false
        val diff = abs(normalizeAngle(pos1.longitude - pos2.longitude))
        return diff <= orb || diff >= (360.0 - orb)
    }

    /**
     * Check if a planet is hemmed between malefics (Papakartari Yoga).
     */
    fun isPapakartari(pos: PlanetPosition, chart: VedicChart): Boolean {
        val prevHouse = if (pos.house == 1) 12 else pos.house - 1
        val nextHouse = if (pos.house == 12) 1 else pos.house + 1

        val hasMaleficBefore = chart.planetPositions.any {
            it.house == prevHouse && isNaturalMalefic(it.planet)
        }
        val hasMaleficAfter = chart.planetPositions.any {
            it.house == nextHouse && isNaturalMalefic(it.planet)
        }

        return hasMaleficBefore && hasMaleficAfter
    }

    /**
     * Check if a planet is hemmed between benefics (Shubhakartari Yoga).
     */
    fun isShubhakartari(pos: PlanetPosition, chart: VedicChart): Boolean {
        val prevHouse = if (pos.house == 1) 12 else pos.house - 1
        val nextHouse = if (pos.house == 12) 1 else pos.house + 1

        val hasBeneficBefore = chart.planetPositions.any {
            it.house == prevHouse && isNaturalBenefic(it.planet)
        }
        val hasBeneficAfter = chart.planetPositions.any {
            it.house == nextHouse && isNaturalBenefic(it.planet)
        }

        return hasBeneficBefore && hasBeneficAfter
    }
}
