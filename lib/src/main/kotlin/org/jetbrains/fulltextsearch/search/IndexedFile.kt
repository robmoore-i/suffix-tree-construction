package org.jetbrains.fulltextsearch.search

interface IndexedFile {
    fun query(queryString: String): List<QueryMatch>

    fun path(): String
}