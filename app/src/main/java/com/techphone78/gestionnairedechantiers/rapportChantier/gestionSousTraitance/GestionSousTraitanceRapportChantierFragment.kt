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
import com.google.android.material.snackbar.Snackbar
import com.techphone78.gestionnairedechantiers.utils.Flipper
import com.techphone78.gestionnairedechantiers.utils.Status
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

        viewModel.state.observe(viewLifecycleOwner) {
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
        }

        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_AJOUT_SOUS_TRAITANCE,
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_MODIFICATIONS_SOUS_TRAITANCE -> {
                    val customLayout = DialogAddSousTraitanceBinding.inflate(inflater)
                    customLayout.viewModel = viewModel

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Ajouter un sous traitant")
                        .setView(customLayout.root)
                        .setNegativeButton("Annuler") { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Valider") { dialog, which ->
                            viewModel.onClickButtonConfirmationAjoutSousTraitance()
                            viewModel.successDialog.observe(viewLifecycleOwner) {
                                if (it) dialog.dismiss()
                            }
                        }.show()

                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_GESTION_SOUS_TRAITANCE -> {
                    Timber.i("VALIDATION_GESTION_PERSONNEL")
                    val action =
                        GestionSousTraitanceRapportChantierFragmentDirections.actionGestionSousTraitanceRapportChantierFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                else -> {}
            }

        }


        return binding.root
    }
}