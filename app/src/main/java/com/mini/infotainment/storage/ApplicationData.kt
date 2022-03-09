package com.mini.infotainment.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mini.infotainment.support.ActivityExtended

object ApplicationData {
    const val DATA_ID = "data"
    const val LOGIN_TIME_ID = "LOGIN_TIME_ID"
    val LOGIN_TIME_DEFAULT: Long = 0

    fun getApplicationData(): SharedPreferences {
        return ActivityExtended.lastActivity.getSharedPreferences(DATA_ID, Context.MODE_PRIVATE)!!
    }

    var lastLogin: Long
        get() {
            val savedJson: String? =
                getApplicationData().getString(LOGIN_TIME_ID, LOGIN_TIME_DEFAULT.toString())
            return Gson().fromJson(savedJson, String::class.java).toLong()
        }
        set(value) {
            val json = Gson().toJson(value)
            val dataEditor = getApplicationData().edit()
            dataEditor.putString(LOGIN_TIME_ID, json)
            dataEditor.apply()
        }
}
