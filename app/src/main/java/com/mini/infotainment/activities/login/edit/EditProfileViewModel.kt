package com.mini.infotainment.activities.login.edit

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

class EditProfileViewModel : ViewModel() {
    var plateNumValue: MutableLiveData<String> = MutableLiveData()
    var plateNumPass: MutableLiveData<String> = MutableLiveData()
    var passCurr: MutableLiveData<String> = MutableLiveData()
    var passNew: MutableLiveData<String> = MutableLiveData()
    var result: MutableLiveData<ProfileActivity.ErrorCodes> = MutableLiveData()


    fun handleData() {
        if(!SActivity.lastActivity.isInternetAvailable){
            result.value = ProfileActivity.ErrorCodes.NO_INTERNET
            return
        }

        val plateNumValue = plateNumValue.value.toString().trim()
        val plateNumPass = plateNumPass.value.toString().trim()

        val callback = Runnable {
            val passCurr = passCurr.value.toString().trim()
            val passNew = passNew.value.toString().trim()

            if(passCurr.isNotEmpty() && passNew.isNotEmpty() && passNew != "null" && passCurr != "null"){
                editPass(passCurr, passNew)
            }else result.value = null
        }

        if(plateNumValue.isNotEmpty() && plateNumPass.isNotEmpty() && plateNumValue != "null" && plateNumPass != "null"){
            editPlateNum(plateNumValue, plateNumPass, callback)
        }else callback.run()
    }

    private fun editPlateNum(plateNumValue: String, plateNumPass: String, callback: Runnable?){
        if(plateNumValue.length < ProfileActivity.PLATE_LENGTH){
            result.value = ProfileActivity.ErrorCodes.PLATE_SHORT
            return
        }

        FirebaseClass.areCredentialsCorrect(ApplicationData.getTarga()!!, Utility.getMD5(plateNumPass), object :
            RunnablePar {
            override fun run(p: Any?) {
                val isPwCorrect = p as Boolean? ?: false

                if(!isPwCorrect){
                    result.value = ProfileActivity.ErrorCodes.INVALID_DETAILS
                    return
                }

                FirebaseClass.getCarObject(ApplicationData.getTarga()!!, object: RunnablePar {
                    override fun run(p: Any?) {
                        val currCar = p as MyCar? ?: return
                        currCar.plateNum = plateNumValue

                        FirebaseClass.deleteField(ApplicationData.getTarga()!!){
                            ApplicationData.setTarga(currCar.plateNum)
                            FirebaseClass.addCarObject(currCar){ callback?.run() }
                        }
                    }
                })
            }
        })
    }

    private fun editPass(passCurr: String, passNew: String) {
        if(passNew.length < ProfileActivity.PASS_LENGTH){
            result.value = ProfileActivity.ErrorCodes.PASSWORD_SHORT
            return
        }

        val cripNewPass = Utility.getMD5(passNew)
        val cripCurrPass = Utility.getMD5(passCurr)

        FirebaseClass.areCredentialsCorrect(ApplicationData.getTarga()!!, cripCurrPass, object :
            RunnablePar {
            override fun run(p: Any?) {
                val isPwCorrect = p as Boolean? ?: false

                if(isPwCorrect){
                    ApplicationData.setCarPassword(cripNewPass)
                    FirebaseClass.getPasswordReference().setValue(cripNewPass).addOnCompleteListener {
                        result.value = null
                    }
                }else result.value = ProfileActivity.ErrorCodes.INVALID_DETAILS
            }
        })
    }
}