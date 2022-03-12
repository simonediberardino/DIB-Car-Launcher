package com.mini.infotainment.activities.tts

import android.util.DisplayMetrics
import android.view.View
import android.widget.EditText
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.mini.infotainment.R
import com.mini.infotainment.entities.TTSSentence
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.support.Page

class TTSTopSheet(override val ctx: TTSActivity) : Page{
    private lateinit var topSheetBehavior: TopSheetBehavior<View>
    private lateinit var ttsConfirm: View
    private lateinit var ttsEditText: EditText

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

        ttsConfirm = ctx.findViewById(R.id.tts_top_confirm)
        ttsEditText = ctx.findViewById(R.id.tts_top_input)

        ttsConfirm.setOnClickListener {
            val input = ttsEditText.text.toString()
            if(input.trim().isEmpty())
                return@setOnClickListener
            val ttsSentence = TTSSentence(input)
            ApplicationData.saveTTSSentence(ttsSentence)
            ctx.refreshList()
        }
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