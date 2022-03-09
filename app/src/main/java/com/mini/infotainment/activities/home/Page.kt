package com.mini.infotainment.activities.home

interface Page {
    val ctx: HomeActivity
    fun build()
    fun setListeners(){}
}