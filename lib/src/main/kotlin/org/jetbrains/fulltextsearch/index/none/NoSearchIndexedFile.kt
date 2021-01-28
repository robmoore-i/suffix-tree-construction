package org.jetbrains.fulltextsearch.index.none

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch

/**
 * This type of IndexedFile builds no index, and queries on it return no results.
 */
class NoSearchIndexedFile(private val relativePath: String) : IndexedFile {
    override fun query(queryString: String): List<QueryMatch> {
        return listOf()
    }

    override fun path(): String {
        return relativePath
    }
}
