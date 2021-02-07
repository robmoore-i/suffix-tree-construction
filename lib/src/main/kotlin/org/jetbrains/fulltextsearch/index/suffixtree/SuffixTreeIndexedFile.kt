package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch

class SuffixTreeIndexedFile(
    private val relativePath: String, private val fileText: String
) : IndexedFile {

    private val suffixTree: SuffixTree = SuffixTree.ukkonenConstruction(fileText)

    override fun relativePath(): String = relativePath

    override fun query(queryString: String): List<QueryMatch> {
        if (queryString.isEmpty()) {
            return listOf()
        }
        return suffixTree.offsetsOf(queryString).map { QueryMatch(relativePath, it) }
    }

    override fun getLineOfChar(offset: Int): String {
        // Scan to previous line break
        var i = offset
        while (i > 0 && fileText[i - 1] != '\n') {
            i--
        }
        val substringStart = i
        i = offset
        // Scan to next line break
        while (i < fileText.length && fileText[i] != '\n') {
            i++
        }
        val substringEnd = i
        // Return the substring
        return fileText.substring(substringStart, substringEnd)
    }
}