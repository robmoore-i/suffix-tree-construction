package com.rrm.fulltextsearch.index.none

import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.search.QueryMatch

/**
 * This type of IndexedFile builds no index, and queries on it return no results.
 */
class NoSearchIndexedFile(private val relativePath: String) : IndexedFile {
    override fun relativePath(): String {
        return relativePath
    }

    override fun query(queryString: String): List<QueryMatch> {
        return listOf()
    }

    override fun getLineOfChar(charOffset: Int): String {
        throw UnsupportedOperationException(
            "The file '$relativePath' was not indexed, so getting the line of the character at " +
                    "offset $charOffset is not supported, because this call was probably caused by a " +
                    "bug."
        )
    }
}
