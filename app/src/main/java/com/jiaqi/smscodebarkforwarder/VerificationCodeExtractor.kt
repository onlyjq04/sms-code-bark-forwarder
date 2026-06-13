package com.jiaqi.smscodebarkforwarder

object VerificationCodeExtractor {
    private val googleCode = Regex("(?i)(?<![A-Z0-9])G-?\\d{4,8}(?![A-Z0-9])")
    private val generalCode = Regex("(?i)(?<![A-Z0-9])(?=[A-Z0-9-]*\\d)[A-Z0-9](?:-?[A-Z0-9]){3,7}(?![A-Z0-9])")

    fun extract(body: String): String? {
        val googleMatch = googleCode.find(body)?.value
        if (googleMatch != null) return clean(googleMatch)

        val generalMatch = generalCode.find(body)?.value
        if (generalMatch != null) return clean(generalMatch)

        return null
    }

    private fun clean(value: String): String {
        return value.replace("-", "").uppercase()
    }
}
