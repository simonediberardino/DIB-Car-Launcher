package com.mini.infotainment.data

import android.content.Context
import android.content.SharedPreferences
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.SActivity

object Data {
    const val POLICY_URL = "https://play.google.com/store/apps/datasafety?id=com.mini.infotainment&hl=it&gl=US"
    const val DATA_ID = "data"
    private const val USERNAME_ID = "TARGA_ID"
    private val USERNAME_DEFAULT = null
    private const val PASSWORD_ID = "PASSWORD_ID"
    private val PASSWORD_DEFAULT = null
    private const val BRAND_ID = "BRAND_ID"
    private val BRAND_DEFAULT = null
    private const val WP_ID = "WP_ID"
    private val WP_DEFAULT = true
    private const val U_MEASURE_ID = "U_MEASURE_ID"
    private val U_MEASURE_DEFAULT = 0
    private const val NOTIFICATION_STATUS_ID = "NOTI_STATUS_ID"
    private const val NOTIFICATION_DEFAULT = true
    private val PIN_ID: String = "PIN_ID"
    private val PIN_DEFAULT: String? = null
    private val WP_DRAWABLE_ID: String = "WP_DRAWABLE_ID"
    private val WP_DRAWABLE_DEFAULT: String = "wallpaper_0"

    val applicationData: SharedPreferences
        get() {
            return SActivity.lastActivity.getSharedPreferences(
                DATA_ID,
                Context.MODE_PRIVATE
            )!!
        }

    fun getWallpaper(): String? {
        return applicationData.getString(WP_DRAWABLE_ID, WP_DRAWABLE_DEFAULT)
    }

    fun setWallpaper(wp: String){
        val dataEditor = applicationData.edit()
        dataEditor.putString(WP_DRAWABLE_ID, wp)
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

    fun getUMeasure(): Int {
        return applicationData.getInt(U_MEASURE_ID, U_MEASURE_DEFAULT)
    }

    fun setUMeasure(id: Int){
        val dataEditor = applicationData.edit()
        dataEditor.putInt(U_MEASURE_ID, id)
        dataEditor.apply()
    }

    fun saveUser(user: MyCar){
        Data.setUserPassword(user.password)
        Data.setUsername(user.plateNum.uppercase())
        Data.setPin(user.pin)
    }

    fun deleteUser(){
        Data.setUserPassword(null)
        Data.setUsername(null)
        Data.setPin(null)
    }

    fun getBrandName(): String? {
        return applicationData.getString(BRAND_ID, BRAND_DEFAULT)
    }

    fun setBrandName(brand: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(BRAND_ID, brand)
        dataEditor.apply()
    }

    fun getUserName(): String? {
        return applicationData.getString(USERNAME_ID, USERNAME_DEFAULT)
    }

    fun setUsername(username: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(USERNAME_ID, username?.uppercase().toString())
        dataEditor.apply()
    }

    fun getUserPassword(): String? {
        return applicationData.getString(PASSWORD_ID, PASSWORD_DEFAULT)
    }

    fun setUserPassword(password: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(PASSWORD_ID, password.toString())
        dataEditor.apply()
    }

    fun getPin(): String? {
        return applicationData.getString(PIN_ID, PIN_DEFAULT)
    }

    fun setPin(pin: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(PIN_ID, pin.toString())
        dataEditor.apply()
    }


    fun isNotificationStatusEnabled(): Boolean {
        return applicationData.getBoolean(NOTIFICATION_STATUS_ID, NOTIFICATION_DEFAULT)
    }

    fun setNotificationStatus(status: Boolean){
        val dataEditor = applicationData.edit()
        dataEditor.putBoolean(NOTIFICATION_STATUS_ID, status)
        dataEditor.apply()
    }


    fun isLogged(): Boolean {
        return !(getUserName() == null || getUserName().equals("null", ignoreCase = true))
    }
}
