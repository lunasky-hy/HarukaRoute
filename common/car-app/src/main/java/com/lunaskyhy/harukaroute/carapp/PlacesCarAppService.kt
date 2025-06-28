package com.lunaskyhy.harukaroute.carapp

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import com.lunaskyhy.harukaroute.carapp.place.PlaceScreen

class PlacesCarAppService: CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    } // Allow all hosts by default, but in production, cannot use ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return PlacesSession()
    }
}

class PlacesSession: Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return PlaceScreen(carContext)
    }
}