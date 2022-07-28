package com.mini.infotainment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mini.infotainment.activities.home.HomeActivity

class PackageReceiver : BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {
        // Regenerates the list of the apps, needs to be changed in the future;
        HomeActivity.homeActivity.appsMenu?.build()
    }
}