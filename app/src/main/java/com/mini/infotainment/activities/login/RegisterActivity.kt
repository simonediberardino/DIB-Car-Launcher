package com.mini.infotainment.activities.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.activities.settings.SettingsActivity
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility


class RegisterActivity : ProfileActivity() {
    private lateinit var plateNumTW: TextView
    private lateinit var passwordTW: TextView
    private lateinit var confirmPasswordTw: TextView
    private lateinit var confirmBtn: View
    private lateinit var loginBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    private fun initializeLayout(){
        setContentView(R.layout.activity_register)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(Utility.getWallpaper(
            this
        ))

        plateNumTW = findViewById(R.id.register_targa)
        passwordTW = findViewById(R.id.register_psw)
        confirmPasswordTw = findViewById(R.id.register_psw2)
        confirmBtn = findViewById(R.id.register_confirm_button)
        loginBtn = findViewById(R.id.register_log_btn)

        confirmBtn.setOnClickListener { handleData() }
        loginBtn.setOnClickListener { Utility.navigateTo(this, LoginActivity::class.java) }
    }

    private fun handleData(){
        if(!Utility.isInternetAvailable()){
            showError(ErrorCodes.NO_INTERNET)
            return
        }

        checkIfExists {
            validateDetails{
                doRegister()
            }
        }
    }

    private fun checkIfExists(runnable: Runnable){
        val plateNum = plateNumTW.text.toString().uppercase().trim()
        FirebaseClass.doesCarExist(plateNum,
            object: RunnablePar{
                override fun run(p: Any?) {
                    val exists = p as Boolean
                    if(exists)
                        showError(ErrorCodes.EXISTS)
                    else runnable.run()
                }
            })
    }

    private fun validateDetails(runnable: Runnable){
        val plateNum = plateNumTW.text.toString().uppercase().trim()
        val psw1 = passwordTW.text.toString().trim()
        val psw2 = confirmPasswordTw.text.toString().trim()

        if(psw1 != psw2) {
            showError(ErrorCodes.PASSWORD_DONT_MATCH)
            return
        }

        val minLength = 4
        if(psw1.length < minLength || plateNum.length < minLength){
            showError(ErrorCodes.INVALID_DETAILS)
            return
        }

        runnable.run()
    }

    private fun doRegister(){
        val plateNum = plateNumTW.text.toString().uppercase().trim()
        val password = Utility.getMD5(passwordTW.text.toString().trim())

        val myCar = MyCar(plateNum, password)

        ApplicationData.setTarga(plateNum)
        ApplicationData.setCarPassword(password)
        FirebaseClass.getCarObjectReference(plateNum).setValue(myCar)

        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("isFirstLaunch", true)
        startActivity(intent)

        finish()
    }

    override fun onBackPressed() {

    }
}