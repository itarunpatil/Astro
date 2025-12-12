package com.astro.storm.ephemeris

import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.model.*
import com.astro.storm.ephemeris.VedicAstrologyUtils.normalizeLongitude
import kotlin.math.floor

private const val DEGREES_IN_SIGN = 30.0
private const val DEGREES_IN_CIRCLE = 360.0

object DivisionalChartCalculator {

    fun calculateRashi(chart: VedicChart): DivisionalChartData {
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        
        val rashiPositions = chart.planetPositions.map { position ->
            val houseNumber = calculateHouseFromSign(position.sign.number, ascendantSign.number)
            position.copy(house = houseNumber)
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D1_RASHI,
            planetPositions = rashiPositions,
            ascendantLongitude = chart.ascendant,
            chartTitle = "Rashi (D1)"
        )
    }

    fun calculateHora(chart: VedicChart): DivisionalChartData {
        val horaAscendant = calculateHoraLongitude(chart.ascendant)
        val horaAscendantSign = ZodiacSign.fromLongitude(horaAscendant)

        val horaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, horaAscendantSign.number) { longitude ->
                calculateHoraLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D2_HORA,
            planetPositions = horaPositions,
            ascendantLongitude = horaAscendant,
            chartTitle = "Hora (D2)"
        )
    }

    private fun calculateHoraLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)
        val horaSize = DEGREES_IN_SIGN / 2.0
        val isFirstHalf = degreeInSign < horaSize

        val horaSign = when {
            isOddSign && isFirstHalf -> 4
            isOddSign && !isFirstHalf -> 3
            !isOddSign && isFirstHalf -> 3
            else -> 4
        }

        val positionInHora = degreeInSign % horaSize
        val scaledDegree = (positionInHora / horaSize) * DEGREES_IN_SIGN

        return (horaSign * DEGREES_IN_SIGN) + scaledDegree
    }

    fun calculateDrekkana(chart: VedicChart): DivisionalChartData {
        val drekkanaAscendant = calculateDrekkanaLongitude(chart.ascendant)
        val drekkanaAscendantSign = ZodiacSign.fromLongitude(drekkanaAscendant)

        val drekkanaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, drekkanaAscendantSign.number) { longitude ->
                calculateDrekkanaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D3_DREKKANA,
            planetPositions = drekkanaPositions,
            ascendantLongitude = drekkanaAscendant,
            chartTitle = "Drekkana (D3)"
        )
    }

    private fun calculateDrekkanaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val drekkanaSize = DEGREES_IN_SIGN / 3.0

        val drekkanaPart = getDivisionPart(degreeInSign, drekkanaSize, 3)

        val drekkanaSign = when (drekkanaPart) {
            0 -> signNumber
            1 -> (signNumber + 4) % 12
            else -> (signNumber + 8) % 12
        }

        return calculateScaledLongitude(degreeInSign, drekkanaSize, drekkanaSign)
    }

    fun calculateChaturthamsa(chart: VedicChart): DivisionalChartData {
        val chaturthamsaAscendant = calculateChaturthamsaLongitude(chart.ascendant)
        val chaturthamsaAscendantSign = ZodiacSign.fromLongitude(chaturthamsaAscendant)

        val chaturthamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, chaturthamsaAscendantSign.number) { longitude ->
                calculateChaturthamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D4_CHATURTHAMSA,
            planetPositions = chaturthamsaPositions,
            ascendantLongitude = chaturthamsaAscendant,
            chartTitle = "Chaturthamsa (D4)"
        )
    }

    private fun calculateChaturthamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val chaturthamsaSize = DEGREES_IN_SIGN / 4.0

        val chaturthamsaPart = getDivisionPart(degreeInSign, chaturthamsaSize, 4)
        val chaturthamsaSign = (signNumber + (chaturthamsaPart * 3)) % 12

        return calculateScaledLongitude(degreeInSign, chaturthamsaSize, chaturthamsaSign)
    }

    fun calculateSaptamsa(chart: VedicChart): DivisionalChartData {
        val saptamsaAscendant = calculateSaptamsaLongitude(chart.ascendant)
        val saptamsaAscendantSign = ZodiacSign.fromLongitude(saptamsaAscendant)

        val saptamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, saptamsaAscendantSign.number) { longitude ->
                calculateSaptamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D7_SAPTAMSA,
            planetPositions = saptamsaPositions,
            ascendantLongitude = saptamsaAscendant,
            chartTitle = "Saptamsa (D7)"
        )
    }

    private fun calculateSaptamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)
        val saptamsaSize = DEGREES_IN_SIGN / 7.0

        val saptamsaPart = getDivisionPart(degreeInSign, saptamsaSize, 7)
        val startingSign = if (isOddSign) signNumber else (signNumber + 6) % 12
        val saptamsaSign = (startingSign + saptamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, saptamsaSize, saptamsaSign)
    }

    fun calculateNavamsa(chart: VedicChart): DivisionalChartData {
        val navamsaAscendant = calculateNavamsaLongitude(chart.ascendant)
        val navamsaAscendantSign = ZodiacSign.fromLongitude(navamsaAscendant)

        val navamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, navamsaAscendantSign.number) { longitude ->
                calculateNavamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D9_NAVAMSA,
            planetPositions = navamsaPositions,
            ascendantLongitude = navamsaAscendant,
            chartTitle = "Navamsa (D9)"
        )
    }

    fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val navamsaSize = DEGREES_IN_SIGN / 9.0

        val navamsaPart = getDivisionPart(degreeInSign, navamsaSize, 9)
        
        val startingSignIndex = when (getModality(signNumber)) {
            Modality.MOVABLE -> signNumber
            Modality.FIXED -> (signNumber + 8) % 12
            Modality.DUAL -> (signNumber + 4) % 12
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, navamsaSize, navamsaSignIndex)
    }

    fun getNavamsaSign(longitude: Double): ZodiacSign {
        return ZodiacSign.fromLongitude(calculateNavamsaLongitude(longitude))
    }

    fun isVargottama(rashiSign: ZodiacSign, longitude: Double): Boolean {
        return rashiSign == getNavamsaSign(longitude)
    }

    fun calculateDasamsa(chart: VedicChart): DivisionalChartData {
        val dasamsaAscendant = calculateDasamsaLongitude(chart.ascendant)
        val dasamsaAscendantSign = ZodiacSign.fromLongitude(dasamsaAscendant)

        val dasamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, dasamsaAscendantSign.number) { longitude ->
                calculateDasamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D10_DASAMSA,
            planetPositions = dasamsaPositions,
            ascendantLongitude = dasamsaAscendant,
            chartTitle = "Dasamsa (D10)"
        )
    }

    private fun calculateDasamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)
        val dasamsaSize = DEGREES_IN_SIGN / 10.0

        val dasamsaPart = getDivisionPart(degreeInSign, dasamsaSize, 10)
        val startingSignIndex = if (isOddSign) signNumber else (signNumber + 8) % 12
        val dasamsaSignIndex = (startingSignIndex + dasamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, dasamsaSize, dasamsaSignIndex)
    }

    fun calculateDwadasamsa(chart: VedicChart): DivisionalChartData {
        val dwadasamsaAscendant = calculateDwadasamsaLongitude(chart.ascendant)
        val dwadasamsaAscendantSign = ZodiacSign.fromLongitude(dwadasamsaAscendant)

        val dwadasamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, dwadasamsaAscendantSign.number) { longitude ->
                calculateDwadasamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D12_DWADASAMSA,
            planetPositions = dwadasamsaPositions,
            ascendantLongitude = dwadasamsaAscendant,
            chartTitle = "Dwadasamsa (D12)"
        )
    }

    private fun calculateDwadasamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val dwadasamsaSize = DEGREES_IN_SIGN / 12.0

        val dwadasamsaPart = getDivisionPart(degreeInSign, dwadasamsaSize, 12)
        val dwadasamsaSign = (signNumber + dwadasamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, dwadasamsaSize, dwadasamsaSign)
    }

    fun calculateShodasamsa(chart: VedicChart): DivisionalChartData {
        val shodasamsaAscendant = calculateShodasamsaLongitude(chart.ascendant)
        val shodasamsaAscendantSign = ZodiacSign.fromLongitude(shodasamsaAscendant)

        val shodasamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, shodasamsaAscendantSign.number) { longitude ->
                calculateShodasamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D16_SHODASAMSA,
            planetPositions = shodasamsaPositions,
            ascendantLongitude = shodasamsaAscendant,
            chartTitle = "Shodasamsa (D16)"
        )
    }

    private fun calculateShodasamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val shodasamsaSize = DEGREES_IN_SIGN / 16.0

        val shodasamsaPart = getDivisionPart(degreeInSign, shodasamsaSize, 16)

        val startingSign = when (getModality(signNumber)) {
            Modality.MOVABLE -> 0
            Modality.FIXED -> 4
            Modality.DUAL -> 8
        }

        val shodasamsaSign = (startingSign + shodasamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, shodasamsaSize, shodasamsaSign)
    }

    fun calculateVimsamsa(chart: VedicChart): DivisionalChartData {
        val vimsamsaAscendant = calculateVimsamsaLongitude(chart.ascendant)
        val vimsamsaAscendantSign = ZodiacSign.fromLongitude(vimsamsaAscendant)

        val vimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, vimsamsaAscendantSign.number) { longitude ->
                calculateVimsamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D20_VIMSAMSA,
            planetPositions = vimsamsaPositions,
            ascendantLongitude = vimsamsaAscendant,
            chartTitle = "Vimsamsa (D20)"
        )
    }

    private fun calculateVimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val vimsamsaSize = DEGREES_IN_SIGN / 20.0

        val vimsamsaPart = getDivisionPart(degreeInSign, vimsamsaSize, 20)

        val startingSign = when (getModality(signNumber)) {
            Modality.MOVABLE -> 0
            Modality.FIXED -> 8
            Modality.DUAL -> 4
        }

        val vimsamsaSign = (startingSign + vimsamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, vimsamsaSize, vimsamsaSign)
    }

    fun calculateChaturvimsamsa(chart: VedicChart): DivisionalChartData {
        val chaturvimsamsaAscendant = calculateChaturvimsamsaLongitude(chart.ascendant)
        val chaturvimsamsaAscendantSign = ZodiacSign.fromLongitude(chaturvimsamsaAscendant)

        val chaturvimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, chaturvimsamsaAscendantSign.number) { longitude ->
                calculateChaturvimsamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D24_CHATURVIMSAMSA,
            planetPositions = chaturvimsamsaPositions,
            ascendantLongitude = chaturvimsamsaAscendant,
            chartTitle = "Chaturvimsamsa (D24)"
        )
    }

    private fun calculateChaturvimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)
        val chaturvimsamsaSize = DEGREES_IN_SIGN / 24.0

        val chaturvimsamsaPart = getDivisionPart(degreeInSign, chaturvimsamsaSize, 24)

        val startingSign = if (isOddSign) 4 else 3
        val chaturvimsamsaSign = (startingSign + chaturvimsamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, chaturvimsamsaSize, chaturvimsamsaSign)
    }

    fun calculateSaptavimsamsa(chart: VedicChart): DivisionalChartData {
        val saptavimsamsaAscendant = calculateSaptavimsamsaLongitude(chart.ascendant)
        val saptavimsamsaAscendantSign = ZodiacSign.fromLongitude(saptavimsamsaAscendant)

        val saptavimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, saptavimsamsaAscendantSign.number) { longitude ->
                calculateSaptavimsamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D27_SAPTAVIMSAMSA,
            planetPositions = saptavimsamsaPositions,
            ascendantLongitude = saptavimsamsaAscendant,
            chartTitle = "Bhamsa (D27)"
        )
    }

    private fun calculateSaptavimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val saptavimsamsaSize = DEGREES_IN_SIGN / 27.0

        val saptavimsamsaPart = getDivisionPart(degreeInSign, saptavimsamsaSize, 27)

        val startingSign = when (getElement(signNumber)) {
            Element.FIRE -> 0
            Element.EARTH -> 3
            Element.AIR -> 6
            Element.WATER -> 9
        }

        val saptavimsamsaSign = (startingSign + saptavimsamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, saptavimsamsaSize, saptavimsamsaSign)
    }

    fun calculateTrimsamsa(chart: VedicChart): DivisionalChartData {
        val trimsamsaAscendant = calculateTrimsamsaLongitude(chart.ascendant)
        val trimsamsaAscendantSign = ZodiacSign.fromLongitude(trimsamsaAscendant)

        val trimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, trimsamsaAscendantSign.number) { longitude ->
                calculateTrimsamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D30_TRIMSAMSA,
            planetPositions = trimsamsaPositions,
            ascendantLongitude = trimsamsaAscendant,
            chartTitle = "Trimsamsa (D30)"
        )
    }

    private fun calculateTrimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)

        val (trimsamsaSign, degreeWithinPart, partSize) = if (isOddSign) {
            getOddSignTrimsamsaDivision(degreeInSign)
        } else {
            getEvenSignTrimsamsaDivision(degreeInSign)
        }

        val scaledDegree = (degreeWithinPart / partSize) * DEGREES_IN_SIGN
        return (trimsamsaSign * DEGREES_IN_SIGN) + scaledDegree
    }

    private fun getOddSignTrimsamsaDivision(degreeInSign: Double): Triple<Int, Double, Double> {
        return when {
            degreeInSign < 5.0 -> Triple(0, degreeInSign, 5.0)
            degreeInSign < 10.0 -> Triple(10, degreeInSign - 5.0, 5.0)
            degreeInSign < 18.0 -> Triple(8, degreeInSign - 10.0, 8.0)
            degreeInSign < 25.0 -> Triple(2, degreeInSign - 18.0, 7.0)
            else -> Triple(1, degreeInSign - 25.0, 5.0)
        }
    }

    private fun getEvenSignTrimsamsaDivision(degreeInSign: Double): Triple<Int, Double, Double> {
        return when {
            degreeInSign < 5.0 -> Triple(1, degreeInSign, 5.0)
            degreeInSign < 12.0 -> Triple(5, degreeInSign - 5.0, 7.0)
            degreeInSign < 20.0 -> Triple(11, degreeInSign - 12.0, 8.0)
            degreeInSign < 25.0 -> Triple(9, degreeInSign - 20.0, 5.0)
            else -> Triple(7, degreeInSign - 25.0, 5.0)
        }
    }

    fun calculateKhavedamsa(chart: VedicChart): DivisionalChartData {
        val khavedamsaAscendant = calculateKhavedamsaLongitude(chart.ascendant)
        val khavedamsaAscendantSign = ZodiacSign.fromLongitude(khavedamsaAscendant)

        val khavedamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, khavedamsaAscendantSign.number) { longitude ->
                calculateKhavedamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D40_KHAVEDAMSA,
            planetPositions = khavedamsaPositions,
            ascendantLongitude = khavedamsaAscendant,
            chartTitle = "Khavedamsa (D40)"
        )
    }

    private fun calculateKhavedamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)
        val khavedamsaSize = DEGREES_IN_SIGN / 40.0

        val khavedamsaPart = getDivisionPart(degreeInSign, khavedamsaSize, 40)
        val startingSign = if (isOddSign) 0 else 6
        val khavedamsaSign = (startingSign + khavedamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, khavedamsaSize, khavedamsaSign)
    }

    fun calculateAkshavedamsa(chart: VedicChart): DivisionalChartData {
        val akshavedamsaAscendant = calculateAkshavedamsaLongitude(chart.ascendant)
        val akshavedamsaAscendantSign = ZodiacSign.fromLongitude(akshavedamsaAscendant)

        val akshavedamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, akshavedamsaAscendantSign.number) { longitude ->
                calculateAkshavedamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D45_AKSHAVEDAMSA,
            planetPositions = akshavedamsaPositions,
            ascendantLongitude = akshavedamsaAscendant,
            chartTitle = "Akshavedamsa (D45)"
        )
    }

    private fun calculateAkshavedamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val akshavedamsaSize = DEGREES_IN_SIGN / 45.0

        val akshavedamsaPart = getDivisionPart(degreeInSign, akshavedamsaSize, 45)

        val startingSign = when (getModality(signNumber)) {
            Modality.MOVABLE -> 0
            Modality.FIXED -> 4
            Modality.DUAL -> 8
        }

        val akshavedamsaSign = (startingSign + akshavedamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, akshavedamsaSize, akshavedamsaSign)
    }

    fun calculateShashtiamsa(chart: VedicChart): DivisionalChartData {
        val shashtiamsaAscendant = calculateShashtiamsaLongitude(chart.ascendant)
        val shashtiamsaAscendantSign = ZodiacSign.fromLongitude(shashtiamsaAscendant)

        val shashtiamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, shashtiamsaAscendantSign.number) { longitude ->
                calculateShashtiamsaLongitude(longitude)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D60_SHASHTIAMSA,
            planetPositions = shashtiamsaPositions,
            ascendantLongitude = shashtiamsaAscendant,
            chartTitle = "Shashtiamsa (D60)"
        )
    }

    private fun calculateShashtiamsaLongitude(longitude: Double): Double {
        val normalizedLong = normalizeLongitude(longitude)
        val signNumber = getSignNumber(normalizedLong)
        val degreeInSign = getDegreeInSign(normalizedLong)
        val isOddSign = isOddSign(signNumber)
        val shashtiamsaSize = DEGREES_IN_SIGN / 60.0

        val shashtiamsaPart = getDivisionPart(degreeInSign, shashtiamsaSize, 60)
        val startingSignIndex = if (isOddSign) signNumber else (signNumber + 6) % 12
        val shashtiamsaSignIndex = (startingSignIndex + shashtiamsaPart) % 12

        return calculateScaledLongitude(degreeInSign, shashtiamsaSize, shashtiamsaSignIndex)
    }

    private fun getSignNumber(longitude: Double): Int {
        return floor(longitude / DEGREES_IN_SIGN).toInt() % 12
    }

    private fun getDegreeInSign(longitude: Double): Double {
        val degree = longitude % DEGREES_IN_SIGN
        return if (degree < 0) degree + DEGREES_IN_SIGN else degree
    }

    private fun isOddSign(signNumber: Int): Boolean {
        return signNumber % 2 == 0
    }

    private fun getDivisionPart(degreeInSign: Double, divisionSize: Double, maxParts: Int): Int {
        val part = floor(degreeInSign / divisionSize).toInt()
        return part.coerceIn(0, maxParts - 1)
    }

    private fun calculateScaledLongitude(degreeInSign: Double, divisionSize: Double, targetSign: Int): Double {
        val positionInDivision = degreeInSign % divisionSize
        val scaledDegree = (positionInDivision / divisionSize) * DEGREES_IN_SIGN
        return (targetSign * DEGREES_IN_SIGN) + scaledDegree
    }

    private fun getModality(signNumber: Int): Modality {
        return when (signNumber % 3) {
            0 -> Modality.MOVABLE
            1 -> Modality.FIXED
            else -> Modality.DUAL
        }
    }

    private fun getElement(signNumber: Int): Element {
        return when (signNumber % 4) {
            0 -> Element.FIRE
            1 -> Element.EARTH
            2 -> Element.AIR
            else -> Element.WATER
        }
    }

    private fun calculateDivisionalPosition(
        position: PlanetPosition,
        ascendantSignNumber: Int,
        calculateLongitude: (Double) -> Double
    ): PlanetPosition {
        val divisionalLongitude = calculateLongitude(position.longitude)
        val divisionalSign = ZodiacSign.fromLongitude(divisionalLongitude)
        val degreeInSign = getDegreeInSign(divisionalLongitude)
        val (nakshatra, pada) = Nakshatra.fromLongitude(divisionalLongitude)
        val divisionalHouse = calculateHouseFromSign(divisionalSign.number, ascendantSignNumber)

        val wholeDegree = floor(degreeInSign).toInt()
        val fractionalPart = degreeInSign - wholeDegree
        val totalMinutes = fractionalPart * 60.0
        val wholeMinutes = floor(totalMinutes).toInt()
        val seconds = (totalMinutes - wholeMinutes) * 60.0

        return position.copy(
            longitude = divisionalLongitude,
            sign = divisionalSign,
            degree = wholeDegree.toDouble(),
            minutes = wholeMinutes.toDouble(),
            seconds = seconds,
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = divisionalHouse
        )
    }

    private fun calculateHouseFromSign(planetSignNumber: Int, ascendantSignNumber: Int): Int {
        return ((planetSignNumber - ascendantSignNumber + 12) % 12) + 1
    }

    fun calculateAllDivisionalCharts(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateRashi(chart),
            calculateHora(chart),
            calculateDrekkana(chart),
            calculateChaturthamsa(chart),
            calculateSaptamsa(chart),
            calculateNavamsa(chart),
            calculateDasamsa(chart),
            calculateDwadasamsa(chart),
            calculateShodasamsa(chart),
            calculateVimsamsa(chart),
            calculateChaturvimsamsa(chart),
            calculateSaptavimsamsa(chart),
            calculateTrimsamsa(chart),
            calculateKhavedamsa(chart),
            calculateAkshavedamsa(chart),
            calculateShashtiamsa(chart)
        )
    }

    fun calculateShodashaVarga(chart: VedicChart): List<DivisionalChartData> {
        return calculateAllDivisionalCharts(chart)
    }

    fun calculateSaptaVarga(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateRashi(chart),
            calculateHora(chart),
            calculateDrekkana(chart),
            calculateSaptamsa(chart),
            calculateNavamsa(chart),
            calculateDwadasamsa(chart),
            calculateTrimsamsa(chart)
        )
    }

    fun calculateDashaVarga(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateRashi(chart),
            calculateHora(chart),
            calculateDrekkana(chart),
            calculateSaptamsa(chart),
            calculateNavamsa(chart),
            calculateDasamsa(chart),
            calculateDwadasamsa(chart),
            calculateShodasamsa(chart),
            calculateTrimsamsa(chart),
            calculateShashtiamsa(chart)
        )
    }

    fun calculateCommonDivisionalCharts(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateNavamsa(chart),
            calculateDasamsa(chart)
        )
    }

    fun calculateDivisionalChart(chart: VedicChart, type: DivisionalChartType): DivisionalChartData {
        return when (type) {
            DivisionalChartType.D1_RASHI -> calculateRashi(chart)
            DivisionalChartType.D2_HORA -> calculateHora(chart)
            DivisionalChartType.D3_DREKKANA -> calculateDrekkana(chart)
            DivisionalChartType.D4_CHATURTHAMSA -> calculateChaturthamsa(chart)
            DivisionalChartType.D7_SAPTAMSA -> calculateSaptamsa(chart)
            DivisionalChartType.D9_NAVAMSA -> calculateNavamsa(chart)
            DivisionalChartType.D10_DASAMSA -> calculateDasamsa(chart)
            DivisionalChartType.D12_DWADASAMSA -> calculateDwadasamsa(chart)
            DivisionalChartType.D16_SHODASAMSA -> calculateShodasamsa(chart)
            DivisionalChartType.D20_VIMSAMSA -> calculateVimsamsa(chart)
            DivisionalChartType.D24_CHATURVIMSAMSA -> calculateChaturvimsamsa(chart)
            DivisionalChartType.D27_SAPTAVIMSAMSA -> calculateSaptavimsamsa(chart)
            DivisionalChartType.D30_TRIMSAMSA -> calculateTrimsamsa(chart)
            DivisionalChartType.D40_KHAVEDAMSA -> calculateKhavedamsa(chart)
            DivisionalChartType.D45_AKSHAVEDAMSA -> calculateAkshavedamsa(chart)
            DivisionalChartType.D60_SHASHTIAMSA -> calculateShashtiamsa(chart)
        }
    }

    private enum class Modality { MOVABLE, FIXED, DUAL }
    private enum class Element { FIRE, EARTH, AIR, WATER }
}

enum class DivisionalChartType(
    val division: Int,
    val displayName: String,
    val shortName: String,
    val description: String
) {
    D1_RASHI(1, "Rashi", "D1", "Physical Body, General Life"),
    D2_HORA(2, "Hora", "D2", "Wealth, Prosperity"),
    D3_DREKKANA(3, "Drekkana", "D3", "Siblings, Courage"),
    D4_CHATURTHAMSA(4, "Chaturthamsa", "D4", "Fortune, Property"),
    D7_SAPTAMSA(7, "Saptamsa", "D7", "Children, Progeny"),
    D9_NAVAMSA(9, "Navamsa", "D9", "Marriage, Dharma"),
    D10_DASAMSA(10, "Dasamsa", "D10", "Career, Profession"),
    D12_DWADASAMSA(12, "Dwadasamsa", "D12", "Parents, Ancestry"),
    D16_SHODASAMSA(16, "Shodasamsa", "D16", "Vehicles, Pleasures"),
    D20_VIMSAMSA(20, "Vimsamsa", "D20", "Spiritual Life"),
    D24_CHATURVIMSAMSA(24, "Siddhamsa", "D24", "Education, Learning"),
    D27_SAPTAVIMSAMSA(27, "Bhamsa", "D27", "Strength, Weakness"),
    D30_TRIMSAMSA(30, "Trimsamsa", "D30", "Evils, Misfortunes"),
    D40_KHAVEDAMSA(40, "Khavedamsa", "D40", "Auspicious/Inauspicious Effects"),
    D45_AKSHAVEDAMSA(45, "Akshavedamsa", "D45", "General Indications"),
    D60_SHASHTIAMSA(60, "Shashtiamsa", "D60", "Past Life Karma");

    fun getLocalizedDisplayName(language: Language): String = when (this) {
        D1_RASHI -> StringResources.get(StringKey.VARGA_D1_TITLE, language)
        D2_HORA -> StringResources.get(StringKey.VARGA_D2_TITLE, language)
        D3_DREKKANA -> StringResources.get(StringKey.VARGA_D3_TITLE, language)
        D4_CHATURTHAMSA -> StringResources.get(StringKey.VARGA_D4_TITLE, language)
        D7_SAPTAMSA -> StringResources.get(StringKey.VARGA_D7_TITLE, language)
        D9_NAVAMSA -> StringResources.get(StringKey.VARGA_D9_TITLE, language)
        D10_DASAMSA -> StringResources.get(StringKey.VARGA_D10_TITLE, language)
        D12_DWADASAMSA -> StringResources.get(StringKey.VARGA_D12_TITLE, language)
        D16_SHODASAMSA -> StringResources.get(StringKey.VARGA_D16_TITLE, language)
        D20_VIMSAMSA -> StringResources.get(StringKey.VARGA_D20_TITLE, language)
        D24_CHATURVIMSAMSA -> StringResources.get(StringKey.VARGA_D24_TITLE, language)
        D27_SAPTAVIMSAMSA -> StringResources.get(StringKey.VARGA_D27_TITLE, language)
        D30_TRIMSAMSA -> StringResources.get(StringKey.VARGA_D30_TITLE, language)
        D40_KHAVEDAMSA -> StringResources.get(StringKey.VARGA_D40_TITLE, language)
        D45_AKSHAVEDAMSA -> StringResources.get(StringKey.VARGA_D45_TITLE, language)
        D60_SHASHTIAMSA -> StringResources.get(StringKey.VARGA_D60_TITLE, language)
    }

    fun getLocalizedDescription(language: Language): String = when (this) {
        D1_RASHI -> StringResources.get(StringKey.VARGA_D1_DESC, language)
        D2_HORA -> StringResources.get(StringKey.VARGA_D2_DESC, language)
        D3_DREKKANA -> StringResources.get(StringKey.VARGA_D3_DESC, language)
        D4_CHATURTHAMSA -> StringResources.get(StringKey.VARGA_D4_DESC, language)
        D7_SAPTAMSA -> StringResources.get(StringKey.VARGA_D7_DESC, language)
        D9_NAVAMSA -> StringResources.get(StringKey.VARGA_D9_DESC, language)
        D10_DASAMSA -> StringResources.get(StringKey.VARGA_D10_DESC, language)
        D12_DWADASAMSA -> StringResources.get(StringKey.VARGA_D12_DESC, language)
        D16_SHODASAMSA -> StringResources.get(StringKey.VARGA_D16_DESC, language)
        D20_VIMSAMSA -> StringResources.get(StringKey.VARGA_D20_DESC, language)
        D24_CHATURVIMSAMSA -> StringResources.get(StringKey.VARGA_D24_DESC, language)
        D27_SAPTAVIMSAMSA -> StringResources.get(StringKey.VARGA_D27_DESC, language)
        D30_TRIMSAMSA -> StringResources.get(StringKey.VARGA_D30_DESC, language)
        D40_KHAVEDAMSA -> StringResources.get(StringKey.VARGA_D40_DESC, language)
        D45_AKSHAVEDAMSA -> StringResources.get(StringKey.VARGA_D45_DESC, language)
        D60_SHASHTIAMSA -> StringResources.get(StringKey.VARGA_D60_DESC, language)
    }

    fun getLocalizedChartTitle(language: Language): String {
        return "${getLocalizedDisplayName(language)} ($shortName)"
    }
}

data class DivisionalChartData(
    val chartType: DivisionalChartType,
    val planetPositions: List<PlanetPosition>,
    val ascendantLongitude: Double,
    val chartTitle: String
) {
    val ascendantSign: ZodiacSign
        get() = ZodiacSign.fromLongitude(ascendantLongitude)

    val ascendantDegreeInSign: Double
        get() = ascendantLongitude % DEGREES_IN_SIGN

    fun getPlanetInHouse(house: Int): List<PlanetPosition> {
        return planetPositions.filter { it.house == house }
    }

    fun getPlanetInSign(sign: ZodiacSign): List<PlanetPosition> {
        return planetPositions.filter { it.sign == sign }
    }

    fun toPlainText(): String {
        return buildString {
            appendLine("═══════════════════════════════════════════════════")
            appendLine("           ${chartType.displayName.uppercase()} CHART (${chartType.shortName})")
            appendLine("           ${chartType.description}")
            appendLine("═══════════════════════════════════════════════════")
            appendLine()
            appendLine("Ascendant: ${formatDegree(ascendantLongitude)} (${ascendantSign.displayName})")
            appendLine()
            appendLine("PLANETARY POSITIONS")
            appendLine("───────────────────────────────────────────────────")
            planetPositions.forEach { position ->
                val retrograde = if (position.isRetrograde) " [R]" else ""
                appendLine(
                    "${position.planet.displayName.padEnd(10)}: " +
                    "${position.sign.displayName.padEnd(12)} " +
                    "${formatDegreeInSign(position.longitude)}$retrograde | House ${position.house}"
                )
            }
            appendLine()
        }
    }

    fun toLocalizedPlainText(language: Language): String {
        val ascendantLabel = StringResources.get(StringKey.CHART_LAGNA, language)
        val planetaryPositionsLabel = StringResources.get(StringKey.CHART_PLANETARY_POSITIONS, language)
        val houseLabel = StringResources.get(StringKey.HOUSE, language)
        val retroLabel = StringResources.get(StringKey.CHART_LEGEND_RETRO_SHORT, language)

        return buildString {
            appendLine("═══════════════════════════════════════════════════")
            appendLine("           ${chartType.getLocalizedDisplayName(language).uppercase()} (${chartType.shortName})")
            appendLine("           ${chartType.getLocalizedDescription(language)}")
            appendLine("═══════════════════════════════════════════════════")
            appendLine()
            appendLine("$ascendantLabel: ${formatDegree(ascendantLongitude)} (${ascendantSign.getLocalizedName(language)})")
            appendLine()
            appendLine(planetaryPositionsLabel.uppercase())
            appendLine("───────────────────────────────────────────────────")
            planetPositions.forEach { position ->
                val retrograde = if (position.isRetrograde) " [$retroLabel]" else ""
                appendLine(
                    "${position.planet.getLocalizedName(language).padEnd(10)}: " +
                    "${position.sign.getLocalizedName(language).padEnd(12)} " +
                    "${formatDegreeInSign(position.longitude)}$retrograde | $houseLabel ${position.house}"
                )
            }
            appendLine()
        }
    }

    private fun formatDegree(degree: Double): String {
        val normalizedDegree = normalizeLongitude(degree)
        val degInSign = normalizedDegree % DEGREES_IN_SIGN
        val deg = floor(degInSign).toInt()
        val minTotal = (degInSign - deg) * 60
        val min = floor(minTotal).toInt()
        val sec = floor((minTotal - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }

    private fun formatDegreeInSign(longitude: Double): String {
        val degreeInSign = longitude % DEGREES_IN_SIGN
        val deg = floor(degreeInSign).toInt()
        val minTotal = (degreeInSign - deg) * 60
        val min = floor(minTotal).toInt()
        val sec = floor((minTotal - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }
}