package com.rrm.fulltextsearch.indexer.sync

class SerialSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = SerialSyncIndexer()
}