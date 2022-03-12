package com.mini.infotainment.activities.tts

import android.util.DisplayMetrics
import android.view.View
import android.widget.EditText
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.Page
import com.mini.infotainment.utility.Utility

class TTSTopSheet(override val ctx: TTSActivity) : Page{
    private lateinit var topSheetBehavior: TopSheetBehavior<View>
    private lateinit var ttsConfirm: View
    private lateinit var ttsEditText: EditText
    private lateinit var ttsBack: View

    var visibility: Boolean
        get() {
            return topSheetBehavior.state == TopSheetBehavior.STATE_EXPANDED
        }
        set(value) {
            topSheetBehavior.state = if(value){
                TopSheetBehavior.STATE_EXPANDED
            }else{
                TopSheetBehavior.STATE_HIDDEN
            }
        }

    override fun build() {
        ctx.windowManager?.defaultDisplay?.getMetrics(DisplayMetrics())

        val topSheet = ctx.findViewById<View>(R.id.top_sheet_persistent)

        topSheetBehavior = TopSheetBehavior.from(topSheet)
        topSheetBehavior.isHideable = true

        visibility = false

        topSheetBehavior.setTopSheetCallback(object : TopSheetBehavior.TopSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == TopSheetBehavior.STATE_COLLAPSED)
                    visibility = false
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        ttsConfirm = ctx.findViewById(R.id.tts_top_confirm)
        ttsEditText = ctx.findViewById(R.id.tts_top_input)
        ttsBack = ctx.findViewById(R.id.tts_top_back)

        ttsConfirm.setOnClickListener {
            val input = Utility.capitalizeFirstLetter(ttsEditText.text.toString())

            if(input.trim().isEmpty())
                return@setOnClickListener

            val ttsSentence = TTSSentence(input)

            ApplicationData.saveTTSSentence(ttsSentence)
            ttsEditText.setText(String())
            ctx.refreshList()
            visibility = false
        }

        ttsBack.setOnClickListener {
            visibility = false
        }
    }

}