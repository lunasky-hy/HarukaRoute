@file:OptIn(FlowPreview::class)

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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.widgets.ScaleBar
import com.lunaskyhy.harukaroute.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce


@Composable
fun MapScreen(
    viewModel: NavigationViewModel
) {
    val cameraPosition by viewModel.cameraPosition.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState { position = cameraPosition }
    // 1. ViewModelの状態が変化したら、地図のカメラを動かす (ViewModel -> View)
    LaunchedEffect(cameraPosition) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newCameraPosition(cameraPosition),
            durationMs = 1000,
        )
    }
    // 2. 地図のカメラがユーザーによって操作されたら、ViewModelに通知する (View -> ViewModel)
    LaunchedEffect(cameraPositionState) {
        // cameraPositionStateの変更をFlowに変換
        snapshotFlow { cameraPositionState.position }
            // ドラッグ中の過剰な更新を防ぐために、少し待ってから通知する
            .debounce(300L)
            .collect { position ->
                viewModel.updateCameraPosition(position)
            }
    }

    val detailUiState by viewModel.locationDetailUiState.collectAsStateWithLifecycle()
    val destinationMarkerState: MarkerState = rememberMarkerState()
    LaunchedEffect(detailUiState) {
        if (detailUiState is LocationDetailUiState.Success) {
            val place = (detailUiState as LocationDetailUiState.Success).place
            destinationMarkerState.position = place?.location ?: LatLng(0.0, 0.0)
        }
    }

    val locationSource = remember { MapLocationSource() }

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
            when (detailUiState) {
                is LocationDetailUiState.Success -> {
                    Marker(
                        state = destinationMarkerState,
                        title = (detailUiState as LocationDetailUiState.Success).place?.displayName,
                        snippet = "目的地",
                    )
                }
                else -> {}
            }
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