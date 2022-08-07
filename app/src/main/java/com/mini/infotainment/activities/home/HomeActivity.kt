package com.mini.infotainment.activities.home

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.UI.PagerAdapter
import com.mini.infotainment.activities.checkout.CheckoutActivity
import com.mini.infotainment.activities.login.RegisterActivity
import com.mini.infotainment.activities.settings.SettingsActivity
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.errors.Errors
import com.mini.infotainment.errors.ExceptionHandler
import com.mini.infotainment.gps.GPSManager
import com.mini.infotainment.http.SocketServer
import com.mini.infotainment.receivers.NetworkStatusReceiver
import com.mini.infotainment.receivers.SpotifyIntegration
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility


class HomeActivity : ActivityExtended() {
    internal var hasStartedSpotify = false
    internal val viewPages = mutableListOf<ViewGroup>()
    internal lateinit var viewPager: ViewPager
    internal lateinit var gpsManager: GPSManager
    internal lateinit var locationManager: FusedLocationProviderClient
    internal lateinit var sideMenu: SideMenu
    lateinit var homePage0: HomeZeroPage
    lateinit var homePage1: HomeFirstPage
    lateinit var homePage2: HomeSecondPage
    var homePageAds: HomeAdsPage? = null
    var appsMenu: AppsMenu? = null

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this

        super.onCreate(savedInstanceState)
        this.initializeExceptionHandler()

        if(!Utility.hasLoginData()){
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            return
        }

        if(!Utility.areSettingsSet()){
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("isFirstLaunch", true)
            startActivity(intent)
            return
        }

        this.initializeActivity()
    }

    internal fun initializeActivity(){
        this.initializeLayout()
        this.initializeBroadcastReceiver()
        this.setupOnConnectivityChange()
        this.setupGPS()
        this.welcomeUser()
        this.initializeCarObject()
        this.onPremiumAccountListener()
        this.requestStoragePermission()

        if(ApplicationData.doesSpotifyRunOnBoot()){
            Thread{
                Thread.sleep(10000)
                runSpotify()
            }.start()
        }
    }

    private fun initializeCarObject(){
        MyCar.instance = MyCar(ApplicationData.getTarga()!!)
    }

    private fun initializeLayout(){
        this.setContentView(R.layout.activity_home)
        this.setWallpaper()

        homePageAds = HomeAdsPage(this).also { it.build() }
        homePage0 = HomeZeroPage(this).also { it.build() }
        homePage1 = HomeFirstPage(this).also { it.build() }
        homePage2 = HomeSecondPage(this).also { it.build() }
        appsMenu = AppsMenu(this).also { it.build() }
        sideMenu = SideMenu(this).also { it.build() }

        this.generateViewPager()
    }

    fun generateViewPager(){
        val viewPager = findViewById<View>(R.id.home_view_pager) as ViewPager
        viewPager.removeAllViews()
        viewPager.adapter = PagerAdapter(viewPages)
        viewPager.currentItem = 2
    }

    private fun setupOnConnectivityChange(){
        fun callback(){
            server?.serverSocket?.close()
            SocketServer(this).also {
                server = it
            }.init()
        }

        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(NetworkStatusReceiver(
            object : RunnablePar {
                override fun run(p: Any?) {
                    // Restarts/Starts the socket when internet is available
                    if(p == true)
                        callback()
                }
            }
        ), intentFilter)
    }

    private fun initializeBroadcastReceiver(){
        val filter = IntentFilter()
        filter.addAction("${SpotifyIntegration.SPOTIFY_PACKAGE}.playbackstatechanged")
        filter.addAction("${SpotifyIntegration.SPOTIFY_PACKAGE}.metadatachanged")
        filter.addAction("${SpotifyIntegration.SPOTIFY_PACKAGE}.queuechanged")
        registerReceiver(SpotifyIntegration(), filter)
    }

    private fun initializeExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
    }

    private fun requestStoragePermission(){
        val READ_EXTERNAL_STORAGE_ID = 1005
        if(ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_ID)
        }
    }

    private fun welcomeUser(){
        //MediaPlayer.create(this, R.raw.startup_sound).start()
    }

    @SuppressLint("MissingPermission", "ResourceType")
    fun setWallpaper(){
        val wallpaperView = findViewById<ViewGroup>(R.id.parent)
        wallpaperView.setBackgroundDrawable(Utility.getWallpaper(this))
    }

    private fun onPremiumAccountListener(){
        FirebaseClass.getPremiumDateReference().addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                MyCar.instance.premiumDate = snapshot.value as Long? ?: return
                homePageAds?.handleAds()
                if(MyCar.instance.premiumDate != 0L && !MyCar.instance.isPremium()){
                    premiumExpired()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun premiumExpired(){
        CustomToast(getString(R.string.premium_expired), this).show()
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
    }

    private fun onLocationChanged(newLocation: Location?){
        if(newLocation == null || !Utility.isInternetAvailable() || ApplicationData.getTarga() == null)
            return

        handleGPSLocation(newLocation)
        handleSpeedReport()
        handleAddressReport()
    }

    private fun handleGPSLocation(newLocation: Location?){
        if(gpsManager.currentUserLocation != null){
            gpsManager.previousUserLocation = gpsManager.currentUserLocation
        }

        gpsManager.currentUserLocation = newLocation

        StatsData.increaseTraveledDistance(
            gpsManager.previousUserLocation?.distanceTo(gpsManager.currentUserLocation) ?: 0f
        )

        homePage1.updateTravSpeed()
        homePage0.onLocationChanged(gpsManager.currentUserLocation ?: return)
    }

    private fun handleSpeedReport(){
        val speedInKmH = Utility.msToKmH(gpsManager.calculateSpeed())
        homePage1.speedometerTW.text = speedInKmH.toString()

        if(speedInKmH > 2)
            StatsData.addSpeedReport(speedInKmH.toFloat())
    }

    private fun handleAddressReport() {
        if(!gpsManager.shouldRefreshAddress())
            return

        FirebaseClass.updateCarLocation(gpsManager.currentUserLocation ?: return)
        gpsManager.lastAddressCheck = System.currentTimeMillis()

        Utility.getSimpleAddress(
            gpsManager.currentUserLocation ?: return,
            this,
            object: RunnablePar{
                override fun run(p: Any?) {
                    homePage1.addressTW.text = if(p == null) String() else p as String
                }
            }
        )
    }

    internal fun premiumFeature(callback: Runnable){
        if(!MyCar.instance.isPremium()){
            goToCheckout()
        }else callback.run()
    }

    fun goToCheckout(){
        Utility.navigateTo(this@HomeActivity, CheckoutActivity::class.java)
    }

    internal fun runSpotify() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.component = ComponentName(SpotifyIntegration.SPOTIFY_PACKAGE, "${SpotifyIntegration.SPOTIFY_PACKAGE}.MainActivity")

        try {
            startActivity(intent)
        } catch (exception: Exception) {
            Errors.printError(Errors.ErrorCodes.APP_NOT_INSTALLED, this)
        }
    }

    internal fun runFileManager(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, 1000)
    }

    internal fun runYoutube(){
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/")
            )
        )
    }

    internal fun runGoogleMaps(){
        val location = gpsManager.currentUserLocation
        if(location == null){
            Errors.printError(Errors.ErrorCodes.GPS_REQUIRED, this)
            return
        }

        val gmmIntentUri: Uri = Uri.parse("geo:${location.latitude},${location.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try{
            startActivity(mapIntent)
        }catch (exception: Exception){
            Errors.printError(Errors.ErrorCodes.APP_NOT_INSTALLED, this)
        }
    }

    internal fun runGoogleAssistant(){
        try{
            startActivity(Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }catch (exception: Exception){
            Errors.printError(Errors.ErrorCodes.APP_NOT_INSTALLED, this)
        }
    }

    internal fun runSettings(){
        startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), 0)
    }

    private fun insufficientPermissions(){
        Errors.printError(Errors.ErrorCodes.GPS_REQUIRED, this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                server?.notificationHandler?.onVoiceTextReceived(result?.get(0))
            }
        }
    }

    override fun onBackPressed(){
        appsMenu?.show(false, SLIDE_ANIMATION_DURATION)
    }

    override fun onResume() {
        super.onResume()
        appsMenu?.show(false, 0)
    }

    companion object {
        lateinit var instance: HomeActivity
        internal var server: SocketServer? = null
        private const val GEOLOCATION_PERMISSION_CODE = 1
        const val SLIDE_ANIMATION_DURATION: Long = 300
        const val REQUEST_CODE_SPEECH_INPUT = 10

        fun updateSpotifySong(activity: Activity, intent: Intent){
            val artistName = intent.getStringExtra("artist")
            val trackName = intent.getStringExtra("track")

            if(activity is HomeActivity){
                instance.homePage1.spotifyTitleTW.text = trackName
                instance.homePage1.spotifyAuthorTw.text = artistName
            }
        }
    }
}
