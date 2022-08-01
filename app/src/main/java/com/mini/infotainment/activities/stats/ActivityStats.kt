package com.mini.infotainment.activities.stats

import android.os.Bundle
import com.github.mikephil.charting.charts.BarChart
import com.mini.infotainment.R
import com.mini.infotainment.activities.stats.chart.StatsChart
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.support.ActivityExtended
import java.util.*


class ActivityStats : ActivityExtended() {
    private var chart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        chart = findViewById(R.id.stats_chart)

        val calendar1 = Calendar.getInstance()
        calendar1.add(Calendar.DAY_OF_YEAR, 1)
        StatsData.addSpeedReport(100f, calendar1)
        StatsData.addSpeedReport(120f, Calendar.getInstance())
        StatsData.addSpeedReport(100f, Calendar.getInstance())
        StatsData.addSpeedReport(150f, Calendar.getInstance())
        StatsData.addSpeedReport(50f, Calendar.getInstance())

        StatsChart(
            this,
            chart!!,
            resources.getStringArray(R.array.days_week),
            StatsData.getDaysOfWeek(),
            StatsData.getAvgSpeedForEachDay(StatsData.Mode.WEEK))
        super.pageLoaded()
    }

}