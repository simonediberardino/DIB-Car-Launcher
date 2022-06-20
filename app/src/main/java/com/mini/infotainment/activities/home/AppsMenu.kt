package com.mini.infotainment.activities.home

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
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
    internal var isAppDrawerVisible = false
    internal lateinit var grdView: GridView

    override fun build() {
        apps.clear()
        adapter = null

        containAppDrawer = ctx.findViewById(R.id.home_containAppDrawer)
        containAppDrawer.visibility = View.INVISIBLE
        
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

        grdView.visibility = if(visibility) View.VISIBLE else View.INVISIBLE
        isAppDrawerVisible = visibility

        if (visibility) {
            slideMenuUp(duration)
        } else {
            slideMenuDown(duration)
        }
    }

    private fun slideMenuUp(duration: Long) {
        val background = ctx.findViewById<View>(R.id.home_background)
        background.alpha = 0f
        background.animate().alpha(0.4f).duration = duration

        containAppDrawer.visibility = View.VISIBLE

        val slideAnimation = TranslateAnimation(
            0f,
            0f,
            containAppDrawer.height.toFloat(),
            0f
        )

        slideAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                containAppDrawer.visibility = View.VISIBLE
            }
        })

        slideAnimation.duration = duration
        slideAnimation.fillAfter = false
        containAppDrawer.startAnimation(slideAnimation)
    }

    private fun slideMenuDown(duration: Long) {
        val background = ctx.findViewById<View>(R.id.home_background)
        background.alpha = 0.4f
        background.animate().alpha(0f).duration = duration

        val slideAnimation = TranslateAnimation(
            0f,
            0f,
            0f,
            containAppDrawer.height.toFloat()
        )

        slideAnimation.duration = duration
        slideAnimation.fillAfter = false
        slideAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationEnd(p0: Animation?) {
                containAppDrawer.visibility = View.INVISIBLE
            }

            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?){}
        })

        containAppDrawer.startAnimation(slideAnimation)
    }

}