package com.techphone78.gestionnairedechantiers.personnel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.techphone78.gestionnairedechantiers.firebase.PersonnelRepository
import com.techphone78.gestionnairedechantiers.utils.ParentViewModel
import com.techphone78.gestionnairedechantiers.utils.State
import com.techphone78.gestionnairedechantiers.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class GestionPersonnelViewModel : ViewModel(), ParentViewModel {

    enum class NavigationMenu {
        ENREGISTREMENT_PERSONNEL,
        EN_ATTENTE,
        AJOUT_PHOTO,
        EDIT_PERSONNEL
    }

    val typeData = "personnel"

    private var _navigationPersonnel = MutableLiveData<NavigationMenu>()
    val navigationPersonnel: LiveData<NavigationMenu>
        get() = this._navigationPersonnel

    //Repo
    private val personnelRepository = PersonnelRepository()

    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _listePersonnel = MutableLiveData<List<Personnel>>(emptyList())
    val listePersonnel: LiveData<List<Personnel>>
        get() = this._listePersonnel

    private val _listePersonnelFiltered = MutableLiveData<List<Personnel>>(emptyList())
    val listePersonnelFiltered: LiveData<List<Personnel>>
        get() = this._listePersonnelFiltered

    var searchFilter = MutableLiveData("")
    var enServiceFilter = MutableLiveData(true)
    var archiveFilter = MutableLiveData(false)

    var personnel = MutableLiveData<Personnel?>(Personnel())
    var imagePersonnel = MutableLiveData<String?>(null)

    var state = MutableLiveData(State(Status.LOADING))
    private var typeView: State.TypeView? = null

    init {
        getAllPersonnel()
    }

    private fun getAllPersonnel() {
        viewModelScope.launch {

            state.value = State.loading()
            try {
                _listePersonnel.value = personnelRepository.getAllPersonnel()
                filterListPersonnel()
                state.value = State.success()
            } catch (e: Exception) {
                state.value = State.error(e.toString())
            }
        }
    }

    fun onClickBoutonAjoutPersonnel() {
        _navigationPersonnel.value =
            NavigationMenu.EDIT_PERSONNEL
        this.personnel.value = Personnel()
    }

    fun onClickEditPersonnel(personnel: Personnel) {
        _navigationPersonnel.value =
            NavigationMenu.EDIT_PERSONNEL
        this.personnel.value = personnel.copy()

        if (this.personnel.value!!.urlPicturePersonnel.isNullOrEmpty()) {
            imagePersonnel.value = null
        } else {
            imagePersonnel.value = this.personnel.value!!.urlPicturePersonnel
        }

    }

    fun onChckedSwitchEnserviceChanged(check: Boolean) {
        personnel.value?.enService = check
    }

    fun onCheckedSwitchAdministrateurChanged(check: Boolean) {
        personnel.value?.administrateur = check
    }

    fun onCheckedSwitchChefEquipeChanged(check: Boolean) {
        personnel.value?.chefEquipe = check
    }

    fun onCheckedSwitchInterimaireChanged(check: Boolean) {
        personnel.value?.interimaire = check
    }

    fun onClickButtonCreationOrModificationEnded() {

        Timber.i("personnel ready to save in DB = ${personnel.value?.prenom}")
        personnel.value?.urlPicturePersonnel = imagePersonnel.value
        if (personnel.value?.documentId == null) sendNewDataToDB()
        else updateDataInDB()

        _navigationPersonnel.value = NavigationMenu.ENREGISTREMENT_PERSONNEL
    }

    private fun sendNewDataToDB() {
        uiScope.launch {

            state.value = State.loading()
            try {
                personnelRepository.insertPersonnel(personnel.value!!)
                getAllPersonnel()
                state.value = State.success()
            } catch (e: Exception) {
                state.value = State.error(e.toString())
            }
        }
    }

    private fun updateDataInDB() {
        uiScope.launch {

            state.value = State.loading()
            try {
                personnelRepository.updatePersonnel(personnel.value!!)
                getAllPersonnel()
                state.value = State.success()
            } catch (e: Exception) {
                state.value = State.error(e.toString())
            }
        }
    }

    fun onClickAjoutImage() {
        _navigationPersonnel.value = NavigationMenu.AJOUT_PHOTO
    }

    fun ajoutPathImage(imagePath: String) {
        imagePersonnel.value = imagePath
    }

    fun onClickDeletePicture() {
        personnel.value?.urlPicturePersonnel = null
        imagePersonnel.value = ""
    }

    fun onBoutonClicked() {
        _navigationPersonnel.value = NavigationMenu.EN_ATTENTE
    }

    fun updateSearchFilter(text: String?) {
        searchFilter.value = text
        filterListPersonnel()
    }

    fun filterListPersonnel() {
        _listePersonnelFiltered.value = emptyList()
        val listeOriginalePersonnel: MutableList<Personnel> = mutableListOf()
        val mutableList = mutableListOf<Personnel>()

        if (enServiceFilter.value!! && archiveFilter.value!!) {
            listeOriginalePersonnel.addAll(listePersonnel.value!!)
        } else if (enServiceFilter.value!! && !archiveFilter.value!!) {
            listeOriginalePersonnel.addAll(listePersonnel.value!!.filter { it.enService })
        } else if (!enServiceFilter.value!! && archiveFilter.value!!) {
            listeOriginalePersonnel.addAll(listePersonnel.value!!.filter { !it.enService })
        }

        if (!searchFilter.value.isNullOrEmpty()) {
            listeOriginalePersonnel.filter {
                it.nom.contains(searchFilter.value!!, true)
                        ||
                        it.prenom.contains(searchFilter.value!!, true)
            }.forEach {
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(listeOriginalePersonnel)
        }
        _listePersonnelFiltered.value = mutableList
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()

    }

    fun updateTypeView(data: State.TypeView) {
        typeView = data
    }

    override fun onClickErrorScreenButton() {
        when(typeView){
            State.TypeView.LIST -> TODO()
            State.TypeView.MANAGEMENT -> TODO()
            null -> TODO()
        }
    }

}