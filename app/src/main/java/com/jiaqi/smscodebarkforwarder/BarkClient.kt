package com.jiaqi.smscodebarkforwarder

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object BarkClient {
    data class Result(
        val ok: Boolean,
        val message: String,
    )

    fun push(server: String, deviceKey: String, title: String, body: String): Result {
        val endpoint = BarkEndpoint.resolve(server, deviceKey)
        return push(endpoint, title, body)
    }

    fun push(endpoint: BarkEndpoint.Endpoint, title: String, body: String): Result {
        return try {
            if (endpoint.deviceKey.isBlank()) {
                return Result(ok = false, message = "缺少 Bark device_key")
            }

            val payload = JSONObject()
                .put("title", title)
                .put("body", body)
                .put("group", "短信验证码")
                .put("isArchive", "1")
                .toString()
                .toByteArray(Charsets.UTF_8)

            val connection = (URL(endpoint.pushUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setFixedLengthStreamingMode(payload.size)
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            connection.outputStream.use { stream ->
                stream.write(payload)
            }

            val status = connection.responseCode
            val responseText = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            connection.disconnect()

            if (status in 200..299) {
                Result(ok = true, message = "HTTP $status")
            } else {
                Result(ok = false, message = "HTTP $status $responseText".trim())
            }
        } catch (error: Exception) {
            Result(ok = false, message = error.message ?: error.javaClass.simpleName)
        }
    }
}
