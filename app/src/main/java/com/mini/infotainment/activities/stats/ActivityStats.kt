package com.mini.infotainment.activities.stats

import android.os.Bundle
import com.mini.infotainment.R
import com.mini.infotainment.activities.stats.month.StatsMonth
import com.mini.infotainment.activities.stats.week.StatsWeek
import com.mini.infotainment.support.ActivityExtended


class ActivityStats : ActivityExtended() {
    private lateinit var statsMonth: StatsMonth
    private lateinit var statsWeek: StatsWeek

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initializeLayout()

        this.statsWeek.show()
        this.statsMonth.hide()
        
        super.pageLoaded()
    }

    fun initializeLayout(){
        setContentView(R.layout.activity_stats)

        statsWeek = StatsWeek(this)
        statsMonth = StatsMonth(this)

        statsWeek.button.setOnClickListener {
            this.statsMonth.hide()
            this.statsWeek.show()
        }

        statsMonth.button.setOnClickListener {
            this.statsWeek.hide()
            this.statsMonth.show()
        }
    }
}