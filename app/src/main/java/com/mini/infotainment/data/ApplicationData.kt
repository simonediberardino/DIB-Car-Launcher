package com.mini.infotainment.data

import android.content.Context
import android.content.SharedPreferences
import android.opengl.GLException
import com.google.gson.Gson
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.utility.Utility

object ApplicationData {
    private const val DATA_ID = "data"
    private const val NOTIFICHE_ID = "NOTIFICHE_ID"
    private val NOTIFICHE_DEFAULT = true
    private const val TARGA_ID = "TARGA_ID"
    private val TARGA_DEFAULT = null
    private const val BRAND_ID = "BRAND_ID"
    private val BRAND_DEFAULT = null
    private const val CONSUPTION_ID = "CONSUPTION_ID"
    private val CONSUPTION_DEFAULT = null
    private const val SPOTIFY_ID = "SPOTIFY_ID"
    private val SPOTIFY_DEFAULT = false
    private const val WP_ID = "WP_ID"
    private val WP_DEFAULT = true
    private const val STATS_ID = "STATS-ID"
    private val STATS_DEFAULT = "{}"

    private val applicationData: SharedPreferences
        get() {
            return ActivityExtended.lastActivity.getSharedPreferences(
                DATA_ID,
                Context.MODE_PRIVATE
            )!!
        }

    fun areNotificationsEnabled(): Boolean {
        return applicationData.getBoolean(NOTIFICHE_ID, NOTIFICHE_DEFAULT)
    }

    fun areNotificationsEnabled(boolean: Boolean){
        val dataEditor = applicationData.edit()
        dataEditor.putBoolean(NOTIFICHE_ID, boolean)
        dataEditor.apply()
    }

    fun doesSpotifyRunOnBoot(): Boolean {
        return applicationData.getBoolean(SPOTIFY_ID, SPOTIFY_DEFAULT)
    }

    fun doesSpotifyRunOnBoot(boolean: Boolean){
        val dataEditor = applicationData.edit()
        dataEditor.putBoolean(SPOTIFY_ID, boolean)
        dataEditor.apply()
    }

    fun useDefaultWP(): Boolean {
        return applicationData.getBoolean(WP_ID, WP_DEFAULT)
    }

    fun useDefaultWP(boolean: Boolean){
        val dataEditor = applicationData.edit()
        dataEditor.putBoolean(WP_ID, boolean)
        dataEditor.apply()
    }

    fun getBrandName(): String? {
        return applicationData.getString(BRAND_ID, BRAND_DEFAULT)
    }

    fun setBrandName(brand: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(BRAND_ID, brand)
        dataEditor.apply()
    }

    fun getStats(): HashMap<String, String> {
        return Utility.jsonStringToObject(
            applicationData.getString(STATS_ID, STATS_DEFAULT)!!
        )
    }

    fun setStats(stats: HashMap<String, String>){
        setStats(
            Utility.objectToJsonString(stats)
        )
    }

    fun setStats(stats: String){
        val dataEditor = applicationData.edit()
        dataEditor.putString(STATS_ID, stats)
        dataEditor.apply()
    }

    fun getFuelConsuption(): String? {
        val savedJson: String? = applicationData.getString(CONSUPTION_ID, CONSUPTION_DEFAULT)
        return Gson().fromJson(savedJson, String::class.java)
    }

    fun setFuelConsuption(consuption: String){
        val json = Gson().toJson(consuption)
        val dataEditor = applicationData.edit()
        dataEditor.putString(CONSUPTION_ID, json)
        dataEditor.apply()
    }

    fun getTarga(): String? {
        return applicationData.getString(TARGA_ID, TARGA_DEFAULT)
    }

    fun setTarga(targa: String){
        val dataEditor = applicationData.edit()
        dataEditor.putString(TARGA_ID, targa.uppercase())
        dataEditor.apply()
    }
}
