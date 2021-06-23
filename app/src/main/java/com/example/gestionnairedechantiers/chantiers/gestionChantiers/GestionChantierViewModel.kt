package com.example.gestionnairedechantiers.chantiers.gestionChantiers

import androidx.lifecycle.*
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.entities.Chantier
import com.example.gestionnairedechantiers.entities.Personnel
import com.example.gestionnairedechantiers.firebase.ChantierRepository
import com.example.gestionnairedechantiers.firebase.PersonnelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class GestionChantierViewModel(val id: String? = null) : ViewModel() {

    enum class GestionNavigation {
        ANNULATION,
        PASSAGE_ETAPE2,
        PASSAGE_ETAPE3,
        CONFIRMATION_ETAPE3,
        PASSAGE_ETAPE4,
        AJOUT_IMAGE,
        PASSAGE_ETAPE_RESUME,
        ENREGISTREMENT_CHANTIER,
        MODIFICATION,
        EN_ATTENTE,
        CONFIRMATION_CHEF
    }

    //Navigation
    private var _navigation = MutableLiveData<GestionNavigation>()
    val navigation: LiveData<GestionNavigation>
        get() = _navigation

    //Repos
    private val chantierRepository = ChantierRepository()
    private val personnelRepository = PersonnelRepository()


    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //Création du chantier avec son adresse
    var chantier = MutableLiveData(Chantier())

    //Liste Personnel
    var listePersonnel = MutableLiveData<List<Personnel>?>(mutableListOf())

    //Liste Chefs chantiers:
    var listeChefsChantier = MutableLiveData<List<Personnel>>(mutableListOf())

    private var _chefChantierSelectionne = MutableLiveData<Personnel>(Personnel())
    val chefChantierSelectionne: LiveData<Personnel>
        get() = this._chefChantierSelectionne


    //Image Chantier
    var imageChantier = MutableLiveData<String>()

    val defaultCheckedButton: LiveData<Int> = Transformations.map(chantier) { chantier ->
        if (chantier.typeChantier == 1) R.id.radio_button_chantier
        else R.id.radio_button_entretien
    }

    init {
        viewModelScope.launch {
            Timber.i("Id = $id")
            getAllPersonnel()
            if (id != null) loadChantierFromDatabase()
        }

    }

    private suspend fun getAllPersonnel() {
        val listeChefChantiers = mutableListOf<Personnel>()
        viewModelScope.launch {
            listePersonnel.value = personnelRepository.getAllPersonnel()
            listePersonnel.value?.filter { it.chefEquipe }?.forEach {
                listeChefChantiers.add(it.copy())
            }
            listeChefsChantier.value = listeChefChantiers
        }
    }

    private fun loadChantierFromDatabase() {
        viewModelScope.launch {
            chantier.value = chantierRepository.getChantierById(id!!)
            Timber.i("Chantier = ${chantier.value}")
            for (personnel in chantier.value!!.listEquipe) {
                listePersonnel.value?.find { it.documentId == personnel.documentId }?.isChecked =
                    true
            }
            listeChefsChantier.value!!.find { it.documentId == chantier.value!!.chefChantier.documentId }?.isChecked =
                true

            if(chantier.value!!.urlPictureChantier.isNullOrEmpty()){
                imageChantier.value = null
            }else{
                imageChantier.value = chantier.value!!.urlPictureChantier
            }
        }
    }

    fun onRadioGroupClicked(id: Int) {
        if (id == R.id.radio_button_chantier) chantier.value!!.typeChantier = 1
        else chantier.value!!.typeChantier = 2

    }

    fun onClickChefChantier(personnel: Personnel) {

        listeChefsChantier.value!!.forEach { event ->
            if (event.documentId == personnel.documentId) {
                _chefChantierSelectionne.value = event.copy(isChecked = false)
                event.isChecked = true
            } else {
                event.isChecked = false
            }
        }
        _navigation.value = GestionNavigation.CONFIRMATION_CHEF
    }

    fun onClickChefChantierValide() {
        chantier.value?.chefChantier = chefChantierSelectionne.value!!

        _navigation.value = GestionNavigation.PASSAGE_ETAPE3
    }

    fun onSelectPersonnel(personnel: Personnel) {
        listePersonnel.value = listePersonnel.value?.also { list ->
            list.find { it.documentId == personnel.documentId }?.let {
                it.isChecked = !it.isChecked
            }
        }
    }

    fun onClickChoixEquipeValide() {
        val listePersonnelChantier = mutableListOf<Personnel>()
        listePersonnel.value!!.forEach {
            if (it.isChecked) {
                listePersonnelChantier.add(it.copy(isChecked = false))
            }
        }
        chantier.value!!.listEquipe = listePersonnelChantier

        _navigation.value = GestionNavigation.CONFIRMATION_ETAPE3
    }

    fun onClickAjoutImage() {
        _navigation.value = GestionNavigation.AJOUT_IMAGE
    }

    fun ajoutPathImage(imagePath: String) {
        chantier.value!!.urlPictureChantier = imagePath
        imageChantier.value = imagePath
    }

    fun onClickDeletePicture() {
        chantier.value?.urlPictureChantier = ""
        imageChantier.value = ""
    }

    fun onBoutonClicked() {
        _navigation.value = GestionNavigation.EN_ATTENTE
    }

    fun onClickButtonPassageEtape2() {
        _navigation.value = GestionNavigation.PASSAGE_ETAPE2
    }


    fun onClickConfirmationEtapeImage() {
//        sendNewDataToDB()
        _navigation.value = GestionNavigation.PASSAGE_ETAPE_RESUME
    }

    fun onClickButtonAnnuler() {
        _navigation.value = GestionNavigation.ANNULATION
    }

    fun onClickButtonModifier() {
        _navigation.value = GestionNavigation.MODIFICATION
    }

    fun onClickSaveData() {
        Timber.i("Data ready to save: ${chantier.value}")
        sendDataToDB()

    }

    private fun sendDataToDB() {
        Timber.i("sendDataToDB")
        chantier.value!!.urlPictureChantier = imageChantier.value
        viewModelScope.launch {
            chantierRepository.setChantier(chantier.value!!)
            _navigation.value = GestionNavigation.ENREGISTREMENT_CHANTIER
        }
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}