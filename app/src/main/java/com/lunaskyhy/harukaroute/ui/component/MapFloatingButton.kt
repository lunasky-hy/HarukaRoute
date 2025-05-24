package com.lunaskyhy.harukaroute.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lunaskyhy.harukaroute.R
import com.lunaskyhy.harukaroute.ui.theme.AppTheme


@Composable
fun FloatingButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.padding(
            bottom = dimensionResource(R.dimen.floating_button_bottom_padding)
        ).size(dimensionResource(R.dimen.floating_button_size)),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonColors(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(Icons.Sharp.Search, contentDescription = "Search Location")
    }
}

@Preview(showBackground = true)
@Composable
fun FloatingButtonPreview() {
    AppTheme {
        Scaffold(
            floatingActionButton = { FloatingButton() }
        ) {
            Box(modifier = Modifier.padding(it)) {
                Text("Display")
            }
        }
    }
}