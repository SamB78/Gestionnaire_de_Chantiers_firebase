package com.example.gestionnairedechantiers.chantiers.gestionChantiers

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.example.gestionnairedechantiers.MainActivity
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentGestionChantier1Binding
import com.example.gestionnairedechantiers.utils.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber


class GestionChantier1Fragment : Fragment() {

    private lateinit var viewModelFactory: GestionChantierViewModelFactory
    val viewModel: GestionChantierViewModel by navGraphViewModels(R.id.gestionChantierNavGraph) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

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
    ): View? {

        val binding = FragmentGestionChantier1Binding.inflate(inflater, container, false)
        binding.executePendingBindings()


        //ViewModelFactory

        val idChantier = GestionChantier1FragmentArgs.fromBundle(requireArguments()).id
        viewModelFactory = GestionChantierViewModelFactory(idChantier)

        binding.viewModel = viewModel
        //ViewModel
        binding.lifecycleOwner = this


        //Navigation
        viewModel.navigation.observe(viewLifecycleOwner, Observer { navigation ->
            hideKeyboard(activity as MainActivity)
            when (navigation) {
                GestionChantierViewModel.GestionNavigation.PASSAGE_ETAPE2 -> {
                    viewModel.onBoutonClicked()
                    val action = GestionChantier1FragmentDirections.actionGestionChantier1FragmentToGestionChantier2Fragment()
                    findNavController().navigate(action)
                }
                GestionChantierViewModel.GestionNavigation.ANNULATION -> {
                    val action = GestionChantierNavGraphDirections.actionGestionChantierNavGraphPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                else -> Timber.e("ERREUR NAVIGATION $navigation")
            }
        })

        return binding.root
    }

}