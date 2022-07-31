package com.mini.infotainment.activities.stats

import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.utility.Utility
import java.util.*

class StatsData {
    companion object{
        private val STATS_ID = "STATS-ID"
        private val STATS_DEFAULT = "{}"
    }

    fun getStats(): HashMap<String, String> {
        return Utility.jsonStringToObject(
            ApplicationData.applicationData.getString(
                STATS_ID,
                STATS_DEFAULT
            )!!
        )
    }

    fun setStats(stats: HashMap<String, String>){
        setStats(
            Utility.objectToJsonString(stats)
        )
    }

    fun setStats(stats: String){
        val dataEditor = ApplicationData.applicationData.edit()
        dataEditor.putString(STATS_ID, stats)
        dataEditor.apply()
    }

    // WIP
    fun getWeekData(){
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val startDay = (dayOfWeek - 1)
        calendar.add(Calendar.DAY_OF_YEAR, - startDay)

        val hashMap = hashMapOf<String, String>()
        for(i in 1..7){
            val key = Utility.getDateString(calendar)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        println(hashMap.toString())
    }
}