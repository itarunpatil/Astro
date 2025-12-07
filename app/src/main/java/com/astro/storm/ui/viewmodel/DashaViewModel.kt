package com.astro.storm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DashaCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class DashaUiState {
    data object Loading : DashaUiState()
    data class Success(val timeline: DashaCalculator.DashaTimeline) : DashaUiState()
    data class Error(val message: String) : DashaUiState()
    data object Idle : DashaUiState()
}

class DashaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DashaUiState>(DashaUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var calculationJob: Job? = null
    private var cachedChartKey: String? = null
    private var cachedTimeline: DashaCalculator.DashaTimeline? = null

    fun loadDashaTimeline(chart: VedicChart?) {
        if (chart == null) {
            _uiState.value = DashaUiState.Idle
            return
        }

        val chartKey = generateChartKey(chart)

        if (chartKey == cachedChartKey && cachedTimeline != null) {
            _uiState.value = DashaUiState.Success(cachedTimeline!!)
            return
        }

        if (_uiState.value is DashaUiState.Loading && chartKey == cachedChartKey) {
            return
        }

        calculationJob?.cancel()
        _uiState.value = DashaUiState.Loading

        calculationJob = viewModelScope.launch {
            try {
                val timeline = withContext(Dispatchers.Default) {
                    if (!isActive) return@withContext null
                    DashaCalculator.calculateDashaTimeline(chart)
                }

                if (timeline != null && isActive) {
                    cachedChartKey = chartKey
                    cachedTimeline = timeline
                    _uiState.value = DashaUiState.Success(timeline)
                }
            } catch (e: Exception) {
                if (isActive) {
                    _uiState.value = DashaUiState.Error(
                        e.message ?: "Failed to calculate Dasha timeline."
                    )
                }
            }
        }
    }

    fun clearCache() {
        cachedChartKey = null
        cachedTimeline = null
    }

    private fun generateChartKey(chart: VedicChart): String {
        val birthData = chart.birthData
        return buildString {
            append(birthData.dateTime.toString())
            append("|")
            append(birthData.latitude)
            append("|")
            append(birthData.longitude)
            append("|")
            append(chart.ayanamsa)
        }
    }

    override fun onCleared() {
        super.onCleared()
        calculationJob?.cancel()
        clearCache()
    }
}