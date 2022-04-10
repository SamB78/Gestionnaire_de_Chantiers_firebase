package com.techphone78.gestionnairedechantiers.materiel

import androidx.lifecycle.*
import com.techphone78.gestionnairedechantiers.entities.Couleur
import com.techphone78.gestionnairedechantiers.entities.Materiel
import com.techphone78.gestionnairedechantiers.firebase.CouleurRepository
import com.techphone78.gestionnairedechantiers.firebase.MaterielRepository
import com.techphone78.gestionnairedechantiers.utils.ParentViewModel
import com.techphone78.gestionnairedechantiers.utils.State
import com.techphone78.gestionnairedechantiers.utils.Status
import com.techphone78.gestionnairedechantiers.utils.TypeView
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

class GestionMaterielViewModel : ViewModel(), ParentViewModel {
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

    var state = MutableLiveData(State(Status.LOADING))

    private var _fromCache = MutableLiveData<Boolean>(false)
    val fromCache: LiveData<Boolean>
        get() = _fromCache
    private lateinit var typeView: TypeView

    var selectedColor = MutableLiveData("")
    val selectedColorObserver = Transformations.map(selectedColor) {
        searchColor(it)
    }

    fun setColorChantier(colorString: String?){
        searchColor(colorString)
    }

    private fun searchColor(color: String?) {
        listCouleurs.value?.find { it.colorName == color }?.let {
            materiel.value!!.couleur = if (it.colorName == "Pas de couleur") null
            else it
        }
    }


    init {
        showMaterielList()
    }


    private fun showMaterielList(getServerData: Boolean = false) {
        viewModelScope.launch {
            state.value = State.loading()
            try {
                getAllMateriel(getServerData)
                _listCouleurs.value = couleurRepository.getAllColors()
                state.value = State.success()
            } catch (e: Exception) {
                state.value = State.error(e.toString())
            }
        }
    }

    private suspend fun getAllMateriel(getServerData: Boolean = false) {
        val result = withContext(Dispatchers.IO) {
            materielRepository.getAllMateriel()
        }
        _listeMateriel.value = result.data
        filterListMateriel()
        _fromCache.value = result.fromCache

    }


    fun onClickButtonAjoutMateriel() {
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

        selectedColor.value = when (this.materiel.value!!.couleur?.colorName) {
            null -> "Pas de couleur"
            else -> this.materiel.value!!.couleur?.colorName
        }
    }

    fun onClickButtonCreationOrModificationEnded() {

        state.value = State.loading()
        viewModelScope.launch {
            try {
                materiel.value?.urlPictureMateriel = imageMateriel.value
                if (materiel.value?.documentId == null) sendNewDataToDB()
                else updateDataInDB()
                getAllMateriel()
                _navigationMateriel.value = NavigationMenu.ENREGISTREMENT_MATERIEL
                state.value = State.success()
            } catch (e: Exception) {
                state.value = State.error(e.toString())
            }
        }
    }

    private suspend fun sendNewDataToDB() = withContext(Dispatchers.IO) {
        materielRepository.insertMateriel(materiel.value!!)
    }

    private suspend fun updateDataInDB() = withContext(Dispatchers.IO) {
        materielRepository.updateMateriel(materiel.value!!)
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
        filterListMateriel()
    }

    fun updateSearchFilter(text: String?) {
        searchFilter.value = text
        filterListMateriel()
    }

    private fun filterListMateriel() {
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
                        it.numeroSerie.startsWith(searchFilter.value!!, true)
                        ||
                        it.type.startsWith(searchFilter.value!!, true)
            }.forEach {
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(listeOriginaleMateriel)
        }
        _listeMaterielFiltered.value = mutableList
    }

    override fun onClickErrorScreenButton() {
        when (typeView) {
            TypeView.LIST -> showMaterielList()
            TypeView.MANAGEMENT -> state.value =
                State.success() // Provisoire, il faudrait un affichage plus propre
        }
    }

    fun reloadDataFromServer() {
        showMaterielList(true)
    }

    fun updateTypeView(data: TypeView) {
        typeView = data
    }

}