package com.astro.storm.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.unit.Density
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.AshtakavargaCalculator
import com.astro.storm.ephemeris.AspectCalculator
import com.astro.storm.ephemeris.DivisionalChartCalculator
import com.astro.storm.ephemeris.ShadbalaCalculator
import com.astro.storm.ephemeris.YogaCalculator
import com.astro.storm.ui.chart.ChartRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Comprehensive Chart Export System
 *
 * Provides multiple export formats:
 * 1. PDF Report - Complete professional report with charts and analysis
 * 2. JSON Export - Full data in structured JSON format
 * 3. CSV Export - Tabular data for research purposes
 * 4. Printable Chart Sheets - A4/Letter format ready to print
 * 5. Plain Text Report - Interpretive reports
 * 6. Share-ready Images - With optional watermark
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
class ChartExporter(private val context: Context) {

    private val chartRenderer = ChartRenderer()

    companion object {
        private const val PDF_PAGE_WIDTH = 595 // A4 width in points (72 dpi)
        private const val PDF_PAGE_HEIGHT = 842 // A4 height in points
        private const val PDF_MARGIN = 40
        private const val CHART_SIZE = 400

        private const val WATERMARK_TEXT = "AstroStorm"
        private const val WATERMARK_ALPHA = 80

        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        private val displayDateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.US)
    }

    /**
     * Export format options
     */
    enum class ExportFormat {
        PDF,
        JSON,
        CSV,
        PNG,
        TEXT
    }

    /**
     * Export result
     */
    sealed class ExportResult {
        data class Success(val path: String, val format: ExportFormat) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    /**
     * PDF export options
     */
    data class PdfExportOptions(
        val includeChart: Boolean = true,
        val includeNavamsa: Boolean = true,
        val includePlanetaryPositions: Boolean = true,
        val includeAspects: Boolean = true,
        val includeShadbala: Boolean = true,
        val includeYogas: Boolean = true,
        val includeAshtakavarga: Boolean = true,
        val includeDashas: Boolean = false,
        val pageSize: PageSize = PageSize.A4
    )

    enum class PageSize(val width: Int, val height: Int) {
        A4(595, 842),
        LETTER(612, 792)
    }

    /**
     * Image export options
     */
    data class ImageExportOptions(
        val width: Int = 2048,
        val height: Int = 2048,
        val addWatermark: Boolean = false,
        val watermarkText: String = WATERMARK_TEXT,
        val includeTitle: Boolean = true,
        val includeLegend: Boolean = true
    )

    // ==================== PDF EXPORT ====================

    /**
     * Generate comprehensive PDF report
     */
    suspend fun exportToPdf(
        chart: VedicChart,
        options: PdfExportOptions = PdfExportOptions(),
        density: Density
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            var pageNumber = 1

            // Page 1: Chart and Basic Info
            pageNumber = addChartPage(document, chart, options, density, pageNumber)

            // Page 2: Planetary Positions
            if (options.includePlanetaryPositions) {
                pageNumber = addPlanetaryPositionsPage(document, chart, options, pageNumber)
            }

            // Page 3: Aspects and Yogas
            if (options.includeAspects || options.includeYogas) {
                pageNumber = addAspectsYogasPage(document, chart, options, pageNumber)
            }

            // Page 4: Shadbala
            if (options.includeShadbala) {
                pageNumber = addShadbalaPage(document, chart, options, pageNumber)
            }

            // Page 5: Ashtakavarga
            if (options.includeAshtakavarga) {
                pageNumber = addAshtakavargaPage(document, chart, options, pageNumber)
            }

            // Save the document
            val fileName = "AstroStorm_${chart.birthData.name.replace(" ", "_")}_${dateFormatter.format(Date())}.pdf"
            val path = saveDocument(document, fileName)

            document.close()

            ExportResult.Success(path, ExportFormat.PDF)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export PDF: ${e.message}")
        }
    }

    private fun addChartPage(
        document: PdfDocument,
        chart: VedicChart,
        options: PdfExportOptions,
        density: Density,
        pageNumber: Int
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(options.pageSize.width, options.pageSize.height, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        var yPos = PDF_MARGIN.toFloat()

        // Title
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.rgb(70, 70, 70)
        canvas.drawText("VEDIC BIRTH CHART REPORT", PDF_MARGIN.toFloat(), yPos + 24f, paint)
        yPos += 50f

        // Birth Information Box
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(50, 50, 50)

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.rgb(180, 140, 100)
            strokeWidth = 1f
        }
        canvas.drawRect(PDF_MARGIN.toFloat(), yPos, (options.pageSize.width - PDF_MARGIN).toFloat(), yPos + 80f, boxPaint)

        yPos += 15f
        canvas.drawText("Name: ${chart.birthData.name}", PDF_MARGIN + 10f, yPos + 12f, paint)
        yPos += 18f
        canvas.drawText("Date & Time: ${chart.birthData.dateTime}", PDF_MARGIN + 10f, yPos + 12f, paint)
        yPos += 18f
        canvas.drawText("Location: ${chart.birthData.location}", PDF_MARGIN + 10f, yPos + 12f, paint)
        yPos += 18f
        canvas.drawText("Coordinates: ${formatCoordinate(chart.birthData.latitude.toDouble(), true)}, ${formatCoordinate(chart.birthData.longitude.toDouble(), false)}", PDF_MARGIN + 10f, yPos + 12f, paint)

        yPos += 40f

        // Chart Image
        if (options.includeChart) {
            val chartBitmap = chartRenderer.createChartBitmap(chart, CHART_SIZE, CHART_SIZE, density)
            val chartX = (options.pageSize.width - CHART_SIZE) / 2f
            canvas.drawBitmap(chartBitmap, chartX, yPos, null)
            yPos += CHART_SIZE + 20f

            paint.textSize = 10f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Rashi Chart (D1)", options.pageSize.width / 2f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 20f
        }

        // Navamsa Chart (smaller)
        if (options.includeNavamsa) {
            val navamsaData = DivisionalChartCalculator.calculateNavamsa(chart)
            val navamsaBitmap = chartRenderer.createDivisionalChartBitmap(
                navamsaData.planetPositions,
                navamsaData.ascendantLongitude,
                "Navamsa",
                250, 250, density
            )
            val navamsaX = (options.pageSize.width - 250) / 2f
            canvas.drawBitmap(navamsaBitmap, navamsaX, yPos, null)
            yPos += 260f

            paint.textSize = 10f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Navamsa Chart (D9)", options.pageSize.width / 2f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // Footer
        addPageFooter(canvas, options.pageSize, pageNumber, paint)

        document.finishPage(page)
        return pageNumber + 1
    }

    private fun addPlanetaryPositionsPage(
        document: PdfDocument,
        chart: VedicChart,
        options: PdfExportOptions,
        pageNumber: Int
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(options.pageSize.width, options.pageSize.height, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        var yPos = PDF_MARGIN.toFloat()

        // Title
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.rgb(70, 70, 70)
        canvas.drawText("PLANETARY POSITIONS", PDF_MARGIN.toFloat(), yPos + 18f, paint)
        yPos += 40f

        // Table Header
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val columns = listOf("Planet", "Sign", "Degree", "Nakshatra", "Pada", "House", "Status")
        val columnWidths = listOf(60f, 80f, 70f, 100f, 40f, 50f, 80f)
        var xPos = PDF_MARGIN.toFloat()

        // Draw header background
        val headerPaint = Paint().apply {
            color = Color.rgb(240, 230, 210)
            style = Paint.Style.FILL
        }
        canvas.drawRect(PDF_MARGIN.toFloat(), yPos, (options.pageSize.width - PDF_MARGIN).toFloat(), yPos + 20f, headerPaint)

        columns.forEachIndexed { index, column ->
            canvas.drawText(column, xPos + 5f, yPos + 14f, paint)
            xPos += columnWidths[index]
        }
        yPos += 25f

        // Table Data
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 9f

        chart.planetPositions.forEach { position ->
            xPos = PDF_MARGIN.toFloat()

            val degreeInSign = position.longitude % 30.0
            val deg = degreeInSign.toInt()
            val min = ((degreeInSign - deg) * 60).toInt()
            val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()

            val status = buildString {
                if (position.isRetrograde) append("R ")
                if (isExalted(position.planet, position.sign)) append("Exalted ")
                if (isDebilitated(position.planet, position.sign)) append("Debilitated ")
            }.trim().ifEmpty { "-" }

            val data = listOf(
                position.planet.displayName,
                position.sign.displayName,
                "$deg° $min' $sec\"",
                position.nakshatra.displayName,
                position.nakshatraPada.toString(),
                position.house.toString(),
                status
            )

            data.forEachIndexed { index, value ->
                canvas.drawText(value, xPos + 5f, yPos + 12f, paint)
                xPos += columnWidths[index]
            }

            // Draw row separator
            paint.color = Color.rgb(220, 220, 220)
            paint.strokeWidth = 0.5f
            canvas.drawLine(PDF_MARGIN.toFloat(), yPos + 18f, (options.pageSize.width - PDF_MARGIN).toFloat(), yPos + 18f, paint)
            paint.color = Color.rgb(50, 50, 50)

            yPos += 20f
        }

        // Astronomical Data Section
        yPos += 20f
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("ASTRONOMICAL DATA", PDF_MARGIN.toFloat(), yPos + 14f, paint)
        yPos += 30f

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val astroData = listOf(
            "Julian Day: ${String.format("%.6f", chart.julianDay)}",
            "Ayanamsa: ${chart.ayanamsaName}",
            "Ayanamsa Value: ${formatDegree(chart.ayanamsa)}",
            "Ascendant: ${formatDegree(chart.ascendant)} (${ZodiacSign.fromLongitude(chart.ascendant).displayName})",
            "Midheaven: ${formatDegree(chart.midheaven)} (${ZodiacSign.fromLongitude(chart.midheaven).displayName})",
            "House System: ${chart.houseSystem.displayName}"
        )

        astroData.forEach { line ->
            canvas.drawText(line, PDF_MARGIN.toFloat(), yPos + 12f, paint)
            yPos += 18f
        }

        // House Cusps Table
        yPos += 20f
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("HOUSE CUSPS", PDF_MARGIN.toFloat(), yPos + 14f, paint)
        yPos += 30f

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        // Two columns for house cusps
        val col1X = PDF_MARGIN.toFloat()
        val col2X = options.pageSize.width / 2f

        chart.houseCusps.forEachIndexed { index, cusp ->
            val houseNum = index + 1
            val sign = ZodiacSign.fromLongitude(cusp)
            val text = "House $houseNum: ${formatDegree(cusp)} (${sign.abbreviation})"

            if (houseNum <= 6) {
                canvas.drawText(text, col1X, yPos + 12f, paint)
            } else {
                canvas.drawText(text, col2X, yPos - (6 * 18f) + 12f, paint)
            }

            if (houseNum <= 6) yPos += 18f
        }

        addPageFooter(canvas, options.pageSize, pageNumber, paint)
        document.finishPage(page)
        return pageNumber + 1
    }

    private fun addAspectsYogasPage(
        document: PdfDocument,
        chart: VedicChart,
        options: PdfExportOptions,
        pageNumber: Int
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(options.pageSize.width, options.pageSize.height, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        var yPos = PDF_MARGIN.toFloat()

        if (options.includeYogas) {
            // Yogas Section
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.color = Color.rgb(70, 70, 70)
            canvas.drawText("YOGA ANALYSIS", PDF_MARGIN.toFloat(), yPos + 18f, paint)
            yPos += 35f

            val yogaAnalysis = YogaCalculator.calculateYogas(chart)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

            // Summary
            canvas.drawText("Total Yogas Found: ${yogaAnalysis.allYogas.size}", PDF_MARGIN.toFloat(), yPos + 12f, paint)
            yPos += 18f
            canvas.drawText("Overall Yoga Strength: ${String.format("%.1f", yogaAnalysis.overallYogaStrength)}%", PDF_MARGIN.toFloat(), yPos + 12f, paint)
            yPos += 25f

            // List top yogas
            val topYogas = yogaAnalysis.allYogas
                .filter { it.isAuspicious }
                .sortedByDescending { it.strengthPercentage }
                .take(10)

            if (topYogas.isNotEmpty()) {
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText("Key Yogas:", PDF_MARGIN.toFloat(), yPos + 12f, paint)
                yPos += 20f

                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

                topYogas.forEach { yoga ->
                    val planets = yoga.planets.joinToString(", ") { it.displayName }
                    canvas.drawText("• ${yoga.name} ($planets)", PDF_MARGIN.toFloat() + 10f, yPos + 10f, paint)
                    yPos += 14f

                    paint.color = Color.rgb(100, 100, 100)
                    val effectText = if (yoga.effects.length > 80) yoga.effects.substring(0, 77) + "..." else yoga.effects
                    canvas.drawText("  ${yoga.strength.displayName}: $effectText", PDF_MARGIN.toFloat() + 15f, yPos + 10f, paint)
                    paint.color = Color.rgb(50, 50, 50)
                    yPos += 16f

                    if (yPos > options.pageSize.height - 100) return@forEach
                }
            }

            // Negative Yogas if any
            if (yogaAnalysis.negativeYogas.isNotEmpty() && yPos < options.pageSize.height - 150) {
                yPos += 15f
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.color = Color.rgb(150, 50, 50)
                canvas.drawText("Challenging Yogas:", PDF_MARGIN.toFloat(), yPos + 12f, paint)
                yPos += 20f

                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paint.color = Color.rgb(50, 50, 50)

                yogaAnalysis.negativeYogas.take(3).forEach { yoga ->
                    canvas.drawText("• ${yoga.name}", PDF_MARGIN.toFloat() + 10f, yPos + 10f, paint)
                    yPos += 14f
                    if (yoga.cancellationFactors.isNotEmpty()) {
                        canvas.drawText("  Mitigated by: ${yoga.cancellationFactors.first()}", PDF_MARGIN.toFloat() + 15f, yPos + 10f, paint)
                        yPos += 14f
                    }
                }
            }
        }

        if (options.includeAspects && yPos < options.pageSize.height - 200) {
            yPos += 25f

            // Aspects Section
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.color = Color.rgb(70, 70, 70)
            canvas.drawText("PLANETARY ASPECTS", PDF_MARGIN.toFloat(), yPos + 18f, paint)
            yPos += 35f

            val aspectMatrix = AspectCalculator.calculateAspectMatrix(chart)

            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

            // Show significant aspects
            val significantAspects = aspectMatrix.aspects
                .filter { it.drishtiBala > 0.5 }
                .sortedByDescending { it.drishtiBala }
                .take(15)

            significantAspects.forEach { aspect ->
                val applying = if (aspect.isApplying) "Applying" else "Separating"
                val text = "${aspect.aspectingPlanet.displayName} ${aspect.aspectType.displayName} ${aspect.aspectedPlanet.displayName} " +
                        "(Orb: ${String.format("%.1f", aspect.exactOrb)}°, $applying)"
                canvas.drawText(text, PDF_MARGIN.toFloat(), yPos + 10f, paint)
                yPos += 14f

                if (yPos > options.pageSize.height - 80) return@forEach
            }
        }

        addPageFooter(canvas, options.pageSize, pageNumber, paint)
        document.finishPage(page)
        return pageNumber + 1
    }

    private fun addShadbalaPage(
        document: PdfDocument,
        chart: VedicChart,
        options: PdfExportOptions,
        pageNumber: Int
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(options.pageSize.width, options.pageSize.height, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        var yPos = PDF_MARGIN.toFloat()

        // Title
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.rgb(70, 70, 70)
        canvas.drawText("SHADBALA ANALYSIS", PDF_MARGIN.toFloat(), yPos + 18f, paint)
        yPos += 40f

        val shadbala = ShadbalaCalculator.calculateShadbala(chart)

        // Summary
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        canvas.drawText("Overall Chart Strength: ${String.format("%.1f", shadbala.overallStrengthScore)}%", PDF_MARGIN.toFloat(), yPos + 12f, paint)
        yPos += 20f
        canvas.drawText("Strongest Planet: ${shadbala.strongestPlanet.displayName}", PDF_MARGIN.toFloat(), yPos + 12f, paint)
        yPos += 20f
        canvas.drawText("Weakest Planet: ${shadbala.weakestPlanet.displayName}", PDF_MARGIN.toFloat(), yPos + 12f, paint)
        yPos += 35f

        // Detailed Table
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val columns = listOf("Planet", "Total Rupas", "Required", "%", "Rating")
        val columnWidths = listOf(80f, 80f, 70f, 60f, 120f)
        var xPos = PDF_MARGIN.toFloat()

        // Header
        val headerPaint = Paint().apply {
            color = Color.rgb(240, 230, 210)
            style = Paint.Style.FILL
        }
        canvas.drawRect(PDF_MARGIN.toFloat(), yPos, (options.pageSize.width - PDF_MARGIN).toFloat(), yPos + 22f, headerPaint)

        columns.forEachIndexed { index, column ->
            canvas.drawText(column, xPos + 5f, yPos + 16f, paint)
            xPos += columnWidths[index]
        }
        yPos += 28f

        // Data
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        shadbala.getPlanetsByStrength().forEach { planetShadbala ->
            xPos = PDF_MARGIN.toFloat()

            val data = listOf(
                planetShadbala.planet.displayName,
                String.format("%.2f", planetShadbala.totalRupas),
                String.format("%.2f", planetShadbala.requiredRupas),
                String.format("%.0f%%", planetShadbala.percentageOfRequired),
                planetShadbala.strengthRating.displayName
            )

            // Color code based on strength
            paint.color = when {
                planetShadbala.percentageOfRequired >= 100 -> Color.rgb(0, 100, 0)
                planetShadbala.percentageOfRequired >= 80 -> Color.rgb(50, 50, 50)
                else -> Color.rgb(150, 50, 50)
            }

            data.forEachIndexed { index, value ->
                canvas.drawText(value, xPos + 5f, yPos + 14f, paint)
                xPos += columnWidths[index]
            }

            paint.color = Color.rgb(220, 220, 220)
            canvas.drawLine(PDF_MARGIN.toFloat(), yPos + 20f, (options.pageSize.width - PDF_MARGIN).toFloat(), yPos + 20f, paint)
            paint.color = Color.rgb(50, 50, 50)

            yPos += 24f
        }

        // Detailed breakdown for strongest planet
        yPos += 25f
        val strongest = shadbala.planetaryStrengths[shadbala.strongestPlanet]
        if (strongest != null) {
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("${strongest.planet.displayName} Strength Breakdown:", PDF_MARGIN.toFloat(), yPos + 12f, paint)
            yPos += 25f

            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

            val breakdown = listOf(
                "Sthana Bala (Positional): ${String.format("%.1f", strongest.sthanaBala.total)} virupas",
                "Dig Bala (Directional): ${String.format("%.1f", strongest.digBala)} virupas",
                "Kala Bala (Temporal): ${String.format("%.1f", strongest.kalaBala.total)} virupas",
                "Chesta Bala (Motional): ${String.format("%.1f", strongest.chestaBala)} virupas",
                "Naisargika Bala (Natural): ${String.format("%.1f", strongest.naisargikaBala)} virupas",
                "Drik Bala (Aspectual): ${String.format("%.1f", strongest.drikBala)} virupas"
            )

            breakdown.forEach { line ->
                canvas.drawText(line, PDF_MARGIN.toFloat() + 15f, yPos + 10f, paint)
                yPos += 16f
            }
        }

        addPageFooter(canvas, options.pageSize, pageNumber, paint)
        document.finishPage(page)
        return pageNumber + 1
    }

    private fun addAshtakavargaPage(
        document: PdfDocument,
        chart: VedicChart,
        options: PdfExportOptions,
        pageNumber: Int
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(options.pageSize.width, options.pageSize.height, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        var yPos = PDF_MARGIN.toFloat()

        // Title
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.rgb(70, 70, 70)
        canvas.drawText("ASHTAKAVARGA ANALYSIS", PDF_MARGIN.toFloat(), yPos + 18f, paint)
        yPos += 40f

        val ashtakavarga = AshtakavargaCalculator.calculateAshtakavarga(chart)

        // SAV Summary
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("Sarvashtakavarga (Combined Strength)", PDF_MARGIN.toFloat(), yPos + 12f, paint)
        yPos += 25f

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        // SAV Table Header
        val signWidth = 45f
        var xPos = PDF_MARGIN.toFloat() + 30f

        paint.textSize = 8f
        ZodiacSign.entries.forEach { sign ->
            canvas.drawText(sign.abbreviation, xPos, yPos + 10f, paint)
            xPos += signWidth
        }
        yPos += 20f

        // SAV Values
        xPos = PDF_MARGIN.toFloat()
        canvas.drawText("SAV:", xPos, yPos + 10f, paint)
        xPos += 30f

        ZodiacSign.entries.forEach { sign ->
            val bindus = ashtakavarga.sarvashtakavarga.getBindusForSign(sign)
            paint.color = when {
                bindus >= 30 -> Color.rgb(0, 100, 0)
                bindus >= 25 -> Color.rgb(50, 50, 50)
                else -> Color.rgb(150, 50, 50)
            }
            canvas.drawText(bindus.toString(), xPos, yPos + 10f, paint)
            xPos += signWidth
        }
        paint.color = Color.rgb(50, 50, 50)
        yPos += 25f

        // BAV for each planet
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("Bhinnashtakavarga (Individual Planet Bindus)", PDF_MARGIN.toFloat(), yPos + 12f, paint)
        yPos += 25f

        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        ashtakavarga.bhinnashtakavarga.forEach { (planet, bav) ->
            xPos = PDF_MARGIN.toFloat()
            canvas.drawText(planet.symbol + ":", xPos, yPos + 10f, paint)
            xPos += 30f

            ZodiacSign.entries.forEach { sign ->
                val bindus = bav.getBindusForSign(sign)
                canvas.drawText(bindus.toString(), xPos, yPos + 10f, paint)
                xPos += signWidth
            }

            // Total
            canvas.drawText("=${bav.totalBindus}", xPos, yPos + 10f, paint)
            yPos += 16f
        }

        yPos += 20f

        // Transit Interpretation Guide
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("Transit Interpretation Guide:", PDF_MARGIN.toFloat(), yPos + 12f, paint)
        yPos += 22f

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val guide = listOf(
            "• SAV 30+ bindus: Excellent for transits - major positive events",
            "• SAV 28-29 bindus: Good for transits - favorable outcomes",
            "• SAV 25-27 bindus: Average - mixed results",
            "• SAV below 25: Challenging - caution advised during transits",
            "",
            "• BAV 5+ bindus: Planet transit through this sign is highly beneficial",
            "• BAV 4 bindus: Good results from planet transit",
            "• BAV 3 bindus: Average results",
            "• BAV 0-2 bindus: Difficult transit period for that planet"
        )

        guide.forEach { line ->
            canvas.drawText(line, PDF_MARGIN.toFloat() + 10f, yPos + 10f, paint)
            yPos += 14f
        }

        addPageFooter(canvas, options.pageSize, pageNumber, paint)
        document.finishPage(page)
        return pageNumber + 1
    }

    private fun addPageFooter(canvas: Canvas, pageSize: PageSize, pageNumber: Int, paint: Paint) {
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(128, 128, 128)

        val footerY = pageSize.height - 25f
        canvas.drawText("Generated by AstroStorm - Ultra-Precision Vedic Astrology", PDF_MARGIN.toFloat(), footerY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber", (pageSize.width - PDF_MARGIN).toFloat(), footerY, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun saveDocument(document: PdfDocument, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/AstroStorm")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            ) ?: throw Exception("Failed to create file")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }

            uri.toString()
        } else {
            @Suppress("DEPRECATION")
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val astroStormDir = File(documentsDir, "AstroStorm")
            if (!astroStormDir.exists()) astroStormDir.mkdirs()

            val file = File(astroStormDir, fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }

            file.absolutePath
        }
    }

    // ==================== JSON EXPORT ====================

    /**
     * Export complete chart data to JSON
     */
    suspend fun exportToJson(chart: VedicChart): ExportResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject()

            // Birth Data
            val birthDataJson = JSONObject().apply {
                put("name", chart.birthData.name)
                put("dateTime", chart.birthData.dateTime.toString())
                put("latitude", chart.birthData.latitude)
                put("longitude", chart.birthData.longitude)
                put("timezone", chart.birthData.timezone)
                put("location", chart.birthData.location)
            }
            json.put("birthData", birthDataJson)

            // Astronomical Data
            val astroJson = JSONObject().apply {
                put("julianDay", chart.julianDay)
                put("ayanamsa", chart.ayanamsa)
                put("ayanamsaName", chart.ayanamsaName)
                put("ascendant", chart.ascendant)
                put("ascendantSign", ZodiacSign.fromLongitude(chart.ascendant).name)
                put("midheaven", chart.midheaven)
                put("midheavenSign", ZodiacSign.fromLongitude(chart.midheaven).name)
                put("houseSystem", chart.houseSystem.name)
            }
            json.put("astronomicalData", astroJson)

            // Planetary Positions
            val planetsArray = JSONArray()
            chart.planetPositions.forEach { pos ->
                val planetJson = JSONObject().apply {
                    put("planet", pos.planet.name)
                    put("longitude", pos.longitude)
                    put("latitude", pos.latitude)
                    put("sign", pos.sign.name)
                    put("signDisplayName", pos.sign.displayName)
                    put("degreeInSign", pos.longitude % 30.0)
                    put("degree", pos.degree)
                    put("minutes", pos.minutes)
                    put("seconds", pos.seconds)
                    put("house", pos.house)
                    put("nakshatra", pos.nakshatra.name)
                    put("nakshatraDisplayName", pos.nakshatra.displayName)
                    put("nakshatraPada", pos.nakshatraPada)
                    put("isRetrograde", pos.isRetrograde)
                    put("speed", pos.speed)
                    put("distance", pos.distance)
                }
                planetsArray.put(planetJson)
            }
            json.put("planetaryPositions", planetsArray)

            // House Cusps
            val cuspsArray = JSONArray()
            chart.houseCusps.forEachIndexed { index, cusp ->
                val cuspJson = JSONObject().apply {
                    put("house", index + 1)
                    put("cusp", cusp)
                    put("sign", ZodiacSign.fromLongitude(cusp).name)
                }
                cuspsArray.put(cuspJson)
            }
            json.put("houseCusps", cuspsArray)

            // Yogas
            val yogaAnalysis = YogaCalculator.calculateYogas(chart)
            val yogasArray = JSONArray()
            yogaAnalysis.allYogas.forEach { yoga ->
                val yogaJson = JSONObject().apply {
                    put("name", yoga.name)
                    put("sanskritName", yoga.sanskritName)
                    put("category", yoga.category.name)
                    put("planets", JSONArray(yoga.planets.map { it.name }))
                    put("houses", JSONArray(yoga.houses))
                    put("description", yoga.description)
                    put("effects", yoga.effects)
                    put("strength", yoga.strength.name)
                    put("strengthPercentage", yoga.strengthPercentage)
                    put("isAuspicious", yoga.isAuspicious)
                    put("activationPeriod", yoga.activationPeriod)
                }
                yogasArray.put(yogaJson)
            }
            json.put("yogas", yogasArray)

            // Shadbala
            val shadbala = ShadbalaCalculator.calculateShadbala(chart)
            val shadbalaJson = JSONObject().apply {
                put("overallStrengthScore", shadbala.overallStrengthScore)
                put("strongestPlanet", shadbala.strongestPlanet.name)
                put("weakestPlanet", shadbala.weakestPlanet.name)

                val strengthsArray = JSONArray()
                shadbala.planetaryStrengths.forEach { (planet, strength) ->
                    val strengthJson = JSONObject().apply {
                        put("planet", planet.name)
                        put("totalRupas", strength.totalRupas)
                        put("totalVirupas", strength.totalVirupas)
                        put("requiredRupas", strength.requiredRupas)
                        put("percentageOfRequired", strength.percentageOfRequired)
                        put("strengthRating", strength.strengthRating.name)
                        put("sthanaBala", strength.sthanaBala.total)
                        put("digBala", strength.digBala)
                        put("kalaBala", strength.kalaBala.total)
                        put("chestaBala", strength.chestaBala)
                        put("naisargikaBala", strength.naisargikaBala)
                        put("drikBala", strength.drikBala)
                    }
                    strengthsArray.put(strengthJson)
                }
                put("planetaryStrengths", strengthsArray)
            }
            json.put("shadbala", shadbalaJson)

            // Ashtakavarga
            val ashtakavarga = AshtakavargaCalculator.calculateAshtakavarga(chart)
            val ashtakavargaJson = JSONObject().apply {
                val savJson = JSONObject()
                ZodiacSign.entries.forEach { sign ->
                    savJson.put(sign.name, ashtakavarga.sarvashtakavarga.getBindusForSign(sign))
                }
                put("sarvashtakavarga", savJson)
                put("savTotal", ashtakavarga.sarvashtakavarga.totalBindus)
                put("strongestSign", ashtakavarga.sarvashtakavarga.strongestSign.name)
                put("weakestSign", ashtakavarga.sarvashtakavarga.weakestSign.name)

                val bavJson = JSONObject()
                ashtakavarga.bhinnashtakavarga.forEach { (planet, bav) ->
                    val planetBavJson = JSONObject()
                    ZodiacSign.entries.forEach { sign ->
                        planetBavJson.put(sign.name, bav.getBindusForSign(sign))
                    }
                    planetBavJson.put("total", bav.totalBindus)
                    bavJson.put(planet.name, planetBavJson)
                }
                put("bhinnashtakavarga", bavJson)
            }
            json.put("ashtakavarga", ashtakavargaJson)

            // Metadata
            val metadataJson = JSONObject().apply {
                put("generatedAt", System.currentTimeMillis())
                put("generatedBy", "AstroStorm")
                put("version", "1.0")
                put("calculationEngine", "Swiss Ephemeris (JPL Mode)")
            }
            json.put("metadata", metadataJson)

            // Save to file
            val fileName = "AstroStorm_${chart.birthData.name.replace(" ", "_")}_${dateFormatter.format(Date())}.json"
            val path = saveJsonFile(json.toString(2), fileName)

            ExportResult.Success(path, ExportFormat.JSON)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export JSON: ${e.message}")
        }
    }

    private fun saveJsonFile(content: String, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/AstroStorm")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            ) ?: throw Exception("Failed to create file")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            uri.toString()
        } else {
            @Suppress("DEPRECATION")
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val astroStormDir = File(documentsDir, "AstroStorm")
            if (!astroStormDir.exists()) astroStormDir.mkdirs()

            val file = File(astroStormDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(content)
            }

            file.absolutePath
        }
    }

    // ==================== CSV EXPORT ====================

    /**
     * Export chart data to CSV for research purposes
     */
    suspend fun exportToCsv(chart: VedicChart): ExportResult = withContext(Dispatchers.IO) {
        try {
            val csvBuilder = StringBuilder()

            // Planetary Positions CSV
            csvBuilder.appendLine("PLANETARY POSITIONS")
            csvBuilder.appendLine("Planet,Longitude,Sign,DegreeInSign,House,Nakshatra,Pada,Retrograde,Speed")

            chart.planetPositions.forEach { pos ->
                csvBuilder.appendLine(
                    "${pos.planet.displayName},${pos.longitude},${pos.sign.displayName}," +
                            "${pos.longitude % 30.0},${pos.house},${pos.nakshatra.displayName}," +
                            "${pos.nakshatraPada},${pos.isRetrograde},${pos.speed}"
                )
            }

            csvBuilder.appendLine()
            csvBuilder.appendLine("HOUSE CUSPS")
            csvBuilder.appendLine("House,Cusp,Sign")

            chart.houseCusps.forEachIndexed { index, cusp ->
                csvBuilder.appendLine("${index + 1},$cusp,${ZodiacSign.fromLongitude(cusp).displayName}")
            }

            csvBuilder.appendLine()
            csvBuilder.appendLine("SHADBALA")
            csvBuilder.appendLine("Planet,TotalRupas,RequiredRupas,Percentage,SthanaBala,DigBala,KalaBala,ChestaBala,NaisargikaBala,DrikBala")

            val shadbala = ShadbalaCalculator.calculateShadbala(chart)
            shadbala.planetaryStrengths.forEach { (planet, strength) ->
                csvBuilder.appendLine(
                    "${planet.displayName},${strength.totalRupas},${strength.requiredRupas}," +
                            "${strength.percentageOfRequired},${strength.sthanaBala.total},${strength.digBala}," +
                            "${strength.kalaBala.total},${strength.chestaBala},${strength.naisargikaBala},${strength.drikBala}"
                )
            }

            csvBuilder.appendLine()
            csvBuilder.appendLine("SARVASHTAKAVARGA")
            csvBuilder.append("Sign")
            ZodiacSign.entries.forEach { csvBuilder.append(",${it.abbreviation}") }
            csvBuilder.appendLine()

            val ashtakavarga = AshtakavargaCalculator.calculateAshtakavarga(chart)
            csvBuilder.append("SAV")
            ZodiacSign.entries.forEach { sign ->
                csvBuilder.append(",${ashtakavarga.sarvashtakavarga.getBindusForSign(sign)}")
            }
            csvBuilder.appendLine()

            csvBuilder.appendLine()
            csvBuilder.appendLine("BHINNASHTAKAVARGA")
            csvBuilder.append("Planet")
            ZodiacSign.entries.forEach { csvBuilder.append(",${it.abbreviation}") }
            csvBuilder.appendLine(",Total")

            ashtakavarga.bhinnashtakavarga.forEach { (planet, bav) ->
                csvBuilder.append(planet.symbol)
                ZodiacSign.entries.forEach { sign ->
                    csvBuilder.append(",${bav.getBindusForSign(sign)}")
                }
                csvBuilder.appendLine(",${bav.totalBindus}")
            }

            csvBuilder.appendLine()
            csvBuilder.appendLine("YOGAS")
            csvBuilder.appendLine("Name,Category,Planets,Strength,StrengthPercentage,IsAuspicious")

            val yogaAnalysis = YogaCalculator.calculateYogas(chart)
            yogaAnalysis.allYogas.forEach { yoga ->
                val planets = yoga.planets.joinToString(";") { it.displayName }
                csvBuilder.appendLine(
                    "\"${yoga.name}\",${yoga.category.displayName},\"$planets\"," +
                            "${yoga.strength.displayName},${yoga.strengthPercentage},${yoga.isAuspicious}"
                )
            }

            // Save to file
            val fileName = "AstroStorm_${chart.birthData.name.replace(" ", "_")}_${dateFormatter.format(Date())}.csv"
            val path = saveCsvFile(csvBuilder.toString(), fileName)

            ExportResult.Success(path, ExportFormat.CSV)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export CSV: ${e.message}")
        }
    }

    private fun saveCsvFile(content: String, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/AstroStorm")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            ) ?: throw Exception("Failed to create file")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            uri.toString()
        } else {
            @Suppress("DEPRECATION")
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val astroStormDir = File(documentsDir, "AstroStorm")
            if (!astroStormDir.exists()) astroStormDir.mkdirs()

            val file = File(astroStormDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(content)
            }

            file.absolutePath
        }
    }

    // ==================== IMAGE EXPORT ====================

    /**
     * Export chart as image with optional watermark
     */
    suspend fun exportToImage(
        chart: VedicChart,
        options: ImageExportOptions = ImageExportOptions(),
        density: Density
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            var bitmap = chartRenderer.createChartBitmap(chart, options.width, options.height, density)

            if (options.addWatermark) {
                bitmap = addWatermark(bitmap, options.watermarkText)
            }

            if (options.includeTitle) {
                bitmap = addTitle(bitmap, chart)
            }

            val fileName = "AstroStorm_${chart.birthData.name.replace(" ", "_")}_${dateFormatter.format(Date())}.png"
            val path = saveImageFile(bitmap, fileName)

            ExportResult.Success(path, ExportFormat.PNG)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export image: ${e.message}")
        }
    }

    private fun addWatermark(bitmap: Bitmap, watermarkText: String): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val paint = Paint().apply {
            color = Color.argb(WATERMARK_ALPHA, 128, 128, 128)
            textSize = bitmap.width / 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Diagonal watermark
        canvas.save()
        canvas.rotate(-30f, bitmap.width / 2f, bitmap.height / 2f)
        canvas.drawText(watermarkText, bitmap.width / 2f, bitmap.height / 2f, paint)
        canvas.restore()

        return result
    }

    private fun addTitle(bitmap: Bitmap, chart: VedicChart): Bitmap {
        val titleHeight = 80
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height + titleHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw title background
        val bgPaint = Paint().apply {
            color = Color.rgb(212, 196, 168)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), titleHeight.toFloat(), bgPaint)

        // Draw title text
        val textPaint = Paint().apply {
            color = Color.rgb(70, 70, 70)
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(chart.birthData.name, bitmap.width / 2f, 30f, textPaint)

        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        canvas.drawText(
            "${chart.birthData.dateTime} | ${chart.birthData.location}",
            bitmap.width / 2f,
            55f,
            textPaint
        )

        // Draw chart below title
        canvas.drawBitmap(bitmap, 0f, titleHeight.toFloat(), null)

        return result
    }

    private fun saveImageFile(bitmap: Bitmap, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AstroStorm")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("Failed to create file")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            uri.toString()
        } else {
            @Suppress("DEPRECATION")
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val astroStormDir = File(picturesDir, "AstroStorm")
            if (!astroStormDir.exists()) astroStormDir.mkdirs()

            val file = File(astroStormDir, fileName)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            file.absolutePath
        }
    }

    // ==================== TEXT REPORT EXPORT ====================

    /**
     * Export interpretive text report
     */
    suspend fun exportToText(chart: VedicChart): ExportResult = withContext(Dispatchers.IO) {
        try {
            val report = buildString {
                appendLine("═══════════════════════════════════════════════════════════════════════════════")
                appendLine("                         VEDIC ASTROLOGY BIRTH CHART REPORT")
                appendLine("                              Generated by AstroStorm")
                appendLine("═══════════════════════════════════════════════════════════════════════════════")
                appendLine()
                appendLine("Generated: ${displayDateFormatter.format(Date())}")
                appendLine()

                // Birth Information
                appendLine("BIRTH INFORMATION")
                appendLine("─────────────────────────────────────────────────────────────────────────────")
                appendLine("Name          : ${chart.birthData.name}")
                appendLine("Date & Time   : ${chart.birthData.dateTime}")
                appendLine("Location      : ${chart.birthData.location}")
                appendLine("Coordinates   : ${formatCoordinate(chart.birthData.latitude.toDouble(), true)}, ${formatCoordinate(chart.birthData.longitude.toDouble(), false)}")
                appendLine("Timezone      : ${chart.birthData.timezone}")
                appendLine()

                // Chart Summary
                appendLine("CHART SUMMARY")
                appendLine("─────────────────────────────────────────────────────────────────────────────")
                val ascSign = ZodiacSign.fromLongitude(chart.ascendant)
                val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
                val sunPos = chart.planetPositions.find { it.planet == Planet.SUN }

                appendLine("Ascendant (Lagna): ${ascSign.displayName}")
                moonPos?.let { appendLine("Moon Sign (Rashi): ${it.sign.displayName}") }
                sunPos?.let { appendLine("Sun Sign         : ${it.sign.displayName}") }
                moonPos?.let { appendLine("Birth Nakshatra  : ${it.nakshatra.displayName} (Pada ${it.nakshatraPada})") }
                appendLine()

                // Planetary Positions
                append(chart.toPlainText())
                appendLine()

                // Yogas
                val yogaAnalysis = YogaCalculator.calculateYogas(chart)
                append(yogaAnalysis.toPlainText())
                appendLine()

                // Shadbala Summary
                val shadbala = ShadbalaCalculator.calculateShadbala(chart)
                append(shadbala.getSummaryInterpretation())
                appendLine()

                // Ashtakavarga Summary
                val ashtakavarga = AshtakavargaCalculator.calculateAshtakavarga(chart)
                append(ashtakavarga.toPlainText())
                appendLine()

                // Footer
                appendLine("═══════════════════════════════════════════════════════════════════════════════")
                appendLine("                    Report Generated by AstroStorm")
                appendLine("                  Ultra-Precision Vedic Astrology Software")
                appendLine("                  Calculation Engine: Swiss Ephemeris (JPL Mode)")
                appendLine("═══════════════════════════════════════════════════════════════════════════════")
            }

            val fileName = "AstroStorm_${chart.birthData.name.replace(" ", "_")}_${dateFormatter.format(Date())}.txt"
            val path = saveTextFile(report, fileName)

            ExportResult.Success(path, ExportFormat.TEXT)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export text report: ${e.message}")
        }
    }

    private fun saveTextFile(content: String, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/AstroStorm")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            ) ?: throw Exception("Failed to create file")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            uri.toString()
        } else {
            @Suppress("DEPRECATION")
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val astroStormDir = File(documentsDir, "AstroStorm")
            if (!astroStormDir.exists()) astroStormDir.mkdirs()

            val file = File(astroStormDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(content)
            }

            file.absolutePath
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun formatCoordinate(value: Double, isLatitude: Boolean): String {
        val abs = kotlin.math.abs(value)
        val degrees = abs.toInt()
        val minutes = ((abs - degrees) * 60).toInt()
        val seconds = ((((abs - degrees) * 60) - minutes) * 60).toInt()
        val direction = if (isLatitude) {
            if (value >= 0) "N" else "S"
        } else {
            if (value >= 0) "E" else "W"
        }
        return "$degrees° $minutes' $seconds\" $direction"
    }

    private fun formatDegree(degree: Double): String {
        val normalizedDegree = (degree % 360.0 + 360.0) % 360.0
        val deg = normalizedDegree.toInt()
        val min = ((normalizedDegree - deg) * 60).toInt()
        val sec = ((((normalizedDegree - deg) * 60) - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            else -> false
        }
    }

    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LIBRA
            Planet.MOON -> sign == ZodiacSign.SCORPIO
            Planet.MARS -> sign == ZodiacSign.CANCER
            Planet.MERCURY -> sign == ZodiacSign.PISCES
            Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
            Planet.VENUS -> sign == ZodiacSign.VIRGO
            Planet.SATURN -> sign == ZodiacSign.ARIES
            else -> false
        }
    }
}
