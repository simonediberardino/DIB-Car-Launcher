
import android.location.Location
import com.google.firebase.database.*
import com.mini.infotainment.support.RunnablePar


object FirebaseClass{
    private var DB_REF = "https://infotainment-8c303-default-rtdb.europe-west1.firebasedatabase.app/"
    private var LOCATION_REF = "location"
    private var SERVER_IP_REF = "serverip"

    val databaseReference: DatabaseReference
        get() {
            return FirebaseDatabase.getInstance(DB_REF).reference
        }

    fun updateCarLocation(location: Location){
        getCarLocationReference().setValue(location)
    }

    fun getCarLocation(callback: RunnablePar){
        getCarLocationReference().addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.run(snapshot.getValue(Location::class.java))
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun updateServerIp(ip: String){
        getServerIpReference().setValue(ip)
    }

    fun getServerIP(callback: RunnablePar){
        getServerIpReference().addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.run(snapshot.getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getCarLocationReference(): DatabaseReference {
        return getSpecificField(DB_REF, LOCATION_REF)
    }

    fun getServerIpReference(): DatabaseReference {
        return getSpecificField(DB_REF, SERVER_IP_REF)
    }

    fun getSpecificField(referString: String, path: String): DatabaseReference {
        return FirebaseDatabase.getInstance(referString).getReference(path)
    }

    fun deleteFieldFirebase(reference: DatabaseReference, child: String?) {
        if(child != null)
            reference.child(child).removeValue()
    }

}