package org.jetbrains.fulltextsearch.indexer.sync

import org.jetbrains.fulltextsearch.indexer.async.ParallelAsyncIndexer

class ParallelAsyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = ParallelAsyncIndexer()
}