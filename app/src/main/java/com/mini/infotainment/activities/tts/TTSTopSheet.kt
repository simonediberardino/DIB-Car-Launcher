package com.mini.infotainment.activities.tts

import android.util.DisplayMetrics
import android.view.View
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.mini.infotainment.R
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.support.Page

class TTSTopSheet(override val ctx: ActivityExtended) : Page{
    private lateinit var topSheetBehavior: TopSheetBehavior<View>

    override fun build() {
        ctx.windowManager?.defaultDisplay?.getMetrics(DisplayMetrics())

        val topSheet = ctx.findViewById<View>(R.id.top_sheet_persistent)

        topSheetBehavior = TopSheetBehavior.from(topSheet)
        topSheetBehavior.isHideable = true

        setTopMenuVisibility(false)

        topSheetBehavior.setTopSheetCallback(object : TopSheetBehavior.TopSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == TopSheetBehavior.STATE_COLLAPSED)
                    setTopMenuVisibility(false)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun isTopMenuShown(): Boolean {
        return topSheetBehavior.state == TopSheetBehavior.STATE_EXPANDED
    }

    fun setTopMenuVisibility(flag: Boolean){
        topSheetBehavior.state = if(flag){
            TopSheetBehavior.STATE_EXPANDED
        }else{
            TopSheetBehavior.STATE_HIDDEN
        }
    }

}