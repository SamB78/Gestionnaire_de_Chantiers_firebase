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

    var personnel = MutableLiveData<Personnel>(Personnel())
    var imagePersonnel = MutableLiveData<String>()

    init {
        getAllPersonnel()
    }

    private fun getAllPersonnel(){
        viewModelScope.launch {
            _listePersonnel.value = personnelRepository.getAllPersonnel()
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
        personnel.value?.urlPicturePersonnel = imagePath
        imagePersonnel.value = imagePath
    }

    fun onClickDeletePicture(){
        personnel.value?.urlPicturePersonnel = ""
        imagePersonnel.value = ""
    }

    fun onClickButtonAnnuler() {
        _navigationPersonnel.value = NavigationMenu.ANNULATION
    }

    fun onBoutonClicked() {
        _navigationPersonnel.value = NavigationMenu.EN_ATTENTE
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()

    }

}