package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.autresInformations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.AutresInformationsNestedGraphDirections
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.FragmentAutresInformations2Binding
import com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel


class AutresInformations2Fragment : Fragment() {

    //ViewModel
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAutresInformations2Binding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_AUTRES_INFORMATIONS -> {
//                    findNavController().navigate(R.id.action_autresInformations2Fragment_to_gestionRapportChantierFragment)

                    val action =
                        AutresInformationsNestedGraphDirections.actionNavigationPop()
                    findNavController().navigate(action)

                    viewModel.onBoutonClicked()
                }
            }
        })


        // Inflate the layout for this fragment
        return binding.root
    }

}