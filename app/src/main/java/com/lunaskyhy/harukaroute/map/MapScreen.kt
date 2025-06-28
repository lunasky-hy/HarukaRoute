package com.lunaskyhy.harukaroute.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.widgets.ScaleBar
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider


@Composable
fun MapScreen(
    viewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationSource = remember { MapLocationSource() }

    LaunchedEffect(uiState.lastKnownLocation) {
        uiState.lastKnownLocation?.let {
            locationSource.onLocationChanged(it)
        }
    }

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
            locationSource = locationSource,
            properties = MapProperties(
                isMyLocationEnabled = true,
            )
        ) {
            Marker(
                state = tokyoStationMarkerState,
                title = "tokyo station",
                snippet = "Marker in tokyo sta."
            )
        }

        ScaleBar(
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium)
            ),
            cameraPositionState = cameraPositionState,
            textColor = MaterialTheme.colorScheme.onBackground,
            lineColor = MaterialTheme.colorScheme.onBackground,
            shadowColor = MaterialTheme.colorScheme.background,
        )
    }
}