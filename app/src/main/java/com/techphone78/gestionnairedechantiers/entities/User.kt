package com.techphone78.gestionnairedechantiers.entities

import com.techphone78.gestionnairedechantiers.firebase.PersonnelRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.Exception

data class User(
//    var uid: String,
    var mail: String = "",
    var userData: Personnel? = null,
) {
    companion object {
        suspend fun DocumentSnapshot.toUser(personnelRepository: PersonnelRepository): User {

            val userId = getString("userData")

            if (!userId.isNullOrEmpty()) {
                val userData = personnelRepository.getPersonnelById(userId)
                if(userData != null) {
                    return User(
                        mail = id,
                        userData = userData
                    )
                }else {
                    throw Exception("Erreur base de données, consultez l'administrateur: aucun UserId fourni")
                }
            } else {
                throw Exception("Erreur: votre compte n'est pas enregistré dans la base de données, consultez l'administrateur ")
            }
        }
    }
}