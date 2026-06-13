package com.jiaqi.smscodebarkforwarder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerificationCodeExtractorTest {
    @Test
    fun extractsPlainDigits() {
        assertEquals("123456", VerificationCodeExtractor.extract("您的验证码是 123456，请勿泄露。"))
    }

    @Test
    fun extractsGoogleStyleCode() {
        assertEquals("G123456", VerificationCodeExtractor.extract("G-123456 is your Google verification code."))
    }

    @Test
    fun removesMiddleDashes() {
        assertEquals("123456", VerificationCodeExtractor.extract("验证码 123-456，用于登录。"))
    }

    @Test
    fun ignoresTextWithoutDigits() {
        assertNull(VerificationCodeExtractor.extract("Your Google account sign-in was blocked."))
    }
}
