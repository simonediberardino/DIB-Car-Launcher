package com.mini.infotainment.activities.login

import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.support.ActivityExtended

open class ProfileActivity : ActivityExtended() {
    enum class ErrorCodes{
        EXISTS,
        INVALID_DETAILS,
        PASSWORD_DONT_MATCH,
        NO_INTERNET
    }

    internal fun errors(): HashMap<ErrorCodes, String> {
        return hashMapOf(
            ErrorCodes.EXISTS to getString(R.string.error_car_registered),
            ErrorCodes.INVALID_DETAILS to getString(R.string.error_invalid_details),
            ErrorCodes.PASSWORD_DONT_MATCH to getString(R.string.error_password_dont_match),
            ErrorCodes.NO_INTERNET to getString(R.string.error_no_internet)
        )
    }

    internal fun showError(errorCode: ErrorCodes){
        CustomToast(errors()[errorCode] ?: return, this).show()
    }
}