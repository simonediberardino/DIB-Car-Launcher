package com.mini.infotainment.activities.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.UI.PagerAdapter
import com.mini.infotainment.UI.SwipeHandler
import com.mini.infotainment.activities.checkout.CheckoutActivity
import com.mini.infotainment.activities.login.access.LoginViewModel
import com.mini.infotainment.activities.misc.FakeLauncherActivity
import com.mini.infotainment.activities.settings.SettingsActivity
import com.mini.infotainment.ads.AdHandler
import com.mini.infotainment.ads.VideoInterstitial
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.errors.Errors
import com.mini.infotainment.errors.ExceptionHandler
import com.mini.infotainment.gps.GPSManager
import com.mini.infotainment.http.SocketServer
import com.mini.infotainment.receivers.NetworkStatusReceiver
import com.mini.infotainment.receivers.SpotifyIntegration
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility


class HomeActivity : SActivity() {
    internal var hasStartedSpotify = false
    internal val viewPages = mutableListOf<ViewGroup>()
    internal lateinit var viewPager: ViewPager
    internal lateinit var sideMenu: SideMenu
    lateinit var homePage1: HomeFirstPage
    lateinit var homePage2: HomeSecondPage
    private var premiumAccountEventListener: ValueEventListener? = null
    private var passWordEventListener: ValueEventListener? = null
    var appsMenu: AppsMenu? = null

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this

        super.onCreate(savedInstanceState)
        this.initializeExceptionHandler()

        /*if(!hasLoginData()) {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            return
        }*/

        this.initializeCarObject()

        if(!areSettingsSet()){
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("isFirstLaunch", true)
            startActivity(intent)
            return
        }

        this.initializeActivity()
    }

    internal fun initializeActivity(){
        this.initializeLayout()
        this.initializeSpotifyBroadcastReceiver()
        this.setupOnConnectivityChange()
        this.setupGPS()
        this.addFirebaseListeners()
        this.initializeAdsHandler()
        this.requestDefaultLauncher()
        this.updateSettings()
    }

    fun addFirebaseListeners(){
        this.removeFirebaseListeners()
        this.onPremiumAccountListener()
        this.onPasswordChangedListener()
    }

    fun removeFirebaseListeners(){
        premiumAccountEventListener?.let {
            FirebaseClass.getPremiumDateReference()?.removeEventListener(
                it
            )
        }
        passWordEventListener?.let { FirebaseClass.getPasswordReference()?.removeEventListener(it) }
    }

    private fun requestDefaultLauncher() {
        if(isMyLauncherDefault) return

        val packageManager: PackageManager = packageManager
        val componentName = ComponentName(this, FakeLauncherActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_HOME)
        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(selector)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun initializeCarObject(){
        MyCar.instance = MyCar()
    }

    private fun initializeAdsHandler(){
        AdHandler(this, VideoInterstitial::class.java).startTimeout()
    }

    private fun initializeLayout(){
        this.setContentView(R.layout.activity_home)
        this.setWallpaper()

        homePage1 = HomeFirstPage(this).also { it.build() }
        homePage2 = HomeSecondPage(this).also { it.build() }
        appsMenu = AppsMenu(this).also { it.build() }
        sideMenu = SideMenu(this).also { it.build() }

        this.generateViewPager()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateViewPager(){
        viewPager.removeAllViews()
        viewPager.adapter = PagerAdapter(viewPages)
        viewPager.currentItem = 0

        var swipeHandler: SwipeHandler? = null
        viewPager.setOnTouchListener { _, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_UP)
                swipeHandler = SwipeHandler(motionEvent, this)

            else if(swipeHandler?.wasSwipeUp(motionEvent) == true)
                appsMenu?.show(true)
            false
        }
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

    private fun initializeSpotifyBroadcastReceiver(){
        val intent1 = IntentFilter()
        intent1.addAction("${SpotifyIntegration.SPOTIFY_PACKAGE}.playbackstatechanged")
        intent1.addAction("${SpotifyIntegration.SPOTIFY_PACKAGE}.metadatachanged")
        intent1.addAction("${SpotifyIntegration.SPOTIFY_PACKAGE}.queuechanged")
        registerReceiver(SpotifyIntegration(), intent1)
    }

    private fun initializeExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
    }

    private fun onPasswordChangedListener(){
        if(!ApplicationData.isLogged())
            return

        passWordEventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val dbPass = snapshot.value as String? ?: return

                if(ApplicationData.getUserPassword() != dbPass){
                    LoginViewModel.doLogout()
                    Utility.toast(this@HomeActivity, this@HomeActivity.getString(R.string.disconnected_pw_changed))

                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        FirebaseClass.getPasswordReference()?.addValueEventListener(passWordEventListener!!)
    }

    private fun onPremiumAccountListener(){
        if(!ApplicationData.isLogged())
            return

        premiumAccountEventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                MyCar.instance.premiumDate = snapshot.value as Long? ?: return
                if(MyCar.instance.premiumDate != 0L && !MyCar.instance.isPremium()){
                    premiumExpired()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        FirebaseClass.getPremiumDateReference()?.addValueEventListener(premiumAccountEventListener!!)
    }

    private fun premiumExpired(){
        CustomToast(getString(R.string.premium_expired), this)
    }

    private fun setupGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                GEOLOCATION_PERMISSION_CODE
            )
        }else{
            this.initializeGpsManager()
        }
    }

    private fun initializeGpsManager(){
        gpsManager = GPSManager(this).also{ it.init() }
        this.addGpsCallback()
    }

    private fun addGpsCallback(){
        gpsManager?.callbacks?.set(packageName, (object: RunnablePar{
            override fun run(p: Any?) {
                this@HomeActivity.onLocationChanged()
            }
        }))
    }

    private fun onLocationChanged() {
        handleGPSLocation()
        handleAddressReport()
    }

    private fun handleGPSLocation(){
        if(this::homePage1.isInitialized)
            homePage1.onLocationChanged()
    }

    private fun handleAddressReport() {
        FirebaseClass.isPremiumCar(object: RunnablePar{
            override fun run(p: Any?) {
                if(gpsManager?.shouldRefreshAddress() != true || p != true)
                    return

                FirebaseClass.updateCarLocation(gpsManager?.currentUserLocation ?: return)
                gpsManager?.lastAddressCheck = System.currentTimeMillis()

                gpsManager?.getSimpleAddress(
                    gpsManager?.currentUserLocation ?: return,
                    object: RunnablePar{
                        override fun run(p: Any?) {
                            homePage1.addressTV.text = if(p == null) String() else p as String
                        }
                    }
                )
            }
        })

    }

    private fun updateSettings(){
        MyCar.instance.carbrand = ApplicationData.getBrandName().toString()
        MyCar.instance.plateNum = ApplicationData.getUserName().toString()
        FirebaseClass.updateCarBrand(MyCar.instance.carbrand)
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
    
    internal fun runYoutube(){
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/")
            )
        )
    }

    internal fun runSettings(){
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun insufficientPermissions(){
        Errors.printError(Errors.ErrorCodes.GPS_REQUIRED, this)
    }

    private fun areSettingsSet(): Boolean {
        return ApplicationData.getBrandName() != null
    }

    private fun hasLoginData(): Boolean {
        return ApplicationData.getUserPassword().toString() != "null" && ApplicationData.getUserName().toString() != "null"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.isEmpty())
            return

        when(requestCode){
            GEOLOCATION_PERMISSION_CODE -> {
                if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    this.insufficientPermissions()
                }else{
                    this.initializeGpsManager()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                server?.notificationHandler?.onVoiceTextReceived(result?.get(0))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if(gpsManager != null){
            addGpsCallback()
        }

        if(this::homePage1.isInitialized){
            if(homePage1.mapFragment?.isVisible != true)
                homePage1.createMap()
        }
    }

    override fun onBackPressed(){
        appsMenu?.show(false)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: HomeActivity? = null
        internal var server: SocketServer? = null
        private const val GEOLOCATION_PERMISSION_CODE = 1
        const val REQUEST_CODE_SPEECH_INPUT = 10

        fun updateSong(intent: Intent){
            val artistName = intent.getStringExtra("artist")
            val trackName = intent.getStringExtra("track")

            instance?.homePage1?.musicTitleTV?.text = trackName
            instance?.homePage1?.musicAuthorTV?.text = artistName
        }
    }
}
