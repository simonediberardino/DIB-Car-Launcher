package com.mini.infotainment.activities.login.register


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.activities.login.access.LoginActivity
import com.mini.infotainment.data.ApplicationData
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
        this.findViewById<View>(R.id.register_data_safety).setOnClickListener { showPolicy() }

        viewModel.result.observe(this) {
            when (it) {
                null -> {
                    Utility.toast(this, this.getString(R.string.registered))
                    finish()
                }
                else -> showError(it)
            }
        }
    }

    private fun showPolicy(){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ApplicationData.POLICY_URL))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            intent.setPackage(null)
            startActivity(intent)
        }
    }
}