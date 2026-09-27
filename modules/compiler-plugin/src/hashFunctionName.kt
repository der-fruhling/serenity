package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import java.security.MessageDigest
import kotlin.io.encoding.Base64

private fun hashFunctionName(string: String): String {
    val digest = MessageDigest.getInstance("MD5")
    return Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
        .encode(digest.digest(string.toByteArray()))
}

fun hashFunctionName(callableId: CallableId): String {
    return hashFunctionName(callableId.asSingleFqName().asString())
}

fun hashFunctionName(classId: ClassId): String {
    val outerId = classId.outermostClassId
    return hashFunctionName(CallableId(outerId.packageFqName, outerId.shortClassName))
}
