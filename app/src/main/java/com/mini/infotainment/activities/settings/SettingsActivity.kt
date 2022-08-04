package com.mini.infotainment.activities.settings

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity.Companion.homeActivity
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.utility.Utility


class SettingsActivity : ActivityExtended() {
    data class Logo(val view: View, val brandName: String)

    companion object{
        private var BRANDS = arrayOf("alfaromeo", "audi", "bmw", "citroen", "fiat", "ford", "mercedes", "mini", "nissan", "peugeot", "renault", "skoda", "toyota", "volkswagen")
    }

    private lateinit var settingsDefaultWPCB: CheckBox
    private lateinit var settingsSpotifyOnBootCB: CheckBox
    private lateinit var confirmButton: View
    private lateinit var llLogos: LinearLayout
    private var isFirstLaunch = true

    private var logos = arrayListOf<Logo>()
    private var selectedLogo: Logo? = null
        set(value) {
            field = value
            for(logo: Logo in logos)
                logo.view.setBackgroundColor(
                    if(logo != selectedLogo)
                        Color.TRANSPARENT
                    else homeActivity.getColor(R.color.darkblue)
                )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", true)
        this.initializeLayout()
    }

    private fun initializeLayout(){
        this.setContentView(R.layout.activity_settings)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(Utility.getWallpaper(homeActivity))

        confirmButton = findViewById(R.id.settings_confirm_button)
        llLogos = findViewById(R.id.ll_logos)
        settingsSpotifyOnBootCB = findViewById(R.id.settings_spotify_boot)
        settingsDefaultWPCB = findViewById(R.id.settings_default_wp)

        confirmButton.setOnClickListener { this.handleSettings() }
        settingsSpotifyOnBootCB.isChecked = ApplicationData.doesSpotifyRunOnBoot()
        settingsDefaultWPCB.isChecked = ApplicationData.useDefaultWP()

        inflateLogos()
        super.pageLoaded()
    }

    private fun inflateLogos(){
        for(brand: String in BRANDS)
            inflateLogo(brand)
        selectedLogo = logos.find { it.brandName == ApplicationData.getBrandName()} ?: logos.first()
    }

    private fun inflateLogo(brandName: String){
        val gallery = findViewById<ViewGroup>(R.id.ll_logos)
        val inflater = LayoutInflater.from(homeActivity)

        val logoId: Int = homeActivity.resources.getIdentifier("logo_${brandName}", "drawable", homeActivity.packageName)
        val view: View = inflater.inflate(R.layout.single_logo, gallery, false)

        val logoDrawable: Drawable = homeActivity.getDrawable(logoId) ?: return
        val logoNameTW = view.findViewById<TextView>(R.id.car_brand)
        val logoIW = view.findViewById<ImageView>(R.id.car_logo)

        logoNameTW.text = brandName.uppercase()
        logoIW.setImageDrawable(logoDrawable)

        val thisLogo = Logo(view, brandName.lowercase())
        view.setOnClickListener {
            selectedLogo = thisLogo
        }
        logos.add(thisLogo)
        gallery.addView(view)
    }

    private fun handleSettings(){
        ApplicationData.setBrandName(selectedLogo?.brandName!!.lowercase())
        ApplicationData.doesSpotifyRunOnBoot(settingsSpotifyOnBootCB.isChecked)

        if(ApplicationData.useDefaultWP() != settingsDefaultWPCB.isChecked){
            ApplicationData.useDefaultWP(settingsDefaultWPCB.isChecked)
            homeActivity.setWallpaper()
        }

        finish()
    }

    override fun finish() {
        if(isFirstLaunch)
            homeActivity.continueToActivity()

        homeActivity.homePage1?.updateData()
        super.finish()
    }

    override fun onBackPressed() {
        if(isFirstLaunch)
            super.onBackPressed()
    }
}