package com.example.gestionnairedechantiers.rapportChantier.affichageRapportHebdo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AffichageDetailsRapportChantierViewModelFactory(
    private val application: Application,
    private val idChantier: String,
    private val dateBeginning: Long = -1L,
    private val dateEnd: Long = -1L
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AffichageDetailsRapportChantierViewModel::class.java)) {
            return AffichageDetailsRapportChantierViewModel(
                application,
                idChantier,
                dateBeginning,
                dateEnd
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}