package com.mini.infotainment.firebase
/*

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.mini.infotainment.entities.Car
import com.mini.infotainment.support.RunnablePar
import com.mini.infotainment.utility.Utility

object FirebaseClass{
    private var DB_REF = "https://strade-sicure-default-rtdb.europe-west1.firebasedatabase.app"

    val carRef: DatabaseReference
        get() {
            return FirebaseDatabase.getInstance(DB_REF).reference.child("Anomalies")
        }

    fun getSpecificField(referString: String, path: String): DatabaseReference {
        return FirebaseDatabase.getInstance(referString).getReference(path)
    }

    fun addCarToFirebase(car: Car){
        if(!Utility.isInternetAvailable()) return

        carRef.push().setValue(car)
    }

    fun deleteCarFromFirebase(car: Car){
        if(!Utility.isInternetAvailable()) return

        getCarSnapshot(car, object : RunnablePar {
            override fun run(p: Any?) {
                deleteFieldFirebase(carRef, (p as DataSnapshot?)?.ref?.key)
            }
        })
    }

    fun updateCarLocation(car: Car, callback: Runnable?){
        getCarSnapshotId(car.targa, object : RunnablePar{
            override fun run(p: Any?) {
                val key = p as String

                carRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChild(key)) {
                            carRef.child(key)
                                .child("location")
                                .setValue(car.location)
                                .addOnCompleteListener {
                                    callback?.run()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        })
    }

    fun getCarObjectById(targa: String, callback: RunnablePar) {
        if(!Utility.isInternetAvailable()) return

        getCarSnapshotId(targa, object : RunnablePar {
            override fun run(p: Any?) {
                val snapshot = p as DataSnapshot?
                callback.run(snapshot?.getValue(Car::class.java))
            }
        })
    }

    fun getCarSnapshotId(targa: String, callback: RunnablePar){
        carRef.get().addOnCompleteListener { dataSnapshot ->
            val carFound = dataSnapshot.result?.children?.find {
                it.child("targa").value.toString().equals(targa, ignoreCase = true)
            }
            if(carFound != null)
                callback.run(carFound)
        }
    }

    fun getCarSnapshot(car: Car, callback: RunnablePar){
        if(!Utility.isInternetAvailable()) return

        carRef.get().addOnCompleteListener { dataSnapshot ->
            callback.run(dataSnapshot.result?.children?.find{
                it.getValue(Car::class.java)!!.location == car.location
            })
        }
    }

    fun deleteFieldFirebase(reference: DatabaseReference, child: String?) {
        if(child != null)
            reference.child(child).removeValue()
    }

}
*/