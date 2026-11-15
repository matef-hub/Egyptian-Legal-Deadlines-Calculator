package com.ateflaw.legaldeadlines.utils

import java.text.Normalizer
import java.util.Locale

private val searchMarks = Regex("[\\p{M}ـ]")
private val searchAlif = Regex("[أإآٱ]")
private val searchWhitespace = Regex("\\s+")

fun String.normalizedArabicSearchText(): String {
    val text = Normalizer.normalize(this, Normalizer.Form.NFKC)
        .replace(searchMarks, "")
        .replace(searchAlif, "ا")
        .replace('ى', 'ي')
        .replace('ة', 'ه')
    return buildString {
        text.forEach { char ->
            val digit = Character.digit(char, 10)
            append(if (digit in 0..9) ('0'.code + digit).toChar() else char)
        }
    }.lowercase(Locale.ROOT).trim().replace(searchWhitespace, " ")
}
