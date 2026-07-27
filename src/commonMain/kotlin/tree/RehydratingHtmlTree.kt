package net.derfruhling.html.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.snapshots.MutableSnapshot
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withCompositionLocal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.derfruhling.html.SerialSavedData
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.tree.platform.ElementNode
import net.derfruhling.html.tree.platform.NodeWithChildren
import net.derfruhling.html.tree.platform.RootNode

class RehydratingHtmlTree<Node: RootNode> internal constructor(
    val root: Node,
    val applier: HtmlApplier,
    val composition: Composition,
    val saveableRegistry: SaveableStateRegistry
) : AutoCloseable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val rootElement: ElementNode
        get() = (root as NodeWithChildren<*>).children.first { it is ElementNode } as ElementNode

    private lateinit var composable: @Composable @HtmlComposable () -> Unit

    val snapshot: MutableSnapshot = Snapshot.takeMutableSnapshot(
        readObserver = {
            (composition as ControlledComposition).recordReadOf(it)
        },
        writeObserver = {
            (composition as ControlledComposition).recordWriteOf(it)
        }
    )

    @PublishedApi
    internal fun actuallySetContent(fn: @Composable @HtmlComposable () -> Unit) {
        composable = fn
        composition.setContent {
            snapshot.enter {
                fn()
            }
        }
    }

    inline fun setContent(crossinline fn: @Composable @HtmlComposable () -> Unit) {
        actuallySetContent {
            withCompositionLocal(LocalSaveableStateRegistry provides saveableRegistry) {
                fn()
            }
        }
    }

    override fun close() {
        composition.dispose()
    }

    fun save(): SerialSavedData {
        return SerialSavedData.of(saveableRegistry.performSave())
    }
}

