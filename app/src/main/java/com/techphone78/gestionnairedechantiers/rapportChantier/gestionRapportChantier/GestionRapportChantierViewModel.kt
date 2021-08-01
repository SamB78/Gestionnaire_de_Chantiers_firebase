package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier

import androidx.lifecycle.*
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.entities.*
import com.techphone78.gestionnairedechantiers.firebase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.util.*

class GestionRapportChantierViewModel(
    var idRapportChantier: String? = null,
    val idChantier: String,
    val dateRapportChantier: Long = -1L
) : ViewModel() {
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

    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Rapport chantier

    private var rapportChantierOriginal = MutableLiveData<RapportChantier>()

    private var _rapportChantier = MutableLiveData<RapportChantier>()
    val rapportChantier: LiveData<RapportChantier>
        get() = this._rapportChantier

    private var _chantier = MutableLiveData<Chantier>()
    val chantier: LiveData<Chantier>
        get() = this._chantier

    private val chantierRepository = ChantierRepository()
    private val rapportChantierRepository = RapportChantierRepository(idChantier)
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

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        initializeData()
        onBoutonClicked()
    }

    private fun initializeData() {
        when {
            idRapportChantier != null -> {
                viewModelScope.launch {
                    _isLoading.value = true
                    loadRapportChantier().also {
                        _chantier.value =
                            chantierRepository.getChantierById(rapportChantierOriginal.value!!.chantierId!!)
                    }

                    _isLoading.value = false
                }
            }
            dateRapportChantier != -1L -> {
                val date = Instant.ofEpochMilli(dateRapportChantier)
                viewModelScope.launch {
                    _isLoading.value = true
                    _chantier.value = chantierRepository.getChantierById(idChantier)

                    idRapportChantier =
                        rapportChantierRepository.getRapportChantierIdByDate(Date.from(date))

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
                        idRapportChantier =
                            rapportChantierRepository.insertRapportChantier(_rapportChantier.value!!)
                        loadRapportChantier()
                    }
                    _isLoading.value = false
                }
            }
            else -> {
                Timber.e("ERROR: PAS DE DATE OU D'ID")
            }
        }
    }

    private suspend fun loadRapportChantier() {
        Timber.i("Id rapportChantier = $idRapportChantier, id chantier $idChantier")
        viewModelScope.launch {
            idRapportChantier?.let {
                rapportChantierOriginal.value =
                    rapportChantierRepository.getRapportChantierById(it)
                _rapportChantier.value = rapportChantierRepository.getRapportChantierById(it)
            } ?: showErrorLoadingRapportChantier()
            Timber.i("date rapport = ${rapportChantier.value!!.dateRapportChantier}")

        }.join()
    }

    private suspend fun loadRapportChantierAfterUpdate() {
        _isLoading.value = true
        idRapportChantier?.let {
            rapportChantierOriginal.value =
                rapportChantierRepository.getRapportChantierById(it)
        }
        _isLoading.value = false
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
    }

    fun onClickButtonAddPersonnel() {
        isDataPersonnelNeededToActualize = true
        initializeDataPersonnelAjoutable()
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
        val bool = rapportChantierRepository.updateListePersonnelRC(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataPersonnel = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_PERSONNEL
            }

        } else {
            showErrorUpdatePersonnel()
        }

    }

    private fun showErrorUpdatePersonnel() {
        TODO("Not yet implemented")
    }

    /////////////////////// AJOUT PERSONNEL ////////////////////////////////////////

    var listePersonnelAjoutable = MutableLiveData<MutableList<Personnel>>(mutableListOf())
    var listePersonnelAjoutableFiltered = MutableLiveData<MutableList<Personnel>>(mutableListOf())
    var searchFilterPersonnel = MutableLiveData<String?>("")

    private fun initializeDataPersonnelAjoutable() {

        uiScope.launch {
            _isLoading.value = true
            val liste =
                personnelRepository.getAllPersonnel() as MutableList<Personnel>
            listePersonnelAjoutable.value = mutableListOf()

            liste.forEach lit@{ personnel ->
                rapportChantier.value!!.listePersonnel.find { it.documentId == personnel.documentId }
                    ?.let {
                        return@lit
                    }
                listePersonnelAjoutable.value!!.add(personnel)
            }
            listePersonnelAjoutableFiltered.value = listePersonnelAjoutable.value

            _isLoading.value = false
        }

    }

    fun updateSearchFilterPersonnel(text: String?) {
        text?.let{
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


    fun onClickButtonGestionMateriel() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_MATERIEL
    }

    fun onClickButtonAddMateriel() {
        isDataMaterielNeededToActualize = true
        initializeDataMaterielAjoutable()
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
        val bool = rapportChantierRepository.updateListeMaterielRC(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataMateriel = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_MATERIEL
            }


        } else {
            Timber.i("ERROR VALIDATION GESTION MATERIEL")
            showErrorUpdateMateriel()
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

    private fun initializeDataMaterielAjoutable() {

        uiScope.launch {
            _isLoading.value = true
            val liste =
                materielRepository.getAllMateriel() as MutableList<Materiel>
            listeMaterielAjoutable.value = mutableListOf()

            liste.forEach lit@{ materiel ->
                rapportChantier.value!!.listeMateriel.find { it.documentId == materiel.documentId }
                    ?.let {
                        return@lit
                    }
                listeMaterielAjoutable.value!!.add(materiel)
            }
            listeMaterielAjoutableFiltered.value = listeMaterielAjoutable.value
            filterListMateriel()

            _isLoading.value = false
        }
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
            listeOriginaleMateriel.addAll(listeMaterielAjoutable.value!!.filter { it.couleur == chantier.value!!.couleur })
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
                mutableList.add(it)
            }
        } else {
            mutableList.addAll(listeOriginaleMateriel)
        }
        listeMaterielAjoutableFiltered.value = mutableList
    }

    fun onClickValidationAjoutMateriel() {
        val completeList = mutableListOf<Materiel>()
        completeList.addAll(rapportChantier.value!!.listeMateriel)
        listeMaterielAjoutableFiltered.value!!.filter { it.isChecked }.forEach {
            it.isChecked = false
            completeList.add(it)
        }
        _rapportChantier.value!!.listeMateriel = completeList
        _navigation.value = GestionNavigation.VALIDATION_AJOUT_MATERIEL
    }

    fun onClickMateriel(materiel: Materiel) {
        listeMaterielAjoutableFiltered.value!!.find { it.documentId == materiel.documentId }?.isChecked =
            !materiel.isChecked
        listeMaterielAjoutableFiltered.value = listeMaterielAjoutableFiltered.value
    }

    /////////////////////// GESTION MATERIEL LOCATION /////////////////////////////////////

    // Gestion Materiel Location
    var newMaterielLocation = MutableLiveData<MaterielLocation>()

    fun onClickButtonGestionMaterielLocation() {
        _navigation.value = GestionNavigation.PASSAGE_GESTION_MATERIEL_LOCATION
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
        uiScope.launch {
            val bool =
                materielLocationRepository.deleteMaterielLocation(item)
            if (bool) {
                val listeMaterielLocation: MutableList<MaterielLocation> =
                    _rapportChantier.value!!.listeMaterielLocation as MutableList

                listeMaterielLocation.remove(item)
                _rapportChantier.value = _rapportChantier.value!!.also {
                    it.listeMaterielLocation = listeMaterielLocation
                }
            } else {
                showErrorDeleteMaterielLocation()
            }
        }
    }

    private fun showErrorDeleteMaterielLocation() {
        TODO("Not yet implemented")
    }

    fun onClickButtonValidationGestionMaterielLocation() {
        updateTotauxMaterielLocation()
        val bool = rapportChantierRepository.updateListeMaterielLocationRC(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataMaterielLocation = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_MATERIEL_LOCATION
            }
        } else {
            showErrorUpdateMaterielLocation()
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

        uiScope.launch {
            val documentId =
                materielLocationRepository.insertMaterielLocation(newMaterielLocation.value!!)
            if (!documentId.isNullOrEmpty()) {
                Timber.i("reussite")
                newMaterielLocation.value!!.documentId = documentId
                val completeList = mutableListOf<MaterielLocation>()
                completeList.addAll(rapportChantier.value!!.listeMaterielLocation)
                completeList.add(newMaterielLocation.value!!)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.listeMaterielLocation = completeList
                }

                Timber.i("complete liste = ${rapportChantier.value!!.listeMaterielLocation}")
//        _navigation.value = GestionNavigation.VALIDATION_AJOUT_MATERIEL_LOCATION
            } else {
                Timber.i("ERROR ")
                showErrorAddMaterielLocation()
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
        uiScope.launch {
            val bool =
                materiauxRepository.deleteMateriaux(item)
            if (bool) {
                val listeMateriaux: MutableList<Materiaux> =
                    _rapportChantier.value!!.listeMateriaux as MutableList

                listeMateriaux.remove(item)
                _rapportChantier.value = _rapportChantier.value!!.also {
                    it.listeMateriaux = listeMateriaux
                }
            } else {
                showErrorDeleteMateriaux()
            }
        }
    }

    private fun showErrorDeleteMateriaux() {
        TODO("Not yet implemented")
    }

    fun onClickButtonValidationGestionMateriaux() {
        updateTotauxMateriaux()
        val bool = rapportChantierRepository.updateListeMateriauxRC(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataMateriaux = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_MATERIAUX
            }
        } else {
            showErrorUpdateMateriaux()
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

        uiScope.launch {
            val documentId =
                materiauxRepository.insertMateriaux(newMateriaux.value!!)
            if (!documentId.isNullOrEmpty()) {
                Timber.i("reussite")
                newMateriaux.value!!.documentId = documentId
                val completeList = mutableListOf<Materiaux>()
                completeList.addAll(rapportChantier.value!!.listeMateriaux)
                completeList.add(newMateriaux.value!!)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.listeMateriaux = completeList
                }

                Timber.i("complete liste = ${rapportChantier.value!!.listeMateriaux}")
            } else {
                Timber.i("ERROR ")
                showErrorAddMateriaux()
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
    }

    fun onClickDeleteSousTraitance(item: SousTraitance) {
        uiScope.launch {
            val bool =
                sousTraitanceRepository.deleteSousTraitance(item)
            if (bool) {
                val listeSousTraitance: MutableList<SousTraitance> =
                    _rapportChantier.value!!.listeSousTraitance as MutableList

                listeSousTraitance.remove(item)
                _rapportChantier.value = _rapportChantier.value!!.also {
                    it.listeSousTraitance = listeSousTraitance
                }
            } else {
                showErrorDeleteSousTraitance()
            }
        }
    }

    private fun showErrorDeleteSousTraitance() {
        TODO("Not yet implemented")
    }

    fun onClickButtonValidationGestionSousTraitance() {
        updateTotauxSousTraitance()
        val bool = rapportChantierRepository.updateListeSousTraitanceRC(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataSousTraitance = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_GESTION_SOUS_TRAITANCE
            }
        } else {
            showErrorUpdateSousTraitance()
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

        uiScope.launch {
            val documentId =
                sousTraitanceRepository.insertSousTraitance(newSousTraitance.value!!)
            if (!documentId.isNullOrEmpty()) {
                Timber.i("reussite")
                newSousTraitance.value!!.documentId = documentId
                val completeList = mutableListOf<SousTraitance>()
                completeList.addAll(rapportChantier.value!!.listeSousTraitance)
                completeList.add(newSousTraitance.value!!)
                _rapportChantier.value = _rapportChantier.value.also {
                    it!!.listeSousTraitance = completeList
                }

                Timber.i("complete liste = ${rapportChantier.value!!.listeSousTraitance}")
            } else {
                Timber.i("ERROR ")
                showErrorAddSousTraitance()
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

        val bool = rapportChantierRepository.updateListeAutresInformations(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataConformiteChantier = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_AUTRES_INFORMATIONS
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

        val bool = rapportChantierRepository.updateListeObservations(rapportChantier.value!!)
        if (bool) {
            _rapportChantier.value = _rapportChantier.value.also {
                it!!.dataSaved.dataObservations = true
            }
            viewModelScope.launch {
                loadRapportChantierAfterUpdate()
                _navigation.value = GestionNavigation.VALIDATION_OBSERVATIONS
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
        viewModelJob.cancel()
    }

}