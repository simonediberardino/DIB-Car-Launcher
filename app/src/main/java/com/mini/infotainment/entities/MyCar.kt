package com.mini.infotainment.entities

import com.mini.infotainment.gps.LocationExtended

data class MyCar(
    var plateNum: String,
    var password: String = String(),
    var serverip: String = "0.0.0.0",
    val start: Long = 0,
    val time: Long = 0,
    var location: LocationExtended = LocationExtended(),
    var premiumDate: Long? = 0
){
    constructor() : this(String(), String())

    companion object{
        lateinit var instance: MyCar
    }

    fun isPremium(): Boolean {
        println("${this.premiumDate} > System.currentTimeMillis()")
        return this.premiumDate!! > System.currentTimeMillis()
    }
}