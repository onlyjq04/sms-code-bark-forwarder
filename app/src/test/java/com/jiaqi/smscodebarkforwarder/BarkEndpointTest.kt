package com.jiaqi.smscodebarkforwarder

import org.junit.Assert.assertEquals
import org.junit.Test

class BarkEndpointTest {
    @Test
    fun usesDeviceKeyEndpointWhenServerHasNoPath() {
        val endpoint = BarkEndpoint.resolve("https://api.day.app", "abc123456789")

        assertEquals("https://api.day.app/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }

    @Test
    fun extractsDeviceKeyFromCopiedBarkUrl() {
        val endpoint = BarkEndpoint.resolve("https://api.day.app/abc123456789", "")

        assertEquals("https://api.day.app/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }

    @Test
    fun normalizesOldKeyThenPushUrl() {
        val endpoint = BarkEndpoint.resolve("https://api.day.app/abc123456789/push", "abc123456789")

        assertEquals("https://api.day.app/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }

    @Test
    fun keepsSelfHostedPathPrefix() {
        val endpoint = BarkEndpoint.resolve("https://example.com/bark", "abc123456789")

        assertEquals("https://example.com/bark/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }

    @Test
    fun addsHttpsWhenSchemeIsMissing() {
        val endpoint = BarkEndpoint.resolve("api.day.app/abc123456789", "")

        assertEquals("https://api.day.app/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }

    @Test
    fun ignoresExtraPathSegmentsOnOfficialServer() {
        val endpoint = BarkEndpoint.resolve("https://api.day.app/abc123456789/TestTitle", "abc123456789")

        assertEquals("https://api.day.app/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }

    @Test
    fun extractsDeviceKeyFromSelfHostedUrl() {
        val endpoint = BarkEndpoint.resolve("https://example.com/bark/abc123456789", "")

        assertEquals("https://example.com/bark/abc123456789", endpoint.pushUrl)
        assertEquals("abc123456789", endpoint.deviceKey)
    }
}
