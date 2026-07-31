package net.derfruhling.serenity.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.ReusableComposition
import androidx.compose.runtime.snapshots.MutableSnapshot
import androidx.compose.runtime.snapshots.Snapshot
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.tree.platform.ElementNode
import net.derfruhling.serenity.tree.platform.NodeWithChildren
import net.derfruhling.serenity.tree.platform.RootNode

class RehydratingHtmlTree<Node : RootNode> internal constructor(
    val root: Node,
    val applier: HtmlApplier,
    val composition: ReusableComposition
) : AutoCloseable {
    val rootElement: ElementNode
        get() = (root as NodeWithChildren<*, *>).children.first { it is ElementNode } as ElementNode

    private lateinit var composable: @Composable @HtmlComposable () -> Unit

    val snapshot: MutableSnapshot = Snapshot.takeMutableSnapshot(
        readObserver = {
            (composition as ControlledComposition).recordReadOf(it)
        },
        writeObserver = {
            (composition as ControlledComposition).recordWriteOf(it)
        }
    )

    fun setContent(fn: @Composable @HtmlComposable () -> Unit) {
        composable = fn
        composition.setContentWithReuse {
            snapshot.enter {
                fn()
            }
        }
    }

    override fun close() {
        composition.dispose()
    }
}

