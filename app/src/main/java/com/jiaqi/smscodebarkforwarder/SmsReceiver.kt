package com.jiaqi.smscodebarkforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!AppPrefs.read(context).enabled) return

        val pendingResult = goAsync()
        Thread {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                val sender = messages.firstOrNull()?.displayOriginatingAddress ?: "未知号码"
                val body = messages.joinToString(separator = "") { message ->
                    message.messageBody.orEmpty()
                }

                if (body.isNotBlank()) {
                    SmsForwarder.forwardIncoming(context.applicationContext, sender, body)
                }
            } catch (error: Exception) {
                AppLog.add(context, "处理短信失败：${error.message ?: error.javaClass.simpleName}")
            } finally {
                pendingResult.finish()
            }
        }.start()
    }
}
