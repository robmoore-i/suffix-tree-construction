package org.jetbrains.fulltextsearch.indexer.async

class NaiveParallelAsyncIndexerTest : AsyncFullTextSearchTest() {
    override fun indexerUnderTest(): AsyncIndexer = NaiveParallelAsyncIndexer()
}