package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.stats.store.StatsData
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.receivers.SpotifyIntegration
import com.mini.infotainment.utility.Utility


class HomeFirstPage(override val ctx: HomeActivity) : Page() {
    internal lateinit var homeButton: View
    internal lateinit var spotifyAuthorTw: TextView
    internal lateinit var spotifyTitleTW: TextView
    internal lateinit var addressTW: TextView
    internal lateinit var speedometerTW: TextView
    internal lateinit var timeTW: TextView
    internal lateinit var spotifyWidget: View
    internal lateinit var carIcon: ImageView
    internal lateinit var travSpeedTW: TextView
    internal lateinit var carTargaTW: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun build() {
        parent = ctx.layoutInflater.inflate(R.layout.activity_home_1, ctx.viewPager, false) as ViewGroup

        spotifyWidget = parent!!.findViewById(R.id.home_1_spotify)
        timeTW = parent!!.findViewById(R.id.home_1_datetime)
        speedometerTW = parent!!.findViewById(R.id.home_1_speed)
        addressTW = parent!!.findViewById(R.id.home_1_address)
        spotifyTitleTW = parent!!.findViewById(R.id.spotify_title)
        spotifyAuthorTw = parent!!.findViewById(R.id.spotify_author)
        homeButton = parent!!.findViewById(R.id.home_1_swipe)
        carIcon = parent!!.findViewById(R.id.home_1_car_icon)
        travSpeedTW = parent!!.findViewById(R.id.home_1_trav_speed)
        carTargaTW = parent!!.findViewById(R.id.home_1_targa)

        ctx.viewPages.add(parent!!)

        updateData()
        setListeners()
        setupTimer()

        super.pageLoaded()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setListeners() {
        spotifyWidget.setOnTouchListener { v, e ->
            when(e.action){
                MotionEvent.ACTION_UP -> {
                    val isRight = v.width/2 < e.x
                    if(isRight)
                        SpotifyIntegration.nextSpotifyTrack(ctx)
                    else
                        SpotifyIntegration.previousSpotifyTrack(ctx)

                    if(spotifyTitleTW.text == ctx.getString(R.string.spotify_no_data)){
                        CustomToast(ctx.getString(R.string.spotify_no_data_why), ctx).show()
                    }
                }
            }
            true
        }

        homeButton.setOnClickListener {
            ctx.appsMenu?.show(true, HomeActivity.SLIDE_ANIMATION_DURATION)
        }
    }

    fun updateData(){
        updateLogoImageView()
        updateCarName()
        updateTravSpeed()
    }

    fun updateTravSpeed(){
        travSpeedTW.text = "${StatsData.getTraveledDistance(StatsData.Mode.DAY)}\nkm/24h"
    }

    fun updateCarName(){
        val brandName = (ApplicationData.getBrandName() ?: String()).uppercase()
        val targa = (ApplicationData.getTarga() ?: String()).uppercase()
        carTargaTW.text = "$brandName\n$targa"
    }

    fun updateLogoImageView(){
        carIcon.setImageDrawable(Utility.getBrandDrawable(ctx) ?: return)
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