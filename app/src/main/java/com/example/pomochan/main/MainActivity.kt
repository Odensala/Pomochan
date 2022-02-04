package com.example.pomochan.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pomochan.Constants.ACTION_SHOW_MAIN_FRAGMENT
import com.example.pomochan.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToMainFragmentIfNeeded(intent)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainFragment,
                R.id.breakFragment,
                R.id.breakLongFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)

        // Hides bottomNavigationView when applicable
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.settingsFragment -> bottomNavigationView.visibility = View.GONE
                R.id.feedbackFragment -> bottomNavigationView.visibility = View.GONE
                R.id.aboutFragment -> bottomNavigationView.visibility = View.GONE
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToMainFragmentIfNeeded(intent)
    }

    /**
     * Enables up button navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    private fun navigateToMainFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_MAIN_FRAGMENT) {
            val navController = this.findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.action_global_mainFragment)
        }
    }
}