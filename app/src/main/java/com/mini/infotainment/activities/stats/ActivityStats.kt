package com.mini.infotainment.activities.stats

import android.os.Bundle
import android.view.ViewGroup
import com.mini.infotainment.R
import com.mini.infotainment.activities.stats.tab.StatsMonth
import com.mini.infotainment.activities.stats.tab.StatsWeek
import com.mini.infotainment.support.SActivity


class ActivityStats : SActivity() {
    private lateinit var statsMonth: StatsMonth
    private lateinit var statsWeek: StatsWeek

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initializeLayout()
        super.pageLoaded()
        this.createCharts()
    }

    private fun initializeLayout(){
        setContentView(R.layout.activity_stats)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(wpaper)

    }

    private fun createCharts(){
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