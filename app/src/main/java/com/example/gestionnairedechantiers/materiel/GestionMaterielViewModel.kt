package com.example.gestionnairedechantiers.materiel

import androidx.lifecycle.*
import com.example.gestionnairedechantiers.entities.Couleur
import com.example.gestionnairedechantiers.entities.Materiel
import com.example.gestionnairedechantiers.firebase.CouleurRepository
import com.example.gestionnairedechantiers.firebase.MaterielRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val couleurRepository = CouleurRepository()

    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val listTypeMateriel = listOf(
        "Camions/Utilitaires",
        "Remorques",
        "Tondeuses",
        "Tondeuses autoportées",
        "Tracteurs",
        "Engins",
        "Aspirateurs",
        "Petit matériel",
        "Divers"
    )

    private val _listeMateriel = MutableLiveData<List<Materiel>>(emptyList())
    val listeMateriel: LiveData<List<Materiel>>
        get() = this._listeMateriel

    private val _listeMaterielFiltered = MutableLiveData<List<Materiel>>(emptyList())
    val listeMaterielFiltered: LiveData<List<Materiel>>
        get() = this._listeMaterielFiltered

    var searchFilter = MutableLiveData("")
    var enServiceFilter = MutableLiveData(true)
    var archiveFilter = MutableLiveData(false)

    var materiel = MutableLiveData<Materiel?>()
    var imageMateriel = MutableLiveData<String>()

    //Couleurs
    private var _listCouleurs = MutableLiveData<List<Couleur>>()
    val listCouleurs: LiveData<List<Couleur>>
        get() = _listCouleurs

    var selectedColor = MutableLiveData("")
    val selectedColorObserver = Transformations.map(selectedColor) {
        searchColor(it)
    }

    private fun searchColor(color: String?) {
        listCouleurs.value?.find { it.colorName == color }?.let {
            materiel.value!!.couleur = it
        }
    }


    init {
        getAllMateriel()


    }

    private fun getAllMateriel() {
        viewModelScope.launch {
            _listeMateriel.value = materielRepository.getAllMateriel()
            Timber.i("couleur: ${_listeMateriel.value}")
            _listeMaterielFiltered.value = listeMateriel.value
            _listCouleurs.value = couleurRepository.getAllColors()
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
        selectedColor.value = this.materiel.value!!.couleur?.colorName
        if (this.materiel.value!!.urlPictureMateriel.isNullOrEmpty()) {
            imageMateriel.value = null
        } else {
            imageMateriel.value = this.materiel.value!!.urlPictureMateriel
        }
        selectedColor.value = this.materiel.value!!.couleur?.colorName
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

    fun updateSearchFilter() {
        filterListPersonnel()
    }

    fun updateSearchFilter(text: String?) {
        searchFilter.value = text
        filterListPersonnel()
    }

    private fun filterListPersonnel() {
        _listeMaterielFiltered.value = emptyList()
        val listeOriginaleMateriel: MutableList<Materiel> = mutableListOf()
        val mutableList = mutableListOf<Materiel>()

        if (enServiceFilter.value!! && archiveFilter.value!!) {
            listeOriginaleMateriel.addAll(listeMateriel.value!!)
        } else if (enServiceFilter.value!! && !archiveFilter.value!!) {
            listeOriginaleMateriel.addAll(listeMateriel.value!!.filter { it.enService })
        } else if (!enServiceFilter.value!! && archiveFilter.value!!) {
            listeOriginaleMateriel.addAll(listeMateriel.value!!.filter { !it.enService })
        }

        if (!searchFilter.value.isNullOrEmpty()) {
            listeOriginaleMateriel.filter {
                it.marque.contains(searchFilter.value!!, true)
                        ||
                        it.modele.contains(searchFilter.value!!, true)
                        ||
                        it.numeroSerie.startsWith(searchFilter.value!!)
            }.forEach {
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(listeOriginaleMateriel)
        }
        _listeMaterielFiltered.value = mutableList
    }

    // onCleared()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()

    }

}