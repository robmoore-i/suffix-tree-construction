package org.jetbrains.fulltextsearch.index.sync

class NaiveSerialSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = NaiveSerialSyncIndexer()
}