package com.lunaskyhy.harukaroute.ui.screen.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.screen.navigation.freedrive.FreeDriveLayout
import com.lunaskyhy.harukaroute.ui.screen.navigation.guidance.ActiveGuidanceLayout
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.mapbox.navigation.core.trip.session.NavigationSessionState

@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory),
    mapController: HarukaMapController = MapControllerProvider.harukaMapController
) {
    val navigationState = mapController.navigationState.collectAsState()

    NavigationScreenLayout(
        navigationState = navigationState.value,
    )
}

@Composable
fun NavigationScreenLayout(
    modifier: Modifier = Modifier,
    navigationState: NavigationSessionState = NavigationSessionState.Idle,
) { // Box全体を画面いっぱいに広げる
    Box(modifier = modifier.fillMaxSize()) {
        when(navigationState) {
            is NavigationSessionState.ActiveGuidance -> ActiveGuidanceLayout()
            is NavigationSessionState.FreeDrive -> FreeDriveLayout()
            NavigationSessionState.Idle -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FreeDriveScreenPreview() {
    AppTheme() {
        NavigationScreenLayout()
    }
}