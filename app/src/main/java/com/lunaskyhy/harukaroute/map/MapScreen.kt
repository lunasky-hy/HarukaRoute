package com.lunaskyhy.harukaroute.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline

@Composable
fun MapScreen(
    viewModel: NavigationViewModel = NavigationViewModel()
) {
    val route: List<LatLng> = viewModel.routePolyline
    val nextTurn: String? = viewModel.nextTurnInfo

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap() {
            // 取得したルートを描画
            if (route.isNotEmpty()) {
                Polyline(points = route, color = Color.Blue, width = 20f)
            }
        }

        // 次の案内などをカスタムUIで表示
        if (nextTurn != null) {
            Card(modifier = Modifier.padding(8.dp)) {
                Text(text = nextTurn, modifier = Modifier.padding(16.dp))
            }
        }
    }

}