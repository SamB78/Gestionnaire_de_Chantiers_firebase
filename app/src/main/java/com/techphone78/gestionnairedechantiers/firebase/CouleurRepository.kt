package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Couleur
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class CouleurRepository {

    private val db = FirebaseFirestore.getInstance().collection("colors")

    suspend fun getAllColors(): List<Couleur> {

        val list = mutableListOf<Couleur>()
        val result = db.get()
            .await()
        for (color in result) {
            list.add(color.toObject(Couleur::class.java))
        }
        Timber.i("list = $list")
        return list
    }

    suspend fun getCouleurById(id: String): Couleur? {
        return db.document(id).get().await().toObject(Couleur::class.java)
    }
}