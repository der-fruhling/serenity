package net.derfruhling.serenity.test

import net.derfruhling.serenity.platform.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun compareNodes(a: ComposeNode, b: ComposeNode): Int {
    val bReal = b is ComposeNodeWithReal<*>
    val aReal = a is ComposeNodeWithReal<*>

    // real nodes will maintain their order, "fake" nodes will be pushed to the
    // end and sorted consistently
    return when {
        aReal && bReal -> 0
        aReal -> -1
        bReal -> +1
        a is ComparableComposeNode -> a compareTo b
        b is ComparableComposeNode -> -(b compareTo a)
        else -> a.hashCode() compareTo b.hashCode()
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <reified T> imply(expected: ComposeNode, value: ComposeNode) {
    contract {
        returns() implies (expected is T)
        returns() implies (value is T)
    }

    expected as T
    value as T
}

private inline fun <reified T : NodeWithChildren<*, *>> compareTree(expected: T, value: T) {
    val lhsChildren = expected.children.sortedWith(::compareNodes)
    val rhsChildren = value.children.sortedWith(::compareNodes)

    assertEquals(
        lhsChildren.size,
        rhsChildren.size,
        "Count of $expected and $value node's children differ"
    )

    for ((lhs, rhs) in lhsChildren zip rhsChildren) {
        assertEquivalence(lhs, rhs)
    }
}

private enum class HighLevelKind {
    DOCUMENT_LIKE {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<DocumentLike<*, *>>(expected, value)
            compareTree(expected, value)
        }
    },

    ELEMENT {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<ElementNode>(expected, value)
            assertEquals(expected.name, value.name, "Mismatched element names")
            compareTree(expected, value)
            assertEquals(expected.attributes, value.attributes)
        }
    },

    TEXT {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<TextNode>(expected, value)
            assertEquals(expected.textContent, value.textContent)
        }
    },

    DATA {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<DataNode>(expected, value)
            assertEquals(expected.textContent, value.textContent)
        }
    },

    EVENT_HANDLER {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<AbstractEventHandlerNode<*>>(expected, value)
            assertEquals(expected.type, value.type)
        }
    },

    COMMENT {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<CommentNode>(expected, value)
            assertEquals(expected.commentContent, value.commentContent)
        }
    },

    DOCTYPE {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<DocumentTypeNode>(expected, value)
            assertTrue { expected.isRealized }
            assertTrue { value.isRealized }

            assertEquals(expected.type, value.type)
            assertEquals(expected.system, value.system)
            assertEquals(expected.public, value.public)
        }
    },

    INLINE_STYLE {
        override fun assertEqual(expected: ComposeNode, value: ComposeNode) {
            imply<InlineStyleNode>(expected, value)
            assertEquals(expected.style, value.style)
        }
    };

    abstract fun assertEqual(expected: ComposeNode, value: ComposeNode)
}

private val ComposeNode.highLevelKind: HighLevelKind
    get() = when (this) {
        is DocumentLike<*, *> -> HighLevelKind.DOCUMENT_LIKE
        is ElementNode -> HighLevelKind.ELEMENT
        is TextNode -> HighLevelKind.TEXT
        is DataNode -> HighLevelKind.DATA
        is AbstractEventHandlerNode<*> -> HighLevelKind.EVENT_HANDLER
        is CommentNode -> HighLevelKind.COMMENT
        is DocumentTypeNode -> HighLevelKind.DOCTYPE
        is InlineStyleNode -> HighLevelKind.INLINE_STYLE
    }

fun assertEquivalence(expected: ComposeNode, value: ComposeNode) {
    val highLevel = expected.highLevelKind
    assertEquals(highLevel, value.highLevelKind, "Nodes are not compatible")

    highLevel.assertEqual(expected, value)
}
