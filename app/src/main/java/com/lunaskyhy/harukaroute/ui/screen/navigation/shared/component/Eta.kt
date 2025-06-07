package com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DisplayEta(modifier: Modifier = Modifier, etaMinutes: Int) {
    Text(
        text = if (etaMinutes < 60) "$etaMinutes min"
        else "${etaMinutes / 60} h ${etaMinutes % 60} min",
        modifier = modifier
    )
}