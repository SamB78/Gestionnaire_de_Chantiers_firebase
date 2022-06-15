package com.techphone78.gestionnairedechantiers.firebase

import com.techphone78.gestionnairedechantiers.entities.*
import com.techphone78.gestionnairedechantiers.entities.RapportChantier.Companion.toRapportChantier
import com.techphone78.gestionnairedechantiers.entities.RapportChantier.Companion.toRapportChantierWithoutAllData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*


class RapportChantierRepository() {


    private val personnelRepository = PersonnelRepository()
    private val materielRepository = MaterielRepository()
    private val materielLocationRepository = MaterielLocationRepository()
    private val materiauxRepository = MateriauxRepository()
    private val sousTraitanceRepository = SousTraitanceRepository()
    private val tacheEntretienRepository = TacheEntretienRepository()


    suspend fun insertRapportChantier(
        rapportChantier: RapportChantier,
        idChantier: String
    ): String? {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        try {

            val listPersonnel = mutableListOf<ItemWithQuantity3>()
            for (item in rapportChantier.listePersonnel) listPersonnel.add(
                ItemWithQuantity3(
                    item.documentId!!,
                    item.nbHeuresTravaillees
                )
            )

            val listMateriel = mutableListOf<ItemWithQuantity>()
            for (item in rapportChantier.listeMateriel) listMateriel.add(
                ItemWithQuantity(
                    item.documentId!!,
                    item.quantite
                )
            )

            val listMaterielLocation = mutableListOf<ItemWithQuantity>()
            for (item in rapportChantier.listeMaterielLocation) listMaterielLocation.add(
                ItemWithQuantity(
                    item.documentId!!,
                    item.quantite
                )
            )

            val listMateriaux = mutableListOf<ItemWithQuantity>()
            for (item in rapportChantier.listeMateriaux) listMateriaux.add(
                ItemWithQuantity(
                    item.documentId!!,
                    item.quantite
                )
            )

            val listSousTraitance = mutableListOf<ItemWithQuantity>()
            for (item in rapportChantier.listeSousTraitance) listSousTraitance.add(
                ItemWithQuantity(
                    item.documentId!!,
                    item.quantite
                )
            )

            val data = hashMapOf(
                "chantierId" to rapportChantier.chantierId,
                "chefChantier" to rapportChantier.chefChantier.documentId,
                "dateRapportChantier" to Date.from(rapportChantier.dateRapportChantier),
                "listePersonnel" to listPersonnel,
                "listeMateriel" to listMateriel,
                "listeMaterielLocation" to listMaterielLocation,
                "listeMateriaux" to listMateriaux,
                "listeSousTraitance" to listSousTraitance,
                "observations" to rapportChantier.observations,
                "commentaire" to rapportChantier.commentaire,
                "typeChantier" to rapportChantier.typeChantier,
                "dataSaved" to rapportChantier.dataSaved,
                "traitementPhytosanitaire" to rapportChantier.traitementPhytosanitaire,
                "tachesEntretien" to rapportChantier.tachesEntretien,
                "totauxRC" to rapportChantier.totauxRC,
                "meteo" to rapportChantier.meteo,
                "adresseChantier" to rapportChantier.adresseChantier
            )

            val result = db.add(data).await()
            return result.id
        } catch (e: Exception) {
            Timber.e("Error insert rapportChantier Firebase $e")
            return null
        }
    }

    suspend fun getAllRapportsChantier(idChantier: String): List<RapportChantier> {
        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")
        val list = mutableListOf<RapportChantier>()
        val result = db.orderBy("dateRapportChantier", Query.Direction.DESCENDING).get().await()
        for (item in result) {
            val rapportChantier = item.toRapportChantierWithoutAllData()
            rapportChantier.chefChantier =
                personnelRepository.getPersonnelById(item.get("chefChantier") as String)
                    ?: Personnel()
            list.add(rapportChantier)
        }
        return list
    }

    suspend fun getRapportChantierById(id: String, idChantier: String): RapportChantier {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val result = db.document(id).get().await()
        result
        return result.toRapportChantier(
            personnelRepository,
            materielRepository,
            materielLocationRepository,
            materiauxRepository,
            sousTraitanceRepository,
            tacheEntretienRepository
        )
    }

    suspend fun getListRapportsChantierByListOfDates(
        listDates: List<Date>,
        idChantier: String
    ): List<RapportChantier> {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val listOfRC = mutableListOf<RapportChantier>()

        for (date in listDates) {
            val calendarBegin = Calendar.getInstance()
            calendarBegin.time = date
            /*calendarBegin.add(Calendar.HOUR_OF_DAY, 1)*/

            val calendarEnd = Calendar.getInstance()
            calendarEnd.time = date
            calendarEnd.add(Calendar.HOUR_OF_DAY, 22)

            Timber.i("TEST12: ${calendarBegin.time}, ${calendarEnd.time}")

            val result = db
                /*.whereEqualTo("dateRapportChantier", calendar.time)*/
                .whereGreaterThanOrEqualTo("dateRapportChantier", calendarBegin.time)
                .whereLessThanOrEqualTo("dateRapportChantier", calendarEnd.time)
                .get()
                .await()
            Timber.i("result: ${result.documents.size} for date : ${calendarBegin.time}")
            for (document in result) {
                Timber.i("document: ${document.data}")
                listOfRC.add(
                    document.toRapportChantier(
                        personnelRepository,
                        materielRepository,
                        materielLocationRepository,
                        materiauxRepository,
                        sousTraitanceRepository,
                        tacheEntretienRepository
                    )
                )
            }
        }
        return listOfRC
    }

    suspend fun getWeeklyListRapportsChantierByListOfDates(
        date: Date,
        idChantier: String
    ): List<RapportChantier> {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val listOfRC = mutableListOf<RapportChantier>()


        val calendarBegin = Calendar.getInstance()
        calendarBegin.time = date

        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = date
        calendarEnd.add(Calendar.HOUR_OF_DAY, 22)
        calendarEnd.add(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)

        Timber.i("TEST12: ${calendarBegin.time}, ${calendarEnd.time}")

        val result = db
            /*.whereEqualTo("dateRapportChantier", calendar.time)*/
            .whereGreaterThanOrEqualTo("dateRapportChantier", calendarBegin.time)
            .whereLessThanOrEqualTo("dateRapportChantier", calendarEnd.time)
            .get()
            .await()
        Timber.i("result: ${result.documents.size} for date : ${calendarBegin.time}")
        for (document in result) {
            Timber.i("document: ${document.data}")
            listOfRC.add(
                document.toRapportChantier(
                    personnelRepository,
                    materielRepository,
                    materielLocationRepository,
                    materiauxRepository,
                    sousTraitanceRepository,
                    tacheEntretienRepository
                )
            )
        }

        return listOfRC
    }

    suspend fun getRapportChantierIdByDate(date: Date, idChantier: String): String? {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val result = db.whereEqualTo("dateRapportChantier", date).get().await()
        Timber.i("result = ${result.documents}")
        return if (result.documents.isNotEmpty()) {
            result.first().id
        } else {
            null
        }
    }

    suspend fun updateListePersonnelRC(rapportChantier: RapportChantier, idChantier: String) {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val listPersonnel = mutableListOf<ItemWithQuantity3>()
        for (item in rapportChantier.listePersonnel) listPersonnel.add(
            ItemWithQuantity3(
                item.documentId!!,
                item.nbHeuresTravaillees
            )
        )
        val updates = hashMapOf<String, Any>(
            "listePersonnel" to listPersonnel,
            "totauxRC.totalMO" to rapportChantier.totauxRC.totalMO,
            "totauxRC.totalMOInterimaire" to rapportChantier.totauxRC.totalMOInterimaire,
            "totauxRC.totalMOPersonnel" to rapportChantier.totauxRC.totalMOPersonnel,
            "dataSaved.dataPersonnel" to true
        )
        db.document(rapportChantier.documentId!!).update(updates).await()
    }

    suspend fun updateListeMaterielRC(rapportChantier: RapportChantier, idChantier: String) {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val listMateriel = mutableListOf<ItemWithQuantity>()
        for (item in rapportChantier.listeMateriel) listMateriel.add(
            ItemWithQuantity(
                item.documentId!!,
                item.quantite
            )
        )
        val updates = hashMapOf<String, Any>(
            "listeMateriel" to listMateriel,
            "totauxRC.totalQuantiteMaterielSociete" to rapportChantier.totauxRC.totalQuantiteMaterielSociete,
            "totauxRC.totalQuantiteMateriel" to rapportChantier.totauxRC.totalQuantiteMateriel,
            "dataSaved.dataMateriel" to true
        )
        db.document(rapportChantier.documentId!!).update(updates).await()

    }

    fun updateListeMaterielLocationRC(rapportChantier: RapportChantier, idChantier: String) {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val listItems = mutableListOf<ItemWithQuantity>()
        for (item in rapportChantier.listeMaterielLocation) listItems.add(
            ItemWithQuantity(
                item.documentId!!,
                item.quantite
            )
        )
        val updates = hashMapOf<String, Any>(
            "listeMaterielLocation" to listItems,
            "totauxRC.totalQuantiteMaterielLocation" to rapportChantier.totauxRC.totalQuantiteMaterielLocation,
            "totauxRC.totalQuantiteMateriel" to rapportChantier.totauxRC.totalQuantiteMateriel,
            "dataSaved.dataMaterielLocation" to true
        )
        db.document(rapportChantier.documentId!!).update(updates)
    }

    fun updateListeMateriauxRC(rapportChantier: RapportChantier, idChantier: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val listItems = mutableListOf<ItemWithQuantity>()
        for (item in rapportChantier.listeMateriaux) listItems.add(
            ItemWithQuantity(
                item.documentId!!,
                item.quantite
            )
        )
        val updates = hashMapOf<String, Any>(
            "listeMateriaux" to listItems,
            "totauxRC.totalMateriaux" to rapportChantier.totauxRC.totalMateriaux,
            "dataSaved.dataMateriaux" to true
        )
        db.document(rapportChantier.documentId!!).update(updates)
    }

    fun updateListeSousTraitanceRC(rapportChantier: RapportChantier, idChantier: String) {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")


        val listItems = mutableListOf<ItemWithQuantity>()
        for (item in rapportChantier.listeSousTraitance) listItems.add(
            ItemWithQuantity(
                item.documentId!!,
                item.quantite
            )
        )
        val updates = hashMapOf<String, Any>(
            "listeSousTraitance" to listItems,
            "totauxRC.totalSousTraitance" to rapportChantier.totauxRC.totalSousTraitance,
            "dataSaved.dataSousTraitance" to true
        )
        db.document(rapportChantier.documentId!!).update(updates)

    }

    fun updateListeAutresInformations(rapportChantier: RapportChantier, idChantier: String) {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")

        val updates = hashMapOf(
            "observations" to rapportChantier.observations,
            "dataSaved.dataConformiteChantier" to true
        )
        db.document(rapportChantier.documentId!!).update(updates)
    }

    fun updateListeObservations(rapportChantier: RapportChantier, idChantier: String) {

        val db = FirebaseFirestore.getInstance()
            .collection("chantiers").document(idChantier)
            .collection("rapportsChantier")


        Timber.i("adresseChantier = ${rapportChantier.adresseChantier}")

        val updates = hashMapOf(
            "commentaire" to rapportChantier.commentaire,
            "meteo" to rapportChantier.meteo,
            "traitementPhytosanitaire" to rapportChantier.traitementPhytosanitaire,
            "tachesEntretien" to rapportChantier.tachesEntretien,
            "dataSaved.dataObservations" to true,
            "adresseChantier" to rapportChantier.adresseChantier
        )
        db.document(rapportChantier.documentId!!).update(updates)
    }
}

data class ItemWithQuantity(var id: String, var nb: Int)
data class ItemWithQuantity2(var id: String, var nb: Long)
data class ItemWithQuantity3(var id: String, var nb: Double)