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
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.maps.MapsActivity
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.gps.TripHandler
import com.mini.infotainment.receivers.MusicIntegration
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.SActivity.Companion.displayRatio
import com.mini.infotainment.support.SActivity.Companion.isGpsManagerInitializated
import com.mini.infotainment.utility.Utility
import com.mini.infotainment.utility.Utility.kmToMile
import java.util.*


class HomeFirstPage(override val ctx: HomeActivity) : Page(), OnMapReadyCallback {
    private lateinit var tripHandler: TripHandler
    private var mapFragment: SupportMapFragment? = null

    /** Location icon placed on the user location updated every time the user location changes; */
    private var userLocMarker: Marker? = null

    private var googleMap: GoogleMap? = null
    private var bearing: Float = 0f
    private lateinit var tripTimeTV: TextView
    private lateinit var dayTV: TextView
    private lateinit var accelTV: TextView
    private lateinit var speedUmTV: TextView
    private lateinit var distUmTV: TextView
    private lateinit var speedometerTV: TextView
    private lateinit var timeTV: TextView
    private lateinit var musicWidget: View
    private lateinit var carIcon: ImageView
    private lateinit var travDist: TextView
    internal lateinit var musicAuthorTV: TextView
    internal lateinit var musicTitleTV: TextView
    internal lateinit var addressTV: TextView
    @SuppressLint("ClickableViewAccessibility")
    
    override fun build() {
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)
        ctx.viewPages.clear()

        parent = ctx.layoutInflater.inflate(R.layout.activity_home_1, ctx.viewPager, false) as ViewGroup

        musicWidget = parent!!.findViewById(R.id.home_1_music)
        timeTV = parent!!.findViewById(R.id.home_1_datetime)
        tripTimeTV = parent!!.findViewById(R.id.home_1_trip_time)
        dayTV = parent!!.findViewById(R.id.home_1_day)
        speedometerTV = parent!!.findViewById(R.id.home_1_speed)
        addressTV = parent!!.findViewById(R.id.home_1_address)
        musicTitleTV = parent!!.findViewById(R.id.music_title)
        musicAuthorTV = parent!!.findViewById(R.id.music_author)
        carIcon = parent!!.findViewById(R.id.home_1_car_icon)
        travDist = parent!!.findViewById(R.id.home_1_trav_dist)
        speedUmTV = parent!!.findViewById(R.id.home_1_speed_um)
        distUmTV = parent!!.findViewById(R.id.home_1_trav_dist_um)
        accelTV = parent!!.findViewById(R.id.home_1_acc)

        ctx.viewPages.add(parent!!)

        createMap()
        setListeners()
        setupTimer()
        updateData()

        super.pageLoaded()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setListeners() {
        musicWidget.setOnTouchListener { v, e ->
            when(e.action){
                MotionEvent.ACTION_UP -> {
                    val isLeft = v.width/3 > e.x
                    val isRight = (v.width/3)*2 < e.x
                    val isCenter = !isLeft && !isRight

                    when(true){
                        isLeft -> MusicIntegration.previousTrack(ctx)
                        isRight -> MusicIntegration.nextTrack(ctx)
                        isCenter -> MusicIntegration.togglePlayState(ctx)
                        else -> {}
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
        updateTripTime()
    }

    private fun updateAccel(){
        val accel = if(isGpsManagerInitializated) SActivity.gpsManager.currentAcceleration else 0f
        accelTV.text = accel.toString()
    }

    private fun updateSpeed(){
        val temp = if(isGpsManagerInitializated) SActivity.gpsManager.currentSpeed.value.toFloat() else 0f
        speedometerTV.text = (if(Utility.isUMeasureKM()) temp else temp.kmToMile()).toInt().toString()
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
        tripHandler = TripHandler{
            ctx.runOnUiThread {
                updateDayHour()
                updateTripTime()
            }
        }.apply { start() }
    }

    private fun updateDayHour() {
        timeTV.text = Utility.getTime()
        dayTV.text = ctx.resources.getStringArray(R.array.days_week)[Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK)-1]
    }

    private fun updateTripTime(){
        tripTimeTV.text = tripHandler.getElapsedTime()
    }
    
    private fun createMap(){
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
        updateAccel()

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

        val bearing: Float =
            if(SActivity.gpsManager.currentSpeed.value > 2)
                SActivity.gpsManager.previousUserLocation?.bearingTo(SActivity.gpsManager.currentUserLocation) ?: 0f
            else this.bearing

        this.bearing = bearing

        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    SActivity.gpsManager.currentUserLocation!!.latitude,
                    SActivity.gpsManager.currentUserLocation!!.longitude
                )
            )
            .zoom(15.2f)
            .bearing(this.bearing)
            .tilt(80f)
            .build()

        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

}