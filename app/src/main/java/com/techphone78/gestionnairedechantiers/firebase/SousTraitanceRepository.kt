package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.SousTraitance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SousTraitanceRepository {

    private val db = FirebaseFirestore.getInstance().collection("sousTraitance")

    suspend fun insertSousTraitance(sousTraitance: SousTraitance): String {

        val test = db.add(sousTraitance).await()
        Timber.i("SousTraitance envoyé Firebase")
        return test.id

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
            db.document(sousTraitance.documentId!!)
                .set(sousTraitance)
                .await()
            Timber.i("SousTraitance Updated with success")
    }

    suspend fun getSousTraitanceById(id: String): SousTraitance? {
        return db.document(id).get().await()
            .toObject(SousTraitance::class.java)
    }

    suspend fun deleteSousTraitance(item: SousTraitance) {

        db.document(item.documentId!!).delete().await()
        Timber.i("SousTratiance envoyé Firebase")
    }
}