package org.jetbrains.fulltextsearch.index.async

import kotlinx.coroutines.Job
import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.index.IndexingProgressListener

interface AsyncIndexer {
    suspend fun buildIndexAsync(
        directory: Directory,
        indexingProgressListener: IndexingProgressListener
    ): Job

    companion object {
        fun default(): AsyncIndexer {
            return NaiveParallelAsyncIndexer()
        }
    }
}