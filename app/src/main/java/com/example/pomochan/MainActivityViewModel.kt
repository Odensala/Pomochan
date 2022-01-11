package com.example.pomochan

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    init {
        Log.i("MainActivityViewModel", "MainActivityViewModel created!")
    }

    object TimerConstants {
        // 1500000 = 25 min
        const val START_TIME_IN_MILLIS: Long = 5000
    }

    private lateinit var countDownTimer: CountDownTimer

    // Holds time in milliseconds
    var timeLeftInMillis = TimerConstants.START_TIME_IN_MILLIS
    /*private val _timeLeftInMillis = MutableLiveData(TimerConstants.START_TIME_IN_MILLIS)
    val timeLeftInMillis: MutableLiveData<Long>
        get() = _timeLeftInMillis*/

    // Progressbar progress
    var progr = 0

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
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // If the timer stops our variable keeps track of it
                timeLeftInMillis = millisUntilFinished

                // Sends calculated and formatted String to LiveData
                _timerString.value = updateCountdownText()

                // Updates progressbar
                progr++
                _progressBarLiveData.value = progr
            }

            override fun onFinish() {
                _timerRunning.value = false
                _finished.value = true
            }
        }.start()
        _timerRunning.value = true
    }

    fun restartTimer() {
        if (_finished.value == true) {
            startTimer()
        }
    }

    fun pauseTimer() {
        countDownTimer.cancel()
        _timerRunning.value = false
    }

    fun resetTimer() {
        // Resets milliseconds
        timeLeftInMillis = TimerConstants.START_TIME_IN_MILLIS

        pauseTimer()
        resetProgressBar()
    }

    fun updateCountdownText(): String {
        // Separates timeLeftInMillis into minutes and seconds
        var minutes = (timeLeftInMillis / 1000) / 60
        var seconds = (timeLeftInMillis / 1000) % 60

        // Converts minutes and seconds to a String
        var timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        return timeLeftFormatted
    }

    fun resetProgressBar() {
        progr = 0
        _progressBarLiveData.value = progr
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainActivityViewmodel", "MainActivityViewModel destroyed!")
    }
}