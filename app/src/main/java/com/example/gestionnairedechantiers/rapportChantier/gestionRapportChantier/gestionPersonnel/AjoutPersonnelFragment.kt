package com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionPersonnel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentAjoutPersonnelBinding
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel


class AjoutPersonnelFragment : Fragment() {

    //ViewModel
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentAjoutPersonnelBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_AJOUT_PERSONNEL -> {
                    val action = AjoutPersonnelFragmentDirections.actionAjoutPersonnelFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                else -> {
                }
            }
        })



        return binding.root
    }


}