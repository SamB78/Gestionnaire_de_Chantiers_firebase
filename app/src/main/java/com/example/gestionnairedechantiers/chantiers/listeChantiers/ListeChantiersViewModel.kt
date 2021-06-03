package com.example.gestionnairedechantiers.chantiers.listeChantiers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.entities.Chantier
import com.example.gestionnairedechantiers.firebase.ChantierRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class ListeChantiersViewModel : ViewModel() {

    enum class navigationMenu {
        CREATION,
        MODIFICATION,
        EN_ATTENTE
    }

    //Repo
    private val chantierRepository = ChantierRepository()

    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _listeChantiers = MutableLiveData<List<Chantier>>(emptyList())
    val listeChantiers: LiveData<List<Chantier>>
        get() = this._listeChantiers

    private var _navigationPersonnel = MutableLiveData<navigationMenu>()
    val navigation: LiveData<navigationMenu>
        get() = this._navigationPersonnel

    var chantierId = ""

    private var initViewModel = true
    init {
        getAllChantiers()
    }

    private fun getAllChantiers() {
        Timber.i("GetAllChantiers")
        viewModelScope.launch {
            _listeChantiers.value = chantierRepository.getAllChantiers()
        }
    }

    fun onResumeFragment(){
        if (!initViewModel) getAllChantiers()
        else{
            initViewModel = false
        }
    }

    fun onClickBoutonAjoutChantier() {
        _navigationPersonnel.value =
            navigationMenu.CREATION
    }

    fun onBoutonClicked() {
        _navigationPersonnel.value =
            navigationMenu.EN_ATTENTE
    }

    fun onClickChantier(chantier: Chantier) {
        chantierId = chantier.numeroChantier!!
        _navigationPersonnel.value = navigationMenu.MODIFICATION
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}