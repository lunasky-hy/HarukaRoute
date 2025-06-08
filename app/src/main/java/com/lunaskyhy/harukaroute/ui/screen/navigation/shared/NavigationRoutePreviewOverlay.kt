package com.lunaskyhy.harukaroute.ui.screen.navigation.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.screen.navigation.NavigationScreenViewModel

@Composable
fun NavigationRoutePreviewOverlay(
    modifier: Modifier = Modifier,
    mapController: HarukaMapController = MapControllerProvider.harukaMapController,
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory)
) {
    val uiState = viewModel.uiState.collectAsState()

    val onCancel = {
        viewModel.previewRouteClose()
        viewModel.unselectDetailSuggestion()
    }

    NavigationRoutePreviewLayout(
        modifier = modifier,
        placeName = uiState.value.selectedSuggestion!!.name,
        address = uiState.value.selectedSuggestion?.address?.formattedAddress ?: "",
        distance = uiState.value.selectedSuggestion?.distanceMeters,
        etaMinutes = uiState.value.selectedSuggestion?.etaMinutes?.toInt(),
        onNavigate = { mapController.startNavigation() },
        onCancel = onCancel,
    )
}


@Composable
fun NavigationRoutePreviewLayout(
    modifier: Modifier = Modifier,
    placeName: String = "",
    address: String = "",
    distance: Double? = null,
    etaMinutes: Int? = null,
    onNavigate: () -> Unit = {},
    onCancel: () -> Unit = {},
) {

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier, contentAlignment = Alignment.TopEnd) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
            ) {
                CloseRoutePreviewButton(onClick = onCancel)
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
            PlaceDetail(
                placeName = placeName,
                address = address,
                distance = distance,
                etaMinutes = etaMinutes,
                onNavigate = onNavigate,
                onCancel = onCancel
            )
        }
    }
}


@Composable
fun CloseRoutePreviewButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier // 必要に応じてmodifierを適用
    ) {
        Icon(
            Icons.Filled.Close,
            modifier = Modifier,
            contentDescription = "Close Preview"
        )
    }
}