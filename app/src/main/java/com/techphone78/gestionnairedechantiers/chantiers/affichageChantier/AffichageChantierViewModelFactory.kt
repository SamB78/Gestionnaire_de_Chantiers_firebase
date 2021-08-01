package com.techphone78.gestionnairedechantiers.chantiers.affichageChantier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AffichageChantierViewModelFactory(

    private val chantierId: String

) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AffichageChantierViewModel::class.java)) {
            return AffichageChantierViewModel(
                chantierId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}