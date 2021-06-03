package com.example.gestionnairedechantiers.entities

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
    var adresseChantier: Adresse = Adresse(),
    var urlPictureChantier: String? = null,
    var identiteResponsableSite: String = "",
    var numContactResponsableSite: String = "",
    var mailContactResponsableSite: String = "",
    @Exclude @set:Exclude @get:Exclude
    var chefChantier: Personnel = Personnel(),
    var typeChantier: Int = 1, // 1 = Chantier 2 = Entretien
    @Exclude @set:Exclude @get:Exclude
    var listEquipe: List<Personnel> = emptyList()

){
    companion object {
        fun QueryDocumentSnapshot.toChantierWithoutPersonnel(): Chantier {
            try {
                return Chantier(
                    numeroChantier = id,
                    nomChantier = getString("nomChantier")!!,
                    adresseChantier = get("adresseChantier", Adresse::class.java)!!,
                    urlPictureChantier = getString("urlPictureChantier"),
                    identiteResponsableSite = getString("identiteResponsableSite")!!,
                    numContactResponsableSite = getString("numContactResponsableSite")!!,
                    mailContactResponsableSite = getString("mailContactResponsableSite")!!,
                    typeChantier = getLong("typeChantier")!!.toInt()

                )

            } catch (e: Exception) {
                Timber.e("Error: $e")
                return Chantier()
            }
        }

        fun DocumentSnapshot.toChantierWithoutPersonnel(): Chantier {
            try {
                return Chantier(
                    numeroChantier = id,
                    nomChantier = getString("nomChantier")!!,
                    adresseChantier = get("adresseChantier", Adresse::class.java)!!,
                    urlPictureChantier = getString("urlPictureChantier"),
                    identiteResponsableSite = getString("identiteResponsableSite")!!,
                    numContactResponsableSite = getString("numContactResponsableSite")!!,
                    mailContactResponsableSite = getString("mailContactResponsableSite")!!,
                    typeChantier = getLong("typeChantier")!!.toInt()

                )

            } catch (e: Exception) {
                Timber.e("Error: $e")
                return Chantier()
            }
        }
    }
}

