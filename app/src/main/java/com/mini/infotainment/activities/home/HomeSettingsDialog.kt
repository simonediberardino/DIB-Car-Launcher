package com.mini.infotainment.activities.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.utility.Utility


class HomeSettingsDialog(val homeActivity: HomeActivity) : Dialog(homeActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    data class Logo(val view: View, val brandName: String)

    companion object{
        private var brands = arrayOf("alfaromeo", "audi", "bmw", "citroen", "fiat", "ford", "mercedes", "mini", "nissan", "peugeot", "renault", "skoda", "toyota", "volkswagen")
    }

    private var settingsDefaultWPCB: CheckBox
    private var settingsSpotifyOnBootCB: CheckBox
    private var settingsTargaEt: EditText
    private var settingsConsuptionEt: EditText
    private var confirmButton: View
    private var llLogos: LinearLayout
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

    init {
        this.setContentView(R.layout.activity_settings)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(Utility.getWallpaper(homeActivity))
        settingsTargaEt = findViewById(R.id.settings_targa)
        settingsConsuptionEt = findViewById(R.id.settings_consumi)
        confirmButton = findViewById(R.id.settings_confirm_button)
        llLogos = findViewById(R.id.ll_logos)
        settingsSpotifyOnBootCB = findViewById(R.id.settings_spotify_boot)
        settingsDefaultWPCB = findViewById(R.id.settings_default_wp)

        confirmButton.setOnClickListener { this.handleSettings() }
        settingsTargaEt.setText(ApplicationData.getTarga() ?: String())
        settingsConsuptionEt.setText(ApplicationData.getFuelConsuption() ?: String())
        settingsSpotifyOnBootCB.isChecked = ApplicationData.doesSpotifyRunOnBoot()
        settingsDefaultWPCB.isChecked = ApplicationData.useDefaultWP()

        inflateLogos()
        Utility.ridimensionamento(homeActivity, this.findViewById(R.id.parent))
    }

    private fun inflateLogos(){
        for(brand: String in brands)
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
        val enteredTarga = settingsTargaEt.text.toString().trim()
        val enteredConsuption = settingsConsuptionEt.text.toString().trim()

        if(!isValidTarga(enteredTarga)
            || enteredConsuption.trim().isEmpty()
        ){
            Utility.showToast(homeActivity, homeActivity.getString(R.string.errore_input))
            return
        }

        ApplicationData.setBrandName(selectedLogo?.brandName!!.lowercase())
        ApplicationData.setTarga(enteredTarga)
        ApplicationData.setFuelConsuption(enteredConsuption)
        ApplicationData.doesSpotifyRunOnBoot(settingsSpotifyOnBootCB.isChecked)

        if(ApplicationData.useDefaultWP() != settingsDefaultWPCB.isChecked){
            ApplicationData.useDefaultWP(settingsDefaultWPCB.isChecked)
            homeActivity.setWallpaper()
        }

        this.dismiss()
    }

    private fun isValidTarga(targa: String): Boolean {
        if(targa.length != 7) return false

        return(
                targa[0].isLetter()
                        && targa[1].isLetter()
                        && targa[2].isDigit()
                        && targa[3].isDigit()
                        && targa[4].isDigit()
                        && targa[5].isLetter()
                        && targa[6].isLetter()
                )
    }
}