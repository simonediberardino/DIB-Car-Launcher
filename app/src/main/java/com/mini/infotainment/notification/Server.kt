package com.mini.infotainment.notification

import FirebaseClass
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.QrcodeData
import com.mini.infotainment.utility.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class Server(val activity: HomeActivity) {
    // Porta del socket da creare (Default: 8080);
    var SERVER_PORT = 8080

    // Indirizzo del socket da creare;
    lateinit var serverIPV4: String
    var client: ClientInstance? = null
    var serverSocket: ServerSocket? = null
    var notificationHandler: NotificationHandler? = null

    fun init(){
        this.startSocketServer()
        Thread { this.listenClients() }.start()
    }

    private fun startSocketServer() {
        activity.log("Starting socket.")
        try {
            serverIPV4 = Utility.getLocalIpAddress(activity)
            serverSocket = ServerSocket(SERVER_PORT)

            activity.log("Socket created successfully! Listening on $serverIPV4.")

            this.updateTimeLiveLoop()
            FirebaseClass.updateServerIp(serverIPV4)
            FirebaseClass.updateStartTime(System.currentTimeMillis())

            activity.runOnUiThread {
                notificationHandler = NotificationHandler(activity)

                val bitmap = Utility.generateQrCode(
                    Utility.objectToJsonString(
                        QrcodeData(serverIPV4, ApplicationData.getTarga()!!)
                    ),
                    activity
                )

                bitmap?.let { activity.homePage3.updateQrCode(it) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun listenClients() {
        while (serverSocket != null || serverSocket?.isClosed == false) {
            try {
                this.handleClientConnection(serverSocket?.accept() ?: return)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClientConnection(socket: Socket) {
        this.closeClientInstance()

        ClientInstance(
            DataOutputStream(socket.getOutputStream()),
            DataInputStream(socket.getInputStream()),
            socket.localAddress
        ).also {
            this.client = it

            Utility.showToast(activity, activity.getString(R.string.client_connesso))
            Thread { messageListener(it) }.start()
        }
    }

    private fun messageListener(instance: ClientInstance?) {
        activity.log("Server listening to the client.")

        while (instance?.input != null && !instance.isClosed) {
            try {
                val jsonString = instance.input.readUTF() ?: continue

                activity.runOnUiThread {
                    notificationHandler?.onNotificationReceived(jsonString)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTimeLiveLoop(){
        val delay = 5000L

        Thread{
            while(serverSocket != null || serverSocket?.isClosed == false){
                Thread.sleep(delay)
                FirebaseClass.updateLiveTime(System.currentTimeMillis())
            }
        }.start()
    }

    private fun closeClientInstance() {
        this.client?.close()
        this.client = null
    }

    class ClientInstance(val output: DataOutputStream?, val input: DataInputStream?, val ipv4: InetAddress){
        var isClosed = false

        fun close(){
            this.isClosed = true
            output?.close()
            input?.close()
        }

        fun send(string: String){
            output?.writeUTF(string)
            output?.flush()
        }
    }
}