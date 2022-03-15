package com.example.pomochan.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pomochan.Constants
import com.example.pomochan.Constants.ACTION_SHOW_LONGBREAK_FRAGMENT
import com.example.pomochan.Constants.ACTION_SHOW_MAIN_FRAGMENT
import com.example.pomochan.Constants.ACTION_SHOW_SHORTBREAK_FRAGMENT
import com.example.pomochan.R
import com.example.pomochan.TimerService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.i("MainActivity created!")


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // TODO figure out why we can't call navHostFragment from XML
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainFragment,
                R.id.breakFragment,
                R.id.breakLongFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)
        navigateToFragmentIfNeeded(intent)

        // TODO This whole part can probably be made more efficient (At least it werkz lol)
        // Bottom navigation selectedListener
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {

                // MainFragment
                R.id.mainFragment -> {
                    // Checks if timer is running and that user has clicked a different
                    // bottom navigation button than currently selected
                    // TODO Need to figure out a way so when user clicks already selected nav option
                    // TODO nothing should happen. The it.itemId != R.id.mainFragment option
                    // TODO didn't work properly

                    if (TimerService.serviceIsRunning.value == true && !it.isChecked) {
                        if (dialogOnPreferenceChanged(R.id.mainFragment, navController)) {
                            // If user cancelled dialog
                            return@setOnItemSelectedListener false
                        }
                    } else {
                        navController.popBackStack()
                        navController.navigate(R.id.mainFragment)
                        return@setOnItemSelectedListener true
                    }
                    Timber.d("Pomochan clicked")
                }

                // BreakFragment
                R.id.breakFragment -> {
                    if (TimerService.serviceIsRunning.value == true && !it.isChecked) {
                        if (dialogOnPreferenceChanged(R.id.breakFragment, navController)) {
                            return@setOnItemSelectedListener false
                        }
                    } else {
                        navController.popBackStack()
                        navController.navigate(R.id.breakFragment)
                        return@setOnItemSelectedListener true
                    }
                    Timber.d("Short break clicked")
                }

                // BreakLongFragment
                R.id.breakLongFragment -> {
                    if (TimerService.serviceIsRunning.value == true && !it.isChecked) {
                        if (dialogOnPreferenceChanged(R.id.breakLongFragment, navController)) {
                            return@setOnItemSelectedListener false
                        }
                    } else {
                        navController.popBackStack()
                        navController.navigate(R.id.breakLongFragment)
                        return@setOnItemSelectedListener true
                    }
                    Timber.d("Long break clicked")
                }
            }
            false
        }

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

    /**
     * If activity is not destroyed
     * makes sure we still navigate correctly when
     * notification is clicked
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToFragmentIfNeeded(intent)
    }

    /**
     * Displays reset dialog
     * @fragmentId fragment we want to navigate to
     * @return dialogCancel which returns Boolean depending on user's chosen dialog option
     */
    private fun dialogOnPreferenceChanged(fragmentId: Int, navController: NavController): Boolean {
        var dialogCancel = false

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title))
            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                Timber.d("Negative button pressed")
                dialogCancel = true
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                // Resets timer on positive button press
                navController.navigate(fragmentId)
                sendCommandToService(Constants.ACTION_STOP_SERVICE)
                TimerService.serviceIsRunning.value = false
                dialogCancel = false
            }
            .show()
        return dialogCancel
    }

    private fun resetDialogOnBottomNav() {
        // TODO figure out a way for navController to be incorporated here
    }

    /**
     * Stop service intent
     */
    private fun sendCommandToService(action: String) {
        val intent = Intent(this, TimerService::class.java).also {
            it.action = action
        }
        this.startService(intent)
    }

    override fun onDestroy() {
        Timber.i("MainActivity destroyed!")
        super.onDestroy()
    }

    /**
     * Enables up button navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    /**
     * Handles navigation from notification
     */
    private fun navigateToFragmentIfNeeded(intent: Intent?) {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        when (intent?.action) {
            ACTION_SHOW_MAIN_FRAGMENT -> {
                navController.navigate(R.id.action_global_mainFragment)
            }
            ACTION_SHOW_SHORTBREAK_FRAGMENT -> {
                navController.navigate(R.id.action_global_shortbreakFragment)
            }
            ACTION_SHOW_LONGBREAK_FRAGMENT -> {
                navController.navigate(R.id.action_global_longbreakFragment)
            }
        }
    }
}