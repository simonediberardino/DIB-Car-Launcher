package com.mini.infotainment.utility

import android.R as R1
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
import com.mini.infotainment.R
import com.mini.infotainment.support.ActivityExtended
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.common.api.Response
import com.google.gson.Gson
import com.mini.infotainment.support.RunnablePar
import okhttp3.OkHttpClient
import okhttp3.Request

object Utility {
    fun isInternetAvailable(): Boolean {
        return isInternetAvailable(ActivityExtended.lastActivity)
    }

    fun isInternetAvailable(appCompatActivity: Activity): Boolean {
        val cm = appCompatActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nInfo = cm.activeNetworkInfo
        return nInfo != null && nInfo.isAvailable && nInfo.isConnected
    }

    fun navigateTo(c: Activity, cl: Class<*>?) {
        val intent = Intent(c, cl)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val bundle = ActivityOptionsCompat.makeCustomAnimation(c, R1.anim.fade_in, R1.anim.fade_out).toBundle()
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

    fun getAddress(location: Location, callback: RunnablePar) {
        Thread{
            val client = OkHttpClient().newBuilder()
                .build()
            val request: Request = Request.Builder()
                .url("https://api.geoapify.com/v1/geocode/reverse?lat=${location.latitude}&lon=${location.longitude}&apiKey=827645ed3da54b00a91ac7217a17fdb9")
                .method("GET", null)
                .build()

            client.newCall(request).execute().use {
                    response ->
                run {
                    var result = response.body?.string()
                    val field = "formatted"
                    if(result != null && field in result) {
                            result = response.body?.string()!!.split(field)[1]
                            result = result.split("\n")[0].drop(3).dropLast(2)
                            callback.run(result)
                    }else{
                        callback.run(String())
                    }
                }
            }
        }.start()
    }

    fun getSimpleAddress(location: Location, callback: RunnablePar) {
        getAddress(location, object: RunnablePar{
            override fun run(p: Any?) {
                val tokens = (p as String).split(",")
                callback.run(if(tokens.size > 1)
                    "${tokens[0]},${tokens[1]}"
                else tokens[0])
            }
        })
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

    fun msToKmH(ms: Float): Int {
        return (ms * 3.6).toInt()
    }
}