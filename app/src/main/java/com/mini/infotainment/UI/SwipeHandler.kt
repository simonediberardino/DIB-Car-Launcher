package com.mini.infotainment.UI

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.mini.infotainment.support.SActivity.Companion.screenSize
import kotlin.math.abs
import kotlin.properties.Delegates

class SwipeHandler(motionEvent: MotionEvent, val activity: AppCompatActivity) {
    var downY by Delegates.notNull<Float>()
    var downX by Delegates.notNull<Float>()

    init {
        downY = motionEvent.y
        downX = motionEvent.x
    }

    fun wasStraight(event: MotionEvent): Boolean{
        return abs(downX - event.x) < activity.screenSize[0]/20
    }

    fun wasSwipeDown(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.downY = event.y
                false
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP ->
                downY - event.y + activity.screenSize[1]/4 < 0 && wasStraight(event)
            else -> false
        }
    }

    fun wasSwipeUp(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.downX = event.x
                this.downY = event.y
                false
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP ->
                downY - event.y - activity.screenSize[1]/4 > 0 && wasStraight(event)

            else -> false
        }
    }

    fun wasSwipeToRightEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                false
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> downX - event.x - activity.screenSize[1]/4 > 0
            else -> false
        }
    }

    private fun wasSwipeToLeftEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                false
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> downX - event.x + activity.screenSize[1]/4 < 0
            else -> false
        }
    }

}