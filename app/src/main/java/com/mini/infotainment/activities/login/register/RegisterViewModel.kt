package com.mini.infotainment.activities.login.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mini.infotainment.activities.login.ProfileActivity
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.support.SActivity
import com.mini.infotainment.support.SActivity.Companion.isInternetAvailable
import com.mini.infotainment.utility.Utility

class RegisterViewModel : ViewModel(){
    var plateNum: MutableLiveData<String> = MutableLiveData()
    var pass: MutableLiveData<String> = MutableLiveData()
    var passConfirm: MutableLiveData<String> = MutableLiveData()
    var result: MutableLiveData<ProfileActivity.ErrorCodes> = MutableLiveData()

    fun handleData(){
        if(!SActivity.lastActivity.isInternetAvailable){
            result.value = ProfileActivity.ErrorCodes.NO_INTERNET
            return
        }

        checkIfExists {
            validateDetails{
                doRegister()
            }
        }
    }

    private fun checkIfExists(runnable: Runnable){
        val plateNum = plateNum.value.toString().replace(" ", "").uppercase().trim()

        FirebaseClass.doesCarExist(plateNum,
            object: RunnablePar {
                override fun run(p: Any?) {
                    val exists = p as Boolean
                    if(exists)
                        result.value = ProfileActivity.ErrorCodes.EXISTS
                    else runnable.run()
                }
            })
    }

    private fun validateDetails(runnable: Runnable){
        val plateNum = plateNum.value.toString().replace(" ", "").uppercase().trim()
        val psw1 = pass.value.toString().trim()
        val psw2 = passConfirm.value.toString().trim()

        if(psw1 != psw2) {
            result.value = ProfileActivity.ErrorCodes.PASSWORD_DONT_MATCH
            return
        }

        if(psw1.length < ProfileActivity.PASS_LENGTH){
            result.value = ProfileActivity.ErrorCodes.PASSWORD_SHORT
            return
        }

        if(plateNum.length < ProfileActivity.PLATE_LENGTH){
            result.value = ProfileActivity.ErrorCodes.PLATE_SHORT
            return
        }

        runnable.run()
    }

    private fun doRegister(){
        val plateNum = plateNum.value.toString().replace(" ", "").uppercase().trim()
        val password = Utility.getMD5(pass.value.toString().trim())

        val myCar = MyCar(plateNum, password)

        ApplicationData.setTarga(plateNum)
        ApplicationData.setCarPassword(password)
        FirebaseClass.getCarObjectReference(plateNum).setValue(myCar)

        result.value = null
    }
}