package com.example.pomochan.shortbreak

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
import com.example.pomochan.databinding.FragmentBreakBinding
import com.example.pomochan.utils.TimerUtils
import timber.log.Timber

class BreakFragment : Fragment(R.layout.fragment_break) {

    private lateinit var binding: FragmentBreakBinding
    private lateinit var viewModel: BreakViewModel
    private lateinit var timerSetting: String

    private var currentTimeInMillis = 0L
    private var timerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBreakBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(BreakViewModel::class.java)

        refreshStartTime()

        loadSettings()

        if(TimerService.serviceIsRunning) {
            Timber.d("timer is running")
        }

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
            refreshStartTime()
        }

        setHasOptionsMenu(true)
        return binding.root
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

    private fun updateCountdown() {
        // Timer text
        TimerService.currentTimeLiveData.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TimerUtils.formatTime(currentTimeInMillis)
            binding.textViewCountdown.text = formattedTime
        })

        TimerService.timerRunning.observe(viewLifecycleOwner) {
            updateRunning(it)
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
        timerSetting = sp.getString("shortbreak", "300000").toString()
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