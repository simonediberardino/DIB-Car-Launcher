package com.mini.infotainment.activities.stats.month

import android.util.DisplayMetrics
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Animations
import com.mini.infotainment.activities.stats.ActivityStats
import com.mini.infotainment.activities.stats.tab.StatsTab

class StatsMonth(override val ctx: ActivityStats) : StatsTab() {
    override val scrollView: HorizontalScrollView
        get() = ctx.findViewById(R.id.stats_month_sw)
    override val linearLayout: LinearLayout
        get() = ctx.findViewById(R.id.stats_ll_month)
    override val button: TextView
        get() = ctx.findViewById(R.id.stats_month_btn)

    init{
        button.setOnClickListener { this.show() }
    }

    override fun doShow(){
        val displayMetrics = DisplayMetrics()
        ctx.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toDouble()

        linearLayout.visibility = View.VISIBLE

        Animations
            .moveAnimation(
                scrollView,
                (screenWidth+scrollView.width).toFloat(),
                0f,
                0f,
                0f,
                600
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
                (screenWidth+scrollView.width).toFloat(),
                0f,
                0f,
                600
            ){
                linearLayout.visibility = View.GONE
            }
    }

}