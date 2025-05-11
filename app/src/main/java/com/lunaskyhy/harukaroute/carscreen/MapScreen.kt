package com.lunaskyhy.harukaroute.carscreen

import android.graphics.Color
import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MapTemplate
import androidx.car.app.navigation.model.NavigationTemplate

class MapScreen(carContext: CarContext): Screen(carContext) {

    private val surfaceCallback = object: SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            val surface = surfaceContainer.surface
            val canvas = surface?.lockCanvas(null)
            canvas?.drawColor(Color.BLUE)
            surface?.unlockCanvasAndPost(canvas)
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {}
        override fun onVisibleAreaChanged(visibleArea: Rect) {}
        override fun onStableAreaChanged(stableArea: Rect) {}
    }

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)
    }

    override fun onGetTemplate(): Template {
        val mapActionStrip = ActionStrip.Builder()
                .addAction(
                    Action.Builder()
                        .setTitle("TestAction")
                        .build()
                ).build()

        return NavigationTemplate.Builder()
            .setActionStrip(mapActionStrip)
            .build()
    }
}