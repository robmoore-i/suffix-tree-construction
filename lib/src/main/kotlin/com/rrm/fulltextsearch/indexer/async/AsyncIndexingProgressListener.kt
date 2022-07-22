package com.rrm.fulltextsearch.indexer.async

import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.search.IndexedDirectory

interface AsyncIndexingProgressListener {
    fun onNewFileIndexed(indexedFile: IndexedFile)

    fun onIndexingCompleted(indexedDirectory: IndexedDirectory)
}