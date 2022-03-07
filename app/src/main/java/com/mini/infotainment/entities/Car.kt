package com.mini.infotainment.entities

import android.location.Location
import com.mini.infotainment.support.LocationExtended

class Car(var targa: String, var carName: String, var password: String, var location: Location?) {
    companion object{
        val currentCar = Car("CW832LX", "Mini 1.4 TDI One D", "3238", null)
    }

}