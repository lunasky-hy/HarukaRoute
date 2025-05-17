package com.lunaskyhy.harukaroute

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.R

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

class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var replayProgressObserver: ReplayProgressObserver
    private val navigationLocationProvider = NavigationLocationProvider()
    private val replayRouteMapper = ReplayRouteMapper()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                MapboxNavigationApp.attach(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                MapboxNavigationApp.detach(owner)
            }
        })
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

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
        } else {
            // Request location permissions
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
            )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    private fun initializeMapComponents() {
        // create a new Mapbox map
        mapView = MapView(this, mapInitOptions = MapInitOptions(applicationContext))
//        setContentView(mapView)

        setContent {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Initialize location puck using navigationLocationProvider as its data source
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = LocationPuck2D()
            puckBearingEnabled = true
            enabled = true
        }

        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(139.7644865, 35.6811398))
                .zoom(14.0)
                .build()
        )

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

// locationObserver updates the location puck and camera to follow the user's location
    private val locationObserver =
        object : LocationObserver {
            override fun onNewRawLocation(rawLocation: Location) {}

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
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
                        )
                        .build()
                )
            }
        }

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