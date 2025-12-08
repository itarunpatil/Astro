package com.astro.storm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.api.GeocodingService
import com.astro.storm.data.localization.StringKey
import com.astro.storm.data.localization.stringResource
import com.astro.storm.ui.theme.AppTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.util.Locale

/**
 * Error type enum for tracking error state without calling @Composable in LaunchedEffect
 */
private enum class SearchErrorType {
    NONE,
    NETWORK,
    RATE_LIMIT,
    GENERIC
}

@OptIn(FlowPreview::class)
@Composable
fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (location: String, latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(StringKey.LOCATION_SEARCH),
    placeholder: String = stringResource(StringKey.LOCATION_PLACEHOLDER)
) {
    val focusManager = LocalFocusManager.current

    var searchResults by remember { mutableStateOf<List<GeocodingService.GeocodingResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var errorType by remember { mutableStateOf(SearchErrorType.NONE) }
    var networkErrorMessage by remember { mutableStateOf<String?>(null) }
    var hasFocus by remember { mutableStateOf(false) }

    val searchQueryFlow = remember { MutableStateFlow("") }

    // Pre-fetch localized error strings for display
    val rateLimitText = stringResource(StringKey.ERROR_RATE_LIMIT)
    val searchFailedText = stringResource(StringKey.ERROR_SEARCH_FAILED)
    val clearSearchText = stringResource(StringKey.LOCATION_CLEAR)

    // Compute the display error message from error type
    val errorMessage = when (errorType) {
        SearchErrorType.NONE -> null
        SearchErrorType.NETWORK -> networkErrorMessage
        SearchErrorType.RATE_LIMIT -> rateLimitText
        SearchErrorType.GENERIC -> searchFailedText
    }

    LaunchedEffect(value) {
        searchQueryFlow.emit(value)
    }

    LaunchedEffect(Unit) {
        searchQueryFlow
            .debounce(400)
            .distinctUntilChanged()
            .filter { it.length >= 3 }
            .collectLatest { query ->
                isSearching = true
                errorType = SearchErrorType.NONE
                networkErrorMessage = null

                val result = GeocodingService.searchLocation(query, limit = 6)
                result.onSuccess { results ->
                    searchResults = results
                    showResults = results.isNotEmpty() && hasFocus
                }.onFailure { error ->
                    when (error) {
                        is GeocodingService.GeocodingError.NetworkError -> {
                            errorType = SearchErrorType.NETWORK
                            networkErrorMessage = error.message
                        }
                        is GeocodingService.GeocodingError.RateLimitExceeded -> {
                            errorType = SearchErrorType.RATE_LIMIT
                        }
                        else -> {
                            errorType = SearchErrorType.GENERIC
                        }
                    }
                    searchResults = emptyList()
                    showResults = false
                }

                isSearching = false
            }
    }

    LaunchedEffect(value) {
        if (value.length < 3) {
            searchResults = emptyList()
            showResults = false
            errorType = SearchErrorType.NONE
            networkErrorMessage = null
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = AppTheme.TextMuted, fontSize = 14.sp) },
            placeholder = { Text(placeholder, color = AppTheme.TextSubtle, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = AppTheme.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    AnimatedVisibility(visible = isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            color = AppTheme.AccentGold,
                            strokeWidth = 2.dp
                        )
                    }
                    AnimatedVisibility(visible = value.isNotEmpty() && !isSearching) {
                        IconButton(
                            onClick = {
                                onValueChange("")
                                searchResults = emptyList()
                                showResults = false
                                errorType = SearchErrorType.NONE
                                networkErrorMessage = null
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = clearSearchText,
                                tint = AppTheme.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState: FocusState ->
                    hasFocus = focusState.isFocused
                    if (focusState.isFocused && searchResults.isNotEmpty()) {
                        showResults = true
                    }
                },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.TextPrimary,
                unfocusedTextColor = AppTheme.TextPrimary,
                focusedBorderColor = AppTheme.AccentGold,
                unfocusedBorderColor = AppTheme.BorderColor,
                focusedLabelColor = AppTheme.AccentGold,
                unfocusedLabelColor = AppTheme.TextMuted,
                cursorColor = AppTheme.AccentGold
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
        )

        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = AppTheme.ErrorColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        AnimatedVisibility(
            visible = showResults && searchResults.isNotEmpty(),
            enter = fadeIn() + slideInVertically { -it / 4 },
            exit = fadeOut() + slideOutVertically { -it / 4 }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = AppTheme.CardBackground,
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    itemsIndexed(
                        items = searchResults,
                        key = { index, result -> "${result.latitude}_${result.longitude}_$index" }
                    ) { index, result ->
                        LocationResultItem(
                            result = result,
                            onClick = {
                                val formattedName = result.formattedShortName
                                onValueChange(formattedName)
                                onLocationSelected(formattedName, result.latitude, result.longitude)
                                showResults = false
                                focusManager.clearFocus()
                            }
                        )

                        if (index < searchResults.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = AppTheme.BorderColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationResultItem(
    result: GeocodingService.GeocodingResult,
    onClick: () -> Unit
) {
    val selectText = stringResource(StringKey.LOCATION_SELECT, result.formattedShortName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { contentDescription = selectText },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppTheme.AccentGold.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = AppTheme.AccentGold,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            val parts = result.displayName.split(",").map { it.trim() }
            val mainName = parts.firstOrNull() ?: result.displayName
            val details = if (parts.size > 1) {
                parts.drop(1).take(2).joinToString(", ")
            } else ""

            Text(
                text = mainName,
                color = AppTheme.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = details,
                    color = AppTheme.TextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(
                    Locale.US,
                    "%.4f°, %.4f°",
                    result.latitude,
                    result.longitude
                ),
                color = AppTheme.TextSubtle,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Stable
class LocationSearchState {
    var isSearching by mutableStateOf(false)
        private set
    var results by mutableStateOf<List<GeocodingService.GeocodingResult>>(emptyList())
        private set
    var errorType by mutableStateOf(SearchErrorType.NONE)
        private set
    var networkErrorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun search(query: String, limit: Int = 5) {
        if (query.length < 3) {
            results = emptyList()
            return
        }

        isSearching = true
        errorType = SearchErrorType.NONE
        networkErrorMessage = null

        val result = GeocodingService.searchLocation(query, limit)
        result.onSuccess { searchResults ->
            results = searchResults
        }.onFailure { e ->
            when (e) {
                is GeocodingService.GeocodingError.NetworkError -> {
                    errorType = SearchErrorType.NETWORK
                    networkErrorMessage = e.message
                }
                is GeocodingService.GeocodingError.RateLimitExceeded -> {
                    errorType = SearchErrorType.RATE_LIMIT
                }
                else -> {
                    errorType = SearchErrorType.GENERIC
                }
            }
            results = emptyList()
        }

        isSearching = false
    }

    fun clear() {
        results = emptyList()
        errorType = SearchErrorType.NONE
        networkErrorMessage = null
        isSearching = false
    }
}

@Composable
fun rememberLocationSearchState(): LocationSearchState {
    return remember { LocationSearchState() }
}