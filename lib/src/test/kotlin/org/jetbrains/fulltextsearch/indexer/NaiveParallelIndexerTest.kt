package org.jetbrains.fulltextsearch.indexer

class NaiveParallelIndexerTest : FullTextSearchTest() {
    override fun indexerUnderTest() = NaiveParallelIndexer()
}