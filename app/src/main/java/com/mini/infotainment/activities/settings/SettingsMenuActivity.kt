package com.mini.infotainment.activities.settings

import android.os.Bundle
import android.view.View
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.activities.login.register.RegisterActivity
import com.mini.infotainment.data.Data
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

class SettingsMenuActivity : SActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_menu)
        super.pageLoaded()
    }

    fun onClickCustomize(view: View){
        Utility.navigateTo(this, this.javaClass)
    }

    fun onClickProfile(view: View){
        val clazz: Class<out SActivity> = if(Data.isLogged())
            ProfileActivity::class.java else RegisterActivity::class.java

        Utility.navigateTo(this, clazz)
    }

    fun onClickSettings(view: View){
        Utility.navigateTo(this, SettingsActivityConfiguration::class.java)
    }



}