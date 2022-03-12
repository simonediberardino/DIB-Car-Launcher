package com.mini.infotainment.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mini.infotainment.entities.TTSSentence
import com.mini.infotainment.support.ActivityExtended

object ApplicationData {
    private const val DATA_ID = "data"
    private const val LOGIN_TIME_ID = "LOGIN_TIME_ID"
    private val LOGIN_TIME_DEFAULT: Long = 0
    private const val WELCOME_MSG_ID = "WELCOME_MSG_ID"
    private val WELCOME_MSG_DEFAULT = null
    private const val MESSAGES_ID = "MSG_ID"
    private val MESSAGES_DEFAULT = null

    private val applicationData: SharedPreferences
        get() {
            return ActivityExtended.lastActivity.getSharedPreferences(
                DATA_ID,
                Context.MODE_PRIVATE
            )!!
        }

    var welcomeMsg: TTSSentence?
        get() {
            val savedJson: String? = applicationData.getString(WELCOME_MSG_ID, WELCOME_MSG_DEFAULT)
            return Gson().fromJson(savedJson, TTSSentence::class.java)
        }
        set(value) {
            val json = Gson().toJson(value)
            val dataEditor = applicationData.edit()
            dataEditor.putString(WELCOME_MSG_ID, json)
            dataEditor.apply()
        }

    fun getTTSSentence(): MutableList<TTSSentence> {
        val savedJson: String = applicationData.getString(MESSAGES_ID, MESSAGES_DEFAULT)
            ?: return mutableListOf()
        return Gson().fromJson(savedJson, Array<TTSSentence>::class.java).toMutableList()
    }

    fun saveTTSSentence(ttsSentence: TTSSentence){
        val list = getTTSSentence()
        list.add(ttsSentence)
        val json = Gson().toJson(list)
        val dataEditor = applicationData.edit()
        dataEditor.putString(MESSAGES_ID, json)
        dataEditor.apply()
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
