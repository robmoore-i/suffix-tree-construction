package org.jetbrains.fulltextsearch.index.sync

class NaiveParallelSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = NaiveParallelSyncIndexer()
}