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
import com.example.pomochan.Constants.EXTRA_TIMER
import com.example.pomochan.R
import com.example.pomochan.TimerService
import com.example.pomochan.databinding.FragmentMainBinding
import com.example.pomochan.utils.TimerUtils

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    private lateinit var timerSetting: String

    private var currentTimeInMillis = 0L
    private var timerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        val application = requireNotNull(activity).application
        viewModelFactory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.textViewCountdown.text = loadSettings()?.let { TimerUtils.formatTime(it) }

        loadSettings()

        /*// Progressbar
        viewModel.progressBarLiveData.observe(viewLifecycleOwner, Observer {
            binding.progressBar.progress = it
        })*/

        // Start/Pause button
        binding.buttonStartPause.setOnClickListener {
            toggleStart()
        }

        // Reset button
        binding.buttonReset.setOnClickListener {
            sendCommandToService(ACTION_STOP_SERVICE)
            TimerService.serviceIsRunning = false

            //viewModel.resetProgressBar()
            binding.textViewCountdown.text = TimerUtils.formatTime(currentTimeInMillis)
        }
        timerServiceObservers()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun toggleStart() {
        if (timerRunning) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun timerServiceObservers() {
        // Timer text
        TimerService.currentTimeLiveData.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TimerUtils.formatTime(currentTimeInMillis)
            binding.textViewCountdown.text = formattedTime
        })

        //
        TimerService.timerRunning.observe(viewLifecycleOwner, Observer {
            updateRunning(it)
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

    /**
     * Single expression function
     * returns value after equal sign
     * @return delivers intent to TimerService
     * NOTE! This doesn't start the service, instead only delivers intent
     */
    private fun sendCommandToService(action: String) {
        TimerService.serviceIsRunning = true
        val intent = Intent(requireContext(), TimerService::class.java).also {
            it.action = action
        }
        intent.apply {
            putExtra(EXTRA_TIMER, loadSettings())
        }
        requireContext().startService(intent)
    }

    /**
     * Loads timer setting from SharedPreferences
     * @return User set timer setting converted to long
     */
    private fun loadSettings(): Long? {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        timerSetting = sp.getString("timer", "").toString()
        return timerSetting?.toLong()
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