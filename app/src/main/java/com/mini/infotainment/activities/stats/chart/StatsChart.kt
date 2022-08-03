package com.mini.infotainment.activities.stats.chart

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.mini.infotainment.utility.Utility

@SuppressLint("ViewConstructor")
class StatsChart(
    var activity: AppCompatActivity,
    var descriptions: Array<String>,
    var keysSorted: Array<String>,
    var chartValues: Map<String, Float>,
    var title: String,
    val description: String)
{
    var barChart: BarChart? = null

    fun apply(){
        configureChartAppearance()
        prepareChartData(createChartData())
    }

    private fun configureChartAppearance() {
        barChart!!.description.isEnabled = false
        barChart!!.xAxis.valueFormatter =
            IAxisValueFormatter { value, axis ->
                axis?.textColor = Color.WHITE
                axis?.textSize = Utility.convertValue(
                    8f,
                    activity)
                    .toFloat()
                descriptions[value.toInt()]
            }

        val sideValueFormatter = IAxisValueFormatter { value, axis ->
            axis?.textColor = Color.WHITE
            axis?.textSize = Utility.convertValue(8f, activity).toFloat()

            value.toString()
        }

        barChart!!.axisLeft.valueFormatter = sideValueFormatter
        barChart!!.axisRight.valueFormatter = sideValueFormatter
    }

    private fun createChartData(): BarData {
        val values: ArrayList<BarEntry> = ArrayList()
        for (i in keysSorted.indices) {
            val x = i.toFloat()
            val y = chartValues[keysSorted[i]] ?: 0f
            values.add(BarEntry(x, y))
        }

        val set1 = BarDataSet(values, String())

        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(set1)
        return BarData(dataSets)
    }

    private fun prepareChartData(data: BarData) {
        data.setValueTextSize(
            if(keysSorted.size < 10)
                Utility.convertValue(12f, activity).toFloat()
            else 0f
        )

        data.setValueTextColor(Color.WHITE)
        barChart!!.data = data
        barChart!!.invalidate()
    }
}