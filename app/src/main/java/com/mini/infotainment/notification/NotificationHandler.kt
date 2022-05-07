package com.mini.infotainment.notification

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.activities.home.HomeActivity.Companion.REQUEST_CODE_SPEECH_INPUT
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.utility.Utility
import java.util.*
import kotlin.collections.HashMap


class NotificationHandler(private val ctx: HomeActivity) {
    @SuppressLint("UseCompatLoadingForDrawables")
    private val APPS_MAP: HashMap<String, Application> = hashMapOf(
        "com.instagram.android" to Application(
            ctx.getDrawable(R.drawable.instagram_logo)!!,
            false),

        "com.whatsapp" to Application(
            ctx.getDrawable(R.drawable.whatsapp_logo)!!,
            true),
    )

    private var notificationDialog: NotificationDialog? = null
    private var notifications = HashMap<String, MutableList<NotificationData>>()
    private var lastNotification: NotificationData? = null

    fun onNotificationReceived(jsonString: String){
        val currentNotification = Utility.jsonStringToObject<NotificationData>(jsonString)
        if(currentNotification.title.isEmpty() || currentNotification.text.isEmpty()) return

        val application = APPS_MAP[currentNotification.packageName]

        val mapKey = currentNotification.mapKey
        val previousNotifications = notifications[mapKey] ?: mutableListOf()
        previousNotifications.add(currentNotification)
        notifications[mapKey] = previousNotifications

        if(notificationDialog?.isShowing == true){
            // If the new notification is associated to the same instance of the same application as the previous one (E.G. the same whatsapp chat);
            if(lastNotification?.mapKey == currentNotification.mapKey){
                notificationDialog!!.addNotification(currentNotification.text)
                lastNotification = currentNotification
                return
            }else{
                // Doesn't show new notifications if the user is typing a message if it's not the same instance;
                if(!notificationDialog!!.isTimerRunning) return
            }
        }

        notificationDialog?.dismiss()
        lastNotification = currentNotification

        notificationDialog = NotificationDialog(
            ctx,
            currentNotification.title,
            previousNotifications.toMutableList(),
            application,
            currentNotification.appName,
            currentNotification.packageName
        )

        // Clears the previous notifications, remove this instruction if you want to keep them;
        notifications[mapKey]?.clear()
    }

    fun onVoiceTextReceived(message: String?){
        if(message == null) return
        notificationDialog?.notificationInputText?.setText(message)
    }

    class NotificationData(val title: String, val text: String, val appName: String, val packageName: String, val id: Int){
        // Key used to indicate a specific instance of an application, E.G. a WhatsApp private chat;
        val mapKey: String
            get() {
                return "$packageName:$title"
            }
    }

    class Application(val icon: Drawable?, val doesAllowInput: Boolean)

    @SuppressLint("SetTextI18n")
    class NotificationDialog(
        val ctx: HomeActivity,
        val title: String,
        val notiList: MutableList<NotificationData>,
        val application: Application?,
        val appName: String,
        val packageName: String) : Dialog(ctx)
    {
        companion object{
            const val UNKNOWN = "UNKNOWN"
            const val DIALOG_DURATION = 15000
        }

        internal lateinit var notificationInputLayout: ViewGroup
        internal lateinit var notificationConfirm: View
        internal lateinit var notificationIcon: ImageView
        internal lateinit var notificationAppName: TextView
        internal lateinit var notificationTitle: TextView
        internal lateinit var notificationBar: ProgressBar
        internal lateinit var notificationInputText: EditText
        internal lateinit var notificationInputVoice: View

        private var startTime = System.currentTimeMillis()

        var isTimerRunning: Boolean = true
            set(value) {
                if(value)
                    timerLoop()
                field = value
            }

        init{
            if(ApplicationData.areNotificationsEnabled()){
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.notification_dialog)

                notificationTitle = findViewById(R.id.noti_title)
                notificationAppName = findViewById(R.id.noti_app_name)
                notificationIcon = findViewById(R.id.noti_icon)
                notificationConfirm = findViewById(R.id.noti_confirm_button)
                notificationBar = findViewById(R.id.noti_progress)
                notificationInputText = findViewById(R.id.noti_edit_text)
                notificationInputLayout = findViewById(R.id.noti_input_layout)
                notificationInputVoice = findViewById(R.id.noti_input_voice)

                notificationTitle.text = "${ctx.getString(R.string.new_notification)}: $title"
                notificationAppName.text =
                    if(appName == UNKNOWN)
                        ctx.getString(R.string.new_notification)
                    else
                        ctx.getString(R.string.notifica_da).replace("{appname}", appName)

                notificationIcon.background =
                    if(application == null)
                        ctx.getDrawable(R.drawable.ic_baseline_notifications_active_24)
                    else
                        application.icon

                notificationConfirm.setOnClickListener {
                    handleConfirm()
                }

                notificationInputVoice.setOnClickListener {
                    isTimerRunning = false
                    handleVoice()
                }

                notificationInputLayout.visibility = if(application?.doesAllowInput == true) View.VISIBLE else View.GONE

                for(notification: NotificationData in notiList)
                    addNotification(notification.text)

                show()

                isTimerRunning = true
            }
        }

        private fun handleVoice(){
            ctx.homePage1.pauseSpotifyTrack()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, ctx.getString(R.string.listening))

            try {
                ctx.startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Utility.showToast(ctx, e.message.toString())
            }
        }

        private fun handleConfirm(){
            val inputText = notificationInputText.text.toString().trim()
            this.dismiss()

            if(inputText.isNotEmpty()){
                val inputMessage = Utility.objectToJsonString(InputMessage(inputText, notiList.last().id, packageName))

                Thread{
                    HomeActivity.server?.sendMessage(inputMessage)
                    Utility.showToast(ctx, ctx.getString(R.string.message_sent))
                }.start()

                addNotification(ctx.getString(R.string.you_msg)
                    .replace("{message}", inputMessage))
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