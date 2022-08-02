package com.mini.infotainment.activities.stats.store


// Represents the object that contains the stats of every single day
data class StatsOfDay(var travDist: Float = 0f,
                      var maxSpeed: Float = 0f,
                      var avgSpeed: AvgSpeed = AvgSpeed()
){
    data class AvgSpeed(var value: Float = 0f, var nElements: Int = 0)
}