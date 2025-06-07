package com.lunaskyhy.harukaroute.ui.screen.navigation.shared

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun SearchPlaceOverlay(
    mapController: HarukaMapController = MapControllerProvider.harukaMapController,
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory)
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    SearchPlaceSuggestion(
        query = viewModel.searchQuery,
        onQueryChanged = viewModel::searchQueryChange,
        onSearchCloseClicked = viewModel::toggleSearchActive,
        suggestions = uiState.value.placeSuggestions.map {
            SuggestionPlaceItem(
                name = it.name,
                distanceMeters = it.distanceMeters,
                etaMinutes = it.etaMinutes?.toInt(),
                suggestionsOnClicked = { viewModel.displayDetailSuggestion(it) }
            )
        }
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun SearchPlaceSuggestion(
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChanged: (String) -> Unit = {},
    onSearchCloseClicked: () -> Unit = {},
    suggestions: List<SuggestionPlaceItem> = emptyList()
) {
    Card(
        modifier = modifier
            .padding(dimensionResource(R.dimen.padding_medium))
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.place_search_card_corner_radius)),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier) {
            TextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = { Text(text = stringResource(R.string.destination_search_input_label)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Place") },
                trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Close Search",
                    modifier = Modifier.clickable { onSearchCloseClicked() })},
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ))

            LazyColumn(modifier = Modifier
                .padding(top = dimensionResource(R.dimen.padding_small))
                .heightIn(max = dimensionResource(R.dimen.place_suggestion_min_height))) {
                items(suggestions) { item ->
                    Box(modifier
                        .padding(
                            horizontal = dimensionResource(R.dimen.padding_medium_large)
                        )
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = Color(0xFFCCCCCC),
                                start = Offset(0f, size.height - 1.dp.toPx() / 2),
                                end = Offset(size.width, size.height - 1.dp.toPx() / 2),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .clickable { item.suggestionsOnClicked() }) {
                        Column(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_medium))) {
                            Text(
                                text = item.name,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row {
                                if (item.distanceMeters != null) {
                                    val distance = item.distanceMeters
                                    DisplayDistance(modifier = Modifier.weight(1f), distance = distance)
                                }
                                if (item.etaMinutes != null) {
                                    val etaMinutes = item.etaMinutes
                                    DisplayEta(etaMinutes = etaMinutes)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


data class SuggestionPlaceItem(
    val name: String = "",
    val distanceMeters: Double? = null,
    val etaMinutes: Int? = null,
    val suggestionsOnClicked: () -> Unit = {},
)

@Preview(showBackground = true)
@Composable
fun SearchPlaceSuggestionPreview() {
    val suggestions = listOf(
        SuggestionPlaceItem(name = "Test", distanceMeters = 900.0, etaMinutes = 10),
        SuggestionPlaceItem(name = "TestTest", distanceMeters = 1200.0, etaMinutes = 50),
        SuggestionPlaceItem(name = "TestTestTest", distanceMeters = 10100.0, etaMinutes = 80),
    )

    AppTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
            .background(MaterialTheme.colorScheme.inverseOnSurface)) {
            SearchPlaceSuggestion(query = "Test", suggestions = suggestions)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPlacePreview() {
    val suggestions = emptyList<SuggestionPlaceItem>()

    AppTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
            .background(MaterialTheme.colorScheme.inverseOnSurface)) {
            SearchPlaceSuggestion(query = "", suggestions = suggestions)
        }
    }
}