package com.rrm.fulltextsearch.indexer.sync

class ParallelSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = ParallelSyncIndexer()
}