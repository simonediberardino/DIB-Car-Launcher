package com.mini.infotainment.activities.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mini.infotainment.R
import com.mini.infotainment.UI.Page
import com.mini.infotainment.activities.stats.ActivityStats
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.support.QrcodeData
import com.mini.infotainment.utility.Utility

class HomeSecondPage(override val ctx: HomeActivity) : Page() {
    override fun build() {
        parent = ctx.layoutInflater.inflate(R.layout.activity_home_2, ctx.viewPager, false) as ViewGroup
        val gridView = parent!!.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.home_2_grid)

        class GridButton(name: String, drawableId: Int, val callback: Runnable){
            init{
                val singleItem = ctx.layoutInflater.inflate(R.layout.home_2_items, parent, false) as ViewGroup
                val itemNameTW = singleItem.findViewById<TextView>(R.id.home_2_item_name)
                val itemNameIW = singleItem.findViewById<ImageView>(R.id.home_2_item_image)

                itemNameTW.text = name
                itemNameIW.setImageResource(drawableId)
                singleItem.setOnClickListener {
                    callback.run()
                }

                gridView.addView(singleItem)
            }
        }

        GridButton(ctx.getString(R.string.menu_phone), R.drawable.menu_phone) { showQrCodeDialog() }
        GridButton(ctx.getString(R.string.stats_title), R.drawable.menu_stats) { Utility.navigateTo(ctx, ActivityStats::class.java) }
        GridButton(ctx.getString(R.string.menu_voice), R.drawable.menu_voice) { ctx.runGoogleAssistant() }

        GridButton(ctx.getString(R.string.menu_navigatore), R.drawable.menu_navigation) { ctx.runGoogleMaps() }
        GridButton(ctx.getString(R.string.menu_spotify), R.drawable.menu_spotify) { ctx.runSpotify() }
        GridButton(ctx.getString(R.string.menu_settings), R.drawable.menu_settings) { ctx.runSettings() }

        ctx.viewPages.add(parent!!)
        super.pageLoaded()
    }

    fun showQrCodeDialog(){
        val qrCodeBitmap = Utility.generateQrCode(
            Utility.objectToJsonString(
                QrcodeData(HomeActivity.server?.serverIPV4 ?: return, ApplicationData.getTarga()!!)
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