package org.jetbrains.fulltextsearch.index

class NaiveParallelIndexerTest : FullTextSearchTest() {
    override fun indexerUnderTest() = NaiveParallelIndexer()
}