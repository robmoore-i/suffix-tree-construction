package org.jetbrains.fulltextsearch.search

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.index.IndexedFile

class IndexedDirectory(private val indexedFiles: List<IndexedFile>) {
    fun queryCaseSensitive(s: String): List<QueryMatch> {
        val collector = QueryMatchListener.MatchCollector()
        runBlocking { queryCaseSensitiveAsync(s, collector) }
        return collector.matches()
    }

    suspend fun queryCaseSensitiveAsync(
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

    fun correspondingFileLine(queryMatch: QueryMatch): String {
        return indexedFiles.first { it.relativePath() == queryMatch.fileRelativePath }
            .getLineOfChar(queryMatch.offset)
    }
}
