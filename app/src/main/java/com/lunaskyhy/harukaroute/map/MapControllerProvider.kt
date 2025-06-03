package com.lunaskyhy.harukaroute.map

import android.content.Context

object MapControllerProvider {

    private lateinit var _mapController: MapController
    val mapController: MapController
        get() {
            check(:: _mapController.isInitialized) {
                "MapControllerProvider not initialized."
            }
            return _mapController
        }

    fun initialize(context: Context) {
        if(!:: _mapController.isInitialized) {
            _mapController = MapController(context)
        }
    }
}