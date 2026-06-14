package com.jiaqi.smscodebarkforwarder

import android.content.Context

object SmsForwarder {
    fun forwardIncoming(context: Context, sender: String, body: String) {
        val config = AppPrefs.read(context)
        if (!config.enabled) return

        val code = VerificationCodeExtractor.extract(body, config.verificationKeywords) ?: return
        val endpoint = BarkEndpoint.resolve(config.server, config.deviceKey)

        if (endpoint.deviceKey.isBlank()) {
            AppLog.add(context, "未转发 $code：缺少 Bark device_key")
            return
        }

        val title = sender.ifBlank { "短信验证码" }
        val result = BarkClient.push(endpoint, title, body)

        if (result.ok) {
            AppLog.add(context, "已转发 $code，来自 $sender")
        } else {
            AppLog.add(context, "转发失败 $code：${result.message}")
        }
    }

    fun sendTest(context: Context): BarkClient.Result {
        val config = AppPrefs.read(context)
        val endpoint = BarkEndpoint.resolve(config.server, config.deviceKey)
        if (endpoint.deviceKey.isBlank()) {
            return BarkClient.Result(ok = false, message = "缺少 Bark device_key")
        }

        return BarkClient.push(
            endpoint = endpoint,
            title = "Test",
            body = "测试短信：您的验证码是 123456，请勿泄露。",
        )
    }
}
