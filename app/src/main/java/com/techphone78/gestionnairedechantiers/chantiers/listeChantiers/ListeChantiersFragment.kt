package com.techphone78.gestionnairedechantiers.chantiers.listeChantiers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.techphone78.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.techphone78.gestionnairedechantiers.databinding.ListeChantiersFragmentBinding
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
                    val action =
                        GestionChantierNavGraphDirections.actionGlobalGestionChantierNavGraph(null)
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                ListeChantiersViewModel.navigationMenu.MODIFICATION -> {
                    val action =
                        ListeChantiersFragmentDirections.actionListeChantiersFragmentToAffichageChantierNavGraph(
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


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchFilter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

        })

//        viewModel.listeChantiersFiltre.observe(viewLifecycleOwner, {
//            Timber.i("test: ${it.size}")
//            binding.personnelListe.adapter!!.notifyDataSetChanged()
//        })


        return binding.root
    }


    override fun onResume() {
        super.onResume()
        viewModel.onResumeFragment()
    }


}