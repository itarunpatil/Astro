package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.localization.BikramSambatConverter
import com.astro.storm.data.localization.DateSystem
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalDateSystem
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.getLocalizedName
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.Gender
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.components.BSDatePickerDialog
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.ChartViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * Profile Edit Screen - Edit existing birth chart profile details
 *
 * Features:
 * - Edit name, location, date/time, coordinates
 * - Gender selection
 * - Timezone selection
 * - Location search with geocoding integration
 * - Input validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    chart: VedicChart?,
    viewModel: ChartViewModel,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val language = LocalLanguage.current
    val dateSystem = LocalDateSystem.current

    if (chart == null) {
        EmptyChartScreen(
            title = stringResource(StringKey.EDIT_PROFILE_TITLE),
            message = stringResource(StringKey.EDIT_PROFILE_NO_DATA),
            onBack = onBack
        )
        return
    }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Initialize form fields with existing chart data
    var name by remember { mutableStateOf(chart.birthData.name) }
    var selectedGender by remember { mutableStateOf(chart.birthData.gender) }
    var locationLabel by remember { mutableStateOf(chart.birthData.location) }
    var selectedDate by remember { mutableStateOf(chart.birthData.dateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(chart.birthData.dateTime.toLocalTime()) }
    var latitude by remember { mutableStateOf(chart.birthData.latitude.toString()) }
    var longitude by remember { mutableStateOf(chart.birthData.longitude.toString()) }
    var selectedTimezone by remember { mutableStateOf(chart.birthData.timezone) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showBSDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTimezoneDropdown by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorKey by remember { mutableStateOf<StringKey?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Common timezones
    val timezones = remember {
        listOf(
            "Asia/Kathmandu",
            "Asia/Kolkata",
            "Asia/Dubai",
            "Asia/Singapore",
            "Asia/Tokyo",
            "Asia/Shanghai",
            "Asia/Hong_Kong",
            "Europe/London",
            "Europe/Paris",
            "Europe/Berlin",
            "America/New_York",
            "America/Los_Angeles",
            "America/Chicago",
            "America/Denver",
            "Australia/Sydney",
            "Pacific/Auckland"
        ).plus(
            TimeZone.getAvailableIDs()
                .filter { it.contains("/") && !it.startsWith("Etc/") }
                .sorted()
        ).distinct()
    }

    // Extract localized string outside of regular function (stringResource is @Composable)
    val unknownText = stringResource(StringKey.MISC_UNKNOWN)

    fun saveProfile() {
        try {
            val lat = latitude.toDoubleOrNull()
            val lon = longitude.toDoubleOrNull()

            if (lat == null || lon == null) {
                errorKey = StringKey.ERROR_INVALID_COORDS
                showError = true
                return
            }

            if (lat < -90 || lat > 90) {
                errorKey = StringKey.ERROR_LATITUDE_RANGE
                showError = true
                return
            }

            if (lon < -180 || lon > 180) {
                errorKey = StringKey.ERROR_LONGITUDE_RANGE
                showError = true
                return
            }

            isSaving = true
            val dateTime = LocalDateTime.of(selectedDate, selectedTime)
            val updatedBirthData = BirthData(
                name = name.ifBlank { unknownText },
                dateTime = dateTime,
                latitude = lat,
                longitude = lon,
                timezone = selectedTimezone,
                location = locationLabel.ifBlank { unknownText },
                gender = selectedGender
            )

            scope.launch {
                viewModel.calculateChart(updatedBirthData)
                isSaving = false
                onSaveComplete()
            }
        } catch (e: Exception) {
            errorKey = StringKey.ERROR_CHECK_INPUT
            showError = true
            isSaving = false
        }
    }

    Scaffold(
        containerColor = AppTheme.ScreenBackground,
        topBar = {
            ProfileEditTopBar(
                onBack = onBack,
                onSave = { saveProfile() },
                isSaving = isSaving
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.ScreenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Identity Section
                SectionTitle(stringResource(StringKey.INPUT_IDENTITY))
                Spacer(modifier = Modifier.height(12.dp))

                StyledOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(StringKey.INPUT_FULL_NAME),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Selection
                Text(
                    text = stringResource(StringKey.INPUT_GENDER),
                    fontSize = 14.sp,
                    color = AppTheme.TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Gender.entries.forEach { gender ->
                        GenderChip(
                            text = gender.getLocalizedName(language),
                            isSelected = selectedGender == gender,
                            onClick = { selectedGender = gender },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                StyledOutlinedTextField(
                    value = locationLabel,
                    onValueChange = { locationLabel = it },
                    label = stringResource(StringKey.INPUT_LOCATION),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Date & Time Section
                SectionTitle(stringResource(StringKey.INPUT_DATE_TIME))
                Spacer(modifier = Modifier.height(12.dp))

                // Format date based on selected date system
                val dateDisplayText = remember(selectedDate, dateSystem, language) {
                    if (dateSystem == DateSystem.BS) {
                        BikramSambatConverter.toBS(selectedDate)?.format(language)
                            ?: selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } else {
                        selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateTimeChip(
                        text = dateDisplayText,
                        onClick = {
                            if (dateSystem == DateSystem.BS) {
                                showBSDatePicker = true
                            } else {
                                showDatePicker = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DateTimeChip(
                        text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timezone Dropdown
                ExposedDropdownMenuBox(
                    expanded = showTimezoneDropdown,
                    onExpandedChange = { showTimezoneDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedTimezone,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(stringResource(StringKey.INPUT_TIMEZONE), color = AppTheme.TextMuted, fontSize = 14.sp)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimezoneDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.TextPrimary,
                            unfocusedTextColor = AppTheme.TextPrimary,
                            focusedBorderColor = AppTheme.BorderColor,
                            unfocusedBorderColor = AppTheme.BorderColor,
                            focusedLabelColor = AppTheme.TextMuted,
                            unfocusedLabelColor = AppTheme.TextMuted,
                            cursorColor = AppTheme.AccentGold,
                            focusedTrailingIconColor = AppTheme.TextMuted,
                            unfocusedTrailingIconColor = AppTheme.TextMuted
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )

                    ExposedDropdownMenu(
                        expanded = showTimezoneDropdown,
                        onDismissRequest = { showTimezoneDropdown = false },
                        modifier = Modifier
                            .background(AppTheme.CardBackground)
                            .heightIn(max = 300.dp)
                    ) {
                        timezones.forEach { timezone ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = timezone,
                                        color = AppTheme.TextPrimary,
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    selectedTimezone = timezone
                                    showTimezoneDropdown = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = AppTheme.TextPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Coordinates Section
                SectionTitle(stringResource(StringKey.INPUT_COORDINATES))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StyledOutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = stringResource(StringKey.INPUT_LATITUDE),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        )
                    )
                    StyledOutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = stringResource(StringKey.INPUT_LONGITUDE),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Date Picker Dialog (AD)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(StringKey.BTN_OK), color = AppTheme.AccentGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(StringKey.BTN_CANCEL), color = AppTheme.TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = AppTheme.CardBackground)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = AppTheme.CardBackground,
                    titleContentColor = AppTheme.TextPrimary,
                    headlineContentColor = AppTheme.TextPrimary,
                    weekdayContentColor = AppTheme.TextMuted,
                    subheadContentColor = AppTheme.TextMuted,
                    yearContentColor = AppTheme.TextPrimary,
                    currentYearContentColor = AppTheme.AccentGold,
                    selectedYearContentColor = AppTheme.ScreenBackground,
                    selectedYearContainerColor = AppTheme.AccentGold,
                    dayContentColor = AppTheme.TextPrimary,
                    selectedDayContentColor = AppTheme.ScreenBackground,
                    selectedDayContainerColor = AppTheme.AccentGold,
                    todayContentColor = AppTheme.AccentGold,
                    todayDateBorderColor = AppTheme.AccentGold,
                    navigationContentColor = AppTheme.TextMuted
                )
            )
        }
    }

    // BS Date Picker Dialog
    if (showBSDatePicker) {
        val currentBSDate = remember(selectedDate) {
            BikramSambatConverter.toBS(selectedDate) ?: BikramSambatConverter.today()
        }
        BSDatePickerDialog(
            initialDate = currentBSDate,
            onDismiss = { showBSDatePicker = false },
            onConfirm = { bsDate ->
                BikramSambatConverter.toAD(bsDate)?.let { adDate ->
                    selectedDate = adDate
                }
                showBSDatePicker = false
            }
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        )
    }

    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = {
                showError = false
                errorKey = null
            },
            title = {
                Text(stringResource(StringKey.ERROR_INPUT), color = AppTheme.TextPrimary, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text(
                    errorKey?.let { stringResource(it) } ?: errorMessage,
                    color = AppTheme.TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showError = false
                    errorKey = null
                }) {
                    Text(stringResource(StringKey.BTN_OK), color = AppTheme.AccentGold)
                }
            },
            containerColor = AppTheme.CardBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(StringKey.EDIT_PROFILE_TITLE),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(StringKey.BTN_BACK),
                    tint = AppTheme.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppTheme.AccentGold,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(StringKey.BTN_SAVE),
                        tint = AppTheme.AccentGold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.ScreenBackground
        )
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.TextPrimary,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label, color = AppTheme.TextMuted, fontSize = 14.sp)
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.TextPrimary,
            unfocusedTextColor = AppTheme.TextPrimary,
            focusedBorderColor = AppTheme.BorderColor,
            unfocusedBorderColor = AppTheme.BorderColor,
            focusedLabelColor = AppTheme.TextMuted,
            unfocusedLabelColor = AppTheme.TextMuted,
            cursorColor = AppTheme.AccentGold
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
}

@Composable
private fun GenderChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) AppTheme.AccentGold else AppTheme.ChipBackground,
        border = BorderStroke(1.dp, if (isSelected) AppTheme.AccentGold else AppTheme.BorderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = if (isSelected) AppTheme.ScreenBackground else AppTheme.TextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun DateTimeChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = AppTheme.ChipBackground,
        border = BorderStroke(1.dp, AppTheme.BorderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = AppTheme.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val timePickerState = rememberTimePickerState(is24Hour = true)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppTheme.CardBackground,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(StringKey.INPUT_SELECT_TIME),
                    color = AppTheme.TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = AppTheme.CardBackground,
                        clockDialColor = AppTheme.ChipBackground,
                        clockDialSelectedContentColor = AppTheme.ScreenBackground,
                        clockDialUnselectedContentColor = AppTheme.TextPrimary,
                        selectorColor = AppTheme.AccentGold,
                        periodSelectorBorderColor = AppTheme.BorderColor,
                        periodSelectorSelectedContainerColor = AppTheme.AccentGold,
                        periodSelectorUnselectedContainerColor = AppTheme.CardBackground,
                        periodSelectorSelectedContentColor = AppTheme.ScreenBackground,
                        periodSelectorUnselectedContentColor = AppTheme.TextMuted,
                        timeSelectorSelectedContainerColor = AppTheme.AccentGold,
                        timeSelectorUnselectedContainerColor = AppTheme.ChipBackground,
                        timeSelectorSelectedContentColor = AppTheme.ScreenBackground,
                        timeSelectorUnselectedContentColor = AppTheme.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(StringKey.BTN_CANCEL), color = AppTheme.TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(StringKey.BTN_OK), color = AppTheme.AccentGold)
                    }
                }
            }
        }
    }
}
