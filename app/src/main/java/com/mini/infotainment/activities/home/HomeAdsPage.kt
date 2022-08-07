package com.mini.infotainment.activities.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.utility.Utility


class HomeAdsPage(override val ctx: HomeActivity) : Page() {
    private lateinit var viewGroup: ViewGroup

    companion object{
        //val BANNER_ID = "ca-app-pub-5725383971112097/9554636134"
        val BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        val N_ADS = 5
    }

    override fun build() {
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)
        ctx.viewPages.clear()

        parent = ctx.layoutInflater.inflate(R.layout.activity_home_ads, ctx.viewPager, false) as ViewGroup
        viewGroup = parent!!.findViewById(R.id.home_ads_ll)!!

        noAdsAvailable()
        ctx.viewPages.add(parent!!)
    }

    fun handleAds(){
        viewGroup.removeAllViews()

        if(MyCar.instance.isPremium())
            showNoAdsPremium()
        else showAds()
    }

    fun showAds(){
        val params: FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.gravity = Gravity.TOP
        viewGroup.layoutParams = params

        for(i in 0 until N_ADS)
            showBanner()
    }

    fun noAdsAvailable(){
        val params: FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.gravity = Gravity.CENTER
        viewGroup.layoutParams = params

        val textView = TextView(ctx)
        textView.setTextColor(Color.WHITE)
        textView.text = ctx.getString(R.string.no_ads_available)
        textView.textSize = 12f

        viewGroup.addView(textView)

        Utility.ridimensionamento(ctx, viewGroup)
    }
    
    @SuppressLint("UseCompatLoadingForDrawables")
    fun showNoAdsPremium(){
        val params: FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.gravity = Gravity.CENTER
        viewGroup.layoutParams = params

        val child: View = ctx.layoutInflater.inflate(R.layout.viewgroup_premium_no_ads, null)
        viewGroup.addView(child)

        Utility.ridimensionamento(ctx, viewGroup)
    }

    fun showBanner(){
        val adRequest = AdRequest.Builder().build()

        val adView = AdView(ctx)
        adView.adUnitId = BANNER_ID
        adView.setAdSize(AdSize.LARGE_BANNER)

        adView.setPadding(0,0,0,8)
        viewGroup.addView(adView)
        adView.loadAd(adRequest)

        Utility.ridimensionamento(ctx, viewGroup)
    }
}