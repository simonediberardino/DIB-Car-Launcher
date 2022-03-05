package com.mini.infotainment.activities
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.mini.infotainment.R
import java.lang.Exception
import java.util.ArrayList

class GetApps : Activity() {
    var grdView: GridView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applist)
        apps = null
        adapter = null
        loadApps()
        loadListView()
        addGridListeners()
    }

    fun addGridListeners() {
        try {
            grdView!!.onItemClickListener =
                OnItemClickListener { _, _, i, _ ->
                    val intent =
                        packageManager!!.getLaunchIntentForPackage(apps!![i].name.toString())
                    this@GetApps.startActivity(intent)
                }
        } catch (ex: Exception) {}
    }

    private fun loadListView() {
        try {
            grdView = findViewById<View>(R.id.grd_allApps) as GridView
            if (adapter == null) {
                adapter = object : ArrayAdapter<AppInfo>(this, R.layout.grd_items, apps as MutableList<AppInfo>) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        var convertView = convertView
                        val viewHolder: Any?

                        if (convertView == null) {
                            convertView = layoutInflater.inflate(R.layout.grd_items, parent, false)
                            viewHolder = ViewHolderItem()
                            viewHolder.icon = convertView!!.findViewById<View>(R.id.img_icon) as ImageView?
                            viewHolder.name = convertView.findViewById<View>(R.id.txt_name) as TextView?
                            viewHolder.label = convertView.findViewById<View>(R.id.txt_label) as TextView?
                            convertView.tag = viewHolder
                        } else {
                            viewHolder = convertView.tag as ViewHolderItem
                        }

                        val appInfo: AppInfo = apps!![position]
                        viewHolder.icon!!.setImageDrawable(appInfo.icon)
                        viewHolder.label?.text = appInfo.label
                        viewHolder.name?.text = appInfo.name

                        return convertView
                    }

                    inner class ViewHolderItem {
                        var icon: ImageView? = null
                        var label: TextView? = null
                        var name: TextView? = null
                    }
                }
            }
            grdView!!.adapter = adapter
        } catch (ex: Exception){}
    }

    private fun loadApps() {
        try {
            if (apps == null) {
                apps = ArrayList<AppInfo>()
                val i = Intent(Intent.ACTION_MAIN, null)
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                val availableApps = packageManager!!.queryIntentActivities(i, 0)
                for (ri in availableApps) {
                    val appinfo = AppInfo()
                    appinfo.label = ri.loadLabel(packageManager)
                    appinfo.name = ri.activityInfo.packageName
                    appinfo.icon = ri.activityInfo.loadIcon(packageManager)
                    apps!!.add(appinfo)
                }
            }
        } catch (ex: Exception){}
    }

    companion object {
        var apps: MutableList<AppInfo>? = null
        var adapter: ArrayAdapter<AppInfo>? = null
    }
}
