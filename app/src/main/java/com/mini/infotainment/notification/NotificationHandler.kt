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
    val APPS_MAP: HashMap<String, Application> = hashMapOf(
        "com.instagram.android" to
                Application("Instagram", "com.instagram.android", context.getDrawable(R.drawable.instagram_logo)!!),
        "com.whatsapp" to
                Application("Whatsapp", "com.whatsapp", context.getDrawable(R.drawable.whatsapp_logo)!!),
        )

    var notificationDialog: NotificationDialog? = null
    var notifications = HashMap<String, MutableList<NotificationData>>()
    var lastNotification: NotificationData? = null

    fun onNotificationReceived(jsonString: String){
        val currentNotification = Utility.jsonStringToObject<NotificationData>(jsonString)

        if(!APPS_MAP.containsKey(currentNotification.packageName))
            return

        val application = APPS_MAP[currentNotification.packageName]

        if(notificationDialog?.isShowing == true && lastNotification == currentNotification){
            notificationDialog!!.addNotification(currentNotification.text)
            return
        }

        lastNotification = currentNotification

        val previousNotification = notifications[currentNotification.title] ?: mutableListOf()
        previousNotification.add(currentNotification)

        notificationDialog = NotificationDialog(
            context,
            currentNotification.title,
            previousNotification,
            application!!.appName,
            application.icon
        )
    }

    class NotificationData(val title: String, val text: String, val packageName: String){
        override fun equals(other: Any?): Boolean {
            return if(other is NotificationData){
                this.title == other.title && this.packageName == other.packageName
            }else{
                false
            }
        }
    }

    class Application(val appName: String, val packageName: String, val icon: Drawable)

    class NotificationDialog(val ctx: Context, val title: String, val notiList: MutableList<NotificationData>, val appName: String, val appIcon: Drawable) : Dialog(ctx){
        init{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.notification_dialog)

            val notificationTitle = findViewById<TextView>(R.id.noti_title)
            val notificationAppname = findViewById<TextView>(R.id.noti_app_name)
            val notificationIcon = findViewById<ImageView>(R.id.noti_icon)

            notificationTitle.text = title
            notificationAppname.text = appName
            notificationIcon.background = appIcon

            for(notification: NotificationData in notiList)
                addNotification(notification.text)

            show()
        }

        fun addNotification(body: String){
            val gallery: LinearLayout = findViewById(R.id.noti_lllayout)
            val newNotif = layoutInflater.inflate(R.layout.single_notification, gallery, false)

            val notificationBody = newNotif.findViewById<TextView>(R.id.single_noti_text)
            notificationBody.text = body
            gallery.addView(newNotif)
        }
    }
}