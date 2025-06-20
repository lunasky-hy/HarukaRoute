package com.lunaskyhy.harukaroute.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.lunaskyhy.harukaroute.TAG
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.IncidentsOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.base.trip.model.RouteProgressState
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.preview.RoutesPreviewObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.NavigationSessionState
import com.mapbox.navigation.core.trip.session.NavigationSessionStateObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.callout.api.DefaultRouteCalloutAdapter
import com.mapbox.navigation.ui.maps.route.callout.api.DefaultRouteCalloutAdapter.CalloutClickData
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.utils.internal.toPoint
import com.mapbox.search.autocomplete.PlaceAutocomplete
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class HarukaMapController (
    private val context: Context,
){
    lateinit var mapView: MapView
        private set

    private lateinit var lifecycleScope: Lifecycle
    private lateinit var navigation: MapboxNavigation
    private var viewportDataSource: MapboxNavigationViewportDataSource
    private var navigationCamera: NavigationCamera
    val navigationLocationProvider = NavigationLocationProvider()

    private val _isCameraFollowingPosition = MutableStateFlow(true)
    var isCameraFollowingPosition: StateFlow<Boolean> = _isCameraFollowingPosition.asStateFlow()

    private val _navigationSessionState = MutableStateFlow<NavigationSessionState>(NavigationSessionState.Idle)
    val navigationState = _navigationSessionState.asStateFlow()

    val placeAutocomplete = PlaceAutocomplete.create()

    init {
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(context)
                    .enableSensors(true)
                    .incidentsOptions(IncidentsOptions.Builder().build())
                    .build()
            }
        }

        initializeMapView()

        // set viewportDataSource, which tells the navigationCamera where to look
        viewportDataSource = MapboxNavigationViewportDataSource(mapView.mapboxMap)

        // set padding for the navigation camera
        val pixelDensity = context.resources.displayMetrics.density
        viewportDataSource.followingPadding =
            EdgeInsets(
                180.0 * pixelDensity,
                40.0 * pixelDensity,
                150.0 * pixelDensity,
                40.0 * pixelDensity
            )

        // initialize a NavigationCamera
        navigationCamera = NavigationCamera(mapView.mapboxMap, mapView.camera, viewportDataSource)
    }

    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder()
            .isRouteCalloutsEnabled(true)
            .build())
    }

    private val routeLineView: MapboxRouteLineView by lazy {
        val routeCalloutClickListener: ((CalloutClickData) -> Unit) = { data ->
            reorderRoutes(data.route)
        }

        MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build())
            .also {
                it.enableCallouts(
                    mapView.viewAnnotationManager,
                    DefaultRouteCalloutAdapter(context, routeCalloutClickListener = routeCalloutClickListener)
                )
            }
    }

    private val navigationStateObserver =
        NavigationSessionStateObserver { navigationSession -> _navigationSessionState.value = navigationSession }

    private val navigationObserver =
        object : MapboxNavigationObserver {
            private val mutableLocation = MutableStateFlow<LocationMatcherResult?>(null)
            val locationFlow: Flow<LocationMatcherResult?> = mutableLocation

            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                Log.d(TAG, "navigationObserver onAttached is called.")

                navigation = mapboxNavigation

                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerArrivalObserver(arrivalObserver)
                mapboxNavigation.registerRoutesPreviewObserver(routesPreviewObserver)
                mapboxNavigation.registerNavigationSessionStateObserver(navigationStateObserver)
                navigation.startTripSession()

                Log.d(TAG, "navigationObserver onAttached is completed.")
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                Log.d(TAG, "navigationObserver onDetached is called.")
                mapboxNavigation.stopTripSession()
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
                mapboxNavigation.unregisterRoutesPreviewObserver(routesPreviewObserver)
                mapboxNavigation.unregisterNavigationSessionStateObserver(navigationStateObserver)

                routeLineView.cancel()
                routeLineApi.cancel()
                Log.d(TAG, "navigationObserver onDetached is completed.")
            }
        }

    // locationObserver updates the location puck and camera to follow the user's location
    private val locationObserver =
        object : LocationObserver {
            lateinit var lastLocation: Location

            override fun onNewRawLocation(rawLocation: Location) {}

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                Log.d(TAG, "locationObserver onNewLocationMatcherResult is called.")

                val enhancedLocation = locationMatcherResult.enhancedLocation
                lastLocation = enhancedLocation

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

                if (_isCameraFollowingPosition.value) {
                    mapView.mapboxMap.easeTo(
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
                Log.d(TAG, "locationObserver onNewLocationMatcherResult is completed.")
            }
        }

    private val routesObserver =
        RoutesObserver { result ->
            if (result.navigationRoutes.isNotEmpty()) {
                // generate route geometries asynchronously and render them
                routeLineApi.setNavigationRoutes(result.navigationRoutes) { value ->
                    mapView.mapboxMap.style?.apply {
                        routeLineView.renderRouteDrawData(
                            this,
                            value
                        )
                    }
                }

                // update viewportSourceData to include the new route
                viewportDataSource.onRouteChanged(result.navigationRoutes.first())
                viewportDataSource.evaluate()

                // set the navigationCamera to OVERVIEW
                navigationCamera.requestNavigationCameraToOverview()
            }
        }

    private val routeProgressObserver =
        RouteProgressObserver { routeProgress ->
            routeProgress.currentState.let { currentState ->
                when (currentState) {
                    RouteProgressState.INITIALIZED -> Log.d(TAG, "R.drawable.your_drawable_for_initialized_state")
                    RouteProgressState.TRACKING -> Log.d(TAG, "R.drawable.your_drawable_for_tracking_state")
                    RouteProgressState.COMPLETE -> Log.d(TAG, "R.drawable.your_drawable_for_complete_state")
                    RouteProgressState.OFF_ROUTE -> Log.d(TAG, "R.drawable.your_drawable_for_off_route_state")
                    RouteProgressState.UNCERTAIN -> Log.d(TAG, "R.drawable.your_drawable_for_uncertain_state")
                }
            }
        }

    private val arrivalObserver = object : ArrivalObserver {
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {}
        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {}
        override fun onWaypointArrival(routeProgress: RouteProgress) {}
    }

    fun initializeMapView() {
        mapView = MapView(context, mapInitOptions = MapInitOptions(context))
//        mapView = MapView(context, mapInitOptions = MapInitOptions(context, styleUri = ""))

        // Initialize location puck using navigationLocationProvider as its data source
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.Companion.from(com.mapbox.navigation.ui.components.R.drawable.mapbox_navigation_puck_icon)
            )
            puckBearingEnabled = true
            enabled = true
        }

        mapView.scalebar.updateSettings {
            position = Gravity.BOTTOM or Gravity.START
            borderWidth = 4f
            marginBottom = 80f
        }

        mapView.compass.updateSettings {
            position = Gravity.TOP or Gravity.END
            marginTop = 100f
            marginRight = 50f
            clickable = true
        }

        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(139.7644865, 35.6811398))
                .zoom(14.0)
                .build()
        )

        mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMove(detector: MoveGestureDetector): Boolean { return false }
            override fun onMoveBegin(detector: MoveGestureDetector) { toggleCameraFollowingPosition(false) }
            override fun onMoveEnd(detector: MoveGestureDetector) {}
        })
    }

    fun lifecycleOnResume(owner: LifecycleOwner) {
        MapboxNavigationApp.attach(owner)
        MapboxNavigationApp.registerObserver(navigationObserver)
        lifecycleScope = owner.lifecycle
    }

    fun lifecycleOnPause(owner: LifecycleOwner) {
        MapboxNavigationApp.detach(owner)
        MapboxNavigationApp.registerObserver(navigationObserver)
    }

    fun toggleCameraFollowingPosition(isFollowing: Boolean) {
        _isCameraFollowingPosition.value = isFollowing
    }

    fun routePreviewRequest(destination: Point) {
        Log.d(TAG, "routeSearchOnClick is called.")
        val lastLocation = locationObserver.lastLocation
        navigationRoutePreviewRequest(listOf(
            Point.fromLngLat(lastLocation.longitude, lastLocation.latitude),
            destination
        ))
        Log.d(TAG, "routeSearchOnClick is finished.")
    }

    fun routePreviewClose() {
        navigation.setRoutesPreview(emptyList())
        routeLineApi.clearRouteLine { expect ->
            routeLineView.renderClearRouteLineValue(
                mapView.mapboxMap.style!!,
                expect
            )
            toggleCameraFollowingPosition(true)
        }
        Log.d(TAG, "routePreviewClose is called. ${navigation.getRoutesPreview()}")
    }

    fun startNavigationWithoutRoutePreview(destination: Point) {
        Log.d(TAG, "startNavigation is called.")
        val lastLocation = locationObserver.lastLocation
        navigationRoutePreviewRequest(listOf(
            Point.fromLngLat(lastLocation.longitude, lastLocation.latitude),
            destination
        ), true)
        toggleCameraFollowingPosition(true)
    }

    fun startNavigation() {
        Log.d(TAG, "startNavigation is called.")
        val routes = navigation.getRoutesPreview()
        if (routes != null) {
            navigation.setNavigationRoutes(routes.routesList)
            toggleCameraFollowingPosition(true)
        } else {
            Log.d(TAG, "navigation.getNavigationRoutes is empty.")
        }
    }

    @OptIn(MapboxDelicateApi::class)
    private fun navigationRoutePreviewRequest(coordinates: List<Point>, startNavigation: Boolean = false) {
        Log.d(TAG, "navigationRoutePreviewRequest is called.")

        navigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(coordinates)
                .layersList(listOf(navigation.getZLevel(), null))
                .alternatives(true)
                .language("ja")
//                .continueStraight(false)
//                .steps(false)
                .build(),
            object: NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    Log.d(TAG, "navigationRoutePreviewRequest NavigationRouterCallback.onCanceled is called.")
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    reasons.forEach {
                        Log.d(TAG, "navigationRoutePreviewRequest NavigationRouterCallback.onFailure: ${it.message}")
                    }
                }

                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    Log.d(TAG, "navigationRoutePreviewRequest NavigationRouterCallback.onRoutesReady is called.")
                    Log.d(TAG, "routes size : ${routes.size}, routerOrigin : $routerOrigin")
                    routes.forEach {
                        Log.d(TAG, "route : $it")
                    }
                    if (routes.isNotEmpty()) {
                        routePreviewCamera(
                            originPoint = locationObserver.lastLocation.toPoint(),
                            destinationPoint = coordinates.last()
                        )

                        if (startNavigation) {
                            navigation.setNavigationRoutes(routes)
                            toggleCameraFollowingPosition(true)
                        } else {
                            navigation.setRoutesPreview(routes)
                        }
                        Log.d(TAG, "navigationRoutePreviewRequest NavigationRouterCallback.onRoutesReady is completed.")
                    }
                }
            }
        )
    }

    private val routesPreviewObserver: RoutesPreviewObserver = RoutesPreviewObserver { update ->
        val preview = update.routesPreview ?: return@RoutesPreviewObserver
//        updateRoutes(preview.routesList, preview.alternativesMetadata)
        lifecycleScope.coroutineScope.launch {
            routeLineApi.setNavigationRoutes(
                newRoutes = preview.routesList,
                alternativeRoutesMetadata = preview.alternativesMetadata,
            ).apply {
                routeLineView.renderRouteDrawData(
                    mapView.mapboxMap.style!!,
                    this
                )
            }
        }
    }

    private fun reorderRoutes(clickedRoute: NavigationRoute) {
        // if we clicked on some route callout that is not primary,
        // we make this route primary and all the others - alternative
        if (clickedRoute != routeLineApi.getPrimaryNavigationRoute()) {
            if (navigation.getRoutesPreview() == null) {
                val reOrderedRoutes = navigation.getNavigationRoutes()
                    .filter { clickedRoute.id != it.id }
                    .toMutableList()
                    .also { list ->
                        list.add(0, clickedRoute)
                    }
                navigation.setNavigationRoutes(reOrderedRoutes)
            } else {
                navigation.changeRoutesPreviewPrimaryRoute(clickedRoute)
            }
        }
    }

    @OptIn(MapboxDelicateApi::class)
    private fun routePreviewCamera(originPoint: Point, destinationPoint: Point) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        val overviewOption = mapView.mapboxMap.cameraForCoordinates(
            listOf(
                originPoint,
                destinationPoint
            ),
            CameraOptions.Builder()
                .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                .build(),
            null,
            null,
            null,
        )
        toggleCameraFollowingPosition(false)

        mapView.camera.easeTo(
            overviewOption,
            mapAnimationOptions
        )
    }
}