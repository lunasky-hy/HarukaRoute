package com.lunaskyhy.harukaroute.ui.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.NavigationViewModel
import com.lunaskyhy.harukaroute.ui.theme.AppTheme

@Composable
fun FreeDriveOverlayScreen(
    mapViewModel: NavigationViewModel,
    toSearchLocation: () -> Unit = {},
) {
    FreeDriveOverlayLayout(toSearchLocation = toSearchLocation)
}

@Composable
private fun FreeDriveOverlayLayout(
    modifier: Modifier = Modifier,
    searchText: String = "",
    toSearchLocation: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Card(modifier = Modifier.fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_large))
            .padding(top = dimensionResource(R.dimen.map_overlay_search_box_padding_top))
            .clickable { toSearchLocation() },
            shape = CircleShape,
            colors = CardColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
                .padding(horizontal = dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Icon(Icons.Default.Search, contentDescription = "Location Search")
                Text("場所を検索...")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FreeDriveOverlayPreview() {
    AppTheme {
        FreeDriveOverlayLayout()
    }
}