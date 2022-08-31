package com.mini.infotainment.activities.login.edit


import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mini.infotainment.R
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.databinding.ActivityEditProfileBinding

class EditProfileActivity : ProfileActivity(){
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    private fun initializeLayout(){
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        DataBindingUtil.setContentView<ActivityEditProfileBinding>(
            this, R.layout.activity_edit_profile
        ).apply {
            this.lifecycleOwner = this@EditProfileActivity
            this.viewmodel = viewModel
        }

        viewModel.result.observe(this) {
            when (it) {
                null -> finish()
                else -> showError(it)
            }
        }
    }
}