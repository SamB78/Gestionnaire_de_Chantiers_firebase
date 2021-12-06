package com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionMateriel

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.FragmentGestionMaterielRapportChantierBinding
import com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel
import com.techphone78.gestionnairedechantiers.utils.Flipper
import com.techphone78.gestionnairedechantiers.utils.Status


class GestionMaterielRapportChantierFragment : Fragment() {


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
            viewModel.onClickButtonAddMateriel()
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
        val binding = FragmentGestionMaterielRapportChantierBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.state.observe(viewLifecycleOwner, {
            binding.vfMain.displayedChild = when (it.status) {

                Status.LOADING -> Flipper.LOADING

                Status.SUCCESS -> Flipper.CONTENT

                Status.ERROR -> {
                    Snackbar.make(
                        binding.mainConstraintLayout,
                        "Impossible de sauvegarder les données, veuillez réessayer",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Flipper.CONTENT
                }
            }
        })

        //Navigation
        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_GESTION_MATERIEL -> {
                    val action = GestionMaterielRapportChantierFragmentDirections.actionGestionMaterielRapportChantierFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_AJOUT_MATERIEL -> {
                    val action = GestionMaterielRapportChantierFragmentDirections.actionGestionMaterielRapportChantierFragmentToAjoutMaterielFragment()

                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }
        })

        return binding.root
    }


}