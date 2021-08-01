package com.techphone78.gestionnairedechantiers.rapportChantier.listeRapportsChantier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.AffichageChantierNavGraphDirections
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.chantiers.affichageChantier.AffichageChantierViewModel
import com.techphone78.gestionnairedechantiers.databinding.FragmentListeRapportsChantierBinding
import com.google.android.material.datepicker.MaterialDatePicker
import timber.log.Timber


class ListeRapportsChantierFragment : Fragment() {

    //ViewModel
    val viewModel: AffichageChantierViewModel by navGraphViewModels(R.id.affichageChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentListeRapportsChantierBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        // Create the date picker builder and set the title
        val builder = MaterialDatePicker.Builder.datePicker()
        // create the date picker
        val datePicker = builder.build()
        var date: Long = 0
        // set listener when date is selected
        datePicker.addOnPositiveButtonClickListener {

            // Create calendar object and set the date to be that returned from selectio
            date = it
            viewModel.onDateSelected()
        }

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                AffichageChantierViewModel.NavigationMenu.SELECTION_PAR_DATE -> {

                    val action =
                        AffichageChantierNavGraphDirections.actionGlobalGestionRapportChantierNavGraph(
                            null,
                            date,
                            viewModel.chantier.value?.numeroChantier
                        )
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                AffichageChantierViewModel.NavigationMenu.SELECTION_PAR_ID -> {
                    Timber.i("id rapport chaniter = ${viewModel.idRapportChantier.value!!}")
                    val action =
                        AffichageChantierNavGraphDirections.actionGlobalGestionRapportChantierNavGraph(
                            viewModel.idRapportChantier.value!!,
                            -1L,
                            viewModel.chantier.value!!.numeroChantier!!
                        )
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }

                AffichageChantierViewModel.NavigationMenu.SELECTION_DATE -> {
                    viewModel.onBoutonClicked()
                    datePicker.show(activity?.supportFragmentManager!!, "MyTAG")
//                    dpd?.show()
                }
                else -> {
                    Timber.i("Nothing")
                }
            }
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
       viewModel.onResumeLoadData()
    }
}