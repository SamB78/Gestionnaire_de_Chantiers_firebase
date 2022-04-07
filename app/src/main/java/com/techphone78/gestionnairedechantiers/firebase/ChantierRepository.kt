package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.Chantier
import com.techphone78.gestionnairedechantiers.entities.Chantier.Companion.toChantierWithoutPersonnel
import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import com.techphone78.gestionnairedechantiers.firebase.ImagesStorage.Companion.CHANTIER_FOLDER
import com.techphone78.gestionnairedechantiers.utils.FireStoreResponse
import timber.log.Timber

class ChantierRepository {

    private val db = FirebaseFirestore.getInstance().collection("chantiers")
    private val personnelRepository = PersonnelRepository()
    private val colorsRepository = CouleurRepository()


    private val imagesStorage = ImagesStorage()
    private val authRepository = AuthRepository()

    suspend fun insertChantier(chantier: Chantier) {
        db.document(chantier.numeroChantier!!)
            .set(chantier)
            .await()
        Timber.i("chantier envoyé Firebase")
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)

    }

    suspend fun setChantier(chantier: Chantier) {

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
            "adresseUnique" to chantier.adresseUnique,
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
    }

    suspend fun getAllChantiers(): FireStoreResponse<List<Chantier>> {

//        val user = authRepository.getDataUser()
        val list = mutableListOf<Chantier>()
//        val result: QuerySnapshot = if (user.userData!!.administrateur) {
//            db.get().await()
//        } else {
//            db.whereEqualTo("chefChantier", user.userData!!.documentId).get().await()
//        }

        val result = db.get().await()
        val colors = colorsRepository.getAllColors()
        for (chantier in result) {
           val idChefChantier = chantier.get("chefChantier") as String
           val chefChantier = Personnel(documentId = idChefChantier)


            val idCouleur = chantier.get("couleur") as String?
            val couleur = colors.find { it.colorName == idCouleur }
            val chantierConvertedToObject = chantier.toChantierWithoutPersonnel()
            chantierConvertedToObject.couleur = couleur
            chantierConvertedToObject.chefChantier = chefChantier
            list.add(chantierConvertedToObject)

        }

        return FireStoreResponse(list, result.metadata.isFromCache)
    }

    suspend fun getChantierById(id: String): Chantier {
        val result = db.document(id).get().await()
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
}