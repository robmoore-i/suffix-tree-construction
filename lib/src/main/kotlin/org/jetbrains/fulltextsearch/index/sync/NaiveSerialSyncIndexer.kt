package org.jetbrains.fulltextsearch.index.sync

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
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

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}