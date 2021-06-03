package com.example.gestionnairedechantiers.entities


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.util.*


data class Materiel(
    @DocumentId
    val documentId: String? = null,
    var marque: String = "",
    var modele: String = "",
    var numeroSerie: String = "",
    var type: String = "",
    var enService: Boolean = true,
    var materielEntretien: Boolean = true,
    var materielChantier: Boolean = false,
    var miseEnCirculation: Date? = null,
    var urlPictureMateriel: String? = null,
    var materielUnique: Boolean = true,
    @Exclude @set:Exclude @get:Exclude
    var isChecked: Boolean = false,
    @Exclude @set:Exclude @get:Exclude
    var quantite: Int = 1

)