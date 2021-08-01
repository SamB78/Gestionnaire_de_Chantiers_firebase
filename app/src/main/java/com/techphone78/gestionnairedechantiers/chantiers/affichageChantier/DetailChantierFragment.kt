package com.techphone78.gestionnairedechantiers.chantiers.affichageChantier

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.FragmentDetailChantierBinding


class DetailChantierFragment : Fragment() {

    val viewModel: AffichageChantierViewModel by navGraphViewModels(R.id.affichageChantierNavGraph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentDetailChantierBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        return binding.root
    }
}