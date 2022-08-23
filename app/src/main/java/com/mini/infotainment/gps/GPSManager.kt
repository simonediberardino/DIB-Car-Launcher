package com.mini.infotainment.gps

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.SActivity.Companion.isInternetAvailable
import com.mini.infotainment.utility.Utility.msToKmH
import okhttp3.OkHttpClient
import okhttp3.Request

class GPSManager(val ctx: AppCompatActivity) {
    internal lateinit var locationManager: FusedLocationProviderClient
    internal var lastAddressCheck: Long = 0
    internal var previousUserLocation: Location? = null
    internal var callbacks: HashMap<String, RunnablePar> = hashMapOf()
    var currentAcceleration: Float = 0f
    var currentUserLocation: Location? = null
    var currentSpeed: Speed = Speed(0, 0)
    private var previousSpeed: Speed = Speed(0, 0)

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
        if(newLocation == null || !ctx.isInternetAvailable || ApplicationData.getTarga() == null)
            return

        if(currentUserLocation != null){
            previousUserLocation = currentUserLocation
        }

        currentUserLocation = newLocation

        StatsData.increaseTraveledDistance(
            previousUserLocation?.distanceTo(currentUserLocation) ?: 0f
        )

        previousSpeed = currentSpeed.copy()
        currentSpeed = Speed((calculateSpeed().toDouble()).msToKmH(), newLocation.time)
        currentAcceleration = calculateAcceleration()

        if(currentSpeed.value > 2)
            StatsData.addSpeedReport(currentSpeed.value.toFloat())

        callbacks[SActivity.lastActivity.packageName]?.run(newLocation)
    }

    private fun calculateSpeed(): Float {
        return if(currentUserLocation?.hasSpeed() == true){
            currentUserLocation!!.speed
        }else{
            if(previousUserLocation == null) {
                0f
            }else{
                val elapsedTimeInSeconds = (currentUserLocation!!.time - previousUserLocation!!.time) / 1000
                val distanceInMeters = currentUserLocation!!.distanceTo(previousUserLocation)
                val speed = distanceInMeters / elapsedTimeInSeconds
                return if(speed == Float.POSITIVE_INFINITY) 0f else speed
            }
        }
    }

    private fun calculateAcceleration(): Float {
        if(previousSpeed.time == 0L || currentSpeed.time == 0L) return 0f
        return (currentSpeed.value/3.6f - previousSpeed.value/3.6f) / (currentUserLocation!!.time-previousUserLocation!!.time)
    }

    fun shouldRefreshAddress(): Boolean {
        return System.currentTimeMillis() - lastAddressCheck >= 1000 * 25
    }

    fun getAddress(location: Location, callback: RunnablePar) {
        if(!ctx.isInternetAvailable) return

        Thread{
            try{
                val client = OkHttpClient().newBuilder()
                    .build()
                val request: Request = Request.Builder()
                    .url("https://api.geoapify.com/v1/geocode/reverse?lat=${location.latitude}&lon=${location.longitude}&apiKey=827645ed3da54b00a91ac7217a17fdb9")
                    .method("GET", null)
                    .build()

                client.newCall(request).execute().use {
                        response ->
                    run {
                        var result = response.body?.string()
                        val field = "formatted"
                        if(result != null && field in result) {
                            result = result
                                .split(field)[1]
                                .split("\n")[0]
                                .drop(3)
                                .dropLast(2)

                            ctx.runOnUiThread {
                                callback.run(result)
                            }
                        }else{
                            callback.run(String())
                        }
                    }
                }
            }catch (exception: Exception){
                return@Thread
            }
        }.start()
    }

    fun getSimpleAddress(location: Location, callback: RunnablePar) {
        getAddress(location, object: RunnablePar{
            override fun run(p: Any?) {
                mutableListOf<View>().toTypedArray()
                callback.run(
                    (p as String).split(",")[0]
                )
            }
        })
    }

    data class Speed(val value: Int, val time: Long)

}