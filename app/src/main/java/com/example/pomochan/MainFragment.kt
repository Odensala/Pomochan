package com.example.pomochan

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.pomochan.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Countdown
        viewModel.timerString.observe(viewLifecycleOwner, Observer {
            binding.textViewCountdown.text = it
        })

        // Timer finish
        viewModel.finished.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(context, "Finished!", Toast.LENGTH_SHORT).show()
            }
        })

        // Start/Pause button text changes
        viewModel.timerRunning.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.buttonStartPause.text = getString(R.string.pause_timer)
            } else {
                binding.buttonStartPause.text = getString(R.string.start_timer)
            }
        })

        // Progressbar
        viewModel.progressBarLiveData.observe(viewLifecycleOwner, Observer {
            binding.progressBar.progress = it
        })

        // Start/Pause button
        binding.buttonStartPause.setOnClickListener {
            if (viewModel.timerRunning.value == true) {
                viewModel.pauseTimer()
            } else if (viewModel.finished.value == true) {
                viewModel.resetTimer()
                viewModel.resetProgressBar()
                viewModel.startTimer()
            } else {
                viewModel.startTimer()
            }
        }

        // Reset button
        binding.buttonReset.setOnClickListener {
            viewModel.resetTimer()
            viewModel.resetProgressBar()
            binding.textViewCountdown.text = "25:00"
        }

        binding.buttonBreak.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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