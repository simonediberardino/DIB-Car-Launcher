package com.mini.infotainment.activities.login.register


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.activities.login.access.LoginActivity
import com.mini.infotainment.activities.settings.SettingsActivity
import com.mini.infotainment.databinding.ActivityRegisterBinding
import com.mini.infotainment.utility.Utility

class RegisterActivity : ProfileActivity(){
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    private fun initializeLayout(){
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        DataBindingUtil.setContentView<ActivityRegisterBinding>(
            this, R.layout.activity_register
        ).apply {
            this.lifecycleOwner = this@RegisterActivity
            this.viewmodel = viewModel
        }

        this.findViewById<View>(R.id.register_log_btn).setOnClickListener { Utility.navigateTo(this, LoginActivity::class.java) }

        viewModel.result.observe(this) {
            when (it) {
                null -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("isFirstLaunch", true)
                    startActivity(intent)
                    finish()
                }
                else -> showError(it)
            }
        }
    }


    override fun onBackPressed(){}
}