package org.jetbrains.fulltextsearch.indexer

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedDirectory
import org.jetbrains.fulltextsearch.IndexedFile
import java.io.File

class NaiveSerialIndexer : Indexer {
    override fun buildIndex(directory: Directory): IndexedDirectory {
        return IndexedDirectory(
            directory.forEachFile { buildIndex(directory, it) })
    }

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}