package com.example.pomochan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.pomochan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    object TimerConstants {
        // Start time 25 min
        const val START_TIME_IN_MILLIS: Long = 1500000
    }

    private lateinit var binding: ActivityMainBinding

    lateinit var mCountDownTimer: CountDownTimer

    var mTimerRunning = false
    var progr = 0

    // Keeps track of time
    var mTimeLeftInMillis = TimerConstants.START_TIME_IN_MILLIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStartPause.setOnClickListener {
            if (mTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.buttonReset.setOnClickListener {
            resetTimer()
        }

        updateCountdownText()
    }

    private fun startTimer() {
        // Creates a CountDownTimer object
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // If the timer stops our variable keeps track of it
                mTimeLeftInMillis = millisUntilFinished
                updateCountdownText()
                progr++
                updateProgressBar()
            }

            override fun onFinish() {
                mTimerRunning = false
                binding.buttonStartPause.text = getString(R.string.start_timer)
            }
        }.start()

        mTimerRunning = true
        binding.buttonStartPause.text = getString(R.string.pause_timer)
    }

    private fun updateCountdownText() {
        var minutes = (mTimeLeftInMillis / 1000) / 60
        var seconds = (mTimeLeftInMillis / 1000) % 60

        // Converts minutes and seconds to a String
        var timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)

        binding.textViewCountdown.text = timeLeftFormatted
    }

    private fun pauseTimer() {
        mCountDownTimer.cancel()
        mTimerRunning = false
        binding.buttonStartPause.text = getString(R.string.start_timer)
    }

    private fun resetTimer() {
        mTimeLeftInMillis = TimerConstants.START_TIME_IN_MILLIS
        pauseTimer()
        updateCountdownText()
        resetProgressBar()
    }

    fun updateProgressBar() {
        binding.progressBar.progress = progr
    }

    fun resetProgressBar() {
        progr = 0
        updateProgressBar()
    }
}