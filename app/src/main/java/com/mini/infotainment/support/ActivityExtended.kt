package com.mini.infotainment.support

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

open class ActivityExtended : Activity() {
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
}