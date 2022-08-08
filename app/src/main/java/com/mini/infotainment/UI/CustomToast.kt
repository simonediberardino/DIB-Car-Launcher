package com.mini.infotainment.UI

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.mini.infotainment.R
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility


class CustomToast(val text: String, val activity: SActivity) {
    companion object{
        var DURATION: Long = 4000L
        @SuppressLint("StaticFieldLeak")
        var toasts: MutableList<CustomToast> = mutableListOf()
    }

    private var toastView: ViewGroup? = null
    private var parent: ConstraintLayout? = null
    private var onHideCallback: Runnable = Runnable {}

    init{
        if(!toasts.any { it.text == this.text }) {
            toasts.add(this)

            activity.runOnUiThread{
                if(toasts.size > 1){
                    toasts[toasts.size-2].onHideCallback = Runnable {
                        init()
                    }
                }else init()
            }
        }
    }

    private fun init() {
        parent = activity.findViewById(R.id.parent)

        toastView = generateToastView()

        val toastText = toastView!!.findViewById<TextView>(R.id.custom_toast_tw)
        toastText.text = text

        Utility.ridimensionamento(activity, toastView ?: return)
        show()
    }

    private fun generateToastView(): LinearLayout {
        val parentLayout = LinearLayout(activity)
        parentLayout.setPadding(0,0,0,32)
        parentLayout.id = R.id.custom_toast_parent

        val childLayout = LinearLayout(activity)
        childLayout.setBackgroundResource(R.drawable.round_blue_view)
        childLayout.setPadding(16, 16, 16, 16)

        val toastIW = ImageView(activity)
        toastIW.setBackgroundResource(R.drawable.app_logo)
        toastIW.id = R.id.custom_toast_iw

        val toastTW = TextView(activity)
        toastTW.textSize = 7f
        toastTW.setTextColor(Color.WHITE)
        toastTW.id = R.id.custom_toast_tw
        toastTW.setPadding(16, 0, 0, 0)

        childLayout.addView(toastIW)
        childLayout.addView(toastTW)
        parentLayout.addView(childLayout)

        toastIW.layoutParams.width = 20
        toastIW.layoutParams.height = 20
        (toastIW.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.CENTER

        toastTW.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        Utility.ridimensionamento(activity, parentLayout)
        return parentLayout
    }

    private fun show(){
        if(toastView == null || parent == null) return

        toastView!!.alpha = 0f
        parent!!.removeView(toastView)
        parent!!.addView(toastView)

        val set = ConstraintSet()
        set.clone(parent)
        set.connect(toastView!!.id, ConstraintSet.RIGHT, parent!!.id, ConstraintSet.RIGHT, 0)
        set.connect(toastView!!.id, ConstraintSet.LEFT, parent!!.id, ConstraintSet.LEFT, 0)
        set.connect(toastView!!.id, ConstraintSet.BOTTOM, parent!!.id, ConstraintSet.BOTTOM, 0)
        set.applyTo(parent)

        Animations.alphaAnimation(toastView!!, 0F, 1F){
            hide()
        }
    }

    private fun hide(){
        Animations.alphaAnimation(toastView!!, 1f, 0f, DURATION){
            parent!!.removeView(toastView)
            toastView!!.removeAllViews()
            toasts.remove(this)

            onHideCallback.run()
        }
    }

}