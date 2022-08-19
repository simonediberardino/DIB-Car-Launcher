package com.mini.infotainment.activities.login

import com.mini.infotainment.R
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

abstract class ProfileActivity : SActivity() {
    enum class ErrorCodes{
        EXISTS,
        INVALID_DETAILS,
        PASSWORD_SHORT,
        PLATE_SHORT,
        PASSWORD_DONT_MATCH,
        NO_INTERNET
    }

    abstract fun handleData()
    internal open fun handleData(callback: Runnable = Runnable {}){
        if(!isInternetAvailable){
            showError(ErrorCodes.NO_INTERNET)
            return
        }
        callback.run()
    }

    private val errors: HashMap<ErrorCodes, String>
        get() {
            return hashMapOf(
                ErrorCodes.PASSWORD_SHORT to getString(R.string.error_password_short),
                ErrorCodes.PLATE_SHORT to getString(R.string.error_plate_short),
                ErrorCodes.EXISTS to getString(R.string.error_car_registered),
                ErrorCodes.INVALID_DETAILS to getString(R.string.error_invalid_details),
                ErrorCodes.PASSWORD_DONT_MATCH to getString(R.string.error_password_dont_match),
                ErrorCodes.NO_INTERNET to getString(R.string.error_no_internet)
            )
        }

    internal fun showError(errorCode: ErrorCodes){
        Utility.toast(this, errors[errorCode] ?: return)
    }

    internal fun success(){
        Utility.toast(this, this.getString(R.string.success))
    }
}