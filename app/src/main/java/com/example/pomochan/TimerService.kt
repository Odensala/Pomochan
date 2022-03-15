package com.example.pomochan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pomochan.Constants.ACTION_LONGBREAK_TIMER_ACTIVE
import com.example.pomochan.Constants.ACTION_PAUSE_SERVICE
import com.example.pomochan.Constants.ACTION_SHOW_LONGBREAK_FRAGMENT
import com.example.pomochan.Constants.ACTION_SHOW_MAIN_FRAGMENT
import com.example.pomochan.Constants.ACTION_SHOW_SHORTBREAK_FRAGMENT
import com.example.pomochan.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.pomochan.Constants.ACTION_STOP_SERVICE
import com.example.pomochan.Constants.EXTRA_MAIN_TIMER_ACTIVE
import com.example.pomochan.Constants.EXTRA_TIMER
import com.example.pomochan.Constants.NOTIFICATION_CHANNEL_ID
import com.example.pomochan.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.pomochan.Constants.NOTIFICATION_ID
import com.example.pomochan.main.MainActivity
import timber.log.Timber

class TimerService : LifecycleService() {

    private lateinit var countDownTimer: CountDownTimer
    private var startingTime: Long = 0
    private var currentTime: Long = 5000
    private lateinit var currentActiveTimer: String

    var progressBarProgress = 0
    var isFirstRun = true

    companion object {
        val currentTimeLiveData = MutableLiveData<Long>()
        val timerRunning = MutableLiveData<Boolean>()
        val serviceIsRunning = MutableLiveData<Boolean>()
        val progressBar = MutableLiveData<Int>()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand started in TimerService")
        intent?.let {
            currentActiveTimer = intent.getStringExtra(EXTRA_MAIN_TIMER_ACTIVE).toString()
            Timber.d("currentActiveTimer: $currentActiveTimer")
            startingTime = intent.getLongExtra(EXTRA_TIMER, 5000)
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.i("ACTION_START_OR_RESUME_SERVICE")
                    if (isFirstRun) {
                        Timber.d("Created foreground service")
                        createNotificationChannel()
                        buildNotification(pendingIntent())
                        currentTime = startingTime
                        startTimer()
                        isFirstRun = false
                    } else {
                        Timber.d("Resumes timer")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseTimer()
                    Timber.i("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.i("Stopped service")
                    resetTimer()

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Timer
     */
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(currentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // If the timer stops our variable keeps track of it
                currentTime = millisUntilFinished
                currentTimeLiveData.value = currentTime
                Timber.d("Timer: $currentTime")

                // Updates progressbar
                progressBarProgress++
                progressBar.value = progressBarProgress
            }

            override fun onFinish() {
                timerRunning.value = false
                currentTime = 5000
                Toast.makeText(applicationContext, "Finished!", Toast.LENGTH_SHORT).show()
                // TODO check if this is the right way to stop the service
                stopSelf()
            }
        }.start()
        timerRunning.value = true
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        timerRunning.value = false
    }

    private fun resetTimer() {
        //TODO maybe reset livedata, that way we can reduce code in MainFragment
        pauseTimer()
        resetProgressBar()
        stopSelf()
    }

    private fun resetProgressBar() {
        progressBarProgress = 0
        progressBar.value = progressBarProgress
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun pendingIntent(): PendingIntent {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).also {
                it.action = currentActiveTimer()
                Timber.d("currentactivetime returns: ${currentActiveTimer()}")
            }.let { notificationIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    // TODO check why this is marked
                    // Indicates that if there already is a pending intent
                    // it will update it instead of recreating it
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        return pendingIntent
    }

    private fun currentActiveTimer(): String {
        var mainTimerKey = "main"
        var shortBreakKey = "shortbreak"
        var longBreakKey = "longbreak"

        return when (currentActiveTimer) {
            mainTimerKey -> {
                ACTION_SHOW_MAIN_FRAGMENT
            }
            shortBreakKey -> {
                ACTION_SHOW_SHORTBREAK_FRAGMENT
            }
            longBreakKey -> {
                ACTION_SHOW_LONGBREAK_FRAGMENT
            }
            else -> {
                ACTION_SHOW_LONGBREAK_FRAGMENT
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // TODO implement working timer in notification
    private fun buildNotification(pendingIntent: PendingIntent) {
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Pomochan")
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
        //.setAutoCancel(false)
        //.setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }
}