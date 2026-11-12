package com.ateflaw.legaldeadlines.data.backup

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-protected, offline-only backup envelope.
 * Binary format: ASCII "MEEAD-ENC-1", 16-byte salt, 12-byte IV, AES-256-GCM ciphertext+tag.
 * PBKDF2-HMAC-SHA1 is used for Android API 24 compatibility; 310k iterations.
 * Authentication covers header, salt and IV as AAD. Never send the key/password to a server.
 */
object EncryptedBackupCodec {
    private val header = "MEEAD-ENC-1".toByteArray(StandardCharsets.US_ASCII)
    private const val saltLength = 16
    private const val nonceLength = 12
    private const val iterations = 310_000
    private const val tagBits = 128
    const val maxPlaintextBytes = 16 * 1024 * 1024
    const val maxEncryptedBytes = maxPlaintextBytes + 128

    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        require(plaintext.size in 1..maxPlaintextBytes) { "حجم النسخة الاحتياطية غير مدعوم." }
        require(password.size >= 12) { "كلمة مرور النسخة الاحتياطية يجب ألا تقل عن 12 حرفًا." }
        val salt = ByteArray(saltLength).also { SecureRandom().nextBytes(it) }
        val nonce = ByteArray(nonceLength).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(tagBits, nonce))
            cipher.updateAAD(header)
            cipher.updateAAD(salt)
            cipher.updateAAD(nonce)
            header + salt + nonce + cipher.doFinal(plaintext)
        } finally {
            key.fill(0)
        }
    }

    fun decrypt(envelope: ByteArray, password: CharArray): ByteArray {
        require(password.isNotEmpty()) { "أدخل كلمة مرور النسخة الاحتياطية." }
        val minimum = header.size + saltLength + nonceLength + 16
        require(envelope.size in minimum..maxEncryptedBytes) { "ملف النسخة الاحتياطية تالف أو أكبر من الحد المسموح." }
        require(envelope.copyOfRange(0, header.size).contentEquals(header)) {
            "صيغة النسخة الاحتياطية غير معروفة."
        }
        val salt = envelope.copyOfRange(header.size, header.size + saltLength)
        val nonce = envelope.copyOfRange(header.size + saltLength, header.size + saltLength + nonceLength)
        val ciphertext = envelope.copyOfRange(header.size + saltLength + nonceLength, envelope.size)
        val key = deriveKey(password, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(tagBits, nonce))
            cipher.updateAAD(header)
            cipher.updateAAD(salt)
            cipher.updateAAD(nonce)
            try {
                cipher.doFinal(ciphertext).also {
                    require(it.size <= maxPlaintextBytes) { "حجم النسخة المستعادة يتجاوز الحد المسموح." }
                }
            } catch (_: AEADBadTagException) {
                throw IllegalArgumentException("كلمة المرور غير صحيحة أو ملف النسخة الاحتياطية تم تعديله.")
            }
        } finally {
            key.fill(0)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
