package com.fitnessapp.tracker.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    private val monthFormat = SimpleDateFormat("yyyy 年 M 月", Locale.CHINESE)
    private val dayFormat = SimpleDateFormat("M 月 d 日", Locale.CHINESE)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINESE)

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatMonth(timestamp: Long): String = monthFormat.format(Date(timestamp))
    fun formatDay(timestamp: Long): String = dayFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long): Long = getStartOfDay(timestamp) + 86400000
}
