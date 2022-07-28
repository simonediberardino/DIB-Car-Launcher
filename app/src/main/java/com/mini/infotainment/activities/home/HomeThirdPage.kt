package com.mini.infotainment.activities.home

import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.utility.Utility

class HomeThirdPage(override val ctx: HomeActivity) : Page() {
    internal lateinit var qrCodeIW: ImageView
    internal lateinit var notificheCB: CheckBox

    override fun build() {
        parent = ctx.layoutInflater.inflate(R.layout.activity_home_3, ctx.viewPager, false) as ViewGroup
        qrCodeIW = parent!!.findViewById(R.id.home_3_qrcode)
        notificheCB = parent!!.findViewById(R.id.home_3_notifiche_cb)

        ctx.viewPages.add(parent!!)
        setListeners()
        super.pageLoaded()
    }

    fun updateQrCode(bitmap: Bitmap){
        qrCodeIW.setImageBitmap(bitmap)
    }

    override fun setListeners(){
        notificheCB.isChecked = ApplicationData.areNotificationsEnabled()
        notificheCB.setOnClickListener {
            val isChecked = (it as CheckBox).isChecked
            ApplicationData.areNotificationsEnabled(isChecked)
        }

        qrCodeIW.setOnClickListener {
            val ipv4Address = HomeActivity.server?.serverIPV4 ?: return@setOnClickListener
            Utility.showToast(ctx, ipv4Address)
        }
    }
}