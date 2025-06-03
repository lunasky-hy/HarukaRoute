package com.lunaskyhy.harukaroute.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.lunaskyhy.harukaroute.TAG
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.IncidentsOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteProgressState
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.preview.RoutesPreviewObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class HarukaMapController (
    private val context: Context,
){
    var mapView: MapView = MapView(context, mapInitOptions = MapInitOptions(context))
        private set

    private lateinit var lifecycleScope: Lifecycle
    private lateinit var navigation: MapboxNavigation
    private var viewportDataSource: MapboxNavigationViewportDataSource
    private var navigationCamera: NavigationCamera
    private val navigationLocationProvider = NavigationLocationProvider()
    private var isTrackingFlag: Boolean = true

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

    init {
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(context)
                    .enableSensors(true)
                    .incidentsOptions(IncidentsOptions.Builder().build())
                    .build()
            }
        }

        // Initialize location puck using navigationLocationProvider as its data source
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = createDefault2DPuck()
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

    fun lifecycleOnResume(owner: LifecycleOwner) {
        MapboxNavigationApp.attach(owner)
        MapboxNavigationApp.registerObserver(navigationObserver)
        lifecycleScope = owner.lifecycle
    }

    fun lifecycleOnPause(owner: LifecycleOwner) {
        MapboxNavigationApp.detach(owner)
        MapboxNavigationApp.registerObserver(navigationObserver)
    }

    fun routeSearchOnClick() {
        Log.d(TAG, "routeSearchOnClick is called.")
        val lastLocation = locationObserver.lastLocation
        navigationRoutePreviewRequest(listOf(
            Point.fromLngLat(lastLocation.longitude, lastLocation.latitude),
            Point.fromLngLat(139.6937075, 35.6820377)
        ))
        Log.d(TAG, "routeSearchOnClick is finished.")
    }

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
                mapboxNavigation.registerRoutesPreviewObserver(routesPreviewObserver)

                mapboxNavigation.startTripSession()

                Log.d(TAG, "navigationObserver onAttached is completed.")
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                Log.d(TAG, "navigationObserver onDetached is called.")
                mapboxNavigation.stopTripSession()
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRoutesPreviewObserver(routesPreviewObserver)

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

                if (isTrackingFlag) {
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
                mapView.location.apply {
                    this.locationPuck = LocationPuck2D()
                }
            }
        }

    @OptIn(MapboxDelicateApi::class)
    private fun navigationRoutePreviewRequest(coordinates: List<Point>) {
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
                        isTrackingFlag = false
                        val overviewOption = mapView.mapboxMap
                            .cameraForCoordinates(
                                coordinates,
                                CameraOptions.Builder()
                                    .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                                    .build(),
                                null,
                                null,
                                null
                            )
                        val animOption = MapAnimationOptions.Builder().duration(1500L).build()

                        mapView.camera.easeTo(overviewOption, animOption)

                        navigation.setRoutesPreview(routes)
//                        navigation.setNavigationRoutes(routes)
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

}