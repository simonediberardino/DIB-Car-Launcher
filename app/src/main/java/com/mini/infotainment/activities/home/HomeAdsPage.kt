package com.mini.infotainment.activities.home

import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page

class HomeAdsPage(override val ctx: HomeActivity) : Page() {
    companion object{
        val BANNER_ID = "ca-app-pub-5725383971112097/9554636134"
        // TEST ID: val BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        val N_ADS = 5
    }

    override fun build() {
        ctx.viewPager = ctx.findViewById(R.id.home_view_pager)
        ctx.viewPages.clear()

        parent = ctx.layoutInflater.inflate(R.layout.activity_home_ads, ctx.viewPager, false) as ViewGroup

        ctx.viewPages.add(parent!!)
    }

    fun showAds(){
        val viewGroup = parent!!.findViewById<ViewGroup>(R.id.home_ads_ll)!!
        viewGroup.removeAllViews()

        for(i in 0 until N_ADS)
            showBanner(viewGroup)
    }

    fun showBanner(viewGroup: ViewGroup){
        val adRequest = AdRequest.Builder().build()

        val adView = AdView(ctx)
        adView.adUnitId = BANNER_ID
        adView.setAdSize(AdSize.LARGE_BANNER)

        adView.setPadding(0,0,0,8)
        viewGroup.addView(adView)
        adView.loadAd(adRequest)
    }
}