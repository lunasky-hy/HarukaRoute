package com.lunaskyhy.harukaroute.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lunaskyhy.harukaroute.map.MapControllerProvider
import com.lunaskyhy.harukaroute.ui.freedrive.FreeDriveViewModel

object AppViewModelProvider {
    val viewModelFactory = viewModelFactory {
        initializer {
            FreeDriveViewModel(MapControllerProvider.harukaMapController)
        }
    }
}