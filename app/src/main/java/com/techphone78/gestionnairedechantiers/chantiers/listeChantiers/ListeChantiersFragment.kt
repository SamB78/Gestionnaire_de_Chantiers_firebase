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
import com.techphone78.gestionnairedechantiers.utils.Flipper
import com.techphone78.gestionnairedechantiers.utils.Status
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


        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                ListeChantiersViewModel.navigationMenu.CREATION -> {
                    val action =
                        GestionChantierNavGraphDirections.actionGlobalGestionChantierNavGraph(null)
                    findNavController().navigate(action)
                    viewModel.onButtonClicked()
                }
                ListeChantiersViewModel.navigationMenu.MODIFICATION -> {
                    val action =
                        ListeChantiersFragmentDirections.actionListeChantiersFragmentToAffichageChantierNavGraph(
                            viewModel.chantierId
                        )
                    findNavController().navigate(action)
                    viewModel.onButtonClicked()
                }
                ListeChantiersViewModel.navigationMenu.EN_ATTENTE -> {
                }
                else -> Timber.e("ERROR NAVIGATION ListeCchantiersFragments:  $navigation")

            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            binding.vfMain.displayedChild = when (it.status) {

                Status.LOADING -> Flipper.LOADING

                Status.SUCCESS -> Flipper.CONTENT

                Status.ERROR -> {
                    binding.errorState.tvMessageError.text = it.message
                    Flipper.ERROR
                }
            }
        }

        binding.warningMessage.setOnClickListener {
            viewModel.reloadDataFromServer()
        }


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