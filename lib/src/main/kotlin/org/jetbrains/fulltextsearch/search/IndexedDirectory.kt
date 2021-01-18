package org.jetbrains.fulltextsearch.search

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.index.IndexedFile

class IndexedDirectory(private val indexedFiles: List<IndexedFile>) {
    fun queryCaseSensitive(s: String): List<QueryMatch> {
        val collector = QueryMatchListener.MatchCollector()
        runBlocking { queryCaseSensitivesAsync(s, collector) }
        return collector.matches()
    }

    suspend fun queryCaseSensitivesAsync(
        s: String,
        listener: QueryMatchListener
    ): Unit = coroutineScope {
        indexedFiles.forEach { indexedFile ->
            launch {
                val matches = indexedFile.query(s)
                if (matches.isNotEmpty()) {
                    listener.onQueryMatches(matches)
                }
            }
        }
    }
}
