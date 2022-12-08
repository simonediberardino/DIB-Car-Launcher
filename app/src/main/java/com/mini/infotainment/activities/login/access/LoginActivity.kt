package com.mini.infotainment.activities.login.access

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.activities.login.register.RegisterActivity
import com.mini.infotainment.databinding.ActivityLoginBinding
import com.mini.infotainment.utility.Utility

class LoginActivity : ProfileActivity(){
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    private fun initializeLayout(){
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        DataBindingUtil.setContentView<ActivityLoginBinding>(
            this, R.layout.activity_login
        ).apply {
            this.lifecycleOwner = this@LoginActivity
            this.viewmodel = viewModel
        }

        this.findViewById<View>(R.id.login_reg_btn).setOnClickListener { onBackPressed() }

        viewModel.result.observe(this) {
            when (it) {
                null -> {
                    Utility.toast(this, this.getString(R.string.logged))
                    finish()
                }
                else -> showError(it)
            }
        }
    }

    override fun onBackPressed() {
        Utility.navigateTo(this, RegisterActivity::class.java)
    }
}