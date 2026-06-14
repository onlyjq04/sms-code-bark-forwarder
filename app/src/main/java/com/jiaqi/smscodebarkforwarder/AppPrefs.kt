package com.jiaqi.smscodebarkforwarder

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {
    private const val PREFS_NAME = "sms_code_bark_forwarder"
    private const val KEY_SERVER = "server"
    private const val KEY_DEVICE_KEY = "device_key"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_VERIFICATION_KEYWORDS = "verification_keywords"

    const val DEFAULT_SERVER = "https://api.day.app"

    val DEFAULT_VERIFICATION_KEYWORDS_TEXT = """
验证码
校验码
动态码
登录码
注册码
激活码
确认码
安全码
验证代码
verification code
security code
one-time password
OTP
""".trim()

    data class Config(
        val server: String,
        val deviceKey: String,
        val enabled: Boolean,
        val verificationKeywords: List<String>,
    )

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun read(context: Context): Config {
        val prefs = prefs(context)
        val keywordsRaw = prefs.getString(KEY_VERIFICATION_KEYWORDS, null)
        return Config(
            server = prefs.getString(KEY_SERVER, DEFAULT_SERVER).orEmpty().ifBlank { DEFAULT_SERVER },
            deviceKey = prefs.getString(KEY_DEVICE_KEY, "").orEmpty(),
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            verificationKeywords = parseKeywords(
                keywordsRaw?.takeIf { it.isNotBlank() } ?: DEFAULT_VERIFICATION_KEYWORDS_TEXT,
            ),
        )
    }

    fun save(
        context: Context,
        server: String,
        deviceKey: String,
        enabled: Boolean,
        verificationKeywords: String,
    ) {
        prefs(context).edit()
            .putString(KEY_SERVER, server.trim().ifBlank { DEFAULT_SERVER })
            .putString(KEY_DEVICE_KEY, deviceKey.trim())
            .putBoolean(KEY_ENABLED, enabled)
            .putString(
                KEY_VERIFICATION_KEYWORDS,
                verificationKeywords.trim().ifBlank { DEFAULT_VERIFICATION_KEYWORDS_TEXT },
            )
            .apply()
    }

    fun parseKeywords(raw: String): List<String> {
        return raw.split(',', '，', ';', '；', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun defaultVerificationKeywords(): List<String> {
        return parseKeywords(DEFAULT_VERIFICATION_KEYWORDS_TEXT)
    }
}
