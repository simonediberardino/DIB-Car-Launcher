package com.mini.infotainment.activities.stats.tab

import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Animations
import com.mini.infotainment.activities.stats.ActivityStats
import com.mini.infotainment.activities.stats.chart.StatsChart
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.utility.Utility

class StatsWeek(override val ctx: ActivityStats) : StatsTab() {
    override val scrollView: HorizontalScrollView
        get() = ctx.findViewById(R.id.stats_week_sw)
    override val linearLayout: LinearLayout
        get() = ctx.findViewById(R.id.stats_ll_week)
    override val button: TextView
        get() = ctx.findViewById(R.id.stats_week_btn)
    override var xAxisElements: Array<String>? = null
        get() = ctx.resources.getStringArray(R.array.days_week)

    private val daysOfWeek = StatsData.getDaysOfWeek()

    override fun doShow(callback: Runnable?){
        linearLayout.visibility = View.VISIBLE

        Animations
            .moveAnimation(
                scrollView,
                -(Utility.getScreenWidth(ctx) + scrollView.width).toFloat(),
                0f,
                0f,
                0f,
                ANIMATION_DURATION,
                1f
            ){
                callback?.run()
            }
    }

    override fun doHide(callback: Runnable?) {
        linearLayout.visibility = View.VISIBLE

        Animations
            .moveAnimation(
                scrollView,
                0f,
                -(Utility.getScreenWidth(ctx) + scrollView.width).toFloat(),
                0f,
                0f,
                ANIMATION_DURATION,
                1f
            ){
                linearLayout.visibility = View.GONE
                callback?.run()
            }
    }


    override fun createTravDistChart() {
        val distTravEachDay = StatsData.getTraveledDistanceForEachDay(StatsData.Mode.WEEK)
        val totalDistTrav: Float = distTravEachDay.values.sum()

        val title = ctx.getString(R.string.dist_trav)
        val description =
            ctx.getString(R.string.total_km)
                .replace("{tot}", totalDistTrav.toInt().toString())

        super.addChart(
            StatsChart(
                ctx,
                xAxisElements!!,
                daysOfWeek,
                StatsData.getTraveledDistanceForEachDay(StatsData.Mode.WEEK),
                title,
                description
            )
        )
    }


    override fun createMaxSpeedChart() {
        val maxSpeedEachDay = StatsData.getMaxSpeedForEachDay(StatsData.Mode.WEEK)
        val maxSpeedTot: Float = maxSpeedEachDay.values.maxOrNull() ?: 0f

        val title = ctx.getString(R.string.max_speed)
        val description =
            ctx.getString(R.string.total_kmh)
                .replace("{tot}", maxSpeedTot.toInt().toString())

        super.addChart(
            StatsChart(
                ctx,
                xAxisElements!!,
                daysOfWeek,
                maxSpeedEachDay,
                title,
                description
            )
        )
    }

    override fun createAvgSpeedChart() {
        val avgSpeedEachDay = StatsData.getAvgSpeedForEachDay(StatsData.Mode.WEEK)
        val avgSpeedTot = avgSpeedEachDay.values.average()

        val title = ctx.getString(R.string.avg_speed)
        val description =
            ctx.getString(R.string.total_kmh)
                .replace("{tot}", avgSpeedTot.toInt().toString())

        super.addChart(
            StatsChart(
                ctx,
                xAxisElements!!,
                daysOfWeek,
                avgSpeedEachDay,
                title,
                description
            )
        )
    }


}