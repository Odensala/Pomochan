package com.example.pomochan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pomochan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Countdown
        viewModel.timerString.observe(this, Observer {
            binding.textViewCountdown.text = it
        })

        // Timer finish
        viewModel.finished.observe(this, Observer {
            if (it) {
                Toast.makeText(this, "Finished!", Toast.LENGTH_SHORT).show()
            }
        })

        // Start/Pause button text changes
        viewModel.timerRunning.observe(this, Observer {
            if (it) {
                binding.buttonStartPause.text = getString(R.string.pause_timer)
            } else {
                binding.buttonStartPause.text = getString(R.string.start_timer)
            }
        })

        // Progressbar
        viewModel.progressBarLiveData.observe(this, Observer {
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
            R.id.nav_about -> Toast.makeText(this, "About selected", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}