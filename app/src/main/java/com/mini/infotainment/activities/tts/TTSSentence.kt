package com.mini.infotainment.activities.tts

open class TTSSentence(val text: String){
    override fun equals(other: Any?): Boolean {
        return if(other == null) false else this.text == (other as TTSSentence).text
    }
}