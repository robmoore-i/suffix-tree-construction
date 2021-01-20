package org.jetbrains.fulltextsearch.index.suffixtree

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ActivePointTest {
    @Test
    internal fun `first new leaf created when active point is on root node, and no edge matches next char`() {
        val root = RootNode()
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 1)

        val canAddMoreSuffixes =
            ActivePoint("xy", root, TextPosition(2), remainingSuffixes)
                .addNextSuffix(1)

        assertTrue(
            root.hasLeafEdge(1, 2, 1),
            "The expected leaf [1, 2](1) wasn't created on the root node.\nInstead, root was $root."
        )
        assertEquals(remainingSuffixes.value(), 0)
        assertFalse(canAddMoreSuffixes)
    }

    @Test
    internal fun `subsequent new leaves created when active point is on root node, and no edge matches next char`() {
        val root = RootNode()
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 1)

        val canAddMoreSuffixes = ActivePoint("xyzabc", root, TextPosition(4), remainingSuffixes)
            .addNextSuffix(3)

        assertTrue(
            root.hasLeafEdge(3, 4, 3),
            "The expected leaf [3, 4](3) wasn't created on the root node.\nInstead, root was $root."
        )
        assertEquals(remainingSuffixes.value(), 0)
        assertFalse(canAddMoreSuffixes)
    }

    @Test
    internal fun `doesn't add leaf if a matching edge already exists from the active point`() {
        val root = RootNode()
        val endPosition = TextPosition(3)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 1)

        val canAddMoreSuffixes = ActivePoint("xyx", root, endPosition, remainingSuffixes)
            .addNextSuffix(2)

        assertFalse(
            root.hasLeafEdge(2, 3, 2),
            "Unexpected leaf [2, 3](2) was created on the root node.\nInstead, root was $root."
        )
        assertEquals(remainingSuffixes.value(), 1)
        assertFalse(canAddMoreSuffixes)
    }

    @Test
    internal fun `moves off the node if a matching edge already exists from the active point`() {
        val root = RootNode()
        val endPosition = TextPosition(3)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint(
            "xyx", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 1)
        )

        activePoint.addNextSuffix(2)

        assertEquals(Pair(0, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `chooses the edge to move down based on the next character`() {
        val root = RootNode()
        val endPosition = TextPosition(3)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint(
            "xyy", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 1)
        )

        activePoint.addNextSuffix(2)

        assertEquals(Pair(1, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `splits an edge when the edge label stops matching`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint(
            "memo", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 2)
        )
        activePoint.setActiveNodeOffset(0, 1)

        activePoint.addNextSuffix(3)

        assertTrue(
            root.hasInternalEdge(0, 1) {
                it.hasLeafEdge(1, 4, 0)
                        && it.hasLeafEdge(3, 4, 2)
            },
            "Root didn't have the expected edges\nInstead, root was $root;"
        )
    }

    @Test
    internal fun `active edge advances after an insertion from root`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        val activePoint = ActivePoint(
            "xxx$", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 3)
        )
        activePoint.setActiveNodeOffset(0, 2)

        activePoint.addNextSuffix(3)

        assertTrue(
            root.hasInternalEdge(0, 2) {
                it.hasLeafEdge(2, 4, 0)
                        && it.hasLeafEdge(3, 4, 1)
            },
            "Root didn't have the expected edges\nInstead, root was $root;"
        )
        assertEquals(Pair(1, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `can split an internal edge`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        val internalNode = InternalNode()
        root.addInternalEdge(internalNode, TextPosition(0), TextPosition(2))
        internalNode.addLeafEdge(LeafNode(0), TextPosition(2), endPosition)
        internalNode.addLeafEdge(LeafNode(1), TextPosition(3), endPosition)
        val activePoint = ActivePoint(
            "xxx$", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 2)
        )
        activePoint.setActiveNodeOffset(1, 1)

        activePoint.addNextSuffix(3)

        assertTrue(
            root.hasInternalEdge(0, 1) { it ->
                it.hasLeafEdge(3, 4, 2)
                        && it.hasInternalEdge(1, 2) { true }
            },
            "Root didn't have the expected edges\nInstead, root was $root;"
        )
    }

    @Test
    internal fun `moves down active edge if character is matching`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint(
            "xyxya", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 2)
        )
        activePoint.setActiveNodeOffset(0, 1)

        activePoint.addNextSuffix(3)

        assertEquals(Pair(0, 2), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `if there are remaining suffixes, and they can be added, then the phase doesn't end`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        val activePoint = ActivePoint(
            "xxx$", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 3)
        )
        activePoint.setActiveNodeOffset(0, 2)

        val canAddMoreSuffixes = activePoint.addNextSuffix(3)

        assertTrue(canAddMoreSuffixes)
    }

    @Test
    internal fun `decrements the number of remaining suffixes after splitting an edge`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 2)
        val activePoint = ActivePoint("memo", root, endPosition, remainingSuffixes)
        activePoint.setActiveNodeOffset(0, 1)

        activePoint.addNextSuffix(3)

        assertEquals(1, remainingSuffixes.value())
    }

    @Test
    internal fun `can make insertion from root with a jump into an internal node`() {
        val root = RootNode()
        val endPosition = TextPosition(9)
        val internalNodeXY = InternalNode()
        internalNodeXY.addLeafEdge(LeafNode(0), TextPosition(2), endPosition)
        internalNodeXY.addLeafEdge(LeafNode(3), TextPosition(5), endPosition)
        root.addInternalEdge(internalNodeXY, TextPosition(0), TextPosition(2))
        val internalNodeY = InternalNode()
        internalNodeY.addLeafEdge(LeafNode(1), TextPosition(2), endPosition)
        internalNodeY.addLeafEdge(LeafNode(4), TextPosition(5), endPosition)
        root.addInternalEdge(internalNodeY, TextPosition(1), TextPosition(2))
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        root.addLeafEdge(LeafNode(5), TextPosition(5), endPosition)
        val activePoint = ActivePoint(
            "xyzxyaxyb", root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = 3)
        )
        activePoint.setActiveNodeOffset(0, 2)

        Debugger.enabledFor {
            activePoint.addNextSuffix(8)
        }

        assertTrue(root.hasInternalEdge(0, 2) {
            it.hasLeafEdge(2, 9, 0) &&
                    it.hasLeafEdge(5, 9, 3) &&
                    it.hasLeafEdge(8, 9, 6)
        }, "Root didn't have the expected edges\nInstead, root was $root;")
    }
}