package com.lunaskyhy.harukaroute.ui.overlay

import android.text.style.UnderlineSpan
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.MyNavigationViewModel
import com.lunaskyhy.harukaroute.tool.toAnnotatedString
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.overlay.viewmodel.LocationAutocompleteUiState
import com.lunaskyhy.harukaroute.ui.overlay.viewmodel.SearchLocationViewModel
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.lunaskyhy.harukaroute.ui.theme.AppTypography

@Composable
fun SearchLocationOverlayScreen(
    mapViewModel: MyNavigationViewModel,
    viewModel: SearchLocationViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backNavigate: () -> Unit = {},
    navigateToLocationDetail: () -> Unit = {},
) {
    val query by viewModel.query.collectAsState()
    val autoComplete by viewModel.searchUiState.collectAsStateWithLifecycle()
    val currentLocation by mapViewModel.currentLocationState.collectAsStateWithLifecycle()

    val onSelectLocation = { prediction: AutocompletePrediction ->
        mapViewModel.setLocationDetail(prediction.placeId, viewModel.placeSessionToken)
        navigateToLocationDetail()
    }

    val searchQueryOnChange: (String) -> Unit = { q ->
        viewModel.updateSearchQuery(q, currentLocation.lastKnownLocation)
    }

    SearchLocationOverlayLayout(
        searchQuery = query,
        searchQueryOnChange = searchQueryOnChange,
        autocompleteUiState = autoComplete,
        backNavigate = backNavigate,
        onSelectLocation = onSelectLocation,
    )
}

@Composable
private fun SearchLocationOverlayLayout(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchQueryOnChange: (String) -> Unit = {},
    autocompleteUiState: LocationAutocompleteUiState = LocationAutocompleteUiState.Empty,
    backNavigate: () -> Unit = {},
    onSelectLocation: (AutocompletePrediction) -> Unit = {},
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(R.dimen.padding_large))
            .padding(top = dimensionResource(R.dimen.map_overlay_search_box_padding_top)),
    ) {
        LocationQueryField(
            searchQuery = searchQuery,
            searchQueryOnChange = searchQueryOnChange,
            backNavigate = backNavigate,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_small))
                .background(color = MaterialTheme.colorScheme.background)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            colors = CardColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
        ) {

            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                LocationSearchAutocomplete(
                    autocompleteUiState = autocompleteUiState,
                    onClick = onSelectLocation,
                )
                SearchHistory()
            }
        }
    }
}

@Composable
fun LocationQueryField(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchQueryOnChange: (String) -> Unit = {},
    backNavigate: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
        Row(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_medium))
                .padding(horizontal = dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Location Search",
                modifier = Modifier.clickable { backNavigate() }
            )
            BasicTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                value = searchQuery,
                onValueChange = searchQueryOnChange,
                textStyle = AppTypography.bodyLarge,
                singleLine = true,
            ) { innerTextField ->
                innerTextField()
            }
        }
    }
}

@Composable
fun LocationSearchAutocomplete(
    autocompleteUiState: LocationAutocompleteUiState = LocationAutocompleteUiState.Loading,
    onClick: (AutocompletePrediction) -> Unit = {},
) {
    when (autocompleteUiState) {
        is LocationAutocompleteUiState.Success -> {
            LazyColumn {
                items(autocompleteUiState.predictions) {
                    Column {
                        Row(
                            modifier = Modifier
                                .clickable { onClick(it) }
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_medium)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .size(dimensionResource(R.dimen.place_autocomplete_icon_container_size)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Place,
                                    "Place Auto complete result",
                                    modifier = Modifier.size(dimensionResource(R.dimen.place_autocomplete_icon_size))
                                )
                            }
                            Column {
                                Text(
                                    it.getPrimaryText(UnderlineSpan())
                                        .toAnnotatedString(MaterialTheme.colorScheme.onSurface),
                                    style = AppTypography.bodyLarge
                                )
                                Text(
                                    it.getSecondaryText(UnderlineSpan())
                                        .toAnnotatedString(MaterialTheme.colorScheme.onSurface),
                                    style = AppTypography.bodySmall
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        is LocationAutocompleteUiState.Error -> {
            Text(
                "エラー" + autocompleteUiState.exception,
                style = AppTypography.labelMedium,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
            )
        }

        is LocationAutocompleteUiState.Loading -> {
            Text(
                "検索中...",
                style = AppTypography.labelMedium,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
            )
        }

        is LocationAutocompleteUiState.Empty -> {}
    }
}

@Composable
fun SearchHistory() {
    Row(modifier = Modifier) {
        Text(
            "検索履歴",
            style = AppTypography.labelMedium,
        )
    }
    Box(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_medium))) {
        Text(
            "検索履歴はありません。",
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchLocationOverlayPreview() {
    AppTheme {
        SearchLocationOverlayLayout(modifier = Modifier.fillMaxSize())
    }
}