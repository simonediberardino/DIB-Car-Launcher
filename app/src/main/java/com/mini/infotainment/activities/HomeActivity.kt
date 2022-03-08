package com.mini.infotainment.activities

import android.Manifest
import android.annotation.SuppressLint
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
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import com.mini.infotainment.entities.Car
import android.content.ComponentName
import com.mini.infotainment.support.*
import android.content.IntentFilter
import androidx.viewpager.widget.ViewPager
import kotlin.collections.ArrayList
import androidx.core.app.ActivityCompat.startActivityForResult





class HomeActivity : ActivityExtended() {
    internal lateinit var viewGroups: ArrayList<ViewGroup>
    internal var isAppDrawerVisible = false
    internal lateinit var grdView: GridView
    internal lateinit var containerHome: ConstraintLayout
    internal lateinit var containAppDrawer: ConstraintLayout
    internal lateinit var speedometerTW: TextView
    internal lateinit var addressTW: TextView
    internal lateinit var timeTW: TextView
    internal lateinit var spotifyWidget: View
    internal lateinit var homeButton: View
    internal lateinit var gpsManager: GPSManager
    internal lateinit var locationManager: FusedLocationProviderClient
    internal lateinit var TTS: TextToSpeech

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        buildFirstPage()
        buildSecondPage()

        initializeHomePager()
        initializeLayout()
        initializeBroadcastReceiver()
        initializeExceptionHandler()
        initializeTTS()
        welcomeUser()
    }

    private fun buildFirstPage(){
        viewGroups = ArrayList()

        val parent = findViewById<ViewGroup>(R.id.home_view_pager)
        val layout = layoutInflater.inflate(R.layout.activity_home_1, parent, false) as ViewGroup

        spotifyWidget = layout.findViewById(R.id.home_1_spotify)
        timeTW = layout.findViewById(R.id.home_1_datetime)
        speedometerTW = layout.findViewById(R.id.home_1_speed)
        addressTW = layout.findViewById(R.id.home_1_address)
        viewGroups.add(layout)
    }

    private fun buildSecondPage(){
        val parent = findViewById<ViewGroup>(R.id.home_view_pager)
        val layout = layoutInflater.inflate(R.layout.activity_home_2, parent, false) as ViewGroup
        val gridView = layout.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.home_2_grid)

        class GridButton(name: String, drawableId: Int, val callback: Runnable){
            init{
                val singleItem = layoutInflater.inflate(R.layout.home_2_items, layout, false) as ViewGroup
                val itemNameTW = singleItem.findViewById<TextView>(R.id.home_2_item_name)
                val itemNameIW = singleItem.findViewById<ImageView>(R.id.home_2_item_image)

                itemNameTW.text = name
                itemNameIW.setImageResource(drawableId)
                singleItem.setOnClickListener {
                    callback.run()
                }

                gridView.addView(singleItem)
            }
        }

        GridButton(getString(R.string.menu_navigatore), R.drawable.menu_navigation) { runGoogleMaps() }
        GridButton(getString(R.string.menu_voice), R.drawable.menu_voice) { runGoogleAssistant() }
        GridButton(getString(R.string.menu_spotify), R.drawable.menu_spotify) { runSpotify() }
        GridButton(getString(R.string.menu_youtube), R.drawable.menu_youtube) { runYoutube() }
        GridButton(getString(R.string.menu_storage), R.drawable.menu_storage) { runFileManager() }
        GridButton(getString(R.string.menu_settings), R.drawable.menu_settings) { runSettings() }

        viewGroups.add(layout)
    }

    private fun initializeHomePager(){
        val viewPager = findViewById<View>(R.id.home_view_pager) as ViewPager
        viewPager.adapter = HomePagerAdapter(this, viewGroups)
    }

    private fun initializeLayout(){
        containAppDrawer = findViewById(R.id.containAppDrawer)
        containAppDrawer.visibility = View.INVISIBLE
        containerHome = findViewById(R.id.home_container)
        homeButton = findViewById(R.id.home_swipe)

        apps = null
        adapter = null

        slideMenuDown(0)
        loadApps()
        loadListView()
        addGridListeners()
        addHomeListeners()
        setupGPS()
        setupTimer()
        loadSideMenu()
    }


    private fun initializeBroadcastReceiver(){
        val filter = IntentFilter()
        filter.addAction("com.spotify.music.playbackstatechanged")
        filter.addAction("com.spotify.music.metadatachanged")
        filter.addAction("com.spotify.music.queuechanged")
        registerReceiver(SpotifyReceiver(), filter)
    }

    private fun initializeExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
    }

    private fun welcomeUser(){
        if(!hasWelcomed){
            hasWelcomed = true

            val mediaPlayer = MediaPlayer.create(this, R.raw.startup_sound)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener{}
        }
    }

    private fun initializeTTS(){
        TTS = TextToSpeech(this) {}
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
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

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
    private fun onLocationChanged(newLocation: Location?){
        if(newLocation == null)
            return

        if(gpsManager.currentUserLocation != null){
            gpsManager.previousUserLocation = gpsManager.currentUserLocation
        }

        Car.currentCar.location = newLocation
        gpsManager.currentUserLocation = Car.currentCar.location

        val speedInKmH = Utility.msToKmH(gpsManager.calculateSpeed())
        speedometerTW.text = speedInKmH.toString()

        if(gpsManager.shouldRefreshAddress()){
            gpsManager.lastAddressCheck = System.currentTimeMillis().toInt()
            Utility.getSimpleAddress(newLocation, this, object: RunnablePar{
                override fun run(p: Any?) {
                    addressTW.text = if(p == null) String() else p as String
                }
            })
        }
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

    private fun updateTime(){
        runOnUiThread {
            timeTW.text = Utility.getTime()
        }
    }

    private fun addHomeListeners(){
        homeButton.setOnClickListener {
            showAppDrawer(true, SLIDE_ANIMATION_DURATION)
        }

        spotifyWidget.setOnClickListener {
            runSpotify()
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
            grdView = findViewById<View>(R.id.grid_apps) as GridView
            if (adapter == null) {
                adapter = object : ArrayAdapter<AppInfo>(this, R.layout.menu_grid, apps!!) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        var convertView = convertView
                        var viewHolder: Any?

                        if (convertView == null) {
                            convertView = layoutInflater.inflate(R.layout.menu_grid, parent, false)
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

    private fun showAppDrawer(visibility: Boolean, duration: Long) {
        if(visibility == isAppDrawerVisible)
            return

        grdView.visibility = if(visibility) View.VISIBLE else View.INVISIBLE
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
            .alpha(0.92f)
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
        background.alpha = 0.92f
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
                SideMenuButton(getString(R.string.menu_spotify)) { runSpotify() },
                SideMenuButton(getString(R.string.menu_navigatore)) { runGoogleMaps() },
                SideMenuButton(getString(R.string.menu_voice)) { runGoogleAssistant() },
                SideMenuButton(getString(R.string.settings)) { runSettings() }
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

    private fun runSpotify() {
        val intent = Intent(Intent.ACTION_MAIN);
        intent.component = ComponentName("com.spotify.music", "com.spotify.music.MainActivity")

        try{
            startActivity(intent)
        }catch (exception: Exception){
            Utility.showToast(this, getString(R.string.app_not_installed))
        }
    }

    private fun runFileManager(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, 1000)
    }

    private fun runYoutube(){
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/")
            )
        )
    }

    private fun runGoogleMaps(){
        if(Car.currentCar.location == null){
            Utility.showToast(this, getString(R.string.gps_not_enabled))
            return
        }

        val gmmIntentUri: Uri = Uri.parse("geo:${Car.currentCar.location!!.latitude},${Car.currentCar.location!!.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try{
            startActivity(mapIntent)
        }catch (exception: Exception){
            Utility.showToast(this, getString(R.string.app_not_installed))
        }
    }

    private fun runGoogleAssistant(){
        try{
            startActivity(Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }catch (exception: Exception){
            Utility.showToast(this, getString(R.string.app_not_installed))
        }
    }

    private fun runSettings(){
        startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), 0)
    }

    private fun insufficientPermissions(){
        Utility.showToast(this, getString(R.string.request_permissions))
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

    override fun onBackPressed(){
        showAppDrawer(false, SLIDE_ANIMATION_DURATION)
    }

    override fun onResume() {
        super.onResume()
        showAppDrawer(false, 0)
    }

    companion object {
        private var hasWelcomed = false
        private const val GEOLOCATION_PERMISSION_CODE = 1
        private const val SLIDE_ANIMATION_DURATION: Long = 300
        private var spotifyTitleTW: TextView? = null
        private var spotifyAuthorTw: TextView? = null
        var apps: MutableList<AppInfo>? = null
        var adapter: ArrayAdapter<AppInfo>? = null

        fun updateSpotifySong(intent: Intent){
            val artistName = intent.getStringExtra("artist")
            val trackName = intent.getStringExtra("track")

            spotifyTitleTW?.text = trackName
            spotifyAuthorTw?.text = artistName
        }
    }
}
