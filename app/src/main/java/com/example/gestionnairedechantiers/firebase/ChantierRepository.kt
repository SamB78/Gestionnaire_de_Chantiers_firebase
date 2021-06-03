package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.Chantier
import com.example.gestionnairedechantiers.entities.Chantier.Companion.toChantierWithoutPersonnel
import com.example.gestionnairedechantiers.entities.Personnel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ChantierRepository {

    private val db = FirebaseFirestore.getInstance().collection("chantiers")
    private val personnelRepository = PersonnelRepository()

    suspend fun insertChantier(chantier: Chantier) {
        try {
            db.document(chantier.numeroChantier!!)
                .set(chantier)
                .await()
            Timber.i("chantier envoyé Firebase")
        } catch (e: Exception) {
            Timber.e("Error insert chantier Firebase")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun setChantier(chantier: Chantier) {
        try {
            Timber.i("Entree setChantier")
            Timber.i("chantier: $chantier")

            val listEquipe = mutableListOf<String>()

            for (personnel in chantier.listEquipe) {
                listEquipe.add(personnel.documentId!!)
            }

            val data = hashMapOf(
                "adresseChantier" to chantier.adresseChantier,
                "chefChantier" to chantier.chefChantier.documentId,
                "identiteResponsableSite" to chantier.identiteResponsableSite,
                "mailContactResponsableSite" to chantier.mailContactResponsableSite,
                "nomChantier" to chantier.nomChantier,
                "numContactResponsableSite" to chantier.numContactResponsableSite,
                "typeChantier" to chantier.typeChantier,
                "urlPictureChantier" to chantier.urlPictureChantier,
                "listEquipe" to listEquipe
            )
            Timber.i("date: $data")

            db.document(chantier.numeroChantier!!)
                .set(data)
                .await()
            Timber.i("Materiel Updated with success")
        } catch (e: Exception) {
            Timber.e("Error update Materiel Firebase: $e")
        }


    }

    suspend fun getAllChantiers(): List<Chantier> {

        val list = mutableListOf<Chantier>()
        val result = db.get()
            .await()
        Timber.i("Result:")
        for (chantier in result) {
//            val idChefChantier = chantier.get("chefChantier") as String
//            val chefChantier = dbPersonnel.document(idChefChantier).get().await()
//                .toObject(Personnel::class.java) ?: Personnel()
            list.add(chantier.toChantierWithoutPersonnel())
//            chantier2.chefChantier = chefChantier
        }

        return list
    }

    suspend fun getChantierById(id: String): Chantier? {
        try {
            val result = db.document(id).get().await()
            if (result != null) {

                val listIdsPersonnel = result.get("listEquipe") as List<*>
                val listPersonnel = mutableListOf<Personnel>()

                for (idPersonnel in listIdsPersonnel) {
                    listPersonnel.add(
                        personnelRepository.getPersonnelById(idPersonnel as String) ?: Personnel()
                    )
                }

                val idChefChantier = result.get("chefChantier") as String
                val chefChantier = personnelRepository.getPersonnelById(idChefChantier) ?: Personnel()

                val chantier = result.toChantierWithoutPersonnel()
                chantier.listEquipe = listPersonnel
                chantier.chefChantier = chefChantier
                return chantier
            }
        } catch (e: java.lang.Exception) {
            Timber.e("Error get Materiel by id $id Firebase: $e")
        }
        return null
    }
}