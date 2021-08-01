package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.MaterielLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MaterielLocationRepository {

    private val db = FirebaseFirestore.getInstance().collection("materielLocation")

    suspend fun insertMaterielLocation(materielLocation: MaterielLocation): String? {
        return try {
            val test = db.add(materielLocation).await()
            Timber.i("MaterielLocation envoyé Firebase")
            test.id
        } catch (e: Exception) {
            Timber.e("Error insert MaterielLocation Firebase")
    //            FirebaseCrashlytics.getInstance().log("Error getting user details")
    //            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
    //            FirebaseCrashlytics.getInstance().recordException(e)
             null
        }
    }

    suspend fun deleteMaterielLocation(materielLocation: MaterielLocation): Boolean {
        return try {
            db.document(materielLocation.documentId!!).delete().await()
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

    suspend fun getAllMaterielLocation(): List<MaterielLocation> {

        val list = mutableListOf<MaterielLocation>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (materielLocation in result) {
            Timber.i("$materielLocation")
            list.add(materielLocation.toObject(MaterielLocation::class.java))
        }
        return list
    }

    suspend fun updateMaterielLocation(materielLocation: MaterielLocation) {
        try {
            db.document(materielLocation.documentId!!)
                .set(materielLocation)
                .await()
            Timber.i("MaterielLocation Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update MaterielLocation Firebase: $e")
        }
    }

    suspend fun getMaterielLocationById(id: String): MaterielLocation? {
        return db.document(id).get().await()
            .toObject(MaterielLocation::class.java)
    }
}