package org.jetbrains.fulltextsearch.indexer.sync

import org.jetbrains.fulltextsearch.index.IndexedFile

interface SyncIndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    class DoNothing : SyncIndexingProgressListener {
        override fun onNewFileIndexed(indexedFile: IndexedFile) {
        }
    }
}