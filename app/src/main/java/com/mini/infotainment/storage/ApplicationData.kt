package com.mini.infotainment.storage

import android.content.Context
import android.content.SharedPreferences
import com.mini.infotainment.support.ActivityExtended

object ApplicationData {
    const val DATA_ID = "data"
    const val LOGIN_TIME_ID = "LOGIN_TIME_ID"
    val LOGIN_TIME_DEFAULT: Long = 0

    fun getApplicationData(): SharedPreferences {
        return ActivityExtended.lastActivity.getSharedPreferences(DATA_ID, Context.MODE_PRIVATE)!!
    }

    fun getLastLogin(): Long {
        return getApplicationData().getLong(LOGIN_TIME_ID, LOGIN_TIME_DEFAULT)
    }

    fun setLastLogin(time: Long) {
        val dataEditor = getApplicationData().edit()
        dataEditor.putLong(LOGIN_TIME_ID, time)
        dataEditor.apply()
    }
}
