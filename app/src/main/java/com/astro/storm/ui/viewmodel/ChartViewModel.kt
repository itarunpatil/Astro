package com.astro.storm.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.unit.Density
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.local.ChartDatabase
import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.ChartRepository
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ephemeris.SwissEphemerisEngine
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.util.ChartExporter
import com.astro.storm.util.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel for chart operations.
 *
 * Manages the lifecycle of chart calculations, storage, and exports.
 * Uses proper coroutine handling to prevent memory leaks and race conditions.
 */
class ChartViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ChartViewModel"
        private const val PREFS_NAME = "chart_prefs"
        private const val PREF_LAST_SELECTED_CHART = "last_selected_chart_id"
        private const val EXECUTOR_SHUTDOWN_TIMEOUT_MS = 500L
    }

    private val repository: ChartRepository
    private val ephemerisEngine: SwissEphemerisEngine
    private val chartRenderer = ChartRenderer()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val chartExporter: ChartExporter

    // Executor for single-threaded state updates with proper naming for debugging
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "ChartViewModel-StateUpdater").apply {
            isDaemon = true
        }
    }
    private val singleThreadContext = executor.asCoroutineDispatcher()

    // Mutex to prevent race conditions during chart operations
    private val chartOperationMutex = Mutex()

    // Track if resources have been cleaned up
    private val isCleared = AtomicBoolean(false)

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Initial)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _savedCharts = MutableStateFlow<List<SavedChart>>(emptyList())
    val savedCharts: StateFlow<List<SavedChart>> = _savedCharts.asStateFlow()

    private val _selectedChartId = MutableStateFlow<Long?>(null)
    val selectedChartId: StateFlow<Long?> = _selectedChartId.asStateFlow()

    init {
        val database = ChartDatabase.getInstance(application)
        repository = ChartRepository(database.chartDao())
        ephemerisEngine = SwissEphemerisEngine.create(application)
        chartExporter = ChartExporter(application)

        loadSavedCharts()
    }

    private fun loadSavedCharts() {
        viewModelScope.launch {
            repository.getAllCharts().collect { charts ->
                if (isCleared.get()) return@collect

                _savedCharts.value = charts

                // Use mutex to prevent race conditions during initial chart selection
                chartOperationMutex.withLock {
                    if (_selectedChartId.value == null) {
                        val lastSelectedId = prefs.getLong(PREF_LAST_SELECTED_CHART, -1)
                        val chartToLoad = when {
                            lastSelectedId != -1L && charts.any { it.id == lastSelectedId } -> lastSelectedId
                            charts.isNotEmpty() -> charts.first().id
                            else -> null
                        }
                        chartToLoad?.let { loadChartInternal(it) }
                    }
                }
            }
        }
    }

    /**
     * Internal chart loading without mutex acquisition (for use within mutex-protected blocks)
     */
    private suspend fun loadChartInternal(chartId: Long) {
        if (isCleared.get()) return

        _uiState.value = ChartUiState.Loading

        try {
            val chart = repository.getChartById(chartId)
            if (chart != null) {
                _uiState.value = ChartUiState.Success(chart)
                _selectedChartId.value = chartId
                prefs.edit().putLong(PREF_LAST_SELECTED_CHART, chartId).apply()
            } else {
                _uiState.value = ChartUiState.Error("Chart not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load chart $chartId", e)
            _uiState.value = ChartUiState.Error(e.message ?: "Failed to load chart")
        }
    }

    /**
     * Calculate a new Vedic chart
     */
    fun calculateChart(
        birthData: BirthData,
        houseSystem: HouseSystem = HouseSystem.DEFAULT
    ) {
        viewModelScope.launch(singleThreadContext) {
            _uiState.value = ChartUiState.Calculating

            try {
                val chart = withContext(Dispatchers.Default) {
                    ephemerisEngine.calculateVedicChart(birthData, houseSystem)
                }
                _uiState.value = ChartUiState.Success(chart)
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Load a saved chart by ID.
     *
     * @param chartId The database ID of the chart to load
     */
    fun loadChart(chartId: Long) {
        if (isCleared.get()) return

        viewModelScope.launch(singleThreadContext) {
            chartOperationMutex.withLock {
                loadChartInternal(chartId)
            }
        }
    }

    /**
     * Save current chart
     */
    fun saveChart(chart: VedicChart) {
        viewModelScope.launch(singleThreadContext) {
            try {
                repository.saveChart(chart)
                _uiState.value = ChartUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Failed to save chart: ${e.message}")
            }
        }
    }

    /**
     * Delete a saved chart.
     *
     * @param chartId The database ID of the chart to delete
     */
    fun deleteChart(chartId: Long) {
        if (isCleared.get()) return

        viewModelScope.launch(singleThreadContext) {
            chartOperationMutex.withLock {
                try {
                    repository.deleteChart(chartId)
                    if (_selectedChartId.value == chartId) {
                        prefs.edit().remove(PREF_LAST_SELECTED_CHART).apply()
                        _selectedChartId.value = null
                        _uiState.value = ChartUiState.Initial
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete chart $chartId", e)
                    _uiState.value = ChartUiState.Error("Failed to delete chart: ${e.message}")
                }
            }
        }
    }

    /**
     * Export chart as image
     */
    fun exportChartImage(chart: VedicChart, fileName: String, density: Density) {
        viewModelScope.launch(singleThreadContext) {
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    chartRenderer.createChartBitmap(chart, 2048, 2048, density)
                }

                val result = ExportUtils.saveChartImage(getApplication(), bitmap, fileName)
                result.onSuccess {
                    _uiState.value = ChartUiState.Exported("Image saved successfully")
                }.onFailure {
                    _uiState.value = ChartUiState.Error("Failed to save image: ${it.message}")
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Export failed: ${e.message}")
            }
        }
    }

    /**
     * Copy chart plaintext to clipboard
     */
    fun copyChartToClipboard(chart: VedicChart) {
        try {
            val plaintext = ExportUtils.getChartPlaintext(chart)
            ExportUtils.copyToClipboard(getApplication(), plaintext, "Vedic Chart Data")
            _uiState.value = ChartUiState.Exported("Chart data copied to clipboard")
        } catch (e: Exception) {
            _uiState.value = ChartUiState.Error("Failed to copy: ${e.message}")
        }
    }

    /**
     * Export chart to PDF with comprehensive report
     */
    fun exportChartToPdf(
        chart: VedicChart,
        density: Density,
        options: ChartExporter.PdfExportOptions = ChartExporter.PdfExportOptions()
    ) {
        viewModelScope.launch(singleThreadContext) {
            try {
                _uiState.value = ChartUiState.Exporting("Generating PDF report...")
                val result = chartExporter.exportToPdf(chart, options, density)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _uiState.value = ChartUiState.Exported("PDF saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _uiState.value = ChartUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("PDF export failed: ${e.message}")
            }
        }
    }

    /**
     * Export chart to JSON
     */
    fun exportChartToJson(chart: VedicChart) {
        viewModelScope.launch(singleThreadContext) {
            try {
                _uiState.value = ChartUiState.Exporting("Generating JSON...")
                val result = chartExporter.exportToJson(chart)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _uiState.value = ChartUiState.Exported("JSON saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _uiState.value = ChartUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("JSON export failed: ${e.message}")
            }
        }
    }

    /**
     * Export chart to CSV
     */
    fun exportChartToCsv(chart: VedicChart) {
        viewModelScope.launch(singleThreadContext) {
            try {
                _uiState.value = ChartUiState.Exporting("Generating CSV...")
                val result = chartExporter.exportToCsv(chart)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _uiState.value = ChartUiState.Exported("CSV saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _uiState.value = ChartUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("CSV export failed: ${e.message}")
            }
        }
    }

    /**
     * Export chart as high-quality image with options
     */
    fun exportChartToImage(
        chart: VedicChart,
        density: Density,
        options: ChartExporter.ImageExportOptions = ChartExporter.ImageExportOptions()
    ) {
        viewModelScope.launch(singleThreadContext) {
            try {
                _uiState.value = ChartUiState.Exporting("Generating image...")
                val result = chartExporter.exportToImage(chart, options, density)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _uiState.value = ChartUiState.Exported("Image saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _uiState.value = ChartUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Image export failed: ${e.message}")
            }
        }
    }

    /**
     * Export chart as plain text report
     */
    fun exportChartToText(chart: VedicChart) {
        viewModelScope.launch(singleThreadContext) {
            try {
                _uiState.value = ChartUiState.Exporting("Generating text report...")
                val result = chartExporter.exportToText(chart)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _uiState.value = ChartUiState.Exported("Text report saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _uiState.value = ChartUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Text export failed: ${e.message}")
            }
        }
    }

    /**
     * Get a chart by ID directly (for matchmaking and other features)
     */
    suspend fun getChartById(chartId: Long): VedicChart? {
        return repository.getChartById(chartId)
    }

    /**
     * Reset UI state to previous chart state if available, otherwise Initial
     * This is used after export operations to restore the normal UI state
     */
    fun resetState() {
        // If we had a chart loaded, restore to Success state
        val currentState = _uiState.value
        when (currentState) {
            is ChartUiState.Exported, is ChartUiState.Error, is ChartUiState.Exporting -> {
                // Try to reload the current chart if one was selected
                _selectedChartId.value?.let { chartId ->
                    loadChart(chartId)
                } ?: run {
                    _uiState.value = ChartUiState.Initial
                }
            }
            else -> {
                // Keep current state for Success, Calculating, Loading, etc.
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        // Mark as cleared to prevent any further operations
        isCleared.set(true)

        // Close ephemeris engine first (this may take time)
        try {
            ephemerisEngine.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ephemeris engine", e)
        }

        // Shutdown executor gracefully
        try {
            singleThreadContext.close()
            executor.shutdown()
            if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow()
                Log.w(TAG, "Executor did not terminate gracefully, forced shutdown")
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
            Log.w(TAG, "Executor shutdown interrupted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down executor", e)
        }

        Log.d(TAG, "ChartViewModel cleared successfully")
    }
}

/**
 * UI states for chart operations
 */
sealed class ChartUiState {
    object Initial : ChartUiState()
    object Loading : ChartUiState()
    object Calculating : ChartUiState()
    data class Success(val chart: VedicChart) : ChartUiState()
    data class Error(val message: String) : ChartUiState()
    object Saved : ChartUiState()
    data class Exporting(val message: String) : ChartUiState()
    data class Exported(val message: String) : ChartUiState()
}
