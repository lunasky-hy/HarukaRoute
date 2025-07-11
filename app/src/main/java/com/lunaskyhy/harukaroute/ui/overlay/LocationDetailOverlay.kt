package com.lunaskyhy.harukaroute.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.LocationDetailUiState
import com.lunaskyhy.harukaroute.map.NavigationViewModel
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.lunaskyhy.harukaroute.ui.theme.AppTypography

@Composable
fun LocationDetailOverlay(
    mapViewModel: NavigationViewModel,
    backNavigate: () -> Unit = {},
) {
    val uiState by mapViewModel.locationDetailUiState.collectAsStateWithLifecycle()

    LocationDetailOverlayLayout(
        uiState = uiState,
        closeOnClick = backNavigate,
    )
}

@Composable
fun LocationDetailOverlayLayout(
    modifier: Modifier = Modifier,
    uiState: LocationDetailUiState = LocationDetailUiState.PlaceUnselected,
    closeOnClick: () -> Unit = {},
    startNaviOnClick: () -> Unit = {},
    routePreviewOnClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Card(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_small))
                .fillMaxWidth(),
            colors = CardColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            when (uiState) {
                is LocationDetailUiState.Success -> {
                    LocationDetailSuccess(
                        locationPrimaryText = uiState.place?.shortFormattedAddress ?: "",
                        locationSecondaryText = uiState.place?.resourceName ?: "",
                        closeOnClick = closeOnClick,
                        startNaviOnClick = startNaviOnClick,
                        routePreviewOnClick = routePreviewOnClick,
                    )
                }
                is LocationDetailUiState.Error -> {
                    Text(uiState.exception)
                }
                is LocationDetailUiState.Loading -> {
                    Text("読み込み中...")
                }
                LocationDetailUiState.PlaceUnselected -> {}
            }
        }
    }
}

@Composable
fun LocationDetailSuccess(
    locationPrimaryText: String,
    locationSecondaryText: String,
    closeOnClick: () -> Unit = {},
    startNaviOnClick: () -> Unit = {},
    routePreviewOnClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
    ) {
        Row {
            Text(locationPrimaryText, style = AppTypography.titleMedium, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clickable { closeOnClick() }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(dimensionResource(R.dimen.place_autocomplete_icon_container_size)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Close,
                    "Place Auto complete result",
                    modifier = Modifier.size(dimensionResource(R.dimen.place_autocomplete_icon_size))
                )
            }
        }
        Text(locationSecondaryText, style = AppTypography.bodySmall)
        ActionButtonGroup(
            startNaviOnClick = startNaviOnClick,
            routePreviewOnClick = routePreviewOnClick,
        )
    }

}

@Composable
fun ActionButtonGroup(
    modifier: Modifier = Modifier,
    startNaviOnClick: () -> Unit,
    routePreviewOnClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_medium)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
    ) {
        ActionButton(
            Icons.Default.Navigation,
            stringResource(R.string.navigation_start_button_label),
            stringResource(R.string.route_preview_button_description),
            colors = ButtonDefaults.buttonColors(),
            onClick = startNaviOnClick,
        )
        ActionButton(
            Icons.Default.Directions,
            stringResource(R.string.route_preview_button_label),
            stringResource(R.string.route_preview_button_description),
            colors = ButtonDefaults.filledTonalButtonColors(),
            onClick = routePreviewOnClick,
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    Button(
        modifier = Modifier.height(dimensionResource(R.dimen.action_button_height)),
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.action_button_padding_value),
            vertical = dimensionResource(R.dimen.button_content_padding_default)
        ),
        colors = colors,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Icon(
                icon,
                contentDescription,
                modifier = Modifier.size(dimensionResource(R.dimen.action_button_icon_size))
            )
            Text(text, style = AppTypography.bodySmall)
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xf7f0da)
@Composable
fun LocationDetailOverlayPreview() {
    AppTheme {
        LocationDetailOverlayLayout(
        )
    }
}