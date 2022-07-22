package com.rrm.fulltextsearch.indexer.sync

import com.rrm.fulltextsearch.indexer.async.ParallelAsyncIndexer

class ParallelAsyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = ParallelAsyncIndexer()
}