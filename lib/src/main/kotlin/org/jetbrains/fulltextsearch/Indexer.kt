package org.jetbrains.fulltextsearch

import java.io.File

class Indexer {
    fun buildIndex(directory: Directory): IndexedDirectory {
        return IndexedDirectory(
            directory.forEachFile { buildIndex(directory, it) })
    }

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}
