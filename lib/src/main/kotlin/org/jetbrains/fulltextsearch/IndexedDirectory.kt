package org.jetbrains.fulltextsearch

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Collections.synchronizedList

class IndexedDirectory(private val indexedFiles: List<IndexedFile>) {
    fun queryCaseSensitive(s: String): List<QueryMatch> {
        val queryMatches = synchronizedList(mutableListOf<QueryMatch>())
        runBlocking {
            indexedFiles.forEach { indexedFile ->
                launch {
                    queryMatches.addAll(indexedFile.query(s))
                }
            }
        }
        return queryMatches
    }
}
