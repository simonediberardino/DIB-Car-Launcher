package com.mini.infotainment.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

class GPSManager(val ctx: Activity) {
    internal lateinit var locationManager: FusedLocationProviderClient
    internal var lastAddressCheck: Long = 0
    internal var previousUserLocation: Location? = null
    internal var callbacks: HashMap<String, RunnablePar> = hashMapOf()
    var currentUserLocation: Location? = null
    var currentSpeed: Int = 0

    @SuppressLint("MissingPermission")
    fun init(){
        val locationRequest = com.google.android.gms.location.LocationRequest.create()
        locationRequest.interval = 1
        locationRequest.fastestInterval = 1
        locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationUpdate(locationResult?.lastLocation)
            }
        }

        locationManager = LocationServices.getFusedLocationProviderClient(ctx)
        locationManager.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun onLocationUpdate(newLocation: Location?){
        if(newLocation == null || !Utility.isInternetAvailable() || ApplicationData.getTarga() == null)
            return

        if(currentUserLocation != null){
            previousUserLocation = currentUserLocation
        }

        currentUserLocation = newLocation

        StatsData.increaseTraveledDistance(
            previousUserLocation?.distanceTo(currentUserLocation) ?: 0f
        )

        currentSpeed = Utility.msToKmH(calculateSpeed())

        if(currentSpeed > 2)
            StatsData.addSpeedReport(currentSpeed.toFloat())

        callbacks[SActivity.lastActivity.packageName]?.run(newLocation)
    }

    private fun calculateSpeed(): Float {
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
        val isFarEnough =
            if(previousUserLocation == null){
                true
            }else{
                currentUserLocation!!.distanceTo(previousUserLocation) > minimumDistanceAddressCheck
            }
        val isTimePassed = System.currentTimeMillis() - lastAddressCheck >= 1000*25
        return isFarEnough && isTimePassed
    }
}