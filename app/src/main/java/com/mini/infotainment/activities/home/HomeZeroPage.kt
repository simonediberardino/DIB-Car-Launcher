package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mini.infotainment.R
import com.mini.infotainment.support.MapInteractions
import com.mini.infotainment.support.Page

class HomeZeroPage(override val ctx: HomeActivity) : Page, OnMapReadyCallback, MapInteractions {
    companion object{
        internal const val CIRCLE_RADIUS = 150.0
        internal const val CIRCLE_MIN_ZOOM = 19f
        internal const val MAP_DEFAULT_ZOOM = 17f
    }

    internal lateinit var googleMap: GoogleMap
    internal lateinit var resetLocBtn: View
    internal var userLocation: Location? = null
    /** Location icon placed on the user location updated every time the user location changes; */
    internal var userLocMarker: Marker? = null
    /** Circle placed on the user location updated every time the user location changes; */
    internal var userLocCircle: Circle? = null

    internal var mapFollowsUser = true
        set(value) {
            resetLocBtn.visibility = if(value) View.INVISIBLE else View.VISIBLE
            field = value
        }

    override fun build() {
        ctx.viewPages.clear()
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)

        val layout = ctx.layoutInflater.inflate(R.layout.activity_home_0, ctx.viewPager, false) as ViewGroup
        ctx.viewPages.add(layout)

        resetLocBtn = layout.findViewById(R.id.home_0_resetpos_btn)

        setListeners()
        createMap()
    }

    override fun setListeners() {
        resetLocBtn.setOnClickListener {
            zoomMapToUser()
        }
    }

    @SuppressLint("RestrictedApi")
    fun createMap(){
        val mapFragment = ctx.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, R.raw.map_night))
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        mapFollowsUser = true

        this.setMapInteractionTrackingListeners()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.N)
    fun onLocationChanged(newLocation: Location?){
        if(newLocation == null)
            return

        val locationLatLng = LatLng(newLocation.latitude, newLocation.longitude)

        val height = 15; val width = 15
        val drawable = ctx.getDrawable(R.drawable.location_icon)
        val bitmap = drawable?.toBitmap(width, height)

        val markerOptions = MarkerOptions()
            .position(locationLatLng)
            .icon(
                BitmapDescriptorFactory
                    .fromBitmap(bitmap!!))

        val circleOptions = CircleOptions()
            .center(markerOptions.position)
            .radius(CIRCLE_RADIUS)
            .strokeColor(0xf1E90FF)
            .fillColor(0x301E90FF)
            .strokeWidth(10f)

        userLocation = newLocation
        userLocCircle?.remove()
        userLocMarker?.remove()
        userLocMarker = googleMap.addMarker(markerOptions)
        userLocCircle = googleMap.addCircle(circleOptions)

        if(mapFollowsUser)
            zoomMapToUser()

        refreshLocationCircle()
    }

    private fun refreshLocationCircle(){
        userLocCircle?.isVisible = getMapZoom() >= CIRCLE_MIN_ZOOM
    }

    private fun setMapInteractionTrackingListeners() {
        var cameraPositionBeforeCameraMoveStarted = googleMap.cameraPosition
        var cameraChangeReason = GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
        var isZoomInStarted = false
        var isZoomOutStarted = false

        googleMap.setOnCameraMoveStartedListener { reason ->
            cameraChangeReason = reason
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                cameraPositionBeforeCameraMoveStarted = googleMap.cameraPosition
                this.onMapPanListener().invoke()
            }
        }

        googleMap.setOnCameraMoveListener {
            if (googleMap.cameraPosition.zoom > cameraPositionBeforeCameraMoveStarted.zoom && !isZoomInStarted) {
                isZoomInStarted = true
                this.onMapZoomInStartListener().invoke(googleMap.cameraPosition.zoom.toDouble())
            } else if (googleMap.cameraPosition.zoom < cameraPositionBeforeCameraMoveStarted.zoom && !isZoomOutStarted) {
                isZoomOutStarted = true
                this.onMapZoomOutStartListener().invoke(googleMap.cameraPosition.zoom.toDouble())
            }
        }

        googleMap.setOnCameraIdleListener {
            if (cameraChangeReason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                if (googleMap.cameraPosition.bearing != cameraPositionBeforeCameraMoveStarted.bearing) {
                    this.onMapRotateListener().invoke()
                }

                val isZoomInOrOutStarted = isZoomInStarted || isZoomOutStarted

                if (googleMap.cameraPosition.bearing == cameraPositionBeforeCameraMoveStarted.bearing
                    && googleMap.cameraPosition.target != cameraPositionBeforeCameraMoveStarted.target
                    && !isZoomInOrOutStarted
                ) {
                    this.onMapPanListener().invoke()
                }

                if (googleMap.cameraPosition.zoom > cameraPositionBeforeCameraMoveStarted.zoom && isZoomInStarted) {
                    this.onMapZoomInEndListener().invoke(googleMap.cameraPosition.zoom.toDouble())
                    isZoomInStarted = false
                }

                if (googleMap.cameraPosition.zoom < cameraPositionBeforeCameraMoveStarted.zoom && isZoomOutStarted) {
                    this.onMapZoomOutEndListener().invoke(googleMap.cameraPosition.zoom.toDouble())
                    isZoomOutStarted = false
                }

                cameraPositionBeforeCameraMoveStarted = googleMap.cameraPosition
                cameraChangeReason = GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
            }
        }
    }


    override fun onMapZoomInEndListener(): (zoomLevel: Double) -> Unit = {
        googleMap.uiSettings.isScrollGesturesEnabled = true
        mapFollowsUser = false
    }

    override fun onMapZoomOutEndListener(): (zoomLevel: Double) -> Unit = {
        googleMap.uiSettings.isScrollGesturesEnabled = true
        mapFollowsUser = false
    }

    override fun onMapZoomOutStartListener(): (zoomLevel: Double) -> Unit = {}
    override fun onMapZoomInStartListener(): (Double) -> Unit = {}
    override fun onMapPanListener(): () -> Unit = {}
    override fun onMapRotateListener(): () -> Unit = {}

    private fun getMapZoom(): Float {
        return googleMap.cameraPosition.zoom
    }

    private fun zoomMapToUser(){
        zoomMapToUser(true, null)
    }

    private fun zoomMapToUser(animation: Boolean, callback: Runnable?){
        if(userLocation == null)
            return

        googleMap.uiSettings.isScrollGesturesEnabled = false
        mapFollowsUser = true

        val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(
            LatLng(userLocation!!.latitude, userLocation!!.longitude),
            MAP_DEFAULT_ZOOM
        )

        if(!animation){
            googleMap.moveCamera(
                cameraUpdateFactory
            )
            callback?.run()
        }else{
            googleMap.animateCamera(cameraUpdateFactory, object: GoogleMap.CancelableCallback{
                override fun onCancel(){
                    callback?.run()
                }

                override fun onFinish() {
                    callback?.run()
                }
            })
        }
    }
}