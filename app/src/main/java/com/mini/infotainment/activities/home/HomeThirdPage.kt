package com.mini.infotainment.activities.home

import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ImageView
import com.mini.infotainment.R
import com.mini.infotainment.support.Page

class HomeThirdPage(override val ctx: HomeActivity) : Page {
    internal lateinit var qrCodeIW: ImageView

    override fun build() {
        val layout = ctx.layoutInflater.inflate(R.layout.activity_home_3, ctx.viewPager, false) as ViewGroup
        qrCodeIW = layout.findViewById(R.id.home_3_qrcode)
        ctx.viewPages.add(layout)
    }

    fun updateQrCode(bitmap: Bitmap){
        qrCodeIW.setImageBitmap(bitmap)
    }
}