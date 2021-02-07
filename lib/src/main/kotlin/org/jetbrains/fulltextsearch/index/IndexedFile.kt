package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.search.QueryMatch

interface IndexedFile {
    fun relativePath(): String

    fun query(queryString: String): List<QueryMatch>

    fun getLineOfChar(charOffset: Int): String
}