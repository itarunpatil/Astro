package com.astro.storm.ui.screen.matchmaking

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
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.data.localization.Language
import com.astro.storm.data.localization.LocalLanguage
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.StringKeyMatch
import com.astro.storm.data.localization.StringResources
import com.astro.storm.data.localization.currentLanguage
import com.astro.storm.data.localization.stringResource
import com.astro.storm.data.model.*
import com.astro.storm.ephemeris.MatchmakingCalculator
import com.astro.storm.ephemeris.VedicAstrologyUtils
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
    savedCharts: List<SavedChart>,
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

    var matchingResult by remember { mutableStateOf<MatchmakingResult?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showBrideSelector by remember { mutableStateOf(false) }
    var showGroomSelector by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(StringKeyMatch.MATCH_OVERVIEW),
        stringResource(StringKeyMatch.MATCH_GUNAS),
        stringResource(StringKeyMatch.MATCH_DOSHAS),
        stringResource(StringKeyMatch.MATCH_NAKSHATRAS),
        stringResource(StringKeyMatch.MATCH_REMEDIES)
    )

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

    // Pre-fetch localized strings and language for use in LaunchedEffect and scope.launch (stringResource is @Composable)
    val errorCalculationFailedText = stringResource(StringKey.ERROR_CALCULATION_FAILED)
    val copiedToClipboardText = stringResource(StringKeyMatch.MATCH_COPIED_TO_CLIPBOARD)
    val language = currentLanguage()

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
                errorMessage = e.message ?: errorCalculationFailedText
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
                            stringResource(StringKeyMatch.MATCH_TITLE),
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary,
                            fontSize = 18.sp
                        )
                        AnimatedVisibility(visible = matchingResult != null) {
                            Text(
                                stringResource(StringKeyMatch.MATCH_ASHTAKOOTA),
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
                            contentDescription = stringResource(StringKey.BTN_BACK),
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
                                    contentDescription = stringResource(StringKeyMatch.MATCH_SHARE_REPORT),
                                    tint = AppTheme.TextSecondary
                                )
                            }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                matchingResult?.let { result ->
                                    val report = MatchmakingReportUtils.generateFullReport(result, brideChart, groomChart, language)
                                    clipboardManager.setText(AnnotatedString(report))
                                    scope.launch {
                                        snackbarHostState.showSnackbar(copiedToClipboardText)
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(StringKeyMatch.MATCH_COPY_REPORT),
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
            title = stringResource(StringKeyMatch.MATCH_SELECT_BRIDE),
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
            title = stringResource(StringKeyMatch.MATCH_SELECT_GROOM),
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
                    snackbarHostState.showSnackbar(copiedToClipboardText)
                }
                showShareSheet = false
            }
        )
    }
}

@Composable
private fun EnhancedProfileSelectionSection(
    savedCharts: List<SavedChart>,
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
                    stringResource(StringKeyMatch.MATCH_SELECT_PROFILES),
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
                                    contentDescription = stringResource(StringKeyMatch.MATCH_SWAP_PROFILES),
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
                                contentDescription = stringResource(StringKeyMatch.MATCH_CLEAR_SELECTION),
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
                    label = stringResource(StringKeyMatch.MATCH_BRIDE),
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
                    label = stringResource(StringKeyMatch.MATCH_GROOM),
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
                            stringResource(StringKeyMatch.MATCH_CREATE_CHARTS_FIRST),
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
                chart?.birthData?.name ?: stringResource(StringKeyMatch.MATCH_TAP_TO_SELECT),
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
private fun EnhancedCompatibilityScoreCard(
    result: MatchmakingResult,
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
                            text = stringResource(StringKeyMatch.MATCH_OUT_OF, result.maxPoints.toInt()),
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
                        append(" ${stringResource(StringKeyMatch.MATCH_COMPATIBILITY)}")
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
private fun QuickInsightsRow(result: MatchmakingResult) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val hasNadiDosha = result.gunaAnalyses.find { it.gunaType == GunaType.NADI }?.hasDosha == true
        val hasBhakootDosha = result.gunaAnalyses.find { it.gunaType == GunaType.BHAKOOT }?.hasDosha == true

        item {
            QuickInsightChip(
                label = stringResource(StringKeyMatch.MATCH_MANGLIK),
                value = result.getManglikQuickStatus(),
                color = getManglikStatusColor(result.manglikCompatibility)
            )
        }

        if (hasNadiDosha) {
            item {
                QuickInsightChip(
                    label = stringResource(StringKeyMatch.MATCH_NADI),
                    value = stringResource(StringKeyMatch.MATCH_DOSHA_PRESENT),
                    color = AppTheme.ErrorColor
                )
            }
        }

        if (hasBhakootDosha) {
            item {
                QuickInsightChip(
                    label = stringResource(StringKeyMatch.MATCH_BHAKOOT),
                    value = stringResource(StringKeyMatch.MATCH_NEEDS_ATTENTION),
                    color = AppTheme.WarningColor
                )
            }
        }

        item {
            QuickInsightChip(
                label = stringResource(StringKeyMatch.MATCH_GUNAS),
                value = "${result.totalPoints.toInt()}/${result.maxPoints.toInt()}",
                color = if (result.totalPoints >= 18) AppTheme.SuccessColor else AppTheme.WarningColor
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
    result: MatchmakingResult,
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
                        stringResource(StringKeyMatch.MATCH_GUNA_DISTRIBUTION),
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
private fun EnhancedGunaScoreBar(guna: GunaAnalysis) {
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
                    stringResource(StringKeyMatch.MATCH_PROFILE_COMPARISON),
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
                        brideChart.birthData.name ?: stringResource(StringKeyMatch.MATCH_BRIDE),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.LifeAreaLove
                    )
                }
                Text(stringResource(StringKeyMatch.MATCH_VS), color = AppTheme.TextMuted)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        groomChart.birthData.name ?: stringResource(StringKeyMatch.MATCH_GROOM),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.AccentTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = AppTheme.DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            ComparisonRow(stringResource(StringKeyMatch.MATCH_MOON_SIGN), getRashiName(brideChart), getRashiName(groomChart))
            ComparisonRow(stringResource(StringKeyMatch.MATCH_NAKSHATRA), getNakshatraName(brideChart), getNakshatraName(groomChart))
            ComparisonRow(stringResource(StringKeyMatch.MATCH_PADA), getPada(brideChart), getPada(groomChart))
        }
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
                    stringResource(StringKeyMatch.MATCH_KEY_CONSIDERATIONS),
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
private fun GunaSummaryHeader(result: MatchmakingResult) {
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
                Text(stringResource(StringKeyMatch.MATCH_FAVORABLE), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
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
                Text(stringResource(StringKeyMatch.MATCH_NEEDS_ATTENTION), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
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
                Text(stringResource(StringKeyMatch.MATCH_TOTAL_SCORE), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
            }
        }
    }
}

@Composable
private fun AnimatedGunaCard(
    guna: GunaAnalysis,
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
                                stringResource(StringKeyMatch.MATCH_BRIDE),
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
                                stringResource(StringKeyMatch.MATCH_GROOM),
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
private fun DoshaSection(result: MatchmakingResult) {
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
                            stringResource(StringKeyMatch.MATCH_MANGLIK_DOSHA_ANALYSIS),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_MARS_PLACEMENT),
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

        EnhancedManglikPersonCard(result.brideManglik, stringResource(StringKeyMatch.MATCH_BRIDE), AppTheme.LifeAreaLove)
        EnhancedManglikPersonCard(result.groomManglik, stringResource(StringKeyMatch.MATCH_GROOM), AppTheme.AccentTeal)

        NadiDoshaCard(result)
        BhakootDoshaCard(result)
    }
}

@Composable
private fun EnhancedManglikPersonCard(
    analysis: ManglikAnalysis,
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
                            if (label == stringResource(StringKeyMatch.MATCH_BRIDE)) Icons.Filled.Female else Icons.Filled.Male,
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
                                stringResource(StringKeyMatch.MATCH_MARS_IN_HOUSE, analysis.marsHouse),
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
                            stringResource(StringKeyMatch.MATCH_CONTRIBUTING_FACTORS),
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
                            stringResource(StringKeyMatch.MATCH_CANCELLATION_FACTORS),
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
private fun NadiDoshaCard(result: MatchmakingResult) {
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
                            stringResource(StringKeyMatch.MATCH_NADI_DOSHA),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_HEALTH_PROGENY),
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
                        if (hasNadiDosha) stringResource(StringKeyMatch.MATCH_PRESENT) else stringResource(StringKeyMatch.MATCH_ABSENT),
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
                        Text(stringResource(StringKeyMatch.MATCH_BRIDE), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                        Text(
                            guna.brideValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.LifeAreaLove
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(StringKeyMatch.MATCH_GROOM), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
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
                            stringResource(StringKeyMatch.MATCH_NADI_WARNING),
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
private fun BhakootDoshaCard(result: MatchmakingResult) {
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
                            stringResource(StringKeyMatch.MATCH_BHAKOOT_DOSHA),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_FINANCIAL_HARMONY),
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
                        if (hasBhakootDosha) stringResource(StringKeyMatch.MATCH_PRESENT) else stringResource(StringKeyMatch.MATCH_ABSENT),
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
                        Text(stringResource(StringKeyMatch.MATCH_BRIDE_RASHI), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
                        Text(
                            guna.brideValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.LifeAreaLove
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(StringKeyMatch.MATCH_GROOM_RASHI), style = MaterialTheme.typography.labelSmall, color = AppTheme.TextMuted)
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
    result: MatchmakingResult,
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
                            stringResource(StringKeyMatch.MATCH_NAKSHATRA_COMPATIBILITY),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_BIRTH_STAR),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.TextMuted
                        )
                    }
                }

                if (brideChart != null && groomChart != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    NakshatraComparisonRow(
                        label = stringResource(StringKeyMatch.MATCH_BIRTH_NAKSHATRA),
                        brideValue = getNakshatraName(brideChart),
                        groomValue = getNakshatraName(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = stringResource(StringKeyMatch.MATCH_NAKSHATRA_LORD),
                        brideValue = getNakshatraLord(brideChart),
                        groomValue = getNakshatraLord(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = stringResource(StringKeyMatch.MATCH_PADA),
                        brideValue = getPada(brideChart),
                        groomValue = getPada(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = stringResource(StringKeyMatch.MATCH_GANA),
                        brideValue = getGana(brideChart),
                        groomValue = getGana(groomChart)
                    )

                    NakshatraComparisonRow(
                        label = stringResource(StringKeyMatch.MATCH_YONI),
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
private fun RajjuAnalysisCard(result: MatchmakingResult) {
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
                            stringResource(StringKeyMatch.MATCH_RAJJU_MATCHING),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_LONGEVITY),
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
                        if (hasRajjuDosha) stringResource(StringKeyMatch.MATCH_CONFLICT) else stringResource(StringKeyMatch.MATCH_COMPATIBLE),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasRajjuDosha) AppTheme.ErrorColor else AppTheme.SuccessColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(StringKeyMatch.MATCH_RAJJU_DESCRIPTION),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun VedhaAnalysisCard(result: MatchmakingResult) {
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
                            stringResource(StringKeyMatch.MATCH_VEDHA_ANALYSIS),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_OBSTRUCTION_CHECK),
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
                        if (hasVedha) stringResource(StringKeyMatch.MATCH_PRESENT) else stringResource(StringKeyMatch.MATCH_NONE),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasVedha) AppTheme.WarningColor else AppTheme.SuccessColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(StringKeyMatch.MATCH_VEDHA_DESCRIPTION),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StreeDeerghCard(
    result: MatchmakingResult,
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
                        stringResource(StringKeyMatch.MATCH_STREE_DEERGHA),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.TextPrimary
                    )
                    Text(
                        stringResource(StringKeyMatch.MATCH_PROSPERITY_FACTORS),
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
                        stringResource(StringKeyMatch.MATCH_STREE_DEERGHA_LABEL),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Text(
                        stringResource(StringKeyMatch.MATCH_FAVORABLE),
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
                        stringResource(StringKeyMatch.MATCH_MAHENDRA),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.TextMuted
                    )
                    Text(
                        stringResource(StringKeyMatch.MATCH_BENEFICIAL),
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
private fun EnhancedRemediesSection(result: MatchmakingResult) {
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
                            stringResource(StringKeyMatch.MATCH_SUGGESTED_REMEDIES),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.TextPrimary
                        )
                        Text(
                            stringResource(StringKeyMatch.MATCH_VEDIC_RECOMMENDATIONS),
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
                    stringResource(StringKeyMatch.MATCH_REMEDIES_DISCLAIMER),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.InfoColor,
                    lineHeight = 18.sp
                )
            }
        }
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
                text = stringResource(
                    when {
                        !hasCharts -> StringKeyMatch.MATCH_NO_CHARTS
                        !hasBride && !hasGroom -> StringKeyMatch.MATCH_SELECT_BOTH
                        !hasBride -> StringKeyMatch.MATCH_SELECT_BRIDE_PROFILE
                        !hasGroom -> StringKeyMatch.MATCH_SELECT_GROOM_PROFILE
                        else -> StringKeyMatch.MATCH_PREPARING_ANALYSIS
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    when {
                        !hasCharts -> StringKeyMatch.MATCH_CREATE_CHARTS
                        !hasBride && !hasGroom -> StringKeyMatch.MATCH_SELECT_TAP_CARDS
                        !hasBride -> StringKeyMatch.MATCH_TAP_BRIDE_CARD
                        !hasGroom -> StringKeyMatch.MATCH_TAP_GROOM_CARD
                        else -> StringKeyMatch.MATCH_CALCULATING
                    }
                ),
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
    charts: List<SavedChart>,
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
                        stringResource(StringKeyMatch.MATCH_CHARTS_AVAILABLE, availableCharts.size),
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
                            stringResource(StringKeyMatch.MATCH_NO_CHARTS_AVAILABLE),
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
                                        contentDescription = stringResource(StringKeyMatch.MATCH_SELECTED),
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
    result: MatchmakingResult?,
    brideChart: VedicChart?,
    groomChart: VedicChart?,
    onDismiss: () -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val language = currentLanguage()

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
                stringResource(StringKeyMatch.MATCH_SHARE_REPORT),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))

            ShareOptionItem(
                icon = Icons.Outlined.ContentCopy,
                title = stringResource(StringKeyMatch.MATCH_COPY_FULL_REPORT),
                subtitle = stringResource(StringKeyMatch.MATCH_COPY_FULL_DESC),
                onClick = {
                    result?.let {
                        onCopyToClipboard(MatchmakingReportUtils.generateFullReport(it, brideChart, groomChart, language))
                    }
                }
            )

            ShareOptionItem(
                icon = Icons.Outlined.Summarize,
                title = stringResource(StringKeyMatch.MATCH_COPY_SUMMARY),
                subtitle = stringResource(StringKeyMatch.MATCH_COPY_SUMMARY_DESC),
                onClick = {
                    result?.let {
                        onCopyToClipboard(MatchmakingReportUtils.generateSummaryReport(it, brideChart, groomChart, language))
                    }
                }
            )

            ShareOptionItem(
                icon = Icons.Outlined.Numbers,
                title = stringResource(StringKeyMatch.MATCH_COPY_SCORES),
                subtitle = stringResource(StringKeyMatch.MATCH_COPY_SCORES_DESC),
                onClick = {
                    result?.let {
                        onCopyToClipboard(MatchmakingReportUtils.generateScoresReport(it, language))
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

// Report generation functions moved to MatchmakingReportUtils.kt

private fun getMoonPosition(chart: VedicChart) = chart.planetPositions.find {
    it.planet == Planet.MOON
}

@Composable
private fun getNakshatraName(chart: VedicChart): String {
    return getMoonPosition(chart)?.nakshatra?.displayName ?: stringResource(StringKeyMatch.MISC_UNKNOWN)
}

@Composable
private fun getRashiName(chart: VedicChart): String {
    val unknownText = stringResource(StringKeyMatch.MISC_UNKNOWN)
    val moonPosition = getMoonPosition(chart) ?: return unknownText
    return moonPosition.sign.displayName
}

@Composable
private fun getPada(chart: VedicChart): String {
    val unknownText = stringResource(StringKeyMatch.MISC_UNKNOWN)
    val moonPosition = getMoonPosition(chart) ?: return unknownText
    return "Pada ${moonPosition.nakshatraPada}"
}

@Composable
private fun getNakshatraLord(chart: VedicChart): String {
    val unknownText = stringResource(StringKeyMatch.MISC_UNKNOWN)
    val moonPosition = getMoonPosition(chart) ?: return unknownText
    return moonPosition.nakshatra.ruler.displayName
}

@Composable
private fun getGana(chart: VedicChart): String {
    val unknownText = stringResource(StringKeyMatch.MISC_UNKNOWN)
    val moonPosition = getMoonPosition(chart) ?: return unknownText
    // Use centralized VedicAstrologyUtils for consistent Gana lookup
    return VedicAstrologyUtils.getGanaDisplayName(moonPosition.nakshatra)
}

@Composable
private fun getYoni(chart: VedicChart): String {
    val unknownText = stringResource(StringKeyMatch.MISC_UNKNOWN)
    val moonPosition = getMoonPosition(chart) ?: return unknownText
    // Use centralized VedicAstrologyUtils for consistent Yoni lookup
    return VedicAstrologyUtils.getYoniDisplayName(moonPosition.nakshatra)
}
