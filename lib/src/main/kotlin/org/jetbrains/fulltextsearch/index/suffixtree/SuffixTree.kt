package org.jetbrains.fulltextsearch.index.suffixtree

class SuffixTree(inputString: String) {
    private val root: RootNode = RootNode()

    // We append a terminating character to the inputString in order to ensure that we get a true
    // suffix tree at the end of the process, rather than an implicit suffix tree.
    private val terminatedInputString = inputString + terminatingCharacter()

    init {
        // PHASE 1 EXTENSION 1
        val endPosition = TextPosition(1)
        root.addLeafEdge(LeafNode(endPosition.value() - 1), TextPosition(0), endPosition)

        var remainingSuffixes = 0
        val activePoint =
            ActivePoint(terminatedInputString, root, SuffixLinkCandidate(), endPosition)

        (2..terminatedInputString.length).forEach { phaseNumber ->
            Debugger.enableIf { true }
            Debugger.printLine(
                "\nStarting phase $phaseNumber for character '${terminatedInputString[phaseNumber - 1]}'\n" +
                        "There are $remainingSuffixes suffixes remaining.\n" +
                        "root=$root;\nactivePoint=$activePoint;\n"
            )
            remainingSuffixes++

            // Extend leaf edge offsets
            endPosition.increment()

            // Suffix link candidates reset at the start of every phase.
            activePoint.resetSuffixLinkCandidate()

            // The phase ends when it is no longer possible to add more suffixes into the tree in
            // the current phase. This happens either when we run out of remaining suffixes to add,
            // i.e. when remainder == 0, or when we perform a rule three suffix extension.
            var canAddMoreSuffixes = true
            while (canAddMoreSuffixes) {
                Debugger.printLine(
                    "\nAdding suffixes. $remainingSuffixes remaining. " +
                            "Currently extending tree with suffix '${
                                terminatedInputString.substring(
                                    endPosition.value() - remainingSuffixes,
                                    endPosition.value()
                                )
                            }'\n" +
                            "root=$root;\nactivePoint=$activePoint;"
                )
                if (activePoint.isAtNode()) {
                    if (activePoint.hasEdgeStartingWith(terminatedInputString[phaseNumber - 1])) {
                        activePoint.activateEdgeStartingWith(terminatedInputString[phaseNumber - 1])
                        canAddMoreSuffixes = false
                    } else {
                        val suffixOffset = endPosition.value() - remainingSuffixes
                        Debugger.printLine(
                            "Creating new leaf node with offsets " +
                                    "${phaseNumber - 1} -> ${endPosition.value()} " +
                                    "for suffix with offset $suffixOffset"
                        )
                        activePoint.addLeafEdge(
                            LeafNode(suffixOffset),
                            TextPosition(phaseNumber - 1)
                        )
                        remainingSuffixes--

                        // TODO: Duplicated
                        if (!activePoint.activeNodeIsRoot()) {
                            activePoint.followSuffixLink(remainingSuffixes)
                        } else if (activePoint.activeNodeIsRoot() && !activePoint.isAtNode()) {
                            activePoint.nextEdge()
                        }
                    }
                } else {
                    if (activePoint.labelHasNextCharacter(terminatedInputString[phaseNumber - 1])) {
                        Debugger.printLine(
                            "Advancing further down the current active edge of the active point $this;\n" +
                                    "This ends the current phase."
                        )
                        activePoint.advanceDownLabel()
                        canAddMoreSuffixes = false
                    } else {
                        val suffixOffset = endPosition.value() - remainingSuffixes
                        Debugger.printLine(
                            "Splitting at the active point $activePoint; " +
                                    "in order to add the character '${terminatedInputString[phaseNumber - 1]}'."
                        )
                        activePoint.split(phaseNumber - 1, suffixOffset)
                        remainingSuffixes--

                        // TODO: Duplicated
                        if (!activePoint.activeNodeIsRoot()) {
                            activePoint.followSuffixLink(remainingSuffixes)
                        } else if (activePoint.activeNodeIsRoot() && !activePoint.isAtNode()) {
                            activePoint.nextEdge()
                        }
                    }
                }
                if (remainingSuffixes == 0) {
                    Debugger.printLine("No more suffixes to add. Ending the current phase.")
                    canAddMoreSuffixes = false
                }
            }

//            (2..phaseNumber).forEach { extensionNumber ->
//                // PHASE i EXTENSION j
//                val debugAlwaysEnabled = false
//                val debugEnabled = false
//                if (debugAlwaysEnabled || (debugEnabled && phaseNumber == 7 && extensionNumber == 6)) {
//                    Debugger.enable()
//                } else {
//                    Debugger.disable()
//                }
//
//                val suffixOffset = extensionNumber - 1
//                val suffixToAdd = terminatedInputString.substring(suffixOffset, phaseNumber)
//                Debugger.printLine("\nPhase $phaseNumber, extension $extensionNumber")
//                Debugger.printLine("Adding string '$suffixToAdd'")
//                root.addSuffix(terminatedInputString, suffixToAdd, suffixOffset, endPosition)
//                Debugger.printLine("Root node: $root")
//            }
        }
    }

    override fun toString(): String {
        return "SuffixTree(root:$root)"
    }

    fun offsetsOf(queryString: String): Set<Int> {
        return root.offsetsOf(terminatedInputString, queryString)
    }

    private fun terminatingCharacter(): Char {
        return '\u0000'
    }
}

class ActivePoint(
    private val inputString: String,
    private val root: RootNode,
    private val suffixLinkCandidate: SuffixLinkCandidate,
    private val endPosition: TextPosition
) {
    // The node from which we are performing the next O(1) step of the algorithm.
    private var activeNode: SrcNode = root

    // Identifies the edge on the activeNode.
    private var activeEdge = -1

    // The number of characters of edge label we need to go over to find the active point.
    private var activeLength = 0

    private var edge: Edge? = null

    override fun toString(): String {
        val values =
            "ActivePoint(activeNode=$activeNode,\nactiveEdge=$activeEdge,activeLength=$activeLength," +
                    "edge=$edge"
        return if (activeEdge >= 0) {
            "$values,representation=${
                inputString.substring(
                    activeEdge,
                    activeEdge + activeLength
                )
            })"
        } else {
            "$values)"
        }
    }

    fun offset() = activeEdge

    fun nextEdge() {
        activeEdge++
        activeLength--
        if (activeLength > 0) {
            Debugger.printLine("Reactivating edge for new activeEdge=$activeEdge and activeLength=$activeLength.")
            activeNode.reactivateEdge(inputString, this)
            if (reachedEndOfEdge()) {
                Debugger.printLine(
                    "Reached an internal node after advancing the active edge pointer. " +
                            "Advancing the activeNode."
                )
                advanceActiveNode()
            }
        }
    }

    fun setEdge(edge: Edge, edgeSrcOffset: Int) {
        this.edge = edge
        this.activeEdge = edgeSrcOffset
    }

    fun resetEdge(edge: Edge) {
        this.edge = edge
    }

    fun labelHasNextCharacter(c: Char): Boolean {
        return edge!!.labelHasCharacter(inputString, c, activeLength)
    }

    fun split(charToAddOffset: Int, suffixOffset: Int) {
        val newInternalNode = edge!!.split(charToAddOffset, activeLength, suffixOffset, endPosition)
        suffixLinkCandidate.linkTo(newInternalNode)
    }

    fun isAtNode(): Boolean {
        return activeLength == 0
    }

    fun advanceDownLabel() {
        activeLength++
        if (reachedEndOfEdge()) {
            Debugger.printLine("Reached the end of the current activeEdge. Advancing the activeNode.")
            advanceActiveNode()
        }
    }

    // Only call this method if you're sure that labelOffset > 0
    private fun reachedEndOfEdge(): Boolean {
        val labelLength = edge!!.labelLength()
        Debugger.printLine(
            "Checking if we reached the end of the current active edge. " +
                    "activeEdge=$activeEdge,activeLength=$activeLength,edgeLabelLength=$labelLength;"
        )
        return activeLength >= labelLength
    }

    private fun advanceActiveNode() {
        val edge = this.edge!!
        val labelLength = edge.labelLength()
        Debugger.printLine(
            "Advancing active node by doing these things:\n" +
                    "- Advancing the active edge offset: $activeEdge + $labelLength => ${activeEdge + labelLength}\n" +
                    "- Reducing the active edge label offset: $activeLength - $labelLength => ${activeLength - labelLength}\n" +
                    "- Setting the active node to ${edge.dstNode()}"
        )
        activeEdge += labelLength
        activeLength -= labelLength
        activeNode = edge.dstNode() as InternalNode
        if (activeLength == 0) {
            this.edge = null
        } else {
            Debugger.printLine("After advancing the active node, we need to again reset the active edge.")
            activeNode.reactivateEdge(inputString, this)
        }
    }

    fun hasEdgeStartingWith(c: Char): Boolean {
        return activeNode.hasEdgeStartingWith(inputString, c)
    }

    fun activateEdgeStartingWith(c: Char) {
        activeNode.activateEdge(inputString, c, this)
        Debugger.printLine(
            "Advancing down the start of the edge with activeEdge=$activeEdge, corresponding to '${inputString[activeEdge]}' " +
                    "and current activeLength=$activeLength. This ends the current phase."
        )
        advanceDownLabel()
    }

    fun addLeafEdge(leafNode: LeafNode, newEdgeSrcOffset: TextPosition) {
        activeNode.addLeafEdge(leafNode, newEdgeSrcOffset, endPosition)
    }

    fun activeNodeIsRoot(): Boolean {
        return activeNode is RootNode
    }

    fun followSuffixLink(remainingSuffixes: Int) {
        val suffixLink: SrcNode = (activeNode as InternalNode).followLinkOrGoToRoot(root)
        Debugger.printLine("Following suffixLink FROM $activeNode;\nTO $suffixLink;")
        activeNode = suffixLink
        if (activeNodeIsRoot() && remainingSuffixes > 1) {
            activeLength = remainingSuffixes - 1
            activeEdge = endPosition.value() - remainingSuffixes
            Debugger.printLine(
                "After resetting to root, we update activeLength to $activeLength and " +
                        "activeEdge to $activeEdge."
            )
        }
        if (activeLength > 0) {
            activeNode.reactivateEdge(inputString, this)
            if (reachedEndOfEdge()) {
                Debugger.printLine(
                    "After following the suffixLink, the new active edge has been hopped over. " +
                            "Advancing the activeNode."
                )
                advanceActiveNode()
            }
        }
    }

    fun resetSuffixLinkCandidate() {
        suffixLinkCandidate.reset()
    }
}

class SuffixLinkCandidate {
    private var nextNodeToLinkFrom: InternalNode? = null

    fun linkTo(internalNode: InternalNode) {
        if (nextNodeToLinkFrom != null && nextNodeToLinkFrom != internalNode) {
            Debugger.printLine("Creating a suffix link FROM $nextNodeToLinkFrom;\nTO $internalNode;")
            nextNodeToLinkFrom!!.linkTo(internalNode)
        }
        nextNodeToLinkFrom = internalNode
        Debugger.printLine("Next suffix link candidate is $nextNodeToLinkFrom;")
    }

    fun reset() {
        Debugger.printLine("Resetting suffix tree candidate")
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
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    )

    /**
     * @return The set of all offsets into the inputString which correspond to matches of the
     * queryString.
     */
    fun offsetsOf(inputString: String, queryString: String): Set<Int>

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at this node.
     */
    fun descendentSuffixOffsets(): Set<Int>

    fun hasEdgeStartingWith(inputString: String, c: Char): Boolean

    fun activateEdge(inputString: String, edgeLeadingCharacter: Char, activePoint: ActivePoint)

    fun reactivateEdge(inputString: String, activePoint: ActivePoint)
}

interface DstNode {
    /**
     * @return An action which should be executed by the parent of this node, in order to
     * add the given suffixToAdd into the tree
     *
     * @param inputString is the string which we are building a suffix tree for
     * @param suffixToAdd is the suffix we want to add. We call this method to create an appropriate
     * SuffixExtension to use for that
     * @param suffixOffset is the offset of the suffixToAdd in the inputString
     * @param endPosition is the current pointer to the end of the input, which is needed to
     * construct leaf nodes
     * @param inboundEdgeDstOffset is the dstOffset of the inbound edge to this node, which is
     * needed for leaf edge extensions
     */
    fun suffixExtension(
        inputString: String,
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
     * @return The set of all offsets into the inputString which correspond to matches of the
     * queryString.
     */
    fun offsetsOf(inputString: String, queryString: String): Set<Int>

    /**
     * @return The set of suffix offsets in all the leaf nodes in the subtree rooted at this node.
     */
    fun descendentSuffixOffsets(): Set<Int>
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

    private fun label(inputString: String) =
        inputString.substring(srcOffset.value(), dstOffset.value())

    /**
     * The edge decides what kind of suffix extension will be necessary in order to add the given
     * suffixToAdd into the
     * suffix tree being built for the given inputString.
     *
     * @return If the suffixToAdd is able to be added within this edge, or within the subtree rooted
     * at the dstNode of this edge, then it will return a suffix extension which will modify the
     * srcNode in order to perform the required extension. If this edge doesn't match the given
     * suffixToAdd, then this method will return null to indicate that there is action to be taken,
     * but not from this edge.
     *
     * @param suffixOffset is the offset of the suffixToAdd in the inputString
     * @param endPosition is the current pointer to the end of the input, which is needed to
     * construct leaf nodes
     */
    fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ): SuffixExtension? {
        // If the suffix won't go along this edge at all, stop and return null, because no extension
        // is needed here
        val label = label(inputString)
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
     * dstNode of this edge, then it will return a set of offsets into the inputString which
     * correspond to instances of the queryString. Otherwise, if this edge doesn't match the
     * queryString, it will return null to indicate that the caller should check the other edges of
     * the dstNode, or they may have to conclude that the search was unsuccessful.
     */
    fun offsetsOf(inputString: String, queryString: String): Set<Int>? {
        // If the query string won't go along this edge at all, stop and return null, because no
        // extension is needed here.
        val label = label(inputString)
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
            return dstNode.offsetsOf(inputString, recursedQueryString)
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

    fun labelStartsWith(inputString: String, c: Char): Boolean {
        return inputString[srcOffset.value()] == c
    }

    fun labelHasCharacter(inputString: String, c: Char, edgeLabelOffset: Int): Boolean {
        return label(inputString)[edgeLabelOffset] == c
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
        srcNode.addInternalEdge(internalNode, srcOffset, dstOffsetOfSrcNode)
        // Preserve the existing edge
        Debugger.printLine("Preserving dstNode $dstNode; of edge $this")
        dstNode.addAsDstOf(internalNode, dstOffsetOfSrcNode, endPosition)
        // Add the new leaf edge
        Debugger.printLine(
            "srcOffset=${srcOffset.value()}, charToAddOffset=$charToAddOffset, " +
                    "endPosition=${endPosition.value()}"
        )

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

    fun dstNode() = dstNode

    fun labelLength(): Int {
        return (dstOffset - srcOffset).value()
    }

    fun activate(activePoint: ActivePoint) {
        activePoint.setEdge(this, srcOffset.value())
    }

    fun reactivate(activePoint: ActivePoint) {
        activePoint.resetEdge(this)
    }
}

class DelegateSrcNode : SrcNode {
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

    override fun addSuffix(
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
        val srcOffset: Int = endPosition.value() - suffixToAdd.length
        Debugger.printLine(
            "Adding leaf with suffixOffset $suffixOffset " +
                    "and offsets $srcOffset, ${endPosition.value()}"
        )
        addLeafEdge(LeafNode(suffixOffset), TextPosition(srcOffset), endPosition)
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

    override fun hasEdgeStartingWith(inputString: String, c: Char): Boolean {
        return edges.any { it.labelStartsWith(inputString, c) }
    }

    override fun activateEdge(
        inputString: String,
        edgeLeadingCharacter: Char,
        activePoint: ActivePoint
    ) {
        edges.first { it.labelStartsWith(inputString, edgeLeadingCharacter) }
            .activate(activePoint)
    }

    override fun reactivateEdge(inputString: String, activePoint: ActivePoint) {
        val edgeLeadingChar = inputString[activePoint.offset()]
        Debugger.printLine("Reactivating active edge for leading character '${edgeLeadingChar}' at node $this;")
        edges.first { it.labelStartsWith(inputString, edgeLeadingChar) }
            .reactivate(activePoint)
    }

    override fun toString(): String {
        return "DelegateSrcNode(edges=${edges.joinToString { "\n${it}" }})"
    }
}

class RootNode : SrcNode {
    private val srcNode: SrcNode = DelegateSrcNode()

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

    override fun addSuffix(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        srcNode.addSuffix(inputString, suffixToAdd, suffixOffset, endPosition)
    }

    override fun offsetsOf(inputString: String, queryString: String): Set<Int> {
        return srcNode.offsetsOf(inputString, queryString)
    }

    override fun descendentSuffixOffsets(): Set<Int> {
        return srcNode.descendentSuffixOffsets()
    }

    override fun hasEdgeStartingWith(inputString: String, c: Char): Boolean {
        return srcNode.hasEdgeStartingWith(inputString, c)
    }

    override fun activateEdge(
        inputString: String,
        edgeLeadingCharacter: Char,
        activePoint: ActivePoint
    ) {
        srcNode.activateEdge(inputString, edgeLeadingCharacter, activePoint)
    }

    override fun reactivateEdge(inputString: String, activePoint: ActivePoint) {
        srcNode.reactivateEdge(inputString, activePoint)
    }

    override fun toString(): String {
        return "RootNode(srcNode=$srcNode)"
    }
}

class InternalNode : SrcNode, DstNode {
    private val srcNode: SrcNode = DelegateSrcNode()
    private var suffixLink: InternalNode? = null

    override fun toString(): String {
        return "InternalNode(srcNode=$srcNode)"
    }

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
    override fun addSuffix(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition
    ) {
        srcNode.addSuffix(inputString, suffixToAdd, suffixOffset, endPosition)
    }

    // Recall that this method returns a suffix extension to apply to a given srcNode in order to
    // add the given suffixToAdd into the subtree rooted the srcNode.
    override fun suffixExtension(
        inputString: String,
        suffixToAdd: String,
        suffixOffset: Int,
        endPosition: TextPosition,
        inboundEdgeDstOffset: TextPosition
    ): SuffixExtension {
        return SuffixExtension { addSuffix(inputString, suffixToAdd, suffixOffset, endPosition) }
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

    override fun hasEdgeStartingWith(inputString: String, c: Char): Boolean {
        return srcNode.hasEdgeStartingWith(inputString, c)
    }

    override fun activateEdge(
        inputString: String,
        edgeLeadingCharacter: Char,
        activePoint: ActivePoint
    ) {
        srcNode.activateEdge(inputString, edgeLeadingCharacter, activePoint)
    }

    override fun reactivateEdge(inputString: String, activePoint: ActivePoint) {
        srcNode.reactivateEdge(inputString, activePoint)
    }

    fun linkTo(internalNode: InternalNode) {
        this.suffixLink = internalNode
    }

    fun followLinkOrGoToRoot(root: RootNode): SrcNode {
        return suffixLink ?: root
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
        return setOf()
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

    fun enabledFor(function: () -> Unit) {
        val wasEnabled = enabled
        enable()
        function.invoke()
        enabled = wasEnabled
    }
}