package org.jetbrains.fulltextsearch.index.sync

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.index.IndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory

interface SyncIndexer {
    fun buildIndex(
        directory: Directory,
        indexingProgressListener: IndexingProgressListener = IndexingProgressListener.DoNothing()
    ): IndexedDirectory

    companion object {
        fun default(): SyncIndexer {
            return NaiveParallelSyncIndexer()
        }
    }
}
