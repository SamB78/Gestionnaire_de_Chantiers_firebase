package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Chantier
import com.techphone78.gestionnairedechantiers.entities.Chantier.Companion.toChantierWithoutPersonnel
import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import com.techphone78.gestionnairedechantiers.firebase.ImagesStorage.Companion.CHANTIER_FOLDER
import timber.log.Timber

class ChantierRepository {

    private val db = FirebaseFirestore.getInstance().collection("chantiers")
    private val personnelRepository = PersonnelRepository()
    private val colorsRepository = CouleurRepository()


    private val imagesStorage = ImagesStorage()
    private val authRepository = AuthRepository()

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

            chantier.urlPictureChantier?.let {
                chantier.urlPictureChantier = imagesStorage.insertImage(it, CHANTIER_FOLDER)
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
                "listEquipe" to listEquipe,
                "couleur" to chantier.couleur?.colorName,
                "accessCode" to chantier.accessCode
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

        val user = authRepository.getDataUser()
        val list = mutableListOf<Chantier>()
        val result: QuerySnapshot = if (user.userData!!.administrateur) {
            db.get().await()
        } else {
            db.whereEqualTo("chefChantier", user.userData!!.documentId).get().await()
        }
        val colors = colorsRepository.getAllColors()
        for (chantier in result) {
//            val idChefChantier = chantier.get("chefChantier") as String
//            val chefChantier = dbPersonnel.document(idChefChantier).get().await()
//                .toObject(Personnel::class.java) ?: Personnel()

            val idCouleur = chantier.get("couleur") as String?
            val couleur = colors.find { it.colorName == idCouleur }
            val chantierConvertedToObject = chantier.toChantierWithoutPersonnel()
            chantierConvertedToObject.couleur = couleur
            list.add(chantierConvertedToObject)

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
                val idCouleur = result.get("couleur") as String?
                val chefChantier =
                    personnelRepository.getPersonnelById(idChefChantier) ?: Personnel()


                val chantier = result.toChantierWithoutPersonnel()
                chantier.listEquipe = listPersonnel
                chantier.chefChantier = chefChantier
                idCouleur?.let {
                    chantier.couleur = colorsRepository.getCouleurById(idCouleur)
                }

                return chantier
            }
        } catch (e: java.lang.Exception) {
            Timber.e("Error get Materiel by id $id Firebase: $e")
        }
        return null
    }
}