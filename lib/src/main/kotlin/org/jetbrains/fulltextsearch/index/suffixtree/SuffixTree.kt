package org.jetbrains.fulltextsearch.index.suffixtree

class SuffixTree(private val terminatedInputString: String, private val root: RootNode) {

    companion object {
        fun defaultConstruction(input: String): SuffixTree {
            return ukkonenConstruction(input)
        }

        fun naiveConstruction(input: String): SuffixTree {
            val root = RootNode()
            val terminatedInputString = input + terminatingCharacter()

            // PHASE 1 EXTENSION 1
            val endPosition = TextPosition(1)
            root.addLeafEdge(LeafNode(endPosition.value() - 1), TextPosition(0), endPosition)

            (2..terminatedInputString.length).forEach { phaseNumber ->
                Debugger.enableIf { false }
                Debugger.printLine(
                    "\nStarting phase $phaseNumber " +
                            "for character '${terminatedInputString[phaseNumber - 1]}'\n"
                )

                // PHASE i EXTENSION 1
                endPosition.increment()

                (2..phaseNumber).forEach { extensionNumber ->
                    // PHASE i EXTENSION j
                    val suffixOffset = extensionNumber - 1
                    val suffixToAdd = terminatedInputString.substring(suffixOffset, phaseNumber)
                    Debugger.printLine("\nPhase $phaseNumber, extension $extensionNumber")
                    Debugger.printLine("Adding string '$suffixToAdd'")
                    root.addSuffix(terminatedInputString, suffixToAdd, suffixOffset, endPosition)
                    Debugger.printLine("Root node: $root")
                }
            }

            return SuffixTree(terminatedInputString, root)
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
            val activePoint = ActivePoint(input, root, endPosition, remainingSuffixes)

            // Phases
            (2..input.length).forEach { phaseNumber ->
                Debugger.enableIf { false }
                val nextCharOffset = phaseNumber - 1
                Debugger.printLine(
                    "\nStarting phase $phaseNumber for character '${input[nextCharOffset]}'\n" +
                            "Before the first extension, there are $remainingSuffixes suffixes remaining.\n" +
                            "root=$root;\nactivePoint=$activePoint;\n"
                )

                // Extend leaf edge offsets
                endPosition.increment()
                remainingSuffixes.increment()

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
    root: RootNode,
    private val endPosition: TextPosition,
    private val remainingSuffixes: RemainingSuffixesPointer
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
        if (activeLength == 0) {
            if (activeNode.hasEdgeWithChar(input, nextChar, 0)) {
                Debugger.printLine("Activating edge $activeEdge with leading char '$nextChar'")
                activeNode.activateEdge(input, nextChar, this)
                activeLength++
            } else {
                Debugger.printLine("Adding leaf node [$nextCharOffset, ${endPosition.value()}]($suffixOffset)")
                activeNode.addLeafEdge(
                    LeafNode(suffixOffset),
                    TextPosition(nextCharOffset),
                    endPosition
                )
                remainingSuffixes.decrement()
            }

            // We only ever enter this branch if we're either about to perform an active-edge
            // extension, or we're about to add the last suffix in the phase.
            return false
        } else {
            if (activeNode.hasEdgeWithChar(input, nextChar, activeLength)) {
                Debugger.printLine(
                    "Advancing along active edge $activeEdge " +
                            "due to next char '$nextChar' at label offset $activeLength"
                )
                activeLength++
                activeNode.advance(input, activeEdge, activeLength, this)
                return false
            } else {
                activeNode.extendEdge(
                    input,
                    activeEdge,
                    activeLength,
                    nextCharOffset,
                    suffixOffset,
                    endPosition
                )
                remainingSuffixes.decrement()
                activeEdge++
                activeLength--
                return true
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
        return "ActivePoint(activeEdge=$activeEdge, activeLength=$activeLength)"
    }

    fun advance(internalNode: InternalNode, activeEdge: Int, activeLength: Int) {
        activeNode = internalNode
        this.activeEdge = activeEdge
        this.activeLength = activeLength
    }
}

class SuffixLinkCandidate {
    private var nextNodeToLinkFrom: InternalNode? = null

    fun linkTo(internalNode: InternalNode) {
        if (nextNodeToLinkFrom != null && nextNodeToLinkFrom != internalNode) {
            nextNodeToLinkFrom!!.linkTo(internalNode)
        }
        nextNodeToLinkFrom = internalNode
    }

    fun reset() {
        nextNodeToLinkFrom = null
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
     * Performs a suffix extension in the subtree rooted at this node. It adds the String stored in
     * the parameter suffixToAdd, which has the given suffixOffset. The current pointer to the end
     * of the string is also given, because it is needed for constructing leaf nodes.
     */
    fun addSuffix(
        input: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    )

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
        charToAddOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition
    )

    fun advance(input: String, edgeSrcOffset: Int, edgeLabelOffset: Int, activePoint: ActivePoint)
}

interface DstNode {
    /**
     * @return An action which should be executed by the parent of this node, in order to
     * add the given suffixToAdd into the tree
     *
     * @param input is the string which we are building a suffix tree for
     * @param suffixToAdd is the suffix we want to add. We call this method to create an appropriate
     * SuffixExtension to use for that
     * @param suffixOffset is the offset of the suffixToAdd in the input
     * @param endPosition is the current pointer to the end of the input, which is needed to
     * construct leaf nodes
     * @param inboundEdgeDstOffset is the dstOffset of the inbound edge to this node, which is
     * needed for leaf edge extensions
     */
    fun suffixExtension(
        input: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition,
        inboundEdgeDstOffset: TextPosition
    ): SuffixExtension

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

/**
 * A suffix extension is an operation which mutates a srcNode in order to add an enclosed suffix to
 * the subtree rooted
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

    fun label(input: String) =
        input.substring(srcOffset.value(), dstOffset.value())

    /**
     * The edge decides what kind of suffix extension will be necessary in order to add the given
     * suffixToAdd into the
     * suffix tree being built for the given input.
     *
     * @return If the suffixToAdd is able to be added within this edge, or within the subtree rooted
     * at the dstNode of this edge, then it will return a suffix extension which will modify the
     * srcNode in order to perform the required extension. If this edge doesn't match the given
     * suffixToAdd, then this method will return null to indicate that there is action to be taken,
     * but not from this edge.
     *
     * @param suffixOffset is the offset of the suffixToAdd in the input
     * @param endPosition is the current pointer to the end of the input, which is needed to
     * construct leaf nodes
     */
    fun suffixExtension(
        input: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ): SuffixExtension? {
        // If the suffix won't go along this edge at all, stop and return null, because no extension
        // is needed here
        val label = label(input)
        if (label[0] != suffixToAdd[0]) {
            Debugger.printLine(
                "Suffix '$suffixToAdd' won't go along the edge starting at offset '$srcOffset'. " +
                        "No applicable suffix extension will be created."
            )
            return null
        }

        // If the suffix is implicitly contained within the edge label already, do nothing
        // If the suffix and the edge label are equal, then we also don't need to do anything. This
        // covers that case.
        if (label.startsWith(suffixToAdd)) {
            Debugger.printLine("Suffix '$suffixToAdd' is implicitly contained within the edge label '$label'")
            return SuffixExtension { }
        }

        // If this edge goes to a leaf, and the suffix would extend the current edge label by adding
        // more characters to the end, then extend the edge label. Alternatively, if this edge goes
        // to an internal node, and the suffix would extend the current edge label by adding more
        // characters to the end, then recurse to the internal node
        if (suffixToAdd.startsWith(label)) {
            val recursingSuffix = suffixToAdd.substring(label.length)
            Debugger.printLine(
                "Suffix '$suffixToAdd' extends the current edge label '$label'. " +
                        "Recursing with suffix '$recursingSuffix'."
            )
            return dstNode.suffixExtension(
                input,
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
        val fullMatchingPrefix =
            matchLength == label.length || matchLength == suffixToAdd.length
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

                // This calculation requires some explanation:
                // We know the length of the suffixToAdd
                // We know the length of the matching prefix between suffixToAdd and the internal edge label
                // From this, we can determine the number of characters in the leaf edge label
                // We also know the index of the end of the string, from endPosition
                // This therefore gives us the srcOffset as the difference of these two values.
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
            Debugger.printLine(
                "Query string '$queryString' won't go along the edge starting at offset '$srcOffset'. " +
                        "This edge won't be queried any further for offsets."
            )
            return null
        }

        // If the query string goes along this edge but doesn't get all the way to the end, or only
        // just gets to the end, then return the suffixOffsets of all the leaves of the subtree
        // rooted at the dstNode of this edge.
        if (label.startsWith(queryString)) {
            Debugger.printLine(
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
            Debugger.printLine(
                "The query string '$queryString' starts with the edge label '$label'. " +
                        "Recursing to the dstNode of this edge, with the shortened query string '$recursedQueryString'."
            )
            return dstNode.offsetsOf(input, recursedQueryString)
        }

        // The only remaining case is a strictly partial match, in which the query string 'falls
        // off' the tree in the middle of the edge label. In this case, there will be no query
        // matches, so we return an empty collection.
        Debugger.printLine(
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

    fun split(
        charToAddOffset: Int,
        edgeLabelOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition
    ): InternalNode {
        // Remove the edge that is being replaced (this edge)
        srcNode.deleteEdge(srcOffset)
        // Add an internal edge for the new internal node
        val internalNode = InternalNode()
        val dstOffsetOfSrcNode = TextPosition(srcOffset.value() + edgeLabelOffset)
        Debugger.printLine(
            "Splitting to create a new internal edge of " +
                    "${srcOffset.value()} -> ${dstOffsetOfSrcNode.value()}"
        )
        srcNode.addInternalEdge(internalNode, srcOffset, dstOffsetOfSrcNode)
        // Preserve the existing edge
        Debugger.printLine(
            "Preserving dstNode under new edge ${dstOffsetOfSrcNode.value()} -> ${dstOffset.value()}"
        )
        dstNode.addAsDstOf(internalNode, dstOffsetOfSrcNode, dstOffset)
        // Add the new leaf edge
        Debugger.printLine(
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

    fun addLeafNodeToChild(suffixOffset: Int, charToAddOffset: Int, endPosition: TextPosition) {
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
        Debugger.printLine(
            "Jumped over a node - updating active edge from ($edgeSrcOffset, $edgeLabelOffset) " +
                    "to (${edgeSrcOffset + edgeLength}, ${edgeLabelOffset - edgeLength})"
        )
        activePoint.advance(
            dstNode as InternalNode,
            edgeSrcOffset + edgeLength,
            edgeLabelOffset - edgeLength
        )
    }
}

open class DelegateSrcNode : ActiveNode {
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

    override fun deleteEdge(srcOffset: TextPosition) {
        edges.removeIf { it.hasSrcOffset(srcOffset) }
    }

    override fun addSuffix(
        input: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        for (edge in edges) {
            val suffixExtension =
                edge.suffixExtension(input, suffixToAdd, suffixOffset, endPosition)
            if (suffixExtension != null) {
                suffixExtension.extend(this)
                return
            }
        }
        val srcOffset: Int = endPosition.value() - suffixToAdd.length
        Debugger.printLine(
            "Adding leaf with suffixOffset $suffixOffset " +
                    "and offsets $srcOffset, ${endPosition.value()}"
        )
        addLeafEdge(LeafNode(suffixOffset), TextPosition(srcOffset), endPosition)
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
        charToAddOffset: Int,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        val activeEdge = edges.first { it.labelHasChar(input, input[edgeSrcOffset], 0) }
        if (edgeLabelOffset == activeEdge.labelLength()) {
            activeEdge.addLeafNodeToChild(suffixOffset, charToAddOffset, endPosition)
        } else {
            activeEdge.split(charToAddOffset, edgeLabelOffset, suffixOffset, endPosition)
        }
    }

    override fun advance(
        input: String,
        edgeSrcOffset: Int,
        edgeLabelOffset: Int,
        activePoint: ActivePoint
    ) {
        val activeEdge = edges.first { it.labelHasChar(input, input[edgeSrcOffset], 0) }
        if (edgeLabelOffset > activeEdge.labelLength()) {
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
}

class InternalNode : DelegateSrcNode(), DstNode {
    private var suffixLink: InternalNode? = null

    override fun toString(): String {
        return "InternalNode(${super.toString()})"
    }

    // Recall that this method returns a suffix extension to apply to a given srcNode in order to
    // add the given suffixToAdd into the subtree rooted the srcNode.
    override fun suffixExtension(
        input: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition,
        inboundEdgeDstOffset: TextPosition
    ): SuffixExtension {
        return SuffixExtension {
            addSuffix(
                input,
                suffixToAdd,
                suffixOffset,
                endPosition
            )
        }
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
}

class LeafNode(private val suffixOffset: Int) : DstNode {

    override fun suffixExtension(
        input: String,
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
    fun enabledFor(function: () -> Unit) {
        val wasEnabled = enabled
        enable()
        function.invoke()
        enabled = wasEnabled
    }
}