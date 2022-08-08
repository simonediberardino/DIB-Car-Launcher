package com.mini.infotainment.activities.settings

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity.Companion.instance
import com.mini.infotainment.activities.login.EditProfileActivity
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility


class SettingsActivity : SActivity() {
    data class Logo(val view: View, val brandName: String)

    companion object{
        private var BRANDS = arrayOf("alfaromeo", "audi", "bmw", "citroen", "fiat", "ford", "mercedes", "mini", "nissan", "peugeot", "renault", "skoda", "toyota", "volkswagen")
    }

    private lateinit var settingsDefaultWPCB: CheckBox
    private lateinit var confirmButton: View
    private lateinit var editAccountButton: View
    private lateinit var llLogos: LinearLayout
    private var isFirstLaunch = false

    private var logos = arrayListOf<Logo>()
    private var selectedLogo: Logo? = null
        set(value) {
            field = value
            for(logo: Logo in logos)
                logo.view.setBackgroundColor(
                    if(logo != selectedLogo)
                        Color.TRANSPARENT
                    else instance.getColor(R.color.darkblue)
                )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        this.initializeLayout()
    }

    private fun initializeLayout(){
        this.setContentView(R.layout.activity_settings)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(Utility.getWallpaper(
            instance
        ))

        confirmButton = findViewById(R.id.settings_confirm_button)
        editAccountButton = findViewById<View?>(R.id.settings_edit_account)
            .also {
                it.visibility = if(isFirstLaunch) View.GONE else View.VISIBLE
                it.setOnClickListener {
                    Utility.navigateTo(this, EditProfileActivity::class.java)
                    finish()
                }
            }

        llLogos = findViewById(R.id.ll_logos)
        settingsDefaultWPCB = findViewById(R.id.settings_default_wp)

        confirmButton.setOnClickListener { this.handleSettings() }
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
        val inflater = LayoutInflater.from(instance)

        val logoId: Int = instance.resources.getIdentifier("logo_${brandName}", "drawable", instance.packageName)
        val view: View = inflater.inflate(R.layout.single_logo, gallery, false)

        val logoDrawable: Drawable = instance.getDrawable(logoId) ?: return
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
        Toast.makeText(this, getString(R.string.applying_data), Toast.LENGTH_LONG).show()

        ApplicationData.setBrandName(selectedLogo?.brandName!!.lowercase())

        if(ApplicationData.useDefaultWP() != settingsDefaultWPCB.isChecked){
            ApplicationData.useDefaultWP(settingsDefaultWPCB.isChecked)
            instance.setWallpaper()
        }

        finish()
    }

    override fun finish() {
        if(isFirstLaunch)
            instance.initializeActivity()

        instance.homePage1.updateData()

        super.finish()
    }

    override fun onBackPressed() {
        if(!isFirstLaunch)
            super.onBackPressed()
    }
}