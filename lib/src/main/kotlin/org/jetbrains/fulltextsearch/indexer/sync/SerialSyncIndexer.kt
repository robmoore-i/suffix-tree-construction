package org.jetbrains.fulltextsearch.indexer.sync

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.search.IndexedDirectory

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