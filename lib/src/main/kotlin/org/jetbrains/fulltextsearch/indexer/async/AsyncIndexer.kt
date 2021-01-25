package org.jetbrains.fulltextsearch.indexer.async

import kotlinx.coroutines.Job
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy

interface AsyncIndexer {
    suspend fun buildIndexAsync(
        directory: Directory,
        indexingProgressListener: AsyncIndexingProgressListener
    ): Job

    companion object {
        fun default(indexerStrategy: IndexerStrategy = IndexerStrategy.default()): AsyncIndexer {
            return ParallelAsyncIndexer(indexerStrategy = indexerStrategy)
        }
    }
}