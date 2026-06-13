package com.jiaqi.smscodebarkforwarder

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {
    private const val PREFS_NAME = "sms_code_bark_forwarder"
    private const val KEY_SERVER = "server"
    private const val KEY_DEVICE_KEY = "device_key"
    private const val KEY_ENABLED = "enabled"

    const val DEFAULT_SERVER = "https://api.day.app"

    data class Config(
        val server: String,
        val deviceKey: String,
        val enabled: Boolean,
    )

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun read(context: Context): Config {
        val prefs = prefs(context)
        return Config(
            server = prefs.getString(KEY_SERVER, DEFAULT_SERVER).orEmpty().ifBlank { DEFAULT_SERVER },
            deviceKey = prefs.getString(KEY_DEVICE_KEY, "").orEmpty(),
            enabled = prefs.getBoolean(KEY_ENABLED, false),
        )
    }

    fun save(context: Context, server: String, deviceKey: String, enabled: Boolean) {
        prefs(context).edit()
            .putString(KEY_SERVER, server.trim().ifBlank { DEFAULT_SERVER })
            .putString(KEY_DEVICE_KEY, deviceKey.trim())
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }
}
