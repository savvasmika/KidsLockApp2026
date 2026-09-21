package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure 6-digit confirmation or temporary code (e.g. 482731).
     */
    fun generateSecure6DigitCode(): String {
        val number = 100000 + secureRandom.nextInt(900000)
        return number.toString()
    }

    /**
     * Generates a random alphanumeric token or secret key.
     */
    fun generateSecureToken(length: Int = 32): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hashes a 4-digit PIN using SHA-256 with a salt.
     */
    fun hashPin(pin: String, salt: String = "KidLock_Salt_2026"): String {
        val combined = "$salt:$pin"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if an entered PIN matches the stored hash.
     */
    fun verifyPin(pin: String, storedHash: String, salt: String = "KidLock_Salt_2026"): Boolean {
        if (storedHash.isBlank()) return false
        val computed = hashPin(pin, salt)
        return computed == storedHash
    }

    /**
     * Signs a message payload with HMAC-SHA256 using the shared pairing secret key.
     */
    fun signPayload(payload: String, secretKey: String): String {
        val algorithm = "HmacSHA256"
        val mac = Mac.getInstance(algorithm)
        val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), algorithm)
        mac.init(keySpec)
        val bytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that the payload signature matches the expected HMAC-SHA256.
     */
    fun verifySignature(payload: String, signature: String, secretKey: String): Boolean {
        val expected = signPayload(payload, secretKey)
        return expected.equals(signature, ignoreCase = true)
    }
}
