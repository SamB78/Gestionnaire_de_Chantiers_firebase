package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionPersonnel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.FragmentAjoutPersonnelBinding
import com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel


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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchFilterPersonnel(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

        })

        return binding.root
    }


}