package com.lunaskyhy.carapp.navigation

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Distance
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.TravelEstimate
import java.time.ZonedDateTime
class NavigationScreen(carContext: CarContext): Screen(carContext) {
    override fun onGetTemplate(): Template {
        val travelEstimate = TravelEstimate.Builder(
                Distance.create(100.0, Distance.UNIT_KILOMETERS),
                ZonedDateTime.now(),
            ).build()

//        val routing = RoutingInfo.Builder().build()

        return NavigationTemplate.Builder()
            .setDestinationTravelEstimate(travelEstimate)
            .setActionStrip(ActionStrip.Builder().addAction(Action.BACK).build())
            .build()
    }
}