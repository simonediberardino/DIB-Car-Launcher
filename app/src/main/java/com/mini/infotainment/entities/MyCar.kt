package com.mini.infotainment.entities

import com.mini.infotainment.gps.LocationExtended

data class MyCar(
    var plateNum: String,
    var password: String,
    var serverip: String = "0.0.0.0",
    val start: Long = 0,
    val time: Long = 0,
    var location: LocationExtended = LocationExtended(),
    val premiumDate: Long = 0
){
    constructor() : this(String(), String())
}