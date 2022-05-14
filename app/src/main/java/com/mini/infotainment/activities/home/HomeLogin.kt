package com.mini.infotainment.activities.home

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.EditText
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.utility.Utility

class HomeLogin(val homeActivity: HomeActivity) : Dialog(homeActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    private var loginPasswordEt: EditText
    private var confirmButton: View

    init {
        this.setContentView(R.layout.activity_login)
        this.setCancelable(false)
        loginPasswordEt = findViewById(R.id.login_password_et)
        confirmButton = findViewById(R.id.login_login_button)
        confirmButton.setOnClickListener { this.handleLogin() }
    }

    private fun handleLogin(){
        val enteredTarga = loginPasswordEt.text.toString().trim()

        if(!isValidTarga(enteredTarga)){
            Utility.showToast(homeActivity, homeActivity.getString(R.string.errore_targa))
            loginPasswordEt.setText(String())
            return
        }

        ApplicationData.setTarga(enteredTarga)
        homeActivity.initializeActivity()
        this.dismiss()
    }

    private fun isValidTarga(targa: String): Boolean {
        if(targa.length != 7) return false

        return(
                targa[0].isLetter()
                && targa[1].isLetter()
                && targa[2].isDigit()
                && targa[3].isDigit()
                && targa[4].isDigit()
                && targa[5].isLetter()
                && targa[6].isLetter()
        )
    }
}