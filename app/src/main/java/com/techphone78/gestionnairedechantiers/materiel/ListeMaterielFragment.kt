package com.techphone78.gestionnairedechantiers.materiel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.MainActivity
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.ListeMaterielFragmentBinding
import com.techphone78.gestionnairedechantiers.utils.*
import timber.log.Timber

class ListeMaterielFragment : Fragment() {

    private val viewModel: GestionMaterielViewModel by navGraphViewModels(R.id.gestionMaterielNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = ListeMaterielFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.errorState.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        viewModel.state.observe(viewLifecycleOwner, {
            binding.vfMain.displayedChild = when (it.status) {

                Status.LOADING -> Flipper.LOADING

                Status.SUCCESS -> Flipper.CONTENT

                Status.ERROR -> {
                    binding.errorState.tvMessageError.text = it.message
                    Flipper.ERROR
                }
            }
        })

        binding.warningMessage.setOnClickListener {
            viewModel.reloadDataFromServer()
        }


        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            hideKeyboard(activity as MainActivity)
            when (navigation) {
                GestionMaterielViewModel.NavigationMenu.EDIT_MATERIEL -> {
                    viewModel.onBoutonClicked()
                    val action =
                        ListeMaterielFragmentDirections.actionListeMaterielFragmentToGestionMaterielFragment()
                    findNavController().navigate(action)
                }
                else -> Timber.e("ERROR NAVIGATION LISTEMATERIELFRAGMENT $navigation")
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

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateTypeView(TypeView.LIST)
    }

}