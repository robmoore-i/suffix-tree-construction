package org.jetbrains.fulltextsearch.index.async

class NaiveParallelAsyncIndexerTest : AsyncFullTextSearchTest() {
    override fun indexerUnderTest(): AsyncIndexer = NaiveParallelAsyncIndexer()
}