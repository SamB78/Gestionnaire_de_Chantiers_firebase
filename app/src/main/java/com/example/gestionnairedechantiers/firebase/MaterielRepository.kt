package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.Materiel
import com.example.gestionnairedechantiers.entities.Personnel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MaterielRepository {

    private val db = FirebaseFirestore.getInstance().collection("materiel")

    suspend fun insertMateriel(materiel: Materiel) {
        try {
            db.add(materiel).await()
            Timber.i("Materiel envoyé Firebase")
        } catch (e: Exception) {
            Timber.e("Error insert Materiel Firebase")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun getAllMateriel(): List<Materiel> {

        val list = mutableListOf<Materiel>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (materiel in result) {
            Timber.i("$materiel")
            list.add(materiel.toObject(Materiel::class.java))
        }
        return list
    }

    suspend fun updateMateriel(materiel: Materiel) {
        try {
            db.document(materiel.documentId!!)
                .set(materiel)
                .await()
            Timber.i("Materiel Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update Materiel Firebase: $e")
        }
    }

    suspend fun getMaterielById(id: String): Materiel? {
        return db.document(id).get().await()
            .toObject(Materiel::class.java)
    }


}