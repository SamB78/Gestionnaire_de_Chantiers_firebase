package com.techphone78.gestionnairedechantiers.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.QueryDocumentSnapshot
import timber.log.Timber
import java.lang.Exception


data class Chantier(


    @DocumentId
    var numeroChantier: String? = null,
    var nomChantier: String = "",
    var adresseUnique: Boolean = true,
    var adresseChantier: Adresse = Adresse(),
    var urlPictureChantier: String? = null,
    var identiteResponsableSite: String = "",
    var numContactResponsableSite: String = "",
    var mailContactResponsableSite: String = "",
    var accessCode: String? = null,
    @Exclude @set:Exclude @get:Exclude
    var chefChantier: Personnel = Personnel(),
    var typeChantier: Int = 1, // 1 = Chantier 2 = Entretien
    var enService: Boolean = true,
    @Exclude @set:Exclude @get:Exclude
    var listEquipe: List<Personnel> = emptyList(),
    @Exclude @set:Exclude @get:Exclude
    var couleur: Couleur? = null,
    @Exclude @set:Exclude @get:Exclude
    var listRapportChantiers: List<RapportChantier>? = null


) {
    companion object {
        fun QueryDocumentSnapshot.toChantierWithoutPersonnel(): Chantier {
                return Chantier(
                    numeroChantier = id,
                    nomChantier = getString("nomChantier")!!,
                    adresseUnique = getBoolean("adresseUnique")?: true,
                    adresseChantier = get("adresseChantier", Adresse::class.java)!!,
                    urlPictureChantier = getString("urlPictureChantier"),
                    identiteResponsableSite = getString("identiteResponsableSite")!!,
                    numContactResponsableSite = getString("numContactResponsableSite")!!,
                    mailContactResponsableSite = getString("mailContactResponsableSite")!!,
                    typeChantier = getLong("typeChantier")!!.toInt(),
                    accessCode = getString("accessCode"),
                    enService = getBoolean("enService")?: true

                )
        }

        fun DocumentSnapshot.toChantierWithoutPersonnel(): Chantier {
                return Chantier(
                    numeroChantier = id,
                    nomChantier = getString("nomChantier")!!,
                    adresseUnique = getBoolean("adresseUnique")?: true,
                    adresseChantier = get("adresseChantier", Adresse::class.java)!!,
                    urlPictureChantier = getString("urlPictureChantier"),
                    identiteResponsableSite = getString("identiteResponsableSite")!!,
                    numContactResponsableSite = getString("numContactResponsableSite")!!,
                    mailContactResponsableSite = getString("mailContactResponsableSite")!!,
                    typeChantier = getLong("typeChantier")!!.toInt(),
                    accessCode = getString("accessCode"),
                    enService = getBoolean("enService")?: true

                )
        }
    }
}

