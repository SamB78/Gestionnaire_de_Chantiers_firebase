package com.techphone78.gestionnairedechantiers.rapportChantier.weeklyBuildingReports

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.techphone78.gestionnairedechantiers.databinding.FragmentWeeklyBuildingReportsBinding
import com.techphone78.gestionnairedechantiers.rapportChantier.affichageRapportHebdo.AffichageDetailsRapportChantierViewModel
import timber.log.Timber
import java.util.*

class WeeklyBuildingReportsFragment : Fragment() {

    val viewModel: WeeklyBuildingReportsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentWeeklyBuildingReportsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        val builder = MaterialDatePicker.Builder.datePicker()
        val constraintBuilder = CalendarConstraints.Builder()
            .setValidator(CustomDateValidator())
        // create the date picker
        val datePicker = builder
            .setCalendarConstraints(constraintBuilder.build())
            .build()

        binding.button.setOnClickListener {
            datePicker.show(activity?.supportFragmentManager!!, "MyTAG")
        }

        datePicker.addOnPositiveButtonClickListener {
            viewModel.generateExcel(it)
        }

        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                WeeklyBuildingReportsViewModel.Navigation.EXCEL_DISPLAY -> {
                    viewModel.onButtonClicked()
                    val uri = viewModel.uri
                    val cR = requireContext().contentResolver
                    Timber.i("uri = ${uri.toString()}, mime = ${cR.getType(uri)}")

                    uri.let {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(uri, cR.getType(uri))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    }

                }
                else -> {}
            }
        }

        return binding.root
    }
}

class CustomDateValidator(): CalendarConstraints.DateValidator{
    constructor(parcel: Parcel) : this() {
    }


    override fun isValid(date: Long): Boolean {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.timeInMillis = date
        val dayOfWeek: Int = utc.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.MONDAY
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
    }

    companion object CREATOR : Parcelable.Creator<CustomDateValidator> {
        override fun createFromParcel(parcel: Parcel): CustomDateValidator {
            return CustomDateValidator(parcel)
        }

        override fun newArray(size: Int): Array<CustomDateValidator?> {
            return arrayOfNulls(size)
        }
    }

}