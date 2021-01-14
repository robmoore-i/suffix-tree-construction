package org.jetbrains.fulltextsearch.index.async

import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory

interface AsyncIndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    fun onIndexingCompleted(indexedDirectory: IndexedDirectory)
}