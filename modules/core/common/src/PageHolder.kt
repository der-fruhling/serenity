@file:Suppress("NOTHING_TO_INLINE")

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Immutable
import kotlinx.serialization.*
import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface SerialPageHolder {
    val hash: Map<String, String>
        get() = emptyMap()
    val details: PageDetails

    @Transient
    val extensions: Map<KClass<out Annotation>, AbstractPageExtension>
        get() = emptyMap()

    @Composable
    @HtmlComposable
    fun Main()
}

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface PageHolder<R : PageHolder<R>> : SerialPageHolder, PageHolderFactory<Any?, R> {
    @Suppress("UNCHECKED_CAST")
    override fun create(ctx: Any?): R {
        return this as R
    }
}

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface PageHolderFactory<in Ctx, R : PageHolder<R>> {
    val id: String
    val path: String
    fun create(ctx: Ctx): R
}

@Serializable
@SerialName($$"$page-details")
data class PageDetails(val title: String? = null)

@UsedByGeneratedCode
abstract class PageContext {
    abstract fun getParameter(name: String): String?
}

interface PageContract {
    var title: String
}

@PublishedApi
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

inline fun pageContract(fn: @DisallowComposableCalls PageContract.() -> Unit) {}

@OptIn(UsedByGeneratedCode::class)
fun Function<*>.pageFactory(): PageHolderFactory<PageContext, *> =
    throw IllegalStateException("This is implemented as an intrinsic")

inline fun <T: () -> Unit> T.of(): PageHolder<*> =
    throw IllegalStateException("This is implemented as an intrinsic")

inline fun <T: (P1) -> Unit, P1> T.of(p1: P1): PageHolder<*> =
    throw IllegalStateException("This is implemented as an intrinsic")

inline fun <T: (P1, P2) -> Unit, P1, P2> T.of(p1: P1, p2: P2): PageHolder<*> =
    throw IllegalStateException("This is implemented as an intrinsic")

inline fun <T: (P1, P2, P3) -> Unit, P1, P2, P3> T.of(p1: P1, p2: P2, p3: P3): PageHolder<*> =
    throw IllegalStateException("This is implemented as an intrinsic")
