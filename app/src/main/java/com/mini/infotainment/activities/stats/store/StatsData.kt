package com.mini.infotainment.activities.stats.store

import com.google.gson.internal.LinkedTreeMap
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.utility.Utility
import java.util.*

object StatsData {
    private val STATS_ID = "STATS_ID"
    private val STATS_DEFAULT = "{}"

    enum class Mode{
        DAY,
        WEEK,
        MONTH
    }

    private fun getStats(): HashMap<String, StatsOfDay> {
        return Utility.jsonStringToObject(
            ApplicationData.applicationData.getString(
                STATS_ID,
                STATS_DEFAULT
            )!!
        )
    }

    private fun setStats(stats: HashMap<String, StatsOfDay>){
        setStats(Utility.objectToJsonString(stats))
    }

    private fun setStats(stats: String){
        val dataEditor = ApplicationData.applicationData.edit()
        dataEditor.putString(STATS_ID, stats)
        dataEditor.apply()
    }

    fun getAvgSpeedForEachDay(mode: Mode): HashMap<String, Float> {
        return getAvgSpeedForEachDay(Calendar.getInstance(TimeZone.getDefault()), mode)
    }

    fun getAvgSpeedForEachDay(calendar: Calendar, mode: Mode): HashMap<String, Float> {
        val hashMap = hashMapOf<String, Float>()

        //((data?.get("regSpeeds") as MutableList<Float>?)
        //                    ?.average() ?: 0f).toFloat()
        val data = when (mode) {
            Mode.WEEK -> {
                getWeekData(calendar)
            }
            Mode.MONTH -> {
                getMonthData()
            }
            else -> return hashMapOf()
        }

        for(key: String in data.keys){
            val regSpeeds =
                ((data[key]) as LinkedTreeMap<*, *>?)
                    ?.get("regSpeeds") as MutableList<Float>?

            hashMap[key] = regSpeeds?.average()
                ?.toFloat() ?: 0f

        }
        return hashMap
    }

    fun getAvgSpeed(mode: Mode): Double {
        return getAvgSpeed(Calendar.getInstance(TimeZone.getDefault()), mode)
    }

    fun getAvgDaySpeed(key: String): Double {
        return getDayData(key).regSpeeds.average()
    }

    // TODO: Delete reg speed of other days
    fun getAvgSpeed(calendar: Calendar, mode: Mode): Double {
        return when (mode) {
            Mode.DAY -> {
                getDayData(calendar).regSpeeds.average()
            }
            Mode.WEEK -> {
                val weekData = getWeekData(calendar)
                weekData.flatMap { it.value.regSpeeds }.average()
            }
            else -> {
                val weekData = getWeekData(calendar)
                weekData.flatMap { it.value.regSpeeds }.average()
            }
        }
    }

    fun getMaxSpeed(mode: Mode) : Float{
        return getMaxSpeed(Calendar.getInstance(TimeZone.getDefault()), mode)
    }

    fun getMaxSpeed(calendar: Calendar, mode: Mode): Float {
        return if(mode == Mode.DAY){
            getDayData(calendar).maxSpeed
        }else{
            val data = if(mode == Mode.WEEK)
                getWeekData(calendar)
            else getMonthData(calendar)

            data.maxOf {
                it.value.maxSpeed
            }
        }
    }

    fun setMaxSpeed(maxSpeed: Float, calendar: Calendar){
        val stats = getStats()
        val key = Utility.getDateString(calendar)

        if(!stats.containsKey(key))
            stats[key] = StatsOfDay()

        stats[key]?.maxSpeed = maxSpeed

        setStats(stats)
    }

    fun increaseTraveledDistance(dist: Float, calendar: Calendar){
        val stats = getStats()
        val key = Utility.getDateString(calendar)

        if(!stats.containsKey(key))
            stats[key] = StatsOfDay()

        val curTravDist = stats[key]?.travDist ?: 0f
        stats[key]?.travDist = stats[key]?.travDist?.plus(curTravDist) ?: dist

        setStats(stats)
    }

    // CHECK MAX SPEED
    fun addSpeedReport(curSpeed: Float, calendar: Calendar){
        val stats = getStats()

        // Need to convert the object to string in order to prevent com.google.gson.internal.LinkedTreeMap cannot be cast to class
        val parent = Utility.jsonStringToObject<HashMap<String, StatsOfDay>>(
            Utility.objectToJsonString(stats)
        )

        val key = Utility.getDateString(calendar)

        if(!parent.containsKey(key))
            parent[key] = StatsOfDay()

        val child = Utility.jsonStringToObject<StatsOfDay>(
            Utility.objectToJsonString(stats[key])
        )

        child.regSpeeds.add(curSpeed)
        parent[key] = child

        setStats(parent)
    }

    fun getTraveledDistance(mode: Mode): Float {
        return getTraveledDistance(Calendar.getInstance(TimeZone.getDefault()), mode)
    }

    fun getTraveledDistance(calendar: Calendar, mode: Mode): Float {
        return if(mode == Mode.DAY){
            getDayData(calendar).travDist
        }else{
            val data = if(mode == Mode.WEEK)
                getWeekData(calendar)
            else getMonthData(calendar)

            data.values.sumOf {
                it.travDist.toInt()
            }.toFloat()
        }
    }

    fun getDayData() : StatsOfDay {
        return getDayData(Calendar.getInstance(TimeZone.getDefault()))
    }

    fun getDayData(calendar: Calendar): StatsOfDay {
        return getStats()[Utility.getDateString(calendar)] ?: StatsOfDay()
    }

    fun getDayData(key: String): StatsOfDay {
        return getStats()[key] ?: StatsOfDay()
    }

    fun getDaysOfWeek() : Array<String> {
        return getDaysOfWeek(Calendar.getInstance(TimeZone.getDefault()))
    }

    fun getDaysOfWeek(calendar: Calendar): Array<String> {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val startDay = (dayOfWeek - 1)
        calendar.add(Calendar.DAY_OF_YEAR, - startDay)

        val availableDates = Array(7){String()}
        for(i in 0..6){
            availableDates[i] = (Utility.getDateString(calendar))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return availableDates
    }

    fun getWeekData() : Map<String, StatsOfDay>{
        return getWeekData(Calendar.getInstance(TimeZone.getDefault()))
    }

    fun getWeekData(calendar: Calendar): Map<String, StatsOfDay> {
        val availableDates = getDaysOfWeek(calendar)
        return getStats()
            .filter {
                availableDates.contains(it.key)
            }
    }

    fun getMonthData() : Map<String, StatsOfDay>{
        return getMonthData(Calendar.getInstance(TimeZone.getDefault()))
    }

    fun getMonthData(calendar: Calendar): Map<String, StatsOfDay> {
        return getStats()
            .filter {
                val tokens = it.key.split("-")
                val month = tokens[1]
                val year = tokens[2]
                (calendar.get(Calendar.MONTH) + 1).toString() == month
                        && calendar.get(Calendar.YEAR).toString() == year
            }
    }
}
