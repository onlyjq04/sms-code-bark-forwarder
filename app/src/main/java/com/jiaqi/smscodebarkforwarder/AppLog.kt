package com.jiaqi.smscodebarkforwarder

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLog {
    private const val KEY_LOGS = "logs"
    private val timeFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US)

    @Synchronized
    fun add(context: Context, message: String) {
        val prefs = AppPrefs.prefs(context)
        val now = timeFormat.format(Date())
        val oldLines = prefs.getString(KEY_LOGS, "").orEmpty()
            .lines()
            .filter { it.isNotBlank() }
        val newLines = listOf("$now $message") + oldLines
        prefs.edit()
            .putString(KEY_LOGS, newLines.take(30).joinToString("\n"))
            .apply()
    }

    fun read(context: Context): String {
        return AppPrefs.prefs(context).getString(KEY_LOGS, "").orEmpty()
    }
}
