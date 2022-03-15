package com.example.pomochan.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.example.pomochan.Constants.ACTION_PAUSE_SERVICE
import com.example.pomochan.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.pomochan.Constants.ACTION_STOP_SERVICE
import com.example.pomochan.Constants.EXTRA_MAIN_TIMER_ACTIVE
import com.example.pomochan.Constants.EXTRA_TIMER
import com.example.pomochan.R
import com.example.pomochan.TimerService
import com.example.pomochan.databinding.FragmentMainBinding
import com.example.pomochan.utils.TimerUtils
import timber.log.Timber

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    private lateinit var timerSetting: String

    private var currentTimeInMillis = 0L

    // Decides whether timer is running or paused
    private var timerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreate called MainFragment")
        // Necessary to check else app crashes because service is reset outside this fragment
        // and the value here is not checked without this
        if (TimerService.serviceIsRunning.value == false) {
            timerRunning = false
        }

        // Method needed to observe when fragment is recreated
        updateCountdown()

        binding = FragmentMainBinding.inflate(inflater, container, false)

        val application = requireNotNull(activity).application
        viewModelFactory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        // Sets correct start time
        refreshStartTime()

        // Start/Pause button
        binding.buttonStartPause.setOnClickListener {
            toggleStart()
        }

        updateProgressBar()

        // Reset button
        binding.buttonReset.setOnClickListener {
            if (TimerService.serviceIsRunning.value == true) {
                sendCommandToService(ACTION_STOP_SERVICE)
                TimerService.serviceIsRunning.value = false

                //viewModel.resetProgressBar()
                refreshStartTime()
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    // TODO because we are cancelling the timer outside of this (in SettingsFragment), what happens is that
    // TODO timerRunning is not properly updated to false causing it to send a PAUSE
    // TODO request and then crashing the app because the PAUSE action is called on an
    // TODO uninitialized countdowntimer object
    private fun toggleStart() {
        if (timerRunning) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            updateCountdown()
        }
    }

    private fun refreshStartTime() {
        binding.textViewCountdown.text = loadSettings().let { TimerUtils.formatTime(it) }
        Timber.d("refreshStartTime: ${loadSettings().let { TimerUtils.formatTime(it) }}")
    }

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

    /**
     * Single expression function
     * returns value after equal sign
     * @return delivers intent to TimerService
     * NOTE! This doesn't start the service, instead only delivers intent
     */
    private fun sendCommandToService(action: String) {
        TimerService.serviceIsRunning.value = true
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        intent.apply {
            putExtra(EXTRA_TIMER, loadSettings())
            putExtra(EXTRA_MAIN_TIMER_ACTIVE, "main")
        }
        requireContext().startService(intent)
    }

    private fun updateProgressBar() {
        var timerSettingInSeconds = loadSettings() / 1000
        TimerService.progressBar.observe(viewLifecycleOwner) {
            binding.progressBar.progress = it
            binding.progressBar.max = timerSettingInSeconds.toInt()
            Timber.d("progressbar setting: ${timerSettingInSeconds.toInt()}")
        }
    }

    /**
     * Loads timer setting from SharedPreferences
     * @return User set timer setting converted to long
     */
    private fun loadSettings(): Long {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerSetting = sp.getString("timer", "1500000").toString()
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