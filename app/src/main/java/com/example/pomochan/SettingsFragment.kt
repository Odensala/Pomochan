package com.example.pomochan

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.pomochan.Constants.ACTION_STOP_SERVICE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var timerOldValue: String
    private lateinit var shortBreakOldValue: String
    private lateinit var longBreakOldValue: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        loadSettings()
        resetDialogOnBackstack()
    }

    private fun listenOnMainPreferenceChanged() {
        findPreference<Preference>("timer")?.setOnPreferenceChangeListener { preference, newValue ->
            Timber.d("newValue: $newValue")
            if (!newValue.equals(timerOldValue) && TimerService.serviceIsRunning.value == true) {
                Timber.d("timerOldValue in conditional: $timerOldValue")
                dialogOnPreferenceChanged()
                timerOldValue = newValue.toString()
            } else {
                Timber.d("Do nothing")
            }
            true
        }
    }

    private fun listenOnShortBreakPreferenceChanged() {
        findPreference<Preference>("shortbreak")?.setOnPreferenceChangeListener { preference, newValue ->
            if (!newValue.equals(shortBreakOldValue) && TimerService.serviceIsRunning.value == true) {
                dialogOnPreferenceChanged()
                shortBreakOldValue = newValue.toString()
            }
            true
        }
    }

    private fun listenOnLongBreakPreferenceChanged() {
        findPreference<Preference>("longbreak")?.setOnPreferenceChangeListener { preference, newValue ->
            if (!newValue.equals(longBreakOldValue) && TimerService.serviceIsRunning.value == true) {
                dialogOnPreferenceChanged()
                longBreakOldValue = newValue.toString()
            }
            true
        }
    }

    private fun dialogOnPreferenceChanged() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.title))
            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                // Respond to positive button press
                //Toast.makeText(context, "Pomochan timer changed", Toast.LENGTH_SHORT).show()
                sendCommandToService(ACTION_STOP_SERVICE)
                TimerService.serviceIsRunning.value = false
            }
            .show()
    }

    private fun loadSettings() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerOldValue = sp.getString("timer", "").toString()
        Timber.d("timerOldValue: $timerOldValue")
        shortBreakOldValue = sp.getString("shortbreak", "").toString()
        longBreakOldValue = sp.getString("longbreak", "").toString()
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        requireContext().startService(intent)
    }

    private fun NavController.isFragmentInBackStack(destinationId: Int) =
        try {
            getBackStackEntry(destinationId)
            true
        } catch (e: Exception) {
            false
        }

    /**
     * Enables reset of timer depending on which timer is running
     */
    private fun resetDialogOnBackstack() {
        when {
            findNavController().isFragmentInBackStack(R.id.mainFragment) -> {
                listenOnMainPreferenceChanged()
            }
            findNavController().isFragmentInBackStack(R.id.breakFragment) -> {
                listenOnShortBreakPreferenceChanged()
            }
            findNavController().isFragmentInBackStack(R.id.breakLongFragment) -> {
                listenOnLongBreakPreferenceChanged()
            }
        }
    }
}
