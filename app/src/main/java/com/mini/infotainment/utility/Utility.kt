package com.mini.infotainment.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.TypedValue
import android.view.Display
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.gson.Gson
import com.google.zxing.WriterException
import com.mini.infotainment.R
import com.mini.infotainment.data.Data
import com.mini.infotainment.support.SActivity.Companion.displayRatio
import com.mini.infotainment.support.SActivity.Companion.screenSize
import com.mini.infotainment.utility.Utility.Resolution.Companion.BASE_RESOLUTION
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
    fun generatePin(): String {
        return String().randomStr(6)
    }

    fun String.randomStr(size: Int): String{
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..size)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun String.alphaNumeric(): String {
        val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val result = StringBuilder()

        for(c in this)
            if(validChars.contains(c.uppercase()))
                result.append(c)

        return result.toString()
    }

    fun millisToHoursFormatted(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()

        val hoursStr = if(hours.toString().length == 1) "0$hours" else hours.toString()
        val minutesStr = if(minutes.toString().length == 1) "0$minutes" else minutes.toString()
        val secondsStr = if(seconds.toString().length == 1) "0$seconds" else seconds.toString()

        return "$hoursStr:$minutesStr:$secondsStr"
    }

    fun getSpeedMeasure(context: Context): String {
        return context.resources.getStringArray(R.array.speedmeasures)[Data.getUMeasure()]
    }

    fun getDistMeasure(context: Context): String {
        return context.resources.getStringArray(R.array.measures)[Data.getUMeasure()]
    }

    fun getTravDistMeasure(context: Context): String {
        return context.resources.getStringArray(R.array.travdistmeasures)[Data.getUMeasure()]
    }

    fun isUMeasureKM(): Boolean {
        return Data.getUMeasure() == 0
    }

    fun Float.kmToMile(): Float {
        return (((this/1.60934449789f)*10).toInt())/10f
    }

    fun Float.msToKmH(): Float {
        return (this * 3.6f)
    }

    fun Double.toKm(): Float{
        return ((this.toFloat()/100f).roundToInt())/10f
    }

    val System.currentDate: String
        get() {
            val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
            return getDateString(calendar)
        }

    val System.networkDate: Date
        get() = Date(Date().networkDateMillis)

    val Date.networkDateMillis: Long
        get(){
            val timeServer = "time-a.nist.gov";
            val timeClient = NTPUDPClient()
            val inetAddress: InetAddress = InetAddress.getByName(timeServer)
            val timeInfo: TimeInfo = timeClient.getTime(inetAddress)
            return timeInfo.message.transmitTimeStamp.time
        }


    fun getDateString(calendar: Calendar) : String{
        val currentYear: Int = calendar.get(Calendar.YEAR)
        val currentMonth: Int = calendar.get(Calendar.MONTH) + 1
        val currentDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

        return "$currentDay-$currentMonth-$currentYear"
    }


    inline fun <reified T> objectToJsonString(`object`: T): String {
        return Gson().toJson(`object`)
    }

    inline fun <reified T> jsonStringToObject(jsonString: String): T {
        return Gson().fromJson(jsonString, T::class.java)
    }

    fun getBrandDrawable(ctx: Context): Drawable? {
        val brandName = Data.getBrandName()
        if(brandName == "no" || brandName == "none") return null

        val logoId: Int = ctx.resources.getIdentifier("logo_${brandName}", "drawable", ctx.packageName)
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

    fun confirmDialog(c: Activity, runnable: Runnable){
        shortDialog(c.getString(R.string.confirm_title), c.getString(R.string.confirm_desc), c, runnable)
    }

    private fun shortDialog(title: String, description: String, c: Activity, runnable: Runnable){
        AlertDialog.Builder(c)
            .setTitle(title)
            .setMessage(description)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(c.resources.getStringArray(R.array.yesno)[0]) { _, _ -> runnable.run() }
            .setNegativeButton(c.resources.getStringArray(R.array.yesno)[1], null).show()
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

    fun <N : Number> convertValue(x: N, activity: AppCompatActivity): Double {
        return x.toDouble() * activity.displayRatio
    }


    fun ridimensionamento(activity: AppCompatActivity, v: ViewGroup) {
        val height = if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            activity.screenSize[1]
        }else activity.screenSize[0]

        if(activity.displayRatio == 1.0) return

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

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        return LocalDateTime.now().format(formatter)
    }
}