package com.example.pomochan

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.pomochan.Constants.ACTION_STOP_SERVICE
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var timerOldValue: String
    private var shortBreakOldValue = 0L
    private var longBreakOldValue = 0L

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        // TODO make a conditional statement that asks if user wants to proceed with changing the setting and cancelling timer
        // TODO if user proceeds change value
        // TODO if user cancels revert previous value
        timerOldValue = loadSettings()
        if (TimerService.serviceIsRunning) {
            listenOnPreferenceChanged()
        }
    }

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
            }
            .show()
    }

    private fun loadSettings(): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerOldValue = sp.getString("timer", "").toString()
        return timerOldValue
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        requireContext().startService(intent)
    }
}