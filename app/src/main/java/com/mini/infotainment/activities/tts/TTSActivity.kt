package com.mini.infotainment.activities.tts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.utility.Utility

class TTSActivity : ActivityExtended() {
    private lateinit var ttsAddButton: View
    private lateinit var ttsTopSheet: TTSTopSheet
    private var welcomeMsg: TTSWelcomeSentence? = null
    private lateinit var TTS: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts)

        TTS = TextToSpeech(this) {}
        refreshList()

        ttsTopSheet = TTSTopSheet(this)
        ttsTopSheet.build()

        setListeners()
    }

    fun refreshList(){
        val linearLayout = findViewById<ViewGroup>(R.id.tts_linearlayout) as LinearLayout
        linearLayout.removeAllViews()

        val sentences = ApplicationData.getTTSSentence()

        for (i in sentences.indices) {
            val layout = layoutInflater.inflate(R.layout.menu_single_tts_text, linearLayout, false) as ViewGroup
            val senTitleTW = layout.findViewById<TextView>(R.id.single_tts_title)
            val senDescTW = layout.findViewById<TextView>(R.id.single_tts_description)
            val senDeleteBtn = layout.findViewById<View>(R.id.single_tts_delete)
            val senPlayBtn = layout.findViewById<View>(R.id.single_tts_play)

            senTitleTW.text = getString(R.string.tts_n_sentence).replace("{numero}", (i+1).toString())
            senDescTW.text = sentences[i].text
            senDeleteBtn.setOnClickListener {
                Utility.confirmDialog(this){
                    if(sentences[i] == welcomeMsg)
                        updateWelcomeMsg(null, null)

                    ApplicationData.deleteTTSSentence(sentences[i])
                    refreshList()
                }
            }

            senPlayBtn.setOnClickListener {
                TTS.speak(sentences[i].text, TextToSpeech.QUEUE_FLUSH, null)
            }

            layout.setOnClickListener {
                if(sentences[i] != welcomeMsg){
                    ApplicationData.setWelcomeSentence(sentences[i])
                    Utility.showToast(this, getString(R.string.welcome_msg_confirm))
                }else{
                    updateWelcomeMsg(null, null)
                }
                refreshList()
            }

            if(sentences[i] == ApplicationData.getWelcomeSentence())
                updateWelcomeMsg(layout, sentences[i])

            linearLayout.addView(layout)
        }
    }

    private fun setListeners(){
        ttsAddButton = findViewById(R.id.tts_button)
        ttsAddButton.setOnClickListener {
            ttsTopSheet.visibility = true
        }
    }

    private fun updateWelcomeMsg(layout: View?, ttsSentence: TTSSentence?){
        welcomeMsg?.view?.setBackgroundColor(0x111111)
        welcomeMsg = null

        ApplicationData.setWelcomeSentence(null)

        if(ttsSentence != null){
            welcomeMsg = TTSWelcomeSentence(ttsSentence.text)
            welcomeMsg!!.view = layout
            welcomeMsg!!.view!!.setBackgroundColor(0x3000ddff)
            ApplicationData.setWelcomeSentence(ttsSentence)
        }
    }
}