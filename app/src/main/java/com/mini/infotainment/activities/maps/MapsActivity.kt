package com.mini.infotainment.activities.maps

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mini.infotainment.R
import com.mini.infotainment.UI.MapInteractions
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

class MapsActivity : SActivity(), OnMapReadyCallback, MapInteractions {
    companion object{
        internal const val CIRCLE_RADIUS = 150.0
        internal const val CIRCLE_MIN_ZOOM = 21f
        internal const val MAP_DEFAULT_ZOOM = 19.2f
    }

    var googleMap: GoogleMap? = null
    internal var mapFragment: SupportMapFragment? = null
    internal lateinit var resetLocBtn: View
    private lateinit var speedTw: TextView
    /** Location icon placed on the user location updated every time the user location changes; */
    internal var userLocMarker: Marker? = null
    /** Circle placed on the user location updated every time the user location changes; */
    internal var userLocCircle: Circle? = null

    internal var mapFollowsUser = true
        set(value) {
            resetLocBtn.visibility = if(value) View.INVISIBLE else View.VISIBLE
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initialize()
    }
    
    private fun initialize() {
        setViews()
        createMap()
        setWallpaper()
        addGpsCallback()

        super.pageLoaded()
    }

    private fun setViews() {
        setContentView(R.layout.activity_maps)

        resetLocBtn = findViewById(R.id.maps_resetpos_btn)
        speedTw = findViewById(R.id.maps_speed_tw)

        resetLocBtn.setOnClickListener {
            zoomMapToUser()
        }
    }

    private fun addGpsCallback(){
        gpsManager.callbacks[packageName] = (object: RunnablePar {
            override fun run(p: Any?) {
                onLocationChanged()
            }
        })
    }

    @SuppressLint("RestrictedApi")
    fun createMap(){
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_night))
        googleMap!!.uiSettings.isScrollGesturesEnabled = false
        googleMap!!.uiSettings.isRotateGesturesEnabled = false
        googleMap!!.uiSettings.isCompassEnabled = false
        mapFollowsUser = true

        println("MYMAPISREADY!")
        this.setMapInteractionTrackingListeners()
        this.onLocationChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.N)
    fun onLocationChanged(){
        if(googleMap == null) return

        if((gpsManager.currentUserLocation?.distanceTo(gpsManager.previousUserLocation) ?: 0f) < 2.5)
            return

        val newLocation = gpsManager.currentUserLocation ?: return
        val locationLatLng = LatLng(newLocation.latitude, newLocation.longitude)

        val height: Int = (Utility.getDisplayRatio(this) * 15).toInt()
        val width: Int = (Utility.getDisplayRatio(this) * 15).toInt()
        val drawable = getDrawable(R.drawable.location_icon)
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

        userLocCircle?.remove()
        userLocMarker?.remove()
        userLocMarker = googleMap!!.addMarker(markerOptions)
        userLocCircle = googleMap!!.addCircle(circleOptions)

        speedTw.text = gpsManager.currentSpeed.toString()

        if(mapFollowsUser)
            zoomMapToUser()

        refreshLocationCircle()
    }

    private fun refreshLocationCircle(){
        userLocCircle?.isVisible = getMapZoom() >= CIRCLE_MIN_ZOOM
    }

    private fun setMapInteractionTrackingListeners() {
        var cameraPositionBeforeCameraMoveStarted = googleMap!!.cameraPosition
        var cameraChangeReason = GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
        var isZoomInStarted = false
        var isZoomOutStarted = false

        googleMap!!.setOnCameraMoveStartedListener { reason ->
            cameraChangeReason = reason
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                cameraPositionBeforeCameraMoveStarted = googleMap!!.cameraPosition
                this.onMapPanListener().invoke()
            }
        }

        googleMap!!.setOnCameraMoveListener {
            if (googleMap!!.cameraPosition.zoom > cameraPositionBeforeCameraMoveStarted.zoom && !isZoomInStarted) {
                isZoomInStarted = true
                this.onMapZoomInStartListener().invoke(googleMap!!.cameraPosition.zoom.toDouble())
            } else if (googleMap!!.cameraPosition.zoom < cameraPositionBeforeCameraMoveStarted.zoom && !isZoomOutStarted) {
                isZoomOutStarted = true
                this.onMapZoomOutStartListener().invoke(googleMap!!.cameraPosition.zoom.toDouble())
            }
        }

        googleMap!!.setOnCameraIdleListener {
            if (cameraChangeReason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                if (googleMap!!.cameraPosition.bearing != cameraPositionBeforeCameraMoveStarted.bearing) {
                    this.onMapRotateListener().invoke()
                }

                val isZoomInOrOutStarted = isZoomInStarted || isZoomOutStarted

                if (googleMap!!.cameraPosition.bearing == cameraPositionBeforeCameraMoveStarted.bearing
                    && googleMap!!.cameraPosition.target != cameraPositionBeforeCameraMoveStarted.target
                    && !isZoomInOrOutStarted
                ) {
                    this.onMapPanListener().invoke()
                }

                if (googleMap!!.cameraPosition.zoom > cameraPositionBeforeCameraMoveStarted.zoom && isZoomInStarted) {
                    this.onMapZoomInEndListener().invoke(googleMap!!.cameraPosition.zoom.toDouble())
                    isZoomInStarted = false
                }

                if (googleMap!!.cameraPosition.zoom < cameraPositionBeforeCameraMoveStarted.zoom && isZoomOutStarted) {
                    this.onMapZoomOutEndListener().invoke(googleMap!!.cameraPosition.zoom.toDouble())
                    isZoomOutStarted = false
                }

                cameraPositionBeforeCameraMoveStarted = googleMap!!.cameraPosition
                cameraChangeReason = GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
            }
        }
    }


    override fun onMapZoomInEndListener(): (zoomLevel: Double) -> Unit = {
        googleMap!!.uiSettings.isScrollGesturesEnabled = true
        mapFollowsUser = false
    }

    override fun onMapZoomOutEndListener(): (zoomLevel: Double) -> Unit = {
        googleMap!!.uiSettings.isScrollGesturesEnabled = true
        mapFollowsUser = false
    }

    override fun onMapZoomOutStartListener(): (zoomLevel: Double) -> Unit = {}
    override fun onMapZoomInStartListener(): (Double) -> Unit = {}
    override fun onMapPanListener(): () -> Unit = {}
    override fun onMapRotateListener(): () -> Unit = {}

    private fun getMapZoom(): Float {
        return googleMap!!.cameraPosition.zoom
    }

    private fun zoomMapToUser(callback: Runnable = Runnable {}){
        if(gpsManager.currentUserLocation == null)
            return

        googleMap!!.uiSettings.isScrollGesturesEnabled = false
        mapFollowsUser = true

        val bearing: Float = gpsManager.previousUserLocation?.bearingTo(gpsManager.currentUserLocation) ?: 0f

        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    gpsManager.currentUserLocation!!.latitude,
                    gpsManager.currentUserLocation!!.longitude
                )
            )
            .zoom(MAP_DEFAULT_ZOOM)
            .bearing(bearing)
            .tilt(80f)
            .build()

        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object: GoogleMap.CancelableCallback{
            override fun onCancel(){
                callback.run()
            }

            override fun onFinish() {
                callback.run()
            }
        })
    }
}