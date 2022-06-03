package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.support.Page
import com.mini.infotainment.support.SpotifyReceiver
import com.mini.infotainment.utility.Utility

class HomeFirstPage(override val ctx: HomeActivity) : Page {
    internal lateinit var homeButton: View
    internal lateinit var spotifyAuthorTw: TextView
    internal lateinit var spotifyTitleTW: TextView
    internal lateinit var addressTW: TextView
    internal lateinit var speedometerTW: TextView
    internal lateinit var timeTW: TextView
    internal lateinit var spotifyWidget: View

    @SuppressLint("ClickableViewAccessibility")
    override fun build() {
        val layout = ctx.layoutInflater.inflate(R.layout.activity_home_1, ctx.viewPager, false) as ViewGroup

        spotifyWidget = layout.findViewById(R.id.home_1_spotify)
        timeTW = layout.findViewById(R.id.home_1_datetime)
        speedometerTW = layout.findViewById(R.id.home_1_speed)
        addressTW = layout.findViewById(R.id.home_1_address)
        spotifyTitleTW = layout.findViewById(R.id.spotify_title)
        spotifyAuthorTw = layout.findViewById(R.id.spotify_author)
        homeButton = layout.findViewById(R.id.home_1_swipe)

        ctx.viewPages.add(layout)

        setListeners()
        setupTimer()
    }

    override fun setListeners() {
        spotifyWidget.setOnClickListener {
            SpotifyReceiver.nextSpotifyTrack(ctx)
        }

        spotifyWidget.setOnLongClickListener {
            SpotifyReceiver.previousSpotifyTrack(ctx)
            true
        }

        homeButton.setOnClickListener {
            ctx.appsMenu.show(true, HomeActivity.SLIDE_ANIMATION_DURATION)
        }
    }

    private fun setupTimer(){
        Thread{
            while(true){
                try{
                    updateTime()
                    Thread.sleep(1000)
                }catch (exception: Exception){}
            }
        }.start()
    }

    private fun updateTime() {
        ctx.runOnUiThread { timeTW.text = Utility.getTime() }
    }
}