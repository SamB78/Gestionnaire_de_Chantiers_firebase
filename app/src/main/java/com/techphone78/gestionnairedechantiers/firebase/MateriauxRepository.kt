package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Materiaux
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MateriauxRepository {

    private val db = FirebaseFirestore.getInstance().collection("materiaux")

    suspend fun insertMateriaux(materiaux: Materiaux): String {
        val test = db.add(materiaux).await()
        Timber.i("Materiaux envoyé Firebase")
        return test.id
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
        db.document(materiaux.documentId!!)
            .set(materiaux)
            .await()
        Timber.i("Materiaux Updated with success")
    }

    suspend fun getMateriauxById(id: String): Materiaux? {
        return db.document(id).get().await()
            .toObject(Materiaux::class.java)
    }


    suspend fun deleteMateriaux(materiaux: Materiaux) {

        db.document(materiaux.documentId!!).delete().await()
        Timber.i("MaterielLocation envoyé Firebase")
    }


}