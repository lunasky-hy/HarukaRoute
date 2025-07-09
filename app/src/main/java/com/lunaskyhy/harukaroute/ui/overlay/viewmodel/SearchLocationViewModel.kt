package com.lunaskyhy.harukaroute.ui.overlay.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SearchLocationViewModel: ViewModel() {
    var searchText by mutableStateOf("")
        private set

    fun updateSearchText(text: String) {
        searchText = text
    }
}