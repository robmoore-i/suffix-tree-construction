package com.rrm.fulltextsearch.indexer.sync

import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.search.IndexedDirectory

class SerialSyncIndexer(
    private val indexerStrategy: IndexerStrategy = IndexerStrategy.default()
) : SyncIndexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener
    ): IndexedDirectory {
        return IndexedDirectory(
            directory.forEachFile {
                val indexedFile = indexerStrategy.buildIndexFor(directory, it)
                indexingProgressListener.onNewFileIndexed(indexedFile)
                indexedFile
            })
    }
}