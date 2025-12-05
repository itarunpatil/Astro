package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.MuhurtaCalculator
import com.astro.storm.ui.theme.AppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private sealed interface MuhurtaUiState {
    data object Loading : MuhurtaUiState
    data class Success(
        val muhurta: MuhurtaCalculator.MuhurtaDetails,
        val choghadiyaList: List<MuhurtaCalculator.ChoghadiyaInfo>
    ) : MuhurtaUiState
    data class Error(val message: String) : MuhurtaUiState
}

private sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Searching : SearchUiState
    data class Results(val results: List<MuhurtaCalculator.MuhurtaSearchResult>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@Stable
private object MuhurtaFormatters {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
    val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
}

private enum class InauspiciousSeverity { HIGH, MEDIUM, LOW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuhurtaScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedActivity by remember { mutableStateOf(MuhurtaCalculator.ActivityType.GENERAL) }
    var uiState by remember { mutableStateOf<MuhurtaUiState>(MuhurtaUiState.Loading) }
    var searchState by remember { mutableStateOf<SearchUiState>(SearchUiState.Idle) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val latitude = remember(chart) { chart?.birthData?.latitude ?: 28.6139 }
    val longitude = remember(chart) { chart?.birthData?.longitude ?: 77.2090 }
    val timezone = remember(chart) { chart?.birthData?.timezone ?: "Asia/Kolkata" }

    val calculator = remember(context) { MuhurtaCalculator(context) }

    DisposableEffect(calculator) {
        onDispose { calculator.close() }
    }

    val loadMuhurtaData: suspend (LocalDate) -> Unit = remember(calculator, latitude, longitude, timezone) {
        { date ->
            uiState = MuhurtaUiState.Loading
            try {
                withContext(Dispatchers.IO) {
                    val now = LocalDateTime.of(date, LocalTime.now())
                    val muhurta = calculator.calculateMuhurta(now, latitude, longitude, timezone)
                    val (dayChoghadiyas, _) = calculator.getDailyChoghadiya(date, latitude, longitude, timezone)
                    uiState = MuhurtaUiState.Success(muhurta, dayChoghadiyas)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = MuhurtaUiState.Error(e.message ?: "Failed to calculate muhurta")
            }
        }
    }

    LaunchedEffect(selectedDate) {
        loadMuhurtaData(selectedDate)
    }

    val tabs = remember { listOf("Today", "Find Muhurta") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Muhurta",
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = AppTheme.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.ScreenBackground
                )
            )
        },
        containerColor = AppTheme.ScreenBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DateSelectorBar(
                    selectedDate = selectedDate,
                    onDateChange = { newDate ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedDate = newDate
                    },
                    onShowDatePicker = { showDatePicker = true }
                )
            }

            MuhurtaTabs(
                selectedTab = selectedTab,
                tabs = tabs,
                onTabSelected = { index ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTab = index
                }
            )

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "tab_content"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> TodayTabContent(
                        uiState = uiState,
                        onRetry = {
                            scope.launch { loadMuhurtaData(selectedDate) }
                        }
                    )
                    1 -> FindMuhurtaTabContent(
                        selectedActivity = selectedActivity,
                        onActivityChange = { activity ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedActivity = activity
                        },
                        searchState = searchState,
                        onSearch = { startDate, endDate, activity ->
                            scope.launch {
                                searchState = SearchUiState.Searching
                                try {
                                    withContext(Dispatchers.IO) {
                                        val results = calculator.findAuspiciousMuhurtas(
                                            activity, startDate, endDate, latitude, longitude, timezone
                                        )
                                        searchState = SearchUiState.Results(results)
                                    }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    searchState = SearchUiState.Error(e.message ?: "Search failed")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        MuhurtaDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuhurtaDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK", color = AppTheme.AccentPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppTheme.TextMuted)
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
                dayContentColor = AppTheme.TextPrimary,
                selectedDayContainerColor = AppTheme.AccentPrimary,
                todayContentColor = AppTheme.AccentPrimary,
                todayDateBorderColor = AppTheme.AccentPrimary
            )
        )
    }
}

@Composable
private fun MuhurtaTabs(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = AppTheme.AccentPrimary,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = AppTheme.AccentPrimary
                )
            }
        },
        divider = {
            HorizontalDivider(thickness = 1.dp, color = AppTheme.DividerColor.copy(alpha = 0.3f))
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == index) AppTheme.AccentPrimary else AppTheme.TextMuted
                    )
                }
            )
        }
    }
}

@Composable
private fun DateSelectorBar(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit
) {
    val isToday = selectedDate == LocalDate.now()
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDateChange(selectedDate.minusDays(1)) },
                modifier = Modifier.semantics { contentDescription = "Previous day" }
            ) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = AppTheme.TextPrimary
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
    interactionSource = interactionSource,
    indication = LocalIndication.current
) { onShowDatePicker() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selectedDate.format(MuhurtaFormatters.dateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextPrimary
                    )
                    if (isToday) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.AccentPrimary
                        )
                    }
                }
            }

            IconButton(
                onClick = { onDateChange(selectedDate.plusDays(1)) },
                modifier = Modifier.semantics { contentDescription = "Next day" }
            ) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun TodayTabContent(
    uiState: MuhurtaUiState,
    onRetry: () -> Unit
) {
    when (uiState) {
        is MuhurtaUiState.Loading -> LoadingContent()
        is MuhurtaUiState.Error -> ErrorContent(message = uiState.message, onRetry = onRetry)
        is MuhurtaUiState.Success -> TodayTabList(muhurta = uiState.muhurta, choghadiyaList = uiState.choghadiyaList)
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = AppTheme.AccentPrimary, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Calculating muhurta...",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = AppTheme.ErrorColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.AccentPrimary)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry", color = AppTheme.ButtonText)
            }
        }
    }
}

@Composable
private fun TodayTabList(
    muhurta: MuhurtaCalculator.MuhurtaDetails,
    choghadiyaList: List<MuhurtaCalculator.ChoghadiyaInfo>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "current_muhurta") { CurrentMuhurtaCard(muhurta) }
        item(key = "panchanga") { PanchangaCard(muhurta) }
        item(key = "inauspicious") { InauspiciousPeriodsCard(muhurta) }
        item(key = "choghadiya") { ChoghadiyaCard(choghadiyaList, muhurta.choghadiya) }
        if (muhurta.suitableActivities.isNotEmpty()) {
            item(key = "suitable_activities") {
                ActivitiesCard(title = "Suitable Activities", activities = muhurta.suitableActivities, isPositive = true)
            }
        }
        if (muhurta.avoidActivities.isNotEmpty()) {
            item(key = "avoid_activities") {
                ActivitiesCard(title = "Activities to Avoid", activities = muhurta.avoidActivities, isPositive = false)
            }
        }
        if (muhurta.recommendations.isNotEmpty()) {
            item(key = "recommendations") { RecommendationsCard(muhurta.recommendations) }
        }
    }
}

@Composable
private fun CurrentMuhurtaCard(muhurta: MuhurtaCalculator.MuhurtaDetails) {
    val scoreColor = remember(muhurta.overallScore) { getScoreColor(muhurta.overallScore) }
    val choghadiyaColor = remember(muhurta.choghadiya.choghadiya) { getChoghadiyaColor(muhurta.choghadiya.choghadiya) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(scoreColor.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { muhurta.overallScore / 100f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = scoreColor,
                        trackColor = AppTheme.ChipBackground
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${muhurta.overallScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.TextPrimary
                        )
                        Text("Score", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (muhurta.isAuspicious) "Auspicious Time" else "Average Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (muhurta.isAuspicious) AppTheme.SuccessColor else AppTheme.WarningColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(color = choghadiyaColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "${muhurta.choghadiya.choghadiya.displayName} Choghadiya",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = choghadiyaColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${muhurta.hora.lord.displayName} Hora",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
        }
    }
}

@Composable
private fun PanchangaCard(muhurta: MuhurtaCalculator.MuhurtaDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Panchanga",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PanchangaItem(label = "Vara", value = muhurta.vara.displayName, modifier = Modifier.weight(1f))
                    PanchangaItem(
                        label = "Tithi",
                        value = muhurta.tithi.name,
                        isPositive = muhurta.tithi.isAuspicious,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PanchangaItem(
                        label = "Nakshatra",
                        value = "${muhurta.nakshatra.nakshatra.displayName} (Pada ${muhurta.nakshatra.pada})",
                        modifier = Modifier.weight(1f)
                    )
                    PanchangaItem(
                        label = "Yoga",
                        value = muhurta.yoga.name,
                        isPositive = muhurta.yoga.isAuspicious,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PanchangaItem(
                        label = "Karana",
                        value = muhurta.karana.name,
                        isPositive = muhurta.karana.isAuspicious,
                        modifier = Modifier.weight(1f)
                    )
                    PanchangaItem(
                        label = "Sunrise / Sunset",
                        value = "${muhurta.sunrise.format(MuhurtaFormatters.timeFormatter)} - ${muhurta.sunset.format(MuhurtaFormatters.timeFormatter)}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PanchangaItem(
    label: String,
    value: String,
    isPositive: Boolean? = null,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, color = AppTheme.CardBackgroundElevated, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = when (isPositive) {
                    true -> AppTheme.SuccessColor
                    false -> AppTheme.WarningColor
                    null -> AppTheme.TextPrimary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InauspiciousPeriodsCard(muhurta: MuhurtaCalculator.MuhurtaDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.ErrorColor.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = AppTheme.ErrorColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Inauspicious Periods",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InauspiciousPeriodRow(
                    name = "Rahukala",
                    description = "Avoid important work",
                    startTime = muhurta.inauspiciousPeriods.rahukala.startTime,
                    endTime = muhurta.inauspiciousPeriods.rahukala.endTime,
                    severity = InauspiciousSeverity.HIGH
                )
                InauspiciousPeriodRow(
                    name = "Yamaghanta",
                    description = "Avoid travel",
                    startTime = muhurta.inauspiciousPeriods.yamaghanta.startTime,
                    endTime = muhurta.inauspiciousPeriods.yamaghanta.endTime,
                    severity = InauspiciousSeverity.MEDIUM
                )
                InauspiciousPeriodRow(
                    name = "Gulika Kala",
                    description = "Avoid new beginnings",
                    startTime = muhurta.inauspiciousPeriods.gulikaKala.startTime,
                    endTime = muhurta.inauspiciousPeriods.gulikaKala.endTime,
                    severity = InauspiciousSeverity.MEDIUM
                )
                        }
        }
    }
}

@Composable
private fun InauspiciousPeriodRow(
    name: String,
    description: String,
    startTime: LocalTime,
    endTime: LocalTime,
    severity: InauspiciousSeverity
) {
    val severityColor = remember(severity) {
        when (severity) {
            InauspiciousSeverity.HIGH -> Color(0xFFE53935)
            InauspiciousSeverity.MEDIUM -> Color(0xFFFF9800)
            InauspiciousSeverity.LOW -> Color(0xFFFFC107)
        }
    }

    val now = LocalTime.now()
    val isActive = now.isAfter(startTime) && now.isBefore(endTime)

    Surface(
        color = if (isActive) severityColor.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) Modifier.border(1.dp, severityColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(severityColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = AppTheme.TextPrimary
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = severityColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        description,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Surface(
                color = AppTheme.CardBackgroundElevated,
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppTheme.TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${startTime.format(MuhurtaFormatters.timeFormatter)} - ${endTime.format(MuhurtaFormatters.timeFormatter)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoghadiyaCard(
    choghadiyaList: List<MuhurtaCalculator.ChoghadiyaInfo>,
    currentChoghadiya: MuhurtaCalculator.ChoghadiyaInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Day Choghadiya",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${choghadiyaList.size} periods",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                choghadiyaList.forEach { chog ->
                    val isCurrent = chog.choghadiya == currentChoghadiya.choghadiya &&
                            chog.startTime == currentChoghadiya.startTime
                    ChoghadiyaRow(chog, isCurrent)
                }
            }
        }
    }
}

@Composable
private fun ChoghadiyaRow(
    choghadiya: MuhurtaCalculator.ChoghadiyaInfo,
    isCurrent: Boolean
) {
    val choghadiyaColor = remember(choghadiya.choghadiya) { getChoghadiyaColor(choghadiya.choghadiya) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(
                    width = 1.5.dp,
                    color = choghadiyaColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) else Modifier
            ),
        color = if (isCurrent) choghadiyaColor.copy(alpha = 0.12f) else AppTheme.CardBackgroundElevated,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(choghadiyaColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        choghadiya.choghadiya.displayName.take(2),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = choghadiyaColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            choghadiya.choghadiya.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            color = AppTheme.TextPrimary
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = choghadiyaColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "NOW",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        choghadiya.choghadiya.nature.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = choghadiyaColor
                    )
                }
            }

            Text(
                "${choghadiya.startTime.format(MuhurtaFormatters.timeFormatter)} - ${choghadiya.endTime.format(MuhurtaFormatters.timeFormatter)}",
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.TextMuted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivitiesCard(
    title: String,
    activities: List<MuhurtaCalculator.ActivityType>,
    isPositive: Boolean
) {
    val containerColor = if (isPositive)
        AppTheme.SuccessColor.copy(alpha = 0.05f)
    else
        AppTheme.ErrorColor.copy(alpha = 0.05f)

    val accentColor = if (isPositive) AppTheme.SuccessColor else AppTheme.ErrorColor
    val icon = if (isPositive) Icons.Filled.CheckCircle else Icons.Filled.Cancel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { activity ->
                    ActivityChip(activity = activity, isPositive = isPositive)
                }
            }
        }
    }
}

@Composable
private fun ActivityChip(
    activity: MuhurtaCalculator.ActivityType,
    isPositive: Boolean
) {
    val chipColor = if (isPositive)
        AppTheme.SuccessColor.copy(alpha = 0.12f)
    else
        AppTheme.ErrorColor.copy(alpha = 0.12f)

    val contentColor = if (isPositive) AppTheme.SuccessColor else AppTheme.ErrorColor
    val icon = remember(activity) { getActivityIcon(activity) }

    Surface(color = chipColor, shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                activity.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.InfoColor.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = AppTheme.InfoColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recommendations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendations.forEachIndexed { index, rec ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = AppTheme.InfoColor.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.InfoColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            rec,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FindMuhurtaTabContent(
    selectedActivity: MuhurtaCalculator.ActivityType,
    onActivityChange: (MuhurtaCalculator.ActivityType) -> Unit,
    searchState: SearchUiState,
    onSearch: (LocalDate, LocalDate, MuhurtaCalculator.ActivityType) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "activity_selector") {
            ActivitySelectorCard(
                selectedActivity = selectedActivity,
                onActivityChange = onActivityChange
            )
        }

        item(key = "activity_info") {
            ActivityInfoCard(selectedActivity = selectedActivity)
        }

        item(key = "date_range") {
            DateRangeCard(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it }
            )
        }

        item(key = "search_button") {
            SearchButton(
                isSearching = searchState is SearchUiState.Searching,
                onSearch = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSearch(startDate, endDate, selectedActivity)
                }
            )
        }

        when (searchState) {
            is SearchUiState.Idle -> {
                item(key = "empty_state") { SearchEmptyState() }
            }
            is SearchUiState.Searching -> {
                item(key = "searching") { SearchingState() }
            }
            is SearchUiState.Results -> {
                if (searchState.results.isEmpty()) {
                    item(key = "no_results") { NoResultsState() }
                } else {
                    item(key = "results_header") {
                        ResultsHeader(count = searchState.results.size)
                    }
                    items(
                        items = searchState.results,
                        key = { it.dateTime.toString() }
                    ) { result ->
                        SearchResultCard(result = result)
                    }
                }
            }
            is SearchUiState.Error -> {
                item(key = "error") { SearchErrorState(message = searchState.message) }
            }
        }
    }
}

@Composable
private fun ActivitySelectorCard(
    selectedActivity: MuhurtaCalculator.ActivityType,
    onActivityChange: (MuhurtaCalculator.ActivityType) -> Unit
) {
    val activities = remember { MuhurtaCalculator.ActivityType.entries.toList() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Select Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(activities, key = { it.name }) { activity ->
                    FilterChip(
                        selected = activity == selectedActivity,
                        onClick = { onActivityChange(activity) },
                        label = {
                            Text(
                                activity.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                getActivityIcon(activity),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.AccentPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = AppTheme.AccentPrimary,
                            selectedLeadingIconColor = AppTheme.AccentPrimary,
                            containerColor = AppTheme.ChipBackground,
                            labelColor = AppTheme.TextSecondary,
                            iconColor = AppTheme.TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = activity == selectedActivity,
                            borderColor = Color.Transparent,
                            selectedBorderColor = AppTheme.AccentPrimary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityInfoCard(selectedActivity: MuhurtaCalculator.ActivityType) {
    val icon = remember(selectedActivity) { getActivityIcon(selectedActivity) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackgroundElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AppTheme.AccentPrimary.copy(alpha = 0.2f),
                                AppTheme.AccentPrimary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    selectedActivity.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    selectedActivity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeCard(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Date Range",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatePickerButton(
                    label = "From",
                    date = startDate,
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerButton(
                    label = "To",
                    date = endDate,
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showStartPicker) {
        MuhurtaDatePickerDialog(
            selectedDate = startDate,
            onDateSelected = { date ->
                onStartDateChange(date)
                if (date.isAfter(endDate)) {
                    onEndDateChange(date.plusDays(30))
                }
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MuhurtaDatePickerDialog(
            selectedDate = endDate,
            onDateSelected = { date ->
                if (!date.isBefore(startDate)) {
                    onEndDateChange(date)
                }
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun DatePickerButton(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = AppTheme.CardBackgroundElevated,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                date.format(MuhurtaFormatters.shortDateFormatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
        }
    }
}

@Composable
private fun SearchButton(
    isSearching: Boolean,
    onSearch: () -> Unit
) {
    Button(
        onClick = onSearch,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.AccentPrimary),
        shape = RoundedCornerShape(12.dp),
        enabled = !isSearching
    ) {
        AnimatedContent(
            targetState = isSearching,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "search_button_content"
        ) { searching ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (searching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppTheme.ButtonText,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Searching...", color = AppTheme.ButtonText, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AppTheme.ButtonText
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Find Auspicious Dates", color = AppTheme.ButtonText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = AppTheme.TextSubtle,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Search for Auspicious Times",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select an activity and date range to find\nthe most favorable muhurtas",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SearchingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppTheme.AccentPrimary, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Finding auspicious times...",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun NoResultsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.EventBusy,
                contentDescription = null,
                tint = AppTheme.WarningColor,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Auspicious Times Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Try expanding your date range",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun SearchErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = AppTheme.ErrorColor,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Search Failed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultsHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Auspicious Times",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.TextPrimary
        )
        Surface(
            color = AppTheme.SuccessColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "$count found",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.SuccessColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SearchResultCard(result: MuhurtaCalculator.MuhurtaSearchResult) {
    val scoreColor = remember(result.score) { getScoreColor(result.score) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        result.dateTime.format(MuhurtaFormatters.shortDateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AppTheme.AccentPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            result.dateTime.format(MuhurtaFormatters.timeFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.AccentPrimary
                        )
                    }
                }

                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${result.score}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            "Score",
                            style = MaterialTheme.typography.labelSmall,
                            color = scoreColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultDetailChip(
                    label = "Day",
                    value = result.vara.displayName,
                    modifier = Modifier.weight(1f)
                )
                ResultDetailChip(
                    label = "Nakshatra",
                    value = result.nakshatra.displayName,
                    modifier = Modifier.weight(1f)
                )
                ResultDetailChip(
                    label = "Choghadiya",
                    value = result.choghadiya.displayName,
                    modifier = Modifier.weight(1f)
                )
            }

            if (result.reasons.isNotEmpty() || result.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = AppTheme.DividerColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (result.reasons.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.reasons.forEach { reason ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = AppTheme.SuccessColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }
                    }
                }
            }

            if (result.warnings.isNotEmpty()) {
                if (result.reasons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.warnings.forEach { warning ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = AppTheme.WarningColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultDetailChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AppTheme.CardBackgroundElevated,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 85 -> Color(0xFF2E7D32)
        score >= 70 -> Color(0xFF4CAF50)
        score >= 55 -> Color(0xFF8BC34A)
        score >= 40 -> Color(0xFFFFC107)
        score >= 25 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }
}

private fun getChoghadiyaColor(choghadiya: MuhurtaCalculator.Choghadiya): Color {
    return when (choghadiya.nature) {
        MuhurtaCalculator.ChoghadiyaNature.EXCELLENT -> Color(0xFF2E7D32)
        MuhurtaCalculator.ChoghadiyaNature.VERY_GOOD -> Color(0xFF4CAF50)
        MuhurtaCalculator.ChoghadiyaNature.GOOD -> Color(0xFF8BC34A)
        MuhurtaCalculator.ChoghadiyaNature.NEUTRAL -> Color(0xFF9E9E9E)
        MuhurtaCalculator.ChoghadiyaNature.INAUSPICIOUS -> Color(0xFFE53935)
    }
}

private fun getActivityIcon(activity: MuhurtaCalculator.ActivityType): ImageVector {
    return when (activity) {
        MuhurtaCalculator.ActivityType.MARRIAGE -> Icons.Outlined.Favorite
        MuhurtaCalculator.ActivityType.TRAVEL -> Icons.Outlined.Flight
        MuhurtaCalculator.ActivityType.BUSINESS -> Icons.Outlined.Business
        MuhurtaCalculator.ActivityType.PROPERTY -> Icons.Outlined.Home
        MuhurtaCalculator.ActivityType.EDUCATION -> Icons.Outlined.School
        MuhurtaCalculator.ActivityType.MEDICAL -> Icons.Outlined.LocalHospital
        MuhurtaCalculator.ActivityType.VEHICLE -> Icons.Outlined.DirectionsCar
        MuhurtaCalculator.ActivityType.SPIRITUAL -> Icons.Outlined.SelfImprovement
        MuhurtaCalculator.ActivityType.GENERAL -> Icons.Outlined.Star
        MuhurtaCalculator.ActivityType.GRIHA_PRAVESHA -> Icons.Outlined.Home
        MuhurtaCalculator.ActivityType.NAMING_CEREMONY -> Icons.Outlined.ChildCare
    }
}
