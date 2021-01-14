package org.jetbrains.fulltextsearch.index.async

import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.IndexedFile

interface AsyncIndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    fun onIndexingCompleted(indexedDirectory: IndexedDirectory)
}