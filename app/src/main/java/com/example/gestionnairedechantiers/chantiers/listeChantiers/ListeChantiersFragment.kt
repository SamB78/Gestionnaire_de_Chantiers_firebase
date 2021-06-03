package com.example.gestionnairedechantiers.chantiers.listeChantiers

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.ListeChantiersFragmentBinding
import timber.log.Timber

class ListeChantiersFragment : Fragment() {

    private val viewModel: ListeChantiersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ListeChantiersFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this


        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                ListeChantiersViewModel.navigationMenu.CREATION -> {
                    val action = GestionChantierNavGraphDirections.actionGlobalGestionChantierNavGraph(null)
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                ListeChantiersViewModel.navigationMenu.MODIFICATION -> {
                    val action = ListeChantiersFragmentDirections.actionListeChantiersFragmentToAffichageChantierNavGraph(
                        viewModel.chantierId
                    )
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                ListeChantiersViewModel.navigationMenu.EN_ATTENTE -> {
                }
                else -> Timber.e("ERROR NAVIGATION ListeCchantiersFragments:  $navigation")

            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeFragment()
    }


}