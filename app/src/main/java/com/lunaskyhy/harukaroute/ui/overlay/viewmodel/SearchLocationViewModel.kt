package com.lunaskyhy.harukaroute.ui.overlay.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.lunaskyhy.harukaroute.map.MapPlaces
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

const val TAG = "SearchLocationViewModel"

class SearchLocationViewModel(
//    locationRepository: LocationRepository,
): ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchUiState: StateFlow<LocationAutocompleteUiState> = query.debounce(500L)
        .filter { it.isNotBlank() }
        .flatMapLatest { q ->
            Log.d(TAG, "query: $q")
            flow {
                emit(LocationAutocompleteUiState.Loading)

//                val center = LatLng(37.7749, -122.4194)
//                val locationNearbyCircle = CircularBounds.newInstance(center,  /* radius = */5000.0)

                val token = AutocompleteSessionToken.newInstance()
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(q)
//                    .setLocationRestriction(locationNearbyCircle)
                    .build()
                Log.d(TAG, "request")

                val res = MapPlaces.getPlacesClient().findAutocompletePredictions(request).await()
                Log.d(TAG, "response")
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

    fun updateSearchQuery(text: String) {
        _query.value = text
    }

//    fun currentLocaton() {
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
    data object Loading: LocationAutocompleteUiState
    data class Success(val predictions: List<AutocompletePrediction>): LocationAutocompleteUiState
    data class Error(val exception: String): LocationAutocompleteUiState
    data object  Empty: LocationAutocompleteUiState
}