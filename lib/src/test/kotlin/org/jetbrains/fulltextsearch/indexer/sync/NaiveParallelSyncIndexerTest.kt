package org.jetbrains.fulltextsearch.indexer.sync

class NaiveParallelSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = NaiveParallelSyncIndexer()
}