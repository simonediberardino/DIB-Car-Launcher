package com.mini.infotainment.ads

import android.os.Handler
import android.os.Looper
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.SActivity.Companion.isInternetAvailable

class AdHandler<T : Ads>(val ctx: SActivity, val adClass: Class<T>) {
    private lateinit var ad: T

    fun startTimeout() {
        Thread{
            while(true){
                val timeOut = 1000*30*1L

                try{
                    Thread.sleep(timeOut)
                    FirebaseClass.isPremiumCar(object : RunnablePar{
                        override fun run(p: Any?) {
                            try{
                                ctx.runOnUiThread {
                                    if(p != true)
                                        showAd()
                                }
                            }catch (exception: java.lang.Exception){}

                        }
                    })


                }catch (exception: java.lang.Exception){
                    continue
                }
            }
        }.start()
    }

    fun showAd() {
        if(!ctx.isInternetAvailable){
            return
        }

        try {
            ad = adClass.newInstance().also {
                it.ctx = ctx
                it.onAdDismissed = Runnable {
                    ctx.runOnUiThread {
                        CustomToast(ctx.getString(R.string.remove_ads_premium), ctx)
                    }
                }
                it.init()
            }
        }catch (exception: java.lang.Exception){

        }
    }
}