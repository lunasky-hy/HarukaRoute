package com.lunaskyhy.harukaroute.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.navigation.ui.androidauto.notification.MapboxCarNotificationOptions
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens

class MainCarSession: Session() {
    private var mapboxCarMap: MapboxCarMap = MapboxCarMap()
    private val mapboxCarContext = MapboxCarContext(lifecycle, mapboxCarMap)

    init {
        MapboxNavigationApp.attach(lifecycleOwner = this)
        mapboxCarContext.prepareScreens()

        mapboxCarContext.customize {
            notificationOptions = MapboxCarNotificationOptions.Builder()
                .startAppService(MainCarAppService::class.java)
                .build()
        }

        lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                if (!MapboxNavigationApp.isSetup()) {
                    MapboxNavigationApp.setup(
                        NavigationOptions.Builder(carContext)
                            .build()
                    )
                }

                mapboxCarMap.setup(carContext, MapInitOptions(context = carContext))
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapboxCarMap.clearObservers()
            }
        })
    }

    override fun onCreateScreen(intent: Intent): Screen {
        val firstScreenKey = if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            MapboxScreenManager.current()?.key ?: MapboxScreen.FREE_DRIVE
        } else {
            MapboxScreen.NEEDS_LOCATION_PERMISSION
        }

        return mapboxCarContext.mapboxScreenManager.createScreen(firstScreenKey)
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
    }
}

