package com.mini.infotainment.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.mini.infotainment.R
import java.lang.Exception
import java.util.*

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.mini.infotainment.utility.Utility
import android.media.AudioManager
import android.net.Uri
import com.mini.infotainment.support.GPSManager


class HomeActivity : Activity() {
    internal var isAppDrawerVisible = false
    internal lateinit var grdView: GridView
    internal lateinit var containerHome: ConstraintLayout
    internal lateinit var containAppDrawer: ConstraintLayout
    internal lateinit var locationManager: FusedLocationProviderClient
    internal var carLocation: Location? = null
    internal lateinit var gpsManager: GPSManager

    override fun onResume() {
        super.onResume()
        showAppDrawer(false, 0)
    }

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLayout()
    }

    private fun initializeLayout(){
        setContentView(R.layout.activity_home)

        containAppDrawer = findViewById(R.id.containAppDrawer)
        containAppDrawer.visibility = View.INVISIBLE
        containerHome = findViewById(R.id.home_container)

        slideMenuDown(0)

        apps = null
        adapter = null

        loadApps()
        loadListView()
        addGridListeners()
        setupGPS()
        setupTimer()
        loadSideMenu()
    }

    private fun setupGPS() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                GEOLOCATION_PERMISSION_CODE
            )
        }else{
            this.setupUserLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private fun setupUserLocation(){
        gpsManager = GPSManager()

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1
        locationRequest.fastestInterval = 1
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                this@HomeActivity.onLocationChanged(locationResult?.lastLocation)
            }
        }

        locationManager = LocationServices.getFusedLocationProviderClient(this)
        locationManager.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        locationManager.lastLocation.addOnSuccessListener(this) {
            this.onLocationChanged(it)
        }.addOnFailureListener(this) {
            this.insufficientPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onLocationChanged(newLocation: Location?){
        if(newLocation == null)
            return

        if(gpsManager.currentUserLocation != null){
            gpsManager.previousUserLocation = gpsManager.currentUserLocation
        }

        carLocation = newLocation
        gpsManager.currentUserLocation = carLocation

        val speedometerTW = findViewById<TextView>(R.id.home_speed)
        val speedInKmH = Utility.msToKmH(gpsManager.calculateSpeed())
        speedometerTW.text = speedInKmH.toString()

        val addressTW = findViewById<TextView>(R.id.home_address)
        addressTW.text = Utility.getSimpleAddress(newLocation, this)
    }

    private fun setupTimer(){
        Thread{
            while(true){
                try{
                    Thread.sleep(1)
                    updateTime()
                }catch (exception: Exception){}
            }
        }.start()

    }

    fun updateTime(){
        runOnUiThread {
            val timeTW = findViewById<TextView>(R.id.home_datetime)
            timeTW.text = Utility.getTime()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            GEOLOCATION_PERMISSION_CODE -> {
                if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    this.insufficientPermissions()
                }else{
                    this.setupUserLocation()
                }
            }
        }
    }

    private fun addGridListeners() {
        try {
            grdView.onItemClickListener =
                OnItemClickListener { _, _, i, _ ->
                    val intent = packageManager!!.getLaunchIntentForPackage(apps!![i].name.toString())
                    this@HomeActivity.startActivity(intent)
                }
        } catch (ex: Exception) {}
    }

    private fun loadListView() {
        try {
            grdView = findViewById<View>(R.id.grd_allApps) as GridView
            if (adapter == null) {
                adapter = object : ArrayAdapter<AppInfo>(this, R.layout.grd_items, apps!!) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        var convertView = convertView
                        var viewHolder: Any?

                        if (convertView == null) {
                            convertView = layoutInflater.inflate(R.layout.grd_items, parent, false)
                            viewHolder = ViewHolderItem()

                            viewHolder.icon = convertView!!.findViewById(R.id.img_icon)
                            viewHolder.name = convertView.findViewById(R.id.txt_name)
                            viewHolder.label = convertView.findViewById(R.id.txt_label)
                            convertView.tag = viewHolder
                        } else {
                            viewHolder = convertView.tag as ViewHolderItem
                        }

                        val appInfo = apps!![position]
                        viewHolder.icon!!.setImageDrawable(appInfo.icon)
                        viewHolder.label!!.text = appInfo.label
                        viewHolder.name!!.text = appInfo.name

                        return convertView
                    }

                    inner class ViewHolderItem {
                        var icon: ImageView? = null
                        var label: TextView? = null
                        var name: TextView? = null
                    }
                }
            }
            grdView.adapter = adapter
        } catch (ex: Exception) {}
    }

    private fun loadApps() {
        try {
            if (apps == null) {
                apps = ArrayList()
                val i = Intent(Intent.ACTION_MAIN, null)
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                val availableApps = packageManager!!.queryIntentActivities(i, 0)
                for (ri in availableApps) {
                    val appinfo = AppInfo()
                    appinfo.label = ri.loadLabel(packageManager)
                    appinfo.name = ri.activityInfo.packageName
                    appinfo.icon = ri.activityInfo.loadIcon(packageManager)
                    (apps as ArrayList<AppInfo>).add(appinfo)
                }
            }
        }catch (ex: Exception) {}
    }

    fun showApps(v: View?) {
        showAppDrawer(true, SLIDE_ANIMATION_DURATION)
    }

    private fun showAppDrawer(visibility: Boolean, duration: Long) {
        if(visibility == isAppDrawerVisible)
            return

        isAppDrawerVisible = visibility

        if (visibility) {
            slideMenuUp(duration)
        } else {
            slideMenuDown(duration)
        }
    }

    private fun slideMenuUp(duration: Long) {
        val background = findViewById<View>(R.id.home_background)
        background.alpha = 0f
        background
            .animate()
            .alpha(0.6f)
            .duration = duration

        containAppDrawer.visibility = View.VISIBLE

        val slideAnimation = TranslateAnimation(
            0f,
            0f,
            containAppDrawer.height.toFloat(),
            0f
        )

        slideAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                containAppDrawer.visibility = View.VISIBLE
            }
        })

        slideAnimation.duration = duration
        slideAnimation.fillAfter = false
        containAppDrawer.startAnimation(slideAnimation)
    }

    private fun slideMenuDown(duration: Long) {
        val background = findViewById<View>(R.id.home_background)
        background.alpha = 0.6f
        background
            .animate()
            .alpha(0f)
            .duration = duration

        val slideAnimation = TranslateAnimation(
            0f,
            0f,
            0f,
            containAppDrawer.height.toFloat()
        )

        slideAnimation.duration = duration
        slideAnimation.fillAfter = false
        slideAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationEnd(p0: Animation?) {
                containAppDrawer.visibility = View.INVISIBLE
            }

            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?){}
        })

        containAppDrawer.startAnimation(slideAnimation)
    }

    private fun loadSideMenu(){
        class SideMenuButton(val title: String, val listener: Runnable)

        val buttons = arrayOf(
                SideMenuButton(getString(R.string.google_maps)) { runGoogleMaps() },
                SideMenuButton(getString(R.string.volume_up)) { turnUpVolume() },
                SideMenuButton(getString(R.string.volume_down)) { turnDownVolume() }
            )

        val parent = findViewById<LinearLayout>(R.id.home_sidemenu)

        for(button : SideMenuButton in buttons){
            val inflatedView = layoutInflater.inflate(R.layout.menu_items, parent, false)
            val titleTW = inflatedView.findViewById<TextView>(R.id.menu_title)

            titleTW.text = button.title
            inflatedView.setOnClickListener {
                button.listener.run()
            }

            parent.addView(inflatedView)
        }
    }

    private fun runGoogleMaps(){
        if(carLocation == null){
            Utility.showToast(this, getString(R.string.gps_not_enabled))
            return
        }

        val gmmIntentUri: Uri = Uri.parse("geo:${carLocation!!.latitude},${carLocation!!.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try{
            startActivity(mapIntent)
        }catch (exception: Exception){
            Utility.showToast(this, getString(R.string.maps_not_installed))
        }
    }

    private fun turnUpVolume(){
        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
    }

    private fun turnDownVolume(){
        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
    }

    private fun insufficientPermissions(){
        Utility.showToast(this, getString(R.string.request_permissions))
    }

    override fun onBackPressed(){
        showAppDrawer(false, SLIDE_ANIMATION_DURATION)
    }

    companion object {
        private const val GEOLOCATION_PERMISSION_CODE = 1
        private const val SLIDE_ANIMATION_DURATION: Long = 300
        private var spotifyTitleTW: TextView? = null
        private var spotifyAuthorTw: TextView? = null
        var apps: MutableList<AppInfo>? = null
        var adapter: ArrayAdapter<AppInfo>? = null

        fun updateSpotifySong(intent: Intent){
            val trackId = intent.getStringExtra("id")
            val artistName = intent.getStringExtra("artist")
            val albumName = intent.getStringExtra("album")
            val trackName = intent.getStringExtra("track")
            val trackLengthInSec = intent.getIntExtra("length", 0)

            spotifyTitleTW?.text = trackName
            spotifyAuthorTw?.text = artistName
        }
    }
}
