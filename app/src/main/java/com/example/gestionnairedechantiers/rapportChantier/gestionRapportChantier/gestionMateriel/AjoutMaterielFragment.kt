package com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionMateriel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentAjoutMaterielBinding
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionPersonnel.AjoutPersonnelFragmentDirections
import timber.log.Timber


class AjoutMaterielFragment : Fragment() {

    //ViewModel
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentAjoutMaterielBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->

            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_AJOUT_MATERIEL -> {
                   Timber.i("Passage retour après validation")
                    val action = AjoutMaterielFragmentDirections.actionAjoutMaterielFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                else -> {
                }
            }
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchFilterMateriel(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

        })

        return binding.root
    }
}