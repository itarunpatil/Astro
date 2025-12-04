package com.astro.storm.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.MuhurtaCalculator
import com.astro.storm.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Production-Grade Muhurta Screen for Vedic Astrology
 *
 * Features:
 * - Today's Panchanga elements
 * - Choghadiya table for the day
 * - Rahukala, Yamaghanta, Gulika display
 * - Activity-based muhurta search
 * - Date range muhurta finder
 *
 * @author AstroStorm - Ultra-Precision Vedic Astrology
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuhurtaScreen(
    chart: VedicChart?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedActivity by remember { mutableStateOf(MuhurtaCalculator.ActivityType.GENERAL) }
    var todayMuhurta by remember { mutableStateOf<MuhurtaCalculator.MuhurtaDetails?>(null) }
    var choghadiyaList by remember { mutableStateOf<List<MuhurtaCalculator.ChoghadiyaInfo>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<MuhurtaCalculator.MuhurtaSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showActivityPicker by remember { mutableStateOf(false) }

    // Location from chart or default
    val latitude = chart?.birthData?.latitude ?: 28.6139 // Default: Delhi
    val longitude = chart?.birthData?.longitude ?: 77.2090
    val timezone = chart?.birthData?.timezone ?: "Asia/Kolkata"

    // Calculate muhurta when date changes
    LaunchedEffect(selectedDate) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val calculator = MuhurtaCalculator(context)
            try {
                val now = LocalDateTime.of(selectedDate, LocalTime.now())
                todayMuhurta = calculator.calculateMuhurta(now, latitude, longitude, timezone)
                val (dayChoghadiyas, _) = calculator.getDailyChoghadiya(selectedDate, latitude, longitude, timezone)
                choghadiyaList = dayChoghadiyas
            } finally {
                calculator.close()
            }
        }
        isLoading = false
    }

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Today", "Find Muhurta")

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
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            // Date selector
            DateSelectorBar(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                onShowDatePicker = { showDatePicker = true }
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = AppTheme.AccentPrimary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) AppTheme.AccentPrimary else AppTheme.TextMuted
                            )
                        }
                    )
                }
            }

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.AccentPrimary)
                }
            } else {
                when (selectedTab) {
                    0 -> TodayTab(
                        muhurta = todayMuhurta,
                        choghadiyaList = choghadiyaList,
                        selectedDate = selectedDate
                    )
                    1 -> FindMuhurtaTab(
                        context = context,
                        selectedActivity = selectedActivity,
                        onActivityChange = { selectedActivity = it },
                        searchResults = searchResults,
                        isSearching = isSearching,
                        latitude = latitude,
                        longitude = longitude,
                        timezone = timezone,
                        onSearch = { startDate, endDate, activity ->
                            scope.launch {
                                isSearching = true
                                withContext(Dispatchers.IO) {
                                    val calculator = MuhurtaCalculator(context)
                                    try {
                                        searchResults = calculator.findAuspiciousMuhurtas(
                                            activity, startDate, endDate, latitude, longitude, timezone
                                        )
                                    } finally {
                                        calculator.close()
                                    }
                                }
                                isSearching = false
                            }
                        }
                    )
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = AppTheme.AccentPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = AppTheme.TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = AppTheme.CardBackground
            )
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
}

@Composable
private fun DateSelectorBar(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Previous day",
                    tint = AppTheme.TextPrimary
                )
            }

            Row(
                modifier = Modifier
                    .clickable(onClick = onShowDatePicker)
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
                Text(
                    selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextPrimary
                )
            }

            IconButton(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Next day",
                    tint = AppTheme.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun TodayTab(
    muhurta: MuhurtaCalculator.MuhurtaDetails?,
    choghadiyaList: List<MuhurtaCalculator.ChoghadiyaInfo>,
    selectedDate: LocalDate
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        if (muhurta == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data available",
                        color = AppTheme.TextMuted
                    )
                }
            }
            return@LazyColumn
        }

        // Current Muhurta Score
        item {
            CurrentMuhurtaCard(muhurta)
        }

        // Panchanga Elements
        item {
            PanchangaCard(muhurta)
        }

        // Inauspicious Periods
        item {
            InauspiciousPeriodsCard(muhurta)
        }

        // Choghadiya Table
        item {
            ChoghadiyaCard(choghadiyaList, muhurta.choghadiya)
        }

        // Suitable Activities
        if (muhurta.suitableActivities.isNotEmpty()) {
            item {
                ActivitiesCard(
                    title = "Suitable Activities",
                    activities = muhurta.suitableActivities,
                    isPositive = true
                )
            }
        }

        // Avoid Activities
        if (muhurta.avoidActivities.isNotEmpty()) {
            item {
                ActivitiesCard(
                    title = "Activities to Avoid",
                    activities = muhurta.avoidActivities,
                    isPositive = false
                )
            }
        }

        // Recommendations
        if (muhurta.recommendations.isNotEmpty()) {
            item {
                RecommendationsCard(muhurta.recommendations)
            }
        }
    }
}

@Composable
private fun CurrentMuhurtaCard(muhurta: MuhurtaCalculator.MuhurtaDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score circle
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { muhurta.overallScore / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    color = getScoreColor(muhurta.overallScore),
                    trackColor = AppTheme.ChipBackground
                )

                Text(
                    "${muhurta.overallScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                if (muhurta.isAuspicious) "Auspicious Time" else "Average Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (muhurta.isAuspicious) AppTheme.SuccessColor else AppTheme.WarningColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Choghadiya
            Surface(
                color = getChoghadiyaColor(muhurta.choghadiya.choghadiya).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "${muhurta.choghadiya.choghadiya.displayName} Choghadiya",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = getChoghadiyaColor(muhurta.choghadiya.choghadiya),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hora
            Text(
                "${muhurta.hora.lord.displayName} Hora",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
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
            Text(
                "Panchanga",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of panchanga elements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PanchangaItem(
                    label = "Vara",
                    value = muhurta.vara.displayName,
                    modifier = Modifier.weight(1f)
                )
                PanchangaItem(
                    label = "Tithi",
                    value = muhurta.tithi.name,
                    isPositive = muhurta.tithi.isAuspicious,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PanchangaItem(
                    label = "Karana",
                    value = muhurta.karana.name,
                    isPositive = muhurta.karana.isAuspicious,
                    modifier = Modifier.weight(1f)
                )
                PanchangaItem(
                    label = "Sunrise/Sunset",
                    value = "${formatTime(muhurta.sunrise)} - ${formatTime(muhurta.sunset)}",
                    modifier = Modifier.weight(1f)
                )
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
    Column(modifier = modifier.padding(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
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

@Composable
private fun InauspiciousPeriodsCard(muhurta: MuhurtaCalculator.MuhurtaDetails) {
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

            Spacer(modifier = Modifier.height(12.dp))

            InauspiciousPeriodRow(
                name = "Rahukala",
                startTime = muhurta.inauspiciousPeriods.rahukala.startTime,
                endTime = muhurta.inauspiciousPeriods.rahukala.endTime,
                severity = "High"
            )

            Spacer(modifier = Modifier.height(8.dp))

            InauspiciousPeriodRow(
                name = "Yamaghanta",
                startTime = muhurta.inauspiciousPeriods.yamaghanta.startTime,
                endTime = muhurta.inauspiciousPeriods.yamaghanta.endTime,
                severity = "Medium"
            )

            Spacer(modifier = Modifier.height(8.dp))

            InauspiciousPeriodRow(
                name = "Gulika Kala",
                startTime = muhurta.inauspiciousPeriods.gulikaKala.startTime,
                endTime = muhurta.inauspiciousPeriods.gulikaKala.endTime,
                severity = "Medium"
            )
        }
    }
}

@Composable
private fun InauspiciousPeriodRow(
    name: String,
    startTime: LocalTime,
    endTime: LocalTime,
    severity: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (severity) {
                            "High" -> AppTheme.ErrorColor
                            else -> AppTheme.WarningColor
                        }
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextPrimary
            )
        }

        Text(
            "${formatTime(startTime)} - ${formatTime(endTime)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppTheme.TextSecondary
        )
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
            Text(
                "Day Choghadiya",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            choghadiyaList.forEach { chog ->
                val isCurrent = chog.choghadiya == currentChoghadiya.choghadiya &&
                        chog.startTime == currentChoghadiya.startTime
                ChoghadiyaRow(chog, isCurrent)
                if (chog != choghadiyaList.last()) {
                    Spacer(modifier = Modifier.height(6.dp))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrent) getChoghadiyaColor(choghadiya.choghadiya).copy(alpha = 0.15f)
                else Color.Transparent
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(getChoghadiyaColor(choghadiya.choghadiya).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    choghadiya.choghadiya.displayName.take(1),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = getChoghadiyaColor(choghadiya.choghadiya)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    choghadiya.choghadiya.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = AppTheme.TextPrimary
                )
                Text(
                    choghadiya.choghadiya.nature.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = getChoghadiyaColor(choghadiya.choghadiya)
                )
            }
        }

        Text(
            "${formatTime(choghadiya.startTime)} - ${formatTime(choghadiya.endTime)}",
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.TextMuted
        )
    }
}

@Composable
private fun ActivitiesCard(
    title: String,
    activities: List<MuhurtaCalculator.ActivityType>,
    isPositive: Boolean
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
                    if (isPositive) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = if (isPositive) AppTheme.SuccessColor else AppTheme.ErrorColor,
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

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    ActivityChip(activity, isPositive)
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
    Surface(
        color = if (isPositive) AppTheme.SuccessColor.copy(alpha = 0.1f)
        else AppTheme.ErrorColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getActivityIcon(activity),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isPositive) AppTheme.SuccessColor else AppTheme.ErrorColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                activity.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = if (isPositive) AppTheme.SuccessColor else AppTheme.ErrorColor
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
        colors = CardDefaults.cardColors(containerColor = AppTheme.InfoColor.copy(alpha = 0.1f)),
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

            recommendations.forEach { rec ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•", color = AppTheme.InfoColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        rec,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FindMuhurtaTab(
    context: android.content.Context,
    selectedActivity: MuhurtaCalculator.ActivityType,
    onActivityChange: (MuhurtaCalculator.ActivityType) -> Unit,
    searchResults: List<MuhurtaCalculator.MuhurtaSearchResult>,
    isSearching: Boolean,
    latitude: Double,
    longitude: Double,
    timezone: String,
    onSearch: (LocalDate, LocalDate, MuhurtaCalculator.ActivityType) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Activity selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Select Activity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MuhurtaCalculator.ActivityType.entries.toList()) { activity ->
                            FilterChip(
                                selected = activity == selectedActivity,
                                onClick = { onActivityChange(activity) },
                                label = { Text(activity.displayName) },
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
                                    labelColor = AppTheme.TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Activity description
        item {
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppTheme.AccentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            getActivityIcon(selectedActivity),
                            contentDescription = null,
                            tint = AppTheme.AccentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            selectedActivity.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            selectedActivity.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
            }
        }

        // Search button
        item {
            Button(
                onClick = { onSearch(startDate, endDate, selectedActivity) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.AccentPrimary
                ),
                enabled = !isSearching
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppTheme.ButtonText,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (isSearching) "Searching..." else "Find Auspicious Dates",
                    color = AppTheme.ButtonText
                )
            }
        }

        // Results
        if (searchResults.isNotEmpty()) {
            item {
                Text(
                    "Found ${searchResults.size} auspicious times",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(searchResults) { result ->
                SearchResultCard(result)
            }
        } else if (!isSearching) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = AppTheme.TextSubtle,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Search for auspicious muhurtas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(result: MuhurtaCalculator.MuhurtaSearchResult) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        result.dateTime.format(dateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        result.dateTime.format(timeFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.AccentPrimary
                    )
                }

                // Score badge
                Surface(
                    color = getScoreColor(result.score).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${result.score}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = getScoreColor(result.score),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Day", result.vara.displayName)
                DetailItem("Nakshatra", result.nakshatra.displayName)
                DetailItem("Choghadiya", result.choghadiya.displayName)
            }

            // Reasons
            if (result.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                result.reasons.forEach { reason ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = AppTheme.SuccessColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            reason,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.SuccessColor
                        )
                    }
                }
            }

            // Warnings
            if (result.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                result.warnings.forEach { warning ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = AppTheme.WarningColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            warning,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.WarningColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.TextMuted
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = AppTheme.TextSecondary
        )
    }
}

// Helper functions
private fun formatTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return time.format(formatter)
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 60 -> Color(0xFF8BC34A)
        score >= 40 -> Color(0xFFFFC107)
        else -> Color(0xFFFF9800)
    }
}

private fun getChoghadiyaColor(choghadiya: MuhurtaCalculator.Choghadiya): Color {
    return when (choghadiya.nature) {
        MuhurtaCalculator.ChoghadiyaNature.EXCELLENT -> Color(0xFF4CAF50)
        MuhurtaCalculator.ChoghadiyaNature.VERY_GOOD -> Color(0xFF8BC34A)
        MuhurtaCalculator.ChoghadiyaNature.GOOD -> Color(0xFFFFC107)
        MuhurtaCalculator.ChoghadiyaNature.NEUTRAL -> AppTheme.TextMuted
        MuhurtaCalculator.ChoghadiyaNature.INAUSPICIOUS -> Color(0xFFF44336)
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
        MuhurtaCalculator.ActivityType.GRIHA_PRAVESHA -> Icons.Default.Home
        MuhurtaCalculator.ActivityType.NAMING_CEREMONY -> Icons.Default.ChildCare
    }
}
