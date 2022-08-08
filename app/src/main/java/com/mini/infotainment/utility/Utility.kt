package com.mini.infotainment.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.google.gson.Gson
import com.google.zxing.WriterException
import com.mini.infotainment.R
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility.Resolution.Companion.BASE_RESOLUTION
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import java.math.BigInteger
import java.net.InetAddress
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt
import android.R as R1


object Utility {
    fun areSettingsSet(): Boolean {
        return ApplicationData.getBrandName() != null
    }
    fun hasLoginData(): Boolean {
        return ApplicationData.getCarPassword() != null && ApplicationData.getTarga() != null
    }

    fun metersToKm(valueInMeters: Double): Float {
        return ((valueInMeters.toFloat()/100f).roundToInt())/10f
    }

    fun getScreenWidth(activity: AppCompatActivity): Double {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels.toDouble()
    }

    fun getScreenHeight(activity: AppCompatActivity): Double {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels.toDouble()
    }

    fun getCurrentDate(): String {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        return getDateString(calendar)
    }

    fun getNetworkDate(): Date {
        return Date(getNetworkDateMillis())
    }

    fun getNetworkDateMillis(): Long {
        val TIME_SERVER = "time-a.nist.gov";
        val timeClient = NTPUDPClient()
        val inetAddress: InetAddress = InetAddress.getByName(TIME_SERVER)
        val timeInfo: TimeInfo = timeClient.getTime(inetAddress)
        return timeInfo.message.transmitTimeStamp.time
    }

    fun getDateString(calendar: Calendar) : String{
        val currentYear: Int = calendar.get(Calendar.YEAR)
        val currentMonth: Int = calendar.get(Calendar.MONTH) + 1
        val currentDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

        return "$currentDay-$currentMonth-$currentYear"
    }

    fun getWallpaper(context: Context): Drawable {
        val defaultBackgroundDrawable = context.getDrawable(R.drawable.background)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return defaultBackgroundDrawable!!
        }

        return if(ApplicationData.useDefaultWP()){
            defaultBackgroundDrawable!!
        }else{
            val wallpaperManager = WallpaperManager.getInstance(context)
            val wallpaperDrawable = wallpaperManager.drawable
            wallpaperDrawable
        }
    }

    inline fun <reified T> objectToJsonString(`object`: T): String {
        return Gson().toJson(`object`)
    }

    inline fun <reified T> jsonStringToObject(jsonString: String): T {
        return Gson().fromJson(jsonString, T::class.java)
    }

    fun getBrandDrawable(ctx: Context): Drawable? {
        val logoId: Int = ctx.resources.getIdentifier("logo_${ApplicationData.getBrandName()}", "drawable", ctx.packageName)
        return if(logoId == 0) null else ctx.getDrawable(logoId)
    }

    fun generateQrCode(textToEncode: String, activity: AppCompatActivity): Bitmap? {
        val manager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val display: Display = manager!!.defaultDisplay

        val point = Point()
        display.getSize(point)

        val width: Int = point.x
        val height: Int = point.y

        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4

        val qrgEncoder = QRGEncoder(textToEncode, null, QRGContents.Type.TEXT, dimen)
        return try {
            qrgEncoder.encodeAsBitmap()
        } catch (e: WriterException) {
            null
        }
    }

    fun getLocalIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    fun isInternetAvailable(): Boolean {
        return isInternetAvailable(SActivity.lastActivity)
    }

    fun confirmDialog(c: Activity, runnable: Runnable){
        shortDialog(c.getString(R.string.confirm_title), c.getString(R.string.confirm_desc), c, runnable)
    }

    fun shortDialog(title: String, description: String, c: Activity, runnable: Runnable){
        AlertDialog.Builder(c)
            .setTitle(title)
            .setMessage(description)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(c.resources.getStringArray(R.array.yesno)[0]) { _, _ -> runnable.run() }
            .setNegativeButton(c.resources.getStringArray(R.array.yesno)[1], null).show()
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

    fun getMD5(input: String): String {
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

    fun toast(c: Activity, message: String){
        c.runOnUiThread { Toast.makeText(c, message, Toast.LENGTH_LONG).show() }
    }

    fun toast(c: AppCompatActivity, message: String){
        c.runOnUiThread { Toast.makeText(c, message, Toast.LENGTH_LONG).show() }
    }

    data class Resolution(val x: Double, val y: Double) {
        companion object{
            val BASE_RESOLUTION = Resolution(1024.0, 600.0)
        }
    }

    fun <N : Number> convertValue(x: N, context: AppCompatActivity): Double {
        return x.toDouble() * getDisplayRatio(context)
    }

    fun getDisplayRatio(activity: AppCompatActivity): Double {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels.toDouble()

        return height / BASE_RESOLUTION.y
    }

    fun ridimensionamento(activity: AppCompatActivity, v: ViewGroup) {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels.toDouble()

        if(getDisplayRatio(activity) == 1.0) return

        for (i in 0 until v.childCount) {
            val vAtI = v.getChildAt(i)
            val curHeight = vAtI.layoutParams.height
            val curWidth = vAtI.layoutParams.width
            val ratio = height / BASE_RESOLUTION.y

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

    fun getAddress(location: Location, activity: Activity, callback: RunnablePar) {
        if(!isInternetAvailable(activity)){
            return
        }

        Thread{
            try{
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
                            result = result
                                .split(field)[1]
                                .split("\n")[0]
                                .drop(3)
                                .dropLast(2)

                            activity.runOnUiThread {
                                callback.run(result)
                            }
                        }else{
                            callback.run(String())
                        }
                    }
                }
            }catch (exception: Exception){
                return@Thread
            }
        }.start()
    }

    fun getSimpleAddress(location: Location, activity: Activity, callback: RunnablePar) {
        getAddress(location, activity, object: RunnablePar{
            override fun run(p: Any?) {
                mutableListOf<View>().toTypedArray()
                callback.run(
                    (p as String).split(",")[0]
                )
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }

    fun msToKmH(ms: Float): Int {
        return (ms * 3.6).toInt()
    }
}