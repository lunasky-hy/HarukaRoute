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
import com.google.android.libraries.navigation.ListenableResultFuture
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.Navigator.RouteStatus
import com.google.android.libraries.navigation.RoutingOptions
import com.google.android.libraries.navigation.Waypoint
import com.google.android.libraries.navigation.Waypoint.UnsupportedPlaceIdException
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.lunaskyhy.harukaroute.data.PlacesClientProvider
import com.lunaskyhy.harukaroute.data.repos.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG = "MyNavigationViewModel"

object LocationDetailSettings {
    val placeFields: List<Place.Field> = listOf(
        Place.Field.ID,
        Place.Field.LOCATION,
        Place.Field.DISPLAY_NAME,
        Place.Field.SHORT_FORMATTED_ADDRESS,
        Place.Field.RESOURCE_NAME
    )
}

class MyNavigationViewModel(
    private val locationRepository: LocationRepository,
    private val placeClient: PlacesClientProvider,
) : ViewModel() {
    private val _currentLocationState = MutableStateFlow(CurrentLocationState())
    val currentLocationState = _currentLocationState.asStateFlow()

    private val _navigateLocationUiState = MutableStateFlow<NavigateLocationUiState>(NavigateLocationUiState.PlaceUnselected)
    val navigateLocationUiState = _navigateLocationUiState.asStateFlow()

    private val _navigator = MutableStateFlow<Navigator?>(null)
    val navigator: StateFlow<Navigator?> = _navigator.asStateFlow()

    private val _cameraPosition = MutableStateFlow(
        CameraPosition.fromLatLngZoom(LatLng(35.681236, 139.767125), 16.4F)
    )
    val cameraPosition = _cameraPosition.asStateFlow()

    private var trackingCamera by mutableStateOf(true)

    init {
        startLocationUpdates()
    }

    fun initializeNavigator(navigatorClient: Navigator?) {
        _navigator.value = navigatorClient
    }

    fun startNavigation(place: Place?) {
        // ... Navigatorを初期化して、ナビを開始 ...
        // navigator.setRouteChangedListener { ... } でルート情報を取得して routePolyline を更新
        // navigator.addNavInfoListener { navInfo -> ... } でナビ情報を取得して nextTurnInfo を更新
        _navigator.value?.let { navigateToPlaceDetail(navigator = it, place = place ?: return) }
    }

    fun updateCameraPosition(newPosition: CameraPosition) {
        _cameraPosition.value = newPosition
    }

    private fun moveCamera(latLng: LatLng, zoom: Float? = null) {
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
        _navigateLocationUiState.value = NavigateLocationUiState.Loading(placeId, sessionToken)

        // viewModelScopeで非同期処理を開始
        viewModelScope.launch {
            getLocationDetail(placeId)
                .addOnSuccessListener { response ->
                    // 成功したら、取得したPlaceオブジェクトでSuccess状態に更新
                    val place = response.place
                    _navigateLocationUiState.value = NavigateLocationUiState.PlaceDetail(place = place)
                    trackingCamera = false
                    moveCamera(place.location ?: _cameraPosition.value.target)
                }
                .addOnFailureListener { exception ->
                    // 失敗した場合の処理
                    // ここではログを出力して、状態を未選択に戻している
                    Log.e("NavigationViewModel", "場所の詳細取得に失敗しました。", exception)
                    _navigateLocationUiState.value = NavigateLocationUiState.Error("場所の詳細取得に失敗しました。$exception")
                }
        }
    }

    fun closeLocationDetail() {
        _navigateLocationUiState.value = NavigateLocationUiState.PlaceUnselected
        trackingCamera = true
    }

    private fun getLocationDetail(placeId: String): Task<FetchPlaceResponse> {
        // Define a place ID.
        val request = FetchPlaceRequest.newInstance(placeId, LocationDetailSettings.placeFields)
        val placeTask = placeClient.getPlacesClient().fetchPlace(request)
        return placeTask
    }
}

private fun navigateToPlaceDetail(
    navigator: Navigator,
    place: Place,
    routingOptions: RoutingOptions = RoutingOptions(),
) {
    routingOptions.travelMode(RoutingOptions.TravelMode.DRIVING);

    val destination: Waypoint
    try {
        destination = Waypoint.builder().setPlaceIdString(place.id).build()
    } catch (e: UnsupportedPlaceIdException) {
        Log.e("MyNavigationVM", "Error starting navigation: Place ID is not supported.")
        return
    }
    val pendingRoute: ListenableResultFuture<RouteStatus> = navigator.setDestination(destination, routingOptions)


    // Define the action to perform when the SDK has determined the route.
    pendingRoute.setOnResultListener { code ->
        when (code) {
            RouteStatus.OK -> {
                // Hide the toolbar to maximize the navigation UI.
//                if (getActionBar() != null) {
//                    getActionBar().hide()
//                }

                // Enable voice audio guidance (through the device speaker).
                navigator.setAudioGuidance(
                    Navigator.AudioGuidance.VOICE_ALERTS_AND_GUIDANCE
                )

                // Simulate vehicle progress along the route for demo/debug builds.
//                if (BuildConfig.DEBUG) {
//                    mNavigator.getSimulator().simulateLocationsAlongExistingRoute(
//                        SimulationOptions().speedMultiplier(5f)
//                    )
//                }

                // Start turn-by-turn guidance along the current route.
                navigator.startGuidance()
            }

            RouteStatus.NO_ROUTE_FOUND -> Log.e(TAG, "Error starting navigation: No route found.")
            RouteStatus.NETWORK_ERROR -> Log.e(TAG, "Error starting navigation: Network error.")
            RouteStatus.ROUTE_CANCELED -> Log.e(TAG, "Error starting navigation: Route canceled.")
            else -> Log.e(TAG, "Error starting navigation: $code")
        }
    }
}

data class CurrentLocationState(
    val lastKnownLocation: Location? = null,
    val isLoading: Boolean = true,
)

sealed interface NavigateLocationUiState {
    data object PlaceUnselected : NavigateLocationUiState
    data class Loading(val placeId: String, val sessionToken: AutocompleteSessionToken?) : NavigateLocationUiState
    data class PlaceDetail(val place: Place? = null, val isLoading: Boolean = true) : NavigateLocationUiState
    data class RoutePreviewing(val place: Place? = null) : NavigateLocationUiState
    data class Error(val exception: String) : NavigateLocationUiState
}