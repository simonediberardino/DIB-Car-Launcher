package com.mini.infotainment.http

import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class SocketServer(val activity: HomeActivity) {
    var SERVER_PORT = 8080

    var serverIPV4 = Utility.getLocalIpAddress(activity)
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
            serverSocket = ServerSocket(SERVER_PORT)

            activity.log("Socket created successfully! Listening on $serverIPV4.")

            this.updateTimeLiveLoop()
            FirebaseClass.updateServerIp(serverIPV4)
            FirebaseClass.updateStartTime(System.currentTimeMillis())

            notificationHandler = NotificationHandler(activity)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun listenClients() {
        while (serverSocket != null || serverSocket?.isClosed != true) {
            try {
                FirebaseClass.isPremiumCar(ApplicationData.getTarga()!!, object : RunnablePar{
                    override fun run(p: Any?) {
                        val isPremium = p as Boolean
                        if(isPremium)
                            handleClientConnection(serverSocket?.accept() ?: return)
                    }
                })
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
            this.output?.close()
            this.input?.close()
        }

        fun send(string: String){
            this.output?.writeUTF(string)
            this.output?.flush()
        }
    }
}