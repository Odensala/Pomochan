package com.example.pomochan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.pomochan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    object TimerConstants {
        // Start time 25 min
        const val START_TIME_IN_MILLIS: Long = 1500000
    }

    private lateinit var binding: ActivityMainBinding

    lateinit var mCountDownTimer: CountDownTimer
    lateinit var mTextViewCountdown: TextView
    lateinit var mButtonStartPause: Button
    lateinit var mButtonReset: Button
    var mTimerRunning = false

    // Keeps track of time
    var mTimeLeftInMillis = TimerConstants.START_TIME_IN_MILLIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)

        mTextViewCountdown = findViewById(R.id.textViewCountdown)
        mButtonStartPause = findViewById(R.id.buttonStartPause)
        mButtonReset = findViewById(R.id.buttonReset)

        mButtonStartPause.setOnClickListener {
            if (mTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        mButtonReset.setOnClickListener {
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
            }

            override fun onFinish() {
                mTimerRunning = false
                mButtonStartPause.text = "Start"
            }
        }.start()

        mTimerRunning = true
        mButtonStartPause.text = "Pause"
    }

    private fun updateCountdownText() {
        var minutes = (mTimeLeftInMillis / 1000) / 60
        var seconds = (mTimeLeftInMillis / 1000) % 60

        // Converts minutes and seconds to a String
        var timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)

        mTextViewCountdown.text = timeLeftFormatted
    }

    private fun pauseTimer() {
        mCountDownTimer.cancel()
        mTimerRunning = false
        mButtonStartPause.text = "Start"
    }

    private fun resetTimer() {
        mTimeLeftInMillis = TimerConstants.START_TIME_IN_MILLIS
        pauseTimer()
        updateCountdownText()
    }

    fun updateProgressBar() {

    }

}