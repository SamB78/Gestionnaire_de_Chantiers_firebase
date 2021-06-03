package com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.gestionMaterielLocation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.DialogAddMaterielLocationBinding
import com.example.gestionnairedechantiers.databinding.FragmentGestionMaterielLocationRapportChantierBinding
import com.example.gestionnairedechantiers.rapportChantier.gestionRapportChantier.GestionRapportChantierViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber


class GestionMaterielLocationRapportChantierFragment : Fragment() {

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
            viewModel.onClickButtonAddMaterielLocation()
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
            FragmentGestionMaterielLocationRapportChantierBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        viewModel.navigation.observe(viewLifecycleOwner,  { navigation ->
            when (navigation) {
                GestionRapportChantierViewModel.GestionNavigation.PASSAGE_AJOUT_MATERIEL_LOCATION -> {
                    val customLayout = DialogAddMaterielLocationBinding.inflate(inflater)
                    customLayout.viewModel = viewModel

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Ajouter un matériel de location")
                        .setView(customLayout.root)
                        .setNegativeButton("Annuler") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Valider") { dialog, _ ->
                            viewModel.onClickButtonConfirmationAjoutMaterielLocation()
                            dialog.dismiss()
                        }
                        .show()


                    viewModel.onBoutonClicked()
                }
                GestionRapportChantierViewModel.GestionNavigation.VALIDATION_GESTION_MATERIEL_LOCATION -> {
                    Timber.i("VALIDATION_GESTION_PERSONNEL")
                    val action =
                        GestionMaterielLocationRapportChantierFragmentDirections.actionGestionMaterielLocationRapportChantierFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }

        })


        return binding.root
    }

}