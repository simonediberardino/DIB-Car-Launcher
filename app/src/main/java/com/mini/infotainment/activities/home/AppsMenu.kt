package com.mini.infotainment.activities.home

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.mini.infotainment.R
import com.mini.infotainment.UI.Animations
import com.mini.infotainment.UI.Animations.alphaAnimation
import com.mini.infotainment.UI.Animations.moveAnimation
import com.mini.infotainment.UI.AppInfo
import com.mini.infotainment.UI.Page
import com.mini.infotainment.utility.Utility


class AppsMenu(override val ctx: HomeActivity) : Page() {
    companion object {
        var apps = mutableListOf<AppInfo>()
        var adapter: ArrayAdapter<AppInfo>? = null
    }

    private var isResized: Boolean = false
    private var isAppDrawerVisible = true
    private lateinit var containAppDrawer: ConstraintLayout
    private lateinit var grdView: GridView

    override fun build() {
        isResized = false

        apps.clear()
        adapter = null

        containAppDrawer = ctx.findViewById(R.id.home_containAppDrawer)

        loadApps()
        loadListView()
        addGridListeners()

        show(false, 0)
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
        apps.sortBy { it.label.toString() }
    }

    /*
        Loads the views of each installed app. Not made by me, need to be recoded in the future
        since it's not the best way to do it
    */
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
                    intent?.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intent)
                }
        }catch (ex: Exception) {}
    }

    internal fun show(visibility: Boolean, duration: Long = Animations.SLIDE_ANIMATION_DURATION) {
        if(visibility == isAppDrawerVisible)
            return

        // Resizes all the element the first time that the users presses the home button
        // that's just a workaround because you can't know the exact moment when the system has shown all the apps
        // this needs to be changed
        if(!isResized && duration != 0L){
            isResized = true
            Utility.ridimensionamento(ctx, containAppDrawer)
        }

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
            duration
        ){}
    }

    private fun hideMainMenu(duration: Long){
        val mainMenu = ctx.findViewById<View>(R.id.home_container)

        alphaAnimation(
            mainMenu,
            1f,
            0f,
            duration
        )

        moveAnimation(
            mainMenu,
            0f,
            0f,
            0f,
            -mainMenu.height.toFloat(),
            duration
        ){}
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
            duration
        ){}
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