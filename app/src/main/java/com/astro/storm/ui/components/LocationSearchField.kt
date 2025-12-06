package com.astro.storm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.astro.storm.data.api.GeocodingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Matching the theme from the Input Screen
private object SearchTheme {
    val CardBackground = Color(0xFF2A201A)
    val AccentColor = Color(0xFFB8A99A)
    val TextPrimary = Color(0xFFE8DFD6)
    val TextSecondary = Color(0xFF9E8F85)
    val BorderColor = Color(0xFF4A3F38)
    val ErrorColor = Color(0xFFCF6679)
}

/**
 * Professional Location Search Component.
 * 
 * Features:
 * - Floating result list (doesn't break layout).
 * - Integration with GeocodingService structured data.
 * - Returns Timezone for Astrology calculations.
 * - Debounced network calls.
 */
@Composable
fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (location: String, latitude: Double, longitude: Double, timezone: String?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Location",
    placeholder: String = "City, Town or Village"
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var searchResults by remember { mutableStateOf<List<GeocodingService.GeocodingResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var errorState by remember { mutableStateOf<String?>(null) }

    // Flag to prevent search triggering when user clicks a result
    var isProgrammaticUpdate by remember { mutableStateOf(false) }

    fun performSearch(query: String) {
        searchJob?.cancel()
        
        if (query.length < 3) {
            searchResults = emptyList()
            return
        }

        searchJob = scope.launch {
            // Debounce: wait for user to stop typing
            delay(600) 
            
            isSearching = true
            errorState = null
            
            val result = GeocodingService.searchLocation(query, limit = 5)
            
            result.onSuccess { results ->
                searchResults = results
                if (results.isEmpty()) {
                    // Optional: Could show "No results" state
                }
            }.onFailure { error ->
                errorState = "Connection failed"
                searchResults = emptyList()
            }
            
            isSearching = false
        }
    }

    Box(modifier = modifier.zIndex(10f)) { // zIndex ensures dropdown floats over content below
        Column {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                    if (!isProgrammaticUpdate) {
                        performSearch(newValue)
                    }
                    isProgrammaticUpdate = false
                },
                label = { Text(label, color = SearchTheme.TextSecondary, fontSize = 14.sp) },
                placeholder = { Text(placeholder, color = SearchTheme.BorderColor, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = if (isFocused) SearchTheme.AccentColor else SearchTheme.TextSecondary
                    )
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SearchTheme.AccentColor,
                            strokeWidth = 2.dp
                        )
                    } else if (value.isNotEmpty()) {
                        IconButton(onClick = {
                            onValueChange("")
                            searchResults = emptyList()
                            performSearch("")
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear",
                                tint = SearchTheme.TextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { 
                        isFocused = it.isFocused 
                        // Hide results if lost focus
                        if (!it.isFocused) {
                            // Small delay to allow click event on list item to pass through
                            scope.launch {
                                delay(200) 
                                if (!isFocused) searchResults = emptyList()
                            }
                        }
                    },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SearchTheme.TextPrimary,
                    unfocusedTextColor = SearchTheme.TextPrimary,
                    focusedBorderColor = SearchTheme.AccentColor,
                    unfocusedBorderColor = SearchTheme.BorderColor,
                    cursorColor = SearchTheme.AccentColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        performSearch(value)
                        focusManager.clearFocus()
                    }
                )
            )

            // Error Text
            AnimatedVisibility(visible = errorState != null) {
                Row(
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Warning, null, tint = SearchTheme.ErrorColor, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = errorState ?: "",
                        color = SearchTheme.ErrorColor,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Dropdown Results Overlay
        // Placed inside the Box but outside the Column to overlap subsequent UI elements
        AnimatedVisibility(
            visible = isFocused && searchResults.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .padding(top = 68.dp) // Offset to appear below TextField
                .fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SearchTheme.CardBackground,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, SearchTheme.BorderColor),
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                LazyColumn {
                    items(searchResults) { result ->
                        LocationResultItem(
                            result = result,
                            onClick = {
                                isProgrammaticUpdate = true
                                onValueChange(result.formattedName)
                                onLocationSelected(
                                    result.formattedName,
                                    result.latitude,
                                    result.longitude,
                                    result.timezone
                                )
                                searchResults = emptyList()
                                focusManager.clearFocus()
                            }
                        )
                        // Separator
                        if (searchResults.last() != result) {
                            HorizontalDivider(
                                color = SearchTheme.BorderColor.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SearchTheme.AccentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = SearchTheme.AccentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Main Location (e.g., "Mumbai")
            Text(
                text = result.formattedName.substringBefore(","),
                color = SearchTheme.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Detail (e.g., "Maharashtra, India")
            val details = result.formattedName.substringAfter(", ", "")
            if (details.isNotEmpty()) {
                Text(
                    text = details,
                    color = SearchTheme.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
            
            // Coordinates Preview
            Text(
                text = "Lat: ${String.format("%.2f", result.latitude)} • Lon: ${String.format("%.2f", result.longitude)}",
                color = SearchTheme.TextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}