package com.mini.infotainment.support

interface Page {
    val ctx: ActivityExtended
    fun build()
    fun setListeners(){}
}