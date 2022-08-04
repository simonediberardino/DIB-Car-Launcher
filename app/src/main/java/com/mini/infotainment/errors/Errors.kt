package com.mini.infotainment.errors

import android.app.Activity
import com.mini.infotainment.R
import com.mini.infotainment.utility.Utility

object Errors {
    enum class ErrorCodes{
        APP_NOT_INSTALLED,
        GPS_REQUIRED,
        UNKNOWN,
        INTERNET_NOT_AVAILABLE
    }

    fun printError(errorCode: ErrorCodes, activity: Activity){
        Utility.showToast(activity,
            activity.getString(when(errorCode){
                ErrorCodes.APP_NOT_INSTALLED -> R.string.app_not_installed
                ErrorCodes.GPS_REQUIRED -> R.string.gps_not_enabled
                ErrorCodes.UNKNOWN -> R.string.errore_sconosciuto
                ErrorCodes.INTERNET_NOT_AVAILABLE -> R.string.error_no_internet
            })
        )
    }
}