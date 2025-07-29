package com.lunaskyhy.harukaroute.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lunaskyhy.harukaroute.MainApplication
import com.lunaskyhy.harukaroute.map.MyNavigationViewModel
import com.lunaskyhy.harukaroute.ui.overlay.viewmodel.SearchLocationViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MyNavigationViewModel(
                locationRepository = myApplication().container.locationRepository,
                placeClient = myApplication().container.placesClient,
            )
        }

        initializer {
            SearchLocationViewModel(
                placeClient = myApplication().container.placesClient
            )
        }
    }
}

fun CreationExtras.myApplication(): MainApplication = (
        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication
        )