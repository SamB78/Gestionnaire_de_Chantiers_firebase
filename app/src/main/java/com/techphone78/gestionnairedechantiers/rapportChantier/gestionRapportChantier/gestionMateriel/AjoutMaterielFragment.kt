package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionMateriel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.FragmentAjoutMaterielBinding
import com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel
import com.techphone78.gestionnairedechantiers.utils.Flipper
import com.techphone78.gestionnairedechantiers.utils.Status
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
        binding.errorState.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        viewModel.reinitSearchField()

        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->

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
        }

        viewModel.state.observe(viewLifecycleOwner, {
            binding.vfMain.displayedChild = when (it.status) {

                Status.LOADING -> Flipper.LOADING

                Status.SUCCESS -> Flipper.CONTENT

                Status.ERROR -> {
                    binding.errorState.tvMessageError.text = it.message
                    Flipper.ERROR
                }
            }
        })

        binding.warningMessage.setOnClickListener {
            viewModel.initializeDataMaterielAddable()
        }
        binding.errorState.buttonRetry.setOnClickListener {
            viewModel.initializeDataMaterielAddable()
        }

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