package com.techphone78.gestionnairedechantiers.rapportChantier.affichageRapportHebdo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.techphone78.gestionnairedechantiers.databinding.FragmentAffichageDetailsRapportChantierBinding
import timber.log.Timber


class AffichageDetailsRapportChantierFragment : Fragment() {

    private lateinit var viewMOdelFactory: AffichageDetailsRapportChantierViewModelFactory
    val viewModel: AffichageDetailsRapportChantierViewModel by viewModels { viewMOdelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val application = requireNotNull(this.activity).application
        val idChantier =
            AffichageDetailsRapportChantierFragmentArgs.fromBundle(requireArguments()).idChantier
        val dateBeginning =
            AffichageDetailsRapportChantierFragmentArgs.fromBundle(requireArguments()).dateBeginning
        val dateEnd =
            AffichageDetailsRapportChantierFragmentArgs.fromBundle(requireArguments()).dateEnd

        viewMOdelFactory = AffichageDetailsRapportChantierViewModelFactory(
            application,
            idChantier,
            dateBeginning,
            dateEnd
        )

        val binding =
            FragmentAffichageDetailsRapportChantierBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                AffichageDetailsRapportChantierViewModel.GestionNavigation.AFFICHAGE_EXCEL -> {
                    viewModel.onButtonClicked()
                    val uri = viewModel.uri
                    val cR = requireContext().contentResolver
                    Timber.i("uri = ${uri.toString()}, mime = ${cR.getType(uri)}")

                    uri.let {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(uri, cR.getType(uri))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    }

                }
            }
        })

        return binding.root
    }

}