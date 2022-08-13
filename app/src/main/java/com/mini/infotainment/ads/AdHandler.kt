package com.mini.infotainment.ads

import android.icu.util.Calendar
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

class AdHandler<T : Ads>(val ctx: SActivity, val adClass: Class<T>) {
    private lateinit var ad: T

    fun startTimeout() {
        Thread{
            while(true){
                val timeOut = 1000*60*7L
                Thread.sleep(timeOut)
                ctx.runOnUiThread { showAd() }
            }
        }.start()
    }

    private fun showAd() {
        if(MyCar.instance.isPremium() || !Utility.isInternetAvailable()){
            return
        }

        ad = adClass.newInstance().also {
            it.ctx = ctx
            it.onAdDismissed = Runnable {
                CustomToast(ctx.getString(R.string.remove_ads_premium), ctx)
            }
            it.init()
        }
    }

    private fun getTimeout(): Int {
        val calendar = Calendar.getInstance()
        val minutes = calendar.get(Calendar.MINUTE)

        return if(minutes >= 30)
            60-minutes
        else 30-minutes
    }
}