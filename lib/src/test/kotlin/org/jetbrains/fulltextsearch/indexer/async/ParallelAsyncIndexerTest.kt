package org.jetbrains.fulltextsearch.indexer.async

class ParallelAsyncIndexerTest : AsyncFullTextSearchTest() {
    override fun indexerUnderTest(): AsyncIndexer = ParallelAsyncIndexer()
}