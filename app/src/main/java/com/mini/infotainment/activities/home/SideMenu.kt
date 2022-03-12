package com.mini.infotainment.activities.home

import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.activities.tts.TTSActivity
import com.mini.infotainment.support.Page
import com.mini.infotainment.utility.Utility

class SideMenu(override val ctx: HomeActivity) : Page {
    override fun build() {
        class SideMenuButton(val title: String, val listener: Runnable)

        val buttons = arrayOf(
            SideMenuButton(ctx.getString(R.string.menu_navigatore)) { ctx.runGoogleMaps() },
            SideMenuButton(ctx.getString(R.string.menu_voice)) { Utility.navigateTo(ctx, TTSActivity::class.java) },
            SideMenuButton(ctx.getString(R.string.menu_spotify)) { ctx.runSpotify() },
            SideMenuButton(ctx.getString(R.string.settings)) { ctx.runSettings() }
        )

        val parent = ctx.findViewById<LinearLayout>(R.id.home_sidemenu)

        for(button : SideMenuButton in buttons){
            val inflatedView = ctx.layoutInflater.inflate(R.layout.menu_side_items, parent, false)
            val titleTW = inflatedView.findViewById<TextView>(R.id.menu_side_title)

            titleTW.text = button.title
            inflatedView.setOnClickListener {
                button.listener.run()
            }

            parent.addView(inflatedView)
        }
    }
}