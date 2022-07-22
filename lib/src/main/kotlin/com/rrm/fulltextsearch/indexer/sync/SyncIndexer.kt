package com.rrm.fulltextsearch.indexer.sync

import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.search.IndexedDirectory

interface SyncIndexer {
    fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener = SyncIndexingProgressListener.DoNothing()
    ): IndexedDirectory

    companion object {
        fun default(): SyncIndexer {
            return ParallelSyncIndexer()
        }
    }
}
