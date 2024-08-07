package com.mini.infotainment.activities.home

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.LinearLayout
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.settings.SettingsMenuActivity
import com.mini.infotainment.utility.Utility

class SideMenu(override val ctx: HomeActivity) : Page() {
    override fun build() {
        class SideMenuButton(val image: Drawable?, val listener: Runnable)

        val buttons = arrayOf(
            SideMenuButton(ctx.getDrawable(R.drawable.spotify_logo)) { ctx.runSpotify() },
            SideMenuButton(ctx.getDrawable(R.drawable.youtube_logo)) { ctx.runYoutube() },
            SideMenuButton(ctx.getDrawable(R.drawable.logo_settings)){
                Utility.navigateTo(ctx, SettingsMenuActivity::class.java)
            }
        )

        parent = ctx.findViewById<LinearLayout>(R.id.home_sidemenu)

        for(button : SideMenuButton in buttons){
            val inflatedView = ctx.layoutInflater.inflate(R.layout.menu_side_items, parent, false)
            val sideMenuImage = inflatedView.findViewById<ImageView>(R.id.side_menu_image)

            sideMenuImage.setImageDrawable(button.image)
            inflatedView.setOnClickListener {
                button.listener.run()
            }

            parent!!.addView(inflatedView)
        }

        super.pageLoaded()
    }
}