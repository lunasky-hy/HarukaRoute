package com.lunaskyhy.harukaroute.data.repos

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface LocationRepository {
    /**
     * 位置情報の更新をFlowとして提供する.
     * このFlowをcollectすると位置情報の取得が開始され, scopeがキャンセルされると停止する.
     *
     * @return LocationのFlow
     * @throws SecurityException 位置情報のパーミッションがない場合にスローされる可能性がある
     */
    fun getLocationUpdates(): Flow<Location>
}

class DeviceLocationRepository(
    private val locationProviderClient: FusedLocationProviderClient
): LocationRepository {
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> {
        return callbackFlow {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L
            ).apply {
                setMinUpdateIntervalMillis(1000L)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations.forEach { location ->
                        trySend(location).isSuccess
                    }
                }
            }

            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                locationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}