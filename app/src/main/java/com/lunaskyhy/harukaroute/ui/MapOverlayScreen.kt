package com.lunaskyhy.harukaroute.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.freedrive.FreeDriveScreen

@Composable
fun MapOverlayScreen(
    modifier: Modifier = Modifier,
    harukaMapController: HarukaMapController = MapControllerProvider.harukaMapController
) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {},
        bottomBar = {},
        floatingActionButton = {}
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(
            top = 0.dp,
            bottom = 0.dp,
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)),
        ) {
            AndroidView(
                factory = { harukaMapController.mapView },
                modifier = Modifier.fillMaxSize()
            )
            NavHost(
                navController = navController,
                startDestination = NavRoute.Free.name,
                modifier = modifier.padding(paddingValues).padding(24.dp),
            ) {
                composable(NavRoute.Free.name) {
                    FreeDriveScreen(modifier = modifier)
                }
            }
        }
    }
}

enum class NavRoute {
    Free,
    Search,
    Route,
    Navigate,
}