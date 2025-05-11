package com.lunaskyhy.harukaroute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.car.app.connection.CarConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lunaskyhy.harukaroute.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val carConnectionType by CarConnection(this).type.observeAsState(initial = -1)

            AppTheme {
                Surface {
                    Column {
                        Text(
                            text = "Places",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(8.dp)
                        )
                        ProjectionState(
                            carConnectionType = carConnectionType,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProjectionState(carConnectionType: Int, modifier: Modifier = Modifier) {
    val text = when (carConnectionType) {
        CarConnection.CONNECTION_TYPE_NOT_CONNECTED -> "Not projecting"
        CarConnection.CONNECTION_TYPE_NATIVE -> "Running on Android Automotive OS"
        CarConnection.CONNECTION_TYPE_PROJECTION -> "Projecting"
        else -> "Unknown connection type"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
    }
}