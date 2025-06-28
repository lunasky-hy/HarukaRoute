package com.lunaskyhy.harukaroute.map

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.Navigator
import com.lunaskyhy.harukaroute.data.repos.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class NavigationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {
    // Navigator のインスタンスを保持
    private var navigator: Navigator? = null

    private val _uiState = MutableStateFlow(NavigationState())
    val uiState = _uiState.asStateFlow()

    // ルートのPolylineやナビ情報などをStateで公開
    var routePolyline by mutableStateOf<List<LatLng>>(emptyList())
    var nextTurnInfo by mutableStateOf<String?>(null)

    fun startNavigation() {
        // ... Navigatorを初期化して、ナビを開始 ...
        // navigator.setRouteChangedListener { ... } でルート情報を取得して routePolyline を更新
        // navigator.addNavInfoListener { navInfo -> ... } でナビ情報を取得して nextTurnInfo を更新
    }

    fun startLocationUpdates() {
        locationRepository.getLocationUpdates()
            .onEach { location ->
                _uiState.update {
                    it.copy(lastKnownLocation = location, isLoading = false)
                }
            }.catch { e -> Log.e("LocationUpdates", "Error: ${e.message}") }
            .launchIn(viewModelScope)
    }
}

data class NavigationState(
    val lastKnownLocation: Location? = null,
    val isLoading: Boolean = true,
)