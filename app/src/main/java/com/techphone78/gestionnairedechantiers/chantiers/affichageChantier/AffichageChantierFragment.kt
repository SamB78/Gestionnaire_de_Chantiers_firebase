package com.techphone78.gestionnairedechantiers.chantiers.affichageChantier

import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.techphone78.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.techphone78.gestionnairedechantiers.MainActivity
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.AffichageChantierFragmentBinding
import com.techphone78.gestionnairedechantiers.rapportChantier.listeRapportsChantier.ListeRapportsChantierFragment
import com.techphone78.gestionnairedechantiers.utils.Flipper
import com.techphone78.gestionnairedechantiers.utils.Status
import com.techphone78.gestionnairedechantiers.utils.hideKeyboard

class AffichageChantierFragment : Fragment() {

    private val fragmentList = arrayListOf(
        DetailChantierFragment(),
        ListeRapportsChantierFragment()
    )


    private lateinit var viewModelFactory: AffichageChantierViewModelFactory
    val viewModel: AffichageChantierViewModel by navGraphViewModels(R.id.affichageChantierNavGraph) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_chantier, menu)
    }

    //Menu Option
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_export -> {
            viewModel.onClickButtonExportData()
            true
        }
        R.id.action_edit -> {
            viewModel.onClickButtonEditChantier()
            true
        }
        R.id.action_archive -> {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Annulation")
                .setMessage("Souhaitez vous archiver le chantier ?")
                .setNegativeButton("ARCHIVER") { _, _ ->
                    // Respond to negative button press
                    viewModel.onClickButtonArchive()
                    findNavController().popBackStack()
                }
                .setPositiveButton("ANNULER") { dialog, _ ->
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
    ): View {
        val chantierId = AffichageChantierFragmentArgs.fromBundle(requireArguments()).id
        viewModelFactory = AffichageChantierViewModelFactory(chantierId)

        val binding = AffichageChantierFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.errorState.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this


        val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()
        datePicker.addOnPositiveButtonClickListener {
            viewModel.onDatesToExportSelected(it.first!!, it.second!!)
        }

        val tabsAffichageChantierAdapter = AffichageChantierViewerPagerAdapter(this, fragmentList)
        binding.pager.adapter = tabsAffichageChantierAdapter

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "INFORMATIONS"

                }
                1 -> {
                    tab.text = "RAPPORTS DE CHANTIER"
                }
            }
        }.attach()

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

        viewModel.isUserAdmin.observe(viewLifecycleOwner) {
            if (it) {
                setHasOptionsMenu(true)
            }
        }


        viewModel.navigation.observe(
            viewLifecycleOwner
        ) { navigation ->

            when (navigation) {
                AffichageChantierViewModel.NavigationMenu.SELECTION_DATE_EXPORT -> {
                    datePicker.show(activity?.supportFragmentManager!!, "MyTAG")
                    viewModel.onBoutonClicked()

                }
                AffichageChantierViewModel.NavigationMenu.EXPORT -> {
                    val action =
                        AffichageChantierFragmentDirections.actionGlobalAffichageDetailsRapportChantierFragment(
                            viewModel.chantier.value!!.numeroChantier!!,
                            viewModel.dateDebut.value!!,
                            viewModel.dateFin.value!!
                        )
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()

                }
                AffichageChantierViewModel.NavigationMenu.EDIT -> {

                    val action =
                        GestionChantierNavGraphDirections.actionGlobalGestionChantierNavGraph(
                            viewModel.chantier.value!!.numeroChantier!!
                        )
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                else -> {}
            }
        }


        return binding.root
    }

    override fun onResume() {
        viewModel.onResumeLoadData()
        hideKeyboard(activity as MainActivity)
        super.onResume()
    }

}