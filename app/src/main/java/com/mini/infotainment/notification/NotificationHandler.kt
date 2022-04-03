package com.mini.infotainment.notification

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.utility.Utility

class NotificationHandler(private val context: HomeActivity) {
    private val APPS_MAP: HashMap<String, Application> = hashMapOf(
        "com.instagram.android" to Application("Instagram",  context.getDrawable(R.drawable.instagram_logo)!!),
        "com.whatsapp" to Application("WhatsApp", context.getDrawable(R.drawable.whatsapp_logo)!!),
        )

    private var notificationDialog: NotificationDialog? = null
    private var notifications = HashMap<String, MutableList<NotificationData>>()
    private var lastNotification: NotificationData? = null

    fun onNotificationReceived(jsonString: String){
        val currentNotification = Utility.jsonStringToObject<NotificationData>(jsonString)

        if(!APPS_MAP.containsKey(currentNotification.packageName))
            return

        val application = APPS_MAP[currentNotification.packageName]

        if(application!!.appName == currentNotification.title)
            return

        if(currentNotification == lastNotification)
            return

        val mapKey = currentNotification.mapKey
        val previousNotifications = notifications[mapKey] ?: mutableListOf()
        previousNotifications.add(currentNotification)
        notifications[mapKey] = previousNotifications

        if(notificationDialog?.isShowing == true && lastNotification?.mapKey == currentNotification.mapKey){
            notificationDialog!!.addNotification(currentNotification.text)
            return
        }else{
            notificationDialog?.dismiss()
        }

        lastNotification = currentNotification

        notificationDialog = NotificationDialog(
            context,
            currentNotification.title,
            previousNotifications,
            application.appName,
            application.icon
        )
    }

    class NotificationData(val title: String, val text: String, val packageName: String){
        val mapKey: String
            get() {
                return "$packageName:$title"
            }

        override fun equals(other: Any?): Boolean {
            return if(other is NotificationData){
                return this.title == other.title && this.text == other.text && this.packageName == other.packageName
            }else{
                false
            }
        }
    }

    class Application(val appName: String, val icon: Drawable)

    class NotificationDialog(ctx: Context, title: String, notiList: MutableList<NotificationData>, appName: String, appIcon: Drawable) : Dialog(ctx){
        init{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.notification_dialog)

            val notificationTitle = findViewById<TextView>(R.id.noti_title)
            val notificationAppname = findViewById<TextView>(R.id.noti_app_name)
            val notificationIcon = findViewById<ImageView>(R.id.noti_icon)
            val notificationConfirm = findViewById<View>(R.id.noti_confirm_button)

            notificationTitle.text = ctx.getString(R.string.nuovo_messaggio).replace("{sender}", title)
            notificationAppname.text = ctx.getString(R.string.notifica_da).replace("{appname}", appName)
            notificationIcon.background = appIcon
            notificationConfirm.setOnClickListener { this.dismiss() }

            for(notification: NotificationData in notiList)
                addNotification(notification.text)

            if(ApplicationData.areNotificationsEnabled())
                show()
        }

        fun addNotification(body: String){
            val scrollView: ScrollView = findViewById(R.id.noti_scrollview)
            val gallery: LinearLayout = findViewById(R.id.noti_lllayout)
            val newNotif = layoutInflater.inflate(R.layout.single_notification, gallery, false)

            val notificationBody = newNotif.findViewById<TextView>(R.id.single_noti_text)
            notificationBody.text = body
            gallery.addView(newNotif)

            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }
}