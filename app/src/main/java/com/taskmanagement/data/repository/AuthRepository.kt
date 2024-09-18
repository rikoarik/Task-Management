package com.taskmanagement.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.taskmanagement.data.model.User
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun saveUserDataToDatabase(userId: String, name: String, email: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val user = User(userId, name, email)

        database.child("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    suspend fun getCurrentUserData(): Result<User?> {
        val currentUser = getCurrentUser() ?: return Result.success(null)

        val database = FirebaseDatabase.getInstance().reference
        return suspendCoroutine { continuation ->
            database.child("users").child(currentUser.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    val user = snapshot?.getValue(User::class.java)
                    continuation.resume(Result.success(user))
                } else {
                    continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                }
            }
        }
    }

}
