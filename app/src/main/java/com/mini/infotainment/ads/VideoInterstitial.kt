package com.mini.infotainment.ads

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mini.infotainment.support.SActivity

class VideoInterstitial(override var ctx: SActivity?) : Ads(){
    override val TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    override val AD_ID = "ca-app-pub-5725383971112097/5863846712"
    private var mInterstitialAd: InterstitialAd? = null

    constructor() : this(null)

    override fun init() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(ctx!!, AD_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                ctx!!.log("Ad failed to load: $adError")
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                ctx!!.log("Ad was loaded")
                mInterstitialAd = interstitialAd
                show()
            }
        })
    }

    override fun show(){
        mInterstitialAd?.show(ctx!!)

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                ctx!!.log("Ad was clicked")
            }

            override fun onAdDismissedFullScreenContent() {
                ctx!!.log("Ad dismissed fullscreen content.")
                onAdDismissed.run()
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ctx!!.log("Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                ctx!!.log("Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                ctx!!.log("Ad showed fullscreen content.")
            }
        }
    }
}