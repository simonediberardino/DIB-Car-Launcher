package com.mini.infotainment.notification

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import android.widget.*
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.utility.Utility

class NotificationHandler(private val context: HomeActivity) {
    private val APPS_MAP: HashMap<String, Application> = hashMapOf(
        "com.instagram.android" to Application("Instagram", context.getDrawable(R.drawable.instagram_logo)!!, false),
        "com.whatsapp" to Application("WhatsApp", context.getDrawable(R.drawable.whatsapp_logo)!!, true),
    )

    private var notificationDialog: NotificationDialog? = null
    private var notifications = HashMap<String, MutableList<NotificationData>>()
    private var lastNotification: NotificationData? = null

    fun onNotificationReceived(jsonString: String){
        val currentNotification = Utility.jsonStringToObject<NotificationData>(jsonString)
        if(currentNotification == lastNotification)
            return

        val application = APPS_MAP[currentNotification.packageName] ?: return

        val mapKey = currentNotification.mapKey
        val previousNotifications = notifications[mapKey] ?: mutableListOf()
        previousNotifications.add(currentNotification)
        notifications[mapKey] = previousNotifications

        if(notificationDialog?.isShowing == true && lastNotification?.mapKey == currentNotification.mapKey){
            notificationDialog!!.addNotification(currentNotification.text)
            lastNotification = currentNotification
            return
        }

        notificationDialog?.dismiss()
        lastNotification = currentNotification

        notificationDialog = NotificationDialog(
            context,
            currentNotification.title,
            previousNotifications,
            application,
            currentNotification.packageName
        )

        // Clears the previous notifications, remove this instruction if you want to keep them;
        notifications[mapKey]?.clear()
    }

    class NotificationData(val title: String, val text: String, val packageName: String, val id: Int){
        // Key used to indicate a specific instance of an application, E.G. a WhatsApp private chat;
        val mapKey: String
            get() {
                return "$packageName:$title"
            }

        override fun equals(other: Any?): Boolean {
            return if(other is NotificationData){
                return this.title == other.title && this.text == other.text && this.packageName == other.packageName && this.id == other.id
            }else{
                false
            }
        }
    }

    class Application(val appName: String, val icon: Drawable, val doesAllowInput: Boolean)

    class NotificationDialog(
        val ctx: HomeActivity,
        val title: String,
        val notiList: MutableList<NotificationData>,
        val application: Application,
        val packageName: String) : Dialog(ctx)
    {
        companion object{
            const val DIALOG_DURATION = 15000
        }

        private var notificationConfirm: View
        private var notificationIcon: ImageView
        private var notificationAppName: TextView
        private var notificationTitle: TextView
        private var notificationBar: ProgressBar
        private var notificationInputText: EditText
        private var startTime = System.currentTimeMillis()

        private var isTimerRunning: Boolean = true
            set(value) {
                if(value)
                    timerLoop()
                field = value
            }

        init{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.notification_dialog)

            notificationTitle = findViewById(R.id.noti_title)
            notificationAppName = findViewById(R.id.noti_app_name)
            notificationIcon = findViewById(R.id.noti_icon)
            notificationConfirm = findViewById(R.id.noti_confirm_button)
            notificationBar = findViewById(R.id.noti_progress)
            notificationInputText = findViewById(R.id.noti_edit_text)

            notificationTitle.text = ctx.getString(R.string.nuovo_messaggio).replace("{sender}", title)
            notificationAppName.text = ctx.getString(R.string.notifica_da).replace("{appname}", application.appName)
            notificationIcon.background = application.icon

            notificationConfirm.setOnClickListener {
                handleConfirm()
            }

            if(!application.doesAllowInput)
                notificationInputText.visibility = View.GONE

            for(notification: NotificationData in notiList)
                addNotification(notification.text)

            notificationInputText.setOnFocusChangeListener { view, b -> isTimerRunning = !b }

            handleConfirm()

            if(ApplicationData.areNotificationsEnabled())
                show()

            isTimerRunning = ApplicationData.areNotificationsEnabled()
        }

        private fun handleConfirm(){
            val inputText = notificationInputText.text.toString().trim()
            if(inputText.isEmpty()){
                this.dismiss()
            }else{
                val inputMessage = Utility.objectToJsonString(InputMessage(inputText, notiList.last().id, packageName))
                HomeActivity.server?.sendMessage(inputMessage)
                this.startTime = System.currentTimeMillis()
            }
        }

        private fun timerLoop(){
            Thread{
                val updateTime: Long = (DIALOG_DURATION/100).toLong()
                while(this.isShowing && this.isTimerRunning){
                    Thread.sleep(updateTime)
                    notificationBar.progress--
                    if(notificationBar.progress == 0)
                        dismiss()
                }
            }.start()
        }

        fun addNotification(body: String){
            val scrollView: ScrollView = findViewById(R.id.noti_scrollview)
            val gallery: LinearLayout = findViewById(R.id.noti_lllayout)
            val newNotif = layoutInflater.inflate(R.layout.single_notification, gallery, false)

            val notificationBody = newNotif.findViewById<TextView>(R.id.single_noti_text)
            notificationBody.text = body
            gallery.addView(newNotif)

            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            this.startTime = System.currentTimeMillis()
        }
    }

    class InputMessage(val message: String, val notificationId: Int, val packageName: String)
}