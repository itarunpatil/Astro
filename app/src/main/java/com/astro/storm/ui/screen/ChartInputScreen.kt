package com.astro.storm.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.astro.storm.ui.components.BSDatePickerDialog
import com.astro.storm.ui.components.LocationSearchField
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

private object ChartInputTheme {
    val ScreenBackground = Color(0xFF1C1410)
    val CardBackground = Color(0xFF2A201A)
    val AccentColor = Color(0xFFB8A99A)
    val TextPrimary = Color(0xFFE8DFD6)
    val TextSecondary = Color(0xFFB8A99A)
    val BorderColor = Color(0xFF4A3F38)
    val ChipBackground = Color(0xFF3D322B)
    val ButtonBackground = Color(0xFFB8A99A)
    val ButtonText = Color(0xFF1C1410)
    val ErrorColor = Color(0xFFCF6679)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartInputScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit,
    onChartCalculated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val language = LocalLanguage.current
    val dateSystem = LocalDateSystem.current

    var name by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf(Gender.OTHER) }
    var locationLabel by rememberSaveable { mutableStateOf("") }
    var selectedDateMillis by rememberSaveable {
        mutableStateOf(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }
    var selectedHour by rememberSaveable { mutableStateOf(10) }
    var selectedMinute by rememberSaveable { mutableStateOf(0) }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var altitude by rememberSaveable { mutableStateOf("") }
    var selectedTimezone by rememberSaveable { mutableStateOf(ZoneId.systemDefault().id) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showBSDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTimezoneDropdown by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorKey by remember { mutableStateOf<StringKey?>(null) }
    var chartCalculationInitiated by remember { mutableStateOf(false) }

    val selectedDate = remember(selectedDateMillis) {
        java.time.Instant.ofEpochMilli(selectedDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    val selectedTime = remember(selectedHour, selectedMinute) {
        LocalTime.of(selectedHour, selectedMinute)
    }

    val latitudeFocusRequester = remember { FocusRequester() }
    val longitudeFocusRequester = remember { FocusRequester() }
    val altitudeFocusRequester = remember { FocusRequester() }

    val timezones = remember {
        val common = listOf(
            "Asia/Kathmandu", "Asia/Kolkata", "Asia/Dubai", "Asia/Singapore",
            "Asia/Tokyo", "Asia/Shanghai", "Asia/Hong_Kong", "Europe/London",
            "Europe/Paris", "Europe/Berlin", "America/New_York", "America/Los_Angeles",
            "America/Chicago", "America/Denver", "Australia/Sydney", "Pacific/Auckland"
        )
        val all = TimeZone.getAvailableIDs()
            .filter { it.contains("/") && !it.startsWith("Etc/") && !it.startsWith("SystemV/") }
            .sorted()
        (common + all).distinct()
    }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ChartUiState.Success -> {
                if (chartCalculationInitiated) {
                    viewModel.saveChart(state.chart)
                }
            }
            is ChartUiState.Saved -> {
                if (chartCalculationInitiated) {
                    chartCalculationInitiated = false
                    onChartCalculated()
                }
            }
            is ChartUiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
                chartCalculationInitiated = false
            }
            else -> {}
        }
    }

    val scrollState = rememberScrollState()
    val isCalculating = uiState is ChartUiState.Calculating

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChartInputTheme.ScreenBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            ChartInputHeader(onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(28.dp))

            IdentitySection(
                name = name,
                onNameChange = { name = it },
                selectedGender = selectedGender,
                onGenderChange = { selectedGender = it },
                onFocusNext = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LocationSearchField(
                value = locationLabel,
                onValueChange = { locationLabel = it },
                onLocationSelected = { location, lat, lon ->
                    locationLabel = location
                    latitude = String.format(java.util.Locale.US, "%.6f", lat)
                    longitude = String.format(java.util.Locale.US, "%.6f", lon)
                },
                label = stringResource(StringKey.INPUT_LOCATION),
                placeholder = stringResource(StringKey.INPUT_SEARCH_LOCATION)
            )

            Spacer(modifier = Modifier.height(28.dp))

            DateTimeSection(
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                selectedTimezone = selectedTimezone,
                timezones = timezones,
                showTimezoneDropdown = showTimezoneDropdown,
                dateSystem = dateSystem,
                language = language,
                onShowDatePicker = {
                    if (dateSystem == DateSystem.BS) {
                        showBSDatePicker = true
                    } else {
                        showDatePicker = true
                    }
                },
                onShowTimePicker = { showTimePicker = true },
                onTimezoneDropdownChange = { showTimezoneDropdown = it },
                onTimezoneSelected = {
                    selectedTimezone = it
                    showTimezoneDropdown = false
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            CoordinatesSection(
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                onLatitudeChange = { latitude = it },
                onLongitudeChange = { longitude = it },
                onAltitudeChange = { altitude = it },
                latitudeFocusRequester = latitudeFocusRequester,
                longitudeFocusRequester = longitudeFocusRequester,
                altitudeFocusRequester = altitudeFocusRequester,
                onDone = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Extract localized string outside the lambda (stringResource is @Composable)
            val unknownText = stringResource(StringKey.MISC_UNKNOWN)

            GenerateButton(
                isCalculating = isCalculating,
                onClick = {
                    val validationKey = validateInputLocalized(latitude, longitude)
                    if (validationKey != null) {
                        errorKey = validationKey
                        showErrorDialog = true
                        return@GenerateButton
                    }

                    val lat = latitude.toDouble()
                    val lon = longitude.toDouble()
                    val dateTime = LocalDateTime.of(selectedDate, selectedTime)

                    val birthData = BirthData(
                        name = name.ifBlank { unknownText },
                        dateTime = dateTime,
                        latitude = lat,
                        longitude = lon,
                        timezone = selectedTimezone,
                        location = locationLabel.ifBlank { unknownText },
                        gender = selectedGender
                    )

                    chartCalculationInitiated = true
                    viewModel.calculateChart(birthData)
                }
            )
        }
    }

    if (showDatePicker) {
        ChartDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                selectedDateMillis = millis
                showDatePicker = false
            }
        )
    }

    // BS Date Picker (when date system is BS)
    if (showBSDatePicker) {
        val currentBSDate = remember(selectedDate) {
            BikramSambatConverter.toBS(selectedDate) ?: BikramSambatConverter.today()
        }
        BSDatePickerDialog(
            initialDate = currentBSDate,
            onDismiss = { showBSDatePicker = false },
            onConfirm = { bsDate ->
                // Convert BS to AD and update selectedDateMillis
                BikramSambatConverter.toAD(bsDate)?.let { adDate ->
                    selectedDateMillis = adDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
                showBSDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        ChartTimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                errorKey = null
            },
            title = {
                Text(
                    stringResource(StringKey.ERROR_INPUT),
                    color = ChartInputTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    errorKey?.let { stringResource(it) } ?: errorMessage,
                    color = ChartInputTheme.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    errorKey = null
                }) {
                    Text(stringResource(StringKey.BTN_OK), color = ChartInputTheme.AccentColor)
                }
            },
            containerColor = ChartInputTheme.CardBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

private fun validateInput(latitude: String, longitude: String): String? {
    val lat = latitude.toDoubleOrNull()
    val lon = longitude.toDoubleOrNull()

    return when {
        lat == null || lon == null -> "Please enter valid latitude and longitude"
        lat < -90 || lat > 90 -> "Latitude must be between -90 and 90"
        lon < -180 || lon > 180 -> "Longitude must be between -180 and 180"
        else -> null
    }
}

private fun validateInputLocalized(latitude: String, longitude: String): StringKey? {
    val lat = latitude.toDoubleOrNull()
    val lon = longitude.toDoubleOrNull()

    return when {
        lat == null || lon == null -> StringKey.ERROR_INVALID_COORDS
        lat < -90 || lat > 90 -> StringKey.ERROR_LATITUDE_RANGE
        lon < -180 || lon > 180 -> StringKey.ERROR_LONGITUDE_RANGE
        else -> null
    }
}

@Composable
private fun ChartInputHeader(onNavigateBack: () -> Unit) {
    val goBackText = stringResource(StringKey.BTN_BACK)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.semantics { contentDescription = goBackText }
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = null,
                tint = ChartInputTheme.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(StringKey.INPUT_NEW_CHART),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = ChartInputTheme.TextPrimary,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun IdentitySection(
    name: String,
    onNameChange: (String) -> Unit,
    selectedGender: Gender,
    onGenderChange: (Gender) -> Unit,
    onFocusNext: () -> Unit
) {
    val language = LocalLanguage.current
    Column {
        SectionTitle(stringResource(StringKey.INPUT_IDENTITY))
        Spacer(modifier = Modifier.height(12.dp))

        ChartOutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(StringKey.INPUT_FULL_NAME),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onFocusNext() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(StringKey.INPUT_GENDER),
            fontSize = 14.sp,
            color = ChartInputTheme.TextSecondary,
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
                    onClick = { onGenderChange(gender) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeSection(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    selectedTimezone: String,
    timezones: List<String>,
    showTimezoneDropdown: Boolean,
    dateSystem: DateSystem,
    language: Language,
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit,
    onTimezoneDropdownChange: (Boolean) -> Unit,
    onTimezoneSelected: (String) -> Unit
) {
    // Format date based on selected date system
    val dateDisplayText = remember(selectedDate, dateSystem, language) {
        if (dateSystem == DateSystem.BS) {
            BikramSambatConverter.toBS(selectedDate)?.format(language)
                ?: selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } else {
            selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
    }

    val selectDateText = stringResource(StringKey.INPUT_SELECT_DATE)
    val selectTimeText = stringResource(StringKey.INPUT_SELECT_TIME)

    Column {
        SectionTitle(stringResource(StringKey.INPUT_DATE_TIME))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateTimeChip(
                text = dateDisplayText,
                onClick = onShowDatePicker,
                modifier = Modifier.weight(1f),
                contentDescription = selectDateText
            )
            DateTimeChip(
                text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onClick = onShowTimePicker,
                modifier = Modifier.weight(0.7f),
                contentDescription = selectTimeText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = showTimezoneDropdown,
            onExpandedChange = onTimezoneDropdownChange
        ) {
            OutlinedTextField(
                value = selectedTimezone,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(stringResource(StringKey.INPUT_TIMEZONE), color = ChartInputTheme.TextSecondary, fontSize = 14.sp)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimezoneDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = chartTextFieldColors(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            ExposedDropdownMenu(
                expanded = showTimezoneDropdown,
                onDismissRequest = { onTimezoneDropdownChange(false) },
                modifier = Modifier
                    .background(ChartInputTheme.CardBackground)
                    .heightIn(max = 300.dp)
            ) {
                timezones.forEach { timezone ->
                    DropdownMenuItem(
                        text = {
                            Text(text = timezone, color = ChartInputTheme.TextPrimary, fontSize = 14.sp)
                        },
                        onClick = { onTimezoneSelected(timezone) },
                        colors = MenuDefaults.itemColors(textColor = ChartInputTheme.TextPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordinatesSection(
    latitude: String,
    longitude: String,
    altitude: String,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onAltitudeChange: (String) -> Unit,
    latitudeFocusRequester: FocusRequester,
    longitudeFocusRequester: FocusRequester,
    altitudeFocusRequester: FocusRequester,
    onDone: () -> Unit
) {
    Column {
        SectionTitle(stringResource(StringKey.INPUT_COORDINATES))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChartOutlinedTextField(
                value = latitude,
                onValueChange = onLatitudeChange,
                label = stringResource(StringKey.INPUT_LATITUDE),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(latitudeFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { longitudeFocusRequester.requestFocus() }
                )
            )

            ChartOutlinedTextField(
                value = longitude,
                onValueChange = onLongitudeChange,
                label = stringResource(StringKey.INPUT_LONGITUDE),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(longitudeFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { altitudeFocusRequester.requestFocus() }
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChartOutlinedTextField(
            value = altitude,
            onValueChange = onAltitudeChange,
            label = stringResource(StringKey.INPUT_ALTITUDE),
            modifier = Modifier.focusRequester(altitudeFocusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }
}

@Composable
private fun GenerateButton(
    isCalculating: Boolean,
    onClick: () -> Unit
) {
    val buttonContentDesc = stringResource(StringKey.BTN_GENERATE_SAVE)
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = buttonContentDesc },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ChartInputTheme.ButtonBackground,
            contentColor = ChartInputTheme.ButtonText,
            disabledContainerColor = ChartInputTheme.ButtonBackground.copy(alpha = 0.5f),
            disabledContentColor = ChartInputTheme.ButtonText.copy(alpha = 0.5f)
        ),
        enabled = !isCalculating
    ) {
        Crossfade(targetState = isCalculating, label = "button_content") { calculating ->
            if (calculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = ChartInputTheme.ButtonText,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(StringKey.BTN_GENERATE_SAVE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartDatePickerDialog(
    initialDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onConfirm)
                }
            ) {
                Text(stringResource(StringKey.BTN_OK), color = ChartInputTheme.AccentColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(StringKey.BTN_CANCEL), color = ChartInputTheme.TextSecondary)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = ChartInputTheme.CardBackground)
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = ChartInputTheme.CardBackground,
                titleContentColor = ChartInputTheme.TextPrimary,
                headlineContentColor = ChartInputTheme.TextPrimary,
                weekdayContentColor = ChartInputTheme.TextSecondary,
                subheadContentColor = ChartInputTheme.TextSecondary,
                yearContentColor = ChartInputTheme.TextPrimary,
                currentYearContentColor = ChartInputTheme.AccentColor,
                selectedYearContentColor = ChartInputTheme.ButtonText,
                selectedYearContainerColor = ChartInputTheme.AccentColor,
                dayContentColor = ChartInputTheme.TextPrimary,
                selectedDayContentColor = ChartInputTheme.ButtonText,
                selectedDayContainerColor = ChartInputTheme.AccentColor,
                todayContentColor = ChartInputTheme.AccentColor,
                todayDateBorderColor = ChartInputTheme.AccentColor,
                navigationContentColor = ChartInputTheme.TextSecondary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ChartInputTheme.CardBackground,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(StringKey.INPUT_SELECT_TIME),
                    color = ChartInputTheme.TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = ChartInputTheme.CardBackground,
                        clockDialColor = ChartInputTheme.ChipBackground,
                        clockDialSelectedContentColor = ChartInputTheme.ButtonText,
                        clockDialUnselectedContentColor = ChartInputTheme.TextPrimary,
                        selectorColor = ChartInputTheme.AccentColor,
                        periodSelectorBorderColor = ChartInputTheme.BorderColor,
                        periodSelectorSelectedContainerColor = ChartInputTheme.AccentColor,
                        periodSelectorUnselectedContainerColor = ChartInputTheme.CardBackground,
                        periodSelectorSelectedContentColor = ChartInputTheme.ButtonText,
                        periodSelectorUnselectedContentColor = ChartInputTheme.TextSecondary,
                        timeSelectorSelectedContainerColor = ChartInputTheme.AccentColor,
                        timeSelectorUnselectedContainerColor = ChartInputTheme.ChipBackground,
                        timeSelectorSelectedContentColor = ChartInputTheme.ButtonText,
                        timeSelectorUnselectedContentColor = ChartInputTheme.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(StringKey.BTN_CANCEL), color = ChartInputTheme.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
                    ) {
                        Text(stringResource(StringKey.BTN_OK), color = ChartInputTheme.AccentColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = ChartInputTheme.TextPrimary,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun ChartOutlinedTextField(
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
        label = { Text(label, color = ChartInputTheme.TextSecondary, fontSize = 14.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = chartTextFieldColors(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
}

@Composable
private fun chartTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ChartInputTheme.TextPrimary,
    unfocusedTextColor = ChartInputTheme.TextPrimary,
    focusedBorderColor = ChartInputTheme.AccentColor,
    unfocusedBorderColor = ChartInputTheme.BorderColor,
    focusedLabelColor = ChartInputTheme.AccentColor,
    unfocusedLabelColor = ChartInputTheme.TextSecondary,
    cursorColor = ChartInputTheme.AccentColor,
    focusedTrailingIconColor = ChartInputTheme.TextSecondary,
    unfocusedTrailingIconColor = ChartInputTheme.TextSecondary
)

@Composable
private fun DateTimeChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else Modifier
            ),
        shape = RoundedCornerShape(26.dp),
        color = ChartInputTheme.ChipBackground,
        border = BorderStroke(1.dp, ChartInputTheme.BorderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = ChartInputTheme.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
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
        color = if (isSelected) ChartInputTheme.AccentColor else ChartInputTheme.ChipBackground,
        border = BorderStroke(1.dp, if (isSelected) ChartInputTheme.AccentColor else ChartInputTheme.BorderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = if (isSelected) ChartInputTheme.ButtonText else ChartInputTheme.TextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}