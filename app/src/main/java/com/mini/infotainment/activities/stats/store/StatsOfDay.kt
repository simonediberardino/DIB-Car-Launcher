package com.mini.infotainment.activities.stats.store


// Represents the object that contains the stats of every single day
data class StatsOfDay(var travDist: Float = 0f,
                      var regSpeeds: MutableList<Float> = mutableListOf(),
                      var maxSpeed: Float = 0f)