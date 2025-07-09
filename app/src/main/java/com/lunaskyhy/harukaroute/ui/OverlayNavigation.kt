package com.lunaskyhy.harukaroute.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lunaskyhy.harukaroute.map.NavigationViewModel
import com.lunaskyhy.harukaroute.ui.overlay.FreeDriveOverlayScreen
import com.lunaskyhy.harukaroute.ui.overlay.SearchLocationOverlayScreen

@Composable
fun OverlayNavigation(
    modifier: Modifier = Modifier,
    mapViewModel: NavigationViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = OverlayRoutePath.FREE_DRIVE.name) {
        composable(OverlayRoutePath.FREE_DRIVE.name) {
            FreeDriveOverlayScreen(mapViewModel, toSearchLocation = { navController.navigate(OverlayRoutePath.SEARCH_LOCATION.name) })
        }
        composable(OverlayRoutePath.SEARCH_LOCATION.name) {
            SearchLocationOverlayScreen(mapViewModel)
        }
    }
}

private enum class OverlayRoutePath {
    FREE_DRIVE,
    SEARCH_LOCATION,
    ROUTE_PLAN,
    NAVIGATION_ROUTE,
}

//private sealed class OverlayRoute(val route: String) {
//    object FREE_DRIVE : OverlayRoute(route = OverlayRoutePath.FREE_DRIVE.name)
//    object SEARCH_LOCATION : OverlayRoute(route = OverlayRoutePath.SEARCH_LOCATION.name)
//    object ROUTE_PLAN : OverlayRoute(route = OverlayRoutePath.ROUTE_PLAN.name)
//    object NAVIGATION_ROUTE : OverlayRoute(route = OverlayRoutePath.NAVIGATION_ROUTE.name)
//}