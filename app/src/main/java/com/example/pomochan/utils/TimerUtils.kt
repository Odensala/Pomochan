package com.example.pomochan.utils

object TimerUtils {
    fun formatTime(timeInMillis: Long): String {
        // Separates timeInMillis into minutes and seconds
        var minutes = (timeInMillis / 1000) / 60
        var seconds = (timeInMillis / 1000) % 60

        // Converts minutes and seconds to a String
        return String.format("%02d:%02d", minutes, seconds)
    }
}