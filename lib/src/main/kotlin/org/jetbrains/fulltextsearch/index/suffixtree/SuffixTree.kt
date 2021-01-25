package org.jetbrains.fulltextsearch.index.suffixtree


class SuffixTree {
    private var currentlyInsertedInput = ""
    private var remainingSuffixes = 0

    private val rootNode = RootNode()
    private val activePoint = ActivePoint()

    companion object {
        fun ukkonenConstruction(input: String): SuffixTree {
            val suffixTree = SuffixTree()
            input.forEach { suffixTree.addChar(it) }
            suffixTree.canonize()
            return suffixTree
        }
    }

    fun addChar(c: Char) {
        // Add the character to the string of characters whose suffixes are present in the tree
        // already.
        currentlyInsertedInput += c

        // There is now an additional suffix which is not yet explicit in the tree, so we increment
        // our counter for the number of remaining suffixes.
        remainingSuffixes++

        // We ask the active point to add the remaining suffixes, with each suffix able to be added
        // in O(1) time because the active point is creating and exploiting knowledge about the
        // string's suffixes through the use of suffix links.
        activePoint.addRemainingSuffixes(c)
    }

    /**
     * Finds the offsets of the given query string within the input string by exploiting the built
     * tree.
     */
    fun offsetsOf(queryString: String): Set<Int> {
        return rootNode.offsetsOf(queryString)
    }

    /**
     * This converts the implicit suffix tree into a canonical suffix tree by adding a character
     * that doesn't appear elsewhere in the input.
     */
    private fun canonize() {
        addChar('\u0000')
    }

    override fun toString(): String {
        return "SuffixTree(rootNode={\n$rootNode\n})"
    }

    inner class ActivePoint {
        private var activeNode: Node = rootNode
        private var activeLength = 0
        private var activeEdge = 0

        private var suffixLinkCandidate: Node? = null

        fun addRemainingSuffixes(c: Char) {
            // We only add suffix links within a phase, so we reset it at the start of the phase.
            suffixLinkCandidate = null

            while (remainingSuffixes > 0) {
                val extensionRuleThatWasApplied = addSuffix(c)
                if (extensionRuleThatWasApplied == SuffixExtensionRule.RULE_THREE) {
                    // When we apply rule three extensions, it ends the current phase because the
                    // suffix we want to add is already implicitly present in the tree.
                    break
                }

                // Since we haven't broken out of the loop, it means we did a rule three extension,
                // which means that a new suffix has been made explicit within the tree. When this
                // happens, we decrement the number of suffixes that still need to be added.
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
         * @return Which suffix extension rule was applied in order to add the next suffix.
         */
        private fun addSuffix(c: Char): SuffixExtensionRule {
            if (activeLength == 0) {
                // If we're at a node, set our active edge at the last added character in the text.
                activeEdge = currentlyInsertedInput.length - 1
            }

            val activeEdgeLeadingChar = currentlyInsertedInput[activeEdge]
            val nextNode = activeNode.edges[activeEdgeLeadingChar]
            if (nextNode == null) {
                // If the active node doesn't yet have a child node corresponding to the next
                // character, add one. When we perform a leaf insertion like this, we need to add a
                // suffix link.
                activeNode.edges[activeEdgeLeadingChar] = LeafNode()
                addSuffixLink(activeNode)
                return SuffixExtensionRule.RULE_TWO
            } else {
                // If the reference to the active point is non-canonical, then canonize it by
                // recursively stepping through the tree, and then go to the next extension of the
                // current phase so that we can do all our steps from the basis of a canonical
                // reference to the active point.
                val edgeLength = nextNode.edgeLength()
                if (activeLength >= edgeLength) {
                    activeEdge += edgeLength
                    activeLength -= edgeLength
                    activeNode = nextNode
                    return addSuffix(c)
                }

                // If the character is already present on the edge we are creating for the next
                // node, then we perform a rule three extension. We add a suffix link to the active
                // node, so the active point gets to the right place after our next node insertion.
                if (currentlyInsertedInput[nextNode.start + activeLength] == c) {
                    activeLength++
                    addSuffixLink(activeNode)
                    return SuffixExtensionRule.RULE_THREE
                }

                // We split the active edge by creating a new internal node and a new leaf node for
                // the next character. We add a suffix link for the newly created internal node.
                val internalNode = Node(nextNode.start, nextNode.start + activeLength)
                activeNode.edges[activeEdgeLeadingChar] = internalNode
                internalNode.edges[c] = LeafNode()
                nextNode.start += activeLength
                internalNode.edges[currentlyInsertedInput[nextNode.start]] = nextNode
                addSuffixLink(internalNode)
                return SuffixExtensionRule.RULE_TWO
            }
        }

        private fun addSuffixLink(node: Node) {
            suffixLinkCandidate?.linkTo(node)
            suffixLinkCandidate = node
        }
    }

    open inner class Node(var start: Int, private var end: Int) {
        private var suffixLink: Node? = null

        val suffix = currentlyInsertedInput.length - remainingSuffixes
        var edges = mutableMapOf<Char, Node>()

        fun edgeLength(): Int = minOf(end, currentlyInsertedInput.length) - start

        fun suffixLink(): Node = suffixLink ?: rootNode

        fun linkTo(node: Node) {
            suffixLink = node
        }

        fun edgeLabel(): String =
            currentlyInsertedInput.substring(start, minOf(end, currentlyInsertedInput.length))

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

    inner class LeafNode : Node(currentlyInsertedInput.length - 1, Int.MAX_VALUE) {
        override fun toString(): String {
            return "LeafNode(start=$start, end=end, suffix=$suffix, label=${edgeLabel()})"
        }

        override fun toString(indentationLevel: Int): String {
            return toString()
        }
    }

    inner class RootNode : Node(-1, -1) {
        fun offsetsOf(queryString: String): Set<Int> {
            var i = 0
            var node: Node = this
            while (i < queryString.length) {
                // We try to follow the edge to the next internal node. If there is no such edge, then
                // there are no matches and we return the empty set.
                node = (node.edges[queryString[i]] ?: return setOf())

                // If the edge we just followed is longer than the remainder of the query string, then
                // we get matches only if the edge label starts with the remainder of the query string.
                // Otherwise we get no matches
                if (node.edgeLength() >= (queryString.length - i)) {
                    var j = 0
                    while (j < queryString.length - i) {
                        if (queryString[i + j] != currentlyInsertedInput[node.start + j]) {
                            return setOf()
                        }
                        j++
                    }
                    return suffixesUnderSubtreeRootedAt(node)
                }

                // If the edge we just followed doesn't have an edge label matching the query string,
                // from the current character until the end of the edge label, then there are no
                // matches.
                var k = 0
                while (k < node.edgeLength()) {
                    if (queryString[i + k] != currentlyInsertedInput[node.start + k]) {
                        return setOf()
                    }
                    k++
                }

                // We increase i by the size of the matching edge label we just crossed.
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

        override fun toString(): String {
            return "RootNode(edges:${edges.map { "\n\t'${it.key}'=${it.value.toString(2)}" }})"
        }
    }
}

private enum class SuffixExtensionRule {
    /**
     * Rule one extensions happen in the first extension of every phase, when the leaf nodes are
     * implicitly extended by the addition of the next character. I have included it here for my
     * documentation of the algorithm.
     */
    @Suppress("unused")
    RULE_ONE,

    /**
     * Rule two extensions happen when the suffix is not implicitly present in the tree, so we add
     * it in a new leaf node.
     */
    RULE_TWO,

    /**
     * Rule three extensions happen when the suffix is already implicitly present in the tree, in
     * which case we do nothing. These suffixes will be made explicit later when the tree is
     * canonized by the addition of a unique character at the end. d
     */
    RULE_THREE
}