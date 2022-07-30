package com.mini.infotainment.activities.stats

import android.app.Application
import android.os.Bundle
import androidx.leanback.widget.Util
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.utility.Utility

class ActivityStats : ActivityExtended() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val x: HashMap<String, String> = HashMap()


        // Testing application data for dates
        x["1"] = "a"
        x["2"] = "b"

        ApplicationData.setStats(x)

        println(
            ApplicationData.getStats().toString()
        )

        val y = ApplicationData.getStats()
        y["3"] = "c"
        ApplicationData.setStats(y)
        println(
            ApplicationData.getStats().toString()
        )

        setContentView(R.layout.activity_stats)
        super.pageLoaded()
    }
}