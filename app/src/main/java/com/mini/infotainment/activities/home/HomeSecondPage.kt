package com.mini.infotainment.activities.home

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page

class HomeSecondPage(override val ctx: HomeActivity) : Page() {
    override fun build() {
        parent = ctx.layoutInflater.inflate(R.layout.activity_home_2, ctx.viewPager, false) as ViewGroup
        val gridView = parent!!.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.home_2_grid)

        class GridButton(name: String, drawableId: Int, val callback: Runnable){
            init{
                val singleItem = ctx.layoutInflater.inflate(R.layout.home_2_items, parent, false) as ViewGroup
                val itemNameTW = singleItem.findViewById<TextView>(R.id.home_2_item_name)
                val itemNameIW = singleItem.findViewById<ImageView>(R.id.home_2_item_image)

                itemNameTW.text = name
                itemNameIW.setImageResource(drawableId)
                singleItem.setOnClickListener {
                    callback.run()
                }

                gridView.addView(singleItem)
            }
        }

        GridButton(ctx.getString(R.string.menu_navigatore), R.drawable.menu_navigation) { ctx.runGoogleMaps() }
        GridButton(ctx.getString(R.string.menu_voice), R.drawable.menu_voice) { ctx.runGoogleAssistant() }
        GridButton(ctx.getString(R.string.menu_spotify), R.drawable.menu_spotify) { ctx.runSpotify() }
        GridButton(ctx.getString(R.string.menu_youtube), R.drawable.menu_youtube) { ctx.runYoutube() }
        GridButton(ctx.getString(R.string.menu_storage), R.drawable.menu_storage) { ctx.runFileManager() }
        GridButton(ctx.getString(R.string.menu_settings), R.drawable.menu_settings) { ctx.runSettings() }

        ctx.viewPages.add(parent!!)
        super.pageLoaded()
    }
}