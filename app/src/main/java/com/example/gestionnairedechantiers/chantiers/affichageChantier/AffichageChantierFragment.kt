package com.example.gestionnairedechantiers.chantiers.affichageChantier

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.example.gestionnairedechantiers.MainActivity
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.AffichageChantierFragmentBinding
import com.example.gestionnairedechantiers.rapportChantier.listeRapportsChantier.ListeRapportsChantierFragment
import com.example.gestionnairedechantiers.utils.hideKeyboard
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator

class AffichageChantierFragment : Fragment() {

    private val fragmentList = arrayListOf(
        DetailChantierFragment(),
        ListeRapportsChantierFragment()
    )



    private lateinit var viewModelFactory: AffichageChantierViewModelFactory
    val viewModel: AffichageChantierViewModel by navGraphViewModels(R.id.affichageChantierNavGraph) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
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

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->

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
                AffichageChantierViewModel.NavigationMenu.EDIT ->{

                    val action = GestionChantierNavGraphDirections.actionGlobalGestionChantierNavGraph(viewModel.chantier.value!!.numeroChantier!!)
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }
        }
        )


        return binding.root
    }

    override fun onResume() {
        viewModel.onResumeLoadData()
        hideKeyboard(activity as MainActivity)
        super.onResume()
    }


}