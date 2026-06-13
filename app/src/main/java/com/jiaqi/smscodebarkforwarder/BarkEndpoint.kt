package com.jiaqi.smscodebarkforwarder

import java.net.URI

object BarkEndpoint {
    data class Endpoint(
        val pushUrl: String,
        val deviceKey: String,
    )

    private val likelyDeviceKey = Regex("[A-Za-z0-9_-]{10,}")
    private val officialHosts = setOf("api.day.app", "www.day.app", "day.app")

    fun resolve(server: String, deviceKey: String): Endpoint {
        val rawServer = server.trim().ifBlank { AppPrefs.DEFAULT_SERVER }
        val serverWithScheme = if ("://" in rawServer) rawServer else "https://$rawServer"
        val uri = URI(serverWithScheme.trimEnd('/'))
        val enteredKey = deviceKey.trim()
        val pathSegments = uri.path
            .orEmpty()
            .trim('/')
            .split('/')
            .filter { it.isNotBlank() }

        val endpointKey = extractDeviceKey(pathSegments, enteredKey)
        val apiRoot = apiRoot(uri, pathSegments, endpointKey)
        val pushUrl = if (endpointKey.isNotBlank()) {
            "$apiRoot/$endpointKey"
        } else {
            "$apiRoot/push"
        }

        return Endpoint(pushUrl = pushUrl, deviceKey = endpointKey)
    }

    private fun extractDeviceKey(pathSegments: List<String>, enteredKey: String): String {
        if (enteredKey.isNotBlank()) return enteredKey

        return pathSegments.firstOrNull { segment ->
            likelyDeviceKey.matches(segment)
        }.orEmpty()
    }

    private fun apiRoot(uri: URI, pathSegments: List<String>, endpointKey: String): String {
        val origin = origin(uri)
        if (pathSegments.isEmpty()) return origin

        if (uri.host.orEmpty().lowercase() in officialHosts) {
            return origin
        }

        val remaining = pathSegments.toMutableList()
        if (endpointKey.isNotBlank()) {
            remaining.remove(endpointKey)
        }
        if (remaining.lastOrNull()?.equals("push", ignoreCase = true) == true) {
            remaining.removeAt(remaining.lastIndex)
        }

        return if (remaining.isEmpty()) {
            origin
        } else {
            "${origin}/${remaining.joinToString("/")}"
        }
    }

    private fun origin(uri: URI): String {
        val port = if (uri.port == -1) "" else ":${uri.port}"
        return "${uri.scheme}://${uri.host}$port"
    }
}
