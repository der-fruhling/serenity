package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.Serializable

@Serializable
data class SourceMap(
    val version: Int,
    val file: String? = null,
    val sourceRoot: String = "",
    val sources: List<String>,
    val sourcesContent: List<String>? = null,
    val names: List<String> = emptyList(),
    val mappings: String,
    val ignoreList: List<Int> = emptyList()
)
