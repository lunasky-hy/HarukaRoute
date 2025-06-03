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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lunaskyhy.harukaroute.map.MapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.component.FloatingButton
import com.lunaskyhy.harukaroute.ui.theme.AppTheme

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var mapController: MapController

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
                mapController.lifecycleOnResume(owner)
                Log.d(TAG, "lifecycle onResume is completed.")
            }

            override fun onPause(owner: LifecycleOwner) {
                Log.d(TAG, "lifecycle onPause is started.")
                mapController.lifecycleOnPause(owner)
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
            mapController = MapControllerProvider.mapController
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
                    mapController = MapController(context = applicationContext)
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
                Scaffold(
                    topBar = {},
                    bottomBar = {},
                    floatingActionButton = {
                        FloatingButton(onClick = {
                            mapController.routeSearchOnClick()
                        })
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(
                        top = 0.dp,
                        bottom = paddingValues.calculateBottomPadding(),
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr))
                    ) {
                        AndroidView(
                            factory = { mapController.mapView },
                            modifier = Modifier.fillMaxSize()
                        )
                        Card(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
                            .padding(top = 32.dp)
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(text = "検索...", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

