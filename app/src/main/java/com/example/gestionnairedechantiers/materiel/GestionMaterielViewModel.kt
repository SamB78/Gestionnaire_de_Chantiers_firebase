package com.example.gestionnairedechantiers.materiel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.entities.Materiel
import com.example.gestionnairedechantiers.firebase.MaterielRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.util.*

class GestionMaterielViewModel : ViewModel() {
    enum class NavigationMenu {
        EDIT_MATERIEL,
        CHOIX_DATE_IMMAT,
        ANNULATION,
        ENREGISTREMENT_MATERIEL,
        AJOUT_PHOTO,
        EN_ATTENTE
    }

    val typeData = "materiel"

    private var _navigationMateriel = MutableLiveData<NavigationMenu>()
    val navigation: LiveData<NavigationMenu>
        get() = this._navigationMateriel

    //Repo
    private val materielRepository = MaterielRepository()

    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val listTypeMateriel = listOf(
        "Camions/Utilitaires",
        "Remorques",
        "Tondeuses",
        "Tracteurs",
        "Engins",
        "Aspirateurs",
        "Divers"
    )

    private val _listeMateriel = MutableLiveData<List<Materiel>>(emptyList())
    val listeMateriel: LiveData<List<Materiel>>
        get() = this._listeMateriel

    var materiel = MutableLiveData<Materiel?>()
    var imageMateriel = MutableLiveData<String>()


    init {
        getAllMateriel()

    }

    private fun getAllMateriel() {
        viewModelScope.launch {
            _listeMateriel.value = materielRepository.getAllMateriel()
        }
    }

    fun onClickBoutonAjoutMateriel() {
        _navigationMateriel.value =
            NavigationMenu.EDIT_MATERIEL
        materiel.value = Materiel()
    }

    fun onClickEditMateriel(materiel: Materiel) {
        _navigationMateriel.value =
            NavigationMenu.EDIT_MATERIEL
        this.materiel.value = materiel.copy()

        if(this.materiel.value!!.urlPictureMateriel.isNullOrEmpty()){
            imageMateriel.value = null
        }else{
            imageMateriel.value = this.materiel.value!!.urlPictureMateriel
        }
    }

    fun onClickButtonCreationOrModificationEnded() {


        materiel.value?.urlPictureMateriel = imageMateriel.value
        if (materiel.value?.documentId == null) sendNewDataToDB()
        else updateDataInDB()

        _navigationMateriel.value = NavigationMenu.ENREGISTREMENT_MATERIEL
    }

    private fun sendNewDataToDB() {
        uiScope.launch {
            materielRepository.insertMateriel(materiel.value!!)
            getAllMateriel()
        }
    }

    private fun updateDataInDB() {
        uiScope.launch {
            materielRepository.updateMateriel(materiel.value!!)
            getAllMateriel()
        }
    }

    fun onClickAjoutImage() {
        _navigationMateriel.value = NavigationMenu.AJOUT_PHOTO
    }

    fun ajoutPathImage(imagePath: String) {
        imageMateriel.value = imagePath
    }

    fun onClickDeletePicture() {
        materiel.value?.urlPictureMateriel = null
        imageMateriel.value = ""
    }

    fun onClickButtonAnnuler() {
        _navigationMateriel.value = NavigationMenu.ANNULATION
    }

    fun onBoutonClicked() {
        _navigationMateriel.value = NavigationMenu.EN_ATTENTE
    }

    fun onClickButtonChoixDate() {
        _navigationMateriel.value = NavigationMenu.CHOIX_DATE_IMMAT
    }

    fun onDateSelected(date: Date) {
//        materiel.value?.miseEnCirculation = date

        materiel.value = materiel.value?.also {
            it.miseEnCirculation = date
        }
        Timber.i("Date recue = ${materiel.value?.miseEnCirculation}")
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()

    }

}