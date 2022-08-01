package com.mini.infotainment.activities.stats

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.mini.infotainment.R
import com.mini.infotainment.activities.stats.chart.StatsChart
import com.mini.infotainment.activities.stats.month.StatsMonth
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.activities.stats.week.StatsWeek
import com.mini.infotainment.support.ActivityExtended


class ActivityStats : ActivityExtended() {
    enum class Options{
        WEEK,
        MONTH
    }
    private var selectedOptions: Options? = null
        set(value) {

        }

    private lateinit var statsMonth: StatsMonth
    private lateinit var statsWeek: StatsWeek

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeLayout()


        inflateWeekChart()
        inflateWeekChart()
        inflateWeekChart()

        inflateMonthChart()
        inflateMonthChart()
        inflateMonthChart()

        this.statsWeek.show()
        super.pageLoaded()
    }

    fun initializeLayout(){
        setContentView(R.layout.activity_stats)

        statsWeek = StatsWeek(this)
        statsMonth = StatsMonth(this)


        statsWeek.button.setOnClickListener {
            it.setOnClickListener {
                this.statsMonth.hide()
                this.statsWeek.show()
            }
        }

        statsMonth.button.setOnClickListener {
            it.setOnClickListener {
                this.statsWeek.hide()
                this.statsMonth.show()
            }
        }
    }

    private fun highlightWeekButton(){

    }


    fun inflateWeekChart(){
        val linearLayout = this.statsWeek.linearLayout
        val parent = layoutInflater.inflate(
            R.layout.activity_stats_charts,
            linearLayout,
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
        title.text = "Distance travelled"

        val desc = parent.findViewById<TextView>(R.id.stats_chart_timestamp)
        desc.text = "Current week"

        this.statsWeek.linearLayout.visibility = View.GONE

        linearLayout.addView(parent)
    }

    fun inflateMonthChart(){
        val linearLayout = this.statsMonth.linearLayout
        val parent = layoutInflater.inflate(
            R.layout.activity_stats_charts,
            linearLayout,
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
        title.text = "Distance travelled"

        val desc = parent.findViewById<TextView>(R.id.stats_chart_timestamp)
        desc.text = "Current week"

        this.statsMonth.linearLayout.visibility = View.GONE
        linearLayout.addView(parent)
    }



}

/*
package com.mini.infotainment.activities.stats

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.mini.infotainment.R
import com.mini.infotainment.UI.Animations
import com.mini.infotainment.activities.stats.chart.StatsChart
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.support.ActivityExtended


class ActivityStats : ActivityExtended() {
    private lateinit var statsWeekSw: HorizontalScrollView
    private lateinit var statsMonthSw: HorizontalScrollView
    private lateinit var this.statsWeek.linearLayout: LinearLayout
    private lateinit var statsMonthLL: LinearLayout
    private lateinit var weekBtn: TextView
    private lateinit var monthBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeLayout()

        inflateWeekChart()
        inflateWeekChart()
        inflateWeekChart()

        inflateMonthChart()
        inflateMonthChart()
        inflateMonthChart()

        this.showWeekTab()
        super.pageLoaded()
    }

    fun initializeLayout(){
        setContentView(R.layout.activity_stats)
        statsWeekSw = findViewById(R.id.stats_week_sw)
        statsMonthSw = findViewById(R.id.stats_month_sw)
        this.statsWeek.linearLayout = findViewById(R.id.stats_ll_week)
        statsMonthLL = findViewById(R.id.stats_ll_month)

        weekBtn = findViewById<TextView?>(R.id.stats_week).also {
            it.setOnClickListener {
                this.showWeekCharts()
            }
        }

        monthBtn = findViewById<TextView>(R.id.stats_month).also{
            it.setOnClickListener{
                this.showMonthCharts()
            }
        }
    }

    private fun highlightWeekButton(){

    }

    private fun showWeekCharts(){
        if(!isWeekTabVisible()) {
            this.hideMonthTab()
            this.showWeekTab()
        }
    }


    private fun showMonthCharts(){
        if(!isMonthTabVisible()) {
            this.hideWeekTab()
            this.showMonthTab()
        }
    }


    private fun showMonthTab(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        statsMonthLL.visibility = VISIBLE
        statsMonthSw.requestFocus()

        Animations
            .moveAnimation(
                statsMonthSw,
                (screenWidth+statsMonthSw.width).toFloat(),
                0f,
                0f,
                0f,
                600
            ){}
    }

    private fun hideMonthTab(callback: Runnable? = null){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        statsMonthLL.visibility = VISIBLE

        Animations
            .moveAnimation(
                statsMonthSw,
                0f,
                (screenWidth+statsMonthSw.width).toFloat(),
                0f,
                0f,
                600
            ){
                statsMonthLL.visibility = View.GONE
                callback?.run()
            }
    }

    private fun showWeekTab(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        this.statsWeek.linearLayout.visibility = VISIBLE
        statsWeekSw.requestFocus()

        Animations
            .moveAnimation(
                statsWeekSw,
                -(screenWidth+statsWeekSw.width).toFloat(),
                0f,
                0f,
                0f,
                600
            ){}
    }


    private fun hideWeekTab(callback: Runnable? = null){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        this.statsWeek.linearLayout.visibility = VISIBLE

        Animations
            .moveAnimation(
                statsWeekSw,
                0f,
                -(screenWidth+statsWeekSw.width).toFloat(),
                0f,
                0f,
                600
            ){
                this.statsWeek.linearLayout.visibility = View.GONE
                callback?.run()
            }
    }

    fun inflateWeekChart(){
        val stats_ll = findViewById<ViewGroup>(R.id.stats_ll_week)
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
        title.text = "Distance travelled"

        val desc = parent.findViewById<TextView>(R.id.stats_chart_timestamp)
        desc.text = "Current week"

        this.statsWeek.linearLayout.visibility = View.GONE

        stats_ll.addView(parent)
    }

    fun inflateMonthChart(){
        val stats_ll = findViewById<ViewGroup>(R.id.stats_ll_month)
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
        title.text = "Distance travelled"

        val desc = parent.findViewById<TextView>(R.id.stats_chart_timestamp)
        desc.text = "Current week"

        statsMonthLL.visibility = View.GONE
        stats_ll.addView(parent)
    }

    fun isWeekTabVisible(): Boolean {
        return this.statsWeek.linearLayout.visibility == VISIBLE
    }

    fun isMonthTabVisible(): Boolean{
        return statsMonthLL.visibility == VISIBLE
    }
}
 */