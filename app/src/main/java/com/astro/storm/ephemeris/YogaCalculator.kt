package com.astro.storm.ephemeris

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.Nakshatra
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import kotlin.math.abs
import kotlin.math.min

/**
 * Comprehensive Yoga Calculator for Vedic Astrology
 *
 * This calculator identifies all major Yogas (planetary combinations) including:
 * 1. Raja Yogas - Power and authority combinations
 * 2. Dhana Yogas - Wealth combinations
 * 3. Pancha Mahapurusha Yogas - Five great person yogas
 * 4. Nabhasa Yogas - Sky/pattern-based yogas
 * 5. Chandra Yogas - Moon-based combinations
 * 6. Solar Yogas - Sun-based combinations (Vesi, Vosi, Ubhayachari)
 * 7. Negative Yogas - Kemadruma, Daridra, etc.
 *
 * All calculations are based on:
 * - Brihat Parasara Hora Shastra (BPHS)
 * - Phaladeepika
 * - Saravali
 * - Jataka Parijata
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
object YogaCalculator {

    /**
     * Yoga category enumeration
     */
    enum class YogaCategory(val displayName: String, val description: String) {
        RAJA_YOGA("Raja Yoga", "Power, authority, and leadership combinations"),
        DHANA_YOGA("Dhana Yoga", "Wealth and prosperity combinations"),
        MAHAPURUSHA_YOGA("Pancha Mahapurusha Yoga", "Five great person combinations"),
        NABHASA_YOGA("Nabhasa Yoga", "Pattern-based planetary combinations"),
        CHANDRA_YOGA("Chandra Yoga", "Moon-based combinations"),
        SOLAR_YOGA("Solar Yoga", "Sun-based combinations"),
        NEGATIVE_YOGA("Negative Yoga", "Challenging combinations"),
        SPECIAL_YOGA("Special Yoga", "Other significant combinations");

        /**
         * Get localized display name
         */
        fun getLocalizedName(language: Language): String {
            val key = when (this) {
                RAJA_YOGA -> StringKey.YOGA_CAT_RAJA
                DHANA_YOGA -> StringKey.YOGA_CAT_DHANA
                MAHAPURUSHA_YOGA -> StringKey.YOGA_CAT_PANCHA_MAHAPURUSHA
                NABHASA_YOGA -> StringKey.YOGA_CAT_NABHASA
                CHANDRA_YOGA -> StringKey.YOGA_CAT_CHANDRA
                SOLAR_YOGA -> StringKey.YOGA_CAT_SOLAR
                NEGATIVE_YOGA -> StringKey.YOGA_CAT_NEGATIVE
                SPECIAL_YOGA -> StringKey.YOGA_CAT_SPECIAL
            }
            return StringResources.get(key, language)
        }

        /**
         * Get localized description
         */
        fun getLocalizedDescription(language: Language): String {
            val key = when (this) {
                RAJA_YOGA -> StringKey.YOGA_CAT_RAJA_DESC
                DHANA_YOGA -> StringKey.YOGA_CAT_DHANA_DESC
                MAHAPURUSHA_YOGA -> StringKey.YOGA_CAT_PANCHA_MAHAPURUSHA_DESC
                NABHASA_YOGA -> StringKey.YOGA_CAT_NABHASA_DESC
                CHANDRA_YOGA -> StringKey.YOGA_CAT_CHANDRA_DESC
                SOLAR_YOGA -> StringKey.YOGA_CAT_SOLAR_DESC
                NEGATIVE_YOGA -> StringKey.YOGA_CAT_NEGATIVE_DESC
                SPECIAL_YOGA -> StringKey.YOGA_CAT_SPECIAL_DESC
            }
            return StringResources.get(key, language)
        }
    }

    /**
     * Yoga strength level
     */
    enum class YogaStrength(val displayName: String, val value: Int) {
        EXTREMELY_STRONG("Extremely Strong", 5),
        STRONG("Strong", 4),
        MODERATE("Moderate", 3),
        WEAK("Weak", 2),
        VERY_WEAK("Very Weak", 1);

        /**
         * Get localized display name
         */
        fun getLocalizedName(language: Language): String {
            val key = when (this) {
                EXTREMELY_STRONG -> StringKey.YOGA_STRENGTH_EXTREMELY_STRONG
                STRONG -> StringKey.YOGA_STRENGTH_STRONG
                MODERATE -> StringKey.YOGA_STRENGTH_MODERATE
                WEAK -> StringKey.YOGA_STRENGTH_WEAK
                VERY_WEAK -> StringKey.YOGA_STRENGTH_VERY_WEAK
            }
            return StringResources.get(key, language)
        }
    }

    /**
     * Complete Yoga data class
     */
    data class Yoga(
        val name: String,
        val sanskritName: String,
        val category: YogaCategory,
        val planets: List<Planet>,
        val houses: List<Int>,
        val description: String,
        val effects: String,
        val strength: YogaStrength,
        val strengthPercentage: Double,
        val isAuspicious: Boolean,
        val activationPeriod: String,
        val cancellationFactors: List<String>
    )

    /**
     * Complete Yoga analysis result
     */
    data class YogaAnalysis(
        val chart: VedicChart,
        val allYogas: List<Yoga>,
        val rajaYogas: List<Yoga>,
        val dhanaYogas: List<Yoga>,
        val mahapurushaYogas: List<Yoga>,
        val nabhasaYogas: List<Yoga>,
        val chandraYogas: List<Yoga>,
        val solarYogas: List<Yoga>,
        val negativeYogas: List<Yoga>,
        val specialYogas: List<Yoga>,
        val dominantYogaCategory: YogaCategory,
        val overallYogaStrength: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun toPlainText(language: Language = Language.ENGLISH): String = buildString {
            val reportTitle = StringResources.get(StringKey.REPORT_YOGA_ANALYSIS, language)
            val totalYogasLabel = StringResources.get(StringKey.REPORT_TOTAL_YOGAS, language)
            val overallStrengthLabel = StringResources.get(StringKey.REPORT_OVERALL_STRENGTH, language)
            val dominantCategoryLabel = StringResources.get(StringKey.REPORT_DOMINANT_CATEGORY, language)
            val planetsLabel = StringResources.get(StringKey.REPORT_PLANETS, language)
            val housesLabel = StringResources.get(StringKey.REPORT_HOUSES, language)
            val strengthLabel = StringResources.get(StringKey.TAB_STRENGTH, language)
            val effectsLabel = StringResources.get(StringKey.REPORT_EFFECTS, language)
            val activationLabel = StringResources.get(StringKey.REPORT_ACTIVATION, language)
            val patternLabel = StringResources.get(StringKey.REPORT_PATTERN, language)
            val cancellationLabel = StringResources.get(StringKey.REPORT_CANCELLATION_FACTORS, language)
            val auspiciousText = StringResources.get(StringKey.REPORT_AUSPICIOUS, language)
            val inauspiciousText = StringResources.get(StringKey.REPORT_INAUSPICIOUS, language)

            appendLine("═══════════════════════════════════════════════════════════")
            appendLine("                    $reportTitle")
            appendLine("═══════════════════════════════════════════════════════════")
            appendLine()
            appendLine("$totalYogasLabel: ${allYogas.size}")
            appendLine("$overallStrengthLabel: ${String.format("%.1f", overallYogaStrength)}%")
            appendLine("$dominantCategoryLabel: ${dominantYogaCategory.getLocalizedName(language)}")
            appendLine()

            if (mahapurushaYogas.isNotEmpty()) {
                appendLine(YogaCategory.MAHAPURUSHA_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                mahapurushaYogas.forEach { yoga ->
                    appendLine("★ ${yoga.name} (${yoga.sanskritName})")
                    appendLine("  $planetsLabel: ${yoga.planets.joinToString { it.getLocalizedName(language) }}")
                    appendLine("  $strengthLabel: ${yoga.strength.getLocalizedName(language)} (${String.format("%.0f", yoga.strengthPercentage)}%)")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine()
                }
            }

            if (rajaYogas.isNotEmpty()) {
                appendLine(YogaCategory.RAJA_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                rajaYogas.forEach { yoga ->
                    appendLine("★ ${yoga.name} (${yoga.sanskritName})")
                    appendLine("  $planetsLabel: ${yoga.planets.joinToString { it.getLocalizedName(language) }}")
                    appendLine("  $housesLabel: ${yoga.houses.joinToString()}")
                    appendLine("  $strengthLabel: ${yoga.strength.getLocalizedName(language)} (${String.format("%.0f", yoga.strengthPercentage)}%)")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine("  $activationLabel: ${yoga.activationPeriod}")
                    appendLine()
                }
            }

            if (dhanaYogas.isNotEmpty()) {
                appendLine(YogaCategory.DHANA_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                dhanaYogas.forEach { yoga ->
                    appendLine("★ ${yoga.name} (${yoga.sanskritName})")
                    appendLine("  $planetsLabel: ${yoga.planets.joinToString { it.getLocalizedName(language) }}")
                    appendLine("  $strengthLabel: ${yoga.strength.getLocalizedName(language)}")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine()
                }
            }

            if (chandraYogas.isNotEmpty()) {
                appendLine(YogaCategory.CHANDRA_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                chandraYogas.forEach { yoga ->
                    val auspicious = if (yoga.isAuspicious) auspiciousText else inauspiciousText
                    appendLine("★ ${yoga.name} - $auspicious")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine()
                }
            }

            if (solarYogas.isNotEmpty()) {
                appendLine(YogaCategory.SOLAR_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                solarYogas.forEach { yoga ->
                    appendLine("★ ${yoga.name} (${yoga.sanskritName})")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine()
                }
            }

            if (nabhasaYogas.isNotEmpty()) {
                appendLine(YogaCategory.NABHASA_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                nabhasaYogas.forEach { yoga ->
                    appendLine("★ ${yoga.name} (${yoga.sanskritName})")
                    appendLine("  $patternLabel: ${yoga.description}")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine()
                }
            }

            if (negativeYogas.isNotEmpty()) {
                appendLine(YogaCategory.NEGATIVE_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                negativeYogas.forEach { yoga ->
                    appendLine("⚠ ${yoga.name} (${yoga.sanskritName})")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    if (yoga.cancellationFactors.isNotEmpty()) {
                        appendLine("  $cancellationLabel: ${yoga.cancellationFactors.joinToString("; ")}")
                    }
                    appendLine()
                }
            }

            if (specialYogas.isNotEmpty()) {
                appendLine(YogaCategory.SPECIAL_YOGA.getLocalizedName(language).uppercase())
                appendLine("─────────────────────────────────────────────────────────")
                specialYogas.forEach { yoga ->
                    appendLine("★ ${yoga.name}")
                    appendLine("  $effectsLabel: ${yoga.effects}")
                    appendLine()
                }
            }
        }
    }

    /**
     * Calculate all Yogas in a chart
     */
    fun calculateYogas(chart: VedicChart): YogaAnalysis {
        val allYogas = mutableListOf<Yoga>()

        // Calculate each category
        val rajaYogas = calculateRajaYogas(chart)
        val dhanaYogas = calculateDhanaYogas(chart)
        val mahapurushaYogas = calculatePanchaMahapurushaYogas(chart)
        val nabhasaYogas = calculateNabhasaYogas(chart)
        val chandraYogas = calculateChandraYogas(chart)
        val solarYogas = calculateSolarYogas(chart)
        val negativeYogas = calculateNegativeYogas(chart)
        val additionalYogas = calculateAdditionalYogas(chart)
        val specialYogas = calculateSpecialYogas(chart)

        allYogas.addAll(rajaYogas)
        allYogas.addAll(dhanaYogas)
        allYogas.addAll(mahapurushaYogas)
        allYogas.addAll(nabhasaYogas)
        allYogas.addAll(chandraYogas)
        allYogas.addAll(solarYogas)
        allYogas.addAll(negativeYogas)
        allYogas.addAll(additionalYogas)
        allYogas.addAll(specialYogas)

        // Calculate dominant category
        val categoryCount = mapOf(
            YogaCategory.RAJA_YOGA to rajaYogas.size,
            YogaCategory.DHANA_YOGA to dhanaYogas.size,
            YogaCategory.MAHAPURUSHA_YOGA to mahapurushaYogas.size,
            YogaCategory.NABHASA_YOGA to nabhasaYogas.size,
            YogaCategory.CHANDRA_YOGA to chandraYogas.size,
            YogaCategory.SOLAR_YOGA to solarYogas.size,
            YogaCategory.SPECIAL_YOGA to specialYogas.size
        )
        val dominantCategory = categoryCount.maxByOrNull { it.value }?.key ?: YogaCategory.SPECIAL_YOGA

        // Calculate overall strength
        val positiveYogas = allYogas.filter { it.isAuspicious }
        val negativeCount = negativeYogas.size
        val overallStrength = if (positiveYogas.isNotEmpty()) {
            val avgStrength = positiveYogas.map { it.strengthPercentage }.average()
            (avgStrength * (1.0 - (negativeCount * 0.1))).coerceIn(0.0, 100.0)
        } else 50.0

        return YogaAnalysis(
            chart = chart,
            allYogas = allYogas,
            rajaYogas = rajaYogas,
            dhanaYogas = dhanaYogas,
            mahapurushaYogas = mahapurushaYogas,
            nabhasaYogas = nabhasaYogas,
            chandraYogas = chandraYogas,
            solarYogas = solarYogas,
            negativeYogas = negativeYogas,
            specialYogas = specialYogas,
            dominantYogaCategory = dominantCategory,
            overallYogaStrength = overallStrength
        )
    }

    // ==================== RAJA YOGAS ====================

    /**
     * Calculate all Raja Yogas
     * Raja Yogas are formed by the association of Kendra lords (1,4,7,10) and Trikona lords (1,5,9)
     */
    private fun calculateRajaYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

        // Get house lords
        val houseLords = getHouseLords(ascendantSign)
        if (houseLords.isEmpty()) {
            // Log an error or handle the case where house lords cannot be determined.
            // For now, returning an empty list as no yogas can be calculated.
            return yogas
        }
        val kendraLords = listOf(houseLords[1], houseLords[4], houseLords[7], houseLords[10]).filterNotNull()
        val trikonaLords = listOf(houseLords[1], houseLords[5], houseLords[9]).filterNotNull()

        // 1. Kendra-Trikona Raja Yoga
        kendraLords.forEach { kendraLord ->
            trikonaLords.forEach { trikonaLord ->
                if (kendraLord != trikonaLord) {
                    val kendraPos = chart.planetPositions.find { it.planet == kendraLord }
                    val trikonaPos = chart.planetPositions.find { it.planet == trikonaLord }

                    if (kendraPos != null && trikonaPos != null) {
                        // Check for conjunction
                        if (areConjunct(kendraPos, trikonaPos)) {
                            val strength = calculateYogaStrength(chart, listOf(kendraPos, trikonaPos))
                            yogas.add(createKendraTrikonaRajaYoga(kendraLord, trikonaLord, "conjunction", strength, chart))
                        }

                        // Check for mutual aspect
                        if (areMutuallyAspecting(kendraPos, trikonaPos)) {
                            val strength = calculateYogaStrength(chart, listOf(kendraPos, trikonaPos)) * 0.8
                            yogas.add(createKendraTrikonaRajaYoga(kendraLord, trikonaLord, "aspect", strength, chart))
                        }

                        // Check for exchange (Parivartana)
                        if (areInExchange(kendraPos, trikonaPos)) {
                            val strength = calculateYogaStrength(chart, listOf(kendraPos, trikonaPos)) * 1.2
                            yogas.add(createParivartanaRajaYoga(kendraLord, trikonaLord, strength, chart))
                        }
                    }
                }
            }
        }

        // 2. Viparita Raja Yoga (lords of 6, 8, 12 in each other's houses)
        val dusthanaLords = listOf(houseLords[6], houseLords[8], houseLords[12]).filterNotNull()
        dusthanaLords.forEachIndexed { i, lord1 ->
            dusthanaLords.drop(i + 1).forEach { lord2 ->
                val pos1 = chart.planetPositions.find { it.planet == lord1 }
                val pos2 = chart.planetPositions.find { it.planet == lord2 }

                if (pos1 != null && pos2 != null) {
                    if (areInExchange(pos1, pos2) || areConjunct(pos1, pos2)) {
                        val strength = calculateYogaStrength(chart, listOf(pos1, pos2)) * 0.7
                        yogas.add(createViparitaRajaYoga(lord1, lord2, strength, chart))
                    }
                }
            }
        }

        // 3. Neecha Bhanga Raja Yoga
        chart.planetPositions.forEach { pos ->
            if (isDebilitated(pos)) {
                if (hasNeechaBhanga(pos, chart)) {
                    val strength = calculateYogaStrength(chart, listOf(pos))
                    yogas.add(createNeechaBhangaRajaYoga(pos.planet, strength, chart))
                }
            }
        }

        // 4. Maha Raja Yoga (specific powerful combinations)
        // Jupiter and Venus in Kendra from Moon
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        val venusPos = chart.planetPositions.find { it.planet == Planet.VENUS }

        if (moonPos != null && jupiterPos != null && venusPos != null) {
            if (isInKendraFrom(jupiterPos, moonPos) && isInKendraFrom(venusPos, moonPos)) {
                val strength = calculateYogaStrength(chart, listOf(jupiterPos, venusPos, moonPos))
                yogas.add(
                    Yoga(
                        name = "Maha Raja Yoga",
                        sanskritName = "Maha Raja Yoga",
                        category = YogaCategory.RAJA_YOGA,
                        planets = listOf(Planet.JUPITER, Planet.VENUS, Planet.MOON),
                        houses = listOf(jupiterPos.house, venusPos.house),
                        description = "Jupiter and Venus in Kendra from Moon",
                        effects = "Exceptional fortune, royal status, widespread fame, great wealth and power",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Jupiter and Venus Dashas",
                        cancellationFactors = emptyList()
                    )
                )
            }
        }

        return yogas
    }

    // ==================== DHANA YOGAS ====================

    /**
     * Calculate all Dhana (Wealth) Yogas
     */
    private fun calculateDhanaYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        val houseLords = getHouseLords(ascendantSign)

        // Dhana houses: 2 (wealth), 5 (past merit), 9 (fortune), 11 (gains)
        val dhanaHouses = listOf(2, 5, 9, 11)
        val dhanaLords = dhanaHouses.mapNotNull { houseLords[it] }

        // 1. Basic Dhana Yoga: Lords of 2, 5, 9, 11 in conjunction or exchange
        dhanaLords.forEachIndexed { i, lord1 ->
            dhanaLords.drop(i + 1).forEach { lord2 ->
                val pos1 = chart.planetPositions.find { it.planet == lord1 }
                val pos2 = chart.planetPositions.find { it.planet == lord2 }

                if (pos1 != null && pos2 != null && areConjunct(pos1, pos2)) {
                    val strength = calculateYogaStrength(chart, listOf(pos1, pos2))
                    yogas.add(
                        Yoga(
                            name = "${lord1.displayName}-${lord2.displayName} Dhana Yoga",
                            sanskritName = "Dhana Yoga",
                            category = YogaCategory.DHANA_YOGA,
                            planets = listOf(lord1, lord2),
                            houses = listOf(pos1.house, pos2.house),
                            description = "Lords of wealth houses in conjunction",
                            effects = "Wealth accumulation through ${getHouseSignifications(pos1.house)}",
                            strength = strengthFromPercentage(strength),
                            strengthPercentage = strength,
                            isAuspicious = true,
                            activationPeriod = "${lord1.displayName} or ${lord2.displayName} Dasha",
                            cancellationFactors = listOf("Combustion", "Debilitation without cancellation")
                        )
                    )
                }
            }
        }

        // 2. Lakshmi Yoga - Venus in own/exalted sign in Kendra/Trikona
        val venusPos = chart.planetPositions.find { it.planet == Planet.VENUS }
        if (venusPos != null) {
            val isStrong = isInOwnSign(venusPos) || isExalted(venusPos)
            val isGoodHouse = venusPos.house in listOf(1, 4, 5, 7, 9, 10)

            if (isStrong && isGoodHouse) {
                val strength = calculateYogaStrength(chart, listOf(venusPos)) * 1.2
                yogas.add(
                    Yoga(
                        name = "Lakshmi Yoga",
                        sanskritName = "Lakshmi Yoga",
                        category = YogaCategory.DHANA_YOGA,
                        planets = listOf(Planet.VENUS),
                        houses = listOf(venusPos.house),
                        description = "Venus in own/exalted sign in Kendra/Trikona",
                        effects = "Blessed by Goddess Lakshmi, abundant wealth, luxury, beauty, artistic success",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Venus Mahadasha and Antardashas",
                        cancellationFactors = listOf("Affliction by malefics")
                    )
                )
            }
        }

        // 3. Kubera Yoga - Jupiter in 2nd with Mercury
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        val mercuryPos = chart.planetPositions.find { it.planet == Planet.MERCURY }

        if (jupiterPos != null && mercuryPos != null) {
            if (jupiterPos.house == 2 && areConjunct(jupiterPos, mercuryPos)) {
                val strength = calculateYogaStrength(chart, listOf(jupiterPos, mercuryPos))
                yogas.add(
                    Yoga(
                        name = "Kubera Yoga",
                        sanskritName = "Kubera Yoga",
                        category = YogaCategory.DHANA_YOGA,
                        planets = listOf(Planet.JUPITER, Planet.MERCURY),
                        houses = listOf(2),
                        description = "Jupiter and Mercury in 2nd house",
                        effects = "Treasury of wealth like Kubera, excellent financial acumen, banking success",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Jupiter-Mercury periods",
                        cancellationFactors = emptyList()
                    )
                )
            }
        }

        // 4. Chandra-Mangala Yoga (Moon-Mars conjunction for wealth)
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        val marsPos = chart.planetPositions.find { it.planet == Planet.MARS }

        if (moonPos != null && marsPos != null && areConjunct(moonPos, marsPos)) {
            val strength = calculateYogaStrength(chart, listOf(moonPos, marsPos))
            yogas.add(
                Yoga(
                    name = "Chandra-Mangala Yoga",
                    sanskritName = "Chandra-Mangala Yoga",
                    category = YogaCategory.DHANA_YOGA,
                    planets = listOf(Planet.MOON, Planet.MARS),
                    houses = listOf(moonPos.house),
                    description = "Moon and Mars in conjunction",
                    effects = "Wealth through business, enterprise, real estate, aggressive financial pursuits",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Moon-Mars periods",
                    cancellationFactors = listOf("In 6th, 8th, or 12th house reduces results")
                )
            )
        }

        // 5. Dhana Yoga from 11th lord placement
        val lord11 = houseLords[11]
        if (lord11 != null) {
            val lord11Pos = chart.planetPositions.find { it.planet == lord11 }
            if (lord11Pos != null && lord11Pos.house in listOf(1, 2, 5, 9, 10, 11)) {
                val strength = calculateYogaStrength(chart, listOf(lord11Pos))
                yogas.add(
                    Yoga(
                        name = "Labha Yoga",
                        sanskritName = "Labha Yoga",
                        category = YogaCategory.DHANA_YOGA,
                        planets = listOf(lord11),
                        houses = listOf(lord11Pos.house),
                        description = "11th lord well-placed",
                        effects = "Continuous gains, fulfillment of desires, income through ${getHouseSignifications(lord11Pos.house)}",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "${lord11.displayName} Dasha",
                        cancellationFactors = emptyList()
                    )
                )
            }
        }

        return yogas
    }

    // ==================== PANCHA MAHAPURUSHA YOGAS ====================

    /**
     * Calculate Pancha Mahapurusha Yogas
     * Formed when Mars, Mercury, Jupiter, Venus, or Saturn is:
     * - In its own sign or exaltation
     * - In a Kendra house (1, 4, 7, 10)
     */
    private fun calculatePanchaMahapurushaYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val kendraHouses = listOf(1, 4, 7, 10)

        // Ruchaka Yoga - Mars
        val marsPos = chart.planetPositions.find { it.planet == Planet.MARS }
        if (marsPos != null && marsPos.house in kendraHouses) {
            if (marsPos.sign in listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO, ZodiacSign.CAPRICORN)) {
                val (strength, cancellations) = calculateMahapurushaStrengthWithReasons(marsPos, chart)
                yogas.add(
                    Yoga(
                        name = "Ruchaka Yoga",
                        sanskritName = "Ruchaka Mahapurusha Yoga",
                        category = YogaCategory.MAHAPURUSHA_YOGA,
                        planets = listOf(Planet.MARS),
                        houses = listOf(marsPos.house),
                        description = "Mars in own/exalted sign in Kendra",
                        effects = "Commander, army chief, valorous, muscular body, red complexion, successful in conflicts, " +
                                "skilled in warfare, leader of thieves or soldiers, wealth through martial arts or defense",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Mars Mahadasha and related Antardashas",
                        cancellationFactors = cancellations.ifEmpty { listOf("None - yoga is unafflicted") }
                    )
                )
            }
        }

        // Bhadra Yoga - Mercury
        val mercuryPos = chart.planetPositions.find { it.planet == Planet.MERCURY }
        if (mercuryPos != null && mercuryPos.house in kendraHouses) {
            if (mercuryPos.sign in listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO)) {
                val (strength, cancellations) = calculateMahapurushaStrengthWithReasons(mercuryPos, chart)
                yogas.add(
                    Yoga(
                        name = "Bhadra Yoga",
                        sanskritName = "Bhadra Mahapurusha Yoga",
                        category = YogaCategory.MAHAPURUSHA_YOGA,
                        planets = listOf(Planet.MERCURY),
                        houses = listOf(mercuryPos.house),
                        description = "Mercury in own/exalted sign in Kendra",
                        effects = "Intelligent, eloquent speaker, skilled in arts and sciences, long-lived, " +
                                "wealthy through intellect, respected in assemblies, lion-like face, broad chest",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Mercury Mahadasha and related Antardashas",
                        cancellationFactors = cancellations.ifEmpty { listOf("None - yoga is unafflicted") }
                    )
                )
            }
        }

        // Hamsa Yoga - Jupiter
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        if (jupiterPos != null && jupiterPos.house in kendraHouses) {
            if (jupiterPos.sign in listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES, ZodiacSign.CANCER)) {
                val (strength, cancellations) = calculateMahapurushaStrengthWithReasons(jupiterPos, chart)
                yogas.add(
                    Yoga(
                        name = "Hamsa Yoga",
                        sanskritName = "Hamsa Mahapurusha Yoga",
                        category = YogaCategory.MAHAPURUSHA_YOGA,
                        planets = listOf(Planet.JUPITER),
                        houses = listOf(jupiterPos.house),
                        description = "Jupiter in own/exalted sign in Kendra",
                        effects = "Righteous king, fair complexion, elevated nose, beautiful face, devoted to gods and brahmins, " +
                                "fond of water sports, walks like a swan, respected by rulers, spiritual inclinations",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Jupiter Mahadasha and related Antardashas",
                        cancellationFactors = cancellations.ifEmpty { listOf("None - yoga is unafflicted") }
                    )
                )
            }
        }

        // Malavya Yoga - Venus
        val venusPos = chart.planetPositions.find { it.planet == Planet.VENUS }
        if (venusPos != null && venusPos.house in kendraHouses) {
            if (venusPos.sign in listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA, ZodiacSign.PISCES)) {
                val (strength, cancellations) = calculateMahapurushaStrengthWithReasons(venusPos, chart)
                yogas.add(
                    Yoga(
                        name = "Malavya Yoga",
                        sanskritName = "Malavya Mahapurusha Yoga",
                        category = YogaCategory.MAHAPURUSHA_YOGA,
                        planets = listOf(Planet.VENUS),
                        houses = listOf(venusPos.house),
                        description = "Venus in own/exalted sign in Kendra",
                        effects = "Wealthy, enjoys all comforts, beautiful spouse, strong limbs, attractive face, " +
                                "blessed with vehicles and servants, learned in scriptures, lives up to 77 years",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Venus Mahadasha and related Antardashas",
                        cancellationFactors = cancellations.ifEmpty { listOf("None - yoga is unafflicted") }
                    )
                )
            }
        }

        // Sasa Yoga - Saturn
        val saturnPos = chart.planetPositions.find { it.planet == Planet.SATURN }
        if (saturnPos != null && saturnPos.house in kendraHouses) {
            if (saturnPos.sign in listOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS, ZodiacSign.LIBRA)) {
                val (strength, cancellations) = calculateMahapurushaStrengthWithReasons(saturnPos, chart)
                yogas.add(
                    Yoga(
                        name = "Sasa Yoga",
                        sanskritName = "Sasa Mahapurusha Yoga",
                        category = YogaCategory.MAHAPURUSHA_YOGA,
                        planets = listOf(Planet.SATURN),
                        houses = listOf(saturnPos.house),
                        description = "Saturn in own/exalted sign in Kendra",
                        effects = "Head of village/town/city, wicked disposition but good servants, intriguing nature, " +
                                "knows others' weaknesses, commands over masses, wealth through iron or labor",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Saturn Mahadasha and related Antardashas",
                        cancellationFactors = cancellations.ifEmpty { listOf("None - yoga is unafflicted") }
                    )
                )
            }
        }

        return yogas
    }

    // ==================== NABHASA YOGAS ====================

    /**
     * Calculate Nabhasa Yogas (Pattern-based)
     * These are based on the distribution of planets across signs/houses
     */
    private fun calculateNabhasaYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()

        // Get occupied houses
        val occupiedHouses = chart.planetPositions
            .filter { it.planet in Planet.MAIN_PLANETS }
            .map { it.house }
            .distinct()
            .sorted()

        val occupiedSigns = chart.planetPositions
            .filter { it.planet in Planet.MAIN_PLANETS }
            .map { it.sign }
            .distinct()

        // 1. Yava Yoga - All planets in houses 1 and 7 (or 4 and 10)
        if (occupiedHouses.all { it in listOf(1, 7) }) {
            yogas.add(createNabhasaYoga("Yava Yoga", "Yava Yoga",
                "All planets in 1st and 7th houses",
                "Medium wealth initially, prosperity in middle age, decline in old age"))
        }

        // 2. Shringataka Yoga - Planets in trines (1, 5, 9)
        if (occupiedHouses.all { it in listOf(1, 5, 9) }) {
            yogas.add(createNabhasaYoga("Shringataka Yoga", "Shringataka Yoga",
                "All planets in Trikona houses (1, 5, 9)",
                "Fond of quarrels initially, happiness in middle age, wandering in old age"))
        }

        // 3. Gada Yoga - Planets in two adjacent Kendras (1-4, 4-7, 7-10, 10-1)
        val kendras = listOf(1, 4, 7, 10)
        var gadaYogaFound = false
        for (i in kendras.indices) {
            val kendra1 = kendras[i]
            val kendra2 = kendras[(i + 1) % 4]
            // Check if planets are distributed between two adjacent kendras
            val hasInFirstKendra = occupiedHouses.any { it == kendra1 }
            val hasInSecondKendra = occupiedHouses.any { it == kendra2 }
            val allInTwoKendras = occupiedHouses.all { it == kendra1 || it == kendra2 }

            if (hasInFirstKendra && hasInSecondKendra && allInTwoKendras) {
                yogas.add(createNabhasaYoga("Gada Yoga", "Gada Yoga",
                    "Planets in two adjacent Kendra houses",
                    "Wealthy through ceremonies, always engaged in auspicious activities"))
                gadaYogaFound = true
                break
            }
        }

        // 4. Shakata Yoga - Planets in houses 1 and 7 only (lagna and 7th)
        val planetsInLagna = chart.planetPositions.count { it.house == 1 && it.planet in Planet.MAIN_PLANETS }
        val planetsIn7th = chart.planetPositions.count { it.house == 7 && it.planet in Planet.MAIN_PLANETS }
        if (planetsInLagna > 0 && planetsIn7th > 0 && occupiedHouses.all { it in listOf(1, 7) }) {
            yogas.add(createNabhasaYoga("Shakata Yoga", "Shakata Yoga",
                "All planets in 1st and 7th houses (like cart wheels)",
                "Fluctuating fortune, poverty followed by wealth in cycles"))
        }

        // 5. Rajju Yoga - All planets in movable signs
        val movableSigns = listOf(ZodiacSign.ARIES, ZodiacSign.CANCER, ZodiacSign.LIBRA, ZodiacSign.CAPRICORN)
        if (occupiedSigns.all { it in movableSigns }) {
            yogas.add(createNabhasaYoga("Rajju Yoga", "Rajju Yoga",
                "All planets in movable signs (Chara Rashi)",
                "Fond of travel, living in foreign lands, restless nature"))
        }

        // 6. Musala Yoga - All planets in fixed signs
        val fixedSigns = listOf(ZodiacSign.TAURUS, ZodiacSign.LEO, ZodiacSign.SCORPIO, ZodiacSign.AQUARIUS)
        if (occupiedSigns.all { it in fixedSigns }) {
            yogas.add(createNabhasaYoga("Musala Yoga", "Musala Yoga",
                "All planets in fixed signs (Sthira Rashi)",
                "Proud, wealthy, learned, famous, many children"))
        }

        // 7. Nala Yoga - All planets in dual signs
        val dualSigns = listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO, ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)
        if (occupiedSigns.all { it in dualSigns }) {
            yogas.add(createNabhasaYoga("Nala Yoga", "Nala Yoga",
                "All planets in dual signs (Dwiswabhava Rashi)",
                "Handsome, skilled in arts, wealthy through multiple sources"))
        }

        // 8. Kedara Yoga - All planets in 4 signs
        if (occupiedSigns.size == 4) {
            yogas.add(createNabhasaYoga("Kedara Yoga", "Kedara Yoga",
                "All planets in exactly 4 signs",
                "Agricultural wealth, helpful to others, truthful"))
        }

        // 9. Shoola Yoga - All planets in 3 signs
        if (occupiedSigns.size == 3) {
            yogas.add(createNabhasaYoga("Shoola Yoga", "Shoola Yoga",
                "All planets in exactly 3 signs",
                "Sharp intellect, quarrelsome, cruel, poor"))
        }

        // 10. Yuga Yoga - All planets in 2 signs
        if (occupiedSigns.size == 2) {
            yogas.add(createNabhasaYoga("Yuga Yoga", "Yuga Yoga",
                "All planets in exactly 2 signs",
                "Heretic, poor, rejected by family"))
        }

        // 11. Gola Yoga - All planets in 1 sign
        if (occupiedSigns.size == 1) {
            yogas.add(createNabhasaYoga("Gola Yoga", "Gola Yoga",
                "All planets in exactly 1 sign",
                "Poor, dirty, ignorant, idle"))
        }

        // 12. Veena Yoga - All planets in 7 signs
        if (occupiedSigns.size == 7) {
            yogas.add(createNabhasaYoga("Veena Yoga", "Veena Yoga",
                "All planets spread across 7 signs",
                "Fond of music, dance, leader, wealthy, happy"))
        }

        return yogas
    }

    // ==================== CHANDRA YOGAS ====================

    /**
     * Calculate Moon-based Yogas
     */
    private fun calculateChandraYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON } ?: return yogas

        // 1. Sunafa Yoga - Planet (except Sun) in 2nd from Moon
        val planetsIn2ndFromMoon = chart.planetPositions.filter {
            it.planet != Planet.SUN && it.planet != Planet.MOON &&
                    getHouseFrom(it.sign, moonPos.sign) == 2
        }
        if (planetsIn2ndFromMoon.isNotEmpty()) {
            val planets = planetsIn2ndFromMoon.map { it.planet }
            val strength = calculateYogaStrength(chart, planetsIn2ndFromMoon)
            yogas.add(
                Yoga(
                    name = "Sunafa Yoga",
                    sanskritName = "Sunafa Yoga",
                    category = YogaCategory.CHANDRA_YOGA,
                    planets = planets,
                    houses = planetsIn2ndFromMoon.map { it.house },
                    description = "${planets.joinToString { it.displayName }} in 2nd from Moon",
                    effects = "Self-made wealth, intelligent, good status, praised by kings",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Moon Dasha and related periods",
                    cancellationFactors = emptyList()
                )
            )
        }

        // 2. Anafa Yoga - Planet (except Sun) in 12th from Moon
        val planetsIn12thFromMoon = chart.planetPositions.filter {
            it.planet != Planet.SUN && it.planet != Planet.MOON &&
                    getHouseFrom(it.sign, moonPos.sign) == 12
        }
        if (planetsIn12thFromMoon.isNotEmpty()) {
            val planets = planetsIn12thFromMoon.map { it.planet }
            val strength = calculateYogaStrength(chart, planetsIn12thFromMoon)
            yogas.add(
                Yoga(
                    name = "Anafa Yoga",
                    sanskritName = "Anafa Yoga",
                    category = YogaCategory.CHANDRA_YOGA,
                    planets = planets,
                    houses = planetsIn12thFromMoon.map { it.house },
                    description = "${planets.joinToString { it.displayName }} in 12th from Moon",
                    effects = "Good reputation, health, happiness, self-respect",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Moon Dasha and related periods",
                    cancellationFactors = emptyList()
                )
            )
        }

        // 3. Durudhara Yoga - Planets in both 2nd and 12th from Moon
        if (planetsIn2ndFromMoon.isNotEmpty() && planetsIn12thFromMoon.isNotEmpty()) {
            val planets = (planetsIn2ndFromMoon + planetsIn12thFromMoon).map { it.planet }
            val strength = calculateYogaStrength(chart, planetsIn2ndFromMoon + planetsIn12thFromMoon)
            yogas.add(
                Yoga(
                    name = "Durudhara Yoga",
                    sanskritName = "Durudhara Yoga",
                    category = YogaCategory.CHANDRA_YOGA,
                    planets = planets,
                    houses = (planetsIn2ndFromMoon + planetsIn12thFromMoon).map { it.house },
                    description = "Planets on both sides of Moon (2nd and 12th)",
                    effects = "Highly fortunate, wealthy, vehicles, servants, charitable, enjoys life",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Moon Dasha",
                    cancellationFactors = emptyList()
                )
            )
        }

        // 4. Gaja-Kesari Yoga - Jupiter in Kendra from Moon
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        if (jupiterPos != null) {
            val houseFromMoon = getHouseFrom(jupiterPos.sign, moonPos.sign)
            if (houseFromMoon in listOf(1, 4, 7, 10)) {
                val strength = calculateYogaStrength(chart, listOf(jupiterPos, moonPos))
                yogas.add(
                    Yoga(
                        name = "Gaja-Kesari Yoga",
                        sanskritName = "Gaja-Kesari Yoga",
                        category = YogaCategory.CHANDRA_YOGA,
                        planets = listOf(Planet.JUPITER, Planet.MOON),
                        houses = listOf(jupiterPos.house, moonPos.house),
                        description = "Jupiter in Kendra (${houseFromMoon}th) from Moon",
                        effects = "Destroyer of enemies like lion, eloquent speaker, virtuous, long-lived, famous",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Jupiter and Moon Dashas",
                        cancellationFactors = listOf("Jupiter combust or debilitated")
                    )
                )
            }
        }

        // 5. Adhi Yoga - Benefics in 6, 7, 8 from Moon
        val beneficsFrom678 = chart.planetPositions.filter {
            it.planet in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY) &&
                    getHouseFrom(it.sign, moonPos.sign) in listOf(6, 7, 8)
        }
        if (beneficsFrom678.size >= 2) {
            val strength = calculateYogaStrength(chart, beneficsFrom678)
            yogas.add(
                Yoga(
                    name = "Adhi Yoga",
                    sanskritName = "Adhi Yoga",
                    category = YogaCategory.CHANDRA_YOGA,
                    planets = beneficsFrom678.map { it.planet },
                    houses = beneficsFrom678.map { it.house },
                    description = "Multiple benefics in 6th, 7th, 8th from Moon",
                    effects = "Commander, minister, or king; polite, trustworthy, healthy, wealthy, defeats enemies",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Benefic planet Dashas",
                    cancellationFactors = emptyList()
                )
            )
        }

        return yogas
    }

    // ==================== SOLAR YOGAS ====================

    /**
     * Calculate Sun-based Yogas
     */
    private fun calculateSolarYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val sunPos = chart.planetPositions.find { it.planet == Planet.SUN } ?: return yogas

        // Get planets in 2nd and 12th from Sun (excluding Moon)
        val planetsIn2ndFromSun = chart.planetPositions.filter {
            it.planet != Planet.MOON && it.planet != Planet.SUN &&
                    getHouseFrom(it.sign, sunPos.sign) == 2
        }
        val planetsIn12thFromSun = chart.planetPositions.filter {
            it.planet != Planet.MOON && it.planet != Planet.SUN &&
                    getHouseFrom(it.sign, sunPos.sign) == 12
        }

        // 1. Vesi Yoga - Planet in 2nd from Sun
        if (planetsIn2ndFromSun.isNotEmpty()) {
            val planets = planetsIn2ndFromSun.map { it.planet }
            val strength = calculateYogaStrength(chart, planetsIn2ndFromSun)
            val effects = if (planets.any { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY) }) {
                "Truthful, lazy, balanced perspective, learned, happy"
            } else {
                "Brave but may face challenges, determined"
            }
            yogas.add(
                Yoga(
                    name = "Vesi Yoga",
                    sanskritName = "Vesi Yoga",
                    category = YogaCategory.SOLAR_YOGA,
                    planets = planets,
                    houses = planetsIn2ndFromSun.map { it.house },
                    description = "${planets.joinToString { it.displayName }} in 2nd from Sun",
                    effects = effects,
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = planets.any { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY) },
                    activationPeriod = "Sun Dasha and related periods",
                    cancellationFactors = emptyList()
                )
            )
        }

        // 2. Vosi Yoga - Planet in 12th from Sun
        if (planetsIn12thFromSun.isNotEmpty()) {
            val planets = planetsIn12thFromSun.map { it.planet }
            val strength = calculateYogaStrength(chart, planetsIn12thFromSun)
            val effects = if (planets.any { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY) }) {
                "Learned, skilled, wealthy, charitable, famous"
            } else {
                "Active, may face expenditure, spiritually inclined"
            }
            yogas.add(
                Yoga(
                    name = "Vosi Yoga",
                    sanskritName = "Vosi Yoga",
                    category = YogaCategory.SOLAR_YOGA,
                    planets = planets,
                    houses = planetsIn12thFromSun.map { it.house },
                    description = "${planets.joinToString { it.displayName }} in 12th from Sun",
                    effects = effects,
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = planets.any { it in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY) },
                    activationPeriod = "Sun Dasha and related periods",
                    cancellationFactors = emptyList()
                )
            )
        }

        // 3. Ubhayachari Yoga - Planets on both sides of Sun
        if (planetsIn2ndFromSun.isNotEmpty() && planetsIn12thFromSun.isNotEmpty()) {
            val planets = (planetsIn2ndFromSun + planetsIn12thFromSun).map { it.planet }
            val strength = calculateYogaStrength(chart, planetsIn2ndFromSun + planetsIn12thFromSun)
            yogas.add(
                Yoga(
                    name = "Ubhayachari Yoga",
                    sanskritName = "Ubhayachari Yoga",
                    category = YogaCategory.SOLAR_YOGA,
                    planets = planets,
                    houses = (planetsIn2ndFromSun + planetsIn12thFromSun).map { it.house },
                    description = "Planets on both sides of Sun (2nd and 12th)",
                    effects = "King or equal to king, eloquent, handsome, all comforts",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Sun Dasha",
                    cancellationFactors = listOf("Only malefics flanking Sun reduces results")
                )
            )
        }

        return yogas
    }

    // ==================== NEGATIVE YOGAS ====================

    /**
     * Calculate Negative/Challenging Yogas
     */
    private fun calculateNegativeYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON } ?: return yogas

        // 1. Kemadruma Yoga - No planets in 2nd and 12th from Moon
        val planetsIn2ndFromMoon = chart.planetPositions.filter {
            it.planet != Planet.SUN && it.planet != Planet.MOON &&
                    it.planet in Planet.MAIN_PLANETS &&
                    getHouseFrom(it.sign, moonPos.sign) == 2
        }
        val planetsIn12thFromMoon = chart.planetPositions.filter {
            it.planet != Planet.SUN && it.planet != Planet.MOON &&
                    it.planet in Planet.MAIN_PLANETS &&
                    getHouseFrom(it.sign, moonPos.sign) == 12
        }

        if (planetsIn2ndFromMoon.isEmpty() && planetsIn12thFromMoon.isEmpty()) {
            // Check for cancellation factors
            val cancellations = mutableListOf<String>()

            // Cancellation 1: Moon in Kendra from Lagna
            if (moonPos.house in listOf(1, 4, 7, 10)) {
                cancellations.add("Moon in Kendra (${moonPos.house}th house)")
            }

            // Cancellation 2: Moon aspected by/conjunct Jupiter
            val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
            if (jupiterPos != null && (areConjunct(moonPos, jupiterPos) || areMutuallyAspecting(moonPos, jupiterPos))) {
                cancellations.add("Jupiter aspects/conjoins Moon")
            }

            // Cancellation 3: Planet in Kendra from Moon
            val planetsInKendraFromMoon = chart.planetPositions.filter {
                it.planet in Planet.MAIN_PLANETS && it.planet != Planet.MOON &&
                        getHouseFrom(it.sign, moonPos.sign) in listOf(1, 4, 7, 10)
            }
            if (planetsInKendraFromMoon.isNotEmpty()) {
                cancellations.add("Planet(s) in Kendra from Moon")
            }

            yogas.add(
                Yoga(
                    name = "Kemadruma Yoga",
                    sanskritName = "Kemadruma Yoga",
                    category = YogaCategory.NEGATIVE_YOGA,
                    planets = listOf(Planet.MOON),
                    houses = listOf(moonPos.house),
                    description = "No planets in 2nd and 12th from Moon",
                    effects = if (cancellations.isEmpty())
                        "Poverty, suffering, struggles, lack of support, lonely, menial work"
                    else
                        "Kemadruma effects significantly reduced due to cancellation factors",
                    strength = if (cancellations.isEmpty()) YogaStrength.STRONG else YogaStrength.WEAK,
                    strengthPercentage = if (cancellations.isEmpty()) 80.0 else 20.0,
                    isAuspicious = false,
                    activationPeriod = "Moon Dasha if uncancelled",
                    cancellationFactors = cancellations
                )
            )
        }

        // 2. Daridra Yoga - 11th lord in 6th, 8th, or 12th
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        val houseLords = getHouseLords(ascendantSign)
        val lord11 = houseLords[11]

        if (lord11 != null) {
            val lord11Pos = chart.planetPositions.find { it.planet == lord11 }
            if (lord11Pos != null && lord11Pos.house in listOf(6, 8, 12)) {
                yogas.add(
                    Yoga(
                        name = "Daridra Yoga",
                        sanskritName = "Daridra Yoga",
                        category = YogaCategory.NEGATIVE_YOGA,
                        planets = listOf(lord11),
                        houses = listOf(lord11Pos.house),
                        description = "11th lord (${lord11.displayName}) in ${lord11Pos.house}th house (Dusthana)",
                        effects = "Obstacles to gains, financial struggles, unfulfilled desires",
                        strength = YogaStrength.MODERATE,
                        strengthPercentage = 60.0,
                        isAuspicious = false,
                        activationPeriod = "${lord11.displayName} Dasha",
                        cancellationFactors = listOf("If aspected by Jupiter or lord is strong")
                    )
                )
            }
        }

        // 3. Graha Malika Yoga check for malefic version
        // If all planets are hemmed between malefics (Papakartari)

        // 4. Shakata Yoga (negative form) - Moon in 6, 8, 12 from Jupiter
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        if (jupiterPos != null) {
            val moonFromJupiter = getHouseFrom(moonPos.sign, jupiterPos.sign)
            if (moonFromJupiter in listOf(6, 8, 12)) {
                yogas.add(
                    Yoga(
                        name = "Shakata Yoga",
                        sanskritName = "Shakata Yoga",
                        category = YogaCategory.NEGATIVE_YOGA,
                        planets = listOf(Planet.MOON, Planet.JUPITER),
                        houses = listOf(moonPos.house, jupiterPos.house),
                        description = "Moon in ${moonFromJupiter}th from Jupiter",
                        effects = "Fluctuating fortune, periods of poverty alternating with wealth",
                        strength = YogaStrength.MODERATE,
                        strengthPercentage = 50.0,
                        isAuspicious = false,
                        activationPeriod = "Moon-Jupiter periods",
                        cancellationFactors = listOf("Moon in Kendra from Lagna", "Jupiter strong")
                    )
                )
            }
        }

        // 5. Guru-Chandal Yoga - Jupiter with Rahu
        val rahuPos = chart.planetPositions.find { it.planet == Planet.RAHU }
        if (jupiterPos != null && rahuPos != null && areConjunct(jupiterPos, rahuPos)) {
            yogas.add(
                Yoga(
                    name = "Guru-Chandal Yoga",
                    sanskritName = "Guru-Chandal Yoga",
                    category = YogaCategory.NEGATIVE_YOGA,
                    planets = listOf(Planet.JUPITER, Planet.RAHU),
                    houses = listOf(jupiterPos.house),
                    description = "Jupiter conjunct Rahu",
                    effects = "Unorthodox beliefs, breaks from tradition, possible disgrace through teachers/religion",
                    strength = YogaStrength.MODERATE,
                    strengthPercentage = 65.0,
                    isAuspicious = false,
                    activationPeriod = "Jupiter-Rahu periods",
                    cancellationFactors = listOf("Jupiter in own/exalted sign", "Aspect from benefics")
                )
            )
        }

        // 6. Shakat (Cart) Yoga check
        // Already covered in Nabhasa but adding here for completeness if applicable

        return yogas
    }

    // ==================== ADDITIONAL IMPORTANT YOGAS ====================

    /**
     * Calculate additional important yoga types not covered in other categories
     * This includes Parivartana variations, Dasa-Mula, and other significant combinations
     */
    private fun calculateAdditionalYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()

        // 1. Dasa-Mula Yoga - Birth Nakshatra in 8th from Moon
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        if (moonPos != null) {
            val dasaMulaNakshatras = listOf(
                Nakshatra.ASHWINI,
                Nakshatra.DHANISHTHA,
                Nakshatra.MULA,
                Nakshatra.REVATI
            )
            val (birthNakshatra, _) = Nakshatra.fromLongitude(moonPos.longitude)
            if (birthNakshatra in dasaMulaNakshatras) {
                yogas.add(
                    Yoga(
                        name = "Dasa-Mula Yoga",
                        sanskritName = "Dasa-Mula Yoga",
                        category = YogaCategory.NEGATIVE_YOGA,
                        planets = listOf(Planet.MOON),
                        houses = listOf(moonPos.house),
                        description = "Birth in Dasa-Mula Nakshatra (${birthNakshatra.displayName})",
                        effects = "Requires mitigation; obstacles in early life, need for protective measures",
                        strength = YogaStrength.MODERATE,
                        strengthPercentage = 60.0,
                        isAuspicious = false,
                        activationPeriod = "Early life period",
                        cancellationFactors = listOf("Jupiter aspect", "Lord of nakshatra strong", "Benefic in 4th/7th")
                    )
                )
            }
        }

        // 2. Vargottama Yogas - Planets in same sign in Rashi and Navamsa
        // This checks for Rashi-Navamsa alignment for strength
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        chart.planetPositions.forEach { pos ->
            // Simplified vargottama check - planet's rashi lord strong indicator
            if (pos.planet in Planet.MAIN_PLANETS && (isInOwnSign(pos) || isExalted(pos))) {
                val strength = calculateYogaStrength(chart, listOf(pos)) * 1.1
                yogas.add(
                    Yoga(
                        name = "${pos.planet.displayName} Vargottama Strength",
                        sanskritName = "Vargottama Bala",
                        category = YogaCategory.SPECIAL_YOGA,
                        planets = listOf(pos.planet),
                        houses = listOf(pos.house),
                        description = "${pos.planet.displayName} in own/exalted sign",
                        effects = "Exceptional strength, power, and effectiveness in the planet's significations",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "${pos.planet.displayName} Dasha",
                        cancellationFactors = emptyList()
                    )
                )
            }
        }

        return yogas
    }

    // ==================== SPECIAL YOGAS ====================

    /**
     * Calculate other special Yogas
     */
    private fun calculateSpecialYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()

        // 1. Budha-Aditya Yoga - Sun-Mercury conjunction
        val sunPos = chart.planetPositions.find { it.planet == Planet.SUN }
        val mercuryPos = chart.planetPositions.find { it.planet == Planet.MERCURY }

        if (sunPos != null && mercuryPos != null && areConjunct(sunPos, mercuryPos, customOrb = 6.0)) {
            // Mercury combustion threshold is tighter (12-14° as per classics)
            val distance = abs(sunPos.longitude - mercuryPos.longitude)
            val normalizedDistance = if (distance > 180) 360 - distance else distance
            val isCombust = normalizedDistance < 12.0

            val strength = if (isCombust) 45.0 else calculateYogaStrength(chart, listOf(sunPos, mercuryPos))
            yogas.add(
                Yoga(
                    name = "Budha-Aditya Yoga",
                    sanskritName = "Budha-Aditya Yoga",
                    category = YogaCategory.SPECIAL_YOGA,
                    planets = listOf(Planet.SUN, Planet.MERCURY),
                    houses = listOf(sunPos.house),
                    description = "Sun and Mercury in conjunction",
                    effects = "Intelligence, skilled in many arts, famous, sweet speech, scholarly",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Sun and Mercury Dashas",
                    cancellationFactors = if (isCombust) listOf("Mercury is combust - effects reduced") else emptyList()
                )
            )
        }

        // 2. Amala Yoga - Natural benefic in 10th from Lagna or Moon
        val benefics = chart.planetPositions.filter {
            it.planet in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
        }
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }

        benefics.forEach { beneficPos ->
            if (beneficPos.house == 10 || (moonPos != null && getHouseFrom(beneficPos.sign, moonPos.sign) == 10)) {
                val strength = calculateYogaStrength(chart, listOf(beneficPos))
                yogas.add(
                    Yoga(
                        name = "${beneficPos.planet.displayName} Amala Yoga",
                        sanskritName = "Amala Yoga",
                        category = YogaCategory.SPECIAL_YOGA,
                        planets = listOf(beneficPos.planet),
                        houses = listOf(10),
                        description = "${beneficPos.planet.displayName} (benefic) in 10th house",
                        effects = "Pure character, lasting fame, prosperous, ethical conduct, respected by rulers",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "${beneficPos.planet.displayName} Dasha",
                        cancellationFactors = emptyList()
                    )
                )
            }
        }

        // 3. Saraswati Yoga - Jupiter, Venus, Mercury in Kendra/Trikona, Jupiter strong
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        val venusPos = chart.planetPositions.find { it.planet == Planet.VENUS }

        if (jupiterPos != null && venusPos != null && mercuryPos != null) {
            val goodHouses = listOf(1, 4, 5, 7, 9, 10)
            val allInGoodHouses = listOf(jupiterPos, venusPos, mercuryPos).all { it.house in goodHouses }
            val jupiterStrong = isInOwnSign(jupiterPos) || isExalted(jupiterPos) || jupiterPos.house in listOf(1, 4, 5, 7, 9, 10)

            if (allInGoodHouses && jupiterStrong) {
                val strength = calculateYogaStrength(chart, listOf(jupiterPos, venusPos, mercuryPos))
                yogas.add(
                    Yoga(
                        name = "Saraswati Yoga",
                        sanskritName = "Saraswati Yoga",
                        category = YogaCategory.SPECIAL_YOGA,
                        planets = listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY),
                        houses = listOf(jupiterPos.house, venusPos.house, mercuryPos.house),
                        description = "Jupiter, Venus, Mercury well-placed with Jupiter strong",
                        effects = "Highly learned, poet, prose writer, famous speaker, skilled in all arts",
                        strength = strengthFromPercentage(strength),
                        strengthPercentage = strength,
                        isAuspicious = true,
                        activationPeriod = "Jupiter, Venus, Mercury periods",
                        cancellationFactors = emptyList()
                    )
                )
            }
        }

        // 4. Parvata Yoga - Benefics in Kendras, no malefics in Kendras
        val kendras = listOf(1, 4, 7, 10)
        val beneficsInKendras = chart.planetPositions.filter {
            it.planet in listOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY) && it.house in kendras
        }
        val maleficsInKendras = chart.planetPositions.filter {
            it.planet in listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU) && it.house in kendras
        }

        if (beneficsInKendras.isNotEmpty() && maleficsInKendras.isEmpty()) {
            val strength = calculateYogaStrength(chart, beneficsInKendras)
            yogas.add(
                Yoga(
                    name = "Parvata Yoga",
                    sanskritName = "Parvata Yoga",
                    category = YogaCategory.SPECIAL_YOGA,
                    planets = beneficsInKendras.map { it.planet },
                    houses = beneficsInKendras.map { it.house },
                    description = "Only benefics in Kendra houses",
                    effects = "King or minister, famous, generous, wealthy, charitable, mountain-like stability",
                    strength = strengthFromPercentage(strength),
                    strengthPercentage = strength,
                    isAuspicious = true,
                    activationPeriod = "Benefic planet Dashas",
                    cancellationFactors = emptyList()
                )
            )
        }

        // 5. Kahala Yoga - Lords of 4th and 9th strong and connected
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        val houseLords = getHouseLords(ascendantSign)
        val lord4 = houseLords[4]
        val lord9 = houseLords[9]

        if (lord4 != null && lord9 != null) {
            val lord4Pos = chart.planetPositions.find { it.planet == lord4 }
            val lord9Pos = chart.planetPositions.find { it.planet == lord9 }

            if (lord4Pos != null && lord9Pos != null) {
                if (areConjunct(lord4Pos, lord9Pos) || areMutuallyAspecting(lord4Pos, lord9Pos) || areInExchange(lord4Pos, lord9Pos)) {
                    val strength = calculateYogaStrength(chart, listOf(lord4Pos, lord9Pos))
                    yogas.add(
                        Yoga(
                            name = "Kahala Yoga",
                            sanskritName = "Kahala Yoga",
                            category = YogaCategory.SPECIAL_YOGA,
                            planets = listOf(lord4, lord9),
                            houses = listOf(lord4Pos.house, lord9Pos.house),
                            description = "Lords of 4th and 9th connected",
                            effects = "Bold, energetic, leads armies, stubborn, wealthy, fortunate",
                            strength = strengthFromPercentage(strength),
                            strengthPercentage = strength,
                            isAuspicious = true,
                            activationPeriod = "${lord4.displayName} and ${lord9.displayName} Dashas",
                            cancellationFactors = emptyList()
                        )
                    )
                }
            }
        }

        return yogas
    }

    // ==================== COMBUSTION & AFFLICTION DETECTION ====================

    /**
     * Combustion orbs based on BPHS and Saravali:
     * - Moon: 12° (but considers phase - dark moon is stronger)
     * - Mars: 17°
     * - Mercury: 14° (direct) / 12° (retrograde - considered less combust)
     * - Jupiter: 11°
     * - Venus: 10° (direct) / 8° (retrograde)
     * - Saturn: 15°
     *
     * Planets within these orbs from Sun are considered combust (Asta).
     * Combustion significantly weakens a planet's ability to deliver yoga results.
     */
    private fun getCombustionOrb(planet: Planet, isRetrograde: Boolean): Double {
        return when (planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> if (isRetrograde) 12.0 else 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> if (isRetrograde) 8.0 else 10.0
            Planet.SATURN -> 15.0
            else -> 0.0 // Rahu/Ketu cannot be combust
        }
    }

    /**
     * Check if a planet is combust (Asta) based on Vedic combustion rules.
     * Returns a combustion factor between 0.0 (fully combust) and 1.0 (not combust)
     * that can be used to reduce yoga strength.
     */
    private fun getCombustionFactor(pos: PlanetPosition, chart: VedicChart): Double {
        if (pos.planet == Planet.SUN || pos.planet in listOf(Planet.RAHU, Planet.KETU)) {
            return 1.0 // Sun, Rahu, Ketu cannot be combust
        }

        val sunPos = chart.planetPositions.find { it.planet == Planet.SUN } ?: return 1.0

        val distance = abs(pos.longitude - sunPos.longitude)
        val normalizedDistance = if (distance > 180) 360 - distance else distance

        val combustionOrb = getCombustionOrb(pos.planet, pos.isRetrograde)

        if (normalizedDistance >= combustionOrb) {
            return 1.0 // Not combust
        }

        // Deep combustion (within 3°) - severely weakens the planet
        if (normalizedDistance <= 3.0) {
            return 0.2 // 80% reduction
        }

        // Calculate gradual combustion factor
        // Closer to Sun = more combust = lower factor
        val combustionDepth = 1.0 - (normalizedDistance / combustionOrb)
        return 1.0 - (combustionDepth * 0.6) // Max 60% reduction at orb edge
    }

    /**
     * Check if a planet is under Papakartari Yoga (hemmed between malefics).
     * This severely restricts a planet's positive effects.
     */
    private fun isPapakartari(pos: PlanetPosition, chart: VedicChart): Boolean {
        val malefics = listOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU, Planet.SUN)

        val house = pos.house
        val prevHouse = if (house == 1) 12 else house - 1
        val nextHouse = if (house == 12) 1 else house + 1

        val hasMaleficBefore = chart.planetPositions.any {
            it.planet in malefics && it.house == prevHouse
        }
        val hasMaleficAfter = chart.planetPositions.any {
            it.planet in malefics && it.house == nextHouse
        }

        return hasMaleficBefore && hasMaleficAfter
    }

    /**
     * Check if planet is afflicted by malefic aspects using proper Vedic aspects.
     * Returns affliction factor between 0.0 (severely afflicted) and 1.0 (not afflicted)
     *
     * Vedic Aspect Rules:
     * - All planets aspect the 7th house from their position (opposition)
     * - Mars additionally aspects 4th and 8th houses
     * - Jupiter additionally aspects 5th and 9th houses
     * - Saturn additionally aspects 3rd and 10th houses
     * - Rahu/Ketu aspect like Saturn according to some authorities
     */
    private fun getMaleficAfflictionFactor(pos: PlanetPosition, chart: VedicChart): Double {
        val malefics = mapOf(
            Planet.SATURN to 0.25,  // Saturn aspect is most restrictive
            Planet.MARS to 0.20,    // Mars aspect causes aggression/conflicts
            Planet.RAHU to 0.18,    // Rahu creates illusion/obsession
            Planet.KETU to 0.12,    // Ketu creates detachment/confusion
            Planet.SUN to 0.08      // Sun's malefic aspect is milder (ego clashes)
        )

        var totalAffliction = 0.0

        malefics.forEach { (malefic, strength) ->
            if (malefic == pos.planet) return@forEach // Planet can't afflict itself

            val maleficPos = chart.planetPositions.find { it.planet == malefic } ?: return@forEach

            if (isAspecting(maleficPos, pos)) {
                totalAffliction += strength
            }
        }

        // Cap total affliction at 60% reduction
        return (1.0 - min(totalAffliction, 0.6))
    }

    /**
     * Check if aspectingPlanet aspects targetPlanet using Vedic aspect rules.
     * Uses 5° orb for aspect calculations.
     */
    private fun isAspecting(aspectingPlanet: PlanetPosition, targetPlanet: PlanetPosition): Boolean {
        val aspectOrb = 5.0 // Degrees of orb for aspects

        // Calculate house distance (1-12)
        val houseDistance = getHouseFrom(targetPlanet.sign, aspectingPlanet.sign)

        // All planets have 7th house (opposition) aspect
        if (houseDistance == 7) {
            return isWithinAspectOrb(aspectingPlanet.longitude, targetPlanet.longitude, 180.0, aspectOrb)
        }

        // Mars special aspects: 4th and 8th houses
        if (aspectingPlanet.planet == Planet.MARS) {
            if (houseDistance == 4 || houseDistance == 8) {
                val expectedAngle = if (houseDistance == 4) 90.0 else 210.0
                return isWithinAspectOrb(aspectingPlanet.longitude, targetPlanet.longitude, expectedAngle, aspectOrb)
            }
        }

        // Jupiter special aspects: 5th and 9th houses
        if (aspectingPlanet.planet == Planet.JUPITER) {
            if (houseDistance == 5 || houseDistance == 9) {
                val expectedAngle = if (houseDistance == 5) 120.0 else 240.0
                return isWithinAspectOrb(aspectingPlanet.longitude, targetPlanet.longitude, expectedAngle, aspectOrb)
            }
        }

        // Saturn special aspects: 3rd and 10th houses
        if (aspectingPlanet.planet == Planet.SATURN) {
            if (houseDistance == 3 || houseDistance == 10) {
                val expectedAngle = if (houseDistance == 3) 60.0 else 270.0
                return isWithinAspectOrb(aspectingPlanet.longitude, targetPlanet.longitude, expectedAngle, aspectOrb)
            }
        }

        // Rahu/Ketu aspects like Saturn (some traditions)
        if (aspectingPlanet.planet in listOf(Planet.RAHU, Planet.KETU)) {
            if (houseDistance == 3 || houseDistance == 10) {
                return true // Using sign-based aspect for nodes
            }
        }

        return false
    }

    /**
     * Check if two longitudes are within orb of an expected aspect angle.
     */
    private fun isWithinAspectOrb(
        long1: Double,
        long2: Double,
        expectedAngle: Double,
        orb: Double
    ): Boolean {
        val actualAngle = abs(long1 - long2)
        val normalizedAngle = if (actualAngle > 180) 360 - actualAngle else actualAngle
        return abs(normalizedAngle - expectedAngle) <= orb
    }

    /**
     * Check if planet receives benefic aspect (Shubha Drishti).
     * Returns a boost factor between 1.0 (no benefic aspect) and 1.3 (strong benefic aspects)
     */
    private fun getBeneficAspectBoost(pos: PlanetPosition, chart: VedicChart): Double {
        val benefics = mapOf(
            Planet.JUPITER to 0.15,  // Jupiter aspect is most beneficial
            Planet.VENUS to 0.10,    // Venus aspect adds comfort/harmony
            Planet.MERCURY to 0.08,  // Mercury aspect adds intelligence (if not afflicted)
            Planet.MOON to 0.05      // Moon aspect adds emotional support (if waxing)
        )

        var totalBoost = 0.0

        benefics.forEach { (benefic, strength) ->
            if (benefic == pos.planet) return@forEach

            val beneficPos = chart.planetPositions.find { it.planet == benefic } ?: return@forEach

            // Skip Moon if waning (weak)
            if (benefic == Planet.MOON) {
                val moonStrength = getMoonPhaseStrength(beneficPos, chart)
                if (moonStrength < 0.5) return@forEach
            }

            // Skip Mercury if combust
            if (benefic == Planet.MERCURY) {
                val combustionFactor = getCombustionFactor(beneficPos, chart)
                if (combustionFactor < 0.6) return@forEach
            }

            if (isAspecting(beneficPos, pos)) {
                totalBoost += strength
            }
        }

        return 1.0 + min(totalBoost, 0.3) // Max 30% boost
    }

    /**
     * Calculate Moon phase strength (Paksha Bala).
     * Returns 0.0-1.0 where 1.0 is full moon and 0.0 is new moon.
     */
    private fun getMoonPhaseStrength(moonPos: PlanetPosition, chart: VedicChart): Double {
        val sunPos = chart.planetPositions.find { it.planet == Planet.SUN } ?: return 0.5

        val distance = (moonPos.longitude - sunPos.longitude + 360) % 360

        // 0° = New Moon, 180° = Full Moon
        return if (distance <= 180) {
            distance / 180.0 // Waxing: 0 to 1
        } else {
            (360 - distance) / 180.0 // Waning: 1 to 0
        }
    }

    /**
     * Calculate the net strength modification factor for a yoga considering all cancellation factors.
     * This is the core function that applies cancellation logic to yoga strength.
     *
     * Factors considered:
     * 1. Combustion (Asta) - proximity to Sun
     * 2. Papakartari - hemmed between malefics
     * 3. Malefic aspects - Saturn, Mars, Rahu aspects
     * 4. Debilitation - planet in fall
     * 5. Enemy sign placement
     * 6. Benefic aspects (positive boost)
     * 7. Strength of yoga-forming planets
     */
    private fun calculateCancellationFactor(
        positions: List<PlanetPosition>,
        chart: VedicChart
    ): Pair<Double, List<String>> {
        val cancellationFactors = mutableListOf<String>()
        var netFactor = 1.0

        positions.forEach { pos ->
            // 1. Check combustion
            val combustionFactor = getCombustionFactor(pos, chart)
            if (combustionFactor < 0.9) {
                netFactor *= combustionFactor
                if (combustionFactor < 0.5) {
                    cancellationFactors.add("${pos.planet.displayName} is deeply combust")
                } else if (combustionFactor < 0.8) {
                    cancellationFactors.add("${pos.planet.displayName} is combust")
                }
            }

            // 2. Check Papakartari
            if (isPapakartari(pos, chart)) {
                netFactor *= 0.7 // 30% reduction
                cancellationFactors.add("${pos.planet.displayName} hemmed between malefics")
            }

            // 3. Check malefic aspects
            val afflictionFactor = getMaleficAfflictionFactor(pos, chart)
            if (afflictionFactor < 0.9) {
                netFactor *= afflictionFactor
                if (afflictionFactor < 0.7) {
                    cancellationFactors.add("${pos.planet.displayName} severely afflicted by malefics")
                }
            }

            // 4. Check debilitation (extra reduction if in enemy sign after debilitation)
            if (isDebilitated(pos)) {
                if (!hasNeechaBhanga(pos, chart)) {
                    netFactor *= 0.5 // 50% reduction for uncancelled debilitation
                    cancellationFactors.add("${pos.planet.displayName} debilitated without cancellation")
                }
            }

            // 5. Check enemy sign (Shatru Kshetra)
            if (isInEnemySign(pos)) {
                netFactor *= 0.85 // 15% reduction
                cancellationFactors.add("${pos.planet.displayName} in enemy sign")
            }

            // 6. Benefic aspect boost (positive factor)
            val beneficBoost = getBeneficAspectBoost(pos, chart)
            if (beneficBoost > 1.0) {
                netFactor *= beneficBoost
            }
        }

        return Pair(netFactor.coerceIn(0.1, 1.5), cancellationFactors)
    }

    /**
     * Check if planet is in enemy sign based on natural friendship.
     * Vedic planetary friendships (Naisargika Maitri):
     */
    private fun isInEnemySign(pos: PlanetPosition): Boolean {
        val enemies = getEnemies(pos.planet)
        return pos.sign.ruler in enemies
    }

    /**
     * Get natural enemies of a planet based on BPHS.
     */
    private fun getEnemies(planet: Planet): List<Planet> {
        return when (planet) {
            Planet.SUN -> listOf(Planet.SATURN, Planet.VENUS)
            Planet.MOON -> emptyList() // Moon has no natural enemies
            Planet.MARS -> listOf(Planet.MERCURY)
            Planet.MERCURY -> listOf(Planet.MOON)
            Planet.JUPITER -> listOf(Planet.MERCURY, Planet.VENUS)
            Planet.VENUS -> listOf(Planet.SUN, Planet.MOON)
            Planet.SATURN -> listOf(Planet.SUN, Planet.MOON, Planet.MARS)
            Planet.RAHU -> listOf(Planet.SUN, Planet.MOON)
            Planet.KETU -> listOf(Planet.SUN, Planet.MOON)
            else -> emptyList()
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun getHouseLords(ascendantSign: ZodiacSign): Map<Int, Planet> {
        val lords = mutableMapOf<Int, Planet>()
        for (house in 1..12) {
            val signIndex = (ascendantSign.ordinal + house - 1) % 12
            val sign = ZodiacSign.entries[signIndex]
            lords[house] = sign.ruler
        }
        return lords
    }

    private fun areConjunct(pos1: PlanetPosition, pos2: PlanetPosition, customOrb: Double? = null): Boolean {
        // Use precise orb-based conjunction detection (within 8° as per Vedic astrology standards)
        val distance = abs(pos1.longitude - pos2.longitude)
        val normalizedDistance = if (distance > 180) 360 - distance else distance

        // Conjunction orb: typically 8° in Vedic astrology for accurate detection
        // Can be customized for specific planetary combinations (Mercury conjunctions closer, etc.)
        val orb = customOrb ?: 8.0
        return normalizedDistance <= orb
    }

    private fun areMutuallyAspecting(pos1: PlanetPosition, pos2: PlanetPosition): Boolean {
        val angle = abs(pos1.longitude - pos2.longitude)
        val normalizedAngle = if (angle > 180) 360 - angle else angle
        // Check for opposition (180°) or special aspects
        return normalizedAngle in 170.0..190.0
    }

    private fun areInExchange(pos1: PlanetPosition, pos2: PlanetPosition): Boolean {
        return pos1.sign.ruler == pos2.planet && pos2.sign.ruler == pos1.planet
    }

    private fun isInKendraFrom(pos: PlanetPosition, reference: PlanetPosition): Boolean {
        val house = getHouseFrom(pos.sign, reference.sign)
        return house in listOf(1, 4, 7, 10)
    }

    private fun getHouseFrom(targetSign: ZodiacSign, referenceSign: ZodiacSign): Int {
        val diff = targetSign.number - referenceSign.number
        return if (diff >= 0) diff + 1 else diff + 13
    }

    private fun isInOwnSign(pos: PlanetPosition): Boolean {
        return pos.sign.ruler == pos.planet
    }

    private fun isExalted(pos: PlanetPosition): Boolean {
        return when (pos.planet) {
            Planet.SUN -> pos.sign == ZodiacSign.ARIES
            Planet.MOON -> pos.sign == ZodiacSign.TAURUS
            Planet.MARS -> pos.sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> pos.sign == ZodiacSign.VIRGO
            Planet.JUPITER -> pos.sign == ZodiacSign.CANCER
            Planet.VENUS -> pos.sign in listOf(ZodiacSign.PISCES, ZodiacSign.TAURUS)
            Planet.SATURN -> pos.sign == ZodiacSign.LIBRA
            else -> false
        }
    }

    private fun isDebilitated(pos: PlanetPosition): Boolean {
        return when (pos.planet) {
            Planet.SUN -> pos.sign == ZodiacSign.LIBRA
            Planet.MOON -> pos.sign == ZodiacSign.SCORPIO
            Planet.MARS -> pos.sign == ZodiacSign.CANCER
            Planet.MERCURY -> pos.sign == ZodiacSign.PISCES
            Planet.JUPITER -> pos.sign == ZodiacSign.CAPRICORN
            Planet.VENUS -> pos.sign == ZodiacSign.VIRGO
            Planet.SATURN -> pos.sign == ZodiacSign.ARIES
            else -> false
        }
    }

    private fun hasNeechaBhanga(pos: PlanetPosition, chart: VedicChart): Boolean {
        // Neecha Bhanga conditions:
        // 1. Lord of debilitation sign aspects the debilitated planet
        // 2. Lord of exaltation sign aspects the debilitated planet
        // 3. Debilitated planet is in Kendra from Lagna or Moon
        // 4. Lord of the sign where planet is debilitated is in Kendra from Lagna or Moon

        // Check condition 3
        if (pos.house in listOf(1, 4, 7, 10)) return true

        // Check condition 4
        val debilitatedSignLord = pos.sign.ruler
        val lordPos = chart.planetPositions.find { it.planet == debilitatedSignLord }
        if (lordPos != null && lordPos.house in listOf(1, 4, 7, 10)) return true

        return false
    }

    /**
     * Calculate yoga strength with comprehensive cancellation logic applied.
     * This is the main strength calculation that integrates all Vedic factors.
     *
     * Base strength factors:
     * - Exaltation: +15%
     * - Own sign (Swakshetra): +12%
     * - Friend's sign (Mitra Kshetra): +6%
     * - Kendra/Trikona placement: +8%
     * - Debilitation: -15% (before cancellation check)
     * - Dusthana placement (6,8,12): -10%
     * - Retrograde benefics: +5% (considered stronger)
     *
     * Then applies cancellation factors from calculateCancellationFactor()
     */
    private fun calculateYogaStrength(chart: VedicChart, positions: List<PlanetPosition>): Double {
        var baseStrength = 50.0

        positions.forEach { pos ->
            // Add strength for exaltation
            if (isExalted(pos)) baseStrength += 15.0

            // Add strength for own sign
            if (isInOwnSign(pos)) baseStrength += 12.0

            // Add strength for friend's sign
            if (isInFriendSign(pos)) baseStrength += 6.0

            // Add strength for good houses (Kendra/Trikona)
            if (pos.house in listOf(1, 4, 5, 7, 9, 10)) baseStrength += 8.0

            // Add for 2nd and 11th (wealth houses)
            if (pos.house in listOf(2, 11)) baseStrength += 4.0

            // Reduce for debilitation
            if (isDebilitated(pos)) baseStrength -= 15.0

            // Reduce for 6, 8, 12 placement (Dusthanas)
            if (pos.house in listOf(6, 8, 12)) baseStrength -= 10.0

            // Check for retrograde - benefics gain strength, malefics more intense
            if (pos.isRetrograde) {
                when (pos.planet) {
                    Planet.JUPITER, Planet.VENUS, Planet.MERCURY -> baseStrength += 5.0
                    Planet.SATURN -> baseStrength += 3.0 // Saturn gains focus when retrograde
                    Planet.MARS -> baseStrength -= 2.0 // Mars becomes more erratic
                    else -> {}
                }
            }

            // Dig Bala (directional strength) - planet in its preferred direction
            if (hasDigBala(pos)) baseStrength += 7.0
        }

        // Apply comprehensive cancellation factors
        val (cancellationFactor, _) = calculateCancellationFactor(positions, chart)
        val adjustedStrength = baseStrength * cancellationFactor

        return adjustedStrength.coerceIn(10.0, 100.0)
    }

    /**
     * Calculate yoga strength and return both strength and cancellation reasons.
     * Used when creating Yoga objects to populate cancellationFactors list.
     */
    private fun calculateYogaStrengthWithReasons(
        chart: VedicChart,
        positions: List<PlanetPosition>
    ): Pair<Double, List<String>> {
        var baseStrength = 50.0

        positions.forEach { pos ->
            if (isExalted(pos)) baseStrength += 15.0
            if (isInOwnSign(pos)) baseStrength += 12.0
            if (isInFriendSign(pos)) baseStrength += 6.0
            if (pos.house in listOf(1, 4, 5, 7, 9, 10)) baseStrength += 8.0
            if (pos.house in listOf(2, 11)) baseStrength += 4.0
            if (isDebilitated(pos)) baseStrength -= 15.0
            if (pos.house in listOf(6, 8, 12)) baseStrength -= 10.0

            if (pos.isRetrograde) {
                when (pos.planet) {
                    Planet.JUPITER, Planet.VENUS, Planet.MERCURY -> baseStrength += 5.0
                    Planet.SATURN -> baseStrength += 3.0
                    Planet.MARS -> baseStrength -= 2.0
                    else -> {}
                }
            }

            if (hasDigBala(pos)) baseStrength += 7.0
        }

        val (cancellationFactor, cancellationReasons) = calculateCancellationFactor(positions, chart)
        val adjustedStrength = (baseStrength * cancellationFactor).coerceIn(10.0, 100.0)

        return Pair(adjustedStrength, cancellationReasons)
    }

    /**
     * Check if planet has Dig Bala (directional strength).
     * Based on BPHS:
     * - Sun/Mars: Strong in 10th house (South)
     * - Moon/Venus: Strong in 4th house (North)
     * - Mercury/Jupiter: Strong in 1st house (East)
     * - Saturn: Strong in 7th house (West)
     */
    private fun hasDigBala(pos: PlanetPosition): Boolean {
        return when (pos.planet) {
            Planet.SUN, Planet.MARS -> pos.house == 10
            Planet.MOON, Planet.VENUS -> pos.house == 4
            Planet.MERCURY, Planet.JUPITER -> pos.house == 1
            Planet.SATURN -> pos.house == 7
            else -> false
        }
    }

    /**
     * Check if planet is in a friend's sign based on natural friendship.
     */
    private fun isInFriendSign(pos: PlanetPosition): Boolean {
        val friends = getFriends(pos.planet)
        return pos.sign.ruler in friends
    }

    /**
     * Get natural friends of a planet based on BPHS.
     */
    private fun getFriends(planet: Planet): List<Planet> {
        return when (planet) {
            Planet.SUN -> listOf(Planet.MOON, Planet.MARS, Planet.JUPITER)
            Planet.MOON -> listOf(Planet.SUN, Planet.MERCURY)
            Planet.MARS -> listOf(Planet.SUN, Planet.MOON, Planet.JUPITER)
            Planet.MERCURY -> listOf(Planet.SUN, Planet.VENUS)
            Planet.JUPITER -> listOf(Planet.SUN, Planet.MOON, Planet.MARS)
            Planet.VENUS -> listOf(Planet.MERCURY, Planet.SATURN)
            Planet.SATURN -> listOf(Planet.MERCURY, Planet.VENUS)
            Planet.RAHU -> listOf(Planet.VENUS, Planet.SATURN)
            Planet.KETU -> listOf(Planet.MARS, Planet.JUPITER)
            else -> emptyList()
        }
    }

    /**
     * Calculate Pancha Mahapurusha Yoga strength with proper cancellation logic.
     * These yogas have specific requirements and cancellation factors per BPHS/Phaladeepika:
     *
     * Full strength conditions:
     * - Planet in Lagna or 10th house (highest strength)
     * - Planet in 7th or 4th house (good strength)
     * - Free from combustion
     * - Not afflicted by malefics
     * - Aspected by benefics (bonus)
     *
     * Cancellation factors (as per classical texts):
     * - Combustion severely reduces Mahapurusha yoga
     * - Malefic aspects reduce results
     * - Placement in Dusthana from Moon weakens it
     */
    private fun calculateMahapurushaStrength(pos: PlanetPosition, chart: VedicChart): Double {
        var strength = 70.0 // Base strength for Mahapurusha

        // Boost for specific house placements (Dig Bala alignment)
        when (pos.house) {
            1 -> strength += 15.0 // In Lagna - strongest
            10 -> strength += 12.0 // In 10th - very strong (especially for Sun/Mars)
            7 -> strength += 10.0 // In 7th - strong (especially for Saturn)
            4 -> strength += 8.0 // In 4th - good (especially for Moon/Venus)
        }

        // Extra boost if planet has Dig Bala
        if (hasDigBala(pos)) strength += 5.0

        // Apply combustion check (critical for Mahapurusha yogas)
        val combustionFactor = getCombustionFactor(pos, chart)
        if (combustionFactor < 1.0) {
            strength *= combustionFactor
        }

        // Check benefic aspects using proper Vedic aspect rules
        val beneficBoost = getBeneficAspectBoost(pos, chart)
        strength *= beneficBoost

        // Check malefic affliction
        val afflictionFactor = getMaleficAfflictionFactor(pos, chart)
        strength *= afflictionFactor

        // Check Papakartari (hemmed between malefics)
        if (isPapakartari(pos, chart)) {
            strength *= 0.75 // 25% reduction
        }

        // Check placement from Moon (Chandra Lagna strength)
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        if (moonPos != null) {
            val houseFromMoon = getHouseFrom(pos.sign, moonPos.sign)
            // Weak if in Dusthana from Moon
            if (houseFromMoon in listOf(6, 8, 12)) {
                strength *= 0.85
            }
            // Strong if in Kendra from Moon
            if (houseFromMoon in listOf(1, 4, 7, 10)) {
                strength *= 1.1
            }
        }

        return strength.coerceIn(30.0, 100.0)
    }

    /**
     * Calculate Mahapurusha strength with cancellation reasons for UI display.
     */
    private fun calculateMahapurushaStrengthWithReasons(
        pos: PlanetPosition,
        chart: VedicChart
    ): Pair<Double, List<String>> {
        val cancellations = mutableListOf<String>()
        var strength = 70.0

        when (pos.house) {
            1 -> strength += 15.0
            10 -> strength += 12.0
            7 -> strength += 10.0
            4 -> strength += 8.0
        }

        if (hasDigBala(pos)) strength += 5.0

        val combustionFactor = getCombustionFactor(pos, chart)
        if (combustionFactor < 1.0) {
            strength *= combustionFactor
            if (combustionFactor < 0.6) {
                cancellations.add("${pos.planet.displayName} is combust - yoga significantly weakened")
            }
        }

        strength *= getBeneficAspectBoost(pos, chart)

        val afflictionFactor = getMaleficAfflictionFactor(pos, chart)
        if (afflictionFactor < 0.85) {
            strength *= afflictionFactor
            cancellations.add("Malefic aspects reduce yoga results")
        }

        if (isPapakartari(pos, chart)) {
            strength *= 0.75
            cancellations.add("Planet hemmed between malefics")
        }

        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        if (moonPos != null) {
            val houseFromMoon = getHouseFrom(pos.sign, moonPos.sign)
            if (houseFromMoon in listOf(6, 8, 12)) {
                strength *= 0.85
                cancellations.add("Weak position from Moon")
            } else if (houseFromMoon in listOf(1, 4, 7, 10)) {
                strength *= 1.1
            }
        }

        return Pair(strength.coerceIn(30.0, 100.0), cancellations)
    }

    private fun strengthFromPercentage(percentage: Double): YogaStrength {
        return when {
            percentage >= 85 -> YogaStrength.EXTREMELY_STRONG
            percentage >= 70 -> YogaStrength.STRONG
            percentage >= 50 -> YogaStrength.MODERATE
            percentage >= 30 -> YogaStrength.WEAK
            else -> YogaStrength.VERY_WEAK
        }
    }

    private fun createKendraTrikonaRajaYoga(
        kendraLord: Planet,
        trikonaLord: Planet,
        type: String,
        baseStrength: Double,
        chart: VedicChart
    ): Yoga {
        val kendraPos = chart.planetPositions.find { it.planet == kendraLord }
        val trikonaPos = chart.planetPositions.find { it.planet == trikonaLord }
        val positions = listOfNotNull(kendraPos, trikonaPos)

        // Apply comprehensive cancellation logic
        val (cancellationFactor, cancellationReasons) = calculateCancellationFactor(positions, chart)
        val adjustedStrength = (baseStrength * cancellationFactor).coerceIn(10.0, 100.0)

        return Yoga(
            name = "Kendra-Trikona Raja Yoga",
            sanskritName = "Kendra-Trikona Raja Yoga",
            category = YogaCategory.RAJA_YOGA,
            planets = listOf(kendraLord, trikonaLord),
            houses = listOfNotNull(kendraPos?.house, trikonaPos?.house),
            description = "${kendraLord.displayName} (Kendra lord) and ${trikonaLord.displayName} (Trikona lord) in $type",
            effects = "Rise to power and authority, leadership position, recognition from government",
            strength = strengthFromPercentage(adjustedStrength),
            strengthPercentage = adjustedStrength,
            isAuspicious = true,
            activationPeriod = "${kendraLord.displayName}-${trikonaLord.displayName} Dasha/Antardasha",
            cancellationFactors = cancellationReasons.ifEmpty { listOf("None - yoga is unafflicted") }
        )
    }

    private fun createParivartanaRajaYoga(
        planet1: Planet,
        planet2: Planet,
        baseStrength: Double,
        chart: VedicChart
    ): Yoga {
        val pos1 = chart.planetPositions.find { it.planet == planet1 }
        val pos2 = chart.planetPositions.find { it.planet == planet2 }
        val positions = listOfNotNull(pos1, pos2)

        // Apply comprehensive cancellation logic
        val (cancellationFactor, cancellationReasons) = calculateCancellationFactor(positions, chart)
        val adjustedStrength = (baseStrength * cancellationFactor).coerceIn(10.0, 100.0)

        return Yoga(
            name = "Parivartana Raja Yoga",
            sanskritName = "Parivartana Raja Yoga",
            category = YogaCategory.RAJA_YOGA,
            planets = listOf(planet1, planet2),
            houses = listOfNotNull(pos1?.house, pos2?.house),
            description = "Exchange between ${planet1.displayName} and ${planet2.displayName}",
            effects = "Strong Raja Yoga through mutual exchange, stable rise to power, lasting authority",
            strength = strengthFromPercentage(adjustedStrength),
            strengthPercentage = adjustedStrength,
            isAuspicious = true,
            activationPeriod = "${planet1.displayName} and ${planet2.displayName} Dashas",
            cancellationFactors = cancellationReasons.ifEmpty { listOf("None - yoga is unafflicted") }
        )
    }

    private fun createViparitaRajaYoga(
        planet1: Planet,
        planet2: Planet,
        baseStrength: Double,
        chart: VedicChart
    ): Yoga {
        val pos1 = chart.planetPositions.find { it.planet == planet1 }
        val pos2 = chart.planetPositions.find { it.planet == planet2 }
        val positions = listOfNotNull(pos1, pos2)

        // Apply comprehensive cancellation logic
        val (cancellationFactor, cancellationReasons) = calculateCancellationFactor(positions, chart)
        val adjustedStrength = (baseStrength * cancellationFactor).coerceIn(10.0, 100.0)

        // Add Viparita-specific consideration
        val specificReasons = cancellationReasons.toMutableList()
        // Viparita Raja works best when Dusthana lords are weak; strong lords may not give classical results
        positions.forEach { pos ->
            if (isExalted(pos) || isInOwnSign(pos)) {
                specificReasons.add("${pos.planet.displayName} is strong - Viparita results may be modified")
            }
        }

        return Yoga(
            name = "Viparita Raja Yoga",
            sanskritName = "Viparita Raja Yoga",
            category = YogaCategory.RAJA_YOGA,
            planets = listOf(planet1, planet2),
            houses = positions.map { it.house },
            description = "Lords of Dusthanas (6,8,12) connected",
            effects = "Rise through fall of enemies, sudden fortune from unexpected sources, gains through others' losses",
            strength = strengthFromPercentage(adjustedStrength),
            strengthPercentage = adjustedStrength,
            isAuspicious = true,
            activationPeriod = "${planet1.displayName}-${planet2.displayName} periods",
            cancellationFactors = specificReasons.ifEmpty { listOf("None - yoga is unafflicted") }
        )
    }

    private fun createNeechaBhangaRajaYoga(
        planet: Planet,
        baseStrength: Double,
        chart: VedicChart
    ): Yoga {
        val pos = chart.planetPositions.find { it.planet == planet }
        val positions = listOfNotNull(pos)

        // For Neecha Bhanga, we apply different cancellation rules
        // The debilitation is already cancelled, so we only check other factors
        val cancellationReasons = mutableListOf<String>()
        var adjustedStrength = baseStrength

        if (pos != null) {
            // Check combustion (still affects the planet)
            val combustionFactor = getCombustionFactor(pos, chart)
            if (combustionFactor < 0.9) {
                adjustedStrength *= combustionFactor
                if (combustionFactor < 0.6) {
                    cancellationReasons.add("${planet.displayName} is combust - Neecha Bhanga weakened")
                }
            }

            // Check malefic aspects
            val afflictionFactor = getMaleficAfflictionFactor(pos, chart)
            if (afflictionFactor < 0.85) {
                adjustedStrength *= afflictionFactor
                cancellationReasons.add("Malefic aspects reduce yoga effectiveness")
            }

            // Check Papakartari
            if (isPapakartari(pos, chart)) {
                adjustedStrength *= 0.8
                cancellationReasons.add("Planet hemmed between malefics")
            }

            // Benefic aspects boost Neecha Bhanga
            val beneficBoost = getBeneficAspectBoost(pos, chart)
            if (beneficBoost > 1.0) {
                adjustedStrength *= beneficBoost
            }

            // Identify the cancellation type for informational purposes
            if (pos.house in listOf(1, 4, 7, 10)) {
                cancellationReasons.add(0, "Neecha Bhanga via Kendra placement")
            } else {
                val debilitatedSignLord = pos.sign.ruler
                val lordPos = chart.planetPositions.find { it.planet == debilitatedSignLord }
                if (lordPos != null && lordPos.house in listOf(1, 4, 7, 10)) {
                    cancellationReasons.add(0, "Neecha Bhanga via sign lord in Kendra")
                }
            }
        }

        return Yoga(
            name = "Neecha Bhanga Raja Yoga",
            sanskritName = "Neecha Bhanga Raja Yoga",
            category = YogaCategory.RAJA_YOGA,
            planets = listOf(planet),
            houses = listOfNotNull(pos?.house),
            description = "${planet.displayName} debilitated but with cancellation",
            effects = "Rise from humble beginnings, success after initial struggles, respected leader",
            strength = strengthFromPercentage(adjustedStrength.coerceIn(10.0, 100.0)),
            strengthPercentage = adjustedStrength.coerceIn(10.0, 100.0),
            isAuspicious = true,
            activationPeriod = "${planet.displayName} Dasha",
            cancellationFactors = cancellationReasons.ifEmpty { listOf("Clean Neecha Bhanga - yoga operates fully") }
        )
    }

    private fun createNabhasaYoga(
        name: String,
        sanskritName: String,
        description: String,
        effects: String
    ): Yoga {
        return Yoga(
            name = name,
            sanskritName = sanskritName,
            category = YogaCategory.NABHASA_YOGA,
            planets = emptyList(),
            houses = emptyList(),
            description = description,
            effects = effects,
            strength = YogaStrength.MODERATE,
            strengthPercentage = 60.0,
            isAuspicious = !effects.lowercase().contains("poor") && !effects.lowercase().contains("dirty"),
            activationPeriod = "Throughout life",
            cancellationFactors = emptyList()
        )
    }

    private fun getHouseSignifications(house: Int): String {
        return when (house) {
            1 -> "self-effort and personality"
            2 -> "family wealth and speech"
            3 -> "courage and communication"
            4 -> "property and domestic comfort"
            5 -> "speculation and creative ventures"
            6 -> "service and defeating competition"
            7 -> "partnership and business"
            8 -> "inheritance and unexpected gains"
            9 -> "fortune and higher pursuits"
            10 -> "career and public recognition"
            11 -> "gains and social networks"
            12 -> "foreign connections and spiritual pursuits"
            else -> "various activities"
        }
    }
}
