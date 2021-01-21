package org.jetbrains.fulltextsearch.index.suffixtree

class SuffixTree(private val terminatedInputString: String, private val root: RootNode) {

    companion object {
        fun defaultConstruction(input: String): SuffixTree {
            return ukkonenConstruction(input)
        }

        fun ukkonenConstruction(input: String): SuffixTree {
            @Suppress("NAME_SHADOWING")
            val input = input + terminatingCharacter()

            // Initialise suffix tree
            val root = RootNode()
            val endPosition = TextPosition(1)
            root.addLeafEdge(LeafNode(0), TextPosition(0), endPosition)

            // Prepare variables
            val remainingSuffixes = RemainingSuffixesPointer(0)
            val suffixLinkCandidate = SuffixLinkCandidate()
            val activePoint = ActivePoint(
                input, root, endPosition, remainingSuffixes, suffixLinkCandidate
            )

            // Phases
            (2..input.length).forEach { phaseNumber ->
                Debugger.enableIf { false }
                val nextCharOffset = phaseNumber - 1
                Debugger.info(
                    "\nStarting phase $phaseNumber for character '${input[nextCharOffset]}'\n" +
                            "Before the first extension, there are $remainingSuffixes suffixes remaining.\n" +
                            "root=$root;\nactivePoint=$activePoint;\n"
                )

                // Extend leaf edge offsets
                endPosition.increment()
                remainingSuffixes.increment()

                // Suffix links should only be created within a phase
                suffixLinkCandidate.reset()

                // The phase ends when it is no longer possible to add more suffixes into the tree in
                // the current phase. This happens either when we run out of remaining suffixes to add,
                // i.e. when remainder == 0, or when we perform a rule three suffix extension.
                var canAddMoreSuffixes = true
                while (canAddMoreSuffixes) {
                    canAddMoreSuffixes = activePoint.addNextSuffix(nextCharOffset)
                }
            }
            return SuffixTree(input, root)
        }

        // We append a terminating character to the input in order to ensure that we get a true
        // suffix tree at the end of the process, rather than an implicit suffix tree.
        private fun terminatingCharacter(): Char {
            return '\u0000'
        }
    }

    override fun toString(): String {
        return "SuffixTree(root:$root)"
    }

    fun offsetsOf(queryString: String): Set<Int> {
        return root.offsetsOf(terminatedInputString, queryString)
    }

    fun leaves(): Set<LeafNode> {
        return root.descendentLeaves()
    }
}

class RemainingSuffixesPointer(private var remainingSuffixes: Int = 0) {
    fun value(): Int = remainingSuffixes

    fun increment() {
        remainingSuffixes++
    }

    fun decrement() {
        remainingSuffixes--
    }

    override fun toString(): String {
        return "$remainingSuffixes"
    }
}

class ActivePoint(
    private val input: String,
    private val root: RootNode,
    private val endPosition: TextPosition,
    private val remainingSuffixes: RemainingSuffixesPointer,
    private val suffixLinkCandidate: SuffixLinkCandidate
) {
    companion object;

    private var activeEdge = -1
    private var activeLength = 0
    private var activeNode: ActiveNode = root

    /**
     * @return Whether or not more suffixes can be added in the current phase.
     */
    fun addNextSuffix(nextCharOffset: Int): Boolean {
        val nextChar = input[nextCharOffset]
        val suffixOffset = endPosition.value() - remainingSuffixes.value()
        Debugger.debug(
            "Adding suffix '${
                input.substring(
                    suffixOffset,
                    endPosition.value()
                )
            }' with offset $suffixOffset for char '$nextChar' at index $nextCharOffset, " +
                    "and there are ${remainingSuffixes.value()} suffixes remaining. " +
                    "endPosition=${endPosition.value()}\nActive point=$this"
        )
        if (activeLength == 0) {
            if (activeNode.hasEdgeWithChar(input, nextChar, 0)) {
                Debugger.info("Activating edge with leading char '$nextChar'")
                activeEdge = nextCharOffset
                activeLength++
                return false
            } else {
                Debugger.info("Adding leaf node [$nextCharOffset, ${endPosition.value()}]($suffixOffset)")
                activeNode.addLeafEdge(
                    LeafNode(suffixOffset),
                    TextPosition(nextCharOffset),
                    endPosition
                )
                remainingSuffixes.decrement()
                (activeNode as? InternalNode)?.advanceActivePoint(this, suffixOffset + 1)
                return remainingSuffixes.value() > 0
            }
        } else {
            if (activeNode.edgeHasChar(input, activeEdge, activeLength, nextChar)) {
                Debugger.info(
                    "Advancing along active edge $activeEdge " +
                            "due to next char '$nextChar' at label offset $activeLength"
                )
                activeLength++
                normalizeActivePoint()
                return false
            } else {
                val canAddMoreSuffixes = activeNode.extendEdge(
                    input, activeEdge, activeLength, suffixLinkCandidate,
                    nextCharOffset, suffixOffset, endPosition,
                    this, remainingSuffixes
                )
                activeNode.advanceActivePoint(this, suffixOffset + 1)
                return canAddMoreSuffixes
            }
        }
    }

    fun activeNodeOffset(): Pair<Int, Int> {
        return Pair(activeEdge, activeLength)
    }

    fun setActiveEdgeOffset(edgeSrcOffset: Int) {
        this.activeEdge = edgeSrcOffset
    }

    fun setActiveNodeOffset(activeEdge: Int, activeLength: Int) {
        this.activeEdge = activeEdge
        this.activeLength = activeLength
    }

    override fun toString(): String {
        return "ActivePoint(activeEdge=$activeEdge, activeLength=$activeLength, activeNode=$activeNode)"
    }

    fun advance(internalNode: InternalNode, activeEdge: Int, activeLength: Int) {
        this.activeNode = internalNode
        this.activeEdge = activeEdge
        this.activeLength = activeLength
    }

    fun activeNodeIsInternalNode(activeNodeMatcher: (InternalNode) -> Boolean): Boolean {
        val internalNode = activeNode as? InternalNode
        return if (internalNode == null) {
            false
        } else {
            activeNodeMatcher(internalNode)
        }
    }

    fun shiftActiveEdge(nextSuffixOffset: Int) {
        activeEdge = nextSuffixOffset
        activeLength--
        normalizeActivePoint(eagerNodeHop = true)
    }

    fun followSuffixLink(suffixLink: InternalNode?) {
        activeNode = suffixLink ?: root
        normalizeActivePoint(eagerNodeHop = true)
    }

    private fun normalizeActivePoint(eagerNodeHop: Boolean = false) {
        // TODO: Normalise recursively until reaching a node where it's all cool.
        activeNode.normalizeActivePoint(
            input, activeEdge, activeLength, this,
            eagerNodeHop = eagerNodeHop
        )
    }

    fun activeNodeIsRoot(): Boolean {
        return activeNode is RootNode
    }
}

class SuffixLinkCandidate {
    private var nextNodeToLinkFrom: InternalNode? = null

    fun linkTo(internalNode: InternalNode) {
        if (nextNodeToLinkFrom != null && nextNodeToLinkFrom != internalNode) {
            Debugger.info("Linking from $nextNodeToLinkFrom;\nto $internalNode;")
            nextNodeToLinkFrom!!.linkTo(internalNode)
        }
        Debugger.info("Next suffix link candidate is $internalNode;")
        nextNodeToLinkFrom = internalNode
    }

    fun reset() {
        nextNodeToLinkFrom = null
    }
}

interface SrcNode {
    fun addLeafEdge(dstNode: LeafNode, srcOffset: TextPosition, dstOffset: TextPosition)

    fun addInternalEdge(dstNode: InternalNode, srcOffset: TextPosition, dstOffset: TextPosition)

    fun deleteEdge(edge: Edge)

    /**
     * @return The set of all offsets into the input which correspond to matches of the
     * queryString.
     */
    fun offsetsOf(input: String, queryString: String): Set<Int>

    /**
     * @return The set of all the leaf nodes in the subtree rooted at this node.
     */
    fun descendentLeaves(): Set<LeafNode>
}

/**
 * This defines the set of responsibilities that an ActiveNode must support.
 * An ActiveNode is always a SrcNode.
 */
interface ActiveNode : SrcNode {
    fun hasEdgeWithChar(input: String, c: Char, labelOffset: Int): Boolean

    fun edgeHasChar(input: String, edgeSrcOffset: Int, edgeLabelOffset: Int, c: Char): Boolean

    fun hasLeafEdge(srcOffset: Int, dstOffset: Int, suffixOffset: Int): Boolean

    fun hasInternalEdge(
        srcOffset: Int,
        dstOffset: Int,
        internalNodeMatcher: (InternalNode) -> Boolean
    ): Boolean

    fun activateEdge(input: String, edgeLeadingChar: Char, activePoint: ActivePoint)

    fun extendEdge(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        suffixLinkCandidate: SuffixLinkCandidate,
        charToAddOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition,
        activePoint: ActivePoint,
        remainingSuffixes: RemainingSuffixesPointer
    ): Boolean

    fun normalizeActivePoint(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        activePoint: ActivePoint,
        eagerNodeHop: Boolean = false
    )

    fun advanceActivePoint(activePoint: ActivePoint, nextSuffixOffset: Int)
}

interface DstNode {
    /**
     * Add an appropriate edge to the srcNode, which goes to this node. Use the given values for the
     * edge's srcOffset and dstOffset.
     */
    fun addAsDstOf(srcNode: InternalNode, srcOffset: TextPosition, dstOffset: TextPosition)

    /**
     * @return The set of all offsets into the input which correspond to matches of the
     * queryString.
     */
    fun offsetsOf(input: String, queryString: String): Set<Int>

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at this node.
     */
    fun descendentSuffixOffsets(): Set<Int>

    fun leaves(): Set<LeafNode>
}

class Edge(
    private val srcNode: SrcNode, private val dstNode: DstNode,
    private val srcOffset: TextPosition, private val dstOffset: TextPosition
) {
    override fun toString(): String {
        return "Edge(srcOffset=${srcOffset.value()}, dstOffset=${dstOffset.value()}, dstNode=$dstNode)"
    }

    private fun label(input: String) =
        input.substring(srcOffset.value(), dstOffset.value())

    /**
     * @return If the queryString is able to be found on this edge, or in any subtree rooted at the
     * dstNode of this edge, then it will return a set of offsets into the input which
     * correspond to instances of the queryString. Otherwise, if this edge doesn't match the
     * queryString, it will return null to indicate that the caller should check the other edges of
     * the dstNode, or they may have to conclude that the search was unsuccessful.
     */
    fun offsetsOf(input: String, queryString: String): Set<Int>? {
        // If the query string won't go along this edge at all, stop and return null, because no
        // extension is needed here.
        val label = label(input)
        if (label[0] != queryString[0]) {
            Debugger.info(
                "Query string '$queryString' won't go along the edge starting at offset '$srcOffset'. " +
                        "This edge won't be queried any further for offsets."
            )
            return null
        }

        // If the query string goes along this edge but doesn't get all the way to the end, or only
        // just gets to the end, then return the suffixOffsets of all the leaves of the subtree
        // rooted at the dstNode of this edge.
        if (label.startsWith(queryString)) {
            Debugger.info(
                "The edge label '$label' starts with the query string '$queryString'. " +
                        "Returning all descendent suffix offsets."
            )
            return descendentSuffixOffsets()
        }

        // If the query string goes along this edge, but is longer than the label and needs to
        // traverse further, then recurse to the dstNode of this edge, and behead the query string
        // by the length that was covered by the label of this edge.
        if (queryString.startsWith(label)) {
            @Suppress("SpellCheckingInspection")
            val recursedQueryString = queryString.substring(label.length)
            Debugger.info(
                "The query string '$queryString' starts with the edge label '$label'. " +
                        "Recursing to the dstNode of this edge, with the shortened query string '$recursedQueryString'."
            )
            return dstNode.offsetsOf(input, recursedQueryString)
        }

        // The only remaining case is a strictly partial match, in which the query string 'falls
        // off' the tree in the middle of the edge label. In this case, there will be no query
        // matches, so we return an empty collection.
        Debugger.info(
            "The query string '$queryString' has a strictly partial match with the edge label '$label'. " +
                    "This means that there are no matches for the query string in the text. " +
                    "Returning an empty collection."
        )
        return setOf()
    }

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at the dstNode
     * of this edge.
     */
    fun descendentSuffixOffsets(): Set<Int> {
        return dstNode.descendentSuffixOffsets()
    }

    fun labelHasChar(input: String, c: Char, labelOffset: Int): Boolean {
        return input[srcOffset.value() + labelOffset] == c
    }

    private fun split(
        charToAddOffset: Int,
        edgeLabelOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition
    ): InternalNode {
        // Remove the edge that is being replaced (this edge)
        srcNode.deleteEdge(this)
        // Add an internal edge for the new internal node
        val internalNode = InternalNode()
        val dstOffsetOfSrcNode = TextPosition(srcOffset.value() + edgeLabelOffset)
        Debugger.info(
            "Splitting to create a new internal edge of " +
                    "${srcOffset.value()} -> ${dstOffsetOfSrcNode.value()}"
        )
        srcNode.addInternalEdge(internalNode, srcOffset, dstOffsetOfSrcNode)
        // Preserve the existing edge
        Debugger.info(
            "Preserving dstNode under new edge ${dstOffsetOfSrcNode.value()} -> ${dstOffset.value()}"
        )
        dstNode.addAsDstOf(internalNode, dstOffsetOfSrcNode, dstOffset)
        // Add the new leaf edge
        Debugger.info(
            "Adding new leaf edge from $charToAddOffset -> ${endPosition.value()}," +
                    "for suffix with offset $suffixOffset"
        )
        internalNode.addLeafEdge(
            LeafNode(suffixOffset),
            TextPosition(charToAddOffset),
            endPosition
        )
        return internalNode
    }

    fun leaves(): Set<LeafNode> {
        return dstNode.leaves()
    }

    fun isLeafEdge(srcOffset: Int, dstOffset: Int, suffixOffset: Int): Boolean {
        val offsetsMatch =
            this.srcOffset.value() == srcOffset && this.dstOffset.value() == dstOffset
        val dstNodeMatches = (dstNode as? LeafNode)?.suffixOffset() == suffixOffset
        return offsetsMatch && dstNodeMatches
    }

    fun isInternalEdge(srcOffset: Int, dstOffset: Int): Boolean {
        val offsetsMatch =
            this.srcOffset.value() == srcOffset && this.dstOffset.value() == dstOffset
        val dstNodeMatches = dstNode is InternalNode
        return offsetsMatch && dstNodeMatches
    }

    fun leadsToInternalNode(internalNodeMatcher: (InternalNode) -> Boolean): Boolean {
        return (dstNode as? InternalNode)?.let { internalNodeMatcher(it) } ?: false
    }

    fun activateEdge(activePoint: ActivePoint) {
        activePoint.setActiveEdgeOffset(srcOffset.value())
    }

    fun labelLength(): Int {
        return dstOffset.value() - srcOffset.value()
    }

    private fun addLeafNodeToChild(
        suffixOffset: Int,
        charToAddOffset: Int,
        endPosition: TextPosition
    ) {
        Debugger.info(
            "Adding leaf [$charToAddOffset, ${endPosition.value()}]($suffixOffset) " +
                    "to an internal node."
        )
        (dstNode as InternalNode).addLeafEdge(
            LeafNode(suffixOffset),
            TextPosition(charToAddOffset),
            endPosition
        )
    }

    fun advanceActivePoint(
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        activePoint: ActivePoint
    ) {
        val edgeLength = labelLength()
        Debugger.info(
            "Hopped over a node - updating active edge from ($edgeSrcOffset, $edgeLabelOffset) " +
                    "to (${edgeSrcOffset + edgeLength}, ${edgeLabelOffset - edgeLength})"
        )
        activePoint.advance(
            dstNode as InternalNode,
            edgeSrcOffset + edgeLength,
            edgeLabelOffset - edgeLength
        )
    }

    fun extend(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        suffixLinkCandidate: SuffixLinkCandidate,
        charToAddOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition,
        activePoint: ActivePoint,
        remainingSuffixes: RemainingSuffixesPointer
    ): Boolean {
        if (edgeLabelOffset == labelLength()) {
            val reachedNode = dstNode as InternalNode
            val nextChar = input[charToAddOffset]
            return if (reachedNode.hasEdgeWithChar(input, nextChar, 0)) {
                // These values will then be modified again during active point normalization
                activePoint.advance(reachedNode, edgeSrcOffset, edgeLabelOffset + 1)
                // This is an implicit extension and ends the current phase.
                false
            } else {
                addLeafNodeToChild(suffixOffset, charToAddOffset, endPosition)
                remainingSuffixes.decrement()
                true
            }
        } else {
            val newNode = split(
                charToAddOffset, edgeLabelOffset, suffixOffset, endPosition
            )
            remainingSuffixes.decrement()
            suffixLinkCandidate.linkTo(newNode)
            return true
        }
    }
}

abstract class DelegateSrcNode : ActiveNode {
    private val edges = mutableSetOf<Edge>()

    override fun addLeafEdge(
        dstNode: LeafNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        edges.add(Edge(this, dstNode, srcOffset, dstOffset))
    }

    override fun addInternalEdge(
        dstNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        edges.add(Edge(this, dstNode, srcOffset, dstOffset))
    }

    override fun deleteEdge(edge: Edge) {
        edges.remove(edge)
    }

    override fun offsetsOf(input: String, queryString: String): Set<Int> {
        for (edge in edges) {
            val offsets: Set<Int>? = edge.offsetsOf(input, queryString)
            if (offsets != null) {
                return offsets
            }
        }
        return setOf()
    }

    override fun hasEdgeWithChar(input: String, c: Char, labelOffset: Int): Boolean {
        return edges.any { it.labelHasChar(input, c, labelOffset) }
    }

    override fun edgeHasChar(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        c: Char
    ): Boolean {
        val activeEdge = edges.first { it.labelHasChar(input, input[edgeSrcOffset], 0) }
        return activeEdge.labelHasChar(input, c, edgeLabelOffset)
    }

    override fun descendentLeaves(): Set<LeafNode> {
        return edges.flatMapTo(mutableSetOf()) { it.leaves() }
    }

    override fun hasLeafEdge(srcOffset: Int, dstOffset: Int, suffixOffset: Int): Boolean {
        return edges.any { it.isLeafEdge(srcOffset, dstOffset, suffixOffset) }
    }

    override fun hasInternalEdge(
        srcOffset: Int,
        dstOffset: Int,
        internalNodeMatcher: (InternalNode) -> Boolean
    ): Boolean {
        val internalEdge: Edge? = edges.firstOrNull { it.isInternalEdge(srcOffset, dstOffset) }
        return internalEdge?.leadsToInternalNode(internalNodeMatcher) ?: false
    }

    override fun activateEdge(input: String, edgeLeadingChar: Char, activePoint: ActivePoint) {
        edges.first { it.labelHasChar(input, edgeLeadingChar, 0) }
            .activateEdge(activePoint)
    }

    override fun extendEdge(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        suffixLinkCandidate: SuffixLinkCandidate,
        charToAddOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition,
        activePoint: ActivePoint,
        remainingSuffixes: RemainingSuffixesPointer
    ): Boolean {
        val activeEdge = edges.first { it.labelHasChar(input, input[edgeSrcOffset], 0) }
        return activeEdge.extend(
            input, edgeSrcOffset, edgeLabelOffset, suffixLinkCandidate,
            charToAddOffset, suffixOffset, endPosition,
            activePoint, remainingSuffixes
        )
    }

    override fun normalizeActivePoint(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        activePoint: ActivePoint,
        eagerNodeHop: Boolean
    ) {
        val activeEdge = edges.firstOrNull { it.labelHasChar(input, input[edgeSrcOffset], 0) }
            ?: return
        val eagerHopModifier = if (eagerNodeHop) 1 else 0
        if (edgeLabelOffset + eagerHopModifier > activeEdge.labelLength()) {
            activeEdge.advanceActivePoint(edgeSrcOffset, edgeLabelOffset, activePoint)
        }
    }

    fun descendentLeavesSuffixOffsets(): Set<Int> {
        return edges.flatMapTo(mutableSetOf()) {
            it.descendentSuffixOffsets()
        }
    }

    override fun toString(): String {
        return "DelegateSrcNode(edges=${edges.joinToString { "\n${it}" }})"
    }
}

class RootNode : DelegateSrcNode() {
    override fun toString(): String {
        return "RootNode(${super.toString()})"
    }

    override fun advanceActivePoint(activePoint: ActivePoint, nextSuffixOffset: Int) {
        activePoint.shiftActiveEdge(nextSuffixOffset)
    }
}

class InternalNode : DelegateSrcNode(), DstNode {
    private var suffixLink: InternalNode? = null

    override fun toString(): String {
        return "InternalNode(${super.toString()}, suffixLink.null?=${suffixLink == null})"
    }

    override fun advanceActivePoint(activePoint: ActivePoint, nextSuffixOffset: Int) {
        activePoint.followSuffixLink(suffixLink)
    }

    override fun addAsDstOf(
        srcNode: InternalNode,
        srcOffset: TextPosition,
        dstOffset: TextPosition
    ) {
        srcNode.addInternalEdge(this, srcOffset, dstOffset)
    }

    override fun descendentSuffixOffsets(): Set<Int> {
        return super.descendentLeavesSuffixOffsets()
    }

    override fun leaves(): Set<LeafNode> {
        return descendentLeaves()
    }

    fun linkTo(internalNode: InternalNode) {
        this.suffixLink = internalNode
    }

    fun hasSuffixLink(suffixLinkMatcher: (InternalNode?) -> Boolean): Boolean {
        return suffixLinkMatcher(suffixLink)
    }
}

class LeafNode(private val suffixOffset: Int) : DstNode {
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

    override fun leaves(): Set<LeafNode> {
        return setOf(this)
    }

    override fun offsetsOf(input: String, queryString: String): Set<Int> {
        return setOf()
    }

    override fun toString(): String {
        return "LeafNode(suffixOffset=$suffixOffset)"
    }

    fun suffixOffset() = suffixOffset
}

class TextPosition(private var i: Int) {
    fun increment() {
        i++
    }

    fun value() = i

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
    private var debug = false

    fun info(s: String) {
        if (enabled) {
            println(s)
        }
    }

    fun debug(s: String) {
        if (enabled && debug) {
            println(s)
        }
    }

    fun enableIf(function: () -> Boolean) {
        if (function()) {
            enable()
        } else {
            disable()
        }
    }

    private fun enable() {
        enabled = true
    }

    private fun disable() {
        enabled = false
    }

    @Suppress("unused")
    fun debugFor(function: () -> Unit) {
        val wasEnabled = enabled
        val wasDebug = debug
        enable()
        debug = true
        function.invoke()
        enabled = wasEnabled
        debug = wasDebug
    }

    @Suppress("unused")
    fun enableFor(function: () -> Unit) {
        val wasEnabled = enabled
        enable()
        function.invoke()
        enabled = wasEnabled
    }
}