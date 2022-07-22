package com.rrm.fulltextsearch.indexer.async

import kotlinx.coroutines.Job
import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.indexer.IndexerStrategy

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