package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.Personnel
import com.example.gestionnairedechantiers.firebase.ImagesStorage.Companion.PERSONNEL_FOLDER
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class PersonnelRepository {
    private val db = FirebaseFirestore.getInstance().collection("personnel")

    private val storage = Firebase.storage
    val storageRef = storage.reference

    private val imagesStorage = ImagesStorage()


    suspend fun insertPersonnel(personnel: Personnel) {

        try {
            personnel.urlPicturePersonnel.let {
                personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)
            }

            db.add(personnel).await()
        } catch (e: Exception) {
            Timber.e("Error insert Personnel Firebase")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

//    private suspend fun insertImage(personnel: Personnel): String? {
//        var url: String? = null
//        if (!personnel.urlPicturePersonnel.isNullOrEmpty()) {
//            val uri2 = (personnel.urlPicturePersonnel)
//            val string = uri2!!.substring(uri2.lastIndexOf('/') + 1)
//            val personnelStorageRef = storageRef.child("personnel_pictures/${string}")
//
//            personnelStorageRef.putFile(Uri.fromFile(File(personnel.urlPicturePersonnel!!)))
//                .continueWithTask { task ->
//                    if (!task.isSuccessful) {
//                        task.exception?.let {
//                            Timber.i("exception = $it")
//                            throw it
//
//                        }
//                    }
//                    personnelStorageRef.downloadUrl
//                }.addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val downloadUri = task.result
//
//                        url = downloadUri.toString()
//                    } else {
//                        Timber.i("error ${task.exception}")
//                    }
//                }.await()
//        }
//        return url
//    }

    suspend fun getAllPersonnel(): List<Personnel> {

        val list = mutableListOf<Personnel>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (personnel in result) {
            list.add(personnel.toObject(Personnel::class.java))
        }
        return list
    }

    suspend fun updatePersonnel(personnel: Personnel) {
        try {

            personnel.urlPicturePersonnel.let {
                personnel.urlPicturePersonnel = imagesStorage.insertImage(it, PERSONNEL_FOLDER)
            }

            db.document(personnel.documentId!!)
                .set(personnel)
                .await()
            Timber.i("Personnel Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update Personnel Firebase: $e")
        }
    }

    suspend fun getPersonnelById(id: String): Personnel? {
        return db.document(id).get().await()
            .toObject(Personnel::class.java)
    }
}