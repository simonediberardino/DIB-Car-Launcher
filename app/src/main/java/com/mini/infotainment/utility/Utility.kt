package com.mini.infotainment.utility

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


object Utility {
    fun navigateTo(c: AppCompatActivity, cl: Class<*>?) {
        val intent = Intent(c, cl)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val bundle = ActivityOptionsCompat.makeCustomAnimation(c, R.anim.fade_in, R.anim.fade_out).toBundle()
        c.startActivity(intent, bundle)
    }

    fun getMD5(input: String): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())

            val no = BigInteger(1, messageDigest)

            var hashtext = no.toString(16)
            while (hashtext.length < 32) {
                hashtext = "0$hashtext"
            }
            hashtext
        }
        catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    fun capitalizeFirstLetter(string: String): String {
        if(string.isEmpty())
            return string

        val firstChar = string[0]
        val restOfString = if(string.length > 1) string.drop(1) else String()
        return "${firstChar.uppercase()}${restOfString.lowercase()}"
    }

    fun String.capitalizeWords(): String =
        lowercase().split(" ").joinToString(" ") { it.capitalize() }

    fun showToast(c: Activity, message: String){
        Toast.makeText(c, message, Toast.LENGTH_LONG).show()
    }

    fun ridimensionamento(activity: AppCompatActivity, v: ViewGroup) {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val baseHeight = 1920.0
        val height = displayMetrics.heightPixels.toDouble()

        for (i in 0 until v.childCount) {
            val vAtI = v.getChildAt(i)
            val curHeight = vAtI.layoutParams.height
            val curWidth = vAtI.layoutParams.width
            val ratio = height / baseHeight

            if (curHeight > ViewGroup.LayoutParams.MATCH_PARENT)
                vAtI.layoutParams.height = (curHeight * ratio).toInt()

            if (curWidth > ViewGroup.LayoutParams.MATCH_PARENT)
                vAtI.layoutParams.width = (curWidth * ratio).toInt()

            if (vAtI is TextView) {
                val curSize = vAtI.textSize.toInt()
                val newSize = (curSize * ratio).toInt()
                vAtI.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize.toFloat())
            }

            vAtI.requestLayout()

            if (vAtI is ViewGroup) {
                ridimensionamento(activity, vAtI)
            }
        }
    }

    fun getAddress(location: Location, activity: Activity) : String{
        return Geocoder(activity, Locale.getDefault()).getFromLocation(
            location.latitude,
            location.longitude,
            1
        )[0].getAddressLine(0)
    }

    fun getSimpleAddress(location: Location, activity: Activity): String {
        val tokens = getAddress(location, activity).split(",")
        return if(tokens.size > 1)
            "${tokens[0]},${tokens[1]}"
        else tokens[0]
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val timeZone = TimeZone.getTimeZone("GMT+1:00")
        val cal = Calendar.getInstance(timeZone)
        val currentLocalTime = cal.time
        val date: DateFormat = SimpleDateFormat("HH:mm:ss a")
        date.timeZone = timeZone

        return date.format(currentLocalTime).replace("AM", "").replace("PM", "")
    }
}