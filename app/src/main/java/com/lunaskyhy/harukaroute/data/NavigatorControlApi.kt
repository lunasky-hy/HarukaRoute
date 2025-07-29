package com.lunaskyhy.harukaroute.data

import android.util.Log
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.Navigator
import com.lunaskyhy.harukaroute.MainApplication

interface NavigationClientProvider {
    fun getNavigationClient(): Navigator
}

class NavigatorControlApi(application: MainApplication): NavigationClientProvider {
    private lateinit var navigatorClient: Navigator

    init {
        NavigationApi.getNavigator(
            application,
            object : NavigationApi.NavigatorListener {
                override fun onError(p0: Int) {
                    Log.e("Navigator", "Error: $p0")
                }

                override fun onNavigatorReady(navigator: Navigator?) {
                    navigatorClient = navigator!!
                }
            }
        )
    }

    override fun getNavigationClient(): Navigator {
        return navigatorClient
    }
}