package com.techphone78.gestionnairedechantiers.chantiers.listeChantiers

import androidx.lifecycle.*
import com.techphone78.gestionnairedechantiers.entities.Chantier
import com.techphone78.gestionnairedechantiers.entities.User
import com.techphone78.gestionnairedechantiers.firebase.AuthRepository
import com.techphone78.gestionnairedechantiers.firebase.ChantierRepository
import com.techphone78.gestionnairedechantiers.utils.State
import com.techphone78.gestionnairedechantiers.utils.Status
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception

class ListeChantiersViewModel : ViewModel() {

    enum class navigationMenu {
        CREATION,
        MODIFICATION,
        EN_ATTENTE
    }


    //Repo&
    private val chantierRepository = ChantierRepository()
    private val authRepository = AuthRepository()
    var user = User()

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
    var allChantiers = MutableLiveData(false)

    private var _state = MutableLiveData(State(Status.LOADING))
    val state: LiveData<State>
        get() = _state

    private var _fromCache = MutableLiveData<Boolean>(false)
    val fromCache: LiveData<Boolean>
        get() = _fromCache


    private var initViewModel = true

    init {
        showChantiersList()
    }

    private fun showChantiersList(getServerData: Boolean = false) {
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                user = authRepository.getDataUser()
                getAllChantiers()
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    private suspend fun getAllChantiers() {
        val result = withContext(Dispatchers.IO) {
            chantierRepository.getAllChantiers()
        }
        _listeChantiers.value = result.data!!
        filterListChantiers()
        _fromCache.value = result.fromCache
    }

    fun onResumeFragment() {
        if (!initViewModel) showChantiersList()
        else {
            initViewModel = false
        }
    }

    fun onClickButtonAjoutChantier() {
        _navigationPersonnel.value =
            navigationMenu.CREATION
    }

    fun onButtonClicked() {
        _navigationPersonnel.value =
            navigationMenu.EN_ATTENTE
    }

    fun onClickChantier(chantier: Chantier) {
        chantierId = chantier.numeroChantier!!
        _navigationPersonnel.value = navigationMenu.MODIFICATION
    }

    fun updateSearchFilter(text: String?) {
        searchFilter.value = text
        filterListChantiers()
    }

    fun filterListChantiers() {
        Timber.i("test")
        _listeChantiersFiltered.value = emptyList()
        val listeOriginaleChantiers: MutableList<Chantier> = mutableListOf()
        val mutableList = mutableListOf<Chantier>()

        if (entretienFilter.value!! && chantierFilter.value!!) {
            listeOriginaleChantiers.addAll(listeChantiers.value!!)
        } else if (entretienFilter.value!! && !chantierFilter.value!!) {
            listeOriginaleChantiers.addAll(listeChantiers.value!!.filter { it.typeChantier == 2 })
        } else if (!entretienFilter.value!! && chantierFilter.value!!) {
            listeOriginaleChantiers.addAll(listeChantiers.value!!.filter { it.typeChantier == 1 })
        }
        if (!allChantiers.value!!) {
            Timber.i("passage AllChantier = false, ${user.userData} ")
           // Timber.i("listOrginale: before $listeOriginaleChantiers")
            val tmpList =
                listeOriginaleChantiers.filter { it.chefChantier.documentId != user.userData!!.documentId }
            Timber.i("tmpList: ${tmpList.map { it.chefChantier.documentId }}")
            listeOriginaleChantiers.removeAll(tmpList)
            Timber.i("listOrgiinale: $listeOriginaleChantiers")
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


    fun reloadDataFromServer() {
        showChantiersList(true)
    }


}