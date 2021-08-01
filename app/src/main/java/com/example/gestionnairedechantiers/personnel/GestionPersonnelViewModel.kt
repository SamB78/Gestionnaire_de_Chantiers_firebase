package com.example.gestionnairedechantiers.personnel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.entities.Personnel
import com.example.gestionnairedechantiers.firebase.PersonnelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class GestionPersonnelViewModel : ViewModel() {

    enum class NavigationMenu {

        ENREGISTREMENT_PERSONNEL,
        EN_ATTENTE,
        AJOUT_PHOTO,
        ANNULATION,
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

    init {
        getAllPersonnel()
    }

    private fun getAllPersonnel(){
        viewModelScope.launch {
            _listePersonnel.value = personnelRepository.getAllPersonnel()
            _listePersonnelFiltered.value = listePersonnel.value
        }
    }

    fun onClickBoutonAjoutPersonnel(){
        _navigationPersonnel.value =
            NavigationMenu.EDIT_PERSONNEL
        this.personnel.value = Personnel()
    }

    fun onClickEditPersonnel(personnel: Personnel){
        _navigationPersonnel.value =
            NavigationMenu.EDIT_PERSONNEL
        this.personnel.value = personnel.copy()

        if(this.personnel.value!!.urlPicturePersonnel.isNullOrEmpty()){
            imagePersonnel.value = null
        }else{
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
            personnelRepository.insertPersonnel(personnel.value!!)
            getAllPersonnel()
        }
    }

    private fun updateDataInDB() {
        uiScope.launch {
            personnelRepository.updatePersonnel(personnel.value!!)
            getAllPersonnel()
        }
    }

    fun onClickAjoutImage(){
        _navigationPersonnel.value = NavigationMenu.AJOUT_PHOTO
    }

    fun ajoutPathImage(imagePath: String) {
        imagePersonnel.value = imagePath
    }

    fun onClickDeletePicture(){
        personnel.value?.urlPicturePersonnel = null
        imagePersonnel.value = ""
    }

    fun onClickButtonAnnuler() {
        _navigationPersonnel.value = NavigationMenu.ANNULATION
    }

    fun onBoutonClicked() {
        _navigationPersonnel.value = NavigationMenu.EN_ATTENTE
    }

    fun updateSearchFilter(text: String?){
        searchFilter.value = text
        filterListPersonnel()
    }

    fun filterListPersonnel(){
        _listePersonnelFiltered.value = emptyList()
        val listeOriginalePersonnel: MutableList<Personnel> = mutableListOf()
        val mutableList = mutableListOf<Personnel>()

        if(enServiceFilter.value!! && archiveFilter.value!!){
            listeOriginalePersonnel.addAll(listePersonnel.value!!)
        }else if(enServiceFilter.value!! && !archiveFilter.value!!){
            listeOriginalePersonnel.addAll(listePersonnel.value!!.filter { it.enService})
        }else if(!enServiceFilter.value!! && archiveFilter.value!!){
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

}