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

class LoginActivity : ProfileActivity() {
    private lateinit var plateNumTW: TextView
    private lateinit var passwordTW: TextView
    private lateinit var confirmBtn: View
    private lateinit var registerBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    private fun initializeLayout(){
        setContentView(R.layout.activity_login)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(Utility.getWallpaper(
            this
        ))

        plateNumTW = findViewById(R.id.login_targa)
        passwordTW = findViewById(R.id.login_psw)
        confirmBtn = findViewById(R.id.login_confirm_button)
        registerBtn = findViewById(R.id.login_reg_btn)

        confirmBtn.setOnClickListener { proceed() }
        registerBtn.setOnClickListener { onBackPressed() }
    }

    private fun proceed(){
        val plateNum = plateNumTW.text.toString().uppercase().trim()
        val password = Utility.getMD5(passwordTW.text.toString().trim())

        FirebaseClass.areCredentialsCorrect(plateNum, password, object : RunnablePar{
            override fun run(p: Any?) {
                val canLogin = p as Boolean
                if(!canLogin){
                    showError(ErrorCodes.INVALID_DETAILS)
                    return
                }

                doLogin(MyCar(plateNum, password))

                val intent = Intent(this@LoginActivity, SettingsActivity::class.java)
                intent.putExtra("isFirstLaunch", true)
                startActivity(intent)

                finish()
            }
        })
    }

    override fun onBackPressed() {
        Utility.navigateTo(this, RegisterActivity::class.java)
    }

    companion object{
        fun doLogin(myCar: MyCar){
            ApplicationData.setCarPassword(myCar.password)
            ApplicationData.setTarga(myCar.plateNum)
        }

        fun doLogout(){
            ApplicationData.setCarPassword(null)
            ApplicationData.setTarga(null)
        }
    }

}