package com.mini.infotainment.activities.stats.tab

import android.graphics.Color
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.UI.Page

abstract class StatsTab : Page(){
    abstract val scrollView: HorizontalScrollView
    abstract val linearLayout: LinearLayout
    abstract val button: TextView

    protected abstract fun doShow()
    protected abstract fun doHide()
    var isVisible = false


    fun show(){
        button.setTextColor(Color.RED)
        this.doShow()
    }

    fun hide(){
        button.setTextColor(Color.WHITE)
        this.doHide()
    }
}