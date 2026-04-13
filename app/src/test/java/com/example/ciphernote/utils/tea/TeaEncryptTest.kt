package com.example.ciphernote.utils.tea

import org.junit.Assert.*
import org.junit.Test

class TeaEncryptTest {

    private val validKeyHex = "b3be6b55584e1a4e13928e8fdb6e1e5f"

    @Test
    fun testRoundTripIntegrity() {
        val originalText = "Hello, TEA! This is a secret message."
        val encrypted = TeaEncrypt.encryptString(originalText, validKeyHex)
        val decrypted = TeaEncrypt.decryptString(encrypted, validKeyHex)
        assertEquals("Decrypted text should match the original", originalText, decrypted)
    }

    @Test
    fun testEmptyStringHandling() {
        // Note: The current implementation might have specific behavior for empty strings 
        // due to how padding and header size are calculated.
        val originalText = ""
        val encrypted = TeaEncrypt.encryptString(originalText, validKeyHex)
        val decrypted = TeaEncrypt.decryptString(encrypted, validKeyHex)
        assertEquals("Decrypted text should match the empty string", "", decrypted)
    }

    @Test
    fun testLargePayloadBlockChaining() {
        // Create a large payload to ensure multiple blocks are handled correctly via chaining
        val originalText = "A".repeat(1024) 
        val encrypted = TeaEncrypt.encryptString(originalText, validKeyHex)
        val decrypted = TeaEncrypt.decryptString(encrypted, validKeyHex)
        assertEquals("Large payload integrity check failed", originalText, decrypted)
    }

    @Test
    fun testKeySensitivity() {
        val originalText = "Sensitive Data"
        val wrongKeyHex = "00000000000000000000000000000000"
        val encrypted = TeaEncrypt.encryptString(originalText, validKeyHex)
        val decryptedWithWrongKey = TeaEncrypt.decryptString(encrypted, wrongKeyHex)
        
        // Decryption with a wrong key should either return an empty string (due to padding check failure)
        // or garbage text that does not match the original.
        assertNotEquals("Decryption with wrong key should not yield original text", originalText, decryptedWithWrongKey)
    }

    @Test
    fun testUtf8AndEmojiSupport() {
        val originalText = "Hello 🌍! Special chars: ñ, á, ç, 漢字"
        val encrypted = TeaEncrypt.encryptString(originalText, validKeyHex)
        val decrypted = TeaEncrypt.decryptString(encrypted, validKeyHex)
        assertEquals("UTF-8 and Emoji support failed", originalText, decrypted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidKeyLengthHandling() {
        // Key must be 16 bytes (32 hex chars). Providing a shorter one should trigger require().
        val shortKeyHex = "b3be6b55"
        TeaEncrypt.encryptString("test", shortKeyHex)
    }

    @Test
    fun testMalformedCiphertextBehavior() {
        // Provide random base64 that isn't valid ciphertext for the given key/structure
        val malformedBase64 = "SGVsbG8gV29ybGQ=" // "Hello World" in Base64
        try {
            val decrypted = TeaEncrypt.decryptString(malformedBase64, validKeyHex)
            // The implementation returns "" if padding check fails or it's malformed
            assertEquals("Malformed ciphertext should return an empty string", "", decrypted)
        } catch (e: Exception) {
            // If it throws an exception instead of returning "", that is also acceptable 
            // behavior for truly malformed input, but we want to see if it handles it gracefully.
            assertTrue(true)
        }
    }
}
