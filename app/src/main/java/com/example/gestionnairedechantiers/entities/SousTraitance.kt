package com.example.gestionnairedechantiers.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class SousTraitance(
    @DocumentId
    var documentId: String? = null,
    var societe: String = "",
    var prestations: String = "",
    var numeroDevis: String = "",
    @Exclude @set:Exclude @get:Exclude
    var quantite: Int = 1
)