package com.mini.infotainment.activities.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.login.register.RegisterActivity
import com.mini.infotainment.activities.maps.MapsActivity
import com.mini.infotainment.activities.stats.ActivityStats
import com.mini.infotainment.data.Data
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.errors.Errors
import com.mini.infotainment.support.QrcodeData
import com.mini.infotainment.support.SActivity.Companion.isInternetAvailable
import com.mini.infotainment.utility.Utility

class HomeSecondPage(override val ctx: HomeActivity) : Page() {
    override fun build() {
        parent = ctx.layoutInflater.inflate(R.layout.activity_home_2, ctx.viewPager, false) as ViewGroup
        val gridView = parent!!.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.home_2_grid)

        class GridButton(name: String, drawableId: Int, val callback: Runnable) {
            init {
                val singleItem = ctx.layoutInflater.inflate(R.layout.item_image, parent, false) as ViewGroup
                val itemNameTW = singleItem.findViewById<TextView>(R.id.image_item_name)
                val itemNameIW = singleItem.findViewById<ImageView>(R.id.item_image)

                itemNameTW.text = name
                itemNameIW.setImageResource(drawableId)
                singleItem.setOnClickListener {
                    callback.run()
                }

                gridView.addView(singleItem)
            }
        }

        GridButton(ctx.getString(R.string.menu_phone), R.drawable.menu_phone) {
            showQrCodeDialog()
        }
        GridButton(ctx.getString(R.string.stats_title), R.drawable.menu_stats) { goToStatsActivity() }

        GridButton(
            ctx.getString(R.string.premium_dib),
            R.drawable.menu_premium
        ) { showPremiumPage() }

        GridButton(
            ctx.getString(R.string.menu_navigatore),
            R.drawable.menu_navigation
        ) { goToMapsActivity() }
        GridButton(
            ctx.getString(R.string.menu_spotify),
            R.drawable.menu_spotify
        ) { ctx.runSpotify() }
        GridButton(
            ctx.getString(R.string.menu_settings),
            R.drawable.menu_settings
        ) { ctx.runSettings() }

        ctx.viewPages.add(parent!!)
        super.pageLoaded()
    }

    private fun showPremiumPage(){
        if(!ctx.isInternetAvailable) {
            Errors.printError(Errors.ErrorCodes.INTERNET_NOT_AVAILABLE, ctx)
            return
        }

        if(!Data.isLogged()){
            Utility.navigateTo(ctx, RegisterActivity::class.java)
            return
        }
        
        if(!MyCar.instance.isPremium()){
            ctx.goToCheckout()
        }else CustomToast(ctx.getString(R.string.already_premium), ctx)
    }

    private fun goToStatsActivity(){
        if(!ctx.isInternetAvailable) {
            Errors.printError(Errors.ErrorCodes.INTERNET_NOT_AVAILABLE, ctx)
            return
        }

        if(!Data.isLogged()){
            Utility.navigateTo(ctx, RegisterActivity::class.java)
            return
        }

        ctx.premiumFeature{
            Utility.navigateTo(
                ctx,
                ActivityStats::class.java
            )
        }
    }

    private fun goToMapsActivity(){
        if(!ctx.isInternetAvailable) {
            Errors.printError(Errors.ErrorCodes.INTERNET_NOT_AVAILABLE, ctx)
            return
        }

        if(!Data.isLogged()){
            Utility.navigateTo(ctx, RegisterActivity::class.java)
            return
        }

        ctx.premiumFeature {
            Utility.navigateTo(ctx, MapsActivity::class.java)
        }
    }

    private fun showQrCodeDialog() {
        if(!ctx.isInternetAvailable) {
            Errors.printError(Errors.ErrorCodes.INTERNET_NOT_AVAILABLE, ctx)
            return
        }

        if(!Data.isLogged()){
            Utility.navigateTo(ctx, RegisterActivity::class.java)
            return
        }

        ctx.premiumFeature{
            val qrCodeBitmap = Utility.generateQrCode(
                Utility.objectToJsonString(
                    QrcodeData(
                        HomeActivity.server?.serverIPV4 ?: return@premiumFeature,
                        Data.getUserName()!!,
                        Data.getPin()!!
                    )
                ),
                ctx
            )

            val dialog = Dialog(ctx)
            dialog.setContentView(R.layout.dialog_qrcode)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val closeBtn = dialog.findViewById<View>(R.id.qrcode_close)
            val qrCodeIW = dialog.findViewById<ImageView>(R.id.qrcode_qrcode)

            qrCodeIW.setImageBitmap(qrCodeBitmap)
            closeBtn.setOnClickListener { dialog.dismiss() }

            Utility.ridimensionamento(ctx, dialog.findViewById(R.id.parent))

            dialog.show()
        }
    }
}
