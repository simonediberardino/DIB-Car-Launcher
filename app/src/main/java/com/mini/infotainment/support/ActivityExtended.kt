package com.mini.infotainment.support

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mini.infotainment.R
import com.mini.infotainment.utility.Utility

open class ActivityExtended : AppCompatActivity() {
    companion object{
        lateinit var lastActivity: ActivityExtended
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        lastActivity = this
    }

    fun pageLoaded(){
        Utility.ridimensionamento(this, findViewById(R.id.parent))
    }

    fun log(event: String){
        Log.i(this.localClassName, event)
    }
}