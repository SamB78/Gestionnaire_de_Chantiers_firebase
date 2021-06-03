package com.example.gestionnairedechantiers.chantiers.gestionChantiers

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentResumeGestionChantierBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber


class ResumeGestionChantierFragment : Fragment() {
    //ViewModel
    val viewModel: GestionChantierViewModel by navGraphViewModels(R.id.gestionChantierNavGraph)

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite->{
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
        }else-> {
            super.onOptionsItemSelected(item)

        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentResumeGestionChantierBinding.inflate(inflater, container, false)


        //ViewModel Binding
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.executePendingBindings()




        viewModel.navigation.observe(viewLifecycleOwner,  { navigation ->
            when (navigation){
                GestionChantierViewModel.GestionNavigation.ENREGISTREMENT_CHANTIER->{
                    val action = GestionChantierNavGraphDirections.actionGestionChantierNavGraphPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionChantierViewModel.GestionNavigation.MODIFICATION->{
                    val action = ResumeGestionChantierFragmentDirections.actionResumeGestionChantierFragmentToGestionChantier1Fragment(null)
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionChantierViewModel.GestionNavigation.ANNULATION->{
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