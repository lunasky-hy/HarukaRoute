package com.lunaskyhy.harukaroute

import android.app.Application
import com.lunaskyhy.harukaroute.data.AppContainer
import com.lunaskyhy.harukaroute.data.MyAppContainer

class MainApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = MyAppContainer(this)
    }
}