package com.mini.infotainment.activities.home

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.mini.infotainment.R
import com.mini.infotainment.support.AppInfo
import com.mini.infotainment.support.Page


class AppsMenu(override val ctx: HomeActivity) : Page() {
    companion object {
        var apps = mutableListOf<AppInfo>()
        var adapter: ArrayAdapter<AppInfo>? = null
    }
    
    internal lateinit var containAppDrawer: ConstraintLayout
    internal var isAppDrawerVisible = true
    internal lateinit var grdView: GridView

    override fun build() {
        apps.clear()
        adapter = null

        containAppDrawer = ctx.findViewById(R.id.home_containAppDrawer)

        loadApps()
        loadListView()
        addGridListeners()

        this.show(false, 0)
    }

    private fun loadApps() {
        apps.clear()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val availableApps = ctx.packageManager!!.queryIntentActivities(intent, 0)

        for (appAtI in availableApps) {
            val appInfo = AppInfo(
                appAtI.activityInfo.packageName,
                appAtI.loadLabel(ctx.packageManager),
                appAtI.activityInfo.loadIcon(ctx.packageManager)
            )

            apps.add(appInfo)
        }
    }

    private fun loadListView() {
        grdView = ctx.findViewById<View>(R.id.grid_apps) as GridView
        if (adapter == null) {
            adapter = object : ArrayAdapter<AppInfo>(ctx, R.layout.menu_single_app, apps) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    var convertView = convertView
                    val viewHolder: Any?

                    if(convertView == null){
                        convertView = ctx.layoutInflater.inflate(R.layout.menu_single_app, parent, false)
                        viewHolder = ViewHolderItem()

                        viewHolder.icon = convertView!!.findViewById(R.id.app_icon)
                        viewHolder.label = convertView.findViewById(R.id.app_label)
                        convertView.tag = viewHolder
                    }else{
                        viewHolder = convertView.tag as ViewHolderItem
                    }

                    val appInfo = apps[position]
                    viewHolder.icon!!.setImageDrawable(appInfo.icon)
                    viewHolder.label!!.text = appInfo.label

                    return convertView
                }

                inner class ViewHolderItem {
                    var icon: ImageView? = null
                    var label: TextView? = null
                }
            }
        }

        grdView.adapter = adapter
    }

    private fun addGridListeners() {
        try {
            grdView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, i, _ ->
                    val intent = ctx.packageManager!!.getLaunchIntentForPackage(apps[i].name.toString())
                    ctx.startActivity(intent)
                }
        }catch (ex: Exception) {}
    }

    internal fun show(visibility: Boolean, duration: Long) {
        if(visibility == isAppDrawerVisible)
            return

        isAppDrawerVisible = visibility

        if (visibility) {
            slideAppsMenuUp(duration)
            hideMainMenu(duration)
        } else {
            showMainMenu(duration)
            slideAppsMenuDown(duration)
        }
    }

    private fun showMainMenu(duration: Long){
        val mainMenu = ctx.findViewById<View>(R.id.home_container)

        alphaAnimation(
            mainMenu,
            0f,
            1f,
            duration
        )

        moveAnimation(
            mainMenu,
            0f,
            0f,
            -mainMenu.height.toFloat(),
            0f,
            duration,
            null
        )

        scaleViewAnimation(
            mainMenu,
            3f,
            1f,
            duration
        )
    }

    private fun hideMainMenu(duration: Long){
        val mainMenu = ctx.findViewById<View>(R.id.home_container)

        alphaAnimation(mainMenu, 1f, 0f, duration)
        moveAnimation(mainMenu, 0f, 0f, 0f, -mainMenu.height.toFloat(), duration, null)
        scaleViewAnimation(mainMenu, 1f, 3f, duration)
    }

    private fun alphaAnimation(v: View, startAlpha: Float, endAlpha: Float, duration: Long){
        v.alpha = startAlpha
        v.animate().alpha(endAlpha).duration = duration
    }
    
    private fun moveAnimation(v: View, startX: Float, endX: Float, startY: Float, endY: Float, duration: Long, callback: Runnable?){
        val slideAnimation = TranslateAnimation(
            startX,
            endX,
            startY,
            endY
        )

        slideAnimation.interpolator = AccelerateInterpolator(2f)
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
    
    private fun scaleViewAnimation(v: View, startScale: Float, endScale: Float, duration: Long) {
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

    private fun slideAppsMenuUp(duration: Long) {
        grdView.visibility = View.VISIBLE

        alphaAnimation(
            containAppDrawer,
            0f,
            1f,
            duration
        )
        
        moveAnimation(
            containAppDrawer,
            0f,
            0f,
            containAppDrawer.height.toFloat(),
            0f,
            duration,
            null
        )
    }

    private fun slideAppsMenuDown(duration: Long) {
        alphaAnimation(
            containAppDrawer,
            1f, 
            0f,
            duration
        )
        
        moveAnimation(
            containAppDrawer,
            0f, 
            0f, 
            0f, 
            containAppDrawer.height.toFloat(), 
            duration
        ){
            grdView.visibility = View.INVISIBLE
        }
    }
}