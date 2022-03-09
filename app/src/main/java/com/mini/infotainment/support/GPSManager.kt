package com.mini.infotainment.support

import android.location.Location
import com.google.firebase.ktx.Firebase

class GPSManager {
    var lastAddressCheck: Long = 0
    var currentUserLocation: Location? = null
    var previousUserLocation: Location? = null

    fun calculateSpeed(): Float {
        return if(currentUserLocation?.hasSpeed() == true){
            currentUserLocation!!.speed
        }else{
            if(previousUserLocation == null)
                0f
            else{
                val elapsedTimeInSeconds = (currentUserLocation!!.time - previousUserLocation!!.time) / 1000
                val distanceInMeters = currentUserLocation!!.distanceTo(previousUserLocation)
                val speed = distanceInMeters / elapsedTimeInSeconds
                return if(speed == Float.POSITIVE_INFINITY) 0f else speed
            }
        }
    }

    fun shouldRefreshAddress(): Boolean {
        val minimumDistanceAddressCheck = 5
        val isFarEnough = currentUserLocation!!.distanceTo(previousUserLocation) > minimumDistanceAddressCheck
        val isTimePassed = System.currentTimeMillis() - lastAddressCheck >= 1000*25
        return isFarEnough && isTimePassed
    }
}