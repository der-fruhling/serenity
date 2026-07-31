package net.derfruhling.serenity.manifest

import androidx.compose.runtime.ProvidedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$resource-index")
data class ResourceIndex(
    private val sourceBaseUrl: String? = null,
    private val targetBaseUrl: String? = null,
    private val contents: Map<String, String>
) : SharedManifestEntry, ResourceResolver {
    override val provide: Array<ProvidedValue<*>>
        get() = arrayOf(ResourceResolver.local provides this)

    fun reinstateSourceUrl(fragment: String): String =
        sourceBaseUrl?.let { it + fragment } ?: fragment

    fun reinstateTargetUrl(fragment: String): String =
        targetBaseUrl?.let { it + fragment } ?: fragment

    private val fromSourceUrl by lazy {
        contents.entries.associate { (src, tgt) ->
            reinstateSourceUrl(src) to reinstateTargetUrl(
                tgt
            )
        }
    }
    private val fromTargetUrl by lazy {
        contents.entries.associate { (src, tgt) ->
            reinstateTargetUrl(tgt) to reinstateSourceUrl(
                src
            )
        }
    }

    override fun getTargetUrl(sourceUrl: String) = fromSourceUrl[sourceUrl] ?: sourceUrl
    override fun getSourceUrl(targetUrl: String) = fromTargetUrl[targetUrl] ?: targetUrl
}
