package com.jiaqi.smscodebarkforwarder

object VerificationCodeExtractor {
    private val googleCode = Regex("""(?i)(?<![A-Z0-9])G-?(\d{4,8})(?![A-Z0-9])""")
    private const val CODE = """(\d{4,8}|\d{3}-\d{3})"""

    fun extract(body: String, keywords: List<String>): String? {
        val activeKeywords = keywords.map { it.trim() }.filter { it.isNotEmpty() }
        if (activeKeywords.isEmpty()) return null

        googleCode.find(body)?.let { match ->
            return clean(match.value)
        }

        if (!containsKeyword(body, activeKeywords)) return null

        val keywordPattern = activeKeywords.joinToString("|") { Regex.escape(it) }
        for (pattern in buildPatterns(keywordPattern)) {
            val raw = pattern.find(body)?.groupValues?.getOrNull(1) ?: continue
            val code = clean(raw)
            if (code.length in 4..8) return code
        }

        return null
    }

    private fun containsKeyword(body: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> body.contains(keyword, ignoreCase = true) }
    }

    private fun buildPatterns(keywordPattern: String): List<Regex> {
        return listOf(
            Regex(
                """(?i)(?:$keywordPattern)[^0-9]{0,24}$CODE""",
            ),
            Regex(
                """$CODE[）)\s]*[（(]?[^）)\n]{0,30}(?:$keywordPattern)""",
                RegexOption.IGNORE_CASE,
            ),
            Regex("""【[^】]{1,24}】\s*$CODE"""),
        )
    }

    private fun clean(value: String): String {
        return value.replace("-", "").uppercase()
    }
}
