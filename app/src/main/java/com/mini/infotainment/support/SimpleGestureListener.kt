package com.mini.infotainment.support

import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent


/**
 * Created by CoXier on 17-2-21.
 */
class SimpleGestureListener : SimpleOnGestureListener() {
    private var mListener: Listener? = null
    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.i(
            TAG, """
     $e1
     $e2
     """.trimIndent()
        )
        Log.d(
            TAG,
            "distanceX = $distanceX,distanceY = $distanceY"
        )
        if (mListener == null) return true
        if (distanceX == 0f && Math.abs(distanceY) > 1) {
            mListener!!.onScrollVertical(distanceY)
        }
        if (distanceY == 0f && Math.abs(distanceX) > 1) {
            mListener!!.onScrollHorizontal(distanceX)
        }
        return true
    }

    fun setListener(mListener: Listener?) {
        this.mListener = mListener
    }

    interface Listener {
        /**
         * left scroll dx >0
         * right scroll dx <0
         * @param dx
         */
        fun onScrollHorizontal(dx: Float)

        /**
         * upward scroll dy > 0
         * downward scroll dy < 0
         * @param dy
         */
        fun onScrollVertical(dy: Float)
    }

    companion object {
        private const val TAG = "SimpleGestureListener"
    }
}