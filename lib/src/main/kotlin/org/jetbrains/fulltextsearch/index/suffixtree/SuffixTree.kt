package org.jetbrains.fulltextsearch.index.suffixtree

class SuffixTree(private val inputString: String) {
    private val root: RootNode = RootNode()

    init {
        // PHASE 1 EXTENSION 1
        val endPosition = TextPosition(1)
        root.addLeafEdge(LeafNode(endPosition.value() - 1), TextPosition(0), endPosition)

        (2..inputString.length).forEach { phaseNumber ->
            // PHASE i EXTENSION 1
            endPosition.increment()

            (2..phaseNumber).forEach { extensionNumber ->
                // PHASE i EXTENSION j
                val debugAlwaysEnabled = false
                val debugEnabled = false
                if (debugAlwaysEnabled || (debugEnabled && phaseNumber == 7 && extensionNumber == 6)) {
                    Debugger.enable()
                } else {
                    Debugger.disable()
                }

                val suffixOffset = extensionNumber - 1
                val suffixToAdd = inputString.substring(suffixOffset, phaseNumber)
                Debugger.printLine("\nPhase $phaseNumber, extension $extensionNumber")
                Debugger.printLine("Adding string '$suffixToAdd'")
                root.suffixExtension(inputString, suffixToAdd, suffixOffset, endPosition)
                Debugger.printLine("Root node: $root")
            }
        }
    }

    override fun toString(): String {
        return "SuffixTree(root:$root)"
    }

    fun offsetsOf(queryString: String): Set<Int> {
        return root.offsetsOf(inputString, queryString)
    }
}

interface SrcNode {
    fun addLeafEdge(dstNode: LeafNode, srcOffset: TextPosition, dstOffset: TextPosition)

    fun addInternalEdge(dstNode: InternalNode, srcOffset: TextPosition, dstOffset: TextPosition)

    /**
     * Deletes the outbound edge whose srcOffset equals the given value.
     */
    fun deleteEdge(srcOffset: TextPosition)

    /**
     * Performs a suffix extension in the subtree rooted at this node. It adds the String stored in the parameter
     * suffixToAdd, which has the given suffixOffset. The current pointer to the end of the string is also given,
     * because it is needed for constructing leaf nodes.
     */
    fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    )

    /**
     * @return The set of all offsets into the inputString which correspond to matches of the queryString.
     */
    fun offsetsOf(inputString: String, queryString: String): Set<Int>

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at this node.
     */
    fun descendentSuffixOffsets(): Set<Int>
}

interface DstNode {
    /**
     * @return The suffix extension which should be applied to the parent of this node, in order to add the given
     * suffixToAdd into the tree being built from the given inputString.
     *
     * @param inputString is the string which we are building a suffix tree for
     * @param suffixToAdd is the suffix we want to add. We call this method to create an appropriate SuffixExtension to use for that
     * @param suffixOffset is the offset of the suffixToAdd in the inputString
     * @param endPosition is the current pointer to the end of the input, which is needed to construct leaf nodes
     * @param inboundEdgeDstOffset is the dstOffset of the inbound edge to this node, which is needed for leaf edge extensions
     */
    fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition,
        inboundEdgeDstOffset: TextPosition
    ): SuffixExtension

    /**
     * Add an appropriate edge to the srcNode, which goes to this node. Use the given values for the edge's srcOffset
     * and dstOffset.
     */
    fun addAsDstOf(srcNode: InternalNode, srcOffset: TextPosition, dstOffset: TextPosition)

    /**
     * @return The set of all offsets into the inputString which correspond to matches of the queryString.
     */
    fun offsetsOf(inputString: String, queryString: String): Set<Int>

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at this node.
     */
    fun descendentSuffixOffsets(): Set<Int>
}

/**
 * A suffix extension is an operation which mutates a srcNode in order to add an enclosed suffix to the subtree rooted
 * at the srcNode.
 */
fun interface SuffixExtension {
    /**
     * Execute the suffix extension on the subtree rooted at the given srcNode.
     */
    fun extend(srcNode: SrcNode)
}

class Edge(
    @Suppress("unused") private val srcNode: SrcNode, private val dstNode: DstNode,
    private val srcOffset: TextPosition, private val dstOffset: TextPosition
) {
    override fun toString(): String {
        return "Edge(srcOffset=${srcOffset.value()}, dstOffset=${dstOffset.value()}, dstNode=$dstNode)"
    }

    /**
     * The edge decides what kind of suffix extension will be necessary in order to add the given suffixToAdd into the
     * suffix tree being built for the given inputString.
     *
     * @return If the suffixToAdd is able to be added within this edge, or within the subtree rooted at the dstNode of
     * this edge, then it will return a suffix extension which will modify the srcNode in order to perform the required
     * extension. If this edge doesn't match the given suffixToAdd, then this method will return null to indicate that
     * there is action to be taken, but not from this edge.
     *
     * @param suffixOffset is the offset of the suffixToAdd in the inputString
     * @param endPosition is the current pointer to the end of the input, which is needed to construct leaf nodes
     */
    fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ): SuffixExtension? {
        // If the suffix won't go along this edge at all, stop and return null, because no extension is needed here
        val label = label(inputString)
        if (label[0] != suffixToAdd[0]) {
            Debugger.printLine(
                "Suffix '$suffixToAdd' won't go along the edge starting at offset '$srcOffset'. " +
                        "No applicable suffix extension will be created."
            )
            return null
        }

        // If the suffix is implicitly contained within the edge label already, do nothing
        // If the suffix and the edge label are equal, then we also don't need to do anything. This covers that case.
        if (label.startsWith(suffixToAdd)) {
            Debugger.printLine("Suffix '$suffixToAdd' is implicitly contained within the edge label '$label'")
            return SuffixExtension { }
        }

        // If this edge goes to a leaf, and the suffix would extend the current edge label by adding more characters to
        // the end, then extend the edge label.
        // If this edge goes to an internal node, and the suffix would extend the current edge label by adding more
        // characters to the end, then recurse to the internal node
        if (suffixToAdd.startsWith(label)) {
            val recursingSuffix = suffixToAdd.substring(label.length)
            Debugger.printLine(
                "Suffix '$suffixToAdd' extends the current edge label '$label'. " +
                        "Recursing with suffix '$recursingSuffix'."
            )
            return dstNode.suffixExtension(
                inputString,
                recursingSuffix,
                suffixOffset,
                endPosition,
                dstOffset
            )
        }

        // If we fall off the tree in the middle of the edge label, then split the edge
        var i = 0
        val iBound = minOf(label.length, suffixToAdd.length)
        val thisChars = label.toCharArray()
        val otherChars = suffixToAdd.toCharArray()
        while (i < iBound) {
            if (thisChars[i] != otherChars[i]) {
                break
            }
            i++
        }
        val matchLength = i
        val noMatchingPrefix = matchLength == 0
        val fullMatchingPrefix = matchLength == label.length || matchLength == suffixToAdd.length
        val matchIsStrictlyPartial = !(noMatchingPrefix || fullMatchingPrefix)
        if (matchIsStrictlyPartial) {
            Debugger.printLine(
                "Suffix '$suffixToAdd' has a strictly partial match with edge label '$label'. " +
                        "Adding an internal node at offset '$matchLength'."
            )
            return SuffixExtension { srcNode ->
                // Remove the edge that is being replaced
                srcNode.deleteEdge(srcOffset)
                // Add an internal edge for the new internal node
                val internalNode = InternalNode()
                val dstOffsetOfSrcNode = TextPosition(srcOffset.value() + matchLength)
                srcNode.addInternalEdge(internalNode, srcOffset, dstOffsetOfSrcNode)
                // Preserve the existing edge
                dstNode.addAsDstOf(internalNode, dstOffsetOfSrcNode, endPosition)
                // Add the new leaf edge
                Debugger.printLine(
                    "srcOffset=${srcOffset.value()}, suffixOffset=$suffixOffset, " +
                            "matchLength=$matchLength, endPosition=${endPosition.value()}, " +
                            "suffixToAdd.length=${suffixToAdd.length}"
                )
                val newLeafSrcOffset =
                    TextPosition(endPosition.value() - suffixToAdd.length + matchLength)
                Debugger.printLine("Adding new leaf edge from ${newLeafSrcOffset.value()} -> ${endPosition.value()}")
                internalNode.addLeafEdge(LeafNode(suffixOffset), newLeafSrcOffset, endPosition)
            }
        }

        // Throw an exception if we exhaust all the cases - it means there is a bug
        throw RuntimeException("There's a bug in Edge::suffixExtension")
    }

    fun hasSrcOffset(srcOffset: TextPosition): Boolean {
        return this.srcOffset == srcOffset
    }

    /**
     * @return If the queryString is able to be found on this edge, or in any subtree rooted at the dstNode of this
     * edge, then it will return a set of offsets into the inputString which correspond to instances of the queryString.
     * Otherwise, if this edge doesn't match the queryString, it will return null to indicate that the caller should
     * check the other edges of the dstNode, or they may have to conclude that the search was unsuccessful.
     */
    fun offsetsOf(inputString: String, queryString: String): Set<Int>? {
        // If the query string won't go along this edge at all, stop and return null, because no extension is needed here.
        val label = label(inputString)
        if (label[0] != queryString[0]) {
            Debugger.printLine(
                "Query string '$queryString' won't go along the edge starting at offset '$srcOffset'. " +
                        "This edge won't be queried any further for offsets."
            )
            return null
        }

        // If the query string goes along this edge but doesn't get all the way to the end, or only just gets to the end,
        // then return the suffixOffsets of all the leaves of the subtree rooted at the dstNode of this edge.
        if (label.startsWith(queryString)) {
            return descendentSuffixOffsets()
        }

        // If the query string goes along this edge, but is longer than the label and needs to traverse further, then
        // recurse to the dstNode of this edge, and behead the query string by the length that was covered by the label
        // of this edge.
        if (queryString.startsWith(label)) {
            return dstNode.offsetsOf(inputString, queryString.substring(label.length))
        }

        // The only remaining case is a strictly partial match, in which the query string 'falls off' the tree in the
        // middle of the edge label. In this case, there will be no query matches, so we return an empty collection.
        return setOf()
    }

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at the dstNode of this edge.
     */
    fun descendentSuffixOffsets(): Set<Int> {
        return dstNode.descendentSuffixOffsets()
    }

    private fun label(inputString: String) =
        inputString.substring(srcOffset.value(), dstOffset.value())
}

class LogicalSrcNode : SrcNode {
    private val edges = mutableSetOf<Edge>()

    override fun addLeafEdge(dstNode: LeafNode, srcOffset: TextPosition, dstOffset: TextPosition) {
        edges.add(Edge(this, dstNode, srcOffset, dstOffset))
    }

    override fun addInternalEdge(
        dstNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        edges.add(Edge(this, dstNode, srcOffset, dstOffset))
    }

    override fun deleteEdge(srcOffset: TextPosition) {
        edges.removeIf { it.hasSrcOffset(srcOffset) }
    }

    override fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        for (edge in edges) {
            val suffixExtension =
                edge.suffixExtension(inputString, suffixToAdd, suffixOffset, endPosition)
            if (suffixExtension != null) {
                suffixExtension.extend(this)
                return
            }
        }
        Debugger.printLine("Adding leaf with suffixOffset $suffixOffset")
        addLeafEdge(LeafNode(suffixOffset), TextPosition(suffixOffset), endPosition)
    }

    override fun offsetsOf(inputString: String, queryString: String): Set<Int> {
        for (edge in edges) {
            val offsets: Set<Int>? = edge.offsetsOf(inputString, queryString)
            if (offsets != null) {
                return offsets
            }
        }
        return setOf()
    }

    override fun descendentSuffixOffsets(): Set<Int> {
        return edges.flatMapTo(mutableSetOf()) {
            it.descendentSuffixOffsets()
        }
    }

    override fun toString(): String {
        return "LogicalSrcNode(edges=${edges.joinToString { "\n${it}" }})"
    }
}

class RootNode : SrcNode {
    private val srcNode: SrcNode = LogicalSrcNode()

    override fun addLeafEdge(dstNode: LeafNode, srcOffset: TextPosition, dstOffset: TextPosition) {
        srcNode.addLeafEdge(dstNode, srcOffset, dstOffset)
    }

    override fun addInternalEdge(
        dstNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        srcNode.addInternalEdge(dstNode, srcOffset, dstOffset)
    }

    override fun deleteEdge(srcOffset: TextPosition) {
        srcNode.deleteEdge(srcOffset)
    }

    override fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        srcNode.suffixExtension(inputString, suffixToAdd, suffixOffset, endPosition)
    }

    override fun offsetsOf(inputString: String, queryString: String): Set<Int> {
        return srcNode.offsetsOf(inputString, queryString)
    }

    override fun descendentSuffixOffsets(): Set<Int> {
        return srcNode.descendentSuffixOffsets()
    }

    override fun toString(): String {
        return "RootNode(srcNode=$srcNode)"
    }
}

class InternalNode : SrcNode, DstNode {
    private val srcNode: SrcNode = LogicalSrcNode()

    override fun addLeafEdge(dstNode: LeafNode, srcOffset: TextPosition, dstOffset: TextPosition) {
        srcNode.addLeafEdge(dstNode, srcOffset, dstOffset)
    }

    override fun addInternalEdge(
        dstNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        srcNode.addInternalEdge(dstNode, srcOffset, dstOffset)
    }

    override fun deleteEdge(srcOffset: TextPosition) {
        srcNode.deleteEdge(srcOffset)
    }

    // Recall that this method applies a suffix extension to the node.
    override fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        srcNode.suffixExtension(inputString, suffixToAdd, suffixOffset, endPosition)
    }

    // Recall that this method returns a suffix extension to apply to a given srcNode in order to add the given
    // suffixToAdd into the subtree rooted the srcNode.
    override fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition,
        inboundEdgeDstOffset: TextPosition
    ): SuffixExtension {
        return SuffixExtension {
            srcNode.suffixExtension(inputString, suffixToAdd, suffixOffset, endPosition)
        }
    }

    override fun addAsDstOf(
        srcNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        srcNode.addInternalEdge(this, srcOffset, dstOffset)
    }

    override fun offsetsOf(inputString: String, queryString: String): Set<Int> {
        return srcNode.offsetsOf(inputString, queryString)
    }

    override fun descendentSuffixOffsets(): Set<Int> {
        return srcNode.descendentSuffixOffsets()
    }

    override fun toString(): String {
        return "InternalNode(srcNode=$srcNode)"
    }
}

class LeafNode(private val suffixOffset: Int) : DstNode {

    override fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition,
        inboundEdgeDstOffset: TextPosition
    ): SuffixExtension {
        return SuffixExtension { inboundEdgeDstOffset.setTo(suffixToAdd.length) }
    }

    override fun addAsDstOf(
        srcNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        srcNode.addLeafEdge(this, srcOffset, dstOffset)
    }

    override fun descendentSuffixOffsets(): Set<Int> {
        return setOf(suffixOffset)
    }

    override fun offsetsOf(inputString: String, queryString: String): Set<Int> {
        return setOf(suffixOffset)
    }

    override fun toString(): String {
        return "LeafNode(suffixOffset=$suffixOffset)"
    }
}

class TextPosition(private var i: Int) {
    fun increment() {
        i++
    }

    fun value() = i

    fun setTo(i: Int) {
        this.i = i
    }

    operator fun minus(fromOffset: TextPosition) = TextPosition(i - fromOffset.i)

    operator fun plus(fromOffset: TextPosition) = TextPosition(i + fromOffset.i)

    override fun toString(): String {
        return "TextPosition(i=$i)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextPosition

        if (i != other.i) return false

        return true
    }

    override fun hashCode(): Int {
        return i
    }
}

object Debugger {
    private var enabled = false

    fun printLine(s: String) {
        if (enabled) {
            println(s)
        }
    }

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
    }
}