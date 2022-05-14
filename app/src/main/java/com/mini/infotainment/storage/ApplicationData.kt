package com.mini.infotainment.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mini.infotainment.activities.tts.TTSSentence
import com.mini.infotainment.support.ActivityExtended

object ApplicationData {
    private const val DATA_ID = "data"
    private const val LOGIN_TIME_ID = "LOGIN_TIME_ID"
    private val LOGIN_TIME_DEFAULT: Long = 0
    private const val WELCOME_MSG_ID = "WELCOME_MSG_ID"
    private val WELCOME_MSG_DEFAULT = null
    private const val MESSAGES_ID = "MSG_ID"
    private val MESSAGES_DEFAULT = null
    private const val NOTIFICHE_ID = "NOTIFICHE_ID"
    private val NOTIFICHE_DEFAULT = "true"
    private const val TARGA_ID = "TARGA_ID"
    private val TARGA_DEFAULT = null

    private val applicationData: SharedPreferences
        get() {
            return ActivityExtended.lastActivity.getSharedPreferences(
                DATA_ID,
                Context.MODE_PRIVATE
            )!!
        }

    fun areNotificationsEnabled(): Boolean {
        val savedJson: String? = applicationData.getString(NOTIFICHE_ID, NOTIFICHE_DEFAULT)
        return Gson().fromJson(savedJson, Boolean::class.java)
    }

    fun areNotificationsEnabled(boolean: Boolean){
        val json = Gson().toJson(boolean)
        val dataEditor = applicationData.edit()
        dataEditor.putString(NOTIFICHE_ID, json)
        dataEditor.apply()
    }

    fun getWelcomeSentence(): TTSSentence? {
        val savedJson: String? = applicationData.getString(WELCOME_MSG_ID, WELCOME_MSG_DEFAULT)
        return Gson().fromJson(savedJson, TTSSentence::class.java)
    }

    fun setWelcomeSentence(ttsSentence: TTSSentence?){
        val json = Gson().toJson(ttsSentence)
        val dataEditor = applicationData.edit()
        dataEditor.putString(WELCOME_MSG_ID, json)
        dataEditor.apply()
    }

    fun getTarga(): String? {
        val savedJson: String? = applicationData.getString(TARGA_ID, TARGA_DEFAULT)
        return Gson().fromJson(savedJson, String::class.java)
    }

    fun setTarga(targa: String?){
        val json = Gson().toJson(targa?.uppercase())
        val dataEditor = applicationData.edit()
        dataEditor.putString(TARGA_ID, json)
        dataEditor.apply()
    }

    fun getTTSSentence(): MutableList<TTSSentence> {
        val savedJson: String = applicationData.getString(MESSAGES_ID, MESSAGES_DEFAULT)
            ?: return mutableListOf()
        return Gson().fromJson(savedJson, Array<TTSSentence>::class.java).toMutableList()
    }

    fun saveTTSSentence(ttsSentence: TTSSentence){
        val list = getTTSSentence()
        if(list.map { it.text }.any { it == ttsSentence.text })
            return

        list.add(ttsSentence)
        saveTTSSentences(list)
    }

    fun saveTTSSentences(list: MutableList<TTSSentence>){
        val json = Gson().toJson(list)
        val dataEditor = applicationData.edit()
        dataEditor.putString(MESSAGES_ID, json)
        dataEditor.apply()
    }

    fun deleteTTSSentence(ttsSentence: TTSSentence){
        val list = getTTSSentence()
        list.removeIf { it.text == ttsSentence.text }
        saveTTSSentences(list)
    }

    var lastLogin: Long
        get() {
            val savedJson: String? =
                applicationData.getString(LOGIN_TIME_ID, LOGIN_TIME_DEFAULT.toString())
            return Gson().fromJson(savedJson, String::class.java).toLong()
        }
        set(value) {
            val json = Gson().toJson(value)
            val dataEditor = applicationData.edit()
            dataEditor.putString(LOGIN_TIME_ID, json)
            dataEditor.apply()
        }
}
