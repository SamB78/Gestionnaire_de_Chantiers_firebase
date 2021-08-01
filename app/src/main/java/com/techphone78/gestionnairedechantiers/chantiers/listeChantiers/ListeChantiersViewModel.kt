package com.techphone78.gestionnairedechantiers.chantiers.listeChantiers

import androidx.lifecycle.*
import com.techphone78.gestionnairedechantiers.entities.Chantier
import com.techphone78.gestionnairedechantiers.firebase.ChantierRepository
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

    private var _listeChantiersFiltered = MutableLiveData<List<Chantier>>(emptyList())
    val listeChantiersFiltered: LiveData<List<Chantier>>
        get() = this._listeChantiersFiltered

    private var _navigationPersonnel = MutableLiveData<navigationMenu>()
    val navigation: LiveData<navigationMenu>
        get() = this._navigationPersonnel

    var chantierId = ""

    var searchFilter = MutableLiveData("")
    var entretienFilter = MutableLiveData(true)
    var chantierFilter = MutableLiveData(true)



    private var initViewModel = true

    init {
        getAllChantiers()
        viewModelScope.launch {
        }
    }

    private fun getAllChantiers() {
        Timber.i("GetAllChantiers")
        viewModelScope.launch {
            _listeChantiers.value = chantierRepository.getAllChantiers()
            _listeChantiersFiltered.value = listeChantiers.value
        }
    }

    fun onResumeFragment() {
        if (!initViewModel) getAllChantiers()
        else {
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

    fun updateSearchFilter(text: String?){
        searchFilter.value = text
        filterListChantiers()
    }

    fun filterListChantiers() {
        Timber.i("test")
        _listeChantiersFiltered.value = emptyList()
        val listeOriginaleChantiers: MutableList<Chantier> = mutableListOf()
        val mutableList = mutableListOf<Chantier>()

       if(entretienFilter.value!! && chantierFilter.value!!){
           listeOriginaleChantiers.addAll(listeChantiers.value!!)
           Timber.i("test1")
       }else if(entretienFilter.value!! && !chantierFilter.value!!){
           listeOriginaleChantiers.addAll(listeChantiers.value!!.filter { it.typeChantier ==2})
           Timber.i("test2")
       }else if(!entretienFilter.value!! && chantierFilter.value!!){
           listeOriginaleChantiers.addAll(listeChantiers.value!!.filter { it.typeChantier ==1})
           Timber.i("test3")
       }

        if (!searchFilter.value.isNullOrEmpty()) {
            listeOriginaleChantiers.filter {
                it.nomChantier.contains(searchFilter.value!!, true)
                        ||
                        it.numeroChantier!!.startsWith(searchFilter.value!!)
            }.forEach {
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(listeOriginaleChantiers)
        }
        _listeChantiersFiltered.value = mutableList
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}