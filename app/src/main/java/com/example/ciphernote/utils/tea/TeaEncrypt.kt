package com.example.ciphernote.utils.tea

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64

object TeaEncrypt {
    private const val DELTA = 0x9e3779b9u

    /* ---------- TEA primitives ---------- */
    private fun teaEncrypt(block: ByteArray, key: ByteArray, iterations: Int): ByteArray {
        require(block.size == 8) { "Block must be 8 bytes" }
        require(key.size == 16) { "Key must be 16 bytes" }
        var v0 = ByteBuffer.wrap(block, 0, 4).int.toUInt()
        var v1 = ByteBuffer.wrap(block, 4, 4).int.toUInt()
        val k = UIntArray(4)
        for (i in 0 until 4) {
            k[i] = ByteBuffer.wrap(key, i * 4, 4).int.toUInt()
        }
        var sum = 0u
        repeat(iterations) {
            sum = (sum + DELTA) and 0xffffffffu
            v0 = (v0 + (((v1 shl 4) + k[0]) xor (v1 + sum) xor ((v1 shr 5) + k[1]))) and 0xffffffffu
            v1 = (v1 + (((v0 shl 4) + k[2]) xor (v0 + sum) xor ((v0 shr 5) + k[3]))) and 0xffffffffu
        }
        val out = ByteArray(8)
        ByteBuffer.wrap(out, 0, 4).putInt(v0.toInt())
        ByteBuffer.wrap(out, 4, 4).putInt(v1.toInt())
        return out
    }

    private fun teaDecrypt(block: ByteArray, key: ByteArray, iterations: Int): ByteArray {
        require(block.size == 8) { "Block must be 8 bytes" }
        require(key.size == 16) { "Key must be 16 bytes" }
        var v0 = ByteBuffer.wrap(block, 0, 4).int.toUInt()
        var v1 = ByteBuffer.wrap(block, 4, 4).int.toUInt()
        val k = UIntArray(4)
        for (i in 0 until 4) {
            k[i] = ByteBuffer.wrap(key, i * 4, 4).int.toUInt()
        }
        var sum = (DELTA * iterations.toUInt()) and 0xffffffffu
        repeat(iterations) {
            v1 = (v1 - (((v0 shl 4) + k[2]) xor (v0 + sum) xor ((v0 shr 5) + k[3]))) and 0xffffffffu
            v0 = (v0 - (((v1 shl 4) + k[0]) xor (v1 + sum) xor ((v1 shr 5) + k[1]))) and 0xffffffffu
            sum = (sum - DELTA) and 0xffffffffu
        }
        val out = ByteArray(8)
        ByteBuffer.wrap(out, 0, 4).putInt(v0.toInt())
        ByteBuffer.wrap(out, 4, 4).putInt(v1.toInt())
        return out
    }

    /* ---------- Random padding ---------- */
    private val random = SecureRandom()
    private fun randomPadding(len: Int): ByteArray {
        val buf = ByteArray(len)
        random.nextBytes(buf)
        return buf
    }

    /* ---------- Hex utilities ---------- */
    private fun toHexBytes(hex: String): ByteArray {
        val cleaned = hex.replace("\\s", "")
        require(cleaned.length % 2 == 0) { "Hex string must have even length" }
        val res = ByteArray(cleaned.length / 2)
        for (i in res.indices) {
            val byteStr = cleaned.substring(i * 2, i * 2 + 2)
            res[i] = byteStr.toInt(16).toByte()
        }
        return res
    }

    /* ---------- String helpers ---------- */
    fun encryptString(plain: String, keyHex: String, iterations: Int = 32): String {
        val key = toHexBytes(keyHex)
        val plainBytes = plain.toByteArray(StandardCharsets.UTF_8)
        val iter = if (iterations > 32) 64 else 32
        val padLen = (8 - ((plainBytes.size + 2) % 8)) % 8
        val fillN = padLen + 2
        val headerSize = 1 + fillN + plainBytes.size + 7
        val header = ByteArray(headerSize)
        header[0] = ((fillN - 2) or 0xf8).toByte()
        System.arraycopy(randomPadding(fillN), 0, header, 1, fillN)
        System.arraycopy(plainBytes, 0, header, 1 + fillN, plainBytes.size)
        // remaining 7 bytes are zero by default
        val result = ByteArray(headerSize)
        var prevCipher = 0L
        var prevPlain = 0L
        for (i in header.indices step 8) {
            val block = header.copyOfRange(i, i + 8)
            if (i == 0) {
                val enc = teaEncrypt(block, key, iter)
                System.arraycopy(enc, 0, result, i, 8)
                prevCipher = ByteBuffer.wrap(enc).long
                prevPlain = ByteBuffer.wrap(block).long
            } else {
                val blockLong = ByteBuffer.wrap(block).long
                val plainLong = blockLong xor prevCipher
                val plainBytes2 = ByteBuffer.allocate(8).putLong(plainLong).array()
                val enc = teaEncrypt(plainBytes2, key, iter)
                val encLong = ByteBuffer.wrap(enc).long xor prevPlain
                val encOut = ByteBuffer.allocate(8).putLong(encLong).array()
                System.arraycopy(encOut, 0, result, i, 8)
                prevCipher = ByteBuffer.wrap(encOut).long
                prevPlain = plainLong
            }
        }
        return Base64.getEncoder().encodeToString(result)
    }

    fun decryptString(cipherBase64: String, keyHex: String, iterations: Int = 32): String {
        val key = toHexBytes(keyHex)
        val data = Base64.getDecoder().decode(cipherBase64)
        val iter = if (iterations > 32) 64 else 32
        val result = ByteArray(data.size)
        var prevCipher = 0L
        var prevPlain = 0L
        var pos = 0
        for (i in data.indices step 8) {
            val block = data.copyOfRange(i, i + 8)
            if (i == 0) {
                prevCipher = ByteBuffer.wrap(block).long
                val dec = teaDecrypt(block, key, iter)
                val decLong = ByteBuffer.wrap(dec).long
                val firstByte = (decLong shr 56).toInt() and 0xFF
                pos = (firstByte and 0x07) + 2
                System.arraycopy(dec, 0, result, 0, 8)
                prevPlain = decLong
            } else {
                val blockLong = ByteBuffer.wrap(block).long
                val encLong = blockLong xor prevPlain
                val encBytes = ByteBuffer.allocate(8).putLong(encLong).array()
                val dec = teaDecrypt(encBytes, key, iter)
                val plainLong = ByteBuffer.wrap(dec).long xor prevCipher
                val plainBytes = ByteBuffer.allocate(8).putLong(plainLong).array()
                System.arraycopy(plainBytes, 0, result, i, 8)
                prevPlain = plainLong xor prevCipher
                prevCipher = blockLong
            }
        }
        // check padding
        for (i in result.size - 7 until result.size) {
            if (result[i] != 0.toByte()) return ""
        }
        val finalResult = result.copyOfRange(pos + 1, result.size - 7)
        return String(finalResult, StandardCharsets.UTF_8)
    }

    /* ---------- Demo ---------- */
    @JvmStatic
    fun main(args: Array<String>) {
        val keyHex = "b3be6b55584e1a4e13928e8fdb6e1e5f"
        println("key length: ${keyHex.length}")
        val plain = "Hello, TEA!"
        println("Original: $plain")
        val cipher = encryptString(plain, keyHex)
        println("Cipher: $cipher")
        val plainAgain = decryptString(cipher, keyHex)
        println("Plain Deciphered: $plainAgain")

        // Test byte encryption/decryption
        val bytes = "ABCDEFGH".toByteArray(StandardCharsets.UTF_8)
        val keyBytes = toHexBytes(keyHex)
        val encBytes = encrypt(bytes, keyBytes)
        val decBytes = decrypt(encBytes, keyBytes)
        println("Dec bytes: ${String(decBytes, StandardCharsets.UTF_8)}")
    }

    private fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 16) { "Key must be 16 bytes" }
        val padded = padToBlock(data)
        val out = ByteArray(padded.size)
        for (i in padded.indices step 8) {
            val block = padded.copyOfRange(i, i + 8)
            val enc = teaEncrypt(block, key, 32)
            System.arraycopy(enc, 0, out, i, 8)
        }
        return out
    }

    private fun decrypt(data: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 16) { "Key must be 16 bytes" }
        val out = ByteArray(data.size)
        for (i in data.indices step 8) {
            val block = data.copyOfRange(i, i + 8)
            val dec = teaDecrypt(block, key, 32)
            System.arraycopy(dec, 0, out, i, 8)
        }
        return out
    }

    /* ---------- Padding helper ---------- */
    private fun padToBlock(data: ByteArray): ByteArray {
        if (data.size % 8 == 0) return data
        val padded = ByteArray(((data.size + 7) / 8) * 8)
        System.arraycopy(data, 0, padded, 0, data.size)
        return padded
    }
}
