package org.jetbrains.fulltextsearch.indexer.sync

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory

class SerialSyncIndexer : SyncIndexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener
    ): IndexedDirectory {
        return IndexedDirectory(
            directory.forEachFile {
                val indexedFile = IndexedFile.buildFor(directory, it)
                indexingProgressListener.onNewFileIndexed(indexedFile)
                indexedFile
            })
    }
}