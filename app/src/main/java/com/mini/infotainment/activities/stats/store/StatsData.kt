package com.mini.infotainment.activities.stats.store

import com.google.gson.internal.LinkedTreeMap
import com.mini.infotainment.data.Data
import com.mini.infotainment.utility.Utility
import com.mini.infotainment.utility.Utility.kmToMile
import com.mini.infotainment.utility.Utility.toKm
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
            Data.applicationData.getString(
                STATS_ID,
                STATS_DEFAULT
            )!!
        )
    }

    private fun setStats(stats: HashMap<String, StatsOfDay>){
        setStats(Utility.objectToJsonString(stats))
    }

    private fun setStats(stats: String){
        val dataEditor = Data.applicationData.edit()
        dataEditor.putString(STATS_ID, stats)
        dataEditor.apply()
    }

    fun getAvgSpeedForEachDay(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): HashMap<String, Float> {
        val hashMap = hashMapOf<String, Float>()
        val data = getDataFromMode(mode, calendar)

        for(key: String in data.keys){
            val avgSpeed =
                ((data[key]) as LinkedTreeMap<*, *>?)
                    ?.get("avgSpeed") as LinkedTreeMap<*, *>

            hashMap[key] = if(Utility.isUMeasureKM())(avgSpeed["value"] as Double).toFloat() else (avgSpeed["value"] as Double).toFloat().kmToMile()
        }
        return hashMap
    }

    fun getAvgSpeed(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Double {
        val data = getDataFromMode(mode, calendar)
        val values = mutableListOf<Float>()

        data.forEach {
            val avgSpeed =
                ((data[it.key]) as LinkedTreeMap<*, *>?)
                    ?.get("avgSpeed") as LinkedTreeMap<*, *>

            for(i in 0 until (avgSpeed["nElements"] as Double).toInt())
                values.add((avgSpeed["value"] as Double).toFloat())
        }
        return values.average()
    }

    fun getMaxSpeedForEachDay(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): HashMap<String, Float>{
        val hashMap = hashMapOf<String, Float>()
        val data = getDataFromMode(mode, calendar)

        for(key: String in data.keys){
            val maxSpeed =
                (((data[key]) as LinkedTreeMap<*, *>?)
                    ?.get("maxSpeed") as Double).toFloat()

            hashMap[key] = if(Utility.isUMeasureKM()) maxSpeed else maxSpeed.kmToMile()
        }
        return hashMap
    }

    fun getMaxSpeed(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Float {
        return getDataFromMode(mode, calendar).maxOf { it.value.maxSpeed }
    }

    fun setMaxSpeed(maxSpeed: Float, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())){
        val stats = getStats()
        val key = Utility.getDateString(calendar)

        if(!stats.containsKey(key))
            stats[key] = StatsOfDay()

        // Need to convert the object to string in order to prevent com.google.gson.internal.LinkedTreeMap cannot be cast to class
        val child = Utility.jsonStringToObject<StatsOfDay>(
            Utility.objectToJsonString(stats[key])
        )

        child.maxSpeed = maxSpeed
        stats[key] = child

        setStats(stats)
    }

    fun increaseTraveledDistance(dist: Float, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())){
        if(dist <= 0) return

        val stats = getStats()
        val key = Utility.getDateString(calendar)

        if(!stats.containsKey(key))
            stats[key] = StatsOfDay()

        // Need to convert the object to string in order to prevent com.google.gson.internal.LinkedTreeMap cannot be cast to class
        val child = Utility.jsonStringToObject<StatsOfDay>(
            Utility.objectToJsonString(stats[key])
        )

        child.travDist += dist
        stats[key] = child

        setStats(stats)
    }

    fun addSpeedReport(curSpeed: Float, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())){
        val curSpeed = curSpeed.toInt().toFloat()
        val stats = getStats()
        val key = Utility.getDateString(calendar)

        if(!stats.containsKey(key))
            stats[key] = StatsOfDay()

        // Need to convert the object to string in order to prevent com.google.gson.internal.LinkedTreeMap cannot be cast to class
        val child = Utility.jsonStringToObject<StatsOfDay>(
            Utility.objectToJsonString(stats[key])
        )

        child.avgSpeed.nElements++
        child.avgSpeed.value = ((child.avgSpeed.value * (child.avgSpeed.nElements-1)) + curSpeed) / child.avgSpeed.nElements
        child.maxSpeed = Math.max(child.maxSpeed, curSpeed)

        stats[key] = child
        setStats(stats)
    }

    fun getTraveledDistanceForEachDay(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): HashMap<String, Float> {
        val hashMap = hashMapOf<String, Float>()
        val data = getDataFromMode(mode, calendar)

        for(key: String in data.keys){
            val valueInKm = (((data[key]) as LinkedTreeMap<*, *>?)
                ?.get("travDist") as Double).toKm()

            hashMap[key] = if(Utility.isUMeasureKM()) valueInKm else valueInKm.kmToMile()
        }

        return hashMap
    }

    fun getTraveledDistance(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Float {
        return getTraveledDistanceForEachDay(mode, calendar).values.sum()
    }

    fun getDayDataValue(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): StatsOfDay {
        return getStats()[Utility.getDateString(calendar)] ?: StatsOfDay()
    }

    fun getDayDataMap(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())) : Map<String, StatsOfDay> {
        return getStats()
            .filter { it.key ==  Utility.getDateString(calendar)}
    }

    fun getDaysOfWeek(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Array<String> {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val firstDayOfWeek = (dayOfWeek - 1)
        calendar.add(Calendar.DAY_OF_YEAR, - firstDayOfWeek)

        val availableDates = Array(7){String()}
        for(i in 0..6){
            availableDates[i] = (Utility.getDateString(calendar))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return availableDates
    }

    fun getDaysOfMonth(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Array<String> {
        val curMonth = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = (dayOfMonth - 1)
        calendar.add(Calendar.DAY_OF_YEAR, - firstDayOfMonth)

        val availableDates = mutableListOf<String>()
        while(calendar.get(Calendar.MONTH) == curMonth){
            availableDates.add((Utility.getDateString(calendar)))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return availableDates.toTypedArray()
    }

    fun getDaysOfMonthComplete(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Array<String> {
        return getDaysOfMonth(calendar)
            .mapIndexed {
                    index, s -> "${(index+1)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            }.toTypedArray()
    }

    fun getWeekData(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Map<String, StatsOfDay> {
        val availableDates = getDaysOfWeek(calendar)
        return getStats()
            .filter {
                availableDates.contains(it.key)
            }
    }

    fun getMonthData(calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Map<String, StatsOfDay> {
        return getStats()
            .filter {
                val tokens = it.key.split("-")
                val month = tokens[1]
                val year = tokens[2]
                (calendar.get(Calendar.MONTH) + 1).toString() == month
                        && calendar.get(Calendar.YEAR).toString() == year
            }
    }

    fun getDataFromMode(mode: Mode, calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())): Map<String, StatsOfDay> {
        return when (mode) {
            Mode.DAY -> {
                getDayDataMap(calendar)
            }
            Mode.WEEK -> {
                getWeekData(calendar)
            }
            Mode.MONTH -> {
                getMonthData(calendar)
            }
        }
    }
}
