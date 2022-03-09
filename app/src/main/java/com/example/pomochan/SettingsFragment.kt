package com.example.pomochan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.pomochan.Constants.ACTION_STOP_SERVICE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var timerOldValue: String
    private var shortBreakOldValue = 0L
    private var longBreakOldValue = 0L

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        // TODO issue where user resets timer, changes time and reset prompt
        // TODO incorrectly comes up again. I think its because old TimerService.serviceIsRunning value is being used.
        timerOldValue = loadSettings()
        Timber.d("timerOldValue is ${timerOldValue}")
        if (TimerService.serviceIsRunning) {
            Timber.d("TimerService is ${TimerService.serviceIsRunning}")
            listenOnPreferenceChanged()
            Timber.d("TimerService end is ${TimerService.serviceIsRunning}")
        }
    }

    // TODO I think the issue is here where the old value is compared
    // TODO therefore triggering the dialog
    private fun listenOnPreferenceChanged() {
        findPreference<Preference>("timer")?.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue !== timerOldValue) {
                dialogOnPreferenceChanged()
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
                TimerService.serviceIsRunning = false

            }
            .show()
    }

    private fun loadSettings(): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerOldValue = sp.getString("timer", "").toString()
        return timerOldValue
    }

    private fun sendCommandToService(action: String) {
        // serviceIsRunning is set to false here because
        // service can only be stopped from this screen
        TimerService.serviceIsRunning = false
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        requireContext().startService(intent)
    }
}