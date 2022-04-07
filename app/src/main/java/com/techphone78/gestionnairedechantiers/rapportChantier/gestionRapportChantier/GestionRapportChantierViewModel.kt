package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier

import androidx.lifecycle.*
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.entities.*
import com.techphone78.gestionnairedechantiers.firebase.*
import com.techphone78.gestionnairedechantiers.utils.ParentViewModel
import com.techphone78.gestionnairedechantiers.utils.State
import com.techphone78.gestionnairedechantiers.utils.Status
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception
import java.time.Instant
import java.util.*

class GestionRapportChantierViewModel(
    var idRapportChantier: String? = null,
    private val idChantier: String,
    val dateRapportChantier: Long = -1L
) : ViewModel(), ParentViewModel {
    enum class GestionNavigation {
        PASSAGE_GESTION_PERSONNEL,
        VALIDATION_GESTION_PERSONNEL,
        PASSAGE_AJOUT_PERSONNEL,
        VALIDATION_AJOUT_PERSONNEL,

        PASSAGE_AUTRES_INFORMATIONS,
        PASSAGE_ETAPE_2_AUTRES_INFORMATIONS,
        VALIDATION_AUTRES_INFORMATIONS,

        PASSAGE_OBSERVATIONS,
        VALIDATION_OBSERVATIONS,

        PASSAGE_GESTION_MATERIEL,
        PASSAGE_AJOUT_MATERIEL,
        VALIDATION_GESTION_MATERIEL,
        VALIDATION_AJOUT_MATERIEL,

        PASSAGE_GESTION_MATERIEL_LOCATION,
        PASSAGE_AJOUT_MATERIEL_LOCATION,
        VALIDATION_AJOUT_MATERIEL_LOCATION,
        VALIDATION_GESTION_MATERIEL_LOCATION,

        PASSAGE_GESTION_MATERIAUX,
        PASSAGE_AJOUT_MATERIAUX,
        VALIDATION_AJOUT_MATERIAUX,
        VALIDATION_GESTION_MATERIAUX,

        VALIDATION_GESTION_SOUS_TRAITANCE,
        PASSAGE_AJOUT_SOUS_TRAITANCE,
        VALIDATION_AJOUT_SOUS_TRAITANCE,
        PASSAGE_GESTION_SOUS_TRAITANCE,

        ENREGISTREMENT_CHANTIER,

        ANNULATION,
        EN_ATTENTE,
    }


    //Navigation
    private var _navigation = MutableLiveData<GestionNavigation>()
    val navigation: LiveData<GestionNavigation>
        get() = _navigation

    // Rapport chantier

    private var rapportChantierOriginal = MutableLiveData<RapportChantier>()

    private var _rapportChantier = MutableLiveData<RapportChantier>()
    val rapportChantier: LiveData<RapportChantier>
        get() = this._rapportChantier

    private var _chantier = MutableLiveData<Chantier>()
    val chantier: LiveData<Chantier>
        get() = this._chantier

    private val chantierRepository = ChantierRepository()
    private val rapportChantierRepository = RapportChantierRepository()
    private val personnelRepository = PersonnelRepository()
    private val materielRepository = MaterielRepository()
    private val materielLocationRepository = MaterielLocationRepository()
    private val materiauxRepository = MateriauxRepository()
    private val sousTraitanceRepository = SousTraitanceRepository()

    //Verification sur les données ont été changées sans avoir été sauvegardées
    private var _dataChangedWithoutSave = MutableLiveData<DataSaved>(DataSaved())
    val dataChangedWithoutSave: LiveData<DataSaved>
        get() = _dataChangedWithoutSave


//    val dataChangedWithoutSave: LiveData<DataSaved> = Transformations.switchMap() {
//        Timber.i("Passage Transformation dataChangedWithoutSave ")
//        compareRapportsChantier()
//    }

    //Booleans Actualisation données
    private var isDataPersonnelNeededToActualize = false
    private var isDataMaterielNeededToActualize = false

    private var _state = MutableLiveData(State(Status.LOADING))
    val state: LiveData<State>
        get() = _state

    private var _successDialog = MutableLiveData(false)
    val successDialog: LiveData<Boolean>
        get() = _successDialog

    init {
        initializeData()
        onBoutonClicked()
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                _state.value = State.loading()
                when {
                    idRapportChantier != null -> {

                        loadRapportChantier().also {
                            _chantier.value =
                                withContext(Dispatchers.IO) {
                                    chantierRepository.getChantierById(
                                        rapportChantierOriginal.value!!.chantierId!!
                                    )
                                }
                        }
                    }
                    dateRapportChantier != -1L -> {
                        val date = Instant.ofEpochMilli(dateRapportChantier)
                        Timber.i("Date: $date")
                        _chantier.value =
                            withContext(Dispatchers.IO) {
                                chantierRepository.getChantierById(
                                    idChantier
                                )
                            }!!

                        idRapportChantier =
                            withContext(Dispatchers.IO) {
                                rapportChantierRepository.getRapportChantierIdByDate(
                                    Date.from(date), idChantier
                                )
                            }

                        if (idRapportChantier != null) {
                            Timber.i("idRapportChantier = $idRapportChantier")
                            loadRapportChantier()
                        } else {
                            // Si nouveau rapport de Chantier
                            _rapportChantier.value = RapportChantier(
                                chantierId = chantier.value!!.numeroChantier,
                                chefChantier = chantier.value!!.chefChantier,
                                dateRapportChantier = date,
                                typeChantier = chantier.value!!.typeChantier,
                                listePersonnel = chantier.value!!.listEquipe
                            )
                            idRapportChantier = withContext(Dispatchers.IO) {
                                rapportChantierRepository.insertRapportChantier(_rapportChantier.value!!, idChantier)
                            }
                            loadRapportChantier()
                        }
                    }
                    else -> {
                        Timber.e("ERROR: PAS DE DATE OU D'ID")
                    }

                }
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())

            }
        }
    }

    private suspend fun loadRapportChantier() {
        Timber.i("Id rapportChantier = $idRapportChantier, id chantier $idChantier")
        idRapportChantier?.let {
            rapportChantierOriginal.value =
                withContext(Dispatchers.IO) {
                    rapportChantierRepository.getRapportChantierById(
                        it, idChantier
                    )
                }
            _rapportChantier.value = withContext(Dispatchers.IO) {
                rapportChantierRepository.getRapportChantierById(it, idChantier)
            }
        } ?: showErrorLoadingRapportChantier()
        Timber.i("date rapport = ${rapportChantier.value!!.dateRapportChantier}")

    }

    private suspend fun loadRapportChantierAfterUpdate() {
        idRapportChantier?.let {
            rapportChantierOriginal.value = withContext(Dispatchers.IO) {
                rapportChantierRepository.getRapportChantierById(it, idChantier)
            }
        }
    }

    private fun showErrorLoadingRapportChantier() {
        TODO("Not yet implemented")
    }


    fun onBoutonClicked() {
        _navigation.value = GestionNavigation.EN_ATTENTE
    }

    fun onResumeGestionChantier() {
        rapportChantier.value?.let {
            compareRapportsChantier()
            updateTotauxPersonnel()
            updateTotauxMateriel()
            updateTotauxMaterielLocation()
            updateTotauxMateriaux()
            updateTotauxSousTraitance()
        }
    }

    /////////////////////// GESTION PERSONNEL ////////////////////////////////////////

    fun onClickButtonGestionPersonnel() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_PERSONNEL
        _state.value = State.success()
    }

    fun onClickButtonAddPersonnel() {
        isDataPersonnelNeededToActualize = true
        initializeDataPersonnelAddable()
        _navigation.value = GestionNavigation.PASSAGE_AJOUT_PERSONNEL
    }

    fun onResumeGestionPersonnelFragment() {
        if (isDataPersonnelNeededToActualize) {
//            updateDataPersonnelAfterPersonnelAdded(_rapportChantier.value!!.rapportChantierId!!.toLong())
            isDataPersonnelNeededToActualize = false
        }
    }

    fun onClickDeletePersonnel(item: Personnel) {
        val listePersonnel: MutableList<Personnel> =
            _rapportChantier.value!!.listePersonnel as MutableList

        listePersonnel.remove(item)
        Timber.i("$listePersonnel")
        _rapportChantier.value = _rapportChantier.value!!.also {
            it.listePersonnel = listePersonnel
        }
    }

    fun onPersonnelProgressChanged(progress: Int, item: Personnel) {

        _rapportChantier.value!!.listePersonnel.find { it.documentId == item.documentId }?.nbHeuresTravaillees =
            progress
    }

    private fun updateTotauxPersonnel() {
        rapportChantier.value!!.totauxRC.totalMOPersonnel = 0
        rapportChantier.value!!.totauxRC.totalMOInterimaire = 0
        for (personnel in rapportChantier.value!!.listePersonnel) {
            if (!personnel.interimaire)
                rapportChantier.value!!.totauxRC.totalMOPersonnel += personnel.nbHeuresTravaillees
            else rapportChantier.value!!.totauxRC.totalMOInterimaire += personnel.nbHeuresTravaillees
        }
        rapportChantier.value!!.totauxRC.totalMO =
            rapportChantier.value!!.totauxRC.totalMOPersonnel + rapportChantier.value!!.totauxRC.totalMOInterimaire

    }

    fun onClickButtonValidationGestionPersonnel() {
        updateTotauxPersonnel()
        viewModelScope.launch {
            try {
                _state.value = State.loading()
                withContext(Dispatchers.IO) {
                    rapportChantierRepository.updateListePersonnelRC(rapportChantier.value!!, idChantier)
                }
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataPersonnel = true
                }
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_PERSONNEL
            } catch (e: Exception) {

                _state.value = State.error()

            }
            _state.value = State.success()
        }

    }

    /////////////////////// AJOUT PERSONNEL ////////////////////////////////////////

    var listePersonnelAjoutable = MutableLiveData<MutableList<Personnel>??>(mutableListOf())
    var listePersonnelAjoutableFiltered = MutableLiveData<MutableList<Personnel>>(mutableListOf())
    var searchFilterPersonnel = MutableLiveData<String?>("")

    private var _fromCache = MutableLiveData(false)
    val fromCache: LiveData<Boolean>
        get() = _fromCache


    fun initializeDataPersonnelAddable() {
        listePersonnelAjoutable.value = mutableListOf()
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                val list = getAddablePersonnel()

                list.forEach lit@{ personnel ->
                    rapportChantier.value!!.listePersonnel.find { it.documentId == personnel.documentId }
                        ?.let {
                            return@lit
                        }
                    listePersonnelAjoutable.value!!.add(personnel)

                }
                listePersonnelAjoutableFiltered.value = listePersonnelAjoutable.value
                filterListPersonnel()
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    private suspend fun getAddablePersonnel(): List<Personnel> =
        withContext(Dispatchers.IO) { personnelRepository.getAllAddablePersonnel() }.let {
            _fromCache.value = it.fromCache
            return it.data
        }

    fun updateSearchFilterPersonnel(text: String?) {
        text?.let {
            searchFilterPersonnel.value = it
        }
        filterListPersonnel()
    }

    private fun filterListPersonnel() {
        listePersonnelAjoutableFiltered.value = mutableListOf()
        val listeOriginalePersonnel: MutableList<Personnel> = mutableListOf()
        val mutableList = mutableListOf<Personnel>()

        listeOriginalePersonnel.addAll(listePersonnelAjoutable.value!!)

        if (!searchFilterPersonnel.value.isNullOrEmpty()) {
            listeOriginalePersonnel.filter {
                it.nom.contains(searchFilterPersonnel.value!!, true)
                        ||
                        it.prenom.contains(searchFilterPersonnel.value!!, true)
            }.forEach {
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(listeOriginalePersonnel)
        }
        listePersonnelAjoutableFiltered.value = mutableList
    }

    fun onClickValidationAjoutPersonnel() {
        val completeList = mutableListOf<Personnel>()
        completeList.addAll(rapportChantier.value!!.listePersonnel)
        listePersonnelAjoutableFiltered.value!!.filter { it.isChecked }.forEach {
            it.isChecked = false
            completeList.add(it)
        }
        _rapportChantier.value!!.listePersonnel = completeList
        _navigation.value = GestionNavigation.VALIDATION_AJOUT_PERSONNEL
    }

    fun onClickPersonnel(personnel: Personnel) {
        listePersonnelAjoutableFiltered.value!!.find { it.documentId == personnel.documentId }?.isChecked =
            !personnel.isChecked
        listePersonnelAjoutableFiltered.value = listePersonnelAjoutableFiltered.value
    }


    /////////////////////// GESTION MATERIEL ////////////////////////////////////////

    val listTypeMateriel = listOf(
        "TOUS",
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


    fun onClickButtonGestionMateriel() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_MATERIEL
        _state.value = State.success()
    }

    fun onClickButtonAddMateriel() {
        isDataMaterielNeededToActualize = true
        initializeDataMaterielAddable()
        _navigation.value = GestionNavigation.PASSAGE_AJOUT_MATERIEL
    }


    fun onClickDeleteMateriel(item: Materiel) {
        val listeMateriel: MutableList<Materiel> =
            _rapportChantier.value!!.listeMateriel as MutableList

        listeMateriel.remove(item)
        Timber.i("$listeMateriel")
        _rapportChantier.value = _rapportChantier.value!!.also {
            it.listeMateriel = listeMateriel
        }

    }

    fun onMaterielProgressChanged(progress: Int, item: Materiel) {

        _rapportChantier.value!!.listeMateriel.find { it.documentId == item.documentId }?.quantite =
            progress
    }

    private fun updateTotauxMateriel() {
        rapportChantier.value!!.totauxRC.totalQuantiteMaterielSociete = 0
        for (materiel in rapportChantier.value!!.listeMateriel) {
            rapportChantier.value!!.totauxRC.totalQuantiteMaterielSociete += materiel.quantite
            Timber.i("quantite materiel = ${materiel.quantite}")
        }
        Timber.i("quantite Materiel Societe = ${rapportChantier.value!!.totauxRC.totalQuantiteMaterielSociete}")
        rapportChantier.value!!.totauxRC.totalQuantiteMateriel =
            rapportChantier.value!!.totauxRC.totalQuantiteMaterielSociete + rapportChantier.value!!.totauxRC.totalQuantiteMaterielLocation

    }

    fun onClickButtonValidationGestionMateriel() {
        updateTotauxMateriel()
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                withContext(Dispatchers.IO) {
                    rapportChantierRepository.updateListeMaterielRC(rapportChantier.value!!, idChantier)
                }
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataMateriel = true
                }

                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_MATERIEL
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error()
            }
        }
    }

    private fun showErrorUpdateMateriel() {
        TODO("Not yet implemented")
    }

    /////////////////////// AJOUT MATERIEL ////////////////////////////////////////

    var listeMaterielAjoutable = MutableLiveData<MutableList<Materiel>>(mutableListOf())
    var listeMaterielAjoutableFiltered = MutableLiveData<MutableList<Materiel>>(mutableListOf())
    var searchFilterMateriel = MutableLiveData("")
    var filterByChantierColor = MutableLiveData(true)

    var filterTypeMateriel = MutableLiveData<String>(null)

    fun initializeDataMaterielAddable() {
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                val list = getAddableMateriel()
                listeMaterielAjoutable.value = mutableListOf()

                list.forEach lit@{ materiel ->
                    rapportChantier.value!!.listeMateriel.find { it.documentId == materiel.documentId }
                        ?.let {
                            return@lit
                        }
                    listeMaterielAjoutable.value!!.add(materiel)
                }
                listeMaterielAjoutableFiltered.value = listeMaterielAjoutable.value
                filterListMateriel()
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }

//            _isLoading.value = false
        }
    }

    private suspend fun getAddableMateriel(): List<Materiel> =
        withContext(Dispatchers.IO) {
            materielRepository.getAllAddableMateriel(
                chantier.value?.typeChantier ?: 1
            )
        }.let {
            _fromCache.value = it.fromCache
            return it.data
        }

    fun updateSearchFilterMateriel(text: String?) {
        text?.let {
            searchFilterMateriel.value = it
        }
        filterListMateriel()
    }

    fun updateSearchFilterMateriel() {
        filterListMateriel()
    }

    fun onCheckedSwitchFilterByColor(check: Boolean) {
        filterByChantierColor.value = check
    }

    private fun filterListMateriel() {
        listeMaterielAjoutableFiltered.value = mutableListOf()
        val listeOriginaleMateriel: MutableList<Materiel> = mutableListOf()
        val mutableList = mutableListOf<Materiel>()

        if (filterByChantierColor.value!!) {
            listeOriginaleMateriel.addAll(listeMaterielAjoutable.value!!.filter { it.couleur == chantier.value!!.couleur || it.couleur == null })
        } else {
            listeOriginaleMateriel.addAll(listeMaterielAjoutable.value!!)
        }



        if (!searchFilterMateriel.value.isNullOrEmpty()) {
            listeOriginaleMateriel.filter {
                it.marque.contains(searchFilterMateriel.value!!, true)
                        ||
                        it.modele.contains(searchFilterMateriel.value!!, true)
                        ||
                        it.numeroSerie.contains(searchFilterMateriel.value!!, true)
            }.forEach {
                Timber.i("filterTypeMateriel = ${filterTypeMateriel.value}")
                if (!filterTypeMateriel.value.isNullOrBlank() && filterTypeMateriel.value != "TOUS" && it.type == filterTypeMateriel.value) {
                    Timber.i(" filterTypeMateriel Passage if OK  = ${filterTypeMateriel.value}")
                    mutableList.add(it)
                } else {
                    Timber.i(" filterTypeMateriel Passage if NOK  = ${filterTypeMateriel.value}")
                    mutableList.add(it)
                }


            }
        } else {
            mutableList.addAll(listeOriginaleMateriel)
        }

        mutableList.sortByDescending { it.couleur == chantier.value!!.couleur }

        listeMaterielAjoutableFiltered.value = mutableList
    }

    fun onClickValidationAjoutMateriel() {
        val completeList = mutableListOf<Materiel>()
        completeList.addAll(rapportChantier.value!!.listeMateriel)
        listeMaterielAjoutableFiltered.value!!.filter { it.isChecked }.forEach {
            it.isChecked = false
            completeList.add(it)
        }
        listeMaterielAjoutable.value!!.filter { it.isChecked }.forEach { materiel ->
            if (completeList.find { materiel.documentId == it.documentId } == null) {
                completeList.add(materiel)
            }
        }

        _rapportChantier.value!!.listeMateriel = completeList
        _navigation.value = GestionNavigation.VALIDATION_AJOUT_MATERIEL
    }

    fun onClickMateriel(materiel: Materiel) {
        listeMaterielAjoutableFiltered.value!!.find { it.documentId == materiel.documentId }?.isChecked =
            !materiel.isChecked
        listeMaterielAjoutableFiltered.value = listeMaterielAjoutableFiltered.value
    }

    fun reinitSearchField() {
        searchFilterMateriel.value = ""
    }

    /////////////////////// GESTION MATERIEL LOCATION /////////////////////////////////////

    // Gestion Materiel Location
    var newMaterielLocation = MutableLiveData<MaterielLocation>()

    fun onClickButtonGestionMaterielLocation() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_MATERIEL_LOCATION
        _state.value = State.success()
    }

    fun onMaterielLocationProgressChanged(progress: Int) {
        newMaterielLocation.value!!.quantite = progress

    }

    fun onMaterielLocationProgressChanged(progress: Int, materiel: MaterielLocation) {
        rapportChantier.value!!.listeMaterielLocation.find { it.documentId == materiel.documentId }?.quantite =
            progress
        dataChangedWithoutSave.value!!.dataMaterielLocation = true
    }

    fun onClickDeleteMaterielLocation(item: MaterielLocation) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    materielLocationRepository.deleteMaterielLocation(
                        item
                    )
                }

                val listeMaterielLocation: MutableList<MaterielLocation> =
                    _rapportChantier.value!!.listeMaterielLocation as MutableList

                listeMaterielLocation.remove(item)
                _rapportChantier.value = _rapportChantier.value!!.also {
                    it.listeMaterielLocation = listeMaterielLocation
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun showErrorDeleteMaterielLocation() {
        TODO("Not yet implemented")
    }

    fun onClickButtonValidationGestionMaterielLocation() {
        updateTotauxMaterielLocation()
        viewModelScope.launch {
            _state.value = State.loading()

            try {
                withContext(Dispatchers.IO) {
                    rapportChantierRepository.updateListeMaterielLocationRC(
                        rapportChantier.value!!, idChantier
                    )
                }

                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataMaterielLocation = true
                }
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_MATERIEL_LOCATION
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error()
            }
        }
    }

    private fun showErrorUpdateMaterielLocation() {
        TODO("Not yet implemented")
    }

    fun onClickButtonAddMaterielLocation() {
        newMaterielLocation.value = MaterielLocation()
        _navigation.value = GestionNavigation.PASSAGE_AJOUT_MATERIEL_LOCATION
    }


    fun onClickButtonConfirmationAjoutMaterielLocation() {

        viewModelScope.launch {
            _state.value = State.loading()
            _successDialog.value = false
            try {
                val documentId = withContext(Dispatchers.IO) {
                    materielLocationRepository.insertMaterielLocation(newMaterielLocation.value!!)
                }
                Timber.i("reussite")
                newMaterielLocation.value!!.documentId = documentId
                val completeList = mutableListOf<MaterielLocation>()
                completeList.addAll(rapportChantier.value!!.listeMaterielLocation)
                completeList.add(newMaterielLocation.value!!)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.listeMaterielLocation = completeList
                }
                _successDialog.value = true
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    private fun showErrorAddMaterielLocation() {
        TODO("Not yet implemented")
    }

    private fun updateTotauxMaterielLocation() {
        rapportChantier.value!!.totauxRC.totalQuantiteMaterielLocation = 0
        for (materiel in rapportChantier.value!!.listeMaterielLocation) {
            rapportChantier.value!!.totauxRC.totalQuantiteMaterielLocation += materiel.quantite
        }
        rapportChantier.value!!.totauxRC.totalQuantiteMateriel =
            rapportChantier.value!!.totauxRC.totalQuantiteMaterielSociete + rapportChantier.value!!.totauxRC.totalQuantiteMaterielLocation

    }

    /////////////////////// GESTION DES MATERIAUX ////////////////////////////////////////

    var newMateriaux = MutableLiveData(Materiaux())

    fun onClickButtonGestionMateriaux() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_MATERIAUX
        _state.value = State.success()
    }

    fun onMateriauxProgressChanged(progress: Int) {
        newMateriaux.value!!.quantite = progress
    }

    fun onMateriauxProgressChanged(progress: Int, materiaux: Materiaux) {
        rapportChantier.value!!.listeMateriaux.find { it.documentId == materiaux.documentId }?.quantite =
            progress
        dataChangedWithoutSave.value!!.dataMateriaux = true
    }

    fun onClickDeleteMateriaux(item: Materiaux) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                materiauxRepository.deleteMateriaux(item)
            }
            val listeMateriaux: MutableList<Materiaux> =
                _rapportChantier.value!!.listeMateriaux as MutableList

            listeMateriaux.remove(item)
            _rapportChantier.value = _rapportChantier.value!!.also {
                it.listeMateriaux = listeMateriaux
            }
        }
    }

    private fun showErrorDeleteMateriaux() {
        TODO("Not yet implemented")
    }

    fun onClickButtonValidationGestionMateriaux() {
        updateTotauxMateriaux()
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                rapportChantierRepository.updateListeMateriauxRC(rapportChantier.value!!, idChantier)

                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataMateriaux = true
                }
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_MATERIAUX
                _state.value = State.success()

            } catch (e: Exception) {
                _state.value = State.error()
            }
        }
    }

    private fun showErrorUpdateMateriaux() {
        TODO("Not yet implemented")
    }

    fun onClickButtonAddMateriaux() {
        newMateriaux.value = Materiaux()
        _navigation.value = GestionNavigation.PASSAGE_AJOUT_MATERIAUX
    }

    fun onClickButtonConfirmationAjoutMateriaux() {

        viewModelScope.launch {
            _state.value = State.loading()
            _successDialog.value = false
            try {
                val documentId =
                    withContext(Dispatchers.IO) {
                        materiauxRepository.insertMateriaux(newMateriaux.value!!)
                    }
                if (documentId.isNotEmpty()) {
                    Timber.i("reussite")
                    newMateriaux.value!!.documentId = documentId
                    val completeList = mutableListOf<Materiaux>()
                    completeList.addAll(rapportChantier.value!!.listeMateriaux)
                    completeList.add(newMateriaux.value!!)
                    _rapportChantier.value = _rapportChantier.value.also {
                        it!!.listeMateriaux = completeList
                    }
                    _successDialog.value = true
                    _state.value = State.success()
                } else {
                    Timber.i("ERROR ")
                    showErrorAddMateriaux()
                }
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }

    }

    private fun showErrorAddMateriaux() {
        TODO("Not yet implemented")
    }


    private fun updateTotauxMateriaux() {
        rapportChantier.value!!.totauxRC.totalMateriaux = 0
        for (materiaux in rapportChantier.value!!.listeMateriaux) {
            rapportChantier.value!!.totauxRC.totalMateriaux += materiaux.quantite
        }
    }


    /////////////////////// GESTION SOUS-TRAITANCE /////////////////////////////////////

    var newSousTraitance = MutableLiveData(SousTraitance())

    fun onClickButtonGestionSousTraitance() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_SOUS_TRAITANCE
        _state.value = State.success()
    }

    fun onClickDeleteSousTraitance(item: SousTraitance) {
        viewModelScope.launch {
            try {
                sousTraitanceRepository.deleteSousTraitance(item)
                val listeSousTraitance: MutableList<SousTraitance> =
                    _rapportChantier.value!!.listeSousTraitance as MutableList

                listeSousTraitance.remove(item)
                _rapportChantier.value = _rapportChantier.value!!.also {
                    it.listeSousTraitance = listeSousTraitance
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun showErrorDeleteSousTraitance() {
        TODO("Not yet implemented")
    }

    fun onClickButtonValidationGestionSousTraitance() {
        updateTotauxSousTraitance()
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                rapportChantierRepository.updateListeSousTraitanceRC(rapportChantier.value!!, idChantier)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataSousTraitance = true
                }
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_SOUS_TRAITANCE
                _state.value = State.success()

            } catch (e: Exception) {
                _state.value = State.error()
            }
        }
    }

    private fun showErrorUpdateSousTraitance() {
        TODO("Not yet implemented")
    }

    fun onClickButtonAddSousTraitance() {
        newSousTraitance.value = SousTraitance()
        _navigation.value = GestionNavigation.PASSAGE_AJOUT_SOUS_TRAITANCE
    }

    fun onClickButtonConfirmationAjoutSousTraitance() {

        viewModelScope.launch {
            _state.value = State.loading()
            _successDialog.value = false
            try {
                val documentId =
                    sousTraitanceRepository.insertSousTraitance(newSousTraitance.value!!)
                if (documentId.isNotEmpty()) {
                    Timber.i("reussite")
                    newSousTraitance.value!!.documentId = documentId
                    val completeList = mutableListOf<SousTraitance>()
                    completeList.addAll(rapportChantier.value!!.listeSousTraitance)
                    completeList.add(newSousTraitance.value!!)
                    _rapportChantier.value = _rapportChantier.value.also {
                        it!!.listeSousTraitance = completeList
                    }
                    _successDialog.value = true
                    _state.value = State.success()
                } else {
                    Timber.i("ERROR ")
                    showErrorAddSousTraitance()
                }
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    private fun showErrorAddSousTraitance() {
        TODO("Not yet implemented")
    }


    private fun updateTotauxSousTraitance() {
        rapportChantier.value!!.totauxRC.totalSousTraitance = 0
        for (sousTraitance in rapportChantier.value!!.listeSousTraitance) {
            rapportChantier.value!!.totauxRC.totalSousTraitance += sousTraitance.quantite
        }
    }


    /////////////////////// SECURITE & ENVIRONNEMENT ////////////////////////////////////////

    fun onClickButtonAutresInformations() {
        _navigation.value = GestionNavigation.PASSAGE_AUTRES_INFORMATIONS
        _state.value = State.success()
    }

    fun radioGroupsSecurite(id: Int) {
        when (id) {

            R.id.radioButtonEPI1 -> rapportChantier.value?.observations!!.securiteRespectPortEPI =
                true
            R.id.radioButtonEPI2 -> rapportChantier.value?.observations!!.securiteRespectPortEPI =
                false
            R.id.radioButtonBalisage1 -> rapportChantier.value?.observations!!.securiteBalisage =
                true
            R.id.radioButtonBalisage2 -> rapportChantier.value?.observations!!.securiteBalisage =
                false
        }
        dataChangedWithoutSave.value!!.dataConformiteChantier = true
    }

    fun radioGroupsEnvironnement(id: Int) {
        when (id) {
            R.id.radioButtonProprete1 -> rapportChantier.value?.observations!!.environnementProprete =
                true
            R.id.radioButtonProprete2 -> rapportChantier.value?.observations!!.environnementProprete =
                false
            R.id.radioButtonNonPollution1 -> rapportChantier.value?.observations!!.environnementNonPollution =
                true
            R.id.radioButtonNonPollution2 -> rapportChantier.value?.observations!!.environnementNonPollution =
                false
        }
        dataChangedWithoutSave.value!!.dataConformiteChantier = true
    }

    fun radioGroupsMateriel(id: Int) {
        when (id) {
            R.id.radioButtonPropreteVehicule1 -> rapportChantier.value?.observations!!.propreteVehicule =
                true
            R.id.radioButtonPropreteVehicule2 -> rapportChantier.value?.observations!!.propreteVehicule =
                false

            R.id.radioButtonEntretienMateriel1 -> rapportChantier.value?.observations!!.entretienMateriel =
                true
            R.id.radioButtonEntretienMateriel2 -> rapportChantier.value?.observations!!.entretienMateriel =
                false

            R.id.radioButtonRenduCarnet1 -> rapportChantier.value?.observations!!.renduCarnetDeBord =
                true
            R.id.radioButtonRenduCarnet2 -> rapportChantier.value?.observations!!.renduCarnetDeBord =
                false

            R.id.radioButtonRenduBonDecharge1 -> rapportChantier.value?.observations!!.renduBonDecharge =
                true
            R.id.radioButtonRenduBonDecharge2 -> rapportChantier.value?.observations!!.renduBonDecharge =
                false

            R.id.radioButtonRenduBonCarburant1 -> rapportChantier.value?.observations!!.renduBonCarburant =
                true
            R.id.radioButtonRenduBonCarburant2 -> rapportChantier.value?.observations!!.renduBonCarburant =
                false

            R.id.radioButtonRenduFeuillesInterimaire1 -> rapportChantier.value?.observations!!.feuillesInterimaires =
                true
            R.id.radioButtonRenduFeuillesInterimaire2 -> rapportChantier.value?.observations!!.feuillesInterimaires =
                false

            R.id.radioButtonRenduBonDeCommande1 -> rapportChantier.value?.observations!!.bonDeCommande =
                true
            R.id.radioButtonRenduBonDeCommande2 -> rapportChantier.value?.observations!!.bonDeCommande =
                false

        }
        dataChangedWithoutSave.value!!.dataConformiteChantier = true
    }

    fun onClickPassageEtape2AutresInformations() {
        _navigation.value = GestionNavigation.PASSAGE_ETAPE_2_AUTRES_INFORMATIONS
    }

    fun onClickButtonValidationAutresInformations() {
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                rapportChantierRepository.updateListeAutresInformations(rapportChantier.value!!, idChantier)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataConformiteChantier = true
                }

                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_AUTRES_INFORMATIONS
                _state.value = State.success()

            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }
    /////////////////////// OBSERVATIONS ////////////////////////////////////////

    val traitementPhytosanitaireBoolean = MutableLiveData<Boolean>(false)

//    val traitementPhytosanitaireBoolean = Transformations.map(rapportChantier) { rapportChantier ->
//        Timber.i("phyto = ${rapportChantier.tachesEntretien.find { it.type == "PH" }?.checked}")
//        rapportChantier.tachesEntretien.find { it.type == "PH" }?.checked ?: false
//    }


    fun onClickButtonValidationObservations() {
        Timber.i("adresseChantier = ${rapportChantier.value!!.adresseChantier}")
        viewModelScope.launch {
            _state.value = State.loading()
            try {
                rapportChantierRepository.updateListeObservations(rapportChantier.value!!, idChantier)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.dataSaved.dataObservations = true
                }
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_OBSERVATIONS
                _state.value = State.success()
            } catch (e: Exception) {
                _state.value = State.error(e.toString())
            }
        }
    }

    fun onClickEntretienItem(item: TacheEntretien) {
        if (item.type == "PH") {
            traitementPhytosanitaireBoolean.value =
                rapportChantier.value!!.tachesEntretien.find { it.type == "PH" }!!.checked
        }


    }


    fun onClickButtonObservations() {
        _navigation.value = GestionNavigation.PASSAGE_OBSERVATIONS
        _state.value = State.success()
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    private fun compareRapportsChantier() {

        val dataSaved = MutableLiveData(DataSaved())

//        dataSaved.value!!.dataPersonnel = isEqual(
//            rapportChantier.value!!.listePersonnel, rapportChantierOriginal.value!!.listePersonnel
//        )
        dataSaved.value!!.dataPersonnel =
            rapportChantier.value?.listePersonnel != rapportChantierOriginal.value?.listePersonnel
        dataSaved.value!!.dataMateriel =
            rapportChantier.value?.listeMateriel != rapportChantierOriginal.value?.listeMateriel
        dataSaved.value!!.dataMaterielLocation =
            rapportChantier.value?.listeMaterielLocation != rapportChantierOriginal.value?.listeMaterielLocation
        dataSaved.value!!.dataMateriaux =
            rapportChantier.value?.listeMateriaux != rapportChantierOriginal.value!!.listeMateriaux
        dataSaved.value!!.dataSousTraitance =
            rapportChantier.value?.listeSousTraitance != rapportChantierOriginal.value!!.listeSousTraitance
        dataSaved.value!!.dataConformiteChantier =
            rapportChantier.value?.observations != rapportChantierOriginal.value!!.observations

        Timber.i("rapportChantier.value?.listeMateriel = ${rapportChantier.value?.listeMateriel}")
        Timber.i("rapportChantierOriginal.value?.listeMateriel = ${rapportChantierOriginal.value?.listeMateriel}")

        _dataChangedWithoutSave.value = dataSaved.value
    }

    private fun <T> isEqual(first: List<T>, second: List<T>): Boolean {

        if (first.size != second.size) {
            return false
        }

        first.forEachIndexed { index, value ->
            Timber.i("first  = $value")
            Timber.i("second = $value")
            if (second[index] != value) {
                return false
            }
        }
        return true
    }


    // onCleared()
    override fun onCleared() {
        super.onCleared()
    }

    override fun onClickErrorScreenButton() {
        TODO("Not yet implemented")
    }

}