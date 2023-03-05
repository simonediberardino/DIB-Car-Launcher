package com.mini.infotainment.activities.login.access

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.data.Data
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.SActivity.Companion.isInternetAvailable
import com.mini.infotainment.utility.Utility

class LoginViewModel : ViewModel(){
    var plateNum: MutableLiveData<String> = MutableLiveData()
    var pass: MutableLiveData<String> = MutableLiveData()
    var result: MutableLiveData<ProfileActivity.ErrorCodes> = MutableLiveData()

    fun handleData() {
        if(!SActivity.lastActivity.isInternetAvailable){
            result.value = ProfileActivity.ErrorCodes.NO_INTERNET
            return
        }

        val plateNum = plateNum.value.toString().uppercase().trim()
        val password = Utility.getMD5(pass.value.toString().trim())

        FirebaseClass.areCredentialsCorrect(plateNum, password, object : RunnablePar {
            override fun run(p: Any?) {
                val canLogin = p as Boolean
                if(!canLogin){
                    result.value = ProfileActivity.ErrorCodes.INVALID_DETAILS
                    return
                }

                FirebaseClass.getCarObject(plateNum, object : RunnablePar{
                    override fun run(p: Any?) {
                        val myCar = p as MyCar?
                        result.value = if(myCar == null) ProfileActivity.ErrorCodes.NO_INTERNET else null
                        doLogin(myCar ?: return)
                    }
                })
            }
        })
    }

    companion object{

        fun doLogin(myCar: MyCar){
            MyCar.instance.plateNum = myCar.plateNum.uppercase()
            MyCar.instance.password = myCar.password
            MyCar.instance.premiumDate = myCar.premiumDate
            MyCar.instance.pin = Utility.generatePin()

            Data.saveUser(myCar)
            FirebaseClass.getCarObjectReference(myCar.plateNum).setValue(MyCar.instance)

            HomeActivity.instance?.addFirebaseListeners()
            HomeActivity.instance?.startServer()
        }


        fun doLogout(){
            HomeActivity.instance?.removeFirebaseListeners()
            Data.deleteUser()
            HomeActivity.instance?.stopServer()
        }
    }

}