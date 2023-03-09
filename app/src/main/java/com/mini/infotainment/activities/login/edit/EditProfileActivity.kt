package com.mini.infotainment.activities.login.edit


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.activities.login.access.LoginViewModel
import com.mini.infotainment.data.Data
import com.mini.infotainment.databinding.ActivityEditProfileBinding
import com.mini.infotainment.utility.Utility

class EditProfileActivity : ProfileActivity(){
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    override fun initializeLayout(){
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        DataBindingUtil.setContentView<ActivityEditProfileBinding>(
            this, R.layout.activity_edit_profile
        ).apply {
            this.lifecycleOwner = this@EditProfileActivity
            this.viewmodel = viewModel
        }

        this.findViewById<TextView>(R.id.edit_profile_name).text = Data.getUserName()

        this.findViewById<View>(R.id.edit_disconnect).setOnClickListener {
            LoginViewModel.doLogout()
            Utility.toast(this, this.getString(R.string.disconnected))
            finish()
        }

        viewModel.result.observe(this) {
            when (it) {
                null -> finish()
                else -> showError(it)
            }
        }

        super.initializeLayout()
    }
}