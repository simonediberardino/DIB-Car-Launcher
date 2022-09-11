package com.mini.infotainment.gps

import com.mini.infotainment.utility.Utility

class TripHandler(val tickCallback: Runnable) {
    private var timePassed: Long = 0
    private lateinit var thread: Thread

    val elapsedTime: String
        get() {
            return Utility.millisToHoursFormatted(timePassed)
        }

    fun start(){
        timePassed = 0
        thread = Thread{
            while (true){
                Thread.sleep(1000)
                timePassed+=1000
                tickCallback.run()
            }
        }.apply { start() }
    }

    fun stop(){
        thread.interrupt()
    }

    fun reset(){
        timePassed = 0
    }
}