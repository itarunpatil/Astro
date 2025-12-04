package com.astro.storm.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.local.ChartEntity
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.MatchmakingCalculator
import com.astro.storm.ui.theme.AppTheme
import com.astro.storm.ui.viewmodel.ChartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchmakingScreen(
    savedCharts: List<ChartEntity>,
    viewModel: ChartViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedBrideId by remember { mutableStateOf<Long?>(null) }
    var selectedGroomId by remember { mutableStateOf<Long?>(null) }
    var brideChart by remember { mutableStateOf<VedicChart?>(null) }
    var groomChart by remember { mutableStateOf<VedicChart?>(null) }

    var matchingResult by remember { mutableStateOf<MatchmakingCalculator.MatchmakingResult?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showBrideSelector by remember { mutableStateOf(false) }
    var showGroomSelector by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Gunas", "Doshas", "Nakshatras", "Remedies")

    val animatedProgress by animateFloatAsState(
        targetValue = matchingResult?.let { (it.totalPoints / it.maxPoints).toFloat() } ?: 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(selectedBrideId) {
        selectedBrideId?.let { id ->
            brideChart = withContext(Dispatchers.IO) { viewModel.getChartById(id) }
        } ?: run { brideChart = null }
    }

    LaunchedEffect(selectedGroomId) {
        selectedGroomId?.let { id ->
            groomChart = withContext(Dispatchers.IO) { viewModel.getChartById(id) }
        } ?: run { groomChart = null }
    }

    LaunchedEffect(brideChart, groomChart) {
        if (brideChart != null && groomChart != null) {
            isCalculating = true
            errorMessage = null
            delay(300)
            try {
                matchingResult = withContext(Dispatchers.Default) {
                    MatchmakingCalculator.calculateMatchmaking(brideChart!!, groomChart!!)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Calculation failed"
            }
            isCalculating = false
        } else {
            matchingResult = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Kundli Milan",
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary,
                            fontSize = 18.sp
                        )
                        AnimatedVisibility(visible = matchingResult != null) {
                            Text(
                                "Ashtakoota Matching",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = AppTheme.TextPrimary
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(visible = matchingResult != null) {
                        Row {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showShareSheet = true
                            }) {
                                Icon(
                                    Icons.Outlined.Share,
                                    contentDescription = "Share report",
                                    tint = AppTheme.TextSecondary
                                )
                            }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                matchingResult?.let { result ->
                                    val report = generateTextReport(result, brideChart, groomChart)
                                    clipboardManager.setText(AnnotatedString(report))
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Report copied to clipboard")
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy report",
                                    tint = AppTheme.TextSecondary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.ScreenBackground
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = AppTheme.CardBackground,
                    contentColor = AppTheme.TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = AppTheme.ScreenBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                EnhancedProfileSelectionSection(
                    savedCharts = savedCharts,
                    selectedBrideId = selectedBrideId,
                    selectedGroomId = selectedGroomId,
                    brideChart = brideChart,
                    groomChart = groomChart,
                    onSelectBride = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showBrideSelector = true
                    },
                    onSelectGroom = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showGroomSelector = true
                    },
                    onSwapProfiles = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val tempId = selectedBrideId
                        selectedBrideId = selectedGroomId
                        selectedGroomId = tempId
                    },
                    onClearSelection = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedBrideId = null
                        selectedGroomId = null
                        brideChart = null
                        groomChart = null
                        matchingResult = null
                    }
                )
            }

            if (isCalculating) {
                item {
                    CalculatingState()
                }
            }

            errorMessage?.let { error ->
                item {
                    ErrorCard(error) {
                        errorMessage = null
                        scope.launch {
                            delay(100)
                            if (brideChart != null && groomChart != null) {
                                isCalculating = true
                                try {
                                    matchingResult = withContext(Dispatchers.Default) {
                                        MatchmakingCalculator.calculateMatchmaking(brideChart!!, groomChart!!)
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                }
                                isCalculating = false
                            }
                        }
                    }
                }
            }

            matchingResult?.let { result ->
                item {
                    EnhancedCompatibilityScoreCard(result, animatedProgress)
                }

                item {
                    QuickInsightsRow(result)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    TabSelector(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { index ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = index
                        }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        item {
                            OverviewSection(result, brideChart, groomChart)
                        }
                    }
                    1 -> {
                        item {
                            GunaSummaryHeader(result)
                        }
                        itemsIndexed(result.gunaAnalyses) { index, guna ->
                            AnimatedGunaCard(guna, index)
                        }
                    }
                    2 -> {
                        item {
                            DoshaSection(result)
                        }
                    }
                    3 -> {
                        item {
                            NakshatraSection(result, brideChart, groomChart)
                        }
                    }
                    4 -> {
                        item {
                            EnhancedRemediesSection(result)
                        }
                    }
                }
            }

            if (matchingResult == null && !isCalculating && errorMessage == null) {
                item {
                    EmptyMatchingState(
                        hasBride = brideChart != null,
                        hasGroom = groomChart != null,
                        hasCharts = savedCharts.isNotEmpty()
                    )
                }
            }
        }
    }

    if (showBrideSelector) {
        EnhancedProfileSelectorBottomSheet(
            title = "Select Bride's Chart",
            icon = Icons.Filled.Female,
            accentColor = AppTheme.LifeAreaLove,
            charts = savedCharts,
            selectedId = selectedBrideId,
            excludeId = selectedGroomId,
            onSelect = { id ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedBrideId = id
                showBrideSelector = false
            },
            onDismiss = { showBrideSelector = false }
        )
    }

    if (showGroomSelector) {
        EnhancedProfileSelectorBottomSheet(
            title = "Select Groom's Chart",
            icon = Icons.Filled.Male,
            accentColor = AppTheme.AccentTeal,
            charts = savedCharts,
            selectedId = selectedGroomId,
            excludeId = selectedBrideId,
            onSelect = { id ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedGroomId = id
                showGroomSelector = false
            },
            onDismiss = { showGroomSelector = false }
        )
    }

    if (showShareSheet) {
        ShareOptionsSheet(
            result = matchingResult,
            brideChart = brideChart,
            groomChart = groomChart,
            onDismiss = { showShareSheet = false },
            onCopyToClipboard = { report ->
                clipboardManager.setText(AnnotatedString(report))
                scope.launch {
                    snackbarHostState.showSnackbar("Report copied to clipboard")
                }
                showShareSheet = false
            }
        )
    }
}

@Composable
private fun EnhancedProfileSelectionSection(
    savedCharts: List<ChartEntity>,
    selectedBrideId: Long?,
    selectedGroomId: Long?,
    brideChart: VedicChart?,
    groomChart: VedicChart?,
    onSelectBride: () -> Unit,
    onSelectGroom: () -> Unit,
    onSwapProfiles: () -> Unit,
    onClearSelection: () -> Unit
) {
    val hasSelection = brideChart != null || groomChart != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
                
                AnimatedVisibility(visible = hasSelection) {
                    Row {
                        if (brideChart != null && groomChart != null) {
                            IconButton(
                                onClick = onSwapProfiles,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.SwapHoriz,
                                    contentDescription = "Swap profiles",
                                    tint = AppTheme.AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onClearSelection,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear selection",
                                tint = AppTheme.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedProfileCard(
                    label = "Bride",
                    chart = brideChart,
                    icon = Icons.Filled.Female,
                    color = AppTheme.LifeAreaLove,
                    onClick = onSelectBride,
                    modifier = Modifier.weight(1f)
                )

                ConnectionIndicator(
                    isConnected = brideChart != null && groomChart != null,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                EnhancedProfileCard(
                    label = "Groom",
                    chart = groomChart,
                    icon = Icons.Filled.Male,
                    color = AppTheme.AccentTeal,
                    onClick = onSelectGroom,
                    modifier = Modifier.weight(1f)
                )
            }

            if (savedCharts.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = AppTheme.InfoColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppTheme.InfoColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create birth charts first to use matchmaking",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.InfoColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedProfileCard(
    label: String,
    chart: VedicChart?,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (chart != null) 1f else 0.98f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (chart != null) color.copy(alpha = 0.08f) else AppTheme.ChipBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (chart != null) null else androidx.compose.foundation.BorderStroke(
            1.dp, AppTheme.BorderColor.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (chart != null) color.copy(alpha = 0.15f) else AppTheme.ChipBackground
                    )
                    .border(
                        width = 2.dp,
                        color = if (chart != null) color else AppTheme.BorderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (chart != null) color else AppTheme.TextMuted,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (chart != null) color else AppTheme.TextMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                chart?.birthData?.name ?: "Tap to select",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (chart != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (chart != null) AppTheme.TextPrimary else AppTheme.TextSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            chart?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    getNakshatraName(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ConnectionIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isConnected) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Connected",
                tint = AppTheme.LifeAreaLove,
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale)
            )
        } else {
            Icon(
                Icons.Outlined.FavoriteBorder,
                contentDescription = "Not connected",
                tint = AppTheme.TextSubtle,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CalculatingState() {
    val infiniteTransition = rememberInfiniteTransition(label = "calculating")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = AppTheme.AccentPrimary.copy(alpha = 0.3f),
                    strokeWidth = 4.dp
                )
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Analyzing Cosmic Compatibility",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Calculating Ashtakoota & Doshas...",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextMuted
            )
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.ErrorColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = AppTheme.ErrorColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Calculation Error",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.ErrorColor
                )
                Text(
                    error,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.ErrorColor.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry", color = AppTheme.ErrorColor)
            }
        }
    }
}

@Composable
private fun EnhancedCompatibilityScoreCard(
    result: MatchmakingCalculator.MatchmakingResult,
    animatedProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            getRatingColor(result.rating).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 14.dp,
                        color = AppTheme.ChipBackground,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 14.dp,
                        color = getRatingColor(result.rating),
                        strokeCap = StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", result.totalPoints),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            text = "out of ${result.maxPoints.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = getRatingColor(result.rating).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            getRatingIcon(result.rating),
                            contentDescription = null,
                            tint = getRatingColor(result.rating),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            result.rating.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = getRatingColor(result.rating)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = getRatingColor(result.rating)
                        )) {
                            append(String.format("%.1f%%", result.percentage))
                        }
                        append(" Compatibility")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.rating.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickInsightsRow(result: MatchmakingCalculator.MatchmakingResult) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val hasNadiDosha = result.gunaAnalyses.find { it.name == "Nadi" }?.obtainedPoints == 0.0
        val hasBhakootDosha = result.gunaAnalyses.find { it.name == "Bhakoot" }?.obtainedPoints == 0.0

        item {
            QuickInsightChip(
                label = "Manglik",
                value = getManglikQuickStatus(result),
                color = getManglikStatusColor(result.manglikCompatibility)
            )
        }

        if (hasNadiDosha) {
            item {
                QuickInsightChip(
                    label = "Nadi",
                    value = "Dosha Present",
                    color = AppTheme.ErrorColor
                )
            }
        }

        if (hasBhakootDosha) {
            item {
                QuickInsightChip(
                    label = "Bhakoot",
                    value = "Needs Attention",
                    color = AppTheme.WarningColor
                )
            }
        }

        item {
            QuickInsightChip(
                label = "Gunas",
                value = "${result.totalPoints.toInt()}/${result.maxPoints.toInt()}",
                color = if (result.totalPoints >= 18) AppTheme.SuccessColor else AppTheme.WarningColor
            )
        }
    }
}

@Composable
private fun QuickInsightChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun TabSelector(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, title ->
            val isSelected = selectedTab == index
            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) AppTheme.AccentPrimary else Color.Transparent,
                animationSpec = tween(200),
                label = "tab_color"
            )

            Surface(
                onClick = { onTabSelected(index) },
                color = animatedColor.copy(alpha = if (isSelected) 0.15f else 0f),
                shape = RoundedCornerShape(10.dp),
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(
                    1.dp, AppTheme.BorderColor
                ) else null
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) AppTheme.AccentPrimary else AppTheme.TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(
    result: MatchmakingCalculator.MatchmakingResult,
    brideChart: VedicChart?,
    groomChart: VedicChart?
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = AppTheme.AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Guna Score Distribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                result.gunaAnalyses.forEachIndexed { index, guna ->
                    EnhancedGunaScoreBar(guna)
                    if (index != result.gunaAnalyses.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        if (brideChart != null && groomChart != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileComparisonCard(brideChart, groomChart)
        }

        if (result.specialConsiderations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            SpecialConsiderationsCard(result.specialConsiderations)
        }
    }
}

@Composable
private fun EnhancedGunaScoreBar(guna: MatchmakingCalculator.GunaAnalysis) {
    val animatedProgress by animateFloatAsState(
        targetValue = (guna.obtainedPoints / guna.maxPoints).toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "guna_progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    guna.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "(${guna.description})",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
            Surface(
                color = if (guna.isPositive) AppTheme.SuccessColor.copy(alpha = 0.1f) 
                       else AppTheme.WarningColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (guna.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppTheme.ChipBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (guna.isPositive) 
                                listOf(AppTheme.SuccessColor.copy(alpha = 0.7f), AppTheme.SuccessColor)
                            else 
                                listOf(AppTheme.WarningColor.copy(alpha = 0.7f), AppTheme.WarningColor)
                        )
                    )
            )
        }
    }
}

@Composable
private fun ProfileComparisonCard(
    brideChart: VedicChart,
    groomChart: VedicChart
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Compare,
                    contentDescription = null,
                    tint = AppTheme.AccentSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Profile Comparison",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        brideChart.birthData.name ?: "Bride",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.LifeAreaLove
                    )
                }
                Text("vs", color = AppTheme.TextMuted)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        groomChart.birthData.name ?: "Groom",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.AccentTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = AppTheme.DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            ComparisonRow("Moon Sign", getRashiName(brideChart), getRashiName(groomChart))
            ComparisonRow("Nakshatra", getNakshatraName(brideChart), getNakshatraName(groomChart))
            ComparisonRow("Pada", getPada(brideChart), getPada(groomChart))
        }
    }
}

@Composable
private fun ComparisonRow(label: String, brideValue: String, groomValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            brideValue,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.LifeAreaLove,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Surface(
            color = AppTheme.ChipBackground,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Text(
            groomValue,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.AccentTeal,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SpecialConsiderationsCard(considerations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = AppTheme.InfoColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Key Considerations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            considerations.forEach { consideration ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppTheme.AccentPrimary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        consideration,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GunaSummaryHeader(result: MatchmakingCalculator.MatchmakingResult) {
    val positiveCount = result.gunaAnalyses.count { it.isPositive }
    val totalCount = result.gunaAnalyses.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.AccentPrimary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$positiveCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.SuccessColor
                )
                Text("Favorable", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(AppTheme.DividerColor)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${totalCount - positiveCount}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.WarningColor
                )
                Text("Needs Attention", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(AppTheme.DividerColor)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    String.format("%.1f", result.totalPoints),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.AccentPrimary
                )
                Text("Total Score", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
            }
        }
    }
}

@Composable
private fun AnimatedGunaCard(
    guna: MatchmakingCalculator.GunaAnalysis,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (guna.isPositive) AppTheme.SuccessColor.copy(alpha = 0.12f)
                                    else AppTheme.WarningColor.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (guna.isPositive) Icons.Filled.CheckCircle else Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = if (guna.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                guna.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.TextPrimary
                            )
                            Text(
                                guna.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }

                    Surface(
                        color = if (guna.isPositive) AppTheme.SuccessColor.copy(alpha = 0.12f)
                        else AppTheme.WarningColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (guna.isPositive) AppTheme.SuccessColor else AppTheme.WarningColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppTheme.DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.LifeAreaLove)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Bride",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                        }
                        Text(
                            guna.brideValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.LifeAreaLove
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Groom",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.AccentTeal)
                            )
                        }
                        Text(
                            guna.groomValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.AccentTeal,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = AppTheme.ChipBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        guna.analysis,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextSecondary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DoshaSection(result: MatchmakingCalculator.MatchmakingResult) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = AppTheme.PlanetMars,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Manglik Dosha Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Mars placement compatibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = getManglikStatusColor(result.manglikCompatibility).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            when {
                                result.manglikCompatibility.contains("No concerns") -> Icons.Filled.CheckCircle
                                result.manglikCompatibility.contains("cancel") -> Icons.Filled.CheckCircle
                                result.manglikCompatibility.contains("Manageable") -> Icons.Outlined.Info
                                else -> Icons.Filled.Warning
                            },
                            contentDescription = null,
                            tint = getManglikStatusColor(result.manglikCompatibility),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            result.manglikCompatibility,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = getManglikStatusColor(result.manglikCompatibility),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        EnhancedManglikPersonCard(result.brideManglik, "Bride", AppTheme.LifeAreaLove)
        EnhancedManglikPersonCard(result.groomManglik, "Groom", AppTheme.AccentTeal)

        NadiDoshaCard(result)
        BhakootDoshaCard(result)
    }
}

@Composable
private fun EnhancedManglikPersonCard(
    analysis: MatchmakingCalculator.ManglikAnalysis,
    label: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (label == "Bride") Icons.Filled.Female else Icons.Filled.Male,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            analysis.person,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                        if (analysis.marsHouse > 0) {
                            Text(
                                "Mars in House ${analysis.marsHouse}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.TextMuted
                            )
                        }
                    }
                }

                Surface(
                    color = getManglikSeverityColor(analysis.effectiveDosha).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        analysis.effectiveDosha.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = getManglikSeverityColor(analysis.effectiveDosha),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (analysis.factors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = AppTheme.WarningColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Contributing Factors",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.WarningColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        analysis.factors.forEach { factor ->
                            Text(
                                "• $factor",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.WarningColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            if (analysis.cancellations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = AppTheme.SuccessColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Cancellation Factors",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.SuccessColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        analysis.cancellations.forEach { cancellation ->
                            Text(
                                "✓ $cancellation",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.SuccessColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NadiDoshaCard(result: MatchmakingCalculator.MatchmakingResult) {
    val nadiGuna = result.gunaAnalyses.find { it.name == "Nadi" }
    val hasNadiDosha = nadiGuna?.obtainedPoints == 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasNadiDosha) AppTheme.ErrorColor.copy(alpha = 0.06f) 
                           else AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Bloodtype,
                        contentDescription = null,
                        tint = if (hasNadiDosha) AppTheme.ErrorColor else AppTheme.SuccessColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Nadi Dosha",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Health & Progeny Compatibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
                Surface(
                    color = if (hasNadiDosha) AppTheme.ErrorColor.copy(alpha = 0.12f) 
                           else AppTheme.SuccessColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (hasNadiDosha) "Present" else "Absent",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasNadiDosha) AppTheme.ErrorColor else AppTheme.SuccessColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            nadiGuna?.let { guna ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bride", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                        Text(
                            guna.brideValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.LifeAreaLove
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Groom", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                        Text(
                            guna.groomValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.AccentTeal
                        )
                    }
                }

                if (hasNadiDosha) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AppTheme.InfoColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Same Nadi indicates potential health compatibility concerns. " +
                            "Exceptions may apply based on Nakshatra positions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.InfoColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BhakootDoshaCard(result: MatchmakingCalculator.MatchmakingResult) {
    val bhakootGuna = result.gunaAnalyses.find { it.name == "Bhakoot" }
    val hasBhakootDosha = bhakootGuna?.obtainedPoints == 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasBhakootDosha) AppTheme.WarningColor.copy(alpha = 0.06f) 
                           else AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CurrencyExchange,
                        contentDescription = null,
                        tint = if (hasBhakootDosha) AppTheme.WarningColor else AppTheme.SuccessColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Bhakoot Dosha",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Financial & Family Harmony",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
                Surface(
                    color = if (hasBhakootDosha) AppTheme.WarningColor.copy(alpha = 0.12f) 
                           else AppTheme.SuccessColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (hasBhakootDosha) "Present" else "Absent",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasBhakootDosha) AppTheme.WarningColor else AppTheme.SuccessColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            bhakootGuna?.let { guna ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bride's Rashi", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                        Text(
                            guna.brideValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.LifeAreaLove
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Groom's Rashi", style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                        Text(
                            guna.groomValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.AccentTeal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NakshatraSection(
    result: MatchmakingCalculator.MatchmakingResult,
    brideChart: VedicChart?,
    groomChart: VedicChart?
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = AppTheme.AccentSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Nakshatra Compatibility",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Birth star analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                if (brideChart != null && groomChart != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    NakshatraComparisonRow(
                        label = "Birth Nakshatra",
                        brideValue = getNakshatraName(brideChart),
                        groomValue = getNakshatraName(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = "Nakshatra Lord",
                        brideValue = getNakshatraLord(brideChart),
                        groomValue = getNakshatraLord(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = "Pada (Quarter)",
                        brideValue = getPada(brideChart),
                        groomValue = getPada(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = "Gana (Nature)",
                        brideValue = getGana(brideChart),
                        groomValue = getGana(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = "Yoni (Animal)",
                        brideValue = getYoni(brideChart),
                        groomValue = getYoni(groomChart)
                    )
                }
            }
        }

        RajjuAnalysisCard(result)
        VedhaAnalysisCard(result)
        StreeDeerghCard(result, brideChart, groomChart)
    }
}

@Composable
private fun NakshatraComparisonRow(
    label: String,
    brideValue: String,
    groomValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                brideValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.LifeAreaLove
            )
        }
        Surface(
            color = AppTheme.ChipBackground,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.TextMuted,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                groomValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.AccentTeal
            )
        }
    }
}

@Composable
private fun RajjuAnalysisCard(result: MatchmakingCalculator.MatchmakingResult) {
    val hasRajjuDosha = result.specialConsiderations.any { 
        it.contains("Rajju", ignoreCase = true) 
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasRajjuDosha) AppTheme.ErrorColor.copy(alpha = 0.06f) 
                           else AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Link,
                        contentDescription = null,
                        tint = if (hasRajjuDosha) AppTheme.ErrorColor else AppTheme.SuccessColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Rajju Matching",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Longevity & Well-being",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
                Surface(
                    color = if (hasRajjuDosha) AppTheme.ErrorColor.copy(alpha = 0.12f) 
                           else AppTheme.SuccessColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (hasRajjuDosha) "Conflict" else "Compatible",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasRajjuDosha) AppTheme.ErrorColor else AppTheme.SuccessColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Rajju indicates the cosmic bond that ensures longevity of spouse and " +
                "harmonious married life. No conflict is highly favorable.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun VedhaAnalysisCard(result: MatchmakingCalculator.MatchmakingResult) {
    val hasVedha = result.specialConsiderations.any { 
        it.contains("Vedha", ignoreCase = true) 
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasVedha) AppTheme.WarningColor.copy(alpha = 0.06f) 
                           else AppTheme.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Block,
                        contentDescription = null,
                        tint = if (hasVedha) AppTheme.WarningColor else AppTheme.SuccessColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Vedha Analysis",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Obstruction Check",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }
                Surface(
                    color = if (hasVedha) AppTheme.WarningColor.copy(alpha = 0.12f) 
                           else AppTheme.SuccessColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (hasVedha) "Present" else "None",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasVedha) AppTheme.WarningColor else AppTheme.SuccessColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Vedha represents Nakshatra pairs that create obstacles when combined. " +
                "Absence of Vedha is considered auspicious for marital harmony.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StreeDeerghCard(
    result: MatchmakingCalculator.MatchmakingResult,
    brideChart: VedicChart?,
    groomChart: VedicChart?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.WbTwilight,
                    contentDescription = null,
                    tint = AppTheme.AccentPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Stree Deergha & Mahendra",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "Prosperity & Longevity factors",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = AppTheme.SuccessColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Stree Deergha",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Text(
                        "Favorable",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.SuccessColor
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(AppTheme.DividerColor)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = AppTheme.SuccessColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Mahendra",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Text(
                        "Beneficial",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.SuccessColor
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedRemediesSection(result: MatchmakingCalculator.MatchmakingResult) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Spa,
                        contentDescription = null,
                        tint = AppTheme.LifeAreaHealth,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Suggested Remedies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            "Traditional Vedic recommendations",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                result.remedies.forEachIndexed { index, remedy ->
                    RemedyItem(index + 1, remedy)
                    if (index != result.remedies.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = AppTheme.InfoColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = AppTheme.InfoColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "These remedies are based on traditional Vedic astrology principles. " +
                    "For personalized guidance, consult a qualified astrologer. Compatibility " +
                    "is one of many factors in a successful relationship.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.InfoColor,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun RemedyItem(number: Int, remedy: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AppTheme.AccentPrimary.copy(alpha = 0.2f),
                            AppTheme.AccentSecondary.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$number",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.AccentPrimary
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            remedy,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.TextSecondary,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyMatchingState(
    hasBride: Boolean,
    hasGroom: Boolean,
    hasCharts: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AppTheme.ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasCharts) Icons.Outlined.FavoriteBorder else Icons.Outlined.PersonAddAlt,
                    contentDescription = null,
                    tint = AppTheme.TextSubtle,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = when {
                    !hasCharts -> "No Charts Available"
                    !hasBride && !hasGroom -> "Select Both Profiles"
                    !hasBride -> "Select Bride's Profile"
                    !hasGroom -> "Select Groom's Profile"
                    else -> "Preparing Analysis..."
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    !hasCharts -> "Create birth charts to use matchmaking"
                    !hasBride && !hasGroom -> "Tap on the profile cards above to select"
                    !hasBride -> "Tap on the bride card to continue"
                    !hasGroom -> "Tap on the groom card to continue"
                    else -> "Calculating compatibility..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedProfileSelectorBottomSheet(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    charts: List<ChartEntity>,
    selectedId: Long?,
    excludeId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val availableCharts = charts.filter { it.id != excludeId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppTheme.TextSubtle.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        "${availableCharts.size} charts available",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.TextMuted
                    )
                }
            }

            if (availableCharts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.SearchOff,
                            contentDescription = null,
                            tint = AppTheme.TextSubtle,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No charts available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCharts, key = { it.id }) { chart ->
                        val isSelected = chart.id == selectedId

                        Surface(
                            onClick = { onSelect(chart.id) },
                            color = if (isSelected) accentColor.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                1.5.dp, accentColor
                            ) else androidx.compose.foundation.BorderStroke(
                                1.dp, AppTheme.BorderColor.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) accentColor.copy(alpha = 0.15f) 
                                            else AppTheme.ChipBackground
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        chart.name.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) accentColor else AppTheme.TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        chart.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.TextPrimary
                                    )
                                    Text(
                                        chart.location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppTheme.TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareOptionsSheet(
    result: MatchmakingCalculator.MatchmakingResult?,
    brideChart: VedicChart?,
    groomChart: VedicChart?,
    onDismiss: () -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppTheme.TextSubtle.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Share Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))

            ShareOptionItem(
                icon = Icons.Outlined.ContentCopy,
                title = "Copy Full Report",
                subtitle = "Copy detailed analysis to clipboard",
                onClick = {
                    result?.let {
                        onCopyToClipboard(generateTextReport(it, brideChart, groomChart))
                    }
                }
            )

            ShareOptionItem(
                icon = Icons.Outlined.Summarize,
                title = "Copy Summary",
                subtitle = "Copy brief compatibility summary",
                onClick = {
                    result?.let {
                        onCopyToClipboard(generateSummaryReport(it, brideChart, groomChart))
                    }
                }
            )

            ShareOptionItem(
                icon = Icons.Outlined.Numbers,
                title = "Copy Scores Only",
                subtitle = "Copy Guna scores breakdown",
                onClick = {
                    result?.let {
                        onCopyToClipboard(generateScoresReport(it))
                    }
                }
            )
        }
    }
}

@Composable
private fun ShareOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.AccentPrimary.copy(alpha = 0.1f)),
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
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.TextPrimary
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.TextMuted
                )
            }
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.TextSubtle,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun generateTextReport(
    result: MatchmakingCalculator.MatchmakingResult,
    brideChart: VedicChart?,
    groomChart: VedicChart?
): String {
    return buildString {
        appendLine("═══════════════════════════════════════")
        appendLine("       KUNDLI MILAN REPORT")
        appendLine("         AstroStorm Analysis")
        appendLine("═══════════════════════════════════════")
        appendLine()
        
        appendLine("PROFILES")
        appendLine("─────────────────────────────────────")
        brideChart?.let {
            appendLine("Bride: ${it.birthData.name ?: "N/A"}")
            appendLine("  Moon Sign: ${getRashiName(it)}")
            appendLine("  Nakshatra: ${getNakshatraName(it)} (${getPada(it)})")
        }
        groomChart?.let {
            appendLine("Groom: ${it.birthData.name ?: "N/A"}")
            appendLine("  Moon Sign: ${getRashiName(it)}")
            appendLine("  Nakshatra: ${getNakshatraName(it)} (${getPada(it)})")
        }
        appendLine()
        
        appendLine("COMPATIBILITY SCORE")
        appendLine("─────────────────────────────────────")
        appendLine("Total Points: ${String.format("%.1f", result.totalPoints)} / ${result.maxPoints.toInt()}")
        appendLine("Percentage: ${String.format("%.1f", result.percentage)}%")
        appendLine("Rating: ${result.rating.displayName}")
        appendLine()
        
        appendLine("ASHTAKOOTA (8 GUNA) ANALYSIS")
        appendLine("─────────────────────────────────────")
        result.gunaAnalyses.forEach { guna ->
            val status = if (guna.isPositive) "✓" else "✗"
            appendLine("$status ${guna.name} (${guna.description})")
            appendLine("  Score: ${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}")
            appendLine("  Bride: ${guna.brideValue} | Groom: ${guna.groomValue}")
            appendLine("  ${guna.analysis}")
            appendLine()
        }
        
        appendLine("MANGLIK DOSHA ANALYSIS")
        appendLine("─────────────────────────────────────")
        appendLine("Compatibility: ${result.manglikCompatibility}")
        appendLine()
        appendLine("Bride: ${result.brideManglik.effectiveDosha.displayName}")
        if (result.brideManglik.marsHouse > 0) {
            appendLine("  Mars in House ${result.brideManglik.marsHouse}")
        }
        result.brideManglik.factors.forEach { appendLine("  • $it") }
        result.brideManglik.cancellations.forEach { appendLine("  ✓ $it (Cancellation)") }
        appendLine()
        appendLine("Groom: ${result.groomManglik.effectiveDosha.displayName}")
        if (result.groomManglik.marsHouse > 0) {
            appendLine("  Mars in House ${result.groomManglik.marsHouse}")
        }
        result.groomManglik.factors.forEach { appendLine("  • $it") }
        result.groomManglik.cancellations.forEach { appendLine("  ✓ $it (Cancellation)") }
        appendLine()
        
        if (result.specialConsiderations.isNotEmpty()) {
            appendLine("SPECIAL CONSIDERATIONS")
            appendLine("─────────────────────────────────────")
            result.specialConsiderations.forEach { appendLine("• $it") }
            appendLine()
        }
        
        appendLine("SUGGESTED REMEDIES")
        appendLine("─────────────────────────────────────")
        result.remedies.forEachIndexed { index, remedy ->
            appendLine("${index + 1}. $remedy")
        }
        appendLine()
        
        appendLine("═══════════════════════════════════════")
        appendLine("Generated by AstroStorm")
        appendLine("Vedic Astrology • Ultra-Precision")
        appendLine("═══════════════════════════════════════")
    }
}

private fun generateSummaryReport(
    result: MatchmakingCalculator.MatchmakingResult,
    brideChart: VedicChart?,
    groomChart: VedicChart?
): String {
    return buildString {
        appendLine("🔮 KUNDLI MILAN SUMMARY")
        appendLine()
        brideChart?.let { appendLine("👰 Bride: ${it.birthData.name ?: "N/A"}") }
        groomChart?.let { appendLine("🤵 Groom: ${it.birthData.name ?: "N/A"}") }
        appendLine()
        appendLine("⭐ Score: ${String.format("%.1f", result.totalPoints)}/${result.maxPoints.toInt()} (${String.format("%.1f", result.percentage)}%)")
        appendLine("📊 Rating: ${result.rating.displayName}")
        appendLine()
        appendLine("${result.rating.description}")
        appendLine()
        appendLine("🔴 Manglik: ${result.manglikCompatibility}")
        appendLine()
        appendLine("— Generated by AstroStorm")
    }
}

private fun generateScoresReport(result: MatchmakingCalculator.MatchmakingResult): String {
    return buildString {
        appendLine("ASHTAKOOTA GUNA SCORES")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        result.gunaAnalyses.forEach { guna ->
            val emoji = if (guna.isPositive) "✅" else "⚠️"
            appendLine("$emoji ${guna.name}: ${guna.obtainedPoints.toInt()}/${guna.maxPoints.toInt()}")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("TOTAL: ${String.format("%.1f", result.totalPoints)}/${result.maxPoints.toInt()}")
        appendLine()
        appendLine("— AstroStorm")
    }
}

private fun getRatingColor(rating: MatchmakingCalculator.CompatibilityRating): Color {
    return when (rating) {
        MatchmakingCalculator.CompatibilityRating.EXCELLENT -> Color(0xFF2E7D32)
        MatchmakingCalculator.CompatibilityRating.GOOD -> Color(0xFF558B2F)
        MatchmakingCalculator.CompatibilityRating.AVERAGE -> Color(0xFFF9A825)
        MatchmakingCalculator.CompatibilityRating.BELOW_AVERAGE -> Color(0xFFEF6C00)
        MatchmakingCalculator.CompatibilityRating.POOR -> Color(0xFFC62828)
    }
}

private fun getRatingIcon(rating: MatchmakingCalculator.CompatibilityRating): ImageVector {
    return when (rating) {
        MatchmakingCalculator.CompatibilityRating.EXCELLENT -> Icons.Filled.Stars
        MatchmakingCalculator.CompatibilityRating.GOOD -> Icons.Filled.ThumbUp
        MatchmakingCalculator.CompatibilityRating.AVERAGE -> Icons.Outlined.Balance
        MatchmakingCalculator.CompatibilityRating.BELOW_AVERAGE -> Icons.Outlined.TrendingDown
        MatchmakingCalculator.CompatibilityRating.POOR -> Icons.Filled.Cancel
    }
}

private fun getManglikStatusColor(status: String): Color {
    return when {
        status.contains("No concerns", ignoreCase = true) -> AppTheme.SuccessColor
        status.contains("cancel", ignoreCase = true) -> AppTheme.SuccessColor
        status.contains("Both", ignoreCase = true) -> AppTheme.SuccessColor
        status.contains("Manageable", ignoreCase = true) -> AppTheme.WarningColor
        status.contains("Partial", ignoreCase = true) -> AppTheme.WarningColor
        else -> AppTheme.ErrorColor
    }
}

private fun getManglikSeverityColor(dosha: MatchmakingCalculator.ManglikDosha): Color {
    return when (dosha) {
        MatchmakingCalculator.ManglikDosha.NONE -> AppTheme.SuccessColor
        MatchmakingCalculator.ManglikDosha.PARTIAL -> AppTheme.WarningColor
        MatchmakingCalculator.ManglikDosha.FULL -> AppTheme.ErrorColor
        MatchmakingCalculator.ManglikDosha.DOUBLE -> Color(0xFF8B0000)
    }
}

private fun getManglikQuickStatus(result: MatchmakingCalculator.MatchmakingResult): String {
    val brideStatus = result.brideManglik.effectiveDosha
    val groomStatus = result.groomManglik.effectiveDosha
    
    return when {
        brideStatus == MatchmakingCalculator.ManglikDosha.NONE && 
            groomStatus == MatchmakingCalculator.ManglikDosha.NONE -> "No Dosha"
        brideStatus != MatchmakingCalculator.ManglikDosha.NONE && 
            groomStatus != MatchmakingCalculator.ManglikDosha.NONE -> "Both Manglik"
        brideStatus != MatchmakingCalculator.ManglikDosha.NONE -> "Bride Only"
        groomStatus != MatchmakingCalculator.ManglikDosha.NONE -> "Groom Only"
        else -> "Check Details"
    }
}

private fun getNakshatraName(chart: VedicChart): String {
    val moonPosition = chart.planetPositions.find {
        it.planet.name.equals("Moon", ignoreCase = true)
    } ?: return "Unknown"
    
    val moonLongitude = moonPosition.longitude
    val nakshatraIndex = ((moonLongitude / 13.333333333) % 27.0).toInt()
    return nakshatraNames.getOrElse(nakshatraIndex) { "Unknown" }
}

private fun getRashiName(chart: VedicChart): String {
    val moonPosition = chart.planetPositions.find {
        it.planet.name.equals("Moon", ignoreCase = true)
    } ?: return "Unknown"
    
    val moonLongitude = moonPosition.longitude
    val rashiIndex = (moonLongitude / 30.0).toInt() % 12
    return rashiNames.getOrElse(rashiIndex) { "Unknown" }
}

private fun getPada(chart: VedicChart): String {
    val moonPosition = chart.planetPositions.find {
        it.planet.name.equals("Moon", ignoreCase = true)
    } ?: return "Unknown"
    
    val moonLongitude = moonPosition.longitude
    val nakshatraDegree = moonLongitude % 13.333333333
    val pada = (nakshatraDegree / 3.333333333).toInt() + 1
    return "Pada $pada"
}

private fun getNakshatraLord(chart: VedicChart): String {
    val moonPosition = chart.planetPositions.find {
        it.planet.name.equals("Moon", ignoreCase = true)
    } ?: return "Unknown"
    
    val moonLongitude = moonPosition.longitude
    val nakshatraIndex = ((moonLongitude / 13.333333333) % 27.0).toInt()
    return nakshatraLords.getOrElse(nakshatraIndex) { "Unknown" }
}

private fun getGana(chart: VedicChart): String {
    val moonPosition = chart.planetPositions.find {
        it.planet.name.equals("Moon", ignoreCase = true)
    } ?: return "Unknown"
    
    val moonLongitude = moonPosition.longitude
    val nakshatraIndex = ((moonLongitude / 13.333333333) % 27.0).toInt()
    return nakshatraGana.getOrElse(nakshatraIndex) { "Unknown" }
}

private fun getYoni(chart: VedicChart): String {
    val moonPosition = chart.planetPositions.find {
        it.planet.name.equals("Moon", ignoreCase = true)
    } ?: return "Unknown"
    
    val moonLongitude = moonPosition.longitude
    val nakshatraIndex = ((moonLongitude / 13.333333333) % 27.0).toInt()
    return nakshatraYoni.getOrElse(nakshatraIndex) { "Unknown" }
}

private val nakshatraNames = listOf(
    "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
    "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
    "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
    "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
    "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
)

private val rashiNames = listOf(
    "Mesha (Aries)", "Vrishabha (Taurus)", "Mithuna (Gemini)", "Karka (Cancer)",
    "Simha (Leo)", "Kanya (Virgo)", "Tula (Libra)", "Vrishchika (Scorpio)",
    "Dhanu (Sagittarius)", "Makara (Capricorn)", "Kumbha (Aquarius)", "Meena (Pisces)"
)

private val nakshatraLords = listOf(
    "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu",
    "Jupiter", "Saturn", "Mercury", "Ketu", "Venus", "Sun",
    "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury",
    "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu",
    "Jupiter", "Saturn", "Mercury"
)

private val nakshatraGana = listOf(
    "Deva", "Manushya", "Rakshasa", "Manushya", "Deva", "Manushya",
    "Deva", "Deva", "Rakshasa", "Rakshasa", "Manushya", "Manushya",
    "Deva", "Rakshasa", "Deva", "Rakshasa", "Deva", "Rakshasa",
    "Rakshasa", "Manushya", "Manushya", "Deva", "Rakshasa", "Rakshasa",
    "Manushya", "Manushya", "Deva"
)

private val nakshatraYoni = listOf(
    "Horse (Male)", "Elephant (Male)", "Sheep (Female)", "Serpent (Male)", 
    "Serpent (Female)", "Dog (Female)", "Cat (Female)", "Sheep (Male)", 
    "Cat (Male)", "Rat (Male)", "Rat (Female)", "Cow (Male)",
    "Buffalo (Female)", "Tiger (Female)", "Buffalo (Male)", "Tiger (Male)", 
    "Deer (Female)", "Deer (Male)", "Dog (Male)", "Monkey (Male)", 
    "Mongoose (Male)", "Monkey (Female)", "Lion (Female)", "Horse (Female)",
    "Lion (Male)", "Cow (Female)", "Elephant (Female)"
)
