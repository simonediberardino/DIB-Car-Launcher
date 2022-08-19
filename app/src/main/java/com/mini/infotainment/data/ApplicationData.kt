package com.mini.infotainment.data

import android.content.Context
import android.content.SharedPreferences
import com.mini.infotainment.support.SActivity

object ApplicationData {
    const val DATA_ID = "data"
    private const val TARGA_ID = "TARGA_ID"
    private val TARGA_DEFAULT = null
    private const val PASSWORD_ID = "PASSWORD_ID"
    private val PASSWORD_DEFAULT = null
    private const val BRAND_ID = "BRAND_ID"
    private val BRAND_DEFAULT = null
    private const val WP_ID = "WP_ID"
    private val WP_DEFAULT = true
    private const val U_MEASURE_ID = "U_MEASURE_ID"
    private val U_MEASURE_DEFAULT = 0

    val applicationData: SharedPreferences
        get() {
            return SActivity.lastActivity.getSharedPreferences(
                DATA_ID,
                Context.MODE_PRIVATE
            )!!
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

    fun getBrandName(): String? {
        return applicationData.getString(BRAND_ID, BRAND_DEFAULT)
    }

    fun setBrandName(brand: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(BRAND_ID, brand)
        dataEditor.apply()
    }

    fun getTarga(): String? {
        return applicationData.getString(TARGA_ID, TARGA_DEFAULT)
    }

    fun setTarga(targa: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(TARGA_ID, targa?.uppercase().toString())
        dataEditor.apply()
    }

    fun getCarPassword(): String? {
        return applicationData.getString(PASSWORD_ID, PASSWORD_DEFAULT)
    }

    fun setCarPassword(password: String?){
        val dataEditor = applicationData.edit()
        dataEditor.putString(PASSWORD_ID, password.toString())
        dataEditor.apply()
    }
}
