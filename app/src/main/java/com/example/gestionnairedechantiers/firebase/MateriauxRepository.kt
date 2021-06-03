package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.Materiaux
import com.example.gestionnairedechantiers.entities.MaterielLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MateriauxRepository {

    private val db = FirebaseFirestore.getInstance().collection("materiaux")

    suspend fun insertMateriaux(materiaux: Materiaux): String? {
        return try {
            val test = db.add(materiaux).await()
            Timber.i("Materiaux envoyé Firebase")
            test.id
        } catch (e: Exception) {
            Timber.e("Error insert Materiaux Firebase")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    suspend fun getAllMateriaux(): List<Materiaux> {

        val list = mutableListOf<Materiaux>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (materiaux in result) {
            Timber.i("$materiaux")
            list.add(materiaux.toObject(Materiaux::class.java))
        }
        return list
    }

    suspend fun updateMateriaux(materiaux: Materiaux) {
        try {
            db.document(materiaux.documentId!!)
                .set(materiaux)
                .await()
            Timber.i("Materiaux Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update Materiaux Firebase: $e")
        }
    }

    suspend fun getMateriauxById(id: String): Materiaux? {
        return db.document(id).get().await()
            .toObject(Materiaux::class.java)
    }


    suspend fun deleteMateriaux(materiaux: Materiaux): Boolean {
        return try {
            db.document(materiaux.documentId!!).delete().await()
            Timber.i("MaterielLocation envoyé Firebase")
            true
        } catch (e: Exception) {
            Timber.e("Error insert MaterielLocation Firebase")
            //            FirebaseCrashlytics.getInstance().log("Error getting user details")
            //            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
            //            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }


}