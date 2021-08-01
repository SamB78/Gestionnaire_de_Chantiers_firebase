package com.techphone78.gestionnairedechantiers.entities

import com.google.firebase.firestore.DocumentId

data class Couleur(
    @DocumentId
    val colorName: String? = null,
    val colorCode: String? = null
)
