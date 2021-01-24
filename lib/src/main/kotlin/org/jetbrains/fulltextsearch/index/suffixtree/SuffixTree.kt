package org.jetbrains.fulltextsearch.index.suffixtree

import java.util.*


class SuffixTree(length: Int) {
    private val text: CharArray = CharArray(length)
    private val infinity = Int.MAX_VALUE / 2
    private val rootNode: Node = RootNode()

    private val nodes: MutableList<Node?> = MutableList<Node?>(2 * length + 2) { null }.run {
        this[1] = rootNode
        this
    }


    private var activeNodeId: Int = rootNode.id
    private var position: Int = -1
    private var currentNode = 1
    private var nodeNeedingSuffixLink: Node? = null
    private var remainder = 0
    private var activeLength = 0
    private var activeEdge = 0


    companion object {
        fun ukkonenConstruction(input: String): SuffixTree {
            val tree = SuffixTree(input.length + 1)
            input.forEach { tree.addChar(it) }
            tree.canonizeTree()
            return tree
        }
    }

    private fun addSuffixLink(node: Node) {
        nodeNeedingSuffixLink?.linkTo(node)
        nodeNeedingSuffixLink = node
    }

    private fun activeEdge(): Char {
        return text[activeEdge]
    }

    /**
     * @return true if the reference to the active node was non-canonical, requiring us to step
     * through the tree.
     */
    private fun canonizeActivePoint(nextNode: Node): Boolean {
        val edgeLength = nextNode.edgeLength()
        if (activeLength >= edgeLength) {
            activeEdge += edgeLength
            activeLength -= edgeLength
            activeNodeId = nextNode.id
            return true
        }
        return false
    }

    private fun canonizeTree() {
        addChar('\u0000')
    }

    private fun addNode(start: Int, end: Int): Node {
        val i = ++currentNode
        val node = Node(start, end)
        nodes[i] = node
        return node
    }

    fun addChar(c: Char) {
        // Add the character into the array of characters we have already indexed, and increase our
        // pointer to the end of the text
        text[++position] = c

        // We only add suffix links within a phase, so we reset it.
        nodeNeedingSuffixLink = null

        // There is now an additional suffix which is not yet explicit in the tree
        remainder++

        while (remainder > 0) {
            if (activeLength == 0) {
                // Point our active edge at the next character in the text
                activeEdge = position
            }

            if (!activeNode().next.containsKey(activeEdge())) {
                // If the active node doesn't yet have a child node corresponding to the next
                // character, add one. When we perform a leaf insertion like this, we need to add a
                // suffix link.
                val leaf = addLeaf().id
                activeNode().next[activeEdge()] = leaf
                addSuffixLink(activeNode())
            } else {
                // Since the active node has an edge starting with the next character, we need to
                // either create a new leaf node, continue down the active edge, or split the
                // current edge and create both an internal node and a leaf node.
                val nextNodeId = activeNode().next[activeEdge()]!!
                val nextNode = nodes[nextNodeId]!!

                // If the reference to the active point is non-canonical, then canonize it by
                // stepping through the tree, and then go to the next extension of the current
                // phase so that we can do all our steps from the basis of a canonical reference to
                // the active point.
                if (canonizeActivePoint(nextNode)) {
                    continue
                }

                // If the character is already present on the edge we are creating for the next
                // node, then the suffix is implicitly contained within the tree already, so we
                // end the current phase. We need to add a suffix link to the active node as well,
                // because otherwise the active point won't get to the correct place after our next
                // insertion.
                if (text[nextNode.start + activeLength] == c) {
                    activeLength++
                    addSuffixLink(activeNode())
                    break
                }

                // The next node we're adding will be an internal node. We add it, and create a
                // leaf node from it whose edge corresponds to the character we're adding. We also
                // create a suffix link for the newly added internal node.
                val internalNode = addNode(nextNode.start, nextNode.start + activeLength)
                activeNode().next[activeEdge()] = internalNode.id
                val leaf = addLeaf().id
                internalNode.next[c] = leaf
                nextNode.start += activeLength
                internalNode.next[text[nextNode.start]] = nextNode.id
                addSuffixLink(internalNode)
            }

            // Since we have completed the above conditional block, it means that we have added a
            // new leaf node, which means that a new suffix has been made explicit within the tree.
            // When this happens, we decrement the number of suffixes that still need to be added to
            // the tree.
            remainder--

            if (activeNodeId == rootNode.id && activeLength > 0) {
                // When we insert a node from root, we decrement our active length, and pull our
                // active edge forwards to point at the start of the next suffix we're adding.
                activeLength--
                activeEdge = position - remainder + 1
            } else {
                // When we insert a node from an internal node, we follow its suffix link if it has
                // one. The default suffix link for any node is root.
                activeNodeId = activeNode().linkedNodeId()
            }
        }
    }

    private fun addLeaf(): Node = addNode(position, infinity)

    private fun activeNode(): Node = nodes[activeNodeId]!!

    /**
     * Finds the offsets of the given query string in the root
     */
    fun offsetsOf(queryString: String): Set<Int> {
        var i = 0
        var nodeId = rootNode.id
        while (i < queryString.length) {
            // If we're at a leaf, then we get a match if the leaf's text matches the query string.
            // Otherwise we don't get a match.
            if (nodes[nodeId]!!.next.isEmpty()) {
                val edgeLabel = nodes[nodeId]!!.edgeLabel()
                return if (edgeLabel.startsWith(queryString)) {
                    setOf(nodes[nodeId]!!.suffix)
                } else {
                    setOf()
                }
            }

            // If there are no outbound edges for the next character, then there are no matches
            val queryChar = queryString[i]
            if (!nodes[nodeId]!!.next.containsKey(queryChar)) {
                return setOf()
            }

            // We follow the edge to the next internal node
            nodeId = nodes[nodeId]!!.next[queryChar]!!
            val edgeLabel = nodes[nodeId]!!.edgeLabel()

            // If the edge we just followed is longer than the remainder of the query string, then
            // we get matches the edge label starts with the remainder of the query string.
            // Otherwise we get no matches
            if (nodes[nodeId]!!.edgeLength() >= (queryString.length - i)) {
                return if (edgeLabel.startsWith(queryString.substring(i))) {
                    suffixesUnderSubtreeRootedAt(nodeId)
                } else {
                    setOf()
                }
            }

            // If the edge we just followed doesn't have an edge label matching the query string,
            // then there are no matches
            if (edgeLabel != queryString.substring(i, i + nodes[nodeId]!!.edgeLength())) {
                return setOf()
            }

            // We increase i by the size of the next edge label
            i += nodes[nodeId]!!.edgeLength()
        }

        // If we make it out of the loop, then we have consumed the full query string by traversing
        // edges from the root. This means that all the suffixes stored within the current subtree
        // will be prefixed by the query string.
        return suffixesUnderSubtreeRootedAt(nodeId)
    }

    private fun suffixesUnderSubtreeRootedAt(node: Int): Set<Int> {
        return if (nodes[node]!!.next.isEmpty()) {
            setOf(nodes[node]!!.suffix)
        } else {
            nodes[node]!!.next.flatMap { suffixesUnderSubtreeRootedAt(it.value) }.toSet()
        }
    }

    override fun toString(): String {
        return "SuffixTree(nodes={\n${
            nodes.mapIndexed { index, node -> "\t$index $node" }.joinToString("\n")
        }\n})"
    }

    /**
     * This class represents all nodes, including the root node, internal nodes, and leaf nodes.
     *
     * It also contains information about the edge that connects it to its parent, via the 'start'
     * and 'end' fields.
     */
    open inner class Node(var start: Int, private var end: Int) {
        open val id = currentNode

        private var link: Int? = null

        val suffix = position - remainder + 1
        var next = TreeMap<Char, Int>()

        open fun linkedNodeId(): Int {
            return link ?: rootNode.id
        }

        open fun linkTo(node: Node) {
            link = node.id
        }

        fun edgeLength(): Int {
            return minOf(end, position + 1) - start
        }

        fun edgeLabel(): String {
            if (start == -1) {
                return ""
            }
            val endPosition = minOf(end, text.size)
            val chars = CharArray(endPosition - start)
            (start until endPosition).forEach {
                chars[it - start] = text[it]
            }
            return String(chars)
        }

        override fun toString(): String {
            return "Node(next=$next, start=$start, end=${
                if (end == infinity) {
                    "end"
                } else {
                    end.toString()
                }
            }, suffix=$suffix, link=$link, label=${edgeLabel()})"
        }
    }

    inner class RootNode : Node(-1, -1) {
        override var id = 1
    }
}
