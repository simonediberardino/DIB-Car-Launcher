package com.mini.infotainment.activities.home

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.LinearLayout
import com.mini.infotainment.R
import com.mini.infotainment.support.Page

class SideMenu(override val ctx: HomeActivity) : Page() {
    override fun build() {
        class SideMenuButton(val image: Drawable?, val listener: Runnable)

        val buttons = arrayOf(
            SideMenuButton(ctx.getDrawable(R.drawable.spotify_logo_2)) { ctx.runSpotify() },
            SideMenuButton(ctx.getDrawable(R.drawable.youtube_logo)) { ctx.runYoutube() },
            SideMenuButton(ctx.getDrawable(R.drawable.car_settings)) { ctx.runSettings() },
            SideMenuButton(ctx.getDrawable(R.drawable.more_logo)){
                val dialog = HomeLogin(ctx)
                dialog.setOnDismissListener { ctx.homePage1.updateData() }
                dialog.show()
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