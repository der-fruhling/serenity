package net.derfruhling.serenity.tree.platform

import androidx.compose.runtime.Applier
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.tree.HtmlApplier

expect sealed interface RootNode

val RootNode.head: ElementNode?
    get() = (this as NodeWithChildren<*, *>).findDescendentNamed(Name.of("head"), 3)
val RootNode.body: ElementNode?
    get() = (this as NodeWithChildren<*, *>).findDescendentNamed(Name.of("body"), 3)

expect open class PlatformApplier(document: RootNode) : Applier<ComposeNode>, HtmlApplier {
    final override var current: ComposeNode private set
    override var reflowTransformer: ((String) -> String)?
    override fun down(node: ComposeNode)
    override fun up()
    override fun insertTopDown(index: Int, instance: ComposeNode)
    override fun insertBottomUp(index: Int, instance: ComposeNode)
    override fun remove(index: Int, count: Int)
    override fun move(from: Int, to: Int, count: Int)
    override fun clear()
    override fun reuse()
}
