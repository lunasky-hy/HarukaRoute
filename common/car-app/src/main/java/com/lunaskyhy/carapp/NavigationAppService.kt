package com.lunaskyhy.carapp

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import com.lunaskyhy.carapp.navigation.NavigationScreen

class NavigationAppService: CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return NavigationSession()
    }
}

class NavigationSession: Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return NavigationScreen(carContext)
    }
}