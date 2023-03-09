package com.mini.infotainment.activities.settings

import android.os.Bundle
import android.view.View
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.edit.EditProfileActivity
import com.mini.infotainment.activities.login.register.RegisterActivity
import com.mini.infotainment.data.Data
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

class SettingsMenuActivity : SActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_menu)
        super.pageLoaded()
        super.initializeLayout()
    }

    fun onClickOK(view: View){
        finish()
    }

    fun onClickCustomize(view: View){
        Utility.navigateTo(this, SettingsActivityCustomize::class.java)
    }

    fun onClickProfile(view: View){
        val clazz: Class<out SActivity> = if(Data.isLogged())
            EditProfileActivity::class.java else RegisterActivity::class.java

        Utility.navigateTo(this, clazz)
    }

    fun onClickSettings(view: View){
        Utility.navigateTo(this, SettingsActivityConfiguration::class.java)
    }

}