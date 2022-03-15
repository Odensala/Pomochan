package com.example.pomochan.longbreak

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.example.pomochan.Constants
import com.example.pomochan.R
import com.example.pomochan.TimerService
import com.example.pomochan.databinding.FragmentBreakLongBinding
import com.example.pomochan.utils.TimerUtils
import timber.log.Timber

class BreakLongFragment : Fragment(R.layout.fragment_break_long) {

    private lateinit var binding: FragmentBreakLongBinding
    private lateinit var viewModel: BreakLongViewModel
    private lateinit var timerSetting: String

    private var currentTimeInMillis = 0L
    private var timerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBreakLongBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(BreakLongViewModel::class.java)

        // Method needed to observe when fragment is recreated
        updateCountdown()
        refreshStartTime()

        updateProgressBar()

        // Start/Pause button
        binding.buttonStartPause.setOnClickListener {
            toggleStart()
        }

        // Reset button
        binding.buttonReset.setOnClickListener {
            if (TimerService.serviceIsRunning.value == true) {
                sendCommandToService(Constants.ACTION_STOP_SERVICE)
                TimerService.serviceIsRunning.value = false

                //viewModel.resetProgressBar()
                refreshStartTime()
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("BreakLongFragment destroyed")
    }

    private fun toggleStart() {
        if (timerRunning) {
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
            updateCountdown()
        }
    }

    private fun refreshStartTime() {
        binding.textViewCountdown.text = loadSettings().let { TimerUtils.formatTime(it) }
    }

    // TODO Issue with that the formattedTime is being set instead of the sharedpref after reset
    private fun updateCountdown() {
        // Conditional necessary to make timer update properly when exiting settingsfragment
        if (TimerService.serviceIsRunning.value == true) {
            // Timer text
            TimerService.currentTimeLiveData.observe(viewLifecycleOwner, Observer {
                currentTimeInMillis = it
                val formattedTime = TimerUtils.formatTime(currentTimeInMillis)
                binding.textViewCountdown.text = formattedTime
            })

            TimerService.timerRunning.observe(viewLifecycleOwner, Observer {
                updateRunning(it)
            })
        }
    }

    private fun updateRunning(timerRunning: Boolean) {
        this.timerRunning = timerRunning
        if (timerRunning) {
            binding.buttonStartPause.text = getString(R.string.pause_timer)
        } else {
            binding.buttonStartPause.text = getString(R.string.start_timer)
        }
    }

    private fun sendCommandToService(action: String) {
        TimerService.serviceIsRunning.value = true
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        intent.apply {
            putExtra(Constants.EXTRA_TIMER, loadSettings())
            putExtra(Constants.EXTRA_MAIN_TIMER_ACTIVE, "longbreak")
        }
        requireContext().startService(intent)
    }

    private fun updateProgressBar() {
        var timerSettingInSeconds = loadSettings() / 1000
        TimerService.progressBar.observe(viewLifecycleOwner) {
            binding.progressBarHor.progress = it
            binding.progressBarHor.max = timerSettingInSeconds.toInt()
            Timber.d("progressbar setting: ${timerSettingInSeconds.toInt()}")
        }
    }

    /**
     * Loads shortbreak setting from SharedPreferences
     * @return User set timer setting converted to long
     */
    private fun loadSettings(): Long {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerSetting = sp.getString("longbreak", "1200000").toString()
        return timerSetting.toLong()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.nav_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }
}