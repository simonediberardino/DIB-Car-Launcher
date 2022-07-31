package com.mini.infotainment.activities.stats

import android.os.Bundle
import com.github.mikephil.charting.charts.BarChart
import com.mini.infotainment.R
import com.mini.infotainment.support.ActivityExtended


class ActivityStats : ActivityExtended() {
    private val MAX_X_VALUE = 7
    private val MAX_Y_VALUE = 45
    private val MIN_Y_VALUE = 5
    private val DAYS = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    private var chart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatsData().getWeekData()
        setContentView(R.layout.activity_stats)
    }


/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        chart = findViewById(R.id.chart)
        val data = createChartData()
        configureChartAppearance()
        prepareChartData(data)
        super.pageLoaded()
    }

    private fun configureChartAppearance() {
        chart!!.description.isEnabled = false
        val xAxis = chart!!.xAxis
        xAxis.valueFormatter =
            IAxisValueFormatter { value, axis ->
                axis?.textColor = Color.WHITE
                axis?.textSize = Utility.convertValue(
                    8f,
                    this@ActivityStats)
                    .toFloat()
                DAYS[value.toInt()]
            }

        val sideValueFormatter = IAxisValueFormatter { value, axis ->
            axis?.textColor = Color.WHITE
            axis?.textSize = Utility.convertValue(
                8f,
                this@ActivityStats)
                .toFloat()
            value.toString()
        }

        chart!!.axisLeft.valueFormatter = sideValueFormatter
        chart!!.axisRight.valueFormatter = sideValueFormatter
    }

    private fun createChartData(): BarData {
        val values: ArrayList<BarEntry> = ArrayList()
        for (i in 0 until MAX_X_VALUE) {
            val x = i.toFloat()
            val y: Float = Random
                .nextInt(
                    IntRange(MIN_Y_VALUE, MAX_Y_VALUE)
                ).toFloat()

            values.add(BarEntry(x, y))
        }
        val set1 = BarDataSet(values, String())
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(set1)
        return BarData(dataSets)
    }

    private fun prepareChartData(data: BarData) {
        data.setValueTextSize(Utility.convertValue(12f, this).toFloat())
        data.setValueTextColor(Color.WHITE)
        chart!!.data = data
        chart!!.invalidate()
    }*/
}