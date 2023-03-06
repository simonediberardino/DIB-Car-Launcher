package com.mini.infotainment.data
import android.location.Location
import com.google.firebase.database.*
import com.mini.infotainment.entities.MyCar
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility.networkDateMillis
import java.util.*


object FirebaseClass{
    private var DB_REF = "https://infotainment-8c303-default-rtdb.europe-west1.firebasedatabase.app/"
    private var LOCATION_REF = "location"
    private var CAR_BRAND_REF = "carbrand"
    private var SERVER_IP_REF = "serverip"
    private var TIME_REF = "time"
    private var START_REF = "start"
    private var PASSWORD_REF = "password"
    private var PIN_REF = "pin"
    private var PREMIUM_DATE_REF = "premiumDate"

    fun isPremiumCar(runnablePar: RunnablePar){
        val userId = Data.getUserName()
        if(userId == null){
            runnablePar.run(false)
        }else{
            isPremiumCar(Data.getUserName() ?: return, runnablePar)
        }
    }

    private fun isPremiumCar(plateNum: String, runnablePar: RunnablePar){
        getCarObject(plateNum, object : RunnablePar{
            override fun run(p: Any?) {
                Thread{
                    try{
                        val currMs = Date().networkDateMillis
                        val carObject = p as MyCar?
                        val isPremium = (carObject?.premiumDate ?: 0) > currMs
                        runnablePar.run(isPremium)
                    }catch (ex: Exception){
                        runnablePar.run(false)
                        return@Thread
                    }
                }.start()
            }
        })

    }

    fun doesCarExist(plateNum: String, runnablePar: RunnablePar){
        getCarObjectReference(plateNum)?.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                runnablePar.run(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                runnablePar.run(false)
            }
        })
    }

    fun areCredentialsCorrect(plateNum: String?, password: String?, runnablePar: RunnablePar){
        if(plateNum.isNullOrEmpty() || password.isNullOrEmpty()) {
            runnablePar.run(false)
            return
        }

        getCarObject(plateNum, object: RunnablePar{
            override fun run(p: Any?) {
                val carObject = p as MyCar?
                runnablePar.run(carObject?.password == password)
            }
        })
    }

    fun promoteToPremium(days: Long, callback: Runnable = Runnable {}) {
        val currMs = System.currentTimeMillis()
        val daysInMs: Long = (1000 * 60 * 60 * 24) * days * 30
        val nextDeadline = currMs + daysInMs

        getPremiumDateReference()?.setValue(nextDeadline)?.addOnCompleteListener {
            callback.run()
        }
    }

    fun updateCarLocation(location: Location){
        getCarLocationReference()?.setValue(location)
    }

    fun updateCarBrand(brand: String){
        getCarBrandReference()?.setValue(brand)
    }

    fun updateStartTime(time: Long){
        getStartReference()?.setValue(time)
    }

    fun updateLiveTime(time: Long){
        getTimeReference()?.setValue(time)
    }

    fun updateServerIp(ip: String){
        getServerIpReference()?.setValue(ip)
    }

    fun updatePin(ip: String){
        getPinReference()?.setValue(ip)
    }

    fun getCarLocationReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(LOCATION_REF)
    }

    fun getCarBrandReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(CAR_BRAND_REF)
    }

    fun getTimeReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(TIME_REF)
    }

    fun getStartReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(START_REF)
    }

    fun getServerIpReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(SERVER_IP_REF)
    }

    fun getPasswordReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(PASSWORD_REF)
    }

    fun getPinReference(): DatabaseReference? {
        return getCarObjectReference(Data.getUserName() ?: return null)?.child(PIN_REF)
    }

    fun getPremiumDateReference(): DatabaseReference? {
        return try{
            getCarObjectReference(Data.getUserName() ?: return null)?.child(PREMIUM_DATE_REF)
        }catch (ex: Exception){
            return null
        }
    }

    fun getCarObjectReference(plateNum: String): DatabaseReference? {
        return try{
            FirebaseDatabase.getInstance(DB_REF).getReference(plateNum)
        }catch (ex: java.lang.Exception){
            null
        }
    }

    fun getCarObject(plateNum: String, runnablePar: RunnablePar){
        getCarObjectReference(plateNum.uppercase())?.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                runnablePar.run(snapshot.getValue(MyCar::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                runnablePar.run(null)
            }
        })
    }

    fun addCarObject(car: MyCar, callback: Runnable = Runnable {}){
        getCarObjectReference(car.plateNum.uppercase())?.setValue(car)?.addOnCompleteListener { callback.run() }
    }

    fun deleteField(referString: String, callback: Runnable = Runnable {}){
        FirebaseDatabase.getInstance(DB_REF).getReference(referString).setValue(null).addOnCompleteListener {
            callback.run()
        }
    }

    fun getSpecificField(referString: String, path: String): DatabaseReference {
        return FirebaseDatabase.getInstance(referString).getReference(path)
    }
}