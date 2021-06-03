package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.SousTraitance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SousTraitanceRepository {

    private val db = FirebaseFirestore.getInstance().collection("sousTraitance")

    suspend fun insertSousTraitance(sousTraitance: SousTraitance): String? {
       return try {
            val test = db.add(sousTraitance).await()
            Timber.i("SousTraitance envoyé Firebase")
           test.id
        } catch (e: Exception) {
            Timber.e("Error insert SousTraitance Firebase")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
           null
        }
    }

    suspend fun getAllSousTraitance(): List<SousTraitance> {

        val list = mutableListOf<SousTraitance>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (sousTraitance in result) {
            Timber.i("$sousTraitance")
            list.add(sousTraitance.toObject(SousTraitance::class.java))
        }
        return list
    }

    suspend fun updateSousTraitance(sousTraitance: SousTraitance) {
        try {
            db.document(sousTraitance.documentId!!)
                .set(sousTraitance)
                .await()
            Timber.i("SousTraitance Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update SousTraitance Firebase: $e")
        }
    }

    suspend fun getSousTraitanceById(id: String): SousTraitance? {
        return db.document(id).get().await()
            .toObject(SousTraitance::class.java)
    }

    suspend fun deleteSousTraitance(item: SousTraitance): Boolean {
        return try {
            db.document(item.documentId!!).delete().await()
            Timber.i("SousTratiance envoyé Firebase")
            true
        } catch (e: Exception) {
            Timber.e("Error insert SousTraitance Firebase")
            //            FirebaseCrashlytics.getInstance().log("Error getting user details")
            //            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
            //            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }
}