package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.search.IndexedDirectory

interface Indexer {
    fun buildIndex(
        directory: Directory,
        indexingProgressListener: IndexingProgressListener = IndexingProgressListener.DoNothing()
    ): IndexedDirectory

    companion object {
        fun defaultIndexer(): Indexer {
            return NaiveParallelIndexer()
        }
    }
}
