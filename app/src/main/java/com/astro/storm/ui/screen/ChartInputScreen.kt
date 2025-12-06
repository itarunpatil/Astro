package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.Gender
import com.astro.storm.ui.components.LocationSearchField
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

// Centralized Theme Colors
private object ChartTheme {
    val ScreenBackground = Color(0xFF1C1410)
    val CardBackground = Color(0xFF2A201A)
    val AccentColor = Color(0xFFB8A99A)
    val TextPrimary = Color(0xFFE8DFD6)
    val TextSecondary = Color(0xFF9E8F85) // Slightly darkened for better contrast
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
    val scrollState = rememberScrollState()

    // -- Form State --
    // Using rememberSaveable to survive configuration changes (rotation)
    var name by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf(Gender.PREFER_NOT_TO_SAY) }
    var locationLabel by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var altitude by rememberSaveable { mutableStateOf("") }
    
    // Date/Time State
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var selectedTime by rememberSaveable { mutableStateOf(LocalTime.of(10, 0)) }
    var selectedTimezone by rememberSaveable { mutableStateOf(ZoneId.systemDefault().id) }

    // Dialog Visibility State
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTimezoneSheet by remember { mutableStateOf(false) } // Changed to Sheet/Dialog for better UX

    // -- Navigation Logic --
    var isCalculating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChartUiState.Success -> {
                if (isCalculating) {
                    val chart = (uiState as ChartUiState.Success).chart
                    viewModel.saveChart(chart)
                }
            }
            is ChartUiState.Saved -> {
                if (isCalculating) {
                    isCalculating = false
                    onChartCalculated()
                }
            }
            is ChartUiState.Error -> {
                isCalculating = false
            }
            else -> {}
        }
    }

    // -- UI --
    Scaffold(
        containerColor = ChartTheme.ScreenBackground,
        topBar = {
            ChartTopBar(onBack = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .imePadding() // Handles keyboard overlap
        ) {
            // 1. Identity
            SectionHeader("Identity")
            IdentitySection(
                name = name,
                onNameChange = { name = it },
                gender = selectedGender,
                onGenderChange = { selectedGender = it },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Location (Geocoding)
            SectionHeader("Birth Location")
            LocationSection(
                locationLabel = locationLabel,
                onLocationLabelChange = { locationLabel = it },
                onLocationSelected = { locName, lat, lon, timezone ->
                    locationLabel = locName
                    latitude = lat.toString()
                    longitude = lon.toString()
                    // Auto-fill timezone if available from GeocodingService
                    timezone?.let { selectedTimezone = it }
                },
                latitude = latitude,
                onLatChange = { latitude = it },
                longitude = longitude,
                onLonChange = { longitude = it },
                altitude = altitude,
                onAltChange = { altitude = it },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Time Details
            SectionHeader("Date & Time")
            DateTimeSection(
                date = selectedDate,
                time = selectedTime,
                timezone = selectedTimezone,
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true },
                onTimezoneClick = { showTimezoneSheet = true }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 4. Action Button
            // Validate inputs before enabling button
            val isFormValid by remember {
                derivedStateOf {
                    name.isNotBlank() &&
                    locationLabel.isNotBlank() &&
                    latitude.toDoubleOrNull() in -90.0..90.0 &&
                    longitude.toDoubleOrNull() in -180.0..180.0
                }
            }

            GenerateButton(
                enabled = isFormValid && !isCalculating,
                isLoading = uiState is ChartUiState.Calculating,
                onClick = {
                    isCalculating = true
val birthData = BirthData(
    name = name.trim(),
    dateTime = LocalDateTime.of(selectedDate, selectedTime),
    latitude = latitude.toDoubleOrNull() ?: 0.0,
    longitude = longitude.toDoubleOrNull() ?: 0.0,
    timezone = selectedTimezone,
    location = locationLabel.trim(),
    gender = selectedGender
)
                    viewModel.calculateChart(birthData)
                }
            )
            
            if (uiState is ChartUiState.Error) {
                Text(
                    text = (uiState as ChartUiState.Error).message,
                    color = ChartTheme.ErrorColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // -- Dialogs --
    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        selectedDate = java.time.Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = ChartTheme.AccentColor) }
            },
            colors = DatePickerDefaults.colors(containerColor = ChartTheme.CardBackground)
        ) {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(
                    containerColor = ChartTheme.CardBackground,
                    titleContentColor = ChartTheme.TextPrimary,
                    headlineContentColor = ChartTheme.TextPrimary,
                    dayContentColor = ChartTheme.TextPrimary,
                    selectedDayContainerColor = ChartTheme.AccentColor,
                    selectedDayContentColor = ChartTheme.ButtonText
                )
            )
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(selectedTime.hour, selectedTime.minute, is24Hour = true)
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timeState.hour, timeState.minute)
                showTimePicker = false
            }
        ) {
            TimePicker(
                state = timeState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = ChartTheme.ChipBackground,
                    clockDialSelectedContentColor = ChartTheme.ButtonText,
                    selectorColor = ChartTheme.AccentColor,
                    timeSelectorSelectedContainerColor = ChartTheme.AccentColor,
                    timeSelectorSelectedContentColor = ChartTheme.ButtonText
                )
            )
        }
    }
    
    if (showTimezoneSheet) {
        TimezoneSelectionDialog(
            currentTimezone = selectedTimezone,
            onTimezoneSelected = { 
                selectedTimezone = it
                showTimezoneSheet = false 
            },
            onDismiss = { showTimezoneSheet = false }
        )
    }
}

// --------------------------
// Sub-Composables (Clean Architecture)
// --------------------------

@Composable
private fun ChartTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = ChartTheme.TextSecondary
            )
        }
        Text(
            text = "New Birth Chart",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = ChartTheme.TextPrimary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun IdentitySection(
    name: String,
    onNameChange: (String) -> Unit,
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    focusManager: FocusManager
) {
    ChartTextField(
        value = name,
        onValueChange = onNameChange,
        label = "Full Name",
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text("Gender", color = ChartTheme.TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Gender.entries.forEach { item ->
            GenderChip(
                text = item.displayName,
                isSelected = gender == item,
                onClick = { onGenderChange(item) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LocationSection(
    locationLabel: String,
    onLocationLabelChange: (String) -> Unit,
    onLocationSelected: (String, Double, Double, String?) -> Unit,
    latitude: String,
    onLatChange: (String) -> Unit,
    longitude: String,
    onLonChange: (String) -> Unit,
    altitude: String,
    onAltChange: (String) -> Unit,
    focusManager: FocusManager
) {
    // Search Field
    LocationSearchField(
        value = locationLabel,
        onValueChange = onLocationLabelChange,
        // Assumption: Your LocationSearchField is updated to pass Timezone string as 4th param
        // If not, pass null, but update your SearchField component to support the updated Service
        onLocationSelected = onLocationSelected,
        label = "City / Place",
        placeholder = "Search location...",
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChartTextField(
            value = latitude,
            onValueChange = onLatChange,
            label = "Latitude",
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            isError = latitude.isNotEmpty() && latitude.toDoubleOrNull() == null
        )
        ChartTextField(
            value = longitude,
            onValueChange = onLonChange,
            label = "Longitude",
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            isError = longitude.isNotEmpty() && longitude.toDoubleOrNull() == null
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    ChartTextField(
        value = altitude,
        onValueChange = onAltChange,
        label = "Altitude (Optional)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
private fun DateTimeSection(
    date: LocalDate,
    time: LocalTime,
    timezone: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onTimezoneClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ConfigTile(
            icon = Icons.Outlined.DateRange,
            title = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            subtitle = "Date",
            onClick = onDateClick,
            modifier = Modifier.weight(1f)
        )
        ConfigTile(
            icon = Icons.Outlined.Schedule,
            title = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            subtitle = "Time",
            onClick = onTimeClick,
            modifier = Modifier.weight(0.8f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    ConfigTile(
        icon = Icons.Outlined.LocationOn,
        title = timezone.replace("_", " "),
        subtitle = "Timezone",
        onClick = onTimezoneClick,
        modifier = Modifier.fillMaxWidth()
    )
}

// --------------------------
// UI Components
// --------------------------

@Composable
private fun ChartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if(isError) ChartTheme.ErrorColor else ChartTheme.TextSecondary) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ChartTheme.TextPrimary,
            unfocusedTextColor = ChartTheme.TextPrimary,
            focusedBorderColor = if(isError) ChartTheme.ErrorColor else ChartTheme.AccentColor,
            unfocusedBorderColor = if(isError) ChartTheme.ErrorColor else ChartTheme.BorderColor,
            cursorColor = ChartTheme.AccentColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
private fun ConfigTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = ChartTheme.CardBackground,
        border = BorderStroke(1.dp, ChartTheme.BorderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ChartTheme.AccentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = ChartTheme.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, color = ChartTheme.TextSecondary, fontSize = 12.sp)
            }
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
        color = if (isSelected) ChartTheme.AccentColor else ChartTheme.ChipBackground,
        border = BorderStroke(1.dp, if (isSelected) ChartTheme.AccentColor else ChartTheme.BorderColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                color = if (isSelected) ChartTheme.ButtonText else ChartTheme.TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun GenerateButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ChartTheme.ButtonBackground,
            contentColor = ChartTheme.ButtonText,
            disabledContainerColor = ChartTheme.ButtonBackground.copy(alpha = 0.3f),
            disabledContentColor = ChartTheme.ButtonText.copy(alpha = 0.3f)
        )
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ChartTheme.ButtonText, strokeWidth = 2.dp)
        }
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Chart", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = ChartTheme.TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimezoneSelectionDialog(
    currentTimezone: String,
    onTimezoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val allTimezones = remember { ZoneId.getAvailableZoneIds().sorted() }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTimezones = remember(searchQuery) {
        if (searchQuery.isEmpty()) allTimezones 
        else allTimezones.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ChartTheme.CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ChartTheme.TextSecondary) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 500.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search timezone", color = ChartTheme.TextSecondary) },
                leadingIcon = { Icon(Icons.Outlined.Clear, null, tint = ChartTheme.TextSecondary, modifier = Modifier.clickable { searchQuery = "" }) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ChartTheme.TextPrimary,
                    unfocusedTextColor = ChartTheme.TextPrimary,
                    focusedBorderColor = ChartTheme.AccentColor,
                    unfocusedBorderColor = ChartTheme.BorderColor
                )
            )
            
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(filteredTimezones.size) { index ->
                    val tz = filteredTimezones[index]
                    val isSelected = tz == currentTimezone
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTimezoneSelected(tz) }
                            .background(if (isSelected) ChartTheme.AccentColor.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tz.replace("_", " "),
                            color = if (isSelected) ChartTheme.AccentColor else ChartTheme.TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    HorizontalDivider(color = ChartTheme.BorderColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ChartTheme.CardBackground,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = ChartTheme.TextSecondary) }
                    TextButton(onClick = onConfirm) { Text("OK", color = ChartTheme.AccentColor) }
                }
            }
        }
    }
}