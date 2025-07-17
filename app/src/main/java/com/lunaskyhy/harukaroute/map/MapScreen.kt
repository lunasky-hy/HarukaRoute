@file:OptIn(FlowPreview::class)

package com.lunaskyhy.harukaroute.map

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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

@SuppressLint("MissingPermission")
@Composable
fun NavigationMapScreen(
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit = {},
    viewModel: NavigationViewModel,
) {
    // Contextを取得
    val context = LocalContext.current
    // MapViewのインスタンスを記憶
    val mapView = remember { MapView(context) }
    var map by remember { mutableStateOf<GoogleMap?>(null) }

    // MapViewのライフサイクルを管理するためのComposable
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // Google Mapのカメラ関係の処理、ViewModelからFlowで受け取る
    // ViewModelの値が変更されたらカメラ位置も切り替える
    val cameraPosition by viewModel.cameraPosition.collectAsStateWithLifecycle()
    LaunchedEffect(cameraPosition) {
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    // GoogleMapでのマーカーの描画
    //　Stateに変更があったら、マーカーを描画し、必要なくなればマーカーを消す
    val detailUiState by viewModel.locationDetailUiState.collectAsStateWithLifecycle()
    val markers = remember { mutableStateListOf<Marker>() }
    LaunchedEffect(detailUiState) {
        if (detailUiState is LocationDetailUiState.Success) {
            val place = (detailUiState as LocationDetailUiState.Success).place
            place?.location?.let {
                val marker = map?.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title(place.displayName)
                )
                marker?.let { m ->
                    markers.add(m)
                }
            }
        } else {
            markers.forEach { it.remove() }
        }
    }


    // AndroidViewを使ってMapViewをComposeに描画
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    // マップの非同期読み込みを開始
                    getMapAsync { googleMap ->
                        // マップの準備ができたらコールバックを呼び出す
                        onMapReady(googleMap)
                        map = googleMap
                        googleMap.setOnCameraIdleListener {
                            val currentCameraPosition = googleMap.cameraPosition
                            viewModel.updateCameraPosition(currentCameraPosition)
                        }
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        googleMap.isMyLocationEnabled = true
                    }
                }
            },
        )
    }
}


@Composable
fun MapScreen(
    @SuppressLint("MissingPermission") viewModel: NavigationViewModel
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