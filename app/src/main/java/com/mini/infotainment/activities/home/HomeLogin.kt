package com.mini.infotainment.activities.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.utility.Utility

class HomeLogin(val homeActivity: HomeActivity) : Dialog(homeActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    data class Logo(val view: View, val brandName: String)

    companion object{
        private var brands = arrayOf("volkswagen", "mini", "bmw", "ford", "fiat")
    }
    private var loginTargaEt: EditText
    private var loginConsuptionEt: EditText
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
        this.setContentView(R.layout.activity_login)
        this.setCancelable(false)
        loginTargaEt = findViewById(R.id.login_targa)
        loginConsuptionEt = findViewById(R.id.login_consumi)
        confirmButton = findViewById(R.id.login_login_button)
        llLogos = findViewById(R.id.ll_logos)
        confirmButton.setOnClickListener { this.handleLogin() }
        loginTargaEt.setText(ApplicationData.getTarga() ?: String())
        loginConsuptionEt.setText(ApplicationData.getFuelConsuption() ?: String())
        inflateLogos()
    }

    private fun inflateLogos(){
        for(brand: String in brands)
            inflateLogo(brand)
        selectedLogo = logos.find { it.brandName == ApplicationData.getBrandName()} ?: logos.first()
    }

    private fun inflateLogo(brandName: String){
        val gallery = findViewById<ViewGroup>(R.id.ll_logos)
        val inflater = LayoutInflater.from(homeActivity)

        val logoId: Int = homeActivity.resources.getIdentifier("${brandName}_icon", "drawable", homeActivity.packageName)
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

    private fun handleLogin(){
        val enteredTarga = loginTargaEt.text.toString().trim()
        val enteredConsuption = loginConsuptionEt.text.toString().trim()

        if(!isValidTarga(enteredTarga)
            || enteredConsuption.trim().isEmpty()
        ){
            Utility.showToast(homeActivity, homeActivity.getString(R.string.errore_input))
            return
        }

        ApplicationData.setBrandName(selectedLogo?.brandName!!.lowercase())
        ApplicationData.setTarga(enteredTarga)
        ApplicationData.setFuelConsuption(enteredConsuption)

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