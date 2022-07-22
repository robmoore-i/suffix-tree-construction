package com.rrm.fulltextsearch.index

import com.rrm.fulltextsearch.search.QueryMatch

interface IndexedFile {
    fun relativePath(): String

    fun query(queryString: String): List<QueryMatch>

    fun getLineOfChar(charOffset: Int): String
}