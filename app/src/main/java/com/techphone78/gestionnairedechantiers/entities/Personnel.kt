package com.techphone78.gestionnairedechantiers.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Personnel(
    @DocumentId
    val documentId: String? = null,
    var nom: String = "",
    var prenom: String = "",
    var numContact: String = "",
    var mail: String = "",
    var administrateur: Boolean = false,
    var chefEquipe: Boolean = false,
    var interimaire: Boolean = false,
    var enService: Boolean = true,
    var urlPicturePersonnel: String? = null,
    @Exclude @set:Exclude @get:Exclude
    var isChecked: Boolean = false,
    @Exclude @set:Exclude @get:Exclude
    var nbHeuresTravaillees: Double = 0.0
)