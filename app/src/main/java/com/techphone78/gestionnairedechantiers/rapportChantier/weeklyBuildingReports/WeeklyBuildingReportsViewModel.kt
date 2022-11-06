package com.techphone78.gestionnairedechantiers.rapportChantier.weeklyBuildingReports

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.techphone78.gestionnairedechantiers.BuildConfig
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.entities.*
import com.techphone78.gestionnairedechantiers.firebase.ChantierRepository
import com.techphone78.gestionnairedechantiers.firebase.RapportChantierRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.*

class WeeklyBuildingReportsViewModel(application: Application) : AndroidViewModel(application) {

    enum class Navigation {
        SELECTION_DATES,
        EXCEL_DISPLAY,
        WAITING,
    }

    //navigation
    private var _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation>
        get() = this._navigation

    lateinit var uri: Uri

    private var _listeChantiers = MutableLiveData<MutableList<Chantier>>()
    val listeChantiers: LiveData<MutableList<Chantier>>
        get() = this._listeChantiers

    private val chantierRepository = ChantierRepository()
    private val rapportChantierRepository = RapportChantierRepository()


    private var dateBeginning: Long = -1L

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        viewModelScope.launch {
            _listeChantiers.value =
                chantierRepository.getAllChantiers().data!! as MutableList<Chantier>
        }
    }


    fun generateExcel(date: Long) {
        this.dateBeginning = date
        val datesList = generateListDates()
        Timber.i("listDates: $datesList")
        viewModelScope.launch {
            _isLoading.value = true
            val deferred = mutableListOf<Deferred<Any>>()
            _listeChantiers.value?.forEach { chantier ->
                deferred.add(async {
                    val rapports = chantier.numeroChantier?.let { it ->
                        rapportChantierRepository.getWeeklyListRapportsChantierByListOfDates(
                            datesList.first(),
                            it
                        )
                    }
                    if (rapports.isNullOrEmpty()) {
                        _listeChantiers.value!!.remove(chantier)
                    } else {
                        chantier.listRapportChantiers = rapports
                    }
                })
            }

            deferred.awaitAll()
            Timber.i("listeRapportsChantiers : ${listeChantiers.value?.map { rapportChantier -> rapportChantier.numeroChantier }}")

            uri = generateXlsxFile(datesList)


            _navigation.value = Navigation.EXCEL_DISPLAY
            _isLoading.value = false
        }
    }

    private fun generateListDates(): List<Date> {
        val list = mutableListOf<Date>()
        for (i in 0..6) {
            val date =
                Instant.ofEpochMilli(dateBeginning)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .plusDays(i.toLong())

            list.add(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))

        }
        return list
    }

    private fun generateXlsxFile(datesList: List<Date>): Uri {

        val context = getApplication<Application>().applicationContext
        val inputStream = context.resources.openRawResource(R.raw.classeur1)
        val xlwb = WorkbookFactory.create(inputStream)

        val style: CellStyle = xlwb.createCellStyle()
        val font: Font = xlwb.createFont()
        style.setAlignment(HorizontalAlignment.CENTER)
        style.setVerticalAlignment(VerticalAlignment.CENTER)
        font.bold = true
        font.color = IndexedColors.BLUE.index
        style.setFont(font)

        listeChantiers.value?.forEachIndexed { index, _ ->
            if (index > 0) xlwb.cloneSheet(0)
        }

        listeChantiers.value?.forEachIndexed { index, chantier ->
            val xlWs = xlwb.getSheetAt(index)

            var sheetName = if (chantier.aliasChantier.isNotBlank())
                chantier.aliasChantier.replace(Regex("""[\\/:*?"<>&|]"""), "_")
            else chantier.nomChantier.replace(Regex("""[\\/:*?"<>&|]"""), "_")

            if (chantier.typeChantier == 1) sheetName += " TS"
            try {
                xlwb.setSheetName(
                    index,
                    sheetName
                )
            } catch (e: Exception) {
                xlwb.setSheetName(
                    index,
                    "2 $sheetName"
                )
            }

            fillSheetMainInformation(xlWs, chantier, datesList, sheetName)
            chantier.listRapportChantiers?.let { fillSheet(xlWs, it, style) }


        }

        return generateExcelFile(context, xlwb)

    }

    private fun fillSheet(xlWs: Sheet, listRapports: List<RapportChantier>, style: CellStyle) {


        fillSheetWithOtherData(xlWs, listRapports, style)

        listRapports.forEach { rapportChantier ->

            rapportChantier.dateRapportChantier?.let { date ->
                val personnel = rapportChantier.listePersonnel.filter { !it.interimaire }
                val temporaryPersonal = rapportChantier.listePersonnel.filter { it.interimaire }
                fillSheetWithPersonalData(
                    xlWs, personnel,
                    date
                )
                fillSheetWithTemporaryPersonalData(
                    xlWs, temporaryPersonal,
                    date
                )
                fillSheetWithMaterialData(xlWs, rapportChantier.listeMateriel, date)
                fillSheetWithLocationMaterialData(xlWs, rapportChantier.listeMaterielLocation, date)
                fillSheetWithMateriauxData(xlWs, rapportChantier.listeMateriaux, date)
                fillSheetWithSousTraitanceData(xlWs, rapportChantier.listeSousTraitance, date)
            }

        }
    }


    private fun fillSheetMainInformation(
        xlWs: Sheet,
        chantier: Chantier,
        datesList: List<Date>,
        sheetName: String
    ) {

        val weekFiled: WeekFields = WeekFields.of(Locale.FRANCE)
        val sdf = SimpleDateFormat("dd-MM", Locale.FRANCE)

        val calendar = Calendar.getInstance()

        calendar.time = datesList.first()

        val firstDateString = sdf.format(datesList.first())
        val lastDateString = sdf.format(datesList.last())

        var stringNumeroChantier = "Chantier n° ${chantier.numeroChantier}"

        if (chantier.typeChantier == 1) stringNumeroChantier += " TS"


        xlWs.getRow(0).getCell(0)
            .setCellValue("RAPPORT HEBDOMADAIRE du $firstDateString au $lastDateString")
        xlWs.getRow(1).getCell(0)
            .setCellValue("Semaine n°${calendar.get(Calendar.WEEK_OF_YEAR)}")
        xlWs.getRow(1).getCell(3).setCellValue("du $firstDateString")
        xlWs.getRow(1).getCell(6).setCellValue("au $lastDateString")
        xlWs.getRow(1).getCell(13)
            .setCellValue(stringNumeroChantier)
        xlWs.getRow(1).getCell(15)
            .setCellValue(sheetName)
        xlWs.getRow(56).getCell(6).setCellValue("Etabli par ${chantier.chefChantier.nom}")

    }


    private fun fillSheetWithPersonalData(
        xlWs: Sheet,
        listPersonnel: List<Personnel>,
        dateRapportChantier: Instant
    ) {
        listPersonnel.forEachIndexed { index, personnel ->
            val rowToEdit = 3 + index
            val value = personnel.nbHeuresTravaillees

            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(personnel.prenom + " " + personnel.nom)

            when (dateRapportChantier.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                DayOfWeek.MONDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                        value
                    )
                }
                DayOfWeek.TUESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                        value
                    )
                }
                DayOfWeek.WEDNESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                        value
                    )
                }
                DayOfWeek.THURSDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                        value
                    )
                }
                DayOfWeek.FRIDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                        value
                    )
                }
                DayOfWeek.SATURDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                        value
                    )
                }

                else -> Timber.i("ERROR")
            }
        }
    }

    private fun fillSheetWithTemporaryPersonalData(
        xlWs: Sheet,
        listPersonnelInterimaire: List<Personnel>,
        dateRapportChantier: Instant
    ) {
        listPersonnelInterimaire.forEachIndexed { index, personnel ->
            val rowToEdit = 14 + index
            val value = personnel.nbHeuresTravaillees

            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(personnel.prenom + " " + personnel.nom)

            when (dateRapportChantier.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                DayOfWeek.MONDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                        value
                    )
                }
                DayOfWeek.TUESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                        value
                    )
                }
                DayOfWeek.WEDNESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                        value
                    )
                }
                DayOfWeek.THURSDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                        value
                    )
                }
                DayOfWeek.FRIDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                        value
                    )
                }
                DayOfWeek.SATURDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                        value
                    )
                }

                else -> Timber.i("ERROR")
            }
        }
    }


    private fun fillSheetWithMaterialData(
        xlWs: Sheet,
        listMaterial: List<Materiel>,
        dateRapportChantier: Instant
    ) {
        listMaterial.forEachIndexed { index, item ->
            val rowToEdit = 20 + index
            val value = item.quantite.toDouble()

            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(item.marque + " " + item.modele)

            when (dateRapportChantier.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                DayOfWeek.MONDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                        value
                    )
                }
                DayOfWeek.TUESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                        value
                    )
                }
                DayOfWeek.WEDNESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                        value
                    )
                }
                DayOfWeek.THURSDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                        value
                    )
                }
                DayOfWeek.FRIDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                        value
                    )
                }
                DayOfWeek.SATURDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                        value
                    )
                }

                else -> Timber.i("ERROR")
            }
        }
    }

    private fun fillSheetWithLocationMaterialData(
        xlWs: Sheet,
        list: List<MaterielLocation>,
        dateRapportChantier: Instant
    ) {
        list.forEachIndexed { index, item ->
            val rowToEdit = 35 + index
            val value = item.quantite.toDouble()

            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(item.fournisseur)
            xlWs.getRow(rowToEdit).getCell(1)
                .setCellValue(item.description)

            when (dateRapportChantier.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                DayOfWeek.MONDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                        value
                    )
                }
                DayOfWeek.TUESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                        value
                    )
                }
                DayOfWeek.WEDNESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                        value
                    )
                }
                DayOfWeek.THURSDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                        value
                    )
                }
                DayOfWeek.FRIDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                        value
                    )
                }
                DayOfWeek.SATURDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                        value
                    )
                }

                else -> Timber.i("ERROR")
            }
        }
    }

    private fun fillSheetWithMateriauxData(
        xlWs: Sheet,
        list: List<Materiaux>,
        dateRapportChantier: Instant
    ) {
        list.forEachIndexed { index, item ->
            val rowToEdit = 43 + index
            val value = item.quantite.toDouble()

            xlWs.getRow(rowToEdit).getCell(0).setCellValue(item.fournisseur)
            xlWs.getRow(rowToEdit).getCell(1).setCellValue(item.description)
            xlWs.getRow(rowToEdit).getCell(2).setCellValue(item.nDeBon)

            when (dateRapportChantier.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                DayOfWeek.MONDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                        value
                    )
                }
                DayOfWeek.TUESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                        value
                    )
                }
                DayOfWeek.WEDNESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                        value
                    )
                }
                DayOfWeek.THURSDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                        value
                    )
                }
                DayOfWeek.FRIDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                        value
                    )
                }
                DayOfWeek.SATURDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                        value
                    )
                }

                else -> Timber.i("ERROR")
            }
        }
    }

    private fun fillSheetWithSousTraitanceData(
        xlWs: Sheet,
        list: List<SousTraitance>,
        dateRapportChantier: Instant
    ) {
        list.forEachIndexed { index, item ->
            val rowToEdit = 52 + index
            val value = item.quantite.toDouble()

            xlWs.getRow(rowToEdit).getCell(0)
                .setCellValue(item.societe)
            xlWs.getRow(rowToEdit).getCell(1)
                .setCellValue(item.prestations)

            when (dateRapportChantier.atZone(ZoneId.systemDefault())?.dayOfWeek) {
                DayOfWeek.MONDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(5).setCellValue(
                        value
                    )
                }
                DayOfWeek.TUESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(6).setCellValue(
                        value
                    )
                }
                DayOfWeek.WEDNESDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(7).setCellValue(
                        value
                    )
                }
                DayOfWeek.THURSDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(8).setCellValue(
                        value
                    )
                }
                DayOfWeek.FRIDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(9).setCellValue(
                        value
                    )
                }
                DayOfWeek.SATURDAY -> value.let {
                    xlWs.getRow(rowToEdit).getCell(10).setCellValue(
                        value
                    )
                }

                else -> Timber.i("ERROR")
            }
        }
    }

    private fun fillSheetWithOtherData(
        xlWs: Sheet,
        list: List<RapportChantier>,
        style: CellStyle
    ) {


        list.forEach { rapportChantier ->

            var value = rapportChantier.commentaire

            if (value.isNotBlank()) value += "\n"

            rapportChantier.tachesEntretien.forEach { it ->
                if (it.checked) value += "${it.description}, "
            }

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


    private fun generateExcelFile(context: Context, xlwb: Workbook): Uri {
        val fileName =
            "$dateBeginning TEST.xlsx"

        val extStorageDirectory: String =
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                .toString()
        val folder = File(
            extStorageDirectory,
            "Rapports de chantier"
        ) // Name of the folder you want to keep your file in the local storage.

        folder.mkdir() //creating the folder

        Timber.i("errorExcel $extStorageDirectory ")

        val file = File(extStorageDirectory, fileName.replace(Regex("""[\\/:*?"<>&|]"""), "_"))
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

        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
    }


    fun onButtonClicked() {
        _navigation.value = WeeklyBuildingReportsViewModel.Navigation.WAITING
    }

}