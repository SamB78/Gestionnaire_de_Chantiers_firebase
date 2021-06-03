package com.example.gestionnairedechantiers.rapportChantier.affichageRapportHebdo

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.BuildConfig
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.entities.*
import com.example.gestionnairedechantiers.firebase.ChantierRepository
import com.example.gestionnairedechantiers.firebase.RapportChantierRepository
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class AffichageDetailsRapportChantierViewModel(
    application: Application,
    val idChantier: String,
    dateBeginning: Long = -1L,
    dateEnd: Long = -1L
) : AndroidViewModel(application) {

    enum class GestionNavigation {
        RETOUR,
        CREATION_EXCEL,

        SELECTION_CHANTIER,
        SELECTION_DATE,
        DONNEES_SELECTIONNEES,
        AFFICHAGE_EXCEL,
        EN_ATTENTE,
    }

    //navigation
    private var _navigation = MutableLiveData<GestionNavigation>()
    val navigation: LiveData<GestionNavigation>
        get() = this._navigation

    lateinit var uri: Uri

    private val rapportChantierRepository = RapportChantierRepository(idChantier)
    private val chantierRepository = ChantierRepository()

    private val totalDates: MutableList<LocalDate> = ArrayList()

    private var selectedChantier = MutableLiveData<Chantier>()

    private var _listRapportsChantier = MutableLiveData<List<RapportChantier>>(emptyList())
    val listRapportsChantier: LiveData<List<RapportChantier>>
        get() = _listRapportsChantier

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        Timber.i("idchantier = $idChantier")
        if (dateBeginning != -1L && dateEnd != -1L) {
            viewModelScope.launch {
                _isLoading.value = true
                selectedChantier.value = chantierRepository.getChantierById(idChantier)
                loadDataFromDates(dateBeginning, dateEnd)
                _isLoading.value = false
            }
        }
    }

    private suspend  fun loadDataFromDates(dateBeginning: Long, dateEnd: Long) {
        var date1 = Instant.ofEpochMilli(dateBeginning).atZone(ZoneId.systemDefault()).toLocalDate()
        var date2 = Instant.ofEpochMilli(dateEnd).atZone(ZoneId.systemDefault()).toLocalDate()
        val listDates = mutableListOf<Date>()

        while (!date1.isAfter(date2)) {
            totalDates.add(date1)
            listDates.add(Date.from(date1.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            date1 = date1.plusDays(1)
        }
        Timber.i("list dates = ^$totalDates")
        viewModelScope.launch {
            _listRapportsChantier.value =
                rapportChantierRepository.getListRapportsChantierByListOfDates(listDates)
            Timber.i("liste rapport Chantiers = ${listRapportsChantier.value}")
            uri = generateXlsxFile()
        }.join()
    }


    private fun generateXlsxFile(): Uri {

        val weekFiled: WeekFields = WeekFields.of(Locale.FRANCE)

        val context = getApplication<Application>().applicationContext
        val inputStream = context.resources.openRawResource(R.raw.classeur1)

        val xlwb = WorkbookFactory.create(inputStream)

        val style: CellStyle = xlwb.createCellStyle()
        val font: Font = xlwb.createFont()
        style.setAlignment(HorizontalAlignment.CENTER)
        style.setVerticalAlignment(VerticalAlignment.CENTER)
        font.bold = true
        font.color = IndexedColors.BLUE.index
        // font.color = Color.parseColor("#305496").toShort()
        style.setFont(font)

        var firstSheet = true
        var currentDateOfSheet = ""
        var firstDateOfSheet = ""
        var lastDateOfSheet = ""
        var firstWeekOfTableau = 0

        //Boucle creation Sheets avec dates et données en-tête
        totalDates.forEachIndexed { index, date ->
            val sdf = DateTimeFormatter.ofPattern("dd-MM")
            if (index == 0) {
                firstDateOfSheet = sdf.format(date)
                firstWeekOfTableau = date.get(weekFiled.weekOfWeekBasedYear())
                Timber.i("firstWeekOfTableau = $firstWeekOfTableau")
            }

            if (firstSheet) {
                currentDateOfSheet = sdf.format(date)
                Timber.i("firstDateOfSheet = $currentDateOfSheet")
                xlwb.setSheetName(
                    xlwb.numberOfSheets - 1,
                    "SEMAINE ${date.get(weekFiled.weekOfWeekBasedYear())}"
                )
                firstSheet = false
            }

            if (index > 0 && index < totalDates.size - 1) {
                if (totalDates[index - 1].dayOfWeek.value >= date.dayOfWeek.value) {
                    lastDateOfSheet = sdf.format(
                        totalDates[index - 1].with(
                            WeekFields.of(Locale.FRANCE).dayOfWeek(), 5L
                        )
                    )
                    val xlWs = xlwb.getSheetAt(xlwb.numberOfSheets - 1)
                    Timber.i("currentDateOfSheet 2 = $currentDateOfSheet")
                    xlWs.getRow(0).getCell(0)
                        .setCellValue("RAPPORT HEBDOMADAIRE du $currentDateOfSheet au $lastDateOfSheet")
                    xlWs.getRow(1).getCell(3).setCellValue("du $currentDateOfSheet")
                    xlWs.getRow(1).getCell(6).setCellValue("au $lastDateOfSheet")
                    xlWs.getRow(1).getCell(13)
                        .setCellValue("Chantier n° ${selectedChantier.value!!.numeroChantier}")
                    xlWs.getRow(1).getCell(15)
                        .setCellValue(selectedChantier.value!!.adresseChantier.adresseToString())
                    currentDateOfSheet = sdf.format(date)
                    Timber.i("firstDateOfSheet 3 = $currentDateOfSheet")
                    xlwb.cloneSheet(0)
                    // GENERATION TITRE SHEET
                    xlwb.setSheetName(
                        xlwb.numberOfSheets - 1,
                        "SEMAINE ${date.get(weekFiled.weekOfWeekBasedYear())}"
                    )
                    Timber.i("MAKING NEW SHEET")
                }
            } else if (index == totalDates.size - 1) {
                Timber.i("Dernier index")
                lastDateOfSheet = sdf.format(totalDates[index])
                val xlWs = xlwb.getSheetAt(xlwb.numberOfSheets - 1)
                Timber.i("firstDateOfSheet 2 = $currentDateOfSheet")
                xlWs.getRow(0).getCell(0)
                    .setCellValue("RAPPORT HEBDOMADAIRE du $currentDateOfSheet au $lastDateOfSheet")
                xlWs.getRow(1).getCell(3).setCellValue("du $currentDateOfSheet")
                xlWs.getRow(1).getCell(6).setCellValue("au $lastDateOfSheet")
            }

        }


        //////////////////////////////////////// Remplissage Heures PERSONNEL ////////////////////////////////////////

        val listePersonnel: MutableList<Personnel> = mutableListOf()
        val listePersonnelInterimaire: MutableList<Personnel> = mutableListOf()

        listRapportsChantier.value?.forEach { rc ->
            for (personnel in rc.listePersonnel) {
                if (!personnel.interimaire) {
                    if (listePersonnel.find { it.documentId == personnel.documentId } == null) {
                        listePersonnel.add(personnel)
                    }
                } else {
                    if (listePersonnelInterimaire.find { it.documentId == personnel.documentId } == null) {
                        listePersonnelInterimaire.add(personnel)
                    }
                }
            }
        }

        var rowToEdit = 3
        listePersonnel.forEach { personnel ->
            var sheetToEdit = 0
            var compteurSemaines = 0
            var xlWs = xlwb.getSheetAt(sheetToEdit)
            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(personnel.prenom + " " + personnel.nom)

            listRapportsChantier.value?.forEach { rapportChantier ->

                if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                        ZoneId.systemDefault()
                    )!!.get(
                        weekFiled.weekOfWeekBasedYear()
                    )
                ) {
                    compteurSemaines += 1
                    sheetToEdit += 1
                    xlWs = xlwb.getSheetAt(sheetToEdit)
                    xlWs.getRow(rowToEdit).getCell(0)
                        .setCellValue(personnel.prenom + " " + personnel.nom)
                }

                val value =
                    rapportChantier.listePersonnel.find { it.documentId == personnel.documentId }?.nbHeuresTravaillees?.toDouble()

                when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                    DayOfWeek.MONDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.TUESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.WEDNESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.THURSDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.FRIDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.SATURDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                            value
                        )
                    }

                    else -> Timber.i("ERROR")
                }
            }
            rowToEdit++
        }

        //////////////////////////////////////// PERSONNEL INTERIMAIRE ////////////////////////////////////////

        rowToEdit = 14
        listePersonnelInterimaire.forEach { personnel ->
            var sheetToEdit = 0
            val compteurSemaines = 0
            var xlWs = xlwb.getSheetAt(sheetToEdit)
            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(personnel.prenom + " " + personnel.nom)
            _listRapportsChantier.value?.forEach { rapportChantier ->

                if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                        ZoneId.systemDefault()
                    )!!.get(
                        weekFiled.weekOfWeekBasedYear()
                    )
                ) {
                    compteurSemaines.plus(1)
                    sheetToEdit = +1
                    xlWs = xlwb.getSheetAt(sheetToEdit)
                    xlWs.getRow(rowToEdit).getCell(0)
                        .setCellValue(personnel.prenom + " " + personnel.nom)
                }

                val value =
                    rapportChantier.listePersonnel.find { it.documentId == personnel.documentId }?.nbHeuresTravaillees?.toDouble()

                when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                    DayOfWeek.MONDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.TUESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.WEDNESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.THURSDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.FRIDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.SATURDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                            value
                        )
                    }

                    else -> Timber.i("ERROR")
                }
            }
            rowToEdit++
        }

        //////////////////////////////////////// MATERIEL ////////////////////////////////////////

        val listeMateriel: MutableList<Materiel> = mutableListOf()

        listRapportsChantier.value?.forEach { rc ->
            for (materiel in rc.listeMateriel) {
                if (listePersonnel.find { it.documentId == materiel.documentId } == null) {
                    listeMateriel.add(materiel)
                }
            }
        }

        rowToEdit = 20
        listeMateriel.forEach { materiel ->
            var sheetToEdit = 0
            var compteurSemaines = 0
            var xlWs = xlwb.getSheetAt(sheetToEdit)
            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(materiel.marque + " " + materiel.modele)
            _listRapportsChantier.value?.forEach { rapportChantier ->

                if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                        ZoneId.systemDefault()
                    )!!.get(
                        weekFiled.weekOfWeekBasedYear()
                    )
                ) {
                    compteurSemaines += 1
                    sheetToEdit++
                    Timber.i("sheetToEdit = $sheetToEdit")
                    xlWs = xlwb.getSheetAt(sheetToEdit)
                    xlWs.getRow(rowToEdit).getCell(0)
                        .setCellValue(materiel.marque + " " + materiel.modele)
                }

                val value =
                    rapportChantier.listeMateriel.find { it.documentId == materiel.documentId }?.quantite?.toDouble()


                when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                    DayOfWeek.MONDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.TUESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.WEDNESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.THURSDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.FRIDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.SATURDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                            value
                        )
                    }
                    else -> Timber.i("ERROR")
                }
            }
            rowToEdit++
        }

        // MATERIEL LOCATION
        val listeMaterielLocation: MutableList<MaterielLocation> = mutableListOf()

        listRapportsChantier.value?.forEach { rc ->
            for (materielLocation in rc.listeMaterielLocation) {
                if (listeMaterielLocation.find { it.documentId == materielLocation.documentId } == null) {
                    listeMaterielLocation.add(materielLocation)
                }
            }
        }

        rowToEdit = 35
        listeMaterielLocation.forEach { materielLocation ->
            var sheetToEdit = 0
            var compteurSemaines = 0
            var xlWs = xlwb.getSheetAt(sheetToEdit)
            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(materielLocation.fournisseur)
            xlWs.getRow(rowToEdit).getCell(1)
                .setCellValue(materielLocation.description)

            _listRapportsChantier.value?.forEach { rapportChantier ->

                if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                        ZoneId.systemDefault()
                    )!!.get(
                        weekFiled.weekOfWeekBasedYear()
                    )
                ) {
                    compteurSemaines += 1
                    sheetToEdit++
                    xlWs = xlwb.getSheetAt(sheetToEdit)
                    xlWs.getRow(rowToEdit).getCell(0)
                        .setCellValue(materielLocation.fournisseur)
                    xlWs.getRow(rowToEdit).getCell(1)
                        .setCellValue(materielLocation.description)
                }

                xlWs = xlwb.getSheetAt(sheetToEdit)
                val value =
                    rapportChantier.listeMaterielLocation.find { it.documentId == materielLocation.documentId }?.quantite?.toDouble()

                when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                    DayOfWeek.MONDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.TUESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.WEDNESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.THURSDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.FRIDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.SATURDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                            value
                        )
                    }
                    else -> Timber.i("ERROR")
                }
            }
            rowToEdit++
        }

        // MATERIAUX

        val listeMateriaux: MutableList<Materiaux> = mutableListOf()

        listRapportsChantier.value?.forEach { rc ->
            for (materiaux in rc.listeMateriaux) {
                if (listePersonnel.find { it.documentId == materiaux.documentId } == null) {
                    listeMateriaux.add(materiaux)
                }
            }
        }

        rowToEdit = 43

        listeMateriaux.forEach { materiaux ->
            var sheetToEdit = 0
            var compteurSemaines = 0
            var xlWs = xlwb.getSheetAt(sheetToEdit)
            xlWs.getRow(rowToEdit).getCell(0).setCellValue(materiaux.fournisseur)
            xlWs.getRow(rowToEdit).getCell(1).setCellValue(materiaux.description)
            xlWs.getRow(rowToEdit).getCell(2).setCellValue(materiaux.nDeBon)
            _listRapportsChantier.value?.forEach { rapportChantier ->

                if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                        ZoneId.systemDefault()
                    )!!.get(
                        weekFiled.weekOfWeekBasedYear()
                    )
                ) {
                    compteurSemaines += 1
                    sheetToEdit++
                    Timber.i("sheetToEdit = $sheetToEdit")
                    xlWs = xlwb.getSheetAt(sheetToEdit)
                    xlWs.getRow(rowToEdit).getCell(0).setCellValue(materiaux.fournisseur)
                    xlWs.getRow(rowToEdit).getCell(1).setCellValue(materiaux.description)
                    xlWs.getRow(rowToEdit).getCell(2).setCellValue(materiaux.nDeBon)
                }

                val value =
                    rapportChantier.listeMateriaux.find { it.documentId == materiaux.documentId }?.quantite?.toDouble()
                Timber.i("value materiaux = $value")



                when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                    DayOfWeek.MONDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.TUESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.WEDNESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.THURSDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.FRIDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.SATURDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                            value
                        )
                    }
                    else -> Timber.i("ERROR")
                }
            }
            rowToEdit++
        }

        // SOUS TRAITANCE

        val listeSousTraitance: MutableList<SousTraitance> = mutableListOf()

        listRapportsChantier.value?.forEach { rc ->
            for (sousTraitance in rc.listeSousTraitance) {
                if (listePersonnel.find { it.documentId == sousTraitance.documentId } == null) {
                    listeSousTraitance.add(sousTraitance)
                }
            }
        }

        rowToEdit = 52
        listeSousTraitance.forEach { sousTraitance ->
            var sheetToEdit = 0
            var compteurSemaines = 0
            var xlWs = xlwb.getSheetAt(sheetToEdit)
            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(sousTraitance.societe)
            xlWs.getRow(rowToEdit).getCell(1)
                .setCellValue(sousTraitance.prestations)


            _listRapportsChantier.value?.forEach { rapportChantier ->

                if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                        ZoneId.systemDefault()
                    )!!.get(
                        weekFiled.weekOfWeekBasedYear()
                    )
                ) {
                    compteurSemaines += 1
                    sheetToEdit++
                    Timber.i("sheetToEdit = $sheetToEdit")
                    xlWs = xlwb.getSheetAt(sheetToEdit)
                    xlWs.getRow(rowToEdit).getCell(0)
                        .setCellValue(sousTraitance.societe)
                    xlWs.getRow(rowToEdit).getCell(1)
                        .setCellValue(sousTraitance.prestations)
                }
                val value =
                    rapportChantier.listeSousTraitance.find { it.documentId == sousTraitance.documentId }?.quantite?.toDouble()

                when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                    DayOfWeek.MONDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.TUESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.WEDNESDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.THURSDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.FRIDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                            value
                        )
                    }

                    DayOfWeek.SATURDAY -> value?.let {
                        xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                            value
                        )
                    }
                    else -> Timber.i("ERROR")
                }
            }
            rowToEdit++
        }

        // COMMENTAIRES METEO ET NOM RESPONSABLE
        var sheetToEdit = 0
        var compteurSemaines = 0
        var xlWs = xlwb.getSheetAt(sheetToEdit)
        val chefChantierName =
            listePersonnel.find { it.documentId == selectedChantier.value!!.chefChantier.documentId }?.nom
                ?: "ERREUR"
        xlWs.getRow(56).getCell(6).setCellValue("Etabli par $chefChantierName")


        _listRapportsChantier.value?.forEach { rapportChantier ->
            if (firstWeekOfTableau + compteurSemaines < rapportChantier.dateRapportChantier!!.atZone(
                    ZoneId.systemDefault()
                )!!.get(
                    weekFiled.weekOfWeekBasedYear()
                )
            ) {
                sheetToEdit++
                compteurSemaines += 1
                xlWs = xlwb.getSheetAt(sheetToEdit)
            }

            val value = rapportChantier.commentaire

            when (rapportChantier.dateRapportChantier?.atZone(ZoneId.systemDefault())?.dayOfWeek) {

                DayOfWeek.MONDAY -> {
                    xlWs.getRow(20).getCell(12).setCellValue(value)
                    fillMeteoField(rapportChantier.meteo, DayOfWeek.MONDAY, xlWs, style)
                }

                DayOfWeek.TUESDAY -> {
                    xlWs.getRow(26).getCell(12).setCellValue(value)
                    fillMeteoField(rapportChantier.meteo, DayOfWeek.TUESDAY, xlWs, style)
                }

                DayOfWeek.WEDNESDAY -> {
                    xlWs.getRow(32).getCell(12).setCellValue(value)
                    fillMeteoField(rapportChantier.meteo, DayOfWeek.WEDNESDAY, xlWs, style)
                }

                DayOfWeek.THURSDAY -> {
                    xlWs.getRow(38).getCell(12).setCellValue(value)
                    fillMeteoField(rapportChantier.meteo, DayOfWeek.THURSDAY, xlWs, style)
                }

                DayOfWeek.FRIDAY -> {
                    xlWs.getRow(44).getCell(12).setCellValue(value)
                    fillMeteoField(rapportChantier.meteo, DayOfWeek.FRIDAY, xlWs, style)
                }

                DayOfWeek.SATURDAY -> {
                    xlWs.getRow(50).getCell(12).setCellValue(value)
                    fillMeteoField(rapportChantier.meteo, DayOfWeek.SATURDAY, xlWs, style)
                }

                else -> Timber.i("ERROR")
            }
        }

        val fileName =
            "${selectedChantier.value!!.nomChantier} $firstDateOfSheet $lastDateOfSheet.xlsx"

        val extStorageDirectory: String =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString()
        val folder = File(
            extStorageDirectory,
            "Rapports de chantier"
        ) // Name of the folder you want to keep your file in the local storage.

        folder.mkdir() //creating the folder

        Timber.i("errorExcel $extStorageDirectory ")

        val file = File(extStorageDirectory, fileName)
        try {
            file.createNewFile() // creating the file inside the folder
        } catch (e1: IOException) {
            e1.printStackTrace()
            Timber.i("Cannot create new file")
        }

        try {
            val fileOut = FileOutputStream(file) //Opening the file
            xlwb.write(fileOut) //Writing all your row column inside the file
            fileOut.close() //closing the file and done
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val uri =
            FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            )
        return uri
    }

    private fun fillMeteoField(meteo: Meteo, dayOfWeek: DayOfWeek, xlWs: Sheet, style: CellStyle) {


        val row = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 19
            DayOfWeek.TUESDAY -> 25
            DayOfWeek.WEDNESDAY -> 31
            DayOfWeek.THURSDAY -> 37
            DayOfWeek.FRIDAY -> 43
            DayOfWeek.SATURDAY -> 49
            else -> 0
        }

        Timber.i("fillMeteoField")

        if (meteo.soleil) {
            xlWs.getRow(row).getCell(13).cellStyle = style
            Timber.i("fillMeteoField soleil")
        }
        if (meteo.pluie) {
            xlWs.getRow(row).getCell(14).cellStyle = style
            Timber.i("fillMeteoField pluie")
        }
        if (meteo.vent) {
            xlWs.getRow(row).getCell(15).cellStyle = style
            Timber.i("fillMeteoField vent")
        }
        if (meteo.gel) {
            xlWs.getRow(row).getCell(16).cellStyle = style
            Timber.i("fillMeteoField gel")
        }
        if (meteo.neige) {
            xlWs.getRow(row).getCell(17).cellStyle = style
            Timber.i("fillMeteoField neige")
        }
        Timber.i("fillMeteoField")
    }

    fun onClickShowExcelDocument() {
        _navigation.value = GestionNavigation.AFFICHAGE_EXCEL
    }

    fun onButtonClicked() {
        _navigation.value = GestionNavigation.EN_ATTENTE
    }
}