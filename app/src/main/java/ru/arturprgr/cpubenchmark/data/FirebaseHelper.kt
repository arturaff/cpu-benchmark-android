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
    }
}