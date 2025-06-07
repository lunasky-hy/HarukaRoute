package com.lunaskyhy.harukaroute.ui.screen.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lunaskyhy.harukaroute.map.HarukaMapController

class NavigationScreenViewModel(
    mapController: HarukaMapController
): ViewModel() {
    var searchQuery: String by mutableStateOf("")
        private set

    fun searchQueryChange(query: String) {
        searchQuery = query
    }
}