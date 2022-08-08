package com.mini.infotainment.activities.stats

import android.os.Bundle
import android.view.ViewGroup
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity.Companion.instance
import com.mini.infotainment.activities.stats.tab.StatsMonth
import com.mini.infotainment.activities.stats.tab.StatsWeek
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility


class ActivityStats : SActivity() {
    private lateinit var statsMonth: StatsMonth
    private lateinit var statsWeek: StatsWeek

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initializeLayout()
        super.pageLoaded()
        this.createCharts()
    }

    fun initializeLayout(){
        setContentView(R.layout.activity_stats)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(Utility.getWallpaper(instance))

    }

    fun createCharts(){
/*        var calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        StatsData.addSpeedReport(10f, calendar)
        StatsData.addSpeedReport(50f, calendar)
        StatsData.addSpeedReport(55f, calendar)
        StatsData.increaseTraveledDistance(2f, calendar)
        StatsData.increaseTraveledDistance(5f, calendar)
        StatsData.increaseTraveledDistance(7f, calendar)


        calendar.add(Calendar.DAY_OF_YEAR, 1)
        StatsData.addSpeedReport(70f, calendar)
        StatsData.addSpeedReport(100f, calendar)
        StatsData.addSpeedReport(80f, calendar)
        StatsData.increaseTraveledDistance(10f, calendar)
        StatsData.increaseTraveledDistance(10f, calendar)
        StatsData.increaseTraveledDistance(5f, calendar)

        calendar.add(Calendar.DAY_OF_YEAR, 3)
        StatsData.addSpeedReport(50f, calendar)
        StatsData.addSpeedReport(10f, calendar)
        StatsData.addSpeedReport(30f, calendar)
        StatsData.increaseTraveledDistance(2f, calendar)
        StatsData.increaseTraveledDistance(10f, calendar)
        StatsData.increaseTraveledDistance(5f, calendar)


        calendar.add(Calendar.DAY_OF_YEAR, 3)
        StatsData.addSpeedReport(150f, calendar)
        StatsData.addSpeedReport(10f, calendar)
        StatsData.addSpeedReport(40f, calendar)
        StatsData.increaseTraveledDistance(2f, calendar)
        StatsData.increaseTraveledDistance(20f, calendar)
        StatsData.increaseTraveledDistance(5f, calendar)*/

        statsWeek = StatsWeek(this)
        statsWeek.create()

        statsMonth = StatsMonth(this)
        statsMonth.create()

        statsWeek.button.setOnClickListener {
            this.statsMonth.hide()
            this.statsWeek.show()
        }

        statsMonth.button.setOnClickListener {
            this.statsWeek.hide()
            this.statsMonth.show()
        }

        this.statsWeek.show()
        this.statsMonth.hide()
    }
}