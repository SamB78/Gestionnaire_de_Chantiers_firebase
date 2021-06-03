package com.example.gestionnairedechantiers.chantiers.affichageChantier

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestionnairedechantiers.entities.Chantier
import com.example.gestionnairedechantiers.entities.RapportChantier
import com.example.gestionnairedechantiers.firebase.ChantierRepository
import com.example.gestionnairedechantiers.firebase.RapportChantierRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class AffichageChantierViewModel(
    val id: String
) : ViewModel() {

    enum class NavigationMenu {
        SELECTION_PAR_DATE,
        SELECTION_PAR_ID,
        CONSULTATION,
        AJOUT,
        SELECTION_DATE,
        EN_ATTENTE,
        EDIT,
        EXPORT,
        SELECTION_DATE_EXPORT,
    }


    private val chantierRepository = ChantierRepository()
    private val rapportChantierRepository = RapportChantierRepository(id)

    private var _navigation = MutableLiveData<NavigationMenu>()
    val navigation: LiveData<NavigationMenu>
        get() = this._navigation

    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _chantier = MutableLiveData<Chantier>(Chantier())
    val chantier: LiveData<Chantier>
        get() = _chantier

    private var _listeRapportsChantiers = MutableLiveData<List<RapportChantier>>(emptyList())
    val listeRapportsChantiers: LiveData<List<RapportChantier>>
        get() = this._listeRapportsChantiers

    private var _idRapportChantier = MutableLiveData<String>()
    val idRapportChantier: LiveData<String>
        get() = this._idRapportChantier


    // Dates pour export
    private var _dateDebut = MutableLiveData<Long>()
    val dateDebut: LiveData<Long>
        get() = this._dateDebut

    private var _dateFin = MutableLiveData<Long>()
    val dateFin: LiveData<Long>
        get() = this._dateFin

    init {
        onResumeGestionFragment()
        onBoutonClicked()
    }

    private fun onResumeGestionFragment() {
        initializeData(id)
    }

    private fun initializeData(id: String) {
        uiScope.launch {
           _chantier.value = chantierRepository.getChantierById(id)
            Timber.i("Chantier initialisé  = ${chantier.value?.numeroChantier}")
            _listeRapportsChantiers.value = rapportChantierRepository.getAllRapportsChantier()
        }
    }


    fun onBoutonClicked() {
        _navigation.value = NavigationMenu.EN_ATTENTE
    }

    private var needToActualizeData= false
    fun onClickButtonEditChantier() {
        _navigation.value = NavigationMenu.EDIT
        needToActualizeData = true
    }

    fun onResumeLoadData(){
        if(needToActualizeData){
            onResumeGestionFragment()
            needToActualizeData = false
        }
    }

    fun onDatesToExportSelected(date1: Long, date2: Long) {
        _dateDebut.value = date1
        _dateFin.value = date2

        Timber.i("Dates = ${dateDebut.value} ${dateFin.value}")

        _navigation.value = NavigationMenu.EXPORT
    }

    fun onClickButtonExportData() {
        _navigation.value = NavigationMenu.SELECTION_DATE_EXPORT
    }

    fun onDateSelected() {
        _navigation.value = NavigationMenu.SELECTION_PAR_DATE
    }

    fun onClickBoutonAjoutRapportChantier() {
        _navigation.value = NavigationMenu.SELECTION_DATE
        needToActualizeData = true
    }

    fun onButtonClickEditRapportChantier(rapportChantier: RapportChantier) {
        _idRapportChantier.value = rapportChantier.documentId!!
        _navigation.value = NavigationMenu.SELECTION_PAR_ID
        needToActualizeData = true
    }


}