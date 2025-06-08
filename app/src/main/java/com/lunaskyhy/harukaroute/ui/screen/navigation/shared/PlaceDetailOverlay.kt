package com.lunaskyhy.harukaroute.ui.screen.navigation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.screen.navigation.NavigationScreenViewModel
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component.DisplayDistance
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component.DisplayEta
import com.lunaskyhy.harukaroute.ui.theme.AppTheme

@Composable
fun PlaceDetailOverlay(
    modifier: Modifier = Modifier,
    mapController: HarukaMapController = MapControllerProvider.harukaMapController,
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory)
) {
    val uiState = viewModel.uiState.collectAsState()

    PlaceDetailLayout(
        modifier = modifier,
        placeName = uiState.value.selectedSuggestion!!.name,
        address = uiState.value.selectedSuggestion?.address?.formattedAddress ?: "",
        distance = uiState.value.selectedSuggestion?.distanceMeters,
        etaMinutes = uiState.value.selectedSuggestion?.etaMinutes?.toInt(),
        onNavigate = { viewModel.startNavigationWithoutPreview(uiState.value.selectedSuggestion!!) },
        onPreviewRoute = { viewModel.previewRouteSuggestion(uiState.value.selectedSuggestion!!) },
        onCancel = viewModel::unselectDetailSuggestion,
    )
}

@Composable
fun PlaceDetailLayout(
    modifier: Modifier = Modifier,
    placeName: String = "",
    address: String = "",
    distance: Double? = null,
    etaMinutes: Int? = null,
    onNavigate: () -> Unit = {},
    onPreviewRoute: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    Column(modifier = modifier
        .padding(dimensionResource(R.dimen.padding_medium))
        .fillMaxWidth()) {
        Card {
            Row(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.clickable { onCancel() })
                Text(text = placeName, modifier = Modifier.weight(1f))
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
            PlaceDetail(
                placeName = placeName,
                address = address,
                distance = distance,
                etaMinutes = etaMinutes,
                onNavigate = onNavigate,
                onPreviewRoute = onPreviewRoute,
                onCancel = onCancel
            )
        }
    }
}

@Composable
fun PlaceDetail(
    modifier: Modifier = Modifier,
    placeName: String = "",
    address: String = "",
    distance: Double? = null,
    etaMinutes: Int? = null,
    onNavigate: () -> Unit = {},
    onPreviewRoute: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium_large))) {
            Text(text = placeName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = address, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small))
            )
            Row {
                if (distance != null) {
                    DisplayDistance(distance = distance, modifier = Modifier.weight(1f))
                }
                if (etaMinutes != null)
                    DisplayEta(etaMinutes = etaMinutes)
            }
            Row(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_small))
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Button(
                        onClick = onNavigate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.navigation_start_button_label))
                    }
                    Button(
                        onClick = onPreviewRoute,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(stringResource(R.string.route_preview_button_label))
                    }

                }
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(stringResource(R.string.place_preview_cancel_button_label))
                }
            }
        }
    }
}

@Preview
@Composable
fun PlaceDetailPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_small))
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            PlaceDetailLayout(
                placeName = "渋谷駅",
                address = "東京都千代田区外神田１－１７, 千代田区, Tokyo 101-0021, 日本",
                distance = 1241.2
            )
        }
    }
}