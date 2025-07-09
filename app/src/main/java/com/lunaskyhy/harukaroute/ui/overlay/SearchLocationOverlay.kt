package com.lunaskyhy.harukaroute.ui.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.NavigationViewModel
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.overlay.viewmodel.SearchLocationViewModel
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.lunaskyhy.harukaroute.ui.theme.AppTypography

@Composable
fun SearchLocationOverlayScreen(
    mapViewModel: NavigationViewModel,
    viewModel: SearchLocationViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    SearchLocationOverlayLayout(
        searchText = viewModel.searchText,
        searchTextOnChange = viewModel::updateSearchText,
    )
}

@Composable
private fun SearchLocationOverlayLayout(
    modifier: Modifier = Modifier,
    searchText: String = "",
    searchTextOnChange: (String) -> Unit = {},
) {

    Box(modifier = modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
                .padding(top = dimensionResource(R.dimen.map_overlay_search_history_padding_top))
                .background(color = MaterialTheme.colorScheme.background)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        ) {

            Column(
                modifier = Modifier.padding(
                    vertical = dimensionResource(R.dimen.padding_large),
                    horizontal = dimensionResource(R.dimen.padding_medium)
                ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Row(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large))) {
                    Text(
                        "検索履歴",
                        style = AppTypography.labelMedium,
                    )
                }

                Text(
                    "検索履歴はありません。",
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
                .padding(top = dimensionResource(R.dimen.map_overlay_search_box_padding_top)),
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
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchText,
                onValueChange = searchTextOnChange,
                textStyle = AppTypography.bodyLarge,
                singleLine = true,
            ) { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .padding(horizontal = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Location Search"
                    )
                    innerTextField()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchLocationOverlayPreview() {
    AppTheme {
        SearchLocationOverlayLayout()
    }
}