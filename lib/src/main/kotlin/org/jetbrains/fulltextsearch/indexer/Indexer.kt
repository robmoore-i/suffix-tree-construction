package org.jetbrains.fulltextsearch.indexer

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedDirectory

interface Indexer {
    fun buildIndex(directory: Directory): IndexedDirectory

    companion object {
        fun defaultIndexer(): Indexer {
            return NaiveParallelIndexer()
        }
    }
}
