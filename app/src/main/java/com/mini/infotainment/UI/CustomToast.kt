package com.mini.infotainment.UI

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mini.infotainment.R
import com.mini.infotainment.utility.Utility

class CustomToast(val text: String, val activity: AppCompatActivity) : Toast(activity) {
    init{
        val inflater = activity.layoutInflater

        val layout: View = inflater.inflate(
            R.layout.custom_toast,
            activity.findViewById<View>(R.id.toast_parent) as ViewGroup?
        )

        val parent = layout.findViewById<ViewGroup>(R.id.toast_parent)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)

        toastText.text = text
        setGravity(Gravity.BOTTOM, 0, 0)
        duration = LENGTH_LONG
        view = layout

        Utility.ridimensionamento(activity, parent!!)
    }
}