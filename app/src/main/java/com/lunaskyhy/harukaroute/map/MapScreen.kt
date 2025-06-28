package com.lunaskyhy.harukaroute.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen(
    viewModel: NavigationViewModel = NavigationViewModel()
) {
    val route: List<LatLng> = viewModel.routePolyline

    // 1. カメラの状態を管理
    val cameraPositionState = rememberCameraPositionState {
        // 初期位置（例：東京駅）
        position = CameraPosition.fromLatLngZoom(LatLng(35.681236, 139.767125), 15f)
    }

    val tokyoStation = LatLng(35.681236, 139.767125)
    val tokyoStationMarkerState = rememberMarkerState(position = tokyoStation)

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            if (route.isNotEmpty()) {
                Polyline(points = route, color = Color.Blue, width = 20f)
            }

            Marker(
                state = tokyoStationMarkerState,
                title = "tokyo station",
                snippet = "Marker in tokyo sta."
            )
        }
    }
}