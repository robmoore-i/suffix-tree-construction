package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch

class SuffixTreeIndexedFile(private val path: String, fileText: String) : IndexedFile {
    private val suffixTree = SuffixTree(fileText)

    override fun query(queryString: String): List<QueryMatch> {
        return suffixTree.offsetsOf(queryString)
            .map { QueryMatch(path, it) }
    }

    override fun path(): String = path
}