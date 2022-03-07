package com.mini.infotainment.support

import android.app.Activity
import android.os.Bundle

open class ActivityExtended : Activity() {
    companion object{
        lateinit var lastActivity: ActivityExtended
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastActivity = this
    }
}