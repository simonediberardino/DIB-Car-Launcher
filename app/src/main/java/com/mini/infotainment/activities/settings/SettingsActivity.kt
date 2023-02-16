package com.mini.infotainment.activities.settings

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity.Companion.instance
import com.mini.infotainment.activities.login.edit.EditProfileActivity
import com.mini.infotainment.activities.login.register.RegisterActivity
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility


class SettingsActivity : SActivity() {
    data class Logo(val view: View, val brandName: String)

    companion object{
        private var BRANDS = arrayOf("alfaromeo", "audi", "bmw", "citroen", "fiat", "ford", "mercedes", "mini", "nissan", "peugeot", "renault", "skoda", "toyota", "volkswagen", "none")
    }

    private lateinit var smartphoneNotiSwitch: CheckBox
    private lateinit var uMeasureSpinner: Spinner
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
                    else getColor(R.color.darkblue)
                )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        this.initializeLayout()
        this.requestStoragePermission()
    }

    private fun initializeLayout(){
        this.setContentView(R.layout.activity_settings)

        confirmButton = findViewById(R.id.settings_confirm_button)
        smartphoneNotiSwitch = findViewById(R.id.settings_noti_on)
        smartphoneNotiSwitch.isChecked = ApplicationData.isNotificationStatusEnabled()

        editAccountButton = findViewById<View?>(R.id.settings_edit_account)
            .also {
                it.visibility = if(isFirstLaunch) View.GONE else View.VISIBLE
                it.setOnClickListener {
                    if(ApplicationData.isLogged()){
                        Utility.navigateTo(this, EditProfileActivity::class.java)
                        finish()
                    }else{
                        Utility.navigateTo(this, RegisterActivity::class.java)
                        finish()
                    }
                }
            }

        llLogos = findViewById(R.id.ll_logos)
        settingsDefaultWPCB = findViewById(R.id.settings_default_wp)

        confirmButton.setOnClickListener {
            finish()
        }

        settingsDefaultWPCB.isChecked = ApplicationData.useDefaultWP()

        uMeasureSpinner = findViewById<Spinner?>(R.id.settings_um).also {
            it.setSelection(ApplicationData.getUMeasure())
        }

        inflateLogos()
        super.pageLoaded()
    }

    private fun inflateLogos() {
        for(brand: String in BRANDS)
            inflateLogo(brand)
        selectedLogo = logos.find { it.brandName == ApplicationData.getBrandName()} ?: logos.first()
    }

    private fun inflateLogo(brandName: String){
        val gallery = findViewById<ViewGroup>(R.id.ll_logos)
        val inflater = LayoutInflater.from(this)

        val logoId: Int = resources.getIdentifier("logo_${brandName}", "drawable", packageName)
        val view: View = inflater.inflate(R.layout.single_logo, gallery, false)

        val logoDrawable: Drawable = getDrawable(logoId) ?: return
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

    override fun onDestroy() {
        handleSettings()
        super.onDestroy()
    }

    private fun handleSettings(){
        ApplicationData.setBrandName(selectedLogo?.brandName!!.lowercase())
        ApplicationData.setUMeasure(uMeasureSpinner.selectedItemPosition)

        if(ApplicationData.useDefaultWP() != settingsDefaultWPCB.isChecked){
            ApplicationData.useDefaultWP(settingsDefaultWPCB.isChecked)
            instance?.setWallpaper()
        }

        ApplicationData.setNotificationStatus(smartphoneNotiSwitch.isChecked)

        MyCar.instance.carbrand = selectedLogo?.brandName!!.lowercase()
        FirebaseClass.updateCarBrand(selectedLogo?.brandName!!.lowercase())
    }

    private fun requestStoragePermission(){
        val READ_EXTERNAL_STORAGE_ID = 1005
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_ID)
        }
    }

    override fun finish() {
        if(isFirstLaunch)
            instance?.initializeActivity()

        instance?.homePage1?.updateData()

        super.finish()
    }

    override fun onBackPressed() {
        if(!isFirstLaunch)
            super.onBackPressed()
    }
}