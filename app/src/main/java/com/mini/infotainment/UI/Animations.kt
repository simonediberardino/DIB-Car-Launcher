package com.mini.infotainment.UI

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation

object Animations {
    val DEFAULT_ANIM_DURATION = 500L
    const val SLIDE_ANIMATION_DURATION: Long = 300

     fun alphaAnimation(v: View, startAlpha: Float, endAlpha: Float, delay: Long = 0, duration: Long = DEFAULT_ANIM_DURATION, callback: Runnable = Runnable {  }){
         Handler().postDelayed({
             v.alpha = startAlpha
             v.animate().setListener(object: AnimatorListener
             {
                 override fun onAnimationEnd(p0: Animator?) {
                     callback.run()
                 }
                 override fun onAnimationStart(p0: Animator?) {}
                 override fun onAnimationCancel(p0: Animator?) {}
                 override fun onAnimationRepeat(p0: Animator?) {}

             }).alpha(endAlpha).duration = duration
         }, delay)
    }

     fun moveAnimation(v: View, startX: Float, endX: Float, startY: Float, endY: Float, duration: Long, accelerateMult: Float = 2f, callback: Runnable?){
        val slideAnimation = TranslateAnimation(
            startX,
            endX,
            startY,
            endY
        )

        slideAnimation.interpolator = AccelerateInterpolator(accelerateMult)
        slideAnimation.duration = duration
        slideAnimation.fillAfter = true
        slideAnimation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                callback?.run()
            }
        })

        v.startAnimation(slideAnimation)
    }

     fun scaleViewAnimation(v: View, startScale: Float, endScale: Float, duration: Long) {
        val anim: Animation = ScaleAnimation(
            startScale,
            endScale,
            startScale, endScale,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f
        )

        anim.fillAfter = true
        anim.duration = duration
        anim.interpolator = AccelerateInterpolator(2f)

        v.startAnimation(anim)
    }
}