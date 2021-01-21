@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ActivePointTest {
    @Test
    internal fun `first new leaf created when active point is on root node, and no edge matches next char`() {
        val root = RootNode()
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 1)

        val canAddMoreSuffixes = ActivePoint.default(
            "xy", root, TextPosition(2), remainingSuffixes
        ).addNextSuffix(1)

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

        val canAddMoreSuffixes = ActivePoint.default(
            "xyzabc", root, TextPosition(4), remainingSuffixes
        ).addNextSuffix(3)

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

        val canAddMoreSuffixes = ActivePoint.default(
            "xyx", root, endPosition, remainingSuffixes
        ).addNextSuffix(2)

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
        val activePoint = ActivePoint.default(
            "xyx", root, endPosition, RemainingSuffixesPointer(remainingSuffixes = 1)
        )

        activePoint.addNextSuffix(2)

        assertEquals(Pair(2, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `chooses the edge to move down based on the next character`() {
        val root = RootNode()
        val endPosition = TextPosition(3)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint.default(
            "xyy", root, endPosition, RemainingSuffixesPointer(remainingSuffixes = 1)
        )

        activePoint.addNextSuffix(2)

        assertEquals(Pair(2, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `splits an edge when the edge label stops matching`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "memo", root, endPosition,
            remainingSuffixes = 2, activeEdge = 0, activeLength = 1
        )

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
    internal fun `after insertion from root, advance active edge`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xxx$", root, endPosition,
            remainingSuffixes = 3, activeEdge = 0, activeLength = 2
        )

        activePoint.addNextSuffix(3)

        assertTrue(
            root.hasInternalEdge(0, 2) {
                it.hasLeafEdge(2, 4, 0)
                        && it.hasLeafEdge(3, 4, 1)
            },
            "Root didn't have the expected edges\nInstead, root was $root;"
        )
        assertEquals(Pair(2, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `can split an internal edge`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafSuffixOffset = 0, firstLeafEdgeSrcOffset = 1,
            secondLeafSuffixOffset = 1, secondLeafEdgeSrcOffset = 3, endPosition = endPosition
        )
        val activePoint = ActivePoint.positionedAt(
            "xxx$", root, endPosition,
            remainingSuffixes = 2, activeEdge = 1, activeLength = 1
        )

        activePoint.addNextSuffix(3)

        assertTrue(
            root.hasInternalEdge(0, 1) {
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
        val activePoint = ActivePoint.positionedAt(
            "xyxya", root, endPosition,
            remainingSuffixes = 2, activeEdge = 0, activeLength = 1
        )

        activePoint.addNextSuffix(3)

        assertEquals(Pair(0, 2), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `if there are remaining suffixes, and they can be added, then the phase doesn't end`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xxx$", root, endPosition,
            remainingSuffixes = 3, activeEdge = 0, activeLength = 2
        )

        val canAddMoreSuffixes = activePoint.addNextSuffix(3)

        assertTrue(canAddMoreSuffixes)
    }

    @Test
    internal fun `after splitting edge, decrement the number of remaining suffixes`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 2)
        val activePoint = ActivePoint.default(
            "memo", root, endPosition, remainingSuffixes
        )
        activePoint.setActiveNodeOffset(0, 1)

        activePoint.addNextSuffix(3)

        assertEquals(1, remainingSuffixes.value())
    }

    @Test
    internal fun `makes leaf insertion from root with a hop into an internal node`() {
        val root = RootNode()
        val endPosition = TextPosition(9)
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafSuffixOffset = 0, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 3, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        root.addInternalEdge(
            internalEdgeOffsets = Pair(1, 2),
            firstLeafSuffixOffset = 1, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 4, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        root.addLeafEdge(LeafNode(5), TextPosition(5), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xyzxyaxyb", root, endPosition,
            remainingSuffixes = 3, activeEdge = 0, activeLength = 2
        )

        activePoint.addNextSuffix(8)

        assertTrue(root.hasInternalEdge(0, 2) {
            it.hasLeafEdge(2, 9, 0) &&
                    it.hasLeafEdge(5, 9, 3) &&
                    it.hasLeafEdge(8, 9, 6)
        }, "Root didn't have the expected edges\nInstead, root was $root;")
    }

    @Test
    internal fun `update active node when hopping over internal node during active edge extension`() {
        val root = RootNode()
        val endPosition = TextPosition(9)
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafSuffixOffset = 0, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 3, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        root.addInternalEdge(
            internalEdgeOffsets = Pair(1, 2),
            firstLeafSuffixOffset = 1, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 4, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        root.addLeafEdge(LeafNode(5), TextPosition(5), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xyzxyaxyzb", root, endPosition,
            remainingSuffixes = 3, activeEdge = 0, activeLength = 2
        )

        activePoint.addNextSuffix(8)

        assertEquals(Pair(2, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `after splitting edge, create suffix link between internal nodes`() {
        val root = RootNode()
        val endPosition = TextPosition(6)
        val suffixLinkCandidate = SuffixLinkCandidate()
        val existingInternalNode = root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafSuffixOffset = 0, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 3, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        suffixLinkCandidate.linkTo(existingInternalNode)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xyzxya", root, endPosition, remainingSuffixes = 2,
            activeEdge = 1, activeLength = 1, suffixLinkCandidate = suffixLinkCandidate
        )

        activePoint.addNextSuffix(5)

        assertTrue(root.hasInternalEdge(0, 2) { internalNode ->
            internalNode.hasSuffixLink { suffixLink ->
                if (suffixLink == null) {
                    false
                } else {
                    suffixLink.hasLeafEdge(2, 6, 1)
                            && suffixLink.hasLeafEdge(5, 6, 4)
                }
            }
        }, "Expected suffix link was missing.\nInstead, root was $root")
    }

    @Test
    internal fun `after insertion from internal node, follow suffix link without altering active edge`() {
        val root = RootNode()
        val endPosition = TextPosition(9)
        val suffixLinkSrc = root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafSuffixOffset = 0, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 3, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        val suffixLinkDst = root.addInternalEdge(
            internalEdgeOffsets = Pair(1, 2),
            firstLeafSuffixOffset = 1, firstLeafEdgeSrcOffset = 2,
            secondLeafSuffixOffset = 4, secondLeafEdgeSrcOffset = 5, endPosition = endPosition
        )
        suffixLinkSrc.linkTo(suffixLinkDst)
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        root.addLeafEdge(LeafNode(5), TextPosition(5), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xyzxyaxyzb", root, endPosition, remainingSuffixes = 4,
            activeEdge = 2, activeLength = 1, activeNode = suffixLinkSrc
        )

        activePoint.addNextSuffix(8)

        assertEquals(Pair(2, 1), activePoint.activeNodeOffset())
        assertTrue(
            activePoint.activeNodeIsInternalNode { it == suffixLinkDst },
            "Active point didn't meet expectations.\nInstead, active point was $activePoint;"
        )
    }

    @Test
    internal fun `hops over an internal node after following suffix link if necessary`() {
        val root = RootNode()
        val endPosition = TextPosition(9)
        val activeNode = root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 1),
            firstLeafEdgeSrcOffset = 1, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 4, secondLeafSuffixOffset = 3, endPosition = endPosition
        )
        val expectedNextActiveNode = root.addInternalEdge(
            internalEdgeOffsets = Pair(1, 2),
            firstLeafEdgeSrcOffset = 6, firstLeafSuffixOffset = 5,
            secondLeafEdgeSrcOffset = 2, secondLeafSuffixOffset = 1, endPosition = endPosition
        )
        val activePoint = ActivePoint.positionedAt(
            "xyzxzyxy$", root, endPosition, remainingSuffixes = 3,
            activeEdge = 1, activeLength = 1, activeNode = activeNode
        )

        activePoint.addNextSuffix(8)

        assertEquals(Pair(2, 0), activePoint.activeNodeOffset())
        assertTrue(
            activePoint.activeNodeIsInternalNode { it == expectedNextActiveNode },
            "Active point didn't meet expectations.\nInstead, active point was $activePoint;"
        )
    }

    @Test
    internal fun `doesn't moves down active edge if character is not matching`() {
        val root = RootNode()
        val endPosition = TextPosition(4)
        root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xyxxz", root, endPosition,
            remainingSuffixes = 2, activeEdge = 0, activeLength = 1
        )

        activePoint.addNextSuffix(3)

        assertEquals(Pair(3, 0), activePoint.activeNodeOffset())
        assertTrue(
            root.hasInternalEdge(0, 1) {
                it.hasLeafEdge(1, 4, 0)
                        && it.hasLeafEdge(3, 4, 2)
            },
            "Root didn't have the expected edges\nInstead, root was $root;"
        )
    }

    @Test
    internal fun `after leaf insertion from internal node, continue current phase`() {
        val root = RootNode()
        val endPosition = TextPosition(6)
        val nestedInternalNode = InternalNode()
        nestedInternalNode.addLeafEdge(LeafNode(0), TextPosition(2), endPosition)
        nestedInternalNode.addLeafEdge(LeafNode(3), TextPosition(5), endPosition)
        val internalNode = InternalNode()
        internalNode.addInternalEdge(nestedInternalNode, TextPosition(1), TextPosition(2))
        internalNode.addLeafEdge(LeafNode(1), TextPosition(2), endPosition)
        root.addInternalEdge(internalNode, TextPosition(0), TextPosition(1))
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "yyxyyz", root, endPosition,
            remainingSuffixes = 2, activeEdge = 2, activeLength = 0, activeNode = internalNode
        )

        val canAddMoreSuffixes = activePoint.addNextSuffix(5)

        assertTrue(canAddMoreSuffixes)
    }

    @Test
    internal fun `after leaf insertion from internal node, follow suffix link`() {
        val root = RootNode()
        val endPosition = TextPosition(6)
        val nestedInternalNode = InternalNode()
        nestedInternalNode.addLeafEdge(LeafNode(0), TextPosition(2), endPosition)
        nestedInternalNode.addLeafEdge(LeafNode(3), TextPosition(5), endPosition)
        val internalNode = InternalNode()
        internalNode.addInternalEdge(nestedInternalNode, TextPosition(1), TextPosition(2))
        internalNode.addLeafEdge(LeafNode(1), TextPosition(2), endPosition)
        root.addInternalEdge(internalNode, TextPosition(0), TextPosition(1))
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "yyxyyz", root, endPosition,
            remainingSuffixes = 2, activeEdge = 2, activeLength = 0, activeNode = internalNode
        )

        activePoint.addNextSuffix(5)

        assertTrue(activePoint.activeNodeIsRoot())
    }

    @Test
    internal fun `after insertion from root, active point jumps over nodes, if necessary`() {
        val root = RootNode()
        val endPosition = TextPosition(8)
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 1),
            firstLeafEdgeSrcOffset = 1, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 4, secondLeafSuffixOffset = 3, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(1), TextPosition(1), endPosition)
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xzyxyxy$", root, endPosition,
            remainingSuffixes = 4, activeEdge = 2, activeLength = 3
        )

        activePoint.addNextSuffix(7)

        assertTrue(activePoint.activeNodeIsInternalNode {
            it.hasLeafEdge(1, endPosition.value(), 0)
                    && it.hasLeafEdge(4, endPosition.value(), 3)
        }, "Active point didn't meet expectations.\nInstead, active point was $activePoint;")
        assertEquals(Pair(6, 1), activePoint.activeNodeOffset())
    }

    @Test
    internal fun `after insertion from root, active edge pointer normalizes and keeps up with the phase number`() {
        val root = RootNode()
        val endPosition = TextPosition(6)
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 1),
            firstLeafEdgeSrcOffset = 1, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 2, secondLeafSuffixOffset = 1, endPosition = endPosition
        )
        root.addInternalEdge(
            internalEdgeOffsets = Pair(2, 3),
            firstLeafEdgeSrcOffset = 3, firstLeafSuffixOffset = 2,
            secondLeafEdgeSrcOffset = 5, secondLeafSuffixOffset = 4, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(3), TextPosition(3), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xxyzyxyz$", root, endPosition,
            remainingSuffixes = 1, activeEdge = 3, activeLength = 0
        )

        activePoint.addNextSuffix(5)

        assertEquals(Pair(5, 1), activePoint.activeNodeOffset())
        assertTrue(activePoint.activeNodeIsRoot())
    }

    @Test
    internal fun `after splitting edge from root insertion, active length decrements`() {
        val root = RootNode()
        val endPosition = TextPosition(6)
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 1),
            firstLeafEdgeSrcOffset = 1, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 2, secondLeafSuffixOffset = 1, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(2), TextPosition(2), endPosition)
        root.addLeafEdge(LeafNode(3), TextPosition(3), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xxyzyxyz$", root, endPosition,
            remainingSuffixes = 2, activeEdge = 2, activeLength = 1
        )

        activePoint.addNextSuffix(5)

        assertEquals(Pair(5, 0), activePoint.activeNodeOffset())
        assertTrue(activePoint.activeNodeIsRoot())
    }

    @Test
    internal fun `when extending an edge from root, it traverses down a matching edge if there is one`() {
        val root = RootNode()
        val endPosition = TextPosition(7)
        val expectedInternalNode = root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 1),
            firstLeafEdgeSrcOffset = 1, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 2, secondLeafSuffixOffset = 1, endPosition = endPosition
        )
        root.addInternalEdge(
            internalEdgeOffsets = Pair(2, 3),
            firstLeafEdgeSrcOffset = 3, firstLeafSuffixOffset = 2,
            secondLeafEdgeSrcOffset = 5, secondLeafSuffixOffset = 4, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(3), TextPosition(3), endPosition)
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 2)
        val activePoint = ActivePoint(
            "xxyzyxyz$", root, endPosition, remainingSuffixes, SuffixLinkCandidate()
        )
        activePoint.setActiveNodeOffset(activeEdge = 5, activeLength = 1)

        val canAddMoreSuffixes = activePoint.addNextSuffix(6)

        assertEquals(Pair(6, 1), activePoint.activeNodeOffset())
        assertTrue(
            activePoint.activeNodeIsInternalNode { it == expectedInternalNode },
            "Active point didn't meet expectations.\nInstead, active point was $activePoint;"
        )
        assertEquals(2, remainingSuffixes.value())
        assertFalse(canAddMoreSuffixes)
    }

    @Test
    internal fun `doesn't follow suffix link when hopping over an internal node`() {
        val root = RootNode()
        val endPosition = TextPosition(8)
        val suffixLinkSrc = root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafEdgeSrcOffset = 2, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 4, secondLeafSuffixOffset = 2, endPosition = endPosition
        )
        val suffixLinkDst = root.addInternalEdge(
            internalEdgeOffsets = Pair(1, 2),
            firstLeafEdgeSrcOffset = 2, firstLeafSuffixOffset = 1,
            secondLeafEdgeSrcOffset = 4, secondLeafSuffixOffset = 3, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(4), TextPosition(4), endPosition)
        val remainingSuffixes = RemainingSuffixesPointer(remainingSuffixes = 3)
        val suffixLinkCandidate = SuffixLinkCandidate()
        suffixLinkCandidate.linkTo(suffixLinkSrc)
        suffixLinkCandidate.linkTo(suffixLinkDst)
        val activePoint = ActivePoint(
            "xyxyzxyz$", root, endPosition, remainingSuffixes, suffixLinkCandidate
        )
        activePoint.setActiveNodeOffset(activeEdge = 5, activeLength = 2)

        val canAddMoreSuffixes = activePoint.addNextSuffix(7)

        assertEquals(Pair(7, 1), activePoint.activeNodeOffset())
        assertTrue(
            activePoint.activeNodeIsInternalNode { it == suffixLinkSrc },
            "Active point didn't meet expectations.\nInstead, active point was $activePoint;"
        )
        assertEquals(3, remainingSuffixes.value())
        assertFalse(canAddMoreSuffixes)
    }

    @Test
    internal fun `don't eagerly hop into internal nodes after reverting to root`() {
        val root = RootNode()
        val endPosition = TextPosition(10)
        val ySubtree = InternalNode()
        ySubtree.addLeafEdge(LeafNode(1), TextPosition(2), endPosition)
        val yzxySubtree = InternalNode()
        yzxySubtree.addLeafEdge(LeafNode(2), TextPosition(6), endPosition)
        yzxySubtree.addLeafEdge(LeafNode(5), TextPosition(9), endPosition)
        ySubtree.addInternalEdge(yzxySubtree, TextPosition(3), TextPosition(6))
        root.addInternalEdge(
            internalEdgeOffsets = Pair(0, 2),
            firstLeafEdgeSrcOffset = 2, firstLeafSuffixOffset = 0,
            secondLeafEdgeSrcOffset = 6, secondLeafSuffixOffset = 4, endPosition = endPosition
        )
        root.addLeafEdge(LeafNode(3), TextPosition(3), endPosition)
        val activePoint = ActivePoint.positionedAt(
            "xyyzxyzxy$", root, endPosition,
            remainingSuffixes = 4, activeEdge = 6, activeLength = 3
        )

        activePoint.addNextSuffix(9)

        assertTrue(
            activePoint.activeNodeIsRoot(),
            "Active point didn't meet expectations.\nInstead, active point was $activePoint;"
        )
        assertEquals(Pair(7, 2), activePoint.activeNodeOffset())
    }

    private fun RootNode.addInternalEdge(
        internalEdgeOffsets: Pair<Int, Int>, firstLeafSuffixOffset: Int,
        firstLeafEdgeSrcOffset: Int, secondLeafSuffixOffset: Int,
        secondLeafEdgeSrcOffset: Int, endPosition: TextPosition
    ): InternalNode {
        val internalNode = InternalNode()
        internalNode.addLeafEdge(
            LeafNode(firstLeafSuffixOffset),
            TextPosition(firstLeafEdgeSrcOffset),
            endPosition
        )
        internalNode.addLeafEdge(
            LeafNode(secondLeafSuffixOffset),
            TextPosition(secondLeafEdgeSrcOffset),
            endPosition
        )
        addInternalEdge(
            internalNode,
            TextPosition(internalEdgeOffsets.first),
            TextPosition(internalEdgeOffsets.second)
        )
        return internalNode
    }

    private fun ActivePoint.Companion.positionedAt(
        input: String,
        root: RootNode,
        endPosition: TextPosition,
        remainingSuffixes: Int,
        activeEdge: Int,
        activeLength: Int,
        activeNode: InternalNode? = null,
        suffixLinkCandidate: SuffixLinkCandidate = SuffixLinkCandidate()
    ): ActivePoint {
        val activePoint = ActivePoint(
            input, root, endPosition,
            RemainingSuffixesPointer(remainingSuffixes = remainingSuffixes),
            suffixLinkCandidate
        )
        if (activeNode == null) {
            activePoint.setActiveNodeOffset(activeEdge, activeLength)
        } else {
            activePoint.advance(activeNode, activeEdge, activeLength)
        }
        return activePoint
    }

    private fun ActivePoint.Companion.default(
        input: String,
        root: RootNode,
        endPosition: TextPosition,
        remainingSuffixesPointer: RemainingSuffixesPointer
    ): ActivePoint {
        return ActivePoint(
            input, root, endPosition, remainingSuffixesPointer, SuffixLinkCandidate()
        )
    }

    private fun ActiveNode.hasInternalEdge(
        srcOffset: Int,
        dstOffset: Int,
        internalNodeMatcher: (InternalNode) -> Boolean
    ): Boolean {
        return hasEdge { edge ->
            edge.isInternalEdge { internalEdge ->
                internalEdge.hasLabelOffsets(Pair(srcOffset, dstOffset))
                        && internalEdge.dstMatches(internalNodeMatcher)
            }
        }
    }

    private fun ActiveNode.hasLeafEdge(srcOffset: Int, dstOffset: Int, suffixOffset: Int): Boolean {
        return hasEdge { edge ->
            edge.isLeafEdge { leafEdge ->
                leafEdge.hasLabelOffsets(Pair(srcOffset, dstOffset))
                        && leafEdge.dstMatches {
                    it.suffixOffset() == suffixOffset
                }
            }
        }
    }
}