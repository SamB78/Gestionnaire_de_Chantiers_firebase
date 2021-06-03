package com.example.gestionnairedechantiers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gestionnairedechantiers.auth.AuthActivity
import com.example.gestionnairedechantiers.databinding.ActivityMainBinding
import com.example.gestionnairedechantiers.databinding.LogoHeaderBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        setupNavigation()
        profileLogout()

    }

    private fun profileLogout() {
        viewModel.logoutObseve.observe(this, {
            Timber.i("logout")
            if(it){
                val intent = Intent(applicationContext, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        })
    }

    override fun onSupportNavigateUp() =
        NavigationUI.navigateUp(findNavController(R.id.navHostFragment), binding.drawerLayout)

    private fun setupNavigation(){

        // first find the nav controller
        val navController = findNavController(R.id.navHostFragment)
        setSupportActionBar(binding.toolbar)
        // then setup the action bar, tell it about the DrawerLayout
        setupActionBarWithNavController(navController, binding.drawerLayout)

        findViewById<NavigationView>(R.id.activity_main_nav_view)
            .setupWithNavController(navController)

        activity_main_nav_view.setNavigationItemSelectedListener {
            Timber.i("TEST")
            when (it.itemId) {
                R.id.itemPersonnel -> {
                    Timber.i("itemPersonnel")
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    val action = GestionPersonnelNavGraphDirections.actionGlobalGestionPersonnelNavGraph()
                    findNavController(R.id.navHostFragment).navigate(action)
                    true
                }
                R.id.itemMateriel -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    val action = GestionMaterielNavGraphDirections.actionGlobalGestionMaterielNavGraph()
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