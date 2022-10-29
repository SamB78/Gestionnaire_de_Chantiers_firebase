package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GestionRapportChantierViewModelFactory(
    private val idRapportChantier: String?,
    private val idChantier: String,
    private val dateRapportChantier: Long
) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionRapportChantierViewModel::class.java)) {
            return GestionRapportChantierViewModel(
                idRapportChantier,
                idChantier,
                dateRapportChantier
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}