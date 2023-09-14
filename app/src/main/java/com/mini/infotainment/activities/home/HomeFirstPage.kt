package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.mini.infotainment.support.SActivity.Companion.displayRatio
import com.mini.infotainment.support.SActivity.Companion.gpsManager
import com.mini.infotainment.utility.Utility
import com.mini.infotainment.utility.Utility.kmToMile
import java.util.*


class HomeFirstPage(override val ctx: HomeActivity) : Page(), OnMapReadyCallback {
    private lateinit var tripHandler: TripHandler
    var mapFragment: SupportMapFragment? = null

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
    private lateinit var audioBar: SeekBar
    private lateinit var previousSongBtn: View
    private lateinit var nextSongBtn: View
    private lateinit var pauseSongBtn: View
    internal lateinit var musicAuthorTV: TextView
    internal lateinit var musicTitleTV: TextView
    internal lateinit var addressTV: TextView
    private var audioBarEnabled = false

    @SuppressLint("ClickableViewAccessibility")
    
    override fun build() {
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)
        ctx.viewPages.clear()

        parent = ctx.layoutInflater.inflate(R.layout.activity_home_1, ctx.viewPager, false) as ViewGroup

        musicWidget = parent!!.findViewById(R.id.home_1_music)
        timeTV = parent!!.findViewById(R.id.home_1_datetime)
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
        tripTimeTV = parent!!.findViewById(R.id.home_1_trip_time)
        audioBar = parent!!.findViewById(R.id.music_audiobar)
        nextSongBtn = parent!!.findViewById(R.id.music_next)
        previousSongBtn = parent!!.findViewById(R.id.music_previous)
        pauseSongBtn = parent!!.findViewById(R.id.music_pause)

        ctx.viewPages.add(parent!!)

        createMap()
        setListeners()
        setupTimer()
        updateData()
        registerVolumeChangedReceiver()

        super.pageLoaded()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setListeners() {
        /*musicWidget.setOnTouchListener { v, e ->
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
        }*/

        nextSongBtn.setOnClickListener {
            MusicIntegration.nextTrack(ctx)
        }

        previousSongBtn.setOnClickListener {
            MusicIntegration.previousTrack(ctx)
        }

        pauseSongBtn.setOnClickListener {
            MusicIntegration.togglePlayState(ctx)
        }

        tripTimeTV.setOnClickListener {
            tripHandler.reset()
            updateTripTime()
        }


    }

    fun setSeekBarChangeListener(){
        audioBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                audioBarEnabled = false
                ctx.setVolume(audioBar.progress.toFloat())

                Handler(Looper.getMainLooper()).postDelayed({
                    audioBarEnabled = true
                }, 1000)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })
    }

    fun removeSeekBarChangeListener(){
        audioBar.setOnSeekBarChangeListener(null)
    }

    fun setAudiobar(progress: Int){
        removeSeekBarChangeListener()
        audioBar.setProgress(progress, true)
        setSeekBarChangeListener()
    }

    fun updateData(){
        updateLogoImageView()
        updateTravDist()
        updateSpeed()
        updateTripTime()
    }

    private fun registerVolumeChangedReceiver(){
        updateVolume()

        val intent = IntentFilter()
        intent.addAction("android.media.VOLUME_CHANGED_ACTION")
        val receiver = object: BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                updateVolume()
            }
        }

        ctx.registerReceiver(receiver, intent)
    }

    private fun updateVolume(){
        val am = ctx.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager?
        val volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return

        updateVolume(volumeLevel)
    }

    private fun updateVolume(volume: Int){
        if(audioBar.isPressed) return

        val am = ctx.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager?
        val maxVolume = am?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: return
        val progress = (volume.toFloat() / maxVolume.toFloat()) * 100

        setAudiobar(progress.toInt())
    }

    private fun updateAccel(){
        /*val accel = if(gpsManager != null) gpsManager?.currentAcceleration else 0f
        accelTV.text = accel.toString()*/
    }

    private fun updateSpeed(){
        val temp = (if(gpsManager != null) gpsManager!!.currentSpeed.value else 0f)
        speedometerTV.text = (if(Utility.isUMeasureKM()) temp else temp.kmToMile()).toInt().toString()
        speedUmTV.text = Utility.getSpeedMeasure(ctx)
    }

    private fun updateTravDist(){
        travDist.text = StatsData.getTraveledDistance(StatsData.Mode.DAY).toString()
        distUmTV.text = Utility.getTravDistMeasure(ctx)
    }

    private fun updateLogoImageView(){
        val brandDrawable = Utility.getBrandDrawable(ctx)
        carIcon.visibility = if(brandDrawable == null) View.INVISIBLE else View.VISIBLE
        carIcon.setImageDrawable(brandDrawable)
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
        tripTimeTV.text = tripHandler.elapsedTime
    }
    
    fun createMap(){
        mapFragment = ctx.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment? ?: return
        mapFragment?.onCreate(ctx.savedInstanceState)
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        fun onClickCallback(){
            if(MyCar.instance.isPremium() && gpsManager?.currentUserLocation != null){
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

        if(gpsManager == null) return
        if(gpsManager?.currentUserLocation == null) return

        val minDistToUpdate = 2.5f
        if((gpsManager?.previousUserLocation?.distanceTo(gpsManager?.currentUserLocation ?: return) ?: minDistToUpdate) < minDistToUpdate)
            return

        doOnLocationChanged()
    }

    private fun doOnLocationChanged(){
        if(googleMap == null) return

        val newLocation = gpsManager?.currentUserLocation!!
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
        if(gpsManager == null) return
        if(gpsManager?.currentUserLocation == null)
            return

        val bearing: Float =
            if(gpsManager!!.currentSpeed.value > 2)
                gpsManager!!.previousUserLocation?.bearingTo(gpsManager?.currentUserLocation ?: return) ?: 0f
            else this.bearing

        this.bearing = bearing

        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    gpsManager!!.currentUserLocation!!.latitude,
                    gpsManager!!.currentUserLocation!!.longitude
                )
            )
            .zoom(17f)
            .bearing(this.bearing)
            .tilt(80f)
            .build()

        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

}