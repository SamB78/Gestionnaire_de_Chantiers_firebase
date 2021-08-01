package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.techphone78.gestionnairedechantiers.entities.User
import com.techphone78.gestionnairedechantiers.entities.User.Companion.toUser
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
    private val personnelRepository = PersonnelRepository()


    suspend fun authWithGoogle(authCredential: AuthCredential): Boolean {
        return try {
            firebaseAuth.signInWithCredential(authCredential).await()
            true
        } catch (e: Exception) {
            Timber.e("Error connection: $e")
            false
        }
    }

    suspend fun getDataUser(): User {

        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null) {

            val mail = firebaseUser.email
            var personnel: Personnel? = null
            var user: User? = null

            if (!mail.isNullOrEmpty()) {
                val userResult = dbUsers.document(mail).get().await()
                if(userResult.exists()){
                    user = userResult.toUser(personnelRepository)
                }else{
                    throw Exception("Accès refusé: vous n'avez pas la permission de vous connecter")
                }
                Timber.i("user = $user")
            } else {
                throw Exception("Erreur de connexion Google")
            }

//            if (user != null) {
//
//                val documents = dbPersonnel.whereEqualTo("mailContact", mail).get().await()
//                if (documents != null) {
//                    for (document in documents) {
//                        personnel = document.toObject(Personnel::class.java)
//                        Timber.i("$personnel")
//                    }
//
//                    user.userData = personnel
//
//
//                    return user
//                } else {
//                    throw java.lang.Exception("Erreur dans la base de données, contactez l'administrateur")
//                }
//            } else {
//                Timber.e("Personnel not found in database")
//                throw Exception("Vous n'êtes pas enregistré dans la base de données")
//            }
            return user
        } else {
            throw Exception("User not connected")
        }
    }


}