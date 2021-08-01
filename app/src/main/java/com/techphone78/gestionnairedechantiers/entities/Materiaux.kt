package com.techphone78.gestionnairedechantiers.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Materiaux(
    @DocumentId
    var documentId: String? = null,
    var fournisseur: String = "",
    var description: String = "",
    var nDeBon: String = "",
    @Exclude @set:Exclude @get:Exclude
    var quantite: Int = 0
) {
}