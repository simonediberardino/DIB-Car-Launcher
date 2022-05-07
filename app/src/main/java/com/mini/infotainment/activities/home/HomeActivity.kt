package com.mini.infotainment.activities.home

import FirebaseClass
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.*
import com.mini.infotainment.R
import com.mini.infotainment.entities.Car
import com.mini.infotainment.notification.Server
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.*
import com.mini.infotainment.utility.Utility
import java.util.*




class HomeActivity : ActivityExtended() {
    internal val viewPages = mutableListOf<ViewGroup>()
    internal lateinit var viewPager: ViewPager
    internal lateinit var homeButton: View
    internal lateinit var gpsManager: GPSManager
    internal lateinit var locationManager: FusedLocationProviderClient
    internal lateinit var TTS: TextToSpeech
    internal lateinit var homePage0: HomeZeroPage
    internal lateinit var homePage1: HomeFirstPage
    internal lateinit var homePage2: HomeSecondPage
    internal lateinit var homePage3: HomeThirdPage
    internal lateinit var appsMenu: AppsMenu
    internal lateinit var sideMenu: SideMenu

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initializeExceptionHandler()
        initializeLayout()
        initializeSocketServer()
        initializeBroadcastReceiver()
        initializeTTS()
        welcomeUser()
        setupGPS()
    }

    private fun initializeLayout(){
        homePage0 = HomeZeroPage(this).also { it.build() }
        homePage1 = HomeFirstPage(this).also { it.build() }
        homePage2 = HomeSecondPage(this).also { it.build() }
        homePage3 = HomeThirdPage(this).also { it.build() }

        appsMenu = AppsMenu(this).also { it.build() }
        sideMenu = SideMenu(this).also { it.build() }

        val viewPager = findViewById<View>(R.id.home_view_pager) as ViewPager
        viewPager.adapter = PagerAdapter(viewPages)
        viewPager.currentItem = 1
    }

    private fun initializeSocketServer(){
        if(server != null)
            return

        fun callback(){
            server = Server(this)
            server!!.init()
        }

        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(NetworkStatusListener(
            object : RunnablePar {
                override fun run(p: Any?) {
                    if(p == true && server == null)
                        callback()
                }
            }
        ), intentFilter)

        if(Utility.isInternetAvailable(this))
            callback()
    }

    private fun initializeBroadcastReceiver(){
        val filter = IntentFilter()
        filter.addAction("${SpotifyReceiver.SPOTIFY_PACKAGE}.playbackstatechanged")
        filter.addAction("${SpotifyReceiver.SPOTIFY_PACKAGE}.metadatachanged")
        filter.addAction("${SpotifyReceiver.SPOTIFY_PACKAGE}.queuechanged")
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
            mediaPlayer.setOnCompletionListener{
                TTS.speak(ApplicationData.getWelcomeSentence()?.text ?: return@setOnCompletionListener, TextToSpeech.QUEUE_FLUSH, null)
            }
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
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun onLocationChanged(newLocation: Location?){
        if(newLocation == null || !Utility.isInternetAvailable())
            return

        if(gpsManager.currentUserLocation != null){
            gpsManager.previousUserLocation = gpsManager.currentUserLocation
        }

        Car.currentCar.location = newLocation
        gpsManager.currentUserLocation = Car.currentCar.location

        val speedInKmH = Utility.msToKmH(gpsManager.calculateSpeed())
        homePage1.speedometerTW.text = speedInKmH.toString()

        homePage0.onLocationChanged(newLocation)

        if(gpsManager.shouldRefreshAddress()){
            FirebaseClass.updateCarLocation(newLocation)
            gpsManager.lastAddressCheck = System.currentTimeMillis()
            Utility.getSimpleAddress(
                newLocation,
                this,
                object: RunnablePar{
                override fun run(p: Any?) {
                    homePage1.addressTW.text = if(p == null) String() else p as String
                }
            })
        }
    }

    internal fun runSpotify() {
        val intent = Intent(Intent.ACTION_MAIN);
        intent.component = ComponentName(SpotifyReceiver.SPOTIFY_PACKAGE, "${SpotifyReceiver.SPOTIFY_PACKAGE}.MainActivity")

        try{
            startActivity(intent)
        }catch (exception: Exception){
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
        if(Car.currentCar.location == null){
            Errors.printError(Errors.ErrorCodes.GPS_REQUIRED, this)
            return
        }

        val gmmIntentUri: Uri = Uri.parse("geo:${Car.currentCar.location!!.latitude},${Car.currentCar.location!!.longitude}")
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
                homePage1.resumeSpotifyTrack()
            }
        }
    }

    override fun onBackPressed(){
        appsMenu.show(false, SLIDE_ANIMATION_DURATION)
    }

    override fun onResume() {
        super.onResume()
        appsMenu.show(false, 0)
    }

    companion object {
        internal var server: Server? = null
        private var hasWelcomed = false
        private const val GEOLOCATION_PERMISSION_CODE = 1
        const val SLIDE_ANIMATION_DURATION: Long = 300
        const val REQUEST_CODE_SPEECH_INPUT = 10

        fun updateSpotifySong(activity: Activity, intent: Intent){
            val artistName = intent.getStringExtra("artist")
            val trackName = intent.getStringExtra("track")

            if(activity is HomeActivity){
                activity.homePage1.spotifyTitleTW.text = trackName
                activity.homePage1.spotifyAuthorTw.text = artistName
            }
        }
    }
}
