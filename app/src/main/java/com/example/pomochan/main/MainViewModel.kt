package com.example.pomochan.main

import android.app.Application
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager

class MainViewModel(application: Application) : AndroidViewModel(application) {

    object TimerConstants {
        // 1500000 = 25 min
        const val START_TIME_IN_MILLIS: Long = 1500000
    }

    private lateinit var countDownTimer: CountDownTimer

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    // Holds user set pomodoro time (implying)
    private var userSetTime: Long = 0

    // No idea if this listener even works lol
    private var listener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "pomochan" ->
                    userSetTime = sharedPreferences.getString("pomochan", "1500000")!!.toLong()
            }
        }

    init {
        // TODO Implying this works
        preferences.registerOnSharedPreferenceChangeListener(listener)
        Log.i("MainViewModel", "$userSetTime")
        //userSetTime = preferences.getString("pomochan", "1500000")?.toLong()!!
        //currentTime = preferences.getString("pomochan", "1500000")?.toLong()!!
    }

    // Holds time in milliseconds
    private var currentTime: Long = TimerConstants.START_TIME_IN_MILLIS

    // Progressbar progress
    var progressBarProgress = 0

    // Timer initiated
    var timerInitiated = false

    // Checks if timer is running
    private val _timerRunning = MutableLiveData(false)
    val timerRunning: LiveData<Boolean>
        get() = _timerRunning

    // Finished LiveData
    private val _finished = MutableLiveData(false)
    val finished: LiveData<Boolean>
        get() = _finished

    // Timer String LiveData
    private val _timerString = MutableLiveData<String>()
    val timerString: LiveData<String>
        get() = _timerString

    // Progressbar LiveData
    private val _progressBarLiveData = MutableLiveData<Int>()
    val progressBarLiveData: LiveData<Int>
        get() = _progressBarLiveData

    /**
     * Timer
     */
    fun startTimer() {
        countDownTimer = object : CountDownTimer(currentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // If the timer stops our variable keeps track of it
                currentTime = millisUntilFinished

                // Sends calculated and formatted String to LiveData
                _timerString.value = formatText(currentTime)

                // Updates progressbar
                progressBarProgress++
                _progressBarLiveData.value = progressBarProgress


            }

            override fun onFinish() {
                _timerRunning.value = false
                _finished.value = true
            }
        }.start()
        _timerRunning.value = true
        timerInitiated = true
    }

    fun pauseTimer() {
        countDownTimer.cancel()
        _timerRunning.value = false
    }

    fun resetTimer() {
        if (timerInitiated) {
            // Resets milliseconds
            currentTime = TimerConstants.START_TIME_IN_MILLIS

            pauseTimer()
            resetProgressBar()
            _timerString.value = formatText(currentTime)
        }
    }

    fun formatText(timeInMillis: Long): String {
        // Separates timeInMillis into minutes and seconds
        var minutes = (timeInMillis / 1000) / 60
        var seconds = (timeInMillis / 1000) % 60

        // Converts minutes and seconds to a String
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun resetProgressBar() {
        progressBarProgress = 0
        _progressBarLiveData.value = progressBarProgress
    }

    override fun onCleared() {
        super.onCleared()
        // TODO Cancel the timer
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
        Log.i("MainViewModel", "MainViewModel destroyed!")
    }
}