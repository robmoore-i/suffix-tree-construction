package org.jetbrains.fulltextsearch.indexer.sync

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.search.IndexedDirectory

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
