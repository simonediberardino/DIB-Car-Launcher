package com.mini.infotainment.gps

import com.mini.infotainment.utility.Utility

class TripHandler(val tickCallback: Runnable) {
    private var startTime: Long = -1
    private lateinit var thread: Thread

    fun getElapsedTime(): String {
        val currTime = System.currentTimeMillis()
        val elapsTime = (currTime) - startTime

        return Utility.millisToHoursFormatted(elapsTime)
    }

    fun start(){
        thread = Thread{
            startTime = System.currentTimeMillis()

            while (true){
                Thread.sleep(1000)
                tickCallback.run()
            }
        }.apply { start() }
    }

    fun stop(){
        thread.interrupt()
    }
}