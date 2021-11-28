package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.techphone78.gestionnairedechantiers.firebase.ImagesStorage.Companion.PERSONNEL_FOLDER
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.techphone78.gestionnairedechantiers.utils.FireStoreResponse
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class PersonnelRepository {
    private val db = FirebaseFirestore.getInstance()
    private val personnelRef = FirebaseFirestore.getInstance().collection("personnel")

    private val userRef = FirebaseFirestore.getInstance().collection("users")

    private val storage = Firebase.storage
    val storageRef = storage.reference

    private val imagesStorage = ImagesStorage()


    suspend fun insertPersonnel(personnel: Personnel) {

        val batch = db.batch()

            personnel.urlPicturePersonnel?.let {
                personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)
            }
            val generatedId = personnelRef.document().id

            batch.set(personnelRef.document(generatedId), personnel)


            if (personnel.mail.isNotEmpty()) {

                val dataUser = hashMapOf(
                    "mail" to personnel.mail,
                    "userData" to generatedId
                )
                batch.set(userRef.document(personnel.mail), dataUser)
            }

            batch.commit()


        }

    suspend fun getAllPersonnel(getServerData: Boolean = false): FireStoreResponse<List<Personnel>> {

        val list = mutableListOf<Personnel>()
        Timber.i("Entering getAllPersonnel")
        var result = personnelRef.get(if (getServerData) Source.SERVER else Source.DEFAULT)
            .await()
        Timber.i("result: ${result.isEmpty} outside")
        if (result.isEmpty && getServerData) {
            Timber.i("result: ${result.isEmpty} inside")
            result = personnelRef.get()
                .await()
        }
        for (personnel in result) {
            list.add(personnel.toObject(Personnel::class.java))
        }
        return FireStoreResponse(list, result.metadata.isFromCache)
    }


    suspend fun getAllAddablePersonnel(): FireStoreResponse<List<Personnel>> {

        val list = mutableListOf<Personnel>()
        val result = personnelRef.whereEqualTo("enService", true).get()
            .await()
        Timber.i("Result:")
        for (personnel in result) {
            list.add(personnel.toObject(Personnel::class.java))
        }
        return FireStoreResponse(list, result.metadata.isFromCache)

    }

    suspend fun updatePersonnel(personnel: Personnel) {

        val batch = db.batch();

            val oldPersonnel = getPersonnelById(personnel.documentId!!)
            if (oldPersonnel!!.mail !== personnel.mail && oldPersonnel!!.mail.isNotEmpty()) {
                batch.delete(userRef.document(oldPersonnel.mail))
            }
            personnel.urlPicturePersonnel?.let {
                personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)
            }

            batch.set(personnelRef.document(personnel.documentId), personnel)

            val dataUser = hashMapOf(
                "mail" to personnel.mail,
                "userData" to personnel.documentId
            )

            if (personnel.mail.isNotEmpty()) {
                batch.set(userRef.document(personnel.mail), dataUser)
            }

            batch.commit().await()

            Timber.i("Personnel Updated with success")
        }

    suspend fun getPersonnelById(id: String): Personnel? {
        return personnelRef.document(id).get().await()
            .toObject(Personnel::class.java)
    }

}

//suspend fun updatePersonnel3(personnel: Personnel) {
//
//    try {
//        val oldPersonnel = getPersonnelById(personnel.documentId!!)
//        if (oldPersonnel!!.mail !== personnel.mail) {
//
//        }
//        personnel.urlPicturePersonnel?.let {
//            personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)
//        }
//
//        personnelRef.document(personnel.documentId)
//            .set(personnel)
//            .await()
//
//        val dataUser = hashMapOf(
//            "mail" to personnel.mail,
//            "userData" to personnel.documentId
//        )
//
//        if (personnel.mail.isNotEmpty()) {
//            userRef.document(personnel.mail).set(dataUser).await()
//        }
//
//        Timber.i("Personnel Updated with success")
//    } catch (e: Exception) {
//        Timber.e("Error update Personnel Firebase: $e")
//    }
//}
