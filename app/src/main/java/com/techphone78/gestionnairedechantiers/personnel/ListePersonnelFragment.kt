package com.techphone78.gestionnairedechantiers.personnel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.MainActivity
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.ListePersonnelFragmentBinding
import com.techphone78.gestionnairedechantiers.utils.hideKeyboard
import timber.log.Timber

class ListePersonnelFragment : Fragment() {

    private val viewModel: GestionPersonnelViewModel by navGraphViewModels(R.id.gestionPersonnelNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = ListePersonnelFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        viewModel.navigationPersonnel.observe(viewLifecycleOwner, { navigation ->
            hideKeyboard(activity as MainActivity)
            when (navigation) {
                GestionPersonnelViewModel.NavigationMenu.EDIT_PERSONNEL -> {
                    viewModel.onBoutonClicked()
                    val action =
                        ListePersonnelFragmentDirections.actionListePersonnelFragmentToGestionPersonnelFragment()
                    findNavController().navigate(action)
                }
                else -> Timber.e("ERROR NAVIGATION LISTEPERSONNELFRAGMENT $navigation")
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

        return binding.root
    }
}
