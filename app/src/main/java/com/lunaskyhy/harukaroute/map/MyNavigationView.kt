package com.lunaskyhy.harukaroute.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.NavigationView
import com.google.android.libraries.navigation.Navigator
import com.lunaskyhy.harukaroute.MainActivity

@SuppressLint("MissingPermission")
@Composable
fun MyNavigationView(
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit = {},
    activity: MainActivity,
    viewModel: MyNavigationViewModel,
) {
    val context = LocalContext.current
    val navigationView by remember { mutableStateOf(NavigationView(context)) }
    var map by remember { mutableStateOf<GoogleMap?>(null) }

    LaunchedEffect(Unit) {
        NavigationApi.getNavigator(
            activity,
            object : NavigationApi.NavigatorListener {
                override fun onError(p0: Int) = navigatorInitializeError(p0)
                override fun onNavigatorReady(navigator: Navigator?) {
                    viewModel.initializeNavigator(navigator)
                }
            }
        )
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, navigationView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> navigationView.onCreate(null)
                Lifecycle.Event.ON_START -> navigationView.onStart()
                Lifecycle.Event.ON_RESUME -> navigationView.onResume()
                Lifecycle.Event.ON_PAUSE -> navigationView.onPause()
                Lifecycle.Event.ON_STOP -> navigationView.onStop()
                Lifecycle.Event.ON_DESTROY -> navigationView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver((lifecycleObserver))
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
    val detailUiState by viewModel.navigateLocationUiState.collectAsStateWithLifecycle()
    val markers = remember { mutableStateListOf<Marker>() }
    LaunchedEffect(detailUiState) {
        if (detailUiState is NavigateLocationUiState.PlaceDetail) {
            val place = (detailUiState as NavigateLocationUiState.PlaceDetail).place
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

    AndroidView(
        factory = {
            navigationView.apply {
                getMapAsync {
                    onMapReady(it)
                    map = it
                    it.setOnCameraIdleListener {
                        val currentCameraPosition = it.cameraPosition
                        viewModel.updateCameraPosition(currentCameraPosition)
                    }
                    it.moveCamera(CameraUpdateFactory.newCameraPosition((cameraPosition)))
                    it.isMyLocationEnabled = true
                }
            }
        },
//        update = { navigationView ->
//            if (map != null) {
//
//            }
//        }
    )
}

private fun navigatorInitializeError(errorCode: Int) {
    val tag = "NavigatorAPI"
    when (errorCode) {
        NavigationApi.ErrorCode.NOT_AUTHORIZED -> Log.e( tag,
            "Error loading Navigation SDK: Your API key is "
                    + "invalid or not authorized to use the Navigation SDK."
        )

        NavigationApi.ErrorCode.TERMS_NOT_ACCEPTED -> Log.e( tag,
            "Error loading Navigation SDK: User did not accept "
                    + "the Navigation Terms of Use."
        )

        NavigationApi.ErrorCode.NETWORK_ERROR -> Log.e(tag, "Error loading Navigation SDK: Network error.")
        NavigationApi.ErrorCode.LOCATION_PERMISSION_MISSING -> Log.e( tag,
            "Error loading Navigation SDK: Location permission "
                    + "is missing."
        )

        else -> Log.e(tag, "Error loading Navigation SDK: $errorCode")
    }
}
