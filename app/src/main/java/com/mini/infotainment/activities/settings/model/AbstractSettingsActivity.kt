package com.mini.infotainment.activities.settings.model

import com.mini.infotainment.support.SActivity

abstract class AbstractSettingsActivity: SActivity() {
    abstract fun handleSettings()

    override fun finish() {
        handleSettings()
        super.finish()
    }

}