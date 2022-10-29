package com.techphone78.gestionnairedechantiers.chantiers.gestionChantiers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GestionChantierViewModelFactory(
    private val idPersonnel: String? = null
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(GestionChantierViewModel::class.java)) {
            return  GestionChantierViewModel(
                idPersonnel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}