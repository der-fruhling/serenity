@file:PlatformGenerateStructure(
    "Paragraph",
    "Bold",
    "Italic",
    "Underline",
    "Strikethrough"
)

package net.derfruhling.serenity.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.derfruhling.serenity.dynamic.DynamicString
import net.derfruhling.serenity.dynamic.DynamicStringContext
import net.derfruhling.serenity.dynamic.DynamicStringRenderable
import net.derfruhling.serenity.dynamic.toComponent

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
internal annotation class PlatformGenerateStructure(vararg val names: String)

private class DynamicStringContextImpl : DynamicStringContext {
    constructor(vararg args: DynamicStringRenderable) {
        this.args = args.toList()
    }

    constructor(args: List<DynamicStringRenderable>) {
        this.args = args
    }

    constructor() {
        this.args = emptyList()
    }

    override val args: List<DynamicStringRenderable>
}

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun rememberContent(value: ConstantName): DynamicString {
    val loaded = loadedLocalizationResource.current
    val default = defaultLocalizationResource.current

    val content = remember(language.current, value) {
        loaded.strings[value]
        ?: default.strings[value]!!
    }

    return content
}

private fun Any.toRenderable(): DynamicStringRenderable {
    return when(this) {
        is DynamicStringRenderable -> this
        is String                  -> this.toComponent()
        else                       -> this.toString().toComponent()
    }
}

@Composable
fun TextOf(value: ConstantName) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl())
}

@Composable
fun TextOf(value: ConstantName, arg0: String) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toComponent()))
}

@Composable
fun TextOf(value: ConstantName, arg0: Any) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toRenderable()))
}

@Composable
fun TextOf(value: ConstantName, arg0: String, arg1: String) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toComponent(), arg1.toComponent()))
}

@Composable
fun TextOf(value: ConstantName, arg0: Any, arg1: Any) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toRenderable(), arg1.toRenderable()))
}

@Composable
fun TextOf(value: ConstantName, arg0: String, arg1: String, arg2: String) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toComponent(), arg1.toComponent(), arg2.toComponent()))
}

@Composable
fun TextOf(value: ConstantName, arg0: Any, arg1: Any, arg2: Any) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toRenderable(), arg1.toRenderable(), arg2.toRenderable()))
}

@Composable
fun TextOf(value: ConstantName, arg0: String, arg1: String, arg2: String, arg3: String) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toComponent(), arg1.toComponent(), arg2.toComponent(), arg3.toComponent()))
}

@Composable
fun TextOf(value: ConstantName, arg0: Any, arg1: Any, arg2: Any, arg3: Any) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(arg0.toRenderable(), arg1.toRenderable(), arg2.toRenderable(), arg3.toRenderable()))
}

@Composable
fun TextOf(value: ConstantName, vararg args: String) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(args.map { it.toComponent() }))
}

@Composable
fun TextOf(value: ConstantName, vararg args: Any) {
    val content = rememberContent(value)
    content.component.Render(DynamicStringContextImpl(args.map { it.toRenderable() }))
}
