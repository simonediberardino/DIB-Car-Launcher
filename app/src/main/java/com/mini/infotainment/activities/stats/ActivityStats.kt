package com.mini.infotainment.activities.stats

import android.os.Bundle
import com.anychart.anychart.*
import com.mini.infotainment.R
import com.mini.infotainment.support.ActivityExtended

class ActivityStats : ActivityExtended() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val anyChartView = findViewById<AnyChartView>(R.id.any_chart_view)

        val cartesian = AnyChart.column()

        val data: MutableList<DataEntry> = ArrayList()
        data.add(ValueDataEntry("Rouge", 80540))
        data.add(ValueDataEntry("Foundation", 94190))
        data.add(ValueDataEntry("Mascara", 102610))
        data.add(ValueDataEntry("Lip gloss", 110430))
        data.add(ValueDataEntry("Lipstick", 128000))
        data.add(ValueDataEntry("Nail polish", 143760))
        data.add(ValueDataEntry("Eyebrow pencil", 170670))
        data.add(ValueDataEntry("Eyeliner", 213210))
        data.add(ValueDataEntry("Eyeshadows", 249980))

        val column = cartesian.column(data)

        column.tooltip
            .setTitleFormat("{%X}")
            .setPosition(Position.CENTER_BOTTOM)
            .setAnchor(EnumsAnchor.CENTER_BOTTOM)
            .setOffsetX(0.0)
            .setOffsetY(5.0)
            .setFormat("\${%Value}{groupsSeparator: }")

        cartesian.setAnimation(true)
        cartesian.setTitle("Top 10 Cosmetic Products by Revenue")

        cartesian.yScale.setMinimum(0.0)

        cartesian.getYAxis(0.0).labels.setFormat("\${%Value}{groupsSeparator: }")

        cartesian.tooltip.setPositionMode(TooltipPositionMode.POINT)
        cartesian.interactivity.setHoverMode(HoverMode.BY_X)

        cartesian.getYAxis(0.0).setTitle("Product")
        cartesian.getYAxis(0.0).setTitle("Revenue")

        anyChartView.setChart(cartesian)
    }
}