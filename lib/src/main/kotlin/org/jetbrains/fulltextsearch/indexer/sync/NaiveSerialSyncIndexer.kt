package org.jetbrains.fulltextsearch.indexer.sync

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.NaiveIndexedFile
import java.io.File

class NaiveSerialSyncIndexer : SyncIndexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener
    ): IndexedDirectory {
        return IndexedDirectory(
            directory.forEachFile {
                val indexedFile = buildIndex(directory, it)
                indexingProgressListener.onNewFileIndexed(indexedFile)
                indexedFile
            })
    }

    private fun buildIndex(root: Directory, file: File): NaiveIndexedFile {
        return NaiveIndexedFile(root.relativePathTo(file.path), file.readText())
    }
}