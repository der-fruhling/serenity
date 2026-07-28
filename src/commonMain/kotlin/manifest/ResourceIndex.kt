package net.derfruhling.html.manifest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$resource-index")
data class ResourceIndex(
    private val sourceBaseUrl: String? = null,
    private val targetBaseUrl: String? = null,
    private val contents: Map<String, String>
) : SharedManifestEntry {
    fun reinstateSourceUrl(fragment: String): String =
        sourceBaseUrl?.let { it + fragment } ?: fragment

    fun reinstateTargetUrl(fragment: String): String =
        targetBaseUrl?.let { it + fragment } ?: fragment

    private val fromSourceUrl by lazy { contents.entries.associate { (src, tgt) -> reinstateSourceUrl(src) to reinstateTargetUrl(tgt) } }
    private val fromTargetUrl by lazy { contents.entries.associate { (src, tgt) -> reinstateTargetUrl(tgt) to reinstateSourceUrl(src) } }

    fun getTargetUrl(sourceUrl: String) = fromSourceUrl[sourceUrl]
    fun getSourceUrl(targetUrl: String) = fromTargetUrl[targetUrl]
}
