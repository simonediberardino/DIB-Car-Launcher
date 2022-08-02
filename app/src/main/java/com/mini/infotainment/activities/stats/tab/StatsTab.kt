package com.mini.infotainment.activities.stats.tab

import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.stats.chart.StatsChart

abstract class StatsTab : Page(){
    abstract val scrollView: HorizontalScrollView
    abstract val linearLayout: LinearLayout
    abstract val button: TextView

    protected var ANIMATION_DURATION = 400L
    protected var STATE_IDLE = 0
    protected var STATE_INACTIVE = 1
    protected var STATE_SELECTED = 2
    protected var state = STATE_IDLE
    protected abstract fun doShow()
    protected abstract fun doHide()


    fun show(){
        if(state == STATE_SELECTED) return
        state = STATE_SELECTED
        button.setBackgroundResource(R.drawable.square_light_grey_round)
        this.doShow()
    }

    fun hide(){
        if(state == STATE_INACTIVE) return
        state = STATE_INACTIVE
        button.setBackgroundResource(0)
        this.doHide()
    }

    fun addChart(statsChart: StatsChart){
        val linearLayout = linearLayout
        val parent = ctx.layoutInflater.inflate(
            R.layout.activity_stats_charts,
            linearLayout,
            false
        ) as ViewGroup

        val barChart = parent.findViewById<BarChart>(R.id.stats_chart_chart)

        statsChart.barChart = barChart

        val title = parent.findViewById<TextView>(R.id.stats_chart_title)
        title.text = statsChart.title

        val desc = parent.findViewById<TextView>(R.id.stats_chart_timestamp)
        desc.text = statsChart.description

        statsChart.apply()
        linearLayout.addView(parent)
    }
}