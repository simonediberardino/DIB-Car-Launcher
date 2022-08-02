package com.mini.infotainment.activities.stats.week

import android.util.DisplayMetrics
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Animations
import com.mini.infotainment.activities.stats.ActivityStats
import com.mini.infotainment.activities.stats.chart.StatsChart
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.activities.stats.tab.StatsTab

class StatsWeek(override val ctx: ActivityStats) : StatsTab() {
    override val scrollView: HorizontalScrollView
        get() = ctx.findViewById(R.id.stats_week_sw)
    override val linearLayout: LinearLayout
        get() = ctx.findViewById(R.id.stats_ll_week)
    override val button: TextView
        get() = ctx.findViewById(R.id.stats_week_btn)
    override var xAxisElements: Array<String>? = null
        get() = ctx.resources.getStringArray(R.array.days_week)

    init {
        inflateAvgSpeedChart()
    }

    fun inflateAvgSpeedChart(){
        val avgSpeedWeek = StatsData.getAvgSpeed(StatsData.Mode.WEEK)
        val title = ctx.getString(R.string.avg_speed)
        val description = "${ctx.getString(R.string.avg_speed)}: ${avgSpeedWeek.toInt()} km/h"

        super.addChart(
            StatsChart(
                ctx,
                xAxisElements!!,
                StatsData.getDaysOfWeek(),
                StatsData.getAvgSpeedForEachDay(StatsData.Mode.WEEK),
                title,
                description
            )
        )
    }

    override fun doShow(){
        val displayMetrics = DisplayMetrics()
        ctx.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        linearLayout.visibility = View.VISIBLE

        Animations
            .moveAnimation(
                scrollView,
                -(screenWidth+scrollView.width).toFloat(),
                0f,
                0f,
                0f,
                ANIMATION_DURATION,
                1f
            ){}
    }

    override fun doHide() {
        val displayMetrics = DisplayMetrics()
        ctx.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        linearLayout.visibility = View.VISIBLE

        Animations
            .moveAnimation(
                scrollView,
                0f,
                -(screenWidth+scrollView.width).toFloat(),
                0f,
                0f,
                ANIMATION_DURATION,
                1f
            ){
                linearLayout.visibility = View.GONE
            }
    }
}