package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.techphone78.gestionnairedechantiers.firebase.ImagesStorage.Companion.PERSONNEL_FOLDER
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class PersonnelRepository {
    private val personnelRef = FirebaseFirestore.getInstance().collection("personnel")

    private val userRef = FirebaseFirestore.getInstance().collection("users")

    private val storage = Firebase.storage
    val storageRef = storage.reference

    private val imagesStorage = ImagesStorage()


    suspend fun insertPersonnel(personnel: Personnel) {

        val batch = FirebaseFirestore.getInstance().batch()

        try {
            personnel.urlPicturePersonnel?.let {
                personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)

            }

            val result = personnelRef.add(personnel).await()

            val dataUser = hashMapOf(
                "mail" to personnel.mail,
                "userData" to result.id
            )
            userRef.document(personnel.mail).set(dataUser).await()

        } catch (e: Exception) {
            Timber.e("Error insert Personnel Firebase: $e")
            throw java.lang.Exception("Erreur integration image: $e")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun getAllPersonnel(): List<Personnel> {

        val list = mutableListOf<Personnel>()
        val result = personnelRef.get()
            .await()
        Timber.i("Result:")
        for (personnel in result) {
            list.add(personnel.toObject(Personnel::class.java))
        }
        return list
    }

    suspend fun updatePersonnel(personnel: Personnel) {
        try {

            personnel.urlPicturePersonnel?.let {
                personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)
            }

            personnelRef.document(personnel.documentId!!)
                .set(personnel)
                .await()
            Timber.i("Personnel Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update Personnel Firebase: $e")
        }
    }

    suspend fun getPersonnelById(id: String): Personnel? {
        return personnelRef.document(id).get().await()
            .toObject(Personnel::class.java)
    }
}