package net.derfruhling.serenity.processor

import net.derfruhling.serenity.annotations.RegisterPage
import java.security.MessageDigest
import kotlin.io.encoding.Base64

fun hashFunctionName(string: String): String {
    val digest = MessageDigest.getInstance("MD5")
    return Base64.Mime.encode(digest.digest(string.toByteArray()))
}

fun generatePageDetails(annotation: RegisterPage): String = buildString {
    fun wrapString(s: String) = "$$\"${
        s
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("\t", "\\t")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }\""

    append("PageDetails(")

    annotation.takeIfNotEmpty({ title }) { title ->
        append("title = ${wrapString(title)}, ")
    }

    append(")")
}

private inline fun RegisterPage.takeIfNotEmpty(
    fn: RegisterPage.() -> String,
    value: (String) -> Unit
) {
    val v = try {
        fn()
    } catch (e: NoSuchElementException) {
        return
    }

    if (v.isNotEmpty()) value(v)
}
