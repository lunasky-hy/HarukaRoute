package com.lunaskyhy.harukaroute.ui.screen.navigation.freedrive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.screen.navigation.NavigationScreenViewModel
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.ActionButtons
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.PlaceDetailOverlay
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.PreviewRouteOverlay
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.SearchPlaceOverlay

@Composable
fun FreeDriveLayout(
    modifier: Modifier = Modifier,
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory),
    mapController: HarukaMapController = MapControllerProvider.harukaMapController
) {
    val uiState = viewModel.uiState.collectAsState()
    val isCameraFollowingPosition = mapController.isCameraFollowingPosition.collectAsState()

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        if (uiState.value.previewRouteSuggestion != null) {
            PreviewRouteOverlay()
        } else if (uiState.value.selectedSuggestion != null) {
            PlaceDetailOverlay()
        } else if (uiState.value.isSearchActive) {
            SearchPlaceOverlay()
        } else {
            ActionButtons(
                isCameraFollowingPosition = !isCameraFollowingPosition.value,
                toggleSearchActive = viewModel::toggleSearchActive,
                followCameraOnClick = { mapController.toggleCameraFollowingPosition(true) }
            )
        }
    }
}