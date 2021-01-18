package org.jetbrains.fulltextsearch.indexer.sync

class ParallelSyncIndexerTest : SyncFullTextSearchTest() {
    override fun indexerUnderTest() = ParallelSyncIndexer()
}