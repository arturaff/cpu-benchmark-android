package ru.arturprgr.cpubenchmark.data

import com.google.firebase.Firebase
import com.google.firebase.database.database

class FirebaseHelper(path: String) {
    private val reference = Firebase.database.getReference(path)

    fun setValue(value: Any) = reference.setValue(value)

    fun getValue(onGet: (value: String) -> Unit) {
        reference.get().addOnSuccessListener {
            onGet("${it.value}")
        }
//        кусок прошлой версии
//        reference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                onGet("${snapshot.value}")
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
    }
}