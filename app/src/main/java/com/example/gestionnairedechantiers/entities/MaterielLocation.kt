package com.example.gestionnairedechantiers.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class MaterielLocation(
    @DocumentId
    var documentId: String? = null,
    var fournisseur: String = "",
    var description: String ="",
    var numBonCommande: String = "",
    @Exclude @set:Exclude @get:Exclude
    var quantite: Int = 1
)