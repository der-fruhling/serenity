package net.derfruhling.serenity

import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import kotlin.reflect.KClass

@PublishedApi
@UsedByGeneratedCode
internal class PageContractBuilder : PageContract {
    private var _title: String? = null
    private val _extensions = mutableMapOf<KClass<out Annotation>, AbstractPageExtension>()

    override var title: String
        get() = _title ?: ""
        set(value) {
            _title = value
        }

    fun getDetails(): PageDetails = PageDetails(_title)
    fun getExtensions(): Map<KClass<out Annotation>, AbstractPageExtension> = _extensions.toMap()
}
