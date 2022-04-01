package com.mini.infotainment.notification

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.utility.Utility

class NotificationHandler(val context: HomeActivity) {
    var notificationDialog: NotificationDialog? = null
    var lastNotificationId: String? = null
    var notifications = HashMap<String, MutableList<String>>()

    fun onNotificationReceived(jsonString: String){
        Utility.showToast(context, jsonString)
        val jsonObject = Utility.jsonStringToObject<NotificationData>(jsonString)
        if(notificationDialog?.isShowing == true && lastNotificationId == jsonObject.title){
            notificationDialog!!.addNotification(jsonObject.text)
            return
        }

        val previousNotification = notifications[jsonObject.title] ?: mutableListOf()
        previousNotification.add(jsonObject.text)
        notificationDialog = NotificationDialog(context, jsonObject.title, previousNotification, jsonObject.drawable)
    }

    class NotificationData(val title: String, val text: String, val drawable: Drawable?)

    class NotificationDialog(val ctx: Context, val title: String, val textList: MutableList<String>, val drawable: Drawable?) : Dialog(ctx){
        constructor(
            ctx: Context,
            title: String,
            text: String,
            drawable: Drawable?) : this(ctx, title, mutableListOf(text), drawable)

        init{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.notification_dialog)

            val notificationTitle = findViewById<TextView>(R.id.noti_app_name)
            val notificationIcon = findViewById<ImageView>(R.id.noti_icon)

            notificationTitle.text = title
            notificationIcon.setImageDrawable(drawable)

            for(text: String in textList)
                addNotification(text)

            show()
        }

        fun addNotification(body: String){
            val gallery: LinearLayout = findViewById(R.id.noti_lllayout)
            val newNotif = layoutInflater.inflate(R.layout.single_notification, gallery, false)

            val notificationBody = newNotif.findViewById<TextView>(R.id.single_noti_text)
            notificationBody.text = body
        }
    }
}