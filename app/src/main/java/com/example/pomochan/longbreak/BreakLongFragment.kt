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

        binding.textViewCountdown.text = loadSettings().let { TimerUtils.formatTime(it) }
        Timber.d(loadSettings().let { TimerUtils.formatTime(it) })

        loadSettings()

        if (TimerService.serviceIsRunning) {
            Timber.d("timer is running")
        }

        observeTimerRunning()

        /*// Progressbar
        viewModel.progressBarLiveData.observe(viewLifecycleOwner, Observer {
            binding.progressBarHor.progress = it
        })*/

        // Start/Pause button
        binding.buttonStartPause.setOnClickListener {
            toggleStart()
        }

        // Reset button
        binding.buttonReset.setOnClickListener {
            sendCommandToService(Constants.ACTION_STOP_SERVICE)
            TimerService.serviceIsRunning = false

            //viewModel.resetProgressBar()
            currentTimeInMillis = loadSettings()
            Timber.d(" currentimeinmillis: $currentTimeInMillis")
            //binding.textViewCountdown.text = TimerUtils.formatTime(currentTimeInMillis)
            binding.textViewCountdown.text = loadSettings().let { TimerUtils.formatTime(it) }
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
        }
    }

    // TODO Issue with that the formattedTime is being set instead of the sharedpref after reset
    private fun timerServiceObservers() {
        // Timer text
        TimerService.currentTimeLiveData.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TimerUtils.formatTime(currentTimeInMillis)
            binding.textViewCountdown.text = formattedTime
            Timber.d("$formattedTime")
        })
    }

    private fun observeTimerRunning() {
        TimerService.timerRunning.observe(viewLifecycleOwner, Observer {
            updateRunning(it)
            timerServiceObservers()
        })
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
        TimerService.serviceIsRunning = true
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        intent.apply {
            putExtra(Constants.EXTRA_TIMER, loadSettings())
        }
        requireContext().startService(intent)
    }

    /**
     * Loads shortbreak setting from SharedPreferences
     * @return User set timer setting converted to long
     */
    private fun loadSettings(): Long {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerSetting = sp.getString("longbreak", "").toString()
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