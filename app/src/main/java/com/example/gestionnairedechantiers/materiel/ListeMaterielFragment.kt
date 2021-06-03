package com.example.gestionnairedechantiers.materiel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.MainActivity
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.ListeMaterielFragmentBinding
import com.example.gestionnairedechantiers.utils.hideKeyboard
import timber.log.Timber

class ListeMaterielFragment : Fragment() {

    private val viewModel: GestionMaterielViewModel by navGraphViewModels(R.id.gestionMaterielNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = ListeMaterielFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

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

        // Inflate the layout for this fragment
        return binding.root
    }

}