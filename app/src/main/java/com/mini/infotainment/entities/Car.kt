package com.mini.infotainment.entities

import android.location.Location

class Car(var location: Location?) {
    companion object{
        val currentCar = Car(null)
    }
}