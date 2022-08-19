package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.maps.MapsActivity
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.receivers.SpotifyIntegration
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.SActivity.Companion.displayRatio
import com.mini.infotainment.support.SActivity.Companion.isGpsManagerInitializated
import com.mini.infotainment.utility.Utility
import com.mini.infotainment.utility.Utility.kmToMile
import java.util.*


class HomeFirstPage(override val ctx: HomeActivity) : Page(), OnMapReadyCallback {
    internal var mapFragment: SupportMapFragment? = null

    private var googleMap: GoogleMap? = null
    private lateinit var dayTW: TextView
    private lateinit var speedUmTV: TextView
    private lateinit var distUmTV: TextView
    internal lateinit var spotifyAuthorTw: TextView
    internal lateinit var spotifyTitleTW: TextView
    internal lateinit var addressTW: TextView
    internal lateinit var speedometerTW: TextView
    internal lateinit var timeTW: TextView
    internal lateinit var spotifyWidget: View
    internal lateinit var carIcon: ImageView
    internal lateinit var travDist: TextView
    /** Location icon placed on the user location updated every time the user location changes; */
    internal var userLocMarker: Marker? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun build() {
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)
        ctx.viewPages.clear()

        parent = ctx.layoutInflater.inflate(R.layout.activity_home_1, ctx.viewPager, false) as ViewGroup

        spotifyWidget = parent!!.findViewById(R.id.home_1_spotify)
        timeTW = parent!!.findViewById(R.id.home_1_datetime)
        dayTW = parent!!.findViewById(R.id.home_1_day)
        speedometerTW = parent!!.findViewById(R.id.home_1_speed)
        addressTW = parent!!.findViewById(R.id.home_1_address)
        spotifyTitleTW = parent!!.findViewById(R.id.spotify_title)
        spotifyAuthorTw = parent!!.findViewById(R.id.spotify_author)
        carIcon = parent!!.findViewById(R.id.home_1_car_icon)
        travDist = parent!!.findViewById(R.id.home_1_trav_dist)
        speedUmTV = parent!!.findViewById(R.id.home_1_speed_um)
        distUmTV = parent!!.findViewById(R.id.home_1_trav_dist_um)

        ctx.viewPages.add(parent!!)

        createMap()
        updateData()
        setListeners()
        setupTimer()

        super.pageLoaded()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setListeners() {
        spotifyWidget.setOnTouchListener { v, e ->
            when(e.action){
                MotionEvent.ACTION_UP -> {
                    val isLeft = v.width/3 > e.x
                    val isRight = (v.width/3)*2 < e.x

                    if(isRight)
                        SpotifyIntegration.nextSpotifyTrack(ctx)
                    else if(isLeft) SpotifyIntegration.previousSpotifyTrack(ctx)
                    else SpotifyIntegration.togglePlayState(ctx)

                    if(spotifyTitleTW.text == ctx.getString(R.string.spotify_no_data)){
                        CustomToast(ctx.getString(R.string.spotify_no_data_why), ctx)
                    }
                }
            }
            true
        }
    }

    fun updateData(){
        updateLogoImageView()
        updateTravDist()
        updateSpeed()
    }

    private fun updateSpeed(){
        val temp = if(isGpsManagerInitializated) SActivity.gpsManager.currentSpeed.toFloat() else 0f
        speedometerTW.text = (if(Utility.isUMeasureKM()) temp else temp.kmToMile()).toInt().toString()
        speedUmTV.text = Utility.getSpeedMeasure(ctx)
    }

    private fun updateTravDist(){
        travDist.text = StatsData.getTraveledDistance(StatsData.Mode.DAY).toString()
        distUmTV.text = Utility.getTravDistMeasure(ctx)
    }

    private fun updateLogoImageView(){
        carIcon.setImageDrawable(Utility.getBrandDrawable(ctx) ?: return)
    }

    private fun setupTimer(){
        Thread{
            while(true){
                try{
                    updateTime()
                    Thread.sleep(1000)
                }catch (exception: Exception){}
            }
        }.start()
    }

    private fun updateTime() {
        ctx.runOnUiThread {
            timeTW.text = Utility.getTime()
            dayTW.text = ctx.resources.getStringArray(R.array.days_week)[Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK)-1]
        }
    }

    fun createMap(){
        mapFragment = ctx.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        fun onClickCallback(){
            if(MyCar.instance.isPremium()){
                Utility.navigateTo(ctx, MapsActivity::class.java)
            }
        }

        googleMap = p0
        googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, R.raw.map_night))
        googleMap!!.setOnMapClickListener {
            onClickCallback()
        }
        googleMap!!.setOnMarkerClickListener {
            onClickCallback()
            true
        }
        googleMap!!.uiSettings.isScrollGesturesEnabled = false
        googleMap!!.uiSettings.isRotateGesturesEnabled = false
        googleMap!!.uiSettings.isCompassEnabled = false
        googleMap!!.uiSettings.isZoomControlsEnabled = false
        googleMap!!.uiSettings.isZoomGesturesEnabled = false
        googleMap!!.isBuildingsEnabled = false

        onLocationChanged()
    }

    fun onLocationChanged(){
        updateTravDist()
        updateSpeed()

        if(!isGpsManagerInitializated) return
        if(SActivity.gpsManager.currentUserLocation == null) return

        val minDistToUpdate = 2.5f
        if((SActivity.gpsManager.previousUserLocation?.distanceTo(SActivity.gpsManager.currentUserLocation) ?: minDistToUpdate) < minDistToUpdate)
            return

        doOnLocationChanged()
    }

    private fun doOnLocationChanged(){
        if(googleMap == null) return

        val newLocation = SActivity.gpsManager.currentUserLocation!!
        val locationLatLng = LatLng(newLocation.latitude, newLocation.longitude)

        val height: Int = (ctx.displayRatio * 15).toInt()
        val width: Int = (ctx.displayRatio * 15).toInt()
        val drawable = ctx.getDrawable(R.drawable.location_icon)
        val bitmap = drawable?.toBitmap(width, height)

        val markerOptions = MarkerOptions()
            .position(locationLatLng)
            .icon(
                BitmapDescriptorFactory
                    .fromBitmap(bitmap!!))

        userLocMarker?.remove()
        userLocMarker = googleMap!!.addMarker(markerOptions)

        zoomMapToUser()
    }

    private fun zoomMapToUser() {
        if(SActivity.gpsManager.currentUserLocation == null)
            return

        val bearing: Float = SActivity.gpsManager.previousUserLocation?.bearingTo(SActivity.gpsManager.currentUserLocation) ?: 0f

        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    SActivity.gpsManager.currentUserLocation!!.latitude,
                    SActivity.gpsManager.currentUserLocation!!.longitude
                )
            )
            .zoom(15.5f)
            .bearing(bearing)
            .tilt(80f)
            .build()

        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

}