package com.mini.infotainment.activities.settings

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.mini.infotainment.R
import com.mini.infotainment.activities.home.HomeActivity.Companion.instance
import com.mini.infotainment.activities.settings.model.AbstractSettingsActivity
import com.mini.infotainment.data.Data

class SettingsActivityCustomize: AbstractSettingsActivity() {
    companion object{
        private var BRANDS = arrayOf("alfaromeo", "audi", "bmw", "citroen", "fiat", "ford", "mazda", "mercedes", "mini", "nissan", "peugeot", "renault", "skoda", "toyota", "volkswagen")
    }

    private var isFirstLaunch: Boolean = false
    private lateinit var confirmButton: View
    private lateinit var llLogos: LinearLayout
    private var logos = arrayListOf<Logo>()

    private var selectedLogo: Logo? = null
        set(value) {
            field = value
            for(logo: Logo in logos)
                logo.view.setBackgroundColor(
                    if(logo != selectedLogo)
                        Color.TRANSPARENT
                    else{
                        getColor(R.color.darkblue)
                    }
                )
        }

    private var wallpapers = mutableListOf<WallpaperView>()
    private var selectedWallpaper: WallpaperView? = null
        set(value) {
            field = value
            for(wp: WallpaperView in wallpapers)
                (wp.view.findViewById<View>(R.id.item_image_cardview) as CardView).setCardBackgroundColor(
                    if(wp != selectedWallpaper)
                        Color.WHITE
                    else{
                        getColor(R.color.darkblue)
                    }
                )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        initializeLayout()
        super.pageLoaded()
    }

    override fun initializeLayout() {
        setContentView(R.layout.activity_settings_customize)
        super.initializeLayout()

        llLogos = findViewById(R.id.settings_customize_ll_logos)

        confirmButton = findViewById(R.id.settings_confirm_button)
        confirmButton.setOnClickListener {
            finish()
        }

        inflateLogos()
        inflateWallpapers()
    }

    override fun onBackPressed() {
        if(!isFirstLaunch)
            super.onBackPressed()
    }


    private fun inflateLogos() {
        val brands = BRANDS.map { it.lowercase() }.sorted().toMutableList()
        brands.add("no")

        for(brand: String in brands)
            inflateLogo(brand)

        selectedLogo = logos.find { it.brandName == Data.getBrandName()} ?: logos.first()
    }

    private fun inflateLogo(brandName: String){
        val gallery = findViewById<ViewGroup>(R.id.settings_customize_ll_logos)
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

    private fun getWallpapers(): MutableList<Wallpaper> {
        val result = mutableListOf<Wallpaper>()

        var i = 0
        while(true){
            val wpName = "wallpaper_$i"
            val wpId: Int = resources.getIdentifier(wpName, "drawable", packageName)
            if(wpId == 0) break

            try{
                val wpDrawable: Drawable = getDrawable(wpId) ?: break
                result.add(Wallpaper(wpDrawable, wpName))
            }catch (r: Resources.NotFoundException){
                break
            }

            i++
        }
        return result
    }

    private fun inflateWallpapers(){
        for(wp: Wallpaper in getWallpapers())
            doInflateWallpaper(wp)

        selectedWallpaper = wallpapers.find { it.name.equals(Data.getWallpaper(), ignoreCase = true) }
    }

    private fun doInflateWallpaper(wallpaper: Wallpaper){
        val gallery = findViewById<ViewGroup>(R.id.settings_customize_ll_wallpapers)
        val inflater = LayoutInflater.from(this)

        val view: View = inflater.inflate(R.layout.item_image, gallery, false)
        val wallpaperView = WallpaperView(view, wallpaper.name)
        val image = view.findViewById<ImageView>(R.id.item_image)
        val description = view.findViewById<TextView>(R.id.image_item_name)


        image.setImageDrawable(wallpaper.drawable)
        description.visibility = View.GONE

        view.setOnClickListener {
            selectedWallpaper = wallpaperView
        }

        wallpapers.add(wallpaperView)
        gallery.addView(view)
    }


    override fun handleSettings() {
        Data.setBrandName(selectedLogo!!.brandName)
        Data.setWallpaper(selectedWallpaper!!.name)

        if(isFirstLaunch)
            instance?.initializeActivity()
    }

    internal class WallpaperView(val view: View, val name: String)
    internal class Wallpaper(val drawable: Drawable, val name: String)
    internal class Logo(val view: View, val brandName: String)
}