package com.lunaskyhy.harukaroute.ui.overlay.viewmodel

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.lunaskyhy.harukaroute.data.PlacesClientProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

const val TAG = "SearchLocationViewModel"

class SearchLocationViewModel(
//    locationRepository: LocationRepository,
    private val placeClient: PlacesClientProvider
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private var _currentLocation: Location? by mutableStateOf(null)

    val placeSessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchUiState: StateFlow<LocationAutocompleteUiState> = query.debounce(500L)
        .filter { it.isNotBlank() }
        .flatMapLatest { q ->
            Log.d(TAG, "query: $q")
            flow {
                emit(LocationAutocompleteUiState.Loading)
                val res = getAutocompletePredictions(q).await()
                emit(LocationAutocompleteUiState.Success(res.autocompletePredictions))
            }.catch {
                emit(LocationAutocompleteUiState.Error(it.message.toString()))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = LocationAutocompleteUiState.Empty,
        ).also {
            Log.d(TAG, "searchUiState: $it")
        }

    fun updateSearchQuery(text: String, currentLocation: Location?) {
        _currentLocation = currentLocation
        _query.value = text
    }

    private fun getAutocompletePredictions(queryStr: String): Task<FindAutocompletePredictionsResponse> {
        val center = _currentLocation
        lateinit var request: FindAutocompletePredictionsRequest

        if (center != null) {
            val locationNearbyCircle = CircularBounds.newInstance(LatLng(center.latitude, center.longitude),  /* radius = */5000.0)
            request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(placeSessionToken)
                .setQuery(queryStr)
                .setLocationRestriction(locationNearbyCircle)
                .build()
        } else {
            request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(placeSessionToken)
                .setQuery(queryStr)
                .build()
        }

        Log.d(TAG, "request")

        return placeClient.getPlacesClient().findAutocompletePredictions(request)
    }

//    fun currentLocation() {
//        locationRepository.getLocationUpdates()
//            .onEach { location ->
//                _uiState.update {
//                    it.copy(lastKnownLocation = location, isLoading = false)
//                }
//            }.catch { e -> Log.e("LocationUpdates", "Error: ${e.message}") }
//            .launchIn(viewModelScope)
//    }
}

sealed interface LocationAutocompleteUiState {
    data object Loading : LocationAutocompleteUiState
    data class Success(val predictions: List<AutocompletePrediction>) : LocationAutocompleteUiState
    data class Error(val exception: String) : LocationAutocompleteUiState
    data object Empty : LocationAutocompleteUiState
}