package com.lunaskyhy.harukaroute.ui.screen.navigation.freedrive.overlay

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.ui.AppViewModelProvider
import com.lunaskyhy.harukaroute.ui.screen.navigation.NavigationScreenViewModel
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component.DisplayDistance
import com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component.DisplayEta
import com.lunaskyhy.harukaroute.ui.theme.AppTheme
import com.lunaskyhy.harukaroute.ui.theme.AppTypography

@Composable
fun SearchPlaceOverlay(
    viewModel: NavigationScreenViewModel = viewModel(factory = AppViewModelProvider.viewModelFactory)
) {

    SearchPlaceField(
        query = viewModel.searchQuery,
        onQueryChanged = viewModel::searchQueryChange,
        onSearchCloseClicked = { viewModel.searchQueryChange("")},
        suggestions = emptyList()
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun SearchPlaceField(
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
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = AppTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            ) { innerTextField ->
                Row(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search,
                        contentDescription = "Search Place",
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    if (query.isNotEmpty()) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Search",
                            modifier = Modifier.clickable { onSearchCloseClicked() })
                    }
                }
            }

            if (suggestions.isNotEmpty()) {
                SearchPlaceSuggestion(suggestions = suggestions)
            }
        }
    }
}

@Composable
fun SearchPlaceSuggestion(
    modifier: Modifier = Modifier,
    suggestions: List<SuggestionPlaceItem> = emptyList()
) {
    LazyColumn(modifier = modifier
        .padding(bottom = dimensionResource(R.dimen.padding_medium))
        .heightIn(max = dimensionResource(R.dimen.place_suggestion_min_height))
    ) {
        items(suggestions) { item ->
            Box(modifier
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
                .fillMaxWidth()
                .clickable { item.suggestionsOnClicked() }
            ) {
                Column(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_medium))) {
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
            SearchPlaceField(query = "Test", suggestions = suggestions)
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
            SearchPlaceField(query = "", suggestions = suggestions)
        }
    }
}