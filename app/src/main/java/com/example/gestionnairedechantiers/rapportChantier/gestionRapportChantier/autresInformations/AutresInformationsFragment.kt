package com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.autresInformations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentAutresInformationsBinding
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel

class AutresInformationsFragment : Fragment() {

    //ViewModel
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAutresInformationsBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.navigation.observe(viewLifecycleOwner,  { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_ETAPE_2_AUTRES_INFORMATIONS -> {

                    val action = AutresInformationsFragmentDirections.actionAutresInformationsFragmentToAutresInformations2Fragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }
        })

        // Inflate the layout for this fragment
        return binding.root
    }


}