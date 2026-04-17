package com.example.ciphernote.security

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CryptoManagerTest {

    private lateinit var crypto: CryptoManager

    @Before
    fun setup() {
        crypto = CryptoManager()
    }

    @Test
    fun testRoundTripIntegrity() {
        val originalText = "Hello, secure world!"
        val encrypted = crypto.encrypt(originalText)
        val decrypted = crypto.decrypt(encrypted)

        assertEquals(originalText, decrypted)
    }

    @Test
    fun testEmptyStringHandling() {
        val originalText = ""
        val encrypted = crypto.encrypt(originalText)
        val decrypted = crypto.decrypt(encrypted)

        assertEquals("", decrypted)
    }

    @Test
    fun testLargePayload() {
        val originalText = "A".repeat(4096)
        val encrypted = crypto.encrypt(originalText)
        val decrypted = crypto.decrypt(encrypted)

        assertEquals(originalText, decrypted)
    }

    @Test
    fun testUtf8AndEmojiSupport() {
        val originalText = "Hello 🌍! ñ á ç 漢字"
        val encrypted = crypto.encrypt(originalText)
        val decrypted = crypto.decrypt(encrypted)

        assertEquals(originalText, decrypted)
    }

    @Test
    fun testDifferentCiphertextEachTime() {
        val text = "Same input"

        val encrypted1 = crypto.encrypt(text)
        val encrypted2 = crypto.encrypt(text)

        // AES-GCM uses random IV → should NOT match
        assertNotEquals(encrypted1, encrypted2)
    }

    @Test(expected = Exception::class)
    fun testTamperedCiphertextFails() {
        val originalText = "Sensitive Data"
        val encrypted = crypto.encrypt(originalText)

        // Tamper with ciphertext
        val tampered = encrypted.dropLast(2) + "AA"

        crypto.decrypt(tampered)
    }

    @Test(expected = Exception::class)
    fun testInvalidBase64Fails() {
        val invalid = "NotBase64!!!"
        crypto.decrypt(invalid)
    }
}