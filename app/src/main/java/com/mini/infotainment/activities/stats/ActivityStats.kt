package com.mini.infotainment.activities.stats

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.mini.infotainment.R
import com.mini.infotainment.activities.stats.chart.StatsChart
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.support.ActivityExtended


class ActivityStats : ActivityExtended() {
    private var chart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        inflateChartBox()
        inflateChartBox()
        inflateChartBox()

        super.pageLoaded()
    }

    fun inflateChartBox(){
        val stats_ll = findViewById<ViewGroup>(R.id.stats_ll)
        val parent = layoutInflater.inflate(
            R.layout.activity_stats_charts,
            stats_ll,
            false
        ) as ViewGroup

        val chart = parent.findViewById<BarChart>(R.id.stats_chart_chart)

        StatsChart(
            this,
            chart!!,
            resources.getStringArray(R.array.days_week),
            StatsData.getDaysOfWeek(),
            StatsData.getAvgSpeedForEachDay(StatsData.Mode.WEEK)
        )

        // TESTING
        val title = parent.findViewById<TextView>(R.id.stats_chart_title)
        title.text = "Distanza percorsa"

        val desc = parent.findViewById<TextView>(R.id.stats_chart_timestamp)
        desc.text = "Oggi"

        stats_ll.addView(parent)
    }

}