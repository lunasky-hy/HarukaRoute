package com.lunaskyhy.harukaroute.map

import android.annotation.SuppressLint
import android.content.Context

object MapControllerProvider {

    @SuppressLint("StaticFieldLeak")
    private lateinit var _Haruka_mapController: HarukaMapController
    val harukaMapController: HarukaMapController
        get() {
            check(:: _Haruka_mapController.isInitialized) {
                "MapControllerProvider not initialized."
            }
            return _Haruka_mapController
        }

    fun initialize(context: Context) {
        if(!:: _Haruka_mapController.isInitialized) {
            _Haruka_mapController = HarukaMapController(context)
        }
    }
}