package com.lunaskyhy.harukaroute

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.ScaleBar
import com.mapbox.maps.plugin.scalebar.ScaleBarPlugin
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettings
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettingsBase
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettingsInterface
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.androidauto.internal.RendererUtils.dpToPx
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            val carConnectionType by CarConnection(this).type.observeAsState(initial = -1)
//
//            AppTheme {
//                Surface {
//                    Column {
//                        Text(
//                            text = "Places",
//                            style = MaterialTheme.typography.displayLarge,
//                            modifier = Modifier.padding(8.dp)
//                        )
//                        ProjectionState(
//                            carConnectionType = carConnectionType,
//                            modifier = Modifier.padding(8.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private val navigationLocationProvider = NavigationLocationProvider()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                Log.d(TAG, "lifecycle onCreate is started.")
                super.onCreate(owner)

                Log.d(TAG, "lifecycle onCreate is completed.")
            }

            @SuppressLint("MissingPermission")
            override fun onResume(owner: LifecycleOwner) {
                Log.d(TAG, "lifecycle onResume is started.")
                MapboxNavigationApp.attach(owner)
                MapboxNavigationApp.registerObserver(navigationObserver)
                Log.d(TAG, "lifecycle onResume is completed.")
            }

            override fun onPause(owner: LifecycleOwner) {
                Log.d(TAG, "lifecycle onPause is started.")
                MapboxNavigationApp.detach(owner)
                MapboxNavigationApp.registerObserver(navigationObserver)

                Log.d(TAG, "lifecycle onPause is completed.")
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Activity onCreate is started.")
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(applicationContext)
                    // additional options
                    .build()
            }
        }

        // check/request location permissions
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are already granted
            initializeMapComponents()
            Log.d(TAG, "initializeMapComponents is completed. (in Activity.onCreated.)")
        } else {
            // Request location permissions
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
            )
        }
        Log.d(TAG, "Activity onCreate is completed.")
    }

    // Activity result launcher for location permissions
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    initializeMapComponents()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Location permissions denied. Please enable permissions in settings.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }

    private fun initializeMapComponents() {
        // create a new Mapbox map
        mapView = MapView(this, mapInitOptions = MapInitOptions(applicationContext))

        mapView.scalebar.updateSettings {
            position = Gravity.BOTTOM or Gravity.END
            borderWidth = 4f
        }

        mapView.compass.updateSettings {
            position = Gravity.TOP or Gravity.END
            marginTop = 100f
            marginRight = 20f
            clickable = true
        }

        setContent {
            AppTheme {
                Scaffold(
                    topBar = {},
                    bottomBar = {},
                ) { paddingValues ->
                     AndroidView(
                        factory = { mapView },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 0.dp,
                                bottom = paddingValues.calculateBottomPadding(),
                                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                            ),
                    )
                }
            }
        }

        // Initialize location puck using navigationLocationProvider as its data source
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = createDefault2DPuck()
            puckBearingEnabled = true
            enabled = true
        }

        // set viewportDataSource, which tells the navigationCamera where to look
        viewportDataSource = MapboxNavigationViewportDataSource(mapView.mapboxMap)

        // set padding for the navigation camera
        val pixelDensity = this.resources.displayMetrics.density
        viewportDataSource.followingPadding =
            EdgeInsets(
                180.0 * pixelDensity,
                40.0 * pixelDensity,
                150.0 * pixelDensity,
                40.0 * pixelDensity
            )

        // initialize a NavigationCamera
        navigationCamera = NavigationCamera(mapView.mapboxMap, mapView.camera, viewportDataSource)

        // Initialize route line api and view for drawing the route on the map
        routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
        routeLineView = MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(this).build())
    }

// locationObserver updates the location puck and camera to follow the user's location
    private val locationObserver =
        object : LocationObserver {
            override fun onNewRawLocation(rawLocation: Location) {}

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                Log.d(TAG, "locationObserver onNewLocationMatcherResult is called.")

                val enhancedLocation = locationMatcherResult.enhancedLocation
                // update location puck's position on the map
                navigationLocationProvider.changePosition(
                    location = enhancedLocation,
                    keyPoints = locationMatcherResult.keyPoints,
                )

                // update viewportDataSource to trigger camera to follow the location
                viewportDataSource.onLocationChanged(enhancedLocation)
                viewportDataSource.evaluate()

                // set the navigationCamera to FOLLOWING
                navigationCamera.requestNavigationCameraToFollowing()

                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(
                            Point.fromLngLat(
                                enhancedLocation.longitude,
                                enhancedLocation.latitude
                            )
                        ).zoom(14.0)
                        .build()
                )
                Log.d(TAG, "locationObserver onNewLocationMatcherResult is completed.")
            }
        }

    // routes observer draws a route line and origin/destination circles on the map
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(routeUpdateResult.navigationRoutes) { value ->
                mapView.mapboxMap.style?.apply { routeLineView.renderRouteDrawData(this, value) }
            }

            // update viewportSourceData to include the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()

            // set the navigationCamera to OVERVIEW
            navigationCamera.requestNavigationCameraToOverview()
        }
    }


    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val navigationObserver =
        object : MapboxNavigationObserver {
            private val mutableLocation = MutableStateFlow<LocationMatcherResult?>(null)
            val locationFlow: Flow<LocationMatcherResult?> = mutableLocation

            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                Log.d(TAG, "navigationObserver onAttached is called.")

                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)

//                replayProgressObserver =
//                    ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
//                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
//                mapboxNavigation.startReplayTripSession()
                mapboxNavigation.startTripSession()

                Log.d(TAG, "navigationObserver onAttached is completed.")
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                Log.d(TAG, "navigationObserver onDetached is called.")
                mapboxNavigation.stopTripSession()
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                Log.d(TAG, "navigationObserver onDetached is completed.")
            }
        }

//    private lateinit var replayProgressObserver: ReplayProgressObserver
//    private val replayRouteMapper = ReplayRouteMapper()

    // define MapboxNavigation
//    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
//    private val mapboxNavigation: MapboxNavigation by
//    requireMapboxNavigation(
//        onResumedObserver =
//            object : MapboxNavigationObserver {
//                @SuppressLint("MissingPermission")
//                override fun onAttached(mapboxNavigation: MapboxNavigation) {
//                    // register observers
//                    mapboxNavigation.registerRoutesObserver(routesObserver)
//                    mapboxNavigation.registerLocationObserver(locationObserver)
//
//                    replayProgressObserver =
//                        ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
//                    mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
//                    mapboxNavigation.startReplayTripSession()
//                }
//
//                override fun onDetached(mapboxNavigation: MapboxNavigation) {}
//            },
//        onInitialize = this::initNavigation
//    )

    // on initialization of MapboxNavigation, request a route between two fixed points
//    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
//    private fun initNavigation() {
//        MapboxNavigationApp.setup(NavigationOptions.Builder(this).build())
//
//        // initialize location puck
//        mapView.location.apply {
//            setLocationProvider(navigationLocationProvider)
//            this.locationPuck = createDefault2DPuck()
//            enabled = true
//        }

//        val origin = Point.fromLngLat(139.7644865, 35.6811398)
//        val destination = Point.fromLngLat(139.6974803, 35.6900803)
//        mapboxNavigation.requestRoutes(
//            RouteOptions.builder()
//                .applyDefaultNavigationOptions()
//                .coordinatesList(listOf(origin, destination))
//                .layersList(listOf(mapboxNavigation.getZLevel(), null))
//                .build(),
//            object : NavigationRouterCallback {
//                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {}
//
//                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {}
//
//                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
//                    mapboxNavigation.setNavigationRoutes(routes)

                    // start simulated user movement
//                    val replayData =
//                        replayRouteMapper.mapDirectionsRouteGeometry(routes.first().directionsRoute)
//                    mapboxNavigation.mapboxReplayer.pushEvents(replayData)
//                    mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
//                    mapboxNavigation.mapboxReplayer.play()
//                }
//            }
//        )
//    }
}