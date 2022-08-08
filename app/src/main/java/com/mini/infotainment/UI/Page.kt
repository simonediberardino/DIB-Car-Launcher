package com.mini.infotainment.UI

import android.view.ViewGroup
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.utility.Utility

abstract class Page {
    abstract val ctx: SActivity
    var parent: ViewGroup? = null

    open fun show(){}
    open fun build(){}
    fun pageLoaded(){
        Utility.ridimensionamento(ctx, parent ?: return)
    }
    open fun setListeners(){}
}