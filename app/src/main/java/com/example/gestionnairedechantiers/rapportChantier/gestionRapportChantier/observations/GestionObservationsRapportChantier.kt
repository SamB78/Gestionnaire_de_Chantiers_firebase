package com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.observations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.MainActivity
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentGestionMateriauxRapportChantierBinding
import com.example.gestionnairedechantiers.databinding.FragmentGestionObservationsRapportChantierBinding
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel
import com.example.gestionnairedechantiers.utils.hideKeyboard


class GestionObservationsRapportChantier : Fragment() {

    //ViewModel
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentGestionObservationsRapportChantierBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.navigation.observe(viewLifecycleOwner,  { navigation ->
            hideKeyboard(activity as MainActivity)
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_OBSERVATIONS -> {
                    val action = GestionObservationsRapportChantierDirections.actionGestionObservationsRapportChantierPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }
        })

        // Inflate the layout for this fragment
        return binding.root
    }
}