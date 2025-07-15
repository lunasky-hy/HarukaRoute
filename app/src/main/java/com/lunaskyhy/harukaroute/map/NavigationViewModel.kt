package com.lunaskyhy.harukaroute.map

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.lunaskyhy.harukaroute.data.repos.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object LocationDetailSettings {
    val placeFields: List<Place.Field> = listOf(
        Place.Field.ID,
        Place.Field.LOCATION,
        Place.Field.SHORT_FORMATTED_ADDRESS,
        Place.Field.RESOURCE_NAME
    )
}

class NavigationViewModel(
    private val locationRepository: LocationRepository,
    private val placeClient: PlacesClientProvider,
) : ViewModel() {
    // Navigator のインスタンスを保持
    private var navigator: Navigator? = null

    private val _currentLocationState = MutableStateFlow(CurrentLocationState())
    val currentLocationState = _currentLocationState.asStateFlow()

    private val _locationDetailUiState = MutableStateFlow<LocationDetailUiState>(LocationDetailUiState.PlaceUnselected)
    val locationDetailUiState = _locationDetailUiState.asStateFlow()

    private val _cameraPosition = MutableStateFlow(
        CameraPosition.fromLatLngZoom(LatLng(35.681236, 139.767125), 16.4F)
    )
    val cameraPosition = _cameraPosition.asStateFlow()

    private var trackingCamera by mutableStateOf(true)

    // ルートのPolylineやナビ情報などをStateで公開
    var routePolyline by mutableStateOf<List<LatLng>>(emptyList())
    var nextTurnInfo by mutableStateOf<String?>(null)

    init {
        startLocationUpdates()
    }

    fun startNavigation() {
        // ... Navigatorを初期化して、ナビを開始 ...
        // navigator.setRouteChangedListener { ... } でルート情報を取得して routePolyline を更新
        // navigator.addNavInfoListener { navInfo -> ... } でナビ情報を取得して nextTurnInfo を更新
    }

    fun updateCameraPosition(newPosition: CameraPosition) {
        _cameraPosition.value = newPosition
    }

    fun moveCamera(latLng: LatLng, zoom: Float? = null) {
        _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom ?: _cameraPosition.value.zoom)
    }

    private fun startLocationUpdates() {
        locationRepository.getLocationUpdates()
            .onEach { location ->
                _currentLocationState.update {
                    it.copy(lastKnownLocation = location, isLoading = false)
                }.also {
                    if (trackingCamera)
                        moveCamera(LatLng(location.latitude, location.longitude))
                }
            }.catch { e -> Log.e("LocationUpdates", "Error: ${e.message}") }
            .launchIn(viewModelScope)
    }

    fun setLocationDetail(placeId: String, sessionToken: AutocompleteSessionToken?) {
        _locationDetailUiState.value = LocationDetailUiState.Loading(placeId, sessionToken)

        // viewModelScopeで非同期処理を開始
        viewModelScope.launch {
            getLocationDetail(placeId)
                .addOnSuccessListener { response ->
                    // 成功したら、取得したPlaceオブジェクトでSuccess状態に更新
                    val place = response.place
                    _locationDetailUiState.value = LocationDetailUiState.Success(place = place)
                    trackingCamera = false
                    moveCamera(place.location ?: _cameraPosition.value.target)
                }
                .addOnFailureListener { exception ->
                    // 失敗した場合の処理
                    // ここではログを出力して、状態を未選択に戻している
                    Log.e("NavigationViewModel", "場所の詳細取得に失敗しました。", exception)
                    _locationDetailUiState.value = LocationDetailUiState.Error("場所の詳細取得に失敗しました。$exception")
                }
        }
    }

    fun closeLocationDetail() {
        _locationDetailUiState.value = LocationDetailUiState.PlaceUnselected
        trackingCamera = true
    }

    private fun getLocationDetail(placeId: String): Task<FetchPlaceResponse> {
        // Define a place ID.
        val request = FetchPlaceRequest.newInstance(placeId, LocationDetailSettings.placeFields)
        val placeTask = placeClient.getPlacesClient().fetchPlace(request)
        return placeTask
    }
}

data class CurrentLocationState(
    val lastKnownLocation: Location? = null,
    val isLoading: Boolean = true,
)

sealed interface LocationDetailUiState {
    data object PlaceUnselected : LocationDetailUiState
    data class Loading(val placeId: String, val sessionToken: AutocompleteSessionToken?) : LocationDetailUiState
    data class Success(val place: Place? = null, val isLoading: Boolean = true, ) : LocationDetailUiState
    data class Error(val exception: String) : LocationDetailUiState
}