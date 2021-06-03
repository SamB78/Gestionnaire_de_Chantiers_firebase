package com.example.gestionnairedechantiers.firebase

import android.net.Uri
import com.example.gestionnairedechantiers.entities.Personnel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

class ImagesStorage {

    private val storage = Firebase.storage
    val storageRef = storage.reference


    suspend fun insertImage(pathFile: String?, folder: String): String? {
        var url: String? = null
        if (!pathFile.isNullOrEmpty()) {
            val uri2 = (pathFile)
            val string = uri2.substring(uri2.lastIndexOf('/') + 1)
            val personnelStorageRef = storageRef.child("${folder}/${string}")

            personnelStorageRef.putFile(Uri.fromFile(File(pathFile)))
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            Timber.i("exception = $it")
                            throw it
                        }
                    }
                    personnelStorageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        url = downloadUri.toString()
                    } else {
                        Timber.i("error ${task.exception}")
                    }
                }.await()
        }
        return url
    }


    companion object {
        const val PERSONNEL_FOLDER = "personnel_pictures"
        const val MATERIEL_FOLDER = "materiel_pictures"
        const val CHANTIER_FOLDER = "chantier_pictures"
    }
}