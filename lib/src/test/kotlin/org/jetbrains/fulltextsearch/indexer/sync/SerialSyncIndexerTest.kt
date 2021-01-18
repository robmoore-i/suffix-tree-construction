package org.jetbrains.fulltextsearch.indexer.sync

class SerialSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = SerialSyncIndexer()
}