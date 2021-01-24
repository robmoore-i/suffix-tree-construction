package org.jetbrains.fulltextsearch.index.suffixtree

import java.util.*


class SuffixTree {
    private var currentlyInsertedInput = ""

    private val rootNode: Node = RootNode()

    private var activeNode: Node = rootNode
    private var activeLength = 0
    private var activeEdge = 0

    private var suffixLinkCandidate: Node? = null

    private var remainingSuffixes = 0

    companion object {
        fun ukkonenConstruction(input: String): SuffixTree {
            val suffixTree = SuffixTree()
            input.forEach { suffixTree.addChar(it) }
            suffixTree.canonizeTree()
            return suffixTree
        }
    }

    fun addChar(c: Char) {
        // Add the character to the string of characters whose suffixes are present in the tree
        // already.
        currentlyInsertedInput += c

        // We only add suffix links within a phase, so we reset it at the start of this phase.
        suffixLinkCandidate = null

        // There is now an additional suffix which is not yet explicit in the tree, so we increment
        // our counter for the number of remaining suffixes.
        remainingSuffixes++

        while (remainingSuffixes > 0) {
            if (activeLength == 0) {
                // If we're at a node, point our active edge at the most recently added character in
                // the text.
                activeEdge = currentlyInsertedInput.length - 1
            }

            val activeEdgeChar = currentlyInsertedInput[activeEdge]
            if (!activeNode.edges.containsKey(activeEdgeChar)) {
                // If the active node doesn't yet have a child node corresponding to the next
                // character, add one. When we perform a leaf insertion like this, we need to add a
                // suffix link.
                activeNode.edges[activeEdgeChar] = LeafNode()
                addSuffixLink(activeNode)
            } else {
                // Since the active node has an edge starting with the next character, we need to
                // either create a new leaf node, continue down the active edge, or split the
                // current edge and create both an internal node and a leaf node.
                val nextNode = activeNode.edges[activeEdgeChar]!!

                // If the reference to the active point is non-canonical, then canonize it by
                // stepping through the tree, and then go to the next extension of the current
                // phase so that we can do all our steps from the basis of a canonical reference to
                // the active point.
                val edgeLength = nextNode.edgeLength()
                if (activeLength >= edgeLength) {
                    activeEdge += edgeLength
                    activeLength -= edgeLength
                    activeNode = nextNode
                    continue
                }

                // If the character is already present on the edge we are creating for the next
                // node, then the suffix is implicitly contained within the tree already, so we
                // end the current phase. We need to add a suffix link to the active node as well,
                // because otherwise the active point won't get to the correct place after our next
                // insertion.
                if (currentlyInsertedInput[nextNode.start + activeLength] == c) {
                    activeLength++
                    addSuffixLink(activeNode)
                    break
                }

                // The next node we're adding will be an internal node. We add it, and create a
                // leaf node from it whose edge corresponds to the character we're adding. We also
                // create a suffix link for the newly added internal node.
                val internalNode = Node(nextNode.start, nextNode.start + activeLength)
                activeNode.edges[activeEdgeChar] = internalNode
                internalNode.edges[c] = LeafNode()
                nextNode.start += activeLength
                internalNode.edges[currentlyInsertedInput[nextNode.start]] = nextNode
                addSuffixLink(internalNode)
            }

            // Since we have completed the above conditional block, it means that we have added a
            // new leaf node, which means that a new suffix has been made explicit within the tree.
            // When this happens, we decrement the number of suffixes that still need to be added to
            // the tree.
            remainingSuffixes--

            if (activeNode == rootNode && activeLength > 0) {
                // When we insert a node from root, we decrement our active length, and pull our
                // active edge forwards to point at the start of the next suffix we're adding.
                activeLength--
                activeEdge = currentlyInsertedInput.length - remainingSuffixes
            } else {
                // When we insert a node from an internal node, we follow its suffix link if it has
                // one. The default suffix link for any node is root.
                activeNode = activeNode.suffixLink()
            }
        }
    }

    /**
     * This converts the implicit suffix tree into a canonical suffix tree by adding a character
     * that doesn't appear elsewhere in the input.
     */
    private fun canonizeTree() {
        addChar('\u0000')
    }

    private fun addSuffixLink(node: Node) {
        suffixLinkCandidate?.linkTo(node)
        suffixLinkCandidate = node
    }

    /**
     * Finds the offsets of the given query string in the root
     */
    fun offsetsOf(queryString: String): Set<Int> {
        var i = 0
        var node = rootNode
        while (i < queryString.length) {
            // If we're at a leaf, then we get a match if the leaf's text matches the query string.
            // Otherwise we don't get a match.
            if (node.edges.isEmpty()) {
                val edgeLabel = node.edgeLabel()
                return if (edgeLabel.startsWith(queryString)) {
                    setOf(node.suffix)
                } else {
                    setOf()
                }
            }

            // If there are no outbound edges for the next character, then there are no matches
            val queryChar = queryString[i]
            if (!node.edges.containsKey(queryChar)) {
                return setOf()
            }

            // We follow the edge to the next internal node
            node = node.edges[queryChar]!!
            val edgeLabel = node.edgeLabel()

            // If the edge we just followed is longer than the remainder of the query string, then
            // we get matches the edge label starts with the remainder of the query string.
            // Otherwise we get no matches
            if (node.edgeLength() >= (queryString.length - i)) {
                return if (edgeLabel.startsWith(queryString.substring(i))) {
                    suffixesUnderSubtreeRootedAt(node)
                } else {
                    setOf()
                }
            }

            // If the edge we just followed doesn't have an edge label matching the query string,
            // then there are no matches
            if (edgeLabel != queryString.substring(i, i + node.edgeLength())) {
                return setOf()
            }

            // We increase i by the size of the next edge label
            i += node.edgeLength()
        }

        // If we make it out of the loop, then we have consumed the full query string by traversing
        // edges from the root. This means that all the suffixes stored within the current subtree
        // will be prefixed by the query string.
        return suffixesUnderSubtreeRootedAt(node)
    }

    private fun suffixesUnderSubtreeRootedAt(node: Node): Set<Int> {
        return if (node.edges.isEmpty()) {
            setOf(node.suffix)
        } else {
            node.edges.flatMap { suffixesUnderSubtreeRootedAt(it.value) }.toSet()
        }
    }

    open inner class Node(var start: Int, private var end: Int) {
        private var suffixLink: Node? = null

        val suffix = currentlyInsertedInput.length - remainingSuffixes
        var edges = TreeMap<Char, Node>()

        fun suffixLink(): Node {
            return suffixLink ?: rootNode
        }

        fun linkTo(node: Node) {
            suffixLink = node
        }

        fun edgeLength(): Int {
            return minOf(end, currentlyInsertedInput.length) - start
        }

        fun edgeLabel(): String {
            if (start == -1) {
                return ""
            }
            val endPosition = minOf(end, currentlyInsertedInput.length)
            val chars = CharArray(endPosition - start)
            (start until endPosition).forEach {
                chars[it - start] = currentlyInsertedInput[it]
            }
            return String(chars)
        }

        override fun toString(): String {
            return toString(1)
        }

        open fun toString(indentationLevel: Int): String {
            return "Node(start=$start, end=$end, suffix=$suffix, hasLink?=${suffixLink != null}, label=${edgeLabel()}, " +
                    "edges:${
                        edges.map {
                            "\n${"\t".repeat(indentationLevel)}'${it.key}'=${
                                it.value.toString(
                                    indentationLevel + 1
                                )
                            }"
                        }
                    })"
        }
    }

    inner class RootNode : Node(-1, -1) {
        override fun toString(): String {
            return "RootNode(edges:${edges.map { "\n\t'${it.key}'=${it.value.toString(2)}" }})"
        }
    }

    inner class LeafNode : Node(currentlyInsertedInput.length - 1, Int.MAX_VALUE / 2) {
        override fun toString(): String {
            return "LeafNode(start=$start, end=end, suffix=$suffix, label=${edgeLabel()})"
        }

        override fun toString(indentationLevel: Int): String {
            return toString()
        }
    }

    override fun toString(): String {
        return "SuffixTree(rootNode={\n$rootNode\n})"
    }
}