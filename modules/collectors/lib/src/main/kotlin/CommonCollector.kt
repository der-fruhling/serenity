package net.derfruhling.serenity.processor

import java.security.MessageDigest
import kotlin.io.encoding.Base64

fun hashFunctionName(string: String): String {
    val digest = MessageDigest.getInstance("MD5")
    return Base64.Mime.encode(digest.digest(string.toByteArray()))
}
