package com.snapmint.checkoutsdk.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

object Helper {
    fun generateCheckSum(checkSumString: String): String {
        var messageDigest: MessageDigest? = null
        try {
            messageDigest = MessageDigest.getInstance("SHA-512")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val digest = messageDigest!!.digest(checkSumString.toByteArray())
        val stringBuilder = StringBuilder()
        for (i in digest.indices) {
            stringBuilder.append(((digest[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }

        return stringBuilder.toString()
    }
}