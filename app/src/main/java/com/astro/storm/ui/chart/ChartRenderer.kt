package com.astro.storm.ui.chart

import android.graphics.Bitmap
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.Quality
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class ChartRenderer {

    private val textPaint = android.graphics.Paint().apply {
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isSubpixelText = true
        hinting = android.graphics.Paint.HINTING_ON
    }

    private val borderStroke = Stroke(width = 3f)
    private val lineStroke = Stroke(width = 2.5f)
    private val frameLinesPath = Path()

    private enum class HouseType {
        DIAMOND,
        SIDE,
        CORNER
    }

    private data class HouseDisplayItem(
        val text: String,
        val color: Color,
        val isBold: Boolean,
        val isExalted: Boolean = false,
        val isDebilitated: Boolean = false,
        val isLagna: Boolean = false
    )

    private data class HouseBounds(
        val minX: Float,
        val maxX: Float,
        val minY: Float,
        val maxY: Float,
        val width: Float,
        val height: Float,
        val effectiveWidth: Float,
        val effectiveHeight: Float
    )

    private data class ChartFrame(
        val left: Float,
        val top: Float,
        val size: Float,
        val centerX: Float,
        val centerY: Float
    )

    private data class GridLayout(
        val columns: Int,
        val rows: Int,
        val textSize: Float,
        val lineHeight: Float,
        val columnSpacing: Float
    )

    companion object {
        private val TYPEFACE_NORMAL = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        private val TYPEFACE_BOLD = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        private val BACKGROUND_COLOR = Color(0xFFD4C4A8)
        private val BORDER_COLOR = Color(0xFFB8860B)
        private val HOUSE_NUMBER_COLOR = Color(0xFF4A4A4A)
        private val TEXT_BACKGROUND_COLOR = Color(0xFFD4C4A8)

        private val SUN_COLOR = Color(0xFFD2691E)
        private val MOON_COLOR = Color(0xFFDC143C)
        private val MARS_COLOR = Color(0xFFB22222)
        private val MERCURY_COLOR = Color(0xFF228B22)
        private val JUPITER_COLOR = Color(0xFFDAA520)
        private val VENUS_COLOR = Color(0xFF9370DB)
        private val SATURN_COLOR = Color(0xFF4169E1)
        private val RAHU_COLOR = Color(0xFF8B0000)
        private val KETU_COLOR = Color(0xFF8B0000)
        private val URANUS_COLOR = Color(0xFF20B2AA)
        private val NEPTUNE_COLOR = Color(0xFF4682B4)
        private val PLUTO_COLOR = Color(0xFF800080)
        private val LAGNA_COLOR = Color(0xFF8B4513)
        private val EXALTED_ARROW_COLOR = Color(0xFF006400)
        private val DEBILITATED_ARROW_COLOR = Color(0xFFB22222)

        const val SYMBOL_RETROGRADE = "*"
        const val SYMBOL_COMBUST = "^"
        const val SYMBOL_VARGOTTAMA = "\u00A4"

        private const val NAVAMSA_PART_DEGREES = 10.0 / 3.0

        private const val BASE_TEXT_SIZE_RATIO = 0.030f
        private const val BASE_LINE_HEIGHT_RATIO = 0.040f
        private const val MIN_SCALE_FACTOR = 0.60f
        private const val MAX_SCALE_FACTOR = 1.0f
        private const val ARROW_SIZE_RATIO = 0.35f
        private const val TEXT_PADDING_RATIO = 0.12f
    }

    private fun getPlanetColor(planet: Planet): Color = when (planet) {
        Planet.SUN -> SUN_COLOR
        Planet.MOON -> MOON_COLOR
        Planet.MARS -> MARS_COLOR
        Planet.MERCURY -> MERCURY_COLOR
        Planet.JUPITER -> JUPITER_COLOR
        Planet.VENUS -> VENUS_COLOR
        Planet.SATURN -> SATURN_COLOR
        Planet.RAHU -> RAHU_COLOR
        Planet.KETU -> KETU_COLOR
        Planet.URANUS -> URANUS_COLOR
        Planet.NEPTUNE -> NEPTUNE_COLOR
        Planet.PLUTO -> PLUTO_COLOR
    }

    private fun toSuperscript(degree: Int): String {
        val superscripts = charArrayOf(
            '\u2070', '\u00B9', '\u00B2', '\u00B3', '\u2074',
            '\u2075', '\u2076', '\u2077', '\u2078', '\u2079'
        )
        return buildString {
            degree.toString().forEach { char ->
                append(superscripts[char - '0'])
            }
        }
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean = when (planet) {
        Planet.SUN -> sign == ZodiacSign.ARIES
        Planet.MOON -> sign == ZodiacSign.TAURUS
        Planet.MARS -> sign == ZodiacSign.CAPRICORN
        Planet.MERCURY -> sign == ZodiacSign.VIRGO
        Planet.JUPITER -> sign == ZodiacSign.CANCER
        Planet.VENUS -> sign == ZodiacSign.PISCES
        Planet.SATURN -> sign == ZodiacSign.LIBRA
        Planet.RAHU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
        Planet.KETU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
        else -> false
    }

    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean = when (planet) {
        Planet.SUN -> sign == ZodiacSign.LIBRA
        Planet.MOON -> sign == ZodiacSign.SCORPIO
        Planet.MARS -> sign == ZodiacSign.CANCER
        Planet.MERCURY -> sign == ZodiacSign.PISCES
        Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
        Planet.VENUS -> sign == ZodiacSign.VIRGO
        Planet.SATURN -> sign == ZodiacSign.ARIES
        Planet.RAHU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
        Planet.KETU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
        else -> false
    }

    private fun isVargottama(planet: PlanetPosition, chart: VedicChart): Boolean {
        val navamsaLongitude = calculateNavamsaLongitude(planet.longitude)
        val navamsaSign = ZodiacSign.fromLongitude(navamsaLongitude)
        return planet.sign == navamsaSign
    }

    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val sign = ZodiacSign.fromLongitude(normalizedLong)
        val degreeInSign = normalizedLong % 30.0
        val navamsaPart = (degreeInSign / NAVAMSA_PART_DEGREES).toInt().coerceIn(0, 8)

        val startingSignIndex = when (sign.quality) {
            Quality.CARDINAL -> sign.ordinal
            Quality.FIXED -> (sign.ordinal + 8) % 12
            Quality.MUTABLE -> (sign.ordinal + 4) % 12
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12
        val positionInNavamsa = degreeInSign % NAVAMSA_PART_DEGREES
        val navamsaDegree = (positionInNavamsa / NAVAMSA_PART_DEGREES) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    private fun isCombust(planet: PlanetPosition, sunPosition: PlanetPosition?): Boolean {
        if (planet.planet == Planet.SUN) return false
        if (planet.planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return false
        }
        if (sunPosition == null) return false

        val angularDistance = calculateAngularDistance(planet.longitude, sunPosition.longitude)

        val combustionOrb = when (planet.planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> if (planet.isRetrograde) 12.0 else 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> if (planet.isRetrograde) 8.0 else 10.0
            Planet.SATURN -> 15.0
            else -> 0.0
        }

        return angularDistance <= combustionOrb
    }

    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    private fun getHouseType(houseNum: Int): HouseType = when (houseNum) {
        1, 4, 7, 10 -> HouseType.DIAMOND
        3, 5, 9, 11 -> HouseType.SIDE
        2, 6, 8, 12 -> HouseType.CORNER
        else -> HouseType.DIAMOND
    }

    private fun DrawScope.drawNorthIndianFrame(size: Float): ChartFrame {
        val padding = size * 0.02f
        val chartSize = size - (padding * 2)
        val left = padding
        val top = padding
        val right = left + chartSize
        val bottom = top + chartSize
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2

        drawRect(color = BACKGROUND_COLOR, size = Size(size, size))

        drawRect(
            color = BORDER_COLOR,
            topLeft = Offset(left, top),
            size = Size(chartSize, chartSize),
            style = borderStroke
        )

        frameLinesPath.reset()
        frameLinesPath.moveTo(centerX, top)
        frameLinesPath.lineTo(right, centerY)
        frameLinesPath.lineTo(centerX, bottom)
        frameLinesPath.lineTo(left, centerY)
        frameLinesPath.close()
        frameLinesPath.moveTo(left, top)
        frameLinesPath.lineTo(right, bottom)
        frameLinesPath.moveTo(right, top)
        frameLinesPath.lineTo(left, bottom)

        drawPath(frameLinesPath, BORDER_COLOR, style = lineStroke)

        return ChartFrame(left, top, chartSize, centerX, centerY)
    }

    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val frame = drawNorthIndianFrame(size)
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

            drawAllHouseContents(
                left = frame.left,
                top = frame.top,
                chartSize = frame.size,
                centerX = frame.centerX,
                centerY = frame.centerY,
                ascendantSign = ascendantSign,
                planetPositions = chart.planetPositions,
                size = size,
                chart = chart
            )
        }
    }

    fun drawDivisionalChart(
        drawScope: DrawScope,
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        size: Float,
        chartTitle: String,
        originalChart: VedicChart? = null
    ) {
        with(drawScope) {
            val frame = drawNorthIndianFrame(size)
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

            drawAllHouseContents(
                left = frame.left,
                top = frame.top,
                chartSize = frame.size,
                centerX = frame.centerX,
                centerY = frame.centerY,
                ascendantSign = ascendantSign,
                planetPositions = planetPositions,
                size = size,
                chart = originalChart
            )
        }
    }

    private fun signNumberForHouse(houseNum: Int, ascendantSign: ZodiacSign): Int {
        return ((ascendantSign.ordinal + houseNum - 1) % 12) + 1
    }

    private fun DrawScope.drawAllHouseContents(
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float,
        ascendantSign: ZodiacSign,
        planetPositions: List<PlanetPosition>,
        size: Float,
        chart: VedicChart? = null,
        showSignNumbers: Boolean = true
    ) {
        val planetsByHouse = planetPositions.groupBy { it.house }
        val sunPosition = chart?.planetPositions?.find { it.planet == Planet.SUN }

        for (houseNum in 1..12) {
            val housePolygon = getHousePolygon(houseNum, left, top, chartSize, centerX, centerY)
            val houseCentroid = polygonCentroid(housePolygon)
            val houseBounds = calculateHouseBounds(housePolygon, getHouseType(houseNum))
            val houseType = getHouseType(houseNum)

            val numberText = if (showSignNumbers) {
                signNumberForHouse(houseNum, ascendantSign).toString()
            } else {
                houseNum.toString()
            }

            val planets = planetsByHouse[houseNum] ?: emptyList()
            val displayItems = buildDisplayItems(houseNum, planets, chart, sunPosition)

            val numberPosition = calculateOptimalNumberPosition(
                houseNum = houseNum,
                houseType = houseType,
                houseCentroid = houseCentroid,
                houseBounds = houseBounds,
                displayItems = displayItems,
                chartSize = chartSize,
                size = size,
                left = left,
                top = top,
                centerX = centerX,
                centerY = centerY
            )

            val numberTextSize = size * 0.033f
            drawTextCentered(
                text = numberText,
                position = numberPosition,
                textSize = numberTextSize,
                color = HOUSE_NUMBER_COLOR,
                isBold = false
            )

            if (displayItems.isNotEmpty()) {
                val contentCenter = calculateContentCenter(
                    houseCentroid = houseCentroid,
                    houseBounds = houseBounds,
                    houseType = houseType,
                    numberPosition = numberPosition,
                    numberTextSize = numberTextSize,
                    itemCount = displayItems.size,
                    size = size
                )

                drawHouseContents(
                    displayItems = displayItems,
                    contentCenter = contentCenter,
                    houseBounds = houseBounds,
                    houseType = houseType,
                    houseNum = houseNum,
                    size = size
                )
            }
        }
    }

    private fun buildDisplayItems(
        houseNum: Int,
        planets: List<PlanetPosition>,
        chart: VedicChart?,
        sunPosition: PlanetPosition?
    ): List<HouseDisplayItem> {
        val items = mutableListOf<HouseDisplayItem>()

        if (houseNum == 1) {
            items.add(
                HouseDisplayItem(
                    text = "Lg",
                    color = LAGNA_COLOR,
                    isBold = true,
                    isLagna = true
                )
            )
        }

        planets.forEach { planet ->
            val abbrev = planet.planet.symbol
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            val exalted = isExalted(planet.planet, planet.sign)
            val debilitated = isDebilitated(planet.planet, planet.sign)

            val statusIndicators = buildString {
                if (planet.isRetrograde) append(SYMBOL_RETROGRADE)
                if (chart != null && isCombust(planet, sunPosition)) {
                    append(SYMBOL_COMBUST)
                }
                if (chart != null && isVargottama(planet, chart)) {
                    append(SYMBOL_VARGOTTAMA)
                }
            }

            items.add(
                HouseDisplayItem(
                    text = "$abbrev$degreeSuper$statusIndicators",
                    color = getPlanetColor(planet.planet),
                    isBold = true,
                    isExalted = exalted,
                    isDebilitated = debilitated
                )
            )
        }

        return items
    }

    private fun getHousePolygon(
        houseNum: Int,
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float
    ): List<Offset> {
        val right = left + chartSize
        val bottom = top + chartSize

        val a = Offset(left, top)
        val b = Offset(right, top)
        val c = Offset(right, bottom)
        val d = Offset(left, bottom)

        val e = Offset(centerX, top)
        val f = Offset(right, centerY)
        val g = Offset(centerX, bottom)
        val h = Offset(left, centerY)

        val o = Offset(centerX, centerY)

        val quarter = chartSize * 0.25f
        val threeQuarter = chartSize * 0.75f

        val p = Offset(left + quarter, top + quarter)
        val q = Offset(left + threeQuarter, top + threeQuarter)
        val r = Offset(left + threeQuarter, top + quarter)
        val s = Offset(left + quarter, top + threeQuarter)

        return when (houseNum) {
            1 -> listOf(e, r, o, p)
            4 -> listOf(h, p, o, s)
            7 -> listOf(g, q, o, s)
            10 -> listOf(f, q, o, r)
            2 -> listOf(a, e, p)
            6 -> listOf(d, s, g)
            8 -> listOf(c, q, g)
            12 -> listOf(b, r, e)
            3 -> listOf(a, p, h)
            5 -> listOf(d, h, s)
            9 -> listOf(c, f, q)
            11 -> listOf(b, f, r)
            else -> emptyList()
        }
    }

    private fun calculateHouseBounds(polygon: List<Offset>, houseType: HouseType): HouseBounds {
        if (polygon.isEmpty()) return HouseBounds(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        val minX = polygon.minOf { it.x }
        val maxX = polygon.maxOf { it.x }
        val minY = polygon.minOf { it.y }
        val maxY = polygon.maxOf { it.y }
        val width = maxX - minX
        val height = maxY - minY

        val (effectiveWidth, effectiveHeight) = when (houseType) {
            HouseType.CORNER -> Pair(width * 0.55f, height * 0.55f)
            HouseType.SIDE -> Pair(width * 0.60f, height * 0.65f)
            HouseType.DIAMOND -> Pair(width * 0.70f, height * 0.70f)
        }

        return HouseBounds(minX, maxX, minY, maxY, width, height, effectiveWidth, effectiveHeight)
    }

    private fun calculateOptimalNumberPosition(
        houseNum: Int,
        houseType: HouseType,
        houseCentroid: Offset,
        houseBounds: HouseBounds,
        displayItems: List<HouseDisplayItem>,
        chartSize: Float,
        size: Float,
        left: Float,
        top: Float,
        centerX: Float,
        centerY: Float
    ): Offset {
        val right = left + chartSize
        val bottom = top + chartSize

        return when (houseNum) {
            1 -> {
                val baseY = top + chartSize * 0.15f
                if (displayItems.isEmpty()) {
                    Offset(centerX, houseCentroid.y)
                } else {
                    Offset(centerX, baseY)
                }
            }

            4 -> {
                val baseX = left + chartSize * 0.15f
                if (displayItems.isEmpty()) {
                    Offset(houseCentroid.x, centerY)
                } else {
                    Offset(baseX, centerY)
                }
            }

            7 -> {
                val baseY = bottom - chartSize * 0.15f
                if (displayItems.isEmpty()) {
                    Offset(centerX, houseCentroid.y)
                } else {
                    Offset(centerX, baseY)
                }
            }

            10 -> {
                val baseX = right - chartSize * 0.15f
                if (displayItems.isEmpty()) {
                    Offset(houseCentroid.x, centerY)
                } else {
                    Offset(baseX, centerY)
                }
            }

            2 -> {
                val cornerInsetX = chartSize * 0.22f
                val cornerInsetY = chartSize * 0.048f
                Offset(left + cornerInsetX, top + cornerInsetY)
            }

            6 -> {
                val cornerInsetX = chartSize * 0.22f
                val cornerInsetY = chartSize * 0.048f
                Offset(left + cornerInsetX, bottom - cornerInsetY)
            }

            8 -> {
                val cornerInsetX = chartSize * 0.22f
                val cornerInsetY = chartSize * 0.048f
                Offset(right - cornerInsetX, bottom - cornerInsetY)
            }

            12 -> {
                val cornerInsetX = chartSize * 0.22f
                val cornerInsetY = chartSize * 0.048f
                Offset(right - cornerInsetX, top + cornerInsetY)
            }

            3 -> {
                val sideInsetX = chartSize * 0.055f
                val sideOffsetY = chartSize * 0.22f
                Offset(left + sideInsetX, centerY - sideOffsetY)
            }

            5 -> {
                val sideInsetX = chartSize * 0.055f
                val sideOffsetY = chartSize * 0.22f
                Offset(left + sideInsetX, centerY + sideOffsetY)
            }

            9 -> {
                val sideInsetX = chartSize * 0.055f
                val sideOffsetY = chartSize * 0.22f
                Offset(right - sideInsetX, centerY + sideOffsetY)
            }

            11 -> {
                val sideInsetX = chartSize * 0.055f
                val sideOffsetY = chartSize * 0.22f
                Offset(right - sideInsetX, centerY - sideOffsetY)
            }

            else -> houseCentroid
        }
    }

    private fun calculateContentCenter(
        houseCentroid: Offset,
        houseBounds: HouseBounds,
        houseType: HouseType,
        numberPosition: Offset,
        numberTextSize: Float,
        itemCount: Int,
        size: Float
    ): Offset {
        val numberRadius = numberTextSize * 0.8f

        val offsetFromNumber = when (houseType) {
            HouseType.CORNER -> size * 0.025f
            HouseType.SIDE -> size * 0.030f
            HouseType.DIAMOND -> size * 0.035f
        }

        val directionFromNumber = Offset(
            houseCentroid.x - numberPosition.x,
            houseCentroid.y - numberPosition.y
        )

        val distance = sqrt(directionFromNumber.x * directionFromNumber.x + directionFromNumber.y * directionFromNumber.y)

        return if (distance > 0.01f) {
            val normalizedDir = Offset(directionFromNumber.x / distance, directionFromNumber.y / distance)
            val shift = numberRadius + offsetFromNumber

            Offset(
                numberPosition.x + normalizedDir.x * shift + (houseCentroid.x - numberPosition.x) * 0.6f,
                numberPosition.y + normalizedDir.y * shift + (houseCentroid.y - numberPosition.y) * 0.6f
            )
        } else {
            houseCentroid
        }
    }

    private fun calculateGridLayout(
        itemCount: Int,
        houseType: HouseType,
        houseBounds: HouseBounds,
        size: Float
    ): GridLayout {
        val baseTextSize = size * BASE_TEXT_SIZE_RATIO
        val baseLineHeight = size * BASE_LINE_HEIGHT_RATIO
        val avgItemWidth = baseTextSize * 2.5f

        val maxColsByWidth = (houseBounds.effectiveWidth / avgItemWidth).toInt().coerceIn(1, 3)

        val (columns, rows) = when (houseType) {
            HouseType.CORNER -> {
                when {
                    itemCount <= 2 -> Pair(1, itemCount)
                    itemCount <= 4 -> Pair(2, (itemCount + 1) / 2)
                    itemCount <= 6 -> {
                        if (maxColsByWidth >= 3) Pair(3, 2)
                        else Pair(2, 3)
                    }
                    else -> Pair(minOf(3, maxColsByWidth), (itemCount + 2) / 3)
                }
            }
            HouseType.SIDE -> {
                when {
                    itemCount <= 3 -> Pair(1, itemCount)
                    itemCount <= 6 -> Pair(2, (itemCount + 1) / 2)
                    else -> Pair(minOf(3, maxColsByWidth), (itemCount + 2) / 3)
                }
            }
            HouseType.DIAMOND -> {
                when {
                    itemCount <= 4 -> Pair(1, itemCount)
                    itemCount <= 8 -> Pair(2, (itemCount + 1) / 2)
                    else -> Pair(minOf(3, maxColsByWidth), (itemCount + 2) / 3)
                }
            }
        }

        val actualRows = (itemCount + columns - 1) / columns
        val requiredHeight = actualRows * baseLineHeight
        val requiredWidth = columns * avgItemWidth

        val heightScale = if (requiredHeight > houseBounds.effectiveHeight) {
            houseBounds.effectiveHeight / requiredHeight
        } else 1.0f

        val widthScale = if (requiredWidth > houseBounds.effectiveWidth) {
            houseBounds.effectiveWidth / requiredWidth
        } else 1.0f

        val scaleFactor = minOf(heightScale, widthScale).coerceIn(MIN_SCALE_FACTOR, MAX_SCALE_FACTOR)

        val adjustedTextSize = baseTextSize * scaleFactor
        val adjustedLineHeight = baseLineHeight * scaleFactor

        val columnSpacing = when (houseType) {
            HouseType.CORNER -> houseBounds.effectiveWidth / columns.coerceAtLeast(1) * 0.90f
            HouseType.SIDE -> houseBounds.effectiveWidth / columns.coerceAtLeast(1) * 0.92f
            HouseType.DIAMOND -> houseBounds.effectiveWidth / columns.coerceAtLeast(1) * 0.95f
        }

        return GridLayout(columns, actualRows, adjustedTextSize, adjustedLineHeight, columnSpacing)
    }

    private fun DrawScope.drawHouseContents(
        displayItems: List<HouseDisplayItem>,
        contentCenter: Offset,
        houseBounds: HouseBounds,
        houseType: HouseType,
        houseNum: Int,
        size: Float
    ) {
        val layout = calculateGridLayout(displayItems.size, houseType, houseBounds, size)

        val totalContentHeight = layout.rows * layout.lineHeight
        val startY = contentCenter.y - totalContentHeight / 2 + layout.lineHeight / 2

        displayItems.forEachIndexed { index, item ->
            val col = index % layout.columns
            val row = index / layout.columns

            val xOffset = if (layout.columns > 1) {
                (col - (layout.columns - 1) / 2f) * layout.columnSpacing
            } else {
                0f
            }

            val yOffset = row * layout.lineHeight
            val position = Offset(contentCenter.x + xOffset, startY + yOffset)

            val arrowWidth = if (item.isExalted || item.isDebilitated) {
                layout.textSize * ARROW_SIZE_RATIO * 1.2f
            } else {
                0f
            }

            drawTextWithBackground(
                text = item.text,
                position = position,
                textSize = layout.textSize,
                color = item.color,
                isBold = item.isBold,
                extraWidth = arrowWidth
            )

            when {
                item.isExalted -> drawExaltedArrow(position, layout.textSize, item.text)
                item.isDebilitated -> drawDebilitatedArrow(position, layout.textSize, item.text)
            }
        }
    }

    private fun DrawScope.drawTextWithBackground(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean,
        extraWidth: Float = 0f
    ) {
        val typeface = if (isBold) TYPEFACE_BOLD else TYPEFACE_NORMAL
        textPaint.color = color.toArgb()
        textPaint.textSize = textSize
        textPaint.typeface = typeface

        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.descent() - textPaint.ascent()
        val padding = textSize * TEXT_PADDING_RATIO

        val bgLeft = position.x - textWidth / 2 - padding
        val bgTop = position.y - textHeight / 2 - padding * 0.5f
        val bgWidth = textWidth + padding * 2 + extraWidth
        val bgHeight = textHeight + padding

        drawRect(
            color = TEXT_BACKGROUND_COLOR,
            topLeft = Offset(bgLeft, bgTop),
            size = Size(bgWidth, bgHeight)
        )

        drawContext.canvas.nativeCanvas.apply {
            val textOffset = textHeight / 2 - textPaint.descent()
            drawText(text, position.x, position.y + textOffset, textPaint)
        }
    }

    private fun DrawScope.drawExaltedArrow(
        textPosition: Offset,
        textSize: Float,
        text: String
    ) {
        textPaint.textSize = textSize
        val textWidth = textPaint.measureText(text)

        val arrowSize = textSize * ARROW_SIZE_RATIO
        val arrowCenterX = textPosition.x + textWidth / 2 + arrowSize * 0.8f
        val arrowCenterY = textPosition.y

        val path = Path().apply {
            moveTo(arrowCenterX, arrowCenterY - arrowSize * 0.7f)
            lineTo(arrowCenterX - arrowSize * 0.5f, arrowCenterY + arrowSize * 0.2f)
            lineTo(arrowCenterX - arrowSize * 0.15f, arrowCenterY + arrowSize * 0.2f)
            lineTo(arrowCenterX - arrowSize * 0.15f, arrowCenterY + arrowSize * 0.7f)
            lineTo(arrowCenterX + arrowSize * 0.15f, arrowCenterY + arrowSize * 0.7f)
            lineTo(arrowCenterX + arrowSize * 0.15f, arrowCenterY + arrowSize * 0.2f)
            lineTo(arrowCenterX + arrowSize * 0.5f, arrowCenterY + arrowSize * 0.2f)
            close()
        }

        drawPath(path = path, color = EXALTED_ARROW_COLOR)
    }

    private fun DrawScope.drawDebilitatedArrow(
        textPosition: Offset,
        textSize: Float,
        text: String
    ) {
        textPaint.textSize = textSize
        val textWidth = textPaint.measureText(text)

        val arrowSize = textSize * ARROW_SIZE_RATIO
        val arrowCenterX = textPosition.x + textWidth / 2 + arrowSize * 0.8f
        val arrowCenterY = textPosition.y

        val path = Path().apply {
            moveTo(arrowCenterX, arrowCenterY + arrowSize * 0.7f)
            lineTo(arrowCenterX - arrowSize * 0.5f, arrowCenterY - arrowSize * 0.2f)
            lineTo(arrowCenterX - arrowSize * 0.15f, arrowCenterY - arrowSize * 0.2f)
            lineTo(arrowCenterX - arrowSize * 0.15f, arrowCenterY - arrowSize * 0.7f)
            lineTo(arrowCenterX + arrowSize * 0.15f, arrowCenterY - arrowSize * 0.7f)
            lineTo(arrowCenterX + arrowSize * 0.15f, arrowCenterY - arrowSize * 0.2f)
            lineTo(arrowCenterX + arrowSize * 0.5f, arrowCenterY - arrowSize * 0.2f)
            close()
        }

        drawPath(path = path, color = DEBILITATED_ARROW_COLOR)
    }

    private fun polygonCentroid(points: List<Offset>): Offset {
        if (points.isEmpty()) return Offset.Zero
        if (points.size == 1) return points[0]
        if (points.size == 2) {
            return Offset((points[0].x + points[1].x) / 2f, (points[0].y + points[1].y) / 2f)
        }

        var signedArea = 0.0
        var cx = 0.0
        var cy = 0.0

        for (i in points.indices) {
            val j = (i + 1) % points.size
            val xi = points[i].x.toDouble()
            val yi = points[i].y.toDouble()
            val xj = points[j].x.toDouble()
            val yj = points[j].y.toDouble()

            val cross = xi * yj - xj * yi
            signedArea += cross
            cx += (xi + xj) * cross
            cy += (yi + yj) * cross
        }

        if (abs(signedArea) < 1e-10) {
            val avgX = points.sumOf { it.x.toDouble() } / points.size
            val avgY = points.sumOf { it.y.toDouble() } / points.size
            return Offset(avgX.toFloat(), avgY.toFloat())
        }

        signedArea *= 0.5
        val factor = 1.0 / (6.0 * signedArea)
        return Offset((cx * factor).toFloat(), (cy * factor).toFloat())
    }

    private fun DrawScope.drawTextCentered(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean = false
    ) {
        val typeface = if (isBold) TYPEFACE_BOLD else TYPEFACE_NORMAL
        textPaint.color = color.toArgb()
        textPaint.textSize = textSize
        textPaint.typeface = typeface

        drawContext.canvas.nativeCanvas.apply {
            val textHeight = textPaint.descent() - textPaint.ascent()
            val textOffset = textHeight / 2 - textPaint.descent()
            drawText(text, position.x, position.y + textOffset, textPaint)
        }
    }

    fun createChartBitmap(chart: VedicChart, width: Int, height: Int, density: Density): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density,
            LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawNorthIndianChart(this, chart, min(width, height).toFloat())
        }

        return bitmap
    }

    fun createDivisionalChartBitmap(
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        chartTitle: String,
        width: Int,
        height: Int,
        density: Density
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density,
            LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawDivisionalChart(this, planetPositions, ascendantLongitude, min(width, height).toFloat(), chartTitle)
        }

        return bitmap
    }

    fun drawSouthIndianChart(drawScope: DrawScope, chart: VedicChart, size: Float) {
        drawNorthIndianChart(drawScope, chart, size, "Lagna")
    }

    fun DrawScope.drawChartLegend(
        chartBottom: Float,
        chartLeft: Float,
        chartWidth: Float,
        textSize: Float
    ) {
        val legendY = chartBottom + textSize * 1.5f
        val legendSpacing = chartWidth / 5f

        val legendStartX = chartLeft + legendSpacing / 2

        drawTextCentered(
            text = "$SYMBOL_RETROGRADE Retro",
            position = Offset(legendStartX, legendY),
            textSize = textSize * 0.75f,
            color = HOUSE_NUMBER_COLOR,
            isBold = false
        )

        drawTextCentered(
            text = "$SYMBOL_COMBUST Comb",
            position = Offset(legendStartX + legendSpacing, legendY),
            textSize = textSize * 0.75f,
            color = HOUSE_NUMBER_COLOR,
            isBold = false
        )

        drawTextCentered(
            text = "$SYMBOL_VARGOTTAMA Vargo",
            position = Offset(legendStartX + legendSpacing * 2, legendY),
            textSize = textSize * 0.75f,
            color = HOUSE_NUMBER_COLOR,
            isBold = false
        )

        val exaltedX = legendStartX + legendSpacing * 3
        drawLegendExaltedArrow(Offset(exaltedX - textSize * 1.2f, legendY), textSize)
        drawTextCentered(
            text = "Exalt",
            position = Offset(exaltedX + textSize * 0.3f, legendY),
            textSize = textSize * 0.75f,
            color = HOUSE_NUMBER_COLOR,
            isBold = false
        )

        val debilitatedX = legendStartX + legendSpacing * 4
        drawLegendDebilitatedArrow(Offset(debilitatedX - textSize * 1.2f, legendY), textSize)
        drawTextCentered(
            text = "Deb",
            position = Offset(debilitatedX + textSize * 0.2f, legendY),
            textSize = textSize * 0.75f,
            color = HOUSE_NUMBER_COLOR,
            isBold = false
        )
    }

    private fun DrawScope.drawLegendExaltedArrow(position: Offset, textSize: Float) {
        val arrowSize = textSize * 0.4f
        val path = Path().apply {
            moveTo(position.x, position.y - arrowSize * 0.6f)
            lineTo(position.x - arrowSize * 0.45f, position.y + arrowSize * 0.15f)
            lineTo(position.x - arrowSize * 0.12f, position.y + arrowSize * 0.15f)
            lineTo(position.x - arrowSize * 0.12f, position.y + arrowSize * 0.6f)
            lineTo(position.x + arrowSize * 0.12f, position.y + arrowSize * 0.6f)
            lineTo(position.x + arrowSize * 0.12f, position.y + arrowSize * 0.15f)
            lineTo(position.x + arrowSize * 0.45f, position.y + arrowSize * 0.15f)
            close()
        }
        drawPath(path = path, color = EXALTED_ARROW_COLOR)
    }

    private fun DrawScope.drawLegendDebilitatedArrow(position: Offset, textSize: Float) {
        val arrowSize = textSize * 0.4f
        val path = Path().apply {
            moveTo(position.x, position.y + arrowSize * 0.6f)
            lineTo(position.x - arrowSize * 0.45f, position.y - arrowSize * 0.15f)
            lineTo(position.x - arrowSize * 0.12f, position.y - arrowSize * 0.15f)
            lineTo(position.x - arrowSize * 0.12f, position.y - arrowSize * 0.6f)
            lineTo(position.x + arrowSize * 0.12f, position.y - arrowSize * 0.6f)
            lineTo(position.x + arrowSize * 0.12f, position.y - arrowSize * 0.15f)
            lineTo(position.x + arrowSize * 0.45f, position.y - arrowSize * 0.15f)
            close()
        }
        drawPath(path = path, color = DEBILITATED_ARROW_COLOR)
    }

    fun drawChartWithLegend(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna",
        showLegend: Boolean = true
    ) {
        with(drawScope) {
            val legendHeight = if (showLegend) size * 0.08f else 0f
            val chartSize = size - legendHeight

            drawNorthIndianChart(this, chart, chartSize, chartTitle)

            if (showLegend) {
                val padding = chartSize * 0.02f
                val chartWidth = chartSize - (padding * 2)
                val textSize = chartSize * 0.028f

                drawRect(
                    color = BACKGROUND_COLOR,
                    topLeft = Offset(0f, chartSize),
                    size = Size(size, legendHeight)
                )

                drawChartLegend(
                    chartBottom = chartSize,
                    chartLeft = padding,
                    chartWidth = chartWidth,
                    textSize = textSize
                )
            }
        }
    }
}