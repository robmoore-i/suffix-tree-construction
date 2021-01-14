package org.jetbrains.fulltextsearch.index.sync

import org.jetbrains.fulltextsearch.IndexedFile

interface SyncIndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    class DoNothing : SyncIndexingProgressListener {
        override fun onNewFileIndexed(indexedFile: IndexedFile) {
        }
    }
}