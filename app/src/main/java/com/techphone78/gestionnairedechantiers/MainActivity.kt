package com.techphone78.gestionnairedechantiers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.techphone78.gestionnairedechantiers.auth.AuthActivity
import com.techphone78.gestionnairedechantiers.databinding.ActivityMainBinding
import com.techphone78.gestionnairedechantiers.databinding.LogoHeaderBinding
import com.techphone78.gestionnairedechantiers.utils.Status
import com.google.android.material.navigation.NavigationView
import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.techphone78.gestionnairedechantiers.rapportChantier.weeklyBuildingReports.WeeklyBuildingReportsFragmentDirections
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel

        val headerBinding = LogoHeaderBinding.bind(binding.activityMainNavView.getHeaderView(0))
        headerBinding.viewModel = viewModel
        headerBinding.executePendingBindings()
        headerBinding.lifecycleOwner = this

//        binding.activityMainNavView.getHeaderView(0)


        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        );

        Timber.plant(Timber.DebugTree())

        // first find the nav controller


        viewModel.state.observe(this) {
            if (it.status == Status.SUCCESS) {
                setupNavGraph()
                setupNavigation(viewModel.user.value!!.userData!!)
                binding.linearLayout.visibility = View.VISIBLE
                binding.loadingState.visibility = View.INVISIBLE
            }
        }
        profileLogout()

    }

    private fun profileLogout() = viewModel.logoutObseve.observe(this) {
        Timber.i("logout")
        if (it) {
            val intent = Intent(applicationContext, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("error", viewModel.state.value!!.message)
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp() =
        NavigationUI.navigateUp(findNavController(R.id.navHostFragment), binding.drawerLayout)

    private fun setupNavigation(personnel: Personnel) {


        val navController = findNavController(R.id.navHostFragment)

        setSupportActionBar(binding.toolbar)
        // then setup the action bar, tell it about the DrawerLayout
        setupActionBarWithNavController(navController, binding.drawerLayout)

        findViewById<NavigationView>(R.id.activity_main_nav_view)
            .setupWithNavController(navController)

        if (personnel.administrateur) {
            activity_main_nav_view.inflateMenu(R.menu.activity_main_menu_drawer)
            activity_main_nav_view.setNavigationItemSelectedListener {
                Timber.i("TEST")
                when (it.itemId) {
                    R.id.itemPersonnel -> {
                        Timber.i("itemPersonnel")
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        val action =
                            GestionPersonnelNavGraphDirections.actionGlobalGestionPersonnelNavGraph()
                        findNavController(R.id.navHostFragment).navigate(action)
                        true
                    }
                    R.id.itemMateriel -> {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        val action =
                            GestionMaterielNavGraphDirections.actionGlobalGestionMaterielNavGraph()
                        findNavController(R.id.navHostFragment).navigate(action)
                        true
                    }
                    R.id.itemAffichageRapportChantiers -> {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        val action =
                            WeeklyBuildingReportsFragmentDirections.actionGlobalWeeklyBuildingReportsFragment()
                        findNavController(R.id.navHostFragment).navigate(action)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }

    private fun setupNavGraph() {

        //Setup the navGraph for this activity
        val navController = findNavController(R.id.navHostFragment)
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.navigation)
        navController.graph = graph

    }
}