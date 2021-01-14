package org.jetbrains.fulltextsearch.index.async

import kotlinx.coroutines.Job
import org.jetbrains.fulltextsearch.filesystem.Directory

interface AsyncIndexer {
    suspend fun buildIndexAsync(
        directory: Directory,
        indexingProgressListener: AsyncIndexingProgressListener
    ): Job

    companion object {
        fun default(): AsyncIndexer {
            return NaiveParallelAsyncIndexer()
        }
    }
}