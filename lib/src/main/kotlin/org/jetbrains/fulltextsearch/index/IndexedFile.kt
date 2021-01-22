package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.search.QueryMatch

interface IndexedFile {
    fun query(queryString: String): List<QueryMatch>

    fun path(): String
}