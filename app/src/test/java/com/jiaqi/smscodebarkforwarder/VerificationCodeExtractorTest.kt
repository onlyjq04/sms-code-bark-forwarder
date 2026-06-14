package com.jiaqi.smscodebarkforwarder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerificationCodeExtractorTest {
    private val defaultKeywords = AppPrefs.defaultVerificationKeywords()

    @Test
    fun extractsPlainDigits() {
        assertEquals(
            "123456",
            VerificationCodeExtractor.extract("您的验证码是 123456，请勿泄露。", defaultKeywords),
        )
    }

    @Test
    fun extractsGoogleStyleCode() {
        assertEquals(
            "G123456",
            VerificationCodeExtractor.extract("G-123456 is your Google verification code.", defaultKeywords),
        )
    }

    @Test
    fun removesMiddleDashes() {
        assertEquals(
            "123456",
            VerificationCodeExtractor.extract("验证码 123-456，用于登录。", defaultKeywords),
        )
    }

    @Test
    fun extractsBracketPrefixCode() {
        assertEquals(
            "854126",
            VerificationCodeExtractor.extract(
                "【新浪】854126（微博登录验证码），此验证码只用于登录你的微博，请勿转发。",
                defaultKeywords,
            ),
        )
    }

    @Test
    fun extractsCodeBeforeKeyword() {
        assertEquals(
            "654321",
            VerificationCodeExtractor.extract("654321为您的登录验证码，5分钟内有效。", defaultKeywords),
        )
    }

    @Test
    fun extractsWithCustomKeyword() {
        assertEquals(
            "998877",
            VerificationCodeExtractor.extract(
                "Votre code de vérification est 998877.",
                listOf("code de vérification"),
            ),
        )
    }

    @Test
    fun ignoresTextWithoutDigits() {
        assertNull(
            VerificationCodeExtractor.extract(
                "Your Google account sign-in was blocked.",
                defaultKeywords,
            ),
        )
    }

    @Test
    fun ignoresBankTransferMessage() {
        assertNull(
            VerificationCodeExtractor.extract(
                "【招商银行】您尾号1234账户于06月13日15:30向张三转账5000.00元，余额12345.67元。",
                defaultKeywords,
            ),
        )
    }

    @Test
    fun ignoresPaymentNotification() {
        assertNull(
            VerificationCodeExtractor.extract(
                "您已成功消费1288.00元，交易时间2026-06-13，商户：某某商场。",
                defaultKeywords,
            ),
        )
    }

    @Test
    fun ignoresIncomingTransferAlert() {
        assertNull(
            VerificationCodeExtractor.extract(
                "到账通知：您收到一笔转账，金额8888.00元，付款方：李四。",
                defaultKeywords,
            ),
        )
    }
}
