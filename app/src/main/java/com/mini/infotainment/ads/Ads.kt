package com.mini.infotainment.ads

import com.mini.infotainment.support.SActivity

abstract class Ads() {
    abstract var ctx: SActivity?
    abstract val AD_ID: String
    abstract fun init()
    abstract fun show()
    open var onAdDismissed: Runnable = Runnable {}
}