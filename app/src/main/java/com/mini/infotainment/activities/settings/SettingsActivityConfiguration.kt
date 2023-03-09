package com.mini.infotainment.activities.settings

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Spinner
import com.mini.infotainment.R
import com.mini.infotainment.activities.settings.model.AbstractSettingsActivity
import com.mini.infotainment.data.Data

class SettingsActivityConfiguration: AbstractSettingsActivity() {
    private lateinit var smartphoneNotiSwitch: CheckBox
    private lateinit var uMeasureSpinner: Spinner
    private lateinit var settingsDefaultWPCB: CheckBox
    private lateinit var confirmButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLayout()
        super.pageLoaded()
    }

    override fun initializeLayout(){
        this.setContentView(R.layout.activity_settings_settings)
        super.initializeLayout()

        confirmButton = findViewById(R.id.settings_confirm_button)
        smartphoneNotiSwitch = findViewById(R.id.settings_noti_on)
        smartphoneNotiSwitch.isChecked = Data.isNotificationStatusEnabled()

        settingsDefaultWPCB = findViewById(R.id.settings_default_wp)

        confirmButton.setOnClickListener {
            finish()
        }

        settingsDefaultWPCB.isChecked = Data.useDefaultWP()

        uMeasureSpinner = findViewById<Spinner?>(R.id.settings_um).also {
            it.setSelection(Data.getUMeasure())
        }
    }

    override fun handleSettings(){
        Data.setUMeasure(uMeasureSpinner.selectedItemPosition)

        if(Data.useDefaultWP() != settingsDefaultWPCB.isChecked){
            Data.useDefaultWP(settingsDefaultWPCB.isChecked)
        }

        Data.setNotificationStatus(smartphoneNotiSwitch.isChecked)
    }
}