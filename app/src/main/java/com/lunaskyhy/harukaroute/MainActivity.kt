package com.lunaskyhy.harukaroute

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.map.MapPlaces
import com.lunaskyhy.harukaroute.map.MapScreen
import com.lunaskyhy.harukaroute.map.NavigationViewModel
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.OverlayNavigation
import com.lunaskyhy.harukaroute.ui.theme.AppTheme


private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()

        if (checkLocationPermission()) {
            render()
        } else {
            requestLocationPermission()
        }
    }

    private fun render() {

        setContent {
            val mapViewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory)

            AppTheme {
                Scaffold(
                    topBar = {},
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MapScreen(mapViewModel)
                        OverlayNavigation(mapViewModel = mapViewModel)
                    }
                }
            }
        }
    }

    private fun renderLocationPermissionDeniedDisplay() {
        setContent {
            Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                Column {
                    Text("位置情報が許可されていません。")
                    Text("このアプリは位置情報が必須です。")
                    Button(onClick = { requestLocationPermission() }) {
                        Text("許可する")
                    }
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                render()
            } else {
                renderLocationPermissionDeniedDisplay()
            }
        }
    }
}