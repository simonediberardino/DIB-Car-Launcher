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
        println("INDEX ${position}")
        if(!pagesReady[position]) {
            collection.addView(pages[position])
            pagesReady[position] = true
        }

        return pages[position]
    }

    fun selectDot(layout: ViewGroup, position: Int){
/*        val boxSize = layout.findViewById<ConstraintLayout>(R.id.quiz_mo_constraintLayout).layoutParams.width
        val dotMargin = 4
        val dotSize = boxSize / (nQuestions) - dotMargin
        val dotsLayout = layout.findViewById<LinearLayout>(R.id.quiz_mo_dots)

        dotsLayout.removeAllViews()

        for(index in 0 until nQuestions) {
            val viewToAdd = ImageView(mContext)
            val isSelected = index == position

            val drawableId = if(isSelected) R.drawable.selected_dot else R.drawable.default_dot
            viewToAdd.alpha = if(isSelected) 1F else 0.5F
            viewToAdd.minimumWidth = dotSize
            viewToAdd.minimumHeight = dotSize

            viewToAdd.setImageResource(drawableId)
            dotsLayout.addView(viewToAdd)
        }

        Utility.ridimensionamento(mContext as AppCompatActivity, dotsLayout)*/
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any){}
    override fun getCount() = pages.size
    override fun isViewFromObject(view: View, `object`: Any) = view === `object`
}