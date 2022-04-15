package com.mini.infotainment.support

interface MapInteractions {
    fun onMapZoomInStartListener(): (Double) -> Unit
    fun onMapZoomInEndListener(): (zoomLevel: Double) -> Unit
    fun onMapZoomOutStartListener(): (zoomLevel: Double) -> Unit
    fun onMapZoomOutEndListener(): (zoomLevel: Double) -> Unit
    fun onMapPanListener(): () -> Unit
    fun onMapRotateListener(): () -> Unit
}