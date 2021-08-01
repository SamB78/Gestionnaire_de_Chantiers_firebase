package com.techphone78.gestionnairedechantiers.entities

data class Adresse(
    var rue: String = "",
    var codePostal: String = "",
    var ville: String = "",
    var pays: String = "France"
) {
    fun adresseToString(): String = "$rue $ville"
}