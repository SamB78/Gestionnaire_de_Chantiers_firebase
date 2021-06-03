package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.TacheEntretien
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class TacheEntretienRepository {

    private val db = FirebaseFirestore.getInstance().collection("tacheEntretien")

    suspend fun getAllTacheEntretien(): List<TacheEntretien> {

        val list = mutableListOf<TacheEntretien>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (tacheEntretien in result) {
            Timber.i("$tacheEntretien")
            list.add(tacheEntretien.toObject(TacheEntretien::class.java))
        }
        return list
    }
}