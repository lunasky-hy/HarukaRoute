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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.screen.navigation.NavigationScreenViewModel

@Composable
fun PreviewRouteOverlay(
    modifier: Modifier = Modifier,
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory)
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
        ) {
            CloseRoutePreviewButton(onClick = viewModel::previewRouteClose)
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