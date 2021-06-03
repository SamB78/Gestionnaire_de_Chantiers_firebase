package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.Personnel
import com.example.gestionnairedechantiers.entities.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber


class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val rootRef = FirebaseFirestore.getInstance()
    private val dbPersonnel = FirebaseFirestore.getInstance().collection("personnel")
    private val dbUsers = FirebaseFirestore.getInstance().collection("users")

    suspend fun authWithGoogle(authCredential: AuthCredential): Boolean {
        return try {
            firebaseAuth.signInWithCredential(authCredential).await()
            true
        } catch (e: Exception) {
            Timber.e("Error connection: $e")
            false
        }
    }

    suspend fun getDataUser(): User? {

        val firebaseUser = firebaseAuth.currentUser



        if (firebaseUser != null) {


            val mail = firebaseUser.email
            var personnel: Personnel? = null

            val documents = dbPersonnel.whereEqualTo("mailContact", mail).get().await()
            return if (documents != null) {
                for (document in documents) {
                    personnel = document.toObject(Personnel::class.java)
                    Timber.i("$personnel")
                }

                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email!!,
                    userData = personnel
                )

                val dataUser = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "userData" to personnel!!.documentId
                )

                dbUsers.document(user.uid).set(dataUser).await()

                user
            } else {
                Timber.e("Personnel not found in database")
                null
            }
        } else {
            Timber.e("User not connected")
            return null
        }
    }
}