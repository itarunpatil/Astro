package com.astro.storm.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.BikramSambatConverter
import com.astro.storm.data.localization.BikramSambatConverter.BSDate
import com.astro.storm.data.localization.BikramSambatConverter.BSMonth
import com.astro.storm.data.localization.BikramSambatConverter.BSWeekday
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.ui.theme.AppTheme
import java.time.LocalDate
import kotlin.math.absoluteValue

@Immutable
data class BSDatePickerColors(
    val containerColor: Color,
    val titleColor: Color,
    val accentColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val selectedDayBackground: Color,
    val selectedDayText: Color,
    val todayBorderColor: Color,
    val saturdayColor: Color,
    val dividerColor: Color,
    val chipBackground: Color
)

object BSDatePickerDefaults {
    @Composable
    fun colors(
        containerColor: Color = AppTheme.CardBackground,
        titleColor: Color = AppTheme.TextPrimary,
        accentColor: Color = AppTheme.AccentPrimary,
        textPrimary: Color = AppTheme.TextPrimary,
        textSecondary: Color = AppTheme.TextSecondary,
        textMuted: Color = AppTheme.TextMuted,
        selectedDayBackground: Color = AppTheme.AccentPrimary,
        selectedDayText: Color = AppTheme.ButtonText,
        todayBorderColor: Color = AppTheme.AccentPrimary,
        saturdayColor: Color = AppTheme.ErrorColor.copy(alpha = 0.8f),
        dividerColor: Color = AppTheme.DividerColor,
        chipBackground: Color = AppTheme.ChipBackground
    ): BSDatePickerColors = BSDatePickerColors(
        containerColor = containerColor,
        titleColor = titleColor,
        accentColor = accentColor,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textMuted = textMuted,
        selectedDayBackground = selectedDayBackground,
        selectedDayText = selectedDayText,
        todayBorderColor = todayBorderColor,
        saturdayColor = saturdayColor,
        dividerColor = dividerColor,
        chipBackground = chipBackground
    )
}

@Stable
private class BSDatePickerState(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    private val minYear: Int,
    private val maxYear: Int
) {
    var year by mutableIntStateOf(initialYear.coerceIn(minYear, maxYear))
        private set

    var month by mutableIntStateOf(initialMonth.coerceIn(1, 12))
        private set

    var day by mutableIntStateOf(initialDay)
        private set

    var navigationDirection by mutableIntStateOf(0)
        private set

    val daysInMonth: Int
        get() = BikramSambatConverter.getDaysInMonth(year, month) ?: 30

    val firstDayWeekdayIndex: Int
        get() = BikramSambatConverter.getFirstDayOfMonthWeekdayIndex(year, month) ?: 0

    val canNavigatePrevious: Boolean
        get() = year > minYear || month > 1

    val canNavigateNext: Boolean
        get() = year < maxYear || month < 12

    val currentBSDate: BSDate
        get() = BSDate(year, month, day.coerceIn(1, daysInMonth))

    fun updateYear(newYear: Int) {
        year = newYear.coerceIn(minYear, maxYear)
        validateDay()
    }

    fun updateMonth(newMonth: Int) {
        month = newMonth.coerceIn(1, 12)
        validateDay()
    }

    fun updateDay(newDay: Int) {
        day = newDay.coerceIn(1, daysInMonth)
    }

    fun navigateToPreviousMonth() {
        navigationDirection = -1
        if (month > 1) {
            month--
        } else if (year > minYear) {
            year--
            month = 12
        }
        validateDay()
    }

    fun navigateToNextMonth() {
        navigationDirection = 1
        if (month < 12) {
            month++
        } else if (year < maxYear) {
            year++
            month = 1
        }
        validateDay()
    }

    fun navigateToDate(date: BSDate) {
        navigationDirection = 0
        year = date.year.coerceIn(minYear, maxYear)
        month = date.month.coerceIn(1, 12)
        day = date.day.coerceIn(1, BikramSambatConverter.getDaysInMonth(year, month) ?: 30)
    }

    private fun validateDay() {
        val maxDay = daysInMonth
        if (day > maxDay) {
            day = maxDay
        }
    }
}

@Composable
private fun rememberBSDatePickerState(
    initialDate: BSDate,
    minYear: Int,
    maxYear: Int
): BSDatePickerState {
    return remember(initialDate, minYear, maxYear) {
        BSDatePickerState(
            initialYear = initialDate.year,
            initialMonth = initialDate.month,
            initialDay = initialDate.day,
            minYear = minYear,
            maxYear = maxYear
        )
    }
}

@Composable
fun BSDatePickerDialog(
    initialDate: BSDate = BikramSambatConverter.today(),
    onDismiss: () -> Unit,
    onConfirm: (BSDate) -> Unit,
    minYear: Int = BikramSambatConverter.minBSYear,
    maxYear: Int = BikramSambatConverter.maxBSYear,
    colors: BSDatePickerColors = BSDatePickerDefaults.colors()
) {
    val language = LocalLanguage.current
    val haptic = LocalHapticFeedback.current
    val todayBS = remember { BikramSambatConverter.today() }

    val state = rememberBSDatePickerState(
        initialDate = initialDate,
        minYear = minYear,
        maxYear = maxYear
    )

    val isCurrentMonthToday by remember(state.year, state.month, todayBS) {
        derivedStateOf {
            state.year == todayBS.year && state.month == todayBS.month
        }
    }

    val todayDayInCurrentMonth = if (isCurrentMonthToday) todayBS.day else null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.containerColor,
        titleContentColor = colors.titleColor,
        title = {
            BSDatePickerHeader(
                language = language,
                colors = colors,
                onTodayClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.navigateToDate(todayBS)
                }
            )
        },
        text = {
            BSDatePickerContent(
                state = state,
                language = language,
                colors = colors,
                todayDay = todayDayInCurrentMonth,
                minYear = minYear,
                maxYear = maxYear,
                onHapticFeedback = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm(state.currentBSDate)
                }
            ) {
                Text(
                    text = stringResource(StringKey.BTN_OK),
                    color = colors.accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(StringKey.BTN_CANCEL),
                    color = colors.textSecondary
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun BSDatePickerHeader(
    language: Language,
    colors: BSDatePickerColors,
    onTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(StringKey.BS_DATE_PICKER_TITLE),
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        Surface(
            onClick = onTodayClick,
            shape = RoundedCornerShape(8.dp),
            color = colors.accentColor.copy(alpha = 0.1f),
            modifier = Modifier.semantics {
                role = Role.Button
                contentDescription = when (language) {
                    Language.ENGLISH -> "Go to today"
                    Language.NEPALI -> "आज मा जानुहोस्"
                }
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = colors.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (language) {
                        Language.ENGLISH -> "Today"
                        Language.NEPALI -> "आज"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accentColor
                )
            }
        }
    }
}

@Composable
private fun BSDatePickerContent(
    state: BSDatePickerState,
    language: Language,
    colors: BSDatePickerColors,
    todayDay: Int?,
    minYear: Int,
    maxYear: Int,
    onHapticFeedback: () -> Unit
) {
    val swipeThreshold = with(LocalDensity.current) { 50.dp.toPx() }
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier.pointerInput(state.canNavigatePrevious, state.canNavigateNext) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (accumulatedDrag > swipeThreshold && state.canNavigatePrevious) {
                        onHapticFeedback()
                        state.navigateToPreviousMonth()
                    } else if (accumulatedDrag < -swipeThreshold && state.canNavigateNext) {
                        onHapticFeedback()
                        state.navigateToNextMonth()
                    }
                    accumulatedDrag = 0f
                },
                onDragCancel = { accumulatedDrag = 0f },
                onHorizontalDrag = { _, dragAmount ->
                    accumulatedDrag += dragAmount
                }
            )
        }
    ) {
        SelectedDateDisplay(
            year = state.year,
            month = state.month,
            day = state.day.coerceIn(1, state.daysInMonth),
            language = language,
            colors = colors
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.dividerColor)
        Spacer(modifier = Modifier.height(16.dp))

        MonthYearNavigator(
            year = state.year,
            month = state.month,
            language = language,
            colors = colors,
            navigationDirection = state.navigationDirection,
            onPreviousMonth = {
                onHapticFeedback()
                state.navigateToPreviousMonth()
            },
            onNextMonth = {
                onHapticFeedback()
                state.navigateToNextMonth()
            },
            canGoPrevious = state.canNavigatePrevious,
            canGoNext = state.canNavigateNext
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            YearSelector(
                selectedYear = state.year,
                onYearChange = {
                    onHapticFeedback()
                    state.updateYear(it)
                },
                minYear = minYear,
                maxYear = maxYear,
                language = language,
                colors = colors,
                modifier = Modifier.weight(1f)
            )

            MonthSelector(
                selectedMonth = state.month,
                onMonthChange = {
                    onHapticFeedback()
                    state.updateMonth(it)
                },
                language = language,
                colors = colors,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalendarGrid(
            selectedDay = state.day.coerceIn(1, state.daysInMonth),
            daysInMonth = state.daysInMonth,
            firstDayWeekdayIndex = state.firstDayWeekdayIndex,
            onDaySelected = {
                onHapticFeedback()
                state.updateDay(it)
            },
            language = language,
            colors = colors,
            todayDay = todayDay
        )
    }
}

@Composable
private fun MonthYearNavigator(
    year: Int,
    month: Int,
    language: Language,
    colors: BSDatePickerColors,
    navigationDirection: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    canGoPrevious: Boolean,
    canGoNext: Boolean
) {
    val bsMonth = remember(month) { BSMonth.fromIndex(month) }

    val displayText = remember(year, month, language) {
        when (language) {
            Language.ENGLISH -> "${bsMonth.englishName} $year"
            Language.NEPALI -> "${bsMonth.nepaliName} ${BikramSambatConverter.toNepaliNumerals(year)}"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            enabled = canGoPrevious,
            modifier = Modifier
                .size(40.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = when (language) {
                        Language.ENGLISH -> "Previous month"
                        Language.NEPALI -> "अघिल्लो महिना"
                    }
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (canGoPrevious) colors.textPrimary else colors.textMuted,
                modifier = Modifier.size(28.dp)
            )
        }

        AnimatedContent(
            targetState = Triple(year, month, displayText),
            transitionSpec = {
                val direction = when {
                    navigationDirection > 0 -> 1
                    navigationDirection < 0 -> -1
                    targetState.first > initialState.first -> 1
                    targetState.first < initialState.first -> -1
                    targetState.second > initialState.second -> 1
                    targetState.second < initialState.second -> -1
                    else -> 1
                }
                (slideInHorizontally(tween(300)) { width -> direction * width } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { width -> -direction * width } + fadeOut(tween(200)))
            },
            label = "month_year_animation"
        ) { (_, _, text) ->
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.semantics {
                    contentDescription = text
                }
            )
        }

        IconButton(
            onClick = onNextMonth,
            enabled = canGoNext,
            modifier = Modifier
                .size(40.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = when (language) {
                        Language.ENGLISH -> "Next month"
                        Language.NEPALI -> "अर्को महिना"
                    }
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (canGoNext) colors.textPrimary else colors.textMuted,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun SelectedDateDisplay(
    year: Int,
    month: Int,
    day: Int,
    language: Language,
    colors: BSDatePickerColors
) {
    val bsMonth = remember(month) { BSMonth.fromIndex(month) }
    val bsDate = remember(year, month, day) { BSDate(year, month, day) }
    val weekday = remember(bsDate) { bsDate.weekday }
    val adDate = remember(year, month, day) { BikramSambatConverter.toAD(year, month, day) }

    val displayDate = remember(year, month, day, language) {
        when (language) {
            Language.ENGLISH -> "$day ${bsMonth.englishName}, $year BS"
            Language.NEPALI -> "${BikramSambatConverter.toNepaliNumerals(day)} ${bsMonth.nepaliName}, ${BikramSambatConverter.toNepaliNumerals(year)} वि.सं."
        }
    }

    val weekdayText = remember(weekday, language) {
        weekday?.getName(language) ?: ""
    }

    val adDateText = remember(adDate) {
        adDate?.let { "(${it.year}-${it.monthValue.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')} AD)" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.accentColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weekdayText.isNotEmpty()) {
                Text(
                    text = weekdayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accentColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = displayDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.accentColor
            )

            adDateText?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun YearSelector(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    minYear: Int,
    maxYear: Int,
    language: Language,
    colors: BSDatePickerColors,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val totalYears = maxYear - minYear + 1
    val selectedIndex = (maxYear - selectedYear).coerceIn(0, totalYears - 1)

    val selectedYearText = remember(selectedYear, language) {
        when (language) {
            Language.ENGLISH -> selectedYear.toString()
            Language.NEPALI -> BikramSambatConverter.toNepaliNumerals(selectedYear)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(StringKey.BS_YEAR),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        DropdownSelector(
            text = selectedYearText,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            colors = colors,
            contentDescription = when (language) {
                Language.ENGLISH -> "Year selector, current: $selectedYear"
                Language.NEPALI -> "वर्ष छान्नुहोस्, हाल: ${BikramSambatConverter.toNepaliNumerals(selectedYear)}"
            }
        ) {
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

            LazyColumn(
                state = listState,
                modifier = Modifier.heightIn(max = 250.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    count = totalYears,
                    key = { index -> maxYear - index }
                ) { index ->
                    val year = maxYear - index
                    val yearText = remember(year, language) {
                        when (language) {
                            Language.ENGLISH -> year.toString()
                            Language.NEPALI -> BikramSambatConverter.toNepaliNumerals(year)
                        }
                    }
                    DropdownItem(
                        text = yearText,
                        isSelected = year == selectedYear,
                        colors = colors,
                        onClick = {
                            onYearChange(year)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    language: Language,
    colors: BSDatePickerColors,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedMonthText = remember(selectedMonth, language) {
        BSMonth.fromIndex(selectedMonth).getName(language)
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(StringKey.BS_MONTH),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        DropdownSelector(
            text = selectedMonthText,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            colors = colors,
            contentDescription = when (language) {
                Language.ENGLISH -> "Month selector, current: $selectedMonthText"
                Language.NEPALI -> "महिना छान्नुहोस्, हाल: $selectedMonthText"
            }
        ) {
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = (selectedMonth - 1).coerceIn(0, 11)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.heightIn(max = 300.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    count = 12,
                    key = { it + 1 }
                ) { index ->
                    val monthIndex = index + 1
                    val month = BSMonth.fromIndex(monthIndex)
                    val monthText = remember(month, language) { month.getName(language) }

                    DropdownItem(
                        text = monthText,
                        isSelected = monthIndex == selectedMonth,
                        colors = colors,
                        onClick = {
                            onMonthChange(monthIndex)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DropdownSelector(
    text: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    colors: BSDatePickerColors,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "dropdown_rotation"
    )

    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) }
                .semantics {
                    role = Role.DropdownList
                    this.contentDescription = contentDescription
                },
            shape = RoundedCornerShape(10.dp),
            color = colors.chipBackground,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (expanded) colors.accentColor else colors.dividerColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(tween(300)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = CardDefaults.cardColors(containerColor = colors.containerColor),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DropdownItem(
    text: String,
    isSelected: Boolean,
    colors: BSDatePickerColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                selected = isSelected
            },
        color = if (isSelected) colors.accentColor.copy(alpha = 0.15f) else colors.containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) colors.accentColor else colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun CalendarGrid(
    selectedDay: Int,
    daysInMonth: Int,
    firstDayWeekdayIndex: Int,
    onDaySelected: (Int) -> Unit,
    language: Language,
    colors: BSDatePickerColors,
    todayDay: Int? = null
) {
    val saturdayIndex = BSWeekday.SATURDAY.ordinal
    val totalCells = firstDayWeekdayIndex + daysInMonth
    val rows = (totalCells + 6) / 7
    val cellHeight = 40.dp
    val gridHeight = (rows * cellHeight.value + (rows - 1) * 2).dp

    Column {
        Text(
            text = stringResource(StringKey.BS_DAY),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        WeekdayHeader(language = language, colors = colors)

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = false
        ) {
            items(count = firstDayWeekdayIndex) {
                EmptyDayCell(cellHeight = cellHeight)
            }

            items(
                count = daysInMonth,
                key = { it + 1 }
            ) { index ->
                val day = index + 1
                val weekdayIndex = (firstDayWeekdayIndex + index) % 7

                DayCell(
                    day = day,
                    isSelected = day == selectedDay,
                    isToday = day == todayDay,
                    isSaturday = weekdayIndex == saturdayIndex,
                    onClick = { onDaySelected(day) },
                    language = language,
                    colors = colors,
                    cellHeight = cellHeight
                )
            }
        }
    }
}

@Composable
private fun WeekdayHeader(
    language: Language,
    colors: BSDatePickerColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BSWeekday.entries.forEach { weekday ->
            val isSaturday = weekday == BSWeekday.SATURDAY
            Text(
                text = weekday.getName(language, short = true),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSaturday) colors.saturdayColor else colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = weekday.getName(language, short = false)
                    }
            )
        }
    }
}

@Composable
private fun EmptyDayCell(cellHeight: Dp) {
    Box(
        modifier = Modifier
            .height(cellHeight)
            .aspectRatio(1f)
            .padding(2.dp)
    )
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isSaturday: Boolean,
    onClick: () -> Unit,
    language: Language,
    colors: BSDatePickerColors,
    cellHeight: Dp
) {
    val displayDay = remember(day, language) {
        when (language) {
            Language.ENGLISH -> day.toString()
            Language.NEPALI -> BikramSambatConverter.toNepaliNumerals(day)
        }
    }

    val backgroundColor = when {
        isSelected -> colors.selectedDayBackground
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> colors.selectedDayText
        isToday -> colors.todayBorderColor
        isSaturday -> colors.saturdayColor
        else -> colors.textPrimary
    }

    val accessibilityLabel = remember(day, isSelected, isToday, isSaturday, language) {
        buildString {
            append(displayDay)
            if (isSelected) {
                append(if (language == Language.ENGLISH) ", selected" else ", चयन गरिएको")
            }
            if (isToday) {
                append(if (language == Language.ENGLISH) ", today" else ", आज")
            }
            if (isSaturday) {
                append(if (language == Language.ENGLISH) ", Saturday" else ", शनिबार")
            }
        }
    }

    Box(
        modifier = Modifier
            .height(cellHeight)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(2.dp, colors.todayBorderColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                selected = isSelected
                contentDescription = accessibilityLabel
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayDay,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
    }
}

@Composable
fun BSDateSelector(
    selectedDate: BSDate,
    onDateSelected: (BSDate) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minYear: Int = BikramSambatConverter.minBSYear,
    maxYear: Int = BikramSambatConverter.maxBSYear,
    colors: BSDatePickerColors = BSDatePickerDefaults.colors()
) {
    val language = LocalLanguage.current
    var showPicker by remember { mutableStateOf(false) }

    val displayText = remember(selectedDate, language) {
        selectedDate.format(language)
    }

    val weekdayText = remember(selectedDate, language) {
        selectedDate.weekday?.getName(language)
    }

    Surface(
        onClick = { if (enabled) showPicker = true },
        enabled = enabled,
        modifier = modifier.semantics {
            role = Role.Button
            contentDescription = when (language) {
                Language.ENGLISH -> "Date selector. Current date: $displayText"
                Language.NEPALI -> "मिति छान्नुहोस्। हालको मिति: $displayText"
            }
        },
        shape = RoundedCornerShape(12.dp),
        color = colors.chipBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.dividerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) colors.textPrimary else colors.textMuted
                )
                weekdayText?.let { weekday ->
                    Text(
                        text = weekday,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showPicker) {
        BSDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showPicker = false },
            onConfirm = { date ->
                onDateSelected(date)
                showPicker = false
            },
            minYear = minYear,
            maxYear = maxYear,
            colors = colors
        )
    }
}

fun LocalDate.toBSDate(): BSDate? = BikramSambatConverter.toBS(this)

fun BSDate.toLocalDate(): LocalDate? = BikramSambatConverter.toAD(this)