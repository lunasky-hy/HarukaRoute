package com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    isCameraFollowingPosition: Boolean = false,
    followCameraOnClick: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FollowCurrentLocation(
            isFollowing = isCameraFollowingPosition,
            onClick = followCameraOnClick
        )
    }
}

@Composable
fun FollowCurrentLocation(
    modifier: Modifier = Modifier,
    isFollowing: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        containerColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier // 必要に応じてmodifierを適用
    ) {
        if (isFollowing) {
            Icon(
                Icons.Filled.LocationSearching,
                modifier = Modifier,
                contentDescription = "Follow Location"
            )
        } else {
            Icon(
                Icons.Filled.MyLocation,
                modifier = Modifier,
                contentDescription = "Followed Location"
            )
        }
    }
}