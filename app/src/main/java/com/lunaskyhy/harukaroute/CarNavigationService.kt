package com.lunaskyhy.harukaroute

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.lunaskyhy.harukaroute.carscreen.MapScreen

class CarNavigationService: CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return MapsSession()
    }
}

class MapsSession: Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MapScreen(carContext)
    }
}