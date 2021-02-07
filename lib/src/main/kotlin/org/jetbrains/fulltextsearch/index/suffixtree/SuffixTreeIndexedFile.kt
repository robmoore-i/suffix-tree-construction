package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch

class SuffixTreeIndexedFile(
    private val relativePath: String, private val fileText: String
) : IndexedFile {

    private val suffixTree: SuffixTree = SuffixTree.ukkonenConstruction(fileText)
    private val lineBreakOffsets = suffixTree.offsetsOf("\n")

    override fun relativePath(): String = relativePath

    override fun query(queryString: String): List<QueryMatch> {
        if (queryString.isEmpty()) {
            return listOf()
        }
        return suffixTree.offsetsOf(queryString).map { QueryMatch(relativePath, it) }
    }

    // Current Approach:
    // - Find the smallest line break offset > char offset
    // - Find the greatest line break offset < char offset
    // Potential improvement:
    // - Sort the line break offsets, store them in an AVL tree, and exploit it like the cracker
    //   index from https://github.com/robmoore-i/AdaptiveCompression
    override fun getLineOfChar(charOffset: Int): String {
        var startOfLineOffset = -1
        var endOfLineOffset = fileText.length
        for (lineBreakOffset in lineBreakOffsets) {
            if (lineBreakOffset in (charOffset + 1) until endOfLineOffset) {
                endOfLineOffset = lineBreakOffset
            } else if (lineBreakOffset in (startOfLineOffset + 1) until charOffset) {
                startOfLineOffset = lineBreakOffset
            }
        }
        return fileText.substring(startOfLineOffset + 1, endOfLineOffset)
    }
}