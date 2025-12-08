package com.astro.storm.localization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.astro.storm.ui.theme.AppTheme
import java.time.LocalDate

/**
 * Bikram Sambat Date Picker Dialog
 * A modern, responsive date picker supporting both BS (Bikram Sambat) and AD (Gregorian) date systems
 */
@Composable
fun BSDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    dateSystem: DateSystem = DateSystem.BS,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val language = LocalLanguage.current

    // Convert initial date to BS if needed
    val initialBSDate = remember(initialDate) {
        BikramSambatConverter.adToBs(initialDate) ?: BSDate(2080, 1, 1)
    }

    var currentDateSystem by remember { mutableStateOf(dateSystem) }

    // State for BS date
    var bsYear by remember { mutableIntStateOf(initialBSDate.year) }
    var bsMonth by remember { mutableIntStateOf(initialBSDate.month) }
    var bsDay by remember { mutableIntStateOf(initialBSDate.day) }

    // State for AD date
    var adDate by remember { mutableStateOf(initialDate) }

    // Sync BS and AD dates when one changes
    LaunchedEffect(bsYear, bsMonth, bsDay) {
        if (currentDateSystem == DateSystem.BS) {
            BikramSambatConverter.bsToAd(bsYear, bsMonth, bsDay)?.let {
                adDate = it
            }
        }
    }

    LaunchedEffect(adDate) {
        if (currentDateSystem == DateSystem.AD) {
            BikramSambatConverter.adToBs(adDate)?.let { bs ->
                bsYear = bs.year
                bsMonth = bs.month
                bsDay = bs.day
            }
        }
    }

    val daysInMonth = remember(bsYear, bsMonth) {
        BikramSambatConverter.getDaysInMonth(bsYear, bsMonth)
    }

    // Ensure day is valid when month/year changes
    LaunchedEffect(daysInMonth) {
        if (bsDay > daysInMonth) {
            bsDay = daysInMonth
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = AppTheme.AccentPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = getString(StringKey.DATE_SELECT_DATE),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date System Toggle
                DateSystemToggle(
                    currentSystem = currentDateSystem,
                    language = language,
                    onSystemChange = { newSystem ->
                        currentDateSystem = newSystem
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Current Date Display
                Surface(
                    color = AppTheme.AccentPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentDateSystem == DateSystem.BS) {
                                BSDate(bsYear, bsMonth, bsDay).format(language, "dd MMMM yyyy")
                            } else {
                                "${adDate.dayOfMonth} ${getMonthName(adDate.monthValue, language)} ${adDate.year}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.AccentPrimary
                        )
                        Text(
                            text = if (currentDateSystem == DateSystem.BS) {
                                getString(StringKey.DATE_BS)
                            } else {
                                getString(StringKey.DATE_AD)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )

                        // Show converted date
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentDateSystem == DateSystem.BS) {
                                "(${adDate.dayOfMonth} ${getMonthName(adDate.monthValue, Language.ENGLISH)} ${adDate.year} ${getString(StringKey.DATE_AD)})"
                            } else {
                                "(${BSDate(bsYear, bsMonth, bsDay).format(language, "dd MMMM yyyy")} ${getString(StringKey.DATE_BS)})"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (currentDateSystem == DateSystem.BS) {
                    // BS Date Picker
                    BSDateSelector(
                        year = bsYear,
                        month = bsMonth,
                        day = bsDay,
                        daysInMonth = daysInMonth,
                        language = language,
                        onYearChange = { bsYear = it },
                        onMonthChange = { bsMonth = it },
                        onDayChange = { bsDay = it }
                    )
                } else {
                    // AD Date Picker
                    ADDateSelector(
                        date = adDate,
                        language = language,
                        onDateChange = { adDate = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = getString(StringKey.CANCEL),
                            color = AppTheme.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Always return AD date
                            val finalDate = if (currentDateSystem == DateSystem.BS) {
                                BikramSambatConverter.bsToAd(bsYear, bsMonth, bsDay) ?: adDate
                            } else {
                                adDate
                            }
                            onDateSelected(finalDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.AccentPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = getString(StringKey.OK),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSystemToggle(
    currentSystem: DateSystem,
    language: Language,
    onSystemChange: (DateSystem) -> Unit
) {
    Surface(
        color = AppTheme.ChipBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DateSystem.entries.forEach { system ->
                val isSelected = currentSystem == system
                Surface(
                    onClick = { onSystemChange(system) },
                    color = if (isSelected) AppTheme.AccentPrimary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (system) {
                            DateSystem.BS -> if (language == Language.NEPALI) "वि.सं." else "BS"
                            DateSystem.AD -> if (language == Language.NEPALI) "ई.सं." else "AD"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else AppTheme.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BSDateSelector(
    year: Int,
    month: Int,
    day: Int,
    daysInMonth: Int,
    language: Language,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Year Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = getString(StringKey.DATE_YEAR),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.TextMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (year > BikramSambatConverter.MIN_BS_YEAR) onYearChange(year - 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous year",
                        tint = AppTheme.TextSecondary
                    )
                }
                Surface(
                    color = AppTheme.ChipBackground,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = BikramSambatConverter.formatNumber(year, language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                IconButton(
                    onClick = { if (year < BikramSambatConverter.MAX_BS_YEAR) onYearChange(year + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Next year",
                        tint = AppTheme.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Month Selector
        Text(
            text = getString(StringKey.DATE_MONTH),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        val monthListState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, month - 2))
        LazyRow(
            state = monthListState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(12) { index ->
                val monthNum = index + 1
                val isSelected = monthNum == month
                val monthName = when (language) {
                    Language.ENGLISH -> BikramSambatConverter.NEPALI_MONTHS_EN[index]
                    Language.NEPALI -> BikramSambatConverter.NEPALI_MONTHS_NE[index]
                }

                Surface(
                    onClick = { onMonthChange(monthNum) },
                    color = if (isSelected) AppTheme.AccentPrimary else AppTheme.ChipBackground,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else AppTheme.TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day Selector
        Text(
            text = getString(StringKey.DATE_DAY),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        DayGrid(
            selectedDay = day,
            daysInMonth = daysInMonth,
            language = language,
            onDayChange = onDayChange
        )
    }
}

@Composable
private fun ADDateSelector(
    date: LocalDate,
    language: Language,
    onDateChange: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Year Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = getString(StringKey.DATE_YEAR),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.TextMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val newDate = date.minusYears(1)
                        if (newDate.year >= 1913) onDateChange(newDate)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous year",
                        tint = AppTheme.TextSecondary
                    )
                }
                Surface(
                    color = AppTheme.ChipBackground,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = BikramSambatConverter.formatNumber(date.year, language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                IconButton(
                    onClick = {
                        val newDate = date.plusYears(1)
                        if (newDate.year <= 2044) onDateChange(newDate)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Next year",
                        tint = AppTheme.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Month Selector
        Text(
            text = getString(StringKey.DATE_MONTH),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        val monthListState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, date.monthValue - 2))
        LazyRow(
            state = monthListState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(12) { index ->
                val monthNum = index + 1
                val isSelected = monthNum == date.monthValue
                val monthName = getMonthName(monthNum, language)

                Surface(
                    onClick = {
                        val maxDay = date.withMonth(monthNum).lengthOfMonth()
                        val newDay = minOf(date.dayOfMonth, maxDay)
                        onDateChange(date.withMonth(monthNum).withDayOfMonth(newDay))
                    },
                    color = if (isSelected) AppTheme.AccentPrimary else AppTheme.ChipBackground,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else AppTheme.TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day Selector
        Text(
            text = getString(StringKey.DATE_DAY),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        val daysInMonth = date.lengthOfMonth()
        DayGrid(
            selectedDay = date.dayOfMonth,
            daysInMonth = daysInMonth,
            language = language,
            onDayChange = { day ->
                onDateChange(date.withDayOfMonth(day))
            }
        )
    }
}

@Composable
private fun DayGrid(
    selectedDay: Int,
    daysInMonth: Int,
    language: Language,
    onDayChange: (Int) -> Unit
) {
    val days = (1..daysInMonth).toList()
    val rows = days.chunked(7)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowDays.forEach { day ->
                    val isSelected = day == selectedDay
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AppTheme.AccentPrimary else Color.Transparent
                            )
                            .then(
                                if (!isSelected) Modifier.border(
                                    1.dp,
                                    AppTheme.BorderColor,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable { onDayChange(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BikramSambatConverter.formatNumber(day, language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppTheme.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                // Fill remaining cells in row
                repeat(7 - rowDays.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun getMonthName(month: Int, language: Language): String {
    val monthNames = when (language) {
        Language.ENGLISH -> listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        Language.NEPALI -> listOf(
            "जनवरी", "फेब्रुअरी", "मार्च", "अप्रिल", "मे", "जुन",
            "जुलाई", "अगस्ट", "सेप्टेम्बर", "अक्टोबर", "नोभेम्बर", "डिसेम्बर"
        )
    }
    return monthNames.getOrElse(month - 1) { "" }
}
