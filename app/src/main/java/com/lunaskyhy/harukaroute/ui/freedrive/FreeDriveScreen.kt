package com.lunaskyhy.harukaroute.ui.freedrive

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.map.HarukaMapController
import com.lunaskyhy.harukaroute.map.MapControllerProvider

@Composable
fun FreeDriveScreen(
    mapController: HarukaMapController = MapControllerProvider.harukaMapController,
    modifier: Modifier = Modifier,
) {
    var text = ""
    FreeDriveScreenLayout(modifier = modifier)
}

@Composable
fun FreeDriveScreenLayout(
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {

    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        TextField(value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.destination_search_input_label)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}