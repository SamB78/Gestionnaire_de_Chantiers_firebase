package com.techphone78.gestionnairedechantiers.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.techphone78.gestionnairedechantiers.entities.MaterielLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MaterielLocationRepository {

    private val db = FirebaseFirestore.getInstance().collection("materielLocation")

    suspend fun insertMaterielLocation(materielLocation: MaterielLocation): String {

        val test = db.add(materielLocation).await()
        Timber.i("MaterielLocation envoyé Firebase")
        return test.id
    }

    suspend fun deleteMaterielLocation(materielLocation: MaterielLocation) {
        db.document(materielLocation.documentId!!).delete().await()
        Timber.i("MaterielLocation envoyé Firebase")
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
        db.document(materielLocation.documentId!!)
            .set(materielLocation)
            .await()
        Timber.i("MaterielLocation Updated with success")

    }

    suspend fun getMaterielLocationById(id: String): MaterielLocation? {
        return db.document(id).get().await()
            .toObject(MaterielLocation::class.java)
    }
}