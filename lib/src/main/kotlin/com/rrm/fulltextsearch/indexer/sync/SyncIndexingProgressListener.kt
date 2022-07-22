package com.rrm.fulltextsearch.indexer.sync

import com.rrm.fulltextsearch.index.IndexedFile

interface SyncIndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    class DoNothing : SyncIndexingProgressListener {
        override fun onNewFileIndexed(indexedFile: IndexedFile) {
        }
    }
}