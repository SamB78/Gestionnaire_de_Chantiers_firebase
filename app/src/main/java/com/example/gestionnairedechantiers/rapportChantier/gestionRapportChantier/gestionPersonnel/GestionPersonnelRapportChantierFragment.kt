package com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionPersonnel

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentGestionPersonnelRapportChantierBinding
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel


class GestionPersonnelRapportChantierFragment : Fragment() {

    //ViewModel
    val viewModel: GestionRapportChantierViewModel by navGraphViewModels(R.id.gestionRapportChantierNavGraph)

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gestion_materiel, menu)
    }

    //Menu Option
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_add_materiel -> {
            viewModel.onClickButtonAddPersonnel()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentGestionPersonnelRapportChantierBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        //Navigation
        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_GESTION_PERSONNEL -> {
                    val action = GestionPersonnelRapportChantierFragmentDirections.actionGestionPersonnelRapportChantierFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_AJOUT_PERSONNEL -> {
                    val action = GestionPersonnelRapportChantierFragmentDirections.actionGestionPersonnelRapportChantierFragmentToAjoutPersonnelFragment()

                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeGestionPersonnelFragment()
    }
}