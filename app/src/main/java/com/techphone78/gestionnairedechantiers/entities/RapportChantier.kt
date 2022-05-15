package com.techphone78.gestionnairedechantiers.entities

import com.techphone78.gestionnairedechantiers.firebase.*
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.QueryDocumentSnapshot
import timber.log.Timber
import java.time.Instant
import java.util.*

data class RapportChantier(
    @DocumentId
    var documentId: String? = null,
    var chantierId: String? = null,
    @Exclude @set:Exclude @get:Exclude
    var chefChantier: Personnel = Personnel(),
    var dateRapportChantier: Instant? = null,
    @Exclude @set:Exclude @get:Exclude
    var listePersonnel: List<Personnel> = emptyList(),
    @Exclude @set:Exclude @get:Exclude
    var listeMateriel: List<Materiel> = emptyList(),
    @Exclude @set:Exclude @get:Exclude
    var listeMaterielLocation: List<MaterielLocation> = emptyList(),
    @Exclude @set:Exclude @get:Exclude
    var listeMateriaux: List<Materiaux> = emptyList(),
    @Exclude @set:Exclude @get:Exclude
    var listeSousTraitance: List<SousTraitance> = emptyList(),
    var observations: Observations = Observations(),
    var commentaire: String = "",
    var typeChantier: Int = 1, // 1 = Chantier 2 = Entretien
    var dataSaved: DataSaved = DataSaved(),
    var traitementPhytosanitaire: TraitementPhytosanitaire = TraitementPhytosanitaire(),
    var tachesEntretien: List<TacheEntretien> = emptyList(),
    var totauxRC: TotauxRapportChantier = TotauxRapportChantier(),
    var meteo: Meteo = Meteo(),
    var adresseChantier: Adresse = Adresse()
) {

    companion object {
        fun QueryDocumentSnapshot.toRapportChantierWithoutAllData(): RapportChantier {

            return RapportChantier(
                documentId = id,
                chantierId = getString("chantierId")!!,
                dateRapportChantier = getDate("dateRapportChantier")!!.toInstant(),

                observations = get("observations", Observations::class.java) ?: Observations(),
                commentaire = getString("commentaire")!!,
                typeChantier = getLong("typeChantier")!!.toInt(),
                traitementPhytosanitaire = get(
                    "traitementPhytosanitaire",
                    TraitementPhytosanitaire::class.java
                )!!,
                totauxRC = get("totauxRC", TotauxRapportChantier::class.java)!!,
                meteo = get("meteo", Meteo::class.java)!!,
                adresseChantier = get("adresseChantier", Adresse::class.java)?: Adresse(),
            )
        }

        fun DocumentSnapshot.toRapportChantierWithoutAllData(): RapportChantier {
            return RapportChantier(
                documentId = id,
                chantierId = getString("chantierId")!!,
                dateRapportChantier = getDate("dateRapportChantier")!!.toInstant(),
                observations = get("observations", Observations::class.java) ?: Observations(),
                commentaire = getString("commentaire")!!,
                typeChantier = getLong("typeChantier")!!.toInt(),
                traitementPhytosanitaire = get(
                    "traitementPhytosanitaire",
                    TraitementPhytosanitaire::class.java
                )!!,
                totauxRC = get("totauxRC", TotauxRapportChantier::class.java)!!,
                meteo = get("meteo", Meteo::class.java)!!,
                dataSaved = get("dataSaved", DataSaved::class.java)!!,
                adresseChantier = get("adresseChantier", Adresse::class.java)?: Adresse(),
            )
        }

        suspend fun DocumentSnapshot.toRapportChantier(
            personnelRepository: PersonnelRepository,
            materielRepository: MaterielRepository,
            materielLocationRepository: MaterielLocationRepository,
            materiauxRepository: MateriauxRepository,
            sousTraitanceRepository: SousTraitanceRepository,
            tacheEntretienRepository: TacheEntretienRepository
        ): RapportChantier {
            val listPersonnel = mutableListOf<Personnel>()
            for (item in get("listePersonnel") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity3(item["id"] as String, item["nb"].toString().toDouble())
                personnelRepository.getPersonnelById(resultItem.id)?.let {
                    it.nbHeuresTravaillees = resultItem.nb
                    listPersonnel.add(it)
                }
            }


            val listMateriel = mutableListOf<Materiel>()
            for (item in get("listeMateriel") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                materielRepository.getMaterielById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listMateriel.add(it)
                }
            }

            val listMaterielLocation = mutableListOf<MaterielLocation>()
            for (item in get("listeMaterielLocation") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                materielLocationRepository.getMaterielLocationById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listMaterielLocation.add(it)
                }
            }

            val listMateriaux = mutableListOf<Materiaux>()
            for (item in get("listeMateriaux") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                materiauxRepository.getMateriauxById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listMateriaux.add(it)
                }
            }

            val listSousTraitance = mutableListOf<SousTraitance>()
            for (item in get("listeSousTraitance") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                sousTraitanceRepository.getSousTraitanceById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listSousTraitance.add(it)
                }
            }

            val listeOriginale = tacheEntretienRepository.getAllTacheEntretien()
            val listeTachesEntretien = mutableListOf<TacheEntretien>()

            for (item in get("tachesEntretien") as List<*>) {
                item as HashMap<*, *>
                val resultItem = TacheEntretien(
                    item["description"] as String,
                    item["type"] as String,
                    item["checked"] as Boolean
                )
                listeTachesEntretien.add(resultItem)
            }

            for (tacheEntretien in listeOriginale) {
                if (listeTachesEntretien.find { it.type == tacheEntretien.type } == null) {
                    listeTachesEntretien.add(tacheEntretien)
                }
            }

            return RapportChantier(
                documentId = id,
                chantierId = getString("chantierId")!!,
                dateRapportChantier = getDate("dateRapportChantier")!!.toInstant(),
                listePersonnel = listPersonnel,
                listeMateriel = listMateriel,
                listeMaterielLocation = listMaterielLocation,
                listeMateriaux = listMateriaux,
                listeSousTraitance = listSousTraitance,
                observations = get("observations", Observations::class.java) ?: Observations(),
                commentaire = getString("commentaire")!!,
                typeChantier = getLong("typeChantier")!!.toInt(),
                traitementPhytosanitaire = get(
                    "traitementPhytosanitaire",
                    TraitementPhytosanitaire::class.java
                )!!,
                tachesEntretien = listeTachesEntretien,
                totauxRC = get("totauxRC", TotauxRapportChantier::class.java)!!,
                meteo = get("meteo", Meteo::class.java)!!,
                dataSaved = get("dataSaved", DataSaved::class.java)!!,
                adresseChantier = get("adresseChantier", Adresse::class.java)?: Adresse(),
            )

        }

        suspend fun QueryDocumentSnapshot.toRapportChantier(
            personnelRepository: PersonnelRepository,
            materielRepository: MaterielRepository,
            materielLocationRepository: MaterielLocationRepository,
            materiauxRepository: MateriauxRepository,
            sousTraitanceRepository: SousTraitanceRepository,
            tacheEntretienRepository: TacheEntretienRepository
        ): RapportChantier {

            val listPersonnel = mutableListOf<Personnel>()
            for (item in get("listePersonnel") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity3(item["id"] as String, item["nb"] as Double)
                personnelRepository.getPersonnelById(resultItem.id)?.let {
                    it.nbHeuresTravaillees = resultItem.nb
                    listPersonnel.add(it)
                }
            }


            val listMateriel = mutableListOf<Materiel>()
            for (item in get("listeMateriel") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                materielRepository.getMaterielById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listMateriel.add(it)
                }
            }

            val listMaterielLocation = mutableListOf<MaterielLocation>()
            for (item in get("listeMaterielLocation") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                materielLocationRepository.getMaterielLocationById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listMaterielLocation.add(it)
                }
            }

            val listMateriaux = mutableListOf<Materiaux>()
            for (item in get("listeMateriaux") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                materiauxRepository.getMateriauxById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listMateriaux.add(it)
                }
            }

            val listSousTraitance = mutableListOf<SousTraitance>()
            for (item in get("listeSousTraitance") as List<*>) {
                item as HashMap<*, *>
                val resultItem = ItemWithQuantity2(item["id"] as String, item["nb"] as Long)
                sousTraitanceRepository.getSousTraitanceById(resultItem.id)?.let {
                    it.quantite = resultItem.nb.toInt()
                    listSousTraitance.add(it)
                }
            }

            val listeOriginale = tacheEntretienRepository.getAllTacheEntretien()
            val listeTachesEntretien = mutableListOf<TacheEntretien>()

            for (item in get("tachesEntretien") as List<*>) {
                item as HashMap<*, *>
                val resultItem = TacheEntretien(
                    item["description"] as String,
                    item["type"] as String,
                    item["checked"] as Boolean
                )
                listeTachesEntretien.add(resultItem)
            }

            for (tacheEntretien in listeOriginale) {
                if (listeTachesEntretien.find { it.type == tacheEntretien.type } == null) {
                    listeTachesEntretien.add(tacheEntretien)
                }
            }

            return RapportChantier(
                documentId = id,
                chantierId = getString("chantierId")!!,
                dateRapportChantier = getDate("dateRapportChantier")!!.toInstant(),
                listePersonnel = listPersonnel,
                listeMateriel = listMateriel,
                listeMaterielLocation = listMaterielLocation,
                listeMateriaux = listMateriaux,
                listeSousTraitance = listSousTraitance,
                observations = get("observations", Observations::class.java) ?: Observations(),
                commentaire = getString("commentaire")!!,
                typeChantier = getLong("typeChantier")!!.toInt(),
                traitementPhytosanitaire = get(
                    "traitementPhytosanitaire",
                    TraitementPhytosanitaire::class.java
                )!!,
                tachesEntretien = listeTachesEntretien,
                totauxRC = get("totauxRC", TotauxRapportChantier::class.java)!!,
                meteo = get("meteo", Meteo::class.java)!!,
                dataSaved = get("dataSaved", DataSaved::class.java)!!,
                adresseChantier = get("adresseChantier", Adresse::class.java)?: Adresse(),
            )


        }
    }

}


data class DataSaved(
    var dataPersonnel: Boolean = false,
    var dataMateriel: Boolean = false,
    var dataMaterielLocation: Boolean = false,
    var dataMateriaux: Boolean = false,
    var dataSousTraitance: Boolean = false,
    var dataConformiteChantier: Boolean = false,
    var dataObservations: Boolean = false
)

data class TraitementPhytosanitaire(

    var nomsOperateurs: String = "",
    var nomProduit: String = "",
    var surfaceTraite: String = "",
    var qteProduit: String = "",
    var pulverisateurMule: Boolean = false,
    var pulverisateurDos: Boolean = false,
    var tracteurCuve: Boolean = false
)

data class TacheEntretien(
    var description: String = "",
    var type: String = "",
    var checked: Boolean = false

)

data class TotauxRapportChantier(
    var totalMOPersonnel: Double = 0.0,
    var totalMOInterimaire: Double = 0.0,
    var totalMO: Double = 0.0,
    var totalQuantiteMaterielSociete: Int = 0,
    var totalQuantiteMaterielLocation: Int = 0,
    var totalQuantiteMateriel: Int = 0,
    var totalMateriaux: Int = 0,
    var totalSousTraitance: Int = 0,
    var totalRapportChantier: Int = 0
)

data class Meteo(
    var soleil: Boolean = false,
    var pluie: Boolean = false,
    var vent: Boolean = false,
    var gel: Boolean = false,
    var neige: Boolean = false
)


