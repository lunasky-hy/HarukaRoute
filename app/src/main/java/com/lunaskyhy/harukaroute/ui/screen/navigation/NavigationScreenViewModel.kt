package com.lunaskyhy.harukaroute.ui.screen.navigation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocompleteResult
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val TAG = "NavigationScreenViewModel"

class NavigationScreenViewModel(
    private val mapController: HarukaMapController = MapControllerProvider.harukaMapController
): ViewModel() {
    private val _uiState = MutableStateFlow(NavigationScreenUiState())
    val uiState = _uiState.asStateFlow()

    var searchQuery: String by mutableStateOf("")
        private set

    fun toggleSearchActive() {
        searchQuery = ""
        _uiState.value = _uiState.value.copy(isSearchActive = !_uiState.value.isSearchActive)
    }

    fun searchQueryChange(query: String) {
        searchQuery = query
        placeAutocomplete()
    }

    fun displayDetailSuggestion(suggestion: PlaceAutocompleteSuggestion) {
        viewModelScope.launch {
            val detailResponse = mapController.placeAutocomplete.select(suggestion)
            Log.d(TAG, "displayPlaceSuggestionDetail: $detailResponse")
            detailResponse.onValue { result ->
                Log.d(TAG, "displayPlaceSuggestionDetail: $result")
                _uiState.value = _uiState.value.copy(selectedSuggestion = result)
            }.onError {
                _uiState.value = _uiState.value.copy(selectedSuggestion = null)
            }
        }
    }

    fun unselectDetailSuggestion() {
        _uiState.value = _uiState.value.copy(selectedSuggestion = null)
    }

    fun previewRouteSuggestion(suggestion: PlaceAutocompleteResult) {
        Log.d(TAG, "previewRouteSuggestion: $suggestion")

        val destination = suggestion.routablePoints?.first()?.point
        if (destination != null) {
            mapController.routePreviewRequest(destination)
            _uiState.value = _uiState.value.copy(previewRouteSuggestion = destination)
        }
    }

    fun previewRouteClose() {
        mapController.routePreviewClose()
        _uiState.value = _uiState.value.copy(previewRouteSuggestion = null)
    }

    private fun placeAutocomplete() {
        viewModelScope.launch {
            val response = mapController.placeAutocomplete.suggestions(query = searchQuery)
            if (response.isValue) {
                val suggestions = response.value.orEmpty()
                _uiState.value = _uiState.value.copy(placeSuggestions = suggestions)
            }
        }
    }
}

data class NavigationScreenUiState(
    val isSearchActive: Boolean = false,
    val placeSuggestions: List<PlaceAutocompleteSuggestion> = emptyList(),
    val selectedSuggestion: PlaceAutocompleteResult? = null,
    val previewRouteSuggestion: Point? = null
)