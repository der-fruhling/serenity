package net.derfruhling.serenity.manifest

import androidx.compose.runtime.staticCompositionLocalOf

interface ResourceResolver {
    fun getTargetUrl(sourceUrl: String): String
    fun getSourceUrl(targetUrl: String): String

    object Default : ResourceResolver {
        override fun getSourceUrl(targetUrl: String): String {
            return targetUrl
        }

        override fun getTargetUrl(sourceUrl: String): String {
            return sourceUrl
        }
    }

    companion object {
        val local = staticCompositionLocalOf<ResourceResolver> { Default }
    }
}