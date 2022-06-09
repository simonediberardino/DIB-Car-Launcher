package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.Page
import com.mini.infotainment.support.SpotifyReceiver
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
    internal lateinit var carConsuptionTW: TextView
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
        carConsuptionTW = parent!!.findViewById(R.id.home_1_consuption)
        carTargaTW = parent!!.findViewById(R.id.home_1_targa)

        ctx.viewPages.add(parent!!)

        updateData()
        setListeners()
        setupTimer()

        super.pageLoaded()
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
            ctx.appsMenu?.show(true, HomeActivity.SLIDE_ANIMATION_DURATION)
        }
    }

    fun updateData(){
        updateLogoImageView()
        updateFuelConsuption()
        updateCarName()
    }

    fun updateFuelConsuption(){
        val fuelConsuption = "${ApplicationData.getFuelConsuption() ?: return}\nl/100km"
        carConsuptionTW.text = fuelConsuption
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