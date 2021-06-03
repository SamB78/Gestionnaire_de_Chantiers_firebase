package com.example.gestionnairedechantiers.firebase

import com.example.gestionnairedechantiers.entities.*
import com.example.gestionnairedechantiers.entities.RapportChantier.Companion.toRapportChantier
import com.example.gestionnairedechantiers.entities.RapportChantier.Companion.toRapportChantierWithoutAllData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant
import java.util.*


class RapportChantierRepository(idChantier: String) {

    private val db = FirebaseFirestore.getInstance()
        .collection("chantiers").document(idChantier)
        .collection("rapportsChantier")

    private val personnelRepository = PersonnelRepository()
    private val materielRepository = MaterielRepository()
    private val materielLocationRepository = MaterielLocationRepository()
    private val materiauxRepository = MateriauxRepository()
    private val sousTraitanceRepository = SousTraitanceRepository()
    private val tacheEntretienRepository = TacheEntretienRepository()


    suspend fun insertRapportChantier(rapportChantier: RapportChantier): String? {
        try {

            val listPersonnel = mutableListOf<ItemWithQuantity>()
            for (item in rapportChantier.listePersonnel) listPersonnel.add(
                ItemWithQuantity(
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
                "meteo" to rapportChantier.meteo
            )

            val result = db.add(data).await()
            return result.id
        } catch (e: Exception) {
            Timber.e("Error insert rapportChantier Firebase $e")
            return null
        }
    }

    suspend fun getAllRapportsChantier(): List<RapportChantier> {
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

    suspend fun getRapportChantierById(id: String): RapportChantier {
        try {
            val result = db.document(id).get().await()

            if (result != null) {

                return result.toRapportChantier(
                    personnelRepository,
                    materielRepository,
                    materielLocationRepository,
                    materiauxRepository,
                    sousTraitanceRepository,
                    tacheEntretienRepository
                ) ?: RapportChantier()
            }
        } catch (e: java.lang.Exception) {
            Timber.e("Error get RapportChantier by id $id Firebase: $e")
        }
        return RapportChantier()
    }

    suspend fun getListRapportsChantierByListOfDates(listDates: List<Date>): List<RapportChantier> {

        val listOfRC = mutableListOf<RapportChantier>()

        for (date in listDates) {
            val result = db.whereEqualTo("dateRapportChantier", date).get().await()
            for (document in result) {
                listOfRC.add(
                    document.toRapportChantier(
                        personnelRepository,
                        materielRepository,
                        materielLocationRepository,
                        materiauxRepository,
                        sousTraitanceRepository,
                        tacheEntretienRepository
                    ) ?: RapportChantier()
                )
            }
        }
        return listOfRC
    }

    suspend fun getRapportChantierIdByDate(date: Date): String? {

        val result = db.whereEqualTo("dateRapportChantier", date).get().await()
        Timber.i("result = ${result.documents}")
        return if (result.documents.isNotEmpty()) {
            result.first().id
        } else {
            null
        }
    }

    fun updateListePersonnelRC(rapportChantier: RapportChantier): Boolean {
        val listPersonnel = mutableListOf<ItemWithQuantity>()
        for (item in rapportChantier.listePersonnel) listPersonnel.add(
            ItemWithQuantity(
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

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }
    }

    fun updateListeMaterielRC(rapportChantier: RapportChantier): Boolean {

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

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }


    }

    fun updateListeMaterielLocationRC(rapportChantier: RapportChantier): Boolean {
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

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }
    }

    fun updateListeMateriauxRC(rapportChantier: RapportChantier): Boolean {
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

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }
    }

    fun updateListeSousTraitanceRC(rapportChantier: RapportChantier): Boolean {
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

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }
    }

    fun updateListeAutresInformations(rapportChantier: RapportChantier): Boolean {
        val updates = hashMapOf(
            "observations" to rapportChantier.observations,
            "dataSaved.dataConformiteChantier" to true
        )

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }
    }

    fun updateListeObservations(rapportChantier: RapportChantier): Boolean {

        Timber.i("listeTachesEntretien2 = ${rapportChantier.tachesEntretien}")

        val updates = hashMapOf(
            "commentaire" to rapportChantier.commentaire,
            "meteo" to rapportChantier.meteo,
            "traitementPhytosanitaire" to rapportChantier.traitementPhytosanitaire,
            "tachesEntretien" to rapportChantier.tachesEntretien,
            "dataSaved.dataObservations" to true
        )

        return try {
            db.document(rapportChantier.documentId!!).update(updates)
            true

        } catch (e: Exception) {
            Timber.e("Error update RapportChantier data : $e")
            false
        }
    }
}

data class ItemWithQuantity(var id: String, var nb: Int)
data class ItemWithQuantity2(var id: String, var nb: Long)


data class ListItemWithQuantity(var list: List<ItemWithQuantity>)