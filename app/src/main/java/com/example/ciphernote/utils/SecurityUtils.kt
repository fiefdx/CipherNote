package com.example.ciphernote.utils

import java.security.MessageDigest

object SecurityUtils {
    fun md5ToHex(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        val md2 = digest.fold("") { str, it -> str + "%02x".format(it) }
        val digest2 = md.digest(md2.toByteArray())
        return digest2.fold("") { str, it -> str + "%02x".format(it) }
    }
}
