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
import com.techphone78.gestionnairedechantiers.utils.TypeView
import kotlinx.coroutines.*

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

    private var _state = MutableLiveData(State(Status.LOADING))
    val state: LiveData<State>
        get() = _state
    private var _fromCache = MutableLiveData<Boolean>(false)
    val fromCache: LiveData<Boolean>
        get() = _fromCache
    private lateinit var typeView: TypeView

    init {
        showPersonnelList()
    }


    private fun showPersonnelList(getServerData: Boolean = false) {
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                getAllPersonnel(getServerData)
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    private suspend fun getAllPersonnel(getServerData: Boolean = false) {
        val result = withContext(Dispatchers.IO) {
            personnelRepository.getAllPersonnel()
        }
        _listePersonnel.value = result.data
        filterListPersonnel()
        _fromCache.value = result.fromCache

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

    fun onCheckedSwitchEnserviceChanged(check: Boolean) {
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

        _state.value = State.loading()
        viewModelScope.launch {
            try {
                personnel.value?.urlPicturePersonnel = imagePersonnel.value
                if (personnel.value?.documentId == null) sendNewDataToDB()
                else updateDataInDB()
                getAllPersonnel()
                _navigationPersonnel.value = NavigationMenu.ENREGISTREMENT_PERSONNEL
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    private suspend fun sendNewDataToDB() = withContext(Dispatchers.IO) {
        personnelRepository.insertPersonnel(personnel.value!!)
    }

    private suspend fun updateDataInDB() = withContext(Dispatchers.IO) {
        personnelRepository.updatePersonnel(personnel.value!!)
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
        val originalList: MutableList<Personnel> = mutableListOf()
        val mutableList = mutableListOf<Personnel>()

        if (enServiceFilter.value!! && archiveFilter.value!!) {
            originalList.addAll(listePersonnel.value!!)
        } else if (enServiceFilter.value!! && !archiveFilter.value!!) {
            originalList.addAll(listePersonnel.value!!.filter { it.enService })
        } else if (!enServiceFilter.value!! && archiveFilter.value!!) {
            originalList.addAll(listePersonnel.value!!.filter { !it.enService })
        }

        if (!searchFilter.value.isNullOrEmpty()) {
            originalList.filter {
                it.nom.contains(searchFilter.value!!, true)
                        ||
                        it.prenom.contains(searchFilter.value!!, true)
            }.forEach {
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(originalList)
        }
        _listePersonnelFiltered.value = mutableList
    }

    fun updateTypeView(data: TypeView) {
        typeView = data
    }

    override fun onClickErrorScreenButton() {
        when (typeView) {
            TypeView.LIST -> showPersonnelList()
            TypeView.MANAGEMENT -> _state.value =
                State.success() // Provisoire, il faudrait un affichage plus propre
        }
    }

    fun reloadDataFromServer() {
        showPersonnelList(true)
    }

}