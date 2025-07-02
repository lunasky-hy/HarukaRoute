package com.lunaskyhy.harukaroute.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider


@Composable
fun MapScreen(
    viewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(35.681236, 139.767125), 16f)
    }

    val locationSource = remember { MapLocationSource() }

    LaunchedEffect(uiState.lastKnownLocation) {
        uiState.lastKnownLocation?.let {
            locationSource.onLocationChanged(it)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude), 16f
            )
        }
    }

    val uiSettings = MapUiSettings(
        compassEnabled = false,
        myLocationButtonEnabled = false,
        indoorLevelPickerEnabled = false,
        mapToolbarEnabled = false,
        zoomControlsEnabled = false,
    )

    val mapProperties = MapProperties(
        isMyLocationEnabled = true,
        isTrafficEnabled = true,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            properties = mapProperties,
            uiSettings = uiSettings,
        ) {
        }

        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.BottomStart,
        ) {
            ScaleBar(
                modifier = Modifier.padding(
                    bottom = dimensionResource(R.dimen.padding_large),
                    start = dimensionResource(R.dimen.padding_medium)
                ),
                cameraPositionState = cameraPositionState,
                textColor = MaterialTheme.colorScheme.onBackground,
                lineColor = MaterialTheme.colorScheme.onBackground,
                shadowColor = MaterialTheme.colorScheme.background,
            )
        }
    }
}