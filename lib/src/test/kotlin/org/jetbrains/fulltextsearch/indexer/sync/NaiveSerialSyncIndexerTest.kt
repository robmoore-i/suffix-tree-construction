package org.jetbrains.fulltextsearch.indexer.sync

class NaiveSerialSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = NaiveSerialSyncIndexer()
}