package com.mini.infotainment.activities.home

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.support.Page
import com.mini.infotainment.support.SpotifyReceiver
import com.mini.infotainment.utility.Utility

class HomeFirstPage(override val ctx: HomeActivity) : Page {
    internal lateinit var spotifyAuthorTw: TextView
    internal lateinit var spotifyTitleTW: TextView
    internal lateinit var addressTW: TextView
    internal lateinit var speedometerTW: TextView
    internal lateinit var timeTW: TextView
    internal lateinit var spotifyWidget: View

    override fun build() {
        ctx.viewPages.clear()
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)

        val layout = ctx.layoutInflater.inflate(R.layout.activity_home_1, ctx.viewPager, false) as ViewGroup

        spotifyWidget = layout.findViewById(R.id.home_1_spotify)
        timeTW = layout.findViewById(R.id.home_1_datetime)
        speedometerTW = layout.findViewById(R.id.home_1_speed)
        addressTW = layout.findViewById(R.id.home_1_address)
        spotifyTitleTW = layout.findViewById(R.id.spotify_title)
        spotifyAuthorTw = layout.findViewById(R.id.spotify_author)
        ctx.viewPages.add(layout)

        setListeners()
        setupTimer()
    }

    override fun setListeners() {
        spotifyWidget.setOnClickListener {
            nextSpotifyTrack()
        }

        spotifyWidget.setOnLongClickListener {
            previousSpotifyTrack()
            true
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

    private fun nextSpotifyTrack(){
        changeSpotifyTrack(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    private fun previousSpotifyTrack(){
        changeSpotifyTrack(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    private fun changeSpotifyTrack(keyCode: Int){
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        intent.setPackage(SpotifyReceiver.SPOTIFY_PACKAGE)
        synchronized(this) {
            intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            ctx.sendOrderedBroadcast(intent, null)
            intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyCode))
            ctx.sendOrderedBroadcast(intent, null)
        }
    }
}