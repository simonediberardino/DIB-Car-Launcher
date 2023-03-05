package com.mini.infotainment.support

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mini.infotainment.R
import com.mini.infotainment.data.Data
import com.mini.infotainment.gps.GPSManager
import com.mini.infotainment.utility.Utility

open class SActivity : AppCompatActivity() {
    var savedInstanceState: Bundle? = null
    var mContentView: View? = null
    val Context.isMyLauncherDefault: Boolean
        get() = ArrayList<ComponentName>().apply {
            packageManager.getPreferredActivities(
                arrayListOf(IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }),
                this,
                packageName
            )
        }.isNotEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.savedInstanceState = savedInstanceState

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        lastActivity = this
    }

    override fun onResume() {
        super.onResume()
        setWallpaper()
        lastActivity = this
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        mContentView = view!!
    }

    @SuppressLint("MissingPermission", "ResourceType")
    fun setWallpaper(){
        val wallpaperView = findViewById<ViewGroup>(R.id.parent)
        wallpaperView?.setBackgroundDrawable(wpaper)
    }

    fun pageLoaded(){
        Utility.ridimensionamento(this, findViewById(R.id.parent))
        setWallpaper()
    }

    fun log(event: String){
        Log.i(this.localClassName, event)
    }


    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var lastActivity: SActivity
        @SuppressLint("StaticFieldLeak")
        var gpsManager: GPSManager? = null

        val Context.wpaper: Drawable
            get(){
                val defaultBackgroundDrawable = getDrawable(R.drawable.background)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return defaultBackgroundDrawable!!
                }

                return if(Data.useDefaultWP()){
                    defaultBackgroundDrawable!!
                }else{
                    val wallpaperManager = WallpaperManager.getInstance(this)
                    val wallpaperDrawable = wallpaperManager.drawable
                    wallpaperDrawable
                }
            }


        val AppCompatActivity.screenSize: Array<Double>
            get() {
                val displayMetrics = DisplayMetrics()
                this.windowManager.defaultDisplay.getMetrics(displayMetrics)
                return arrayOf(displayMetrics.widthPixels.toDouble(), displayMetrics.heightPixels.toDouble())
            }

        val AppCompatActivity.displayRatio: Double
            get() {
                this.screenSize
                return this.screenSize[1] / Utility.Resolution.BASE_RESOLUTION.y
            }

        val AppCompatActivity.isInternetAvailable: Boolean
            get() {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val nInfo = cm.activeNetworkInfo
                return nInfo != null && nInfo.isAvailable && nInfo.isConnected
            }
    }
}