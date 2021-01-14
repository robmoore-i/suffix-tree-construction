package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.io.File

class NaiveSerialIndexer : Indexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: IndexingProgressListener
    ): IndexedDirectory {
        return IndexedDirectory(
            directory.forEachFile {
                val indexedFile = buildIndex(directory, it)
                indexingProgressListener.onNewFileIndexed(it)
                indexedFile
            })
    }

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}