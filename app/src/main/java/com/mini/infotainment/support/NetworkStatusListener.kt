package com.mini.infotainment.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import java.lang.Boolean


class NetworkStatusListener(private val callback: RunnablePar) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        callback.run(!intent?.extras!!.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
    }
}