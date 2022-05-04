package com.mini.infotainment.notification

import FirebaseClass
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.utility.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class Server(val activity: HomeActivity) {
    // Porta del socket da creare (Default: 8080);
    var SERVER_PORT = 8080

    // Indirizzo del socket da creare;
    lateinit var serverIPV4: String
    private lateinit var instances: ArrayList<ClientInstance?>
    private var serverSocket: ServerSocket? = null
    var notificationHandler: NotificationHandler? = null

    fun init(){
        startSocketServer()
        Thread { listenClients() }.start()
    }

    /**
     * Questo metodo crea e starta il server socket e aggiorna le TextView con le informazioni relative al socket;
     * @return void;
     */
    private fun startSocketServer() {
        try {
            // Creazione del socket
            serverIPV4 = Utility.getLocalIpAddress(activity)
            serverSocket = ServerSocket(SERVER_PORT)

            println("Socket creato con successo! Ascoltando sull'IP $serverIPV4.")

            FirebaseClass.updateServerIp(serverIPV4)

            activity.runOnUiThread {
                notificationHandler = NotificationHandler(activity)
                val bitmap = Utility.generateQrCode(serverIPV4, activity)
                bitmap?.let { activity.homePage3.updateQrCode(it) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun listenClients() {
        var socket: Socket?
        instances = ArrayList()
        while (serverSocket != null) {
            try {
                socket = serverSocket!!.accept()

                instances.find { it!!.ipv4.toString() == socket.localAddress.toString() }?.let {
                    closeClientInstance(it)
                }

                val clientInstance = ClientInstance(
                    DataOutputStream(socket.getOutputStream()),
                    DataInputStream(socket.getInputStream()),
                    socket.localAddress
                )

                instances.add(clientInstance)

                Utility.showToast(activity, activity.getString(R.string.client_connesso))
                // Il server si mette in ascolto del client;
                Thread { messageListener(clientInstance) }.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * In questo metodo l'oggetto input si mette in ascolto del client e vengono stampati gli eventuali messaggi in entrata;
     */
    private fun messageListener(instance: ClientInstance?) {
        println("Server in ascolto del client.")

        while (instance?.input != null) {
            try {
                val jsonString = instance.input.readUTF() ?: continue

                println("Messaggio ricevuto: $jsonString.")

                activity.runOnUiThread {
                    notificationHandler?.onNotificationReceived(jsonString)
                }
            } catch (e: IOException) {
                closeClientInstance(instance)
                e.printStackTrace()
                return
            }
        }
    }

    /**
     * Questo metodo invia un messaggio ai client utilizzando l'oggetto che gestisce i messaggi in uscita;
     * @param jsonString da inviare al server;
     * @throws IOException se si riscontra un errore durante l'invio di un messaggio ai client;
     */
    @Throws(IOException::class)
    fun sendMessage(instance: ClientInstance, jsonString: String) {
        instance.output?.writeUTF(jsonString)
        instance.output?.flush()
    }

    fun sendMessage(jsonString: String){
        for (instance in instances)
            sendMessage(instance!!, jsonString)
    }

    private fun closeClientInstance(clientInstance: ClientInstance){
        clientInstance.input?.close()
        clientInstance.output?.close()
        instances.remove(clientInstance)
    }
    
    private fun closeServer() {
        try {
            for (i in instances) {
                i?.output?.close()
                i?.input?.close()
            }

            serverSocket?.close()
            println("Socket chiuso con successo.")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class ClientInstance(val output: DataOutputStream?, val input: DataInputStream?, val ipv4: InetAddress)
}