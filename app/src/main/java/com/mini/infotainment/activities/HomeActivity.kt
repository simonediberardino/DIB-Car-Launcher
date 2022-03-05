package com.mini.infotainment.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.mini.infotainment.R
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.animation.Animator

import android.animation.AnimatorListenerAdapter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.mini.infotainment.utility.Utility


class HomeActivity : Activity() {
    internal var isAppDrawerVisible = false
    internal lateinit var grdView: GridView
    internal lateinit var containerHome: ConstraintLayout
    internal lateinit var containAppDrawer: ConstraintLayout
    internal lateinit var locationManager: FusedLocationProviderClient

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        containAppDrawer = findViewById(R.id.containAppDrawer)
        containAppDrawer.visibility = View.INVISIBLE
        containerHome = findViewById(R.id.home_container)

        slideMenuDown(0)
        
        apps = null
        adapter = null
        
        loadApps()
        loadListView()
        addGridListeners()
        setupGPS()
    }

    private fun setupGPS() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                GEOLOCATION_PERMISSION_CODE
            )
        }else{
            this.setupUserLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private fun setupUserLocation(){
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                this@HomeActivity.onLocationChanged(locationResult?.lastLocation)
            }
        }

        locationManager = LocationServices.getFusedLocationProviderClient(this)
        locationManager.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        locationManager.lastLocation.addOnSuccessListener(this) {
            this.onLocationChanged(it)
        }.addOnFailureListener(this) {
            this.insufficientPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onLocationChanged(newLocation: Location?){
        val speedometerTW = findViewById<TextView>(R.id.home_speed)
        val speedInKmH = newLocation?.speedAccuracyMetersPerSecond?.times(3.6)
        speedometerTW.text = speedInKmH.toString()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            GEOLOCATION_PERMISSION_CODE -> {
                if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    this.insufficientPermissions()
                }else{
                    this.setupUserLocation()
                }
            }
        }
    }

    private fun insufficientPermissions(){
        Utility.showToast(this, getString(R.string.request_permissions))
    }

    private fun addGridListeners() {
        try {
            grdView.onItemClickListener =
                OnItemClickListener { _, _, i, _ ->
                    val intent = packageManager!!.getLaunchIntentForPackage(apps!![i].name.toString())
                    this@HomeActivity.startActivity(intent)
                }
        } catch (ex: Exception) {}
    }

    private fun loadListView() {
        try {
            grdView = findViewById<View>(R.id.grd_allApps) as GridView
            if (adapter == null) {
                adapter = object : ArrayAdapter<AppInfo>(this, R.layout.grd_items, apps!!) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        var convertView = convertView
                        var viewHolder: Any?

                        if (convertView == null) {
                            convertView = layoutInflater.inflate(R.layout.grd_items, parent, false)
                            viewHolder = ViewHolderItem()

                            viewHolder.icon = convertView!!.findViewById(R.id.img_icon)
                            viewHolder.name = convertView.findViewById(R.id.txt_name)
                            viewHolder.label = convertView.findViewById(R.id.txt_label)
                            convertView.tag = viewHolder
                        } else {
                            viewHolder = convertView.tag as ViewHolderItem
                        }

                        val appInfo = apps!![position]
                        viewHolder.icon!!.setImageDrawable(appInfo.icon)
                        viewHolder.label!!.text = appInfo.label
                        viewHolder.name!!.text = appInfo.name

                        return convertView
                    }

                    inner class ViewHolderItem {
                        var icon: ImageView? = null
                        var label: TextView? = null
                        var name: TextView? = null
                    }
                }
            }
            grdView.adapter = adapter
        } catch (ex: Exception) {}
    }

    private fun loadApps() {
        try {
            if (apps == null) {
                apps = ArrayList()
                val i = Intent(Intent.ACTION_MAIN, null)
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                val availableApps = packageManager!!.queryIntentActivities(i, 0)
                for (ri in availableApps) {
                    val appinfo = AppInfo()
                    appinfo.label = ri.loadLabel(packageManager)
                    appinfo.name = ri.activityInfo.packageName
                    appinfo.icon = ri.activityInfo.loadIcon(packageManager)
                    (apps as ArrayList<AppInfo>).add(appinfo)
                }
            }
        }catch (ex: Exception) {}
    }

    fun showApps(v: View?) {
        showAppDrawer(true)
    }

    private fun showAppDrawer(visibility: Boolean) {
        if(visibility == isAppDrawerVisible)
            return

        isAppDrawerVisible = visibility

        if (visibility) {
            slideMenuUp(SLIDE_ANIMATION_DURATION)
        } else {
            slideMenuDown(SLIDE_ANIMATION_DURATION)
        }
    }

    private fun slideMenuUp(duration: Long) {
        containAppDrawer.visibility = View.VISIBLE

        val background = findViewById<View>(R.id.home_background)
        background.alpha = 0f
        background
            .animate()
            .alpha(0.25f)
            .duration = duration*2

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
        val background = findViewById<View>(R.id.home_background)
        background.alpha = 0.25f
        background.animate()
            .alpha(0f)
            .duration = duration*2

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

    override fun onBackPressed(){
        showAppDrawer(false)
    }

    companion object {
        private const val GEOLOCATION_PERMISSION_CODE = 1
        private const val SLIDE_ANIMATION_DURATION: Long = 300
        var apps: MutableList<AppInfo>? = null
        var adapter: ArrayAdapter<AppInfo>? = null
    }
}
