package com.mini.infotainment.activities

import android.os.Bundle

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.*
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mini.infotainment.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showApps(v: View?) {}
}