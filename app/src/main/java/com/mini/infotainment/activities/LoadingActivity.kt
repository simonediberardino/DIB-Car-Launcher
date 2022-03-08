package com.mini.infotainment.activities


import android.os.Bundle
import android.widget.TextView
import androidx.leanback.widget.Util
import com.mini.infotainment.R
import com.mini.infotainment.entities.Car
import com.mini.infotainment.storage.ApplicationData
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility

class LoadingActivity : ActivityExtended() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLayout()
    }

/*    fun checkCar(){
        if(Utility.isInternetAvailable())
            return

        val storedCar = ApplicationData.getCar()
        if(storedCar == null){
            Utility.navigateTo(this, LoginActivity::class.java)
            return
        }

        FirebaseClass.getCarObjectById(
            storedCar.targa,
            object : RunnablePar {
                override fun run(p: Any?) {
                    val retrievedCar = p as Car?
                    val passwordOnDatabase = retrievedUser?.password
                    val passwordOnDevice = storedAccount.password

                    if(passwordOnDatabase == passwordOnDevice){
                        LoginHandler.doLogin(retrievedUser)
                        setAccountDataListener()
                    }else{
                        LoginHandler.logoutByError()
                    }
                }
            })
    }*/

    private fun initializeLayout(){
        setContentView(R.layout.activity_loading)
        val currentTime = System.currentTimeMillis()
        val lastLoginTime = ApplicationData.getLastLogin().toInt()
        val isTimePassed = currentTime - ApplicationData.getLastLogin() >= 60*60*1000

        Utility.navigateTo(this,
            if(isTimePassed && lastLoginTime != 0)
                HomeActivity::class.java
            else
                LoginActivity::class.java
        )
    }

}

