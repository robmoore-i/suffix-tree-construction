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
        return fileText.substring(maxOf(0, offset - 10), minOf(fileText.length, offset + 10))
    }
}