package com.ateflaw.legaldeadlines.data.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EncryptedBackupCodecTest {
    @Test
    fun roundTripSupportsArabicLegalData() {
        val payload = "{\"client\":\"محمود\",\"case\":\"١٢٣ لسنة ٢٠٢٦\"}".toByteArray(Charsets.UTF_8)
        val key = "a-unique-long-secret-2026".toCharArray()
        val encrypted = EncryptedBackupCodec.encrypt(payload, key)
        assertFalse(payload.contentEquals(encrypted))
        assertArrayEquals(payload, EncryptedBackupCodec.decrypt(encrypted, key))
    }

    @Test
    fun wrongPasswordAndCiphertextTamperingAreRejected() {
        val payload = "private-client-data".toByteArray(Charsets.UTF_8)
        val encrypted = EncryptedBackupCodec.encrypt(payload, "correct-secret-12345".toCharArray())
        var wrongRejected = false
        try {
            EncryptedBackupCodec.decrypt(encrypted, "incorrect-secret-12345".toCharArray())
        } catch (_: IllegalArgumentException) {
            wrongRejected = true
        }
        assertTrue(wrongRejected)
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 1).toByte()
        var tamperingRejected = false
        try {
            EncryptedBackupCodec.decrypt(encrypted, "correct-secret-12345".toCharArray())
        } catch (_: IllegalArgumentException) {
            tamperingRejected = true
        }
        assertTrue(tamperingRejected)
    }

    @Test
    fun requiresPasswordAndRejectsUnknownEnvelope() {
        var shortRejected = false
        try {
            EncryptedBackupCodec.encrypt(byteArrayOf(1), "123".toCharArray())
        } catch (_: IllegalArgumentException) {
            shortRejected = true
        }
        assertTrue(shortRejected)
        var unknownRejected = false
        try {
            EncryptedBackupCodec.decrypt(byteArrayOf(1, 2, 3), "a-password-long-enough".toCharArray())
        } catch (_: IllegalArgumentException) {
            unknownRejected = true
        }
        assertTrue(unknownRejected)
    }
}
