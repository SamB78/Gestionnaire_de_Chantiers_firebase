package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.Materiel
import com.example.gestionnairedechantiers.firebase.ImagesStorage.Companion.MATERIEL_FOLDER
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MaterielRepository {

    private val db = FirebaseFirestore.getInstance().collection("materiel")
    private val imagesStorage = ImagesStorage()
    private val colorsRepository = CouleurRepository()

    suspend fun insertMateriel(materiel: Materiel) {
        try {

            materiel.urlPictureMateriel?.let {
                materiel.urlPictureMateriel = imagesStorage.insertImage(it, MATERIEL_FOLDER)
            }


            val data = hashMapOf(
                "marque" to materiel.marque,
                "modele" to materiel.modele,
                "numeroSerie" to materiel.numeroSerie,
                "type" to materiel.type,
                "enService" to materiel.enService,
                "materielEntretien" to materiel.materielEntretien,
                "materielChantier" to materiel.materielChantier,
                "miseEnCirculation" to materiel.miseEnCirculation,
                "urlPictureMateriel" to materiel.urlPictureMateriel,
                "materielUnique" to materiel.materielUnique,
                "couleur" to materiel.couleur?.colorName
            )

            db.add(data).await()
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
        val colors = colorsRepository.getAllColors()
        val result = db.get()
            .await()
        for (materiel in result) {
            val idCouleur = materiel.get("couleur") as String?
            val couleur = colors.find { it.colorName == idCouleur }
            val materielConvertToObject = materiel.toObject(Materiel::class.java)
            materielConvertToObject.couleur = couleur
            list.add(materielConvertToObject)
        }
        return list
    }

    suspend fun updateMateriel(materiel: Materiel) {
        try {

            materiel.urlPictureMateriel?.let {
                materiel.urlPictureMateriel = imagesStorage.insertImage(it, MATERIEL_FOLDER)
            }

            val data = hashMapOf(
                "marque" to materiel.marque,
                "modele" to materiel.modele,
                "numeroSerie" to materiel.numeroSerie,
                "type" to materiel.type,
                "enService" to materiel.enService,
                "materielEntretien" to materiel.materielEntretien,
                "materielChantier" to materiel.materielChantier,
                "miseEnCirculation" to materiel.miseEnCirculation,
                "urlPictureMateriel" to materiel.urlPictureMateriel,
                "materielUnique" to materiel.materielUnique,
                "couleur" to materiel.couleur?.colorName
            )

            db.document(materiel.documentId!!)
                .set(data)
                .await()
            Timber.i("Materiel Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update Materiel Firebase: $e")
        }
    }

    suspend fun getMaterielById(id: String): Materiel? {
        val result = db.document(id).get().await()
        if (result != null) {
            val materiel = result.toObject(Materiel::class.java)

            val idCouleur = result.get("couleur") as String?
            idCouleur?.let {
                materiel?.couleur = colorsRepository.getCouleurById(idCouleur)
            }
            return materiel
        }
        return null

    }


}