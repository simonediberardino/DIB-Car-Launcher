package com.mini.infotainment.UI

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.mini.infotainment.R
import com.mini.infotainment.support.SActivity


class CustomToast(val text: String, val activity: SActivity) {
    companion object{
        var DURATION: Long = 4000L
    }
    private var toastView: ViewGroup? = null
    private var parent: ConstraintLayout? = null

    init{
        activity.runOnUiThread{
            parent = activity.findViewById(R.id.parent)
            val inflater = activity.layoutInflater

            val layout: View = inflater.inflate(
                R.layout.custom_toast,
                activity.findViewById<View>(R.id.toast_parent) as ViewGroup?
            )

            toastView = layout.findViewById(R.id.toast_parent)
            val toastText = layout.findViewById<TextView>(R.id.toast_text)
            toastText.text = text
        }
    }

    fun show(){
        if(toastView == null || parent == null) return

        toastView!!.alpha = 0f
        parent!!.addView(toastView)

        val set = ConstraintSet()
        set.clone(parent)
        set.connect(toastView!!.id, ConstraintSet.RIGHT, parent!!.id, ConstraintSet.RIGHT, 0)
        set.connect(toastView!!.id, ConstraintSet.LEFT, parent!!.id, ConstraintSet.LEFT, 0)
        set.connect(toastView!!.id, ConstraintSet.BOTTOM, parent!!.id, ConstraintSet.BOTTOM, 0)
        set.applyTo(parent)

        Animations.alphaAnimation(toastView!!, 0F, 1F){
            Animations.alphaAnimation(toastView!!, 1f, 0f, DURATION){
                parent!!.removeView(toastView)
            }
        }
    }

}