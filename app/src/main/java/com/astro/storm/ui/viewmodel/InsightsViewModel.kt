package com.astro.storm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ephemeris.HoroscopeCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.time.LocalDate

data class InsightsData(
    val chart: VedicChart,
    val dashaTimeline: DashaCalculator.DashaTimeline?,
    val planetaryInfluences: List<HoroscopeCalculator.PlanetaryInfluence>,
    val todayHoroscope: HoroscopeCalculator.DailyHoroscope?,
    val tomorrowHoroscope: HoroscopeCalculator.DailyHoroscope?,
    val weeklyHoroscope: HoroscopeCalculator.WeeklyHoroscope?,
    val errors: List<InsightError> = emptyList()
)

data class InsightError(
    val type: InsightErrorType,
    val message: String
)

enum class InsightErrorType {
    TODAY_HOROSCOIPE,
    TOMORROW_HOROSCOPE,
    WEEKLY_HOROSCOPE,
    DASHA,
    GENERAL
}

sealed class InsightsUiState {
    data object Loading : InsightsUiState()
    data class Success(val data: InsightsData) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
    data object Idle : InsightsUiState()
}

class InsightsViewModel(private val application: Application) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var cachedData: InsightsData? = null

    fun loadInsights(chart: VedicChart?) {
        if (chart == null) {
            _uiState.value = InsightsUiState.Idle
            return
        }

        val today = LocalDate.now()
        if (cachedData?.chart == chart && cachedData?.todayHoroscope?.date == today) {
            _uiState.value = InsightsUiState.Success(cachedData!!)
            return
        }

        _uiState.value = InsightsUiState.Loading
        viewModelScope.launch {
            try {
                val insights = calculateInsights(chart)
                cachedData = insights
                _uiState.value = InsightsUiState.Success(insights)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = InsightsUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private suspend fun calculateInsights(chart: VedicChart): InsightsData {
        val errors = mutableListOf<InsightError>()
        val today = LocalDate.now()

        return supervisorScope {
            val dashaDeferred = async(Dispatchers.Default) {
                try {
                    DashaCalculator.calculateDashaTimeline(chart)
                } catch (e: Exception) {
                    errors.add(InsightError(InsightErrorType.DASHA, e.message ?: "Dasha calculation failed"))
                    null
                }
            }

            val calculator = HoroscopeCalculator(application)
            try {
                val todayDeferred = async(Dispatchers.Default) {
                    try {
                        calculator.calculateDailyHoroscope(chart, today)
                    } catch (e: Exception) {
                        errors.add(InsightError(InsightErrorType.TODAY_HOROSCOIPE, e.message ?: "Today's horoscope failed"))
                        null
                    }
                }

                val tomorrowDeferred = async(Dispatchers.Default) {
                    try {
                        calculator.calculateDailyHoroscope(chart, today.plusDays(1))
                    } catch (e: Exception) {
                        errors.add(InsightError(InsightErrorType.TOMORROW_HOROSCOPE, e.message ?: "Tomorrow's horoscope failed"))
                        null
                    }
                }

                val weeklyDeferred = async(Dispatchers.Default) {
                    try {
                        calculator.calculateWeeklyHoroscope(chart, today)
                    } catch (e: Exception) {
                        errors.add(InsightError(InsightErrorType.WEEKLY_HOROSCOPE, e.message ?: "Weekly horoscope failed"))
                        null
                    }
                }

                val todayHoroscope = todayDeferred.await()
                val tomorrowHoroscope = tomorrowDeferred.await()
                val weeklyHoroscope = weeklyDeferred.await()
                val dashaTimeline = dashaDeferred.await()

                InsightsData(
                    chart = chart,
                    dashaTimeline = dashaTimeline,
                    planetaryInfluences = todayHoroscope?.planetaryInfluences ?: emptyList(),
                    todayHoroscope = todayHoroscope,
                    tomorrowHoroscope = tomorrowHoroscope,
                    weeklyHoroscope = weeklyHoroscope,
                    errors = errors.toList()
                )
            } finally {
                calculator.close()
            }
        }
    }
}
