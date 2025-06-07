package com.lunaskyhy.harukaroute.ui.screen.navigation.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.lunaskyhy.harukaroute.R


@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    isCameraFollowingPosition: Boolean = false,
    toggleSearchActive: () -> Unit = {},
    followCameraOnClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
    ) {
        SearchLocationButton(onClick = toggleSearchActive)

        if (isCameraFollowingPosition){
            FollowCurrentLocation(onClick = followCameraOnClick)
        }
    }
}

@Composable
fun FollowCurrentLocation(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier // 必要に応じてmodifierを適用
    ) {
        Icon(
            Icons.Filled.Place,
            modifier = Modifier,
            contentDescription = "Search"
        )
    }
}

@Composable
fun SearchLocationButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier// 必要に応じてmodifierを適用
    ) {
        Icon(
            Icons.Filled.Search,
            modifier = Modifier,
            contentDescription = "Search"
        )
    }
}