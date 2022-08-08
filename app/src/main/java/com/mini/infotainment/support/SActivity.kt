package com.mini.infotainment.support

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mini.infotainment.R
import com.mini.infotainment.gps.GPSManager
import com.mini.infotainment.utility.Utility

open class SActivity : AppCompatActivity() {
    var savedInstanceState: Bundle? = null
    var mContentView: View? = null

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var lastActivity: SActivity
        @SuppressLint("StaticFieldLeak")
        lateinit var gpsManager: GPSManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.savedInstanceState = savedInstanceState

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        lastActivity = this
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        mContentView = view!!
    }

    @SuppressLint("MissingPermission", "ResourceType")
    fun setWallpaper(){
        val wallpaperView = findViewById<ViewGroup>(R.id.parent)
        wallpaperView.setBackgroundDrawable(Utility.getWallpaper(this))
    }

    fun pageLoaded(){
        Utility.ridimensionamento(this, findViewById(R.id.parent))
    }

    fun log(event: String){
        Log.i(this.localClassName, event)
    }
}