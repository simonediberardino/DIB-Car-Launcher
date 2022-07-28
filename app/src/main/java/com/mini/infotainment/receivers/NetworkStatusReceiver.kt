package com.mini.infotainment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.mini.infotainment.support.RunnablePar
import java.lang.Boolean


class NetworkStatusReceiver(private val callback: RunnablePar) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        callback.run(!intent?.extras!!.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
    }
}