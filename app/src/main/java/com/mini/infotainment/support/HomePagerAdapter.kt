package com.mini.infotainment.support

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.mini.infotainment.R
import com.mini.infotainment.utility.Utility

class HomePagerAdapter(private val mContext: Context, private val pages: ArrayList<ViewGroup>) : PagerAdapter() {
    val pagesReady = Array(pages.size) { _ -> false }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        if(!pagesReady[position]) {
            collection.addView(pages[position])
            pagesReady[position] = true
        }

        return pages[position]
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any){}
    override fun getCount() = pages.size
    override fun isViewFromObject(view: View, `object`: Any) = view === `object`
}