package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.MainActivity
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.GestionRapportChantierFragmentBinding
import com.techphone78.gestionnairedechantiers.utils.Flipper
import com.techphone78.gestionnairedechantiers.utils.Status
import com.techphone78.gestionnairedechantiers.utils.hideKeyboard

class GestionRapportChantierFragment : Fragment() {

    private lateinit var viewModelFactory: GestionRapportChantierViewModelFactory
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph) { viewModelFactory }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val idRapportChantier =
            GestionRapportChantierFragmentArgs.fromBundle(requireArguments()).idRapportChantier
        val idChantier =
            GestionRapportChantierFragmentArgs.fromBundle(requireArguments()).idChantier
        val dateRapportChantier =
            GestionRapportChantierFragmentArgs.fromBundle(requireArguments()).date


        viewModelFactory = GestionRapportChantierViewModelFactory(
            idRapportChantier, idChantier, dateRapportChantier
        )

        val binding = GestionRapportChantierFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()

        viewModel.state.observe(viewLifecycleOwner) {
            binding.vfMain.displayedChild = when (it.status) {
                Status.LOADING -> Flipper.LOADING
                Status.SUCCESS -> Flipper.CONTENT
                Status.ERROR -> {
                    binding.errorState.tvMessageError.text = it.message
                    Flipper.ERROR
                }
            }
        }


        //Navigation
        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_GESTION_PERSONNEL -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToGestionPersonnelRapportChantierFragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_GESTION_MATERIEL -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToGestionMaterielRapportChantierFragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_GESTION_MATERIEL_LOCATION -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToGestionMaterielLocationRapportChantierFragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_GESTION_MATERIAUX -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToGestionMateriauxRapportChantierFragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_GESTION_SOUS_TRAITANCE -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToGestionSousTraitanceRapportChantierFragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_AUTRES_INFORMATIONS -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToAutresInformationsFragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_OBSERVATIONS -> {
                    val action =
                        GestionRapportChantierFragmentDirections.actionGestionRapportChantierFragmentToGestionObservationsRapportChantier()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        hideKeyboard(activity as MainActivity)
        viewModel.onResumeGestionChantier()
        super.onResume()
    }


}