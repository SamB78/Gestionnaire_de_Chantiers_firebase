package com.example.gestionnairedechantiers.chantiers.gestionChantiers

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.GestionChantierNavGraphDirections
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.FragmentGestionChantier3Binding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class GestionChantier3Fragment : Fragment() {
    val viewModel: GestionChantierViewModel by navGraphViewModels(R.id.gestionChantierNavGraph)

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite->{
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Annulation")
                .setMessage("Souhaitez vous annuler la création du nouveau chantier ?")
                .setNegativeButton("QUITTER") { _, _ ->
                    // Respond to negative button press
                    viewModel.onClickButtonAnnuler()
                }
                .setPositiveButton("CONTINUER") { dialog, _ ->
                    // Respond to positive button press
                    dialog.dismiss()
                }
                .show()
            true
        }else-> {
            super.onOptionsItemSelected(item)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentGestionChantier3Binding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this



//        //RecyclerView
//        val adapter = ListePersonnelAdapter(
//            ListePersonnelListener { personnelId ->
//                Toast.makeText(context, "$personnelId", Toast.LENGTH_SHORT).show()
//                viewModel.onSelectionPersonnel(personnelId.toLong())
//            })
//
//        binding.personnelListe.adapter = adapter
//        val manager = GridLayoutManager(activity, 1, GridLayoutManager.VERTICAL, false)
//        binding.personnelListe.layoutManager = manager
//
//        viewModel.listePersonnel.observe(
//            viewLifecycleOwner,
//            Observer { listePersonnel ->
//                listePersonnel?.let {
//                    adapter.submitList(it)
//                    Timber.i("TEST Observation listePersonnelAAfficher")
//                    adapter.notifyDataSetChanged()
//                }
//            })

        //Navigation
        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                GestionChantierViewModel.GestionNavigation.CONFIRMATION_ETAPE3 -> {
                   val action = GestionChantier3FragmentDirections.actionGestionChantier3FragmentToGestionChantier4Fragment()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }

                GestionChantierViewModel.GestionNavigation.ANNULATION -> {
                    val action =
                        GestionChantierNavGraphDirections.actionGestionChantierNavGraphPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
            }

        })
        // Inflate the layout for this fragment
        return binding.root
    }
}