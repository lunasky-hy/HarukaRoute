package com.lunaskyhy.harukaroute.ui.freedrive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.theme.AppTheme

@Composable
fun FreeDriveScreen(
    modifier: Modifier = Modifier,
    mapController: HarukaMapController = MapControllerProvider.harukaMapController
) {
    val isCameraFollowingPosition = mapController.isCameraFollowingPosition.collectAsState()
    FreeDriveScreenLayout(
        isVisibleFollowCurrentLocation = !isCameraFollowingPosition.value,
        searchLocationOnClick = mapController::routeSearchOnClick,
        followCurrentLocationOnClick = { mapController.toggleCameraFollowingPosition(true) }
    )
}

@Composable
fun FreeDriveScreenLayout(
    modifier: Modifier = Modifier,
    isVisibleFollowCurrentLocation: Boolean = true,
    searchLocationOnClick: () -> Unit = {},
    followCurrentLocationOnClick: () -> Unit = {}
) { // Box全体を画面いっぱいに広げる
    Box(modifier = modifier.fillMaxSize()) {
        ActionButtonList(
            isVisibleFollowCurrentLocation = isVisibleFollowCurrentLocation,
            searchLocationOnClick = searchLocationOnClick,
            followCurrentLocationOnClick = followCurrentLocationOnClick
        )
    }
}

@Composable
fun ActionButtonList(
    modifier: Modifier = Modifier,
    isVisibleFollowCurrentLocation: Boolean = true,
    searchLocationOnClick: () -> Unit,
    followCurrentLocationOnClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
        ) {
            SearchLocationButton(onClick = searchLocationOnClick)
            if (isVisibleFollowCurrentLocation)
                FollowCurrentLocation(onClick = followCurrentLocationOnClick)
        }
    }
}

@Composable
fun FollowCurrentLocation(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
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
        modifier = modifier// 必要に応じてmodifierを適用
    ) {
        Icon(
            Icons.Filled.Search,
            modifier = Modifier,
            contentDescription = "Search"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FreeDriveScreenPreview() {
    AppTheme() {
        FreeDriveScreenLayout()
    }
}