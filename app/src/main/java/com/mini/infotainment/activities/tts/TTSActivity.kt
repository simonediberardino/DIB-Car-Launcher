package com.mini.infotainment.activities.tts

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.ActivityExtended

class TTSActivity : ActivityExtended() {
    private lateinit var ttsAddButton: View
    private lateinit var ttsTopSheet: TTSTopSheet
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts)

        val linearLayout = findViewById<ViewGroup>(R.id.tts_linearlayout) as LinearLayout
        val sentences = ApplicationData.savedMsgs

        if (sentences != null) {
            for (i in sentences.indices) {
                val layout = layoutInflater.inflate(R.layout.menu_single_tts_text, linearLayout, false) as ViewGroup
                val senTitleTW = layout.findViewById<TextView>(R.id.single_tts_title)
                val senDescTW = layout.findViewById<TextView>(R.id.single_tts_description)
                senTitleTW.text = getString(R.string.tts_n_sentence).replace("{numero}", (i+1).toString())
                senDescTW.text = sentences[i].text
                linearLayout.addView(layout)
            }
        }else {

        }

        ttsTopSheet = TTSTopSheet(this)
        ttsTopSheet.build()

        setListeners()
    }

    private fun setListeners(){
        ttsAddButton = findViewById(R.id.tts_button)
        ttsAddButton.setOnClickListener {
            ttsTopSheet.setTopMenuVisibility(true)
        }
    }

}