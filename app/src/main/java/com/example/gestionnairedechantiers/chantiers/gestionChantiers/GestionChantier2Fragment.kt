package com.example.gestionnairedechantiers.chantiers.gestionChantiers

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentGestionChantier2Binding
import com.example.gestionnairedechantiers.databinding.PersonnelItemViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber


class GestionChantier2Fragment : Fragment() {

    val viewModel: GestionChantierViewModel by navGraphViewModels(R.id.gestionChantierNavGraph)

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    //Menu Option
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite -> {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Annulation")
                .setMessage("Souhaitez vous annuler la création du nouveau chantier ?")
                .setNegativeButton("QUITTER") { _, _ ->
                    // Respond to negative button press
                    viewModel.onClickButtonAnnuler()
                }
                .setPositiveButton("CONTINUER") { dialog, _ ->
                    // Respond to positive button press
                    dialog.dismiss()
                }
                .show()
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

        val binding = FragmentGestionChantier2Binding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        //DialogConfirmation
        val confirmationDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Vous avez sélectionné:")
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Valider") { dialog, _ ->
                viewModel.onClickChefChantierValide()
                dialog.dismiss()
            }
        val customLayout = layoutInflater.inflate(R.layout.personnel_item_view, null)

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionChantierViewModel.GestionNavigation.PASSAGE_ETAPE3 -> {
                    val action =
                        GestionChantier2FragmentDirections.actionGestionChantier2FragmentToGestionChantier3Fragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionChantierViewModel.GestionNavigation.ANNULATION -> {
                    val action =
                        GestionChantierNavGraphDirections.actionGestionChantierNavGraphPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionChantierViewModel.GestionNavigation.CONFIRMATION_CHEF ->{


                    if (customLayout.parent != null)
                        (customLayout.parent as ViewGroup).removeView(customLayout) // <- fix
                    confirmationDialog.setView(customLayout)
                    val bindingDialog = PersonnelItemViewBinding.inflate(inflater, customLayout as ViewGroup, false)
                    bindingDialog.item = viewModel.chefChantierSelectionne.value
                    confirmationDialog.setView(bindingDialog.root)
                    confirmationDialog.show()

                }
                else -> Timber.e("ERREUR NAVIGATION $navigation")

            }
        })

        // Inflate the layout for this fragment
        return binding.root
    }
}