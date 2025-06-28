package com.lunaskyhy.harukaroute.map

import android.location.Location
import com.google.android.gms.maps.LocationSource

class MapLocationSource: LocationSource {
    private var listener: LocationSource.OnLocationChangedListener? = null

    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        this.listener = p0
    }

    override fun deactivate() {
        this.listener = null
    }

    fun onLocationChanged(location: Location) {
        listener?.onLocationChanged(location)
    }
}