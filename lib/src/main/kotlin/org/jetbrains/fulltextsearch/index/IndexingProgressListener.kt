package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory

interface IndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    fun onIndexingCompleted(indexedDirectory: IndexedDirectory)

    class DoNothing : IndexingProgressListener {
        override fun onNewFileIndexed(indexedFile: IndexedFile) {
        }

        override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
        }
    }
}