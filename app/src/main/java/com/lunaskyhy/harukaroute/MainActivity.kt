package com.lunaskyhy.harukaroute

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.MapOverlayScreen
import com.lunaskyhy.harukaroute.ui.theme.AppTheme

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val harukaMapController: HarukaMapController = MapControllerProvider.harukaMapController

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
                harukaMapController.lifecycleOnResume(owner)
                Log.d(TAG, "lifecycle onResume is completed.")
            }

            override fun onPause(owner: LifecycleOwner) {
                Log.d(TAG, "lifecycle onPause is started.")
                harukaMapController.lifecycleOnPause(owner)
                Log.d(TAG, "lifecycle onPause is completed.")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Activity onCreate is started.")
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        // check/request location permissions
        if (
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are already granted
            renderMap()
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
            val permissionText = "Location permissions denied. Please enable permissions in settings."

            when {
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    renderMap()
                }
                else -> {
                    Toast.makeText(this, permissionText, Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun renderMap() {

        enableEdgeToEdge()
        setContent {
            AppTheme {
                MapOverlayScreen()
            }
        }
    }
}

