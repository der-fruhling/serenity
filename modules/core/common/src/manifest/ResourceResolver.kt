package net.derfruhling.serenity.manifest

import androidx.compose.runtime.compositionLocalOf

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
        val local = compositionLocalOf<ResourceResolver> { Default }
    }
}