package com.techphone78.gestionnairedechantiers.rapportChantier.gestionSousTraitance

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.DialogAddSousTraitanceBinding
import com.techphone78.gestionnairedechantiers.databinding.FragmentGestionSousTraitanceRapportChantierBinding
import com.techphone78.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber


class GestionSousTraitanceRapportChantierFragment : Fragment() {


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
            viewModel.onClickButtonAddSousTraitance()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding =
            FragmentGestionSousTraitanceRapportChantierBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_AJOUT_SOUS_TRAITANCE -> {
                    val customLayout = DialogAddSousTraitanceBinding.inflate(inflater)
                    customLayout.viewModel = viewModel

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Ajouter un matériel de location")
                        .setView(customLayout.root)
                        .setNegativeButton("Annuler") { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Valider") { dialog, which ->
                            viewModel.onClickButtonConfirmationAjoutSousTraitance()
                            dialog.dismiss()
                        }
                        .show()


                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_GESTION_SOUS_TRAITANCE -> {
                    Timber.i("VALIDATION_GESTION_PERSONNEL")
                    val action =
                        GestionSousTraitanceRapportChantierFragmentDirections.actionGestionSousTraitanceRapportChantierFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }

        })


        return binding.root
    }
}