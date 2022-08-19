package com.mini.infotainment.activities.login

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.mini.infotainment.R
import com.mini.infotainment.data.ApplicationData
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility

class EditProfileActivity : ProfileActivity(){
    private lateinit var plateNumValueET: EditText
    private lateinit var plateNumPassEt: EditText
    private lateinit var passCurrET: EditText
    private lateinit var passNewET: EditText
    private lateinit var confirmBtn: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    private fun initializeLayout(){
        setContentView(R.layout.activity_edit_profile)
        this.findViewById<ViewGroup>(R.id.parent).setBackgroundDrawable(wpaper)

        plateNumValueET = findViewById(R.id.edit_plate_et)
        plateNumPassEt = findViewById(R.id.edit_plate_passw_et)
        passCurrET = findViewById(R.id.edit_passw_curr_et)
        passNewET = findViewById(R.id.edit_passw_new_et)

        confirmBtn = findViewById<View?>(R.id.edit_profile_confirm_button).also {
            it.setOnClickListener { handleData() }
        }
    }

    override fun handleData() {
        super.handleData{
            val plateNumValue = plateNumValueET.text.toString().trim()
            val plateNumPass = plateNumPassEt.text.toString().trim()

            val callback = Runnable {
                val passCurr = passCurrET.text.toString().trim()
                val passNew = passNewET.text.toString().trim()

                if(passCurr.isNotEmpty() && passNew.isNotEmpty()){
                    editPass(passCurr, passNew)
                }else finish()
            }

            if(plateNumValue.isNotEmpty() && plateNumPass.isNotEmpty()){
                editPlateNum(plateNumValue, plateNumPass, callback)
            }else callback.run()
        }
    }

    private fun editPlateNum(plateNumValue: String, plateNumPass: String, callback: Runnable?){
        FirebaseClass.areCredentialsCorrect(ApplicationData.getTarga()!!, Utility.getMD5(plateNumPass), object : RunnablePar {
            override fun run(p: Any?) {
                val isPwCorrect = p as Boolean? ?: false

                if(!isPwCorrect){
                    showError(ErrorCodes.INVALID_DETAILS)
                    return
                }

                FirebaseClass.getCarObject(ApplicationData.getTarga()!!, object: RunnablePar{
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
        FirebaseClass.areCredentialsCorrect(ApplicationData.getTarga()!!, Utility.getMD5(passCurr), object : RunnablePar {
            override fun run(p: Any?) {
                val isPwCorrect = p as Boolean? ?: false

                if(isPwCorrect){
                    FirebaseClass.getPasswordReference().setValue(Utility.getMD5(passNew)).addOnCompleteListener {
                        finish()
                    }
                }else showError(ErrorCodes.INVALID_DETAILS)
            }
        })
    }
}